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
    fetchStops: vi.fn().mockResolvedValue(undefined)
  })
}))

vi.mock('@/stores/classroom', () => ({
  useClassroomStore: () => ({ currentClassroomId: 1 })
}))

const router = createRouter({ history: createMemoryHistory(), routes: [
  { path: '/map', component: MapView },
  { path: '/task', name: 'Task', component: { template: '<div/>' } },
]})

describe('MapView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
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
})
