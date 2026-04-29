import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'

export const useAudioStore = defineStore('audio', () => {
  const volume      = ref(parseFloat(localStorage.getItem('audio_volume') ?? '0.25'))
  const muted       = ref(localStorage.getItem('audio_muted') === 'true')
  const teacherMuted = ref(false)

  const effectiveVolume = computed(() => (muted.value || teacherMuted.value) ? 0 : volume.value)

  // Non-reactive Audio instances — not tracked by Vue
  let _global = null
  let _stage  = null
  let _stageActive = false

  function setTeacherMuted(v) {
    teacherMuted.value = v
    _syncVolume()
    console.log('[audio] Teacher muted:', v)
  }

  function setVolume(v) {
    volume.value = Math.max(0, Math.min(1, v))
    localStorage.setItem('audio_volume', String(volume.value))
    _syncVolume()
    console.log('[audio] Volume set to', volume.value)
  }

  function toggleMute() {
    muted.value = !muted.value
    localStorage.setItem('audio_muted', String(muted.value))
    _syncVolume()
    console.log('[audio] Muted:', muted.value)
  }

  function _syncVolume() {
    const v = effectiveVolume.value
    if (_global) _global.volume = v
    if (_stage)  _stage.volume  = v
  }

  watch(effectiveVolume, _syncVolume)

  // ── Global background music ──────────────────────────────────────

  function startGlobal() {
    if (_stageActive) return
    if (!_global) {
      _global = new Audio('/audio/background_musikk.mp3')
      _global.loop = true
      _global.volume = effectiveVolume.value
    }
    _global.play().catch(() => {
      // Autoplay blocked — retry on first interaction
      const retry = () => {
        if (!_stageActive) _global?.play().catch(() => {})
        document.removeEventListener('click',   retry)
        document.removeEventListener('keydown', retry)
      }
      document.addEventListener('click',   retry, { once: true })
      document.addEventListener('keydown', retry, { once: true })
      console.warn('[audio] Global music autoplay blocked — will retry on interaction')
    })
    console.log('[audio] Global music started')
  }

  function stopGlobal() {
    _global?.pause()
    console.log('[audio] Global music paused')
  }

  // ── Stage music ──────────────────────────────────────────────────

  function startStage() {
    if (_stageActive) return
    _stageActive = true
    stopGlobal()

    if (!_stage) {
      _stage = new Audio('/audio/stage_music.mp3')
      _stage.loop = true
      _stage.volume = effectiveVolume.value
    }
    _stage.currentTime = 0
    _stage.play().catch(e => console.warn('[audio] Stage music autoplay blocked:', e))
    console.log('[audio] Stage music started')
  }

  function stopStage() {
    if (!_stageActive) return
    _stageActive = false
    _stage?.pause()
    if (_stage) _stage.currentTime = 0
    console.log('[audio] Stage music stopped')
    startGlobal()
  }

  return { volume, muted, teacherMuted, effectiveVolume, setVolume, toggleMute, setTeacherMuted, startGlobal, stopGlobal, startStage, stopStage }
})
