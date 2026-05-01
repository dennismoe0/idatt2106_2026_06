import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAvatarStore } from '@/stores/avatar'

vi.mock('@/services/avatarService', () => ({
  avatarService: {
    getMyAvatar: vi.fn(),
    updateAvatar: vi.fn(),
    getMyOptions: vi.fn(),
  }
}))

import { avatarService } from '@/services/avatarService'

describe('avatar store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('initial state is null', () => {
    const store = useAvatarStore()
    expect(store.avatar).toBeNull()
    expect(store.available).toEqual({})
  })

  it('fetchAvatar sets avatar', async () => {
    avatarService.getMyAvatar.mockResolvedValue({
      data: { hairStyle: 'short', outfitStyle: 'casual', skinTone: 'medium', eyeColor: 'brown' }
    })
    const store = useAvatarStore()
    await store.fetchAvatar()
    expect(store.avatar.hairStyle).toBe('short')
  })

  it('updateAvatar updates state and returns new avatar', async () => {
    avatarService.updateAvatar.mockResolvedValue({
      data: { hairStyle: 'long', outfitStyle: 'formal', skinTone: 'light', eyeColor: 'blue' }
    })
    const store = useAvatarStore()
    const result = await store.updateAvatar({ hairStyle: 'long' })
    expect(store.avatar.hairStyle).toBe('long')
    expect(result.eyeColor).toBe('blue')
  })

  it('fetchOptions sets options map', async () => {
    avatarService.getMyOptions.mockResolvedValue({
      data: {
        available: { hairStyle: ['short', 'long', 'curly'], eyeColor: ['brown', 'blue', 'green'] },
        medalLocked: [],
        colorPickerUnlocked: false
      }
    })
    const store = useAvatarStore()
    await store.fetchOptions()
    expect(store.available.hairStyle).toContain('short')
  })

  it('fetchAvatar propagates errors', async () => {
    avatarService.getMyAvatar.mockRejectedValue(new Error('Not found'))
    const store = useAvatarStore()
    await expect(store.fetchAvatar()).rejects.toThrow('Not found')
  })
})
