import { defineStore } from 'pinia'
import { ref } from 'vue'
import { gameService } from '@/services/gameService'

export const useGameStore = defineStore('game', () => {
  const stops = ref([])
  const tasks = ref([])
  const currentTask = ref(null)
  const progress = ref(null)

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

  async function submitAnswer(taskId, classroomId, answer) {
    console.log('[game] Submitting answer for task:', taskId)
    try {
      const { data } = await gameService.submitAnswer(taskId, classroomId, answer)
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

  return {
    stops, tasks, currentTask, progress,
    fetchStops, fetchTasks, fetchTask, submitAnswer, fetchProgress
  }
})
