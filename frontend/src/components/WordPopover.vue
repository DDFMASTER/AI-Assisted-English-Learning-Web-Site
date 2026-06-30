<template>
  <Teleport to="body">
    <div
      v-if="visible"
      class="fixed z-[60] glass-popover p-5 w-80 max-h-[calc(100vh-160px)] overflow-y-auto"
      :style="{ left: position.x + 'px', top: position.y + 'px' }"
      @click.stop
    >
      <!-- ========== 释义视图 ========== -->
      <template v-if="!detailVisible">
        <!-- 页眉：单词 + 音标 + 收藏按钮 -->
        <div class="flex justify-between items-start mb-3">
          <div>
            <h3 class="text-xl font-bold text-[#2563EB]">{{ word.word }}</h3>
            <p v-if="word.phonetic" class="text-xs text-gray-400">{{ word.phonetic }}</p>
            <!-- 词形还原标注 -->
            <p v-if="word.lemmaFrom && word.lemmaTo" class="text-xs text-amber-500 mt-0.5">
              <Icon icon="ph:arrows-split-bold" class="inline text-[10px] mr-0.5" />
              原形: {{ word.lemmaTo }}
            </p>
          </div>
          <button
            class="text-gray-400 hover:text-[#2563EB] transition-colors flex-none"
            @click="handleAddToVocab"
            title="加入生词本"
          >
            <Icon icon="ph:plus-circle-bold" class="text-2xl" />
          </button>
        </div>

        <!-- 加入生词本提示 -->
        <div
          v-if="vocabToast"
          class="mb-3 px-3 py-1.5 rounded-lg text-center text-xs font-bold transition-all duration-200"
          :class="vocabToast.includes('已加入') ? 'bg-green-50 text-green-600' : 'bg-gray-50 text-gray-500'"
        >
          {{ vocabToast }}
        </div>

        <!-- 加载中 -->
        <div v-if="loading" class="flex items-center justify-center py-6">
          <Icon icon="ph:spinner-bold" class="text-lg text-[#2563EB] animate-spin" />
          <span class="text-xs text-gray-400 ml-2">查询中...</span>
        </div>

        <!-- 查询出错 -->
        <div v-else-if="word.error" class="text-center py-4">
          <p class="text-sm text-red-400">{{ word.error }}</p>
        </div>

        <!-- 未找到结果 -->
        <div v-else-if="word.found === false" class="text-center py-4">
          <p class="text-sm text-gray-400">未在当前词库中找到该单词</p>
        </div>

        <!-- 跨词书检索提示 -->
        <div v-if="word.crossStage && word.found" class="mb-3 px-2.5 py-2 bg-amber-50 border border-amber-100 rounded-lg">
          <p class="text-[11px] text-amber-700 leading-relaxed">
            <Icon icon="ph:arrows-clockwise-bold" class="inline text-amber-500 mr-1" />
            该单词未在你的当前阶段词书中找到，以下结果来自其他词书：
          </p>
        </div>

        <!-- 按来源分组的结果 -->
        <div v-if="word.results && word.results.length" class="space-y-3 mb-4">
          <div v-for="(group, gIdx) in word.results" :key="gIdx">
            <div class="flex items-center gap-1.5 mb-1">
              <span
                class="text-[10px] font-bold px-1.5 py-0.5 rounded"
                :class="word.crossStage ? 'bg-amber-50 text-amber-700' : 'bg-blue-50 text-blue-600'"
              >
                {{ group.source }}
              </span>
            </div>
            <div
              v-for="(entry, eIdx) in group.entries"
              :key="eIdx"
            >
              <div class="flex items-baseline gap-2 ml-1 mb-0.5">
                <span v-if="entry.phonetic" class="text-[11px] text-gray-400 font-mono flex-none">
                  {{ entry.phonetic }}
                </span>
                <span class="text-sm text-gray-700 leading-relaxed">
                  {{ entry.translation }}
                </span>
              </div>
              <!-- AI 详细解释 -->
              <div
                v-if="entry.explanation"
                class="ml-1 mt-1 mb-2 px-2.5 py-1.5 bg-blue-50/50 rounded-lg text-[11px] text-gray-500 leading-relaxed"
              >
                {{ entry.explanation }}
              </div>
            </div>
          </div>
        </div>

        <!-- 底部操作栏 -->
        <div class="pt-3 border-t border-gray-100">
          <button
            class="w-full py-2 bg-gray-50 hover:bg-gray-100 text-[11px] font-bold text-gray-500 rounded-lg transition-colors"
            @click="handleViewDetail"
          >
            查看详细解析与例句
          </button>
        </div>
      </template>

      <!-- ========== AI 例句详情视图 ========== -->
      <template v-else>
        <!-- 页眉 -->
        <div class="flex justify-between items-start mb-3">
          <div>
            <h3 class="text-xl font-bold text-[#2563EB]">{{ detailWord }}</h3>
            <p class="text-[10px] text-gray-400 mt-0.5">🤖 AI 生成例句</p>
          </div>
          <button
            class="text-xs font-bold text-[#2563EB] hover:text-blue-700 transition-colors"
            @click="handleBackToSummary"
          >
            ← 返回
          </button>
        </div>

        <!-- 加载中 -->
        <div v-if="detailLoading" class="flex items-center justify-center py-8">
          <Icon icon="ph:spinner-bold" class="text-lg text-[#2563EB] animate-spin" />
          <span class="text-xs text-gray-400 ml-2">AI 正在生成例句...</span>
        </div>

        <!-- 错误提示 -->
        <div v-else-if="detailError" class="text-center py-6">
          <Icon icon="ph:warning-circle-bold" class="text-2xl text-amber-400 mx-auto mb-2" />
          <p class="text-xs text-gray-400">{{ detailError }}</p>
        </div>

        <!-- 例句列表 -->
        <div v-else-if="detailExamples && detailExamples.length" class="space-y-4">
          <div
            v-for="(example, idx) in detailExamples"
            :key="idx"
            class="p-3 bg-blue-50/50 rounded-xl"
          >
            <div class="flex items-start gap-2 mb-1.5">
              <span class="text-[10px] font-bold text-white bg-[#2563EB] w-4 h-4 rounded-full flex items-center justify-center flex-none mt-0.5">
                {{ idx + 1 }}
              </span>
              <p class="text-sm text-gray-800 leading-relaxed">{{ example.en }}</p>
            </div>
            <p class="text-xs text-gray-500 ml-6">{{ example.zh }}</p>
          </div>

          <p class="text-[10px] text-gray-300 text-center pt-1">
            内容由 AI 生成，仅供参考
          </p>
        </div>

        <!-- 无例句 -->
        <div v-else class="text-center py-6">
          <p class="text-xs text-gray-400">暂无例句数据</p>
        </div>
      </template>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Icon } from '@iconify/vue'
import { useReaderStore } from '@/stores/reader'

const props = defineProps({
  visible: { type: Boolean, default: false },
  loading: { type: Boolean, default: false },
  word: {
    type: Object,
    default: () => ({ word: '', results: [] }),
  },
  position: {
    type: Object,
    default: () => ({ x: 0, y: 0 }),
  },
  /** 是否显示 AI 例句详情模式 */
  detailVisible: { type: Boolean, default: false },
  /** AI 例句数据 { examples: Array<{en,zh}>, loading, error } */
  detailData: {
    type: Object,
    default: () => null,
  },
})

const emit = defineEmits(['close', 'add-vocab', 'view-detail', 'back-to-summary'])

const readerStore = useReaderStore()
const vocabToast = ref('')  // 加入生词本成功提示

const detailWord = computed(() => props.word?.word || '')
const detailLoading = computed(() => props.detailData?.loading || false)
const detailError = computed(() => props.detailData?.error || '')
const detailExamples = computed(() => props.detailData?.examples || [])

async function handleAddToVocab() {
  // 提取第一条翻译和来源词书
  const firstResult = props.word.results?.[0]
  const firstEntry = firstResult?.entries?.[0]
  const wordData = {
    word: props.word.word,
    phonetic: props.word.phonetic || firstEntry?.phonetic || '',
    translation: firstEntry?.translation || '',
    source: firstResult?.source || '',
  }
  const success = await readerStore.addToVocabulary(wordData)
  if (success) {
    vocabToast.value = '已加入生词本 ✓'
    setTimeout(() => { vocabToast.value = '' }, 1000)
    emit('add-vocab', wordData)
  } else {
    vocabToast.value = '已在生词本中'
    setTimeout(() => { vocabToast.value = '' }, 1000)
  }
}

function handleViewDetail() {
  emit('view-detail', props.word)
}

function handleBackToSummary() {
  emit('back-to-summary')
}
</script>
