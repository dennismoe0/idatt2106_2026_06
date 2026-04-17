import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { notebookService } from '@/services/notebookService'

export const useNotebookStore = defineStore('notebook', () => {
  const entries = ref([])

  const grouped = computed(() => {
    const map = new Map()
    for (const e of entries.value) {
      if (!map.has(e.stopId)) {
        map.set(e.stopId, {
          stopId: e.stopId,
          stopName: e.stopName,
          stopOrder: e.stopOrder,
          autoTip: null,
          reflections: [],
        })
      }
      const group = map.get(e.stopId)
      if (e.entryType === 'AUTO_TIP') {
        group.autoTip = e
      } else {
        group.reflections.push(e)
      }
    }
    return [...map.values()].sort((a, b) => a.stopOrder - b.stopOrder)
  })

  async function fetchEntries() {
    console.log('[notebook] fetchEntries')
    try {
      const { data } = await notebookService.getEntries()
      entries.value = data
      console.log('[notebook] Fetched', data.length, 'entries')
    } catch (err) {
      console.error('[notebook] Failed to fetch entries:', err)
      throw err
    }
  }

  async function addReflection(stopId, content) {
    console.log('[notebook] addReflection stopId:', stopId)
    try {
      const { data } = await notebookService.createReflection(stopId, content)
      entries.value.push(data)
      console.log('[notebook] Reflection added id:', data.id)
      return data
    } catch (err) {
      console.error('[notebook] Failed to add reflection:', err)
      throw err
    }
  }

  function reset() {
    entries.value = []
  }

  return { entries, grouped, fetchEntries, addReflection, reset }
})
