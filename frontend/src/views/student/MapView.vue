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
        @click="handleStopClick"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
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

onMounted(async () => {
  loading.value = true
  console.log('[MapView] Loading stops for classroom:', classroomStore.currentClassroomId)
  try {
    await gameStore.fetchStops(classroomStore.currentClassroomId)
  } catch (err) {
    console.error('[MapView] Failed to load stops:', err)
    error.value = 'Kunne ikke laste kartet. Prøv igjen.'
  } finally {
    loading.value = false
  }
})

function handleStopClick(stop) {
  if (stop.locked) {
    console.log('[MapView] Stop is locked, ignoring click:', stop.id)
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
  align-items: center;
  gap: var(--space-4);
  max-width: 24rem;
  margin: 0 auto;
}
</style>
