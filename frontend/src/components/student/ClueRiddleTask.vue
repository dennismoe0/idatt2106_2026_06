<template>
  <section class="clue-riddle">
    <header class="clue-riddle__intro">
      <p class="clue-riddle__label">Gåteoppgave</p>
      <h2>{{ task.title }}</h2>
      <p>{{ task.description }}</p>
    </header>

    <section class="clue-riddle__why" aria-label="Hvorfor oppgaven er viktig">
      <h3>Hvorfor løser du dette?</h3>
      <p>{{ content.purpose }}</p>
    </section>

    <article class="clue-riddle__evidence">
      <span>Bevis</span>
      <p>{{ content.evidence }}</p>
    </article>

    <div class="clue-riddle__question">
      <h3>{{ content.question }}</h3>
      <div class="clue-riddle__options">
        <button
          v-for="option in options"
          :key="option.id"
          class="clue-riddle__option"
          :class="{ 'clue-riddle__option--selected': selected === option.id }"
          :disabled="!!result"
          @click="selected = option.id"
        >
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
        <p>{{ result.explanation }}</p>
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

.clue-riddle__intro,
.clue-riddle__why,
.clue-riddle__evidence,
.clue-riddle__result {
  border-radius: var(--radius-lg);
  padding: var(--space-4);
}

.clue-riddle__intro {
  border: 2px solid var(--color-primary);
  background: var(--color-primary-light);
}

.clue-riddle__label {
  margin: 0 0 var(--space-1);
  color: var(--color-primary-dark);
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
  text-transform: uppercase;
}

.clue-riddle h2,
.clue-riddle h3,
.clue-riddle p {
  margin-top: 0;
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
  background: var(--color-surface);
}

.clue-riddle__evidence {
  border: 2px dashed var(--color-warning);
  background: var(--color-warning-light);
}

.clue-riddle__evidence span {
  display: inline-block;
  margin-bottom: var(--space-2);
  padding: var(--space-1) var(--space-2);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-text);
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
}

.clue-riddle__question {
  display: grid;
  gap: var(--space-3);
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
  border-radius: var(--radius-md);
  background: var(--color-surface);
  color: var(--color-text);
  text-align: left;
  cursor: pointer;
}

.clue-riddle__option span {
  color: var(--color-text-muted);
  line-height: 1.4;
}

.clue-riddle__option--selected {
  border-color: var(--color-primary);
  background: var(--color-primary-light);
}

.clue-riddle__submit,
.clue-riddle__actions button {
  justify-self: start;
  padding: var(--space-2) var(--space-5);
  border: 0;
  border-radius: var(--radius-md);
  background: var(--color-primary);
  color: var(--color-text-on-dark);
  font-weight: var(--font-semibold);
  cursor: pointer;
}

.clue-riddle__submit:disabled,
.clue-riddle__option:disabled {
  opacity: 0.6;
  cursor: not-allowed;
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
</style>
