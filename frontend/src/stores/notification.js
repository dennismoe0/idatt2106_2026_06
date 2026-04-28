import { defineStore } from 'pinia'
import { ref } from 'vue'
import { notificationService } from '@/services/notificationService'

export const useNotificationStore = defineStore('notification', () => {
  const notifications = ref([])
  const unreadCount = ref(0)
  const loading = ref(false)
  const error = ref(null)

  async function fetchNotifications() {
    loading.value = true
    error.value = null
    try {
      const { data } = await notificationService.getNotifications()
      notifications.value = data
      return data
    } catch (err) {
      error.value = 'Kunne ikke laste varsler.'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function fetchUnreadCount() {
    error.value = null
    try {
      const { data } = await notificationService.getUnreadCount()
      unreadCount.value = data.count ?? 0
      return unreadCount.value
    } catch (err) {
      error.value = 'Kunne ikke laste antall varsler.'
      throw err
    }
  }

  async function markAsRead(id) {
    const notification = notifications.value.find(item => item.id === id)
    const wasUnread = notification && !notification.isRead

    await notificationService.markAsRead(id)

    if (notification) notification.isRead = true
    if (wasUnread) unreadCount.value = Math.max(0, unreadCount.value - 1)
  }

  async function markAllAsRead() {
    await notificationService.markAllAsRead()
    notifications.value = notifications.value.map(notification => ({
      ...notification,
      isRead: true
    }))
    unreadCount.value = 0
  }

  return {
    notifications,
    unreadCount,
    loading,
    error,
    fetchNotifications,
    fetchUnreadCount,
    markAsRead,
    markAllAsRead
  }
})
