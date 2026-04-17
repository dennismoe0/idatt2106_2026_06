import { defineStore } from 'pinia'
import { ref } from 'vue'
import { schoolService } from '@/services/schoolService'

export const useSchoolStore = defineStore('school', () => {
  const school = ref(null)
  const classrooms = ref([])

  async function fetchMySchool() {
    console.log('[school] Fetching my school')
    try {
      const { data } = await schoolService.getMySchool()
      school.value = data
      console.log('[school] School fetched:', data.id)
      return data
    } catch (err) {
      console.error('[school] Failed to fetch school:', err)
      throw err
    }
  }

  async function fetchSchoolClassrooms() {
    console.log('[school] Fetching school classrooms')
    try {
      const { data } = await schoolService.getSchoolClassrooms()
      classrooms.value = data
      console.log('[school] Classrooms fetched:', data.length)
      return data
    } catch (err) {
      console.error('[school] Failed to fetch school classrooms:', err)
      throw err
    }
  }

  async function createSchool(name) {
    console.log('[school] Creating school name:', name)
    try {
      const { data } = await schoolService.createSchool(name)
      school.value = data
      console.log('[school] School created:', data.id)
      return data
    } catch (err) {
      console.error('[school] Failed to create school:', err)
      throw err
    }
  }

  async function joinSchool(code) {
    console.log('[school] Joining school code:', code)
    try {
      const { data } = await schoolService.joinSchool(code)
      school.value = data
      console.log('[school] Joined school:', data.id)
      return data
    } catch (err) {
      console.error('[school] Failed to join school:', err)
      throw err
    }
  }

  return { school, classrooms, fetchMySchool, fetchSchoolClassrooms, createSchool, joinSchool }
})
