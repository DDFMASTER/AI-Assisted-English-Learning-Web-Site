<template>
  <nav class="sticky top-0 z-50 bg-white/80 dark:bg-[#1E1E1E]/90 backdrop-blur-md border-b border-gray-100 dark:border-gray-800">
    <div class="max-w-[1200px] mx-auto px-6 h-16 flex items-center justify-between">
      <!-- 左侧：Logo + 导航链接 -->
      <div class="flex items-center gap-6">
        <router-link to="/materials" class="flex items-center gap-2 shrink-0">
          <div class="w-8 h-8 bg-[#2563EB] rounded-lg flex items-center justify-center">
            <Icon icon="ph:book-open-text-bold" class="text-white text-xl" />
          </div>
          <span class="text-xl font-bold tracking-tight text-[#1F2937] dark:text-gray-200">EngliAI</span>
        </router-link>

        <!-- 导航链接（桌面端可见） -->
        <div class="hidden lg:flex items-center gap-6">
          <router-link to="/materials" class="nav-link text-gray-500" active-class="nav-active" aria-label="读物匹配">
            读物匹配
          </router-link>
          <router-link to="/assessment" class="nav-link text-gray-500" active-class="nav-active" aria-label="测评中心">
            测评中心
          </router-link>
          <router-link
            v-if="userStore.user?.role === 'admin'"
            to="/admin"
            class="nav-link text-gray-500"
            active-class="nav-active"
            aria-label="管理"
          >
            管理
          </router-link>
        </div>
      </div>

      <!-- 中间：搜索框（平板及以上可见） -->
      <div class="relative flex-1 max-w-md mx-8 hidden sm:block" ref="searchContainer">
        <div class="relative">
          <button
            class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-[#2563EB] transition-colors"
            @click="handleSearch"
            :disabled="searching"
          >
            <Icon icon="ph:magnifying-glass-bold" class="text-lg" />
          </button>
          <input
            v-model="searchQuery"
            type="text"
            placeholder="搜索单词..."
            aria-label="搜索单词"
            class="w-full h-10 pl-10 pr-4 bg-gray-100 dark:bg-[#333] rounded-xl text-sm text-gray-700 dark:text-gray-200 placeholder-gray-400 dark:placeholder-gray-500 focus:outline-none focus:bg-white dark:focus:bg-[#3C3C3C] focus:ring-2 focus:ring-[#2563EB] focus:border-transparent transition-all"
            @keydown.enter="handleSearch"
            @focus="onSearchFocus"
          />
          <!-- 搜索中 loading -->
          <div v-if="searching" class="absolute right-3 top-1/2 -translate-y-1/2">
            <Icon icon="ph:spinner-bold" class="text-gray-400 text-lg animate-spin" />
          </div>
          <!-- 清除按钮 -->
          <button
            v-if="searchQuery && !searching"
            class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-300 hover:text-gray-500"
            @click="clearSearch"
          >
            <Icon icon="ph:x-circle-fill" class="text-lg" />
          </button>
        </div>

        <!-- 搜索结果卡片 -->
        <div
          v-if="showResult && searchResult"
          class="absolute top-full mt-2 left-0 right-0 bg-white dark:bg-[#252526] rounded-xl shadow-xl border border-gray-100 dark:border-gray-700 p-5 z-50"
        >
          <!-- 未找到 -->
          <div v-if="!searchResult.found" class="text-center py-2">
            <Icon icon="ph:smiley-sad-bold" class="text-3xl text-gray-300 mb-2" />
            <p class="text-sm text-gray-400">未找到该单词的释义</p>
          </div>

          <!-- 找到结果 -->
          <template v-else>
            <!-- 单词与音标 -->
            <div class="flex items-center justify-between mb-3">
              <div>
                <h3 class="text-xl font-bold text-[#2563EB]">{{ searchResult.word }}</h3>
                <p v-if="searchResult.phonetic" class="text-xs text-gray-400 mt-0.5">{{ searchResult.phonetic }}</p>
                <p v-if="searchResult.lemmaFrom" class="text-xs text-gray-400 mt-0.5">
                  原形: {{ searchResult.lemmaTo }}
                </p>
              </div>
              <button
                class="text-gray-400 hover:text-red-500 transition-colors"
                @click="clearSearch"
              >
                <Icon icon="ph:x-bold" class="text-lg" />
              </button>
            </div>

            <!-- 释义列表 -->
            <div class="space-y-2 max-h-64 overflow-y-auto">
              <div
                v-for="(group, idx) in searchResult.results"
                :key="idx"
                class="p-3 bg-gray-50 rounded-lg"
              >
                <span class="text-xs font-bold text-gray-500 mb-1.5 block">{{ group.source }}</span>
                <div class="space-y-1">
                  <div
                    v-for="entry in group.entries"
                    :key="entry.id"
                    class="flex items-start gap-2"
                  >
                    <span class="text-xs text-gray-400 mt-0.5 shrink-0">{{ entry.id }}.</span>
                    <span class="text-sm text-gray-700">{{ entry.translation }}</span>
                  </div>
                </div>
              </div>
            </div>
          </template>
        </div>
      </div>

      <!-- 右侧操作 -->
      <div class="flex items-center gap-4 shrink-0">

        <!-- 主题切换 -->
        <div class="relative" @mouseenter="showThemeCardFn" @mouseleave="hideThemeCard">
          <button class="w-9 h-9 rounded-lg flex items-center justify-center hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors" aria-label="切换显示模式">
            <Icon :icon="themeIcon" class="text-lg text-gray-500 dark:text-gray-300" />
          </button>
          <Transition name="popover">
            <div v-if="showThemeCard" class="absolute right-0 top-full mt-1 w-44 bg-white dark:bg-gray-800 rounded-xl shadow-xl border border-gray-100 dark:border-gray-700 p-3 z-50"
              @mouseenter="showThemeCard = true" @mouseleave="hideThemeCard">
              <p class="text-xs font-bold text-gray-400 dark:text-gray-500 mb-2">显示模式</p>
              <div class="space-y-1">
                <button v-for="opt in themeOptions" :key="opt.value"
                  class="w-full flex items-center gap-2 px-2 py-1.5 rounded-lg text-xs font-medium transition-colors"
                  :class="theme === opt.value ? 'bg-blue-50 dark:bg-blue-900 text-[#2563EB] dark:text-blue-300' : 'text-gray-600 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-700'"
                  @click="setTheme(opt.value); showThemeCard = false"
                ><Icon :icon="opt.icon" class="text-base" />{{ opt.label }}</button>
              </div>
            </div>
          </Transition>
        </div>

        <!-- 头像（悬浮显示信息卡片） -->
        <div class="relative" @mouseenter="showCard" @mouseleave="hideCard">
          <router-link
            to="/profile"
            aria-label="个人中心"
            class="w-10 h-10 rounded-xl border-2 border-white overflow-hidden cursor-pointer hover:ring-2 hover:ring-[#2563EB] transition-all block"
          >
            <img
              v-if="navAvatarSrc"
              :src="navAvatarSrc"
              alt="头像"
              class="w-full h-full object-cover rounded-xl"
            />
            <div v-else class="w-full h-full rounded-xl bg-gradient-to-br from-blue-400 to-purple-500 flex items-center justify-center text-white text-sm font-bold">
              {{ avatarLetter }}
            </div>
          </router-link>

          <!-- 悬浮卡片 -->
          <Transition name="popover">
            <div
              v-if="showAvatarCard"
              class="absolute right-0 top-full mt-1 w-60 bg-white dark:bg-[#252526] rounded-xl shadow-xl border border-gray-100 dark:border-gray-700 p-4 z-50"
              @mouseenter="showCard"
              @mouseleave="hideCard"
            >
              <div class="flex items-center gap-3 mb-3 pb-3 border-b border-gray-100">
                <div class="w-10 h-10 rounded-lg overflow-hidden flex-none">
                  <img v-if="navAvatarSrc" :src="navAvatarSrc" alt="头像" class="w-full h-full object-cover" />
                  <div v-else class="w-full h-full bg-gradient-to-br from-blue-400 to-purple-500 flex items-center justify-center text-white text-xs font-bold">{{ avatarLetter }}</div>
                </div>
                <div class="flex-1 min-w-0">
                  <p class="text-sm font-bold text-gray-700 truncate">{{ userStore.user?.username || '用户' }}</p>
                  <p class="text-xs text-gray-400">{{ userStore.user?.studyPurpose || '' }}</p>
                </div>
              </div>
              <div class="space-y-1.5 mb-3 text-xs text-gray-500">
                <div class="flex justify-between"><span>🔥 连续学习</span><span class="font-bold">{{ navStreak }} 天</span></div>
                <div class="flex justify-between"><span>📖 累计阅读</span><span class="font-bold">{{ navTotalRead }} 篇</span></div>
                <div class="flex justify-between"><span>⭐ 经验值</span><span class="font-bold text-[#F59E0B]">{{ userStore.user?.experience || 0 }} XP</span></div>
              </div>
              <button
                class="w-full py-2 rounded-lg text-xs font-bold transition-all"
                :class="checkinDone ? 'bg-green-50 text-green-500 cursor-default' : 'bg-[#2563EB] text-white hover:bg-blue-600'"
                :disabled="checkinLoading || checkinDone"
                @click="doCheckin"
              >
                <span v-if="checkinLoading">签到中...</span>
                <span v-else-if="checkinDone">✅ 今日已签到</span>
                <span v-else>🎁 签到 +10 XP</span>
              </button>
            </div>
          </Transition>
        </div>
      </div>
    </div>

  </nav>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { Icon } from '@iconify/vue'
import { useUserStore } from '@/stores/user'
import { useReaderStore } from '@/stores/reader'
import { getAvatarSrc } from '@/constants/avatars.js'
import { getStreak } from '@/utils/onlineTimeDB'
import { countHistory } from '@/utils/historyDB'
import { useTheme } from '@/composables/useTheme'
import request from '@/utils/request'

const { theme, setTheme } = useTheme()

const userStore = useUserStore()
const readerStore = useReaderStore()

const searchContainer = ref(null)

// 搜索相关
const searchQuery = ref('')
const searching = ref(false)
const searchResult = ref(null)
const showResult = ref(false)

const avatarLetter = computed(() => {
  const username = userStore.user?.username || 'A'
  return username.charAt(0).toUpperCase()
})

// 导航栏头像（与个人中心同步，读取 localStorage）
function getNavAvatarSrc() {
  try {
    const id = parseInt(localStorage.getItem('aael_selected_avatar'), 10)
    return getAvatarSrc(id)
  } catch (_) { /* ignore */ }
  return getAvatarSrc(1)
}

const navAvatarSrc = ref(getNavAvatarSrc())

// 头像悬浮卡片
const showAvatarCard = ref(false)
let cardTimer = null

function showCard() {
  clearTimeout(cardTimer)
  showAvatarCard.value = true
}

function hideCard() {
  cardTimer = setTimeout(() => { showAvatarCard.value = false }, 200)
}

// 主题切换卡片
const showThemeCard = ref(false)
let themeCardTimer = null
function showThemeCardFn() {
  clearTimeout(themeCardTimer)
  showThemeCard.value = true
}
function hideThemeCard() {
  themeCardTimer = setTimeout(() => { showThemeCard.value = false }, 200)
}
const themeOptions = [
  { value: 'light', label: '浅色模式', icon: 'ph:sun-bold' },
  { value: 'dark', label: '深色模式', icon: 'ph:moon-bold' },
  { value: 'system', label: '跟随系统', icon: 'ph:monitor-bold' },
]
const themeIcon = computed(() => {
  if (theme.value === 'dark') return 'ph:moon-bold'
  if (theme.value === 'light') return 'ph:sun-bold'
  return 'ph:monitor-bold'
})

// 导航栏用户统计
const navStreak = ref(0)
const navTotalRead = ref(0)

async function loadNavStats() {
  try {
    const [s, c] = await Promise.all([getStreak(), countHistory()])
    navStreak.value = s
    navTotalRead.value = c
  } catch (_) { /* ignore */ }
}

// 签到相关
const checkinLoading = ref(false)
const checkinDone = ref(false)

async function doCheckin() {
  if (checkinLoading.value || checkinDone.value) return
  checkinLoading.value = true
  try {
    const data = await request.post('/user/checkin')
    if (data.success) {
      checkinDone.value = true
      await userStore.fetchProfile()
      loadNavStats()
    } else if (data.message) {
      if (data.message.includes('今日已签到')) checkinDone.value = true
      console.log('签到:', data.message)
    }
  } catch (e) {
    console.log('签到请求失败:', e?.response?.status, e?.message)
    if (e?.response?.status === 401) {
      console.log('提示：会话可能已过期，请退出后重新登录')
    }
  } finally { checkinLoading.value = false }
}

// 监听头像变更事件（由 ProfilePage 触发）
function onAvatarChanged() {
  navAvatarSrc.value = getNavAvatarSrc()
}

onMounted(() => {
  document.addEventListener('click', onClickOutside)
  window.addEventListener('avatar-changed', onAvatarChanged)
  userStore.fetchProfile()
  loadNavStats()
})

onUnmounted(() => {
  document.removeEventListener('click', onClickOutside)
  window.removeEventListener('avatar-changed', onAvatarChanged)
})

// 点击外部关闭搜索卡片
function onClickOutside(e) {
  if (searchContainer.value && !searchContainer.value.contains(e.target)) {
    showResult.value = false
  }
}

async function handleSearch() {
  const query = searchQuery.value.trim()
  if (!query) return

  searching.value = true
  showResult.value = false
  searchResult.value = null

  try {
    const result = await readerStore.lookupWord(query)
    searchResult.value = result
    showResult.value = true
  } catch (err) {
    searchResult.value = { success: true, word: query, found: false, results: [] }
    showResult.value = true
  } finally {
    searching.value = false
  }
}

function clearSearch() {
  searchQuery.value = ''
  searchResult.value = null
  showResult.value = false
}

function onSearchFocus() {
  if (searchResult.value) showResult.value = true
}
</script>

<style scoped>
/* 搜索动画 */
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
.animate-spin {
  animation: spin 0.8s linear infinite;
}
</style>
