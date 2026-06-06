<template>
  <div class="login-wrap">
    <el-card class="card">
      <template #header>平台总控台</template>
      <el-form :model="form" @submit.prevent="onSubmit">
        <el-form-item label="管理员手机">
          <el-input v-model="form.phone" placeholder="13800000000" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="admin123" />
        </el-form-item>
        <el-button type="primary" native-type="submit" :loading="loading" style="width:100%">登录</el-button>
      </el-form>
      <p class="tip">首次启动后端会自动创建管理员：13800000000 / admin123</p>
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
const form = ref({ phone: '13800000000', password: 'admin123' })

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
.login-wrap { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: #1d1e1f; }
.card { width: 400px; }
.tip { margin-top: 16px; color: #909399; font-size: 13px; }
</style>
