import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ShapeGrid from '../ShapeGrid.vue'
import { defineComponent } from 'vue'

const FakePreview = defineComponent({
  props: { myVariant: String },
  template: '<div class="fake-preview">{{ myVariant }}</div>',
})

describe('ShapeGrid', () => {
  const baseProps = {
    modelValue: 'short',
    variants: ['short', 'long'],
    previewComponent: FakePreview,
    previewProps: {},
    variantProp: 'myVariant',
  }

  it('renders medal-locked tile alongside normal tiles', () => {
    const wrapper = mount(ShapeGrid, {
      props: {
        ...baseProps,
        variants: ['short'],
        medalLocked: [{ value: 'mohawk', stopName: 'Datasenteret' }],
      },
    })
    const tiles = wrapper.findAll('.shape-tile')
    expect(tiles).toHaveLength(2) // short + mohawk
    expect(tiles[1].classes()).toContain('shape-tile--medal-locked')
  })

  it('does not emit update when medal-locked tile is clicked', async () => {
    const wrapper = mount(ShapeGrid, {
      props: {
        ...baseProps,
        variants: ['short'],
        medalLocked: [{ value: 'mohawk', stopName: 'Datasenteret' }],
      },
    })
    await wrapper.findAll('.shape-tile')[1].trigger('click')
    expect(wrapper.emitted('update:modelValue')).toBeFalsy()
  })

  it('shows tooltip on click of medal-locked tile', async () => {
    const wrapper = mount(ShapeGrid, {
      props: {
        ...baseProps,
        variants: ['short'],
        medalLocked: [{ value: 'mohawk', stopName: 'Datasenteret' }],
      },
    })
    await wrapper.findAll('.shape-tile')[1].trigger('click')
    const bubble = wrapper.find('.shape-bubble')
    expect(bubble.isVisible()).toBe(true)
    expect(bubble.text()).toContain('Datasenteret')
  })
})
