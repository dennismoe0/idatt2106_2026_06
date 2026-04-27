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

        <!-- Reusable tree symbol (trunk-base anchored at 0,0) -->
        <symbol id="wm-tree" overflow="visible">
          <ellipse cx="0" cy="4" rx="22" ry="6" fill="#1a1a1a" opacity="0.28"/>
          <rect x="-6" y="-28" width="12" height="32" rx="2" fill="#5D3A1A"/>
          <path d="M 0,-82 L -30,-20 L 30,-20 Z" fill="#2E7D32"/>
          <path d="M 0,-68 L -26,-10 L 26,-10 Z" fill="#43A047"/>
          <path d="M -8,-48 Q 0,-40 8,-48" stroke="#2E7D32" stroke-width="1.5" fill="none" opacity="0.6"/>
        </symbol>

        <!-- Reusable house symbol (front-door base anchored at 0,0) -->
        <symbol id="wm-house" overflow="visible">
          <ellipse cx="0" cy="2" rx="34" ry="6" fill="#1a1a1a" opacity="0.28"/>
          <rect x="-26" y="-40" width="52" height="40" fill="#E8D5A8" stroke="#6B5033" stroke-width="2"/>
          <path d="M -32,-40 L 0,-66 L 32,-40 Z" fill="#8C3E2E" stroke="#5D2418" stroke-width="2" stroke-linejoin="round"/>
          <rect x="-6" y="-18" width="12" height="18" fill="#5D3A1A"/>
          <rect x="-20" y="-32" width="10" height="10" fill="#FFE082" stroke="#6B5033" stroke-width="1.5"/>
          <rect x="10" y="-32" width="10" height="10" fill="#FFE082" stroke="#6B5033" stroke-width="1.5"/>
          <line x1="-15" y1="-32" x2="-15" y2="-22" stroke="#6B5033" stroke-width="1"/>
          <line x1="-20" y1="-27" x2="-10" y2="-27" stroke="#6B5033" stroke-width="1"/>
          <line x1="15"  y1="-32" x2="15"  y2="-22" stroke="#6B5033" stroke-width="1"/>
          <line x1="10"  y1="-27" x2="20"  y2="-27" stroke="#6B5033" stroke-width="1"/>
        </symbol>
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

      <!-- Decorations: houses, bridge, trees (drawn here so nodes still sit above) -->
      <g class="world-canvas__decor">
        <!-- Bridge across the river (river at (430, 642) has tangent ~21 deg; bridge rotated perpendicular) -->
        <g transform="translate(430 642) rotate(-68)">
          <rect x="-55" y="-2"  width="110" height="3"  fill="#5D3A1A"/>
          <rect x="-55" y="0"   width="110" height="18" fill="#8C5A20"/>
          <rect x="-55" y="15"  width="110" height="3"  fill="#5D3A1A"/>
          <line x1="-40" y1="2" x2="-40" y2="16" stroke="#5D3A1A" stroke-width="1.2"/>
          <line x1="-22" y1="2" x2="-22" y2="16" stroke="#5D3A1A" stroke-width="1.2"/>
          <line x1="-4"  y1="2" x2="-4"  y2="16" stroke="#5D3A1A" stroke-width="1.2"/>
          <line x1="14"  y1="2" x2="14"  y2="16" stroke="#5D3A1A" stroke-width="1.2"/>
          <line x1="32"  y1="2" x2="32"  y2="16" stroke="#5D3A1A" stroke-width="1.2"/>
        </g>

        <!-- Houses (door-base anchored at 0,0 in symbol) -->
        <use href="#wm-house" transform="translate(290 850) rotate(-2)"/>
        <use href="#wm-house" transform="translate(1050 600) rotate(3) scale(0.95)"/>
        <use href="#wm-house" transform="translate(1475 820)"/>

        <!-- Trees scattered across empty grass (trunk-base anchored at 0,0 in symbol) -->
        <use href="#wm-tree" transform="translate(70 640)"/>
        <use href="#wm-tree" transform="translate(250 860)"/>
        <use href="#wm-tree" transform="translate(520 790)"/>
        <use href="#wm-tree" transform="translate(760 440) scale(0.8)"/>
        <use href="#wm-tree" transform="translate(960 780)"/>
        <use href="#wm-tree" transform="translate(1200 400)"/>
        <use href="#wm-tree" transform="translate(1430 720) scale(0.85)"/>
        <use href="#wm-tree" transform="translate(1560 520)"/>
      </g>
    </svg>

    <!-- Layer 2: Path SVG overlay (per-segment render) -->
    <svg
      class="world-canvas__layer"
      viewBox="0 0 1600 900"
      xmlns="http://www.w3.org/2000/svg"
      aria-hidden="true"
    >
      <!-- Single continuous drop shadow (avoids opacity stacking at segment joints) -->
      <path
        :d="FULL_PATH"
        fill="none"
        stroke="#1a1a1a"
        stroke-width="30"
        stroke-linecap="round"
        opacity="0.28"
        style="transform: translate(3px, 5px)"
      />

      <!-- Enhanced base stroke: dark outer + lighter inner for cobblestone look -->
      <template v-for="(seg, i) in PATH_SEGMENTS" :key="`base-${i}`">
        <path
          v-if="segmentStates[i] !== 'completed'"
          :d="seg"
          fill="none"
          stroke="#8B7355"
          stroke-width="26"
          stroke-linecap="round"
          :stroke-dasharray="segmentStates[i] === 'locked' ? '14 16' : undefined"
        />
        <path
          v-if="segmentStates[i] === 'upcoming'"
          :d="seg"
          fill="none"
          stroke="#A8926A"
          stroke-width="18"
          stroke-linecap="round"
        />
      </template>

      <!-- Gold completed overlay -->
      <template v-for="(seg, i) in PATH_SEGMENTS" :key="`done-${i}`">
        <path
          v-if="segmentStates[i] === 'completed'"
          :d="seg"
          fill="none"
          style="stroke: var(--color-map-path-done)"
          stroke-width="24"
          stroke-linecap="round"
        />
      </template>

      <!-- Footprint stamps at midpoints of completed segments -->
      <template v-for="(seg, i) in PATH_SEGMENTS" :key="`foot-${i}`">
        <text
          v-if="segmentStates[i] === 'completed'"
          :x="SEGMENT_MIDPOINTS[i].x"
          :y="SEGMENT_MIDPOINTS[i].y"
          text-anchor="middle"
          dominant-baseline="middle"
          font-size="18"
          style="opacity: 0.55; pointer-events: none; user-select: none;"
        >👣</text>
      </template>
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
    <div
      class="world-canvas__avatar"
      :class="{
        'world-canvas__avatar--walking': props.isWalking,
        'world-canvas__avatar--idle':    !props.isWalking,
      }"
      :style="avatarStyle"
    >
      <AvatarPreview :selections="avatarStore.avatar ?? {}" :size="80" />
    </div>

    <!-- Layer 4b: Stop name labels -->
    <svg
      class="world-canvas__layer"
      viewBox="0 0 1600 900"
      xmlns="http://www.w3.org/2000/svg"
      aria-hidden="true"
    >
      <text
        v-for="(stop, i) in stops"
        :key="`label-${stop.id}`"
        :x="NODE_POSITIONS[i].x"
        :y="NODE_POSITIONS[i].y + 62"
        text-anchor="middle"
        font-family="'Special Elite', serif"
        font-size="13"
        fill="#2D1B00"
        stroke="#FFF8E7"
        stroke-width="3"
        paint-order="stroke"
        style="pointer-events: none;"
      >{{ stop.name }}</text>
    </svg>

  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useAvatarStore } from '@/stores/avatar'
import WorldMapNode from '@/components/student/WorldMapNode.vue'
import AvatarPreview from '@/components/student/AvatarPreview.vue'

const props = defineProps({
  stops:            { type: Array,   required: true },
  currentNodeIndex: { type: Number,  required: true },
  shakingNodeIndex: { type: Number,  default: null },
  isWalking:        { type: Boolean, default: false },
})

defineEmits(['node-click'])

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

const SEGMENT_MIDPOINTS = [
  { x: 260, y: 635 },
  { x: 505, y: 400 },
  { x: 750, y: 400 },
  { x: 980, y: 400 },
  { x: 1200, y: 400 },
  { x: 1395, y: 390 },
]

const segmentStates = computed(() =>
  PATH_SEGMENTS.map((_, i) => {
    if (props.currentNodeIndex > i) return 'completed'
    if (props.stops[i + 1]?.locked) return 'locked'
    return 'upcoming'
  }),
)

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

@keyframes avatar-walk {
  0%   { transform: translateX(-50%) rotate(-5deg); }
  50%  { transform: translateX(-50%) rotate(5deg); }
  100% { transform: translateX(-50%) rotate(-5deg); }
}

@keyframes avatar-idle {
  0%, 100% { transform: translateX(-50%) translateY(0px); }
  50%       { transform: translateX(-50%) translateY(-4px); }
}

.world-canvas__avatar--walking {
  animation: avatar-walk 0.45s ease-in-out infinite;
}

.world-canvas__avatar--idle {
  animation: avatar-idle 2.2s ease-in-out infinite;
}
</style>
