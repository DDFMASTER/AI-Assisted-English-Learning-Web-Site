<template>
  <Teleport to="body">
    <div
      v-if="visible"
      class="fixed inset-0 z-[200] flex items-center justify-center bg-black/40 backdrop-blur-sm"
      tabindex="-1"
      @click.self="$emit('close')"
      @keydown.escape="$emit('close')"
    >
      <div class="bg-white rounded-2xl shadow-2xl w-full max-w-sm p-6 mx-4">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-bold">⭐ VIP 兑换</h3>
          <button
            class="w-8 h-8 rounded-full bg-gray-100 flex items-center justify-center hover:bg-gray-200 transition-colors"
            @click="$emit('close')"
          >
            <Icon icon="ph:x-bold" class="text-gray-500" />
          </button>
        </div>

        <div class="bg-yellow-50 rounded-xl p-4 mb-4">
          <p class="text-sm text-yellow-700 font-medium mb-1">当前经验值：<strong>{{ totalXp }}</strong></p>
          <p v-if="isVip" class="text-sm text-yellow-700 font-medium">
            VIP 到期：<strong>{{ vipExpireText }}</strong>
          </p>
          <p v-else class="text-sm text-gray-500">你还不是 VIP 用户</p>
        </div>

        <p class="text-xs text-gray-400 mb-4">180 经验值 = 1 天 VIP，选择要兑换的天数：</p>

        <div class="grid grid-cols-4 gap-2 mb-4">
          <button
            v-for="d in vipDayOptions"
            :key="d"
            class="py-2 rounded-lg text-sm font-bold transition-all border"
            :class="selectedDays === d
              ? 'bg-yellow-100 border-yellow-400 text-yellow-700'
              : 'bg-white border-gray-200 text-gray-500 hover:border-yellow-300'"
            @click="selectedDays = d"
          >
            {{ d }}天
          </button>
        </div>

        <p class="text-center text-sm text-gray-500 mb-4">
          需要 <strong class="text-yellow-600">{{ selectedDays * 180 }}</strong> 经验值
        </p>

        <button
          class="w-full py-3 bg-yellow-400 text-white rounded-xl font-bold hover:bg-yellow-500 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          :disabled="selectedDays * 180 > totalXp || exchanging"
          @click="doExchange"
        >
          <span v-if="exchanging">兑换中...</span>
          <span v-else-if="isVip">续费 VIP {{ selectedDays }} 天</span>
          <span v-else>兑换 VIP {{ selectedDays }} 天</span>
        </button>
        <p v-if="selectedDays * 180 > totalXp" class="text-xs text-red-400 text-center mt-2">
          经验值不足
        </p>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref } from 'vue'
import { Icon } from '@iconify/vue'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'

const props = defineProps({
  visible: { type: Boolean, default: false },
  totalXp: { type: Number, default: 0 },
  isVip: { type: Boolean, default: false },
  vipExpireText: { type: String, default: '' },
})

const emit = defineEmits(['close', 'exchanged'])

const userStore = useUserStore()

const selectedDays = ref(1)
const exchanging = ref(false)
const vipDayOptions = [1, 3, 7, 30]

async function doExchange() {
  exchanging.value = true
  try {
    await userStore.fetchProfile()
    if (!userStore.user?.userId) {
      emit('close')
      return
    }

    const params = new URLSearchParams()
    params.append('userId', String(userStore.user.userId))
    params.append('days', String(selectedDays.value))
    const data = await request.post('/user/vip-exchange', params)
    if (data.success) {
      await userStore.fetchProfile()
      emit('exchanged', selectedDays.value)
    }
  } catch (e) {
    if (e.response?.status === 401) {
      try {
        await userStore.fetchProfile()
        const params2 = new URLSearchParams()
        params2.append('userId', String(userStore.user.userId))
        params2.append('days', String(selectedDays.value))
        const retry = await request.post('/user/vip-exchange', params2)
        if (retry.success) {
          await userStore.fetchProfile()
          emit('exchanged', selectedDays.value)
          return
        }
      } catch (_) {}
    }
    console.error('VIP兑换失败:', e)
  } finally {
    exchanging.value = false
  }
}
</script>
