<template>
  <div class="summary-page" aria-label="Stoppresultat">
    <div class="summary-card">
      <div class="summary-icon" aria-hidden="true">{{ isPerfect ? '🏆' : '📋' }}</div>
      <h2 class="summary-title">{{ isPerfect ? 'Perfekt!' : 'Stopp fullført!' }}</h2>
      <p class="summary-score">
        Du fikk <strong>{{ correctCount }}</strong> av <strong>{{ tasks.length }}</strong> riktige
      </p>

      <div class="summary-stars-row" aria-label="Stjerneresultat">
        <span
          v-for="(task, i) in tasks"
          :key="task.id"
          ref="starRefs"
          class="summary-star"
          :class="{
            'summary-star--earned':   taskResults[task.id]?.correct,
            'summary-star--visible':  visibleStars.has(i)
          }"
          :aria-label="taskResults[task.id]?.correct ? 'Riktig' : 'Feil'"
        >★</span>
      </div>

      <Transition name="xp-pop">
        <div v-if="xpVisible" class="summary-xp" aria-live="polite">
          +{{ displayXp }} XP
        </div>
      </Transition>

      <p v-if="isPerfect" class="summary-msg summary-msg--perfect">
        Strålende innsats — alle oppgaver løst! 🎉
      </p>
      <p v-else class="summary-msg summary-msg--partial">
        Ikke alle riktige — prøv igjen for full poengsum!
      </p>

      <div class="summary-actions">
        <button v-if="!isPerfect" class="summary-btn summary-btn--retry" @click="emit('retry')">
          🔄 Prøv igjen
        </button>
        <button class="summary-btn summary-btn--map" @click="emit('back-to-map')">
          🗺 Tilbake til kartet
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useGameStore } from '@/stores/game'

const props = defineProps({
  tasks:       { type: Array,  required: true },
  taskResults: { type: Object, required: true }
})

const emit = defineEmits(['retry', 'back-to-map'])

const gameStore  = useGameStore()
const starRefs   = ref([])
const visibleStars = ref(new Set())
const flyTimers  = []
const displayXp  = ref(0)
const xpVisible  = ref(false)
let xpRafId      = null

const correctCount = computed(() =>
  props.tasks.filter(t => props.taskResults[t.id]?.correct).length
)

const isPerfect = computed(() => correctCount.value === props.tasks.length)

const newStarsEarned = computed(() =>
  Object.values(props.taskResults).reduce((sum, r) => sum + (r.starsEarned ?? 0), 0)
)

const totalXpEarned = computed(() =>
  Object.values(props.taskResults).reduce((sum, r) => sum + (r.xpEarned ?? 0), 0)
)

function wait(ms) {
  return new Promise(resolve => { flyTimers.push(setTimeout(resolve, ms)) })
}

function animateXp(target) {
  if (target <= 0) return
  xpVisible.value = true
  const startTime = performance.now()
  const duration  = 700
  function tick(now) {
    const progress = Math.min((now - startTime) / duration, 1)
    const eased    = 1 - Math.pow(1 - progress, 3)
    displayXp.value = Math.round(eased * target)
    if (progress < 1) {
      xpRafId = requestAnimationFrame(tick)
    } else {
      displayXp.value = target
    }
  }
  xpRafId = requestAnimationFrame(tick)
}

onMounted(async () => {
  if (newStarsEarned.value > 0) {
    gameStore.prepareStarAnimation(newStarsEarned.value)
  }

  for (let i = 0; i < props.tasks.length; i++) {
    await wait(i === 0 ? 350 : 180)
    visibleStars.value = new Set([...visibleStars.value, i])
  }

  await wait(250)
  animateXp(totalXpEarned.value)

  if (newStarsEarned.value > 0) {
    await wait(200)
    flyGoldStars()
  }
})

onBeforeUnmount(() => {
  flyTimers.forEach(clearTimeout)
  if (xpRafId !== null) cancelAnimationFrame(xpRafId)
})

function flyGoldStars() {
  const target = document.getElementById('player-hud-stars')
  if (!target) return
  const targetRect = target.getBoundingClientRect()

  let flyIndex = 0
  props.tasks.forEach((task, i) => {
    const res = props.taskResults[task.id]
    if (!res?.correct || !(res.starsEarned > 0)) return

    const delay = flyIndex * 280
    flyIndex++

    const t = setTimeout(() => {
      const sourceEl = starRefs.value[i]
      if (!sourceEl) return
      const sourceRect = sourceEl.getBoundingClientRect()

      const clone = document.createElement('span')
      clone.textContent = '★'
      Object.assign(clone.style, {
        position:    'fixed',
        left:        `${sourceRect.left + sourceRect.width  / 2}px`,
        top:         `${sourceRect.top  + sourceRect.height / 2}px`,
        fontSize:    '1.6rem',
        color:       '#f5a623',
        zIndex:      '9999',
        pointerEvents: 'none',
        transform:   'translate(-50%, -50%)',
        textShadow:  '0 0 12px rgba(245,166,35,0.9)',
        willChange:  'transform, opacity',
        transition:  'none',
      })
      document.body.appendChild(clone)

      requestAnimationFrame(() => requestAnimationFrame(() => {
        const dx = (targetRect.left + targetRect.width  / 2) - (sourceRect.left + sourceRect.width  / 2)
        const dy = (targetRect.top  + targetRect.height / 2) - (sourceRect.top  + sourceRect.height / 2)
        clone.style.transition = [
          'transform 0.65s cubic-bezier(0.25, 0.46, 0.45, 0.94)',
          'opacity   0.2s  0.45s',
          'font-size 0.65s cubic-bezier(0.25, 0.46, 0.45, 0.94)',
        ].join(', ')
        clone.style.transform = `translate(calc(-50% + ${dx}px), calc(-50% + ${dy}px)) scale(0.25)`
        clone.style.opacity   = '0'
        clone.style.fontSize  = '0.6rem'
      }))

      const cleanup = setTimeout(() => {
        if (document.body.contains(clone)) document.body.removeChild(clone)
        gameStore.incrementDisplayStar()
      }, 780)
      flyTimers.push(cleanup)
    }, delay)
    flyTimers.push(t)
  })
}
</script>

<style scoped>
.summary-page {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-8) var(--space-4);
  min-height: 60vh;
  animation: fade-in 0.25s ease;
}

@keyframes fade-in {
  from { opacity: 0; }
  to   { opacity: 1; }
}

.summary-card {
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  padding: var(--space-8) var(--space-8) var(--space-6);
  max-width: 420px;
  width: 100%;
  text-align: center;
  box-shadow: var(--shadow-lg);
  animation: slide-up 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes slide-up {
  from { opacity: 0; transform: translateY(40px) scale(0.95); }
  to   { opacity: 1; transform: translateY(0)    scale(1);    }
}

.summary-icon {
  font-size: 3rem;
  line-height: 1;
  margin-bottom: var(--space-2);
}

.summary-title {
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  color: var(--color-text);
  margin: 0 0 var(--space-2);
}

.summary-score {
  font-size: var(--text-lg);
  color: var(--color-text-muted);
  margin: 0 0 var(--space-6);
}
.summary-score strong { color: var(--color-primary); }

.summary-stars-row {
  display: flex;
  justify-content: center;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}

.summary-star {
  font-size: 2.2rem;
  line-height: 1;
  color: #ddd;
  opacity: 0;
  transform: scale(0.2) rotate(-30deg);
  transition: opacity 0.25s ease, transform 0.35s cubic-bezier(0.34, 1.56, 0.64, 1), color 0.25s ease;
}

.summary-star--visible {
  opacity: 1;
  transform: scale(1) rotate(0deg);
}

.summary-star--earned.summary-star--visible {
  color: #f5a623;
  filter: drop-shadow(0 0 6px rgba(245, 166, 35, 0.6));
}

.summary-xp {
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  color: var(--color-primary);
  text-align: center;
  letter-spacing: 0.02em;
  margin-bottom: var(--space-2);
}

.xp-pop-enter-active { transition: opacity 0.3s ease, transform 0.4s cubic-bezier(0.34, 1.56, 0.64, 1); }
.xp-pop-leave-active { transition: opacity 0.2s ease; }
.xp-pop-enter-from   { opacity: 0; transform: translateY(8px) scale(0.85); }
.xp-pop-leave-to     { opacity: 0; }

.summary-msg {
  font-size: var(--text-sm);
  margin: 0 0 var(--space-6);
}
.summary-msg--perfect  { color: var(--color-success); }
.summary-msg--partial  { color: var(--color-text-muted); }

.summary-actions {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.summary-btn {
  width: 100%;
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-md);
  border: none;
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
  cursor: pointer;
  transition: background var(--transition-fast), transform 0.1s;
}
.summary-btn:active { transform: scale(0.98); }

.summary-btn--retry {
  background: var(--color-surface);
  border: 2px solid var(--color-primary);
  color: var(--color-primary);
}
.summary-btn--retry:hover { background: var(--color-primary-light); }

.summary-btn--map {
  background: var(--color-primary);
  color: #fff;
}
.summary-btn--map:hover { background: var(--color-primary-dark, var(--color-primary)); filter: brightness(0.9); }
</style>
