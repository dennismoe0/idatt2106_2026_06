<template>
  <button
    class="world-node"
    :class="[stateClass, { 'world-node--shake': isShaking }]"
    :aria-disabled="stop.locked ? 'true' : undefined"
    :tabindex="stop.locked ? -1 : 0"
    @click="handleClick"
    @keydown.enter="handleClick"
    @keydown.space.prevent="handleClick"
  >
    {{ stop.name }}
  </button>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  stop:      { type: Object,  required: true },
  isCurrent: { type: Boolean, default: false },
  isShaking: { type: Boolean, default: false },
})

const emit = defineEmits(['node-click'])

const stateClass = computed(() => {
  if (props.stop.locked)    return 'world-node--locked'
  if (props.stop.completed) return 'world-node--completed'
  return 'world-node--unlocked'
})

function handleClick() {
  if (props.stop.locked) return
  emit('node-click')
}
</script>

<style scoped>
.world-node {
  position: absolute;
  transform: translate(-50%, -50%);
  padding: 8px 18px;
  border-radius: 24px;
  border: 3px solid transparent;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  white-space: nowrap;
  box-shadow: 0 4px 0 rgba(0, 0, 0, 0.5);
  transition: filter 0.15s;
  font-family: inherit;
  line-height: 1.2;
}

.world-node:hover:not([aria-disabled='true']) {
  filter: brightness(1.15);
}

.world-node--unlocked {
  background: #C0392B;
  border-color: #922B21;
  color: #fff;
}

.world-node--completed {
  background: #2471A3;
  border-color: #1A5276;
  color: #fff;
}

.world-node--locked {
  background: #555;
  border-color: #333;
  color: #aaa;
  cursor: default;
}

@keyframes world-node-shake {
  0%, 100% { transform: translate(-50%, -50%); }
  20%       { transform: translate(calc(-50% + 7px), -50%); }
  40%       { transform: translate(calc(-50% - 7px), -50%); }
  60%       { transform: translate(calc(-50% + 4px), -50%); }
  80%       { transform: translate(calc(-50% - 2px), -50%); }
}

.world-node--shake {
  animation: world-node-shake 0.4s ease-in-out;
}

.world-node:focus-visible {
  outline: 3px solid #fff;
  outline-offset: 3px;
}
</style>
