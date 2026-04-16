import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import WaitingRoomView from '@/views/student/WaitingRoomView.vue'

const { apiGetMock, pushMock, classroomStoreMock } = vi.hoisted(() => ({
  apiGetMock: vi.fn(),
  pushMock: vi.fn(),
  classroomStoreMock: { currentClassroomId: 7 },
}))

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: pushMock }),
}))

vi.mock('@/services/api', () => ({
  default: { get: apiGetMock },
}))

vi.mock('@/stores/classroom', () => ({
  useClassroomStore: () => classroomStoreMock,
}))

describe('WaitingRoomView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.useFakeTimers()
    vi.clearAllMocks()
    classroomStoreMock.currentClassroomId = 7
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('polls the classroom-scoped student status endpoint', async () => {
    apiGetMock.mockResolvedValue({ data: { status: 'PENDING' } })

    const wrapper = mount(WaitingRoomView, {
      global: {
        stubs: {
          LoadingSpinner: { template: '<div>spinner</div>' },
        },
      },
    })

    await flushPromises()

    expect(apiGetMock).toHaveBeenCalledWith('/api/classrooms/7/students/me/status')
    expect(wrapper.text()).toContain('Du venter på godkjenning.')
    expect(wrapper.text()).toContain('Status: PENDING')

    wrapper.unmount()
  })

  it('shows an error when classroom context is missing', async () => {
    const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
    classroomStoreMock.currentClassroomId = null

    const wrapper = mount(WaitingRoomView, {
      global: {
        stubs: {
          LoadingSpinner: { template: '<div>spinner</div>' },
        },
      },
    })

    await flushPromises()

    expect(apiGetMock).not.toHaveBeenCalled()
    expect(wrapper.text()).toContain('Kunne ikke hente venteværelsesstatus.')

    consoleErrorSpy.mockRestore()
    wrapper.unmount()
  })

  it('shows the Norwegian kicked message', async () => {
    const consoleWarnSpy = vi.spyOn(console, 'warn').mockImplementation(() => {})
    apiGetMock.mockResolvedValue({ data: { status: 'KICKED' } })

    const wrapper = mount(WaitingRoomView, {
      global: {
        stubs: {
          LoadingSpinner: { template: '<div>spinner</div>' },
        },
      },
    })

    await flushPromises()

    expect(wrapper.text()).toContain('Du har blitt fjernet fra venteværelset.')

    consoleWarnSpy.mockRestore()
    wrapper.unmount()
  })

  it('redirects home when the student is approved', async () => {
    apiGetMock.mockResolvedValue({ data: { status: 'APPROVED' } })

    const wrapper = mount(WaitingRoomView, {
      global: {
        stubs: {
          LoadingSpinner: { template: '<div>spinner</div>' },
        },
      },
    })

    await flushPromises()

    expect(pushMock).toHaveBeenCalledWith('/')

    wrapper.unmount()
  })
})
