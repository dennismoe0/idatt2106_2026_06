import api from './api'

export const notebookService = {
  getEntries() {
    console.log('[notebookService] getEntries')
    return api.get('/api/notebook')
  },

  createReflection(stopId, content) {
    console.log('[notebookService] createReflection stopId:', stopId)
    return api.post('/api/notebook', { stopId, content })
  },

  getStudentEntries(studentId) {
    console.log('[notebookService] getStudentEntries studentId:', studentId)
    return api.get(`/api/notebook/student/${studentId}`)
  },
}
