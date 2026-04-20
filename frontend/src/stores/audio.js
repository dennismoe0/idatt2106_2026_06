import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAudioStore = defineStore('audio', () => {
  const volume = ref(parseFloat(localStorage.getItem('audio_volume') ?? '0.6'))
  const muted  = ref(localStorage.getItem('audio_muted') === 'true')

  const effectiveVolume = computed(() => muted.value ? 0 : volume.value)

  function setVolume(v) {
    volume.value = Math.max(0, Math.min(1, v))
    localStorage.setItem('audio_volume', String(volume.value))
    console.log('[audio] Volume set to', volume.value)
  }

  function toggleMute() {
    muted.value = !muted.value
    localStorage.setItem('audio_muted', String(muted.value))
    console.log('[audio] Muted:', muted.value)
  }

  return { volume, muted, effectiveVolume, setVolume, toggleMute }
})
