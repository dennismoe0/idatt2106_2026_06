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

  it('submit button is disabled until all articles are rated', async () => {
    const wrapper = mount(FakeNewsTask, { props: { task: TASK } })
    const submitBtn = wrapper.find('.submit-btn')
    expect(submitBtn.attributes('disabled')).toBeDefined()

    const buttons = wrapper.findAll('.actions button')
    await buttons[0].trigger('click')
    expect(submitBtn.attributes('disabled')).toBeDefined()

    await buttons[3].trigger('click')
    expect(submitBtn.attributes('disabled')).toBeUndefined()
  })

  it('emits submitted with correct answer shape', async () => {
    const wrapper = mount(FakeNewsTask, { props: { task: TASK } })
    const buttons = wrapper.findAll('.actions button')

    await buttons[0].trigger('click')
    await buttons[3].trigger('click')
    await wrapper.find('.submit-btn').trigger('click')

    expect(wrapper.emitted('submitted')).toHaveLength(1)
    expect(wrapper.emitted('submitted')[0][0]).toEqual({
      article_0: true,
      article_1: false
    })
  })

  it('toggles selection when clicking different option for same article', async () => {
    const wrapper = mount(FakeNewsTask, { props: { task: TASK } })
    const buttons = wrapper.findAll('.actions button')

    await buttons[0].trigger('click')
    expect(buttons[0].classes()).toContain('selected')

    await buttons[1].trigger('click')
    expect(buttons[0].classes()).not.toContain('selected')
    expect(buttons[1].classes()).toContain('selected')
  })

  it('has aria-labels on Ekte/Falsk buttons', () => {
    const wrapper = mount(FakeNewsTask, { props: { task: TASK } })
    const buttons = wrapper.findAll('.actions button')
    expect(buttons[0].attributes('aria-label')).toBe('Marker artikkel 1 som ekte')
    expect(buttons[1].attributes('aria-label')).toBe('Marker artikkel 1 som falsk')
    expect(buttons[2].attributes('aria-label')).toBe('Marker artikkel 2 som ekte')
    expect(buttons[3].attributes('aria-label')).toBe('Marker artikkel 2 som falsk')
  })
})
