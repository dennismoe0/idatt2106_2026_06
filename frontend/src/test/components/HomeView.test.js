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

  it('renders the student display name and seven corkboard cards', () => {
    const authStore = useAuthStore()
    authStore.email = 'agent.elev@student.local'

    const wrapper = mount(HomeView, {
      global: {
        plugins: [router],
      },
    })

    expect(wrapper.text()).toContain('Hei, Agent Elev')
    expect(wrapper.findAllComponents(CorkboardCard)).toHaveLength(7)
    expect(wrapper.text()).toContain('Kart')
    expect(wrapper.text()).toContain('Avatar')
    expect(wrapper.text()).toContain('Ukens Mysterium')
    expect(wrapper.text()).toContain('Kommer snart')
  })
})
