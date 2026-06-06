import { request } from './request'

export function listCart() {
  return request('/api/cart')
}

export function addToCart(skuId, quantity = 1) {
  return request('/api/cart', {
    method: 'POST',
    body: JSON.stringify({ skuId, quantity })
  })
}

export function updateCartItem(id, data) {
  return request(`/api/cart/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data)
  })
}

export function removeCartItem(id) {
  return request(`/api/cart/${id}`, { method: 'DELETE' })
}
