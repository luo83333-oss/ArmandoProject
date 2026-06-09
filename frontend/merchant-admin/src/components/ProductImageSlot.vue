<template>
  <div class="image-slot" :class="size">
    <el-upload
      class="uploader"
      drag
      :show-file-list="false"
      accept="image/jpeg,image/png,image/webp"
      :http-request="onUpload"
    >
      <div v-if="url" class="preview" @click.stop>
        <img :src="url" alt="" />
        <div class="actions">
          <el-button size="small" @click.stop="onRemove">删除</el-button>
        </div>
      </div>
      <div v-else class="placeholder">
        <div class="icon">+</div>
        <div class="text">{{ label }}</div>
      </div>
    </el-upload>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { uploadImage } from '../api/upload'

const props = defineProps({
  url: { type: String, default: '' },
  label: { type: String, default: '上传图片' },
  size: { type: String, default: 'main' }
})

const emit = defineEmits(['update:url'])

async function onUpload(options) {
  try {
    const url = await uploadImage(options.file)
    emit('update:url', url)
    options.onSuccess({ url })
    ElMessage.success('上传成功')
  } catch (e) {
    options.onError(e)
    ElMessage.error(e?.message || '上传失败')
  }
}

function onRemove() {
  emit('update:url', '')
}
</script>

<style scoped>
.image-slot.main :deep(.el-upload-dragger) { width: 200px; height: 200px; padding: 0; }
.image-slot.aux :deep(.el-upload-dragger) { width: 96px; height: 96px; padding: 0; }
.uploader { width: 100%; height: 100%; }
.preview { position: relative; width: 100%; height: 100%; }
.preview img { width: 100%; height: 100%; object-fit: cover; border-radius: 6px; }
.actions {
  position: absolute; left: 0; right: 0; bottom: 0;
  display: flex; justify-content: center; padding: 6px;
  background: rgba(0, 0, 0, 0.45);
}
.placeholder {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  height: 100%; color: #909399; padding: 8px; box-sizing: border-box;
}
.icon { font-size: 28px; margin-bottom: 6px; color: #c0c4cc; }
.text { font-size: 12px; line-height: 1.4; text-align: center; }
.image-slot.aux .icon { font-size: 20px; }
.image-slot.aux .text { font-size: 11px; }
</style>
