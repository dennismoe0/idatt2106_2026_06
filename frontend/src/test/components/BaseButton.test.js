import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import BaseButton from '@/components/common/BaseButton.vue'

describe('BaseButton', () => {
  it('renders slot content', () => {
    const wrapper = mount(BaseButton, { slots: { default: 'Klikk meg' } })
    expect(wrapper.text()).toContain('Klikk meg')
  })

  it('is disabled when disabled prop is true', () => {
    const wrapper = mount(BaseButton, { props: { disabled: true } })
    expect(wrapper.attributes('disabled')).toBeDefined()
  })

  it('shows spinner and hides slot when loading', () => {
    const wrapper = mount(BaseButton, {
      props: { loading: true },
      slots: { default: 'Klikk' },
      global: { stubs: { LoadingSpinner: true } }
    })
    expect(wrapper.text()).not.toContain('Klikk')
    expect(wrapper.findComponent({ name: 'LoadingSpinner' }).exists()).toBe(true)
  })

  it('has type button by default', () => {
    const wrapper = mount(BaseButton)
    expect(wrapper.attributes('type')).toBe('button')
  })

  it('has btn class for WCAG touch target enforcement', () => {
    const wrapper = mount(BaseButton)
    expect(wrapper.classes()).toContain('btn')
  })
})
