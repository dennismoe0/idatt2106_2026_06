<template>
  <button
    class="world-node"
    :class="[stateClass, { 'world-node--current': showPulse, 'world-node--shake': isShaking }]"
    :aria-disabled="stop.locked ? 'true' : undefined"
    :aria-label="ariaLabel"
    :title="stop.name"
    :tabindex="stop.locked ? -1 : 0"
    @click="handleClick"
    @keydown.enter="handleClick"
    @keydown.space.prevent="handleClick"
  >
    <span v-if="showPulse" class="world-node__halo" aria-hidden="true"></span>
    <span class="world-node__disk" aria-hidden="true">
      <span class="world-node__icon">{{ themeIcon }}</span>
      <span
        v-if="stop.completed"
        class="world-node__badge world-node__badge--done"
        aria-hidden="true"
      >✓</span>
      <span
        v-if="stop.locked"
        class="world-node__badge world-node__badge--lock"
        aria-hidden="true"
      >🔒</span>
    </span>
    <span class="world-node__label">{{ stop.name }}</span>
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

const THEME_ICONS = {
  FAKE_NEWS:      '📰',
  PHISHING_EMAIL: '📧',
  AI_PHOTO:       '📸',
  PASSWORD:       '🔑',
  MARKETPLACE:    '🛒',
  SOCIAL_MEDIA:   '💬',
  FINAL_BOSS:     '🏆',
}

const stateClass = computed(() => {
  if (props.stop.locked)    return 'world-node--locked'
  if (props.stop.completed) return 'world-node--completed'
  return 'world-node--unlocked'
})

const themeIcon = computed(() => THEME_ICONS[props.stop.theme] ?? '📍')

const showPulse = computed(() => props.isCurrent && !props.stop.locked)

const ariaLabel = computed(() => {
  if (props.stop.locked)    return `${props.stop.name} (låst)`
  if (props.stop.completed) return `${props.stop.name} (fullført)`
  if (props.isCurrent)      return `${props.stop.name} (du er her)`
  return props.stop.name
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
  background: transparent;
  border: none;
  padding: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  font-family: inherit;
  line-height: 1.2;
  transition: transform 0.18s ease-out;
  z-index: 1;
}

.world-node:hover:not([aria-disabled='true']) {
  transform: translate(-50%, -50%) scale(1.08);
  z-index: 5;
}

.world-node:hover:not([aria-disabled='true']) .world-node__label {
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.45);
}

.world-node:focus-visible {
  outline: none;
}
.world-node:focus-visible .world-node__disk {
  outline: 3px solid #fff;
  outline-offset: 4px;
}

.world-node__disk {
  position: relative;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow:
    inset 0 -6px 0 rgba(0, 0, 0, 0.28),
    inset 0 3px 0 rgba(255, 255, 255, 0.35),
    0 5px 0 rgba(0, 0, 0, 0.45),
    0 6px 10px rgba(0, 0, 0, 0.35);
  border: 3px solid #fff;
  transition: box-shadow 0.18s ease-out, background 0.18s ease-out;
}

.world-node__icon {
  font-size: 26px;
  line-height: 1;
  filter: drop-shadow(0 1px 1px rgba(0, 0, 0, 0.35));
}

.world-node__label {
  background: #FFF5D6;
  color: #3B1F08;
  padding: 3px 10px;
  border-radius: 10px;
  border: 2px solid #8C5A20;
  font-size: 12px;
  font-weight: 800;
  white-space: nowrap;
  box-shadow: 0 3px 6px rgba(0, 0, 0, 0.35);
  transition: box-shadow 0.18s ease-out, transform 0.18s ease-out;
  letter-spacing: 0.01em;
}

.world-node__badge {
  position: absolute;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 900;
  line-height: 1;
  border: 2px solid #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.45);
  top: -2px;
  right: -2px;
  pointer-events: none;
}
.world-node__badge--done {
  background: #2E7D32;
  color: #fff;
}
.world-node__badge--lock {
  background: #4A4A4A;
  color: #FFD54F;
  font-size: 11px;
}

.world-node--unlocked .world-node__disk {
  background: radial-gradient(circle at 35% 30%, #F06A4E 0%, #C0392B 60%, #922B21 100%);
}

.world-node--completed .world-node__disk {
  background: radial-gradient(circle at 35% 30%, #5DADE2 0%, #2471A3 60%, #1A5276 100%);
}

.world-node--locked {
  cursor: default;
}
.world-node--locked .world-node__disk {
  background: radial-gradient(circle at 35% 30%, #888 0%, #555 60%, #333 100%);
  filter: grayscale(60%);
}
.world-node--locked .world-node__label {
  background: #D7CBB6;
  color: #5A4A36;
  border-color: #7A6B52;
  opacity: 0.85;
}
.world-node--locked {
  opacity: 0.85;
}

.world-node__halo {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 72px;
  height: 72px;
  margin-top: -8px;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  background: radial-gradient(circle, rgba(245, 197, 24, 0.55) 0%, rgba(245, 197, 24, 0) 70%);
  pointer-events: none;
  animation: world-node-pulse 1.6s ease-out infinite;
  z-index: -1;
}

@keyframes world-node-pulse {
  0%   { transform: translate(-50%, -50%) scale(1);   opacity: 0.7; }
  70%  { transform: translate(-50%, -50%) scale(1.6); opacity: 0;   }
  100% { transform: translate(-50%, -50%) scale(1.6); opacity: 0;   }
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
</style>
