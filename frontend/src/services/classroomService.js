import api from './api'

export const classroomService = {
  getMyClassrooms: () =>
    api.get('/api/classrooms'),

  createClassroom: (data) =>
    api.post('/api/classrooms', data),

  joinClassroom: (data) =>
    api.post('/api/classrooms/join', data),

  getStudents: (classroomId) =>
    api.get(`/api/classrooms/${classroomId}/students`),

  updateStudentStatus: (classroomId, studentId, status) =>
    api.put(`/api/classrooms/${classroomId}/students/${studentId}`, { status }),

  getStops: (classroomId) =>
    api.get(`/api/classrooms/${classroomId}/stops`),

  getMyStatus: (classroomId) =>
    api.get(`/api/classrooms/${classroomId}/my-status`),

  getMyClassroom: () =>
    api.get('/api/classrooms/mine'),

  updateMyDisplayName: (classroomId, displayName) =>
    api.put(`/api/classrooms/${classroomId}/my-displayname`, { displayName }),

  setMusicMuted: (classroomId, musicMuted) =>
    api.put(`/api/classrooms/${classroomId}/music-muted`, { musicMuted }),
}
