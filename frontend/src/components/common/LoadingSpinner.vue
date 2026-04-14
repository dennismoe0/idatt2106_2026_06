<template>
  <span :class="['spinner', `spinner--${size}`]" role="status" aria-label="Laster...">
    <span class="sr-only">Laster...</span>
  </span>
</template>

<script setup>
defineProps({
  size: { type: String, default: 'md' } // sm | md | lg
})
</script>

<style scoped>
.spinner {
  display: inline-block;
  border: 3px solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: var(--radius-full);
  animation: spin 0.7s linear infinite;
}
.spinner--sm { width: 16px; height: 16px; }
.spinner--md { width: 32px; height: 32px; }
.spinner--lg { width: 48px; height: 48px; }

@keyframes spin { to { transform: rotate(360deg); } }
</style>
