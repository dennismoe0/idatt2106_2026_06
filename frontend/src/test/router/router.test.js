import { describe, it, expect } from 'vitest'
import router from '@/router'

describe('router meta', () => {
  it('hides nav on the world map route', () => {
    const worldMapRoute = router.getRoutes().find((route) => route.name === 'WorldMap')

    expect(worldMapRoute).toBeTruthy()
    expect(worldMapRoute.meta.hideNav).toBe(true)
  })

  it('registers teacher notifications as a teacher route', () => {
    const route = router.getRoutes().find((route) => route.name === 'TeacherNotifications')

    expect(route).toBeTruthy()
    expect(route.path).toBe('/teacher/notifications')
    expect(route.meta.role).toBe('TEACHER')
  })

  it('uses the student login as the default public login route', () => {
    expect(router.resolve('/login').name).toBe('StudentLogin')
    expect(router.resolve('/student-login').name).toBe('StudentLogin')
  })

  it('keeps a dedicated teacher login route', () => {
    expect(router.resolve('/teacher-login').name).toBe('TeacherLogin')
  })
})
