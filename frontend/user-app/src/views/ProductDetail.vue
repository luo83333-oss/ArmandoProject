<template>
  <div class="page" v-if="product">
    <van-nav-bar title="商品详情" left-arrow @click-left="$router.back()" />
    <van-image width="100%" height="240" fit="cover" :src="product.mainImageUrl || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'" />
    <div class="info">
      <div class="price">¥{{ minPrice }}</div>
      <div class="title">{{ product.title }}</div>
      <div class="shop">{{ product.shopName }}</div>
    </div>
    <van-cell-group inset title="规格">
      <van-cell v-for="sku in product.skus" :key="sku.id" :title="sku.specJson" :value="`¥${sku.price} · 库存 ${sku.stock}`" />
    </van-cell-group>
    <van-cell-group inset title="详情" v-if="product.detailHtml">
      <van-cell>
        <div class="detail">{{ product.detailHtml }}</div>
      </van-cell>
    </van-cell-group>
  </div>
  <van-loading v-else class="loading" />
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { showFailToast } from 'vant'
import { getProduct } from '../api/product'

const route = useRoute()
const product = ref(null)

const minPrice = computed(() => {
  if (!product.value?.minPrice) return '0.00'
  return Number(product.value.minPrice).toFixed(2)
})

onMounted(async () => {
  try {
    product.value = await getProduct(route.params.id)
  } catch (e) {
    showFailToast(e.message)
  }
})
</script>

<style scoped>
.page { min-height: 100vh; background: #f7f8fa; padding-bottom: 24px; }
.info { background: #fff; padding: 16px; margin-bottom: 12px; }
.price { color: #ee0a24; font-size: 22px; font-weight: bold; }
.title { font-size: 16px; margin-top: 8px; }
.shop { color: #969799; font-size: 13px; margin-top: 4px; }
.detail { white-space: pre-wrap; line-height: 1.6; }
.loading { display: flex; justify-content: center; margin-top: 80px; }
</style>
