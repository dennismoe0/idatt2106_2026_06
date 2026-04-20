import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import ConfettiOverlay from '@/components/common/ConfettiOverlay.vue'

// canvas-confetti renders to a full-screen canvas; stub it out.
// Use vi.hoisted so the mock factory can reference the variable before it is hoisted.
const { confettiMock } = vi.hoisted(() => {
  const confettiMock = vi.fn()
  confettiMock.reset = vi.fn()
  return { confettiMock }
})

vi.mock('canvas-confetti', () => ({
  default: confettiMock,
}))

describe('ConfettiOverlay', () => {
  beforeEach(() => {
    vi.useFakeTimers()
    vi.clearAllMocks()
  })
  afterEach(() => { vi.useRealTimers() })

  it('does not fire confetti when active is false', () => {
    mount(ConfettiOverlay, { props: { active: false } })
    expect(confettiMock).not.toHaveBeenCalled()
  })

  it('fires confetti immediately when active is true', async () => {
    mount(ConfettiOverlay, { props: { active: true } })
    await flushPromises()
    expect(confettiMock).toHaveBeenCalled()
  })

  it('fires a bigger burst when active is "stop"', async () => {
    mount(ConfettiOverlay, { props: { active: 'stop' } })
    await flushPromises()
    expect(confettiMock).toHaveBeenCalled()
    // "stop" mode fires more particles — first call should have particleCount >= 160
    const firstCall = confettiMock.mock.calls[0][0]
    expect(firstCall.particleCount).toBeGreaterThanOrEqual(160)
  })

  it('fires a smaller burst for "correct"', async () => {
    mount(ConfettiOverlay, { props: { active: 'correct' } })
    await flushPromises()
    expect(confettiMock).toHaveBeenCalled()
    // "correct" mode first burst <= 80 particles
    const firstCall = confettiMock.mock.calls[0][0]
    expect(firstCall.particleCount).toBeLessThanOrEqual(80)
  })
})
