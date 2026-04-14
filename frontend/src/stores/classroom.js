import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/services/api'

export const useClassroomStore = defineStore('classroom', () => {
  const currentClassroom = ref(null)
  const pendingJoin = ref(null)

  async function joinClassroom({ code, displayName }) {
    const payload = {
      code: code.trim().toUpperCase(),
      displayName: displayName.trim()
    }

    const { data } = await api.post('/api/classrooms/join', payload)
    currentClassroom.value = data?.classroom ?? null
    pendingJoin.value = {
      code: payload.code,
      displayName: payload.displayName,
      ...data
    }

    return data
  }

  return { currentClassroom, pendingJoin, joinClassroom }
})
