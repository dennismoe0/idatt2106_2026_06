import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/services/api'
import router from '@/router'
import { useClassroomStore } from '@/stores/classroom'
import { useAvatarStore } from '@/stores/avatar'

const TOKEN_KEY = 'nettdetektivene_token'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(null)
  const role = ref(null)
  const userId = ref(null)
  const email = ref(null)

  const isAuthenticated = computed(() => !!token.value)
  const isTeacher = computed(() => role.value === 'TEACHER')
  const isStudent = computed(() => role.value === 'STUDENT')

  function setAuth(data) {
    token.value = data.token
    role.value = data.role
    userId.value = data.userId
    email.value = data.email
    localStorage.setItem(TOKEN_KEY, data.token)
    if (data.role === 'STUDENT') {
      useAvatarStore().fetchAvatar().catch(err =>
        console.warn('[auth] Avatar prefetch failed:', err)
      )
    }
  }

  function clearAuthState() {
    console.log('[auth] Clearing auth state for user:', email.value)
    token.value = null
    role.value = null
    userId.value = null
    email.value = null
    localStorage.removeItem(TOKEN_KEY)
    useClassroomStore().reset()
    console.log('[auth] Auth state cleared')
  }

  function logout() {
    const destination = role.value === 'TEACHER'
      ? { name: 'TeacherLogin' }
      : { name: 'StudentLogin' }
    clearAuthState()
    router.push(destination)
  }

  function decodeJwtPayload(t) {
    try {
      return JSON.parse(atob(t.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')))
    } catch {
      return null
    }
  }

  function rehydrate() {
    const stored = localStorage.getItem(TOKEN_KEY)
    if (!stored) return
    const payload = decodeJwtPayload(stored)
    if (!payload) {
      console.warn('[auth] Stored token could not be decoded — clearing')
      localStorage.removeItem(TOKEN_KEY)
      return
    }
    token.value = stored
    role.value = payload.role ?? null
    userId.value = payload.sub ? parseInt(payload.sub, 10) : null
    email.value = payload.email ?? null
    console.log('[auth] Rehydrated session — userId:', userId.value, 'role:', role.value)
    if (role.value === 'STUDENT') {
      useAvatarStore().fetchAvatar().catch(err =>
        console.warn('[auth] Avatar prefetch on rehydrate failed:', err)
      )
    }
  }

  async function login(credentials) {
    console.log('[auth] Login attempt:', credentials.email)
    const { data } = await api.post('/api/auth/login', credentials)
    setAuth(data)
    console.log('[auth] Login successful — role:', data.role, 'userId:', data.userId)
  }

  async function register(credentials) {
    console.log('[auth] Register attempt:', credentials.email)
    const { data } = await api.post('/api/auth/register', credentials)
    setAuth(data)
    console.log('[auth] Registration successful — userId:', data.userId)
  }

  async function studentLogin(username) {
    console.log('[auth] Student login:', username)
    // Reset classroom state so a new student never inherits the previous session's classroomId
    useClassroomStore().reset()
    const { data } = await api.post('/api/auth/student-login', { username })
    setAuth(data)
    console.log('[auth] Student login successful — userId:', data.userId)
  }

  return {
    token,
    role,
    userId,
    email,
    isAuthenticated,
    isTeacher,
    isStudent,
    login,
    register,
    studentLogin,
    logout,
    clearAuthState,
    rehydrate,
  }
})
