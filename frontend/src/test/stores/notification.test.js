import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useNotificationStore } from '@/stores/notification'

vi.mock('@/services/notificationService', () => ({
  notificationService: {
    getNotifications: vi.fn(),
    getUnreadCount: vi.fn(),
    markAsRead: vi.fn(),
    markAllAsRead: vi.fn()
  }
}))

import { notificationService } from '@/services/notificationService'

describe('notification store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('fetchNotifications populates notifications', async () => {
    notificationService.getNotifications.mockResolvedValue({
      data: [{ id: 1, message: 'Ny foresporsel', isRead: false }]
    })

    const store = useNotificationStore()
    await store.fetchNotifications()

    expect(store.notifications).toHaveLength(1)
    expect(store.notifications[0].message).toBe('Ny foresporsel')
  })

  it('fetchUnreadCount stores unread count', async () => {
    notificationService.getUnreadCount.mockResolvedValue({ data: { unreadCount: 3 } })

    const store = useNotificationStore()
    await store.fetchUnreadCount()

    expect(store.unreadCount).toBe(3)
  })

  it('markAsRead marks a notification locally and decrements count once', async () => {
    notificationService.markAsRead.mockResolvedValue({})
    const store = useNotificationStore()
    store.notifications = [{ id: 1, isRead: false }, { id: 2, isRead: true }]
    store.unreadCount = 1

    await store.markAsRead(1)
    await store.markAsRead(1)

    expect(notificationService.markAsRead).toHaveBeenCalledTimes(2)
    expect(store.notifications[0].isRead).toBe(true)
    expect(store.unreadCount).toBe(0)
  })

  it('markAllAsRead marks all notifications locally and clears count', async () => {
    notificationService.markAllAsRead.mockResolvedValue({})
    const store = useNotificationStore()
    store.notifications = [{ id: 1, isRead: false }, { id: 2, isRead: false }]
    store.unreadCount = 2

    await store.markAllAsRead()

    expect(store.notifications.every(notification => notification.isRead)).toBe(true)
    expect(store.unreadCount).toBe(0)
  })
})
