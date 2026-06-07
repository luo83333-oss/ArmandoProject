<template>
  <div class="page">
    <van-nav-bar title="多商户商城" />
    <van-cell-group inset>
      <van-cell title="API 状态" :value="healthText" />
      <van-cell v-if="user" title="当前用户" :value="`${user.nickname} (${user.phone})`" />
    </van-cell-group>
    <div class="actions">
      <van-button v-if="!user" round block type="primary" @click="$router.push('/login')">登录 / 注册</van-button>
      <van-button v-else round block type="danger" plain @click="onLogout">退出登录</van-button>
      <van-button round block type="primary" plain class="mt" @click="$router.push('/products')">逛商品</van-button>
      <van-button round block plain class="mt" @click="$router.push('/shops/rank')">热门店铺榜</van-button>
      <van-button v-if="user" round block plain class="mt" @click="$router.push('/cart')">购物车</van-button>
      <van-button v-if="user" round block plain class="mt" @click="$router.push('/orders')">我的订单</van-button>
      <van-button round block plain class="mt" :loading="checking" @click="checkHealth">检测 API</van-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { showFailToast } from 'vant'
import { getMe } from '../api/auth'
import { clearToken, getToken } from '../api/request'

const healthText = ref('未检测')
const checking = ref(false)
const user = ref(null)

async function checkHealth() {
  checking.value = true
  try {
    const res = await fetch('/api/health')
    const json = await res.json()
    healthText.value = json.code === 0 ? '正常' : json.message
  } catch {
    healthText.value = '连接失败'
    showFailToast('无法连接后端')
  } finally {
    checking.value = false
  }
}

async function loadUser() {
  if (!getToken()) return
  try {
    user.value = await getMe()
  } catch {
    clearToken()
  }
}

function onLogout() {
  clearToken()
  user.value = null
}

onMounted(() => {
  loadUser()
})
</script>

<style scoped>
.page { min-height: 100vh; background: #f7f8fa; }
.actions { margin: 24px 16px; }
.mt { margin-top: 12px; }
</style>
