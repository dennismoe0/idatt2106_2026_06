import { describe, it, expect, beforeEach } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createMemoryHistory } from 'vue-router'
import HomeView from '@/views/student/HomeView.vue'
import CorkboardCard from '@/components/student/CorkboardCard.vue'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createMemoryHistory(),
  routes: [
    { path: '/', name: 'Home', component: HomeView },
    { path: '/map', name: 'Map', component: { template: '<div>Map</div>' } },
    { path: '/medals', name: 'Medals', component: { template: '<div>Medals</div>' } },
    { path: '/notebook', name: 'Notebook', component: { template: '<div>Notebook</div>' } },
    { path: '/profile', name: 'Profile', component: { template: '<div>Profile</div>' } },
    { path: '/leaderboard', name: 'Leaderboard', component: { template: '<div>Leaderboard</div>' } },
    { path: '/avatar', name: 'Avatar', component: { template: '<div>Avatar</div>' } },
    { path: '/intro', name: 'Intro', component: { template: '<div>Intro</div>' } },
    { path: '/student-login', name: 'StudentLogin', component: { template: '<div>Student login</div>' } },
  ],
})

describe('HomeView', () => {
  beforeEach(async () => {
    setActivePinia(createPinia())
    localStorage.setItem('hasSeenIntro', 'true')
    await router.push({ name: 'Home' })
    await router.isReady()
  })

  function mountHomeView() {
    return mount(HomeView, {
      global: {
        plugins: [router],
      },
    })
  }

  it('renders the student display name and seven corkboard cards', () => {
    const authStore = useAuthStore()
    authStore.email = 'agent.elev@student.local'

    const wrapper = mountHomeView()

    expect(wrapper.text()).toContain('Hei, Agent Elev')
    expect(wrapper.findAllComponents(CorkboardCard)).toHaveLength(7)
    expect(wrapper.text()).toContain('Kart')
    expect(wrapper.text()).toContain('Ukens Mysterium')
    expect(wrapper.text()).toContain('Kommer snart')
  })

  it('shows Kommer snart on locked cards', () => {
    const wrapper = mountHomeView()
    const lockedCards = wrapper
      .findAllComponents(CorkboardCard)
      .filter((component) => component.props('locked'))

    expect(lockedCards.length).toBeGreaterThan(0)
    lockedCards.forEach((component) => {
      expect(component.text()).toContain('Kommer snart')
    })
  })

  it('formatDisplayName handles plain email', () => {
    const authStore = useAuthStore()
    authStore.email = 'ole.hansen@gmail.com'

    const wrapper = mountHomeView()

    expect(wrapper.text()).toContain('Hei, Ole Hansen')
  })

  it('logs out and redirects to student login from the header button', async () => {
    const authStore = useAuthStore()
    authStore.token = 'token'
    authStore.email = 'agent.elev@student.local'

    const wrapper = mountHomeView()

    await wrapper.get('button.home-view__logout').trigger('click')
    await flushPromises()

    expect(authStore.isAuthenticated).toBe(false)
    expect(authStore.email).toBe(null)
    expect(router.currentRoute.value.name).toBe('StudentLogin')
  })
})
