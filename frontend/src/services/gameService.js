import api from './api'

export const gameService = {
  getStops: (classroomId) =>
    api.get('/api/game/stops', { params: { classroomId } }),

  getTasks: (stopId, classroomId) =>
    api.get(`/api/game/stops/${stopId}/tasks`, { params: { classroomId } }),

  getTask: (taskId, classroomId) =>
    api.get(`/api/game/tasks/${taskId}`, { params: { classroomId } }),

  submitAnswer: (taskId, classroomId, answer) =>
    api.post(`/api/game/tasks/${taskId}/submit`, { classroomId, answer }),

  getProgress: (classroomId) =>
    api.get('/api/game/progress', { params: { classroomId } }),
}
