<template>
  <section class="clue-riddle">
    <header class="riddle-intro">
      <p class="riddle-kicker">Gåteoppgave</p>
      <h2>{{ task.contentJson?.question ?? 'Finn sporet' }}</h2>
      <p class="guidance">{{ task.guidanceText }}</p>
    </header>

    <section class="why-box" aria-label="Hvorfor du løser gåten">
      <h3>Hvorfor skal du løse dette?</h3>
      <p>{{ purpose }}</p>
    </section>

    <article class="evidence-card">
      <span class="evidence-label">Bevis</span>
      <p>{{ evidence }}</p>
    </article>

    <div class="options" role="list" aria-label="Svaralternativer">
      <button
        v-for="option in options"
        :key="option.id"
        class="option-btn"
        :class="{ 'option-btn--selected': selected === option.id }"
        :disabled="!!result"
        role="listitem"
        @click="selected = option.id"
      >
        {{ option.label }}
      </button>
    </div>

    <button v-if="!result" class="submit-btn" :disabled="!selected" @click="submit">
      Sjekk sporet
    </button>

    <Transition name="result-slide">
      <div
        v-if="result"
        class="riddle-result"
        :class="result.correct ? 'riddle-result--correct' : 'riddle-result--wrong'"
        role="status"
        aria-live="polite"
      >
        <h3>{{ result.correct ? 'Spor funnet!' : 'Ikke helt ennå' }}</h3>
        <p>{{ result.explanation }}</p>
        <button v-if="!result.correct" class="next-btn" @click="$emit('tryAgain')">
          Prøv igjen
        </button>
        <button v-else class="next-btn" @click="$emit('next')">
          {{ isLastTask ? 'Videre til sammendrag' : 'Neste oppgave' }}
        </button>
      </div>
    </Transition>
  </section>
</template>

<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  task: { type: Object, required: true },
  result: { type: Object, default: null },
  isLastTask: { type: Boolean, default: false }
})

const emit = defineEmits(['submitted', 'next', 'tryAgain', 'backToMap'])

const selected = ref('')
const purpose = computed(() => props.task?.contentJson?.purpose ?? '')
const evidence = computed(() => props.task?.contentJson?.evidence ?? '')
const options = computed(() => props.task?.contentJson?.options ?? [])

watch(() => props.task?.id, () => {
  selected.value = ''
}, { immediate: true })

watch(() => props.result, (value) => {
  if (!value) selected.value = ''
})

function submit() {
  if (!selected.value) return
  emit('submitted', { selected: selected.value })
}
</script>

<style scoped>
.clue-riddle {
  display: grid;
  gap: var(--space-4);
}

.riddle-intro,
.why-box,
.evidence-card,
.riddle-result {
  border-radius: var(--radius-lg);
  padding: var(--space-4);
}

.riddle-intro {
  border: 2px solid var(--color-primary);
  background: var(--color-primary-light);
}

.riddle-kicker {
  margin: 0 0 var(--space-1);
  color: var(--color-primary-dark);
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
  text-transform: uppercase;
}

.riddle-intro h2,
.why-box h3,
.riddle-result h3 {
  margin: 0 0 var(--space-2);
  color: var(--color-text);
}

.guidance,
.why-box p,
.evidence-card p,
.riddle-result p {
  margin: 0;
  line-height: 1.5;
}

.why-box {
  border: 1px solid var(--color-border);
  background: var(--color-surface);
}

.evidence-card {
  display: grid;
  gap: var(--space-2);
  border: 2px dashed var(--color-warning);
  background: var(--color-warning-light);
}

.evidence-label {
  width: fit-content;
  padding: var(--space-1) var(--space-2);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-text);
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
}

.options {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: var(--space-3);
}

.option-btn {
  min-height: 4rem;
  padding: var(--space-3);
  border: 2px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-surface);
  color: var(--color-text);
  font-weight: var(--font-semibold);
  cursor: pointer;
  text-align: left;
}

.option-btn--selected {
  border-color: var(--color-primary);
  background: var(--color-primary-light);
}

.option-btn:disabled,
.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.submit-btn,
.next-btn {
  justify-self: start;
  padding: var(--space-2) var(--space-5);
  border: 0;
  border-radius: var(--radius-md);
  background: var(--color-primary);
  color: var(--color-text-on-dark);
  font-weight: var(--font-semibold);
  cursor: pointer;
}

.riddle-result {
  display: grid;
  gap: var(--space-3);
}

.riddle-result--correct {
  border: 2px solid var(--color-success);
  background: var(--color-success-light);
}

.riddle-result--wrong {
  border: 2px solid var(--color-danger);
  background: var(--color-danger-light);
}

.result-slide-enter-active { transition: transform 0.3s ease, opacity 0.3s ease; }
.result-slide-leave-active { transition: transform 0.2s ease, opacity 0.2s ease; }
.result-slide-enter-from { transform: translateY(-12px); opacity: 0; }
.result-slide-leave-to { transform: translateY(-8px); opacity: 0; }
</style>
