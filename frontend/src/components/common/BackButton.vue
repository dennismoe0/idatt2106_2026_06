<template>
  <button type="button" class="back-btn" @click="router.back()" :aria-label="label">
    ← {{ label }}
  </button>
</template>

<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
defineProps({
  label: { type: String, default: 'Tilbake' }
})
</script>

<style scoped>
.back-btn {
  color: var(--color-primary);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-md);
  transition: background-color var(--transition-fast);
}
.back-btn:hover { background: var(--color-primary-light); }
</style>
