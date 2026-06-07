<template>
  <div class="page">
    <div class="toolbar">
      <h2>交易统计</h2>
      <el-button type="danger" link @click="onLogout">退出</el-button>
    </div>

    <el-row :gutter="16" class="cards">
      <el-col :xs="12" :sm="8" :md="4">
        <el-card shadow="hover">
          <div class="metric-label">今日 GMV</div>
          <div class="metric-value primary">¥{{ formatMoney(overview?.todayGmv) }}</div>
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
          <div class="metric-label">累计 GMV</div>
          <div class="metric-value">¥{{ formatMoney(overview?.totalGmv) }}</div>
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
          <div class="metric-label">平台佣金</div>
          <div class="metric-value warning">¥{{ formatMoney(overview?.totalCommission) }}</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <el-card shadow="hover">
          <div class="metric-label">有交易店铺</div>
          <div class="metric-value">{{ overview?.activeShopCount ?? 0 }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="mt">
      <template #header>
        <div class="card-header">
          <span>GMV / 订单趋势</span>
          <div class="filters">
            <el-radio-group v-model="granularity" size="small" @change="loadTrend">
              <el-radio-button value="day">按日</el-radio-button>
              <el-radio-button value="week">按周</el-radio-button>
              <el-radio-button value="month">按月</el-radio-button>
            </el-radio-group>
            <el-radio-group v-model="days" size="small" class="ml" @change="loadTrend">
              <el-radio-button :value="7">近7天</el-radio-button>
              <el-radio-button :value="30">近30天</el-radio-button>
              <el-radio-button :value="90">近90天</el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </template>
      <div v-loading="trendLoading">
        <BaseChart :option="gmvOption" height="340px" />
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
          <template #header>平台佣金趋势</template>
          <BaseChart :option="commissionOption" height="300px" />
        </el-card>
      </el-col>
    </el-row>

    <el-card class="mt">
      <template #header>
        <div class="card-header">
          <span>商家 GMV 排行（近{{ shopDays }}天）</span>
          <div>
            <el-radio-group v-model="shopDays" size="small" @change="loadShops">
              <el-radio-button :value="7">7天</el-radio-button>
              <el-radio-button :value="30">30天</el-radio-button>
              <el-radio-button :value="90">90天</el-radio-button>
            </el-radio-group>
            <el-button class="ml" size="small" :loading="exporting" @click="onExport">导出 CSV</el-button>
          </div>
        </div>
      </template>
      <el-table :data="shops" v-loading="shopLoading" stripe>
        <el-table-column prop="rank" label="排名" width="70" />
        <el-table-column prop="shopName" label="店铺" min-width="140" />
        <el-table-column label="GMV" width="120">
          <template #default="{ row }">¥{{ formatMoney(row.gmvAmount) }}</template>
        </el-table-column>
        <el-table-column prop="orderCount" label="订单数" width="100" />
        <el-table-column label="佣金" width="120">
          <template #default="{ row }">¥{{ formatMoney(row.commissionAmount) }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import BaseChart from '../components/BaseChart.vue'
import { getStatsOverview, getStatsTrend, getShopRanking, exportShopRanking } from '../api/stats'
import { clearToken, getToken } from '../api/request'

const router = useRouter()
const overview = ref(null)
const trend = ref([])
const shops = ref([])
const days = ref(30)
const granularity = ref('day')
const shopDays = ref(30)
const trendLoading = ref(false)
const shopLoading = ref(false)
const exporting = ref(false)

function formatMoney(value) {
  return Number(value ?? 0).toFixed(2)
}

function periodLabels() {
  return trend.value.map((item) => {
    const p = item.period
    if (granularity.value === 'day') return p.slice(5)
    if (granularity.value === 'month') return p
    return p.slice(5)
  })
}

const gmvOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 48, right: 24, top: 32, bottom: 32 },
  xAxis: { type: 'category', data: periodLabels(), boundaryGap: granularity.value !== 'day' },
  yAxis: { type: 'value', name: '元' },
  series: [{
    name: 'GMV',
    type: granularity.value === 'day' ? 'line' : 'bar',
    smooth: true,
    areaStyle: granularity.value === 'day' ? { opacity: 0.12 } : undefined,
    itemStyle: { color: '#e6a23c' },
    data: trend.value.map((item) => Number(item.gmvAmount))
  }]
}))

const orderOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 48, right: 24, top: 32, bottom: 32 },
  xAxis: { type: 'category', data: periodLabels() },
  yAxis: { type: 'value', minInterval: 1 },
  series: [{
    name: '订单量',
    type: 'bar',
    itemStyle: { color: '#409eff' },
    data: trend.value.map((item) => item.orderCount)
  }]
}))

const commissionOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 48, right: 24, top: 32, bottom: 32 },
  xAxis: { type: 'category', data: periodLabels(), boundaryGap: false },
  yAxis: { type: 'value', name: '元' },
  series: [{
    name: '佣金',
    type: 'line',
    smooth: true,
    itemStyle: { color: '#67c23a' },
    data: trend.value.map((item) => Number(item.commissionAmount))
  }]
}))

async function loadOverview() {
  overview.value = await getStatsOverview()
}

async function loadTrend() {
  trendLoading.value = true
  try {
    trend.value = await getStatsTrend(days.value, granularity.value)
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    trendLoading.value = false
  }
}

async function loadShops() {
  shopLoading.value = true
  try {
    shops.value = await getShopRanking(shopDays.value, 20)
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    shopLoading.value = false
  }
}

async function onExport() {
  exporting.value = true
  try {
    await exportShopRanking(shopDays.value)
    ElMessage.success('导出成功')
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    exporting.value = false
  }
}

async function load() {
  if (!getToken()) {
    router.replace('/login')
    return
  }
  try {
    await Promise.all([loadOverview(), loadTrend(), loadShops()])
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
.cards .el-col { margin-bottom: 16px; }
.metric-label { color: #909399; font-size: 13px; }
.metric-value { margin-top: 8px; font-size: 24px; font-weight: 600; }
.metric-value.primary { color: #e6a23c; }
.metric-value.warning { color: #67c23a; }
.mt { margin-top: 16px; }
.ml { margin-left: 8px; }
.card-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 8px; }
.filters { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; }
</style>
