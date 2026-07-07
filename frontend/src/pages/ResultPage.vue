<template>
  <main class="max-w-[1000px] mx-auto px-6 mt-12">
    <!-- 顶部结果卡片 -->
    <div ref="resultTopRef" class="card mb-8 overflow-hidden relative">
      <div class="absolute top-0 right-0 w-64 h-64 bg-blue-50/50 rounded-full -mr-20 -mt-20 z-0"></div>
      <div class="relative z-10 flex flex-col lg:flex-row items-center gap-12">
        <!-- 等级环形图 -->
        <div class="flex-none text-center">
          <div class="relative w-40 h-40 flex items-center justify-center mx-auto mb-4">
            <svg class="w-full h-full transform -rotate-90">
              <circle cx="80" cy="80" fill="transparent" r="72" stroke="#F3F4F6" stroke-width="12" />
              <circle
                cx="80" cy="80" fill="transparent" r="72"
                stroke="#2563EB" stroke-width="12"
                :stroke-dasharray="452"
                :stroke-dashoffset="ringOffset"
                stroke-linecap="round"
              />
            </svg>
            <div class="absolute flex flex-col items-center">
              <span class="text-4xl font-black text-[#2563EB]">{{ result.level }}</span>
              <span class="text-xs font-bold text-gray-400">CEFR 等级</span>
            </div>
          </div>
          <div class="text-sm font-bold text-gray-500">综合得分: {{ result.score }}/100</div>
        </div>

        <!-- 结果描述 -->
        <div class="flex-1">
          <div v-if="result.isMaxLevel && !result.tooLow" class="inline-flex items-center gap-2 px-3 py-1 bg-blue-50 text-blue-600 rounded-full text-xs font-bold mb-4">
            <Icon icon="ph:crown-simple-fill" />
            已达最高等级
          </div>
          <div v-else-if="result.leveledUp" class="inline-flex items-center gap-2 px-3 py-1 bg-yellow-50 text-yellow-600 rounded-full text-xs font-bold mb-4">
            <Icon icon="ph:crown-simple-bold" />
            成功晋级
          </div>
          <div v-else-if="result.tooLow" class="inline-flex items-center gap-2 px-3 py-1 bg-red-50 text-red-500 rounded-full text-xs font-bold mb-4">
            <Icon icon="ph:warning-circle-bold" />
            分数过低
          </div>
          <div v-else class="inline-flex items-center gap-2 px-3 py-1 bg-green-50 text-green-600 rounded-full text-xs font-bold mb-4">
            <Icon icon="ph:check-circle-bold" />
            测评已通过
          </div>
          <h1 v-if="result.isMaxLevel && !result.tooLow" class="text-3xl font-bold mb-4">🏆 你已达到最高等级 C2！</h1>
          <h1 v-else-if="result.leveledUp" class="text-3xl font-bold mb-4">🎉 恭喜！你已成功晋级至 {{ result.level }}！</h1>
          <h1 v-else-if="result.tooLow" class="text-3xl font-bold mb-4">很遗憾，本次得分偏低</h1>
          <h1 v-else class="text-3xl font-bold mb-4">测评完成！当前等级 {{ result.level }}</h1>
          <p v-if="result.tooLow && result.isMaxLevel" class="text-gray-500 leading-relaxed mb-2">
            继续努力，攻克 C2 难题吧！
          </p>
          <p v-else-if="result.tooLow" class="text-gray-500 leading-relaxed mb-2">
            你的得分（{{ result.score }} 分）低于 30 分，本次不会增加英语水平进度。继续努力，多加练习！
          </p>
          <p v-else-if="result.isMaxLevel" class="text-gray-500 leading-relaxed mb-2">
            你已达到 CEFR 最高等级 C2（精通），做题不会继续提高进度，但可以继续练习保持水平！
          </p>
          <p v-else class="text-gray-500 leading-relaxed mb-2">
            根据你的答题表现（{{ result.score }} 分），本次测评获得
            <span class="font-bold text-[#2563EB]">+{{ result.progressGained || 0 }}%</span> 英语水平进度。
          </p>
          <p v-if="result.leveledUp" class="text-green-600 font-bold leading-relaxed mb-6">
            🎊 进度已满，恭喜晋级至 {{ result.level }}！新阶段从 0% 开始，继续加油！
          </p>
          <p v-else-if="!result.tooLow && !result.isMaxLevel" class="text-gray-400 text-sm leading-relaxed mb-8">
            继续加油，距下一阶段 {{ result.nextLevel }} 还需积累更多进度。
          </p>
          <p v-else-if="result.isMaxLevel && !result.tooLow" class="text-blue-500 font-bold text-sm leading-relaxed mb-8">
            💪 C2 精通级别没有更高的等级了，继续保持，你就是英语大神！
          </p>
          <div class="flex items-center gap-4">
            <router-link
              :to="`/materials`"
              class="px-8 py-3 bg-[#2563EB] text-white rounded-xl font-bold shadow-lg shadow-blue-200 hover:scale-105 transition-transform inline-block"
            >
              {{ result.leveledUp ? '开启 ' + result.level + ' 学习之旅' : '返回读物匹配' }}
            </router-link>
          </div>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-12 gap-8">
      <!-- 左侧：雷达图 -->
      <div class="lg:col-span-5 flex flex-col gap-8">
        <div class="card h-full">
          <h3 class="text-lg font-bold mb-6">能力雷达图</h3>
          <div ref="radarChartRef" class="w-full h-80"></div>
        </div>
      </div>

      <!-- 右侧：分项能力 -->
      <div class="lg:col-span-7 flex flex-col gap-8">
        <div class="card">
          <h3 class="text-lg font-bold mb-8">分项能力详情</h3>
          <div class="space-y-8">
            <div v-for="item in abilityDetails" :key="item.label">
              <div class="flex justify-between items-center mb-2">
                <span class="text-sm font-bold">{{ item.label }}</span>
                <span class="text-sm font-bold" :class="item.colorClass">{{ item.percent }}%</span>
              </div>
              <div class="progress-bar">
                <div
                  class="progress-inner"
                  :class="item.barClass"
                  :style="{ width: item.percent + '%' }"
                ></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ========== 答题回顾（分页） ========== -->
    <div ref="reviewSectionRef" class="mt-12">
      <div class="flex items-center gap-3 mb-8">
        <Icon icon="ph:check-square-offset-bold" class="text-2xl text-[#2563EB]" />
        <h2 class="text-2xl font-bold">答题回顾</h2>
        <span class="text-sm text-gray-400 ml-2">
          共 {{ questionReview.length }} 题，
          答对 <strong class="text-green-500">{{ correctCount }}</strong> 题，
          答错 <strong class="text-red-500">{{ wrongCount }}</strong> 题
        </span>
      </div>

      <!-- 进度条：可点击题号 -->
      <div class="flex items-center justify-center gap-2 mb-8">
        <button
          class="w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold transition-all hover:scale-110 flex-none"
          :class="currentReviewIndex > 0
            ? 'bg-white border border-gray-200 text-gray-500 hover:border-[#2563EB] hover:text-[#2563EB] cursor-pointer'
            : 'bg-gray-100 text-gray-300 cursor-not-allowed'"
          :disabled="currentReviewIndex === 0"
          @click="goToQuestion(currentReviewIndex - 1)"
        >
          <Icon icon="ph:caret-left-bold" class="text-sm" />
        </button>

        <!-- 桌面端：窗口化题号 -->
        <div class="hidden lg:flex items-center gap-1.5 py-1 px-2">
          <button
            v-for="(item, idx) in visibleDots"
            :key="item.id"
            class="w-9 h-9 rounded-full flex items-center justify-center text-xs font-bold transition-all flex-none"
            :class="getProgressDotClass(visibleDotStart + idx, item)"
            @click="goToQuestion(visibleDotStart + idx)"
          >
            {{ item.id }}
          </button>
        </div>

        <!-- 移动端：可滑动题号条，自由滚动不换页，点击题号选中居中 -->
        <div
          ref="dotsStripRef"
          class="lg:hidden flex items-center gap-1.5 py-1 overflow-x-auto scroll-smooth no-scrollbar"
          :style="{ maxWidth: (5 * 36 + 4 * 6) + 'px' }"
          @scroll="onDotsScroll"
        >
          <button
            v-for="(item, idx) in questionReview"
            :key="item.id"
            class="w-9 h-9 rounded-full flex items-center justify-center text-xs font-bold transition-all flex-none shrink-0"
            :class="getProgressDotClass(idx, item)"
            @click="goToQuestion(idx)"
          >
            {{ item.id }}
          </button>
        </div>

        <button
          class="w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold transition-all hover:scale-110 flex-none"
          :class="currentReviewIndex < questionReview.length - 1
            ? 'bg-white border border-gray-200 text-gray-500 hover:border-[#2563EB] hover:text-[#2563EB] cursor-pointer'
            : 'bg-gray-100 text-gray-300 cursor-not-allowed'"
          :disabled="currentReviewIndex === questionReview.length - 1"
          @click="goToQuestion(currentReviewIndex + 1)"
        >
          <Icon icon="ph:caret-right-bold" class="text-sm" />
        </button>
      </div>

      <!-- 当前题目卡片（移动端支持触摸滑动切换） -->
      <Transition :name="isMobile ? 'card-slide-' + slideDirection : ''" mode="out-in">
      <div class="card border-l-4 select-none"
        :class="currentItem.isCorrect ? 'border-l-green-400' : 'border-l-red-400'"
        :key="currentReviewIndex"
        @touchstart="onTouchStart"
        @touchend="onTouchEnd"
      >
        <!-- 题号与结果 -->
        <div class="flex items-center justify-between mb-4">
          <div class="flex items-center gap-3">
            <span class="w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold"
              :class="currentItem.isCorrect
                ? 'bg-green-50 text-green-600'
                : 'bg-red-50 text-red-600'"
            >
              {{ currentItem.id }}
            </span>
            <span class="text-xs font-bold text-gray-400 uppercase tracking-widest">
              Question {{ currentItem.id }}
            </span>
          </div>
          <div class="flex items-center gap-3">
            <!-- 加入错题本按钮 -->
            <button
              class="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-bold transition-all duration-200"
              :class="isInWrongBookSet(currentItem.id)
                ? 'bg-amber-50 text-amber-500 border border-amber-200 cursor-default'
                : 'bg-white border border-gray-200 text-gray-400 hover:text-[#2563EB] hover:border-[#2563EB] hover:shadow-sm cursor-pointer'"
              :disabled="isInWrongBookSet(currentItem.id)"
              @click="addCurrentToWrongBook"
              :title="isInWrongBookSet(currentItem.id) ? '已在错题本中' : '加入错题本'"
            >
              <Icon
                :icon="isInWrongBookSet(currentItem.id) ? 'ph:check-bold' : 'ph:plus-bold'"
                class="text-sm"
              />
              {{ isInWrongBookSet(currentItem.id) ? '已加入' : '错题本' }}
            </button>
            <div class="flex items-center gap-2">
              <Icon
                :icon="currentItem.isCorrect ? 'ph:check-circle-fill' : 'ph:x-circle-fill'"
                class="text-xl"
                :class="currentItem.isCorrect ? 'text-green-500' : 'text-red-500'"
              />
              <span
                class="text-sm font-bold"
                :class="currentItem.isCorrect ? 'text-green-600' : 'text-red-600'"
              >
                {{ currentItem.isCorrect ? '回答正确' : '回答错误' }}
              </span>
            </div>
          </div>
        </div>

        <!-- 阅读文本 -->
        <div v-if="currentItem.passage" class="mb-4 p-4 bg-gray-50 rounded-xl">
          <p class="text-sm text-gray-500 leading-relaxed">{{ currentItem.passage }}</p>
        </div>

        <!-- 问题 -->
        <p class="font-bold text-lg mb-4">{{ currentItem.question }}</p>

        <!-- 选项 -->
        <div class="space-y-3 mb-4">
          <div
            v-for="opt in currentItem.options"
            :key="opt.id"
            class="flex items-center gap-3 p-3 rounded-xl text-sm"
            :class="getOptionClass(opt.id, currentItem)"
          >
            <div
              class="w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold flex-none"
              :class="getOptionDotClass(opt.id, currentItem)"
            >
              {{ opt.id }}
            </div>
            <span class="font-medium">{{ opt.text }}</span>
            <span v-if="opt.id === currentItem.correctAnswer" class="ml-auto text-xs font-bold text-green-600 dark:text-green-300">
              ✓ 正确答案
            </span>
            <span v-else-if="opt.id === currentItem.userAnswer && !currentItem.isCorrect" class="ml-auto text-xs font-bold text-red-500 dark:text-red-300">
              ✗ 你的答案
            </span>
            <span v-else-if="opt.id === currentItem.userAnswer && currentItem.isCorrect" class="ml-auto text-xs font-bold text-green-600 dark:text-green-300">
              ✓ 你的答案
            </span>
          </div>
        </div>

        <!-- 解析 -->
        <div class="p-4 rounded-xl"
          :class="currentItem.isCorrect
            ? 'bg-green-50 dark:bg-green-900/30 border border-green-100 dark:border-green-800'
            : 'bg-amber-50 dark:bg-amber-900/30 border border-amber-100 dark:border-amber-800'"
        >
          <div class="flex items-start gap-2">
            <Icon
              :icon="currentItem.isCorrect ? 'ph:lightbulb-fill' : 'ph:info-fill'"
              class="text-lg flex-none mt-0.5"
              :class="currentItem.isCorrect ? 'text-green-500' : 'text-amber-500'"
            />
            <div>
              <p class="text-xs font-bold mb-1"
                :class="currentItem.isCorrect ? 'text-green-700 dark:text-green-300' : 'text-amber-700 dark:text-amber-300'"
              >
                {{ currentItem.isCorrect ? '题目解析' : '错题解析' }}
              </p>
              <p class="text-sm leading-relaxed"
                :class="currentItem.isCorrect ? 'text-green-800 dark:text-green-200' : 'text-amber-800 dark:text-amber-200'"
              >
                {{ currentItem.explanation }}
              </p>
              <!-- 查看中文翻译 -->
              <button
                class="mt-2 text-xs transition-colors"
                :class="currentItem.isCorrect
                  ? 'text-green-600 hover:text-green-700 dark:text-green-400 dark:hover:text-green-300'
                  : 'text-amber-600 hover:text-amber-700 dark:text-amber-400 dark:hover:text-amber-300'"
                @click.stop="toggleReviewExplanationZh"
              >
                <Icon icon="ph:translate-bold" class="inline mr-0.5" />
                {{ showReviewExplanationZh ? '收起翻译' : '查看中文翻译' }}
              </button>
              <p
                v-if="showReviewExplanationZh"
                class="mt-1.5 text-sm leading-relaxed pl-2 border-l-2 border-blue-200 dark:border-blue-700"
                :class="currentItem.isCorrect ? 'text-green-700 dark:text-green-200' : 'text-amber-700 dark:text-amber-200'"
              >
                <template v-if="reviewExplanationState?.loading">
                  <Icon icon="ph:spinner-bold" class="inline animate-spin mr-1" />翻译中...
                </template>
                <template v-else-if="reviewExplanationState?.zh">{{ reviewExplanationState.zh }}</template>
                <template v-else-if="reviewExplanationState?.error">{{ reviewExplanationState.error }}</template>
              </p>
            </div>
          </div>
        </div>
      </div>
      </Transition>

      <!-- 底部翻页按钮 -->
      <div class="flex items-center justify-between mt-6">
        <button
          class="flex items-center gap-2 px-5 py-2.5 rounded-xl text-sm font-bold transition-all"
          :class="currentReviewIndex > 0
            ? 'bg-white border border-gray-200 text-gray-600 hover:border-[#2563EB] hover:text-[#2563EB]'
            : 'bg-gray-50 text-gray-300 cursor-not-allowed'"
          :disabled="currentReviewIndex === 0"
          @click="goToQuestion(currentReviewIndex - 1)"
        >
          <Icon icon="ph:arrow-left-bold" class="text-base" />
          上一题
        </button>

        <span class="text-sm text-gray-400 font-medium">
          {{ currentReviewIndex + 1 }} / {{ questionReview.length }}
        </span>

        <button
          class="flex items-center gap-2 px-5 py-2.5 rounded-xl text-sm font-bold transition-all"
          :class="currentReviewIndex < questionReview.length - 1
            ? 'bg-[#2563EB] text-white hover:bg-blue-600'
            : 'bg-gray-50 text-gray-300 cursor-not-allowed'"
          :disabled="currentReviewIndex === questionReview.length - 1"
          @click="goToQuestion(currentReviewIndex + 1)"
        >
          下一题
          <Icon icon="ph:arrow-right-bold" class="text-base" />
        </button>
      </div>

      <!-- 键盘提示 -->
      <p class="text-center text-xs text-gray-300 mt-4">
        使用 ← → 方向键或点击题号快速切换题目
      </p>
    </div>

    <!-- 桌面端：页面右下角上下滚动按钮 -->
    <div class="hidden lg:flex flex-col gap-2 fixed bottom-8 right-8 z-50">
      <button
        class="w-10 h-10 rounded-full bg-white dark:bg-gray-700 shadow-lg border border-gray-200 dark:border-gray-600 flex items-center justify-center hover:scale-110 transition-all text-gray-500 dark:text-gray-300"
        @click="scrollToTop"
        title="回到顶部"
      >
        <Icon icon="ph:caret-up-bold" class="text-lg" />
      </button>
      <button
        class="w-10 h-10 rounded-full bg-white dark:bg-gray-700 shadow-lg border border-gray-200 dark:border-gray-600 flex items-center justify-center hover:scale-110 transition-all text-gray-500 dark:text-gray-300"
        @click="scrollToReview"
        title="跳至答题回顾"
      >
        <Icon icon="ph:caret-down-bold" class="text-lg" />
      </button>
    </div>

  </main>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
let echartsMod = null
import { useAssessmentStore } from '@/stores/assessment'
import { useReaderStore } from '@/stores/reader'

const router = useRouter()
const assessmentStore = useAssessmentStore()
const readerStore = useReaderStore()
const storeResult = assessmentStore.assessmentResult

// 无结果时重定向
if (!storeResult) {
  router.replace('/assessment')
}

// ========== 答题回顾数据 ==========
const resultTopRef = ref(null)
const reviewSectionRef = ref(null)
const dotsStripRef = ref(null)
const currentReviewIndex = ref(0)
const slideDirection = ref('left')  // 移动端卡片滑动方向
let isProgrammaticScroll = false    // 程序化滚动守卫，防止 onDotsScroll 反馈循环

// 移动端检测
const isMobile = ref(false)
function updateIsMobile() { isMobile.value = window.innerWidth < 1024 }

const questionReview = computed(() => assessmentStore.questionReview)

const currentItem = computed(() =>
  questionReview.value[currentReviewIndex.value] || questionReview.value[0] || {}
)

const correctCount = computed(() =>
  questionReview.value.filter(q => q.isCorrect).length
)

const wrongCount = computed(() =>
  questionReview.value.filter(q => !q.isCorrect).length
)

// ========== 错题本 ==========
const wrongBookIds = ref(new Set())

// ========== 题目解析翻译 ==========
const showReviewExplanationZh = ref(false)
const reviewExplanationState = computed(() => {
  const item = currentItem.value
  if (!item?.id) return null
  const key = `review_${item.id}`
  return readerStore.getExplanationTranslation(key) || null
})

function toggleReviewExplanationZh() {
  showReviewExplanationZh.value = !showReviewExplanationZh.value
  if (showReviewExplanationZh.value) {
    const item = currentItem.value
    if (item?.explanation) {
      const key = `review_${item.id}`
      readerStore.fetchExplanationTranslation(key, item.explanation)
    }
  }
}

// 切换题目时重置翻译展开状态
watch(currentReviewIndex, () => {
  showReviewExplanationZh.value = false
})

/** 加载已在错题本中的题号 */
async function loadWrongBookIds() {
  try {
    const { getAllWrongQuestions } = await import('@/utils/wrongBookDB')
    const all = await getAllWrongQuestions()
    wrongBookIds.value = new Set(all.map(q => q.questionId))
  } catch (e) {
    console.error('加载错题本失败:', e)
  }
}

/** 判断某题是否已在错题本中 */
function isInWrongBookSet(questionId) {
  return wrongBookIds.value.has(questionId)
}

/** 将当前题目加入错题本 */
async function addCurrentToWrongBook() {
  const item = currentItem.value
  if (!item || !item.id) return
  try {
    const { addToWrongBook } = await import('@/utils/wrongBookDB')
    const result = await addToWrongBook({
      questionId: item.id,
      passage: item.passage || '',
      question: item.question || '',
      options: item.options || [],
      userAnswer: item.userAnswer || '',
      correctAnswer: item.correctAnswer || '',
      explanation: item.explanation || '',
    })
    if (result.success) {
      wrongBookIds.value = new Set([...wrongBookIds.value, item.id])
    }
  } catch (e) {
    console.error('加入错题本失败:', e)
  }
}

/** 跳转到指定题号的题目 */
function goToQuestion(index) {
  if (index < 0 || index >= questionReview.value.length) return
  // 仅当索引变化时更新方向和索引（避免同索引触发无意义的状态更新）
  if (index !== currentReviewIndex.value) {
    slideDirection.value = index > currentReviewIndex.value ? 'left' : 'right'
    currentReviewIndex.value = index
  }
  // 移动端：滚动题号条使选中项居中，同时取消自动归位计时器
  if (dotsStripRef.value && isMobile.value) {
    clearAutoReturnTimer()
    isProgrammaticScroll = true
    nextTick(() => {
      const dot = dotsStripRef.value.children[index]
      if (dot) {
        dot.scrollIntoView({ behavior: 'smooth', block: 'nearest', inline: 'center' })
      }
      // 延时清除守卫，确保 scrollIntoView 触发的 scroll 事件不会启动归位计时器
      setTimeout(() => { isProgrammaticScroll = false }, 500)
    })
  }
}

/** 移动端题号条滚动事件：只滚动不选页 → 启动 5s 自动归位计时器 */
let dotsScrollTimer = null
let autoReturnTimer = null

function onDotsScroll() {
  // 跳过程序化滚动（goToQuestion 触发的 scrollIntoView）
  if (!dotsStripRef.value || !isMobile.value || isProgrammaticScroll) return
  // 每次用户滚动时重置计时器
  clearAutoReturnTimer()
  clearTimeout(dotsScrollTimer)
  dotsScrollTimer = setTimeout(() => {
    // 滚动停止后启动 5s 归位计时器
    autoReturnTimer = setTimeout(() => {
      const strip = dotsStripRef.value
      if (!strip) return
      const idx = currentReviewIndex.value
      const dot = strip.children[idx]
      if (dot) {
        isProgrammaticScroll = true
        dot.scrollIntoView({ behavior: 'smooth', block: 'nearest', inline: 'center' })
        setTimeout(() => { isProgrammaticScroll = false }, 500)
      }
    }, 3000)
  }, 250)
}

function clearAutoReturnTimer() {
  if (autoReturnTimer) {
    clearTimeout(autoReturnTimer)
    autoReturnTimer = null
  }
}

// 移动端触摸滑动切换题目
let touchStartX = 0
function onTouchStart(e) { touchStartX = e.touches[0].clientX }
function onTouchEnd(e) {
  const diff = touchStartX - e.changedTouches[0].clientX
  if (Math.abs(diff) > 60) {
    if (diff > 0) goToQuestion(currentReviewIndex.value + 1)
    else goToQuestion(currentReviewIndex.value - 1)
  }
}

/** 进度条圆点样式 */
function getProgressDotClass(idx, item) {
  const isCurrent = idx === currentReviewIndex.value
  const base = 'transition-all hover:scale-110'

  if (isCurrent) {
    return `${base} bg-[#2563EB] text-white shadow-md shadow-blue-200 scale-110`
  }
  if (item.isCorrect) {
    return `${base} bg-green-50 dark:bg-green-900/30 text-green-600 dark:text-green-300 border border-green-200 dark:border-green-700 cursor-pointer`
  }
  return `${base} bg-red-50 dark:bg-red-900/30 text-red-600 dark:text-red-300 border border-red-200 dark:border-red-700 cursor-pointer`
}

// 滑动窗口：一次展示 5 个题号圆点，选中项居中（首尾两题除外）
const DOT_WINDOW = 5
const visibleDotStart = computed(() => {
  const total = questionReview.value.length
  if (total <= DOT_WINDOW) return 0
  const idx = currentReviewIndex.value
  let start = idx - 2
  if (start < 0) start = 0
  if (start + DOT_WINDOW > total) start = total - DOT_WINDOW
  return start
})
const visibleDots = computed(() => {
  const start = visibleDotStart.value
  return questionReview.value.slice(start, start + DOT_WINDOW)
})

/** 滚动到顶部 */
function scrollToTop() {
  if (resultTopRef.value) {
    resultTopRef.value.scrollIntoView({ behavior: 'smooth', block: 'start' })
  } else {
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

/** 滚动到答题回顾区域 */
function scrollToReview() {
  if (reviewSectionRef.value) {
    reviewSectionRef.value.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

/** 键盘左右方向键切换题目 */
function handleReviewKeydown(e) {
  if (e.key === 'ArrowLeft') {
    e.preventDefault()
    goToQuestion(currentReviewIndex.value - 1)
  } else if (e.key === 'ArrowRight') {
    e.preventDefault()
    goToQuestion(currentReviewIndex.value + 1)
  }
}

/**
 * 选项卡片样式
 */
function getOptionClass(optionId, item) {
  const isUserAnswer = optionId === item.userAnswer
  const isCorrectAnswer = optionId === item.correctAnswer

  if (isCorrectAnswer && isUserAnswer) {
    // 答对了 — 绿色高亮
    return 'bg-green-50 dark:bg-green-900/30 border border-green-300 dark:border-green-700'
  }
  if (isCorrectAnswer && !isUserAnswer) {
    // 正确答案（用户选错了）— 绿色边框提示
    return 'bg-green-50 dark:bg-green-900/30 border border-green-300 dark:border-green-700'
  }
  if (isUserAnswer && !isCorrectAnswer) {
    // 用户选错了 — 红色高亮
    return 'bg-red-50 dark:bg-red-900/30 border border-red-300 dark:border-red-700'
  }
  // 普通选项
  return 'bg-gray-50 dark:bg-gray-700/50 border border-transparent'
}

/**
 * 选项圆点样式
 */
function getOptionDotClass(optionId, item) {
  const isUserAnswer = optionId === item.userAnswer
  const isCorrectAnswer = optionId === item.correctAnswer

  if (isCorrectAnswer) {
    return 'bg-green-500 text-white'
  }
  if (isUserAnswer && !isCorrectAnswer) {
    return 'bg-red-500 text-white'
  }
  return 'bg-gray-200 text-gray-500'
}

// ========== 结果数据 ==========
const result = computed(() => ({
  level: storeResult?.level || 'A1',
  nextLevel: storeResult?.nextLevel || 'A2',
  score: storeResult?.score || 0,
  leveledUp: storeResult?.leveledUp || false,
  progressGained: storeResult?.progressGained || 0,
  tooLow: storeResult?.tooLow || false,
  isMaxLevel: storeResult?.isMaxLevel || false,
}))

// 分项能力详情（从 store 动态读取）
const abilityDetails = computed(() => {
  const dims = storeResult?.dimensions || {}
  return [
    { label: '词汇量 (Vocabulary)', percent: dims.vocabulary || 0, colorClass: 'text-[#2563EB]', barClass: 'bg-[#2563EB]' },
    { label: '语法掌握度 (Grammar)', percent: dims.grammar || 0, colorClass: 'text-[#2563EB]', barClass: 'bg-[#2563EB]' },
    { label: '阅读理解 (Reading)', percent: dims.reading || 0, colorClass: 'text-[#2563EB]', barClass: 'bg-[#2563EB]' },
    { label: '文化背景 (Culture)', percent: dims.culture || 0, colorClass: 'text-[#F59E0B]', barClass: 'bg-[#F59E0B]' },
    { label: '逻辑分析 (Logic)', percent: dims.logic || 0, colorClass: 'text-[#10B981]', barClass: 'bg-[#10B981]' },
  ]
})

// 环形进度条的 stroke-dashoffset 动态计算
const ringOffset = computed(() => {
  const circumference = 452 // 2 * PI * 72 ≈ 452
  return circumference - (circumference * result.score / 100)
})

// ========== ECharts 雷达图 ==========
const radarChartRef = ref(null)
let radarChart = null

async function initRadarChart() {
  if (!radarChartRef.value) return
  if (!echartsMod) echartsMod = await import('echarts')
  radarChart = echartsMod.init(radarChartRef.value)
  const dims = storeResult?.dimensions || {}
  const option = {
    radar: {
      indicator: [
        { name: '词汇', max: 100 },
        { name: '语法', max: 100 },
        { name: '阅读理解', max: 100 },
        { name: '文化背景', max: 100 },
        { name: '逻辑分析', max: 100 },
      ],
      splitArea: { show: false },
      axisLine: { lineStyle: { color: '#E5E7EB' } },
      splitLine: { lineStyle: { color: '#E5E7EB' } },
    },
    series: [{
      type: 'radar',
      data: [{
        value: [
          dims.vocabulary || 0,
          dims.grammar || 0,
          dims.reading || 0,
          dims.culture || 0,
          dims.logic || 0,
        ],
        name: '能力评估',
        itemStyle: { color: '#2563EB' },
        areaStyle: { color: 'rgba(37, 99, 235, 0.2)' },
        lineStyle: { width: 3 },
      }],
    }],
  }
  radarChart.setOption(option)
  window.addEventListener('resize', handleResize)
}

function handleResize() {
  if (radarChart) radarChart.resize()
}

onMounted(async () => {
  updateIsMobile()
  window.addEventListener('resize', updateIsMobile)
  await nextTick()
  initRadarChart()
  loadWrongBookIds()
  window.addEventListener('keydown', handleReviewKeydown)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  window.removeEventListener('resize', updateIsMobile)
  window.removeEventListener('keydown', handleReviewKeydown)
  clearAutoReturnTimer()
  if (radarChart) {
    radarChart.dispose()
    radarChart = null
  }
})
</script>

<style scoped>
/* 移动端卡片滑动切换动画 */
.card-slide-left-enter-active,
.card-slide-left-leave-active,
.card-slide-right-enter-active,
.card-slide-right-leave-active {
  transition: all 0.25s ease-out;
}
.card-slide-left-enter-from {
  transform: translateX(60px);
  opacity: 0;
}
.card-slide-left-leave-to {
  transform: translateX(-60px);
  opacity: 0;
}
.card-slide-right-enter-from {
  transform: translateX(-60px);
  opacity: 0;
}
.card-slide-right-leave-to {
  transform: translateX(60px);
  opacity: 0;
}

/* 移动端题号条隐藏滚动条 */
.no-scrollbar::-webkit-scrollbar { display: none; }
.no-scrollbar { -ms-overflow-style: none; scrollbar-width: none; }
</style>
