<template>
  <div class="page">
    <van-nav-bar title="我的关注" left-arrow @click-left="$router.back()" />
    <van-pull-refresh v-model="refreshing" @refresh="load">
      <van-skeleton v-if="loading && list.length === 0" title :row="4" />
      <van-empty v-else-if="list.length === 0" description="暂无关注店铺" />
      <van-cell-group v-else inset>
        <van-cell
          v-for="item in list"
          :key="item.shopId"
          :title="item.shopName"
          :label="item.description || '优质商家'"
          is-link
          @click="$router.push('/shops/rank')"
        >
          <template #icon>
            <van-image
              round
              width="40"
              height="40"
              fit="cover"
              :src="item.logoUrl || defaultLogo"
              class="logo"
            />
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
import { listFollowing } from '../api/shop'
import { getToken } from '../api/request'

const router = useRouter()
const list = ref([])
const loading = ref(false)
const refreshing = ref(false)
const defaultLogo = 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'

async function load() {
  loading.value = true
  try {
    list.value = await listFollowing()
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
.logo { margin-right: 12px; }
</style>
