import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import AvatarPreview from '@/components/student/AvatarPreview.vue'

const SEL = {
  gender: 'female',
  skinColor: '#D08B5B',
  hairStyle: 'short',
  hairColor: '#8B4513',
  eyeStyle: 'round',
  eyeColor: '#4a3000',
  outfit: 'hoodie',
  outfitColor: '#2563eb',
  accessory: 'badge',
}

describe('AvatarPreview', () => {
  it('renders without errors when given a selections object', () => {
    const wrapper = mount(AvatarPreview, {
      props: { selections: SEL },
    })
    expect(wrapper.exists()).toBe(true)
  })

  it('passes selections down to AvatarComposer', () => {
    const wrapper = mount(AvatarPreview, {
      props: { selections: SEL },
    })
    const composer = wrapper.findComponent({ name: 'AvatarComposer' })
    expect(composer.exists()).toBe(true)
    expect(composer.props('selections')).toEqual(SEL)
  })

  it('applies the default size of 120 when no size prop is given', () => {
    const wrapper = mount(AvatarPreview, {
      props: { selections: SEL },
    })
    const composer = wrapper.findComponent({ name: 'AvatarComposer' })
    expect(composer.props('size')).toBe(120)
  })

  it('forwards a custom size to AvatarComposer', () => {
    const wrapper = mount(AvatarPreview, {
      props: { selections: SEL, size: 200 },
    })
    const composer = wrapper.findComponent({ name: 'AvatarComposer' })
    expect(composer.props('size')).toBe(200)
  })
})
