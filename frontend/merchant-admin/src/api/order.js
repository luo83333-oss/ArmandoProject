import { request } from './request'

export function listOrders(status) {
  const qs = status !== undefined && status !== '' ? `?status=${status}` : ''
  return request(`/api/merchant/orders${qs}`)
}

export function getOrder(id) {
  return request(`/api/merchant/orders/${id}`)
}

export function shipOrder(id, logisticsNo) {
  return request(`/api/merchant/orders/${id}/ship`, {
    method: 'POST',
    body: JSON.stringify({ logisticsNo })
  })
}
