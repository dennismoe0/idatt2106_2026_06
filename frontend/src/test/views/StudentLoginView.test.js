import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createMemoryHistory } from 'vue-router'
import StudentLoginView from '@/views/auth/StudentLoginView.vue'
import { useAuthStore } from '@/stores/auth'
import { useClassroomStore } from '@/stores/classroom'

const TestHome = { template: '<div>Home</div>' }
const TestIntro = { template: '<div>Intro</div>' }
const TestTeacherLogin = { template: '<div>Teacher login</div>' }
const TestJoin = { template: '<div>Join</div>' }
const TestWaiting = { template: '<div>Waiting</div>' }

function makeRouter() {
  return createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/login', name: 'StudentLogin', component: StudentLoginView },
      { path: '/', name: 'Home', component: TestHome },
      { path: '/intro', name: 'Intro', component: TestIntro },
      { path: '/teacher-login', name: 'TeacherLogin', component: TestTeacherLogin },
      { path: '/join', name: 'JoinClassroom', component: TestJoin },
      { path: '/waiting', name: 'WaitingRoom', component: TestWaiting },
    ],
  })
}

async function mountView() {
  const pinia = createPinia()
  setActivePinia(pinia)

  const router = makeRouter()
  await router.push('/login')
  await router.isReady()

  const wrapper = mount(StudentLoginView, {
    global: {
      plugins: [router, pinia],
    },
  })

  return {
    wrapper,
    router,
    authStore: useAuthStore(),
    classroomStore: useClassroomStore(),
  }
}

describe('StudentLoginView', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.restoreAllMocks()
  })

  it('shows error when username is empty', async () => {
    const { wrapper } = await mountView()

    await wrapper.find('form').trigger('submit')

    expect(wrapper.text()).toContain('Elevnavn er påkrevd')
  })

  it('shows a visible teacher login link', async () => {
    const { wrapper } = await mountView()

    const teacherLink = wrapper.get('.feide-teacher-link')

    expect(teacherLink.text()).toContain('Gå til lærerinnlogging')
    expect(teacherLink.attributes('href')).toBe('/teacher-login')
  })

  it('calls studentLogin with trimmed username', async () => {
    const { wrapper, authStore, classroomStore } = await mountView()
    authStore.studentLogin = vi.fn().mockResolvedValue({})
    classroomStore.fetchMyClassroom = vi.fn().mockResolvedValue(undefined)
    // Simulate student has a classroom and is approved so routing is predictable
    classroomStore.currentClassroomId = 1
    classroomStore.approvalStatus = 'APPROVED'

    await wrapper.find('input').setValue('  agent.nora  ')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(authStore.studentLogin).toHaveBeenCalledWith('agent.nora')
  })

  it('routes to intro after login when intro has not been seen', async () => {
    const { wrapper, router, authStore, classroomStore } = await mountView()
    authStore.studentLogin = vi.fn().mockResolvedValue({})
    classroomStore.fetchMyClassroom = vi.fn().mockResolvedValue(undefined)
    // Student is in an approved classroom, so routing falls through to intro check
    classroomStore.currentClassroomId = 1
    classroomStore.approvalStatus = 'APPROVED'

    await wrapper.find('input').setValue('agent.nora')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(router.currentRoute.value.name).toBe('Intro')
  })

  it('routes to home after login when intro has been seen', async () => {
    localStorage.setItem('hasSeenIntro', 'true')

    const { wrapper, router, authStore, classroomStore } = await mountView()
    authStore.studentLogin = vi.fn().mockResolvedValue({})
    classroomStore.fetchMyClassroom = vi.fn().mockResolvedValue(undefined)
    // Student is in an approved classroom, so routing falls through to hasSeenIntro check
    classroomStore.currentClassroomId = 1
    classroomStore.approvalStatus = 'APPROVED'

    await wrapper.find('input').setValue('agent.nora')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(router.currentRoute.value.name).toBe('Home')
  })
})
