import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import FinalBossTask from '@/components/student/FinalBossTask.vue'

const TASK = {
  id: 7,
  taskType: 'FINAL_BOSS',
  guidanceText: 'Stopp backup-planen.',
  contentJson: {
    intro: 'Tyvens siste forsvar er aktivert.',
    challenges: [
      {
        id: 'c0',
        type: 'FAKE_NEWS',
        systemName: 'Nyhetsserver',
        description: 'Finn den falske nyheten.',
        failureExplanation: 'Denne artikkelen var for godt dokumentert til å være falsk.',
        correctAnswer: { article_0: true, article_1: false },
        articles: [
          { headline: 'Ekte nyhet', source: 'VG', body: 'OK' },
          { headline: 'Falsk nyhet', source: 'spam.no', body: 'Usant' },
        ],
      },
      {
        id: 'c1',
        type: 'PASSWORD',
        systemName: 'Hovedlås',
        description: 'Velg det sterkeste passordet.',
        failureExplanation: 'Det passordet er for lett å gjette.',
        correctAnswer: { selected: 'b' },
        question: 'Hvilket passord er best?',
        options: [
          { id: 'a', value: 'passord123' },
          { id: 'b', value: 'S0l!Bj0rn#77' },
        ],
      },
    ],
  },
}

const MARKETPLACE_TASK = {
  id: 8,
  taskType: 'FINAL_BOSS',
  guidanceText: 'Stopp backup-planen.',
  contentJson: {
    intro: 'Tyvens siste forsvar er aktivert.',
    challenges: [
      {
        id: 'm0',
        type: 'MARKETPLACE',
        systemName: 'Butikksjekk',
        description: 'Finn faresignalet.',
        failureExplanation: 'Nettadressen og betalingen avslører svindelen.',
        question: 'Hva er galt med denne nettsiden?',
        options: [
          { id: 'a', text: 'Ingenting' },
          { id: 'b', text: 'URL-en er falsk og betalingsvalget er utrygt' },
        ],
        correctAnswer: { selected: 'b' },
      },
    ],
  },
}

describe('FinalBossTask', () => {
  it('shows intro and system preview before start', () => {
    const wrapper = mount(FinalBossTask, { props: { task: TASK } })

    expect(wrapper.find('.boss__intro').exists()).toBe(true)
    expect(wrapper.text()).toContain('Backup-planen har startet!')
    expect(wrapper.text()).toContain('Nyhetsserver')
    expect(wrapper.text()).toContain('Hovedlås')
  })

  it('shows stopped progress as text while solving systems', async () => {
    const wrapper = mount(FinalBossTask, { props: { task: TASK } })

    await wrapper.find('.boss__btn--start').trigger('click')

    expect(wrapper.text()).toContain('0 av 2 systemer stoppet')

    await wrapper.findComponent({ name: 'BossFakeNews' }).vm.$emit('answer', { article_0: true, article_1: false })

    expect(wrapper.text()).toContain('1 av 2 systemer stoppet')
  })

  it('shows one retry on wrong answer before moving on', async () => {
    const wrapper = mount(FinalBossTask, { props: { task: TASK } })

    await wrapper.find('.boss__btn--start').trigger('click')
    await wrapper.findComponent({ name: 'BossFakeNews' }).vm.$emit('answer', { article_0: false, article_1: true })

    expect(wrapper.text()).toContain('Prøv igjen')
    expect(wrapper.text()).toContain('Denne artikkelen var for godt dokumentert til å være falsk.')

    await wrapper.get('.boss__retry-btn').trigger('click')

    expect(wrapper.text()).not.toContain('Prøv igjen')
    expect(wrapper.find('.boss__retry-btn').exists()).toBe(false)
  })

  it('accepts correct answer after a retry and advances to the next challenge', async () => {
    const wrapper = mount(FinalBossTask, { props: { task: TASK } })

    await wrapper.find('.boss__btn--start').trigger('click')
    await wrapper.findComponent({ name: 'BossFakeNews' }).vm.$emit('answer', { article_0: false, article_1: true })
    await wrapper.get('.boss__retry-btn').trigger('click')
    await wrapper.findComponent({ name: 'BossFakeNews' }).vm.$emit('answer', { article_0: true, article_1: false })
    await wrapper.get('.boss__btn:not(.boss__btn--finish)').trigger('click')

    expect(wrapper.text()).toContain('Hovedlås')
  })

  it('locks in failure after the retry is spent and lets the player move on', async () => {
    const wrapper = mount(FinalBossTask, { props: { task: TASK } })

    await wrapper.find('.boss__btn--start').trigger('click')
    await wrapper.findComponent({ name: 'BossFakeNews' }).vm.$emit('answer', { article_0: false, article_1: true })
    await wrapper.get('.boss__retry-btn').trigger('click')
    await wrapper.findComponent({ name: 'BossFakeNews' }).vm.$emit('answer', { article_0: false, article_1: true })

    expect(wrapper.find('.boss__retry-btn').exists()).toBe(false)
    expect(wrapper.text()).toContain('Gå videre')

    await wrapper.get('.boss__continue-btn').trigger('click')

    expect(wrapper.text()).toContain('Hovedlås')
  })

  it('emits all collected answers on finish', async () => {
    const wrapper = mount(FinalBossTask, { props: { task: TASK } })

    await wrapper.find('.boss__btn--start').trigger('click')
    await wrapper.findComponent({ name: 'BossFakeNews' }).vm.$emit('answer', { article_0: false, article_1: true })
    await wrapper.get('.boss__retry-btn').trigger('click')
    await wrapper.findComponent({ name: 'BossFakeNews' }).vm.$emit('answer', { article_0: false, article_1: true })
    await wrapper.get('.boss__btn:not(.boss__btn--finish)').trigger('click')
    await wrapper.findComponent({ name: 'BossPassword' }).vm.$emit('answer', { selected: 'b' })
    await wrapper.get('.boss__btn--finish').trigger('click')

    expect(wrapper.emitted('submitted')).toBeTruthy()
    expect(wrapper.emitted('submitted')[0][0]).toEqual({
      challenge_0: null,
      challenge_1: { selected: 'b' },
    })
  })

  it('renders marketplace challenges through the shared choice component', async () => {
    const wrapper = mount(FinalBossTask, { props: { task: MARKETPLACE_TASK } })

    await wrapper.find('.boss__btn--start').trigger('click')
    await wrapper.get('.mini-task__options button:nth-child(2)').trigger('click')
    await wrapper.get('.mini-task__submit').trigger('click')

    expect(wrapper.text()).toContain('System stoppet!')
    expect(wrapper.find('.boss__btn--finish').exists()).toBe(true)
  })
})
