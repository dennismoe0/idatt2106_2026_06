import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import AvatarPreview from '@/components/student/AvatarPreview.vue'

describe('AvatarPreview', () => {
  const baseSelections = {
    gender: 'neutral',
    eyeColor: 'brown',
    skinColor: 'medium',
    hairColor: 'brown',
    hairStyle: 'short',
    outfit: 'detective-coat',
    outfitColor: 'blue',
    hatColor: 'none',
    accessory: 'badge',
  }

  it('renders an avatar image with descriptive alt text', () => {
    const wrapper = mount(AvatarPreview, {
      props: { selections: baseSelections },
    })

    const image = wrapper.get('img')
    expect(image.attributes('alt')).toContain('medium')
    expect(image.attributes('alt')).toContain('hudtone')
    expect(image.attributes('src')).toContain('adventurer-neutral.svg')
  })

  it('switches to the light SVG when light skin color is selected', async () => {
    const wrapper = mount(AvatarPreview, {
      props: { selections: { ...baseSelections, skinColor: 'light' } },
    })

    expect(wrapper.get('img').attributes('src')).toContain('adventurer-light.svg')
  })

  it('switches to the warm SVG when dark skin color is selected', async () => {
    const wrapper = mount(AvatarPreview, {
      props: { selections: { ...baseSelections, skinColor: 'dark' } },
    })

    expect(wrapper.get('img').attributes('src')).toContain('adventurer-warm.svg')
  })
})
