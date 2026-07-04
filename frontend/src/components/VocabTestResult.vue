<template>
  <div class="p-8 text-center">
    <div
      class="w-24 h-24 rounded-full flex items-center justify-center mx-auto mb-4"
      :class="honestyPercent >= 80 ? 'bg-green-100' : 'bg-yellow-100'"
    >
      <Icon
        :icon="honestyPercent >= 80 ? 'ph:trophy-bold' : 'ph:warning-bold'"
        class="text-4xl"
        :class="honestyPercent >= 80 ? 'text-green-500' : 'text-yellow-500'"
      />
    </div>

    <h2 class="text-2xl font-bold mb-1">测试完成！</h2>
    <p class="text-gray-400 text-sm mb-6">您的词汇量估算结果</p>

    <div class="grid grid-cols-2 gap-4 mb-6">
      <div class="bg-blue-50 rounded-xl p-4">
        <p class="text-3xl font-black text-[#2563EB]">{{ estimatedVocab.toLocaleString() }}</p>
        <p class="text-xs text-gray-400">估算词汇量</p>
      </div>
      <div class="bg-green-50 rounded-xl p-4">
        <p class="text-3xl font-black text-green-500">{{ cefrLevel }}</p>
        <p class="text-xs text-gray-400">{{ cefrLabel }} · CEFR 等级</p>
      </div>
      <div class="bg-gray-50 rounded-xl p-4">
        <p class="text-xl font-bold text-gray-700">{{ correctCount }} / {{ totalWords }}</p>
        <p class="text-xs text-gray-400">正确题数</p>
      </div>
      <div class="bg-amber-50 rounded-xl p-4">
        <p class="text-xl font-bold text-amber-500">+{{ xpEarned }}</p>
        <p class="text-xs text-gray-400">获得经验</p>
      </div>
      <div class="bg-gray-50 rounded-xl p-4">
        <p
          class="text-xl font-bold"
          :class="honestyPercent >= 80 ? 'text-green-500' : 'text-yellow-500'"
        >
          {{ honestyPercent }}%
        </p>
        <p class="text-xs text-gray-400">诚信度</p>
      </div>
      <div class="bg-gray-50 rounded-xl p-4">
        <p class="text-xl font-bold text-gray-700">{{ pseudoHitRate }}%</p>
        <p class="text-xs text-gray-400">伪词命中率</p>
      </div>
    </div>

    <div class="flex items-center gap-3 justify-center">
      <button
        class="px-8 py-3 bg-[#2563EB] text-white rounded-xl font-bold shadow-lg shadow-blue-200 hover:scale-105 transition-transform"
        @click="$emit('test-again')"
      >
        重新测试
      </button>
      <button
        class="px-8 py-3 bg-gray-100 text-gray-600 rounded-xl font-bold hover:bg-gray-200 transition-colors"
        @click="$emit('close')"
      >
        返回
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Icon } from '@iconify/vue'

const props = defineProps({
  correctCount: { type: Number, required: true },
  totalWords: { type: Number, required: true },
  estimatedVocab: { type: Number, required: true },
  cefrLevel: { type: String, required: true },
  xpEarned: { type: Number, required: true },
  honestyPercent: { type: Number, required: true },
  pseudoHitRate: { type: Number, required: true },
})

defineEmits(['close', 'test-again'])

const cefrLabel = computed(() => {
  const map = {
    A1: '初级',
    A2: '初级上',
    B1: '中级',
    B2: '中高级',
    C1: '高级',
    C2: '精通',
  }
  return map[props.cefrLevel] || props.cefrLevel
})
</script>
