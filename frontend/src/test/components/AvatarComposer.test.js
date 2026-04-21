import { mount } from '@vue/test-utils'
import { describe, it, expect } from 'vitest'
import AvatarComposer from '@/components/student/avatar/AvatarComposer.vue'

const BASE = {
  gender: 'female', skinColor: '#D08B5B', hairStyle: 'short', hairColor: '#8B4513',
  eyeStyle: 'round', eyeColor: '#4a3000', outfit: 'hoodie', outfitColor: '#2563eb',
  accessory: 'badge',
}

describe('AvatarComposer', () => {
  it('renders a canvas div with correct dimensions', () => {
    const w = mount(AvatarComposer, { props: { selections: BASE, size: 120 } })
    const canvas = w.find('.avatar-canvas')
    expect(canvas.exists()).toBe(true)
    expect(canvas.attributes('style')).toContain('width: 120px')
    expect(canvas.attributes('style')).toContain('height: 160px')
  })

  it('renders all 6 layer components', () => {
    const w = mount(AvatarComposer, { props: { selections: BASE } })
    expect(w.findAllComponents({ name: 'AvatarBody' })).toHaveLength(1)
    expect(w.findAllComponents({ name: 'AvatarHead' })).toHaveLength(1)
    expect(w.findAllComponents({ name: 'AvatarHair' })).toHaveLength(1)
    expect(w.findAllComponents({ name: 'AvatarEyes' })).toHaveLength(1)
    expect(w.findAllComponents({ name: 'AvatarOutfit' })).toHaveLength(1)
    expect(w.findAllComponents({ name: 'AvatarAccessory' })).toHaveLength(1)
  })

  it('passes skinColor to AvatarBody and AvatarHead', () => {
    const w = mount(AvatarComposer, { props: { selections: BASE } })
    expect(w.findComponent({ name: 'AvatarBody' }).props('skinColor')).toBe('#D08B5B')
    expect(w.findComponent({ name: 'AvatarHead' }).props('skinColor')).toBe('#D08B5B')
  })

  it('passes gender to AvatarBody', () => {
    const w = mount(AvatarComposer, { props: { selections: { ...BASE, gender: 'female' } } })
    expect(w.findComponent({ name: 'AvatarBody' }).props('gender')).toBe('female')
  })
})
