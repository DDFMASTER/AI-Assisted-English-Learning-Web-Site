<template>
  <Teleport to="body">
    <div
      v-if="visible"
      class="fixed inset-0 z-[300] flex items-center justify-center bg-black/50 backdrop-blur-sm"
      tabindex="-1"
      @keydown.escape="endEarly"
    >
      <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-2xl w-full max-w-[95vw] sm:max-w-lg max-h-[90vh] overflow-y-auto mx-2 sm:mx-4">
        <!-- ========== 开始界面 ========== -->
        <div v-if="phase === 'start'" class="p-8 text-center">
          <Icon icon="ph:book-open-text-bold" class="text-5xl text-[#2563EB] mx-auto mb-4" />
          <h2 class="text-2xl font-bold mb-2 dark:text-gray-200">复习生词</h2>
          <p class="text-gray-500 dark:text-gray-400 text-sm leading-relaxed mb-6">
            共 <strong>{{ words.length }}</strong> 个生词待复习。<br/>
            每题展示一个单词和 4 个中文释义，选择你认为正确的释义。
          </p>
          <button
            class="px-8 py-3 bg-[#2563EB] text-white rounded-xl font-bold shadow-lg shadow-blue-200 hover:scale-105 transition-transform"
            @click="startReview"
          >
            开始复习
          </button>
          <button
            class="block mx-auto mt-3 text-xs text-gray-400 dark:text-gray-500 hover:text-gray-600 dark:hover:text-gray-300 transition-colors"
            @click="$emit('close')"
          >
            返回
          </button>
        </div>

        <!-- ========== 加载中 ========== -->
        <div v-else-if="phase === 'loading'" class="p-12 text-center">
          <div class="w-16 h-16 mx-auto mb-6 rounded-full border-4 border-gray-100 dark:border-gray-700 border-t-[#2563EB] animate-spin"></div>
          <h3 class="text-lg font-bold text-gray-700 dark:text-gray-200 mb-2">正在生成释义选项...</h3>
          <p class="text-sm text-gray-400 dark:text-gray-500 mb-4">{{ loadingStatus }}</p>
          <div class="w-48 mx-auto h-1.5 bg-gray-100 dark:bg-gray-700 rounded-full overflow-hidden">
            <div
              class="h-full bg-[#2563EB] rounded-full transition-all duration-300"
              :style="{ width: (words.length > 0 ? Math.round(loadedCount / words.length * 100) : 0) + '%' }"
            ></div>
          </div>
        </div>

        <!-- ========== 测试中 ========== -->
        <div v-else-if="phase === 'testing'" class="p-6">
          <div class="flex items-center gap-3 mb-6">
            <span class="text-xs font-bold text-gray-400 dark:text-gray-500">{{ currentIndex + 1 }} / {{ words.length }}</span>
            <div class="flex-1 h-2 bg-gray-100 dark:bg-gray-700 rounded-full overflow-hidden">
              <div
                class="h-full bg-[#2563EB] rounded-full transition-all duration-500"
                :style="{ width: ((currentIndex + 1) / words.length * 100) + '%' }"
              ></div>
            </div>
            <button
              class="text-xs font-bold text-gray-400 dark:text-gray-500 hover:text-red-500 dark:hover:text-red-400 transition-colors whitespace-nowrap"
              @click="endEarly"
            >
              提前结束
            </button>
          </div>

          <div class="text-center mb-6" :key="'card-' + currentIndex">
            <p class="text-3xl font-black text-gray-800 dark:text-gray-200 mb-1">{{ currentWord.word }}</p>

            <div v-if="currentOptions.length === 0" class="space-y-3 mt-6">
              <div v-for="i in 4" :key="i" class="h-12 bg-gray-100 dark:bg-gray-700 rounded-xl animate-pulse"></div>
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
                  v-if="answered && idx === currentCorrectIndex"
                  icon="ph:check-circle-fill" class="ml-auto text-green-500 text-lg"
                />
                <Icon
                  v-else-if="answered && idx === selectedOptionIdx && idx !== currentCorrectIndex"
                  icon="ph:x-circle-fill" class="ml-auto text-red-500 text-lg"
                />
              </button>
            </div>

            <div v-if="answered" class="mt-4 p-3 rounded-xl text-sm font-bold"
              :class="isCurrentCorrect ? 'bg-green-50 dark:bg-green-900/30 text-green-600 dark:text-green-400' : 'bg-red-50 dark:bg-red-900/30 text-red-600 dark:text-red-400'">
              <Icon :icon="isCurrentCorrect ? 'ph:check-circle-bold' : 'ph:x-circle-bold'" class="inline mr-1" />
              {{ isCurrentCorrect ? '回答正确！' : '回答错误' }}
              <span v-if="!isCurrentCorrect && currentCorrectIndex >= 0" class="block text-xs mt-1 font-normal">
                正确答案是 {{ optionLabels[currentCorrectIndex] }}. {{ currentOptions[currentCorrectIndex] }}
              </span>
              <span v-if="countdown > 0" class="block text-xs mt-1 text-gray-400 dark:text-gray-500">{{ countdown }} 秒后自动下一题</span>
              <span v-else class="block text-xs mt-1 text-[#2563EB]">正在加载下一题...</span>
            </div>
          </div>
        </div>

        <!-- ========== 结果界面 ========== -->
        <div v-else-if="phase === 'result'" class="p-8 text-center">
          <div class="w-20 h-20 rounded-full flex items-center justify-center mx-auto mb-5"
            :class="resultCorrect >= resultTotal * 0.7 ? 'bg-green-50 dark:bg-green-900/30' : 'bg-amber-50 dark:bg-amber-900/30'">
            <Icon
              :icon="resultCorrect >= resultTotal * 0.7 ? 'ph:trophy-fill' : 'ph:target-fill'"
              class="text-4xl"
              :class="resultCorrect >= resultTotal * 0.7 ? 'text-green-500' : 'text-amber-500'"
            />
          </div>
          <h2 class="text-2xl font-bold mb-2 dark:text-gray-200">
            {{ resultCorrect >= resultTotal * 0.7 ? '表现不错！' : '继续加油！' }}
          </h2>
          <p class="text-gray-500 dark:text-gray-400 text-sm mb-6">
            {{ resultTotal > 0 ? `你在 ${resultTotal} 道题中答对了 ${resultCorrect} 道（${Math.round(resultCorrect / resultTotal * 100)}%）` : '没有作答任何题目' }}
          </p>
          <div class="flex gap-3 justify-center">
            <button
              class="px-6 py-3 bg-[#2563EB] text-white rounded-xl font-bold shadow-lg shadow-blue-200 hover:scale-105 transition-transform"
              @click="resetToStart"
            >
              再次复习
            </button>
            <button
              class="px-6 py-3 bg-gray-100 dark:bg-gray-700 text-gray-500 dark:text-gray-300 rounded-xl font-bold hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors"
              @click="$emit('close')"
            >
              返回
            </button>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, computed, onUnmounted, watch } from 'vue'
import { Icon } from '@iconify/vue'
import request from '@/utils/request'

const props = defineProps({
  visible: { type: Boolean, default: false },
  words: { type: Array, default: () => [] },
})

const emit = defineEmits(['close'])

const optionLabels = ['A', 'B', 'C', 'D']
const FEEDBACK_CORRECT = 1
const FEEDBACK_INCORRECT = 2

const phase = ref('start')
const loadingStatus = ref('正在调用 AI 生成释义选项...')
const loadedCount = ref(0)
const optionsCache = ref({})
const currentIndex = ref(0)
const selectedOptionIdx = ref(-1)
const answered = ref(false)
const countdown = ref(0)
let countdownTimer = null
let autoAdvanceTimer = null

const resultCorrect = ref(0)
const resultTotal = ref(0)

const shuffledWords = ref([])

const currentWord = computed(() => shuffledWords.value[currentIndex.value] || {})
const currentOptions = computed(() => {
  const w = currentWord.value
  if (!w || !w.word) return []
  const cached = optionsCache.value[w.word]
  return cached ? cached.options : []
})
const currentCorrectIndex = computed(() => {
  const w = currentWord.value
  if (!w || !w.word) return -1
  const cached = optionsCache.value[w.word]
  return cached ? cached.correctIndex : -1
})
const isCurrentCorrect = computed(() => {
  return answered.value && selectedOptionIdx.value === currentCorrectIndex.value
})

async function startReview() {
  phase.value = 'loading'
  loadingStatus.value = '正在准备复习数据...'
  loadedCount.value = 0
  currentIndex.value = 0
  optionsCache.value = {}
  answered.value = false
  resultCorrect.value = 0
  resultTotal.value = 0

  // 打乱单词顺序（Fisher-Yates）
  shuffledWords.value = [...props.words]
  for (let i = shuffledWords.value.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [shuffledWords.value[i], shuffledWords.value[j]] = [shuffledWords.value[j], shuffledWords.value[i]]
  }

  // 检查本地缓存
  const cached = loadReviewCache()
  const wordSet = new Set(shuffledWords.value.map(w => w.word))
  if (cached && cached.words && cached.words.length === shuffledWords.value.length) {
    const cachedSet = new Set(cached.words.map(w => w.word))
    const match = wordSet.size === cachedSet.size && [...wordSet].every(w => cachedSet.has(w))
    if (match) {
      // 缓存命中，直接使用
      for (const item of cached.words) {
        optionsCache.value[item.word] = {
          options: item.options,
          correctIndex: item.correctIndex,
        }
      }
      phase.value = 'testing'
      return
    }
  }

  // 缓存未命中，请求 AI
  try {
    await fetchAllOptions()
    saveReviewCache()
    phase.value = 'testing'
  } catch (e) {
    console.error('加载选项失败:', e)
    phase.value = 'start'
  }
}

/** Fisher-Yates 洗牌 */
function shuffleArray(arr) {
  const a = [...arr]
  for (let i = a.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [a[i], a[j]] = [a[j], a[i]]
  }
  return a
}

/** 获取复习缓存 key */
function getReviewCacheKey() {
  try {
    const raw = localStorage.getItem('user')
    if (!raw) return 'aael_vocab_review_anon'
    const user = JSON.parse(raw)
    return `aael_vocab_review_${user.userId || 'anon'}`
  } catch { return 'aael_vocab_review_anon' }
}

/** 加载本地缓存的复习数据 */
function loadReviewCache() {
  try {
    const raw = localStorage.getItem(getReviewCacheKey())
    return raw ? JSON.parse(raw) : null
  } catch { return null }
}

/** 保存复习数据到本地缓存 */
function saveReviewCache() {
  try {
    const data = {
      words: shuffledWords.value.map(w => ({
        word: w.word,
        options: (optionsCache.value[w.word]?.options || []).slice(),
        correctIndex: optionsCache.value[w.word]?.correctIndex ?? -1,
      })),
      cachedAt: Date.now(),
    }
    localStorage.setItem(getReviewCacheKey(), JSON.stringify(data))
  } catch { /* localStorage 不可用时静默失败 */ }
}

/** 清除复习缓存（生词本变化时调用） */
function clearReviewCache() {
  try {
    localStorage.removeItem(getReviewCacheKey())
  } catch { /* ignore */ }
}

/** 批量获取所有单词的 AI 释义选项 */
async function fetchAllOptions() {
  const words = shuffledWords.value
  if (!words || words.length === 0) return

  const BATCH_SIZE = 5
  const totalBatches = Math.ceil(words.length / BATCH_SIZE)
  let batchNum = 0

  for (let i = 0; i < words.length; i += BATCH_SIZE) {
    batchNum++
    const batch = words.slice(i, i + BATCH_SIZE).map((w, j) => ({
      vocabId: i + j + 1,
      word: w.word,
      isPseudo: false,
    }))

    const batchStart = i + 1
    const batchEnd = Math.min(i + BATCH_SIZE, words.length)
    loadingStatus.value = `正在生成释义选项... 第 ${batchNum}/${totalBatches} 批（单词 ${batchStart}-${batchEnd}）`

    try {
      const resp = await request.post('/vocabtest/options', { words: batch, mode: 'review' })
      const results = resp.results || []
      for (const r of results) {
        optionsCache.value[r.word || r.vocabId] = {
          options: r.options || [],
          correctIndex: r.correctIndex,
        }
      }
      loadedCount.value = Math.min(i + BATCH_SIZE, words.length)
      loadingStatus.value = `正在生成释义选项... ${loadedCount.value}/${words.length}`
    } catch (e) {
      console.error('获取选项失败:', e)
      for (const w of batch) {
        if (!optionsCache.value[w.word]) {
          optionsCache.value[w.word] = {
            options: [w.word + ' 的释义', '错误选项 1', '错误选项 2', '错误选项 3'],
            correctIndex: 0,
          }
        }
      }
      loadedCount.value = Math.min(i + BATCH_SIZE, words.length)
      loadingStatus.value = `正在生成释义选项... ${loadedCount.value}/${words.length}`
    }
  }
}

function selectOption(idx) {
  if (answered.value) return
  selectedOptionIdx.value = idx
  answered.value = true
  scheduleNext()
}

function scheduleNext() {
  if (isCurrentCorrect.value) {
    resultCorrect.value++
  }
  resultTotal.value++
  clearTimers()

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

async function tryAdvance() {
  if (currentIndex.value >= shuffledWords.value.length - 1) {
    phase.value = 'result'
    return
  }

  const nextWord = shuffledWords.value[currentIndex.value + 1]
  const nextOpts = nextWord ? optionsCache.value[nextWord.word] : null
  const isReady = nextOpts && nextOpts.options && nextOpts.options.length > 0

  if (isReady) {
    currentIndex.value++
    selectedOptionIdx.value = -1
    answered.value = false
  } else {
    // 选项未就绪，等待
    answered.value = true
    countdown.value = 0
    autoAdvanceTimer = setTimeout(() => tryAdvance(), 500)
  }
}

function endEarly() {
  clearTimers()
  phase.value = 'result'
}

function resetToStart() {
  clearTimers()
  phase.value = 'start'
  optionsCache.value = {}
  shuffledWords.value = []
  currentIndex.value = 0
  answered.value = false
  resultCorrect.value = 0
  resultTotal.value = 0
}

function clearTimers() {
  if (countdownTimer) { clearInterval(countdownTimer); countdownTimer = null }
  if (autoAdvanceTimer) { clearTimeout(autoAdvanceTimer); autoAdvanceTimer = null }
}

function getOptBtnClass(idx) {
  if (!answered.value) {
    return 'bg-gray-50 dark:bg-gray-700 border border-transparent hover:border-[#2563EB] hover:bg-blue-50 dark:hover:bg-blue-900/30 cursor-pointer text-gray-700 dark:text-gray-300'
  }
  if (idx === currentCorrectIndex.value) {
    return 'bg-green-50 dark:bg-green-900/30 border border-green-300 dark:border-green-700 text-green-700 dark:text-green-300'
  }
  if (idx === selectedOptionIdx.value && idx !== currentCorrectIndex.value) {
    return 'bg-red-50 dark:bg-red-900/30 border border-red-300 dark:border-red-700 text-red-600 dark:text-red-300'
  }
  return 'bg-gray-50 dark:bg-gray-700 border border-transparent opacity-50 text-gray-400 dark:text-gray-500'
}

function getOptDotClass(idx) {
  if (!answered.value) return 'bg-gray-200 dark:bg-gray-600 text-gray-500 dark:text-gray-400'
  if (idx === currentCorrectIndex.value) {
    return 'bg-green-500 text-white'
  }
  if (idx === selectedOptionIdx.value && idx !== currentCorrectIndex.value) {
    return 'bg-red-500 text-white'
  }
  return 'bg-gray-200 dark:bg-gray-600 text-gray-400 dark:text-gray-500'
}

function handleKeydown(e) {
  if (phase.value !== 'testing' || answered.value) return
  const key = e.key.toUpperCase()
  if (key === 'A' || key === 'B' || key === 'C' || key === 'D') {
    const idx = optionLabels.indexOf(key)
    const opts = currentOptions.value
    if (idx >= 0 && idx < opts.length) {
      selectOption(idx)
    }
  }
}

watch(() => props.visible, (val) => {
  if (val) {
    window.addEventListener('keydown', handleKeydown)
    phase.value = 'start'
    optionsCache.value = {}
    currentIndex.value = 0
    answered.value = false
    resultCorrect.value = 0
    resultTotal.value = 0
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
