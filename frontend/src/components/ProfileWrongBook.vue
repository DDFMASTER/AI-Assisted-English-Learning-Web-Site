<template>
  <div v-if="visible" class="card drawer-card">
    <!-- 标题栏 -->
    <div class="flex items-center justify-between mb-6">
      <h3 class="text-lg font-bold">📋 错题本</h3>
      <span class="text-xs text-gray-400">{{ wrongQuestions.length }} 道错题</span>
    </div>

    <!-- 空状态 -->
    <div v-if="wrongQuestions.length === 0" class="text-center py-12 text-gray-400">
      <Icon icon="ph:clipboard-text-bold" class="text-3xl mx-auto mb-3 opacity-30" />
      <p class="text-sm">暂无错题</p>
      <p class="text-xs mt-1">在测评结果页点击题目旁的 <span class="text-[#2563EB] font-bold">+</span> 即可加入错题本</p>
    </div>

    <!-- 有错题 -->
    <div v-else>
      <!-- 分页进度条 -->
      <div class="flex items-center justify-center gap-2 mb-6">
        <button
          class="w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold transition-all hover:scale-110 flex-none"
          :class="wrongBookIndex > 0
            ? 'bg-white border border-gray-200 text-gray-500 hover:border-[#2563EB] hover:text-[#2563EB] cursor-pointer'
            : 'bg-gray-100 text-gray-300 cursor-not-allowed'"
          :disabled="wrongBookIndex === 0"
          @click="wrongBookIndex--"
        >
          <Icon icon="ph:caret-left-bold" class="text-sm" />
        </button>

        <div class="flex items-center gap-1.5 overflow-x-auto py-1 px-2">
          <button
            v-for="(q, idx) in wrongQuestions"
            :key="q.id"
            class="w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold transition-all flex-none"
            :class="idx === wrongBookIndex
              ? 'bg-[#2563EB] text-white shadow-md shadow-blue-200 scale-110'
              : 'bg-red-50 text-red-600 border border-red-200 cursor-pointer hover:scale-105'"
            @click="wrongBookIndex = idx"
          >
            {{ idx + 1 }}
          </button>
        </div>

        <button
          class="w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold transition-all hover:scale-110 flex-none"
          :class="wrongBookIndex < wrongQuestions.length - 1
            ? 'bg-white border border-gray-200 text-gray-500 hover:border-[#2563EB] hover:text-[#2563EB] cursor-pointer'
            : 'bg-gray-100 text-gray-300 cursor-not-allowed'"
          :disabled="wrongBookIndex === wrongQuestions.length - 1"
          @click="wrongBookIndex++"
        >
          <Icon icon="ph:caret-right-bold" class="text-sm" />
        </button>
      </div>

      <!-- 当前错题卡片 -->
      <div class="card border-l-4 border-l-red-400 bg-white" :key="'wb-' + wrongBookIndex">
        <!-- 题号 -->
        <div class="flex items-center justify-between mb-4">
          <div class="flex items-center gap-3">
            <span class="w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold bg-red-50 text-red-600">
              {{ currentWrongQuestion.questionId }}
            </span>
            <span class="text-xs font-bold text-gray-400 uppercase tracking-widest">
              错题 {{ wrongBookIndex + 1 }} / {{ wrongQuestions.length }}
            </span>
          </div>
          <button
            class="flex items-center gap-1 px-2.5 py-1 rounded-lg text-xs font-bold text-gray-400 hover:text-red-500 hover:bg-red-50 transition-all"
            @click="handleRemove(currentWrongQuestion.id)"
            title="移出错题本"
          >
            <Icon icon="ph:trash-bold" class="text-sm" />
            移除
          </button>
        </div>

        <!-- 阅读文本 -->
        <div v-if="currentWrongQuestion.passage" class="mb-4 p-4 bg-gray-50 rounded-xl">
          <p class="text-sm text-gray-500 leading-relaxed">{{ currentWrongQuestion.passage }}</p>
        </div>

        <!-- 问题 -->
        <p class="font-bold text-lg mb-4">{{ currentWrongQuestion.question }}</p>

        <!-- 选项 -->
        <div class="space-y-3 mb-4">
          <div
            v-for="opt in currentWrongQuestion.options"
            :key="opt.id"
            class="flex items-center gap-3 p-3 rounded-xl text-sm"
            :class="getWrongOptClass(opt.id, currentWrongQuestion)"
          >
            <div
              class="w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold flex-none"
              :class="getWrongOptDotClass(opt.id, currentWrongQuestion)"
            >
              {{ opt.id }}
            </div>
            <span class="font-medium">{{ opt.text }}</span>
            <span v-if="opt.id === currentWrongQuestion.correctAnswer" class="ml-auto text-xs font-bold text-green-600">
              ✓ 正确答案
            </span>
            <span v-else-if="opt.id === currentWrongQuestion.userAnswer" class="ml-auto text-xs font-bold text-red-500">
              ✗ 你的答案
            </span>
          </div>
        </div>

        <!-- 解析 -->
        <div class="p-4 rounded-xl bg-amber-50 border border-amber-100">
          <div class="flex items-start gap-2">
            <Icon icon="ph:info-fill" class="text-lg flex-none mt-0.5 text-amber-500" />
            <div>
              <p class="text-xs font-bold mb-1 text-amber-700">错题解析</p>
              <p class="text-sm leading-relaxed text-amber-800">{{ currentWrongQuestion.explanation || '暂无解析' }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部翻页 -->
      <div class="flex items-center justify-between mt-6">
        <button
          class="flex items-center gap-2 px-5 py-2.5 rounded-xl text-sm font-bold transition-all"
          :class="wrongBookIndex > 0
            ? 'bg-white border border-gray-200 text-gray-600 hover:border-[#2563EB] hover:text-[#2563EB]'
            : 'bg-gray-50 text-gray-300 cursor-not-allowed'"
          :disabled="wrongBookIndex === 0"
          @click="wrongBookIndex--"
        >
          <Icon icon="ph:arrow-left-bold" class="text-base" />
          上一题
        </button>

        <span class="text-sm text-gray-400 font-medium">
          {{ wrongBookIndex + 1 }} / {{ wrongQuestions.length }}
        </span>

        <button
          class="flex items-center gap-2 px-5 py-2.5 rounded-xl text-sm font-bold transition-all"
          :class="wrongBookIndex < wrongQuestions.length - 1
            ? 'bg-[#2563EB] text-white hover:bg-blue-600'
            : 'bg-gray-50 text-gray-300 cursor-not-allowed'"
          :disabled="wrongBookIndex === wrongQuestions.length - 1"
          @click="wrongBookIndex++"
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
  </div>
</template>

<script setup>
import { ref, computed, watch, onUnmounted } from 'vue'
import { Icon } from '@iconify/vue'
import { getAllWrongQuestions, removeFromWrongBook } from '@/utils/wrongBookDB'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['close'])

// ========== 错题数据 ==========
const wrongQuestions = ref([])
const wrongBookIndex = ref(0)

const currentWrongQuestion = computed(() =>
  wrongQuestions.value[wrongBookIndex.value] || {}
)

async function loadWrongQuestions() {
  try {
    wrongQuestions.value = await getAllWrongQuestions()
    // 如果当前索引超出范围，重置到第一题
    if (wrongBookIndex.value >= wrongQuestions.value.length) {
      wrongBookIndex.value = 0
    }
  } catch (e) {
    console.error('加载错题本失败:', e)
  }
}

async function handleRemove(id) {
  try {
    await removeFromWrongBook(id)
    wrongQuestions.value = wrongQuestions.value.filter(q => q.id !== id)
    // 如果删除的是最后一题且不是第一题，索引前移
    if (wrongBookIndex.value >= wrongQuestions.value.length && wrongBookIndex.value > 0) {
      wrongBookIndex.value--
    }
  } catch (e) {
    console.error('移出错题失败:', e)
  }
}

// ========== 选项样式 ==========
function getWrongOptClass(optionId, item) {
  const isUserAnswer = optionId === item.userAnswer
  const isCorrectAnswer = optionId === item.correctAnswer

  if (isCorrectAnswer && isUserAnswer) return 'bg-green-50 border border-green-300'
  if (isCorrectAnswer && !isUserAnswer) return 'bg-green-50 border border-green-300'
  if (isUserAnswer && !isCorrectAnswer) return 'bg-red-50 border border-red-300'
  return 'bg-gray-50 border border-transparent'
}

function getWrongOptDotClass(optionId, item) {
  const isUserAnswer = optionId === item.userAnswer
  const isCorrectAnswer = optionId === item.correctAnswer

  if (isCorrectAnswer) return 'bg-green-500 text-white'
  if (isUserAnswer && !isCorrectAnswer) return 'bg-red-500 text-white'
  return 'bg-gray-200 text-gray-500'
}

// ========== 键盘事件 ==========
function handleWrongBookKeydown(e) {
  if (e.key === 'ArrowLeft') {
    e.preventDefault()
    if (wrongBookIndex.value > 0) wrongBookIndex.value--
  } else if (e.key === 'ArrowRight') {
    e.preventDefault()
    if (wrongBookIndex.value < wrongQuestions.value.length - 1) wrongBookIndex.value++
  }
}

// ========== 可见性监听 ==========
watch(
  () => props.visible,
  (val) => {
    if (val) {
      loadWrongQuestions()
      window.addEventListener('keydown', handleWrongBookKeydown)
    } else {
      window.removeEventListener('keydown', handleWrongBookKeydown)
      wrongBookIndex.value = 0
    }
  }
)

onUnmounted(() => {
  window.removeEventListener('keydown', handleWrongBookKeydown)
})
</script>

<style scoped>
.drawer-card {
  border-radius: 0 16px 16px 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}
</style>
