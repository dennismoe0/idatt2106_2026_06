import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import MarketplaceTask from '@/components/student/MarketplaceTask.vue'

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

const IDENTIFY_HTML_TASK = {
  ...IDENTIFY_TASK,
  id: 7,
  contentJson: {
    ...IDENTIFY_TASK.contentJson,
    renderMode: 'html',
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

  it('defaults identify preview to image mode unless renderMode is html', () => {
    const imageWrapper = mount(MarketplaceTask, { props: { task: IDENTIFY_TASK } })
    const htmlWrapper = mount(MarketplaceTask, { props: { task: IDENTIFY_HTML_TASK } })

    expect(imageWrapper.find('.site-preview__mockup').exists()).toBe(false)
    expect(htmlWrapper.find('.site-preview__mockup').exists()).toBe(true)
  })

  it('renders rank sites and emits selected site id', async () => {
    const wrapper = mount(MarketplaceTask, { props: { task: RANK_TASK } })

    expect(wrapper.findAll('.site-card')).toHaveLength(4)

    await wrapper.findAll('.site-card')[1].trigger('click')
    await wrapper.find('.submit-btn').trigger('click')

    expect(wrapper.emitted('submitted')).toHaveLength(1)
    expect(wrapper.emitted('submitted')[0][0]).toEqual({ selected: 'site-b' })
  })
})
