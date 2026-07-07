<template>
  <Teleport to="body">
    <div
      v-if="visible"
      class="fixed inset-0 z-[100] flex items-center justify-center bg-black/30"
      @click.self="$emit('close')"
      @keydown.escape="$emit('close')"
    >
      <div class="bg-white rounded-xl shadow-xl p-6 w-[560px] max-w-[95vw] max-h-[85vh] overflow-y-auto mx-2 sm:mx-0">
        <h3 class="text-lg font-bold text-gray-800 mb-4">
          {{ isEditMode ? '修改文章' : '上传文章' }}
        </h3>

        <!-- 标题 -->
        <div class="flex items-center justify-between mb-1">
          <label class="text-sm text-gray-600">标题</label>
          <button
            class="text-xs px-2 py-0.5 rounded bg-blue-50 text-blue-500 hover:bg-blue-100 transition-colors disabled:opacity-40 inline-flex items-center gap-1"
            :disabled="aiTitleLoading"
            @click="fetchAiTitle"
          >
            <Icon icon="mdi:sparkles" class="text-xs" />
            <span>{{ aiTitleLoading ? 'AI 生成中...' : 'AI 生成标题' }}</span>
          </button>
        </div>
        <input
          v-model="title"
          type="text"
          class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-[#2563EB] mb-2"
          placeholder="请输入或使用 AI 生成标题"
        />
        <!-- AI 生成标题结果 -->
        <div v-if="aiTitleResult && !aiTitleLoading" class="flex items-center gap-2 mb-3 p-2 bg-blue-50 rounded-lg">
          <span class="text-sm text-gray-700 flex-1 truncate">{{ aiTitleResult }}</span>
          <button
            class="text-xs px-2 py-0.5 rounded bg-blue-500 text-white hover:bg-blue-600 transition-colors flex-none"
            @click="applyAiTitle"
          >直接应用</button>
        </div>

        <!-- 内容 -->
        <label class="block text-sm text-gray-600 mb-1">文章内容</label>
        <div v-if="!isEditMode" class="flex items-center gap-2 mb-2">
          <input
            ref="fileInputRef"
            type="file"
            accept=".txt"
            class="hidden"
            @change="handleFileSelect"
          />
          <button
            class="text-xs px-2 py-1 rounded bg-gray-100 text-gray-600 hover:bg-gray-200 transition-colors"
            @click="fileInputRef?.click()"
          >导入 .txt 文件</button>
          <span class="text-xs text-gray-400 truncate flex-1">
            {{ fileName || '或直接在下方输入内容' }}
          </span>
        </div>
        <textarea
          v-model="content"
          rows="10"
          class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-[#2563EB] mb-1 resize-y"
          placeholder="粘贴或输入文章正文..."
        ></textarea>
        <div class="text-right text-xs text-gray-400 mb-3">{{ content.length }} 字符</div>

        <!-- 来源 -->
        <label class="block text-sm text-gray-600 mb-1">来源</label>
        <input
          v-model="source"
          type="text"
          class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-[#2563EB] mb-3"
          placeholder="如：BBC News / 新概念英语 / 自定义"
        />

        <!-- 难度 -->
        <label class="block text-sm text-gray-600 mb-1">难度等级</label>
        <select
          v-model="difficulty"
          class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-[#2563EB] mb-4"
        >
          <option value="" disabled>请选择难度</option>
          <option v-for="d in difficultyOptions" :key="d" :value="d">{{ d }}</option>
        </select>

        <!-- 操作按钮 -->
        <div class="flex justify-end gap-2">
          <button
            class="px-4 py-1.5 text-xs rounded-lg bg-gray-100 text-gray-500 hover:bg-gray-200"
            @click="$emit('close')"
          >取消</button>
          <button
            class="px-4 py-1.5 text-xs rounded-lg bg-[#2563EB] text-white hover:bg-blue-600 disabled:opacity-50"
            :disabled="submitting"
            @click="submit"
          >
            {{ submitting ? '提交中...' : (isEditMode ? '保存修改' : '提交') }}
          </button>
        </div>
      </div>
    </div>

    <!-- Toast -->
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
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Icon } from '@iconify/vue'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'

const props = defineProps({
  visible: { type: Boolean, default: false },
  article: { type: Object, default: null },
})

const emit = defineEmits(['close', 'saved'])

const userStore = useUserStore()

const isEditMode = computed(() => props.article !== null)

const difficultyOptions = ['初中', '高中', '四级', '六级', '考研', '托福', '期刊', '原著', '网络新闻']

// ---- form state ----
const title = ref('')
const content = ref('')
const source = ref('')
const difficulty = ref('')
const fileName = ref('')
const submitting = ref(false)

// ---- AI title ----
const aiTitleLoading = ref(false)
const aiTitleResult = ref('')

// ---- file input ----
const fileInputRef = ref(null)

// ---- toast ----
const toast = ref({ show: false, message: '', type: 'success' })
let toastTimer = null

function showToast(message, type = 'success') {
  if (toastTimer) clearTimeout(toastTimer)
  toast.value = { show: true, message, type }
  toastTimer = setTimeout(() => { toast.value.show = false }, 2500)
}

// ---- form init / reset ----
function resetForm() {
  title.value = ''
  content.value = ''
  source.value = ''
  difficulty.value = ''
  fileName.value = ''
  submitting.value = false
  aiTitleLoading.value = false
  aiTitleResult.value = ''
}

watch(
  () => props.visible,
  async (val) => {
    if (!val) return
    if (props.article) {
      // Edit mode — pre-fill from article prop
      title.value = props.article.title || ''
      content.value = props.article.content || ''
      source.value = props.article.source || ''
      difficulty.value = props.article.difficulty || ''
      fileName.value = ''
      submitting.value = false
      aiTitleLoading.value = false
      aiTitleResult.value = ''

      // Fetch full content if the article prop only carried summary fields
      if (!props.article.content) {
        try {
          const adminUserId = userStore.user?.userId
          const data = await request.get('/admin/article/get', {
            params: { id: props.article.articleId, adminUserId },
          })
          if (data.success && data.article) {
            title.value = data.article.title || ''
            content.value = data.article.content || ''
            source.value = data.article.source || ''
            difficulty.value = data.article.difficulty || ''
          }
        } catch (_) {
          // keep whatever we have
        }
      }
    } else {
      // Create mode
      resetForm()
    }
  }
)

// ---- file handling (create mode only) ----
function handleFileSelect(e) {
  const file = e.target.files?.[0]
  if (!file) return
  fileName.value = file.name
  const reader = new FileReader()
  reader.onload = () => {
    content.value = reader.result
  }
  reader.readAsText(file, 'UTF-8')
}

// ---- AI title generation ----
async function fetchAiTitle() {
  if (!content.value || content.value.trim().length < 50) {
    showToast('文章内容过短，无法生成标题', 'error')
    return
  }
  aiTitleLoading.value = true
  aiTitleResult.value = ''
  try {
    const params = new URLSearchParams()
    params.append('content', content.value.trim())
    const data = await request.post('/clickbait/generate-title', params)
    if (data.success && data.title) {
      aiTitleResult.value = data.title
    } else {
      showToast(data.message || 'AI 标题生成失败', 'error')
    }
  } catch (e) {
    console.error('AI 标题生成失败:', e)
    showToast('AI 标题生成失败，请稍后重试', 'error')
  } finally {
    aiTitleLoading.value = false
  }
}

function applyAiTitle() {
  if (!aiTitleResult.value) return
  title.value = aiTitleResult.value
  aiTitleResult.value = ''
}

// ---- submit ----
async function submit() {
  if (!title.value.trim()) return showToast('请输入标题', 'error')
  if (!content.value.trim()) {
    return showToast('请输入文章内容或导入文件', 'error')
  }
  if (!isEditMode.value && !source.value.trim()) return showToast('请输入来源', 'error')
  if (!difficulty.value) return showToast('请选择难度等级', 'error')

  submitting.value = true
  try {
    const adminUserId = userStore.user?.userId
    if (isEditMode.value) {
      const params = new URLSearchParams()
      params.append('adminUserId', String(adminUserId))
      params.append('articleId', String(props.article.articleId))
      params.append('title', title.value.trim())
      params.append('content', content.value.trim())
      params.append('source', (source.value || '').trim())
      params.append('difficulty', difficulty.value)
      const data = await request.post('/admin/article/update', params)
      if (data.success) {
        showToast('文章修改成功')
        emit('saved')
        emit('close')
      } else {
        showToast(data.message || '修改失败', 'error')
      }
    } else {
      const formData = new FormData()
      formData.append('adminUserId', String(adminUserId))
      formData.append('title', title.value.trim())
      formData.append('content', content.value)
      formData.append('source', source.value.trim())
      formData.append('difficulty', difficulty.value)
      const data = await request.post('/admin/article/create', formData)
      if (data.success) {
        showToast('文章上传成功')
        emit('saved')
        emit('close')
      } else {
        showToast(data.message || '上传失败', 'error')
      }
    }
  } catch (e) {
    console.error(isEditMode.value ? '修改文章失败:' : '上传文章失败:', e)
    const msg = e?.response?.data?.message || e?.message || (isEditMode.value ? '修改失败' : '上传失败')
    showToast(msg, 'error')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.toast-enter-active { transition: all 0.3s ease-out; }
.toast-leave-active { transition: all 0.25s ease-in; }
.toast-enter-from { opacity: 0; transform: translateY(-16px); }
.toast-leave-to { opacity: 0; transform: translateY(-12px); }
</style>
