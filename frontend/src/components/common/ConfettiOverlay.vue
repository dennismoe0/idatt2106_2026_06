<template>
  <Transition name="fade">
    <div v-if="visible" class="confetti-overlay" aria-hidden="true">
      <div
        v-for="i in 40"
        :key="i"
        :class="['confetti-piece', colorClass(i)]"
        :style="pieceStyle(i)"
      />
    </div>
  </Transition>
</template>

<script setup>
import { ref, watch, onBeforeUnmount } from 'vue'

const props = defineProps({
  active: { type: Boolean, required: true }
})

const visible = ref(props.active)
let timer = null

watch(() => props.active, (val) => {
  if (val) {
    visible.value = true
    clearTimeout(timer)
    timer = setTimeout(() => { visible.value = false }, 3000)
  }
}, { immediate: true })

onBeforeUnmount(() => clearTimeout(timer))

const COLOR_COUNT = 6

function colorClass(i) {
  return `confetti-piece--color-${(i % COLOR_COUNT) + 1}`
}

function pieceStyle(i) {
  return {
    left:              `${(i * 37 + 11) % 100}%`,
    animationDelay:    `${((i * 0.09) % 0.8).toFixed(2)}s`,
    animationDuration: `${(0.8 + (i % 5) * 0.2).toFixed(1)}s`,
    width:             `${6 + (i % 4) * 2}px`,
    height:            `${8 + (i % 3) * 3}px`,
  }
}
</script>

<style scoped>
.confetti-overlay {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 9999;
  overflow: hidden;
}

.confetti-piece {
  position: absolute;
  top: -10px;
  border-radius: 2px;
  animation: confetti-fall linear forwards;
}

.confetti-piece--color-1 { background-color: var(--color-confetti-1); }
.confetti-piece--color-2 { background-color: var(--color-confetti-2); }
.confetti-piece--color-3 { background-color: var(--color-confetti-3); }
.confetti-piece--color-4 { background-color: var(--color-confetti-4); }
.confetti-piece--color-5 { background-color: var(--color-confetti-5); }
.confetti-piece--color-6 { background-color: var(--color-confetti-6); }

@keyframes confetti-fall {
  0%   { transform: translateY(0)     rotate(0deg);   opacity: 1; }
  100% { transform: translateY(100vh) rotate(720deg); opacity: 0; }
}

.fade-enter-active, .fade-leave-active { transition: opacity var(--transition-normal); }
.fade-enter-from, .fade-leave-to       { opacity: 0; }
</style>
