<template>
  <main class="max-w-[1200px] mx-auto px-6 mt-10 pb-8" role="main">
    <!-- 顶部标题区 -->
    <div class="mb-10">
      <h1 class="text-3xl font-bold mb-2">智能读物匹配</h1>
      <p class="text-gray-500">
        基于你的当前水平
        <span class="font-bold text-[#2563EB]">{{ userLevel }}</span>
        ，AI 为你推荐
      </p>
    </div>

    <!-- 移动端快捷操作按钮 -->
    <div class="lg:hidden flex gap-2 mb-6">
      <router-link to="/assessment" class="flex-1 py-3 bg-[#2563EB] text-white rounded-xl text-sm font-bold text-center hover:bg-blue-600 transition-colors">📝 测评中心</router-link>
      <router-link to="/english-world" class="flex-1 py-3 bg-green-500 text-white rounded-xl text-sm font-bold text-center hover:bg-green-600 transition-colors">🎮 英语天地</router-link>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-12 gap-8">
      <!-- 左栏：文章推荐区 -->
      <div class="col-span-1 lg:col-span-8">
        <!-- 移动端：今日任务 -->
        <div class="lg:hidden card mb-6">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-bold">🎯 今日任务</h3>
            <span class="text-xs text-gray-400">{{ taskStore.todayDoneCount }}/{{ taskStore.todayTotalCount }}</span>
          </div>
          <div class="w-full h-2 bg-gray-100 rounded-full overflow-hidden mb-4">
            <div class="h-full bg-[#2563EB] rounded-full transition-all" :style="{ width: taskProgressPercent + '%' }"></div>
          </div>
          <div v-if="taskStore.todayTotalCount === 0" class="text-xs text-gray-400">完成一篇文章的阅读与练习以开始</div>
          <div v-else class="space-y-2">
            <div v-for="task in taskStore.todayTasks" :key="task.id" class="flex items-center gap-3 text-sm" :class="task.done ? 'text-gray-400' : 'text-gray-700'">
              <span class="w-5 h-5 rounded border-2 flex items-center justify-center flex-none cursor-pointer" :class="task.done ? 'bg-green-400 border-green-400 text-white' : 'border-gray-300'" @click="taskStore.toggleTask(task.id)">
                <Icon v-if="task.done" icon="ph:check-bold" class="text-xs" />
              </span>
              <span :class="task.done ? 'line-through' : ''">{{ task.name }}</span>
              <span class="ml-auto text-xs font-bold" :class="task.done ? 'text-green-500' : 'text-orange-400'">+{{ task.xp }}XP</span>
            </div>
          </div>
        </div>

        <!-- ====== 推荐区 ====== -->
        <section class="mb-10" aria-label="为你推荐">
          <h2 class="text-lg font-bold text-gray-700 mb-4">
            📚 为你推荐
            <span class="text-sm font-normal text-gray-400 ml-2">（{{ recommendLabel }}）</span>
          </h2>

          <div v-if="recommendLoading" class="space-y-6">
            <div v-for="n in 3" :key="'skel-r'+n" class="card flex gap-6 animate-pulse">
              <div class="w-48 h-32 rounded-xl bg-gray-200 flex-none" />
              <div class="flex-1 space-y-3 py-2">
                <div class="h-4 w-20 bg-gray-200 rounded" />
                <div class="h-6 w-3/4 bg-gray-200 rounded" />
                <div class="h-4 w-full bg-gray-200 rounded" />
              </div>
            </div>
          </div>

          <div v-else-if="recommendArticles.length > 0" class="space-y-6">
            <ArticleCard
              v-for="article in recommendArticles"
              :key="'rec-'+article.id"
              :article="article"
            />
          </div>

          <div v-else class="card text-center text-gray-400 py-10">
            <Icon icon="ph:hard-drives-bold" class="text-4xl mb-3 opacity-30" />
            <p>文章数据库建设中，敬请期待</p>
          </div>
        </section>
      </div>

      <!-- 右栏 -->
      <div class="col-span-1 lg:col-span-4 space-y-8 hidden lg:block">
        <!-- 今日任务 -->
        <div class="card">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-bold">🎯 今日任务</h3>
            <span class="text-xs text-gray-400">{{ taskStore.todayDoneCount }}/{{ taskStore.todayTotalCount }}</span>
          </div>
          <div class="w-full h-2 bg-gray-100 rounded-full overflow-hidden mb-4">
            <div
              class="h-full bg-[#2563EB] rounded-full transition-all"
              :style="{ width: taskProgressPercent + '%' }"
            ></div>
          </div>
          <div v-if="taskStore.todayTotalCount === 0" class="text-xs text-gray-400">
            完成一篇文章的阅读与练习以开始
          </div>
          <div v-else class="space-y-2">
            <div
              v-for="task in taskStore.todayTasks"
              :key="task.id"
              class="flex items-center gap-3 text-sm"
              :class="task.done ? 'text-gray-400' : 'text-gray-700'"
            >
              <span
                class="w-5 h-5 rounded border-2 flex items-center justify-center flex-none cursor-pointer"
                :class="task.done ? 'bg-green-400 border-green-400 text-white' : 'border-gray-300'"
                @click="taskStore.toggleTask(task.id)"
              >
                <Icon v-if="task.done" icon="ph:check-bold" class="text-xs" />
              </span>
              <span :class="task.done ? 'line-through' : ''">{{ task.name }}</span>
              <span class="ml-auto text-xs font-bold" :class="task.done ? 'text-green-500' : 'text-orange-400'">+{{ task.xp }}XP</span>
            </div>
          </div>
        </div>

        <!-- 浏览历史 -->
        <div class="card">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-bold">📜 最近浏览</h3>
          </div>
          <div v-if="historyItems.length === 0" class="text-xs text-gray-400">暂无浏览记录</div>
          <div v-else class="space-y-3">
            <div
              v-for="(item, idx) in historyItems"
              :key="idx"
              class="flex items-center gap-3 cursor-pointer hover:bg-gray-50 rounded-lg p-1 -mx-1 transition-colors"
              @click="goToReader(item.articleId)"
            >
              <Icon :icon="item.icon" :class="item.iconColor" class="text-xl flex-none" />
              <div class="flex-1 min-w-0">
                <p class="text-sm text-gray-700 truncate">{{ item.title }}</p>
                <p class="text-xs text-gray-400">{{ item.time }}</p>
              </div>
              <span :class="item.statusClass">{{ item.status }}</span>
            </div>
            <button
              class="w-full text-xs text-[#2563EB] hover:underline mt-2"
              @click="router.push('/profile?tab=records')"
            >查看全部记录 →</button>
          </div>
        </div>
      </div>
    </div>

    <!-- ====== 文章目录（全宽）====== -->
    <section aria-label="文章目录" class="mt-10">
      <h2 class="text-lg font-bold text-gray-700 mb-4">📖 文章目录</h2>

      <!-- 分类 Tab -->
      <div class="flex p-1 bg-white rounded-xl mb-6 w-fit shadow-sm">
        <button
          v-for="tab in catalogTabs"
          :key="tab.key"
          class="px-5 py-2 rounded-lg text-sm font-medium transition-all"
          :class="activeCategory === tab.key ? 'tab-active' : 'text-gray-500 hover:bg-gray-50'"
          @click="switchCategory(tab.key)"
        >
          {{ tab.label }}
        </button>
      </div>

      <!-- 加载中 -->
      <div v-if="catalogLoading" class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div v-for="n in 6" :key="'skel-c'+n" class="card flex gap-6 animate-pulse">
          <div class="w-48 h-32 rounded-xl bg-gray-200 flex-none" />
          <div class="flex-1 space-y-3 py-2">
            <div class="h-4 w-20 bg-gray-200 rounded" />
            <div class="h-6 w-3/4 bg-gray-200 rounded" />
            <div class="h-4 w-full bg-gray-200 rounded" />
          </div>
        </div>
      </div>

      <template v-else>
        <!-- 文章列表（双列） -->
        <div v-if="catalogArticles.length > 0" class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <ArticleCard
            v-for="article in catalogArticles"
            :key="'cat-'+article.id"
            :article="article"
          />
        </div>

        <!-- 空状态 -->
        <div v-else class="card text-center text-gray-400 py-12">
          <Icon icon="ph:book-open-bold" class="text-4xl mb-3 opacity-30" />
          <p>暂无此类文章</p>
        </div>

        <!-- 分页（居中，始终显示） -->
        <div v-if="catalogArticles.length > 0" class="mt-8 pt-4 border-t border-gray-100" role="navigation" aria-label="分页导航">
        <div class="flex items-center justify-center gap-2 flex-wrap">
          <button
            class="px-3 py-1.5 text-xs rounded-lg bg-gray-100 text-gray-500 hover:bg-gray-200 transition-colors disabled:opacity-30"
            :disabled="catalogPage <= 1"
            @click="goPage(catalogPage - 1)"
          >上一页</button>

          <template v-if="visiblePages[0] > 1">
            <button
              class="px-3 py-1.5 text-xs rounded-lg text-gray-500 hover:bg-gray-100 transition-colors"
              @click="goPage(1)"
            >1</button>
            <span v-if="visiblePages[0] > 2" class="px-1 text-xs text-gray-300">…</span>
          </template>

          <button
            v-for="p in visiblePages"
            :key="p"
            class="px-3 py-1.5 text-xs rounded-lg transition-colors"
            :class="p === catalogPage ? 'bg-[#2563EB] text-white' : 'text-gray-500 hover:bg-gray-100'"
            @click="goPage(p)"
          >{{ p }}</button>

          <template v-if="visiblePages[visiblePages.length - 1] < catalogTotalPages">
            <span v-if="visiblePages[visiblePages.length - 1] < catalogTotalPages - 1" class="px-1 text-xs text-gray-300">…</span>
            <button
              class="px-3 py-1.5 text-xs rounded-lg text-gray-500 hover:bg-gray-100 transition-colors"
              @click="goPage(catalogTotalPages)"
            >{{ catalogTotalPages }}</button>
          </template>

          <button
            class="px-3 py-1.5 text-xs rounded-lg bg-gray-100 text-gray-500 hover:bg-gray-200 transition-colors disabled:opacity-30"
            :disabled="catalogPage >= catalogTotalPages"
            @click="goPage(catalogPage + 1)"
          >下一页</button>
        </div>

        <!-- 跳转至第 N 页 -->
        <div class="flex items-center justify-center gap-1.5 mt-3">
          <span class="text-xs text-gray-400">跳至第</span>
          <input
            v-model="jumpPageInput"
            type="number"
            :min="1"
            :max="catalogTotalPages"
            class="w-14 h-7 text-center text-xs border border-gray-200 rounded-lg outline-none focus:border-[#2563EB] transition-colors"
            @keyup.enter="handleJumpPage"
          />
          <span class="text-xs text-gray-400">页</span>
          <button
            class="text-xs px-2.5 py-1 bg-gray-100 rounded-lg text-gray-500 hover:bg-gray-200 transition-colors disabled:opacity-30"
            :disabled="!isValidJumpPage"
            @click="handleJumpPage"
          >GO</button>
        </div>
      </div>
      </template>
    </section>

    <!-- 词汇量测试弹窗 -->
    <FirstVocabTestModal
      :visible="showVocabTest"
      @done="onVocabTestDone"
      @skip="showVocabTest = false"
    />

    <!-- 页脚 -->
    <footer class="mt-12 pt-6 pb-8 border-t border-gray-100">
      <div class="flex flex-col sm:flex-row items-center justify-between gap-3 text-xs text-gray-400">
        <div class="flex items-center gap-4">
          <span>EngliAI — 智能英语学习平台</span>
          <router-link to="/materials" class="hover:text-[#2563EB] transition-colors">读物匹配</router-link>
          <router-link to="/profile" class="hover:text-[#2563EB] transition-colors">个人中心</router-link>
        </div>
        <div>
          <span>&copy; {{ new Date().getFullYear() }} EngliAI. All rights reserved.</span>
        </div>
      </div>
    </footer>
  </main>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import { useTaskStore } from '@/stores/task'
import { useUserStore } from '@/stores/user'
import ArticleCard from '@/components/ArticleCard.vue'
import FirstVocabTestModal from '@/components/FirstVocabTestModal.vue'
import request from '@/utils/request'
import { getRecentHistory, relativeTime } from '@/utils/historyDB'

const router = useRouter()
const taskStore = useTaskStore()
const userStore = useUserStore()

// ====== CEFR 映射 ======
const CEFR_DIFFICULTY = {
  'A1': '初中',
  'A2': '高中',
  'B1': '四级',
  'B2': '六级',
  'C1': '考研',
  'C2': '托福,期刊,原著',
}
const CEFR_LABEL = {
  'A1': '初中', 'A2': '高中', 'B1': '四级', 'B2': '六级', 'C1': '考研', 'C2': '托福/期刊/原著',
}

const DIFFICULTY_LABEL = {
  '期刊': '期刊', '原著': '原著', '网络新闻': '网络新闻',
  '初中': '初中', '高中': '高中',
  '四级': 'CET-4', '六级': 'CET-6',
  '考研': '考研', '托福': 'TOEFL',
}

// ====== 词汇量测试弹窗 ======
const showVocabTest = ref(false)

function getLocalVocabResult() {
  try {
    const uid = userStore.user?.userId
    if (!uid) return null
    const raw = localStorage.getItem(`aael_vocab_result_${uid}`)
    return raw ? JSON.parse(raw) : null
  } catch { return null }
}

function getUserCefr() {
  const local = getLocalVocabResult()
  if (local?.cefrLevel) return local.cefrLevel.toUpperCase()
  const lit = userStore.user?.literacy || 0
  if (lit >= 12000) return 'C2'
  if (lit >= 8000) return 'C1'
  if (lit >= 5000) return 'B2'
  if (lit >= 3000) return 'B1'
  if (lit >= 1500) return 'A2'
  return 'A1'
}

const userCefr = computed(() => getUserCefr())
const userLevel = computed(() => {
  const local = getLocalVocabResult()
  if (local) return `${local.cefrLevel} · ${local.cefrLabel}`
  const lit = userStore.user?.literacy || 0
  if (lit >= 12000) return 'C2 · 精通'
  if (lit >= 8000) return 'C1 · 高级'
  if (lit >= 5000) return 'B2 · 中高级'
  if (lit >= 3000) return 'B1 · 中级'
  if (lit >= 1500) return 'A2 · 初级上'
  return 'A1 · 初级'
})

const recommendLabel = computed(() => CEFR_LABEL[userCefr.value] || '四级')

// ====== 推荐区 ======
const recommendArticles = ref([])
const recommendLoading = ref(true)

async function fetchRecommend() {
  recommendLoading.value = true
  try {
    const diff = CEFR_DIFFICULTY[userCefr.value] || '四级'
    const data = await request.get('/article/list', { params: { difficulty: diff, limit: 3 } })
    if (data.success && Array.isArray(data.articles)) {
      recommendArticles.value = data.articles.map(mapArticle)
    }
  } catch (_) {
    recommendArticles.value = []
  } finally {
    recommendLoading.value = false
  }
}

// ====== 文章目录 ======
const catalogTabs = [
  { key: 'advanced', label: '进阶类' },
  { key: 'exam', label: '应试类' },
  { key: 'basic', label: '基础类' },
  { key: 'extended', label: '拓展类' },
]
const activeCategory = ref('advanced')
const catalogArticles = ref([])
const catalogLoading = ref(false)
const catalogPage = ref(1)
const catalogTotalPages = ref(1)
const catalogPageSize = 6

const visiblePages = computed(() => {
  const tp = catalogTotalPages.value
  const cp = catalogPage.value

  // 总页数不足 5 时全部显示
  if (tp <= 5) {
    const pages = []
    for (let i = 1; i <= tp; i++) pages.push(i)
    return pages
  }

  // 总页数 ≥ 5，始终显示固定 5 个连续页码
  let start
  if (cp <= 3) {
    start = 1
  } else if (cp >= tp - 2) {
    start = tp - 4
  } else {
    start = cp - 2
  }

  const pages = []
  for (let i = start; i < start + 5; i++) pages.push(i)
  return pages
})

// ====== 跳转至第 N 页 ======
const jumpPageInput = ref(1)

const isValidJumpPage = computed(() => {
  const v = parseInt(jumpPageInput.value, 10)
  return !isNaN(v) && v >= 1 && v <= catalogTotalPages.value
})

function handleJumpPage() {
  if (!isValidJumpPage.value) return
  const target = parseInt(jumpPageInput.value, 10)
  goPage(target)
}

async function fetchCatalog() {
  catalogLoading.value = true
  try {
    const data = await request.get('/article/list', {
      params: {
        category: activeCategory.value,
        page: catalogPage.value,
        pageSize: catalogPageSize,
      }
    })
    if (data.success && Array.isArray(data.articles)) {
      catalogArticles.value = data.articles.map(mapArticle)
      catalogTotalPages.value = data.totalPages || 1
    }
  } catch (_) {
    catalogArticles.value = []
  } finally {
    catalogLoading.value = false
  }
}

function switchCategory(key) {
  if (activeCategory.value === key) return
  activeCategory.value = key
  catalogPage.value = 1
  fetchCatalog()
}

function goPage(p) {
  if (p < 1 || p > catalogTotalPages.value) return
  catalogPage.value = p
  fetchCatalog()
}

// ====== 文章映射 ======
function mapArticle(raw) {
  const difficulty = raw.difficulty || '四级'
  const vocquizNum = raw.vocquizNum || 0
  return {
    id: raw.articleId,
    article_id: raw.articleId,
    title: raw.title || 'Untitled',
    source: raw.source || '未知来源',
    difficulty: DIFFICULTY_LABEL[difficulty] || difficulty,
    category: getCategory(difficulty),
    readTime: (raw.readTime || 5) + ' min read',
    wordCount: (raw.wordCount || 500) + ' words',
    newWords: vocquizNum > 0 ? vocquizNum + ' 道词汇题' : '',
    abstract: '难度: ' + (DIFFICULTY_LABEL[difficulty] || difficulty)
            + ' · 来源: ' + (raw.source || '未知'),
    articleLikeCount: raw.articleLikeCount || 0,
  }
}

function getCategory(difficulty) {
  if (['考研', '托福', '期刊', '原著'].includes(difficulty)) return 'advanced'
  if (['四级', '六级'].includes(difficulty)) return 'exam'
  if (['初中', '高中'].includes(difficulty)) return 'basic'
  if (['网络新闻'].includes(difficulty)) return 'extended'
  return 'advanced'
}

// ====== 浏览历史 ======
const HISTORY_ICONS = [
  { icon: 'ph:book-open-bold', color: 'text-[#2563EB]' },
  { icon: 'ph:bookmark-simple-bold', color: 'text-green-500' },
  { icon: 'ph:books-bold', color: 'text-yellow-500' },
]
const historyItems = ref([])

async function fetchHistory() {
  try {
    const records = await getRecentHistory(3)
    historyItems.value = records.map((r, i) => {
      const iconCfg = HISTORY_ICONS[i] || HISTORY_ICONS[HISTORY_ICONS.length - 1]
      return {
        articleId: r.articleId,
        title: r.title,
        icon: iconCfg.icon,
        iconColor: iconCfg.color,
        status: '已浏览',
        statusClass: 'text-[10px] text-gray-500 font-bold bg-gray-50 px-1 rounded',
        time: relativeTime(r.visitedAt),
      }
    })
  } catch (_) {
    historyItems.value = []
  }
}

// ====== 任务进度 ======
const taskProgressPercent = computed(() => {
  if (taskStore.todayTotalCount === 0) return 0
  return Math.round((taskStore.todayDoneCount / taskStore.todayTotalCount) * 100)
})

function goToReader(articleId) {
  router.push(`/reader?id=${articleId}`)
}

function onVocabTestDone() {
  showVocabTest.value = false
  userStore.fetchProfile()
}

// ====== 初始化 ======
onMounted(async () => {
  // 词汇量测试
  const local = getLocalVocabResult()
  const lit = userStore.user?.literacy
  if (!local && (lit == null || lit === 0)) {
    showVocabTest.value = true
  }

  fetchRecommend()
  fetchCatalog()
  taskStore.initDailyTasks()
  fetchHistory()
})
</script>
