<script setup>
import { ref, onMounted } from 'vue'
import { weeklyMysteryService } from '@/services/weeklyMysteryService.js'

const props = defineProps({ classroomId: { type: Number, required: true } })

const mystery = ref(null)
const loading = ref(true)
const answer = ref(null)
const result = ref(null)
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
  <div v-if="loading" class="mystery-widget mystery-widget--loading">Laster ukas mysterium...</div>

  <div v-else-if="!mystery" class="mystery-widget mystery-widget--empty">
    Ingen aktiv ukens mysterium akkurat nå.
  </div>

  <div v-else class="mystery-widget">
    <div class="mystery-widget__header">
      <span class="mystery-widget__badge">🔍 UKAS MYSTERIUM</span>
      <span class="mystery-widget__rewards">⭐ {{ mystery.rewardStars }} · {{ mystery.rewardXp }} XP</span>
    </div>

    <h2 class="mystery-widget__title">{{ mystery.title }}</h2>
    <p class="mystery-widget__desc">{{ mystery.description }}</p>
    <img
      v-if="mystery.imageUrl"
      :src="mystery.imageUrl"
      :alt="mystery.title"
      class="mystery-widget__image"
    />

    <div
      v-if="result"
      class="mystery-widget__result"
      :class="result.correct ? 'mystery-widget__result--correct' : 'mystery-widget__result--wrong'"
    >
      <strong>{{ result.correct ? '✅ Riktig!' : '❌ Feil' }}</strong>
      <p v-if="result.teacherComment" class="mystery-widget__comment">{{ result.teacherComment }}</p>
      <p v-if="result.medalEarned" class="mystery-widget__medal">
        🏅 Du fikk medaljen: <strong>{{ result.medalName }}</strong>!
      </p>
    </div>

    <div v-else-if="mystery.mysteryType === 'REAL_OR_FAKE' || mystery.questionText" class="mystery-widget__question">
      <p>{{ mystery.questionText || 'Er dette ekte eller falskt?' }}</p>
      <div class="mystery-widget__choices">
        <label><input v-model="answer" type="radio" value="REAL" /> Ekte</label>
        <label><input v-model="answer" type="radio" value="FAKE" /> Falsk</label>
      </div>
      <button
        class="mystery-widget__btn"
        :disabled="!answer || submitting"
        @click="submitAnswer"
      >
        {{ submitting ? 'Sender...' : 'Send svar' }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.mystery-widget {
  background: var(--color-surface, #fff);
  border: 2px solid var(--color-border, #e5e7eb);
  border-radius: 1rem;
  padding: 1.5rem;
  margin-top: 1.5rem;
}

.mystery-widget--loading,
.mystery-widget--empty {
  padding: 1rem;
  color: var(--color-text-muted, #6b7280);
}

.mystery-widget__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
}

.mystery-widget__badge {
  background: #fef3c7;
  color: #92400e;
  font-weight: 700;
  font-size: 0.75rem;
  padding: 0.25rem 0.75rem;
  border-radius: 999px;
}

.mystery-widget__rewards {
  font-size: 0.875rem;
  color: var(--color-text-muted, #6b7280);
}

.mystery-widget__title {
  font-size: 1.25rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
}

.mystery-widget__desc {
  color: var(--color-text-muted, #6b7280);
  margin-bottom: 1rem;
}

.mystery-widget__image {
  width: 100%;
  max-height: 480px;
  object-fit: contain;
  border-radius: 0.75rem;
  margin-bottom: 1rem;
  background: var(--color-surface-soft);
}

.mystery-widget__question p {
  margin-bottom: 0.5rem;
}

.mystery-widget__choices {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin: 0.75rem 0;
}

.mystery-widget__choices label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
}

.mystery-widget__btn {
  padding: 0.75rem 1.5rem;
  background: var(--color-primary, #4f46e5);
  color: white;
  border: none;
  border-radius: 0.75rem;
  font-weight: 700;
  cursor: pointer;
  font-family: inherit;
  font-size: 1rem;
  transition: opacity 0.15s;
}

.mystery-widget__btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.mystery-widget__result {
  padding: 1rem;
  border-radius: 0.75rem;
  margin-top: 1rem;
}

.mystery-widget__result--correct {
  background: color-mix(in srgb, var(--color-success, #22c55e) 15%, white);
  border: 1px solid var(--color-success, #22c55e);
}

.mystery-widget__result--wrong {
  background: color-mix(in srgb, var(--color-danger, #ef4444) 15%, white);
  border: 1px solid var(--color-danger, #ef4444);
}

.mystery-widget__comment {
  font-style: italic;
  margin-top: 0.5rem;
}

.mystery-widget__medal {
  margin-top: 0.75rem;
  font-size: 1.1rem;
}
</style>
