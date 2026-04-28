<template>
  <section class="clue-riddle">
    <header class="clue-riddle__intro">
      <p class="clue-riddle__label">Gåteoppgave</p>
      <h2>{{ task.title }}</h2>
      <p>{{ task.description }}</p>
    </header>

    <div class="clue-riddle__story-grid">
      <section class="clue-riddle__why" aria-label="Hvorfor oppgaven er viktig">
        <div class="clue-riddle__section-head">
          <span class="clue-riddle__section-icon" aria-hidden="true">🕵️</span>
          <div>
            <h3>Hvorfor løser du dette?</h3>
            <p>{{ content.purpose }}</p>
          </div>
        </div>
      </section>

      <article class="clue-riddle__evidence">
        <div class="clue-riddle__evidence-head">
          <span>Bevis</span>
          <strong>Reservekonto logg</strong>
        </div>
        <div class="clue-riddle__evidence-card">
          <p class="clue-riddle__evidence-note">Siste registrerte passordspor</p>
          <code class="clue-riddle__evidence-password">{{ evidencePassword }}</code>
          <p class="clue-riddle__evidence-body">{{ evidenceContext }}</p>
        </div>
      </article>
    </div>

    <div class="clue-riddle__question">
      <div class="clue-riddle__question-head">
        <h3>{{ content.question }}</h3>
        <p>Velg forklaringen som best kobler passordet til det digitale sporet.</p>
      </div>
      <div class="clue-riddle__options">
        <button
          v-for="option in options"
          :key="option.id"
          class="clue-riddle__option"
          :class="{ 'clue-riddle__option--selected': selected === option.id }"
          :disabled="!!result"
          :aria-pressed="selected === option.id"
          @click="selected = option.id"
        >
          <span class="clue-riddle__option-badge">{{ optionBadge(option.id) }}</span>
          <strong>{{ option.label }}</strong>
          <span v-if="option.detail">{{ option.detail }}</span>
        </button>
      </div>
    </div>

    <button v-if="!result" class="clue-riddle__submit" :disabled="!selected" @click="submit">
      Sjekk svaret
    </button>

    <Transition name="riddle-result">
      <div
        v-if="result"
        class="clue-riddle__result"
        :class="result.correct ? 'clue-riddle__result--correct' : 'clue-riddle__result--wrong'"
        role="status"
        aria-live="polite"
      >
        <h3>{{ result.correct ? 'Spor funnet!' : 'Ikke helt ennå' }}</h3>
        <p>{{ resultMessage }}</p>
        <div class="clue-riddle__actions">
          <button v-if="!result.correct" @click="$emit('tryAgain')">Prøv igjen</button>
          <button v-else @click="$emit('next')">
            {{ isLastTask ? 'Videre til sammendrag' : 'Neste oppgave' }}
          </button>
        </div>
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

const emit = defineEmits(['submitted', 'next', 'tryAgain'])

const selected = ref('')
const content = computed(() => props.task?.contentJson ?? {})
const options = computed(() => content.value.options ?? [])
const selectedOption = computed(() => options.value.find((option) => option.id === selected.value) ?? null)
const evidencePassword = computed(() => {
  const evidence = String(content.value.evidence ?? '')
  return evidence.match(/[A-Za-z][A-Za-z0-9!@#?&*_-]*\d[A-Za-z0-9!@#?&*_-]*/)?.[0] ?? evidence
})
const evidenceContext = computed(() => {
  const evidence = String(content.value.evidence ?? '')
  if (evidence === evidencePassword.value) return 'Analyser passordet og finn hva det avslører om kontoen.'
  return evidence.replace(evidencePassword.value, 'Dette passordet').trim()
})
const resultMessage = computed(() => {
  if (!props.result) return ''
  if (props.result.correct) return props.result.explanation
  return selectedOption.value?.detail
    ? `Ikke helt. ${selectedOption.value.detail}`
    : 'Ikke helt. Se etter hva i passordet som peker direkte mot stedet og rollen.'
})

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

function optionBadge(optionId) {
  const index = options.value.findIndex((option) => option.id === optionId)
  return index >= 0 ? String.fromCharCode(65 + index) : '?'
}
</script>

<style scoped>
.clue-riddle {
  display: grid;
  gap: var(--space-5);
}

.clue-riddle__intro,
.clue-riddle__why,
.clue-riddle__evidence,
.clue-riddle__result {
  border-radius: var(--radius-lg);
  padding: var(--space-4);
}

.clue-riddle__intro {
  border: 2px solid rgba(20, 73, 112, 0.2);
  background:
    linear-gradient(120deg, rgba(255, 255, 255, 0.9), rgba(228, 241, 255, 0.96)),
    radial-gradient(circle at top right, rgba(56, 189, 248, 0.18), transparent 35%);
  box-shadow: 0 18px 32px rgba(20, 73, 112, 0.08);
}

.clue-riddle__label {
  margin: 0 0 var(--space-1);
  color: #0f4c81;
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.clue-riddle h2,
.clue-riddle h3,
.clue-riddle p {
  margin-top: 0;
}

.clue-riddle__story-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(18rem, 22rem);
  gap: var(--space-4);
}

.clue-riddle__intro p,
.clue-riddle__why p,
.clue-riddle__evidence p,
.clue-riddle__result p {
  margin-bottom: 0;
  line-height: 1.55;
}

.clue-riddle__why {
  border: 1px solid var(--color-border);
  background: linear-gradient(180deg, #ffffff, #f8fbff);
}

.clue-riddle__section-head {
  display: flex;
  align-items: flex-start;
  gap: var(--space-3);
}

.clue-riddle__section-icon {
  display: inline-grid;
  place-items: center;
  width: 2.5rem;
  height: 2.5rem;
  border-radius: 999px;
  background: #dbeafe;
  font-size: 1.1rem;
}

.clue-riddle__evidence {
  border: 2px solid #d6a11d;
  background:
    linear-gradient(180deg, #fff6d8 0%, #fff0bf 100%);
  box-shadow: 0 14px 26px rgba(146, 88, 18, 0.12);
}

.clue-riddle__evidence-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-2);
  margin-bottom: var(--space-3);
}

.clue-riddle__evidence-head span {
  display: inline-flex;
  padding: 0.2rem 0.55rem;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.92);
  color: #6b4b00;
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.clue-riddle__evidence-head strong {
  color: #7c5300;
  font-size: var(--text-sm);
}

.clue-riddle__evidence-card {
  display: grid;
  gap: var(--space-2);
  padding: var(--space-3);
  border-radius: var(--radius-md);
  background: rgba(58, 35, 5, 0.92);
  color: #fff7d6;
}

.clue-riddle__evidence-note {
  font-size: var(--text-xs);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: rgba(255, 247, 214, 0.74);
}

.clue-riddle__evidence-password {
  display: inline-block;
  width: fit-content;
  padding: 0.45rem 0.65rem;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  font-size: clamp(1rem, 3vw, 1.3rem);
  font-weight: 800;
  letter-spacing: 0.04em;
}

.clue-riddle__evidence-body {
  color: rgba(255, 247, 214, 0.92);
}

.clue-riddle__question {
  display: grid;
  gap: var(--space-3);
}

.clue-riddle__question-head {
  display: grid;
  gap: var(--space-1);
}

.clue-riddle__question-head p {
  color: var(--color-text-muted);
}

.clue-riddle__options {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: var(--space-3);
}

.clue-riddle__option {
  min-height: 7rem;
  display: grid;
  align-content: start;
  gap: var(--space-2);
  padding: var(--space-3);
  border: 2px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: linear-gradient(180deg, #ffffff, #f8fafc);
  color: var(--color-text);
  text-align: left;
  cursor: pointer;
  box-shadow: 0 10px 18px rgba(15, 23, 42, 0.05);
  transition: transform var(--transition-fast), box-shadow var(--transition-fast), border-color var(--transition-fast), background var(--transition-fast);
}

.clue-riddle__option:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 16px 24px rgba(15, 23, 42, 0.08);
}

.clue-riddle__option span {
  color: var(--color-text-muted);
  line-height: 1.4;
}

.clue-riddle__option-badge {
  display: inline-grid;
  place-items: center;
  width: 2rem;
  height: 2rem;
  border-radius: 999px;
  background: #e2e8f0;
  color: #334155;
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
}

.clue-riddle__option--selected {
  border-color: #2563eb;
  background: linear-gradient(180deg, #eff6ff, #dbeafe);
  box-shadow: 0 18px 30px rgba(37, 99, 235, 0.15);
}

.clue-riddle__option--selected .clue-riddle__option-badge {
  background: #2563eb;
  color: #fff;
}

.clue-riddle__submit,
.clue-riddle__actions button {
  justify-self: start;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 44px;
  padding: var(--space-2) var(--space-6);
  border: none;
  border-radius: var(--radius-md);
  background: var(--color-primary);
  color: var(--color-text-on-dark);
  font-weight: var(--font-semibold);
  font-size: var(--text-base);
  cursor: pointer;
  transition: background var(--transition-fast), transform var(--transition-fast), opacity var(--transition-fast);
}

.clue-riddle__submit:disabled,
.clue-riddle__option:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.clue-riddle__submit:hover:not(:disabled),
.clue-riddle__actions button:hover:not(:disabled) {
  background: var(--color-btn-primary-hover);
}

.clue-riddle__submit:active:not(:disabled),
.clue-riddle__actions button:active:not(:disabled) {
  transform: scale(0.98);
}

.clue-riddle__result {
  display: grid;
  gap: var(--space-3);
}

.clue-riddle__result--correct {
  border: 2px solid var(--color-success);
  background: var(--color-success-light);
}

.clue-riddle__result--wrong {
  border: 2px solid var(--color-danger);
  background: var(--color-danger-light);
}

.clue-riddle__actions {
  display: flex;
  gap: var(--space-3);
}

.riddle-result-enter-active { transition: opacity 0.3s ease, transform 0.3s ease; }
.riddle-result-leave-active { transition: opacity 0.2s ease, transform 0.2s ease; }
.riddle-result-enter-from,
.riddle-result-leave-to { opacity: 0; transform: translateY(-10px); }

@media (max-width: 800px) {
  .clue-riddle__story-grid {
    grid-template-columns: 1fr;
  }
}
</style>
