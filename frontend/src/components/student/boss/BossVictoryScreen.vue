<template>
  <div class="vs" role="dialog" aria-modal="true" aria-label="Boss beseiret - Millie Mus arrestert">
    <canvas ref="canvas" class="vs__confetti" aria-hidden="true" />

    <div class="vs__card">
      <!-- Mugshot with animated jail bars -->
      <div class="vs__mugshot-wrap">
        <img
          src="/suspects/millie_fengsel.jpeg"
          alt="Millie Mus i fengsel"
          class="vs__mugshot"
        />
        <div class="vs__bars" :class="{ 'vs__bars--shut': shut }">
          <div v-for="n in 9" :key="n" class="vs__bar" :style="`--i:${n}`" />
          <div class="vs__rail vs__rail--top" />
          <div class="vs__rail vs__rail--bot" />
        </div>
        <span class="vs__lock" :class="{ 'vs__lock--show': shut }" aria-hidden="true">🔒</span>
      </div>

      <h1 class="vs__headline">🏆 Du er en Mesterdetektiv! 🏆</h1>
      <p class="vs__body">
        Millie Mus er arrestert.<br>
        Pengene til idrettsparken er reddet.<br>
        Internettbyen er trygg igjen!
      </p>

      <button class="vs__btn" @click="$emit('next')">Se oppsummering →</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

defineEmits(['next'])

const canvas = ref(null)
const shut   = ref(false)
let raf = null

onMounted(() => {
  startConfetti()
  playFanfare()
  setTimeout(() => {
    shut.value = true
    playClang()
  }, 480)
})

onUnmounted(() => {
  if (raf) { cancelAnimationFrame(raf); raf = null }
})

function startConfetti() {
  const c = canvas.value
  if (!c) return
  const ctx = c.getContext('2d')

  const resize = () => { c.width = window.innerWidth; c.height = window.innerHeight }
  resize()
  window.addEventListener('resize', resize)

  const COLORS = ['#f5c842','#e74c3c','#27ae60','#3498db','#9b59b6','#e67e22','#1abc9c','#f39c12','#ffffff','#ff69b4']
  const pieces = Array.from({ length: 300 }, () => ({
    x:     Math.random() * c.width,
    y:     Math.random() * c.height - c.height,
    w:     Math.random() * 14 + 6,
    h:     Math.random() * 20 + 8,
    color: COLORS[Math.floor(Math.random() * COLORS.length)],
    vx:    (Math.random() - 0.5) * 6,
    vy:    Math.random() * 5 + 3,
    rot:   Math.random() * 360,
    vr:    (Math.random() - 0.5) * 9,
    round: Math.random() > 0.55,
  }))

  function draw() {
    ctx.clearRect(0, 0, c.width, c.height)
    for (const p of pieces) {
      ctx.save()
      ctx.translate(p.x + p.w / 2, p.y + p.h / 2)
      ctx.rotate((p.rot * Math.PI) / 180)
      ctx.fillStyle = p.color
      if (p.round) {
        ctx.beginPath()
        ctx.arc(0, 0, p.w / 2, 0, Math.PI * 2)
        ctx.fill()
      } else {
        ctx.fillRect(-p.w / 2, -p.h / 2, p.w, p.h)
      }
      ctx.restore()
      p.x  += p.vx
      p.y  += p.vy
      p.rot += p.vr
      p.vx *= 0.992
      if (p.y > c.height + 24) {
        p.y = -30
        p.x = Math.random() * c.width
        p.vy = Math.random() * 5 + 3
      }
    }
    raf = requestAnimationFrame(draw)
  }
  draw()
}

function playFanfare() {
  try {
    const ctx = new AudioContext()
    const t   = ctx.currentTime

    const note = (freq, start, dur, vol = 0.25) => {
      const osc    = ctx.createOscillator()
      const gain   = ctx.createGain()
      // Slight vibrato via LFO
      const lfo    = ctx.createOscillator()
      const lfoGain = ctx.createGain()
      lfo.frequency.value = 6
      lfoGain.gain.value  = freq * 0.012
      lfo.connect(lfoGain)
      lfoGain.connect(osc.frequency)

      osc.type = 'square'
      osc.frequency.setValueAtTime(freq, t + start)
      gain.gain.setValueAtTime(0, t + start)
      gain.gain.linearRampToValueAtTime(vol, t + start + 0.03)
      gain.gain.setValueAtTime(vol, t + start + dur - 0.06)
      gain.gain.linearRampToValueAtTime(0, t + start + dur)

      osc.connect(gain)
      gain.connect(ctx.destination)
      lfo.start(t + start)
      osc.start(t + start)
      osc.stop(t + start + dur)
      lfo.stop(t + start + dur)
    }

    // Classic ta-da-da-DAAAA fanfare (C major)
    // C4=261.6 E4=329.6 G4=392 C5=523.3
    note(261.6, 0.00, 0.18, 0.22)
    note(329.6, 0.18, 0.18, 0.22)
    note(392.0, 0.36, 0.18, 0.22)
    note(523.3, 0.54, 0.65, 0.30)
    note(392.0, 0.54, 0.65, 0.15)

    console.log('[BossVictoryScreen] Fanfare played')
  } catch (e) {
    console.warn('[BossVictoryScreen] Web Audio unavailable:', e)
  }
}

function playClang() {
  try {
    const ctx = new AudioContext()
    const t   = ctx.currentTime

    const voice = (type, freq, freqEnd, vol, dur, start = 0) => {
      const osc  = ctx.createOscillator()
      const gain = ctx.createGain()
      osc.type = type
      osc.frequency.setValueAtTime(freq, t + start)
      osc.frequency.exponentialRampToValueAtTime(freqEnd, t + start + dur * 0.45)
      gain.gain.setValueAtTime(vol, t + start)
      gain.gain.exponentialRampToValueAtTime(0.0001, t + start + dur)
      osc.connect(gain)
      gain.connect(ctx.destination)
      osc.start(t + start)
      osc.stop(t + start + dur)
    }

    // Primary slam
    voice('sawtooth', 200,  55,  0.55, 1.0)
    voice('sawtooth', 340, 100,  0.30, 0.75)
    voice('square',   900, 600,  0.10, 0.45)

    // Bounce echo — bars settle
    voice('sawtooth', 150, 50, 0.22, 0.55, 0.38)
    voice('square',   650, 400, 0.07, 0.35, 0.42)

    console.log('[BossVictoryScreen] Clang played')
  } catch (e) {
    console.warn('[BossVictoryScreen] Web Audio unavailable:', e)
  }
}
</script>

<style scoped>
/* ── Full-screen overlay ── */
.vs {
  position: fixed;
  inset: 0;
  z-index: 300;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(ellipse at 50% 40%, #0d2010 0%, #020a02 100%);
  animation: vs-in 0.45s ease both;
}

@keyframes vs-in {
  from { opacity: 0; transform: scale(1.05); }
  to   { opacity: 1; transform: scale(1); }
}

/* ── Confetti canvas ── */
.vs__confetti {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

/* ── Content card ── */
.vs__card {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-8);
  text-align: center;
  padding: var(--space-6) var(--space-4);
  max-width: 560px;
  width: 100%;
}

/* ── Mugshot frame ── */
.vs__mugshot-wrap {
  position: relative;
  width: min(442px, 90vw);
  border-radius: 4px;
  overflow: hidden;
  box-shadow:
    0 0 0 5px #3a3a3a,
    0 0 0 9px #111,
    0 28px 70px rgba(0, 0, 0, 0.85);
}

.vs__mugshot {
  display: block;
  width: 100%;
  height: auto;
}

/* ── Jail bars ── */
.vs__bars {
  position: absolute;
  inset: 0;
  display: flex;
  gap: 10px;
  padding: 0 6px;
  pointer-events: none;
}

.vs__bar {
  flex: 1;
  background: linear-gradient(
    90deg,
    #141414 0%,
    #484848 18%,
    #909090 40%,
    #b8b8b8 50%,
    #909090 60%,
    #484848 82%,
    #141414 100%
  );
  transform: translateY(-104%);
  transition: transform 3s cubic-bezier(0.18, 1.45, 0.32, 1);
  transition-delay: calc(var(--i) * 220ms);
}

.vs__bars--shut .vs__bar {
  transform: translateY(0);
}

.vs__rail {
  position: absolute;
  left: 0;
  right: 0;
  height: 13px;
  background: linear-gradient(180deg, #606060 0%, #333 45%, #1a1a1a 100%);
  z-index: 2;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.6);
}
.vs__rail--top { top: 0; }
.vs__rail--bot { bottom: 0; }

/* ── Lock badge pop ── */
.vs__lock {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 5rem;
  filter: drop-shadow(0 4px 16px rgba(0, 0, 0, 0.8));
  transform: scale(0) rotate(-15deg);
  transition: transform 0.55s cubic-bezier(0.34, 1.56, 0.64, 1);
  transition-delay: 3.2s;
  pointer-events: none;
  z-index: 10;
}
.vs__lock--show {
  transform: scale(1) rotate(0deg);
}

/* ── Text ── */
.vs__headline {
  margin: 0;
  font-size: clamp(1.35rem, 4vw, 2rem);
  font-weight: 900;
  color: #f5c842;
  text-shadow:
    0 0 24px rgba(245, 200, 66, 0.55),
    0 2px 8px rgba(0, 0, 0, 0.9);
  animation: pop-in 0.55s 3.8s cubic-bezier(0.34, 1.56, 0.64, 1) both;
}

.vs__body {
  margin: 0;
  color: rgba(255, 255, 255, 0.9);
  font-family: 'Georgia', 'Times New Roman', serif;
  font-size: var(--text-xl);
  line-height: 1.7;
  animation: fade-up 0.45s 4.1s ease both;
}

/* ── Button ── */
.vs__btn {
  background: var(--color-success);
  color: #fff;
  border: none;
  border-radius: var(--radius-md);
  padding: var(--space-4) var(--space-10);
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  cursor: pointer;
  box-shadow: 0 6px 28px rgba(0, 0, 0, 0.5);
  transition: background var(--transition-fast), transform var(--transition-fast);
  animation: fade-up 0.45s 4.4s ease both;
}
.vs__btn:hover  { background: #15803d; transform: translateY(-2px); }
.vs__btn:active { transform: scale(0.97); }

@keyframes pop-in {
  from { opacity: 0; transform: scale(0.65); }
  to   { opacity: 1; transform: scale(1); }
}

@keyframes fade-up {
  from { opacity: 0; transform: translateY(10px); }
  to   { opacity: 1; transform: translateY(0); }
}
</style>
