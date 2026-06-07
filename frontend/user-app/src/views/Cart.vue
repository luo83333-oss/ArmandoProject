<template>
  <div class="page page--action-bar">
    <van-nav-bar title="购物车" />
    <van-empty v-if="!loading && list.length === 0" description="购物车是空的">
      <van-button type="primary" @click="$router.push('/products')">去逛逛</van-button>
    </van-empty>
    <van-checkbox-group v-model="checkedIds" v-else>
      <van-swipe-cell v-for="item in list" :key="item.id">
        <van-card
          :num="item.quantity"
          :price="item.price"
          :title="item.productTitle"
          :thumb="item.mainImageUrl || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'"
        >
          <template #tags>
            <van-tag plain>{{ item.specJson }}</van-tag>
          </template>
          <template #footer>
            <van-checkbox :name="item.id" />
            <van-stepper
              :model-value="item.quantity"
              min="1"
              :max="item.stock"
              @change="v => onQtyChange(item, v)"
            />
          </template>
        </van-card>
        <template #right>
          <van-button square type="danger" text="删除" @click="onRemove(item)" />
        </template>
      </van-swipe-cell>
    </van-checkbox-group>
    <van-submit-bar
      v-if="list.length"
      class="submit-bar--tabbar"
      safe-area-inset-bottom
      :price="totalCents"
      button-text="去结算"
      @submit="onCheckout"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showFailToast, showSuccessToast } from 'vant'
import { listCart, updateCartItem, removeCartItem } from '../api/cart'
import { getToken } from '../api/request'

const router = useRouter()
const list = ref([])
const loading = ref(false)
const checkedIds = ref([])

const totalCents = computed(() => {
  const total = list.value
    .filter(i => checkedIds.value.includes(i.id))
    .reduce((sum, i) => sum + Number(i.price) * i.quantity, 0)
  return Math.round(total * 100)
})

async function load() {
  loading.value = true
  try {
    list.value = await listCart()
    checkedIds.value = list.value.filter(i => i.selected === 1).map(i => i.id)
  } catch (e) {
    showFailToast(e.message)
  } finally {
    loading.value = false
  }
}

async function onQtyChange(item, qty) {
  try {
    await updateCartItem(item.id, { quantity: qty })
    item.quantity = qty
  } catch (e) {
    showFailToast(e.message)
  }
}

async function onRemove(item) {
  try {
    await removeCartItem(item.id)
    showSuccessToast('已删除')
    load()
  } catch (e) {
    showFailToast(e.message)
  }
}

async function onCheckout() {
  if (!checkedIds.value.length) {
    showFailToast('请选择商品')
    return
  }
  for (const id of checkedIds.value) {
    const item = list.value.find(i => i.id === id)
    if (item && item.selected !== 1) {
      await updateCartItem(id, { selected: 1 })
    }
  }
  for (const item of list.value) {
    if (!checkedIds.value.includes(item.id) && item.selected === 1) {
      await updateCartItem(item.id, { selected: 0 })
    }
  }
  router.push('/checkout')
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
</style>
