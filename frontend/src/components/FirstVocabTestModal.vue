<template>
  <Teleport to="body">
    <div
      v-if="visible"
      class="fixed inset-0 z-[300] flex items-center justify-center bg-black/50 backdrop-blur-sm"
    >
      <div class="bg-white rounded-2xl shadow-2xl w-full max-w-2xl max-h-[90vh] overflow-y-auto mx-4">
        <!-- ========== 开始界面 ========== -->
        <div v-if="phase === 'start'" class="p-8 text-center">
          <Icon icon="ph:book-open-text-bold" class="text-5xl text-[#2563EB] mx-auto mb-4" />
          <h2 class="text-2xl font-bold mb-2">初次词汇量检测</h2>
          <p class="text-gray-500 text-sm leading-relaxed mb-4">
            欢迎使用 AAEL！在开始学习之前，请先完成一次词汇量检测。<br/>
            系统将展示 <strong>100 个单词</strong>，请勾选你<strong>认识</strong>的单词。<br/>
            测试中包含部分伪词用于诚信检测——不认识请勿勾选。
          </p>
          <div class="bg-blue-50 border border-blue-200 rounded-xl p-4 mb-6 text-left">
            <p class="text-xs text-blue-700 font-bold mb-1">📌 操作说明</p>
            <ul class="text-xs text-blue-600 space-y-1">
              <li>· 认识该单词 → 勾选 ✓</li>
              <li>· 不认识 / 不确定 → 不勾选</li>
              <li>· 可点击"更多单词"增加测试量提升准确度</li>
            </ul>
          </div>
          <button
            class="px-8 py-3 bg-[#2563EB] text-white rounded-xl font-bold shadow-lg shadow-blue-200 hover:scale-105 transition-transform"
            @click="startTest"
          >
            开始检测
          </button>
        </div>

        <!-- ========== 测试中 ========== -->
        <div v-else-if="phase === 'testing'" class="p-6">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-bold">请勾选你认识的单词</h3>
            <span class="text-xs text-gray-400">{{ words.length }} 个单词</span>
          </div>

          <p class="text-xs text-gray-400 mb-4">
            基础测试 100 个单词，测试得越多结果越准。不认识或不确定的请勿勾选。
          </p>

          <!-- 单词网格 -->
          <div class="grid grid-cols-5 gap-2 mb-6 max-h-[50vh] overflow-y-auto">
            <div
              v-for="(item, idx) in words"
              :key="idx"
              class="flex flex-col items-center p-2 border border-gray-200 rounded-lg cursor-pointer transition-colors select-none"
              :class="item.checked ? 'bg-blue-50 border-blue-300' : 'hover:bg-gray-50'"
              @click="item.checked = !item.checked"
            >
              <span class="text-sm font-bold" :class="item.checked ? 'text-[#2563EB]' : 'text-gray-700'">
                {{ item.word }}
              </span>
              <span class="text-[10px] mt-1" :class="item.checked ? 'text-blue-500' : 'text-gray-400'">
                {{ item.checked ? '✓ 认识' : '不认识' }}
              </span>
            </div>
          </div>

          <!-- 按钮行 -->
          <div class="flex items-center justify-center gap-4">
            <button
              class="px-6 py-2.5 bg-white border border-gray-200 rounded-xl text-sm font-bold text-gray-500 hover:border-[#2563EB] hover:text-[#2563EB] transition-colors"
              :disabled="!canAddMore"
              @click="addMoreWords"
            >
              {{ canAddMore ? '更多单词（+10）' : '单词已用完' }}
            </button>
            <button
              class="px-8 py-2.5 bg-[#2563EB] text-white rounded-xl text-sm font-bold hover:bg-blue-600 transition-colors"
              :disabled="submitting"
              @click="submitTest"
            >
              {{ submitting ? '提交中...' : '提交检测' }}
            </button>
          </div>
        </div>

        <!-- ========== 结果界面 ========== -->
        <div v-else-if="phase === 'result'" class="p-8 text-center">
          <Icon icon="ph:trophy-bold" class="text-5xl text-yellow-500 mx-auto mb-4" />
          <h2 class="text-2xl font-bold mb-1">检测完成！</h2>
          <p class="text-gray-400 text-sm mb-6">AI 已评估你的初始英语能力</p>

          <div class="grid grid-cols-2 gap-4 mb-6">
            <div class="bg-blue-50 rounded-xl p-4">
              <p class="text-3xl font-black text-[#2563EB]">{{ resultVocab.toLocaleString() }}</p>
              <p class="text-xs text-gray-400">估算词汇量</p>
            </div>
            <div class="bg-green-50 rounded-xl p-4">
              <p class="text-3xl font-black text-green-500">{{ resultCefr }}</p>
              <p class="text-xs text-gray-400">{{ resultCefrLabel }} · CEFR 等级</p>
            </div>
            <div class="bg-gray-50 rounded-xl p-4 col-span-2">
              <p class="text-sm text-gray-500">
                95% 置信区间：
                <strong>{{ resultLower }} – {{ resultUpper }}</strong>
              </p>
              <p class="text-xs text-gray-400 mt-1">
                伪词命中率 {{ resultFakeHit }}%（越低越诚实）
              </p>
            </div>
          </div>

          <button
            class="px-8 py-3 bg-[#2563EB] text-white rounded-xl font-bold shadow-lg shadow-blue-200 hover:scale-105 transition-transform"
            @click="$emit('done', { vocab: resultVocab, cefr: resultCefr })"
          >
            开始学习
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref } from 'vue'
import { Icon } from '@iconify/vue'
import request from '@/utils/request'

const props = defineProps({
  visible: { type: Boolean, default: false },
})

const emit = defineEmits(['done'])

const phase = ref('start')
const words = ref([])
const submitting = ref(false)
const canAddMore = ref(true)

const resultVocab = ref(0)
const resultCefr = ref('A1')
const resultCefrLabel = ref('初级')
const resultLower = ref(0)
const resultUpper = ref(0)
const resultFakeHit = ref(0)

async function startTest() {
  phase.value = 'testing'
  try {
    const data = await request.get('/vocabtest/words')
    words.value = (data.words || []).map(w => ({
      ...w,
      checked: false,
    }))
  } catch (e) {
    console.error('获取测试单词失败:', e)
    alert('获取测试单词失败，请重试')
    phase.value = 'start'
  }
}

async function addMoreWords() {
  // 追加调用：复用同一个 session，后端会从 session 中取已用词去重
  try {
    const data = await request.get('/vocabtest/words?more=10')
    const newWords = (data.words || []).map(w => ({
      ...w,
      checked: false,
    }))
    if (newWords.length > 0) {
      words.value.push(...newWords)
    } else {
      canAddMore.value = false
    }
  } catch (e) {
    console.error('获取更多单词失败:', e)
    canAddMore.value = false
  }
}

async function submitTest() {
  submitting.value = true
  try {
    const answers = words.value.map(w => w.checked ? '1' : '0').join(',')
    const params = new URLSearchParams()
    const userId = JSON.parse(localStorage.getItem('user') || '{}').userId
    params.append('userId', String(userId || ''))
    params.append('answers', answers)
    const data = await request.post('/vocabtest/submit', params)
    if (data.success) {
      resultVocab.value = data.estimatedVocab || 0
      resultCefr.value = data.cefrLevel || 'A1'
      resultCefrLabel.value = data.cefrLabel || '初级'
      resultLower.value = data.lowerCI || 0
      resultUpper.value = data.upperCI || 0
      resultFakeHit.value = data.fakeHitRate || 0
      phase.value = 'result'
    } else {
      alert(data.message || '提交失败')
    }
  } catch (e) {
    console.error('提交测试失败:', e)
    alert('提交失败，请重试')
  } finally {
    submitting.value = false
  }
}
</script>
