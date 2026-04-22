<template>
  <section class="fake-news-task">
    <p class="fake-news-task__guidance">{{ task.guidanceText }}</p>

    <p class="fake-news-task__instruction">🔍 Klikk på den artikkelen du tror er <strong>falsk</strong></p>

    <div class="fake-news-task__articles">
      <article
        v-for="(article, index) in articles"
        :key="index"
        class="pinned-note article-card"
        :class="articleClass(index)"
        :style="`--card-rotate: ${cardRotation(index)}deg`"
        role="button"
        tabindex="0"
        :aria-label="`Velg denne artikkelen som falsk: ${article.headline}`"
        :aria-pressed="chosenIndex === index"
        :aria-disabled="!!result"
        @click="pickCard(index)"
        @keydown.enter.space.prevent="pickCard(index)"
      >
        <div v-if="shouldShowWarningChip(index)" class="article-card__warning-chip">
          Redaksjonell advarsel
        </div>

        <header class="article-card__header">
          <p class="article-card__source">{{ formatSourceName(article.source) }}</p>
          <div class="article-card__source-rule" aria-hidden="true"></div>
          <p class="article-card__meta">
            <span>{{ getPublishedLabel(article, index) }}</span>
            <span aria-hidden="true">•</span>
            <span>{{ getAuthorLabel(article, index) }}</span>
          </p>
        </header>

        <h3 class="article-card__headline">{{ article.headline }}</h3>
        <p class="article-card__body">{{ article.body }}</p>
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

const chosenIndex = ref(null)
const shakingIndex = ref(null)
const highlightedCorrectIndex = ref(null)
const revealCorrect = ref(null)

const articles = computed(() => props.task?.contentJson?.articles ?? [])

watch(() => props.task?.id, () => {
  chosenIndex.value = null
  shakingIndex.value = null
  highlightedCorrectIndex.value = null
  revealCorrect.value = null
}, { immediate: true })

watch(() => props.result, (r) => {
  if (!r) return

  const correctIndex = getCorrectIndex(r)
  revealCorrect.value = correctIndex

  if (r.correct) {
    highlightedCorrectIndex.value = chosenIndex.value
  } else {
    shakingIndex.value = chosenIndex.value
    setTimeout(() => {
      shakingIndex.value = null
      highlightedCorrectIndex.value = correctIndex
    }, 550)
  }
})

function getCorrectIndex(r) {
  if (typeof r?.correctArticleIndex === 'number') {
    return r.correctArticleIndex
  }

  return articles.value.findIndex((_, index) => r?.[`article_${index}`] === false)
}

function articleClass(index) {
  if (props.result) {
    const isChosen = index === chosenIndex.value
    const isCorrect = index === revealCorrect.value || (props.result.correct && index === chosenIndex.value)

    if (isChosen && props.result.correct) {
      return ['article-card--correct', highlightedCorrectIndex.value === index ? 'card-glow' : '']
    }

    if (isChosen && !props.result.correct) {
      return ['article-card--wrong', shakingIndex.value === index ? 'card-shake' : '']
    }

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

function shouldShowWarningChip(index) {
  return props.result && index === revealCorrect.value
}

function formatSourceName(source) {
  return String(source ?? 'Dagsposten').toUpperCase()
}

function getPublishedLabel(article, index) {
  const publishedDate = getArticleField(article, index, [
    'publishedAt',
    'published_at',
    'publishDate',
    'publish_date',
    'date',
    'published',
    'publicationDate'
  ])

  return publishedDate ? `Publisert ${publishedDate}` : `Publisert ${getFallbackPublishedDate(index)}`
}

function getAuthorLabel(article, index) {
  const authorName = getArticleField(article, index, [
    'author',
    'authorName',
    'author_name',
    'byline',
    'writer',
    'journalist'
  ])

  return authorName ? `Av ${authorName}` : `Av ${getFallbackAuthor(index)}`
}

function getArticleField(article, index, fieldNames) {
  const content = props.task?.contentJson ?? {}
  const articleMeta = Array.isArray(content.articleMeta) ? content.articleMeta[index] : null

  const candidateObjects = [
    article,
    article?.contentJson,
    article?.content_json,
    articleMeta,
    content
  ].filter(Boolean)

  for (const candidate of candidateObjects) {
    for (const fieldName of fieldNames) {
      const value = candidate[fieldName]
      if (typeof value === 'string' && value.trim()) {
        return value.trim()
      }
    }
  }

  return ''
}

function getFallbackPublishedDate(index) {
  const fallbackDates = ['i dag kl. 08:15', 'i går kl. 19:42', 'mandag kl. 07:30']
  return fallbackDates[index % fallbackDates.length]
}

function getFallbackAuthor(index) {
  const fallbackAuthors = ['Redaksjonen', 'Nora Nyheim', 'Emil Berg']
  return fallbackAuthors[index % fallbackAuthors.length]
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
  gap: var(--space-5);
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
}

.article-card {
  position: relative;
  overflow: hidden;
  transform: rotate(var(--card-rotate, 0deg));
  cursor: pointer;
  transition:
    transform var(--transition-normal),
    box-shadow var(--transition-normal),
    border-color var(--transition-normal),
    background var(--transition-normal);
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

.article-card--chosen {
  border-color: var(--color-wood);
  background: var(--color-note-chosen-bg);
  box-shadow: 0 12px 28px rgba(69, 52, 38, 0.18);
}
.article-card--correct {
  border-color: var(--color-success);
  background: var(--color-note-correct-bg);
  box-shadow:
    0 0 0 2px rgba(46, 160, 67, 0.18),
    0 14px 28px rgba(46, 160, 67, 0.2);
}
.article-card--wrong {
  border-color: var(--color-danger);
  background: var(--color-note-wrong-bg);
  box-shadow:
    0 0 0 2px rgba(193, 55, 74, 0.14),
    0 14px 28px rgba(193, 55, 74, 0.16);
}
.article-card--muted {
  opacity: 0.55;
  filter: grayscale(30%);
}

.article-card__warning-chip {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  margin-bottom: var(--space-3);
  padding: 0.35rem 0.7rem;
  border-radius: 999px;
  background: rgba(193, 55, 74, 0.12);
  color: var(--color-danger);
  font-size: var(--text-xs);
  font-weight: 800;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.article-card__header {
  margin-bottom: var(--space-3);
}

.article-card__source {
  margin: 0;
  font-size: 0.72rem;
  font-weight: 900;
  letter-spacing: 0.18em;
  color: var(--color-wood);
}

.article-card__source-rule {
  width: 100%;
  height: 2px;
  margin: var(--space-2) 0;
  background: linear-gradient(90deg, rgba(87, 63, 39, 0.95), rgba(87, 63, 39, 0.18));
}

.article-card__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 0.35rem;
  margin: 0;
  font-size: var(--text-xs);
  color: var(--color-ink-faint);
}

.article-card__headline {
  margin: 0 0 var(--space-2);
  font-size: var(--text-base);
  font-weight: 700;
  color: var(--color-wood);
  line-height: 1.3;
}
.article-card__body {
  margin: 0;
  font-size: var(--text-sm);
  color: var(--color-ink-subtle);
  line-height: 1.5;
}

.card-shake {
  animation: article-card-shake 0.55s ease-in-out;
}

.card-glow {
  animation: article-card-glow 1.6s ease-in-out infinite alternate;
}

@keyframes article-card-shake {
  0%, 100% { transform: rotate(var(--card-rotate, 0deg)) translateX(0); }
  18% { transform: rotate(calc(var(--card-rotate, 0deg) - 1deg)) translateX(-7px); }
  36% { transform: rotate(calc(var(--card-rotate, 0deg) + 1deg)) translateX(8px); }
  54% { transform: rotate(calc(var(--card-rotate, 0deg) - 0.7deg)) translateX(-6px); }
  72% { transform: rotate(calc(var(--card-rotate, 0deg) + 0.6deg)) translateX(5px); }
}

@keyframes article-card-glow {
  from {
    box-shadow:
      0 0 0 2px rgba(46, 160, 67, 0.14),
      0 0 0 0 rgba(46, 160, 67, 0.1),
      0 14px 28px rgba(46, 160, 67, 0.16);
  }
  to {
    box-shadow:
      0 0 0 2px rgba(46, 160, 67, 0.26),
      0 0 24px 8px rgba(46, 160, 67, 0.22),
      0 18px 34px rgba(46, 160, 67, 0.28);
  }
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

/* Slide-in */
.result-slide-enter-active { transition: transform 0.3s ease, opacity 0.3s ease; }
.result-slide-leave-active { transition: transform 0.2s ease, opacity 0.2s ease; }
.result-slide-enter-from   { transform: translateY(-12px); opacity: 0; }
.result-slide-leave-to     { transform: translateY(-8px);  opacity: 0; }
</style>
