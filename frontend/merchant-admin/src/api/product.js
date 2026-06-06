import { request } from './request'

export function listProducts(params = {}) {
  const qs = new URLSearchParams()
  if (params.page) qs.set('page', params.page)
  if (params.size) qs.set('size', params.size)
  if (params.keyword) qs.set('keyword', params.keyword)
  return request(`/api/merchant/products?${qs}`)
}

export function getProduct(id) {
  return request(`/api/merchant/products/${id}`)
}

export function createProduct(data) {
  return request('/api/merchant/products', { method: 'POST', body: JSON.stringify(data) })
}

export function updateProduct(id, data) {
  return request(`/api/merchant/products/${id}`, { method: 'PUT', body: JSON.stringify(data) })
}

export function deleteProduct(id) {
  return request(`/api/merchant/products/${id}`, { method: 'DELETE' })
}

export function updateShelf(id, shelfStatus) {
  return request(`/api/merchant/products/${id}/shelf`, {
    method: 'PATCH',
    body: JSON.stringify({ shelfStatus })
  })
}

export function listCategories() {
  return request('/api/categories')
}
