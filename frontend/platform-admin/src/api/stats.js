import { getToken, request } from './request'

export function getStatsOverview() {
  return request('/api/platform/stats/overview')
}

export function getStatsTrend(days = 30, granularity = 'day') {
  return request(`/api/platform/stats/trend?days=${days}&granularity=${granularity}`)
}

export function getShopRanking(days = 30, limit = 20) {
  return request(`/api/platform/stats/shops?days=${days}&limit=${limit}`)
}

export async function exportShopRanking(days = 30) {
  const token = getToken()
  const res = await fetch(`/api/platform/stats/shops/export?days=${days}`, {
    headers: token ? { Authorization: `Bearer ${token}` } : {}
  })
  if (!res.ok) {
    const text = await res.text()
    throw new Error(text || '导出失败')
  }
  const blob = await res.blob()
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `shop-stats-${days}d.csv`
  a.click()
  URL.revokeObjectURL(url)
}
