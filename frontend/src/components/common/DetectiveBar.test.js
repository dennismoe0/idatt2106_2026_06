import { mount } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import { defineComponent } from 'vue'
import DetectiveBar from './DetectiveBar.vue'

const RouterLinkStub = defineComponent({ template: '<a><slot /></a>', props: ['to'] })

function mountBar(props = {}, classroomState = {}, gameState = {}, authState = {}) {
  return mount(DetectiveBar, {
    props: { pageTitle: 'Medaljer', ...props },
    global: {
      plugins: [createTestingPinia({
        initialState: {
          auth: { email: 'ola@student.local', ...authState },
          classroom: { displayName: 'Ola', ...classroomState },
          game: { level: 2, xp: 240, starBalance: 5, ...gameState }
        }
      })],
      stubs: { RouterLink: RouterLinkStub, SoundControls: true }
    }
  })
}

describe('DetectiveBar', () => {
  it('renders player display name from classroom store', () => {
    const w = mountBar()
    expect(w.text()).toContain('Ola')
  })

  it('renders page title', () => {
    const w = mountBar({ pageTitle: 'Toppliste' })
    expect(w.text()).toContain('Toppliste')
  })

  it('shows back link when backTo provided', () => {
    const w = mountBar({ backTo: { name: 'Home' } })
    expect(w.find('[aria-label="Gå tilbake"]').exists()).toBe(true)
  })

  it('hides back link when backTo is null', () => {
    const w = mountBar({ backTo: null })
    expect(w.find('[aria-label="Gå tilbake"]').exists()).toBe(false)
  })

  it('renders xp and stars from game store', () => {
    const w = mountBar({}, {}, { xp: 500, starBalance: 12 })
    expect(w.text()).toContain('500')
    expect(w.text()).toContain('12')
  })
})
