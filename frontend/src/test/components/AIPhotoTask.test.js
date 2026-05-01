import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import AIPhotoTask from '@/components/student/AIPhotoTask.vue'

const TASK = {
  id: 10,
  guidanceText: 'Se nøye på detaljene.',
  contentJson: {
    images: [
      {
        id: 'image_0',
        label: 'Bilde A',
        alt: 'Strandbilde',
        correctType: 'AI_GENERATED',
        wrongFeedback: 'Feil, se på barna i bakgrunnen. Er det noe rart her?',
        correctFeedback: 'Riktig! Barna i bildet er satt inn i bildet, mens resten av bildet er ekte.',
      },
      {
        id: 'image_1',
        label: 'Bilde B',
        alt: 'Promenadebilde',
        correctType: 'AI_GENERATED',
        wrongFeedback: 'Feil, se på menneskene, bygningene og landskapet. Er det noe feil her?',
        correctFeedback: 'Riktig! Bygninger, landskap og mennesker har mange feil. Dette bildet er helt KI-generert.',
      },
    ],
    question: 'Sorter hvert bilde.',
    explanation: 'Se forklaringene under hvert bilde.',
  },
}

describe('AIPhotoTask', () => {
  it('shows image-specific wrong and correct feedback after submitting', async () => {
    const wrapper = mount(AIPhotoTask, { props: { task: TASK } })
    const buttons = wrapper.findAll('.type-btn')

    await buttons[0].trigger('click')
    await buttons[3].trigger('click')
    await wrapper.setProps({
      result: { correct: false, explanation: 'Se forklaringene under hvert bilde.', stopCompleted: false },
    })

    const cards = wrapper.findAll('.image-card')
    expect(cards[0].classes()).toContain('image-card--wrong')
    expect(cards[0].text()).toContain('Feil, se på barna i bakgrunnen. Er det noe rart her?')
    expect(cards[1].classes()).toContain('image-card--correct')
    expect(cards[1].text()).toContain('Riktig! Bygninger, landskap og mennesker har mange feil. Dette bildet er helt KI-generert.')
  })
})
