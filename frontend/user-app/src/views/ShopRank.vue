<template>
  <div class="page">
    <van-nav-bar title="热门店铺榜" left-arrow @click-left="$router.back()" />
    <van-pull-refresh v-model="refreshing" @refresh="load">
      <van-skeleton v-if="loading && list.length === 0" title :row="5" />
      <van-empty v-else-if="list.length === 0" description="暂无排行，请稍后重试" />
      <van-cell-group v-else inset>
        <van-cell v-for="item in list" :key="item.shopId" :title="`${item.rank}. ${item.shopName}`" :label="item.description || '优质商家'">
          <template #value>
            <div class="cell-value">
              <van-tag type="danger">热度 {{ item.weightScore }}</van-tag>
              <van-button
                v-if="getToken()"
                size="mini"
                :type="followMap[item.shopId] ? 'default' : 'primary'"
                plain
                class="follow-btn"
                @click.stop="onToggleFollow(item.shopId)"
              >
                {{ followMap[item.shopId] ? '已关注' : '关注' }}
              </van-button>
            </div>
          </template>
        </van-cell>
      </van-cell-group>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { showFailToast, showSuccessToast } from 'vant'
import { listShopRank, getFollowStatus, followShop, unfollowShop } from '../api/shop'
import { getToken } from '../api/request'

const list = ref([])
const loading = ref(false)
const refreshing = ref(false)
const followMap = reactive({})

async function loadFollowStatuses() {
  if (!getToken()) return
  await Promise.all(
    list.value.map(async (item) => {
      try {
        const status = await getFollowStatus(item.shopId)
        followMap[item.shopId] = !!status.followed
      } catch {
        followMap[item.shopId] = false
      }
    })
  )
}

async function onToggleFollow(shopId) {
  if (!getToken()) return
  try {
    if (followMap[shopId]) {
      await unfollowShop(shopId)
      followMap[shopId] = false
      showSuccessToast('已取消关注')
    } else {
      await followShop(shopId)
      followMap[shopId] = true
      showSuccessToast('已关注')
    }
  } catch (e) {
    showFailToast(e.message)
  }
}

async function load() {
  loading.value = true
  try {
    list.value = await listShopRank(20)
    await loadFollowStatuses()
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
.cell-value { display: flex; flex-direction: column; align-items: flex-end; gap: 6px; }
.follow-btn { min-width: 56px; }
</style>
