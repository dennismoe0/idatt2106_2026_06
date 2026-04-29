import { mount } from '@vue/test-utils'
import { describe, it, expect } from 'vitest'
import ShapeGrid from '@/components/student/avatar/controls/ShapeGrid.vue'
import { defineComponent, markRaw } from 'vue'

const StubLayer = markRaw(defineComponent({ template: '<div class="stub-layer" />', props: ['hairStyle', 'hairColor'] }))
const VARIANTS = ['short', 'long', 'curly']

describe('ShapeGrid', () => {
  it('renders one tile per variant', () => {
    const w = mount(ShapeGrid, { props: { modelValue: 'short', variants: VARIANTS, previewComponent: StubLayer, previewProps: { hairColor: '#888' }, variantProp: 'hairStyle', locked: [] } })
    expect(w.findAll('.shape-tile')).toHaveLength(3)
  })

  it('marks selected tile', () => {
    const w = mount(ShapeGrid, { props: { modelValue: 'long', variants: VARIANTS, previewComponent: StubLayer, previewProps: {}, variantProp: 'hairStyle', locked: [] } })
    const tiles = w.findAll('.shape-tile')
    expect(tiles[1].classes()).toContain('shape-tile--selected')
    expect(tiles[0].classes()).not.toContain('shape-tile--selected')
  })

  it('emits update:modelValue on tile click', async () => {
    const w = mount(ShapeGrid, { props: { modelValue: 'short', variants: VARIANTS, previewComponent: StubLayer, previewProps: {}, variantProp: 'hairStyle', locked: [] } })
    await w.findAll('.shape-tile')[2].trigger('click')
    expect(w.emitted('update:modelValue')?.[0]).toEqual(['curly'])
  })

  it('does not emit for locked tile', async () => {
    const w = mount(ShapeGrid, { props: { modelValue: 'short', variants: VARIANTS, previewComponent: StubLayer, previewProps: {}, variantProp: 'hairStyle', locked: ['curly'] } })
    await w.findAll('.shape-tile')[2].trigger('click')
    expect(w.emitted('update:modelValue')).toBeFalsy()
  })

  it('renders medal-locked tile alongside normal tiles', () => {
    const w = mount(ShapeGrid, {
      props: {
        modelValue: 'short',
        variants: ['short'],
        previewComponent: StubLayer,
        previewProps: {},
        variantProp: 'hairStyle',
        medalLocked: [{ value: 'mohawk', stopName: 'Datasenteret' }],
      },
    })

    const tiles = w.findAll('.shape-tile')
    expect(tiles).toHaveLength(2)
    expect(tiles[1].classes()).toContain('shape-tile--medal-locked')
  })

  it('does not emit update when medal-locked tile is clicked', async () => {
    const w = mount(ShapeGrid, {
      props: {
        modelValue: 'short',
        variants: ['short'],
        previewComponent: StubLayer,
        previewProps: {},
        variantProp: 'hairStyle',
        medalLocked: [{ value: 'mohawk', stopName: 'Datasenteret' }],
      },
    })

    await w.findAll('.shape-tile')[1].trigger('click')
    expect(w.emitted('update:modelValue')).toBeFalsy()
  })

  it('shows tooltip on click of medal-locked tile', async () => {
    const w = mount(ShapeGrid, {
      props: {
        modelValue: 'short',
        variants: ['short'],
        previewComponent: StubLayer,
        previewProps: {},
        variantProp: 'hairStyle',
        medalLocked: [{ value: 'mohawk', stopName: 'Datasenteret' }],
      },
    })

    await w.findAll('.shape-tile')[1].trigger('click')
    const bubble = w.find('.shape-bubble')
    expect(bubble.isVisible()).toBe(true)
    expect(bubble.text()).toContain('Datasenteret')
  })
})
