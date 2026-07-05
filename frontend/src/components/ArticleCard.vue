<template>
  <div
    class="card flex gap-6 hover:shadow-lg transition-shadow cursor-pointer group"
    @click="goToReader"
  >
    <!-- 左侧封面图片（文字图片，显示难度类型） -->
    <div
      class="w-48 h-32 rounded-xl overflow-hidden flex-none relative flex items-center justify-center select-none"
      :class="bgClass"
    >
      <!-- 装饰背景圆 -->
      <div class="absolute -top-6 -right-6 w-24 h-24 rounded-full opacity-20" :class="accentBgClass"></div>
      <div class="absolute -bottom-4 -left-4 w-16 h-16 rounded-full opacity-10" :class="accentBgClass"></div>
      <!-- 难度文字 -->
      <span
        class="relative text-3xl font-extrabold tracking-wider"
        :class="textColorClass"
        style="text-shadow: 0 2px 8px rgba(0,0,0,0.08);"
      >{{ coverText }}</span>
    </div>

    <!-- 中间内容 -->
    <div class="flex-1 min-w-0">
      <div class="flex items-center gap-2 mb-2">
        <span :class="difficultyBadgeClass">{{ article.difficulty || 'B1+ 适中' }}</span>
        <span class="text-[11px] text-gray-400 font-medium">{{ article.source }}</span>
      </div>
      <h3 class="text-xl font-bold mb-2 group-hover:text-[#2563EB] transition-colors truncate">
        {{ article.title }}
      </h3>
      <p class="text-gray-500 text-sm line-clamp-2 leading-relaxed mb-4">
        {{ article.abstract || article.description || '暂无简介' }}
      </p>
      <div class="flex items-center gap-6 text-[11px] text-gray-400 font-medium">
        <span class="flex items-center gap-1">
          <Icon icon="ph:clock-bold" /> {{ article.readTime || '10 min read' }}
        </span>
        <span class="flex items-center gap-1">
          <Icon icon="ph:text-t-bold" /> {{ article.wordCount || '850 words' }}
        </span>
        <span v-if="article.newWords" class="flex items-center gap-1">
          <Icon icon="ph:translate-bold" /> {{ article.newWords }}
        </span>
      </div>
    </div>

    <!-- 右侧箭头 -->
    <div class="flex flex-col justify-center">
      <div
        class="w-10 h-10 rounded-full bg-blue-50 flex items-center justify-center text-[#2563EB] hover:bg-[#2563EB] hover:text-white transition-all"
        @click.stop="goToReader"
      >
        <Icon icon="ph:arrow-right-bold" class="text-xl" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'

const props = defineProps({
  article: {
    type: Object,
    required: true,
  },
})

const router = useRouter()

const difficultyMap = {
  'A1': 'badge badge-easy',
  'A2': 'badge badge-easy',
  'A2+': 'badge badge-easy',
  'B1': 'badge badge-medium',
  'B1+': 'badge badge-medium',
  'B2': 'badge badge-hard',
  'C1': 'badge badge-hard',
  // 中文难度标签（MaterialsPage 传入的是中文）
  '初中': 'badge badge-easy',
  '高中': 'badge badge-easy',
  '四级': 'badge badge-medium',
  '六级': 'badge badge-hard',
  '考研': 'badge badge-hard',
  '托福': 'badge badge-hard',
}

const bgMap = {
  'A1': 'bg-gradient-to-br from-green-200 to-emerald-300',
  'A2': 'bg-gradient-to-br from-green-200 to-teal-300',
  'A2+': 'bg-gradient-to-br from-green-200 to-teal-300',
  'B1': 'bg-gradient-to-br from-blue-200 to-indigo-300',
  'B1+': 'bg-gradient-to-br from-blue-200 to-indigo-300',
  'B2': 'bg-gradient-to-br from-yellow-200 to-amber-300',
  'C1': 'bg-gradient-to-br from-red-200 to-rose-300',
  // 中文难度映射
  '初中': 'bg-gradient-to-br from-green-200 to-emerald-300',
  '高中': 'bg-gradient-to-br from-teal-200 to-cyan-300',
  '四级': 'bg-gradient-to-br from-blue-200 to-indigo-300',
  '六级': 'bg-gradient-to-br from-yellow-200 to-amber-300',
  '考研': 'bg-gradient-to-br from-red-200 to-rose-300',
  '托福': 'bg-gradient-to-br from-purple-200 to-violet-300',
}

// 封面难度文字
const coverTextMap = {
  'A1': '初中', 'A2': '高中', 'A2+': '高中',
  'B1': '四级', 'B1+': '四级',
  'B2': '六级', 'C1': '考研',
  '初中': '初中', '高中': '高中',
  '四级': '四级', '六级': '六级',
  '考研': '考研', '托福': '托福',
}

const textColorMap = {
  'A1': 'text-green-700', 'A2': 'text-green-700', 'A2+': 'text-green-700',
  'B1': 'text-blue-700', 'B1+': 'text-blue-700',
  'B2': 'text-yellow-700', 'C1': 'text-red-700',
  '初中': 'text-green-700', '高中': 'text-teal-700',
  '四级': 'text-blue-700', '六级': 'text-yellow-700',
  '考研': 'text-red-700', '托福': 'text-purple-700',
}

const accentBgMap = {
  'A1': 'bg-green-400', 'A2': 'bg-green-400', 'A2+': 'bg-green-400',
  'B1': 'bg-blue-400', 'B1+': 'bg-blue-400',
  'B2': 'bg-yellow-400', 'C1': 'bg-red-400',
  '初中': 'bg-green-400', '高中': 'bg-teal-400',
  '四级': 'bg-blue-400', '六级': 'bg-yellow-400',
  '考研': 'bg-red-400', '托福': 'bg-purple-400',
}

const difficultyBadgeClass = computed(() =>
  difficultyMap[props.article.difficulty] || 'badge badge-medium'
)

const bgClass = computed(() =>
  bgMap[props.article.difficulty] || 'bg-gradient-to-br from-blue-200 to-indigo-300'
)

const coverText = computed(() =>
  coverTextMap[props.article.difficulty] || props.article.difficulty || '阅读'
)

const textColorClass = computed(() =>
  textColorMap[props.article.difficulty] || 'text-blue-700'
)

const accentBgClass = computed(() =>
  accentBgMap[props.article.difficulty] || 'bg-blue-400'
)

function goToReader() {
  router.push(`/reader?id=${props.article.id || props.article.article_id}`)
}
</script>
