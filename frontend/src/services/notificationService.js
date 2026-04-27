import api from './api'

export const notificationService = {
  getNotifications: () =>
    api.get('/api/notifications'),

  getUnreadCount: () =>
    api.get('/api/notifications/count'),

  markAsRead: (id) =>
    api.put(`/api/notifications/${id}/read`),

  markAllAsRead: () =>
    api.put('/api/notifications/read-all'),
}
