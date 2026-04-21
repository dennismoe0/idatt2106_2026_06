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
})
