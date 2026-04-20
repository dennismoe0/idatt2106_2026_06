import api from './api'

export const avatarService = {
  getMyAvatar: () =>
    api.get('/api/avatars/me'),

  updateAvatar: (data) =>
    api.put('/api/avatars/me', data),

  getOptions: () =>
    api.get('/api/avatars/options'),
}
