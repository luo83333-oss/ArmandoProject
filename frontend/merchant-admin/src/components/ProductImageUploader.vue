<template>
  <div class="gallery-uploader">
    <div class="section">
      <div class="section-label">主图 <span class="required">*</span></div>
      <div class="slot main-slot">
        <ImageSlot
          :url="mainImageUrl"
          label="拖拽或点击上传主图"
          @update:url="emit('update:mainImageUrl', $event)"
        />
      </div>
    </div>
    <div class="section">
      <div class="section-label">辅图（最多 5 张，可选）</div>
      <div class="aux-grid">
        <ImageSlot
          v-for="(_, index) in auxSlots"
          :key="index"
          :url="auxSlots[index]"
          :label="`辅图 ${index + 1}`"
          size="aux"
          @update:url="onAuxChange(index, $event)"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import ImageSlot from './ProductImageSlot.vue'

const props = defineProps({
  mainImageUrl: { type: String, default: '' },
  galleryUrls: { type: Array, default: () => [] }
})

const emit = defineEmits(['update:mainImageUrl', 'update:galleryUrls'])

const auxSlots = computed(() => {
  const slots = [...props.galleryUrls]
  while (slots.length < 5) slots.push('')
  return slots.slice(0, 5)
})

function onAuxChange(index, url) {
  const next = [...auxSlots.value]
  next[index] = url || ''
  emit('update:galleryUrls', next.filter(Boolean))
}
</script>

<style scoped>
.gallery-uploader { display: flex; flex-direction: column; gap: 16px; }
.section-label { font-size: 14px; color: #606266; margin-bottom: 8px; }
.required { color: #f56c6c; }
.main-slot { width: 200px; }
.aux-grid { display: flex; flex-wrap: wrap; gap: 12px; }
</style>
