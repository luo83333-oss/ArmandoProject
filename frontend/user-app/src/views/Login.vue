<template>
  <div class="page">
    <van-nav-bar title="登录" />
    <van-form @submit="onSubmit">
      <van-cell-group inset>
        <van-field v-model="form.phone" label="手机号" placeholder="请输入手机号" :rules="[{ required: true }]" />
        <van-field v-model="form.password" type="password" label="密码" placeholder="请输入密码" :rules="[{ required: true }]" />
      </van-cell-group>
      <div class="actions">
        <van-button round block type="primary" native-type="submit" :loading="loading">登录</van-button>
        <van-button round block plain type="primary" class="mt" @click="onWechat">微信登录（Mock）</van-button>
        <van-button round block plain class="mt" @click="$router.push('/register')">去注册</van-button>
      </div>
    </van-form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showSuccessToast, showFailToast } from 'vant'
import { login, wechatLogin } from '../api/auth'
import { setToken } from '../api/request'

const router = useRouter()
const loading = ref(false)
const form = ref({ phone: '', password: '' })

async function onSubmit() {
  loading.value = true
  try {
    const data = await login(form.value)
    setToken(data.token)
    showSuccessToast('登录成功')
    router.replace('/')
  } catch (e) {
    showFailToast(e.message)
  } finally {
    loading.value = false
  }
}

async function onWechat() {
  loading.value = true
  try {
    const data = await wechatLogin('mock_wx_code')
    setToken(data.token)
    showSuccessToast('微信登录成功')
    router.replace('/')
  } catch (e) {
    showFailToast(e.message)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f7f8fa; }
.actions { margin: 24px 16px; }
.mt { margin-top: 12px; }
</style>
