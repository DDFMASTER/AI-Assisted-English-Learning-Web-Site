<template>
  <div class="animate-pulse">
    <div v-if="type === 'article'" class="card flex gap-6">
      <div class="w-48 h-32 rounded-xl bg-gray-200 flex-none" />
      <div class="flex-1 space-y-3 py-2">
        <div class="h-4 w-20 bg-gray-200 rounded" />
        <div class="h-6 w-3/4 bg-gray-200 rounded" />
        <div class="h-4 w-full bg-gray-200 rounded" />
      </div>
    </div>
    <div v-else-if="type === 'table'" class="space-y-3">
      <div v-for="n in rows" :key="n" class="flex gap-4">
        <div v-for="m in cols" :key="m" class="h-4 bg-gray-200 rounded" :style="{ width: (100/cols) + '%' }" />
      </div>
    </div>
    <div v-else-if="type === 'card'" class="card space-y-3">
      <div class="h-4 w-20 bg-gray-200 rounded" />
      <div class="h-6 w-3/4 bg-gray-200 rounded" />
      <div class="h-4 w-full bg-gray-200 rounded" />
    </div>
  </div>
</template>
<script setup>
defineProps({ type: { type: String, default: 'article' }, rows: { type: Number, default: 3 }, cols: { type: Number, default: 4 } })
</script>
