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
  ],
})

describe('TaskView', () => {
  beforeEach(async () => {
    setActivePinia(createPinia())
    localStorage.clear()
    localStorage.setItem('tutorial_seen_stop_6', '1')
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
        id: 401,
        taskType: 'PASSWORD',
        stopId: 6,
        stopName: 'Passordbanken',
        stopTheme: 'PASSWORD',
        stopOrderIndex: 6,
        stopDescription: 'Tyven er nesten tatt.',
        contentJson: { type: 'CHOICE', question: 'Velg det sterkeste passordet.', options: [] },
      },
    ])
    vi.spyOn(gameStore, 'fetchStops').mockResolvedValue([
      { id: 7, orderIndex: 7, theme: 'FINAL_BOSS' },
    ])
    vi.spyOn(gameStore, 'submitAnswer').mockResolvedValue({
      correct: true,
      explanation: 'Bra jobbet.',
      stopCompleted: true,
      medalEarned: null,
    })
  })

  it('shows the arrest cutscene after Passordbanken and redirects to Datasenteret', async () => {
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
          PasswordTask: {
            template: `
              <div>
                <button class="submit-answer" @click="$emit('submitted', { selected: 'safe' })">submit</button>
                <button class="next-task" @click="$emit('next')">next</button>
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
          SuspectLineup: true,
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
    await wrapper.get('.submit-answer').trigger('click')
    await flushPromises()
    await wrapper.get('.next-task').trigger('click')
    await flushPromises()

    expect(wrapper.text()).toContain('Tyven er arrestert!')

    await wrapper.get('.arrest-scene__continue').trigger('click')
    await flushPromises()
    expect(wrapper.text()).toContain('reserveplan')
    await wrapper.get('.arrest-scene__continue').trigger('click')
    await flushPromises()

    expect(router.currentRoute.value.name).toBe('Task')
    expect(router.currentRoute.value.query.stopId).toBe('7')
  })
})
