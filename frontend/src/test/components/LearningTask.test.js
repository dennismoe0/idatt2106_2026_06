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

  function optionByText(wrapper, text) {
    const option = wrapper.findAll('.option-btn').find(button => button.text() === text)
    expect(option, `Expected option "${text}" to exist`).toBeTruthy()
    return option
  }

  it('starts in LEARN phase on the first learning section only', () => {
    const wrapper = mount(LearningTask, { props: { task: TASK } })
    expect(wrapper.text()).toContain('Overskrift 1')
    expect(wrapper.text()).toContain('Brødtekst 1')
    expect(wrapper.text()).toContain('Eksempel 1A')
    expect(wrapper.text()).toContain('Første spørsmål?')
    expect(wrapper.text()).not.toContain('Overskrift 2')
    expect(wrapper.text()).not.toContain('Andre spørsmål?')
    expect(wrapper.text()).toContain('Neste del →')
  })

  it('shows one inline question at a time and no separate start quiz button', () => {
    const wrapper = mount(LearningTask, { props: { task: TASK } })
    expect(wrapper.find('.nav-btn--start-quiz').exists()).toBe(false)
    expect(wrapper.findAll('.inline-question')).toHaveLength(1)
    expect(wrapper.find('.learn-complete').exists()).toBe(false)
    expect(wrapper.text()).toContain('Neste del →')
    expect(wrapper.text()).toContain('Forrige del')
  })

  it('renders answer options for the active learning part', async () => {
    const wrapper = mount(LearningTask, { props: { task: TASK } })
    expect(wrapper.text()).toContain('Første spørsmål?')
    expect(wrapper.text()).not.toContain('Andre spørsmål?')
    expect(wrapper.findAll('.option-btn')).toHaveLength(2)
  })

  it('marks an inline correct answer and unlocks the next section', async () => {
    const wrapper = mount(LearningTask, { props: { task: TASK } })

    await optionByText(wrapper, 'Riktig').trigger('click')

    expect(wrapper.find('.option-btn--correct').exists()).toBe(true)
    expect(wrapper.text()).toContain('Riktig!')
    expect(wrapper.text()).toContain('Overskrift 1')
    expect(wrapper.text()).not.toContain('Andre spørsmål?')

    const nextButton = wrapper.find('.nav-btn--primary')
    expect(nextButton.attributes('disabled')).toBeUndefined()

    await nextButton.trigger('click')
    expect(wrapper.text()).toContain('Overskrift 2')
    expect(wrapper.text()).toContain('Andre spørsmål?')
  })

  it('marks wrong answer and resets after 1200ms', async () => {
    const wrapper = mount(LearningTask, { props: { task: TASK } })

    await optionByText(wrapper, 'Feil').trigger('click')

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
    await optionByText(wrapper, 'Riktig').trigger('click')
    await wrapper.vm.$nextTick()

    await wrapper.find('.nav-btn--primary').trigger('click')
    expect(wrapper.text()).toContain('Andre spørsmål?')

    // Answer q2 correctly
    await optionByText(wrapper, 'B').trigger('click')
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
    await optionByText(wrapper, 'Riktig').trigger('click')
    expect(wrapper.find('.option-btn--correct').exists()).toBe(true)

    await wrapper.setProps({ task: { ...TASK, id: 99 } })
    expect(wrapper.text()).toContain('Overskrift 1')
    expect(wrapper.text()).not.toContain('Overskrift 2')
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

  it('renders real and manipulated photo examples in the third AI photo learning part', async () => {
    const wrapper = mount(LearningTask, {
      props: {
        task: {
          ...TASK,
          stopTheme: 'AI_PHOTO',
          contentJson: {
            slides: [
              { heading: 'Del 1', body: 'Tekst', examples: [], checks: [] },
              { heading: 'Del 2', body: 'Tekst', examples: [], checks: [] },
              { heading: 'Del 3', body: 'Tekst', examples: ['Ekte og manipulert'], checks: [] },
            ],
            quiz: [
              { id: 'q1', question: 'Q1?', options: ['Ja'], correct: 'Ja' },
              { id: 'q2', question: 'Q2?', options: ['Ja'], correct: 'Ja' },
              { id: 'q3', question: 'Q3?', options: ['Ja'], correct: 'Ja' },
            ],
          },
        },
      },
    })

    await wrapper.findAll('.option-btn')[0].trigger('click')
    await wrapper.find('.nav-btn--primary').trigger('click')
    await wrapper.findAll('.option-btn')[0].trigger('click')
    await wrapper.find('.nav-btn--primary').trigger('click')

    expect(wrapper.find('.photo-compare').exists()).toBe(true)
    expect(wrapper.text()).toContain('Ekte bilde')
    expect(wrapper.text()).toContain('Manipulert bilde')
    expect(wrapper.find('img[src="/story_pictures/photographer-real-playground.jpg"]').exists()).toBe(true)
    expect(wrapper.find('img[src="/story_pictures/photographer-manipulated-playground.png"]').exists()).toBe(true)
  })

  it('renders the AI photo example in the second AI photo learning part', async () => {
    const wrapper = mount(LearningTask, {
      props: {
        task: {
          ...TASK,
          stopTheme: 'AI_PHOTO',
          contentJson: {
            slides: [
              { heading: 'Del 1', body: 'Tekst', examples: [], checks: [] },
              { heading: 'Del 2', body: 'Tekst', examples: ['KI-bilde'], checks: [] },
              { heading: 'Del 3', body: 'Tekst', examples: [], checks: [] },
            ],
            quiz: [
              { id: 'q1', question: 'Q1?', options: ['Ja'], correct: 'Ja' },
              { id: 'q2', question: 'Q2?', options: ['Ja'], correct: 'Ja' },
              { id: 'q3', question: 'Q3?', options: ['Ja'], correct: 'Ja' },
            ],
          },
        },
      },
    })

    await wrapper.findAll('.option-btn')[0].trigger('click')
    await wrapper.find('.nav-btn--primary').trigger('click')

    expect(wrapper.find('.photo-compare--single').exists()).toBe(true)
    expect(wrapper.text()).toContain('KI-generert bilde')
    expect(wrapper.find('img[src="/story_pictures/photographer-ai-paris-cafe.png"]').exists()).toBe(true)
  })
})
