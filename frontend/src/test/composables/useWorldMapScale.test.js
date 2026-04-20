import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createApp, nextTick } from 'vue'

// ---- withSetup helper ----
function withSetup(composable) {
  let result
  const app = createApp({
    setup() { result = composable(); return () => {} },
  })
  const div = document.createElement('div')
  app.mount(div)
  return [result, () => app.unmount()]
}

// ---- ResizeObserver mock ----
let observeCallback
const observeSpy = vi.fn()
const disconnectSpy = vi.fn()
vi.stubGlobal('ResizeObserver', vi.fn(function (cb) {
  observeCallback = cb
  return { observe: observeSpy, disconnect: disconnectSpy }
}))

describe('useWorldMapScale', () => {
  beforeEach(() => {
    observeSpy.mockClear()
    disconnectSpy.mockClear()
  })

  it('exports containerRef and scale', async () => {
    const { useWorldMapScale } = await import('@/composables/useWorldMapScale')
    const [result, unmount] = withSetup(useWorldMapScale)
    expect(result).toHaveProperty('containerRef')
    expect(result).toHaveProperty('scale')
    unmount()
  })

  it('scale starts at 1', async () => {
    const { useWorldMapScale } = await import('@/composables/useWorldMapScale')
    const [{ scale }, unmount] = withSetup(useWorldMapScale)
    expect(scale.value).toBe(1)
    unmount()
  })

  it('scale = min(w/1600, h/900) — landscape wide container', async () => {
    const { useWorldMapScale } = await import('@/composables/useWorldMapScale')
    const [{ containerRef, scale }, unmount] = withSetup(useWorldMapScale)

    const mockEl = document.createElement('div')
    mockEl.getBoundingClientRect = () => ({ width: 800, height: 450 })
    containerRef.value = mockEl

    await nextTick()
    if (observeCallback) observeCallback([])

    expect(scale.value).toBe(0.5)
    unmount()
  })

  it('scale is height-constrained when container is tall relative to 16:9', async () => {
    const { useWorldMapScale } = await import('@/composables/useWorldMapScale')
    const [{ containerRef, scale }, unmount] = withSetup(useWorldMapScale)

    const mockEl = document.createElement('div')
    mockEl.getBoundingClientRect = () => ({ width: 1600, height: 450 })
    containerRef.value = mockEl

    if (observeCallback) observeCallback([])

    expect(scale.value).toBe(0.5)
    unmount()
  })

  it('disconnects observer on unmount', async () => {
    const { useWorldMapScale } = await import('@/composables/useWorldMapScale')
    const [, unmount] = withSetup(useWorldMapScale)
    unmount()
    expect(disconnectSpy).toHaveBeenCalled()
  })
})
