import axios from 'axios'

const request = axios.create({
    baseURL: import.meta.env.DEV ? '/api' : '../api',
    timeout: 30000,
    withCredentials: true
})

request.interceptors.response.use(
    response => response.data,
    error => {
        console.error('Request error:', error)
        if (error.response?.status === 401) {
            // 解析响应数据（兼容 axios 自动解析和原始字符串两种情形）
            let data = error.response.data
            if (typeof data === 'string') {
                try { data = JSON.parse(data) } catch (_) { data = {} }
            }
            const msg = data?.message || ''

            // 只有被管理员强制下线才自动跳转登录页，避免影响正常用户
            if (msg.includes('强制下线') && !window.location.hash.includes('#/login')) {
                localStorage.removeItem('token')
                localStorage.removeItem('user')
                sessionStorage.setItem('kickReason', msg)
                window.location.hash = '#/login'
            }
        }
        return Promise.reject(error)
    }
)

export default request
