<template>
  <section class="mystery-task">
    <header class="mystery-hero">
      <p class="mystery-eyebrow">Datasenteret</p>
      <h2>Hvem tok pengene?</h2>
      <p>{{ intro }}</p>
    </header>

    <div class="clue-board" aria-label="Spor fra tidligere stopp">
      <article v-for="(clue, index) in clues" :key="clue.stop" class="clue-card">
        <span class="clue-number">{{ index + 1 }}</span>
        <div>
          <h3>{{ clue.stop }}</h3>
          <p class="clue-lesson">{{ clue.lesson }}</p>
          <p class="clue-text">{{ clue.clue }}</p>
        </div>
      </article>
    </div>

    <section class="witness-section" aria-labelledby="witness-title">
      <div class="section-heading">
        <p class="mystery-eyebrow">Vitneutsagn</p>
        <h3 id="witness-title">Les, sjekk og velg</h3>
      </div>

      <div class="suspect-grid">
        <article
          v-for="suspect in suspects"
          :key="suspect.id"
          class="suspect-card"
          :class="{
            'suspect-card--selected': selectedCulprit === suspect.id,
            'suspect-card--correct': result?.correct && suspect.culprit,
            'suspect-card--wrong': result && selectedCulprit === suspect.id && !result.correct
          }"
        >
          <img class="suspect-image" :src="suspect.image" :alt="suspect.name">
          <div class="suspect-content">
            <div class="suspect-title">
              <h4>{{ suspect.name }}</h4>
              <p>{{ suspect.role }}</p>
            </div>
            <p class="suspect-badge">{{ suspect.badge }}</p>
            <blockquote>{{ suspect.quote }}</blockquote>
            <div class="detective-check">
              <span>Etterforskerens sjekk</span>
              <p>{{ suspect.check }}</p>
            </div>
            <button
              class="choose-btn"
              :disabled="!!result"
              :aria-pressed="selectedCulprit === suspect.id"
              @click="selectedCulprit = suspect.id"
            >
              Velg {{ suspect.name }}
            </button>
          </div>
        </article>
      </div>
    </section>

    <button v-if="!result" class="submit-btn" :disabled="!selectedCulprit" @click="submit">
      Send inn løsning
    </button>

    <Transition name="result-slide">
      <div
        v-if="result"
        class="mystery-result"
        :class="result.correct ? 'mystery-result--correct' : 'mystery-result--wrong'"
        role="status"
        aria-live="polite"
      >
        <h3>{{ result.correct ? 'Riktig: Millie Mus' : 'Ikke helt. Prøv å sammenligne alle sporene igjen.' }}</h3>
        <p>{{ result.explanation }}</p>
        <p v-if="result.correct && motive" class="motive">{{ motive }}</p>
        <button v-if="!result.correct" class="next-btn" @click="$emit('tryAgain')">
          Velg på nytt
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
import { suspects } from '@/data/suspects'

const props = defineProps({
  task: { type: Object, required: true },
  result: { type: Object, default: null },
  isLastTask: { type: Boolean, default: false }
})

const emit = defineEmits(['submitted', 'next', 'tryAgain', 'backToMap'])

const selectedCulprit = ref('')
const intro = computed(() => props.task?.contentJson?.intro ?? 'Les sporene og finn tyven.')
const clues = computed(() => props.task?.contentJson?.clues ?? [])
const motive = computed(() => props.task?.contentJson?.motive ?? '')

watch(() => props.task?.id, () => {
  selectedCulprit.value = ''
}, { immediate: true })

function submit() {
  if (!selectedCulprit.value) return
  emit('submitted', { culprit: selectedCulprit.value })
}
</script>

<style scoped>
.mystery-task {
  display: grid;
  gap: var(--space-5);
}

.mystery-hero,
.witness-section {
  display: grid;
  gap: var(--space-3);
}

.mystery-hero {
  padding: var(--space-5);
  border: 2px solid var(--color-primary);
  border-radius: var(--radius-lg);
  background: var(--color-primary-light);
}

.mystery-eyebrow {
  margin: 0;
  color: var(--color-primary-dark);
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
  text-transform: uppercase;
}

.mystery-hero h2,
.section-heading h3 {
  margin: 0;
  color: var(--color-text);
}

.mystery-hero p:last-child,
.section-heading p {
  margin-bottom: 0;
}

.clue-board {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: var(--space-3);
}

.clue-card {
  display: grid;
  grid-template-columns: 2.25rem 1fr;
  gap: var(--space-3);
  padding: var(--space-3);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-surface);
}

.clue-number {
  display: grid;
  place-items: center;
  width: 2rem;
  height: 2rem;
  border-radius: var(--radius-full);
  background: var(--color-primary);
  color: var(--color-text-on-dark);
  font-weight: var(--font-bold);
}

.clue-card h3,
.clue-lesson,
.clue-text {
  margin: 0;
}

.clue-card h3 {
  font-size: var(--text-base);
}

.clue-lesson {
  color: var(--color-primary);
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
}

.clue-text {
  margin-top: var(--space-1);
  color: var(--color-text);
  line-height: 1.45;
}

.suspect-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: var(--space-4);
}

.suspect-card {
  display: grid;
  grid-template-columns: 7rem 1fr;
  gap: var(--space-3);
  padding: var(--space-3);
  border: 2px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-surface);
}

.suspect-card--selected {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--color-primary-light);
}

.suspect-card--correct {
  border-color: var(--color-success);
  background: var(--color-success-light);
}

.suspect-card--wrong {
  border-color: var(--color-danger);
  background: var(--color-danger-light);
}

.suspect-image {
  width: 7rem;
  height: 9rem;
  object-fit: cover;
  object-position: top center;
  border-radius: var(--radius-md);
  background: var(--color-bg);
}

.suspect-content {
  display: grid;
  gap: var(--space-2);
  min-width: 0;
}

.suspect-title h4,
.suspect-title p,
.suspect-badge,
blockquote,
.detective-check p {
  margin: 0;
}

.suspect-title h4 {
  color: var(--color-text);
  font-size: var(--text-lg);
}

.suspect-title p {
  color: var(--color-text-muted);
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
}

.suspect-badge {
  width: fit-content;
  padding: var(--space-1) var(--space-2);
  border-radius: var(--radius-sm);
  background: var(--color-bg);
  color: var(--color-primary-dark);
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
}

blockquote {
  padding: var(--space-2);
  border-left: 4px solid var(--color-primary);
  border-radius: var(--radius-sm);
  background: var(--color-bg);
  color: var(--color-text);
  line-height: 1.45;
}

.detective-check {
  display: grid;
  gap: var(--space-1);
  padding: var(--space-2);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
}

.detective-check span {
  color: var(--color-text);
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
}

.detective-check p {
  color: var(--color-text-muted);
  line-height: 1.4;
}

.choose-btn,
.submit-btn,
.next-btn {
  border: 0;
  border-radius: var(--radius-md);
  background: var(--color-primary);
  color: var(--color-text-on-dark);
  font-weight: var(--font-semibold);
  cursor: pointer;
}

.choose-btn {
  justify-self: start;
  padding: var(--space-2) var(--space-3);
}

.choose-btn:disabled,
.submit-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.submit-btn {
  justify-self: start;
  padding: var(--space-3) var(--space-6);
  font-size: var(--text-base);
}

.mystery-result {
  display: grid;
  gap: var(--space-3);
  padding: var(--space-4);
  border-radius: var(--radius-lg);
}

.mystery-result--correct {
  border: 2px solid var(--color-success);
  background: var(--color-success-light);
}

.mystery-result--wrong {
  border: 2px solid var(--color-danger);
  background: var(--color-danger-light);
}

.mystery-result h3,
.mystery-result p {
  margin: 0;
}

.motive {
  font-weight: var(--font-semibold);
}

.next-btn {
  justify-self: start;
  padding: var(--space-2) var(--space-5);
}

.result-slide-enter-active { transition: transform 0.3s ease, opacity 0.3s ease; }
.result-slide-leave-active { transition: transform 0.2s ease, opacity 0.2s ease; }
.result-slide-enter-from   { transform: translateY(-12px); opacity: 0; }
.result-slide-leave-to     { transform: translateY(-8px); opacity: 0; }

@media (max-width: 640px) {
  .suspect-card {
    grid-template-columns: 1fr;
  }

  .suspect-image {
    width: 100%;
    height: 14rem;
  }
}
</style>
