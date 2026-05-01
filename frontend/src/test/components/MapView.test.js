import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createMemoryHistory } from 'vue-router'
import MapView from '@/views/student/MapView.vue'

vi.mock('@/stores/game', () => ({
  useGameStore: () => ({
    stops: [
      { id: 1, name: 'Nyhetskvartalet', locked: false, completed: false, taskCount: 3 },
      { id: 2, name: 'Postkontoret',    locked: true,  completed: false, taskCount: 3 },
    ],
    fetchStops: vi.fn().mockResolvedValue(undefined),
    fetchProfile: vi.fn().mockResolvedValue(undefined),
    xp: 0,
    starBalance: 0
  })
}))

vi.mock('@/stores/classroom', () => ({
  useClassroomStore: () => ({ currentClassroomId: 1, displayName: 'Testbruker' })
}))

vi.mock('@/stores/auth', () => ({
  useAuthStore: () => ({ email: 'test@student.local' })
}))

const router = createRouter({ history: createMemoryHistory(), routes: [
  { path: '/', name: 'Home', component: { template: '<div/>' } },
  { path: '/map', component: MapView },
  { path: '/task', name: 'Task', component: { template: '<div/>' } },
  { path: '/world-map', name: 'WorldMap', component: { template: '<div/>' } },
  { path: '/suspects', name: 'SuspectDossier', component: { template: '<div/>' } },
]})

describe('MapView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    localStorage.setItem('mapView', 'simple')
  })

  it('renders a StopMarker for each stop', async () => {
    const wrapper = mount(MapView, { global: { plugins: [router] } })
    await flushPromises()
    expect(wrapper.findAllComponents({ name: 'StopMarker' })).toHaveLength(2)
  })

  it('renders stop names', async () => {
    const wrapper = mount(MapView, { global: { plugins: [router] } })
    await flushPromises()
    expect(wrapper.text()).toContain('Nyhetskvartalet')
    expect(wrapper.text()).toContain('Postkontoret')
  })

  it('shows and dismisses the first-time map intro popup', async () => {
    await router.push('/map?showMapIntro=1')
    await router.isReady()

    const wrapper = mount(MapView, {
      global: {
        plugins: [router],
        stubs: { teleport: true },
      },
    })

    await flushPromises()

    expect(wrapper.text()).toContain('Ordføreren trenger hjelp')
    expect(wrapper.text()).toContain('Pengene som skulle bygge den nye idrettsparken er stjålet')

    await wrapper.get('.map-intro-modal__dot:nth-child(3)').trigger('click')
    expect(wrapper.text()).toContain('Som nettdetektiv må du utforske kartet')

    await wrapper.get('.map-intro-modal__nav--secondary').trigger('click')
    expect(wrapper.text()).toContain('Hele Internettbyen peker i forskjellige retninger')

    await wrapper.get('.map-intro-modal__nav--primary').trigger('click')
    await wrapper.get('.map-intro-modal__nav--primary').trigger('click')
    await wrapper.get('.map-intro-modal__nav--primary').trigger('click')

    expect(localStorage.getItem('hasSeenMapIntro')).toBe('true')
    expect(wrapper.text()).not.toContain('Ordføreren trenger hjelp')
  })
})
