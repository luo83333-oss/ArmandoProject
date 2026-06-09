import { request } from './request'

export function listFavorites() {
  return request('/api/favorites')
}

export function getFavoriteStatus(productId) {
  return request(`/api/favorites/products/${productId}/status`)
}

export function addFavorite(productId) {
  return request(`/api/favorites/products/${productId}`, { method: 'POST' })
}

export function removeFavorite(productId) {
  return request(`/api/favorites/products/${productId}`, { method: 'DELETE' })
}
