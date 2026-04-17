<template>
  <section class="task-card">
    <h2>Falske nyheter</h2>
    <p class="guidance">{{ task.guidanceText }}</p>

    <div class="articles">
      <article
        v-for="(article, index) in articles"
        :key="index"
        class="article-card"
        :class="{
          'article-card--answered': result,
          'article-card--correct':  result?.correct,
          'article-card--wrong':    result && !result.correct
        }"
      >
        <h3>{{ article.headline }}</h3>
        <p class="source">{{ article.source }}</p>
        <p>{{ article.body }}</p>

        <div class="actions">
          <button
            :class="{ selected: answers[`article_${index}`] === true }"
            :disabled="!!result"
            :aria-label="`Marker artikkel ${index + 1} som ekte`"
            @click="setAnswer(index, true)"
          >Ekte</button>
          <button
            :class="{ selected: answers[`article_${index}`] === false }"
            :disabled="!!result"
            :aria-label="`Marker artikkel ${index + 1} som falsk`"
            @click="setAnswer(index, false)"
          >Falsk</button>
        </div>
      </article>
    </div>

    <button v-if="!result" class="submit-btn" :disabled="!isReady" @click="submit">
      Send svar
    </button>

    <!-- Inline result — slides in below the articles -->
    <Transition name="result-slide">
      <div
        v-if="result"
        class="inline-result"
        :class="result.correct ? 'inline-result--correct' : 'inline-result--wrong'"
        role="status"
        aria-live="polite"
      >
        <p class="inline-result__label">
          {{ result.correct ? '✅ Riktig!' : '❌ Ikke helt riktig' }}
        </p>
        <p class="inline-result__explanation">{{ result.explanation }}</p>
        <p v-if="result.stopCompleted" class="inline-result__stop">
          🎉 Du fullførte stoppet!
        </p>
        <div class="inline-result__actions">
          <button class="next-btn" @click="$emit('next')">
            {{ isLastTask ? 'Tilbake til kart 🗺️' : 'Neste oppgave →' }}
          </button>
        </div>
      </div>
    </Transition>
  </section>
</template>

<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  task:       { type: Object,  required: true },
  result:     { type: Object,  default: null },
  isLastTask: { type: Boolean, default: false }
})

const emit = defineEmits(['submitted', 'next', 'backToMap'])

const answers = ref({})
const articles = computed(() => props.task?.contentJson?.articles ?? [])

watch(() => props.task?.id, () => { answers.value = {} }, { immediate: true })

const isReady = computed(() =>
  articles.value.length > 0 &&
  articles.value.every((_, i) => answers.value[`article_${i}`] !== undefined)
)

function setAnswer(index, value) {
  answers.value[`article_${index}`] = value
}

function submit() {
  if (!isReady.value) return
  console.log('[FakeNewsTask] Submitting answers:', answers.value)
  emit('submitted', { ...answers.value })
}
</script>

<style scoped>
.task-card {
  display: grid;
  gap: var(--space-4);
}

.guidance {
  margin: 0;
  color: var(--color-text-muted);
}

.articles {
  display: grid;
  gap: var(--space-4);
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
}

.article-card {
  border: 2px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  background: var(--color-surface);
  transition: border-color var(--transition-fast), background var(--transition-fast);
}
.article-card--correct {
  border-color: var(--color-success);
  background: var(--color-success-light);
}
.article-card--wrong {
  border-color: var(--color-danger);
  background: var(--color-danger-light);
}

.article-card h3 { margin-top: 0; }

.source {
  color: var(--color-text-muted);
  font-size: var(--text-sm);
}

.actions {
  display: flex;
  gap: var(--space-3);
  margin-top: var(--space-4);
}

button {
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  border-radius: var(--radius-md);
  padding: var(--space-1) var(--space-3);
  cursor: pointer;
  transition: background var(--transition-fast), border-color var(--transition-fast);
}
button:disabled { opacity: 0.5; cursor: not-allowed; }
button.selected {
  border-color: var(--color-primary);
  background: var(--color-primary-light);
  color: var(--color-primary-dark);
}

.submit-btn { justify-self: start; }
.submit-btn:disabled { opacity: 0.5; cursor: not-allowed; }

/* Inline result */
.inline-result {
  border-radius: var(--radius-lg);
  padding: var(--space-4) var(--space-5);
  display: grid;
  gap: var(--space-2);
}
.inline-result--correct {
  background: var(--color-success-light);
  border: 2px solid var(--color-success);
}
.inline-result--wrong {
  background: var(--color-danger-light);
  border: 2px solid var(--color-danger);
}

.inline-result__label {
  margin: 0;
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
}
.inline-result--correct .inline-result__label { color: var(--color-success); }
.inline-result--wrong   .inline-result__label { color: var(--color-danger); }

.inline-result__explanation {
  margin: 0;
  color: var(--color-text);
  line-height: 1.5;
}

.inline-result__stop {
  margin: 0;
  font-weight: var(--font-semibold);
  color: var(--color-success);
}

.inline-result__actions { padding-top: var(--space-2); }

.next-btn {
  background: var(--color-primary);
  color: var(--color-text-on-dark);
  border: none;
  border-radius: var(--radius-md);
  padding: var(--space-2) var(--space-5);
  font-weight: var(--font-semibold);
  font-size: var(--text-base);
  cursor: pointer;
  transition: background var(--transition-fast), transform var(--transition-fast);
}
.next-btn:hover  { background: var(--color-btn-primary-hover); }
.next-btn:active { transform: scale(0.98); }

/* Slide-in transition */
.result-slide-enter-active { transition: transform 0.3s ease, opacity 0.3s ease; }
.result-slide-leave-active { transition: transform 0.2s ease, opacity 0.2s ease; }
.result-slide-enter-from   { transform: translateY(-12px); opacity: 0; }
.result-slide-leave-to     { transform: translateY(-8px);  opacity: 0; }
</style>
