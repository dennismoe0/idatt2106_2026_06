import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import MedalsView from '@/views/student/MedalsView.vue'

const fetchAllMedals = vi.fn()

vi.mock('@/stores/game', () => ({
  useGameStore: () => ({
    fetchAllMedals,
  }),
}))

describe('MedalsView', () => {
  beforeEach(() => {
    fetchAllMedals.mockReset()
  })

  function mountMedalsView() {
    return mount(MedalsView, {
      global: {
        stubs: {
          CorkBoardPage: {
            props: ['pageTitle', 'backTo'],
            template: '<div class="cork-board-page-stub"><slot /></div>',
          },
        },
      },
    })
  }

  it('shows 0 percent progress when there are no medals', async () => {
    fetchAllMedals.mockResolvedValue([])

    const wrapper = mountMedalsView()
    await flushPromises()

    expect(wrapper.text()).toContain('0 / 0')
    expect(wrapper.text()).toContain('0%')
    expect(wrapper.get('.medals__meter-fill').attributes('style')).toContain('width: 0%')
  })

  it('shows the latest earned medal label when medals are earned', async () => {
    fetchAllMedals.mockResolvedValue([
      { id: 1, name: 'Nyhetsdetektiv', description: 'Fullført stopp 1', earnedAt: '2026-04-10T12:00:00Z' },
      { id: 2, name: 'Passordvokter', description: 'Fullført stopp 2', earnedAt: '2026-04-18T12:00:00Z' },
      { id: 3, name: 'Låst medalje', description: 'Ikke låst opp ennå', earnedAt: null },
    ])

    const wrapper = mountMedalsView()
    await flushPromises()

    expect(wrapper.text()).toContain('Passordvokter')
    expect(wrapper.text()).not.toContain('Ingen medaljer ennå')
  })

  it('shows the empty latest-earned label when no medals are earned', async () => {
    fetchAllMedals.mockResolvedValue([
      { id: 1, name: 'Nyhetsdetektiv', description: 'Fullført stopp 1', earnedAt: null },
      { id: 2, name: 'Passordvokter', description: 'Fullført stopp 2', earnedAt: null },
    ])

    const wrapper = mountMedalsView()
    await flushPromises()

    expect(wrapper.text()).toContain('Ingen medaljer ennå')
  })
})
