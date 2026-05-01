import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import PhishingEmailTask from '@/components/student/PhishingEmailTask.vue'

const TASK = {
  id: 2,
  taskType: 'PHISHING_EMAIL',
  guidanceText: 'Klikk på alle mistenkelige deler av e-posten og trykk Send svar.',
  contentJson: {
    email: {
      fromName: 'DNB Kundeservice',
      fromEmail: 'support@dnb-kundeservice.com',
      subject: 'Viktig: Bekreft kontoen din',
      body: 'Kjære kunde, klikk umiddelbart her.',
      clues: [
        { id: 'sender', type: 'sender', label: 'support@dnb-kundeservice.com', isClue: true, explanation: 'Falskt domene.' },
        { id: 'urgency', type: 'text', label: 'umiddelbart', isClue: true, explanation: 'Hastverk.' }
      ]
    }
  }
}

describe('PhishingEmailTask', () => {
  it('renders email fields', () => {
    const wrapper = mount(PhishingEmailTask, { props: { task: TASK } })
    expect(wrapper.text()).toContain('DNB Kundeservice')
    expect(wrapper.text()).toContain('support@dnb-kundeservice.com')
    expect(wrapper.text()).toContain('Viktig: Bekreft kontoen din')
    expect(wrapper.text()).toContain('Kjære kunde')
  })

  it('renders guidance text', () => {
    const wrapper = mount(PhishingEmailTask, { props: { task: TASK } })
    expect(wrapper.text()).toContain('Klikk på alle mistenkelige deler av e-posten')
  })

  it('submit button is disabled until at least one clue is flagged', async () => {
    const wrapper = mount(PhishingEmailTask, { props: { task: TASK } })
    const submitBtn = wrapper.find('.phishing-task__submit')
    expect(submitBtn.attributes('disabled')).toBeDefined()

    await wrapper.findAll('.clue-btn')[0].trigger('click')
    expect(submitBtn.attributes('disabled')).toBeUndefined()
  })

  it('clicking a clue button flags it; clicking again unflags it', async () => {
    const wrapper = mount(PhishingEmailTask, { props: { task: TASK } })
    const clueBtn = wrapper.findAll('.clue-btn')[0]

    await clueBtn.trigger('click')
    expect(clueBtn.classes()).toContain('clue-btn--flagged')

    await clueBtn.trigger('click')
    expect(clueBtn.classes()).not.toContain('clue-btn--flagged')
  })

  it('emits submitted with flaggedClueIds when submit is clicked', async () => {
    const wrapper = mount(PhishingEmailTask, { props: { task: TASK } })
    const clueBtns = wrapper.findAll('.clue-btn')

    await clueBtns[1].trigger('click') // urgency
    await wrapper.find('.phishing-task__submit').trigger('click')

    expect(wrapper.emitted('submitted')).toHaveLength(1)
    expect(wrapper.emitted('submitted')[0][0]).toEqual({
      flaggedClueIds: ['urgency']
    })
  })

  it('flagged clue button has clue-btn--flagged class', async () => {
    const wrapper = mount(PhishingEmailTask, { props: { task: TASK } })
    const clueBtns = wrapper.findAll('.clue-btn')

    await clueBtns[0].trigger('click') // sender
    expect(clueBtns[0].classes()).toContain('clue-btn--flagged')
    expect(clueBtns[1].classes()).not.toContain('clue-btn--flagged')
  })

  it('shows optional flagged clues with a hint icon instead of wrong', async () => {
    const task = {
      ...TASK,
      contentJson: {
        email: {
          ...TASK.contentJson.email,
          body: 'Hei kunde, klikk umiddelbart her.',
          clues: [
            { id: 'sender', type: 'sender', label: 'support@dnb-kundeservice.com', isClue: true, explanation: 'Falskt domene.' },
            { id: 'urgency', type: 'text', label: 'umiddelbart', isClue: true, explanation: 'Hastverk.' },
            { id: 'greeting', type: 'text', label: 'Hei kunde', isClue: true, explanation: 'Generell hilsen.' }
          ]
        }
      }
    }

    const wrapper = mount(PhishingEmailTask, { props: { task } })
    const greetingBtn = wrapper.findAll('.clue-btn').find(button => button.text() === 'Hei kunde')
    await greetingBtn.trigger('click')
    await wrapper.setProps({
      result: {
        correct: false,
        explanation: 'Forklaring',
        correctClueIds: ['sender', 'urgency'],
        phishingClues: [
          { id: 'sender', label: 'support@dnb-kundeservice.com', explanation: 'Falskt domene.', isClue: true },
          { id: 'urgency', label: 'umiddelbart', explanation: 'Hastverk.', isClue: true },
          { id: 'greeting', label: 'Hei kunde', explanation: 'Generell hilsen.', isClue: true }
        ]
      }
    })

    const clueItems = wrapper.findAll('.phishing-task__clue-item').map(item => item.text())
    expect(clueItems.some(text => text.includes('💡') && text.includes('Hei kunde'))).toBe(true)
    expect(clueItems.some(text => text.includes('❌') && text.includes('Hei kunde'))).toBe(false)
  })

  it('shows counts for correct, wrong and missed clues instead of listing missed clues', async () => {
    const task = {
      ...TASK,
      contentJson: {
        email: {
          ...TASK.contentJson.email,
          body: 'Hei kunde, klikk umiddelbart her.',
          clues: [
            { id: 'sender', type: 'sender', label: 'support@dnb-kundeservice.com', isClue: true, explanation: 'Falskt domene.' },
            { id: 'urgency', type: 'text', label: 'umiddelbart', isClue: true, explanation: 'Hastverk.' },
            { id: 'greeting', type: 'text', label: 'Hei kunde', isClue: true, explanation: 'Generell hilsen.' },
            { id: 'signature', type: 'text', label: 'her', isClue: false, explanation: 'Ikke et faresignal alene.' }
          ]
        }
      }
    }

    const wrapper = mount(PhishingEmailTask, { props: { task } })
    const clueBtns = wrapper.findAll('.clue-btn')

    await clueBtns[0].trigger('click')
    await clueBtns[3].trigger('click')
    await wrapper.setProps({
      result: {
        correct: false,
        explanation: 'Forklaring',
        correctClueIds: ['sender', 'urgency', 'greeting'],
        phishingClues: [
          { id: 'sender', label: 'support@dnb-kundeservice.com', explanation: 'Falskt domene.', isClue: true },
          { id: 'urgency', label: 'umiddelbart', explanation: 'Hastverk.', isClue: true },
          { id: 'greeting', label: 'Hei kunde', explanation: 'Generell hilsen.', isClue: true },
          { id: 'signature', label: 'her', explanation: 'Ikke et faresignal alene.', isClue: false }
        ]
      }
    })

    const stats = wrapper.findAll('.phishing-task__stat').map(item => item.text())
    expect(stats).toContain('1riktig valg')
    expect(stats).toContain('1feil valg')
    expect(stats).toContain('2riktige valg manglet')
    expect(wrapper.text()).not.toContain('🔎')
    expect(wrapper.text()).not.toContain('umiddelbart: Hastverk.')
  })
})
