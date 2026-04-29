<template>
  <section class="fake-news-task">
    <p class="fake-news-task__guidance">{{ task.guidanceText }}</p>

    <p class="fake-news-task__instruction">🔍 Klikk på den artikkelen du tror er FALSK</p>

    <div class="fake-news-task__articles">
      <article
        v-for="(article, index) in articles"
        :key="index"
        class="newspaper-clipping article-card"
        data-peek-trigger
        :class="articleClass(index)"
        :style="`--card-rotate: ${cardRotation(index)}deg`"
        role="button"
        tabindex="0"
        :aria-label="`Velg denne artikkelen som ekte: ${article.headline}`"
        :aria-pressed="chosenIndex === index"
        :aria-disabled="!!result"
        @click="pickCard(index)"
        @keydown.enter.space.prevent="pickCard(index)"
      >
        <header class="newspaper-clipping__masthead" aria-hidden="true">
          <span class="newspaper-clipping__brand">{{ mastheadBrand(article).toUpperCase() }}</span>
          <span v-if="article.date" class="newspaper-clipping__edition">{{ formatEdition(article.date) }}</span>
        </header>
        <div class="newspaper-clipping__rule" aria-hidden="true"></div>

        <p class="newspaper-clipping__kicker">Nyheter</p>
        <h3 class="newspaper-clipping__headline">{{ article.headline }}</h3>
        <p v-if="article.ingress" class="newspaper-clipping__standfirst">{{ article.ingress }}</p>

        <p class="newspaper-clipping__byline">
          <span v-if="article.author">Av {{ article.author }}</span>
          <span v-if="article.author"> &middot; </span>
          <span>Kilde: {{ article.source }}</span>
        </p>

        <div class="newspaper-clipping__body">
          <p class="newspaper-clipping__lede">{{ article.body }}</p>
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
    revealCorrect.value = getCorrectIndex(r)
    setTimeout(() => { shakingIndex.value = null }, 400)
  }
})

function getCorrectIndex(r) {
  // Prefer an explicit index from the server (future-proof)
  if (typeof r?.correctArticleIndex === 'number') return r.correctArticleIndex
  // Fallback: the fake article has value false in the answer map
  return articles.value.findIndex((_, index) => r?.[`article_${index}`] === false)
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
  return index % 2 === 0 ? -0.4 : 0.3
}

function formatEdition(date) {
  if (!date || typeof date !== 'string') return ''
  const parts = date.split('-')
  if (parts.length !== 3) return date
  const months = ['jan', 'feb', 'mar', 'apr', 'mai', 'jun', 'jul', 'aug', 'sep', 'okt', 'nov', 'des']
  const year  = parts[0]
  const month = months[Math.max(0, Math.min(11, parseInt(parts[1], 10) - 1))] ?? ''
  const day   = String(parseInt(parts[2], 10))
  return `${day}. ${month}. ${year}`
}

function pickCard(index) {
  if (props.result) return
  chosenIndex.value = index
  const answer = {}
  articles.value.forEach((_, i) => {
    answer[`article_${i}`] = i !== index  // chosen card = false (fake), others = true (real)
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
  // Trim and safely decode percent-encoding (e.g., %20 -> space). Also convert '+' to space.
  let s = String(input).trim().replace(/\+/g, ' ')
  try {
    s = decodeURIComponent(s)
  } catch {
    // ignore decoding errors, keep original
  }
  // Collapse excessive whitespace
  s = s.replace(/\s+/g, ' ').trim()

  // If it looks like a URL or domain, extract the hostname (without www.)
  // Guard: require a dot in the hostname. Browsers (unlike Node) silently
  // percent-encode plain words — e.g. new URL("https://Trondheim kommune")
  // succeeds in Chrome with hostname "trondheim%20kommune". Rejecting any
  // hostname without a dot ensures plain source names fall through to the
  // final return-as-is path instead of being returned URL-encoded.
  try {
    const url = s.includes('://') ? new URL(s) : new URL(`https://${s}`)
    const host = url.hostname.replace(/^www\./i, '')
    if (host && host.includes('.')) return host
  } catch {
    // Not a URL — fall through
  }

  // If the string contains a domain somewhere inside (e.g., in text), extract it
  const m = s.match(/[A-Za-z0-9.-]+\.[A-Za-z]{2,}/)
  if (m && m[0]) return m[0].replace(/^www\./i, '')

  // Otherwise return the cleaned source name as-is (e.g., "Trondheim kommune", "ATB Pressemelding")
  return s
}
</script>

<style scoped>
.fake-news-task {
  --article-card-width: 500px;
  --article-grid-gap: var(--space-3);
  --article-grid-max: calc((var(--article-card-width) * 2) + var(--article-grid-gap));
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.fake-news-task__instruction {
  max-width: var(--article-grid-max);
  width: min(100%, 560px);
  margin: 0 auto;
  padding: 0;
  border: 0;
  border-radius: 0;
  background: transparent;
  color: var(--color-wood);
  font-size: clamp(1.17rem, 1.95vw, 1.36rem);
  font-weight: 700;
  line-height: 1.25;
  text-align: center;
  align-self: stretch;
  box-shadow: none;
}

.fake-news-task__guidance {
  max-width: var(--article-grid-max);
  width: min(100%, 620px);
  margin: 0 auto;
  padding: 0;
  border: 0;
  border-radius: 0;
  background: transparent;
  color: var(--color-cork-dark);
  font-size: clamp(1.27rem, 2.08vw, 1.46rem);
  font-weight: 700;
  line-height: 1.35;
  text-align: center;
  align-self: stretch;
  box-shadow: none;
}

.fake-news-task__articles {
  display: grid;
  gap: var(--article-grid-gap);
  grid-template-columns: repeat(2, minmax(0, var(--article-card-width)));
  justify-content: center;
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

/* Newspaper clipping card — replaces the old post-it look with a classic newspaper feel */
.newspaper-clipping {
  --paper-bg: #f3ecda;
  --paper-bg-2: #efe7d2;
  --ink: #1b1b1b;
  --ink-soft: rgba(27, 27, 27, 0.78);
  --rule: rgba(27, 27, 27, 0.85);
  --rule-soft: rgba(27, 27, 27, 0.45);

  background:
    radial-gradient(circle at 12% 0%, rgba(0,0,0,0.04), transparent 55%),
    radial-gradient(circle at 88% 100%, rgba(0,0,0,0.05), transparent 55%),
    linear-gradient(var(--paper-bg), var(--paper-bg-2));
  border: 1px solid rgba(27, 27, 27, 0.18);
  border-radius: 0;
  padding: clamp(16px, 2vw, 22px) clamp(16px, 2.2vw, 24px);
  color: var(--ink);
  box-shadow:
    0 1px 0 rgba(0,0,0,0.05),
    2px 3px 10px rgba(0,0,0,0.22);
  transform: rotate(var(--card-rotate, 0deg));
  width: 100%;
  max-width: 500px;
  overflow-wrap: anywhere;
  word-break: break-word;
}

/* Masthead: nameplate + edition info */
.newspaper-clipping__masthead {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: var(--space-2);
}
.newspaper-clipping__brand {
  font-family: "Playfair Display", Georgia, "Times New Roman", serif;
  font-weight: 900;
  font-size: clamp(20px, 2.6vw, 26px);
  letter-spacing: 0.5px;
  text-transform: uppercase;
  color: var(--ink);
}
.newspaper-clipping__edition {
  font-family: Georgia, "Times New Roman", serif;
  font-style: italic;
  font-size: 12px;
  color: var(--ink-soft);
  white-space: nowrap;
}

/* Classic double rule under the nameplate */
.newspaper-clipping__rule {
  margin: 6px 0 10px 0;
  border-top: 2px solid var(--rule);
  border-bottom: 1px solid var(--rule);
  height: 4px;
}

/* Small uppercase category line above headline */
.newspaper-clipping__kicker {
  margin: 0 0 4px 0;
  font-family: Georgia, "Times New Roman", serif;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 2.4px;
  text-transform: uppercase;
  color: var(--ink-soft);
}

.newspaper-clipping__headline {
  margin: 0 0 6px 0;
  font-family: "Playfair Display", Georgia, "Times New Roman", serif;
  font-weight: 800;
  letter-spacing: 0.1px;
  line-height: 1.15;
  font-size: clamp(20px, 2.4vw, 26px);
  color: var(--ink);
  overflow-wrap: anywhere;
  word-break: break-word;
  hyphens: auto;
}

/* Standfirst / dek (ingress) */
.newspaper-clipping__standfirst {
  margin: 0 0 8px 0;
  font-family: Georgia, "Times New Roman", serif;
  font-style: italic;
  font-weight: 600;
  font-size: 14.5px;
  line-height: 1.4;
  color: var(--ink);
}

/* Byline rule */
.newspaper-clipping__byline {
  margin: 0 0 10px 0;
  padding: 6px 0;
  border-top: 1px solid var(--rule-soft);
  border-bottom: 1px solid var(--rule-soft);
  font-family: Georgia, "Times New Roman", serif;
  font-size: 11.5px;
  letter-spacing: 0.4px;
  text-transform: uppercase;
  color: var(--ink-soft);
}

.newspaper-clipping__body {
  column-count: 1;
}
.newspaper-clipping__lede {
  margin: 0;
  font-family: Georgia, "Times New Roman", serif;
  font-size: 14px;
  line-height: 1.5;
  text-align: justify;
  hyphens: auto;
  color: var(--ink);
}
.newspaper-clipping__lede::first-letter {
  font-family: "Playfair Display", Georgia, "Times New Roman", serif;
  font-weight: 800;
  font-size: 2.4em;
  line-height: 0.9;
  float: left;
  margin: 4px 6px 0 0;
  color: var(--ink);
}

/* Preserve interaction visuals */
.newspaper-clipping.article-card:hover:not([aria-disabled="true"]) {
  transform: rotate(0deg) scale(1.02) translateY(-3px);
  box-shadow: 4px 6px 16px rgba(0,0,0,0.32);
}

/* Card state feedback, articleClass() returns these class names */
.newspaper-clipping.article-card--chosen {
  border-color: var(--color-wood);
  box-shadow: 0 0 0 3px var(--color-wood);
}
.newspaper-clipping.article-card--correct {
  border-color: var(--color-success);
  box-shadow: 0 0 0 3px var(--color-success);
}
.newspaper-clipping.article-card--wrong {
  border-color: var(--color-danger);
  box-shadow: 0 0 0 3px var(--color-danger);
}
.newspaper-clipping.article-card--muted {
  opacity: 0.55;
  filter: grayscale(30%);
}

@media (max-width: 900px) {
  .fake-news-task__instruction,
  .fake-news-task__guidance {
    margin: 0 auto;
    width: 100%;
  }

  .fake-news-task__articles {
    grid-template-columns: 1fr;
    justify-content: stretch;
  }
}

</style>
