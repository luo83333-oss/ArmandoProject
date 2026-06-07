<template>
  <div class="main-layout">
    <router-view />
    <van-tabbar v-if="showTabbar" route safe-area-inset-bottom>
      <van-tabbar-item replace to="/" icon="home-o">首页</van-tabbar-item>
      <van-tabbar-item replace to="/products" icon="apps-o">分类</van-tabbar-item>
      <van-tabbar-item replace to="/cart" icon="shopping-cart-o" :badge="cartBadge || ''">购物车</van-tabbar-item>
      <van-tabbar-item replace to="/profile" icon="user-o">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { listCart } from '../api/cart'
import { getToken } from '../api/request'

const route = useRoute()
const cartCount = ref(0)

const showTabbar = computed(() => route.meta.showTabbar === true)
const cartBadge = computed(() => (cartCount.value > 0 ? String(cartCount.value) : ''))

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

watch(() => route.fullPath, refreshCartBadge, { immediate: true })
</script>
