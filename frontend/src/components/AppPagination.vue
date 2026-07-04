<template>
  <div v-if="totalPages > 1" class="mt-4 pt-4 border-t border-gray-100">
    <div class="flex items-center justify-center gap-2 flex-wrap">
      <button class="px-3 py-1 text-xs rounded-lg bg-gray-100 text-gray-500 hover:bg-gray-200 transition-colors disabled:opacity-30"
        :disabled="currentPage <= 1" @click="$emit('change', currentPage - 1)">上一页</button>
      <template v-if="visiblePages[0] > 1">
        <button class="px-2.5 py-0.5 text-xs rounded-lg text-gray-500 hover:bg-gray-100" @click="$emit('change', 1)">1</button>
        <span v-if="visiblePages[0] > 2" class="px-1 text-xs text-gray-300">…</span>
      </template>
      <button v-for="p in visiblePages" :key="p" class="px-2.5 py-0.5 text-xs rounded-lg transition-colors"
        :class="p === currentPage ? 'bg-[#2563EB] text-white' : 'text-gray-500 hover:bg-gray-100'"
        @click="$emit('change', p)">{{ p }}</button>
      <template v-if="visiblePages[visiblePages.length - 1] < totalPages">
        <span v-if="visiblePages[visiblePages.length - 1] < totalPages - 1" class="px-1 text-xs text-gray-300">…</span>
        <button class="px-2.5 py-0.5 text-xs rounded-lg text-gray-500 hover:bg-gray-100" @click="$emit('change', totalPages)">{{ totalPages }}</button>
      </template>
      <button class="px-3 py-1 text-xs rounded-lg bg-gray-100 text-gray-500 hover:bg-gray-200 transition-colors disabled:opacity-30"
        :disabled="currentPage >= totalPages" @click="$emit('change', currentPage + 1)">下一页</button>
    </div>
    <div class="flex items-center justify-center gap-1.5 mt-3">
      <span class="text-xs text-gray-400">跳至第</span>
      <input v-model.number="jumpPage" type="number" :min="1" :max="totalPages"
        class="w-14 h-7 text-center text-xs border border-gray-200 rounded-lg outline-none focus:border-[#2563EB]"
        @keyup.enter="handleJump" />
      <span class="text-xs text-gray-400">页</span>
      <button class="text-xs px-2.5 py-1 bg-gray-100 rounded-lg text-gray-500 hover:bg-gray-200 disabled:opacity-30"
        :disabled="!isValidJump" @click="handleJump">GO</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
const props = defineProps({ currentPage: Number, totalPages: Number })
const emit = defineEmits(['change'])
const jumpPage = ref(1)
const isValidJump = computed(() => jumpPage.value >= 1 && jumpPage.value <= props.totalPages)
function handleJump() { if (isValidJump.value) emit('change', jumpPage.value) }
const visiblePages = computed(() => {
  const tp = props.totalPages, cp = props.currentPage
  if (tp <= 5) { const p = []; for (let i = 1; i <= tp; i++) p.push(i); return p }
  let s; if (cp <= 3) s = 1; else if (cp >= tp - 2) s = tp - 4; else s = cp - 2
  const p = []; for (let i = s; i < s + 5; i++) p.push(i); return p
})
</script>
