import { request } from './request'

export function listOrders(status, shopId) {
  const params = new URLSearchParams()
  if (status !== undefined && status !== '') params.set('status', status)
  if (shopId) params.set('shopId', shopId)
  const qs = params.toString() ? `?${params}` : ''
  return request(`/api/platform/orders${qs}`)
}

export function getOrder(id) {
  return request(`/api/platform/orders/${id}`)
}
