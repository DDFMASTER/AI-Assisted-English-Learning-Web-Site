/**
 * 错题本导出/导入工具
 *
 * 两种导出模式：
 *   1. 设备迁移（.ewb） — 结构化 JSON，可重新导入
 *   2. 可编辑文件（.json） — 美化输出，供人工阅读/编辑
 *
 * 导入仅支持 .ewb 格式，按 UUID 自动去重。
 */

/**
 * 导出为设备迁移文件（.ewb）
 * @param {Array} questions — getAllWrongQuestions() 的返回结果
 */
export function exportToEWB(questions) {
  const data = {
    type: 'engliai-wrongbook',
    version: 1,
    exportedAt: Date.now(),
    count: questions.length,
    questions: questions.map(stripForExport),
  }
  downloadJSON(data, `engliai-wrongbook-${fmtDate(new Date())}.ewb`)
}

/**
 * 导出为可编辑 JSON 文件
 * @param {Array} questions — getAllWrongQuestions() 的返回结果
 */
export function exportToJSON(questions) {
  const list = questions.map((q) => ({
    ...stripForExport(q),
    addedAtFormatted: formatTimestamp(q.addedAt),
  }))
  downloadJSON(list, `engliai-wrongbook-${fmtDate(new Date())}.json`, true)
}

/**
 * 读取并解析 .ewb 导入文件
 * @param {File} file — 用户选择的文件
 * @returns {Promise<{success: boolean, count?: number, questions?: Array, message?: string}>}
 */
export function importFromEWB(file) {
  return new Promise((resolve) => {
    if (!file) {
      return resolve({ success: false, message: '未选择文件' })
    }

    const reader = new FileReader()
    reader.onload = () => {
      try {
        const data = JSON.parse(reader.result)
        const validation = validateImportData(data)
        if (!validation.valid) {
          return resolve({ success: false, message: validation.message })
        }
        resolve({ success: true, count: data.count, questions: data.questions })
      } catch (e) {
        resolve({ success: false, message: '文件解析失败：格式不正确或已损坏' })
      }
    }
    reader.onerror = () => {
      resolve({ success: false, message: '文件读取失败' })
    }
    reader.readAsText(file)
  })
}

/**
 * 校验导入数据结构
 */
function validateImportData(data) {
  if (!data || typeof data !== 'object') {
    return { valid: false, message: '文件格式不正确' }
  }
  if (data.type !== 'engliai-wrongbook') {
    return { valid: false, message: '不是有效的错题本迁移文件' }
  }
  if (!Array.isArray(data.questions) || data.questions.length === 0) {
    return { valid: false, message: '文件中没有错题数据' }
  }
  for (let i = 0; i < data.questions.length; i++) {
    const q = data.questions[i]
    if (!q.uuid || !q.question) {
      return { valid: false, message: `第 ${i + 1} 道题数据不完整（缺少 uuid 或题目内容）` }
    }
  }
  return { valid: true }
}

/** 提取导出所需字段（排除内部 id） */
function stripForExport(q) {
  return {
    uuid: q.uuid,
    passage: q.passage || '',
    question: q.question || '',
    options: (q.options || []).map((o) => ({ id: o.id, text: o.text })),
    userAnswer: q.userAnswer || '',
    correctAnswer: q.correctAnswer || '',
    explanation: q.explanation || '',
    addedAt: q.addedAt || 0,
  }
}

/** 触发浏览器下载 JSON 文件 */
function downloadJSON(data, filename, pretty = false) {
  const json = pretty
    ? JSON.stringify(data, null, 2)
    : JSON.stringify(data)
  const blob = new Blob([json], { type: pretty ? 'application/json' : 'application/octet-stream' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

/** 格式化日期为文件名友好的格式 */
function fmtDate(d) {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}${m}${day}`
}

/** 格式化时间戳为可读字符串 */
function formatTimestamp(ts) {
  if (!ts) return '未知时间'
  const d = new Date(ts)
  const y = d.getFullYear()
  const mo = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0')
  const mi = String(d.getMinutes()).padStart(2, '0')
  const s = String(d.getSeconds()).padStart(2, '0')
  return `${y}-${mo}-${day} ${h}:${mi}:${s}`
}
