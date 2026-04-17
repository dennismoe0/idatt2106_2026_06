import api from './api'

export const gameService = {
  getStops(classroomId) {
    console.log('[gameService] getStops classroomId:', classroomId)
    return api.get('/api/game/stops', { params: { classroomId } })
  },

  getTasks(stopId, classroomId) {
    console.log('[gameService] getTasks stopId:', stopId, 'classroomId:', classroomId)
    return api.get(`/api/game/stops/${stopId}/tasks`, { params: { classroomId } })
  },

  getTask(taskId, classroomId) {
    console.log('[gameService] getTask taskId:', taskId, 'classroomId:', classroomId)
    return api.get(`/api/game/tasks/${taskId}`, { params: { classroomId } })
  },

  submitAnswer(taskId, answer, classroomId) {
    console.log('[gameService] submitAnswer taskId:', taskId, 'classroomId:', classroomId, 'answer:', answer)
    return api.post(`/api/game/tasks/${taskId}/submit`, { answer }, { params: { classroomId } })
  },

  getProgress(classroomId) {
    console.log('[gameService] getProgress classroomId:', classroomId)
    return api.get('/api/game/progress', { params: { classroomId } })
  },

  getMedals() {
    console.log('[gameService] getMedals')
    return api.get('/api/medals/mine')
  },

  getAllMedals() {
    console.log('[gameService] getAllMedals')
    return api.get('/api/medals/all')
  },

  getLeaderboard(classroomId) {
    console.log('[gameService] getLeaderboard classroomId:', classroomId)
    return api.get(`/api/classrooms/${classroomId}/leaderboard`)
  },
}
