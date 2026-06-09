import { request } from './request'

export function listMessages() {
  return request('/api/messages')
}

export function getUnreadCount() {
  return request('/api/messages/unread-count')
}

export function getMessage(id) {
  return request(`/api/messages/${id}`)
}

export function markMessageRead(id) {
  return request(`/api/messages/${id}/read`, { method: 'POST' })
}

export function markAllMessagesRead() {
  return request('/api/messages/read-all', { method: 'POST' })
}
