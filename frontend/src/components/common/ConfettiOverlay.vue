<template>
  <!-- canvas-confetti renders directly to a full-screen canvas — no DOM needed here -->
</template>

<script setup>
import { watch, onBeforeUnmount } from 'vue'
import confetti from 'canvas-confetti'

const props = defineProps({
  /**
   * 'correct'  — small burst on a correct answer
   * 'stop'     — big triple-burst for stop completion
   * false/null — not active
   */
  active: { type: [String, Boolean], default: false }
})

const COLORS = ['#FFD700', '#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEAA7', '#DDA0DD', '#98FB98']

const timers = []

function clearTimers() {
  timers.forEach(clearTimeout)
  timers.length = 0
}

function burstFromBottom(origin, angle, count, velocity) {
  confetti({
    particleCount: count,
    angle,
    spread: 70,
    origin,
    startVelocity: velocity,
    gravity: 0.75,
    scalar: 1.1,
    ticks: 280,
    colors: COLORS,
    disableForReducedMotion: true,
  })
}

function fireCorrect() {
  // Single upward burst from bottom-center
  burstFromBottom({ x: 0.5, y: 1 }, 90, 80, 55)
  // Two side bursts half a beat later — gives the "spreading across screen" feel
  timers.push(setTimeout(() => {
    burstFromBottom({ x: 0.2, y: 1 }, 65, 50, 45)
    burstFromBottom({ x: 0.8, y: 1 }, 115, 50, 45)
  }, 120))
}

function fireStop() {
  // Big center burst
  burstFromBottom({ x: 0.5, y: 1 }, 90, 160, 70)
  // Left sweep
  timers.push(setTimeout(() => {
    burstFromBottom({ x: 0.15, y: 1 }, 60, 100, 55)
    burstFromBottom({ x: 0.85, y: 1 }, 120, 100, 55)
  }, 150))
  // Final extra pop from center
  timers.push(setTimeout(() => {
    burstFromBottom({ x: 0.5, y: 0.9 }, 90, 80, 40)
  }, 350))
}

watch(() => props.active, (val) => {
  clearTimers()
  if (!val) return
  if (val === 'stop') {
    fireStop()
  } else {
    // 'correct' or any truthy string
    fireCorrect()
  }
}, { immediate: true })

onBeforeUnmount(() => {
  clearTimers()
  confetti.reset()
})
</script>
