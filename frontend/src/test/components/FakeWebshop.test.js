import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import FakeWebshop from '@/components/student/FakeWebshop.vue'

describe('FakeWebshop', () => {
  it('renders required webshop content', () => {
    const wrapper = mount(FakeWebshop, {
      props: {
        siteName: 'super-deals-norge.xyz',
        headline: 'Eksklusive sko til halv pris',
        productName: 'Street Runner X',
        price: '79 kr',
      }
    })

    expect(wrapper.text()).toContain('super-deals-norge.xyz')
    expect(wrapper.text()).toContain('Eksklusive sko til halv pris')
    expect(wrapper.text()).toContain('Street Runner X')
    expect(wrapper.text()).toContain('79 kr')
  })

  it('shows original price and badges only when provided', () => {
    const wrapper = mount(FakeWebshop, {
      props: {
        originalPrice: '1 499 kr',
        badges: ['90 % rabatt', 'Kun i dag'],
      }
    })

    expect(wrapper.find('.fake-shop__old-price').exists()).toBe(true)
    expect(wrapper.findAll('.fake-shop__badge')).toHaveLength(2)
  })

  it('hides optional original price and badges when empty', () => {
    const wrapper = mount(FakeWebshop, {
      props: {
        originalPrice: '',
        badges: [],
      }
    })

    expect(wrapper.find('.fake-shop__old-price').exists()).toBe(false)
    expect(wrapper.find('.fake-shop__badges').exists()).toBe(false)
  })

  it('renders payment and contact text from props', () => {
    const wrapper = mount(FakeWebshop, {
      props: {
        paymentText: 'Betaling: Kun gavekort',
        contactText: 'Kontakt: ingen info tilgjengelig',
      }
    })

    expect(wrapper.text()).toContain('Betaling: Kun gavekort')
    expect(wrapper.text()).toContain('Kontakt: ingen info tilgjengelig')
  })
})
