import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import WorldMapNode from '@/components/student/WorldMapNode.vue'

const makeStop = (overrides = {}) => ({
  id: 1,
  name: 'Nyhetskvartalet',
  theme: 'FAKE_NEWS',
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

  it('adds the current class when isCurrent is true', () => {
    const wrapper = mount(WorldMapNode, { props: { stop: makeStop(), isCurrent: true } })
    expect(wrapper.classes()).toContain('world-node--current')
  })

  it('renders the pulse halo only when current and not locked', () => {
    const current = mount(WorldMapNode, { props: { stop: makeStop(), isCurrent: true } })
    expect(current.find('.world-node__halo').exists()).toBe(true)

    const notCurrent = mount(WorldMapNode, { props: { stop: makeStop(), isCurrent: false } })
    expect(notCurrent.find('.world-node__halo').exists()).toBe(false)

    const lockedCurrent = mount(WorldMapNode, {
      props: { stop: makeStop({ locked: true }), isCurrent: true },
    })
    expect(lockedCurrent.find('.world-node__halo').exists()).toBe(false)
    expect(lockedCurrent.classes()).not.toContain('world-node--current')
  })

  it('renders the lock badge when locked', () => {
    const wrapper = mount(WorldMapNode, { props: { stop: makeStop({ locked: true }) } })
    expect(wrapper.find('.world-node__badge--lock').exists()).toBe(true)
    expect(wrapper.find('.world-node__badge--done').exists()).toBe(false)
  })

  it('renders the completed check badge when completed', () => {
    const wrapper = mount(WorldMapNode, { props: { stop: makeStop({ completed: true }) } })
    expect(wrapper.find('.world-node__badge--done').exists()).toBe(true)
    expect(wrapper.find('.world-node__badge--lock').exists()).toBe(false)
  })

  it('renders the theme icon matching stop.theme', () => {
    const wrapper = mount(WorldMapNode, {
      props: { stop: makeStop({ theme: 'PASSWORD', name: 'Passordbanken' }) },
    })
    expect(wrapper.find('.world-node__icon').text()).toBe('🔑')
  })

  it('falls back to pin emoji for unknown theme', () => {
    const wrapper = mount(WorldMapNode, {
      props: { stop: makeStop({ theme: 'UNKNOWN_THEME' }) },
    })
    expect(wrapper.find('.world-node__icon').text()).toBe('📍')
  })
})
