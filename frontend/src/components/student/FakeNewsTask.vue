<template>
  <section class="fake-news-task">
    <p class="fake-news-task__guidance">{{ task.guidanceText }}</p>

    <p class="fake-news-task__instruction">🔍 Klikk på den artikkelen du tror er falsk</p>

    <div class="fake-news-task__articles">
      <article
        v-for="(article, index) in articles"
        :key="index"
        class="newspaper pinned-note article-card"
        :class="articleClass(index)"
        :style="`--card-rotate: ${cardRotation(index)}deg`"
        role="region"
        tabindex="0"
        :aria-label="`Velg denne artikkelen som falsk: ${article.headline}`"
        :aria-pressed="chosenIndex === index"
        :aria-disabled="!!result"
        @click="pickCard(index)"
        @keydown.enter.space.prevent="pickCard(index)"
      >
        <header class="newspaper__masthead" aria-hidden="true">
          <span class="newspaper__brand">{{ mastheadBrand(article) }}</span>
        </header>
        <h3 class="newspaper__headline">{{ article.headline }}</h3>
        <p class="newspaper__byline">Kilde: {{ article.source }}</p>
        <div class="newspaper__body">
          <p class="newspaper__lede">{{ article.body }}</p>
        </div>
      </article>
    </div>

    <!-- Feedback note -->
    <Transition name="result-slide">
      <div
        v-if="result"
        class="pinned-note fake-news-task__result"
        :class="result.correct ? 'fake-news-task__result--correct' : 'fake-news-task__result--wrong'"
        role="status"
        aria-live="polite"
        style="--card-rotate: 0.3deg"
      >
        <p class="fake-news-task__result-label">
          {{ result.correct ? '✅ Riktig!' : '❌ Ikke helt riktig' }}
        </p>
        <p class="fake-news-task__explanation">{{ result.explanation }}</p>
        <p v-if="result.stopCompleted" class="fake-news-task__stop-msg">🎉 Du fullførte stoppet!</p>
        <button class="next-btn" @click="$emit('next')">
          {{ isLastTask ? 'Videre til sammendrag →' : 'Neste oppgave →' }}
        </button>
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

const chosenIndex    = ref(null)
const shakingIndex   = ref(null)
const bouncingIndex  = ref(null)
const revealCorrect  = ref(null)

const articles = computed(() => props.task?.contentJson?.articles ?? [])

watch(() => props.task?.id, () => {
  chosenIndex.value   = null
  shakingIndex.value  = null
  bouncingIndex.value = null
  revealCorrect.value = null
}, { immediate: true })

watch(() => props.result, (r) => {
  if (!r) return
  if (r.correct) {
    bouncingIndex.value = chosenIndex.value
    setTimeout(() => { bouncingIndex.value = null }, 600)
  } else {
    shakingIndex.value = chosenIndex.value
    setTimeout(() => { shakingIndex.value = null; revealCorrect.value = getCorrectIndex(r) }, 400)
  }
})

function getCorrectIndex(r) {
  return r?.correctArticleIndex ?? null
}

function articleClass(index) {
  if (props.result) {
    const isChosen  = index === chosenIndex.value
    const isCorrect = index === revealCorrect.value || (props.result.correct && index === chosenIndex.value)
    if (isChosen && props.result.correct)   return ['article-card--correct', bouncingIndex.value === index ? 'card-bounce' : '']
    if (isChosen && !props.result.correct)  return ['article-card--wrong',   shakingIndex.value  === index ? 'card-shake'  : '']
    if (!props.result.correct && isCorrect) return ['article-card--correct']
    return ['article-card--muted']
  }
  if (index === chosenIndex.value) return ['article-card--chosen']
  return []
}

function cardRotation(index) {
  return index % 2 === 0 ? -0.5 : 0.4
}

function pickCard(index) {
  if (props.result) return
  chosenIndex.value = index
  const answer = {}
  articles.value.forEach((_, i) => {
    answer[`article_${i}`] = i !== index
  })
  console.log('[FakeNewsTask] Card picked index:', index, 'answer:', answer)
  emit('submitted', answer)
}

function mastheadBrand(article) {
  const src = article?.source ?? article?.domain ?? ''
  return extractDomainOrName(src)
}

function extractDomainOrName(input) {
  if (!input || typeof input !== 'string') return 'NYHETER'
  const s = input.trim()
  try {
    const url = s.includes('://') ? new URL(s) : new URL(`https://${s}`)
    const host = url.hostname.replace(/^www\./i, '')
    return host || s
  } catch {
    const m = s.match(/[A-Za-z0-9.-]+\.[A-Za-z]{2,}/)
    if (m && m[0]) return m[0].replace(/^www\./i, '')
    return s
  }
}
</script>

<style scoped>
.fake-news-task {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.fake-news-task__instruction {
  margin: 0;
  font-weight: 700;
  font-size: var(--text-base);
  color: var(--color-wood);
}

.fake-news-task__guidance {
  margin: 0;
  color: var(--color-cork-dark);
  font-weight: 600;
}

.fake-news-task__articles {
  display: grid;
  gap: var(--space-6);
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
}

.article-card {
  transform: rotate(var(--card-rotate, 0deg));
  cursor: pointer;
  transition: transform var(--transition-normal), box-shadow var(--transition-normal);
  user-select: none;
}
.article-card:hover:not([aria-disabled="true"]) {
  transform: rotate(0deg) scale(1.02) translateY(-3px);
  box-shadow: 4px 6px 16px rgba(0,0,0,0.35);
}
.article-card:focus-visible {
  outline: 3px solid var(--color-gold);
  outline-offset: 3px;
}

/* Result note */
.fake-news-task__result {
  transform: rotate(var(--card-rotate, 0deg));
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}
.fake-news-task__result--correct { border-color: var(--color-success); }
.fake-news-task__result--wrong   { border-color: var(--color-danger); }

.fake-news-task__result-label {
  margin: 0;
  font-size: var(--text-lg);
  font-weight: 700;
}
.fake-news-task__result--correct .fake-news-task__result-label { color: var(--color-success); }
.fake-news-task__result--wrong   .fake-news-task__result-label { color: var(--color-danger); }

.fake-news-task__explanation {
  margin: 0;
  font-size: var(--text-sm);
  color: var(--color-ink);
  line-height: 1.5;
}

.fake-news-task__stop-msg {
  margin: 0;
  font-weight: 600;
  color: var(--color-success);
}

.next-btn {
  align-self: flex-start;
  background: var(--color-wood);
  color: var(--color-gold);
  border: none;
  border-radius: var(--radius-md);
  padding: var(--space-2) var(--space-6);
  font-weight: 700;
  font-size: var(--text-base);
  cursor: pointer;
  min-height: 44px;
  transition: background var(--transition-fast), transform var(--transition-fast);
}
.next-btn:hover  { background: var(--color-wood-mid); }
.next-btn:active { transform: scale(0.98); }
.next-btn:focus-visible { outline: 3px solid var(--color-gold); outline-offset: 2px; }

/* ── Newspaper (70s) look for articles ───────────────────── */
.newspaper {
  --paper-bg: #f4efe2;
  --ink: #111;
  --masthead-ink: #0d0d0d;
  --rule: rgba(0,0,0,0.15);

  background: var(--paper-bg);
  border: 1.5px solid var(--rule);
  padding: clamp(12px, 1.5vw, 18px);
  color: var(--ink);
  box-shadow: 2px 3px 10px rgba(0,0,0,0.25);
  transform: rotate(var(--card-rotate, 0deg));
  max-width: 36ch;
}

.newspaper__masthead {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  border-bottom: 2px solid var(--rule);
  margin-bottom: 8px;
  letter-spacing: 0.5px;
}
.newspaper__brand {
  font-family: Georgia, "Times New Roman", serif;
  font-weight: 800;
  font-size: clamp(18px, 2.6vw, 22px);
  text-transform: uppercase;
  color: var(--masthead-ink);
}

.newspaper__headline {
  margin: 6px 0 4px 0;
  font-family: Georgia, "Times New Roman", serif;
  font-weight: 900;
  letter-spacing: 0.2px;
  line-height: 1.05;
  font-size: clamp(20px, 3.6vw, 28px);
  text-transform: uppercase;
}

.newspaper__byline {
  margin: 0 0 8px 0;
  font-size: 12px;
  opacity: 0.9;
  border-bottom: 1px solid var(--rule);
  padding-bottom: 6px;
}

.newspaper__body {
  column-count: 1;
  column-gap: 18px;
}
.newspaper__lede {
  margin: 10px 0 0 0;
  font-family: Georgia, serif;
  line-height: 1.35;
  font-size: 14px;
}

/* Preserve interaction visuals */
.newspaper.article-card:hover:not([aria-disabled="true"]) {
  transform: rotate(0deg) scale(1.02) translateY(-3px);
  box-shadow: 4px 6px 16px rgba(0,0,0,0.35);
}

</style>
