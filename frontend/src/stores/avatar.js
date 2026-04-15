import { defineStore } from 'pinia'
import { ref } from 'vue'
import { avatarService } from '@/services/avatarService'

export const useAvatarStore = defineStore('avatar', () => {
  const avatar = ref(null)
  const options = ref({})

  async function fetchAvatar() {
    console.log('[avatar] Fetching my avatar')
    try {
      const { data } = await avatarService.getMyAvatar()
      avatar.value = data
      console.log('[avatar] Avatar fetched')
      return data
    } catch (err) {
      console.error('[avatar] Failed to fetch avatar:', err)
      throw err
    }
  }

  async function updateAvatar(payload) {
    console.log('[avatar] Updating avatar')
    try {
      const { data } = await avatarService.updateAvatar(payload)
      avatar.value = data
      console.log('[avatar] Avatar updated')
      return data
    } catch (err) {
      console.error('[avatar] Failed to update avatar:', err)
      throw err
    }
  }

  async function fetchOptions() {
    console.log('[avatar] Fetching avatar options')
    try {
      const { data } = await avatarService.getOptions()
      options.value = data
      console.log('[avatar] Avatar options fetched')
      return data
    } catch (err) {
      console.error('[avatar] Failed to fetch avatar options:', err)
      throw err
    }
  }

  return { avatar, options, fetchAvatar, updateAvatar, fetchOptions }
})
