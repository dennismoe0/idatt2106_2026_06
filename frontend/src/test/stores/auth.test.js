import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '@/stores/auth'

vi.mock('@/services/api', () => ({
  default: {
    post: vi.fn()
  }
}))

import api from '@/services/api'

describe('auth store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    vi.clearAllMocks()
  })

  it('initial state is unauthenticated', () => {
    const store = useAuthStore()
    expect(store.token).toBeNull()
    expect(store.isAuthenticated).toBe(false)
  })

  it('login sets token, role, user and persists to localStorage', async () => {
    api.post.mockResolvedValue({
      data: { token: 'test-jwt', role: 'TEACHER', userId: 1, email: 'teacher@test.no' }
    })
    const store = useAuthStore()
    await store.login({ email: 'teacher@test.no', password: 'password' })

    expect(store.token).toBe('test-jwt')
    expect(store.role).toBe('TEACHER')
    expect(store.isAuthenticated).toBe(true)
    expect(store.isTeacher).toBe(true)
    expect(store.isStudent).toBe(false)
    expect(localStorage.getItem('nettdetektivene_token')).toBe('test-jwt')
  })

  it('logout clears state and localStorage', async () => {
    const store = useAuthStore()
    store.token = 'some-token'
    store.role = 'TEACHER'
    localStorage.setItem('nettdetektivene_token', 'some-token')

    store.logout()

    expect(store.token).toBeNull()
    expect(store.role).toBeNull()
    expect(store.isAuthenticated).toBe(false)
    expect(localStorage.getItem('nettdetektivene_token')).toBeNull()
  })

  it('rehydrate restores token and role from localStorage', () => {
    // Craft a fake JWT with payload: { sub: '42', role: 'TEACHER', email: 'x@test.no' }
    const payload = btoa(JSON.stringify({ sub: '42', role: 'TEACHER', email: 'x@test.no' }))
    const fakeJwt = `header.${payload}.sig`
    localStorage.setItem('nettdetektivene_token', fakeJwt)
    const store = useAuthStore()
    store.rehydrate()

    expect(store.token).toBe(fakeJwt)
    expect(store.role).toBe('TEACHER')
    expect(store.userId).toBe(42)
    expect(store.email).toBe('x@test.no')
    expect(store.isTeacher).toBe(true)
  })

  it('login failure propagates error', async () => {
    api.post.mockRejectedValue({ response: { data: { error: 'Invalid credentials' } } })
    const store = useAuthStore()

    await expect(store.login({ email: 'x@x.no', password: 'wrong' })).rejects.toBeDefined()
    expect(store.token).toBeNull()
  })
})
