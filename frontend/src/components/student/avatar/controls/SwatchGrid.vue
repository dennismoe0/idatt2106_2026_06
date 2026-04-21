<template>
  <div class="swatch-grid">
    <button
      v-for="hex in swatches"
      :key="hex"
      class="swatch"
      :class="{ 'swatch--selected': modelValue === hex, 'swatch--circle': shape === 'circle', 'swatch--square': shape === 'square' }"
      :style="{ background: hex }"
      :disabled="locked.includes(hex)"
      :title="locked.includes(hex) ? 'Låst — bruk stjerner for å låse opp' : hex"
      @click="!locked.includes(hex) && $emit('update:modelValue', hex)"
      :aria-label="hex"
      :aria-pressed="modelValue === hex"
    >
      <span v-if="locked.includes(hex)" class="swatch__lock">🔒</span>
    </button>
  </div>
</template>

<script setup>
defineProps({
  modelValue: { type: String, required: true },
  swatches:   { type: Array,  required: true },
  shape:      { type: String, default: 'circle' },
  locked:     { type: Array,  default: () => [] },
})
defineEmits(['update:modelValue'])
</script>

<style scoped>
.swatch-grid { display: flex; flex-wrap: wrap; gap: var(--space-2); }
.swatch {
  width: 32px; height: 32px;
  border: 2px solid var(--color-border);
  cursor: pointer; padding: 0; position: relative;
  transition: border-color var(--transition-fast), transform var(--transition-fast);
}
.swatch--circle { border-radius: 50%; }
.swatch--square { border-radius: var(--radius-sm); }
.swatch--selected { border-color: var(--color-primary); border-width: 3px; transform: scale(1.1); }
.swatch:disabled { cursor: not-allowed; opacity: 0.55; }
.swatch__lock { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; font-size: 12px; }
</style>
