import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createApp } from 'vue'

function withSetup(composable) {
  let result
  const app = createApp({
    setup() { result = composable(); return () => {} },
  })
  const div = document.createElement('div')
  app.mount(div)
  return [result, () => app.unmount()]
}

describe('useAvatarWalk', () => {
  beforeEach(() => {
    vi.useRealTimers()
  })

  it('starts at node 0 and not walking', async () => {
    const { useAvatarWalk } = await import('@/composables/useAvatarWalk')
    const [{ currentNodeIndex, isWalking }, unmount] = withSetup(useAvatarWalk)
    expect(currentNodeIndex.value).toBe(0)
    expect(isWalking.value).toBe(false)
    unmount()
  })

  it('determineTarget returns first unlocked+incomplete index', async () => {
    const { useAvatarWalk } = await import('@/composables/useAvatarWalk')
    const [{ determineTarget }, unmount] = withSetup(useAvatarWalk)
    const stops = [
      { locked: false, completed: true },
      { locked: false, completed: true },
      { locked: false, completed: false },
      { locked: true,  completed: false },
    ]
    expect(determineTarget(stops)).toBe(2)
    unmount()
  })

  it('determineTarget returns last index when all stops completed', async () => {
    const { useAvatarWalk } = await import('@/composables/useAvatarWalk')
    const [{ determineTarget }, unmount] = withSetup(useAvatarWalk)
    const stops = [
      { locked: false, completed: true },
      { locked: false, completed: true },
      { locked: false, completed: true },
    ]
    expect(determineTarget(stops)).toBe(2)
    unmount()
  })

  it('determineTarget returns 0 when no stops are unlocked', async () => {
    const { useAvatarWalk } = await import('@/composables/useAvatarWalk')
    const [{ determineTarget }, unmount] = withSetup(useAvatarWalk)
    const stops = [
      { locked: true, completed: false },
      { locked: true, completed: false },
    ]
    expect(determineTarget(stops)).toBe(0)
    unmount()
  })

  it('walkTo advances currentNodeIndex one step per 700ms', async () => {
    vi.useFakeTimers()
    const { useAvatarWalk } = await import('@/composables/useAvatarWalk')
    const [{ currentNodeIndex, isWalking, walkTo }, unmount] = withSetup(useAvatarWalk)

    const walkPromise = walkTo(3)

    await vi.advanceTimersByTimeAsync(700)
    expect(currentNodeIndex.value).toBe(1)

    await vi.advanceTimersByTimeAsync(700)
    expect(currentNodeIndex.value).toBe(2)

    await vi.advanceTimersByTimeAsync(700)
    expect(currentNodeIndex.value).toBe(3)

    await walkPromise
    expect(isWalking.value).toBe(false)

    vi.useRealTimers()
    unmount()
  })

  it('walkTo is ignored while already walking', async () => {
    vi.useFakeTimers()
    const { useAvatarWalk } = await import('@/composables/useAvatarWalk')
    const [{ currentNodeIndex, walkTo }, unmount] = withSetup(useAvatarWalk)

    walkTo(3) // starts walking
    walkTo(1) // should be ignored

    await vi.advanceTimersByTimeAsync(700 * 3)
    expect(currentNodeIndex.value).toBe(3)

    vi.useRealTimers()
    unmount()
  })

  it('walkTo backwards decrements index', async () => {
    vi.useFakeTimers()
    const { useAvatarWalk } = await import('@/composables/useAvatarWalk')
    const [{ currentNodeIndex, walkTo }, unmount] = withSetup(useAvatarWalk)

    currentNodeIndex.value = 4

    const backPromise = walkTo(2)
    await vi.advanceTimersByTimeAsync(700)
    expect(currentNodeIndex.value).toBe(3)
    await vi.advanceTimersByTimeAsync(700)
    expect(currentNodeIndex.value).toBe(2)
    await backPromise

    vi.useRealTimers()
    unmount()
  })
})
