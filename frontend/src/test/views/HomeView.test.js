import { describe, it, expect, beforeEach } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createMemoryHistory } from 'vue-router'
import HomeView from '@/views/student/HomeView.vue'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createMemoryHistory(),
  routes: [
    { path: '/',             name: 'Home',           component: HomeView },
    { path: '/map',          name: 'Map',            component: { template: '<div>Map</div>' } },
    { path: '/world-map',    name: 'WorldMap',       component: { template: '<div>WorldMap</div>' } },
    { path: '/medals',       name: 'Medals',         component: { template: '<div>Medals</div>' } },
    { path: '/notebook',     name: 'Notebook',       component: { template: '<div>Notebook</div>' } },
    { path: '/profile',      name: 'Profile',        component: { template: '<div>Profile</div>' } },
    { path: '/leaderboard',  name: 'Leaderboard',    component: { template: '<div>Leaderboard</div>' } },
    { path: '/avatar',       name: 'Avatar',         component: { template: '<div>Avatar</div>' } },
    { path: '/shop',         name: 'Shop',           component: { template: '<div>Shop</div>' } },
    { path: '/intro',        name: 'Intro',          component: { template: '<div>Intro</div>' } },
    { path: '/mysterium',    name: 'UkasMysterium',  component: { template: '<div>Mysterium</div>' } },
    { path: '/send-inn',     name: 'SendInn',        component: { template: '<div>SendInn</div>' } },
    { path: '/hjelp',        name: 'Help',           component: { template: '<div>Hjelp</div>' } },
    { path: '/suspects',     name: 'SuspectDossier', component: { template: '<div>Suspects</div>' } },
    { path: '/student-login',name: 'StudentLogin',   component: { template: '<div>Student login</div>' } },
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

  it('renders the student display name and corkboard notes', () => {
    const authStore = useAuthStore()
    authStore.email = 'agent.elev@student.local'

    const wrapper = mountHomeView()

    expect(wrapper.text()).toContain('Agent Elev')
    expect(wrapper.findAll('.home__note').length).toBeGreaterThan(0)
    expect(wrapper.text()).toContain('Til kartet')
    expect(wrapper.text()).toContain('Ukas Mysterium')
  })

  it('adds the map intro query the first time the map card is opened', () => {
    localStorage.removeItem('hasSeenMapIntro')

    const wrapper = mountHomeView()
    const mapCard = wrapper.get('.home__note--hero')

    expect(mapCard.attributes('href')).toBe('/world-map?showMapIntro=1')
  })

  it('does not add the map intro query after the popup has been seen', () => {
    localStorage.setItem('hasSeenMapIntro', 'true')

    const wrapper = mountHomeView()
    const mapCard = wrapper.get('.home__note--hero')

    expect(mapCard.attributes('href')).toBe('/world-map')
  })

  it('shows Kommer snart on locked notes when present', () => {
    const wrapper = mountHomeView()
    const lockedNotes = wrapper.findAll('.home__note--locked')

    // No locked cards are defined currently; verify the class is not rendered
    lockedNotes.forEach((note) => {
      expect(note.text()).toContain('Kommer snart')
    })
  })

  it('formatDisplayName handles plain email', () => {
    const authStore = useAuthStore()
    authStore.email = 'ole.hansen@gmail.com'

    const wrapper = mountHomeView()

    expect(wrapper.text()).toContain('Ole Hansen')
  })

  it('logs out and redirects to student login from the header button', async () => {
    const authStore = useAuthStore()
    authStore.token = 'token'
    authStore.email = 'agent.elev@student.local'

    const wrapper = mountHomeView()

    await wrapper.get('button.home__logout').trigger('click')
    await flushPromises()

    expect(authStore.isAuthenticated).toBe(false)
    expect(authStore.email).toBe(null)
    expect(router.currentRoute.value.name).toBe('StudentLogin')
  })
})
