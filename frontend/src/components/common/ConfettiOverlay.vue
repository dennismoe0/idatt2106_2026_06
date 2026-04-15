<template>
  <Transition name="fade">
    <div v-if="visible" class="confetti-overlay" aria-hidden="true">
      <div
        v-for="i in 40"
        :key="i"
        class="confetti-piece"
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

const COLORS = ['#ED8936', '#2B6CB0', '#38A169', '#E53E3E', '#ECC94B', '#9F7AEA']

function pieceStyle(i) {
  return {
    left:              `${(i * 37 + 11) % 100}%`,
    animationDelay:    `${((i * 0.09) % 0.8).toFixed(2)}s`,
    animationDuration: `${(0.8 + (i % 5) * 0.2).toFixed(1)}s`,
    backgroundColor:   COLORS[i % COLORS.length],
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

@keyframes confetti-fall {
  0%   { transform: translateY(0)     rotate(0deg);   opacity: 1; }
  100% { transform: translateY(100vh) rotate(720deg); opacity: 0; }
}

.fade-enter-active, .fade-leave-active { transition: opacity var(--transition-normal); }
.fade-enter-from, .fade-leave-to       { opacity: 0; }
</style>
