import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/utils/request'
import { userKey } from '@/utils/storage'

/**
 * 生成 UUID v4，兼容非安全上下文（HTTP）。
 * crypto.randomUUID() 仅在 HTTPS 或 localhost 下可用。
 */
function generateUUID() {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    try {
      return crypto.randomUUID()
    } catch (_) { /* 静默回退到 polyfill */ }
  }
  // RFC 4122 v4 polyfill
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
    var r = Math.random() * 16 | 0
    var v = c === 'x' ? r : (r & 0x3 | 0x8)
    return v.toString(16)
  })
}

const getProgressKey = () => userKey('assessment_progress')

export const useAssessmentStore = defineStore('assessment', () => {
  // ========== 状态 ==========
  const currentScreen = ref('start') // 'start' | 'quiz'
  const questions = ref([])
  const currentIndex = ref(0)
  const answers = ref({})       // { questionId: selectedOptionId }
  const loading = ref(false)
  const assessmentResult = ref(null) // 评估结果，供 ResultPage 使用
  const sessionId = ref('')          // 后台生成的会话 ID，用于轮询
  const totalTarget = ref(10)        // 目标总题数
  const pollError = ref('')          // 轮询出错信息
  let pollTimer = null               // 轮询定时器

  // ========== 计算属性 ==========
  const currentQuestion = computed(() =>
    questions.value[currentIndex.value] || null
  )

  // 是否所有题目都已生成完毕
  const allQuestionsReady = computed(() =>
    questions.value.length >= totalTarget.value
  )

  const totalQuestions = computed(() => questions.value.length)

  const progressPercent = computed(() => {
    if (totalTarget.value === 0) return 0
    return Math.round(((currentIndex.value + 1) / totalTarget.value) * 100)
  })

  const isFirstQuestion = computed(() => currentIndex.value === 0)
  const isLastQuestion = computed(() =>
    currentIndex.value >= totalQuestions.value - 1
  )

  const selectedOption = computed(() => {
    if (!currentQuestion.value) return null
    return answers.value[currentQuestion.value.id] || null
  })

  /**
   * 答题回顾数据：供 ResultPage 展示每道题的作答情况。
   * 返回数组，每项包含：题号、题干、用户答案、正确答案、是否答对、解析。
   */
  const questionReview = computed(() => {
    return questions.value.map((q) => {
      const userAnswer = answers.value[q.id] || null
      const isCorrect = userAnswer === q.correctAnswer
      return {
        id: q.id,
        questionNumber: q.questionNumber || (questions.value.indexOf(q) + 1),
        passage: q.passage,
        question: q.question,
        options: q.options,
        userAnswer,
        correctAnswer: q.correctAnswer,
        isCorrect,
        explanation: q.explanation || '暂无解析',
      }
    })
  })

  // ========== CEFR → 难度映射 ==========

  /** 读取用户当前 CEFR 等级 */
  function getUserCefrLevel() {
    try {
      const userId = JSON.parse(localStorage.getItem('user') || '{}').userId
      if (userId) {
        const raw = localStorage.getItem(`aael_vocab_result_${userId}`)
        if (raw) {
          const vr = JSON.parse(raw)
          if (vr.cefrLevel) return vr.cefrLevel.toUpperCase()
        }
      }
    } catch (_) {}
    return 'B1' // 默认中级
  }

  /** CEFR 等级 → 出题难度 */
  function cefrToDifficulty(cefr) {
    const map = {
      'A1': '初中',
      'A2': '高中',
      'B1': '四级',
      'B2': '六级',
      'C1': '考研',
      'C2': '托福',
    }
    return map[cefr] || '四级'
  }

  // ========== 动作 ==========

  /**
   * 开始测评：获取首题（快速），剩余题目后台生成并轮询拉取。
   */
  async function startAssessment() {
    loading.value = true
    assessmentResult.value = null
    stopPolling()
    try {
      const { useUserStore } = await import('@/stores/user')
      const userStore = useUserStore()
      const studyPurpose = cefrToDifficulty(getUserCefrLevel())

      const data = await request.post('/assessment/generate',
        { studyPurpose },
        { timeout: 30000 }  // 首题生成约 15-20s
      )

      if (!data.success || !data.questions || data.questions.length === 0) {
        throw new Error(data.message || '生成测评题目失败')
      }

      sessionId.value = data.sessionId || ''
      totalTarget.value = data.totalTarget || 10
      questions.value = transformQuestions(data.questions)

      // 如果首题拿到但总数不足，启动轮询
      if (sessionId.value && questions.value.length < totalTarget.value) {
        startPolling()
      }
    } catch (err) {
      console.error('生成测评失败:', err)
      throw err
    } finally {
      loading.value = false
    }
    currentScreen.value = 'quiz'
    currentIndex.value = 0
    answers.value = {}
    saveProgress()
  }

  /** 将后端题目格式转为前端格式 */
  function transformQuestions(rawQuestions) {
    return rawQuestions.map((q, i) => ({
      id: generateUUID(),    // 全局唯一标识，用于错题本等跨测评去重
      questionNumber: i + 1,       // 题号（仅用于显示）
      passage: q.passage || '',
      question: q.question || '',
      options: [
        { id: 'A', text: q.optionA || '' },
        { id: 'B', text: q.optionB || '' },
        { id: 'C', text: q.optionC || '' },
        { id: 'D', text: q.optionD || '' },
      ],
      // 后端 answer 是数字索引 (0-3)，转换为选项字母 (A-D)
      correctAnswer: typeof q.answer === 'number'
        ? String.fromCharCode(65 + q.answer)
        : q.answer,
      explanation: q.explanation || '',
    }))
  }

  /** 判断 id 是否为旧版数字 ID（需迁移为 UUID） */
  function isLegacyNumericId(id) {
    return typeof id === 'number' || /^\d+$/.test(String(id))
  }

  /** 轮询获取后台生成的剩余题目 */
  let pollStartTime = 0
  const POLL_TIMEOUT_MS = 180_000  // 3 分钟总超时

  function startPolling() {
    stopPolling()
    pollStartTime = Date.now()
    pollTimer = setInterval(async () => {
      try {
        const data = await request.get('/assessment/questions', {
          params: { sessionId: sessionId.value },
        })
        if (!data.success) {
          // 会话已过期则停止轮询
          if (data.message && data.message.includes('过期')) {
            stopPolling()
            pollError.value = 'AI 出题会话已过期，请重新开始测评'
          }
          return
        }

        const newQuestions = data.questions || []
        if (newQuestions.length > questions.value.length) {
          questions.value = transformQuestions(newQuestions)
          saveProgress()
        }

        if (data.complete || questions.value.length >= totalTarget.value) {
          stopPolling()
        }

        // 超时保护
        if (Date.now() - pollStartTime > POLL_TIMEOUT_MS) {
          stopPolling()
          if (questions.value.length < totalTarget.value) {
            pollError.value = `AI 出题超时（已生成 ${questions.value.length}/${totalTarget.value} 题），请重新测评`
          }
        }
      } catch {
        // 轮询失败静默重试
      }
    }, 3000)
  }

  function stopPolling() {
    if (pollTimer) {
      clearInterval(pollTimer)
      pollTimer = null
    }
  }

  /**
   * 选择答案
   */
  function selectOption(optionId) {
    if (!currentQuestion.value) return
    answers.value = {
      ...answers.value,
      [currentQuestion.value.id]: optionId,
    }
    saveProgress()
  }

  /**
   * 下一题
   */
  function nextQuestion() {
    if (currentIndex.value < totalQuestions.value - 1) {
      currentIndex.value++
      saveProgress()
    }
  }

  /**
   * 上一题
   */
  function prevQuestion() {
    if (currentIndex.value > 0) {
      currentIndex.value--
      saveProgress()
    }
  }

  /**
   * 提交测评结果（AI 评估）
   */
  async function submitAssessment() {
    loading.value = true
    try {
      // 构造提交载荷：题目 + 用户答案
      const payload = {
        questions: questions.value.map(q => ({
          passage: q.passage,
          question: q.question,
          optionA: q.options[0]?.text || '',
          optionB: q.options[1]?.text || '',
          optionC: q.options[2]?.text || '',
          optionD: q.options[3]?.text || '',
          correctAnswer: q.correctAnswer,
          explanation: q.explanation || '',
        })),
        answers: questions.value.reduce((acc, q) => {
          const selected = answers.value[q.id]
          if (selected) acc[q.id] = selected
          return acc
        }, {}),
      }

      const data = await request.post('/assessment/evaluate',
        payload,
        { timeout: 45000 }
      )

      if (!data.success) {
        throw new Error(data.message || '评估失败')
      }

      // 存储评估结果供 ResultPage 使用
      assessmentResult.value = {
        level: data.cefrLevel || 'A1',
        nextLevel: getNextLevel(data.cefrLevel),
        score: data.overallScore || 0,
        dimensions: {
          vocabulary: data.vocabulary || 0,
          grammar: data.grammar || 0,
          reading: data.reading || 0,
          culture: data.culture || 0,
          logic: data.logic || 0,
        },
      }

      // 计算 CEFR 进度增值并保存到后端
      let leveledUp = false
      let progressGained = 0
      let scoreTooLow = false
      let isMaxLevel = false
      try {
        const progressResult = await saveCefrProgress(data.overallScore || 0, data.cefrLevel)
        if (progressResult) {
          assessmentResult.value.level = progressResult.level || data.cefrLevel || 'A1'
          assessmentResult.value.nextLevel = progressResult.nextLevel || getNextLevel(data.cefrLevel)
          assessmentResult.value.leveledUp = progressResult.leveledUp || false
          assessmentResult.value.progressGained = progressResult.progressGained || 0
          assessmentResult.value.tooLow = progressResult.tooLow || false
          assessmentResult.value.isMaxLevel = progressResult.isMaxLevel || false
          leveledUp = progressResult.leveledUp || false
          progressGained = progressResult.progressGained || 0
          scoreTooLow = progressResult.tooLow || false
          isMaxLevel = progressResult.isMaxLevel || false
        }
      } catch (_) { /* 非关键 */ }
      assessmentResult.value.leveledUp = leveledUp
      assessmentResult.value.progressGained = progressGained
      assessmentResult.value.tooLow = scoreTooLow
      assessmentResult.value.isMaxLevel = isMaxLevel

      clearProgress()
      return { success: true, result: assessmentResult.value }
    } catch (error) {
      console.error('提交测评失败:', error)
      return { success: false, error: error.message }
    } finally {
      loading.value = false
    }
  }

  /**
   * 退出测评（重置）
   */
  function resetAssessment() {
    currentScreen.value = 'start'
    questions.value = []
    currentIndex.value = 0
    answers.value = {}
    sessionId.value = ''
    stopPolling()
  }

  /**
   * 获取评估结果
   */
  function getResult() {
    return assessmentResult.value
  }

  // ========== 辅助函数 ==========

  /** 根据当前 CEFR 等级推算下一阶段 */
  function getNextLevel(level) {
    const levels = ['A1', 'A2', 'B1', 'B2', 'C1', 'C2']
    const idx = levels.indexOf(level)
    if (idx >= 0 && idx < levels.length - 1) return levels[idx + 1]
    return level
  }

  /** 保存 CEFR 进度到后端，单次获得 score/5 进度，溢出不保留，每次最多升一级，返回 { level, nextLevel, leveledUp } */
  async function saveCefrProgress(score, aiCefrLevel) {
    const { useUserStore } = await import('@/stores/user')
    const userStore = useUserStore()
    const userId = userStore.user?.userId
    if (!userId) return null

    // 先检测用户当前等级
    let currentLevel = null
    try {
      const raw = localStorage.getItem(`aael_vocab_result_${userId}`)
      if (raw) {
        const vocabResult = JSON.parse(raw)
        if (vocabResult.cefrLevel) currentLevel = vocabResult.cefrLevel.toUpperCase()
      }
    } catch (_) {}
    if (!currentLevel) {
      const literacy = userStore.user?.literacy || 0
      if (literacy >= 12000) currentLevel = 'C2'
      else if (literacy >= 8000) currentLevel = 'C1'
      else if (literacy >= 5000) currentLevel = 'B2'
      else if (literacy >= 3000) currentLevel = 'B1'
      else if (literacy >= 1500) currentLevel = 'A2'
      else currentLevel = 'A1'
    }

    // 已到达最高等级 C2，不再增加进度（分数低于30也提示攻克C2难题）
    if (currentLevel === 'C2') {
      if ((score || 0) < 30) {
        return { level: 'C2', nextLevel: 'C2', leveledUp: false, progressGained: 0, tooLow: true, isMaxLevel: true }
      }
      return { level: 'C2', nextLevel: 'C2', leveledUp: false, progressGained: 0, isMaxLevel: true }
    }

    // 分数低于 30 分不增加进度
    if ((score || 0) < 30) {
      return { level: null, nextLevel: null, leveledUp: false, progressGained: 0, tooLow: true }
    }

    const vocabBases = { A1: 500, A2: 1500, B1: 3000, B2: 5000, C1: 8000, C2: 12000 }
    const cefrLabels = { A1: '初级', A2: '初级上', B1: '中级', B2: '中高级', C1: '高级', C2: '精通' }

    let currentProgress = 0
    try {
      const data = await request.get('/user/cefr-progress', { params: { userId } })
      if (data.success) currentProgress = data.cefrProgress || 0
    } catch (_) {}

    // 单次评分 / 5 = 获得的进度（满分 100 → 20%，5 次满分晋升一级）
    const increment = Math.round((score || 0) / 5)
    let newProgress = currentProgress + increment
    let newLevel = currentLevel
    let leveledUp = false

    // 进度 >= 100 时晋级，溢出进度不保留，每次最多升一级
    if (newProgress >= 100) {
      newProgress = 0  // 溢出丢弃
      newLevel = getNextLevel(currentLevel)
      leveledUp = true
    }

    // 合并发送 literacy（升级时）和 progress
    try {
      const params = new URLSearchParams()
      params.append('userId', String(userId))
      if (leveledUp && vocabBases[newLevel]) {
        params.append('literacy', String(vocabBases[newLevel]))
      }
      params.append('progress', String(newProgress))
      await request.post('/user/cefr-progress', params)
    } catch (_) {}

    // 同步本地
    if (userStore.user) {
      userStore.user.cefrProgress = newProgress
      if (vocabBases[newLevel]) userStore.user.literacy = vocabBases[newLevel]
      localStorage.setItem('user', JSON.stringify(userStore.user))
    }
    localStorage.setItem(`aael_vocab_result_${userId}`, JSON.stringify({
      literacy: vocabBases[newLevel] || userStore.user?.literacy || 3000,
      cefrLevel: newLevel,
      cefrLabel: cefrLabels[newLevel] || '中级',
    }))

    return { level: newLevel, nextLevel: getNextLevel(newLevel), leveledUp, progressGained: increment }
  }

  // ========== 进度持久化 ==========

  function saveProgress() {
    try {
      const data = {
        questions: questions.value,
        currentIndex: currentIndex.value,
        answers: answers.value,
        sessionId: sessionId.value,
        totalTarget: totalTarget.value,
      }
      localStorage.setItem(getProgressKey(), JSON.stringify(data))
    } catch {
      // localStorage 不可用时静默失败
    }
  }

  function loadProgress() {
    try {
      const raw = localStorage.getItem(getProgressKey())
      if (!raw) return null
      const data = JSON.parse(raw)
      if (!data.questions || !Array.isArray(data.questions) || data.questions.length === 0) {
        return null
      }
      return data
    } catch {
      return null
    }
  }

  function clearProgress() {
    try {
      localStorage.removeItem(getProgressKey())
    } catch {
      // 静默失败
    }
    // 注意：不在此处清除 assessmentResult，ResultPage 需要在导航后读取
  }

  function restoreProgress(data) {
    questions.value = data.questions || []
    // 迁移旧版数字 id → UUID（兼容旧进度数据）
    if (questions.value.length > 0 && isLegacyNumericId(questions.value[0].id)) {
      questions.value = questions.value.map((q, i) => ({
        ...q,
        id: generateUUID(),
        questionNumber: typeof q.questionNumber === 'number' ? q.questionNumber : (i + 1),
      }))
    }
    currentIndex.value = data.currentIndex || 0
    // 迁移旧版 answers key（数字 id → UUID）
    const oldAnswers = data.answers || {}
    if (Object.keys(oldAnswers).length > 0 && isLegacyNumericId(Object.keys(oldAnswers)[0])) {
      const migrated = {}
      const idMap = {}
      questions.value.forEach(q => { idMap[q.questionNumber] = q.id })
      Object.entries(oldAnswers).forEach(([key, val]) => {
        const qNum = parseInt(key, 10)
        const newId = idMap[qNum]
        if (newId) migrated[newId] = val
      })
      answers.value = migrated
    } else {
      answers.value = oldAnswers
    }
    sessionId.value = data.sessionId || ''
    totalTarget.value = data.totalTarget || 10
    currentScreen.value = 'quiz'
    // 恢复后如果题目数不足且 sessionId 有效，继续轮询
    if (sessionId.value && questions.value.length < totalTarget.value) {
      startPolling()
    }
  }

  return {
    currentScreen,
    questions,
    currentIndex,
    answers,
    loading,
    assessmentResult,
    sessionId,
    totalTarget,
    pollError,
    allQuestionsReady,
    currentQuestion,
    totalQuestions,
    progressPercent,
    isFirstQuestion,
    isLastQuestion,
    selectedOption,
    questionReview,
    startAssessment,
    selectOption,
    nextQuestion,
    prevQuestion,
    submitAssessment,
    resetAssessment,
    getResult,
    saveProgress,
    loadProgress,
    clearProgress,
    restoreProgress,
  }
})
