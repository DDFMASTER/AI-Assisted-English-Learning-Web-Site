<template>
  <Teleport to="body">
    <div
      v-if="visible"
      class="fixed inset-0 z-[300] flex items-center justify-center bg-black/50 backdrop-blur-sm"
      tabindex="-1"
      @keydown.escape="$emit('close')"
    >
      <div class="bg-white rounded-2xl shadow-2xl w-full max-w-[95vw] sm:max-w-lg max-h-[90vh] overflow-y-auto mx-2 sm:mx-4">
        <!-- ========== 开始界面 ========== -->
        <div v-if="phase === 'start'" class="p-8 text-center">
          <Icon icon="ph:book-open-text-bold" class="text-5xl text-[#2563EB] mx-auto mb-4" />
          <h2 class="text-2xl font-bold mb-2">词汇量测试</h2>
          <p class="text-gray-500 text-sm leading-relaxed mb-6">
            测试共 <strong>100</strong> 道题（90 个真词 + 10 个伪词）。<br/>
            每题展示一个单词和 4 个中文释义，选择你认为正确的释义，<br/>
            或点击 <strong>"认识"</strong> / <strong>"不认识"</strong>。<br/>
            伪词用于诚信检测——对伪词诚实选择"不认识"才算正确。
          </p>
          <div class="bg-amber-50 border border-amber-200 rounded-xl p-4 mb-6 text-left">
            <p class="text-xs text-amber-700 font-bold mb-1">📌 评分规则</p>
            <ul class="text-xs text-amber-600 space-y-1">
              <li>· 真词：选对释义 或 点"认识" → ✓ 正确</li>
              <li>· 伪词：点"不认识" → ✓ 正确（选释义或"认识"都算错）</li>
            </ul>
          </div>
          <button
            class="px-8 py-3 bg-[#2563EB] text-white rounded-xl font-bold shadow-lg shadow-blue-200 hover:scale-105 transition-transform"
            @click="startTest"
          >
            开始测试
          </button>
          <button
            class="block mx-auto mt-3 text-xs text-gray-400 hover:text-gray-600 transition-colors"
            @click="$emit('close')"
          >
            返回
          </button>
        </div>

        <!-- ========== 加载中 ========== -->
        <div v-else-if="phase === 'loading'" class="p-12 text-center">
          <div class="w-16 h-16 mx-auto mb-6 rounded-full border-4 border-gray-100 border-t-[#2563EB] animate-spin"></div>
          <h3 class="text-lg font-bold text-gray-700 mb-2">正在加载测试数据...</h3>
          <p class="text-sm text-gray-400">{{ loadingStatus }}</p>
          <div class="mt-6 w-full max-w-xs mx-auto bg-gray-100 rounded-full h-2 overflow-hidden">
            <div
              class="h-full bg-[#2563EB] rounded-full transition-all duration-500"
              :style="{ width: Math.min(100, (loadingReady / 3) * 100) + '%' }"
            ></div>
          </div>
          <p class="text-xs text-gray-300 mt-2">{{ Math.min(loadingReady, 3) }} / 3 组选项已就绪</p>
        </div>

        <!-- ========== 测试中 ========== -->
        <div v-else-if="phase === 'testing'" class="p-6">
          <div class="flex items-center gap-3 mb-6">
            <span class="text-xs font-bold text-gray-400">{{ currentIndex + 1 }} / {{ totalWords }}</span>
            <div class="flex-1 h-2 bg-gray-100 rounded-full overflow-hidden">
              <div
                class="h-full bg-[#2563EB] rounded-full transition-all duration-500"
                :style="{ width: ((currentIndex + 1) / totalWords * 100) + '%' }"
              ></div>
            </div>
            <button
              class="text-xs font-bold text-gray-400 hover:text-red-500 transition-colors whitespace-nowrap"
              @click="abandonTest"
            >
              放弃测试
            </button>
          </div>

          <div class="text-center mb-6" :key="'card-' + currentIndex">
            <p class="text-3xl font-black text-gray-800 mb-1">{{ currentWord.word }}</p>
            
            <div v-if="currentOptions.length === 0" class="space-y-3 mt-6">
              <div v-for="i in 4" :key="i" class="h-12 bg-gray-100 rounded-xl animate-pulse"></div>
            </div>
            <div v-else class="space-y-3 mt-6 text-left">
              <button
                v-for="(opt, idx) in currentOptions"
                :key="idx"
                class="w-full flex items-center gap-3 p-3.5 rounded-xl text-sm font-medium transition-all"
                :class="getOptBtnClass(idx)"
                :disabled="answered"
                @click="selectOption(idx)"
              >
                <span
                  class="w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold flex-none"
                  :class="getOptDotClass(idx)"
                >{{ optionLabels[idx] }}</span>
                <span>{{ opt }}</span>
                <Icon
                  v-if="answered && idx === currentCorrectIndex && !currentWord.isPseudo"
                  icon="ph:check-circle-fill" class="ml-auto text-green-500 text-lg"
                />
                <Icon
                  v-else-if="answered && idx === selectedOptionIdx && idx !== currentCorrectIndex"
                  icon="ph:x-circle-fill" class="ml-auto text-red-500 text-lg"
                />
              </button>
            </div>

            <div class="flex items-center gap-4 mt-6">
              <button
                class="flex-1 py-3 rounded-xl text-sm font-bold transition-all border-2"
                :class="getKnowBtnClass('know')"
                :disabled="answered"
                @click="selectKnowOrDont('know')"
              >
                <Icon icon="ph:hand-thumbs-up-bold" class="text-lg inline mr-1" />
                认识
                <span v-if="answered && selectedAnswer === 'know'" class="block text-xs mt-0.5">
                  {{ currentWord.isPseudo ? '✗ 伪词选认识为错误' : '✓ 作答正确' }}
                </span>
              </button>
              <button
                class="flex-1 py-3 rounded-xl text-sm font-bold transition-all border-2"
                :class="getKnowBtnClass('dontknow')"
                :disabled="answered"
                @click="selectKnowOrDont('dontknow')"
              >
                <Icon icon="ph:hand-thumbs-down-bold" class="text-lg inline mr-1" />
                不认识
                <span v-if="answered && selectedAnswer === 'dontknow'" class="block text-xs mt-0.5">
                  {{ currentWord.isPseudo ? '✓ 作答正确' : '✗ 真词选不认识为错误' }}
                </span>
              </button>
            </div>

            <div v-if="answered" class="mt-4 p-3 rounded-xl text-sm font-bold"
              :class="isCurrentCorrect ? 'bg-green-50 text-green-600' : 'bg-red-50 text-red-600'">
              <Icon :icon="isCurrentCorrect ? 'ph:check-circle-bold' : 'ph:x-circle-bold'" class="inline mr-1" />
              {{ isCurrentCorrect ? '回答正确！' : '回答错误' }}
              <span v-if="!isCurrentCorrect && !currentWord.isPseudo && currentCorrectIndex >= 0" class="block text-xs mt-1 font-normal">
                正确答案是 {{ optionLabels[currentCorrectIndex] }}. {{ currentOptions[currentCorrectIndex] }}
              </span>
              <span v-if="!isCurrentCorrect && currentWord.isPseudo" class="block text-xs mt-1 font-normal">
                这是伪词，正确选择是"不认识"
              </span>
              <span v-if="countdown > 0" class="block text-xs mt-1 text-gray-400">{{ countdown }} 秒后自动下一题</span>
              <span v-else class="block text-xs mt-1 text-[#2563EB]">正在加载下一题...</span>
            </div>
          </div>
        </div>

        <!-- ========== 结果界面 ========== -->
        <VocabTestResult
          v-else-if="phase === 'result'"
          v-bind="resultProps"
          @close="$emit('close')"
          @test-again="resetToStart()"
        />
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, computed, onUnmounted, watch } from 'vue'
import { Icon } from '@iconify/vue'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import VocabTestResult from './VocabTestResult.vue'

const props = defineProps({
  visible: { type: Boolean, default: false },
})

const emit = defineEmits(['close'])

const optionLabels = ['A', 'B', 'C', 'D']
const PREFETCH_AHEAD = 8
const FEEDBACK_CORRECT = 1
const FEEDBACK_INCORRECT = 2

const phase = ref('start')
const loadingStatus = ref('正在获取测试词汇...')
const loadingReady = ref(0)
const totalWords = ref(0)
const words = ref([])
const optionsCache = ref({})
const answers = ref([])
const currentIndex = ref(0)
const selectedOptionIdx = ref(-1)
const selectedAnswer = ref('')
const answered = ref(false)
const countdown = ref(0)
let countdownTimer = null
let autoAdvanceTimer = null
const prefetching = ref(false)

const resultVocab = ref(0)
const resultCefr = ref('A1')
const resultCefrLabel = ref('初级')
const resultCorrect = ref(0)
const resultHonesty = ref(0)
const resultXp = ref(0)
const resultPseudoRate = ref(0)

// 实时统计
const realCorrect = ref(0)
const realTotal = ref(0)
const pseudoCorrect = ref(0)
const pseudoTotal = ref(0)

const currentWord = computed(() => words.value[currentIndex.value] || {})
const currentOptions = computed(() => {
  const opts = optionsCache.value[currentWord.value.vocabId]
  return opts ? opts.options : []
})
const currentCorrectIndex = computed(() => {
  const opts = optionsCache.value[currentWord.value.vocabId]
  return opts ? opts.correctIndex : -1
})
const isCurrentCorrect = computed(() => {
  const word = currentWord.value
  if (!answered.value) return false
  if (word.isPseudo) {
    return selectedAnswer.value === 'dontknow'
  } else {
    return selectedAnswer.value === 'know' || selectedOptionIdx.value === currentCorrectIndex.value
  }
})

const resultProps = computed(() => ({
  correctCount: resultCorrect.value,
  totalWords: totalWords.value,
  estimatedVocab: resultVocab.value,
  cefrLevel: resultCefr.value,
  xpEarned: resultXp.value,
  honestyPercent: resultHonesty.value,
  pseudoHitRate: resultPseudoRate.value,
}))

async function startTest() {
  phase.value = 'loading'
  loadingStatus.value = '正在获取测试词汇...'
  loadingReady.value = 0
  currentIndex.value = 0
  answers.value = []
  optionsCache.value = {}
  answered.value = false
  realCorrect.value = 0
  realTotal.value = 0
  pseudoCorrect.value = 0
  pseudoTotal.value = 0

  try {
    const data = await request.get('/vocabtest/cards')
    words.value = data.words || []
    totalWords.value = data.total || words.value.length

    if (words.value.length === 0) {
      alert('获取测试词汇失败，请重试')
      phase.value = 'start'
      return
    }

    loadingStatus.value = '正在调用 AI 生成释义选项...'

    // 轮询直到至少 3 个单词的选项就绪（最多重试 15 次）
    let retries = 0
    while (countReadyOptions() < 3 && retries < 15) {
      await prefetchBatch(loadingReady.value)
      loadingReady.value = countReadyOptions()
      if (loadingReady.value < 3) {
        retries++
        loadingStatus.value = 'AI 正在生成释义选项...（' + loadingReady.value + '/3 就绪，第' + retries + '次尝试）'
        await new Promise(r => setTimeout(r, 1200))
      }
    }

    phase.value = 'testing'
  } catch (e) {
    console.error('启动测试失败:', e)
    alert('启动测试失败，请重试')
    phase.value = 'start'
  }
}

/** 统计缓存中非空选项的单词数量 */
function countReadyOptions() {
  let count = 0
  for (const w of words.value) {
    const opts = optionsCache.value[w.vocabId]
    if (opts && opts.options && opts.options.length >= 4 &&
        opts.options.every(o => o && o.trim())) {
      count++
    }
  }
  return count
}

async function prefetchBatch(fromIndex) {
  if (prefetching.value) return
  prefetching.value = true

  try {
    const endIdx = Math.min(fromIndex + PREFETCH_AHEAD, words.value.length)
    const needFetch = []
    for (let i = fromIndex; i < endIdx; i++) {
      const w = words.value[i]
      if (w && !optionsCache.value[w.vocabId]) {
        needFetch.push(w)
      }
    }

    if (needFetch.length > 0) {
      const resp = await request.post('/vocabtest/options', { words: needFetch })
      const results = resp.results || []
      for (const r of results) {
        optionsCache.value[r.vocabId] = {
          options: r.options || [],
          correctIndex: r.correctIndex,
        }
      }
    }
  } catch (e) {
    console.error('预取选项失败:', e)
  } finally {
    prefetching.value = false
  }
}

function selectOption(idx) {
  if (answered.value) return
  selectedOptionIdx.value = idx
  selectedAnswer.value = optionLabels[idx]
  answered.value = true
  scheduleNext()
}

function selectKnowOrDont(type) {
  if (answered.value) return
  selectedOptionIdx.value = -1
  selectedAnswer.value = type
  answered.value = true
  scheduleNext()
}

function scheduleNext() {
  // 实时统计
  const word = currentWord.value
  if (word.isPseudo) {
    pseudoTotal.value++
    if (isCurrentCorrect.value) pseudoCorrect.value++
  } else {
    realTotal.value++
    if (isCurrentCorrect.value) realCorrect.value++
  }

  answers.value.push(selectedAnswer.value)
  clearTimers()

  prefetchBatch(currentIndex.value + 1)

  const delay = isCurrentCorrect.value ? FEEDBACK_CORRECT : FEEDBACK_INCORRECT
  countdown.value = delay
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(countdownTimer)
    }
  }, 1000)

  autoAdvanceTimer = setTimeout(async () => {
    clearInterval(countdownTimer)
    await tryAdvance()
  }, delay * 1000)
}

/** 尝试推进到下一题，若下一题选项未就绪则等待 */
async function tryAdvance() {
  if (currentIndex.value >= words.value.length - 1) {
    submitResults()
    return
  }

  const nextWord = words.value[currentIndex.value + 1]
  const nextOpts = nextWord ? optionsCache.value[nextWord.vocabId] : null
  const isReady = nextOpts && nextOpts.options && nextOpts.options.length > 0 &&
    nextOpts.options.every(o => o && o.trim())

  if (isReady) {
    currentIndex.value++
    selectedOptionIdx.value = -1
    selectedAnswer.value = ''
    answered.value = false
  } else {
    // 选项未就绪，显示加载状态并等待
    answered.value = true  // 保持当前卡片的已答状态
    countdown.value = 0
    await prefetchBatch(currentIndex.value + 1)
    autoAdvanceTimer = setTimeout(() => tryAdvance(), 500)
  }
}

async function submitResults() {
  try {
    const userStore = useUserStore()
    const userId = userStore.user?.userId
    if (!userId) return

    // 计算本地统计结果
    const totalCorrect = realCorrect.value + pseudoCorrect.value
    const totalQuestions = realTotal.value + pseudoTotal.value

    // 伪词正确率 = 诚信度
    const honesty = pseudoTotal.value > 0 ? Math.round(pseudoCorrect.value / pseudoTotal.value * 100) : 100
    // 真词正确率
    const realRate = realTotal.value > 0 ? realCorrect.value / realTotal.value : 0
    // 伪词诚信修正
    const pseudoRate = pseudoTotal.value > 0 ? pseudoCorrect.value / pseudoTotal.value : 0
    const adjustedRate = realRate * (0.7 + 0.3 * pseudoRate)
    // 估算词汇量
    const vocab = Math.max(500, Math.min(20000, Math.round(adjustedRate * 12000)))

    // CEFR 等级
    let cefr, cefrLabel
    if (vocab < 1500) { cefr = 'A1'; cefrLabel = '初级' }
    else if (vocab < 3000) { cefr = 'A2'; cefrLabel = '初级上' }
    else if (vocab < 5000) { cefr = 'B1'; cefrLabel = '中级' }
    else if (vocab < 8000) { cefr = 'B2'; cefrLabel = '中高级' }
    else if (vocab < 12000) { cefr = 'C1'; cefrLabel = '高级' }
    else { cefr = 'C2'; cefrLabel = '精通' }

    resultVocab.value = vocab
    resultCefr.value = cefr
    resultCefrLabel.value = cefrLabel
    resultCorrect.value = totalCorrect
    resultHonesty.value = honesty
    resultPseudoRate.value = Math.round(pseudoRate * 100)

    // 根据得分给予 0-10 经验值
    const xpEarned = totalQuestions > 0 ? Math.round((totalCorrect / totalQuestions) * 10) : 0
    resultXp.value = xpEarned

    // 同步到后端
    try {
      const userStore = useUserStore()
      const userId = userStore.user?.userId
      if (userId) {
        await request.post('/vocabtest/cardresult', {
          userId,
          correct: totalCorrect,
          total: totalQuestions,
          realCorrect: realCorrect.value,
          realTotal: realTotal.value,
          pseudoCorrect: pseudoCorrect.value,
          pseudoTotal: pseudoTotal.value,
          estimatedVocab: vocab,
          cefrLevel: cefr,
        })
        // 同步经验值
        if (xpEarned > 0) {
          const xpParams = new URLSearchParams()
          xpParams.append('userId', String(userId))
          xpParams.append('xp', String(xpEarned))
          await request.post('/user/experience', xpParams)
        }
        await userStore.fetchProfile()
      }
    } catch (_) {}

    phase.value = 'result'
  } catch (e) {
    console.error('提交结果失败:', e)
    alert('提交结果失败，请重试')
  }
}

function clearTimers() {
  if (countdownTimer) { clearInterval(countdownTimer); countdownTimer = null }
  if (autoAdvanceTimer) { clearTimeout(autoAdvanceTimer); autoAdvanceTimer = null }
}

function abandonTest() {
  clearTimers()
  emit('close')
}

function resetToStart() {
  clearTimers()
  phase.value = 'start'
  loadingReady.value = 0
  loadingStatus.value = '正在获取测试词汇...'
  words.value = []
  optionsCache.value = {}
  answers.value = []
  currentIndex.value = 0
  answered.value = false
  realCorrect.value = 0
  realTotal.value = 0
  pseudoCorrect.value = 0
  pseudoTotal.value = 0
}

function getOptBtnClass(idx) {
  if (!answered.value) {
    return 'bg-gray-50 border border-transparent hover:border-[#2563EB] hover:bg-blue-50 cursor-pointer'
  }
  if (currentWord.value.isPseudo) {
    if (idx === selectedOptionIdx.value) return 'bg-red-50 border border-red-300'
    return 'bg-gray-50 border border-transparent opacity-50'
  }
  if (idx === currentCorrectIndex.value) {
    return 'bg-green-50 border border-green-300'
  }
  if (idx === selectedOptionIdx.value && idx !== currentCorrectIndex.value) {
    return 'bg-red-50 border border-red-300'
  }
  return 'bg-gray-50 border border-transparent opacity-50'
}

function getOptDotClass(idx) {
  if (!answered.value) return 'bg-gray-200 text-gray-500'
  if (idx === currentCorrectIndex.value && !currentWord.value.isPseudo) {
    return 'bg-green-500 text-white'
  }
  if (idx === selectedOptionIdx.value && idx !== currentCorrectIndex.value) {
    return 'bg-red-500 text-white'
  }
  return 'bg-gray-200 text-gray-400'
}

function getKnowBtnClass(type) {
  if (!answered.value) {
    if (type === 'know') {
      return 'border-gray-200 bg-white text-gray-600 hover:border-green-400 hover:text-green-500 hover:bg-green-50'
    }
    return 'border-gray-200 bg-white text-gray-600 hover:border-[#2563EB] hover:text-[#2563EB] hover:bg-blue-50'
  }
  if (type === selectedAnswer.value) {
    if (currentWord.value.isPseudo) {
      return type === 'dontknow'
        ? 'border-green-400 bg-green-50 text-green-600'
        : 'border-red-400 bg-red-50 text-red-600'
    } else {
      return type === 'know'
        ? 'border-green-400 bg-green-50 text-green-600'
        : 'border-red-400 bg-red-50 text-red-600'
    }
  }
  return 'border-gray-200 bg-white text-gray-400'
}

function handleKeydown(e) {
  if (phase.value !== 'testing' || answered.value) return
  const key = e.key.toUpperCase()
  if (key === 'A' || key === 'B' || key === 'C' || key === 'D') {
    const idx = optionLabels.indexOf(key)
    if (idx >= 0 && idx < currentOptions.value.length) {
      selectOption(idx)
    }
  }
}

watch(() => props.visible, (val) => {
  if (val) {
    window.addEventListener('keydown', handleKeydown)
    phase.value = 'start'
    loadingReady.value = 0
    loadingStatus.value = '正在获取测试词汇...'
    words.value = []
    optionsCache.value = {}
    answers.value = []
    currentIndex.value = 0
    answered.value = false
    clearTimers()
  } else {
    window.removeEventListener('keydown', handleKeydown)
    clearTimers()
  }
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
  clearTimers()
})
</script>
