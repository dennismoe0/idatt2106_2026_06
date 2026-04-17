import { setActivePinia, createPinia } from 'pinia'
import { useNotebookStore } from '@/stores/notebook'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/services/notebookService', () => ({
  notebookService: {
    getEntries: vi.fn(),
    createReflection: vi.fn(),
    getStudentEntries: vi.fn(),
  },
}))

import { notebookService } from '@/services/notebookService'

describe('notebook store', () => {
  beforeEach(() => { setActivePinia(createPinia()) })

  it('grouped sorts by stopOrder and separates AUTO_TIP from REFLECTION', () => {
    const store = useNotebookStore()
    store.entries = [
      { id: 1, stopId: 1, stopName: 'Stop1', stopOrder: 1, entryType: 'AUTO_TIP',   content: 'tip' },
      { id: 2, stopId: 1, stopName: 'Stop1', stopOrder: 1, entryType: 'REFLECTION', content: 'obs' },
      { id: 3, stopId: 2, stopName: 'Stop2', stopOrder: 2, entryType: 'AUTO_TIP',   content: 'tip2' },
    ]
    expect(store.grouped).toHaveLength(2)
    expect(store.grouped[0].stopOrder).toBe(1)
    expect(store.grouped[0].autoTip.content).toBe('tip')
    expect(store.grouped[0].reflections).toHaveLength(1)
    expect(store.grouped[1].autoTip.content).toBe('tip2')
  })

  it('fetchEntries populates entries', async () => {
    const store = useNotebookStore()
    notebookService.getEntries.mockResolvedValue({ data: [
      { id: 1, stopId: 1, stopName: 'Stop1', stopOrder: 1, entryType: 'AUTO_TIP', content: 'tip' }
    ]})
    await store.fetchEntries()
    expect(store.entries).toHaveLength(1)
  })

  it('addReflection pushes new entry to entries', async () => {
    const store = useNotebookStore()
    const newEntry = { id: 5, stopId: 1, stopName: 'Stop1', stopOrder: 1, entryType: 'REFLECTION', content: 'min obs' }
    notebookService.createReflection.mockResolvedValue({ data: newEntry })
    await store.addReflection(1, 'min obs')
    expect(store.entries).toContainEqual(newEntry)
  })

  it('reset clears entries', () => {
    const store = useNotebookStore()
    store.entries = [{ id: 1 }]
    store.reset()
    expect(store.entries).toHaveLength(0)
  })
})
