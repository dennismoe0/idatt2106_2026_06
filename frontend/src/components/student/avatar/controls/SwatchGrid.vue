<template>
  <div class="swatch-grid">
    <div
      v-for="hex in swatches"
      :key="hex"
      class="swatch-wrap"
    >
      <button
        class="swatch"
        :class="{
          'swatch--selected': modelValue === hex && !isMedalLocked(hex),
          'swatch--circle': shape === 'circle',
          'swatch--square': shape === 'square',
          'swatch--locked': locked.includes(hex),
          'swatch--medal-locked': isMedalLocked(hex),
          'swatch--shaking': shakingHex === hex,
        }"
        :style="{ background: hex }"
        :disabled="locked.includes(hex)"
        :title="locked.includes(hex) ? 'Låst — bruk stjerner for å låse opp' : hex"
        @click="handleClick(hex)"
        :aria-label="hex"
        :aria-pressed="modelValue === hex"
      >
        <span v-if="locked.includes(hex)" class="swatch__lock">🔒</span>
        <span v-else-if="isMedalLocked(hex)" class="swatch__medal">🏅</span>
      </button>

      <div
        v-if="isMedalLocked(hex)"
        class="swatch-bubble"
        :class="{ 'swatch-bubble--visible': bubbleHex === hex }"
        role="tooltip"
        aria-live="polite"
      >
        <span class="swatch-bubble__line1">Lås opp ved å fullføre</span>
        <br>
        <span class="swatch-bubble__line2">{{ getMedalStop(hex) }}</span>
        <span class="swatch-bubble__arrow"></span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onUnmounted } from 'vue'

const props = defineProps({
  modelValue: { type: String, required: true },
  swatches:   { type: Array,  required: true },
  shape:      { type: String, default: 'circle' },
  locked:     { type: Array,  default: () => [] },
  medalLocked: { type: Array, default: () => [] }, // Array<{ value: string, stopName: string }>
})
const emit = defineEmits(['update:modelValue'])

const shakingHex = ref(null)
const bubbleHex = ref(null)
let dismissTimer = null

function isMedalLocked(hex) {
  return props.medalLocked.some(m => m.value === hex)
}

function getMedalStop(hex) {
  return props.medalLocked.find(m => m.value === hex)?.stopName ?? ''
}

function handleClick(hex) {
  if (props.locked.includes(hex)) return
  if (isMedalLocked(hex)) {
    clearTimeout(dismissTimer)
    shakingHex.value = hex
    bubbleHex.value = hex
    dismissTimer = setTimeout(() => {
      shakingHex.value = null
      bubbleHex.value = null
    }, 2200)
    return
  }
  emit('update:modelValue', hex)
}

onUnmounted(() => clearTimeout(dismissTimer))
</script>

<style scoped>
.swatch-grid { display: flex; flex-wrap: wrap; gap: var(--space-2); }

.swatch-wrap { position: relative; display: inline-block; }

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
.swatch__medal { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; font-size: 14px; }

.swatch--medal-locked {
  opacity: 0.4;
  filter: grayscale(60%);
  cursor: pointer;
}
.swatch--shaking {
  animation: swatch-shake 0.45s ease;
  border-color: var(--color-danger, #ef4444) !important;
  border-width: 2px !important;
}

@keyframes swatch-shake {
  0%, 100% { transform: translateX(0); }
  15% { transform: translateX(-5px); }
  30% { transform: translateX(5px); }
  45% { transform: translateX(-4px); }
  60% { transform: translateX(4px); }
  75% { transform: translateX(-2px); }
  90% { transform: translateX(2px); }
}

.swatch-bubble {
  display: none;
  position: absolute;
  bottom: calc(100% + 10px);
  left: 50%;
  transform: translateX(-50%);
  background: var(--color-surface-alt, #1e293b);
  border: 1px solid var(--color-danger, #ef4444);
  border-radius: var(--radius-md, 8px);
  padding: 8px 12px;
  font-size: 11px;
  white-space: nowrap;
  z-index: 20;
  pointer-events: none;
  box-shadow: 0 4px 12px rgba(0,0,0,.4);
}
.swatch-bubble--visible { display: block; }
.swatch-bubble__line1 { color: var(--color-text-muted, #94a3b8); }
.swatch-bubble__line2 { font-weight: 700; color: var(--color-warning, #f59e0b); }
.swatch-bubble__arrow {
  content: '';
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  border: 6px solid transparent;
  border-top-color: var(--color-danger, #ef4444);
}
</style>
