import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import ConfettiOverlay from '@/components/common/ConfettiOverlay.vue'

describe('ConfettiOverlay', () => {
  beforeEach(() => { vi.useFakeTimers() })
  afterEach(() => { vi.useRealTimers() })

  it('is not visible when active is false', () => {
    const wrapper = mount(ConfettiOverlay, { props: { active: false } })
    expect(wrapper.find('.confetti-overlay').exists()).toBe(false)
  })

  it('becomes visible when active changes to true', async () => {
    const wrapper = mount(ConfettiOverlay, { props: { active: false } })
    await wrapper.setProps({ active: true })
    expect(wrapper.find('.confetti-overlay').exists()).toBe(true)
  })

  it('auto-hides after 3 seconds', async () => {
    const wrapper = mount(ConfettiOverlay, { props: { active: true } })
    expect(wrapper.find('.confetti-overlay').exists()).toBe(true)
    vi.advanceTimersByTime(3000)
    await flushPromises()
    expect(wrapper.find('.confetti-overlay').exists()).toBe(false)
  })
})
