<template>
  <div class="layout">
    <div class="toolbar">
      <h2>订单管理</h2>
      <el-button @click="$router.push('/')">返回工作台</el-button>
    </div>
    <el-card>
      <el-radio-group v-model="filterStatus" @change="load">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button :label="20">待发货</el-radio-button>
        <el-radio-button :label="30">已发货</el-radio-button>
        <el-radio-button :label="40">已完成</el-radio-button>
      </el-radio-group>
      <el-table :data="list" v-loading="loading" class="mt" stripe>
        <el-table-column prop="orderNo" label="订单号" min-width="160" />
        <el-table-column label="金额" width="100">
          <template #default="{ row }">¥{{ row.payAmount }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag>{{ row.statusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="logisticsNo" label="物流单号" min-width="120" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 20" size="small" type="primary" @click="openShip(row)">发货</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-dialog v-model="dialogVisible" title="填写物流单号" width="400px">
      <el-input v-model="logisticsNo" placeholder="如 SF1234567890" />
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="shipping" @click="confirmShip">确认发货</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { listOrders, shipOrder } from '../api/order'
import { getToken } from '../api/request'

const router = useRouter()
const list = ref([])
const loading = ref(false)
const filterStatus = ref(20)
const dialogVisible = ref(false)
const logisticsNo = ref('')
const shipping = ref(false)
const pendingRow = ref(null)

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

function openShip(row) {
  pendingRow.value = row
  logisticsNo.value = ''
  dialogVisible.value = true
}

async function confirmShip() {
  if (!logisticsNo.value) {
    ElMessage.warning('请填写物流单号')
    return
  }
  shipping.value = true
  try {
    await shipOrder(pendingRow.value.id, logisticsNo.value)
    ElMessage.success('发货成功')
    dialogVisible.value = false
    load()
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    shipping.value = false
  }
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
.layout { padding: 24px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.mt { margin-top: 16px; }
</style>
