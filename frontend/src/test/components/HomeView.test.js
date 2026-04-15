import { describe, it, expect, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createMemoryHistory } from 'vue-router'
import HomeView from '@/views/student/HomeView.vue'
import CorkboardCard from '@/components/student/CorkboardCard.vue'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createMemoryHistory(),
  routes: [
    { path: '/', component: HomeView },
    { path: '/map', component: { template: '<div>Map</div>' } },
    { path: '/avatar', component: { template: '<div>Avatar</div>' } },
  ],
})

describe('HomeView', () => {
  beforeEach(async () => {
    setActivePinia(createPinia())
    await router.push('/')
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
    expect(wrapper.text()).toContain('Avatar')
    expect(wrapper.text()).toContain('Ukens Mysterium')
    expect(wrapper.text()).toContain('Kommer snart')
  })

  it('shows Kommer snart on locked cards', () => {
    const wrapper = mountHomeView()
    const lockedCards = wrapper
      .findAllComponents(CorkboardCard)
      .filter((component) => component.props('locked'))

    expect(lockedCards).toHaveLength(5)
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
})
