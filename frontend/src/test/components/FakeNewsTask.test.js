import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import FakeNewsTask from '@/components/student/FakeNewsTask.vue'

const TASK = {
  id: 1,
  taskType: 'FAKE_NEWS',
  guidanceText: 'Marker hver artikkel som ekte eller falsk.',
  contentJson: {
    articles: [
      { headline: 'Ekte overskrift', body: 'Ekte brødtekst', source: 'VG' },
      { headline: 'Falsk overskrift', body: 'Falsk brødtekst', source: 'nyheter24-ekte.no' }
    ]
  }
}

describe('FakeNewsTask', () => {
  it('renders both articles', () => {
    const wrapper = mount(FakeNewsTask, { props: { task: TASK } })
    const articles = wrapper.findAll('.article-card')
    expect(articles).toHaveLength(2)
    expect(articles[0].text()).toContain('Ekte overskrift')
    expect(articles[1].text()).toContain('Falsk overskrift')
  })

  it('renders guidance text', () => {
    const wrapper = mount(FakeNewsTask, { props: { task: TASK } })
    expect(wrapper.text()).toContain('Marker hver artikkel som ekte eller falsk.')
  })

  it('clicking an article card immediately emits submitted', async () => {
    const wrapper = mount(FakeNewsTask, { props: { task: TASK } })
    expect(wrapper.emitted('submitted')).toBeFalsy()
    await wrapper.find('.article-card').trigger('click')
    expect(wrapper.emitted('submitted')).toHaveLength(1)
  })

  it('emits correct answer shape — picked card is false, others are true', async () => {
    const wrapper = mount(FakeNewsTask, { props: { task: TASK } })
    const cards = wrapper.findAll('.article-card')
    await cards[0].trigger('click')
    expect(wrapper.emitted('submitted')[0][0]).toEqual({
      article_0: false,
      article_1: true
    })
  })

  it('chosen card gets aria-pressed true', async () => {
    const wrapper = mount(FakeNewsTask, { props: { task: TASK } })
    const cards = wrapper.findAll('.article-card')
    await cards[1].trigger('click')
    expect(cards[1].attributes('aria-pressed')).toBe('true')
    expect(cards[0].attributes('aria-pressed')).toBe('false')
  })

  it('article cards have aria-label describing action', () => {
    const wrapper = mount(FakeNewsTask, { props: { task: TASK } })
    const cards = wrapper.findAll('.article-card')
    expect(cards[0].attributes('aria-label')).toContain('Ekte overskrift')
    expect(cards[1].attributes('aria-label')).toContain('Falsk overskrift')
  })
})
