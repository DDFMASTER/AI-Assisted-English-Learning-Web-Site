import axios from 'axios'

const request = axios.create({
    baseURL: import.meta.env.DEV ? '/api' : '../api',
    timeout: 30000,
    withCredentials: true
})

request.interceptors.response.use(
    response => response.data,
    error => {
        if (error.response?.status === 401) {
            let data = error.response.data
            if (typeof data === 'string') {
                try { data = JSON.parse(data) } catch (_) { data = {} }
            }
            const msg = data?.message || ''

            if (!window.location.hash.includes('#/login')) {
                // 清除本地登录状态
                localStorage.removeItem('token')
                localStorage.removeItem('user')
                // 存储原因供登录页展示
                const reason = msg.includes('强制下线') ? msg : '会话已过期，请重新登录'
                sessionStorage.setItem('kickReason', reason)
                window.location.hash = '#/login'
            }
        }
        return Promise.reject(error)
    }
)

export default request
