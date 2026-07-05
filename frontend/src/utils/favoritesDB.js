/**
 * IndexedDB 收藏夹存储工具
 *
 * 数据库: AAEL_FavoritesDB
 * └─ favorites (key: articleId)
 *      { articleId, title, difficulty, source, readTime, wordCount, addedAt }
 *
 * 用于保存用户在阅读器中通过书签按钮收藏的文章。
 * 个人中心的收藏夹抽屉从此处读取数据。
 */

import { userDBName } from '@/utils/storage'

const DB_VERSION = 1
const MAX_FAVORITES = 100

function openDB() {
  return new Promise((resolve, reject) => {
    const request = indexedDB.open(userDBName('AAEL_FavoritesDB'), DB_VERSION)

    request.onupgradeneeded = (event) => {
      const db = event.target.result
      if (!db.objectStoreNames.contains('favorites')) {
        db.createObjectStore('favorites', { keyPath: 'articleId' })
      }
    }

    request.onsuccess = (event) => resolve(event.target.result)
    request.onerror = (event) => {
      console.error('[favoritesDB] 打开失败:', event.target.error)
      reject(event.target.error)
    }
  })
}

/**
 * 添加文章到收藏夹
 * @param {{ articleId: number|string, title: string, difficulty?: string, source?: string, readTime?: string, wordCount?: string }} articleData
 * @returns {Promise<boolean>} true=新增, false=已存在
 */
export async function addToFavorites(articleData) {
  if (!articleData || !articleData.articleId) return false

  const articleId = Number(articleData.articleId)
  const db = await openDB()

  // 检查是否已存在
  const exists = await new Promise((resolve, reject) => {
    const tx = db.transaction('favorites', 'readonly')
    const store = tx.objectStore('favorites')
    const getReq = store.get(articleId)
    getReq.onsuccess = () => resolve(!!getReq.result)
    getReq.onerror = () => reject(getReq.error)
  })

  if (exists) {
    db.close()
    return false
  }

  // 写入
  await new Promise((resolve, reject) => {
    const tx = db.transaction('favorites', 'readwrite')
    const store = tx.objectStore('favorites')
    store.put({
      articleId,
      title: articleData.title || 'Untitled',
      difficulty: articleData.difficulty || '',
      source: articleData.source || '',
      readTime: articleData.readTime || '',
      wordCount: articleData.wordCount || '',
      addedAt: Date.now(),
    })
    tx.oncomplete = () => resolve()
    tx.onerror = () => reject(tx.error)
  })

  // 超过上限时删除最旧的
  await trimFavorites(db)

  db.close()
  return true
}

/**
 * 从收藏夹移除文章
 * @param {number|string} articleId
 */
export async function removeFromFavorites(articleId) {
  if (!articleId) return
  const db = await openDB()

  await new Promise((resolve, reject) => {
    const tx = db.transaction('favorites', 'readwrite')
    const store = tx.objectStore('favorites')
    store.delete(Number(articleId))
    tx.oncomplete = () => resolve()
    tx.onerror = () => reject(tx.error)
  })

  db.close()
}

/**
 * 判断文章是否已在收藏夹中
 * @param {number|string} articleId
 * @returns {Promise<boolean>}
 */
export async function isInFavorites(articleId) {
  if (!articleId) return false
  const db = await openDB()

  return new Promise((resolve, reject) => {
    const tx = db.transaction('favorites', 'readonly')
    const store = tx.objectStore('favorites')
    const getReq = store.get(Number(articleId))
    getReq.onsuccess = () => resolve(!!getReq.result)
    getReq.onerror = () => reject(getReq.error)
    tx.oncomplete = () => db.close()
  })
}

/**
 * 获取收藏夹全部文章（按收藏时间降序）
 * @returns {Promise<Array<{articleId, title, difficulty, source, readTime, wordCount, addedAt}>>}
 */
export async function getAllFavorites() {
  const db = await openDB()

  return new Promise((resolve, reject) => {
    const tx = db.transaction('favorites', 'readonly')
    const store = tx.objectStore('favorites')
    const getAll = store.getAll()

    getAll.onsuccess = () => {
      const records = getAll.result || []
      records.sort((a, b) => b.addedAt - a.addedAt)
      resolve(records)
    }
    getAll.onerror = () => reject(getAll.error)
    tx.oncomplete = () => db.close()
  })
}

/**
 * 获取收藏夹文章数量
 * @returns {Promise<number>}
 */
export async function countFavorites() {
  const db = await openDB()
  return new Promise((resolve, reject) => {
    const tx = db.transaction('favorites', 'readonly')
    const store = tx.objectStore('favorites')
    const req = store.count()
    req.onsuccess = () => resolve(req.result)
    req.onerror = () => reject(req.error)
    tx.oncomplete = () => db.close()
  })
}

/**
 * 删除多余的旧记录，仅保留最近 MAX_FAVORITES 条
 */
function trimFavorites(db) {
  return new Promise((resolve, reject) => {
    const tx = db.transaction('favorites', 'readwrite')
    const store = tx.objectStore('favorites')

    const getAll = store.getAll()
    getAll.onsuccess = () => {
      const records = getAll.result || []
      if (records.length > MAX_FAVORITES) {
        records.sort((a, b) => b.addedAt - a.addedAt)
        const toDelete = records.slice(MAX_FAVORITES)
        toDelete.forEach((r) => store.delete(r.articleId))
      }
    }
    getAll.onerror = () => reject(getAll.error)

    tx.oncomplete = () => resolve()
    tx.onerror = () => reject(tx.error)
  })
}

/**
 * 将时间戳转为相对时间描述
 * @param {number} timestamp
 * @returns {string}
 */
export function relativeTime(timestamp) {
  const now = Date.now()
  const diff = now - timestamp
  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)

  if (seconds < 60) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 30) return `${days}天前`
  const d = new Date(timestamp)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}