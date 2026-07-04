<template>
  <div class="space-y-1">
    <!-- 学习目标时长 -->
    <div
      class="flex items-center justify-between p-4 hover:bg-gray-50 rounded-xl transition-colors"
      :class="{ 'cursor-pointer': !editingTarget }"
      @click="editingTarget || startEditTarget()"
    >
      <span class="text-sm font-medium text-gray-700">学习目标时长</span>
      <span v-if="!editingTarget" class="text-sm text-gray-400">{{ studyTarget }} 分钟/天</span>
      <div v-else class="flex items-center gap-2" @click.stop>
        <input
          ref="targetInputRef"
          v-model.number="targetDraft"
          type="number"
          min="1"
          max="480"
          class="w-16 h-8 text-sm text-center border border-[#2563EB] rounded-lg focus:outline-none"
          @keydown.enter="saveTarget"
          @keydown.escape="editingTarget = false"
          @blur="saveTarget"
        />
        <span class="text-xs text-gray-400">分钟/天</span>
      </div>
    </div>
    <!-- 学习阶段 -->
    <div class="flex items-center justify-between p-4 hover:bg-gray-50 rounded-xl transition-colors">
      <span class="text-sm font-medium text-gray-700">学习阶段</span>
      <select
        :value="studyPurpose"
        class="text-sm text-gray-500 border border-gray-200 rounded-lg px-2 py-1 focus:outline-none focus:ring-2 focus:ring-[#2563EB]"
        @change="updateStudyPurpose(($event.target).value)"
      >
        <option value="" disabled>请选择</option>
        <option v-for="s in allStages" :key="s" :value="s">{{ s }}</option>
      </select>
    </div>
    <!-- 关于我们 -->
    <div
      class="flex items-center justify-between p-4 hover:bg-gray-50 rounded-xl transition-colors cursor-pointer"
      @click="$emit('open-about')"
    >
      <span class="text-sm font-medium text-gray-700">关于我们</span>
      <Icon icon="ph:caret-right-bold" class="text-gray-300 text-lg" />
    </div>
    <!-- 退出登录 -->
    <div
      class="flex items-center justify-between p-4 hover:bg-red-50 rounded-xl transition-colors cursor-pointer"
      @click="$emit('logout', $event)"
    >
      <span class="text-sm font-medium text-red-500">退出登录</span>
      <Icon icon="ph:sign-out-bold" class="text-red-300 text-lg" />
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { Icon } from '@iconify/vue'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import { setDailyTarget } from '@/utils/onlineTimeDB'

const props = defineProps({
  studyTarget: { type: Number, required: true },
  studyPurpose: { type: String, default: '' },
  allStages: { type: Array, default: () => [] },
})

const emit = defineEmits(['update-target', 'update-purpose', 'open-about', 'logout'])

const userStore = useUserStore()

const editingTarget = ref(false)
const targetDraft = ref(props.studyTarget)
const targetInputRef = ref(null)

function startEditTarget() {
  targetDraft.value = props.studyTarget
  editingTarget.value = true
  nextTick(() => {
    if (targetInputRef.value) {
      targetInputRef.value.focus()
      targetInputRef.value.select()
    }
  })
}

function saveTarget() {
  editingTarget.value = false
  const val = targetDraft.value
  if (!isNaN(val) && val > 0) {
    setDailyTarget(val)
    emit('update-target', val)
  }
}

async function updateStudyPurpose(value) {
  if (!value) return
  try {
    const params = new URLSearchParams()
    params.append('userId', String(userStore.user?.userId))
    params.append('studyPurpose', value)
    const data = await request.post('/user/update-study-purpose', params)
    if (data.success) {
      await userStore.fetchProfile()
      emit('update-purpose', value)
    }
  } catch (e) {
    console.error('更新学习阶段失败:', e)
  }
}
</script>
