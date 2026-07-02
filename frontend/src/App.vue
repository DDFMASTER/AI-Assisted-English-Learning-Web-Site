<template>
  <div id="engli-app">
    <AppNav v-if="$route.name !== 'Login'" />
    <router-view />
    <LoginModal
      :visible="userStore.showLoginModal"
      @close="userStore.closeLoginModal()"
      @login-success="onLoginSuccess"
    />
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import AppNav from '@/components/AppNav.vue'
import LoginModal from '@/components/LoginModal.vue'
import { useUserStore } from '@/stores/user'
import { startOnlineTimer, pauseTimer, resumeTimer } from '@/composables/useOnlineTimer'
import request from '@/utils/request'

const userStore = useUserStore()
const route = useRoute()

let heartbeatTimer = null

/** 心跳：每 30 秒发一次请求，若 session 已被销毁则触发 401 拦截跳转登录页 */
function startHeartbeat() {
  if (heartbeatTimer) return
  heartbeatTimer = setInterval(() => {
    const token = localStorage.getItem('token')
    if (!token || route.name === 'Login') return
    // 发一个轻量请求，让 LoginFilter 检查踢人状态；带上 userId 避免无参报错
    const raw = localStorage.getItem('user')
    const userId = raw ? (() => { try { return JSON.parse(raw).userId } catch { return null } })() : null
    if (!userId) return
    request.get('/user/profile', { params: { userId } }).catch(() => {})
  }, 30000)
}

function stopHeartbeat() {
  if (heartbeatTimer) {
    clearInterval(heartbeatTimer)
    heartbeatTimer = null
  }
}

// 路由变化时重新评估心跳
watch(() => route.name, (name) => {
  if (name === 'Login') {
    stopHeartbeat()
  } else if (localStorage.getItem('token')) {
    startHeartbeat()
  }
})

function onLoginSuccess() {
  userStore.fetchProfile()
  startHeartbeat()
}

function onVisibilityChange() {
  if (document.hidden) {
    pauseTimer()
  } else {
    resumeTimer()
  }
}

function onBeforeUnload() {
  pauseTimer()
}

onMounted(() => {
  if (userStore.token) {
    userStore.fetchProfile()
    startHeartbeat()
  }
  startOnlineTimer()
  document.addEventListener('visibilitychange', onVisibilityChange)
  window.addEventListener('beforeunload', onBeforeUnload)
})

onUnmounted(() => {
  pauseTimer()
  stopHeartbeat()
  document.removeEventListener('visibilitychange', onVisibilityChange)
  window.removeEventListener('beforeunload', onBeforeUnload)
})
</script>

<style>
#engli-app {
  min-height: 100vh;
}
</style>
