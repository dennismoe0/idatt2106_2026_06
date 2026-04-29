<template>
  <RouterView />
  <DevNav v-if="isDev && !route.meta.hideNav" />
</template>

<script setup>
import { watch, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import DevNav from '@/components/common/DevNav.vue'
import { useAuthStore } from '@/stores/auth'
import { useClassroomStore } from '@/stores/classroom'
import { useAudioStore } from '@/stores/audio'

const isDev = import.meta.env.DEV
const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const classroomStore = useClassroomStore()
const audioStore = useAudioStore()

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
      audioStore.startGlobal()
    } else {
      stopKickPolling()
      audioStore.stopGlobal()
    }
  },
  { immediate: true }
)

watch(
  () => classroomStore.musicMuted,
  (muted) => audioStore.setTeacherMuted(muted)
)

onUnmounted(stopKickPolling)
</script>
