<template>
  <div class="page page--tabbar">
    <van-nav-bar title="我的" />
    <div v-if="user" class="header">
      <van-badge :content="messageBadge" :show-zero="false" max="99" class="avatar-badge">
        <van-image round width="56" height="56" :src="avatarUrl" />
      </van-badge>
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
      <van-cell title="我的消息" is-link @click="goMessages">
        <template #icon>
          <van-badge :content="messageBadge" :show-zero="false" max="99" class="msg-icon-badge">
            <van-icon name="chat-o" class="cell-icon" />
          </van-badge>
        </template>
      </van-cell>
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
import { ref, computed, onMounted, onActivated } from 'vue'
import { useRouter } from 'vue-router'
import { showFailToast } from 'vant'
import { getMe } from '../api/auth'
import { useUnreadMessages } from '../composables/useUnreadMessages'
import { clearToken, getToken } from '../api/request'

const router = useRouter()
const user = ref(null)
const avatarUrl = 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'
const { unreadCount, refresh: refreshUnread, badgeText } = useUnreadMessages()

const messageBadge = computed(() => badgeText())

function isAuthError(message) {
  return message?.includes('登录') || message?.includes('401')
}

async function load() {
  if (!getToken()) {
    user.value = null
    unreadCount.value = 0
    return
  }
  try {
    user.value = await getMe()
  } catch (e) {
    if (isAuthError(e.message)) {
      clearToken()
      user.value = null
      unreadCount.value = 0
      showFailToast(e.message)
      return
    }
    showFailToast(e.message)
    return
  }
  await refreshUnread()
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
onActivated(load)
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
.avatar-badge :deep(.van-badge--top-right) {
  transform: translate(20%, -20%);
}
.msg-icon-badge {
  margin-right: 4px;
  display: inline-flex;
  align-items: center;
}
.cell-icon {
  font-size: 18px;
  color: #323233;
}
</style>
