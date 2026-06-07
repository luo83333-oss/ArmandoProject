<template>
  <div class="layout">
    <el-page-header content="商家后台" @back="() => {}" />
    <el-card class="mt">
      <template #header>
        <div class="header">
          <span>工作台</span>
          <el-button v-if="user" link type="danger" @click="onLogout">退出</el-button>
        </div>
      </template>
      <p v-if="user">欢迎，{{ user.nickname }}（{{ user.phone }}）</p>
      <el-empty v-if="!merchant" description="尚未提交入驻申请">
        <el-button type="primary" @click="$router.push('/apply')">去入驻</el-button>
      </el-empty>
      <template v-else>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="公司名称">{{ merchant.companyName }}</el-descriptions-item>
          <el-descriptions-item label="审核状态">
            <el-tag :type="statusType">{{ statusLabel }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="merchant.auditRemark" label="审核备注">{{ merchant.auditRemark }}</el-descriptions-item>
          <el-descriptions-item v-if="merchant.shopName" label="店铺名称">{{ merchant.shopName }}</el-descriptions-item>
        </el-descriptions>
        <el-button v-if="merchant.auditStatus === 2" type="primary" class="mt" @click="$router.push('/apply')">重新申请</el-button>
        <el-button v-if="merchant.auditStatus === 1" type="primary" class="mt" @click="$router.push('/dashboard')">数据看板</el-button>
        <el-button v-if="merchant.auditStatus === 1" class="mt" @click="$router.push('/products')">商品管理</el-button>
        <el-button v-if="merchant.auditStatus === 1" class="mt" @click="$router.push('/orders')">订单管理</el-button>
      </template>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMe } from '../api/auth'
import { getMine } from '../api/merchant'
import { clearToken, getToken } from '../api/request'

const router = useRouter()
const user = ref(null)
const merchant = ref(null)

const STATUS_MAP = { 0: '待审核', 1: '已通过', 2: '已驳回', 3: '已冻结' }
const statusLabel = computed(() => STATUS_MAP[merchant.value?.auditStatus] || '-')
const statusType = computed(() => {
  const s = merchant.value?.auditStatus
  if (s === 1) return 'success'
  if (s === 2) return 'danger'
  if (s === 3) return 'warning'
  return 'info'
})

async function load() {
  if (!getToken()) {
    router.replace('/login')
    return
  }
  try {
    user.value = await getMe()
    merchant.value = await getMine()
  } catch {
    clearToken()
    router.replace('/login')
  }
}

function onLogout() {
  clearToken()
  router.replace('/login')
}

onMounted(load)
</script>

<style scoped>
.layout { padding: 24px; max-width: 800px; margin: 0 auto; }
.mt { margin-top: 16px; }
.header { display: flex; justify-content: space-between; align-items: center; }
</style>
