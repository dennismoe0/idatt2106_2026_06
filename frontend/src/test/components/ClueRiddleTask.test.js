import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import ClueRiddleTask from '@/components/student/ClueRiddleTask.vue'

const TASK = {
  id: 404,
  title: 'Gåtespor: Passordet i loggen',
  description: 'Det siste sporet handler om passordet tyven brukte på en reservekonto.',
  contentJson: {
    purpose: 'Du bruker det du lærte om passord for å lese et siste digitalt spor. Et lekket passord kan avsløre både vaner og hvem kontoen er knyttet til.',
    evidence: 'Reservekontoen brukte passordet XooInnAdmin2019.',
    evidencePassword: 'XooInnAdmin2019',
    variant: 'password',
    question: 'Hva forteller passordet oss?',
    options: [
      {
        id: 'random_strong',
        label: 'Det er et sterkt tilfeldig passord',
        detail: 'Det er ikke tilfeldig: det inneholder sted, rolle og årstall.',
      },
      {
        id: 'cafe_admin',
        label: 'Noen med admin-tilgang på Xoo Inn Cafe laget eller kjente kontoen',
        detail: 'Passordet peker mot stedet og en administratorrolle.',
      },
      {
        id: 'no_clue',
        label: 'Passord gir aldri etterforskningsspor',
        detail: 'Passord kan ofte avsløre vaner og koblinger.',
      },
    ],
  },
}

describe('ClueRiddleTask', () => {
  it('renders the password clue as highlighted evidence', () => {
    const wrapper = mount(ClueRiddleTask, { props: { task: TASK } })

    expect(wrapper.find('.password-chip strong').text()).toBe('XooInnAdmin2019')
    expect(wrapper.text()).toContain('Bevismappe')
    expect(wrapper.text()).toContain('Et lekket passord kan avsløre både vaner og hvem kontoen er knyttet til.')
    expect(wrapper.text()).toContain('Reservekontoen brukte passordet XooInnAdmin2019.')
    expect(wrapper.find('.question-strip h3').text()).toBe('Hva forteller passordet oss?')
  })

  it('emits the selected option id on submit', async () => {
    const wrapper = mount(ClueRiddleTask, { props: { task: TASK } })

    await wrapper.findAll('.evidence-card')[1].trigger('click')
    await wrapper.find('.submit-btn').trigger('click')

    expect(wrapper.emitted('submitted')[0][0]).toEqual({ selected: 'cafe_admin' })
  })

  it('does not show the backend success explanation when the answer is wrong', async () => {
    const wrapper = mount(ClueRiddleTask, { props: { task: TASK } })

    await wrapper.findAll('.evidence-card')[0].trigger('click')
    await wrapper.setProps({
      result: {
        correct: false,
        explanation: 'Riktig. Passordet peker mot noen med admin-kobling til Xoo Inn Cafe.',
      },
    })

    expect(wrapper.text()).toContain('Ikke helt.')
    expect(wrapper.text()).toContain('det inneholder sted, rolle og årstall')
    expect(wrapper.text()).not.toContain('Riktig. Passordet peker mot noen med admin-kobling')
  })
})
