import { request } from './request'

export function sendCode(phone) {
  return request('/api/auth/send-code', {
    method: 'POST',
    body: JSON.stringify({ phone })
  })
}

export function register(data) {
  return request('/api/auth/register', {
    method: 'POST',
    body: JSON.stringify(data)
  })
}

export function login(data) {
  return request('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify(data)
  })
}

export function wechatLogin(code) {
  return request('/api/auth/wechat', {
    method: 'POST',
    body: JSON.stringify({ code })
  })
}

export function getMe() {
  return request('/api/auth/me')
}
