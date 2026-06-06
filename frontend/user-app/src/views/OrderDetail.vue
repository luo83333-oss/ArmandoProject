<template>
  <div class="page" v-if="order">
    <van-nav-bar title="订单详情" left-arrow @click-left="$router.back()" />
    <van-cell-group inset>
      <van-cell title="订单号" :value="order.orderNo" />
      <van-cell title="店铺" :value="order.shopName" />
      <van-cell title="状态" :value="order.statusLabel" />
      <van-cell title="应付金额" :value="`¥${order.payAmount}`" />
      <van-cell v-if="order.logisticsNo" title="物流单号" :value="order.logisticsNo" />
    </van-cell-group>
    <van-cell-group inset title="收货地址" class="mt">
      <van-cell v-if="order.address" :title="order.address.receiverName" :label="`${order.address.receiverPhone} ${order.address.province}${order.address.city}${order.address.detail}`" />
    </van-cell-group>
    <van-cell-group inset title="商品" class="mt">
      <van-cell v-for="item in order.items" :key="item.id" :title="item.productTitle" :label="item.specJson" :value="`¥${item.unitPrice} x${item.quantity}`" />
    </van-cell-group>
    <div class="actions">
      <van-button v-if="order.status === 10" round block type="primary" :loading="acting" @click="onPay">模拟支付</van-button>
      <van-button v-if="order.status === 10" round block plain class="mt" :loading="acting" @click="onCancel">取消订单</van-button>
      <van-button v-if="order.status === 30" round block type="primary" :loading="acting" @click="onConfirm">确认收货</van-button>
      <van-button v-if="order.afterSaleAvailable" round block plain class="mt" disabled>申请售后（V2 开放）</van-button>
    </div>
  </div>
  <van-loading v-else class="loading" />
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showFailToast, showSuccessToast, showConfirmDialog } from 'vant'
import { getOrder, mockPay, confirmReceive, cancelOrder } from '../api/order'
import { getToken } from '../api/request'

const route = useRoute()
const router = useRouter()
const order = ref(null)
const acting = ref(false)

async function load() {
  order.value = await getOrder(route.params.id)
}

async function onPay() {
  acting.value = true
  try {
    order.value = await mockPay(route.params.id)
    showSuccessToast('支付成功，等待商家发货')
  } catch (e) {
    showFailToast(e.message)
  } finally {
    acting.value = false
  }
}

async function onCancel() {
  try {
    await showConfirmDialog({ title: '确认取消订单？' })
    acting.value = true
    order.value = await cancelOrder(route.params.id)
    showSuccessToast('订单已取消')
  } catch (e) {
    if (e !== 'cancel') showFailToast(e.message || '操作取消')
  } finally {
    acting.value = false
  }
}

async function onConfirm() {
  acting.value = true
  try {
    order.value = await confirmReceive(route.params.id)
    showSuccessToast('已确认收货')
  } catch (e) {
    showFailToast(e.message)
  } finally {
    acting.value = false
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
