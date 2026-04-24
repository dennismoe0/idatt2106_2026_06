import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'

vi.mock('@/components/common/BackButton.vue', () => ({
  default: { template: '<button type="button">Tilbake</button>' },
}))
vi.mock('@/components/common/LoadingSpinner.vue', () => ({
  default: { template: '<span data-testid="spinner">Loading</span>' },
}))
vi.mock('@/components/common/BaseButton.vue', () => ({
  default: {
    props: ['loading', 'disabled'],
    emits: ['click'],
    template: `<button :disabled="disabled" @click="$emit('click')"><slot /></button>`,
  },
}))
vi.mock('@/components/student/avatar/layers/AvatarHair.vue', () => ({
  default: { template: '<svg data-testid="avatar-hair" />' },
}))
vi.mock('@/components/student/avatar/layers/AvatarOutfit.vue', () => ({
  default: { template: '<svg data-testid="avatar-outfit" />' },
}))

const router = createRouter({
  history: createMemoryHistory(),
  routes: [
    { path: '/shop', name: 'Shop', component: { template: '<div />' } },
    { path: '/',     name: 'Home', component: { template: '<div />' } },
  ],
})

const SAMPLE_ITEMS = [
  { id: 1, optionType: 'hairColor', optionValue: '#1a1a1a',        starPrice: 10, purchased: false },
  { id: 2, optionType: 'hairColor', optionValue: '#cc8844',        starPrice: 20, purchased: true  },
  { id: 3, optionType: 'hairStyle', optionValue: 'short',          starPrice: 50, purchased: false },
  { id: 4, optionType: 'hairStyle', optionValue: 'long',           starPrice: 50, purchased: true  },
  { id: 5, optionType: 'outfit',    optionValue: 'detective-coat', starPrice: 80, purchased: false },
]

function makeAvatarStore(overrides = {}) {
  return {
    shopItems: SAMPLE_ITEMS,
    fetchShop:    vi.fn().mockResolvedValue(),
    purchaseItem: vi.fn().mockResolvedValue(),
    ...overrides,
  }
}

function makeGameStore(overrides = {}) {
  return { starBalance: 100, ...overrides }
}

async function mountShopView(avatarStore = makeAvatarStore(), gameStore = makeGameStore()) {
  vi.doMock('@/stores/avatar', () => ({ useAvatarStore: () => avatarStore }))
  vi.doMock('@/stores/game',   () => ({ useGameStore:   () => gameStore   }))
  const { default: ShopView } = await import('@/views/student/ShopView.vue')
  return mount(ShopView, { global: { plugins: [router] } })
}

describe('ShopView', () => {
  beforeEach(() => {
    vi.resetModules()
    vi.clearAllMocks()
  })

  it('shows loading spinner while fetchShop is pending', async () => {
    let resolve
    const avatarStore = makeAvatarStore({
      fetchShop: vi.fn().mockReturnValue(new Promise(r => { resolve = r })),
    })
    const wrapper = await mountShopView(avatarStore)

    expect(wrapper.find('[data-testid="spinner"]').exists()).toBe(true)

    resolve()
    await flushPromises()
    expect(wrapper.find('[data-testid="spinner"]').exists()).toBe(false)
  })

  it('shows error state with retry button when fetchShop rejects', async () => {
    const avatarStore = makeAvatarStore({
      fetchShop: vi.fn().mockRejectedValue(new Error('network error')),
    })
    const wrapper = await mountShopView(avatarStore)
    await flushPromises()

    expect(wrapper.find('.shop-state-card').exists()).toBe(true)
    expect(wrapper.text()).toContain('Kunne ikke laste butikken')
    expect(wrapper.find('button').exists()).toBe(true)
  })

  it('renders three category sections after loading', async () => {
    const wrapper = await mountShopView()
    await flushPromises()

    expect(wrapper.findAll('.shop-section')).toHaveLength(3)
  })

  it('section rules contain category label text', async () => {
    const wrapper = await mountShopView()
    await flushPromises()

    const labels = wrapper.findAll('.shop-rule-label')
    const texts  = labels.map(l => l.text())
    expect(texts.some(t => t.includes('HÅRFARGER'))).toBe(true)
    expect(texts.some(t => t.includes('FRISYRER'))).toBe(true)
    expect(texts.some(t => t.includes('ANTREKK'))).toBe(true)
  })

  it('renders hair color swatches for Hårfarger section', async () => {
    const wrapper = await mountShopView()
    await flushPromises()

    expect(wrapper.findAll('.shop-swatch')).toHaveLength(2)
  })

  it('renders item cards with shop-preview for non-hairColor items', async () => {
    const wrapper = await mountShopView()
    await flushPromises()

    // 2 hairStyle + 1 outfit = 3 cards
    expect(wrapper.findAll('.shop-card')).toHaveLength(3)
    expect(wrapper.findAll('.shop-preview')).toHaveLength(3)
  })

  it('shows owned footer for purchased item cards', async () => {
    const wrapper = await mountShopView()
    await flushPromises()

    // only 'long' hairStyle is purchased
    expect(wrapper.findAll('.shop-owned-footer')).toHaveLength(1)
  })

  it('shows ANSKAFFET stamp in preview for purchased item cards', async () => {
    const wrapper = await mountShopView()
    await flushPromises()

    const stamps = wrapper.findAll('.shop-preview__stamp')
    expect(stamps).toHaveLength(1)
    expect(stamps[0].text()).toContain('ANSKAFFET')
  })

  it('shows ticket button with star price for unpurchased items', async () => {
    const wrapper = await mountShopView()
    await flushPromises()

    // 2 unpurchased card items: short + detective-coat
    const tickets = wrapper.findAll('.shop-ticket')
    expect(tickets).toHaveLength(2)
    expect(tickets[0].find('.shop-ticket__amount').text()).toBe('50')
  })

  it('clicking ticket shows confirm buttons', async () => {
    const wrapper = await mountShopView()
    await flushPromises()

    await wrapper.find('.shop-ticket').trigger('click')

    expect(wrapper.find('.shop-confirm__yes').exists()).toBe(true)
    expect(wrapper.find('.shop-confirm__no').exists()).toBe(true)
    expect(wrapper.findAll('.shop-ticket')).toHaveLength(1)
  })

  it('clicking Avbryt hides confirm buttons and restores ticket', async () => {
    const wrapper = await mountShopView()
    await flushPromises()

    await wrapper.find('.shop-ticket').trigger('click')
    await wrapper.find('.shop-confirm__no').trigger('click')

    expect(wrapper.find('.shop-confirm__yes').exists()).toBe(false)
    expect(wrapper.find('.shop-ticket').exists()).toBe(true)
  })

  it('clicking Ja calls purchaseItem and hides confirm', async () => {
    const avatarStore = makeAvatarStore()
    const gameStore   = makeGameStore({ starBalance: 100 })
    const wrapper = await mountShopView(avatarStore, gameStore)
    await flushPromises()

    await wrapper.find('.shop-ticket').trigger('click')
    await wrapper.find('.shop-confirm__yes').trigger('click')
    await flushPromises()

    expect(avatarStore.purchaseItem).toHaveBeenCalledWith('hairStyle', 'short')
    expect(wrapper.find('.shop-confirm__yes').exists()).toBe(false)
  })

  it('locked ticket has shop-ticket--locked class when balance is insufficient', async () => {
    const gameStore = makeGameStore({ starBalance: 5 })
    const wrapper = await mountShopView(makeAvatarStore(), gameStore)
    await flushPromises()

    wrapper.findAll('.shop-ticket').forEach(t =>
      expect(t.classes()).toContain('shop-ticket--locked')
    )
  })
})
