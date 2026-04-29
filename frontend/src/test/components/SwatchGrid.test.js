import { mount } from '@vue/test-utils'
import { describe, it, expect } from 'vitest'
import SwatchGrid from '@/components/student/avatar/controls/SwatchGrid.vue'

const SWATCHES = ['#FDDBB4', '#D08B5B', '#3B1F0E']

describe('SwatchGrid', () => {
  it('renders a button per swatch', () => {
    const w = mount(SwatchGrid, { props: { modelValue: SWATCHES[0], swatches: SWATCHES, locked: [] } })
    expect(w.findAll('.swatch')).toHaveLength(3)
  })

  it('marks selected swatch', () => {
    const w = mount(SwatchGrid, { props: { modelValue: SWATCHES[1], swatches: SWATCHES, locked: [] } })
    const buttons = w.findAll('.swatch')
    expect(buttons[1].classes()).toContain('swatch--selected')
    expect(buttons[0].classes()).not.toContain('swatch--selected')
  })

  it('emits update:modelValue on click', async () => {
    const w = mount(SwatchGrid, { props: { modelValue: SWATCHES[0], swatches: SWATCHES, locked: [] } })
    await w.findAll('.swatch')[2].trigger('click')
    expect(w.emitted('update:modelValue')?.[0]).toEqual([SWATCHES[2]])
  })

  it('does not emit for locked swatch', async () => {
    const w = mount(SwatchGrid, { props: { modelValue: SWATCHES[0], swatches: SWATCHES, locked: [SWATCHES[2]] } })
    await w.findAll('.swatch')[2].trigger('click')
    expect(w.emitted('update:modelValue')).toBeFalsy()
  })

  it('renders medal-locked swatch with medal overlay', () => {
    const w = mount(SwatchGrid, {
      props: {
        modelValue: '#1a1a1a',
        swatches: ['#1a1a1a', '#00E5FF'],
        medalLocked: [{ value: '#00E5FF', stopName: 'Bane 4: Passordbanken' }],
      },
    })

    const buttons = w.findAll('button')
    expect(buttons).toHaveLength(2)
    expect(buttons[1].classes()).toContain('swatch--medal-locked')
    expect(buttons[1].text()).toContain('🏅')
  })

  it('does not emit update when medal-locked swatch is clicked', async () => {
    const w = mount(SwatchGrid, {
      props: {
        modelValue: '#1a1a1a',
        swatches: ['#1a1a1a', '#00E5FF'],
        medalLocked: [{ value: '#00E5FF', stopName: 'Test stop' }],
      },
    })

    await w.findAll('button')[1].trigger('click')
    expect(w.emitted('update:modelValue')).toBeFalsy()
  })

  it('shows tooltip text on click of medal-locked swatch', async () => {
    const w = mount(SwatchGrid, {
      props: {
        modelValue: '#1a1a1a',
        swatches: ['#1a1a1a', '#00E5FF'],
        medalLocked: [{ value: '#00E5FF', stopName: 'Passordbanken' }],
      },
    })

    await w.findAll('button')[1].trigger('click')
    const bubble = w.find('.swatch-bubble')
    expect(bubble.isVisible()).toBe(true)
    expect(bubble.text()).toContain('Passordbanken')
  })
})
