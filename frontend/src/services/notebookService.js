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

  createGeneralNote(content) {
    console.log('[notebookService] createGeneralNote')
    return api.post('/api/notebook/general', { content })
  },

  updateNote(id, content) {
    console.log('[notebookService] updateNote id:', id)
    return api.put(`/api/notebook/${id}`, { content })
  },

  deleteNote(id) {
    console.log('[notebookService] deleteNote id:', id)
    return api.delete(`/api/notebook/${id}`)
  },

  getStudentEntries(studentId) {
    console.log('[notebookService] getStudentEntries studentId:', studentId)
    return api.get(`/api/notebook/student/${studentId}`)
  },
}
