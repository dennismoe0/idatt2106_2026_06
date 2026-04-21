<template>
  <div class="shape-grid">
    <button
      v-for="variant in variants"
      :key="variant"
      class="shape-tile"
      :class="{ 'shape-tile--selected': modelValue === variant }"
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
</template>

<script setup>
defineProps({
  modelValue:       { type: String,  required: true },
  variants:         { type: Array,   required: true },
  previewComponent: { type: Object,  required: true },
  previewProps:     { type: Object,  default: () => ({}) },
  variantProp:      { type: String,  required: true },
  locked:           { type: Array,   default: () => [] },
})
defineEmits(['update:modelValue'])
</script>

<style scoped>
.shape-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(44px, 1fr)); gap: var(--space-2); }
.shape-tile {
  aspect-ratio: 3/4; background: var(--color-surface);
  border: 2px solid var(--color-border); border-radius: var(--radius-md);
  cursor: pointer; padding: 0; position: relative; overflow: hidden;
  transition: border-color var(--transition-fast);
}
.shape-tile--selected { border-color: var(--color-primary); border-width: 3px; }
.shape-tile:disabled { cursor: not-allowed; opacity: 0.5; }
.shape-tile__preview { width: 100%; height: 100%; position: relative; }
.shape-tile__lock { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; font-size: 14px; background: rgba(0,0,0,.35); border-radius: inherit; }
</style>
