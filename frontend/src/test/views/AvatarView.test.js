import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'

vi.mock('@/components/common/BackButton.vue', () => ({
  default: { template: '<button type="button">Back</button>' },
}))

vi.mock('@/components/common/LoadingSpinner.vue', () => ({
  default: { template: '<span data-testid="spinner">Loading</span>' },
}))

vi.mock('@/components/common/BaseButton.vue', () => ({
  default: {
    props: ['loading', 'disabled', 'variant'],
    emits: ['click'],
    template: `<button :disabled="disabled" @click="$emit('click')"><slot /></button>`,
  },
}))

vi.mock('@/components/student/avatar/AvatarComposer.vue', () => ({
  default: {
    props: ['selections', 'size'],
    template: `
      <div data-testid="avatar-composer">
        {{ selections.outfit }}|{{ selections.hairStyle }}|{{ selections.accessory }}|{{ selections.eyeStyle }}
      </div>
    `,
  },
}))

vi.mock('@/components/student/avatar/controls/SwatchGrid.vue', () => ({
  default: {
    props: ['modelValue', 'swatches', 'shape', 'locked'],
    emits: ['update:modelValue'],
    template: `<div class="swatch-grid-mock" />`,
  },
}))

vi.mock('@/components/student/avatar/controls/ShapeGrid.vue', () => ({
  default: {
    props: ['modelValue', 'variants', 'previewComponent', 'previewProps', 'variantProp', 'locked'],
    emits: ['update:modelValue'],
    template: `<div class="shape-grid-mock" />`,
  },
}))

vi.mock('@/components/student/avatar/controls/GenderToggle.vue', () => ({
  default: {
    props: ['modelValue'],
    emits: ['update:modelValue'],
    template: `<div class="gender-toggle-mock" />`,
  },
}))

vi.mock('@/components/student/avatar/layers/AvatarHair.vue', () => ({
  default: { template: '<svg />' },
}))
vi.mock('@/components/student/avatar/layers/AvatarEyes.vue', () => ({
  default: { template: '<svg />' },
}))
vi.mock('@/components/student/avatar/layers/AvatarOutfit.vue', () => ({
  default: { template: '<svg />' },
}))

const router = createRouter({
  history: createMemoryHistory(),
  routes: [{ path: '/avatar', name: 'Avatar', component: { template: '<div />' } }],
})

function makeStore(overrides = {}) {
  return {
    fetchAvatar: vi.fn().mockResolvedValue({
      gender: 'neutral',
      eyeColor: '#4a3000',
      skinColor: '#FDDBB4',
      hairColor: '#1a1a1a',
      hairStyle: 'short',
      eyeStyle: 'round',
      outfit: 'detective-coat',
      outfitColor: '#2563eb',
      accessory: 'badge',
    }),
    fetchOptions: vi.fn().mockResolvedValue({}),
    updateAvatar: vi.fn().mockResolvedValue({
      gender: 'neutral',
      eyeColor: '#4a3000',
      skinColor: '#FDDBB4',
      hairColor: '#1a1a1a',
      hairStyle: 'short',
      eyeStyle: 'round',
      outfit: 'detective-coat',
      outfitColor: '#2563eb',
      accessory: 'badge',
    }),
    ...overrides,
  }
}

async function mountAvatarView(storeFactory) {
  vi.doMock('@/stores/avatar', () => ({
    useAvatarStore: storeFactory,
  }))

  const { default: AvatarView } = await import('@/views/student/AvatarView.vue')

  return mount(AvatarView, {
    global: {
      plugins: [router],
    },
  })
}

describe('AvatarView', () => {
  beforeEach(() => {
    vi.resetModules()
    vi.clearAllMocks()
  })

  it('shows loading spinner while fetching', async () => {
    let resolve
    const store = makeStore({
      fetchAvatar: vi.fn().mockReturnValue(new Promise(r => { resolve = r })),
    })

    const wrapper = await mountAvatarView(() => store)

    expect(wrapper.find('[data-testid="spinner"]').exists()).toBe(true)
    resolve({ gender: 'neutral', eyeColor: '#4a3000', skinColor: '#FDDBB4', hairColor: '#1a1a1a', hairStyle: 'short', eyeStyle: 'round', outfit: 'detective-coat', outfitColor: '#2563eb', accessory: 'badge' })
    await flushPromises()
    expect(wrapper.find('[data-testid="spinner"]').exists()).toBe(false)
  })

  it('loads avatar on mount and renders AvatarComposer with selections', async () => {
    const store = makeStore()
    const wrapper = await mountAvatarView(() => store)
    await flushPromises()

    expect(store.fetchAvatar).toHaveBeenCalledTimes(1)
    expect(store.fetchOptions).toHaveBeenCalledTimes(1)
    expect(wrapper.find('h1').text()).toContain('Bygg din detektiv')
    const composer = wrapper.find('[data-testid="avatar-composer"]')
    expect(composer.exists()).toBe(true)
    expect(composer.text()).toContain('detective-coat|short|badge|round')
  })

  it('shows error state when loading fails', async () => {
    const store = makeStore({
      fetchAvatar: vi.fn().mockRejectedValue(new Error('network error')),
    })

    const wrapper = await mountAvatarView(() => store)
    await flushPromises()

    expect(wrapper.text()).toContain('Kunne ikke laste avatar')
    expect(wrapper.text()).toContain('Noe gikk galt ved lasting av avatardata.')
    expect(wrapper.find('[data-testid="avatar-composer"]').exists()).toBe(false)
  })

  it('pill buttons render for each accessory and toggle active class', async () => {
    const store = makeStore()
    const wrapper = await mountAvatarView(() => store)
    await flushPromises()

    const pills = wrapper.findAll('.pill')
    // ACCESSORIES = ['none','badge','glasses','magnifier','hat']
    expect(pills.length).toBe(5)

    // 'badge' is active from loaded avatar
    const badgePill = pills.find(p => p.text() === 'Merke')
    expect(badgePill.classes()).toContain('pill--active')

    // Click 'none'
    const nonePill = pills.find(p => p.text() === 'Ingen')
    await nonePill.trigger('click')
    expect(nonePill.classes()).toContain('pill--active')
    expect(badgePill.classes()).not.toContain('pill--active')
  })

  it('save button calls updateAvatar and shows success feedback', async () => {
    const store = makeStore({
      updateAvatar: vi.fn().mockResolvedValue({
        gender: 'female',
        eyeColor: '#4a3000',
        skinColor: '#FDDBB4',
        hairColor: '#1a1a1a',
        hairStyle: 'short',
        eyeStyle: 'round',
        outfit: 'detective-coat',
        outfitColor: '#2563eb',
        accessory: 'none',
      }),
    })

    const wrapper = await mountAvatarView(() => store)
    await flushPromises()

    // Make a change so hasChanges becomes true — click a different accessory
    const pills = wrapper.findAll('.pill')
    const nonePill = pills.find(p => p.text() === 'Ingen')
    await nonePill.trigger('click')

    // Find and click the save button (first non-disabled button in preview-panel)
    const saveButton = wrapper.find('.preview-panel button:not([disabled])')
    await saveButton.trigger('click')
    await flushPromises()

    expect(store.updateAvatar).toHaveBeenCalledTimes(1)
    expect(wrapper.text()).toContain('Avatar lagret!')
  })

  it('shows error feedback when save fails', async () => {
    const store = makeStore({
      updateAvatar: vi.fn().mockRejectedValue(new Error('server error')),
    })

    const wrapper = await mountAvatarView(() => store)
    await flushPromises()

    // Make a change
    const pills = wrapper.findAll('.pill')
    const nonePill = pills.find(p => p.text() === 'Ingen')
    await nonePill.trigger('click')

    const saveButton = wrapper.find('.preview-panel button:not([disabled])')
    await saveButton.trigger('click')
    await flushPromises()

    expect(wrapper.text()).toContain('Kunne ikke lagre avatar.')
  })
})
