import { request } from './request'

export function getRankConfig() {
  return request('/api/platform/rank/config')
}

export function updateRankConfig(data) {
  return request('/api/platform/rank/config', {
    method: 'PUT',
    body: JSON.stringify(data)
  })
}

export function recalculateRank() {
  return request('/api/platform/rank/recalculate', { method: 'POST' })
}

export function listRank(limit = 20) {
  return request(`/api/platform/rank?limit=${limit}`)
}
