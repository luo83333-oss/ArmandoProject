import { request } from './request'

export function apply(data) {
  return request('/api/merchant/apply', {
    method: 'POST',
    body: JSON.stringify(data)
  })
}

export function getMine() {
  return request('/api/merchant/mine')
}
