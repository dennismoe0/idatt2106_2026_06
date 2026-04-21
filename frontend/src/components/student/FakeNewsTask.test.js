import { mount } from '@vue/test-utils'
import FakeNewsTask from './FakeNewsTask.vue'

const task = {
  id: 1,
  guidanceText: 'Klikk på den falske nyheten.',
  contentJson: {
    articles: [
      { headline: 'Katter tar over nett', body: 'Et eksempel.', source: 'eksempel.no' },
      { headline: 'Passord123 mest brukt', body: 'En studie viser.', source: 'nrk.no' }
    ]
  }
}

function mountTask(props = {}) {
  return mount(FakeNewsTask, {
    props: { task, result: null, isLastTask: false, ...props },
    global: { stubs: { ConfettiOverlay: true } }
  })
}

describe('FakeNewsTask', () => {
  it('renders all articles as clickable buttons', () => {
    const w = mountTask()
    const cards = w.findAll('[role="button"]')
    expect(cards.length).toBe(2)
  })

  it('emits submitted with correct answer map on card click', async () => {
    const w = mountTask()
    await w.findAll('[role="button"]')[0].trigger('click')
    const emitted = w.emitted('submitted')
    expect(emitted).toBeTruthy()
    expect(emitted[0][0]).toMatchObject({ article_0: false, article_1: true })
  })

  it('does not emit when result is already set', async () => {
    const w = mountTask({ result: { correct: true, explanation: 'test' } })
    await w.findAll('[role="button"]')[0].trigger('click')
    expect(w.emitted('submitted')).toBeFalsy()
  })

  it('marks chosen card as selected before result', async () => {
    const w = mountTask()
    await w.findAll('[role="button"]')[1].trigger('click')
    expect(w.findAll('[role="button"]')[1].classes()).toContain('article-card--chosen')
  })

  it('shows correct feedback classes when result is correct', () => {
    const w = mountTask({ result: { correct: true, explanation: 'Bra!' } })
    expect(w.html()).toContain('article-card')
  })

  it('shows Next button after result', () => {
    const w = mountTask({ result: { correct: false, explanation: 'Prøv igjen.' } })
    expect(w.find('.next-btn').exists()).toBe(true)
  })
})
