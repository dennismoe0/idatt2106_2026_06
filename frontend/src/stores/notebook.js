import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { notebookService } from '@/services/notebookService'

export const useNotebookStore = defineStore('notebook', () => {
  const entries = ref([])

  const generalNotes = computed(() =>
    entries.value.filter(e => e.entryType === 'GENERAL_NOTE')
  )

  const grouped = computed(() => {
    const map = new Map()
    for (const e of entries.value) {
      if (e.entryType === 'GENERAL_NOTE') continue
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

  async function addGeneralNote(content) {
    console.log('[notebook] addGeneralNote')
    try {
      const { data } = await notebookService.createGeneralNote(content)
      entries.value.unshift(data)
      console.log('[notebook] General note added id:', data.id)
      return data
    } catch (err) {
      console.error('[notebook] Failed to add general note:', err)
      throw err
    }
  }

  async function updateNote(id, content) {
    console.log('[notebook] updateNote id:', id)
    try {
      const { data } = await notebookService.updateNote(id, content)
      const idx = entries.value.findIndex(e => e.id === id)
      if (idx !== -1) entries.value[idx] = data
      console.log('[notebook] Note updated id:', id)
      return data
    } catch (err) {
      console.error('[notebook] Failed to update note:', err)
      throw err
    }
  }

  async function deleteNote(id) {
    console.log('[notebook] deleteNote id:', id)
    try {
      await notebookService.deleteNote(id)
      entries.value = entries.value.filter(e => e.id !== id)
      console.log('[notebook] Note deleted id:', id)
    } catch (err) {
      console.error('[notebook] Failed to delete note:', err)
      throw err
    }
  }

  function reset() {
    entries.value = []
  }

  return { entries, generalNotes, grouped, fetchEntries, addReflection, addGeneralNote, updateNote, deleteNote, reset }
})
