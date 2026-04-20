<template>
  <div class="world-map-view">

    <!-- Portrait guard: shown when height > width -->
    <div v-if="isPortrait" class="world-map-view__portrait-guard" role="alert" aria-live="assertive">
      <span class="world-map-view__rotate-icon" aria-hidden="true">↻</span>
      <p>Roter enheten til liggende modus, eller utvid nettleservinduet ditt, for å spille</p>
    </div>

    <template v-else>
      <!-- Scale container — fills the fixed viewport -->
      <div ref="containerRef" class="world-map-view__container">
        <LoadingSpinner v-if="loading" class="world-map-view__loading" />
        <p v-else-if="error" class="world-map-view__error">{{ error }}</p>
        <WorldMapCanvas
          v-else-if="stops.length"
          :stops="stops"
          :current-node-index="currentNodeIndex"
          :shaking-node-index="shakingNodeIndex"
          class="world-map-view__canvas"
          :style="canvasTransform"
          @node-click="handleNodeClick"
        />
      </div>

      <!-- Back button top-left, outside scaled canvas -->
      <button class="world-map-view__back-btn" @click="router.push({ name: 'Home' })" aria-label="Tilbake til hjemmesiden">
        ← Tilbake
      </button>

      <!-- Map toggle top-left below back button -->
      <button class="world-map-view__map-toggle" @click="switchToSimpleMap" aria-label="Bytt til enkel kartvisning">
        Enkel visning
      </button>

      <!-- HUD floats in top-right, outside scaled canvas -->
      <PlayerHud class="world-map-view__hud" />

      <!-- Enter area: bottom-right, fixed, outside scaled canvas -->
      <div class="world-map-view__enter-area" aria-live="polite">
        <p v-if="lockedMessage" class="world-map-view__locked-msg" role="status">
          🔒 Lås opp tidligere stopp først
        </p>
        <button
          v-if="canEnter"
          class="world-map-view__enter-btn"
          @click="handleEnter"
          :aria-label="`Start ${currentStop?.name ?? 'bane'}`"
        >
          Start bane
        </button>
      </div>
    </template>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useGameStore } from '@/stores/game'
import { useClassroomStore } from '@/stores/classroom'
import { useAvatarStore } from '@/stores/avatar'
import { useWorldMapScale } from '@/composables/useWorldMapScale'
import { useAvatarWalk } from '@/composables/useAvatarWalk'
import WorldMapCanvas from '@/components/student/WorldMapCanvas.vue'
import PlayerHud from '@/components/common/PlayerHud.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'

const router = useRouter()
const gameStore = useGameStore()
const classroomStore = useClassroomStore()
const avatarStore = useAvatarStore()

const { containerRef, scale } = useWorldMapScale()
const { currentNodeIndex, isWalking, walkTo, initAutoWalk } = useAvatarWalk()

const loading = ref(false)
const error = ref(null)
const stops = computed(() => gameStore.stops)
const shakingNodeIndex = ref(null)
const lockedMessage = ref(false)
let lockedTimer = null

// Portrait detection
const isPortrait = ref(false)
function checkOrientation() {
  isPortrait.value = window.innerHeight > window.innerWidth
  console.log('[WorldMapView] portrait:', isPortrait.value)
}

// Canvas transform: centred + scaled
const canvasTransform = computed(() => ({
  transform: `translate(-50%, -50%) scale(${scale.value})`,
}))

// The stop the avatar is currently on
const currentStop = computed(() => stops.value[currentNodeIndex.value] ?? null)

// Show Enter button when not walking and avatar is on an unlocked stop
const canEnter = computed(() =>
  !isWalking.value &&
  !!currentStop.value &&
  !currentStop.value.locked
)

function handleNodeClick(index, stop) {
  if (isWalking.value) return
  console.log('[WorldMapView] Node clicked:', index, stop.name, 'locked:', stop.locked)

  if (stop.locked) {
    shakingNodeIndex.value = index
    lockedMessage.value = true
    clearTimeout(lockedTimer)
    lockedTimer = setTimeout(() => {
      shakingNodeIndex.value = null
      lockedMessage.value = false
    }, 1500)
    return
  }

  walkTo(index)
}

function switchToSimpleMap() {
  localStorage.setItem('mapView', 'simple')
  router.push({ name: 'Map' })
}

function handleEnter() {
  if (!currentStop.value || currentStop.value.locked) return
  console.log('[WorldMapView] Entering stop:', currentStop.value.id, currentStop.value.name)
  router.push({
    name: 'Task',
    query: {
      stopId: currentStop.value.id,
      classroomId: classroomStore.currentClassroomId,
    },
  })
}

onMounted(async () => {
  localStorage.setItem('mapView', 'world')
  checkOrientation()
  window.addEventListener('resize', checkOrientation)

  if (!classroomStore.currentClassroomId) {
    console.warn('[WorldMapView] No classroomId — redirecting to join')
    router.push({ name: 'JoinClassroom' })
    return
  }

  loading.value = true
  try {
    await gameStore.fetchStops(classroomStore.currentClassroomId)
    // Ensure avatar is present (may already be prefetched at login)
    if (!avatarStore.avatar) {
      await avatarStore.fetchAvatar().catch(err =>
        console.warn('[WorldMapView] Avatar fetch failed:', err)
      )
    }
    // Cinematic catch-up: avatar spawns at stop 0 then walks to current position
    await initAutoWalk(stops.value)
  } catch (err) {
    console.error('[WorldMapView] Failed to load world map:', err)
    const status = err?.response?.status
    if (status === 400 || status === 403 || status === 404) {
      classroomStore.reset()
      router.push({ name: 'JoinClassroom' })
      return
    }
    error.value = 'Kunne ikke laste kartet. Prøv igjen.'
  } finally {
    loading.value = false
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', checkOrientation)
  clearTimeout(lockedTimer)
})
</script>

<style scoped>
.world-map-view {
  position: fixed;
  inset: 0;
  background: var(--color-map-bg);
  overflow: hidden;
}

/* ---- Portrait guard ---- */
.world-map-view__portrait-guard {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  background: var(--color-map-bg);
  color: #fff;
  text-align: center;
  padding: 2rem;
}

.world-map-view__rotate-icon {
  font-size: 3.5rem;
  display: block;
  animation: wm-spin 2.5s linear infinite;
}

@keyframes wm-spin {
  from { transform: rotate(0deg); }
  to   { transform: rotate(360deg); }
}

.world-map-view__portrait-guard p {
  font-size: 1.125rem;
  max-width: 18rem;
  line-height: 1.5;
}

/* ---- Scale container ---- */
.world-map-view__container {
  position: absolute;
  inset: 0;
}

/* Canvas positioned from top-left 50%/50%, centred via transform in JS */
.world-map-view__canvas {
  position: absolute;
  top: 50%;
  left: 50%;
  transform-origin: center center;
}

/* ---- Loading / error states ---- */
.world-map-view__loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.world-map-view__error {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #ff6b6b;
  text-align: center;
  font-size: 1rem;
}

/* ---- Back button ---- */
.world-map-view__back-btn {
  position: fixed;
  top: 1rem;
  left: 1rem;
  z-index: 100;
  padding: 0.5rem 1rem;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.25);
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  transition: background 0.15s;
}
.world-map-view__back-btn:hover {
  background: rgba(0, 0, 0, 0.8);
}
.world-map-view__back-btn:focus-visible {
  outline: 3px solid #fff;
  outline-offset: 4px;
}

/* ---- Map toggle ---- */
.world-map-view__map-toggle {
  position: fixed;
  top: 3.5rem;
  left: 1rem;
  z-index: 100;
  padding: 0.4rem 0.875rem;
  background: rgba(0, 0, 0, 0.45);
  color: rgba(255, 255, 255, 0.75);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 8px;
  font-size: 0.75rem;
  font-weight: 500;
  cursor: pointer;
  font-family: inherit;
  transition: background 0.15s, color 0.15s;
}
.world-map-view__map-toggle:hover {
  background: rgba(0, 0, 0, 0.7);
  color: #fff;
}
.world-map-view__map-toggle:focus-visible {
  outline: 3px solid #fff;
  outline-offset: 4px;
}

/* ---- HUD overlay ---- */
.world-map-view__hud {
  position: fixed;
  top: 1rem;
  right: 1rem;
  z-index: 100;
}

/* ---- Enter area ---- */
.world-map-view__enter-area {
  position: fixed;
  bottom: 2rem;
  right: 2rem;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 0.5rem;
  z-index: 100;
}

.world-map-view__locked-msg {
  background: rgba(0, 0, 0, 0.8);
  color: #fff;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  font-size: 0.875rem;
  margin: 0;
}

.world-map-view__enter-btn {
  padding: 1rem 2.5rem;
  font-size: 1.375rem;
  font-weight: 800;
  background: #E63946;
  color: #fff;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  box-shadow: 0 6px 0 #9C2733;
  transition: transform 0.1s, box-shadow 0.1s;
  font-family: inherit;
  letter-spacing: 0.02em;
}

.world-map-view__enter-btn:hover {
  filter: brightness(1.1);
}

.world-map-view__enter-btn:active {
  transform: translateY(4px);
  box-shadow: 0 2px 0 #9C2733;
}

.world-map-view__enter-btn:focus-visible {
  outline: 3px solid #fff;
  outline-offset: 4px;
}
</style>
