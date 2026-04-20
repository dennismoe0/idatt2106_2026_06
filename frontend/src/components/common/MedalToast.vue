<template>
  <Transition name="slide-up">
    <div v-if="visible" class="medal-toast" role="status" aria-live="polite">
      <span class="medal-toast__icon" aria-hidden="true">🏅</span>
      <div class="medal-toast__body">
        <strong class="medal-toast__name">{{ medal.name }}</strong>
        <p class="medal-toast__desc">{{ medal.description }}</p>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { ref, watch, onBeforeUnmount } from 'vue'

const props = defineProps({
  medal: { type: Object, default: null }
  // Shape: { name: string, description: string } or null
})

const visible = ref(!!props.medal)
let timer = null

watch(() => props.medal, (val) => {
  if (val) {
    visible.value = true
    clearTimeout(timer)
    timer = setTimeout(() => { visible.value = false }, 4000)
  } else {
    visible.value = false
  }
}, { immediate: true })

onBeforeUnmount(() => clearTimeout(timer))
</script>

<style scoped>
.medal-toast {
  position: fixed;
  bottom: var(--space-6);
  right: var(--space-6);
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4) var(--space-6);
  background: var(--color-surface);
  border: 2px solid var(--color-accent);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  z-index: 9998;
  max-width: 20rem;
}

.medal-toast__icon  { font-size: var(--text-3xl); flex-shrink: 0; }
.medal-toast__name  { display: block; font-size: var(--text-base); font-weight: var(--font-bold); color: var(--color-text); }
.medal-toast__desc  { margin: 0; font-size: var(--text-sm); color: var(--color-text-muted); }

.slide-up-enter-active { transition: transform var(--transition-normal), opacity var(--transition-normal); }
.slide-up-leave-active { transition: transform var(--transition-normal), opacity var(--transition-normal); }
.slide-up-enter-from   { transform: translateY(2rem); opacity: 0; }
.slide-up-leave-to     { transform: translateY(2rem); opacity: 0; }
</style>
