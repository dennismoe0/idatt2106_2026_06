import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import WorldMapNode from '@/components/student/WorldMapNode.vue'

const makeStop = (overrides = {}) => ({
  id: 1,
  name: 'Nyhetskvartalet',
  locked: false,
  completed: false,
  ...overrides,
})

describe('WorldMapNode', () => {
  it('renders the stop name', () => {
    const wrapper = mount(WorldMapNode, { props: { stop: makeStop() } })
    expect(wrapper.text()).toContain('Nyhetskvartalet')
  })

  it('has unlocked class when not locked and not completed', () => {
    const wrapper = mount(WorldMapNode, { props: { stop: makeStop() } })
    expect(wrapper.classes()).toContain('world-node--unlocked')
  })

  it('has completed class when stop is completed', () => {
    const wrapper = mount(WorldMapNode, { props: { stop: makeStop({ completed: true }) } })
    expect(wrapper.classes()).toContain('world-node--completed')
  })

  it('has locked class when stop is locked', () => {
    const wrapper = mount(WorldMapNode, { props: { stop: makeStop({ locked: true }) } })
    expect(wrapper.classes()).toContain('world-node--locked')
  })

  it('emits node-click when unlocked node is clicked', async () => {
    const wrapper = mount(WorldMapNode, { props: { stop: makeStop() } })
    await wrapper.trigger('click')
    expect(wrapper.emitted('node-click')).toBeTruthy()
  })

  it('does not emit node-click when locked node is clicked', async () => {
    const wrapper = mount(WorldMapNode, { props: { stop: makeStop({ locked: true }) } })
    await wrapper.trigger('click')
    expect(wrapper.emitted('node-click')).toBeFalsy()
  })

  it('has shake class when isShaking is true', () => {
    const wrapper = mount(WorldMapNode, {
      props: { stop: makeStop({ locked: true }), isShaking: true },
    })
    expect(wrapper.classes()).toContain('world-node--shake')
  })

  it('is aria-disabled when locked', () => {
    const wrapper = mount(WorldMapNode, { props: { stop: makeStop({ locked: true }) } })
    expect(wrapper.attributes('aria-disabled')).toBe('true')
  })
})
