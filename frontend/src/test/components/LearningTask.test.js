import { describe, expect, it, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import LearningTask from '@/components/student/LearningTask.vue'

const TASK = {
  id: 1,
  guidanceText: 'Les kortene og svar riktig.',
  contentJson: {
    slides: [
      {
        icon: '',
        heading: 'Overskrift 1',
        body: 'Brødtekst 1',
        examples: ['Eksempel 1A', 'Eksempel 1B'],
        checks: ['Husk 1A'],
      },
      {
        icon: '',
        heading: 'Overskrift 2',
        body: 'Brødtekst 2',
        examples: ['Eksempel 2A'],
        checks: ['Husk 2A'],
      },
    ],
    quiz: [
      { id: 'q1', question: 'Første spørsmål?', options: ['Riktig', 'Feil'], correct: 'Riktig' },
      { id: 'q2', question: 'Andre spørsmål?',  options: ['A', 'B'],       correct: 'B'      },
    ],
  },
}

describe('LearningTask', () => {
  beforeEach(() => { vi.useFakeTimers() })
  afterEach(() => { vi.useRealTimers() })

  it('starts in LEARN phase and shows all learning sections on one page', () => {
    const wrapper = mount(LearningTask, { props: { task: TASK } })
    expect(wrapper.text()).toContain('Overskrift 1')
    expect(wrapper.text()).toContain('Overskrift 2')
    expect(wrapper.text()).toContain('Brødtekst 1')
    expect(wrapper.text()).toContain('Eksempel 1A')
    expect(wrapper.text()).toContain('Husk 2A')
    expect(wrapper.text()).toContain('Første spørsmål?')
    expect(wrapper.text()).toContain('Andre spørsmål?')
    expect(wrapper.text()).toContain('Svar på alle spørsmål først')
  })

  it('shows inline questions and no separate start quiz button', () => {
    const wrapper = mount(LearningTask, { props: { task: TASK } })
    expect(wrapper.find('.nav-btn--start-quiz').exists()).toBe(false)
    expect(wrapper.findAll('.inline-question')).toHaveLength(2)
    expect(wrapper.find('.learn-complete .nav-btn').attributes('disabled')).toBeDefined()
    expect(wrapper.find('.nav-btn--next').exists()).toBe(false)
    expect(wrapper.find('.nav-btn--back').exists()).toBe(false)
  })

  it('renders answer options directly under each learning part', async () => {
    const wrapper = mount(LearningTask, { props: { task: TASK } })
    expect(wrapper.text()).toContain('Første spørsmål?')
    expect(wrapper.text()).toContain('Andre spørsmål?')
    expect(wrapper.findAll('.option-btn')).toHaveLength(4)
  })

  it('marks an inline correct answer without hiding the learning content', async () => {
    const wrapper = mount(LearningTask, { props: { task: TASK } })

    const options = wrapper.findAll('.option-btn')
    await options[0].trigger('click') // 'Riktig' — correct

    expect(wrapper.find('.option-btn--correct').exists()).toBe(true)
    expect(wrapper.text()).toContain('Riktig!')
    expect(wrapper.text()).toContain('Overskrift 1')
    expect(wrapper.text()).toContain('Andre spørsmål?')
  })

  it('marks wrong answer and resets after 1200ms', async () => {
    const wrapper = mount(LearningTask, { props: { task: TASK } })

    const options = wrapper.findAll('.option-btn')
    await options[1].trigger('click') // 'Feil' — wrong

    expect(wrapper.find('.option-btn--wrong').exists()).toBe(true)
    expect(wrapper.text()).toContain('Prøv igjen')

    vi.advanceTimersByTime(1200)
    await wrapper.vm.$nextTick()

    expect(wrapper.find('.option-btn--wrong').exists()).toBe(false)
    expect(wrapper.text()).toContain('Første spørsmål?')
  })

  it('emits submitted with quizPassed true after all correct answers', async () => {
    const wrapper = mount(LearningTask, { props: { task: TASK } })

    // Answer q1 correctly
    await wrapper.findAll('.option-btn')[0].trigger('click')
    vi.advanceTimersByTime(900)
    await wrapper.vm.$nextTick()

    // Answer q2 correctly
    const opts = wrapper.findAll('.option-btn')
    await opts[3].trigger('click') // 'B' — correct
    vi.advanceTimersByTime(900)
    await wrapper.vm.$nextTick()

    expect(wrapper.emitted('submitted')).toHaveLength(1)
    expect(wrapper.emitted('submitted')[0][0]).toEqual({ quizPassed: true })

    expect(wrapper.text()).toContain('Lagrer...')
    await wrapper.setProps({ result: { correct: true } })
    expect(wrapper.text()).toContain('Neste oppgave →')
  })

  it('resets to LEARN view when task id changes', async () => {
    const wrapper = mount(LearningTask, { props: { task: TASK } })
    await wrapper.findAll('.option-btn')[0].trigger('click')
    expect(wrapper.find('.option-btn--correct').exists()).toBe(true)

    await wrapper.setProps({ task: { ...TASK, id: 99 } })
    expect(wrapper.text()).toContain('Overskrift 1')
    expect(wrapper.text()).toContain('Overskrift 2')
    expect(wrapper.find('.option-btn--correct').exists()).toBe(false)
  })

  it('renders annotated webshop example in marketplace learning mode', () => {
    const wrapper = mount(LearningTask, {
      props: {
        task: {
          ...TASK,
          stopTheme: 'MARKETPLACE',
          contentJson: {
            slides: [
              {
                heading: 'Hvordan ser nettsvindel ut?',
                body: 'Se på dette eksempelet.',
                exampleType: 'Nettbutikk-eksempel',
                examples: ['Ukjent domene', 'Usikker betaling'],
                checks: [],
              },
            ],
            quiz: [{ id: 'q1', question: 'Test?', options: ['Ja'], correct: 'Ja' }],
          },
        },
      },
    })

    expect(wrapper.find('.marketplace-visual').exists()).toBe(true)
    expect(wrapper.text()).toContain('Ukjent nettadresse')
    expect(wrapper.text()).toContain('Utrygg betaling')
  })
})
