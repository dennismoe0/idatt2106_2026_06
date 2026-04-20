<template>
  <RouterView />
  <DevNav v-if="isDev" />
</template>

<script setup>
import { watch, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import DevNav from '@/components/common/DevNav.vue'
import { useAuthStore } from '@/stores/auth'
import { useClassroomStore } from '@/stores/classroom'

const isDev = import.meta.env.DEV
const router = useRouter()
const authStore = useAuthStore()
const classroomStore = useClassroomStore()

let kickPollInterval = null

function startKickPolling(classroomId) {
  stopKickPolling()
  kickPollInterval = setInterval(async () => {
    if (router.currentRoute.value.name === 'WaitingRoom') return
    try {
      const status = await classroomStore.fetchMyStatus(classroomId)
      if (status === 'KICKED') {
        console.warn('[App] Student was kicked from classroom — redirecting to join')
        stopKickPolling()
        classroomStore.reset()
        router.push({ name: 'JoinClassroom' })
      }
    } catch (err) {
      console.warn('[App] Kick poll failed:', err?.message)
    }
  }, 10000)
}

function stopKickPolling() {
  if (kickPollInterval) {
    clearInterval(kickPollInterval)
    kickPollInterval = null
  }
}

watch(
  () => [authStore.isStudent, classroomStore.currentClassroomId],
  ([isStudent, classroomId]) => {
    if (isStudent && classroomId) {
      startKickPolling(classroomId)
    } else {
      stopKickPolling()
    }
  },
  { immediate: true }
)

onUnmounted(stopKickPolling)
</script>
