<template>
  <div class="layout">
    <div class="toolbar">
      <h2>商家入驻审核</h2>
      <el-button type="danger" link @click="onLogout">退出</el-button>
    </div>
    <el-card>
      <el-radio-group v-model="filterStatus" @change="load">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button :label="0">待审核</el-radio-button>
        <el-radio-button :label="1">已通过</el-radio-button>
        <el-radio-button :label="2">已驳回</el-radio-button>
        <el-radio-button :label="3">已冻结</el-radio-button>
      </el-radio-group>
      <el-table :data="list" v-loading="loading" class="mt" stripe>
        <el-table-column prop="companyName" label="公司" min-width="140" />
        <el-table-column prop="contactName" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="电话" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="tagType(row.auditStatus)">{{ STATUS_MAP[row.auditStatus] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="shopName" label="店铺" min-width="120" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.auditStatus === 0" size="small" type="success" @click="doAudit(row, 1)">通过</el-button>
            <el-button v-if="row.auditStatus === 0" size="small" type="danger" @click="openReject(row)">驳回</el-button>
            <el-button v-if="row.auditStatus === 1" size="small" type="warning" @click="openFreeze(row)">冻结</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="400px">
      <el-input v-model="remark" type="textarea" placeholder="审核备注（可选）" :rows="3" />
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="auditing" @click="confirmAudit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { listMerchants, auditMerchant } from '../api/merchant'
import { clearToken, getToken } from '../api/request'

const router = useRouter()
const list = ref([])
const loading = ref(false)
const filterStatus = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const remark = ref('')
const auditing = ref(false)
const pendingRow = ref(null)
const pendingAction = ref(null)

const STATUS_MAP = { 0: '待审核', 1: '已通过', 2: '已驳回', 3: '已冻结' }

function tagType(s) {
  if (s === 1) return 'success'
  if (s === 2) return 'danger'
  if (s === 3) return 'warning'
  return 'info'
}

async function load() {
  loading.value = true
  try {
    list.value = await listMerchants(filterStatus.value === '' ? undefined : filterStatus.value)
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

async function doAudit(row, action, note = '') {
  auditing.value = true
  try {
    await auditMerchant(row.id, { action, remark: note })
    ElMessage.success('操作成功')
    await load()
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    auditing.value = false
  }
}

function openReject(row) {
  pendingRow.value = row
  pendingAction.value = 2
  dialogTitle.value = '驳回申请'
  remark.value = ''
  dialogVisible.value = true
}

function openFreeze(row) {
  pendingRow.value = row
  pendingAction.value = 3
  dialogTitle.value = '冻结商家'
  remark.value = ''
  dialogVisible.value = true
}

async function confirmAudit() {
  await doAudit(pendingRow.value, pendingAction.value, remark.value)
  dialogVisible.value = false
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
.layout { padding: 24px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.mt { margin-top: 16px; }
</style>
