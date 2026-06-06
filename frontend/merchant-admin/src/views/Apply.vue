<template>
  <el-card>
    <template #header>商家入驻申请</template>
    <el-form :model="form" label-width="100px" @submit.prevent="onSubmit">
      <el-form-item label="公司名称" required>
        <el-input v-model="form.companyName" />
      </el-form-item>
      <el-form-item label="联系人" required>
        <el-input v-model="form.contactName" />
      </el-form-item>
      <el-form-item label="联系电话" required>
        <el-input v-model="form.contactPhone" />
      </el-form-item>
      <el-form-item label="营业执照号">
        <el-input v-model="form.licenseNo" />
      </el-form-item>
      <el-form-item label="执照图片URL">
        <el-input v-model="form.licenseFileUrl" placeholder="可填占位链接" />
      </el-form-item>
      <el-button type="primary" native-type="submit" :loading="loading">提交申请</el-button>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { apply } from '../api/merchant'

const router = useRouter()
const loading = ref(false)
const form = ref({
  companyName: '',
  contactName: '',
  contactPhone: '',
  licenseNo: '',
  licenseFileUrl: ''
})

async function onSubmit() {
  loading.value = true
  try {
    await apply(form.value)
    ElMessage.success('入驻申请已提交，等待平台审核')
    router.replace('/')
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}
</script>
