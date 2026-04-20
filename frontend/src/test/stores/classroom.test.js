import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useClassroomStore } from '@/stores/classroom'

vi.mock('@/services/classroomService', () => ({
  classroomService: {
    getMyClassrooms: vi.fn(),
    createClassroom: vi.fn(),
    joinClassroom: vi.fn(),
    getStudents: vi.fn(),
    updateStudentStatus: vi.fn(),
    deleteClassroom: vi.fn(),
  }
}))

import { classroomService } from '@/services/classroomService'

describe('classroom store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('initial state is empty', () => {
    const store = useClassroomStore()
    expect(store.classrooms).toEqual([])
    expect(store.students).toEqual([])
    expect(store.currentClassroomId).toBeNull()
  })

  it('fetchMyClassrooms populates classrooms', async () => {
    classroomService.getMyClassrooms.mockResolvedValue({
      data: [{ id: 1, name: 'Klasse A', joinCode: 'fjord-tiger' }]
    })
    const store = useClassroomStore()
    await store.fetchMyClassrooms()
    expect(store.classrooms).toHaveLength(1)
    expect(store.classrooms[0].name).toBe('Klasse A')
  })

  it('createClassroom adds to list and returns data', async () => {
    classroomService.createClassroom.mockResolvedValue({
      data: { id: 2, name: 'Ny Klasse', joinCode: 'skog-ulv' }
    })
    const store = useClassroomStore()
    const result = await store.createClassroom({ name: 'Ny Klasse' })
    expect(store.classrooms).toHaveLength(1)
    expect(result.joinCode).toBe('skog-ulv')
  })

  it('joinClassroom stores currentClassroomId', async () => {
    classroomService.joinClassroom.mockResolvedValue({
      data: { classroomId: 5, status: 'PENDING' }
    })
    const store = useClassroomStore()
    await store.joinClassroom({ code: 'fjord-tiger', displayName: 'Elev' })
    expect(store.currentClassroomId).toBe(5)
  })

  it('updateStudentStatus updates the student in the list', async () => {
    classroomService.updateStudentStatus.mockResolvedValue({
      data: { userId: 10, displayName: 'Elev', status: 'APPROVED' }
    })
    const store = useClassroomStore()
    store.students = [{ userId: 10, displayName: 'Elev', status: 'PENDING' }]
    await store.updateStudentStatus(1, 10, 'APPROVED')
    expect(store.students[0].status).toBe('APPROVED')
  })

  it('fetchMyClassrooms propagates errors', async () => {
    classroomService.getMyClassrooms.mockRejectedValue(new Error('Network error'))
    const store = useClassroomStore()
    await expect(store.fetchMyClassrooms()).rejects.toThrow('Network error')
  })

  it('deleteClassroom removes classroom from list', async () => {
    classroomService.deleteClassroom.mockResolvedValue({})
    const store = useClassroomStore()
    store.classrooms = [{ id: 1, name: 'Alpha' }, { id: 2, name: 'Beta' }]
    await store.deleteClassroom(1)
    expect(classroomService.deleteClassroom).toHaveBeenCalledWith(1)
    expect(store.classrooms).toHaveLength(1)
    expect(store.classrooms[0].id).toBe(2)
  })

  it('deleteClassroom throws and does not mutate list when service fails', async () => {
    classroomService.deleteClassroom.mockRejectedValue(new Error('forbidden'))
    const store = useClassroomStore()
    store.classrooms = [{ id: 1, name: 'Alpha' }]
    await expect(store.deleteClassroom(1)).rejects.toThrow('forbidden')
    expect(store.classrooms).toHaveLength(1)
  })
})
