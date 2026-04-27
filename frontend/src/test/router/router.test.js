import { describe, it, expect } from 'vitest'
import router from '@/router'

describe('router meta', () => {
  it('hides nav on the world map route', () => {
    const worldMapRoute = router.getRoutes().find((route) => route.name === 'WorldMap')

    expect(worldMapRoute).toBeTruthy()
    expect(worldMapRoute.meta.hideNav).toBe(true)
  })
})
