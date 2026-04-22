<template>
  <div class="shape-grid">
    <!-- Normal (available) tiles -->
    <div
      v-for="variant in variants"
      :key="variant"
      class="shape-tile-wrap"
    >
      <button
        class="shape-tile"
        :class="{
          'shape-tile--selected': modelValue === variant,
          'shape-tile--disabled': locked.includes(variant),
        }"
        :disabled="locked.includes(variant)"
        :title="locked.includes(variant) ? 'Låst' : variant"
        @click="!locked.includes(variant) && $emit('update:modelValue', variant)"
        :aria-pressed="modelValue === variant"
      >
        <div class="shape-tile__preview">
          <component
            :is="previewComponent"
            v-bind="{ ...previewProps, [variantProp]: variant }"
            style="width:100%;height:100%"
          />
        </div>
        <span v-if="locked.includes(variant)" class="shape-tile__lock">🔒</span>
      </button>
    </div>

    <!-- Medal-locked tiles (grayed, shake + tooltip on click) -->
    <div
      v-for="item in medalLocked"
      :key="'medal-' + item.value"
      class="shape-tile-wrap"
    >
      <button
        class="shape-tile shape-tile--medal-locked"
        :class="{ 'shape-tile--shaking': shakingValue === item.value }"
        @click="handleMedalClick(item)"
        :aria-label="item.value + ' (låst)'"
      >
        <div class="shape-tile__preview">
          <component
            :is="previewComponent"
            v-bind="{ ...previewProps, [variantProp]: item.value }"
            style="width:100%;height:100%;filter:grayscale(70%);opacity:0.5"
          />
        </div>
        <span class="shape-tile__medal-badge">🏅</span>
      </button>

      <div
        class="shape-bubble"
        :class="{ 'shape-bubble--visible': bubbleValue === item.value }"
        role="tooltip"
        aria-live="polite"
      >
        <span class="shape-bubble__line1">Lås opp ved å fullføre</span>
        <br>
        <span class="shape-bubble__line2">{{ item.stopName }}</span>
        <span class="shape-bubble__arrow"></span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onUnmounted } from 'vue'

defineProps({
  modelValue:       { type: String,  required: true },
  variants:         { type: Array,   required: true },
  previewComponent: { type: Object,  required: true },
  previewProps:     { type: Object,  default: () => ({}) },
  variantProp:      { type: String,  required: true },
  locked:           { type: Array,   default: () => [] },
  medalLocked:      { type: Array,   default: () => [] }, // Array<{ value: string, stopName: string }>
})
defineEmits(['update:modelValue'])

const shakingValue = ref(null)
const bubbleValue = ref(null)
let dismissTimer = null

function handleMedalClick(item) {
  clearTimeout(dismissTimer)
  shakingValue.value = item.value
  bubbleValue.value = item.value
  dismissTimer = setTimeout(() => {
    shakingValue.value = null
    bubbleValue.value = null
  }, 2200)
}

onUnmounted(() => clearTimeout(dismissTimer))
</script>

<style scoped>
.shape-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(44px, 1fr)); gap: var(--space-2); }

.shape-tile-wrap { position: relative; }

.shape-tile {
  width: 100%;
  aspect-ratio: 3/4; background: var(--color-surface);
  border: 2px solid var(--color-border); border-radius: var(--radius-md);
  cursor: pointer; padding: 0; position: relative; overflow: hidden;
  transition: border-color var(--transition-fast);
}
.shape-tile--selected { border-color: var(--color-primary); border-width: 3px; }
.shape-tile:disabled { cursor: not-allowed; opacity: 0.5; }
.shape-tile__preview { width: 100%; height: 100%; position: relative; }
.shape-tile__lock { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; font-size: 14px; background: rgba(0,0,0,.35); border-radius: inherit; }

.shape-tile--medal-locked {
  opacity: 0.4;
  filter: grayscale(50%);
  cursor: pointer;
}
.shape-tile__medal-badge { position: absolute; top: 3px; right: 3px; font-size: 11px; line-height: 1; }

.shape-tile--shaking {
  animation: tile-shake 0.45s ease;
  border-color: var(--color-danger, #ef4444) !important;
}

@keyframes tile-shake {
  0%, 100% { transform: translateX(0); }
  15% { transform: translateX(-5px); }
  30% { transform: translateX(5px); }
  45% { transform: translateX(-4px); }
  60% { transform: translateX(4px); }
  75% { transform: translateX(-2px); }
  90% { transform: translateX(2px); }
}

.shape-bubble {
  display: none;
  position: absolute;
  bottom: calc(100% + 8px);
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
.shape-bubble--visible { display: block; }
.shape-bubble__line1 { color: var(--color-text-muted, #94a3b8); }
.shape-bubble__line2 { font-weight: 700; color: var(--color-warning, #f59e0b); }
.shape-bubble__arrow {
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  border: 6px solid transparent;
  border-top-color: var(--color-danger, #ef4444);
}
</style>
