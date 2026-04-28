import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import PasswordTask from '@/components/student/PasswordTask.vue'

const BUILDER_TASK = {
  id: 1,
  guidanceText: 'Bruk brikkene til å lage et sterkt passord.',
  contentJson: {
    type: 'BUILDER',
    question: 'Bygg et sterkt passord',
    words: ['Tiger', 'Måne', 'Isbjørn'],
    numbers: ['42'],
    symbols: ['!'],
    maxLength: 10,
  },
}

describe('PasswordTask', () => {
  it('shows a neutral strength state before any password parts are added', () => {
    const wrapper = mount(PasswordTask, { props: { task: BUILDER_TASK } })

    expect(wrapper.find('.builder__strength').classes()).not.toContain('builder__strength--weak')
    expect(wrapper.find('.builder__strength-label').text()).toBe('Bygg passord')
    expect(wrapper.find('.submit-btn').attributes('disabled')).toBeDefined()
  })

  it('blocks builder tiles that would exceed max length', async () => {
    const wrapper = mount(PasswordTask, { props: { task: BUILDER_TASK } })

    await wrapper.findAll('button.tile--word').find(button => button.text() === 'Tiger').trigger('click')
    await wrapper.findAll('button.tile--word').find(button => button.text() === 'Måne').trigger('click')

    expect(wrapper.find('.builder__password').text()).toBe('TigerMåne')
    expect(wrapper.findAll('button.tile--word').find(button => button.text() === 'Isbjørn').attributes('disabled')).toBeDefined()
    expect(wrapper.find('.builder__limit').text()).toContain('9/10 tegn')
  })

  it('keeps the submit button disabled until the builder password is strong enough', async () => {
    const wrapper = mount(PasswordTask, { props: { task: BUILDER_TASK } })

    expect(wrapper.find('.builder__strength-label').text()).toBe('Bygg passord')
    expect(wrapper.find('.submit-btn').attributes('disabled')).toBeDefined()

    await wrapper.findAll('button.tile--word').find(button => button.text() === 'Tiger').trigger('click')
    await wrapper.findAll('button.tile--word').find(button => button.text() === 'Måne').trigger('click')

    expect(wrapper.find('.builder__strength-label').text()).toBe('Middels')
    expect(wrapper.find('.submit-btn').attributes('disabled')).toBeDefined()

    await wrapper.find('.clear-btn').trigger('click')
    await wrapper.findAll('button.tile--word').find(button => button.text() === 'Tiger').trigger('click')
    await wrapper.findAll('button.tile--number').find(button => button.text() === '42').trigger('click')
    await wrapper.findAll('button.tile--symbol').find(button => button.text() === '!').trigger('click')

    expect(wrapper.find('.builder__strength-label').text()).toBe('Sterkt')
    expect(wrapper.find('.submit-btn').attributes('disabled')).toBeUndefined()
  })

  it('submits a strong password within the configured max length', async () => {
    const wrapper = mount(PasswordTask, { props: { task: BUILDER_TASK } })

    await wrapper.findAll('button.tile--word').find(button => button.text() === 'Tiger').trigger('click')
    await wrapper.findAll('button.tile--number').find(button => button.text() === '42').trigger('click')
    await wrapper.findAll('button.tile--symbol').find(button => button.text() === '!').trigger('click')
    await wrapper.find('.submit-btn').trigger('click')

    expect(wrapper.emitted('submitted')[0][0]).toEqual({ password: 'Tiger42!' })
  })
})
