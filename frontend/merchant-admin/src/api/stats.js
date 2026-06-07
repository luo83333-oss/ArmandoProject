import { request } from './request'

export function getStatsOverview() {
  return request('/api/merchant/stats/overview')
}

export function getStatsTrend(days = 7) {
  return request(`/api/merchant/stats/trend?days=${days}`)
}
