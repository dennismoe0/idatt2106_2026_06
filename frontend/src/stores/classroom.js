import { defineStore } from 'pinia'
import { ref } from 'vue'
import { classroomService } from '@/services/classroomService'

export const useClassroomStore = defineStore('classroom', () => {
  const classrooms = ref([])
  const currentClassroom = ref(null)
  const students = ref([])
  const currentClassroomId = ref(null)
  const pendingJoin = ref(null)
  const displayName = ref(null)

  async function fetchMyClassrooms() {
    console.log('[classroom] Fetching my classrooms')
    try {
      const { data } = await classroomService.getMyClassrooms()
      classrooms.value = data
      console.log('[classroom] Fetched', data.length, 'classrooms')
    } catch (err) {
      console.error('[classroom] Failed to fetch classrooms:', err)
      throw err
    }
  }

  async function createClassroom(payload) {
    console.log('[classroom] Creating classroom:', payload.name)
    try {
      const { data } = await classroomService.createClassroom(payload)
      classrooms.value.push(data)
      console.log('[classroom] Created classroom id:', data.id, 'joinCode:', data.joinCode)
      return data
    } catch (err) {
      console.error('[classroom] Failed to create classroom:', err)
      throw err
    }
  }

  async function joinClassroom(payload) {
    console.log('[classroom] Joining classroom with code:', payload.code)
    try {
      const { data } = await classroomService.joinClassroom(payload)
      currentClassroomId.value = data.classroomId
      displayName.value = payload.displayName
      localStorage.setItem('classroomId', data.classroomId)
      localStorage.setItem('classroomDisplayName', payload.displayName)
      pendingJoin.value = {
        code: payload.code,
        displayName: payload.displayName,
        classroomId: data.classroomId,
        status: data.status
      }
      console.log('[classroom] Joined classroom id:', data.classroomId, 'status:', data.status)
      return data
    } catch (err) {
      console.error('[classroom] Failed to join classroom:', err)
      throw err
    }
  }

  async function fetchStudents(classroomId) {
    console.log('[classroom] Fetching students for classroom:', classroomId)
    try {
      const { data } = await classroomService.getStudents(classroomId)
      students.value = data
      console.log('[classroom] Fetched', data.length, 'students')
    } catch (err) {
      console.error('[classroom] Failed to fetch students:', err)
      throw err
    }
  }

  function reset() {
    classrooms.value = []
    currentClassroom.value = null
    students.value = []
    currentClassroomId.value = null
    pendingJoin.value = null
    displayName.value = null
    localStorage.removeItem('classroomId')
    localStorage.removeItem('classroomDisplayName')
    console.log('[classroom] State reset')
  }

  async function fetchMyClassroom() {
    console.log('[classroom] Fetching my classroom membership from server')
    try {
      const { data } = await classroomService.getMyClassroom()
      currentClassroomId.value = data.classroomId
      displayName.value = data.displayName
      localStorage.setItem('classroomId', data.classroomId)
      if (data.displayName) localStorage.setItem('classroomDisplayName', data.displayName)
      console.log('[classroom] Restored classroom:', data.classroomId, 'displayName:', data.displayName)
      return data
    } catch (err) {
      if (err?.response?.status === 404) {
        console.log('[classroom] Student has no approved classroom membership')
        return null
      }
      console.error('[classroom] Failed to fetch my classroom:', err)
      return null
    }
  }

  function rehydrate() {
    const storedId = localStorage.getItem('classroomId')
    const storedName = localStorage.getItem('classroomDisplayName')
    if (storedId) {
      currentClassroomId.value = parseInt(storedId, 10)
      console.log('[classroom] Rehydrated classroomId:', currentClassroomId.value)
    }
    if (storedName) {
      displayName.value = storedName
      console.log('[classroom] Rehydrated displayName:', storedName)
    }
  }

  async function fetchMyStatus(classroomId) {
    console.log('[classroom] Fetching my status for classroom:', classroomId)
    try {
      const { data } = await classroomService.getMyStatus(classroomId)
      if (pendingJoin.value?.classroomId === classroomId) {
        pendingJoin.value = {
          ...pendingJoin.value,
          status: data.status
        }
      }
      console.log('[classroom] My status is:', data.status)
      return data.status
    } catch (err) {
      console.error('[classroom] Failed to fetch my status:', err)
      throw err
    }
  }

  async function updateStudentStatus(classroomId, studentId, status) {
    console.log('[classroom] Updating student', studentId, 'to', status)
    try {
      const { data } = await classroomService.updateStudentStatus(classroomId, studentId, status)
      const idx = students.value.findIndex(s => s.userId === studentId)
      if (idx !== -1) students.value[idx] = data
      console.log('[classroom] Updated student status:', data.status)
      return data
    } catch (err) {
      console.error('[classroom] Failed to update student status:', err)
      throw err
    }
  }

  return {
    classrooms, currentClassroom, students, currentClassroomId, pendingJoin, displayName,
    fetchMyClassrooms, createClassroom, joinClassroom, fetchStudents, fetchMyStatus, updateStudentStatus, fetchMyClassroom, rehydrate, reset
  }
})
