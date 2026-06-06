import { request } from './request'

export function listProducts(params = {}) {
  const qs = new URLSearchParams()
  if (params.page) qs.set('page', params.page)
  if (params.size) qs.set('size', params.size)
  if (params.keyword) qs.set('keyword', params.keyword)
  if (params.categoryId) qs.set('categoryId', params.categoryId)
  return request(`/api/products?${qs}`)
}

export function getProduct(id) {
  return request(`/api/products/${id}`)
}

export function listCategories() {
  return request('/api/categories')
}
