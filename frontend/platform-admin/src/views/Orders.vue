<template>
  <div class="page">
    <div class="toolbar">
      <h2>订单监管</h2>
      <el-button type="danger" link @click="onLogout">退出</el-button>
    </div>
    <el-card>
      <el-radio-group v-model="filterStatus" @change="load">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button :label="10">待付款</el-radio-button>
        <el-radio-button :label="20">待发货</el-radio-button>
        <el-radio-button :label="30">已发货</el-radio-button>
        <el-radio-button :label="40">已完成</el-radio-button>
        <el-radio-button :label="50">已取消</el-radio-button>
      </el-radio-group>
      <el-table :data="list" v-loading="loading" class="mt" stripe>
        <el-table-column prop="orderNo" label="订单号" min-width="160" />
        <el-table-column prop="shopName" label="店铺" min-width="120" />
        <el-table-column label="金额" width="100">
          <template #default="{ row }">¥{{ row.payAmount }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag>{{ row.statusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="logisticsNo" label="物流单号" min-width="120" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-dialog v-model="dialogVisible" title="订单详情" width="560px">
      <template v-if="detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="订单号">{{ detail.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="店铺">{{ detail.shopName }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ detail.statusLabel }}</el-descriptions-item>
          <el-descriptions-item label="应付金额">¥{{ detail.payAmount }}</el-descriptions-item>
          <el-descriptions-item v-if="detail.logisticsNo" label="物流单号">{{ detail.logisticsNo }}</el-descriptions-item>
        </el-descriptions>
        <el-table :data="detail.items" class="mt" size="small">
          <el-table-column prop="productTitle" label="商品" />
          <el-table-column label="单价" width="90">
            <template #default="{ row }">¥{{ row.unitPrice }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="70" />
        </el-table>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { listOrders, getOrder } from '../api/order'
import { clearToken, getToken } from '../api/request'

const router = useRouter()
const list = ref([])
const loading = ref(false)
const filterStatus = ref('')
const dialogVisible = ref(false)
const detail = ref(null)

async function load() {
  loading.value = true
  try {
    list.value = await listOrders(filterStatus.value === '' ? undefined : filterStatus.value)
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

async function openDetail(row) {
  try {
    detail.value = await getOrder(row.id)
    dialogVisible.value = true
  } catch (e) {
    ElMessage.error(e.message)
  }
}

function onLogout() {
  clearToken()
  router.replace('/login')
}

onMounted(() => {
  if (!getToken()) {
    router.replace('/login')
    return
  }
  load()
})
</script>

<style scoped>
.page { padding: 24px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.mt { margin-top: 16px; }
</style>
