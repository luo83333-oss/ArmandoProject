<template>
  <div class="page">
    <van-nav-bar title="我的消息" left-arrow @click-left="$router.back()">
      <template #right>
        <span v-if="list.length" class="read-all" @click="onReadAll">全部已读</span>
      </template>
    </van-nav-bar>
    <van-pull-refresh v-model="refreshing" @refresh="load">
      <van-skeleton v-if="loading && list.length === 0" title :row="5" />
      <van-empty v-else-if="list.length === 0" description="暂无消息" />
      <van-cell-group v-else inset>
        <van-cell
          v-for="item in list"
          :key="item.id"
          :title="item.title"
          :label="formatTime(item.createdAt)"
          is-link
          :class="{ unread: !item.read }"
          @click="openMessage(item)"
        >
          <template #value>
            <van-tag v-if="!item.read" type="danger" plain>未读</van-tag>
          </template>
          <div class="preview">{{ item.content }}</div>
        </van-cell>
      </van-cell-group>
    </van-pull-refresh>

    <van-popup v-model:show="showDetail" position="bottom" round :style="{ padding: '16px 16px 24px', maxHeight: '70%' }">
      <div v-if="current" class="detail">
        <div class="detail-title">{{ current.title }}</div>
        <div class="detail-time">{{ formatTime(current.createdAt) }}</div>
        <div class="detail-content">{{ current.content }}</div>
        <van-button v-if="current.orderId" block type="primary" class="mt" @click="goOrder(current.orderId)">
          查看订单
        </van-button>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showFailToast, showSuccessToast } from 'vant'
import { listMessages, markMessageRead, markAllMessagesRead } from '../api/message'
import { getToken } from '../api/request'

const router = useRouter()
const list = ref([])
const loading = ref(false)
const refreshing = ref(false)
const showDetail = ref(false)
const current = ref(null)

function formatTime(value) {
  if (!value) return ''
  return String(value).replace('T', ' ').slice(0, 16)
}

async function load() {
  loading.value = true
  try {
    list.value = await listMessages()
  } catch (e) {
    showFailToast(e.message)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

async function openMessage(item) {
  current.value = item
  showDetail.value = true
  if (!item.read) {
    try {
      const updated = await markMessageRead(item.id)
      item.read = updated.read
    } catch (e) {
      showFailToast(e.message)
    }
  }
}

async function onReadAll() {
  try {
    await markAllMessagesRead()
    for (const item of list.value) {
      item.read = true
    }
    showSuccessToast('已全部标为已读')
  } catch (e) {
    showFailToast(e.message)
  }
}

function goOrder(orderId) {
  showDetail.value = false
  router.push(`/orders/${orderId}`)
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
.read-all { font-size: 13px; color: #1989fa; }
.preview {
  margin-top: 4px;
  font-size: 13px;
  color: #969799;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.unread :deep(.van-cell__title) { font-weight: 600; }
.detail-title { font-size: 18px; font-weight: 600; text-align: center; }
.detail-time { font-size: 12px; color: #969799; text-align: center; margin: 8px 0 16px; }
.detail-content { line-height: 1.7; white-space: pre-wrap; }
.mt { margin-top: 16px; }
</style>
