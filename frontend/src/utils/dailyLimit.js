/**
 * 非 VIP 用户每日使用限制
 *
 * - AI 讲解/出题：每天 3 篇文章
 * - 测评中心：每天 1 次
 *
 * VIP 用户无限制。
 */

import { userKey } from '@/utils/storage'

const AI_ARTICLE_LIMIT = 3
const ASSESSMENT_LIMIT = 1

function today() {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function getStorageKey(type) {
  return userKey(`daily_limit_${type}`)
}

function loadData(type) {
  try {
    const raw = localStorage.getItem(getStorageKey(type))
    const data = raw ? JSON.parse(raw) : { date: '', used: 0 }
    if (data.date !== today()) return { date: today(), used: 0 }
    return data
  } catch {
    return { date: today(), used: 0 }
  }
}

function saveData(type, data) {
  localStorage.setItem(getStorageKey(type), JSON.stringify(data))
}

/** 检查是否还能使用 AI 文章功能。isVip 为 true 则始终返回 true */
export function canUseAIArticle(isVip) {
  if (isVip) return true
  return loadData('ai_article').used < AI_ARTICLE_LIMIT
}

/** 记录一次 AI 文章使用。返回剩余次数 */
export function recordAIArticle(isVip) {
  if (isVip) return Infinity
  const data = loadData('ai_article')
  data.used++
  saveData('ai_article', data)
  return Math.max(0, AI_ARTICLE_LIMIT - data.used)
}

/** 获取 AI 文章剩余次数 */
export function remainingAIArticles(isVip) {
  if (isVip) return Infinity
  return Math.max(0, AI_ARTICLE_LIMIT - loadData('ai_article').used)
}

/** 检查是否还能进行测评 */
export function canTakeAssessment(isVip) {
  if (isVip) return true
  return loadData('assessment').used < ASSESSMENT_LIMIT
}

/** 记录一次测评 */
export function recordAssessment(isVip) {
  if (isVip) return
  const data = loadData('assessment')
  data.used++
  saveData('assessment', data)
}
