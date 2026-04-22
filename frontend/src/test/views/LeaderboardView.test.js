import { describe, it, expect, beforeEach, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import LeaderboardView from '@/views/student/LeaderboardView.vue'

const mockGameStore = vi.hoisted(() => ({
  schoolLeaderboard: [],
  fetchSchoolLeaderboard: vi.fn().mockResolvedValue(undefined),
}))

const mockClassroomStore = vi.hoisted(() => ({
  currentClassroomId: 101,
  displayName: 'Ole Pettersen',
}))

const mockAvatarStore = vi.hoisted(() => ({
  avatar: {
    gender: 'neutral',
    eyeColor: '#4a3000',
    eyeStyle: 'round',
    skinColor: '#D08B5B',
    hairColor: '#8B4513',
    hairStyle: 'short',
    outfit: 'detective-coat',
    outfitColor: '#6B4A2F',
    hatColor: 'none',
    accessory: 'badge',
  },
}))

vi.mock('@/stores/game', () => ({
  useGameStore: () => mockGameStore,
}))

vi.mock('@/stores/classroom', () => ({
  useClassroomStore: () => mockClassroomStore,
}))

vi.mock('@/stores/avatar', () => ({
  useAvatarStore: () => mockAvatarStore,
}))

vi.mock('@/components/common/CorkBoardPage.vue', () => ({
  default: {
    template: '<div class="cork-board-page-stub"><slot /></div>',
  },
}))

vi.mock('@/components/student/AvatarPreview.vue', () => ({
  default: {
    props: ['selections', 'size'],
    template: '<div class="avatar-preview-stub" :data-size="size">{{ selections.hairStyle }}</div>',
  },
}))

describe('LeaderboardView', () => {
  beforeEach(() => {
    mockClassroomStore.currentClassroomId = 101
    mockClassroomStore.displayName = 'Ole Pettersen'
    mockGameStore.fetchSchoolLeaderboard.mockClear()
    mockGameStore.schoolLeaderboard = [
      {
        classroomId: 101,
        classroomName: '3A',
        displayName: 'Ada Lovelace',
        completedTasks: 10,
        totalTasks: 12,
        avatar: {
          gender: 'neutral',
          eyeColor: '#332200',
          eyeStyle: 'round',
          skinColor: '#E4B48A',
          hairColor: '#2F1C0F',
          hairStyle: 'bun',
          outfit: 'detective-coat',
          outfitColor: '#5A3B24',
          hatColor: 'none',
          accessory: 'glasses',
        },
      },
      {
        classroomId: 101,
        classroomName: '3A',
        displayName: 'Ole Pettersen',
        completedTasks: 8,
        totalTasks: 12,
        avatar: null,
      },
      {
        classroomId: 202,
        classroomName: '3B',
        displayName: 'Kari Nordmann',
        completedTasks: 11,
        totalTasks: 12,
        avatar: {
          gender: 'neutral',
          eyeColor: '#3f2200',
          eyeStyle: 'happy',
          skinColor: '#C98A5B',
          hairColor: '#4B2E14',
          hairStyle: 'curly',
          outfit: 'hoodie',
          outfitColor: '#8A5A2F',
          hatColor: 'none',
          accessory: 'badge',
        },
      },
    ]
  })

  it('renders classroom tables with avatars and highlights the current student', async () => {
    const wrapper = mount(LeaderboardView)
    await flushPromises()

    expect(mockGameStore.fetchSchoolLeaderboard).toHaveBeenCalledWith(101)
    expect(wrapper.findAll('table')).toHaveLength(2)
    expect(wrapper.find('ol').exists()).toBe(false)
    expect(wrapper.text()).toContain('Din klasse')
    expect(wrapper.text()).toContain('Andre klasser på skolen')
    expect(wrapper.text()).toContain('Fremdrift')

    const avatarTexts = wrapper.findAll('.avatar-preview-stub').map((avatar) => avatar.text())
    expect(avatarTexts).toContain('bun')
    expect(avatarTexts).toContain('short')
    expect(wrapper.findAll('.leaderboard-avatar-shell')).toHaveLength(3)

    const ownRow = wrapper.findAll('tbody tr').find((row) => row.text().includes('Ole Pettersen'))

    expect(ownRow?.classes()).toContain('leaderboard-table__row--me')
    expect(ownRow?.text()).toContain('Deg')
  })
})
