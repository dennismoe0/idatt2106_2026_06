import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createMemoryHistory } from 'vue-router'
import { useGameStore } from '@/stores/game'
import { useClassroomStore } from '@/stores/classroom'
import { useAvatarStore } from '@/stores/avatar'

vi.mock('@/composables/useSound', () => ({
  useSound: () => ({
    playCorrect: vi.fn(),
    playWrong: vi.fn(),
    playFanfare: vi.fn(),
  }),
}))

vi.mock('@/stores/audio', () => ({
  useAudioStore: () => ({
    startStage: vi.fn(),
    stopStage: vi.fn(),
  }),
}))

vi.mock('@/components/common/ConfettiOverlay.vue', () => ({
  default: { template: '<div class="confetti-overlay-stub" />' },
}))

import TaskView from '@/views/student/TaskView.vue'

const router = createRouter({
  history: createMemoryHistory(),
  routes: [
    { path: '/task', name: 'Task', component: TaskView },
    { path: '/world-map', name: 'WorldMap', component: { template: '<div>WorldMap</div>' } },
    { path: '/join', name: 'JoinClassroom', component: { template: '<div>Join</div>' } },
    { path: '/suspects', name: 'SuspectDossier', component: { template: '<div>Suspects</div>' } },
  ],
})

describe('TaskView', () => {
  beforeEach(async () => {
    setActivePinia(createPinia())
    localStorage.clear()
    localStorage.setItem('tutorial_seen_classroom_11_stop_6', '1')
    localStorage.setItem('mapView', 'world')
    await router.push('/task?stopId=6&classroomId=11')
    await router.isReady()

    const gameStore = useGameStore()
    const classroomStore = useClassroomStore()
    const avatarStore = useAvatarStore()

    classroomStore.currentClassroomId = 11
    avatarStore.avatar = {}
    vi.spyOn(gameStore, 'fetchTasks').mockResolvedValue([
      {
        id: 400,
        taskType: 'LEARN',
        stopId: 6,
        stopName: 'Passordbanken',
        stopTheme: 'PASSWORD',
        stopOrderIndex: 6,
        stopDescription: 'Tyven er nesten tatt.',
        contentJson: { slides: [], quiz: [] },
      },
      {
        id: 401,
        taskType: 'PASSWORD',
        stopId: 6,
        stopName: 'Passordbanken',
        stopTheme: 'PASSWORD',
        stopOrderIndex: 6,
        stopDescription: 'Tyven er nesten tatt.',
        contentJson: { type: 'CHOICE', question: 'Velg det sterkeste passordet.', options: [] },
      },
      {
        id: 402,
        taskType: 'PASSWORD',
        stopId: 6,
        stopName: 'Passordbanken',
        stopTheme: 'PASSWORD',
        stopOrderIndex: 6,
        stopDescription: 'Tyven er nesten tatt.',
        contentJson: { type: 'CHOICE', question: 'Gjør passordet bedre.', options: [] },
      },
      {
        id: 403,
        taskType: 'PASSWORD',
        stopId: 6,
        stopName: 'Passordbanken',
        stopTheme: 'PASSWORD',
        stopOrderIndex: 6,
        stopDescription: 'Tyven er nesten tatt.',
        contentJson: { type: 'BUILDER', question: 'Bygg et sterkt passord.' },
      },
      {
        id: 404,
        taskType: 'CLUE_RIDDLE',
        stopId: 6,
        stopName: 'Passordbanken',
        stopTheme: 'PASSWORD',
        stopOrderIndex: 6,
        stopDescription: 'Tyven er nesten tatt.',
        contentJson: { evidence: 'Reservekontoen brukte passordet XooInnAdmin2019.' },
      },
    ])
    vi.spyOn(gameStore, 'fetchStops').mockResolvedValue([
      { id: 7, orderIndex: 7, theme: 'FINAL_BOSS' },
    ])
    vi.spyOn(gameStore, 'submitAnswer').mockImplementation((taskId) => Promise.resolve(
      taskId === 404
        ? { correct: true, explanation: 'Bra jobbet.', stopCompleted: true, clueText: 'Spor: Xoo Inn Cafe.', showSuspectReveal: true, medalEarned: null }
        : { correct: true, explanation: 'Bra jobbet.', stopCompleted: false, showSuspectReveal: false, medalEarned: null }
    ))
  })

  it('shows the arrest cutscene only after the final Passordbanken clue task and returns to the map', async () => {
    const wrapper = mount(TaskView, {
      global: {
        plugins: [router],
        stubs: {
          DetectiveBar: true,
          StopMysteryScreen: {
            template: '<button class="mystery-accept" @click="$emit(\'accept\')">accept</button>',
          },
          TutorialScreen: {
            template: '<button class="tutorial-start" @click="$emit(\'start\')">start</button>',
          },
          LearningTask: {
            template: `
              <div>
                <button class="submit-learn" @click="$emit('submitted', { quizPassed: true })">submit learn</button>
                <button class="next-learn" @click="$emit('next')">next learn</button>
              </div>
            `,
          },
          PasswordTask: {
            template: `
              <div>
                <button class="submit-answer" @click="$emit('submitted', { selected: 'safe' })">submit</button>
                <button class="next-task" @click="$emit('next')">next</button>
              </div>
            `,
          },
          ClueRiddleTask: {
            template: `
              <div>
                <button class="submit-clue" @click="$emit('submitted', { selected: 'cafe_admin' })">submit clue</button>
                <button class="next-clue" @click="$emit('next')">next clue</button>
              </div>
            `,
          },
          FakeNewsTask: true,
          AIPhotoTask: true,
          SocialMediaTask: true,
          MarketplaceTask: true,
          PhishingEmailTask: true,
          FinalBossTask: true,
          ConfettiOverlay: true,
          MedalToast: true,
          StopSummary: true,
          AvatarPreview: true,
        },
      },
    })

    await flushPromises()
    await wrapper.get('.mystery-accept').trigger('click')
    await flushPromises()
    await wrapper.get('.submit-learn').trigger('click')
    await flushPromises()
    await wrapper.get('.next-learn').trigger('click')
    await flushPromises()
    await wrapper.get('.submit-answer').trigger('click')
    await flushPromises()
    expect(wrapper.findComponent({ name: 'ArrestScene' }).exists()).toBe(false)
    await wrapper.get('.next-task').trigger('click')
    await flushPromises()
    expect(wrapper.find('.arrest-scene').exists()).toBe(false)

    await wrapper.get('.submit-answer').trigger('click')
    await flushPromises()
    await wrapper.get('.next-task').trigger('click')
    await flushPromises()
    expect(wrapper.find('.arrest-scene').exists()).toBe(false)

    await wrapper.get('.submit-answer').trigger('click')
    await flushPromises()
    await wrapper.get('.next-task').trigger('click')
    await flushPromises()
    expect(wrapper.find('.arrest-scene').exists()).toBe(false)

    await wrapper.get('.submit-clue').trigger('click')
    await flushPromises()
    expect(wrapper.find('.arrest-scene').exists()).toBe(false)
    await wrapper.get('.next-clue').trigger('click')
    await flushPromises()

    expect(wrapper.text()).toContain('Tyven er arrestert!')

    await wrapper.get('.arrest-scene__continue').trigger('click')
    await flushPromises()
    expect(wrapper.text()).toContain('reserveplan')
    await wrapper.get('.arrest-scene__continue').trigger('click')
    await flushPromises()

    expect(router.currentRoute.value.name).toBe('WorldMap')
  })

  it('starts at the first incomplete Passordbanken task when the tutorial is already completed', async () => {
    localStorage.setItem('mystery_seen_classroom_11_stop_6', '1')
    const gameStore = useGameStore()
    gameStore.fetchTasks.mockResolvedValue([
      {
        id: 400,
        taskType: 'LEARN',
        completed: true,
        stopId: 6,
        stopName: 'Passordbanken',
        stopTheme: 'PASSWORD',
        stopOrderIndex: 6,
        stopDescription: 'Tyven er nesten tatt.',
        contentJson: { slides: [], quiz: [] },
      },
      {
        id: 401,
        taskType: 'PASSWORD',
        completed: false,
        stopId: 6,
        stopName: 'Passordbanken',
        stopTheme: 'PASSWORD',
        stopOrderIndex: 6,
        stopDescription: 'Tyven er nesten tatt.',
        contentJson: { type: 'CHOICE', question: 'Velg det sterkeste passordet.', options: [] },
      },
    ])

    const wrapper = mount(TaskView, {
      global: {
        plugins: [router],
        stubs: {
          DetectiveBar: true,
          StopMysteryScreen: true,
          TutorialScreen: true,
          LearningTask: {
            template: '<div class="learn-stub">learn</div>',
          },
          PasswordTask: {
            template: '<div class="password-stub">password</div>',
          },
          ClueRiddleTask: true,
          FakeNewsTask: true,
          AIPhotoTask: true,
          SocialMediaTask: true,
          MarketplaceTask: true,
          PhishingEmailTask: true,
          FinalBossTask: true,
          ConfettiOverlay: true,
          MedalToast: true,
          StopSummary: true,
          AvatarPreview: true,
        },
      },
    })

    await flushPromises()

    expect(wrapper.find('.learn-stub').exists()).toBe(false)
    expect(wrapper.find('.password-stub').exists()).toBe(true)
  })

  it('starts at the first incomplete Passordbanken task when API returns alreadyCompleted', async () => {
    localStorage.setItem('mystery_seen_classroom_11_stop_6', '1')
    const gameStore = useGameStore()
    gameStore.fetchTasks.mockResolvedValue([
      {
        id: 400,
        taskType: 'LEARN',
        alreadyCompleted: true,
        stopId: 6,
        stopName: 'Passordbanken',
        stopTheme: 'PASSWORD',
        stopOrderIndex: 6,
        stopDescription: 'Tyven er nesten tatt.',
        contentJson: { slides: [], quiz: [] },
      },
      {
        id: 401,
        taskType: 'PASSWORD',
        alreadyCompleted: false,
        stopId: 6,
        stopName: 'Passordbanken',
        stopTheme: 'PASSWORD',
        stopOrderIndex: 6,
        stopDescription: 'Tyven er nesten tatt.',
        contentJson: { type: 'CHOICE', question: 'Velg det sterkeste passordet.', options: [] },
      },
    ])

    const wrapper = mount(TaskView, {
      global: {
        plugins: [router],
        stubs: {
          DetectiveBar: true,
          StopMysteryScreen: true,
          TutorialScreen: true,
          LearningTask: {
            template: '<div class="learn-stub">learn</div>',
          },
          PasswordTask: {
            template: '<div class="password-stub">password</div>',
          },
          ClueRiddleTask: true,
          FakeNewsTask: true,
          AIPhotoTask: true,
          SocialMediaTask: true,
          MarketplaceTask: true,
          PhishingEmailTask: true,
          FinalBossTask: true,
          ConfettiOverlay: true,
          MedalToast: true,
          StopSummary: true,
          AvatarPreview: true,
        },
      },
    })

    await flushPromises()

    expect(wrapper.find('.learn-stub').exists()).toBe(false)
    expect(wrapper.find('.password-stub').exists()).toBe(true)
  })

  it('does not show stored clue modal after learning task completion', async () => {
    const gameStore = useGameStore()
    gameStore.fetchTasks.mockResolvedValue([
      {
        id: 400,
        taskType: 'LEARN',
        stopId: 6,
        stopName: 'Passordbanken',
        stopTheme: 'PASSWORD',
        stopOrderIndex: 6,
        contentJson: { slides: [], quiz: [] },
      },
      {
        id: 402,
        taskType: 'CLUE_RIDDLE',
        stopId: 6,
        stopName: 'Passordbanken',
        stopTheme: 'PASSWORD',
        stopOrderIndex: 6,
        contentJson: { evidence: 'Reservekontoen brukte passordet XooInnAdmin2019.' },
      },
    ])
    gameStore.submitAnswer.mockImplementation((taskId) => Promise.resolve(taskId === 400
      ? { correct: true, explanation: 'Intro ferdig.', stopCompleted: false, showSuspectReveal: false, medalEarned: null }
      : { correct: true, explanation: 'Spor lagret.', stopCompleted: true, clueText: 'Spor: Xoo Inn Cafe.', showSuspectReveal: true, medalEarned: null }))

    const wrapper = mount(TaskView, {
      global: {
        plugins: [router],
        stubs: {
          DetectiveBar: true,
          StopMysteryScreen: true,
          TutorialScreen: true,
          LearningTask: {
            template: `
              <div>
                <button class="submit-learn" @click="$emit('submitted', { quizPassed: true })">submit learn</button>
                <button class="next-learn" @click="$emit('next')">next learn</button>
              </div>
            `,
          },
          ClueRiddleTask: {
            template: '<button class="submit-clue" @click="$emit(\'submitted\', { selected: \'cafe_admin\' })">submit clue</button>',
          },
          PasswordTask: true,
          FakeNewsTask: true,
          AIPhotoTask: true,
          SocialMediaTask: true,
          MarketplaceTask: true,
          PhishingEmailTask: true,
          FinalBossTask: true,
          ConfettiOverlay: true,
          MedalToast: true,
          StopSummary: true,
          AvatarPreview: true,
        },
      },
    })

    await flushPromises()
    await wrapper.get('.submit-learn').trigger('click')
    await flushPromises()
    expect(wrapper.find('.stored-clue-modal').exists()).toBe(false)

    await wrapper.get('.next-learn').trigger('click')
    await flushPromises()
    await wrapper.get('.submit-clue').trigger('click')
    await flushPromises()
    expect(wrapper.find('.stored-clue-modal').exists()).toBe(true)
    expect(wrapper.find('.stored-clue-modal__secondary').exists()).toBe(false)
  })

  it('rejects wrong clue-riddle answers in mock mode', async () => {
    const gameStore = useGameStore()
    gameStore.fetchTasks.mockRejectedValueOnce(new Error('offline'))
    gameStore.submitAnswer.mockRejectedValue(new Error('offline'))

    const wrapper = mount(TaskView, {
      global: {
        plugins: [router],
        stubs: {
          DetectiveBar: true,
          StopMysteryScreen: true,
          TutorialScreen: true,
          PasswordTask: {
            template: `
              <div>
                <button class="submit-password" @click="$emit('submitted', { selected: 'd' })">submit password</button>
                <button class="next-password" @click="$emit('next')">next password</button>
              </div>
            `,
          },
          LearningTask: true,
          FakeNewsTask: true,
          AIPhotoTask: true,
          SocialMediaTask: true,
          MarketplaceTask: true,
          PhishingEmailTask: true,
          FinalBossTask: true,
          ConfettiOverlay: true,
          MedalToast: true,
          StopSummary: true,
          AvatarPreview: true,
        },
      },
    })

    await flushPromises()
    expect(wrapper.text()).toContain('Mock mode aktiv')

    await wrapper.get('.submit-password').trigger('click')
    await flushPromises()
    await wrapper.get('.next-password').trigger('click')
    await flushPromises()
    await wrapper.findAll('.evidence-card')[0].trigger('click')
    await wrapper.get('.submit-btn').trigger('click')
    await flushPromises()

    expect(wrapper.text()).toContain('Ikke helt')
    expect(wrapper.text()).toContain('det inneholder sted, rolle og årstall')
    expect(wrapper.find('.stored-clue-modal').exists()).toBe(false)
  })

  it('clears password result when retry is emitted after wrong answer', async () => {
    localStorage.setItem('mystery_seen_classroom_11_stop_6', '1')
    const gameStore = useGameStore()
    gameStore.fetchTasks.mockResolvedValue([
      {
        id: 401,
        taskType: 'PASSWORD',
        alreadyCompleted: false,
        stopId: 6,
        stopName: 'Passordbanken',
        stopTheme: 'PASSWORD',
        stopOrderIndex: 6,
        stopDescription: 'Tyven er nesten tatt.',
        contentJson: { type: 'CHOICE', question: 'Velg det sterkeste passordet.', options: [] },
      },
    ])
    gameStore.submitAnswer.mockResolvedValue({
      correct: false,
      explanation: 'Feil svar.',
      stopCompleted: false,
      showSuspectReveal: false,
      medalEarned: null,
    })

    const wrapper = mount(TaskView, {
      global: {
        plugins: [router],
        stubs: {
          DetectiveBar: true,
          StopMysteryScreen: true,
          TutorialScreen: true,
          PasswordTask: {
            props: ['result'],
            template: `
              <div>
                <button v-if="!result" class="submit-answer" @click="$emit('submitted', { selected: 'wrong' })">submit</button>
                <button v-else class="retry-answer" @click="$emit('retry')">retry</button>
              </div>
            `,
          },
          LearningTask: true,
          ClueRiddleTask: true,
          FakeNewsTask: true,
          AIPhotoTask: true,
          SocialMediaTask: true,
          MarketplaceTask: true,
          PhishingEmailTask: true,
          FinalBossTask: true,
          ConfettiOverlay: true,
          MedalToast: true,
          StopSummary: true,
          AvatarPreview: true,
        },
      },
    })

    await flushPromises()
    await wrapper.get('.submit-answer').trigger('click')
    await flushPromises()
    expect(wrapper.find('.retry-answer').exists()).toBe(true)

    await wrapper.get('.retry-answer').trigger('click')
    await flushPromises()
    expect(wrapper.find('.submit-answer').exists()).toBe(true)
  })

  it('keeps the task visible when backend rejects an out-of-sequence submit', async () => {
    localStorage.setItem('mystery_seen_classroom_11_stop_6', '1')
    const gameStore = useGameStore()
    gameStore.fetchTasks.mockResolvedValue([
      {
        id: 400,
        taskType: 'LEARN',
        alreadyCompleted: true,
        stopId: 6,
        stopName: 'Passordbanken',
        stopTheme: 'PASSWORD',
        stopOrderIndex: 6,
        contentJson: { slides: [], quiz: [] },
      },
      {
        id: 401,
        taskType: 'PASSWORD',
        alreadyCompleted: false,
        stopId: 6,
        stopName: 'Passordbanken',
        stopTheme: 'PASSWORD',
        stopOrderIndex: 6,

        contentJson: { type: 'CHOICE', question: 'Velg det sterkeste passordet.', options: [] },
      },
    ])
    gameStore.submitAnswer.mockRejectedValue({
      response: {
        status: 403,
        data: { message: 'Previous tasks must be completed first' },
      },
    })

    const wrapper = mount(TaskView, {
      global: {
        plugins: [router],
        stubs: {
          DetectiveBar: true,
          StopMysteryScreen: true,
          TutorialScreen: true,
          PasswordTask: {
            template: '<button class="submit-answer" @click="$emit(\'submitted\', { selected: \'wrong\' })">submit</button>',
          },
          LearningTask: { template: '<div class="learn-stub">learn</div>' },

          ClueRiddleTask: true,
          FakeNewsTask: true,
          AIPhotoTask: true,
          SocialMediaTask: true,
          MarketplaceTask: true,
          PhishingEmailTask: true,
          FinalBossTask: true,
          ConfettiOverlay: true,
          MedalToast: true,
          StopSummary: true,
          AvatarPreview: true,
        },
      },
    })

    await flushPromises()
    await wrapper.get('.submit-answer').trigger('click')
    await flushPromises()

    expect(wrapper.text()).not.toContain('Kunne ikke sende svar')
    expect(wrapper.find('.submit-answer').exists()).toBe(true)

  })
})
