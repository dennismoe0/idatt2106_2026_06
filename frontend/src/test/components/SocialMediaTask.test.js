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

const IDENTIFY_WORST_TASK = {
  id: 7,
  taskType: 'SOCIAL_MEDIA',
  guidanceText: 'Finn innlegget med minst troverdighet.',
  contentJson: {
    type: 'IDENTIFY_WORST',
    question: 'Hvilket innlegg er mest illegitimt?',
    posts: [
      {
        id: 'post_0',
        platform: 'Fjesbok',
        username: 'Ordførerens kontor',
        avatar: '🏛️',
        content: 'Kommunen jobber aktivt med saken.',
        likes: 312,
        timestamp: '1 time siden',
        verified: true,
      },
      {
        id: 'post_1',
        platform: 'Fjesbok',
        username: 'SannhetsJegeren99',
        avatar: '👁️',
        content: 'DEL DETTE FØR DE SLETTER DET!',
        likes: 18432,
        timestamp: '45 min siden',
        verified: false,
      },
      {
        id: 'post_2',
        platform: 'Fjesbok',
        username: 'Lokal Reporter',
        avatar: '📝',
        content: 'Politiet bekrefter at etterforskningen pågår.',
        likes: 891,
        timestamp: '2 timer siden',
        verified: false,
      }
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

  it('renders all identify-worst posts and submits the post ranked as least trustworthy', async () => {
    const wrapper = mount(SocialMediaTask, { props: { task: IDENTIFY_WORST_TASK } })

    expect(wrapper.findAll('.post-card')).toHaveLength(3)
    expect(wrapper.find('.submit-btn').attributes('disabled')).toBeDefined()

    const rankInputs = wrapper.findAll('.rank-picker__select')
    await rankInputs[0].setValue('2')
    await rankInputs[1].setValue('1')
    await rankInputs[2].setValue('3')

    expect(wrapper.find('.submit-btn').attributes('disabled')).toBeUndefined()

    await wrapper.find('.submit-btn').trigger('click')

    expect(wrapper.emitted('submitted')).toHaveLength(1)
    expect(wrapper.emitted('submitted')[0][0]).toEqual({ selected: 'post_1' })
  })
})
