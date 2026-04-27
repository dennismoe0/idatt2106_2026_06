import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import SuspectLineup from '@/components/student/SuspectLineup.vue'

const mountLineup = () => mount(SuspectLineup, { global: { stubs: { Teleport: true } } })

describe('SuspectLineup', () => {
  it('renders all 7 suspect cards', () => {
    const wrapper = mountLineup()
    expect(wrapper.findAll('.suspect-card')).toHaveLength(7)
  })

  it('confirm button is disabled until a suspect is selected', () => {
    const wrapper = mountLineup()
    expect(wrapper.find('.lineup__confirm').attributes('disabled')).toBeDefined()
  })

  it('confirm button is enabled after selecting a suspect', async () => {
    const wrapper = mountLineup()
    await wrapper.findAll('.suspect-card')[0].trigger('click')
    expect(wrapper.find('.lineup__confirm').attributes('disabled')).toBeUndefined()
  })

  it('highlights the selected suspect card', async () => {
    const wrapper = mountLineup()
    await wrapper.findAll('.suspect-card')[3].trigger('click')
    expect(wrapper.findAll('.suspect-card')[3].classes()).toContain('suspect-card--selected')
    expect(wrapper.findAll('.suspect-card')[0].classes()).not.toContain('suspect-card--selected')
  })

  it('always reveals Malte Skygge regardless of who student picked', async () => {
    const wrapper = mountLineup()
    // Student picks suspect index 0 (Birger Bakmann)
    await wrapper.findAll('.suspect-card')[0].trigger('click')
    await wrapper.find('.lineup__confirm').trigger('click')
    // Should now show reveal phase with Malte Skygge
    expect(wrapper.find('.lineup__reveal-name').text()).toBe('Malte Skygge')
  })

  it('reveals Malte Skygge even when student picks him directly', async () => {
    const wrapper = mountLineup()
    await wrapper.findAll('.suspect-card')[2].trigger('click')
    await wrapper.find('.lineup__confirm').trigger('click')
    expect(wrapper.find('.lineup__reveal-name').text()).toBe('Malte Skygge')
  })

  it('shows arrested screen after reveal', async () => {
    const wrapper = mountLineup()
    await wrapper.findAll('.suspect-card')[1].trigger('click')
    await wrapper.find('.lineup__confirm').trigger('click')
    expect(wrapper.text()).toContain('ARRESTERT')
    expect(wrapper.text()).toContain('Politiet har arrestert Malte Skygge')
  })

  it('emits chosen when student clicks continue after reveal', async () => {
    const wrapper = mountLineup()
    await wrapper.findAll('.suspect-card')[0].trigger('click')
    await wrapper.find('.lineup__confirm').trigger('click')
    await wrapper.find('.lineup__confirm').trigger('click')
    expect(wrapper.emitted('chosen')).toBeTruthy()
  })

  it('hides suspect grid after reveal', async () => {
    const wrapper = mountLineup()
    await wrapper.findAll('.suspect-card')[0].trigger('click')
    await wrapper.find('.lineup__confirm').trigger('click')
    expect(wrapper.find('.lineup__grid').exists()).toBe(false)
  })
})
