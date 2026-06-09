<template>
  <div class="page page--tabbar">
    <van-nav-bar title="我的" />
    <div v-if="user" class="header">
      <van-image round width="56" height="56" :src="avatarUrl" />
      <div class="info">
        <div class="name">{{ user.nickname }}</div>
        <div class="phone">{{ user.phone }}</div>
      </div>
    </div>
    <van-cell-group v-else inset class="mt">
      <van-cell title="未登录" label="登录后查看订单与购物车" />
      <van-button block type="primary" class="login-btn" @click="$router.push('/login')">登录 / 注册</van-button>
    </van-cell-group>

    <van-cell-group inset class="mt">
      <van-cell title="我的订单" is-link icon="orders-o" @click="goOrders" />
      <van-cell title="我的消息" is-link icon="chat-o" :badge="unreadBadge" @click="goMessages" />
      <van-cell title="我的收藏" is-link icon="star-o" @click="goFavorites" />
      <van-cell title="我的关注" is-link icon="like-o" @click="goFollowing" />
      <van-cell title="热门店铺榜" is-link icon="shop-o" @click="$router.push('/shops/rank')" />
      <van-cell title="购物车" is-link icon="shopping-cart-o" @click="goCart" />
    </van-cell-group>

    <van-cell-group v-if="user" inset class="mt">
      <van-cell title="退出登录" is-link @click="onLogout" />
    </van-cell-group>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showFailToast } from 'vant'
import { getMe } from '../api/auth'
import { getUnreadCount } from '../api/message'
import { clearToken, getToken } from '../api/request'

const router = useRouter()
const user = ref(null)
const unreadCount = ref(0)
const avatarUrl = 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'

const unreadBadge = computed(() => {
  if (!unreadCount.value) return ''
  return unreadCount.value > 99 ? '99+' : String(unreadCount.value)
})

async function load() {
  if (!getToken()) return
  try {
    user.value = await getMe()
    const result = await getUnreadCount()
    unreadCount.value = Number(result?.count || 0)
  } catch (e) {
    showFailToast(e.message)
    clearToken()
  }
}

function goOrders() {
  if (!getToken()) {
    router.push('/login')
    return
  }
  router.push('/orders')
}

function goCart() {
  if (!getToken()) {
    router.push('/login')
    return
  }
  router.push('/cart')
}

function goFavorites() {
  if (!getToken()) {
    router.push('/login')
    return
  }
  router.push('/favorites')
}

function goFollowing() {
  if (!getToken()) {
    router.push('/login')
    return
  }
  router.push('/following')
}

function goMessages() {
  if (!getToken()) {
    router.push('/login')
    return
  }
  router.push('/messages')
}

function onLogout() {
  clearToken()
  user.value = null
}

onMounted(load)
</script>

<style scoped>
.header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin: 16px;
  padding: 20px 16px;
  background: linear-gradient(135deg, #1989fa, #39b9f9);
  border-radius: 12px;
  color: #fff;
}
.name { font-size: 18px; font-weight: 600; }
.phone { font-size: 13px; opacity: 0.9; margin-top: 4px; }
.mt { margin-top: 12px; }
.login-btn { margin: 12px 16px 4px; }
</style>
