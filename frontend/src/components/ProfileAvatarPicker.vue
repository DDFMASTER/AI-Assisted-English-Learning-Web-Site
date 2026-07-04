<template>
  <Teleport to="body">
    <div v-if="visible" class="fixed inset-0 z-[200] flex items-center justify-center bg-black/40 backdrop-blur-sm" tabindex="-1" @click.self="$emit('close')" @keydown.escape="$emit('close')">
      <div class="bg-white rounded-2xl shadow-2xl w-full max-w-lg max-h-[80vh] overflow-y-auto p-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-bold">选择头像</h3>
          <button class="w-8 h-8 rounded-full bg-gray-100 flex items-center justify-center hover:bg-gray-200 transition-colors" @click="$emit('close')">
            <Icon icon="ph:x-bold" class="text-gray-500" />
          </button>
        </div>
        <div class="grid grid-cols-5 gap-3">
          <div v-for="avatar in AVATAR_LIST" :key="avatar.id"
            class="aspect-square rounded-xl overflow-hidden cursor-pointer border-2 transition-all hover:scale-105"
            :class="currentId === avatar.id ? 'border-[#2563EB] ring-2 ring-blue-200' : 'border-gray-100 hover:border-gray-300'"
            @click="handleSelect(avatar)">
            <img :src="AVATAR_BASE_URL + avatar.file" :alt="avatar.name" class="w-full h-full object-cover" />
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref } from 'vue'
import { AVATAR_LIST, AVATAR_BASE_URL } from '@/constants/avatars.js'
import { Icon } from '@iconify/vue'

defineProps(['visible'])
const emit = defineEmits(['close', 'select'])

function loadSelectedAvatarId() {
  try {
    const val = localStorage.getItem('aael_selected_avatar')
    if (val !== null) {
      const num = parseInt(val, 10)
      if (!isNaN(num) && num >= 1 && num <= AVATAR_LIST.length) return num
    }
  } catch (_) { /* ignore */ }
  return 1
}

const currentId = ref(loadSelectedAvatarId())

function handleSelect(avatar) {
  currentId.value = avatar.id
  localStorage.setItem('aael_selected_avatar', String(avatar.id))
  window.dispatchEvent(new CustomEvent('avatar-changed'))
  emit('select', avatar)
}
</script>
