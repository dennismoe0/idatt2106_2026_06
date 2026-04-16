import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'

vi.mock('@/components/common/BackButton.vue', () => ({
  default: { template: '<button type="button">Back</button>' },
}))

vi.mock('@/components/common/LoadingSpinner.vue', () => ({
  default: { template: '<span data-testid="spinner">Loading</span>' },
}))

vi.mock('@/components/student/AvatarPreview.vue', () => ({
  default: {
    props: ['selections'],
    template: `
      <div data-testid="avatar-preview">
        {{ selections.outfit }}|{{ selections.hairStyle }}|{{ selections.accessory }}
      </div>
    `,
  },
}))

const router = createRouter({
  history: createMemoryHistory(),
  routes: [{ path: '/avatar', name: 'Avatar', component: { template: '<div />' } }],
})

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

  it('loads avatar and options on mount and fills selectors', async () => {
    const store = {
      options: {
        gender: ['neutral', 'female', 'male'],
        eyeColor: ['brown', 'green'],
        skinColor: ['light', 'medium'],
        hairColor: ['brown', 'black'],
        hairStyle: ['short', 'curly'],
        outfit: ['detective-coat', 'hoodie'],
        outfitColor: ['blue', 'red'],
        hatColor: ['none', 'black'],
        accessory: ['badge', 'glasses'],
      },
      fetchAvatar: vi.fn().mockResolvedValue({
        gender: 'neutral',
        eyeColor: 'brown',
        skinColor: 'medium',
        hairColor: 'brown',
        hairStyle: 'short',
        outfit: 'detective-coat',
        outfitColor: 'blue',
        hatColor: 'none',
        accessory: 'badge',
      }),
      fetchOptions: vi.fn().mockResolvedValue({
        gender: ['neutral', 'female', 'male'],
        eyeColor: ['brown', 'green'],
        skinColor: ['light', 'medium'],
        hairColor: ['brown', 'black'],
        hairStyle: ['short', 'curly'],
        outfit: ['detective-coat', 'hoodie'],
        outfitColor: ['blue', 'red'],
        hatColor: ['none', 'black'],
        accessory: ['badge', 'glasses'],
      }),
      updateAvatar: vi.fn(),
    }

    const wrapper = await mountAvatarView(() => store)
    await flushPromises()

    expect(store.fetchAvatar).toHaveBeenCalledTimes(1)
    expect(store.fetchOptions).toHaveBeenCalledTimes(1)
    expect(wrapper.find('h1').text()).toContain('Bygg din detektiv')
    expect(wrapper.find('select').element.value).toBe('neutral')
    expect(wrapper.find('[data-testid="avatar-preview"]').text()).toContain('detective-coat|short|badge')
  })

  it('updates the live preview when selections change', async () => {
    const store = {
      options: {
        gender: ['neutral', 'female'],
        eyeColor: ['brown', 'green'],
        skinColor: ['medium'],
        hairColor: ['brown', 'black'],
        hairStyle: ['short', 'curly'],
        outfit: ['detective-coat', 'hoodie'],
        outfitColor: ['blue', 'red'],
        hatColor: ['none'],
        accessory: ['badge', 'glasses'],
      },
      fetchAvatar: vi.fn().mockResolvedValue({
        gender: 'neutral',
        eyeColor: 'brown',
        skinColor: 'medium',
        hairColor: 'brown',
        hairStyle: 'short',
        outfit: 'detective-coat',
        outfitColor: 'blue',
        hatColor: 'none',
        accessory: 'badge',
      }),
      fetchOptions: vi.fn().mockResolvedValue({
        gender: ['neutral', 'female'],
        eyeColor: ['brown', 'green'],
        skinColor: ['medium'],
        hairColor: ['brown', 'black'],
        hairStyle: ['short', 'curly'],
        outfit: ['detective-coat', 'hoodie'],
        outfitColor: ['blue', 'red'],
        hatColor: ['none'],
        accessory: ['badge', 'glasses'],
      }),
      updateAvatar: vi.fn(),
    }

    const wrapper = await mountAvatarView(() => store)
    await flushPromises()

    const selects = wrapper.findAll('select')
    await selects[4].setValue('curly')
    await selects[5].setValue('hoodie')
    await selects[8].setValue('glasses')

    expect(wrapper.find('[data-testid="avatar-preview"]').text()).toContain('hoodie|curly|glasses')
  })

  it('saves the avatar and shows success feedback', async () => {
    const store = {
      options: {
        gender: ['neutral', 'female'],
        eyeColor: ['brown', 'green'],
        skinColor: ['medium'],
        hairColor: ['brown', 'black'],
        hairStyle: ['short', 'curly'],
        outfit: ['detective-coat', 'hoodie'],
        outfitColor: ['blue', 'red'],
        hatColor: ['none'],
        accessory: ['badge', 'glasses'],
      },
      fetchAvatar: vi.fn().mockResolvedValue({
        gender: 'neutral',
        eyeColor: 'brown',
        skinColor: 'medium',
        hairColor: 'brown',
        hairStyle: 'short',
        outfit: 'detective-coat',
        outfitColor: 'blue',
        hatColor: 'none',
        accessory: 'badge',
      }),
      fetchOptions: vi.fn().mockResolvedValue({
        gender: ['neutral', 'female'],
        eyeColor: ['brown', 'green'],
        skinColor: ['medium'],
        hairColor: ['brown', 'black'],
        hairStyle: ['short', 'curly'],
        outfit: ['detective-coat', 'hoodie'],
        outfitColor: ['blue', 'red'],
        hatColor: ['none'],
        accessory: ['badge', 'glasses'],
      }),
      updateAvatar: vi.fn().mockResolvedValue({
        gender: 'female',
        eyeColor: 'brown',
        skinColor: 'medium',
        hairColor: 'brown',
        hairStyle: 'curly',
        outfit: 'hoodie',
        outfitColor: 'red',
        hatColor: 'none',
        accessory: 'glasses',
      }),
    }

    const wrapper = await mountAvatarView(() => store)
    await flushPromises()

    const selects = wrapper.findAll('select')
    await selects[0].setValue('female')
    await selects[4].setValue('curly')
    await selects[5].setValue('hoodie')
    await selects[6].setValue('red')
    await selects[8].setValue('glasses')

    await wrapper.find('form').trigger('submit.prevent')
    await flushPromises()

    expect(store.updateAvatar).toHaveBeenCalledWith({
      gender: 'female',
      eyeColor: 'brown',
      skinColor: 'medium',
      hairColor: 'brown',
      hairStyle: 'curly',
      outfit: 'hoodie',
      outfitColor: 'red',
      hatColor: 'none',
      accessory: 'glasses',
    })
    expect(wrapper.text()).toContain('Avatar lagret.')
  })

  it('shows an error state when loading fails', async () => {
    const store = {
      options: {},
      fetchAvatar: vi.fn().mockRejectedValue(new Error('boom')),
      fetchOptions: vi.fn().mockResolvedValue({}),
      updateAvatar: vi.fn(),
    }

    const wrapper = await mountAvatarView(() => store)
    await flushPromises()

    expect(wrapper.text()).toContain('Kunne ikke laste avatar')
    expect(wrapper.text()).toContain('Noe gikk galt ved lasting av avatardata.')
  })
})
