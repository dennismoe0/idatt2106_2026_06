import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import SwatchGrid from '../SwatchGrid.vue'

describe('SwatchGrid', () => {
  it('renders medal-locked swatch with medal overlay', () => {
    const wrapper = mount(SwatchGrid, {
      props: {
        modelValue: '#1a1a1a',
        swatches: ['#1a1a1a', '#00E5FF'],
        medalLocked: [{ value: '#00E5FF', stopName: 'Bane 4: Passordbanken' }],
      },
    })
    const buttons = wrapper.findAll('button')
    expect(buttons).toHaveLength(2)
    const lockedBtn = buttons[1]
    expect(lockedBtn.classes()).toContain('swatch--medal-locked')
    expect(lockedBtn.text()).toContain('🏅')
  })

  it('does not emit update when medal-locked swatch is clicked', async () => {
    const wrapper = mount(SwatchGrid, {
      props: {
        modelValue: '#1a1a1a',
        swatches: ['#1a1a1a', '#00E5FF'],
        medalLocked: [{ value: '#00E5FF', stopName: 'Test stop' }],
      },
    })
    const lockedBtn = wrapper.findAll('button')[1]
    await lockedBtn.trigger('click')
    expect(wrapper.emitted('update:modelValue')).toBeFalsy()
  })

  it('shows tooltip text on click of medal-locked swatch', async () => {
    const wrapper = mount(SwatchGrid, {
      props: {
        modelValue: '#1a1a1a',
        swatches: ['#1a1a1a', '#00E5FF'],
        medalLocked: [{ value: '#00E5FF', stopName: 'Passordbanken' }],
      },
    })
    const lockedBtn = wrapper.findAll('button')[1]
    await lockedBtn.trigger('click')
    const bubble = wrapper.find('.swatch-bubble')
    expect(bubble.isVisible()).toBe(true)
    expect(bubble.text()).toContain('Passordbanken')
  })
})
