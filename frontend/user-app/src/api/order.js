import { request } from './request'

export function checkout(address) {
  return request('/api/orders/checkout', {
    method: 'POST',
    body: JSON.stringify({ address })
  })
}

export function listOrders() {
  return request('/api/orders')
}

export function getOrder(id) {
  return request(`/api/orders/${id}`)
}

export function mockPay(id) {
  return request(`/api/orders/${id}/pay`, { method: 'POST' })
}

export function confirmReceive(id) {
  return request(`/api/orders/${id}/confirm`, { method: 'POST' })
}

export function cancelOrder(id) {
  return request(`/api/orders/${id}/cancel`, { method: 'POST' })
}
