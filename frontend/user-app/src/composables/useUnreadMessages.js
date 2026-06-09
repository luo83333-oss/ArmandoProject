import { ref } from 'vue'
import { getUnreadCount } from '../api/message'
import { getToken } from '../api/request'

const unreadCount = ref(0)

export function useUnreadMessages() {
  async function refresh() {
    if (!getToken()) {
      unreadCount.value = 0
      return
    }
    try {
      const result = await getUnreadCount()
      unreadCount.value = Number(result?.count || 0)
    } catch {
      unreadCount.value = 0
    }
  }

  function badgeText() {
    if (!unreadCount.value) return ''
    return unreadCount.value > 99 ? '99+' : String(unreadCount.value)
  }

  return { unreadCount, refresh, badgeText }
}
