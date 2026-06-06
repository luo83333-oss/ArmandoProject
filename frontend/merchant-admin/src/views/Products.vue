<template>
  <div class="layout">
    <div class="toolbar">
      <h2>商品管理</h2>
      <div>
        <el-button @click="$router.push('/')">返回工作台</el-button>
        <el-button type="primary" @click="$router.push('/products/new')">发布商品</el-button>
      </div>
    </div>
    <el-card>
      <el-input v-model="keyword" placeholder="搜索商品标题" clearable style="width:240px" @keyup.enter="load" />
      <el-button type="primary" class="ml" @click="load">搜索</el-button>
      <el-table :data="list" v-loading="loading" class="mt" stripe>
        <el-table-column prop="title" label="商品" min-width="180" />
        <el-table-column prop="categoryName" label="类目" width="100" />
        <el-table-column label="价格" width="100">
          <template #default="{ row }">¥{{ row.minPrice }}</template>
        </el-table-column>
        <el-table-column prop="totalStock" label="库存" width="80" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.shelfStatus === 1 ? 'success' : 'info'">
              {{ row.shelfStatus === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="$router.push(`/products/${row.id}/edit`)">编辑</el-button>
            <el-button size="small" :type="row.shelfStatus === 1 ? 'warning' : 'success'" @click="toggleShelf(row)">
              {{ row.shelfStatus === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button size="small" type="danger" @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-if="total > 0"
        class="mt"
        layout="prev, pager, next"
        :total="total"
        :page-size="size"
        :current-page="page"
        @current-change="onPageChange"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listProducts, deleteProduct, updateShelf } from '../api/product'
import { getToken } from '../api/request'

const router = useRouter()
const list = ref([])
const loading = ref(false)
const keyword = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)

async function load() {
  loading.value = true
  try {
    const data = await listProducts({ page: page.value, size: size.value, keyword: keyword.value })
    list.value = data.records
    total.value = data.total
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

async function toggleShelf(row) {
  const next = row.shelfStatus === 1 ? 0 : 1
  try {
    const updated = await updateShelf(row.id, next)
    row.shelfStatus = updated.shelfStatus
    ElMessage.success(next === 1 ? '已上架' : '已下架')
  } catch (e) {
    ElMessage.error(e.message)
  }
}

async function onDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除「${row.title}」？`, '提示', { type: 'warning' })
    await deleteProduct(row.id)
    ElMessage.success('已删除')
    load()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '操作取消')
  }
}

function onPageChange(p) {
  page.value = p
  load()
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
.layout { padding: 24px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.mt { margin-top: 16px; }
.ml { margin-left: 8px; }
</style>
