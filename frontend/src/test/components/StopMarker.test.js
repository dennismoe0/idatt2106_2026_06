import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import StopMarker from '@/components/student/StopMarker.vue'

const makeStop = (overrides = {}) => ({
  id: 1,
  name: 'Nyhetskvartalet',
  orderIndex: 1,
  locked: false,
  completed: false,
  taskCount: 3,
  ...overrides
})

describe('StopMarker', () => {
  it('renders the stop name', () => {
    const wrapper = mount(StopMarker, { props: { stop: makeStop() } })
    expect(wrapper.text()).toContain('Nyhetskvartalet')
  })

  it('emits click with stop when not locked', async () => {
    const stop = makeStop()
    const wrapper = mount(StopMarker, { props: { stop } })
    await wrapper.trigger('click')
    expect(wrapper.emitted('click')).toBeTruthy()
    expect(wrapper.emitted('click')[0][0]).toEqual(stop)
  })

  it('is disabled and does not emit when locked', async () => {
    const wrapper = mount(StopMarker, { props: { stop: makeStop({ locked: true }) } })
    expect(wrapper.attributes('disabled')).toBeDefined()
    await wrapper.trigger('click')
    expect(wrapper.emitted('click')).toBeFalsy()
  })

  it('applies completed class when stop is completed', () => {
    const wrapper = mount(StopMarker, { props: { stop: makeStop({ completed: true }) } })
    expect(wrapper.classes()).toContain('stop-marker--completed')
  })

  it('applies locked class when stop is locked', () => {
    const wrapper = mount(StopMarker, { props: { stop: makeStop({ locked: true }) } })
    expect(wrapper.classes()).toContain('stop-marker--locked')
  })
})
