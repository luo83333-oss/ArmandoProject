const TOKEN_KEY = 'user_token'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
}

export async function request(url, options = {}) {
  const headers = { 'Content-Type': 'application/json', ...options.headers }
  const token = getToken()
  if (token) headers.Authorization = `Bearer ${token}`

  let res
  try {
    res = await fetch(url, { ...options, headers })
  } catch {
    throw new Error('网络异常，请确认后端已启动（8082）')
  }

  let json
  try {
    json = await res.json()
  } catch {
    throw new Error(`接口响应异常 (HTTP ${res.status})`)
  }

  if (json.code !== 0) {
    const msg = json.message || json.error
    if (msg) throw new Error(msg)
    if (json.code === 401 || res.status === 401) {
      clearToken()
      throw new Error('未登录或登录已过期，请重新登录')
    }
    throw new Error(`请求失败 (HTTP ${res.status}${json.path ? ` ${json.path}` : ''})`)
  }
  return json.data
}
