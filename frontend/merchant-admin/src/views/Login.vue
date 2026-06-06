<template>
  <div class="login-wrap">
    <el-card class="card">
      <template #header>商家后台登录</template>
      <el-form :model="form" @submit.prevent="onSubmit">
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="使用用户端注册的账号" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-button type="primary" native-type="submit" :loading="loading" style="width:100%">登录</el-button>
      </el-form>
      <p class="tip">提示：先在用户端注册账号，再在此提交入驻申请</p>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api/auth'
import { setToken } from '../api/request'

const router = useRouter()
const loading = ref(false)
const form = ref({ phone: '', password: '' })

async function onSubmit() {
  loading.value = true
  try {
    const data = await login(form.value)
    setToken(data.token)
    ElMessage.success('登录成功')
    router.replace('/')
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrap { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: #f0f2f5; }
.card { width: 400px; }
.tip { margin-top: 16px; color: #909399; font-size: 13px; }
</style>
