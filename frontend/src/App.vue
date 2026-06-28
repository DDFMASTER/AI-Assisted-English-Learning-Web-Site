<template>
  <main class="page">
    <section class="panel">
      <h1>AI Assisted English Learning</h1>
      <p class="subtitle">前后端通信测试面板</p>

      <!-- Tab 切换 -->
      <nav class="tabs">
        <button v-for="tab in tabs" :key="tab.key"
                :class="['tab', { active: activeTab === tab.key }]"
                @click="activeTab = tab.key">
          {{ tab.label }}
        </button>
      </nav>

      <!-- 1. 连接测试 -->
      <section v-if="activeTab === 'connect'" class="test-section">
        <h2>后端连接测试</h2>
        <button @click="testConnect" :disabled="connectLoading">
          {{ connectLoading ? '测试中...' : '测试后端连接' }}
        </button>
        <pre class="result" v-if="connectResult">{{ connectResult }}</pre>
      </section>

      <!-- 2. 注册测试 -->
      <section v-if="activeTab === 'register'" class="test-section">
        <h2>用户注册测试</h2>
        <form @submit.prevent="testRegister" class="form">
          <label>用户名 (≤16字符)
            <input v-model="regForm.username" type="text" maxlength="16" required />
          </label>
          <label>密码
            <input v-model="regForm.password" type="password" required />
          </label>
          <label>学习阶段
            <select v-model="regForm.studyPurpose" required>
              <option value="">-- 请选择 --</option>
              <option v-for="s in stages" :key="s" :value="s">{{ s }}</option>
            </select>
          </label>
          <button type="submit" :disabled="regLoading">
            {{ regLoading ? '注册中...' : '注册' }}
          </button>
        </form>
        <pre class="result" v-if="regResult">{{ regResult }}</pre>
      </section>

      <!-- 3. 登录测试 -->
      <section v-if="activeTab === 'login'" class="test-section">
        <h2>用户登录测试</h2>
        <form @submit.prevent="testLogin" class="form">
          <label>用户名
            <input v-model="loginForm.username" type="text" required />
          </label>
          <label>密码
            <input v-model="loginForm.password" type="password" required />
          </label>
          <button type="submit" :disabled="loginLoading">
            {{ loginLoading ? '登录中...' : '登录' }}
          </button>
        </form>
        <pre class="result" v-if="loginResult">{{ loginResult }}</pre>
      </section>

      <!-- 4. 单词查询测试 -->
      <section v-if="activeTab === 'word'" class="test-section">
        <h2>单词查询测试</h2>
        <form @submit.prevent="testWordSearch" class="form">
          <label>单词
            <input v-model="wordForm.word" type="text" required />
          </label>
          <label>学习阶段
            <select v-model="wordForm.studyPurpose">
              <option v-for="s in stages" :key="s" :value="s">{{ s }}</option>
            </select>
          </label>
          <button type="submit" :disabled="wordLoading">
            {{ wordLoading ? '查询中...' : '查询单词' }}
          </button>
        </form>
        <pre class="result" v-if="wordResult">{{ wordResult }}</pre>
      </section>

      <!-- 5. 文章列表测试 -->
      <section v-if="activeTab === 'article'" class="test-section">
        <h2>文章列表测试</h2>
        <div class="form">
          <label>按难度筛选 (可选)
            <select v-model="articleDifficulty">
              <option value="">全部</option>
              <option v-for="s in stages" :key="s" :value="s">{{ s }}</option>
            </select>
          </label>
          <button @click="testArticleList" :disabled="articleLoading">
            {{ articleLoading ? '加载中...' : '获取文章列表' }}
          </button>
        </div>
        <pre class="result" v-if="articleResult">{{ articleResult }}</pre>
      </section>
    </section>
  </main>
</template>

<script setup>
import { ref } from 'vue'
import request from '@/utils/request'

// ==================== 通用状态 ====================
const stages = ['初中', '高中', '四级', '六级', '考研', '托福']

const tabs = [
  { key: 'connect',  label: '连接测试' },
  { key: 'register', label: '注册' },
  { key: 'login',    label: '登录' },
  { key: 'word',     label: '查词' },
  { key: 'article',  label: '文章' },
]
const activeTab = ref('connect')

// ==================== 1. 连接测试 ====================
const connectLoading = ref(false)
const connectResult = ref('')

async function testConnect() {
  connectLoading.value = true
  connectResult.value = ''
  try {
    const data = await request.get('/connect-test')
    connectResult.value = JSON.stringify(data, null, 2)
  } catch (error) {
    connectResult.value = '请求失败: ' + (error.message || error)
  } finally {
    connectLoading.value = false
  }
}

// ==================== 2. 注册测试 ====================
const regLoading = ref(false)
const regResult = ref('')
const regForm = ref({ username: '', password: '', studyPurpose: '四级' })

async function testRegister() {
  regLoading.value = true
  regResult.value = ''
  try {
    const params = new URLSearchParams()
    params.append('username', regForm.value.username)
    params.append('password', regForm.value.password)
    params.append('studyPurpose', regForm.value.studyPurpose)

    const data = await request.post('/user/register', params)
    regResult.value = JSON.stringify(data, null, 2)
  } catch (error) {
    regResult.value = '请求失败: ' + (error.message || error)
  } finally {
    regLoading.value = false
  }
}

// ==================== 3. 登录测试 ====================
const loginLoading = ref(false)
const loginResult = ref('')
const loginForm = ref({ username: '', password: '' })

async function testLogin() {
  loginLoading.value = true
  loginResult.value = ''
  try {
    const params = new URLSearchParams()
    params.append('username', loginForm.value.username)
    params.append('password', loginForm.value.password)

    const data = await request.post('/user/login', params)
    loginResult.value = JSON.stringify(data, null, 2)
  } catch (error) {
    loginResult.value = '请求失败: ' + (error.message || error)
  } finally {
    loginLoading.value = false
  }
}

// ==================== 4. 单词查询测试 ====================
const wordLoading = ref(false)
const wordResult = ref('')
const wordForm = ref({ word: '', studyPurpose: '四级' })

async function testWordSearch() {
  wordLoading.value = true
  wordResult.value = ''
  try {
    const data = await request.get('/word/search', {
      params: { word: wordForm.value.word, studyPurpose: wordForm.value.studyPurpose }
    })
    wordResult.value = JSON.stringify(data, null, 2)
  } catch (error) {
    wordResult.value = '请求失败: ' + (error.message || error)
  } finally {
    wordLoading.value = false
  }
}

// ==================== 5. 文章列表测试 ====================
const articleLoading = ref(false)
const articleResult = ref('')
const articleDifficulty = ref('')

async function testArticleList() {
  articleLoading.value = true
  articleResult.value = ''
  try {
    const params = {}
    if (articleDifficulty.value) {
      params.difficulty = articleDifficulty.value
    }
    const data = await request.get('/article/list', { params })
    articleResult.value = JSON.stringify(data, null, 2)
  } catch (error) {
    articleResult.value = '请求失败: ' + (error.message || error)
  } finally {
    articleLoading.value = false
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f7fb;
  font-family: Arial, sans-serif;
  padding: 24px 16px;
}

.panel {
  max-width: 720px;
  margin: 0 auto;
  padding: 32px;
  border: 1px solid #d8dee9;
  border-radius: 8px;
  background: #ffffff;
}

h1 {
  margin: 0 0 4px;
  font-size: 24px;
}

.subtitle {
  margin: 0 0 20px;
  color: #6b7280;
  font-size: 14px;
}

/* Tabs */
.tabs {
  display: flex;
  gap: 0;
  margin-bottom: 24px;
  border-bottom: 2px solid #e5e7eb;
}

.tab {
  padding: 8px 16px;
  border: 0;
  background: transparent;
  color: #6b7280;
  cursor: pointer;
  font-size: 14px;
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
  transition: color .2s, border-color .2s;
}

.tab:hover {
  color: #2563eb;
}

.tab.active {
  color: #2563eb;
  border-bottom-color: #2563eb;
}

/* Sections */
.test-section {
  margin-bottom: 24px;
}

.test-section h2 {
  font-size: 18px;
  margin: 0 0 12px;
}

/* Forms */
.form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.form label {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 14px;
  color: #374151;
}

.form input, .form select {
  height: 36px;
  padding: 0 10px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
}

button {
  height: 40px;
  padding: 0 16px;
  border: 0;
  border-radius: 6px;
  background: #2563eb;
  color: white;
  font-size: 14px;
  cursor: pointer;
  align-self: flex-start;
  transition: background .2s;
}

button:disabled {
  background: #93c5fd;
  cursor: not-allowed;
}

button:hover:not(:disabled) {
  background: #1d4ed8;
}

/* Result */
.result {
  margin-top: 16px;
  padding: 16px;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  font-size: 13px;
  line-height: 1.5;
  overflow-x: auto;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 400px;
  overflow-y: auto;
}
</style>
