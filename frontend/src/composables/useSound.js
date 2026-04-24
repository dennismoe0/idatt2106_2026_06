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

  /**
   * Soft page-turn / paper-rustle style cue for notebook navigation.
   */
  function noiseBurst(ctx, startTime, duration, volume = 0.2, bandFreq = 1200, Q = 0.8) {
    const sampleRate = ctx.sampleRate
    const frameCount = Math.max(1, Math.floor(sampleRate * duration))
    const buffer = ctx.createBuffer(1, frameCount, sampleRate)
    const data = buffer.getChannelData(0)
    for (let i = 0; i < frameCount; i++) {
      // white noise with gentle decay
      data[i] = (Math.random() * 2 - 1) * (1 - i / frameCount)
    }
    const src = ctx.createBufferSource()
    src.buffer = buffer

    const filter = ctx.createBiquadFilter()
    filter.type = 'bandpass'
    filter.frequency.setValueAtTime(bandFreq, startTime)
    filter.Q.setValueAtTime(Q, startTime)

    const gain = ctx.createGain()
    gain.gain.setValueAtTime(0.0001, startTime)
    gain.gain.exponentialRampToValueAtTime(volume, startTime + 0.005)
    gain.gain.exponentialRampToValueAtTime(0.0001, startTime + duration)

    src.connect(filter)
    filter.connect(gain)
    gain.connect(ctx.destination)

    src.start(startTime)
    src.stop(startTime + duration + 0.02)
  }

  function playPageTurn() {
    const vol = audioStore.effectiveVolume
    if (vol === 0) return
    try {
      const ctx = getCtx()
      const now = ctx.currentTime
      const v = vol * 0.18

      // low body — the weight of the page
      tone(200, now, 0.06, v * 0.55, 'triangle', ctx)
      // crisp edge — the page's leading edge
      tone(1200, now + 0.006, 0.03, v * 0.28, 'triangle', ctx)
      // mid-band rustle (main paper sound)
      noiseBurst(ctx, now, 0.18, v * 0.9, 900, 0.7)
      // high flutter (edge and paper fibres)
      noiseBurst(ctx, now + 0.02, 0.10, v * 0.5, 3500, 0.5)
    } catch (e) {
      console.warn('[useSound] playPageTurn failed:', e)
    }
  }

  return { playCorrect, playWrong, playFanfare, playClick, playPageTurn }
}
