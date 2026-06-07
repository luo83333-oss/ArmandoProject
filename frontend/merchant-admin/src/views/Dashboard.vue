<template>
  <div class="page">
    <div class="toolbar">
      <h2>数据看板</h2>
      <el-button type="danger" link @click="onLogout">退出</el-button>
    </div>

    <el-row :gutter="16" class="cards">
      <el-col :xs="12" :sm="8" :md="4">
        <el-card shadow="hover">
          <div class="metric-label">今日销售额</div>
          <div class="metric-value primary">¥{{ formatMoney(overview?.todaySalesAmount) }}</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <el-card shadow="hover">
          <div class="metric-label">今日订单</div>
          <div class="metric-value">{{ overview?.todayOrderCount ?? 0 }}</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <el-card shadow="hover">
          <div class="metric-label">累计销售额</div>
          <div class="metric-value">¥{{ formatMoney(overview?.totalSalesAmount) }}</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <el-card shadow="hover">
          <div class="metric-label">有效订单</div>
          <div class="metric-value">{{ overview?.totalOrderCount ?? 0 }}</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <el-card shadow="hover">
          <div class="metric-label">待发货</div>
          <div class="metric-value warning">{{ overview?.pendingShipCount ?? 0 }}</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <el-card shadow="hover">
          <div class="metric-label">近7日下单用户</div>
          <div class="metric-value">{{ overview?.recentBuyerCount ?? 0 }}</div>
          <div class="metric-hint">暂无独立访客埋点</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="mt">
      <template #header>
        <div class="card-header">
          <span>销售趋势</span>
          <el-radio-group v-model="days" size="small" @change="loadTrend">
            <el-radio-button :value="7">近7天</el-radio-button>
            <el-radio-button :value="30">近30天</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <div v-loading="loading">
        <BaseChart :option="salesOption" height="340px" />
      </div>
    </el-card>

    <el-row :gutter="16" class="mt">
      <el-col :xs="24" :md="12">
        <el-card>
          <template #header>订单量趋势</template>
          <BaseChart :option="orderOption" height="300px" />
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card>
          <template #header>下单用户趋势</template>
          <BaseChart :option="buyerOption" height="300px" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import BaseChart from '../components/BaseChart.vue'
import { getStatsOverview, getStatsTrend } from '../api/stats'
import { clearToken, getToken } from '../api/request'

const router = useRouter()
const overview = ref(null)
const trend = ref([])
const days = ref(7)
const loading = ref(false)

function formatMoney(value) {
  const num = Number(value ?? 0)
  return num.toFixed(2)
}

function buildDates() {
  return trend.value.map((item) => item.date.slice(5))
}

const salesOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 48, right: 24, top: 32, bottom: 32 },
  xAxis: { type: 'category', data: buildDates(), boundaryGap: false },
  yAxis: { type: 'value', name: '元' },
  series: [{
    name: '销售额',
    type: 'line',
    smooth: true,
    areaStyle: { opacity: 0.12 },
    itemStyle: { color: '#409eff' },
    data: trend.value.map((item) => Number(item.salesAmount))
  }]
}))

const orderOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 48, right: 24, top: 32, bottom: 32 },
  xAxis: { type: 'category', data: buildDates() },
  yAxis: { type: 'value', minInterval: 1 },
  series: [{
    name: '订单量',
    type: 'bar',
    itemStyle: { color: '#67c23a' },
    data: trend.value.map((item) => item.orderCount)
  }]
}))

const buyerOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 48, right: 24, top: 32, bottom: 32 },
  xAxis: { type: 'category', data: buildDates(), boundaryGap: false },
  yAxis: { type: 'value', minInterval: 1 },
  series: [{
    name: '下单用户',
    type: 'line',
    smooth: true,
    itemStyle: { color: '#e6a23c' },
    data: trend.value.map((item) => item.buyerCount)
  }]
}))

async function loadOverview() {
  overview.value = await getStatsOverview()
}

async function loadTrend() {
  loading.value = true
  try {
    trend.value = await getStatsTrend(days.value)
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

async function load() {
  if (!getToken()) {
    router.replace('/login')
    return
  }
  try {
    await Promise.all([loadOverview(), loadTrend()])
  } catch (e) {
    ElMessage.error(e.message)
  }
}

function onLogout() {
  clearToken()
  router.replace('/login')
}

onMounted(load)
</script>

<style scoped>
.page { padding: 24px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.toolbar h2 { margin: 0; }
.cards { margin-bottom: 8px; }
.cards .el-col { margin-bottom: 16px; }
.metric-label { color: #909399; font-size: 13px; }
.metric-value { margin-top: 8px; font-size: 24px; font-weight: 600; }
.metric-value.primary { color: #409eff; }
.metric-value.warning { color: #e6a23c; }
.metric-hint { margin-top: 4px; font-size: 12px; color: #c0c4cc; }
.mt { margin-top: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
