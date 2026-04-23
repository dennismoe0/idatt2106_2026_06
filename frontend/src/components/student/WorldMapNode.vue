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
    <div class="world-node__disk" aria-hidden="true">
      <svg
        class="world-node__svg"
        viewBox="0 0 72 72"
        width="96"
        height="96"
        xmlns="http://www.w3.org/2000/svg"
      >
        <defs>
          <radialGradient :id="gradId" cx="35%" cy="30%" r="75%">
            <stop offset="0%" :stop-color="gradStops[0]" />
            <stop offset="60%" :stop-color="gradStops[1]" />
            <stop offset="100%" :stop-color="gradStops[2]" />
          </radialGradient>
        </defs>
        <circle
          cx="36"
          cy="36"
          r="34"
          :fill="`url(#${gradId})`"
          stroke="#ffffff"
          stroke-width="3"
        />
        <g
          class="world-node__icon"
          :data-icon="iconName"
          transform="translate(12 12) scale(2)"
          fill="none"
          stroke="#ffffff"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
        >
          <template v-for="(shape, i) in iconShapes" :key="i">
            <path v-if="shape.type === 'path'" :d="shape.d" />
            <circle
              v-else-if="shape.type === 'circle'"
              :cx="shape.cx"
              :cy="shape.cy"
              :r="shape.r"
            />
            <rect
              v-else-if="shape.type === 'rect'"
              :x="shape.x"
              :y="shape.y"
              :width="shape.w"
              :height="shape.h"
              :rx="shape.rx"
            />
          </template>
        </g>
      </svg>
      <span
        v-if="stop.completed"
        class="world-node__badge world-node__badge--done"
        aria-hidden="true"
      >✓</span>
      <span
        v-else-if="stop.locked"
        class="world-node__badge world-node__badge--lock"
        aria-hidden="true"
      >🔒</span>
    </div>
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

const gradId = computed(() => `wn-grad-${props.stop.id}`)

const I_NEWSPAPER = [
  { type: 'path', d: 'M4 22h16a2 2 0 0 0 2-2V4a2 2 0 0 0-2-2H8a2 2 0 0 0-2 2v16a2 2 0 0 1-2 2Zm0 0a2 2 0 0 1-2-2v-9c0-1.1.9-2 2-2h2' },
  { type: 'path', d: 'M18 14h-8' },
  { type: 'path', d: 'M15 18h-5' },
  { type: 'path', d: 'M10 6h8v4h-8V6Z' },
]
const I_MAIL = [
  { type: 'rect', x: 2, y: 4, w: 20, h: 16, rx: 2 },
  { type: 'path', d: 'm22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7' },
]
const I_CAMERA = [
  { type: 'path', d: 'M14.5 4h-5L7 7H4a2 2 0 0 0-2 2v9a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2h-3l-2.5-3z' },
  { type: 'circle', cx: 12, cy: 13, r: 3 },
]
const I_KEY = [
  { type: 'path', d: 'm15.5 7.5 2.3 2.3a1 1 0 0 0 1.4 0l2.1-2.1a1 1 0 0 0 0-1.4L19 4' },
  { type: 'path', d: 'm21 2-9.6 9.6' },
  { type: 'circle', cx: 7.5, cy: 15.5, r: 5.5 },
]
const I_CART = [
  { type: 'circle', cx: 8, cy: 21, r: 1 },
  { type: 'circle', cx: 19, cy: 21, r: 1 },
  { type: 'path', d: 'M2.05 2.05h2l2.66 12.42a2 2 0 0 0 2 1.58h9.78a2 2 0 0 0 1.95-1.57l1.65-7.43H5.12' },
]
const I_CHAT = [
  { type: 'path', d: 'M7.9 20A9 9 0 1 0 4 16.1L2 22Z' },
]
const I_TROPHY = [
  { type: 'path', d: 'M6 9H4.5a2.5 2.5 0 0 1 0-5H6' },
  { type: 'path', d: 'M18 9h1.5a2.5 2.5 0 0 0 0-5H18' },
  { type: 'path', d: 'M4 22h16' },
  { type: 'path', d: 'M10 14.66V17c0 .55-.47.98-.97 1.21C7.85 18.75 7 20.24 7 22' },
  { type: 'path', d: 'M14 14.66V17c0 .55.47.98.97 1.21C16.15 18.75 17 20.24 17 22' },
  { type: 'path', d: 'M18 2H6v7a6 6 0 0 0 12 0V2Z' },
]
const I_PIN = [
  { type: 'path', d: 'M20 10c0 4.993-5.539 10.193-7.399 11.799a1 1 0 0 1-1.202 0C9.539 20.193 4 14.993 4 10a8 8 0 0 1 16 0' },
  { type: 'circle', cx: 12, cy: 10, r: 3 },
]

const THEME_ICONS = {
  FAKE_NEWS:      { name: 'newspaper',     shapes: I_NEWSPAPER },
  PHISHING_EMAIL: { name: 'mail',          shapes: I_MAIL },
  AI_PHOTO:       { name: 'camera',        shapes: I_CAMERA },
  PASSWORD:       { name: 'key',           shapes: I_KEY },
  MARKETPLACE:    { name: 'shopping-cart', shapes: I_CART },
  SOCIAL_MEDIA:   { name: 'message-circle',shapes: I_CHAT },
  FINAL_BOSS:     { name: 'trophy',        shapes: I_TROPHY },
}

const FALLBACK_ICON = { name: 'map-pin', shapes: I_PIN }

const GRADIENT_STOPS = {
  unlocked:  ['#F06A4E', '#C0392B', '#922B21'],
  completed: ['#5DADE2', '#2471A3', '#1A5276'],
  locked:    ['#9A9A9A', '#5E5E5E', '#3A3A3A'],
}

const stateClass = computed(() => {
  if (props.stop.locked)    return 'world-node--locked'
  if (props.stop.completed) return 'world-node--completed'
  return 'world-node--unlocked'
})

const iconDef = computed(() => THEME_ICONS[props.stop.theme] ?? FALLBACK_ICON)
const iconName = computed(() => iconDef.value.name)
const iconShapes = computed(() => iconDef.value.shapes)

const gradStops = computed(() => {
  if (props.stop.locked)    return GRADIENT_STOPS.locked
  if (props.stop.completed) return GRADIENT_STOPS.completed
  return GRADIENT_STOPS.unlocked
})

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

.world-node:hover:not([aria-disabled='true']) .world-node__svg {
  filter: drop-shadow(0 7px 6px rgba(0, 0, 0, 0.45));
}

.world-node:focus-visible {
  outline: none;
}
.world-node:focus-visible .world-node__svg {
  outline: 3px solid var(--color-map-node-stroke);
  outline-offset: 4px;
  border-radius: 50%;
}

.world-node__disk {
  position: relative;
  width: 96px;
  height: 96px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.world-node__svg {
  display: block;
  filter: drop-shadow(0 5px 4px rgba(0, 0, 0, 0.5));
  transition: filter 0.18s ease-out;
}

.world-node__label {
  background: var(--color-map-plaque-bg);
  color: var(--color-map-plaque-ink);
  padding: 3px 10px;
  border-radius: 10px;
  border: 2px solid var(--color-map-plaque-border);
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
  border: 2px solid var(--color-map-node-stroke);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.45);
  top: -2px;
  right: -2px;
  pointer-events: none;
  z-index: 2;
}
.world-node__badge--done {
  background: var(--color-map-badge-done-bg);
  color: var(--color-map-node-stroke);
}
.world-node__badge--lock {
  background: var(--color-map-badge-lock-bg);
  color: var(--color-map-badge-lock-fg);
  font-size: 11px;
}

.world-node--locked {
  cursor: default;
  opacity: 0.85;
}
.world-node--locked .world-node__svg {
  filter: grayscale(50%) drop-shadow(0 4px 4px rgba(0, 0, 0, 0.5));
}
.world-node--locked .world-node__label {
  background: var(--color-map-plaque-locked-bg);
  color: var(--color-map-plaque-locked-ink);
  border-color: var(--color-map-plaque-locked-border);
  opacity: 0.9;
}

.world-node__halo {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 128px;
  height: 128px;
  margin-top: -16px;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  background: radial-gradient(circle, var(--color-map-halo) 0%, var(--color-map-halo-fade) 70%);
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
