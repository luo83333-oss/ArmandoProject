<template>
  <div class="gallery-wrap">
    <van-swipe
      v-if="displayImages.length"
      class="gallery-swipe"
      :autoplay="displayImages.length > 1 ? 3000 : 0"
      :loop="displayImages.length > 1"
      :show-indicators="displayImages.length > 1"
      indicator-color="#fff"
      @change="onSwipeChange"
    >
      <van-swipe-item v-for="(img, i) in displayImages" :key="i">
        <van-image
          class="gallery-image"
          width="100%"
          height="100%"
          fit="cover"
          :src="img"
          @click="openPreview(i)"
        />
      </van-swipe-item>
    </van-swipe>
    <van-image
      v-else
      class="gallery-image"
      width="100%"
      height="100%"
      fit="cover"
      :src="placeholder"
    />
    <span v-if="displayImages.length > 1" class="counter">
      {{ currentIndex + 1 }}/{{ displayImages.length }}
    </span>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { showImagePreview } from 'vant'

const props = defineProps({
  images: {
    type: Array,
    default: () => []
  }
})

const placeholder = 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'
const currentIndex = ref(0)

const displayImages = computed(() =>
  (props.images || []).filter(url => !!url)
)

function onSwipeChange(index) {
  currentIndex.value = index
}

function openPreview(index) {
  const images = displayImages.value
  if (!images.length) return
  showImagePreview({
    images,
    startPosition: index,
    closeable: true,
    showIndex: true,
    closeOnClickOverlay: true
  })
}
</script>

<style scoped>
.gallery-wrap {
  position: relative;
  background: #fff;
  aspect-ratio: 1;
  max-height: 75vw;
  overflow: hidden;
}

.gallery-swipe,
.gallery-wrap :deep(.van-swipe),
.gallery-wrap :deep(.van-swipe-item) {
  height: 100%;
}

.gallery-image {
  display: block;
  width: 100%;
  height: 100%;
  cursor: pointer;
}

.counter {
  position: absolute;
  right: 12px;
  bottom: 12px;
  padding: 2px 8px;
  font-size: 12px;
  color: #fff;
  background: rgba(0, 0, 0, 0.45);
  border-radius: 10px;
  pointer-events: none;
  z-index: 1;
}
</style>
