<template>
  <div class="map-view">
    <h1 class="map-view__title">Kart over Nettdetektivene</h1>

    <LoadingSpinner v-if="loading" />

    <p v-else-if="error" class="map-view__error">{{ error }}</p>

    <div v-else class="stops-path">
      <StopMarker
        v-for="stop in stops"
        :key="stop.id"
        :stop="stop"
        :flash="lockedStopId === stop.id"
        @click="handleStopClick"
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

onMounted(async () => {
  loading.value = true
  console.log('[MapView] Loading stops for classroom:', classroomStore.currentClassroomId)
  try {
    await gameStore.fetchStops(classroomStore.currentClassroomId)
  } catch (err) {
    console.error('[MapView] Failed to load stops:', err)
    if (err?.response?.status === 404) {
      console.warn('[MapView] Classroom not found — clearing state and redirecting to join')
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

.map-view__title {
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  color: var(--color-text);
  margin-bottom: var(--space-6);
}

.map-view__error {
  color: var(--color-danger);
  text-align: center;
  padding: var(--space-4);
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
