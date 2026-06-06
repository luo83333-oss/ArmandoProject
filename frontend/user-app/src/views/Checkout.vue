<template>
  <div class="page">
    <van-nav-bar title="确认订单" left-arrow @click-left="$router.back()" />
    <van-form @submit="onSubmit">
      <van-cell-group inset title="收货地址">
        <van-field v-model="form.receiverName" label="收货人" placeholder="姓名" :rules="[{ required: true }]" />
        <van-field v-model="form.receiverPhone" label="手机号" placeholder="手机号" :rules="[{ required: true }]" />
        <van-field v-model="form.province" label="省份" placeholder="如：广东省" :rules="[{ required: true }]" />
        <van-field v-model="form.city" label="城市" placeholder="如：深圳市" :rules="[{ required: true }]" />
        <van-field v-model="form.detail" label="详细地址" placeholder="街道门牌" :rules="[{ required: true }]" />
      </van-cell-group>
      <van-cell-group inset title="结算说明" class="mt">
        <van-cell title="已选商品" :value="`${selectedCount} 件`" />
        <van-cell title="应付金额" :value="`¥${totalAmount}`" />
      </van-cell-group>
      <div class="actions">
        <van-button round block type="primary" native-type="submit" :loading="loading">提交订单</van-button>
      </div>
    </van-form>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showFailToast, showSuccessToast } from 'vant'
import { listCart } from '../api/cart'
import { checkout } from '../api/order'
import { getToken } from '../api/request'

const router = useRouter()
const loading = ref(false)
const selectedCount = ref(0)
const totalAmount = ref('0.00')
const form = ref({
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  detail: ''
})

async function loadSummary() {
  const cart = await listCart()
  const selected = cart.filter(i => i.selected === 1)
  selectedCount.value = selected.length
  totalAmount.value = selected
    .reduce((s, i) => s + Number(i.price) * i.quantity, 0)
    .toFixed(2)
}

async function onSubmit() {
  loading.value = true
  try {
    const orders = await checkout(form.value)
    showSuccessToast(`已创建 ${orders.length} 笔订单`)
    if (orders.length === 1) {
      router.replace(`/orders/${orders[0].id}`)
    } else {
      router.replace('/orders')
    }
  } catch (e) {
    showFailToast(e.message)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  if (!getToken()) {
    router.replace('/login')
    return
  }
  try {
    await loadSummary()
    if (selectedCount.value === 0) {
      showFailToast('请先选择商品')
      router.replace('/cart')
    }
  } catch (e) {
    showFailToast(e.message)
  }
})
</script>

<style scoped>
.page { min-height: 100vh; background: #f7f8fa; }
.mt { margin-top: 12px; }
.actions { margin: 24px 16px; }
</style>
