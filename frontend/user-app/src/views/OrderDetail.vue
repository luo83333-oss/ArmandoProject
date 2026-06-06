<template>
  <div class="page" v-if="order">
    <van-nav-bar title="订单详情" left-arrow @click-left="$router.back()" />
    <van-cell-group inset>
      <van-cell title="订单号" :value="order.orderNo" />
      <van-cell title="店铺" :value="order.shopName" />
      <van-cell title="状态" :value="order.statusLabel" />
      <van-cell title="应付金额" :value="`¥${order.payAmount}`" />
    </van-cell-group>
    <van-cell-group inset title="收货地址" class="mt">
      <van-cell v-if="order.address" :title="order.address.receiverName" :label="`${order.address.receiverPhone} ${order.address.province}${order.address.city}${order.address.detail}`" />
    </van-cell-group>
    <van-cell-group inset title="商品" class="mt">
      <van-cell v-for="item in order.items" :key="item.id" :title="item.productTitle" :label="item.specJson" :value="`¥${item.unitPrice} x${item.quantity}`" />
    </van-cell-group>
    <div class="actions" v-if="order.status === 10">
      <van-button round block type="primary" :loading="paying" @click="onPay">模拟支付</van-button>
    </div>
  </div>
  <van-loading v-else class="loading" />
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showFailToast, showSuccessToast } from 'vant'
import { getOrder, mockPay } from '../api/order'
import { getToken } from '../api/request'

const route = useRoute()
const router = useRouter()
const order = ref(null)
const paying = ref(false)

async function load() {
  order.value = await getOrder(route.params.id)
}

async function onPay() {
  paying.value = true
  try {
    order.value = await mockPay(route.params.id)
    showSuccessToast('支付成功')
  } catch (e) {
    showFailToast(e.message)
  } finally {
    paying.value = false
  }
}

onMounted(async () => {
  if (!getToken()) {
    router.replace('/login')
    return
  }
  try {
    await load()
  } catch (e) {
    showFailToast(e.message)
  }
})
</script>

<style scoped>
.page { min-height: 100vh; background: #f7f8fa; padding-bottom: 24px; }
.mt { margin-top: 12px; }
.actions { margin: 24px 16px; }
.loading { display: flex; justify-content: center; margin-top: 80px; }
</style>
