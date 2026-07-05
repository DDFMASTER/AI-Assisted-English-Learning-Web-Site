<template>
  <div class="min-h-screen">
    <!-- 沉浸式导航栏 -->
    <nav class="sticky top-16 z-40 bg-white/60 backdrop-blur-md border-b border-gray-100">
      <div class="max-w-[1200px] mx-auto px-6 h-16 flex items-center justify-between">
        <div class="flex items-center gap-4">
          <router-link to="/materials" class="p-2 hover:bg-gray-100 rounded-full transition-colors">
            <Icon icon="ph:arrow-left-bold" class="text-xl" />
          </router-link>
          <div class="h-6 w-px bg-gray-200"></div>
          <h2 class="font-bold truncate max-w-[400px]">{{ readerStore.articleTitle }}</h2>
        </div>
        <div class="flex items-center gap-1">
          <!-- AI 面板切换（第一个） -->
          <div class="relative group">
            <button
              class="p-2 transition-colors rounded-lg"
              :class="showSidePanel ? 'text-[#2563EB] bg-blue-50' : 'text-gray-400 hover:text-[#2563EB]'"
              @click="showSidePanel = !showSidePanel"
            >
              <Icon icon="ph:brain-bold" class="text-2xl" />
            </button>
            <div class="absolute top-full mt-2 left-1/2 -translate-x-1/2 bg-gray-800 text-white text-xs rounded-lg px-3 py-1.5 whitespace-nowrap opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none z-[60]">
              AI 文化讲解 & 选择题
            </div>
          </div>

          <!-- 字体设置（第二个） -->
          <div class="relative group">
            <button
              class="p-2 transition-colors rounded-lg"
              :class="showFontPanel ? 'text-[#2563EB] bg-blue-50' : 'text-gray-400 hover:text-[#2563EB]'"
              @click.stop="toggleFontPanel"
            >
              <Icon icon="ph:text-aa-bold" class="text-2xl" />
            </button>
            <div class="absolute top-full mt-2 left-1/2 -translate-x-1/2 bg-gray-800 text-white text-xs rounded-lg px-3 py-1.5 whitespace-nowrap opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none z-[60]">
              字体设置
            </div>
          </div>

          <!-- 书签 / 收藏夹（第三个） -->
          <div class="relative group">
            <button
              class="p-2 transition-colors rounded-lg"
              :class="readerStore.isFavorited ? 'text-yellow-500' : 'text-gray-400 hover:text-gray-600'"
              @click.stop="guard(handleFavorite)"
            >
              <Icon
                :icon="readerStore.isFavorited ? 'ph:bookmark-simple-fill' : 'ph:bookmark-simple-bold'"
                class="text-2xl"
              />
            </button>
            <div class="absolute top-full mt-2 left-1/2 -translate-x-1/2 bg-gray-800 text-white text-xs rounded-lg px-3 py-1.5 whitespace-nowrap opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none z-[60]">
              {{ readerStore.isFavorited ? '已收藏 · 点击取消' : '添加至收藏夹' }}
            </div>
          </div>
        </div>
      </div>
    </nav>

    <!-- 阅读区域 -->
    <main class="reader-container" style="max-width:800px;margin:0 auto" v-if="readerStore.article">
      <header class="mb-12">
        <!-- 分类/阅读时间/难度（无数据时不展示） -->
        <div
          v-if="readerStore.article.difficulty || readerStore.article.readTime"
          class="flex items-center gap-2 text-sm text-gray-400 font-medium mb-4"
        >
          <template v-if="readerStore.article.difficulty">
            <span class="text-[#2563EB]">{{ readerStore.article.difficulty }}</span>
            <span v-if="readerStore.article.readTime">•</span>
          </template>
          <span v-if="readerStore.article.readTime">{{ readerStore.article.readTime }}</span>
        </div>

        <!-- 标题（无数据时不展示） -->
        <h1
          v-if="readerStore.article.title"
          class="text-4xl font-bold leading-tight mb-8"
        >{{ readerStore.article.title }}</h1>

        <!-- 作者信息（无数据时不展示） -->
        <div v-if="readerStore.article.author" class="flex items-center gap-3 mb-12">
          <div class="w-10 h-10 rounded-full bg-gradient-to-br from-blue-400 to-purple-500 flex items-center justify-center text-white text-xs font-bold flex-none">
            {{ (readerStore.article.authorAvatar || readerStore.article.author).charAt(0).toUpperCase() }}
          </div>
          <div>
            <div class="text-sm font-bold">{{ readerStore.article.author }}</div>
            <div v-if="readerStore.article.publishDate" class="text-xs text-gray-400">
              Published on {{ readerStore.article.publishDate }}
            </div>
          </div>
        </div>
      </header>

      <article id="articleContent">
        <!-- 文章段落（按 \\n\\n 分割） -->
        <template v-for="(paragraph, pIdx) in paragraphs" :key="pIdx">
          <p
            class="article-p"
            :class="{ 'cursor-pointer hover:bg-blue-50/30 rounded-lg transition-colors px-2 -mx-2': readerStore.showTranslation }"
            :style="{ fontSize: readerStore.fontSize + 'px' }"
            @click="(e) => handleParagraphClick(pIdx, e)"
          >
            <!-- 段落内混合文本、交互单词、文化注解 -->
            <template v-for="(segment, sIdx) in paragraph.segments" :key="sIdx">
              <span v-if="segment.type === 'text'" v-html="parseInlineMarkdown(segment.text)"></span>
              <span
                v-else-if="segment.type === 'word'"
                class="interactive-word"
                @click="(e) => handleWordClick(segment.data, e)"
              >
                {{ segment.data.word }}
              </span>
              <span
                v-else-if="segment.type === 'culture'"
                class="culture-mark"
                @click="(e) => handleCultureClick(segment.data, e)"
              >
                ★
              </span>
            </template>
          </p>

          <!-- 苏格拉底式提问卡片（在第二个段落后） -->
          <div
            v-if="pIdx === 1 && readerStore.article.socraticQuestion"
            class="my-12 socratic-card border-l-4 border-l-[#2563EB]"
          >
            <div class="flex items-start gap-4">
              <div class="w-12 h-12 rounded-full bg-blue-50 flex items-center justify-center text-[#2563EB] flex-none">
                <Icon icon="ph:chats-circle-bold" class="text-2xl" />
              </div>
              <div>
                <h4 class="text-lg font-bold mb-3">🤔 AI 深度思考</h4>
                <p class="text-gray-600 leading-relaxed mb-6">
                  {{ readerStore.article.socraticQuestion }}
                </p>
                <div class="flex items-center gap-3">
                  <button
                    class="px-6 py-2.5 bg-[#2563EB] text-white rounded-xl text-sm font-bold shadow-lg shadow-blue-200 hover:bg-blue-600 transition-all"
                    @click="guard(() => { showAnswerInput = true })"
                  >
                    写下你的想法
                  </button>
                  <button
                    class="px-6 py-2.5 bg-gray-50 text-gray-500 rounded-xl text-sm font-bold hover:bg-gray-100 transition-all"
                    @click="guard(skipQuestion)"
                  >
                    稍后回答
                  </button>
                </div>

                <!-- 回答输入框 -->
                <div v-if="showAnswerInput" class="mt-4">
                  <textarea
                    v-model="answerText"
                    class="w-full h-24 p-3 border border-gray-200 rounded-xl text-sm resize-none focus:outline-none focus:border-[#2563EB]"
                    placeholder="写下你的想法..."
                  ></textarea>
                  <button
                    class="mt-2 px-4 py-2 bg-[#2563EB] text-white rounded-lg text-xs font-bold"
                    @click="guard(submitAnswer)"
                  >
                    提交
                  </button>
                </div>
              </div>
            </div>
          </div>
        </template>
      </article>

      <!-- 底部留白，防止被操作栏遮挡 -->
      <div class="h-28"></div>
    </main>

    <!-- 加载中 -->
    <div v-else class="text-center py-40">
      <Icon icon="ph:spinner-bold" class="text-4xl text-[#2563EB] animate-spin mx-auto mb-4" />
      <p class="text-gray-400">加载文章中...</p>
    </div>

    <!-- 底部固定操作栏（单词/段落翻译切换） -->
    <div class="fixed bottom-0 left-0 right-0 z-50 bg-white/80 backdrop-blur-md border-t border-gray-100">
      <div class="max-w-[800px] mx-auto px-6 py-2 flex flex-col items-center">
        <!-- 翻译模式切换 -->
        <div class="flex items-center bg-gray-100 rounded-full p-0.5">
          <button
            class="px-5 py-1.5 rounded-full text-xs font-bold transition-all"
            :class="translateMode === 'word'
              ? 'bg-white text-[#2563EB] shadow-sm'
              : 'text-gray-400 hover:text-gray-600'"
            @click="setTranslateMode('word')"
          >
            <Icon icon="ph:text-aa-bold" class="inline mr-1" />
            单词翻译
          </button>
          <button
            class="px-5 py-1.5 rounded-full text-xs font-bold transition-all"
            :class="translateMode === 'paragraph'
              ? 'bg-white text-[#2563EB] shadow-sm'
              : 'text-gray-400 hover:text-gray-600'"
            @click="guard(() => setTranslateMode('paragraph'))"
          >
            <Icon icon="ph:article-bold" class="inline mr-1" />
            段落翻译
          </button>
        </div>
        <!-- 操作提示 -->
        <p class="text-[10px] text-gray-400 mt-1.5">
          {{ translateMode === 'word' ? '点击想要翻译的单词即可一键AI翻译' : '点击想要翻译的段落即可一键AI翻译' }}
        </p>
      </div>
    </div>

    <!-- 单词查词浮窗 -->
    <WordPopover
      :visible="wordPopover.visible"
      :word="wordPopover.word"
      :loading="wordPopover.loading"
      :position="wordPopover.position"
      :detail-visible="showAIDetail"
      :detail-data="aiDetailData"
      @close="wordPopover.visible = false"
      @add-vocab="onAddVocab"
      @view-detail="onViewDetail"
      @back-to-summary="onBackToSummary"
    />

    <!-- 文化注解浮窗 -->
    <CulturePopover
      :visible="culturePopover.visible"
      :title="culturePopover.title"
      :content="culturePopover.content"
      :position="culturePopover.position"
    />

    <!-- 段落翻译浮窗 -->
    <ParagraphTranslationPopover
      :visible="showTranslationPopover"
      :original-text="currentTranslationOriginal"
      :translation-data="currentTranslationData"
      :position="translationPosition"
      @close="showTranslationPopover = false"
    />

    <!-- 文章右侧 AI 面板（文化背景 + 选择题） -->
    <ArticleSidePanel
      :visible="showSidePanel"
      :cultural-notes="readerStore.culturalNotesCache"
      :quiz-data="readerStore.quizCache"
      :position="{ x: sidePanelX, y: 175 }"
      @quiz-completed="onQuizCompleted"
    />

    <!-- 字体设置面板（与 AI 面板等宽，点击字体按钮后打开） -->
    <div
      v-if="showFontPanel"
      class="fixed z-[56] w-[300px]"
      :style="{ right: sidePanelX + 'px', top: '175px' }"
      @click.stop
    >
      <div class="glass-popover p-5">
        <div class="flex items-center justify-between mb-4">
          <div class="flex items-center gap-2">
            <Icon icon="ph:text-aa-bold" class="text-[#2563EB] text-lg" />
            <h3 class="text-sm font-bold text-gray-700">字体设置</h3>
          </div>
          <button
            class="w-7 h-7 rounded-full bg-gray-100 flex items-center justify-center hover:bg-gray-200 transition-colors"
            @click="showFontPanel = false"
          >
            <Icon icon="ph:x-bold" class="text-xs text-gray-500" />
          </button>
        </div>

        <!-- 当前字号预览 -->
        <div class="text-center mb-5">
          <span class="text-4xl font-bold text-[#2563EB]">{{ readerStore.fontSize }}</span>
          <span class="text-sm text-gray-400 ml-1">px</span>
        </div>

        <!-- 字号调节按钮 -->
        <div class="flex items-center justify-center gap-3 mb-5">
          <button
            class="w-10 h-10 rounded-full bg-gray-100 text-gray-500 hover:bg-gray-200 hover:text-gray-700 active:scale-95 transition-all flex items-center justify-center text-sm font-bold"
            @click="adjustFontSize(-1)"
            :disabled="readerStore.fontSize <= 12"
          >−1</button>
          <button
            class="w-10 h-10 rounded-full bg-gray-100 text-gray-500 hover:bg-gray-200 hover:text-gray-700 active:scale-95 transition-all flex items-center justify-center text-sm font-bold"
            @click="adjustFontSize(1)"
            :disabled="readerStore.fontSize >= 28"
          >+1</button>
        </div>

        <!-- 快捷预设 -->
        <div class="mb-5">
          <div class="text-xs text-gray-400 mb-2">快捷预设</div>
          <div class="flex flex-wrap gap-2">
            <button
              v-for="size in [14, 16, 18, 20, 22, 24]"
              :key="size"
              class="px-3 py-1.5 rounded-lg text-xs font-bold transition-all"
              :class="readerStore.fontSize === size
                ? 'bg-[#2563EB] text-white shadow-sm'
                : 'bg-gray-50 text-gray-500 hover:bg-gray-100'"
              @click="readerStore.setFontSize(size)"
            >{{ size }}px</button>
          </div>
        </div>

        <!-- 示例文字 -->
        <div class="p-3 bg-gray-50 rounded-xl">
          <p class="text-gray-400 text-xs mb-1">预览效果</p>
          <p class="text-gray-700 leading-relaxed" :style="{ fontSize: readerStore.fontSize + 'px' }">
            The quick brown fox jumps over the lazy dog.
          </p>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, reactive, watch, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import { useReaderStore } from '@/stores/reader'
import { useTaskStore } from '@/stores/task'
import { useUserStore } from '@/stores/user'
import WordPopover from '@/components/WordPopover.vue'
import CulturePopover from '@/components/CulturePopover.vue'
import ArticleSidePanel from '@/components/ArticleSidePanel.vue'
import ParagraphTranslationPopover from '@/components/ParagraphTranslationPopover.vue'
import { useRequireAuth } from '@/composables/useAuth'
import { addToHistory } from '@/utils/historyDB'
import { canUseAIArticle, recordAIArticle, remainingAIArticles } from '@/utils/dailyLimit'
import { parseInlineMarkdown } from '@/utils/markdown'
import { tokenizeText } from '@/utils/tokenize.js'

const route = useRoute()
const router = useRouter()
const readerStore = useReaderStore()
const taskStore = useTaskStore()
const userStore = useUserStore()
const { guard } = useRequireAuth()

// ========== 段落解析 ==========
const paragraphs = computed(() => {
  if (!readerStore.article?.content) return []
  let text = readerStore.article.content

  // 1. 统一换行符
  text = text.replace(/\r\n/g, '\n').replace(/\r/g, '\n')

  // 2. 策略A：按空行（连续换行）分段
  let parts = text.split(/\n\s*\n/)

  // 3. 策略B：如果策略A只产出1段，尝试按单换行分段
  if (parts.length <= 1) {
    parts = text.split(/\n+/)
  }

  // 4. 策略C：如果还是只有1段且内容很长，按句号+空格强制断句
  parts = parts.flatMap(p => {
    const trimmed = p.trim()
    if (!trimmed) return []
    // 超过500字符的段落，在句号处断句
    if (trimmed.length > 500) {
      const sentences = trimmed.split(/(?<=[.!?])\s+(?=[A-Z])/)
      if (sentences.length > 1) {
        // 每2-3句合并为一段
        const groups = []
        for (let i = 0; i < sentences.length; i += 3) {
          groups.push(sentences.slice(i, i + 3).join(' '))
        }
        return groups
      }
    }
    return [trimmed]
  })

  return parts
    .map(p => p.trim())
    .filter(p => p.length > 0)
    .map(paraText => ({
      segments: tokenizeText(paraText)
    }))
})

// ========== 浮窗状态 ==========
const wordPopover = reactive({
  visible: false,
  loading: false,
  word: { word: '', results: [] },
  position: { x: 0, y: 0 },
})

const culturePopover = reactive({
  visible: false,
  title: '',
  content: '',
  position: { x: 0, y: 0 },
})

// ========== AI 例句详情 ==========
const showAIDetail = ref(false)
const aiDetailWord = ref('')
const showSidePanel = ref(true)

// ========== 段落翻译状态 ==========
const showTranslationPopover = ref(false)
const activeTranslationIndex = ref(-1)
const translationPosition = reactive({ x: 0, y: 0 })

// 当前激活段落的翻译数据
const currentTranslationData = computed(() => {
  if (activeTranslationIndex.value < 0) return null
  const key = `${readerStore.article?.id}_${activeTranslationIndex.value}`
  return readerStore.paragraphTranslations[key] || null
})

// 当前激活段落的原文
const currentTranslationOriginal = computed(() => {
  if (activeTranslationIndex.value < 0) return ''
  const para = paragraphs.value[activeTranslationIndex.value]
  if (!para) return ''
  return para.segments
    .map(s => s.text || (s.data?.word || ''))
    .join('')
})

// 右侧面板位置：放在文章右边缘与滚动条之间，不遮挡文本和滚动条
const PANEL_WIDTH = 300
const SCROLLBAR = 17   // 浏览器滚动条宽度
const GAP = 20         // 面板与文章之间的间距
const sidePanelX = computed(() => {
  // 文章右边缘到窗口右边缘的距离
  const marginR = Math.floor((window.innerWidth - 800) / 2)
  // right = 文章右边缘偏移 - 面板宽 - 间距，但不小于滚动条宽度
  return Math.max(SCROLLBAR, marginR - PANEL_WIDTH - GAP)
})
const aiDetailData = computed(() => {
  if (!aiDetailWord.value) return null
  return readerStore.getAIExamples(aiDetailWord.value) || { examples: [], loading: true }
})

// ========== 苏格拉底提问 ==========
const showAnswerInput = ref(false)
const answerText = ref('')

// ========== 目录和设置面板 ==========
const showToc = ref(false)
const showSettings = ref(false)
const showFontPanel = ref(false)

// ========== 翻译模式 ==========
const translateMode = ref('word')  // 'word' | 'paragraph'

function setTranslateMode(mode) {
  translateMode.value = mode
  readerStore.showTranslation = mode === 'paragraph'
}

// ========== 字体调节 ==========
function toggleFontPanel() {
  // 关闭 AI 面板
  if (showSidePanel.value) {
    showSidePanel.value = false
  }
  showFontPanel.value = !showFontPanel.value
}

function adjustFontSize(delta) {
  readerStore.setFontSize(readerStore.fontSize + delta)
}

function resetFontSize() {
  readerStore.setFontSize(18)
}

// ========== 单词点击 ==========
function handleWordClick(wordData, event) {
  // 段落翻译模式下不触发单词弹窗
  if (readerStore.showTranslation) return
  event.stopPropagation()
  const articleEl = document.querySelector('.reader-container')
  const articleRect = articleEl ? articleEl.getBoundingClientRect() : null

  // 弹窗宽度 320px (w-80)，固定放在文章左侧
  const popoverWidth = 320
  const gap = 20
  const left = articleRect
    ? Math.max(10, articleRect.left - popoverWidth - gap)
    : 10

  // 垂直位置：固定距页面顶端 140px
  const top = 140

  // 立即显示弹窗（加载状态）
  showAIDetail.value = false  // 新单词默认展示释义视图
  aiDetailWord.value = ''
  wordPopover.word = { word: wordData.word, results: [] }
  wordPopover.loading = true
  wordPopover.position = { x: left, y: top }
  wordPopover.visible = true
  culturePopover.visible = false

  // 异步查询
  lookupWordForPopover(wordData.word)
}

async function lookupWordForPopover(word) {
  const studyPurpose = userStore.user?.studyPurpose || ''
  const result = await readerStore.lookupWord(word, studyPurpose)

  // 竞态保护：弹窗已被其他单词覆盖则忽略
  if (wordPopover.word.word.toLowerCase() !== word.toLowerCase()) {
    return
  }

  if (result.success) {
    wordPopover.word = {
      word: result.word,
      phonetic: result.phonetic || '',
      results: result.results || [],
      found: result.found,
      crossStage: result.crossStage || false,
      lemmaFrom: result.lemmaFrom || '',
      lemmaTo: result.lemmaTo || '',
    }

    // 后台预取 AI 例句（不阻塞释义展示）
    readerStore.prefetchAIExamples(result.word || word)
  } else {
    wordPopover.word = {
      word: word,
      results: [],
      found: false,
      error: result.message || '查询失败',
    }
  }
  wordPopover.loading = false
}

// ========== 文化注解点击 ==========
function handleCultureClick(data, event) {
  event.stopPropagation()
  const articleEl = document.querySelector('.reader-container')
  const articleRect = articleEl ? articleEl.getBoundingClientRect() : null

  const popoverWidth = 320
  const gap = 20
  const left = articleRect
    ? Math.max(10, articleRect.left - popoverWidth - gap)
    : 10

  // 垂直位置：固定距页面顶端 140px
  const top = 140

  culturePopover.title = data.title
  culturePopover.content = data.content
  culturePopover.position = { x: left, y: top }
  culturePopover.visible = true
  wordPopover.visible = false
}

// ========== 段落点击（翻译模式） ==========
function handleParagraphClick(pIdx, event) {
  if (!readerStore.showTranslation) return  // 仅在段落翻译模式下生效
  event.stopPropagation()

  const articleEl = document.querySelector('.reader-container')
  const articleRect = articleEl ? articleEl.getBoundingClientRect() : null

  // 弹窗宽度 320px (w-80)，固定放在文章左侧
  const popoverWidth = 320
  const gap = 20
  const left = articleRect
    ? Math.max(10, articleRect.left - popoverWidth - gap)
    : 10

  // 垂直位置：固定距页面顶端 140px
  const top = 140

  translationPosition.x = left
  translationPosition.y = top
  activeTranslationIndex.value = pIdx
  showTranslationPopover.value = true
  culturePopover.visible = false
  wordPopover.visible = false

  // 获取段落原文并请求翻译
  const para = paragraphs.value[pIdx]
  if (para) {
    const paraText = para.segments
      .map(s => s.text || (s.data?.word || ''))
      .join('')
    readerStore.fetchParagraphTranslation(
      readerStore.article?.id, pIdx, paraText
    )
  }
}

// ========== 其他操作 ==========
async function handleFavorite() {
  await readerStore.toggleFavorite()
}

function handleTTS() {
  if ('speechSynthesis' in window) {
    const utterance = new SpeechSynthesisUtterance(readerStore.article?.content?.slice(0, 500))
    utterance.lang = 'en-US'
    speechSynthesis.speak(utterance)
  } else {
    alert('您的浏览器不支持语音朗读')
  }
}

function scrollToNextParagraph() {
  // 更新进度
  readerStore.updateProgress(readerStore.readingProgress + 15)
  window.scrollBy({ top: 400, behavior: 'smooth' })
}

function skipQuestion() {
  showAnswerInput.value = false
}

function submitAnswer() {
  // TODO: 提交回答到后端
  alert('你的想法已记录！')
  answerText.value = ''
  showAnswerInput.value = false
}

function onAddVocab(word) {
  // 已在 store 中处理
}

function onViewDetail(wordData) {
  const word = (wordData.word || wordData).toLowerCase().trim()
  aiDetailWord.value = word
  showAIDetail.value = true

  // 确保已触发预取（正常情况已在 lookupWordForPopover 中触发）
  if (!readerStore.getAIExamples(word)) {
    readerStore.prefetchAIExamples(word)
  }
}

function onBackToSummary() {
  showAIDetail.value = false
  aiDetailWord.value = ''
}

// ========== 答题完成 → 记录文章已读 ==========
let articleReadRecorded = false

function recordArticleReadOnce() {
  if (articleReadRecorded) return
  articleReadRecorded = true
  const articleId = readerStore.article?.id
  const difficulty = readerStore.article?.difficulty
  if (articleId && difficulty) {
    taskStore.recordArticleRead(articleId, difficulty)
  }
}

function onQuizCompleted() {
  recordArticleReadOnce()
}

/** 重置当前文章的已读状态（切换文章时调用） */
function resetReadState() {
  articleReadRecorded = false
}

// ========== 全局点击关闭浮窗 ==========
function handleGlobalClick() {
  wordPopover.visible = false
  culturePopover.visible = false
  showTranslationPopover.value = false
  showAIDetail.value = false
  aiDetailWord.value = ''
  showToc.value = false
  showSettings.value = false
  showFontPanel.value = false
}

// ========== 滚动监听更新进度 ==========
function handleScroll() {
  const scrollTop = window.scrollY
  const docHeight = document.documentElement.scrollHeight - window.innerHeight
  if (docHeight > 0) {
    readerStore.updateProgress(Math.round((scrollTop / docHeight) * 100))
  }
}

// 监听模式切换：关闭段落翻译模式时隐藏翻译弹窗
watch(() => readerStore.showTranslation, (newVal) => {
  if (!newVal) {
    showTranslationPopover.value = false
    activeTranslationIndex.value = -1
  }
})

/** 初始化当前文章的阅读追踪 */
async function initArticle(articleId) {
  resetReadState()
  await readerStore.fetchArticle(articleId)
  // 检查收藏状态
  readerStore.checkFavoriteStatus()
  if (articleId && readerStore.article?.title) {
    // 记录浏览历史到 IndexedDB
    addToHistory(articleId, readerStore.article.title).catch(err => {
      console.error('记录浏览历史失败:', err)
    })
    // 后台预取 AI 文化背景分析和阅读理解选择题（非VIP每日限3篇）
    const content = readerStore.article?.content
    const isVip = userStore.user?.profile === 'vip'
    if (content) {
      if (canUseAIArticle(isVip)) {
        readerStore.prefetchCulturalNotes(articleId, content)
        readerStore.prefetchQuiz(articleId, content)
        recordAIArticle(isVip)
      } else {
        readerStore.culturalNotesCache = { articleId, notes: [], loading: false, error: null, vipLimited: true }
        readerStore.quizCache = { articleId, questions: [], loading: false, error: null, vipLimited: true }
      }
    }

    // 初始化每日任务（阅读完成标记在答题完成后触发）
    taskStore.initDailyTasks()
  }
}

onMounted(async () => {
  await initArticle(route.query.id)
  document.addEventListener('click', handleGlobalClick)
  window.addEventListener('scroll', handleScroll)
})

// 监听文章切换（同一路由不同 query 参数）
watch(() => route.query.id, (newId, oldId) => {
  if (newId && newId !== oldId) {
    initArticle(newId)
  }
})

onUnmounted(() => {
  resetReadState()
  document.removeEventListener('click', handleGlobalClick)
  window.removeEventListener('scroll', handleScroll)
})
</script>
