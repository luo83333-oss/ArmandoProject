<template>
  <div class="page">
    <div class="toolbar">
      <h2>店铺权重排行</h2>
      <el-button type="danger" link @click="onLogout">退出</el-button>
    </div>
    <el-card>
      <template #header>权重系数配置</template>
      <el-form :model="config" label-width="120px" @submit.prevent="onSaveConfig">
        <el-form-item label="销量权重">
          <el-input-number v-model="config.saleWeight" :min="0" :step="0.1" :precision="2" />
        </el-form-item>
        <el-form-item label="评价权重">
          <el-input-number v-model="config.reviewWeight" :min="0" :step="0.1" :precision="2" />
        </el-form-item>
        <el-form-item label="违规扣分">
          <el-input-number v-model="config.violationPenalty" :min="0" :step="1" :precision="2" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="onSaveConfig">保存配置</el-button>
          <el-button type="warning" :loading="calculating" @click="onRecalculate">重新计算排行</el-button>
        </el-form-item>
      </el-form>
      <p class="hint">公式：权重分 = 销量×销量权重 + 评价数×评价权重 − 违规商品数×违规扣分</p>
    </el-card>
    <el-card class="mt">
      <template #header>当前排行榜</template>
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="rank" label="名次" width="70" />
        <el-table-column prop="shopName" label="店铺" min-width="140" />
        <el-table-column prop="weightScore" label="权重分" width="100" />
        <el-table-column prop="saleCount" label="有效订单" width="100" />
        <el-table-column prop="reviewCount" label="评价数" width="90" />
        <el-table-column prop="violationCount" label="违规商品" width="100" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getRankConfig, updateRankConfig, recalculateRank, listRank } from '../api/rank'
import { clearToken, getToken } from '../api/request'

const router = useRouter()
const config = ref({ saleWeight: 1, reviewWeight: 0.5, violationPenalty: 10 })
const list = ref([])
const loading = ref(false)
const saving = ref(false)
const calculating = ref(false)

async function load() {
  loading.value = true
  try {
    config.value = await getRankConfig()
    list.value = await listRank(20)
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

async function onSaveConfig() {
  saving.value = true
  try {
    config.value = await updateRankConfig(config.value)
    ElMessage.success('配置已保存')
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    saving.value = false
  }
}

async function onRecalculate() {
  calculating.value = true
  try {
    list.value = await recalculateRank()
    ElMessage.success('排行已更新')
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    calculating.value = false
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
.hint { color: #909399; font-size: 13px; margin: 0; }
</style>
