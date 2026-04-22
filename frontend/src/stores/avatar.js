import { defineStore } from 'pinia'
import { ref } from 'vue'
import { avatarService } from '@/services/avatarService'

export const useAvatarStore = defineStore('avatar', () => {
  const avatar = ref(null)
  const available = ref({})          // Map<fieldName, string[]> — selectable right now
  const medalLocked = ref([])        // Array<{optionType, optionValue, stopId, stopName}>
  const colorPickerUnlocked = ref(false)
  const shopItems = ref([])          // Array<ShopItemDto>

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
    console.log('[avatar] Fetching my options')
    try {
      const { data } = await avatarService.getMyOptions()
      available.value = data.available ?? {}
      medalLocked.value = data.medalLocked ?? []
      colorPickerUnlocked.value = data.colorPickerUnlocked ?? false
      console.log('[avatar] Options fetched, colorPickerUnlocked:', data.colorPickerUnlocked)
      return data
    } catch (err) {
      console.error('[avatar] Failed to fetch options:', err)
      throw err
    }
  }

  async function fetchShop() {
    console.log('[avatar] Fetching shop')
    try {
      const { data } = await avatarService.getShop()
      shopItems.value = data
      console.log('[avatar] Shop items fetched:', data.length)
      return data
    } catch (err) {
      console.error('[avatar] Failed to fetch shop:', err)
      throw err
    }
  }

  async function purchaseItem(optionType, optionValue) {
    console.log('[avatar] Purchasing', optionType, optionValue)
    try {
      await avatarService.purchaseItem(optionType, optionValue)
      console.log('[avatar] Purchase success, refreshing options and shop')
      await Promise.all([fetchOptions(), fetchShop()])
    } catch (err) {
      console.error('[avatar] Purchase failed:', err)
      throw err
    }
  }

  function getMedalLockedForField(fieldName) {
    return medalLocked.value
      .filter(item => item.optionType === fieldName)
      .map(item => ({ value: item.optionValue, stopName: item.stopName }))
  }

  return {
    avatar, available, medalLocked, colorPickerUnlocked, shopItems,
    fetchAvatar, updateAvatar, fetchOptions, fetchShop, purchaseItem,
    getMedalLockedForField,
  }
})
