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
