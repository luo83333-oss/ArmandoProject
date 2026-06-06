import { request } from './request'

export function listMerchants(auditStatus) {
  const qs = auditStatus !== undefined && auditStatus !== '' ? `?auditStatus=${auditStatus}` : ''
  return request(`/api/platform/merchants${qs}`)
}

export function auditMerchant(id, data) {
  return request(`/api/platform/merchants/${id}/audit`, {
    method: 'POST',
    body: JSON.stringify(data)
  })
}
