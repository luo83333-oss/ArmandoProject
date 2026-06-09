<template>
  <div class="page" v-if="order">
    <van-nav-bar title="订单详情" left-arrow @click-left="$router.back()" />
    <van-notice-bar v-if="order.status === 10" color="#1989fa" background="#ecf9ff" left-icon="info-o">
      订单待付款，请选择支付方式后完成支付
    </van-notice-bar>
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
    <van-cell-group v-if="order.status === 10 && channels.length" inset title="支付方式" class="mt">
      <van-radio-group v-model="payChannel">
        <van-cell v-for="c in channels" :key="c.code" :title="c.label" clickable @click="payChannel = c.code">
          <template #right-icon>
            <van-radio :name="c.code" />
          </template>
        </van-cell>
      </van-radio-group>
    </van-cell-group>
    <div class="actions">
      <van-button v-if="order.status === 10" round block type="primary" :loading="acting" @click="onPay">
        {{ payButtonText }}
      </van-button>
      <van-button v-if="pendingPay" round block type="warning" class="mt" :loading="acting" @click="onSandboxComplete">
        完成沙箱支付
      </van-button>
      <van-button v-if="order.status === 10" round block plain class="mt" :loading="acting" @click="onCancel">取消订单</van-button>
      <van-button v-if="order.status === 30" round block type="primary" :loading="acting" @click="onConfirm">确认收货</van-button>
      <van-button v-if="order.afterSaleAvailable" round block plain class="mt" disabled>申请售后（V2 开放）</van-button>
      <van-button
        v-if="order.status === 40 && !order.reviewed"
        round
        block
        type="primary"
        class="mt"
        @click="showReview = true"
      >
        评价订单
      </van-button>
    </div>

    <van-popup v-model:show="showReview" position="bottom" round :style="{ padding: '16px 16px 24px' }">
      <div class="review-title">订单评价</div>
      <van-rate v-model="reviewRating" :size="28" color="#ffd21e" void-icon="star" void-color="#eee" />
      <van-field
        v-model="reviewContent"
        rows="3"
        autosize
        type="textarea"
        maxlength="1024"
        show-word-limit
        placeholder="说说本次购物体验（选填）"
        class="review-field"
      />
      <van-button round block type="primary" :loading="acting" @click="onSubmitReview">提交评价</van-button>
    </van-popup>
  </div>
  <van-loading v-else class="loading" />
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showFailToast, showSuccessToast, showConfirmDialog } from 'vant'
import { getOrder, payOrder, confirmReceive, cancelOrder, submitReview } from '../api/order'
import { listPaymentChannels, completeSandboxPay } from '../api/payment'
import { getToken } from '../api/request'

const route = useRoute()
const router = useRouter()
const order = ref(null)
const acting = ref(false)
const channels = ref([])
const payChannel = ref('wechat')
const pendingPay = ref(null)
const showReview = ref(false)
const reviewRating = ref(5)
const reviewContent = ref('')

const payButtonText = computed(() => {
  if (payChannel.value === 'mock') return '立即支付'
  const c = channels.value.find(x => x.code === payChannel.value)
  return c ? `唤起${c.label}` : '去支付'
})

async function load() {
  order.value = await getOrder(route.params.id)
}

async function loadChannels() {
  try {
    channels.value = await listPaymentChannels()
    const prefer = channels.value.find(c => c.code === 'wechat') || channels.value[0]
    if (prefer) payChannel.value = prefer.code
  } catch {
    channels.value = [{ code: 'mock', label: '模拟支付' }]
  }
}

async function onPay() {
  if (payChannel.value === 'mock') {
    try {
      await showConfirmDialog({ title: '模拟支付', message: '模拟支付会立即完成，订单变为待发货。确定支付？' })
    } catch {
      return
    }
  }
  acting.value = true
  pendingPay.value = null
  try {
    const result = await payOrder(route.params.id, payChannel.value)
    if (result.status === 'success') {
      order.value = result.order
      showSuccessToast('支付成功，等待商家发货')
    } else {
      pendingPay.value = result
      showSuccessToast(result.sandboxHint || '请完成沙箱支付')
    }
  } catch (e) {
    showFailToast(e.message)
  } finally {
    acting.value = false
  }
}

async function onSandboxComplete() {
  if (!pendingPay.value) return
  acting.value = true
  try {
    const result = await completeSandboxPay({
      orderId: Number(route.params.id),
      tradeNo: pendingPay.value.tradeNo,
      channel: pendingPay.value.channel
    })
    order.value = result.order
    pendingPay.value = null
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
    pendingPay.value = null
    showSuccessToast('订单已取消')
  } catch (e) {
    if (e !== 'cancel') showFailToast(e.message || '操作取消')
  } finally {
    acting.value = false
  }
}

async function onSubmitReview() {
  if (!reviewRating.value) {
    showFailToast('请选择评分')
    return
  }
  acting.value = true
  try {
    await submitReview(route.params.id, {
      rating: reviewRating.value,
      content: reviewContent.value?.trim() || undefined
    })
    showReview.value = false
    await load()
    showSuccessToast('评价成功')
  } catch (e) {
    showFailToast(e.message)
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
    await Promise.all([load(), loadChannels()])
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
.review-title { font-size: 16px; font-weight: 600; margin-bottom: 12px; text-align: center; }
.review-field { margin: 12px 0 16px; }
</style>
