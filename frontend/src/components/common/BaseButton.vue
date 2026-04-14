<template>
  <button
    :type="type"
    :disabled="disabled || loading"
    :class="['btn', `btn--${variant}`, { 'btn--loading': loading }]"
    v-bind="$attrs"
  >
    <LoadingSpinner v-if="loading" size="sm" />
    <slot v-else />
  </button>
</template>

<script setup>
import LoadingSpinner from './LoadingSpinner.vue'

defineProps({
  variant: { type: String, default: 'primary' }, // primary | secondary | danger
  type:    { type: String, default: 'button' },
  disabled: { type: Boolean, default: false },
  loading:  { type: Boolean, default: false }
})
</script>

<style scoped>
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-6);
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
  border-radius: var(--radius-md);
  transition: background-color var(--transition-fast), transform var(--transition-fast);
  min-height: 44px; /* WCAG touch target */
}
.btn:active:not(:disabled) { transform: scale(0.98); }
.btn:disabled { opacity: 0.5; cursor: not-allowed; }

.btn--primary {
  background: var(--color-btn-primary-bg);
  color: var(--color-btn-primary-fg);
}
.btn--primary:hover:not(:disabled) { background: var(--color-btn-primary-hover); }

.btn--secondary {
  background: transparent;
  color: var(--color-primary);
  border: 2px solid var(--color-primary);
}
.btn--secondary:hover:not(:disabled) { background: var(--color-primary-light); }

.btn--danger {
  background: var(--color-danger);
  color: var(--color-text-on-dark);
}
.btn--danger:hover:not(:disabled) { background: color-mix(in srgb, var(--color-danger) 80%, black); }
</style>
