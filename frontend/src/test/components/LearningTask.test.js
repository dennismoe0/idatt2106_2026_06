import { describe, expect, it, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import LearningTask from '@/components/student/LearningTask.vue'

const TASK = {
  id: 1,
  guidanceText: 'Les kortene og svar riktig.',
  contentJson: {
    slides: [
      { icon: '📰', heading: 'Overskrift 1', body: 'Brødtekst 1' },
      { icon: '🔍', heading: 'Overskrift 2', body: 'Brødtekst 2' },
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

  it('starts in SLIDES phase and shows first slide', () => {
    const wrapper = mount(LearningTask, { props: { task: TASK } })
    expect(wrapper.text()).toContain('Overskrift 1')
    expect(wrapper.text()).toContain('Brødtekst 1')
    expect(wrapper.find('.nav-btn--start-quiz').exists()).toBe(false)
  })

  it('shows "Neste" button on first slide, not "Forrige"', () => {
    const wrapper = mount(LearningTask, { props: { task: TASK } })
    expect(wrapper.find('.nav-btn--next').exists()).toBe(true)
    expect(wrapper.find('.nav-btn--back').exists()).toBe(false)
  })

  it('advances to next slide on Neste click', async () => {
    const wrapper = mount(LearningTask, { props: { task: TASK } })
    await wrapper.find('.nav-btn--next').trigger('click')
    expect(wrapper.text()).toContain('Overskrift 2')
  })

  it('shows "Start quiz" button on last slide', async () => {
    const wrapper = mount(LearningTask, { props: { task: TASK } })
    await wrapper.find('.nav-btn--next').trigger('click')
    expect(wrapper.find('.nav-btn--start-quiz').exists()).toBe(true)
  })

  it('transitions to QUIZ phase on Start quiz click', async () => {
    const wrapper = mount(LearningTask, { props: { task: TASK } })
    await wrapper.find('.nav-btn--next').trigger('click')
    await wrapper.find('.nav-btn--start-quiz').trigger('click')
    expect(wrapper.text()).toContain('Første spørsmål?')
    expect(wrapper.findAll('.option-btn')).toHaveLength(2)
  })

  it('marks correct answer and advances after 900ms', async () => {
    const wrapper = mount(LearningTask, { props: { task: TASK } })
    await wrapper.find('.nav-btn--next').trigger('click')
    await wrapper.find('.nav-btn--start-quiz').trigger('click')

    const options = wrapper.findAll('.option-btn')
    await options[0].trigger('click') // 'Riktig' — correct

    expect(wrapper.find('.option-btn--correct').exists()).toBe(true)
    expect(wrapper.text()).toContain('✅ Riktig!')

    vi.advanceTimersByTime(900)
    await wrapper.vm.$nextTick()

    expect(wrapper.text()).toContain('Andre spørsmål?')
  })

  it('marks wrong answer and resets after 1200ms', async () => {
    const wrapper = mount(LearningTask, { props: { task: TASK } })
    await wrapper.find('.nav-btn--next').trigger('click')
    await wrapper.find('.nav-btn--start-quiz').trigger('click')

    const options = wrapper.findAll('.option-btn')
    await options[1].trigger('click') // 'Feil' — wrong

    expect(wrapper.find('.option-btn--wrong').exists()).toBe(true)
    expect(wrapper.text()).toContain('❌ Prøv igjen')

    vi.advanceTimersByTime(1200)
    await wrapper.vm.$nextTick()

    expect(wrapper.find('.option-btn--wrong').exists()).toBe(false)
    expect(wrapper.text()).toContain('Første spørsmål?')
  })

  it('emits submitted with quizPassed true after all correct answers', async () => {
    const wrapper = mount(LearningTask, { props: { task: TASK } })
    await wrapper.find('.nav-btn--next').trigger('click')
    await wrapper.find('.nav-btn--start-quiz').trigger('click')

    // Answer q1 correctly
    await wrapper.findAll('.option-btn')[0].trigger('click')
    vi.advanceTimersByTime(900)
    await wrapper.vm.$nextTick()

    // Answer q2 correctly
    const opts = wrapper.findAll('.option-btn')
    await opts[1].trigger('click') // 'B' — correct
    vi.advanceTimersByTime(900)
    await wrapper.vm.$nextTick()

    expect(wrapper.emitted('submitted')).toHaveLength(1)
    expect(wrapper.emitted('submitted')[0][0]).toEqual({ quizPassed: true })
  })

  it('resets to slide 1 when task id changes', async () => {
    const wrapper = mount(LearningTask, { props: { task: TASK } })
    await wrapper.find('.nav-btn--next').trigger('click')
    expect(wrapper.text()).toContain('Overskrift 2')

    await wrapper.setProps({ task: { ...TASK, id: 99 } })
    expect(wrapper.text()).toContain('Overskrift 1')
  })
})
