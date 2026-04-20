import api from './api'

export const schoolService = {
  createSchool(name) {
    console.log('[schoolService] createSchool name:', name)
    return api.post('/api/schools', { name })
  },

  joinSchool(code) {
    console.log('[schoolService] joinSchool code:', code)
    return api.post('/api/schools/join', { code })
  },

  getMySchool() {
    console.log('[schoolService] getMySchool')
    return api.get('/api/schools/mine')
  },

  getSchoolClassrooms() {
    console.log('[schoolService] getSchoolClassrooms')
    return api.get('/api/schools/mine/classrooms')
  },
}
