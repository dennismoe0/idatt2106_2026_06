<template>
  <main class="waiting-page">
    <section class="waiting-card">
      <h1>{{ kicked ? 'Du ble fjernet fra klassen' : 'Venter på godkjenning' }}</h1>
      <p v-if="!kicked">
        Forespørselen din er sendt til læreren. Når du er godkjent, kan du fortsette inn i klassen.
      </p>
      <p v-else>
        Læreren har avvist eller fjernet forespørselen din. Du kan prøve igjen med en gyldig klassekode.
      </p>

      <p v-if="pendingJoin?.displayName" class="waiting-detail">
        Du er registrert som <strong>{{ pendingJoin.displayName }}</strong>
        <span v-if="pendingJoin.code"> med klassekode <strong>{{ pendingJoin.code }}</strong></span>.
      </p>

      <RouterLink v-if="kicked" class="waiting-link" to="/join">
        Tilbake til bli med i klasse
      </RouterLink>

      <button class="logout-btn" @click="handleLogout">
        Logg ut
      </button>
    </section>
  </main>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useClassroomStore } from '@/stores/classroom'

const router = useRouter()
const authStore = useAuthStore()
const classroomStore = useClassroomStore()
const { pendingJoin } = storeToRefs(classroomStore)

function handleLogout() {
  console.log('[WaitingRoomView] Student logging out from waiting room')
  authStore.logout()
  router.push({ name: 'StudentLogin' })
}
const kicked = ref(false)

let pollInterval = null

onMounted(() => {
  if (!pendingJoin.value) {
    console.warn('[WaitingRoomView] No pending join — redirecting to join page')
    router.replace({ name: 'JoinClassroom' })
    return
  }

  pollInterval = setInterval(async () => {
    try {
      const status = await classroomStore.fetchMyStatus(pendingJoin.value.classroomId)
      if (status === 'APPROVED') {
        clearInterval(pollInterval)
        pollInterval = null
        const hasSeenIntro = localStorage.getItem('hasSeenIntro') === 'true'
        router.push({ name: hasSeenIntro ? 'Home' : 'Intro' })
      } else if (status === 'KICKED') {
        clearInterval(pollInterval)
        pollInterval = null
        kicked.value = true
      }
    } catch (err) {
      console.error('[WaitingRoomView] Failed to poll status:', err)
    }
  }, 3000)
})

onUnmounted(() => {
  if (pollInterval) {
    clearInterval(pollInterval)
  }
})
</script>

<style scoped>
.waiting-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-4);
  background: var(--color-bg);
}

.waiting-card {
  width: min(100%, 460px);
  padding: var(--space-8);
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
}

.waiting-card h1 {
  margin-bottom: var(--space-4);
  color: var(--color-primary);
  font-size: var(--text-3xl);
}

.waiting-card p {
  color: var(--color-text);
  line-height: 1.5;
}

.waiting-detail {
  margin-top: var(--space-4);
  color: var(--color-text-muted);
}

.waiting-link {
  display: inline-block;
  margin-top: var(--space-6);
  color: var(--color-primary);
  font-weight: var(--font-medium);
}

.logout-btn {
  display: block;
  margin-top: var(--space-4);
  background: none;
  border: none;
  color: var(--color-text-muted);
  font-size: var(--text-sm);
  cursor: pointer;
  padding: 0;
  text-decoration: underline;
}
.logout-btn:hover { color: var(--color-danger); }
</style>
