<template>
  <div class="page">
    <van-nav-bar title="我的订单" left-arrow @click-left="$router.push('/profile')" />
    <van-tabs v-model:active="activeTab" sticky @change="onTabChange">
      <van-tab title="全部" name="all" />
      <van-tab title="待付款" name="10" />
      <van-tab title="待发货" name="20" />
      <van-tab title="待收货" name="30" />
      <van-tab title="已完成" name="40" />
    </van-tabs>
    <van-pull-refresh v-model="refreshing" @refresh="load">
      <van-skeleton v-if="loading && list.length === 0" title :row="4" />
      <van-empty v-else-if="filteredList.length === 0" description="暂无订单" />
      <van-cell-group v-else inset>
        <van-cell
          v-for="order in filteredList"
          :key="order.id"
          :title="order.shopName"
          :value="order.statusLabel"
          is-link
          @click="$router.push(`/orders/${order.id}`)"
        >
          <template #label>
            <div>{{ order.orderNo }}</div>
            <div class="amount">¥{{ order.payAmount }}</div>
          </template>
        </van-cell>
      </van-cell-group>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showFailToast } from 'vant'
import { listOrders } from '../api/order'
import { getToken } from '../api/request'

const router = useRouter()
const list = ref([])
const loading = ref(false)
const refreshing = ref(false)
const activeTab = ref('all')

const filteredList = computed(() => {
  if (activeTab.value === 'all') return list.value
  const status = Number(activeTab.value)
  return list.value.filter((o) => o.status === status)
})

async function load() {
  loading.value = true
  try {
    list.value = await listOrders()
  } catch (e) {
    showFailToast(e.message)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

function onTabChange() {
  /* client-side filter */
}

onMounted(async () => {
  if (!getToken()) {
    router.replace('/login')
    return
  }
  await load()
})
</script>

<style scoped>
.amount { color: #ee0a24; margin-top: 4px; }
</style>
