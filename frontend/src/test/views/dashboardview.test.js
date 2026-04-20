import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createMemoryHistory } from 'vue-router'

//Stub child components
vi.mock('@/components/common/LoadingSpinner.vue', () => ({
  default: { template: '<span data-testid="spinner" />' }
}))
vi.mock('@/components/common/BaseModal.vue', () => ({
  default: {
    props: ['title'],
    template: '<div data-testid="modal"><slot name="header" /><slot /></div>'
  }
}))

//Minimal router so router-link resolves
const router = createRouter({
  history: createMemoryHistory(),
  routes: [
    { path: '/teacher', name: 'Dashboard', component: { template: '<div />' } },
    { path: '/teacher/classrooms/:id', name: 'ClassroomDetail', component: { template: '<div />' } }
  ]
})

//Shared mount helper
async function mountDashboard () {
  const pinia = createPinia()
  setActivePinia(pinia)
  const { default: DashboardView } = await import('@/views/teacher/DashboardView.vue')
  return mount(DashboardView, {
    global: {
      plugins: [
        router,
        pinia
      ],
      stubs: { RouterLink: true }
    }
  })
}

describe('DashboardView', () => {
  beforeEach(() => {
    vi.resetModules()
  })

  //Error banner
  describe('when fetchMyClassrooms fails', () => {
    it('renders the error banner and hides the loading spinner', async () => {
      vi.doMock('@/stores/auth', () => ({
        useAuthStore: () => ({
          user: { email: 'teacher@test.no' },
          logout: vi.fn()
        })
      }))
      vi.doMock('@/stores/classroom', () => ({
        useClassroomStore: () => ({
          classrooms: [],
          fetchMyClassrooms: vi.fn().mockRejectedValue(new Error('network error'))
        })
      }))
      vi.doMock('@/stores/school', () => ({
        useSchoolStore: () => ({
          schools: [],
          fetchSchools: vi.fn().mockResolvedValue([])
        })
      }))

      const wrapper = await mountDashboard()
      await flushPromises()

      expect(wrapper.find('.error-banner').exists()).toBe(true)
      expect(wrapper.find('.error-banner').text()).toContain('Kunne ikke laste klasserom')
      expect(wrapper.find('[data-testid="spinner"]').exists()).toBe(false)
    })
  })

  //Successful create shows join code
  describe('when submitCreate succeeds', () => {
    it('shows the join code view after a successful classroom creation', async () => {
      const fakeClassroom = {
        id: '1',
        name: 'Testklasse',
        joinCode: 'ABC123',
        createdAt: new Date().toISOString()
      }

      vi.doMock('@/stores/auth', () => ({
        useAuthStore: () => ({
          user: { email: 'teacher@test.no' },
          logout: vi.fn()
        })
      }))
      vi.doMock('@/stores/classroom', () => ({
        useClassroomStore: () => ({
          classrooms: [],
          fetchMyClassrooms: vi.fn().mockResolvedValue(undefined),
          createClassroom: vi.fn().mockResolvedValue(fakeClassroom)
        })
      }))
      vi.doMock('@/stores/school', () => ({
        useSchoolStore: () => ({
          schools: [],
          fetchSchools: vi.fn().mockResolvedValue([])
        })
      }))

      const wrapper = await mountDashboard()
      await flushPromises()

      await wrapper.find('.btn-primary').trigger('click')
      await flushPromises()

      await wrapper.find('input[type="text"]').setValue('Testklasse')

      await wrapper.find('.create-form').trigger('submit')
      await flushPromises()

      expect(wrapper.find('.join-code-display').exists()).toBe(true)
      expect(wrapper.find('.join-code-text').text()).toBe('ABC123')
      expect(wrapper.find('.success-msg').text()).toContain('Testklasse')
    })
  })
})
