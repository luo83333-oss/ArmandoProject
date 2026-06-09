<template>
  <div class="main-layout">
    <router-view :key="route.fullPath" />
    <van-tabbar v-if="showTabbar" v-model="activeTab" safe-area-inset-bottom @change="onTabChange">
      <van-tabbar-item icon="home-o">首页</van-tabbar-item>
      <van-tabbar-item icon="apps-o">分类</van-tabbar-item>
      <van-tabbar-item icon="shopping-cart-o" :badge="cartBadge || ''">购物车</van-tabbar-item>
      <van-tabbar-item icon="user-o" :badge="messageBadge || ''">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { listCart } from '../api/cart'
import { useUnreadMessages } from '../composables/useUnreadMessages'
import { getToken } from '../api/request'

const route = useRoute()
const router = useRouter()
const cartCount = ref(0)
const activeTab = ref(0)
const { refresh: refreshUnread, badgeText } = useUnreadMessages()

const TAB_ROUTES = ['home', 'products', 'cart', 'profile']

const showTabbar = computed(() => route.meta.showTabbar === true)
const cartBadge = computed(() => (cartCount.value > 0 ? String(cartCount.value) : ''))
const messageBadge = computed(() => badgeText())

watch(
  () => route.name,
  (name) => {
    const idx = TAB_ROUTES.indexOf(name)
    if (idx >= 0) activeTab.value = idx
  },
  { immediate: true }
)

function onTabChange(index) {
  const name = TAB_ROUTES[index]
  if (!name) return
  if (route.name === name) {
    window.scrollTo({ top: 0, behavior: 'smooth' })
    return
  }
  router.replace({ name })
}

async function refreshCartBadge() {
  if (!getToken()) {
    cartCount.value = 0
    return
  }
  try {
    const items = await listCart()
    cartCount.value = items.reduce((sum, item) => sum + item.quantity, 0)
  } catch {
    cartCount.value = 0
  }
}

async function refreshBadges() {
  await Promise.all([refreshCartBadge(), refreshUnread()])
}

watch(() => route.fullPath, refreshBadges, { immediate: true })
</script>
