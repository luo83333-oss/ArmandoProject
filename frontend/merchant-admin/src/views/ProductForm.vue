<template>
  <div class="layout">
    <h2>{{ isEdit ? '编辑商品' : '发布商品' }}</h2>
    <el-card>
      <el-form :model="form" label-width="100px" @submit.prevent="onSubmit">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="类目" required>
          <el-select v-model="form.categoryId" placeholder="选择类目" style="width:100%">
            <el-option v-for="c in flatCategories" :key="c.id" :label="c.label" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="主图 URL">
          <el-input v-model="form.mainImageUrl" placeholder="图片链接" />
        </el-form-item>
        <el-form-item label="详情">
          <el-input v-model="form.detailHtml" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="SKU 规格" required>
          <div v-for="(sku, idx) in form.skus" :key="idx" class="sku-row">
            <el-input v-model="sku.specJson" placeholder='规格 JSON，如 {"颜色":"红"}' style="width:200px" />
            <el-input-number v-model="sku.price" :min="0.01" :step="0.01" :precision="2" placeholder="价格" />
            <el-input-number v-model="sku.stock" :min="0" :step="1" placeholder="库存" />
            <el-button v-if="form.skus.length > 1" type="danger" link @click="form.skus.splice(idx, 1)">删除</el-button>
          </div>
          <el-button type="primary" link @click="form.skus.push({ specJson: '', price: 1, stock: 0 })">+ 添加 SKU</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" native-type="submit" :loading="loading">保存</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { listCategories, getProduct, createProduct, updateProduct } from '../api/product'
import { getToken } from '../api/request'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const categories = ref([])
const form = ref({
  title: '',
  categoryId: null,
  mainImageUrl: '',
  detailHtml: '',
  skus: [{ specJson: '{"默认":"标准"}', price: 9.9, stock: 100 }]
})

const isEdit = computed(() => !!route.params.id && route.path.includes('/edit'))
const flatCategories = computed(() => {
  const result = []
  function walk(nodes, prefix = '') {
    for (const n of nodes) {
      const label = prefix ? `${prefix} / ${n.name}` : n.name
      if (!n.children || n.children.length === 0) {
        result.push({ id: n.id, label })
      } else {
        walk(n.children, label)
      }
    }
  }
  walk(categories.value)
  return result
})

async function loadCategories() {
  categories.value = await listCategories()
}

async function loadProduct() {
  if (!isEdit.value) return
  const data = await getProduct(route.params.id)
  form.value = {
    title: data.title,
    categoryId: data.categoryId,
    mainImageUrl: data.mainImageUrl || '',
    detailHtml: data.detailHtml || '',
    skus: data.skus.map(s => ({
      specJson: s.specJson,
      price: Number(s.price),
      stock: s.stock,
      skuCode: s.skuCode
    }))
  }
}

async function onSubmit() {
  if (!form.value.title || !form.value.categoryId || !form.value.skus.length) {
    ElMessage.warning('请填写完整信息')
    return
  }
  loading.value = true
  try {
    if (isEdit.value) {
      await updateProduct(route.params.id, form.value)
      ElMessage.success('已更新')
    } else {
      await createProduct(form.value)
      ElMessage.success('已创建')
    }
    router.replace('/products')
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  if (!getToken()) {
    router.replace('/login')
    return
  }
  try {
    await loadCategories()
    await loadProduct()
  } catch (e) {
    ElMessage.error(e.message)
  }
})
</script>

<style scoped>
.layout { padding: 24px; max-width: 900px; margin: 0 auto; }
.sku-row { display: flex; gap: 8px; align-items: center; margin-bottom: 8px; flex-wrap: wrap; }
</style>
