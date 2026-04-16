<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import api from '@/services/api'
import { useClassroomStore } from '@/stores/classroom'

const router = useRouter()
const classroomStore = useClassroomStore()

const isLoading = ref(true)
const errorMessage = ref('')
const kickedMessage = ref('')
const currentStatus = ref('')

let pollInterval = null

async function fetchStudentStatus() {
  try {
    errorMessage.value = ''
    const classroomId = classroomStore.currentClassroomId

    if (!classroomId) {
      throw new Error('Missing current classroom context')
    }

    const { data } = await api.get(`/api/classrooms/${classroomId}/students/me/status`)
    const status = data.status

    if (currentStatus.value !== status) {
      console.log('[WaitingRoom] Status changed:', currentStatus.value, '→', status)
    }

    currentStatus.value = status
    isLoading.value = false

    if (status === 'APPROVED') {
      console.log('[WaitingRoom] Student approved → redirecting to /')
      stopPolling()
      router.push('/')
      return
    }

    if (status === 'KICKED') {
      console.warn('[WaitingRoom] Student was kicked')
      kickedMessage.value = 'Du har blitt fjernet fra venteværelset.'
      stopPolling()
      return
    }
  } catch (error) {
    isLoading.value = false
    errorMessage.value = 'Kunne ikke hente venteværelsesstatus.'

    console.error('[WaitingRoom] fetchStudentStatus failed:', error)
  }
}

function startPolling() {
  console.log('[WaitingRoom] Starting polling...')

  fetchStudentStatus()

  pollInterval = setInterval(() => {
    fetchStudentStatus()
  }, 3000)
}

function stopPolling() {
  if (pollInterval) {
    console.log('[WaitingRoom] Stopping polling')
    clearInterval(pollInterval)
    pollInterval = null
  }
}

onMounted(() => {
  console.log('[WaitingRoom] Component mounted')
  startPolling()
})

onUnmounted(() => {
  console.log('[WaitingRoom] Component unmounted')
  stopPolling()
})
</script>

<template>
  <section class="waiting-room-view">
    <h1>Venterom</h1>

    <div v-if="isLoading" class="waiting-room-view__loading">
      <LoadingSpinner />
      <p>Du venter på godkjenning.</p>
    </div>

    <p v-else-if="kickedMessage">{{ kickedMessage }}</p>

    <div v-else>
      <p>Du venter på godkjenning.</p>
      <p v-if="currentStatus">Status: {{ currentStatus }}</p>
    </div>

    <p v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </p>
  </section>
</template>

<style scoped>
.waiting-room-view {
  display: grid;
  gap: var(--space-4);
  align-content: start;
  min-height: 100vh;
  padding: var(--space-6);
  background: var(--color-bg);
}

.waiting-room-view h1 {
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  color: var(--color-text);
}

.waiting-room-view__loading {
  display: grid;
  justify-items: start;
  gap: var(--space-3);
}

.error-message {
  color: var(--color-danger);
}
</style>
