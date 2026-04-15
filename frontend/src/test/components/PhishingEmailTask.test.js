import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import PhishingEmailTask from '@/components/student/PhishingEmailTask.vue'

const TASK = {
  id: 2,
  taskType: 'PHISHING_EMAIL',
  guidanceText: 'Velg tryggeste handling.',
  contentJson: {
    email: {
      fromName: 'DNB Kundeservice',
      fromEmail: 'support@dnb-kundeservice.com',
      subject: 'Viktig: Bekreft kontoen din',
      body: 'Klikk her innen 24 timer.'
    }
  }
}

describe('PhishingEmailTask', () => {
  it('renders email fields', () => {
    const wrapper = mount(PhishingEmailTask, { props: { task: TASK } })
    expect(wrapper.text()).toContain('DNB Kundeservice')
    expect(wrapper.text()).toContain('support@dnb-kundeservice.com')
    expect(wrapper.text()).toContain('Viktig: Bekreft kontoen din')
    expect(wrapper.text()).toContain('Klikk her innen 24 timer.')
  })

  it('renders guidance text', () => {
    const wrapper = mount(PhishingEmailTask, { props: { task: TASK } })
    expect(wrapper.text()).toContain('Velg tryggeste handling.')
  })

  it('submit button is disabled until action is chosen', async () => {
    const wrapper = mount(PhishingEmailTask, { props: { task: TASK } })
    const submitBtn = wrapper.find('.submit-btn')
    expect(submitBtn.attributes('disabled')).toBeDefined()

    const actionButtons = wrapper.findAll('.actions button')
    await actionButtons[1].trigger('click') // Rapporter
    expect(submitBtn.attributes('disabled')).toBeUndefined()
  })

  it('flag toggle adds and removes items', async () => {
    const wrapper = mount(PhishingEmailTask, { props: { task: TASK } })
    const flagBtn = wrapper.find('.flag-toggle')

    await flagBtn.trigger('click')
    expect(flagBtn.classes()).toContain('flagged')

    await flagBtn.trigger('click')
    expect(flagBtn.classes()).not.toContain('flagged')
  })

  it('emits submitted with action and flagged items', async () => {
    const wrapper = mount(PhishingEmailTask, { props: { task: TASK } })

    await wrapper.find('.flag-toggle').trigger('click')

    const actionButtons = wrapper.findAll('.actions button')
    await actionButtons[1].trigger('click') // Rapporter
    await wrapper.find('.submit-btn').trigger('click')

    expect(wrapper.emitted('submitted')).toHaveLength(1)
    expect(wrapper.emitted('submitted')[0][0]).toEqual({
      action: 'REPORT',
      flagged: ['fromEmail']
    })
  })

  it('highlights selected action button', async () => {
    const wrapper = mount(PhishingEmailTask, { props: { task: TASK } })
    const actionButtons = wrapper.findAll('.actions button')

    await actionButtons[2].trigger('click') // Spør en voksen
    expect(actionButtons[2].classes()).toContain('selected')
    expect(actionButtons[0].classes()).not.toContain('selected')
  })
})
