<template>
  <div class="page">
    <van-nav-bar title="我的收藏" left-arrow @click-left="$router.back()" />
    <van-pull-refresh v-model="refreshing" @refresh="load">
      <van-skeleton v-if="loading && list.length === 0" title :row="4" />
      <van-empty v-else-if="list.length === 0" description="暂无收藏商品" />
      <van-cell-group v-else inset>
        <van-cell
          v-for="item in list"
          :key="item.productId"
          :title="item.title"
          :label="item.shopName"
          is-link
          @click="$router.push(`/products/${item.productId}`)"
        >
          <template #icon>
            <van-image width="48" height="48" fit="cover" radius="4" :src="item.mainImageUrl" class="thumb" />
          </template>
          <template #value>
            <span class="price">¥{{ formatPrice(item.minPrice) }}</span>
          </template>
        </van-cell>
      </van-cell-group>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showFailToast } from 'vant'
import { listFavorites } from '../api/favorite'
import { getToken } from '../api/request'

const router = useRouter()
const list = ref([])
const loading = ref(false)
const refreshing = ref(false)

function formatPrice(v) {
  return Number(v || 0).toFixed(2)
}

async function load() {
  loading.value = true
  try {
    list.value = await listFavorites()
  } catch (e) {
    showFailToast(e.message)
  } finally {
    loading.value = false
    refreshing.value = false
  }
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
.page { min-height: 100vh; background: #f7f8fa; }
.thumb { margin-right: 12px; }
.price { color: #ee0a24; font-weight: 600; }
</style>
