<<<<<<< HEAD
import { ref } from 'vue'
import { defineStore } from 'pinia'
=======
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { classroomService } from '@/services/classroomService'
>>>>>>> dev

export const useClassroomStore = defineStore('classroom', () => {
  const classrooms = ref([])
  const currentClassroom = ref(null)
  const students = ref([])
<<<<<<< HEAD

  async function fetchMyClassrooms() { }
  async function createClassroom(data) { }
  async function joinClassroom(data) { }
  async function fetchStudents(classroomId) { }
  async function updateStudentStatus(classroomId, studentId, status) { }

  return { classrooms, currentClassroom, students,
    fetchMyClassrooms, createClassroom, joinClassroom,
    fetchStudents, updateStudentStatus }
=======
  const currentClassroomId = ref(null)

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
    console.log('[classroom] Joining with code:', payload.code)
    try {
      const { data } = await classroomService.joinClassroom(payload)
      currentClassroomId.value = data.classroomId
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
    classrooms, currentClassroom, students, currentClassroomId,
    fetchMyClassrooms, createClassroom, joinClassroom, fetchStudents, updateStudentStatus
  }
>>>>>>> dev
})
