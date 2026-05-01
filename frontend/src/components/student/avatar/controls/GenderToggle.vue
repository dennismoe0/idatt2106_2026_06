<template>
  <div class="gender-toggle" role="group" aria-label="Velg kjønn">
    <button
      v-for="opt in OPTIONS"
      :key="opt.value"
      class="gender-btn"
      :class="{ 'gender-btn--active': modelValue === opt.value }"
      @click="$emit('update:modelValue', opt.value)"
      :aria-pressed="modelValue === opt.value"
    >
      {{ opt.label }}
    </button>
  </div>
</template>

<script setup>
defineProps({ modelValue: { type: String, required: true } })
defineEmits(['update:modelValue'])
const OPTIONS = [
  { value: 'female',  label: '♀ Jente' },
  { value: 'male',    label: '♂ Gutt' },
  { value: 'neutral', label: '⚧ Nøytral' },
]
</script>

<style scoped>
.gender-toggle { display: flex; gap: var(--space-2); }
.gender-btn {
  flex: 1; padding: var(--space-2) var(--space-3);
  border: 2px solid var(--color-border); border-radius: var(--radius-md);
  background: var(--color-surface); color: var(--color-text-muted);
  font-size: var(--text-sm); font-weight: var(--font-semibold); cursor: pointer;
  transition: all var(--transition-fast);
}
.gender-btn--active {
  border-color: var(--color-primary); background: var(--color-primary-soft);
  color: var(--color-primary);
}
</style>
