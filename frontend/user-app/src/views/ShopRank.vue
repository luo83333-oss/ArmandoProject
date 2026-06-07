<template>
  <div class="page">
    <van-nav-bar title="热门店铺榜" left-arrow @click-left="$router.back()" />
    <van-pull-refresh v-model="refreshing" @refresh="load">
      <van-empty v-if="!loading && list.length === 0" description="暂无排行，请稍后重试" />
      <van-cell-group v-else inset>
        <van-cell v-for="item in list" :key="item.shopId" :title="`${item.rank}. ${item.shopName}`" :label="item.description || '优质商家'">
          <template #value>
            <van-tag type="danger">热度 {{ item.weightScore }}</van-tag>
          </template>
        </van-cell>
      </van-cell-group>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { showFailToast } from 'vant'
import { listShopRank } from '../api/shop'

const list = ref([])
const loading = ref(false)
const refreshing = ref(false)

async function load() {
  loading.value = true
  try {
    list.value = await listShopRank(20)
  } catch (e) {
    showFailToast(e.message)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.page { min-height: 100vh; background: #f7f8fa; padding-bottom: 24px; }
</style>
