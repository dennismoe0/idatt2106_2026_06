import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import PasswordTask from '@/components/student/PasswordTask.vue'

const CHOICE_TASK = {
  id: 1,
  guidanceText: 'Velg det tryggeste passordet.',
  contentJson: {
    type: 'CHOICE',
    question: 'Hvilket passord er tryggest?',
    options: [
      { id: 'a', value: 'Ola123' },
      { id: 'b', value: 'F!sk3Taco#92' },
    ],
  },
}

const BUILDER_TASK = {
  id: 2,
  guidanceText: 'Bygg et sterkt passord.',
  contentJson: {
    type: 'BUILDER',
    question: 'Bygg et passord som er sterkt nok.',
    words: ['Tiger', 'Pizza', 'Hund'],
    numbers: ['42', '2026'],
    symbols: ['!', '#'],
    pitfalls: ['hund'],
    minStrength: 'STRONG',
    maxParts: 4,
    maxLength: 13,
  },
}

function mountTask(task = BUILDER_TASK, extraProps = {}) {
  return mount(PasswordTask, {
    props: { task, result: null, isLastTask: false, ...extraProps },
  })
}

describe('PasswordTask', () => {
  it('marks the selected choice card clearly and submits the selected id', async () => {
    const wrapper = mountTask(CHOICE_TASK)
    const options = wrapper.findAll('.option-btn')

    await options[1].trigger('click')

    expect(options[1].classes()).toContain('option-btn--selected')
    expect(options[1].attributes('aria-pressed')).toBe('true')

    await wrapper.find('.submit-btn').trigger('click')
    expect(wrapper.emitted('submitted')[0][0]).toEqual({ selected: 'b' })
  })

  it('shows live strength while building and only enables submit at full strength', async () => {
    const wrapper = mountTask()

    expect(wrapper.find('.builder__strength').exists()).toBe(true)
    expect(wrapper.find('.submit-btn').attributes('disabled')).toBeDefined()

    await wrapper.findAll('.tile--word')[0].trigger('click')
    await wrapper.findAll('.tile--word')[1].trigger('click')

    expect(wrapper.find('.builder__strength-label').text()).not.toContain('Klar')
    expect(wrapper.find('.submit-btn').attributes('disabled')).toBeDefined()

    await wrapper.findAll('.tile--number')[0].trigger('click')
    await wrapper.findAll('.tile--symbol')[0].trigger('click')

    expect(wrapper.find('.builder__strength-fill').attributes('style')).toContain('width: 100%')
    expect(wrapper.find('.builder__strength-label').text()).toContain('Klar')
    expect(wrapper.find('.submit-btn').attributes('disabled')).toBeUndefined()
  })

  it('disables tiles that would exceed maxParts or maxLength', async () => {
    const wrapper = mountTask()

    await wrapper.findAll('.tile--word')[0].trigger('click')
    await wrapper.findAll('.tile--word')[1].trigger('click')

    const disabledTileLabels = wrapper.findAll('.tile:disabled').map(tile => tile.text())
    expect(disabledTileLabels).toContain('2026')

    await wrapper.findAll('.tile--number')[0].trigger('click')
    await wrapper.findAll('.tile--symbol')[0].trigger('click')

    wrapper.findAll('.tile').forEach(tile => {
      expect(tile.attributes('disabled')).toBeDefined()
    })
  })

  it('submits built password with parts for backend validation', async () => {
    const wrapper = mountTask()

    await wrapper.findAll('.tile--word')[0].trigger('click')
    await wrapper.findAll('.tile--word')[1].trigger('click')
    await wrapper.findAll('.tile--number')[0].trigger('click')
    await wrapper.findAll('.tile--symbol')[0].trigger('click')
    await wrapper.find('.submit-btn').trigger('click')

    expect(wrapper.emitted('submitted')[0][0]).toEqual({
      password: 'TigerPizza42!',
      parts: ['Tiger', 'Pizza', '42', '!'],
      strength: 'STRONG',
    })
  })
})
