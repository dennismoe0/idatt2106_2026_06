<template>
  <section class="clue-riddle">
    <header class="clue-riddle__intro">
      <div class="clue-riddle__intro-copy">
        <p class="clue-riddle__label">Gåteoppgave</p>
        <h2>{{ task.title }}</h2>
        <p>{{ task.description }}</p>
      </div>
    </header>

    <div class="clue-riddle__story-grid">
      <section class="clue-riddle__why" aria-label="Hvorfor oppgaven er viktig">
        <div class="clue-riddle__section-head">
          <span class="clue-riddle__section-icon" aria-hidden="true">🕵️</span>
          <div class="clue-riddle__section-copy">
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
          <code class="clue-riddle__evidence-password">{{ evidencePassword ?? evidenceContext }}</code>
          <p class="clue-riddle__evidence-story">
            De fant passordet til brukeren, og det kan være koblet til noe personlig, som kafénavnet i saken.
          </p>
          <p class="clue-riddle__evidence-body">{{ evidenceContext }}</p>
        </div>
      </article>
    </div>

    <div class="clue-riddle__question">
      <div class="clue-riddle__question-head">
        <p class="clue-riddle__question-label">Hva forklarer passordet oss?</p>
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
  const password = String(content.value.evidencePassword ?? '').trim()
  return password || null
})
const evidenceContext = computed(() => {
  const evidence = String(content.value.evidence ?? '')
  if (evidence) return evidence
  return 'Analyser passordet og finn hva det avslører om kontoen.'
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
  gap: var(--space-6);
  --clue-riddle-intro-border: color-mix(in srgb, var(--color-primary) 20%, transparent);
  --clue-riddle-intro-glow: color-mix(in srgb, var(--color-primary) 8%, transparent);
  --clue-riddle-intro-spotlight: color-mix(in srgb, var(--color-primary) 18%, transparent);
  --clue-riddle-panel-shadow: color-mix(in srgb, var(--color-primary) 8%, transparent);
  --clue-riddle-evidence-border: color-mix(in srgb, var(--color-accent) 64%, var(--color-warning));
  --clue-riddle-evidence-shadow: color-mix(in srgb, var(--color-accent-dark) 12%, transparent);
  --clue-riddle-evidence-chip: color-mix(in srgb, var(--color-surface) 92%, transparent);
  --clue-riddle-evidence-card: color-mix(in srgb, var(--color-dossier-frame) 92%, black);
  --clue-riddle-evidence-note: color-mix(in srgb, var(--color-dossier-text-on-dark) 74%, transparent);
  --clue-riddle-evidence-password-bg: color-mix(in srgb, var(--color-dossier-white) 10%, transparent);
  --clue-riddle-option-shadow: color-mix(in srgb, var(--color-text) 5%, transparent);
  --clue-riddle-option-hover-shadow: color-mix(in srgb, var(--color-text) 8%, transparent);
  --clue-riddle-option-selected-shadow: color-mix(in srgb, var(--color-primary) 15%, transparent);
}

.clue-riddle__intro,
.clue-riddle__why,
.clue-riddle__evidence,
.clue-riddle__result {
  border-radius: var(--radius-lg);
  padding: var(--space-4);
}

.clue-riddle__intro {
  border: 2px solid var(--clue-riddle-intro-border);
  background:
    linear-gradient(120deg, color-mix(in srgb, var(--color-surface) 90%, transparent), color-mix(in srgb, var(--color-primary-soft) 70%, var(--color-surface))),
    radial-gradient(circle at top right, var(--clue-riddle-intro-spotlight), transparent 35%);
  box-shadow: 0 18px 32px var(--clue-riddle-panel-shadow);
  padding-block: var(--space-3);
}

.clue-riddle__intro-copy {
  max-width: 38rem;
}

.clue-riddle__label {
  margin: 0 0 var(--space-1);
  color: var(--color-primary-dark);
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

.clue-riddle h2 {
  margin-bottom: var(--space-2);
  font-size: clamp(1.5rem, 3vw, 2rem);
  line-height: 1.15;
}

.clue-riddle__story-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
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
  background: linear-gradient(180deg, var(--color-surface), var(--color-surface-soft));
}

.clue-riddle__section-copy {
  display: grid;
  gap: var(--space-2);
  max-width: 42rem;
}

.clue-riddle__section-copy h3 {
  margin-bottom: 0;
  font-size: clamp(1.1rem, 2vw, 1.35rem);
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
  background: var(--color-primary-soft-strong);
  font-size: 1.1rem;
}

.clue-riddle__evidence {
  border: 2px solid var(--clue-riddle-evidence-border);
  background: linear-gradient(180deg, var(--color-dossier-paper-top) 0%, var(--color-dossier-paper-soft) 100%);
  box-shadow: 0 14px 26px var(--clue-riddle-evidence-shadow);
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
  background: var(--clue-riddle-evidence-chip);
  color: var(--color-dossier-ink-muted);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.clue-riddle__evidence-head strong {
  color: var(--color-dossier-ink-soft);
  font-size: var(--text-sm);
}

.clue-riddle__evidence-card {
  display: grid;
  gap: var(--space-3);
  padding: var(--space-4);
  border-radius: var(--radius-md);
  background: var(--clue-riddle-evidence-card);
  color: var(--color-dossier-text-on-dark);
}

.clue-riddle__evidence-note {
  font-size: var(--text-xs);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--clue-riddle-evidence-note);
}

.clue-riddle__evidence-password {
  display: inline-block;
  width: fit-content;
  padding: 0.45rem 0.65rem;
  border-radius: 8px;
  background: var(--clue-riddle-evidence-password-bg);
  color: var(--color-dossier-white);
  font-size: clamp(1rem, 3vw, 1.3rem);
  font-weight: 800;
  letter-spacing: 0.04em;
}

.clue-riddle__evidence-story {
  max-width: 34rem;
  color: var(--color-dossier-white);
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
}

.clue-riddle__evidence-body {
  color: var(--color-dossier-text-on-dark);
  max-width: 34rem;
}

.clue-riddle__question {
  display: grid;
  gap: var(--space-4);
}

.clue-riddle__question-head {
  display: grid;
  gap: var(--space-2);
  max-width: 42rem;
}

.clue-riddle__question-label {
  margin: 0;
  color: var(--color-primary-dark);
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
  letter-spacing: 0.04em;
}

.clue-riddle__question-head h3 {
  margin-bottom: 0;
  color: var(--color-heading);
  font-size: clamp(1.4rem, 3vw, 1.9rem);
  line-height: 1.2;
}

.clue-riddle__question-head p {
  color: var(--color-text);
  font-size: var(--text-base);
  line-height: 1.6;
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
  background: linear-gradient(180deg, var(--color-surface), var(--color-bg));
  color: var(--color-text);
  text-align: left;
  cursor: pointer;
  box-shadow: 0 10px 18px var(--clue-riddle-option-shadow);
  transition: transform var(--transition-fast), box-shadow var(--transition-fast), border-color var(--transition-fast), background var(--transition-fast);
}

.clue-riddle__option strong {
  color: var(--color-text);
  font-size: var(--text-base);
  line-height: 1.45;
}

.clue-riddle__option:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 16px 24px var(--clue-riddle-option-hover-shadow);
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
  background: var(--color-border);
  color: var(--color-text);
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
}

.clue-riddle__option--selected {
  border-color: var(--color-primary);
  background: linear-gradient(180deg, var(--color-surface-soft), var(--color-primary-soft-strong));
  box-shadow: 0 18px 30px var(--clue-riddle-option-selected-shadow);
}

.clue-riddle__option--selected .clue-riddle__option-badge {
  background: var(--color-primary);
  color: var(--color-text-on-dark);
}

.clue-riddle__submit,
.clue-riddle__actions button {
  justify-self: start;
  min-height: 48px;
  padding: 0.85rem 1.35rem;
  border: 0;
  border-radius: var(--radius-md);
  background: var(--color-primary-dark);
  color: var(--color-text-on-dark);
  font-size: var(--text-base);
  font-weight: var(--font-bold);
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
  .clue-riddle__submit,
  .clue-riddle__actions button {
    width: 100%;
  }
}
</style>
