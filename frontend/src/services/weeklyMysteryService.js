import api from './api'

export const weeklyMysteryService = {
  submitMystery(classroomId, title, description, imageUrl) {
    console.log('[weeklyMysteryService] submitMystery classroomId:', classroomId, 'title:', title)
    return api
      .post('/api/weekly-mysteries/submissions', { classroomId, title, description, imageUrl })
      .then(res => res.data)
      .catch(err => {
        console.error('[weeklyMysteryService] submitMystery failed:', err)
        throw err
      })
  },

  getActive(classroomId) {
    console.log('[weeklyMysteryService] getActive classroomId:', classroomId)
    return api
      .get('/api/weekly-mysteries/active', { params: { classroomId } })
      .then(res => (res.status === 204 ? null : res.data))
      .catch(err => {
        console.error('[weeklyMysteryService] getActive failed:', err)
        throw err
      })
  },

  complete(classroomId, answer) {
    console.log('[weeklyMysteryService] complete classroomId:', classroomId)
    return api
      .post('/api/weekly-mysteries/active/complete', { classroomId, answer })
      .then(res => res.data)
      .catch(err => {
        console.error('[weeklyMysteryService] complete failed:', err)
        throw err
      })
  },

  // Teacher functions

  getSubmissions(classroomId) {
    console.log('[weeklyMysteryService] getSubmissions classroomId:', classroomId)
    return api
      .get('/api/weekly-mysteries/submissions', { params: { classroomId } })
      .then(res => res.data)
      .catch(err => {
        console.error('[weeklyMysteryService] getSubmissions failed:', err)
        throw err
      })
  },

  editMystery(id, editDto) {
    console.log('[weeklyMysteryService] editMystery id:', id)
    return api
      .put(`/api/weekly-mysteries/${id}`, editDto)
      .then(res => res.data)
      .catch(err => {
        console.error('[weeklyMysteryService] editMystery failed:', err)
        throw err
      })
  },

  activateMystery(id, classroomId) {
    console.log('[weeklyMysteryService] activateMystery id:', id, 'classroomId:', classroomId)
    return api
      .put(`/api/weekly-mysteries/${id}/activate`, null, { params: { classroomId } })
      .then(res => res.data)
      .catch(err => {
        console.error('[weeklyMysteryService] activateMystery failed:', err)
        throw err
      })
  },

  rejectMystery(id) {
    console.log('[weeklyMysteryService] rejectMystery id:', id)
    return api
      .put(`/api/weekly-mysteries/${id}/reject`)
      .then(res => res.data)
      .catch(err => {
        console.error('[weeklyMysteryService] rejectMystery failed:', err)
        throw err
      })
  },
}
