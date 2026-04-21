import { mount } from '@vue/test-utils'
import { describe, it, expect } from 'vitest'
import GenderToggle from '@/components/student/avatar/controls/GenderToggle.vue'

describe('GenderToggle', () => {
  it('renders 3 gender buttons', () => {
    const w = mount(GenderToggle, { props: { modelValue: 'female' } })
    expect(w.findAll('.gender-btn')).toHaveLength(3)
  })

  it('marks active button', () => {
    const w = mount(GenderToggle, { props: { modelValue: 'neutral' } })
    const btns = w.findAll('.gender-btn')
    expect(btns[2].classes()).toContain('gender-btn--active')
  })

  it('emits update:modelValue on click', async () => {
    const w = mount(GenderToggle, { props: { modelValue: 'female' } })
    await w.findAll('.gender-btn')[1].trigger('click')
    expect(w.emitted('update:modelValue')?.[0]).toEqual(['male'])
  })
})
