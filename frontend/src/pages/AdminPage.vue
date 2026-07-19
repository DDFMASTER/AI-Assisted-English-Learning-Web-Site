<template>
  <main class="max-w-[1200px] mx-auto px-6 py-8 overflow-x-auto">
    <h1 class="text-2xl font-bold text-[#1F2937] dark:text-gray-200 mb-6">管理后台</h1>

    <!-- Tab 切换 -->
    <div class="flex gap-1 mb-6 border-b border-gray-200">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        class="px-4 py-2 text-sm font-medium rounded-t-lg transition-colors"
        :class="activeTab === tab.key
          ? 'bg-white text-[#2563EB] border border-b-white border-gray-200 -mb-px'
          : 'text-gray-500 hover:text-gray-700'"
        @click="activeTab = tab.key"
      >
        {{ tab.emoji }} {{ tab.label }}
      </button>
    </div>

    <!-- 用户管理 -->
    <div v-if="activeTab === 'users'">
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-lg font-bold text-gray-700">👥 用户列表</h2>
        <button
          class="px-3 py-1.5 text-xs bg-[#2563EB] text-white rounded-lg hover:bg-blue-600 transition-colors"
          @click="loadUsers"
        >
          刷新
        </button>
      </div>

      <div v-if="usersLoading" class="text-center py-8 text-gray-400">加载中...</div>

      <div v-else class="overflow-x-auto">
        <table class="min-w-[700px] w-full text-sm">
          <thead>
            <tr class="border-b border-gray-100 text-left text-gray-400">
              <th class="py-2 px-3">ID</th>
              <th class="py-2 px-3">用户名</th>
              <th class="py-2 px-3">角色</th>
              <th class="py-2 px-3">学习阶段</th>
              <th class="py-2 px-3">经验值</th>
              <th class="py-2 px-3">VIP剩余</th>
              <th class="py-2 px-3">VIP截止</th>
              <th class="py-2 px-3">最近登录</th>
              <th class="py-2 px-3">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="u in users"
              :key="u.userId"
              class="border-b border-gray-50 hover:bg-gray-50 transition-colors"
            >
              <td class="py-2 px-3 text-gray-500">{{ u.userId }}</td>
              <td class="py-2 px-3 font-medium text-gray-700">{{ u.username }}</td>
              <td class="py-2 px-3">
                <span
                  class="text-xs px-1.5 py-0.5 rounded-full"
                  :class="u.role === 'admin' ? 'bg-red-100 text-red-600' : 'bg-gray-100 text-gray-500'"
                >{{ u.role || 'normal' }}</span>
              </td>
              <td class="py-2 px-3 text-gray-500">{{ u.studyPurpose || '-' }}</td>
              <td class="py-2 px-3 text-gray-500">{{ u.experience || 0 }}</td>
              <td class="py-2 px-3 text-xs">
                <span v-if="u.profile === 'vip' && u.vipUntil" class="text-yellow-600 font-bold">{{ vipDaysLeft(u.vipUntil) }}</span>
                <span v-else class="text-gray-300">-</span>
              </td>
              <td class="py-2 px-3 text-gray-400 text-xs">{{ fmtVipUntil(u.vipUntil) }}</td>
              <td class="py-2 px-3 text-gray-400 text-xs">{{ u.lastLogin || '-' }}</td>
              <td class="py-2 px-3">
                <div class="flex gap-1">
                  <button
                    class="text-xs px-2 py-0.5 rounded bg-yellow-50 text-yellow-600 hover:bg-yellow-100 transition-colors"
                    @click="openGrantVip(u)"
                  >VIP</button>
                  <button
                    v-if="u.role !== 'admin'"
                    class="text-xs px-2 py-0.5 rounded bg-red-50 text-red-500 hover:bg-red-100 transition-colors"
                    @click="deleteUser(u)"
                  >删除</button>
                  <button
                    v-if="u.role !== 'admin'"
                    class="text-xs px-2 py-0.5 rounded bg-gray-100 text-gray-500 hover:bg-gray-200 transition-colors"
                    @click="resetPwd(u)"
                  >重置密码</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 文章管理 -->
    <div v-if="activeTab === 'articles'">
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-lg font-bold text-gray-700">
          📰 文章列表
          <span class="text-sm font-normal text-gray-400 ml-2">（共 {{ articleTotal }} 篇）</span>
        </h2>
        <div class="flex items-center gap-2">
          <button
            class="px-3 py-1.5 text-xs bg-green-500 text-white rounded-lg hover:bg-green-600 transition-colors"
            @click="openUploadModal"
          >
            + 上传文章
          </button>
          <span class="text-xs text-gray-400">
            第 {{ articlePage }} / {{ articleTotalPages }} 页
          </span>
          <button
            class="px-3 py-1.5 text-xs bg-[#2563EB] text-white rounded-lg hover:bg-blue-600 transition-colors"
            @click="loadArticles"
          >
            刷新
          </button>
        </div>
      </div>

      <div v-if="articlesLoading" class="text-center py-8 text-gray-400">加载中...</div>

      <div v-else class="overflow-x-auto">
        <table class="min-w-[700px] w-full text-sm">
          <thead>
            <tr class="border-b border-gray-100 text-left text-gray-400">
              <th class="py-2 px-3">ID</th>
              <th class="py-2 px-3">标题</th>
              <th class="py-2 px-3">难度</th>
              <th class="py-2 px-3">来源</th>
              <th class="py-2 px-3">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="a in articles"
              :key="a.articleId"
              class="border-b border-gray-50 hover:bg-gray-50 transition-colors"
            >
              <td class="py-2 px-3 text-gray-500">{{ a.articleId }}</td>
              <td class="py-2 px-3 font-medium text-gray-700 max-w-xs truncate block">{{ a.title }}</td>
              <td class="py-2 px-3">
                <span class="text-xs px-1.5 py-0.5 rounded-full bg-gray-100 text-gray-500">{{ a.difficulty || '-' }}</span>
              </td>
              <td class="py-2 px-3 text-gray-400 text-xs">{{ a.source || '-' }}</td>
              <td class="py-2 px-3">
                <div class="flex gap-1">
                  <button
                    class="text-xs px-2 py-0.5 rounded bg-blue-50 text-blue-500 hover:bg-blue-100 transition-colors"
                    @click="openEditModal(a)"
                  >修改</button>
                  <button
                    class="text-xs px-2 py-0.5 rounded bg-red-50 text-red-500 hover:bg-red-100 transition-colors"
                    @click="deleteArticle(a)"
                  >删除</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <!-- 分页 -->
        <AppPagination :current-page="articlePage" :total-pages="articleTotalPages" @change="p => { articlePage = p; loadArticles() }" />
      </div>
    </div>

    <!-- 在线用户 -->
    <div v-if="activeTab === 'online'">
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-lg font-bold text-gray-700">
          🟢 实时在线用户
          <span class="text-sm font-normal text-gray-400 ml-2">（{{ onlineCount }} 人在线）</span>
        </h2>
        <button
          class="px-3 py-1.5 text-xs bg-[#2563EB] text-white rounded-lg hover:bg-blue-600 transition-colors"
          @click="loadOnlineUsers"
        >
          刷新
        </button>
      </div>

      <div v-if="onlineLoading" class="text-center py-8 text-gray-400">加载中...</div>

      <div v-else-if="onlineUsers.length === 0" class="text-center py-8 text-gray-400">暂无在线用户</div>

      <div v-else class="overflow-x-auto">
        <table class="min-w-[700px] w-full text-sm">
          <thead>
            <tr class="border-b border-gray-100 text-left text-gray-400">
              <th class="py-2 px-3">用户ID</th>
              <th class="py-2 px-3">用户名</th>
              <th class="py-2 px-3">角色</th>
              <th class="py-2 px-3">学习阶段</th>
              <th class="py-2 px-3">Session</th>
              <th class="py-2 px-3">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="u in onlineUsers"
              :key="u.sessionId"
              class="border-b border-gray-50 hover:bg-gray-50 transition-colors"
            >
              <td class="py-2 px-3 text-gray-500">{{ u.userId }}</td>
              <td class="py-2 px-3 font-medium text-gray-700">{{ u.username }}</td>
              <td class="py-2 px-3">
                <span class="text-xs px-1.5 py-0.5 rounded-full bg-gray-100 text-gray-500">{{ u.role || 'normal' }}</span>
              </td>
              <td class="py-2 px-3 text-gray-500">{{ u.studyPurpose || '-' }}</td>
              <td class="py-2 px-3 text-gray-400 text-xs font-mono">{{ u.sessionId?.substring(0, 12) }}...</td>
              <td class="py-2 px-3">
                <button
                  v-if="u.role !== 'admin'"
                  class="text-xs px-2 py-0.5 rounded bg-red-50 text-red-500 hover:bg-red-100 transition-colors"
                  @click="kickUser(u)"
                >强制下线</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 请求日志 -->
    <div v-if="activeTab === 'logs'">
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-lg font-bold text-gray-700">
          📋 IP 请求日志
          <span class="text-sm font-normal text-gray-400 ml-2">（共 {{ logTotal }} 条）</span>
        </h2>
        <div class="flex gap-2">
          <button class="px-3 py-1.5 text-xs bg-red-500 text-white rounded-lg hover:bg-red-600 transition-colors" @click="handleClearLogs">清空</button>
          <button class="px-3 py-1.5 text-xs bg-green-500 text-white rounded-lg hover:bg-green-600 transition-colors" @click="backupLogs">备份</button>
          <button class="px-3 py-1.5 text-xs bg-[#2563EB] text-white rounded-lg hover:bg-blue-600 transition-colors" @click="loadLogs">刷新</button>
        </div>
        <!-- 筛选栏 -->
        <div class="flex flex-wrap gap-2 mb-3">
          <input
            v-model="logFilter.ip"
            type="text"
            placeholder="IP 筛选..."
            class="h-8 px-2.5 text-xs border border-gray-200 rounded-lg focus:outline-none focus:border-[#2563EB] w-32"
            @keydown.enter="loadLogs(1)"
          />
          <input
            v-model="logFilter.user"
            type="text"
            placeholder="用户筛选..."
            class="h-8 px-2.5 text-xs border border-gray-200 rounded-lg focus:outline-none focus:border-[#2563EB] w-28"
            @keydown.enter="loadLogs(1)"
          />
          <input
            v-model="logFilter.timeFrom"
            type="datetime-local"
            class="h-8 px-2 text-xs border border-gray-200 rounded-lg focus:outline-none focus:border-[#2563EB] text-gray-500"
            title="开始时间"
          />
          <span class="text-xs text-gray-400 self-center">—</span>
          <input
            v-model="logFilter.timeTo"
            type="datetime-local"
            class="h-8 px-2 text-xs border border-gray-200 rounded-lg focus:outline-none focus:border-[#2563EB] text-gray-500"
            title="结束时间"
          />
          <button
            class="px-3 py-1 h-8 text-xs bg-gray-100 text-gray-600 rounded-lg hover:bg-gray-200 transition-colors"
            @click="applyLogFilter"
          >筛选</button>
          <button
            v-if="hasLogFilter"
            class="px-3 py-1 h-8 text-xs text-red-400 hover:text-red-600 transition-colors"
            @click="clearLogFilter"
          >清除筛选</button>
        </div>
      </div>

      <div v-if="logsLoading" class="text-center py-8 text-gray-400">加载中...</div>

      <div v-else class="overflow-x-auto">
        <table class="min-w-[700px] w-full text-sm">
          <thead>
            <tr class="border-b border-gray-100 text-left text-gray-400">
              <th class="py-2 px-3 w-16">操作</th>
              <th class="py-2 px-3">时间</th>
              <th class="py-2 px-3">IP</th>
              <th class="py-2 px-3">方法</th>
              <th class="py-2 px-3">路径</th>
              <th class="py-2 px-3">用户</th>
              <th class="py-2 px-3">Session</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="(log, idx) in logs"
              :key="idx"
              class="border-b border-gray-50 hover:bg-gray-50 transition-colors"
            >
              <td class="py-2 px-3">
                <button class="text-xs text-red-400 hover:text-red-600 transition-colors" @click="deleteLogEntry(log.originalIndex)">删除</button>
              </td>
              <td class="py-2 px-3 text-gray-400 text-xs whitespace-nowrap">{{ log.timestamp }}</td>
              <td class="py-2 px-3 text-gray-500 font-mono text-xs">{{ log.ip }}</td>
              <td class="py-2 px-3">
                <span class="text-xs px-1.5 py-0.5 rounded-full"
                  :class="log.method === 'POST' ? 'bg-blue-100 text-blue-600' : 'bg-green-100 text-green-600'"
                >{{ log.method }}</span>
              </td>
              <td class="py-2 px-3 text-gray-500 text-xs font-mono max-w-xs truncate block">{{ log.path }}</td>
              <td class="py-2 px-3 text-gray-500 text-xs">{{ log.username || '-' }}</td>
              <td class="py-2 px-3 text-gray-400 text-xs font-mono">{{ log.sessionId || '-' }}</td>
            </tr>
          </tbody>
        </table>

        <!-- 分页 -->
        <div v-if="logTotalPages > 1" class="flex items-center justify-center gap-3 mt-4 pb-2">
          <button class="px-3 py-1.5 rounded-lg text-sm font-bold transition-all"
            :class="logPage > 1 ? 'bg-gray-100 text-gray-600 hover:bg-gray-200' : 'text-gray-300 cursor-not-allowed'"
            :disabled="logPage <= 1" @click="loadLogs(logPage - 1)">
            ←
          </button>
          <span class="text-xs text-gray-400">{{ logPage }} / {{ logTotalPages }}</span>
          <button class="px-3 py-1.5 rounded-lg text-sm font-bold transition-all"
            :class="logPage < logTotalPages ? 'bg-gray-100 text-gray-600 hover:bg-gray-200' : 'text-gray-300 cursor-not-allowed'"
            :disabled="logPage >= logTotalPages" @click="loadLogs(logPage + 1)">
            →
          </button>
        </div>
      </div>

      <!-- 清空确认弹窗 -->
      <Teleport to="body">
        <div v-if="clearLogsDialog.show" class="fixed inset-0 z-[200] flex items-center justify-center bg-black/40 backdrop-blur-sm" @click.self="clearLogsDialog.show = false">
          <div class="bg-white rounded-2xl shadow-2xl p-6 w-full max-w-sm mx-4 text-center">
            <span class="text-5xl">⚠️</span>
            <h3 class="text-lg font-bold mb-2">清空 IP 日志</h3>
            <p class="text-sm text-gray-500 mb-6">即将清空全部 {{ logTotal }} 条 IP 日志，是否需要备份？</p>
            <div class="flex gap-3">
              <button class="flex-1 py-3 rounded-xl bg-red-500 text-white font-bold hover:bg-red-600 transition-all text-sm" @click="clearLogsDialog.onDirect()">直接删除</button>
              <button class="flex-1 py-3 rounded-xl bg-green-500 text-white font-bold hover:bg-green-600 transition-all text-sm" @click="clearLogsDialog.onBackup()">备份并删除</button>
            </div>
            <button class="mt-3 w-full text-sm text-gray-400 hover:text-gray-600 transition-colors" @click="clearLogsDialog.show = false">取消</button>
          </div>
        </div>
      </Teleport>
    </div>

    <!-- Toast 提示卡片 -->
    <Teleport to="body">
      <Transition name="toast">
        <div
          v-if="toast.show"
          class="fixed top-6 left-1/2 -translate-x-1/2 z-[200] px-5 py-3 rounded-xl shadow-lg text-sm font-medium"
          :class="toast.type === 'success' ? 'bg-green-500 text-white' : 'bg-red-500 text-white'"
        >
          {{ toast.message }}
        </div>
      </Transition>
    </Teleport>

    <!-- 上传文章弹窗 -->
    <ArticleFormModal
      :visible="uploadModalShow"
      @close="uploadModalShow = false"
      @saved="loadArticles"
    />

    <!-- 修改文章弹窗 -->
    <ArticleFormModal
      :visible="editModalShow"
      :article="editModalData"
      @close="editModalShow = false"
      @saved="loadArticles"
    />

    <!-- 授予 VIP 弹窗 -->
    <Teleport to="body">
      <div v-if="grantVipModal.show" class="fixed inset-0 z-[100] flex items-center justify-center bg-black/30" @click.self="grantVipModal.show = false">
        <div class="bg-white rounded-xl shadow-xl p-6 w-80">
          <h3 class="text-lg font-bold text-gray-800 mb-2">授予 VIP</h3>
          <p class="text-sm text-gray-500 mb-4">为用户 <strong>{{ grantVipModal.username }}</strong> 授予 VIP 天数：</p>
          <div class="flex gap-2 mb-4">
            <button v-for="d in [1,7,30,90]" :key="d"
              class="flex-1 py-2 rounded-lg text-sm font-bold border transition-all"
              :class="grantVipModal.days === d ? 'bg-yellow-100 border-yellow-400 text-yellow-700' : 'bg-white border-gray-200 text-gray-500 hover:border-yellow-300'"
              @click="grantVipModal.days = d">{{ d }}天</button>
          </div>
          <div class="flex justify-end gap-2">
            <button class="px-4 py-1.5 text-xs rounded-lg bg-gray-100 text-gray-500 hover:bg-gray-200" @click="grantVipModal.show = false">取消</button>
            <button class="px-4 py-1.5 text-xs rounded-lg bg-yellow-400 text-white hover:bg-yellow-500 disabled:opacity-50" :disabled="grantVipModal.submitting" @click="submitGrantVip">
              {{ grantVipModal.submitting ? '处理中...' : '确认授予' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- 确认弹窗 -->
    <Teleport to="body">
      <div
        v-if="confirmDialog.show"
        class="fixed inset-0 z-[100] flex items-center justify-center bg-black/30"
        @click.self="confirmDialog.show = false"
      >
        <div class="bg-white rounded-xl shadow-xl p-6 w-80">
          <p class="text-sm text-gray-700 mb-4">{{ confirmDialog.message }}</p>
          <div class="flex justify-end gap-2">
            <button
              class="px-4 py-1.5 text-xs rounded-lg bg-gray-100 text-gray-500 hover:bg-gray-200"
              @click="confirmDialog.show = false"
            >取消</button>
            <button
              class="px-4 py-1.5 text-xs rounded-lg bg-red-500 text-white hover:bg-red-600"
              @click="confirmDialog.onConfirm(); confirmDialog.show = false"
            >确认</button>
          </div>
        </div>
      </div>
    </Teleport>
  </main>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import AppPagination from '@/components/AppPagination.vue'
import ArticleFormModal from '@/components/ArticleFormModal.vue'

const userStore = useUserStore()

// ========== Tab ==========
const tabs = [
  { key: 'users', label: '用户管理', emoji: '👥' },
  { key: 'articles', label: '文章管理', emoji: '📰' },
  { key: 'online', label: '在线用户', emoji: '🟢' },
  { key: 'logs', label: 'IP日志', emoji: '📋' },
]
const activeTab = ref('users')

// ========== 用户管理 ==========
const users = ref([])
const usersLoading = ref(false)

async function loadUsers() {
  usersLoading.value = true
  try {
    const adminUserId = userStore.user?.userId
    const data = await request.get('/admin/user/list', { params: { adminUserId } })
    if (data.success) {
      users.value = data.users || []
    }
  } catch (e) {
    console.error('加载用户列表失败:', e)
  } finally {
    usersLoading.value = false
  }
}

// ========== 在线用户 ==========
const onlineUsers = ref([])
const onlineCount = ref(0)
const onlineLoading = ref(false)

async function loadOnlineUsers() {
  onlineLoading.value = true
  try {
    const adminUserId = userStore.user?.userId
    const data = await request.get('/admin/monitor/online-users', { params: { adminUserId } })
    if (data.success) {
      onlineUsers.value = data.onlineUsers || []
      onlineCount.value = data.onlineCount || 0
    }
  } catch (e) {
    console.error('加载在线用户失败:', e)
  } finally {
    onlineLoading.value = false
  }
}

// ========== 请求日志 ==========
const logs = ref([])
const logTotal = ref(0)
const logPage = ref(1)
const logTotalPages = ref(1)
const logsLoading = ref(false)

// 筛选条件
const logFilter = reactive({ ip: '', user: '', timeFrom: '', timeTo: '' })
const hasLogFilter = computed(() => {
  return logFilter.ip || logFilter.user || logFilter.timeFrom || logFilter.timeTo
})

function applyLogFilter() {
  logPage.value = 1
  loadLogs(1)
}

function clearLogFilter() {
  logFilter.ip = ''
  logFilter.user = ''
  logFilter.timeFrom = ''
  logFilter.timeTo = ''
  logPage.value = 1
  loadLogs(1)
}

async function loadLogs(page = 1) {
  logsLoading.value = true
  try {
    const adminUserId = userStore.user?.userId
    const params = { adminUserId, page, pageSize: 15 }
    // 传递筛选条件
    if (logFilter.ip) params.ipFilter = logFilter.ip
    if (logFilter.user) params.userFilter = logFilter.user
    if (logFilter.timeFrom) params.timeFrom = logFilter.timeFrom.replace('T', ' ') + ':00'
    if (logFilter.timeTo) params.timeTo = logFilter.timeTo.replace('T', ' ') + ':00'

    const data = await request.get('/admin/monitor/request-logs', { params })
    if (data.success) {
      // 为每条日志添加 originalIndex（在全部日志列表中的位置），用于删除
      // 使用过滤后的 totalCount
      const startIdx = data.totalCount - (data.page - 1) * 15 - 1
      logs.value = (data.logs || []).map((l, i) => ({ ...l, originalIndex: startIdx - i }))
      logTotal.value = data.totalCount || 0
      logPage.value = data.page || 1
      logTotalPages.value = data.totalPages || 1
    }
  } catch (e) {
    console.error('加载请求日志失败:', e)
  } finally {
    logsLoading.value = false
  }
}

async function deleteLogEntry(index) {
  try {
    const adminUserId = userStore.user?.userId
    const data = await request.post('/admin/monitor/request-logs', { adminUserId, action: 'delete', index })
    if (data.success) {
      showToast('已删除')
      await loadLogs(logPage.value)
    } else {
      showToast(data.message || '删除失败', 'error')
    }
  } catch (e) {
    showToast('网络错误', 'error')
  }
}

async function backupLogs() {
  try {
    const adminUserId = userStore.user?.userId
    // 使用 axios 获取 blob，确保 baseURL 和 cookie 正确
    const resp = await request.post('/admin/monitor/request-logs',
      { adminUserId, action: 'backup' },
      { responseType: 'blob' }
    )
    const url = URL.createObjectURL(resp)
    const a = document.createElement('a')
    a.href = url
    a.download = 'AAEL-IP-logs-' + Date.now() + '.txt'
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
    showToast('备份已下载')
  } catch (e) {
    showToast('备份失败', 'error')
  }
}

const clearLogsDialog = ref({
  show: false,
  onDirect: () => {},
  onBackup: () => {},
})

function handleClearLogs() {
  clearLogsDialog.value = {
    show: true,
    onDirect: async () => {
      clearLogsDialog.value.show = false
      try {
        const adminUserId = userStore.user?.userId
        const data = await request.post('/admin/monitor/request-logs', { adminUserId, action: 'clear' })
        if (data.success) { showToast('已清空'); await loadLogs(1) }
        else showToast(data.message || '清空失败', 'error')
      } catch (e) { showToast('网络错误', 'error') }
    },
    onBackup: async () => {
      clearLogsDialog.value.show = false
      await backupLogs()
      try {
        const adminUserId = userStore.user?.userId
        const data = await request.post('/admin/monitor/request-logs', { adminUserId, action: 'clear' })
        if (data.success) { showToast('已备份并清空'); await loadLogs(1) }
        else showToast(data.message || '清空失败', 'error')
      } catch (e) { showToast('网络错误', 'error') }
    },
  }
}

// ========== Toast ==========
const toast = ref({ show: false, message: '', type: 'success' })
let toastTimer = null

function showToast(message, type = 'success') {
  if (toastTimer) clearTimeout(toastTimer)
  toast.value = { show: true, message, type }
  toastTimer = setTimeout(() => { toast.value.show = false }, 2500)
}

// ========== 上传文章弹窗 ==========
const uploadModalShow = ref(false)

function openUploadModal() {
  uploadModalShow.value = true
}

// ========== 确认弹窗 ==========
const confirmDialog = ref({
  show: false,
  message: '',
  onConfirm: () => {},
})

function showConfirm(message, onConfirm) {
  confirmDialog.value = { show: true, message, onConfirm }
}

// ========== 操作 ==========
const adminUserId = () => userStore.user?.userId

// ===== 授予 VIP =====
const grantVipModal = ref({ show: false, username: '', targetUserId: null, days: 7, submitting: false })

function vipDaysLeft(vipUntil) {
  if (!vipUntil) return '-'
  const d = new Date(vipUntil.replace(' ', 'T'))
  const now = new Date()
  const left = Math.ceil((d - now) / 86400000)
  return left > 0 ? `剩余 ${left} 天` : '已过期'
}

function fmtVipUntil(vipUntil) {
  if (!vipUntil) return '-'
  return vipUntil.substring(0, 16)
}

function openGrantVip(u) {
  grantVipModal.value = { show: true, username: u.username, targetUserId: u.userId, days: 7, submitting: false }
}

async function submitGrantVip() {
  const m = grantVipModal.value
  m.submitting = true
  try {
    const params = new URLSearchParams()
    params.append('adminUserId', String(userStore.user?.userId))
    params.append('targetUserId', String(m.targetUserId))
    params.append('days', String(m.days))
    const data = await request.post('/admin/user/grant-vip', params)
    if (data.success) {
      showToast(data.message || 'VIP 授予成功')
      grantVipModal.value.show = false
      loadUsers()
    } else {
      showToast(data.message || '操作失败', 'error')
    }
  } catch (e) {
    showToast('操作失败', 'error')
  } finally { m.submitting = false }
}

async function deleteUser(u) {
  showConfirm(`确定要删除用户 "${u.username}"（ID: ${u.userId}）吗？此操作不可撤销。`, async () => {
    try {
      const params = new URLSearchParams()
      params.append('adminUserId', String(adminUserId()))
      params.append('userId', String(u.userId))
      const data = await request.post('/admin/user/delete', params)
      if (data.success) {
        users.value = users.value.filter(x => x.userId !== u.userId)
      } else {
        alert(data.message || '删除失败')
      }
    } catch (e) {
      console.error('删除用户失败:', e)
      alert('删除失败，请稍后重试')
    }
  })
}

async function resetPwd(u) {
  showConfirm(`确定重置用户 "${u.username}" 的密码为 123456 吗？`, async () => {
    try {
      const params = new URLSearchParams()
      params.append('adminUserId', String(adminUserId()))
      params.append('userId', String(u.userId))
      const data = await request.post('/admin/user/reset-password', params)
      alert(data.message || '密码已重置')
    } catch (e) {
      console.error('重置密码失败:', e)
      alert('操作失败')
    }
  })
}



async function kickUser(u) {
  showConfirm(`确定强制下线用户 "${u.username}" 吗？`, async () => {
    try {
      const params = new URLSearchParams()
      params.append('adminUserId', String(adminUserId()))
      params.append('targetUserId', String(u.userId))
      const data = await request.post('/admin/monitor/kick-user', params)
      if (data.success) {
        onlineUsers.value = onlineUsers.value.filter(x => x.userId !== u.userId)
        onlineCount.value = Math.max(0, onlineCount.value - 1)
      }
      alert(data.message || '操作完成')
    } catch (e) {
      console.error('强制下线失败:', e)
      alert('操作失败')
    }
  })
}

// ========== 文章管理 ==========
const articles = ref([])
const articleTotal = ref(0)
const articlePage = ref(1)
const articlePageSize = 10
const articlesLoading = ref(false)

async function loadArticles() {
  articlesLoading.value = true
  try {
    const adminUserId = userStore.user?.userId
    const data = await request.get('/admin/article/list', {
      params: { adminUserId, page: articlePage.value, pageSize: articlePageSize }
    })
    if (data.success) {
      articles.value = data.articles || []
      articleTotal.value = data.total || 0
    }
  } catch (e) {
    console.error('加载文章列表失败:', e)
  } finally {
    articlesLoading.value = false
  }
}

const articleTotalPages = computed(() => Math.max(1, Math.ceil(articleTotal.value / articlePageSize)))

// ====== 修改文章弹窗 ======
const editModalShow = ref(false)
const editModalData = ref(null)

function openEditModal(a) {
  editModalData.value = {
    articleId: a.articleId,
    title: a.title,
    source: a.source,
    difficulty: a.difficulty,
  }
  editModalShow.value = true
}

async function deleteArticle(a) {
  showConfirm(`确定要删除文章 "${a.title}"（ID: ${a.articleId}）吗？此操作不可撤销，将级联删除关联数据。`, async () => {
    try {
      const params = new URLSearchParams()
      params.append('adminUserId', String(adminUserId()))
      params.append('articleId', String(a.articleId))
      const data = await request.post('/admin/article/delete', params)
      if (data.success) {
        articles.value = articles.value.filter(x => x.articleId !== a.articleId)
        articleTotal.value = Math.max(0, articleTotal.value - 1)
        // 同步清除浏览历史中该文章的记录
        import('@/utils/historyDB').then(({ removeFromHistory }) => {
          removeFromHistory(a.articleId).catch(() => {})
        })
      } else {
        alert(data.message || '删除失败')
      }
    } catch (e) {
      console.error('删除文章失败:', e)
      alert('删除失败，请稍后重试')
    }
  })
}

// ========== 监听 Tab 切换自动加载数据 ==========
// （简单实现：切换到文章 tab 时首次加载）
let onlineRefreshTimer = null

watch(activeTab, (tab) => {
  if (tab === 'users' && users.value.length === 0) {
    loadUsers()
  }
  if (tab === 'articles' && articles.value.length === 0) {
    loadArticles()
  }
  if (tab === 'online') {
    if (onlineUsers.value.length === 0) {
      loadOnlineUsers()
    }
    // 每分钟自动刷新在线用户
    if (!onlineRefreshTimer) {
      onlineRefreshTimer = setInterval(() => {
        loadOnlineUsers()
      }, 60000)
    }
  } else {
    if (onlineRefreshTimer) {
      clearInterval(onlineRefreshTimer)
      onlineRefreshTimer = null
    }
  }
  if (tab === 'logs' && logs.value.length === 0) {
    loadLogs()
  }
})

onUnmounted(() => {
  if (onlineRefreshTimer) {
    clearInterval(onlineRefreshTimer)
    onlineRefreshTimer = null
  }
})


// 初始化：默认 tab 为 users 时加载用户列表
if (activeTab.value === 'users') loadUsers()

</script>

<style scoped>
.toast-enter-active { transition: all 0.3s ease-out; }
.toast-leave-active { transition: all 0.25s ease-in; }
.toast-enter-from { opacity: 0; transform: translateY(-16px); }
.toast-leave-to { opacity: 0; transform: translateY(-12px); }
</style>
