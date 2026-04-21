<template>
  <div class="world-canvas">

    <!-- Layer 1: Background scenery (wireframe-derived SVG) -->
    <svg
      class="world-canvas__layer"
      viewBox="0 0 1600 900"
      xmlns="http://www.w3.org/2000/svg"
      preserveAspectRatio="xMidYMid slice"
      aria-hidden="true"
    >
      <defs>
        <radialGradient id="wm-light" cx="0.5" cy="0.5" r="0.5">
          <stop offset="0%"   stop-color="#A8E878" stop-opacity="0.3"/>
          <stop offset="100%" stop-color="#A8E878" stop-opacity="0"/>
        </radialGradient>
        <linearGradient id="wm-grass" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%"   stop-color="#6FC847"/>
          <stop offset="100%" stop-color="#3E8A33"/>
        </linearGradient>
        <linearGradient id="wm-water" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%"   stop-color="#5AB5E8"/>
          <stop offset="100%" stop-color="#2E7ACC"/>
        </linearGradient>
      </defs>

      <!-- Grass base -->
      <rect width="1600" height="900" fill="url(#wm-grass)"/>
      <!-- Ambient light spots -->
      <circle cx="350"  cy="400" r="340" fill="url(#wm-light)"/>
      <circle cx="1100" cy="500" r="380" fill="url(#wm-light)"/>
      <!-- River shadow -->
      <path
        d="M -20 180 Q 80 220 140 280 Q 200 340 180 420 Q 160 500 240 540 Q 320 580 380 620 Q 460 660 560 680"
        fill="none" stroke="#2E7ACC" stroke-width="32" stroke-linecap="round" opacity="0.35"
      />
      <!-- River body -->
      <path
        d="M -20 180 Q 80 220 140 280 Q 200 340 180 420 Q 160 500 240 540 Q 320 580 380 620 Q 460 660 560 680"
        fill="none" stroke="url(#wm-water)" stroke-width="22" stroke-linecap="round"
      />
      <!-- River highlight -->
      <path
        d="M -20 177 Q 80 217 140 277 Q 200 337 180 417 Q 160 497 240 537 Q 320 577 380 617 Q 460 657 560 677"
        fill="none" stroke="#FFFFFF" stroke-width="2" stroke-linecap="round" opacity="0.55"
      />
      <!-- Lake -->
      <ellipse cx="580" cy="720" rx="95" ry="56" fill="url(#wm-water)"/>
      <ellipse cx="580" cy="712" rx="88" ry="46" fill="none" stroke="#FFFFFF" stroke-width="1.5" opacity="0.5"/>
      <path
        d="M 540 705 Q 560 700 580 705 M 590 720 Q 610 715 625 720 M 550 735 Q 575 730 595 735"
        stroke="#FFFFFF" stroke-width="1.2" fill="none" opacity="0.6"
      />
    </svg>

    <!-- Layer 2: Path SVG overlay -->
    <svg
      class="world-canvas__layer"
      viewBox="0 0 1600 900"
      xmlns="http://www.w3.org/2000/svg"
      aria-hidden="true"
    >
      <!-- Drop shadow -->
      <path
        :d="FULL_PATH"
        fill="none"
        stroke="#1a1a1a"
        stroke-width="30"
        stroke-linecap="round"
        opacity="0.3"
        style="transform: translate(3px, 5px)"
      />
      <!-- Full grey path (upcoming/background) -->
      <path
        :d="FULL_PATH"
        fill="none"
        stroke="#9E9E9E"
        stroke-width="24"
        stroke-linecap="round"
      />
      <!-- Gold completed segments (drawn on top of grey) -->
      <path
        v-for="(seg, i) in PATH_SEGMENTS"
        :key="`seg-${i}`"
        :d="seg"
        fill="none"
        :stroke="currentNodeIndex > i ? '#F5C518' : 'none'"
        stroke-width="24"
        stroke-linecap="round"
      />
    </svg>

    <!-- Layer 3: Nodes -->
    <WorldMapNode
      v-for="(stop, i) in stops"
      :key="stop.id"
      :stop="stop"
      :is-current="i === currentNodeIndex"
      :is-shaking="shakingNodeIndex === i"
      :style="{ left: `${NODE_POSITIONS[i].x}px`, top: `${NODE_POSITIONS[i].y}px` }"
      @node-click="$emit('node-click', i, stop)"
    />

    <!-- Layer 4: Avatar -->
    <div class="world-canvas__avatar" :style="avatarStyle">
      <AvatarPreview :selections="avatarStore.avatar ?? {}" :size="80" />
    </div>

  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useAvatarStore } from '@/stores/avatar'
import WorldMapNode from '@/components/student/WorldMapNode.vue'
import AvatarPreview from '@/components/student/AvatarPreview.vue'

const props = defineProps({
  stops:            { type: Array,  required: true },
  currentNodeIndex: { type: Number, required: true },
  shakingNodeIndex: { type: Number, default: null },
})

defineEmits(['node-click'])

// ---- Map geometry constants ----
const NODE_POSITIONS = [
  { x: 140,  y: 750 },
  { x: 380,  y: 520 },
  { x: 630,  y: 280 },
  { x: 870,  y: 520 },
  { x: 1090, y: 280 },
  { x: 1310, y: 520 },
  { x: 1480, y: 260 },
]

const PATH_SEGMENTS = [
  'M 140,750 C 200,700 320,560 380,520',
  'M 380,520 C 440,480 570,320 630,280',
  'M 630,280 C 690,240 830,480 870,520',
  'M 870,520 C 910,560 1050,320 1090,280',
  'M 1090,280 C 1130,240 1270,480 1310,520',
  'M 1310,520 C 1360,560 1450,300 1480,260',
]

const FULL_PATH =
  'M 140,750 C 200,700 320,560 380,520 ' +
  'C 440,480 570,320 630,280 ' +
  'C 690,240 830,480 870,520 ' +
  'C 910,560 1050,320 1090,280 ' +
  'C 1130,240 1270,480 1310,520 ' +
  'C 1360,560 1450,300 1480,260'

// ---- Avatar ----
const avatarStore = useAvatarStore()

const avatarStyle = computed(() => {
  const pos = NODE_POSITIONS[props.currentNodeIndex] ?? NODE_POSITIONS[0]
  return {
    left: `${pos.x}px`,
    top:  `${pos.y - 90}px`,
    transition: 'left 0.6s ease-in-out, top 0.6s ease-in-out',
  }
})
</script>

<style scoped>
.world-canvas {
  position: relative;
  width: 1600px;
  height: 900px;
  overflow: hidden;
}

.world-canvas__layer {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.world-canvas__avatar {
  position: absolute;
  pointer-events: none;
  filter: drop-shadow(0 4px 6px rgba(0, 0, 0, 0.6));
  z-index: 10;
  transform: translateX(-50%);
}
</style>
