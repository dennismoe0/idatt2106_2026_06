import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  // Public
  { path: '/login',         name: 'Login',        component: () => import('@/views/auth/LoginView.vue'),              meta: { public: true } },
  { path: '/register',      name: 'Register',     component: () => import('@/views/auth/RegisterView.vue'),           meta: { public: true } },
  { path: '/unauthorized',  name: 'Unauthorized', component: () => import('@/views/UnauthorizedView.vue'),            meta: { public: true } },
  { path: '/student-login', name: 'StudentLogin', component: () => import('@/views/auth/StudentLoginView.vue'),       meta: { public: true } },

  // Student
  { path: '/',              name: 'Home',           component: () => import('@/views/student/HomeView.vue'),          meta: { role: 'STUDENT' } },
  { path: '/intro',         name: 'Intro',          component: () => import('@/views/student/IntroView.vue'),         meta: { role: 'STUDENT' } },
  { path: '/join',          name: 'JoinClassroom',  component: () => import('@/views/student/JoinClassroomView.vue'), meta: { role: 'STUDENT' } },
  { path: '/waiting',       name: 'WaitingRoom',    component: () => import('@/views/student/WaitingRoomView.vue'),   meta: { role: 'STUDENT' } },
  { path: '/map',           name: 'Map',            component: () => import('@/views/student/MapView.vue'),           meta: { role: 'STUDENT' } },
  { path: '/task/:taskId?', name: 'Task',           component: () => import('@/views/student/TaskView.vue'),          meta: { role: 'STUDENT' } },
  { path: '/avatar',        name: 'Avatar',         component: () => import('@/views/student/AvatarView.vue'),        meta: { role: 'STUDENT' } },

  // Teacher
  { path: '/teacher',                 name: 'Dashboard',       component: () => import('@/views/teacher/DashboardView.vue'),       meta: { role: 'TEACHER' } },
  { path: '/teacher/classrooms/:id',  name: 'ClassroomDetail', component: () => import('@/views/teacher/ClassroomDetailView.vue'), meta: { role: 'TEACHER' } },

  // Fallback
  { path: '/:pathMatch(.*)*', redirect: '/login' }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach((to) => {
  const auth = useAuthStore()

  if (to.meta.public) return true

  if (!auth.isAuthenticated) {
    console.log('[router] Unauthenticated — redirecting to login from', to.path)
    return { name: 'Login' }
  }

  // Authenticated but no role — token is corrupt; treat as unauthenticated
  if (!auth.role) {
    console.warn('[router] Authenticated but no role — token corrupt, redirecting to login')
    return { name: 'Login' }
  }

  if (to.meta.role && auth.role !== to.meta.role) {
    if (auth.role === 'TEACHER') {
      console.log('[router] Teacher redirected from', to.path, 'to /teacher')
      return { name: 'Dashboard' }
    }
    if (auth.role === 'STUDENT') {
      console.log('[router] Student redirected from', to.path, 'to /')
      return { name: 'Home' }
    }
    console.warn('[router] Role', auth.role, 'not allowed on', to.path)
    return { name: 'Unauthorized' }
  }

  return true
})

export default router
