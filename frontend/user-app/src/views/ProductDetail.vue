<template>
  <div class="page" v-if="product">
    <van-nav-bar title="商品详情" left-arrow @click-left="$router.back()">
      <template #right>
        <van-icon :name="favorited ? 'star' : 'star-o'" :color="favorited ? '#ee0a24' : undefined" size="20" @click="onToggleFavorite" />
      </template>
    </van-nav-bar>
    <ProductGallery :images="imageList" />
    <div class="info">
      <div class="price">¥{{ minPrice }}</div>
      <div class="title">{{ product.title }}</div>
      <div class="shop">{{ product.shopName }}</div>
    </div>
    <van-cell-group inset title="规格">
      <van-cell
        v-for="sku in product.skus"
        :key="sku.id"
        :title="sku.specJson"
        :value="`¥${sku.price} · 库存 ${sku.stock}`"
        clickable
        :class="{ active: selectedSkuId === sku.id }"
        @click="selectedSkuId = sku.id"
      />
    </van-cell-group>
    <div class="fixed-action-bar">
      <van-button type="warning" @click="onAddCart">加入购物车</van-button>
      <van-button type="primary" @click="onBuyNow">立即购买</van-button>
    </div>
    <van-cell-group inset title="详情" v-if="product.detailHtml">
      <van-cell>
        <div class="detail">{{ product.detailHtml }}</div>
      </van-cell>
    </van-cell-group>
  </div>
  <div v-else class="page">
    <van-nav-bar title="商品详情" left-arrow @click-left="$router.back()" />
    <van-skeleton title avatar :row="5" style="padding: 16px" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useRouter } from 'vue-router'
import { showFailToast, showSuccessToast } from 'vant'
import { getProduct } from '../api/product'
import { addToCart, updateCartItem, listCart } from '../api/cart'
import { getFavoriteStatus, addFavorite, removeFavorite } from '../api/favorite'
import { getToken } from '../api/request'
import ProductGallery from '../components/ProductGallery.vue'

const route = useRoute()
const router = useRouter()
const product = ref(null)
const selectedSkuId = ref(null)
const favorited = ref(false)

const minPrice = computed(() => {
  if (!product.value?.minPrice) return '0.00'
  return Number(product.value.minPrice).toFixed(2)
})

const imageList = computed(() => {
  if (!product.value) return []
  const urls = []
  if (product.value.mainImageUrl) urls.push(product.value.mainImageUrl)
  for (const url of product.value.galleryUrls || []) {
    if (url && !urls.includes(url)) urls.push(url)
  }
  return urls
})

function requireLogin() {
  if (!getToken()) {
    router.push('/login')
    return false
  }
  return true
}

function requireSku() {
  if (!selectedSkuId.value) {
    showFailToast('请选择规格')
    return false
  }
  return true
}

async function onAddCart() {
  if (!requireLogin() || !requireSku()) return
  try {
    await addToCart(selectedSkuId.value, 1)
    showSuccessToast('已加入购物车')
  } catch (e) {
    showFailToast(e.message)
  }
}

async function loadFavoriteStatus() {
  if (!getToken() || !product.value?.id) return
  try {
    const status = await getFavoriteStatus(product.value.id)
    favorited.value = !!status.favorited
  } catch {
    favorited.value = false
  }
}

async function onToggleFavorite() {
  if (!requireLogin() || !product.value?.id) return
  try {
    if (favorited.value) {
      await removeFavorite(product.value.id)
      favorited.value = false
      showSuccessToast('已取消收藏')
    } else {
      await addFavorite(product.value.id)
      favorited.value = true
      showSuccessToast('已收藏')
    }
  } catch (e) {
    showFailToast(e.message)
    if (e.message?.includes('登录')) {
      router.push('/login')
    }
  }
}

async function onBuyNow() {
  if (!requireLogin() || !requireSku()) return
  try {
    const item = await addToCart(selectedSkuId.value, 1)
    const cart = await listCart()
    for (const c of cart) {
      await updateCartItem(c.id, { selected: c.id === item.id ? 1 : 0 })
    }
    router.push('/checkout')
  } catch (e) {
    showFailToast(e.message)
  }
}

onMounted(async () => {
  try {
    product.value = await getProduct(route.params.id)
    if (product.value?.skus?.length) {
      selectedSkuId.value = product.value.skus[0].id
    }
    await loadFavoriteStatus()
  } catch (e) {
    showFailToast(e.message)
  }
})
</script>

<style scoped>
.page { min-height: 100vh; background: #f7f8fa; padding-bottom: calc(72px + env(safe-area-inset-bottom, 0px)); }
.fixed-action-bar .van-button { flex: 1; }
.active { background: #fff7e6; }
.info { background: #fff; padding: 16px; margin-bottom: 12px; }
.price { color: #ee0a24; font-size: 22px; font-weight: bold; }
.title { font-size: 16px; margin-top: 8px; }
.shop { color: #969799; font-size: 13px; margin-top: 4px; }
.detail { white-space: pre-wrap; line-height: 1.6; }
</style>
