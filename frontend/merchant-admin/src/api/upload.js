import { getToken } from './request'

export async function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  const headers = {}
  const token = getToken()
  if (token) headers.Authorization = `Bearer ${token}`

  const res = await fetch('/api/merchant/upload', {
    method: 'POST',
    headers,
    body: formData
  })

  const text = await res.text()
  let json
  try {
    json = JSON.parse(text)
  } catch {
    throw new Error(`上传失败（HTTP ${res.status}）`)
  }

  if (json.code !== 0) {
    throw new Error(json.message || `上传失败（code ${json.code}）`)
  }
  if (!json.data?.url) {
    throw new Error('上传失败：未返回图片地址')
  }
  return json.data.url
}
