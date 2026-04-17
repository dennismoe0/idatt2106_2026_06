import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createMemoryHistory } from 'vue-router'
import StudentLoginView from '@/views/auth/StudentLoginView.vue'
import { useAuthStore } from '@/stores/auth'

const TestHome = { template: '<div>Home</div>' }
const TestIntro = { template: '<div>Intro</div>' }
const TestLogin = { template: '<div>Login</div>' }

function makeRouter() {
  return createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/student-login', name: 'StudentLogin', component: StudentLoginView },
      { path: '/', name: 'Home', component: TestHome },
      { path: '/intro', name: 'Intro', component: TestIntro },
      { path: '/login', name: 'Login', component: TestLogin },
    ],
  })
}

async function mountView() {
  const pinia = createPinia()
  setActivePinia(pinia)

  const router = makeRouter()
  await router.push('/student-login')
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

    expect(wrapper.text()).toContain('Brukernavn er påkrevd')
  })

  it('calls studentLogin with trimmed username', async () => {
    const { wrapper, authStore } = await mountView()
    authStore.studentLogin = vi.fn().mockResolvedValue({})

    await wrapper.find('input').setValue('  agent.nora  ')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(authStore.studentLogin).toHaveBeenCalledWith('agent.nora')
  })

  it('routes to intro after login when intro has not been seen', async () => {
    const { wrapper, router, authStore } = await mountView()
    authStore.studentLogin = vi.fn().mockResolvedValue({})

    await wrapper.find('input').setValue('agent.nora')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(router.currentRoute.value.name).toBe('Intro')
  })

  it('routes to home after login when intro has been seen', async () => {
    localStorage.setItem('hasSeenIntro', 'true')

    const { wrapper, router, authStore } = await mountView()
    authStore.studentLogin = vi.fn().mockResolvedValue({})

    await wrapper.find('input').setValue('agent.nora')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(router.currentRoute.value.name).toBe('Home')
  })
})
