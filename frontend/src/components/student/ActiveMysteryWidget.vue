<script setup>
import { ref, onMounted } from 'vue'
import { weeklyMysteryService } from '@/services/weeklyMysteryService.js'

const props = defineProps({ classroomId: { type: Number, required: true } })

const mystery   = ref(null)
const loading   = ref(true)
const answer    = ref(null)
const result    = ref(null)
const submitting = ref(false)

onMounted(async () => {
  console.log('[ActiveMysteryWidget] loading mystery for classroom:', props.classroomId)
  try {
    mystery.value = await weeklyMysteryService.getActive(props.classroomId)
    console.log('[ActiveMysteryWidget] mystery loaded:', mystery.value)
  } catch (err) {
    console.error('[ActiveMysteryWidget] failed to load', err)
  } finally {
    loading.value = false
  }
})

async function submitAnswer() {
  if (!answer.value) return
  submitting.value = true
  console.log('[ActiveMysteryWidget] submitting answer:', answer.value)
  try {
    result.value = await weeklyMysteryService.complete(props.classroomId, answer.value)
    console.log('[ActiveMysteryWidget] result received:', result.value)
  } catch (err) {
    console.error('[ActiveMysteryWidget] complete failed', err)
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <!-- Loading -->
  <div v-if="loading" class="mystery-card mystery-card--loading" role="status" aria-live="polite">
    <span class="mystery-card__pin" aria-hidden="true" />
    Laster ukens mysterium...
  </div>

  <!-- No active mystery -->
  <div v-else-if="!mystery" class="mystery-card mystery-card--empty">
    <span class="mystery-card__pin" aria-hidden="true" />
    <p class="mystery-card__empty-text">Ingen aktiv ukens mysterium akkurat nå.<br>Kom tilbake senere!</p>
  </div>

  <!-- Active mystery -->
  <article v-else class="mystery-card" aria-label="Ukens mysterium">
    <span class="mystery-card__pin" aria-hidden="true" />

    <div class="mystery-card__header">
      <span class="mystery-card__badge">🔍 UKAS MYSTERIUM</span>
      <span class="mystery-card__rewards" aria-label="Belønning">
        ⭐ {{ mystery.rewardStars }} &nbsp;·&nbsp; ⚡ {{ mystery.rewardXp }} XP
      </span>
    </div>

    <h2 class="mystery-card__title">{{ mystery.title }}</h2>
    <p class="mystery-card__desc">{{ mystery.description }}</p>

    <img
      :src="mystery.imageUrl || '/suspects/basser-gravling.png'"
      :alt="mystery.title"
      class="mystery-card__image"
    />

    <!-- Result -->
    <div
      v-if="result"
      class="mystery-card__result"
      :class="result.correct ? 'mystery-card__result--correct' : 'mystery-card__result--wrong'"
      role="status"
      aria-live="polite"
    >
      <p class="mystery-card__result-headline">
        {{ result.correct ? '✅ Riktig svar!' : '❌ Ikke helt riktig' }}
      </p>
      <p v-if="result.teacherComment" class="mystery-card__comment">
        "{{ result.teacherComment }}"
      </p>
      <p v-if="result.medalEarned" class="mystery-card__medal">
        🏅 Du fikk medaljen: <strong>{{ result.medalName }}</strong>!
      </p>
    </div>

    <!-- Answer form -->
    <div
      v-else-if="mystery.mysteryType === 'REAL_OR_FAKE' || mystery.questionText"
      class="mystery-card__question"
    >
      <p class="mystery-card__question-text">
        {{ mystery.questionText || 'Er dette ekte eller falskt?' }}
      </p>

      <div class="mystery-card__choices" role="radiogroup" aria-label="Velg svar">
        <label class="mystery-card__choice" :class="{ 'mystery-card__choice--selected': answer === 'REAL' }">
          <input v-model="answer" type="radio" value="REAL" class="mystery-card__radio" />
          <span class="mystery-card__choice-label">✅ Ekte</span>
        </label>
        <label class="mystery-card__choice" :class="{ 'mystery-card__choice--selected': answer === 'FAKE' }">
          <input v-model="answer" type="radio" value="FAKE" class="mystery-card__radio" />
          <span class="mystery-card__choice-label">❌ Falskt</span>
        </label>
      </div>

      <button
        class="mystery-card__btn"
        :disabled="!answer || submitting"
        @click="submitAnswer"
      >
        {{ submitting ? 'Sender...' : 'Send svar' }}
      </button>
    </div>
  </article>
</template>

<style scoped>
/* ---- Shared card base ---- */
.mystery-card {
  position: relative;
  background:
    linear-gradient(160deg, #fffef5 0%, var(--color-note-bg) 100%);
  border: 2px solid var(--color-note-border);
  border-radius: var(--radius-md);
  padding: var(--space-8) var(--space-6) var(--space-6);
  box-shadow:
    0 6px 0 rgba(0, 0, 0, 0.25),
    0 2px 12px rgba(0, 0, 0, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  transform: rotate(-0.4deg);
}

/* ---- Pushpin ---- */
.mystery-card__pin {
  position: absolute;
  top: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: radial-gradient(circle at 35% 35%, #e53e3e, #9b2226);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.4);
  display: block;
}

/* ---- States ---- */
.mystery-card--loading,
.mystery-card--empty {
  text-align: center;
  padding: var(--space-10) var(--space-6);
  color: var(--color-wood);
  font-size: var(--text-base);
}

.mystery-card__empty-text {
  margin: var(--space-4) 0 0;
  line-height: 1.6;
}

/* ---- Header ---- */
.mystery-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--space-2);
  margin-bottom: var(--space-4);
}

.mystery-card__badge {
  background: var(--color-medal-bronze-bg);
  color: var(--color-medal-bronze-border);
  border: 1px solid var(--color-medal-bronze-border);
  font-weight: var(--font-bold);
  font-size: var(--text-xs);
  letter-spacing: 0.1em;
  text-transform: uppercase;
  padding: 0.3rem 0.75rem;
  border-radius: 999px;
}

.mystery-card__rewards {
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  color: var(--color-wood);
}

/* ---- Content ---- */
.mystery-card__title {
  font-family: 'Special Elite', serif;
  font-size: clamp(1.3rem, 4vw, 1.8rem);
  font-weight: var(--font-bold);
  color: var(--color-wood);
  margin: 0 0 var(--space-3);
  line-height: 1.25;
}

.mystery-card__desc {
  font-size: var(--text-base);
  color: var(--color-wood-mid);
  line-height: 1.6;
  margin: 0 0 var(--space-4);
}

.mystery-card__image {
  width: 100%;
  max-height: 420px;
  object-fit: contain;
  border-radius: var(--radius-sm);
  border: 2px solid var(--color-note-border);
  margin-bottom: var(--space-4);
  background: var(--color-cork-light);
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.2);
}

/* ---- Question ---- */
.mystery-card__question-text {
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: var(--color-wood);
  margin: 0 0 var(--space-4);
  line-height: 1.4;
}

.mystery-card__choices {
  display: flex;
  gap: var(--space-3);
  margin-bottom: var(--space-8);
  flex-wrap: wrap;
}

.mystery-card__choice {
  flex: 1;
  min-width: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-4);
  background: var(--color-note-bg);
  border: 2px solid var(--color-note-border);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s, transform 0.1s;
  box-shadow: 0 3px 0 rgba(0, 0, 0, 0.18);
}

.mystery-card__choice:hover {
  background: var(--color-note-chosen-bg);
  transform: translateY(-1px);
}

.mystery-card__choice--selected {
  background: var(--color-note-chosen-bg);
  border-color: var(--color-cork-dark);
  box-shadow: 0 2px 0 rgba(0, 0, 0, 0.18);
  transform: translateY(1px);
}

.mystery-card__radio {
  position: absolute;
  opacity: 0;
  width: 0;
  height: 0;
}

.mystery-card__choice-label {
  font-size: var(--text-base);
  font-weight: var(--font-bold);
  color: var(--color-wood);
  pointer-events: none;
}

.mystery-card__btn {
  width: 100%;
  min-height: 48px;
  padding: var(--space-3) var(--space-6);
  background: var(--color-wood);
  color: var(--color-medal-gold-bg);
  border: none;
  border-radius: var(--radius-sm);
  font-family: 'Special Elite', serif;
  font-size: var(--text-base);
  font-weight: var(--font-bold);
  cursor: pointer;
  box-shadow: 0 4px 0 rgba(0, 0, 0, 0.3);
  transition: background 0.15s, transform 0.1s, box-shadow 0.1s;
}

.mystery-card__btn:hover:not(:disabled) {
  background: var(--color-wood-mid);
  transform: translateY(-1px);
  box-shadow: 0 5px 0 rgba(0, 0, 0, 0.3);
}

.mystery-card__btn:active:not(:disabled) {
  transform: translateY(2px);
  box-shadow: 0 2px 0 rgba(0, 0, 0, 0.3);
}

.mystery-card__btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  box-shadow: 0 2px 0 rgba(0, 0, 0, 0.2);
}

/* ---- Result ---- */
.mystery-card__result {
  padding: var(--space-4);
  border-radius: var(--radius-sm);
  border: 2px solid;
}

.mystery-card__result--correct {
  background: var(--color-note-correct-bg);
  border-color: var(--color-success);
}

.mystery-card__result--wrong {
  background: var(--color-note-wrong-bg);
  border-color: var(--color-danger);
}

.mystery-card__result-headline {
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  margin: 0 0 var(--space-2);
  color: var(--color-wood);
}

.mystery-card__comment {
  font-style: italic;
  font-size: var(--text-sm);
  color: var(--color-wood-mid);
  margin: 0 0 var(--space-2);
  line-height: 1.5;
}

.mystery-card__medal {
  font-size: var(--text-base);
  color: var(--color-wood);
  margin: var(--space-2) 0 0;
}
</style>
