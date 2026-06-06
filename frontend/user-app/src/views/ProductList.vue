<template>
  <div class="page">
    <van-nav-bar title="商品列表" left-arrow @click-left="$router.push('/')" />
    <form action="/" @submit.prevent="onSearch">
      <van-search v-model="keyword" placeholder="搜索商品" show-action @search="onSearch">
        <template #action>
          <div @click="onSearch">搜索</div>
        </template>
      </van-search>
    </form>
    <van-tabs v-model:active="activeTab" @change="onTabChange">
      <van-tab title="全部" :name="0" />
      <van-tab v-for="c in topCategories" :key="c.id" :title="c.name" :name="c.id" />
    </van-tabs>
    <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadMore">
      <van-card
        v-for="item in list"
        :key="item.id"
        :price="formatPrice(item.minPrice)"
        :title="item.title"
        :thumb="item.mainImageUrl || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'"
        @click="$router.push(`/products/${item.id}`)"
      >
        <template #desc>
          <div class="desc">{{ item.shopName }} · 库存 {{ item.totalStock }}</div>
        </template>
      </van-card>
    </van-list>
    <van-empty v-if="!loading && list.length === 0" description="暂无商品" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listProducts, listCategories } from '../api/product'

const keyword = ref('')
const activeTab = ref(0)
const topCategories = ref([])
const list = ref([])
const loading = ref(false)
const finished = ref(false)
const page = ref(1)

function formatPrice(p) {
  return p != null ? Number(p).toFixed(2) : '0.00'
}

async function loadMore() {
  loading.value = true
  try {
    const data = await listProducts({
      page: page.value,
      size: 10,
      keyword: keyword.value || undefined,
      categoryId: activeTab.value || undefined
    })
    list.value.push(...data.records)
    page.value++
    if (list.value.length >= data.total) finished.value = true
  } finally {
    loading.value = false
  }
}

function resetAndLoad() {
  list.value = []
  page.value = 1
  finished.value = false
  loadMore()
}

function onSearch() {
  resetAndLoad()
}

function onTabChange() {
  resetAndLoad()
}

onMounted(async () => {
  topCategories.value = await listCategories()
})
</script>

<style scoped>
.page { min-height: 100vh; background: #f7f8fa; padding-bottom: 16px; }
.desc { color: #969799; font-size: 12px; margin-top: 4px; }
</style>
