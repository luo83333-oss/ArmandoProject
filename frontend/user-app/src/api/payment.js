import { request } from './request'

export function listPaymentChannels() {
  return request('/api/payment/channels')
}

export function completeSandboxPay(data) {
  return request('/api/payment/sandbox/complete', {
    method: 'POST',
    body: JSON.stringify(data)
  })
}
