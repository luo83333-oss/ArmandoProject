<template>
  <div class="page">
    <van-nav-bar title="注册" left-arrow @click-left="$router.back()" />
    <van-form @submit="onSubmit">
      <van-cell-group inset>
        <van-field v-model="form.phone" label="手机号" placeholder="请输入手机号" />
        <van-field v-model="form.code" label="验证码" placeholder="开发环境固定 123456">
          <template #button>
            <van-button size="small" type="primary" :disabled="countdown > 0" @click="onSendCode">
              {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
            </van-button>
          </template>
        </van-field>
        <van-field v-model="form.password" type="password" label="密码" placeholder="6-32 位" />
      </van-cell-group>
      <div class="actions">
        <van-button round block type="primary" native-type="submit" :loading="loading">注册</van-button>
      </div>
    </van-form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showSuccessToast, showFailToast } from 'vant'
import { sendCode, register } from '../api/auth'
import { setToken } from '../api/request'

const router = useRouter()
const loading = ref(false)
const countdown = ref(0)
const form = ref({ phone: '', code: '123456', password: '' })

async function onSendCode() {
  if (!form.value.phone) {
    showFailToast('请先输入手机号')
    return
  }
  try {
    await sendCode(form.value.phone)
    showSuccessToast('验证码已发送（开发环境：123456）')
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) clearInterval(timer)
    }, 1000)
  } catch (e) {
    showFailToast(e.message)
  }
}

async function onSubmit() {
  loading.value = true
  try {
    const data = await register(form.value)
    setToken(data.token)
    showSuccessToast('注册成功')
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
</style>
