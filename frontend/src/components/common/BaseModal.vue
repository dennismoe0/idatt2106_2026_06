<template>
  <Teleport to="body">
    <div
      v-if="modelValue"
      class="modal-backdrop"
      @click.self="$emit('update:modelValue', false)"
      role="dialog"
      :aria-labelledby="titleId"
      aria-modal="true"
    >
      <div class="modal-box">
        <div class="modal-header">
          <h2 :id="titleId" class="modal-title">{{ title }}</h2>
          <button
            class="modal-close"
            @click="$emit('update:modelValue', false)"
            aria-label="Lukk"
          >✕</button>
        </div>
        <div class="modal-body">
          <slot />
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
let _uid = 0
const props = defineProps({
  modelValue: Boolean,
  title: { type: String, required: true }
})
defineEmits(['update:modelValue'])
const titleId = `modal-title-${++_uid}`
</script>

<style scoped>
.modal-backdrop {
  position: fixed; inset: 0;
  background: rgba(0,0,0,0.5);
  display: flex; align-items: center; justify-content: center;
  z-index: 1000;
}
.modal-box {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  width: min(90vw, 480px);
  box-shadow: var(--shadow-lg);
}
.modal-header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: var(--space-4);
}
.modal-title { font-size: var(--text-xl); font-weight: var(--font-bold); }
.modal-close {
  font-size: var(--text-lg);
  color: var(--color-text-muted);
  padding: var(--space-1);
}
</style>
