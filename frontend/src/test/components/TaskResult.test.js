import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import TaskResult from '@/components/student/TaskResult.vue'

const CORRECT_RESULT = {
  correct: true,
  score: 100,
  explanation: 'Bra jobbet!',
  stopCompleted: false,
  medalEarned: null
}

const WRONG_RESULT = {
  correct: false,
  score: 40,
  explanation: 'Prøv igjen.',
  stopCompleted: false,
  medalEarned: null
}

const MEDAL_RESULT = {
  correct: true,
  score: 100,
  explanation: 'Perfekt!',
  stopCompleted: true,
  medalEarned: { id: 1, name: 'Nyhetsdetektiv', description: 'Fullført Nyhetskvartalet' }
}

describe('TaskResult', () => {
  it('shows "Riktig!" when correct', () => {
    const wrapper = mount(TaskResult, { props: { result: CORRECT_RESULT } })
    expect(wrapper.text()).toContain('Riktig!')
  })

  it('shows "Ikke helt riktig" when incorrect', () => {
    const wrapper = mount(TaskResult, { props: { result: WRONG_RESULT } })
    expect(wrapper.text()).toContain('Ikke helt riktig')
  })

  it('shows score and explanation', () => {
    const wrapper = mount(TaskResult, { props: { result: CORRECT_RESULT } })
    expect(wrapper.text()).toContain('Poeng: 100')
    expect(wrapper.text()).toContain('Bra jobbet!')
  })

  it('shows stop completed message when stopCompleted is true', () => {
    const wrapper = mount(TaskResult, { props: { result: MEDAL_RESULT } })
    expect(wrapper.text()).toContain('Du fullførte stoppet!')
  })

  it('does not show stop completed message when stopCompleted is false', () => {
    const wrapper = mount(TaskResult, { props: { result: CORRECT_RESULT } })
    expect(wrapper.text()).not.toContain('Du fullførte stoppet!')
  })

  it('shows medal block when medalEarned is set', () => {
    const wrapper = mount(TaskResult, { props: { result: MEDAL_RESULT } })
    expect(wrapper.text()).toContain('Nyhetsdetektiv')
    expect(wrapper.text()).toContain('Fullført Nyhetskvartalet')
  })

  it('does not show medal block when medalEarned is null', () => {
    const wrapper = mount(TaskResult, { props: { result: CORRECT_RESULT } })
    expect(wrapper.find('.medal').exists()).toBe(false)
  })

  it('emits next when "Neste oppgave" is clicked', async () => {
    const wrapper = mount(TaskResult, { props: { result: CORRECT_RESULT } })
    const buttons = wrapper.findAll('.actions button')
    await buttons[0].trigger('click')
    expect(wrapper.emitted('next')).toHaveLength(1)
  })

  it('emits backToMap when "Tilbake til kart" is clicked', async () => {
    const wrapper = mount(TaskResult, { props: { result: CORRECT_RESULT } })
    const buttons = wrapper.findAll('.actions button')
    await buttons[1].trigger('click')
    expect(wrapper.emitted('backToMap')).toHaveLength(1)
  })
})
