import { request } from './request'

export function login(data) {
  return request('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify(data)
  })
}
