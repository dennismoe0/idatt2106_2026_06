<template>
  <div class="map-view">
    <DetectiveBar page-title="Kart" :back-to="{ name: 'Home' }" />

    <div class="map-view__actions" aria-label="Kartvalg">
      <button class="map-view__world-btn" @click="switchToWorldMap" aria-label="Bytt til verdenskart">
        Verdenskart
      </button>
      <RouterLink class="map-view__dossier-btn" :to="{ name: 'SuspectDossier' }" aria-label="Åpne mistenktmappe">
        🗂 Mistenktmappe
      </RouterLink>
    </div>

    <LoadingSpinner v-if="loading" />

    <p v-else-if="error" class="map-view__error">{{ error }}</p>

    <div v-else class="stops-path">
      <StopMarker
        v-for="stop in stops"
        :key="stop.id"
        :stop="stop"
        :flash="lockedStopId === stop.id"
        @click="handleStopClick"
        @claim-xp="handleClaimXp"
      />
    </div>

    <Transition name="fade">
      <p v-if="lockedMessage" class="map-view__locked-msg" role="status">
        🔒 Spill de tidligere stoppene for å låse opp denne!
      </p>
    </Transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useGameStore } from '@/stores/game'
import { useClassroomStore } from '@/stores/classroom'
import DetectiveBar from '@/components/common/DetectiveBar.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import StopMarker from '@/components/student/StopMarker.vue'

const router = useRouter()
const gameStore = useGameStore()
const classroomStore = useClassroomStore()

const loading = ref(false)
const error = ref(null)
const stops = computed(() => gameStore.stops)
const lockedStopId = ref(null)
const lockedMessage = ref(false)
let lockedTimer = null

function switchToWorldMap() {
  localStorage.setItem('mapView', 'world')
  router.push({ name: 'WorldMap' })
}

onMounted(async () => {
  // Redirect to world map unless user explicitly chose simple view
  if (localStorage.getItem('mapView') !== 'simple') {
    router.push({ name: 'WorldMap' })
    return
  }

  // No classroomId at all — student hasn't joined yet
  if (!classroomStore.currentClassroomId) {
    console.warn('[MapView] No classroomId — redirecting to join')
    router.push({ name: 'JoinClassroom' })
    return
  }

  loading.value = true
  console.log('[MapView] Loading stops for classroom:', classroomStore.currentClassroomId)
  try {
    await gameStore.fetchStops(classroomStore.currentClassroomId)
  } catch (err) {
    console.error('[MapView] Failed to load stops:', err)
    const status = err?.response?.status
    if (status === 400 || status === 404 || status === 403) {
      console.warn('[MapView] Invalid/stale classroomId (%s) — clearing and redirecting to join', status)
      classroomStore.reset()
      router.push({ name: 'JoinClassroom' })
      return
    }
    error.value = 'Kunne ikke laste kartet. Prøv igjen.'
  } finally {
    loading.value = false
  }
})

onUnmounted(() => clearTimeout(lockedTimer))

const claimingStopIds = new Set()

async function handleClaimXp(stop) {
  if (!classroomStore.currentClassroomId) {
    console.warn('[MapView] No classroomId during XP claim — redirecting')
    router.push({ name: 'JoinClassroom' })
    return
  }
  if (claimingStopIds.has(stop.id)) return
  claimingStopIds.add(stop.id)
  console.log('[MapView] Claiming weekly XP for stop:', stop.id)
  try {
    await gameStore.claimWeeklyXp(stop.id)
    await gameStore.fetchStops(classroomStore.currentClassroomId)
    console.log('[MapView] XP claimed and stops refreshed for stop:', stop.id)
  } catch (err) {
    console.error('[MapView] Failed to claim XP for stop:', stop.id, err)
  } finally {
    claimingStopIds.delete(stop.id)
  }
}

function handleStopClick(stop) {
  if (stop.locked) {
    console.log('[MapView] Stop is locked:', stop.id)
    lockedStopId.value = stop.id
    lockedMessage.value = true
    clearTimeout(lockedTimer)
    lockedTimer = setTimeout(() => {
      lockedStopId.value = null
      lockedMessage.value = false
    }, 2000)
    return
  }
  console.log('[MapView] Navigating to task for stop:', stop.id)
  router.push({ name: 'Task', query: { stopId: stop.id, classroomId: classroomStore.currentClassroomId } })
}
</script>

<style scoped>
.map-view {
  padding: var(--space-6);
  min-height: 100vh;
  background: var(--color-bg);
}

.map-view__error {
  color: var(--color-danger);
  text-align: center;
  padding: var(--space-4);
}

.map-view__actions {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
  margin: var(--space-3) 0 0;
}

.map-view__world-btn,
.map-view__dossier-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 44px;
  padding: var(--space-2) var(--space-4);
  background: var(--color-surface);
  color: var(--color-primary);
  border: 1px solid var(--color-primary);
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  cursor: pointer;
  font-family: inherit;
  text-decoration: none;
  transition: background var(--transition-fast), color var(--transition-fast);
}

.map-view__world-btn:hover,
.map-view__dossier-btn:hover {
  background: var(--color-primary-light);
}

.map-view__dossier-btn {
  color: var(--color-dossier-danger);
  border-color: var(--color-red-pin);
  font-weight: var(--font-bold);
}

.stops-path {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: var(--space-4);
  max-width: 24rem;
  margin: 0 auto;
}

.map-view__locked-msg {
  max-width: 24rem;
  margin: var(--space-4) auto 0;
  padding: var(--space-3) var(--space-4);
  background: var(--color-danger-light);
  color: var(--color-danger);
  border: 1px solid var(--color-danger);
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  text-align: center;
}

.fade-enter-active, .fade-leave-active { transition: opacity var(--transition-fast); }
.fade-enter-from, .fade-leave-to       { opacity: 0; }
</style>
