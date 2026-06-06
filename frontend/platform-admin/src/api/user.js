import { request } from './request'

export function listUsers() {
  return request('/api/platform/users')
}

export function updateUserStatus(id, status) {
  return request(`/api/platform/users/${id}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ status })
  })
}
