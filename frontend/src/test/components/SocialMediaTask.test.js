import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import SocialMediaTask from '@/components/student/SocialMediaTask.vue'

const TASK = {
  id: 6,
  taskType: 'SOCIAL_MEDIA',
  guidanceText: 'Les innlegget nøye og velg den tryggeste handlingen.',
  contentJson: {
    post: {
      platform: 'Fjesbok',
      username: 'BesteFriend99',
      avatar: '👤',
      content: 'Hei! Jeg vant en premie og trenger telefonnummeret ditt for å sende den.'
    },
    question: 'Hva gjør du?',
    options: [
      { id: 'reply', text: 'Svar med telefonnummeret mitt' },
      { id: 'ignore', text: 'Ignorer meldingen' },
      { id: 'report', text: 'Rapporter og blokker kontoen' },
      { id: 'ask', text: 'Spør hvem det er' }
    ]
  }
}

describe('SocialMediaTask', () => {
  it('renders platform label, post, and options', () => {
    const wrapper = mount(SocialMediaTask, { props: { task: TASK } })

    expect(wrapper.text()).toContain('Fjesbok')
    expect(wrapper.text()).toContain('BesteFriend99')
    expect(wrapper.text()).toContain('Hva gjør du?')
    expect(wrapper.findAll('.option-btn')).toHaveLength(4)
  })

  it('keeps submit disabled until an option is selected', async () => {
    const wrapper = mount(SocialMediaTask, { props: { task: TASK } })
    const submitBtn = wrapper.find('.submit-btn')

    expect(submitBtn.attributes('disabled')).toBeDefined()

    await wrapper.findAll('.option-btn')[2].trigger('click')

    expect(submitBtn.attributes('disabled')).toBeUndefined()
  })

  it('emits selected answer on submit', async () => {
    const wrapper = mount(SocialMediaTask, { props: { task: TASK } })

    await wrapper.findAll('.option-btn')[2].trigger('click')
    await wrapper.find('.submit-btn').trigger('click')

    expect(wrapper.emitted('submitted')).toHaveLength(1)
    expect(wrapper.emitted('submitted')[0][0]).toEqual({ selected: 'report' })
  })
})
