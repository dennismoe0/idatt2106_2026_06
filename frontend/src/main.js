import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import './assets/styles/main.css'
import { setUnauthorizedHandler } from './services/api'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
setUnauthorizedHandler(() => {
  const target = router.currentRoute.value.meta?.role === 'TEACHER'
    ? { name: 'TeacherLogin' }
    : { name: 'StudentLogin' }
  router.push(target)
})

import { useAuthStore } from './stores/auth'
const authStore = useAuthStore()
authStore.rehydrate()

import { useClassroomStore } from './stores/classroom'
const classroomStore = useClassroomStore()
classroomStore.rehydrate()

app.mount('#app')
