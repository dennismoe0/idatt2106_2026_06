<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'

const router = useRouter()

const isLoading = ref(true)
const errorMessage = ref('')
const kickedMessage = ref('')
const currentStatus = ref('')

let pollInterval = null

async function fetchStudentStatus() {
  try {
    errorMessage.value = ''

    // placeholder
    const response = await fetch('/api/student/status')

    if (!response.ok) {
      throw new Error('Failed to fetch student status')
    }

    const data = await response.json()
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
      kickedMessage.value = 'Du ble kastet ut.'
      stopPolling()
      return
    }
  } catch (error) {
    isLoading.value = false
    errorMessage.value = 'Kunne ikke oppdatere status i venterommet.'

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
      <p>Venter på godkjenning fra lærer...</p>
    </div>

    <p v-else-if="kickedMessage">{{ kickedMessage }}</p>

    <div v-else>
      <p>Venter på godkjenning fra lærer...</p>
      <p v-if="currentStatus">Status: {{ currentStatus }}</p>
    </div>

    <p v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </p>
  </section>
</template>

<style scoped>
.waiting-room-view__loading {
  display: grid;
  justify-items: start;
  gap: var(--space-3);
}
</style>
