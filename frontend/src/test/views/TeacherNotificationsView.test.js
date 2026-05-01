import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'

const push = vi.fn()
const fetchNotifications = vi.fn()
const fetchUnreadCount = vi.fn()
const markAsRead = vi.fn()
const markAllAsRead = vi.fn()
const deleteOldNotifications = vi.fn()
const updateStudentStatus = vi.fn()
const recentCreatedAt = new Date().toISOString()

vi.mock('vue-router', () => ({
  useRouter: () => ({ push }),
  RouterLink: { template: '<a><slot /></a>' }
}))

vi.mock('@/stores/notification', () => ({
  useNotificationStore: () => ({
    notifications: [
      {
        id: 1,
        type: 'STUDENT_JOIN_REQUEST',
        message: 'Elev vil bli med',
        referenceId: 11,
        classroomId: 2,
        studentId: null,
        isRead: false,
        createdAt: recentCreatedAt
      },
      {
        id: 2,
        type: 'MYSTERY_SUBMITTED',
        message: 'Nytt mysterium',
        referenceId: 99,
        classroomId: 3,
        studentId: 12,
        isRead: true,
        createdAt: recentCreatedAt
      }
    ],
    unreadCount: 1,
    loading: false,
    error: null,
    fetchNotifications,
    fetchUnreadCount,
    markAsRead,
    markAllAsRead,
    deleteOldNotifications
  })
}))

vi.mock('@/stores/classroom', () => ({
  useClassroomStore: () => ({ updateStudentStatus })
}))

vi.mock('@/stores/auth', () => ({
  useAuthStore: () => ({
    email: 'teacher@test.no',
    logout: vi.fn()
  })
}))

vi.mock('@/stores/school', () => ({
  useSchoolStore: () => ({
    school: { name: 'Testskole' }
  })
}))

async function mountView() {
  setActivePinia(createPinia())
  const { default: TeacherNotificationsView } = await import('@/views/teacher/TeacherNotificationsView.vue')
  const wrapper = mount(TeacherNotificationsView, {
    global: {
      stubs: {
        RouterLink: { template: '<a><slot /></a>' }
      }
    }
  })
  await flushPromises()
  return wrapper
}

describe('TeacherNotificationsView', () => {
  beforeEach(() => {
    vi.resetModules()
    vi.clearAllMocks()
  })

  it('loads and renders notifications with unread state', async () => {
    const wrapper = await mountView()

    expect(fetchNotifications).toHaveBeenCalled()
    expect(fetchUnreadCount).toHaveBeenCalled()
    expect(wrapper.text()).toContain('Elev vil bli med')
    expect(wrapper.find('.notification-card.unread').exists()).toBe(true)
  })

  it('keeps the teacher sidebar visible', async () => {
    const wrapper = await mountView()

    expect(wrapper.find('.sidebar').exists()).toBe(true)
    expect(wrapper.find('[data-testid="notifications-link"]').exists()).toBe(true)
  })

  it('approves join requests before marking the notification read', async () => {
    const wrapper = await mountView()

    await wrapper.find('[data-testid="approve-notification-1"]').trigger('click')
    await flushPromises()

    expect(updateStudentStatus).toHaveBeenCalledWith(2, 11, 'APPROVED')
    expect(markAsRead).toHaveBeenCalledWith(1)
  })

  it('denies join requests before marking the notification read', async () => {
    const wrapper = await mountView()

    await wrapper.find('[data-testid="deny-notification-1"]').trigger('click')
    await flushPromises()

    expect(updateStudentStatus).toHaveBeenCalledWith(2, 11, 'KICKED')
    expect(markAsRead).toHaveBeenCalledWith(1)
  })

  it('routes mystery notifications to weekly mystery management', async () => {
    const wrapper = await mountView()

    await wrapper.find('[data-testid="view-notification-2"]').trigger('click')

    expect(push).toHaveBeenCalledWith({ name: 'WeeklyMysteryManage', params: { classroomId: 3 } })
  })
})
