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

  getMyStatus: (classroomId) =>
    api.get(`/api/classrooms/${classroomId}/my-status`),

  updateStudentStatus: (classroomId, studentId, status) =>
    api.put(`/api/classrooms/${classroomId}/students/${studentId}`, { status }),
}
