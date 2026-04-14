import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useGameStore } from '@/stores/game'

vi.mock('@/services/gameService', () => ({
  gameService: {
    getStops: vi.fn(),
    getTasks: vi.fn(),
    getTask: vi.fn(),
    submitAnswer: vi.fn(),
    getProgress: vi.fn(),
  }
}))

import { gameService } from '@/services/gameService'

describe('game store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('initial state is empty', () => {
    const store = useGameStore()
    expect(store.stops).toEqual([])
    expect(store.tasks).toEqual([])
    expect(store.currentTask).toBeNull()
    expect(store.progress).toBeNull()
  })

  it('fetchStops populates stops', async () => {
    gameService.getStops.mockResolvedValue({
      data: [
        { id: 1, name: 'Nyhetskvartalet', locked: false, completed: false },
        { id: 2, name: 'Postkontoret', locked: true, completed: false },
      ]
    })
    const store = useGameStore()
    await store.fetchStops(1)
    expect(store.stops).toHaveLength(2)
    expect(store.stops[0].name).toBe('Nyhetskvartalet')
  })

  it('fetchTask sets currentTask', async () => {
    gameService.getTask.mockResolvedValue({
      data: { id: 3, taskType: 'FAKE_NEWS', content: {} }
    })
    const store = useGameStore()
    await store.fetchTask(3, 1)
    expect(store.currentTask).not.toBeNull()
    expect(store.currentTask.taskType).toBe('FAKE_NEWS')
  })

  it('submitAnswer returns result with correct and stopCompleted', async () => {
    gameService.submitAnswer.mockResolvedValue({
      data: { correct: true, score: 10, explanation: 'Riktig!', stopCompleted: false, medalEarned: null }
    })
    const store = useGameStore()
    const result = await store.submitAnswer(1, 1, { answer: 'FAKE' })
    expect(result.correct).toBe(true)
    expect(result.score).toBe(10)
  })

  it('submitAnswer with stopCompleted returns medal data', async () => {
    gameService.submitAnswer.mockResolvedValue({
      data: {
        correct: true, score: 10, explanation: 'Bra!',
        stopCompleted: true,
        medalEarned: { id: 1, name: 'Nyhetsdetektiv', description: 'Fullført Nyhetskvartalet' }
      }
    })
    const store = useGameStore()
    const result = await store.submitAnswer(1, 1, { answer: 'FAKE' })
    expect(result.stopCompleted).toBe(true)
    expect(result.medalEarned.name).toBe('Nyhetsdetektiv')
  })

  it('fetchStops propagates errors', async () => {
    gameService.getStops.mockRejectedValue(new Error('Forbidden'))
    const store = useGameStore()
    await expect(store.fetchStops(1)).rejects.toThrow('Forbidden')
  })
})
