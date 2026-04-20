/**
 * useSound — Web Audio API sound synthesis. No audio files required.
 * All sounds generated programmatically, works fully offline.
 */
import { useAudioStore } from '@/stores/audio'

let _ctx = null

function getCtx() {
  if (!_ctx) _ctx = new (window.AudioContext || window.webkitAudioContext)()
  // Resume if browser suspended it (autoplay policy)
  if (_ctx.state === 'suspended') _ctx.resume()
  return _ctx
}

function tone(freq, startTime, duration, vol, type = 'sine', ctx) {
  const osc  = ctx.createOscillator()
  const gain = ctx.createGain()
  osc.connect(gain)
  gain.connect(ctx.destination)
  osc.type = type
  osc.frequency.setValueAtTime(freq, startTime)
  gain.gain.setValueAtTime(0, startTime)
  gain.gain.linearRampToValueAtTime(vol, startTime + 0.01)
  gain.gain.exponentialRampToValueAtTime(0.001, startTime + duration)
  osc.start(startTime)
  osc.stop(startTime + duration + 0.01)
}

export function useSound() {
  const audioStore = useAudioStore()

  /**
   * Bright ascending major-chord chime — plays on correct answer.
   * C5 → E5 → G5, staggered 80ms apart, clean sine waves.
   */
  function playCorrect() {
    const vol = audioStore.effectiveVolume
    if (vol === 0) return
    try {
      const ctx = getCtx()
      const now = ctx.currentTime
      const v = vol * 0.28
      // C5 E5 G5 — ascending major chord arpeggio
      tone(523.25, now,        0.45, v, 'sine', ctx)
      tone(659.25, now + 0.08, 0.40, v, 'sine', ctx)
      tone(783.99, now + 0.16, 0.55, v, 'sine', ctx)
      // Octave C6 shimmer on top
      tone(1046.5, now + 0.22, 0.30, v * 0.5, 'sine', ctx)
    } catch (e) {
      console.warn('[useSound] playCorrect failed:', e)
    }
  }

  /**
   * Soft descending dull thud — plays on wrong answer.
   */
  function playWrong() {
    const vol = audioStore.effectiveVolume
    if (vol === 0) return
    try {
      const ctx = getCtx()
      const now = ctx.currentTime
      const v = vol * 0.22
      tone(220, now,        0.35, v, 'sine', ctx)
      tone(180, now + 0.08, 0.40, v, 'sine', ctx)
      tone(150, now + 0.18, 0.45, v * 0.7, 'sine', ctx)
    } catch (e) {
      console.warn('[useSound] playWrong failed:', e)
    }
  }

  /**
   * Fanfare — stop completion + medal earned.
   * Bright two-bar ascending fanfare.
   */
  function playFanfare() {
    const vol = audioStore.effectiveVolume
    if (vol === 0) return
    try {
      const ctx = getCtx()
      const now = ctx.currentTime
      const v = vol * 0.30
      // Bar 1: C5 E5 G5 C6
      tone(523.25, now,        0.20, v,       'square', ctx)
      tone(659.25, now + 0.14, 0.20, v,       'square', ctx)
      tone(783.99, now + 0.28, 0.20, v,       'square', ctx)
      tone(1046.5, now + 0.42, 0.50, v * 1.1, 'square', ctx)
      // Soften with parallel sine
      tone(523.25, now,        0.20, v * 0.5, 'sine', ctx)
      tone(659.25, now + 0.14, 0.20, v * 0.5, 'sine', ctx)
      tone(783.99, now + 0.28, 0.20, v * 0.5, 'sine', ctx)
      tone(1046.5, now + 0.42, 0.50, v * 0.6, 'sine', ctx)
    } catch (e) {
      console.warn('[useSound] playFanfare failed:', e)
    }
  }

  /**
   * Short soft click / UI feedback.
   */
  function playClick() {
    const vol = audioStore.effectiveVolume
    if (vol === 0) return
    try {
      const ctx = getCtx()
      tone(800, ctx.currentTime, 0.06, vol * 0.12, 'sine', ctx)
    } catch (e) {
      console.warn('[useSound] playClick failed:', e)
    }
  }

  return { playCorrect, playWrong, playFanfare, playClick }
}
