<template>
  <div class="page">
    <van-nav-bar title="我的订单" left-arrow @click-left="$router.push('/')" />
    <van-empty v-if="!loading && list.length === 0" description="暂无订单" />
    <van-cell-group v-else inset>
      <van-cell
        v-for="order in list"
        :key="order.id"
        :title="order.shopName"
        :label="order.orderNo"
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showFailToast } from 'vant'
import { listOrders } from '../api/order'
import { getToken } from '../api/request'

const router = useRouter()
const list = ref([])
const loading = ref(false)

onMounted(async () => {
  if (!getToken()) {
    router.replace('/login')
    return
  }
  loading.value = true
  try {
    list.value = await listOrders()
  } catch (e) {
    showFailToast(e.message)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.page { min-height: 100vh; background: #f7f8fa; }
.amount { color: #ee0a24; margin-top: 4px; }
</style>
