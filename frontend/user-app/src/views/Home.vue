<template>
  <div class="page page--tabbar">
    <van-nav-bar title="多商户商城" />
    <van-swipe class="banner" :autoplay="4000" indicator-color="white">
      <van-swipe-item v-for="(img, i) in banners" :key="i">
        <van-image width="100%" height="160" fit="cover" :src="img" />
      </van-swipe-item>
    </van-swipe>

    <van-grid :column-num="4" :border="false" class="shortcuts">
      <van-grid-item icon="fire-o" text="热榜" @click="$router.push('/shops/rank')" />
      <van-grid-item icon="shopping-cart-o" text="购物车" @click="goCart" />
      <van-grid-item icon="orders-o" text="订单" @click="goOrders" />
      <van-grid-item icon="user-o" text="我的" @click="$router.push('/profile')" />
    </van-grid>

    <van-cell-group inset title="热门店铺">
      <van-skeleton v-if="rankLoading" title :row="3" />
      <template v-else>
        <van-empty v-if="rankList.length === 0" description="暂无排行" />
        <van-cell
          v-for="item in rankList"
          :key="item.shopId"
          :title="`${item.rank}. ${item.shopName}`"
          is-link
          @click="$router.push('/shops/rank')"
        >
          <template #value>
            <van-tag type="danger">热度 {{ item.weightScore }}</van-tag>
          </template>
        </van-cell>
      </template>
    </van-cell-group>

    <div class="cta">
      <van-button round block type="primary" @click="$router.push('/products')">去逛逛</van-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listShopRank } from '../api/shop'
import { getToken } from '../api/request'

const router = useRouter()
const rankList = ref([])
const rankLoading = ref(true)
const banners = [
  'https://fastly.jsdelivr.net/npm/@vant/assets/apple-1.jpeg',
  'https://fastly.jsdelivr.net/npm/@vant/assets/apple-2.jpeg'
]

function goCart() {
  if (!getToken()) {
    router.push('/login')
    return
  }
  router.push('/cart')
}

function goOrders() {
  if (!getToken()) {
    router.push('/login')
    return
  }
  router.push('/orders')
}

onMounted(async () => {
  try {
    rankList.value = (await listShopRank(5)).slice(0, 5)
  } finally {
    rankLoading.value = false
  }
})
</script>

<style scoped>
.banner { margin: 12px 12px 0; border-radius: 8px; overflow: hidden; }
.shortcuts { margin: 8px 0; background: #fff; }
.cta { margin: 16px; }
</style>
