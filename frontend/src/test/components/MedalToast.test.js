import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import MedalToast from '@/components/common/MedalToast.vue'

describe('MedalToast', () => {
  beforeEach(() => { vi.useFakeTimers() })
  afterEach(() => { vi.useRealTimers() })

  it('is not visible when medal is null', () => {
    const wrapper = mount(MedalToast, { props: { medal: null } })
    expect(wrapper.find('.medal-toast').exists()).toBe(false)
  })

  it('becomes visible when medal is set', async () => {
    const wrapper = mount(MedalToast, { props: { medal: null } })
    await wrapper.setProps({ medal: { name: 'Nyhetsdetektiv', description: 'Fullført Nyhetskvartalet' } })
    expect(wrapper.find('.medal-toast').exists()).toBe(true)
  })

  it('shows medal name and description', async () => {
    const wrapper = mount(MedalToast, {
      props: { medal: { name: 'Nyhetsdetektiv', description: 'Fullført Nyhetskvartalet' } }
    })
    expect(wrapper.text()).toContain('Nyhetsdetektiv')
    expect(wrapper.text()).toContain('Fullført Nyhetskvartalet')
  })

  it('auto-hides after 4 seconds', async () => {
    const wrapper = mount(MedalToast, {
      props: { medal: { name: 'Test', description: 'Test' } }
    })
    expect(wrapper.find('.medal-toast').exists()).toBe(true)
    vi.advanceTimersByTime(4000)
    await flushPromises()
    expect(wrapper.find('.medal-toast').exists()).toBe(false)
  })
})
