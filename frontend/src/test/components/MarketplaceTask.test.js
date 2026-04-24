import { describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import FakeWebshop from '@/components/student/FakeWebshop.vue'
import MarketplaceTask from '@/components/student/MarketplaceTask.vue'

const CLICK_SUSPICIOUS_TASK = {
  id: 4,
  taskType: 'MARKETPLACE',
  guidanceText: 'Klikk på de delene som virker mistenkelige.',
  contentJson: {
    type: 'CLICK_SUSPICIOUS',
    siteName: 'sneaker-blitz.shop',
    question: 'Klikk på de delene du synes er mistenkelige.',
    mockup: {
      headline: 'Nike Air Max — KUN I DAG!',
      tagline: 'Salg slutter om 2 timer.',
      productName: 'Nike Air Max 270',
      price: '299 kr',
      originalPrice: '2 599 kr',
      paymentText: 'Western Union / Gavekort',
      contactText: 'kontakt@sneaker-blitz.shop',
      returnPolicyText: 'Ingen retur på kampanjevarer',
    },
    elements: [
      { id: 'domain', label: 'sneaker-blitz.shop', isSuspicious: true, explanation: 'Ukjent domene.' },
      { id: 'payment', label: 'Western Union / Gavekort', isSuspicious: true, explanation: 'Mistenkelig betaling.' },
      { id: 'price', label: '299 kr', isSuspicious: false, explanation: 'Prisen alene er ikke klikkmålet her.' },
    ],
  },
}

const IDENTIFY_TASK = {
  id: 5,
  taskType: 'MARKETPLACE',
  guidanceText: 'Se etter faresignaler før du handler.',
  contentJson: {
    type: 'IDENTIFY',
    siteName: 'sneaker-blitz.shop',
    question: 'Hva er det tydeligste faresignalet?',
    options: [
      { id: 'cheap', text: 'Prisen er altfor lav' },
      { id: 'colors', text: 'Siden bruker sterke farger' },
      { id: 'shipping', text: 'Siden lover rask frakt' }
    ],
    mockup: {
      headline: 'Eksklusive sneakers til 79 kr',
      productName: 'Street Runner X',
      price: '79 kr'
    }
  }
}

const RANK_TASK = {
  id: 6,
  taskType: 'MARKETPLACE',
  guidanceText: 'Velg nettstedet du ville styrt unna.',
  contentJson: {
    type: 'RANK',
    question: 'Hvilken nettbutikk virker mest sannsynlig å være svindel?',
    sites: [
      { id: 'site-a', name: 'friluftshuset.no' },
      { id: 'site-b', name: 'merkevarer-outlet-fast.com' },
      { id: 'site-c', name: 'spillsonen.no' },
      { id: 'site-d', name: 'bokbyen.no' }
    ]
  }
}

describe('MarketplaceTask', () => {
  it('renders click suspicious webshop and shows the task question', () => {
    const wrapper = mount(MarketplaceTask, { props: { task: CLICK_SUSPICIOUS_TASK } })

    expect(wrapper.findComponent(FakeWebshop).exists()).toBe(true)
    expect(wrapper.text()).toContain('Klikk på de delene du synes er mistenkelige.')
    expect(wrapper.text()).toContain('Nike Air Max — KUN I DAG!')
  })

  it('toggles flagged webshop elements and emits flaggedElementIds', async () => {
    const wrapper = mount(MarketplaceTask, { props: { task: CLICK_SUSPICIOUS_TASK } })

    const shop = wrapper.findComponent(FakeWebshop)
    await shop.vm.$emit('toggle', 'domain')
    await shop.vm.$emit('toggle', 'payment')
    await wrapper.vm.$nextTick()
    await wrapper.find('.submit-btn').trigger('click')

    expect(wrapper.text()).toContain('sneaker-blitz.shop')
    expect(wrapper.text()).toContain('Western Union / Gavekort')
    expect(wrapper.emitted('submitted')).toHaveLength(1)
    expect(wrapper.emitted('submitted')[0][0]).toEqual({ flaggedElementIds: ['domain', 'payment'] })
  })

  it('renders identify guidance, preview, and options', () => {
    const wrapper = mount(MarketplaceTask, { props: { task: IDENTIFY_TASK } })

    expect(wrapper.text()).toContain('Se etter faresignaler før du handler.')
    expect(wrapper.text()).toContain('sneaker-blitz.shop')
    expect(wrapper.text()).toContain('Hva er det tydeligste faresignalet?')
    expect(wrapper.findAll('.option-btn')).toHaveLength(3)
  })

  it('keeps submit disabled until an identify option is selected', async () => {
    const wrapper = mount(MarketplaceTask, { props: { task: IDENTIFY_TASK } })
    const submitBtn = wrapper.find('.submit-btn')

    expect(submitBtn.attributes('disabled')).toBeDefined()

    await wrapper.findAll('.option-btn')[0].trigger('click')

    expect(submitBtn.attributes('disabled')).toBeUndefined()
  })

  it('emits selected answer for identify tasks', async () => {
    const wrapper = mount(MarketplaceTask, { props: { task: IDENTIFY_TASK } })

    await wrapper.findAll('.option-btn')[0].trigger('click')
    await wrapper.find('.submit-btn').trigger('click')

    expect(wrapper.emitted('submitted')).toHaveLength(1)
    expect(wrapper.emitted('submitted')[0][0]).toEqual({ selected: 'cheap' })
  })

  it('resets selected state when task id changes', async () => {
    const wrapper = mount(MarketplaceTask, { props: { task: IDENTIFY_TASK } })

    await wrapper.findAll('.option-btn')[0].trigger('click')
    expect(wrapper.findAll('.option-btn')[0].classes()).toContain('option-btn--selected')

    await wrapper.setProps({
      task: {
        ...IDENTIFY_TASK,
        id: 99,
      }
    })

    expect(wrapper.findAll('.option-btn')[0].classes()).not.toContain('option-btn--selected')
    expect(wrapper.find('.submit-btn').attributes('disabled')).toBeDefined()
  })

  it('renders the webshop mockup when mockup data exists without explicit renderMode', () => {
    const wrapper = mount(MarketplaceTask, { props: { task: IDENTIFY_TASK } })

    expect(wrapper.findComponent(FakeWebshop).exists()).toBe(true)
    expect(wrapper.text()).toContain('Eksklusive sneakers til 79 kr')
  })

  it('respects explicit renderMode html', () => {
    const wrapper = mount(MarketplaceTask, {
      props: {
        task: {
          ...IDENTIFY_TASK,
          id: 8,
          contentJson: {
            ...IDENTIFY_TASK.contentJson,
            renderMode: 'html',
          }
        }
      }
    })

    expect(wrapper.findComponent(FakeWebshop).exists()).toBe(true)
  })

  it('stays in image mode when an explicit image renderMode is provided', () => {
    const wrapper = mount(MarketplaceTask, {
      props: {
        task: {
          ...IDENTIFY_TASK,
          id: 7,
          contentJson: {
            ...IDENTIFY_TASK.contentJson,
            renderMode: 'image',
          }
        }
      }
    })

    expect(wrapper.findComponent(FakeWebshop).exists()).toBe(false)
  })

  it('warns when subtype is unknown and falls back to click suspicious', () => {
    const warnSpy = vi.spyOn(console, 'warn').mockImplementation(() => {})

    const wrapper = mount(MarketplaceTask, {
      props: {
        task: {
          ...CLICK_SUSPICIOUS_TASK,
          contentJson: {
            ...CLICK_SUSPICIOUS_TASK.contentJson,
            type: 'RANK_MULTI',
          }
        }
      }
    })

    expect(warnSpy).toHaveBeenCalled()
    expect(wrapper.findComponent(FakeWebshop).exists()).toBe(true)

    warnSpy.mockRestore()
  })

  it('renders rank sites and emits selected site id', async () => {
    const wrapper = mount(MarketplaceTask, { props: { task: RANK_TASK } })

    expect(wrapper.findAll('.site-card')).toHaveLength(4)

    await wrapper.findAll('.site-card')[1].trigger('click')
    await wrapper.find('.submit-btn').trigger('click')

    expect(wrapper.emitted('submitted')).toHaveLength(1)
    expect(wrapper.emitted('submitted')[0][0]).toEqual({ selected: 'site-b' })
  })

  it('emits next from the result action button', async () => {
    const wrapper = mount(MarketplaceTask, {
      props: {
        task: IDENTIFY_TASK,
        result: {
          correct: true,
          explanation: 'Riktig valg.',
          stopCompleted: false,
        }
      }
    })

    await wrapper.find('.next-btn').trigger('click')

    expect(wrapper.emitted('next')).toHaveLength(1)
  })
})
