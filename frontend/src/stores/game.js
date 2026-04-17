import { defineStore } from 'pinia'
import { ref } from 'vue'
import { gameService } from '@/services/gameService'

export const useGameStore = defineStore('game', () => {
  const stops = ref([])
  const tasks = ref([])
  const currentTask = ref(null)
  const progress = ref(null)
  const medals = ref([])
  const leaderboard = ref([])

  async function fetchStops(classroomId) {
    console.log('[game] Fetching stops for classroom:', classroomId)
    try {
      const { data } = await gameService.getStops(classroomId)
      stops.value = data
      console.log('[game] Fetched', data.length, 'stops')
    } catch (err) {
      console.error('[game] Failed to fetch stops:', err)
      throw err
    }
  }

  async function fetchTasks(stopId, classroomId) {
    console.log('[game] Fetching tasks for stop:', stopId)
    try {
      const { data } = await gameService.getTasks(stopId, classroomId)
      tasks.value = data
      console.log('[game] Fetched', data.length, 'tasks for stop:', stopId)
      return data
    } catch (err) {
      console.error('[game] Failed to fetch tasks:', err)
      throw err
    }
  }

  async function fetchTask(taskId, classroomId) {
    console.log('[game] Fetching task:', taskId)
    try {
      const { data } = await gameService.getTask(taskId, classroomId)
      currentTask.value = data
      console.log('[game] Fetched task:', taskId, 'type:', data.taskType)
      return data
    } catch (err) {
      console.error('[game] Failed to fetch task:', err)
      throw err
    }
  }

  async function submitAnswer(taskId, answer, classroomId) {
    console.log('[game] Submitting answer for task:', taskId)
    try {
      const { data } = await gameService.submitAnswer(taskId, answer, classroomId)
      console.log('[game] Answer result — correct:', data.correct, 'stopCompleted:', data.stopCompleted)
      return data
    } catch (err) {
      console.error('[game] Failed to submit answer:', err)
      throw err
    }
  }

  async function fetchProgress(classroomId) {
    console.log('[game] Fetching progress for classroom:', classroomId)
    try {
      const { data } = await gameService.getProgress(classroomId)
      progress.value = data
      console.log('[game] Progress fetched')
      return data
    } catch (err) {
      console.error('[game] Failed to fetch progress:', err)
      throw err
    }
  }

  async function fetchMedals() {
    console.log('[game] Fetching earned medals')
    try {
      const { data } = await gameService.getMedals()
      medals.value = data
      console.log('[game] Fetched', data.length, 'medals')
      return data
    } catch (err) {
      console.error('[game] Failed to fetch medals:', err)
      throw err
    }
  }

  async function fetchAllMedals() {
    console.log('[game] Fetching all medals')
    try {
      const { data } = await gameService.getAllMedals()
      console.log('[game] Fetched', data.length, 'total medals')
      return data
    } catch (err) {
      console.error('[game] Failed to fetch all medals:', err)
      throw err
    }
  }

  async function fetchLeaderboard(classroomId) {
    console.log('[game] Fetching leaderboard for classroom:', classroomId)
    try {
      const { data } = await gameService.getLeaderboard(classroomId)
      leaderboard.value = data
      console.log('[game] Fetched leaderboard:', data.length, 'entries')
      return data
    } catch (err) {
      console.error('[game] Failed to fetch leaderboard:', err)
      throw err
    }
  }

  return {
    stops, tasks, currentTask, progress, medals, leaderboard,
    fetchStops, fetchTasks, fetchTask, submitAnswer, fetchProgress,
    fetchMedals, fetchAllMedals, fetchLeaderboard
  }
})
