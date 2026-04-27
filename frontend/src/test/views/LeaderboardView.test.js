import { describe, it, expect, beforeEach, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import LeaderboardView from '@/views/student/LeaderboardView.vue'

const mockGameStore = vi.hoisted(() => ({
  schoolLeaderboard: [],
  globalLeaderboard: [],
  fetchSchoolLeaderboard: vi.fn().mockResolvedValue(undefined),
  fetchGlobalLeaderboard: vi.fn().mockResolvedValue(undefined),
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

const mockAuthStore = vi.hoisted(() => ({
  userId: 2002,
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

vi.mock('@/stores/auth', () => ({
  useAuthStore: () => mockAuthStore,
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
    mockAuthStore.userId = 2002
    mockGameStore.fetchSchoolLeaderboard.mockClear()
    mockGameStore.fetchGlobalLeaderboard.mockClear()
    mockGameStore.schoolLeaderboard = [
      {
        studentId: 2001,
        classroomId: 101,
        classroomName: '3A',
        schoolName: 'Nordbyen skole',
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
        studentId: 2002,
        classroomId: 101,
        classroomName: '3A',
        schoolName: 'Nordbyen skole',
        displayName: 'Ole Pettersen',
        completedTasks: 8,
        totalTasks: 12,
        avatar: null,
      },
      {
        studentId: 3001,
        classroomId: 202,
        classroomName: '3B',
        schoolName: 'Nordbyen skole',
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
    mockGameStore.globalLeaderboard = [
      ...mockGameStore.schoolLeaderboard,
      {
        studentId: 4001,
        classroomId: 303,
        classroomName: '4C',
        schoolName: 'Sorlia skole',
        displayName: 'Mina Verdensrom',
        completedTasks: 9,
        totalTasks: 12,
        avatar: null,
      },
    ]
  })

  it('renders local and global tabs while keeping top-five ranking behavior', async () => {
    mockGameStore.schoolLeaderboard = [
      {
        studentId: 1999,
        classroomId: 101,
        classroomName: '3A',
        schoolName: 'Nordbyen skole',
        displayName: 'Grace Hopper',
        completedTasks: 12,
        totalTasks: 12,
        avatar: null,
      },
      mockGameStore.schoolLeaderboard[0],
      {
        studentId: 2003,
        classroomId: 101,
        classroomName: '3A',
        schoolName: 'Nordbyen skole',
        displayName: 'Linus Torvalds',
        completedTasks: 7,
        totalTasks: 12,
        avatar: null,
      },
      {
        studentId: 2004,
        classroomId: 101,
        classroomName: '3A',
        schoolName: 'Nordbyen skole',
        displayName: 'Margaret Hamilton',
        completedTasks: 6,
        totalTasks: 12,
        avatar: null,
      },
      {
        studentId: 2005,
        classroomId: 101,
        classroomName: '3A',
        schoolName: 'Nordbyen skole',
        displayName: 'Alan Turing',
        completedTasks: 5,
        totalTasks: 12,
        avatar: null,
      },
      {
        studentId: 2006,
        classroomId: 101,
        classroomName: '3A',
        schoolName: 'Nordbyen skole',
        displayName: 'Katherine Johnson',
        completedTasks: 4,
        totalTasks: 12,
        avatar: null,
      },
      {
        ...mockGameStore.schoolLeaderboard[1],
        completedTasks: 3,
      },
      {
        studentId: 3001,
        classroomId: 202,
        classroomName: '3B',
        schoolName: 'Nordbyen skole',
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
    mockGameStore.globalLeaderboard = [
      ...mockGameStore.schoolLeaderboard,
      {
        studentId: 4001,
        classroomId: 303,
        classroomName: '4C',
        schoolName: 'Sorlia skole',
        displayName: 'Mina Verdensrom',
        completedTasks: 9,
        totalTasks: 12,
        avatar: null,
      },
    ]

    const wrapper = mount(LeaderboardView)
    await flushPromises()

    expect(mockGameStore.fetchSchoolLeaderboard).toHaveBeenCalledWith(101)
    expect(mockGameStore.fetchGlobalLeaderboard).toHaveBeenCalledWith(101)
    expect(wrapper.findAll('table')).toHaveLength(2)
    expect(wrapper.find('ol').exists()).toBe(false)
    expect(wrapper.text()).toContain('Sammenlign deg selv med klassen')
    expect(wrapper.text()).toContain('SAMMENLIGN DEG MED ANDRE PÅ')
    expect(wrapper.text()).toContain('Nordbyen skole')
    expect(wrapper.text()).toContain('RESTEN AV VERDEN')
    expect(wrapper.text()).toContain('Kun topp fem elever')
    expect(wrapper.text()).toContain('#7')
    expect(wrapper.text()).toContain('Fremdrift')
    expect(wrapper.text()).toContain('Kari Nordmann')
    expect(wrapper.text()).toContain('Nordbyen skole')
    expect(wrapper.text()).not.toContain('Mina Verdensrom')

    const avatarTexts = wrapper.findAll('.avatar-preview-stub').map((avatar) => avatar.text())
    expect(avatarTexts).toContain('bun')
    expect(wrapper.findAll('.leaderboard-avatar-shell')).toHaveLength(6)

    const ownRow = wrapper.findAll('tbody tr').find((row) => row.text().includes('Ole Pettersen'))

    expect(ownRow).toBeUndefined()
    expect(wrapper.findAll('.leaderboard-student__badge')).toHaveLength(0)

    await wrapper.get('button[aria-selected="false"]').trigger('click')

    expect(wrapper.text()).toContain('Mina Verdensrom')
    expect(wrapper.text()).toContain('Sorlia skole')
    expect(wrapper.findAll('table')).toHaveLength(3)
  })

  it('does not highlight another student with the same display name', async () => {
    mockGameStore.schoolLeaderboard = [
      {
        studentId: 9999,
        classroomId: 101,
        classroomName: '3A',
        schoolName: 'Nordbyen skole',
        displayName: 'Ole Pettersen',
        completedTasks: 10,
        totalTasks: 12,
        avatar: null,
      },
      {
        studentId: 2002,
        classroomId: 101,
        classroomName: '3A',
        schoolName: 'Nordbyen skole',
        displayName: 'Ole Pettersen',
        completedTasks: 8,
        totalTasks: 12,
        avatar: null,
      },
    ]
    mockGameStore.globalLeaderboard = [...mockGameStore.schoolLeaderboard]

    const wrapper = mount(LeaderboardView)
    await flushPromises()

    const rows = wrapper.findAll('tbody tr').filter((row) => row.text().includes('Ole Pettersen'))
    const highlightedRows = rows.filter((row) =>
      row.classes().includes('leaderboard-table__row--me'),
    )
    const meBadges = wrapper.findAll('.leaderboard-student__badge')

    expect(rows).toHaveLength(2)
    expect(highlightedRows).toHaveLength(1)
    expect(meBadges).toHaveLength(1)
  })
})
