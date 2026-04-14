import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
  headers: { 'Content-Type': 'application/json' }
})

// Attach JWT to every request
api.interceptors.request.use(config => {
  const token = localStorage.getItem('nettdetektivene_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

let _unauthorizedHandler = () => { window.location.href = '/login' }

export function setUnauthorizedHandler(fn) {
  _unauthorizedHandler = fn
}

// 401 → clear auth and redirect to login
api.interceptors.response.use(
  response => {
    console.log('[api]', response.config.method?.toUpperCase(), response.config.url, '→', response.status)
    return response
  },
  error => {
    if (error.response?.status === 401) {
      console.warn('[api] 401 Unauthorized — clearing token and redirecting to login')
      localStorage.removeItem('nettdetektivene_token')
      _unauthorizedHandler()
    } else {
      console.error('[api] Request failed:', error.config?.method?.toUpperCase(), error.config?.url, '→', error.response?.status, error.message)
    }
    return Promise.reject(error)
  }
)

export default api
