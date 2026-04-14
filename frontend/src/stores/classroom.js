import { ref } from 'vue'
import { defineStore } from 'pinia'

export const useClassroomStore = defineStore('classroom', () => {
  const classrooms = ref([])
  const currentClassroom = ref(null)
  const students = ref([])

  async function fetchMyClassrooms() { }
  async function createClassroom(data) { }
  async function joinClassroom(data) { }
  async function fetchStudents(classroomId) { }
  async function updateStudentStatus(classroomId, studentId, status) { }

  return { classrooms, currentClassroom, students,
    fetchMyClassrooms, createClassroom, joinClassroom,
    fetchStudents, updateStudentStatus }
})
