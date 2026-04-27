import api from './api'

export const avatarService = {
  getMyAvatar: () =>
    api.get('/api/avatars/me'),

  updateAvatar: (data) =>
    api.put('/api/avatars/me', data),

  getMyOptions: () =>
    api.get('/api/avatars/options'),

  getShop: () =>
    api.get('/api/avatars/shop'),

  purchaseItem: (optionType, optionValue) =>
    api.post('/api/avatars/shop/purchase', { optionType, optionValue }),
}
