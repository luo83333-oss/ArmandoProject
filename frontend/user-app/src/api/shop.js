import { request } from './request'

export function listShopRank(limit = 10) {
  return request(`/api/shops/rank?limit=${limit}`)
}

export function listFollowing() {
  return request('/api/shops/following')
}

export function getFollowStatus(shopId) {
  return request(`/api/shops/${shopId}/followed`)
}

export function followShop(shopId) {
  return request(`/api/shops/${shopId}/follow`, { method: 'POST' })
}

export function unfollowShop(shopId) {
  return request(`/api/shops/${shopId}/follow`, { method: 'DELETE' })
}
