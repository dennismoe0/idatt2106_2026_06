import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createMemoryHistory } from 'vue-router'
import WaitingRoomView from '@/views/student/WaitingRoomView.vue'
import { useClassroomStore } from '@/stores/classroom'

vi.mock('@/services/classroomService', () => ({
  classroomService: {
    getMyStatus: vi.fn(),
    getMyClassrooms: vi.fn(),
    createClassroom: vi.fn(),
    joinClassroom: vi.fn(),
    getStudents: vi.fn(),
    updateStudentStatus: vi.fn(),
  },
}))

const router = createRouter({
  history: createMemoryHistory(),
  routes: [
    { path: '/waiting', name: 'WaitingRoom', component: WaitingRoomView },
    { path: '/', name: 'Home', component: { template: '<div>Home</div>' } },
    { path: '/join', name: 'JoinClassroom', component: { template: '<div>Join</div>' } },
  ],
})

describe('WaitingRoomView', () => {
  let classroomStore

  beforeEach(async () => {
    setActivePinia(createPinia())
    classroomStore = useClassroomStore()
    vi.useFakeTimers()
    vi.clearAllMocks()
    await router.push('/waiting')
    await router.isReady()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  function mountView() {
    return mount(WaitingRoomView, {
      global: { plugins: [router] },
    })
  }

  it('redirects to JoinClassroom when pendingJoin is null', async () => {
    classroomStore.pendingJoin = null

    const wrapper = mountView()
    await flushPromises()

    expect(router.currentRoute.value.name).toBe('JoinClassroom')
    wrapper.unmount()
  })

  it('shows waiting message when pending', async () => {
    classroomStore.pendingJoin = { classroomId: 7, displayName: 'Agent Ola', code: 'tiger-blue' }
    vi.spyOn(classroomStore, 'fetchMyStatus').mockResolvedValue('PENDING')

    const wrapper = mountView()
    await flushPromises()

    expect(wrapper.text()).toContain('Venter på godkjenning')
    expect(wrapper.text()).toContain('Agent Ola')
    wrapper.unmount()
  })

  it('polls fetchMyStatus on the 3-second interval', async () => {
    classroomStore.pendingJoin = { classroomId: 7, displayName: 'Agent Ola', code: 'tiger-blue' }
    const fetchSpy = vi.spyOn(classroomStore, 'fetchMyStatus').mockResolvedValue('PENDING')

    const wrapper = mountView()
    await flushPromises()

    vi.advanceTimersByTime(3000)
    await flushPromises()
    expect(fetchSpy).toHaveBeenCalledTimes(1)
    expect(fetchSpy).toHaveBeenCalledWith(7)

    vi.advanceTimersByTime(3000)
    await flushPromises()
    expect(fetchSpy).toHaveBeenCalledTimes(2)

    wrapper.unmount()
  })

  it('redirects to Home when status is APPROVED', async () => {
    classroomStore.pendingJoin = { classroomId: 7, displayName: 'Agent Ola', code: 'tiger-blue' }
    vi.spyOn(classroomStore, 'fetchMyStatus').mockResolvedValue('APPROVED')

    const wrapper = mountView()
    vi.advanceTimersByTime(3000)
    await flushPromises()

    expect(router.currentRoute.value.name).toBe('Home')
    wrapper.unmount()
  })

  it('shows kicked message and stops polling when status is KICKED', async () => {
    classroomStore.pendingJoin = { classroomId: 7, displayName: 'Agent Ola', code: 'tiger-blue' }
    const fetchSpy = vi.spyOn(classroomStore, 'fetchMyStatus').mockResolvedValue('KICKED')

    const wrapper = mountView()
    vi.advanceTimersByTime(3000)
    await flushPromises()

    expect(wrapper.text()).toContain('Du ble fjernet fra klassen')

    // Interval should be cleared — no more calls after the kicked tick
    vi.advanceTimersByTime(6000)
    await flushPromises()
    expect(fetchSpy).toHaveBeenCalledTimes(1)

    wrapper.unmount()
  })

  it('clears interval on unmount', async () => {
    classroomStore.pendingJoin = { classroomId: 7, displayName: 'Agent Ola', code: 'tiger-blue' }
    vi.spyOn(classroomStore, 'fetchMyStatus').mockResolvedValue('PENDING')
    const clearSpy = vi.spyOn(globalThis, 'clearInterval')

    const wrapper = mountView()
    await flushPromises()
    wrapper.unmount()

    expect(clearSpy).toHaveBeenCalled()
  })
})
