import { request } from './request'

export function listShopRank(limit = 10) {
  return request(`/api/shops/rank?limit=${limit}`)
}
