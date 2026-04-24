<template>
  <section class="learn-task">
    <template v-if="phase === 'LEARN'">
      <div class="learn-shell">
        <header class="learn-hero">
          <div class="learn-hero__intro">
            <p class="learn-hero__eyebrow">Briefing fra borgermesteren</p>
            <h2 class="learn-hero__title">Før du starter oppgaven</h2>
            <div class="learn-hero__briefing">
              <p class="learn-hero__briefing-label">Borgermesteren sier</p>
              <p class="learn-hero__lead">
                {{ primaryInstruction }}
              </p>
            </div>
            <p class="learn-hero__body">
              {{ task.guidanceText }}
            </p>
            <p class="learn-hero__body">
              Les forklaringene under først. Deretter gjør du oppgaven under og viser at du har forstått hva du skal se etter.
            </p>
          </div>
          <div class="learn-hero__mayor" aria-hidden="true">
            <img :src="mayorImage" alt="" class="learn-hero__mayor-image" />
          </div>
        </header>

        <div class="learn-sections">
          <article
            v-if="currentSlide"
            :key="`${task.id}-slide-${currentSlideIndex}`"
            class="slide"
          >
            <div class="slide__header">
              <span class="slide__step">Del {{ currentSlideIndex + 1 }} av {{ slides.length }}</span>
            <span v-if="currentSlide.icon" class="slide__icon" aria-hidden="true">{{ currentSlide.icon }}</span>
            </div>
            <h3 class="slide__heading">{{ currentSlide.heading }}</h3>
            <p class="slide__body">{{ currentSlide.body }}</p>

            <div v-if="currentSlide.examples?.length" class="slide__examples">
              <p class="slide__examples-title">Slik kan det se ut i virkeligheten</p>
              <div v-if="task.stopTheme === 'FAKE_NEWS' && currentSlide.exampleType === 'Nyhetsartikkel'" class="news-examples">
                <article class="news-example">
                  <p class="news-example__label">{{ currentSlide.exampleType }}</p>
                  <h4 class="news-example__headline">{{ buildNewsExample(currentSlide.examples).headline }}</h4>
                  <p v-if="buildNewsExample(currentSlide.examples).body" class="news-example__body">
                    {{ buildNewsExample(currentSlide.examples).body }}
                  </p>
                </article>
                <div v-if="buildNewsExample(currentSlide.examples).comments.length" class="news-comments">
                  <p class="news-comments__title">Kommentar</p>
                  <ul class="news-comments__list">
                    <li
                      v-for="comment in buildNewsExample(currentSlide.examples).comments"
                      :key="comment"
                      class="news-comments__item"
                    >
                      {{ comment }}
                    </li>
                  </ul>
                </div>
              </div>
              <div v-else-if="task.stopTheme === 'FAKE_NEWS'" class="typed-examples">
                <p class="typed-examples__label">{{ currentSlide.exampleType || 'Eksempel' }}</p>
                <ul class="slide__list">
                  <li v-for="example in currentSlide.examples" :key="example" class="slide__list-item">
                    {{ example }}
                  </li>
                </ul>
              </div>
              <div v-else-if="marketplaceVisualFor(currentSlideIndex)" class="marketplace-visual">
                <div v-if="marketplaceVisualFor(currentSlideIndex).shop" class="marketplace-visual__frame">
                  <FakeWebshop
                    v-bind="marketplaceVisualFor(currentSlideIndex).shop"
                    :clickable-elements="marketplaceVisualFor(currentSlideIndex).callouts.map((callout) => ({ id: callout.key, label: '', isSuspicious: true, explanation: callout.text }))"
                    :flagged-elements="new Set()"
                    :feedback-states="Object.fromEntries(marketplaceVisualFor(currentSlideIndex).callouts.map((callout) => [callout.key, 'focus']))"
                    :field-markers="Object.fromEntries(marketplaceVisualFor(currentSlideIndex).callouts.map((callout, calloutIndex) => [callout.key, String(calloutIndex + 1)]))"
                    :disabled="true"
                  />
                </div>

                <div v-if="marketplaceVisualFor(currentSlideIndex).callouts" class="marketplace-compare">
                  <article
                    v-for="callout in marketplaceVisualFor(currentSlideIndex).callouts"
                    :key="callout.key"
                    class="marketplace-compare__card marketplace-compare__card--risky"
                    :class="`marketplace-compare__card--${callout.key}`"
                  >
                    <p class="marketplace-compare__title">
                      <span class="marketplace-compare__marker">{{ callout.marker }}</span>
                      {{ callout.title }}
                    </p>
                    <p class="marketplace-compare__body">{{ callout.text }}</p>
                  </article>
                </div>

                <div v-else-if="marketplaceVisualFor(currentSlideIndex).comparisons" class="marketplace-compare">
                  <article
                    v-for="item in marketplaceVisualFor(currentSlideIndex).comparisons"
                    :key="item.key"
                    class="marketplace-compare__card"
                    :class="`marketplace-compare__card--${item.tone}`"
                  >
                    <p class="marketplace-compare__eyebrow">{{ item.label }}</p>
                    <p class="marketplace-compare__title">{{ item.title }}</p>
                    <p class="marketplace-compare__value">{{ item.value }}</p>
                    <p class="marketplace-compare__body">{{ item.body }}</p>
                  </article>
                </div>
              </div>
              <ul v-else class="slide__list">
                <li v-for="example in currentSlide.examples" :key="example" class="slide__list-item">
                  {{ example }}
                </li>
              </ul>
            </div>

            <div v-if="currentSlide.checks?.length" class="slide__notes">
              <div
                v-for="check in currentSlide.checks"
                :key="check"
                class="slide__note"
              >
                <span class="slide__note-pin" aria-hidden="true" />
                {{ check }}
              </div>
            </div>

            <div v-if="currentQuestion" class="inline-question" :class="questionClass(currentSlideIndex)">
              <div class="inline-question__content">
                <p class="inline-question__eyebrow">Sjekk at du forstår del {{ currentSlideIndex + 1 }}</p>
                <p class="inline-question__text">{{ currentQuestion.question }}</p>

                <Transition name="feedback-pop">
                  <p
                    v-if="questionStateFor(currentSlideIndex) === 'CORRECT'"
                    class="question__feedback question__feedback--correct"
                  >
                    Riktig!
                  </p>
                  <p
                    v-else-if="questionStateFor(currentSlideIndex) === 'WRONG'"
                    class="question__feedback question__feedback--wrong"
                  >
                    Prøv igjen
                  </p>
                </Transition>
              </div>

              <div class="inline-question__footer">
                <div class="question__options" role="group" :aria-label="currentQuestion.question">
                  <button
                    v-for="opt in currentQuestion.options"
                    :key="opt"
                    class="option-btn"
                    :class="optionClass(opt, currentSlideIndex)"
                    :disabled="questionStateFor(currentSlideIndex) === 'CORRECT'"
                    @click="submitAnswer(opt, currentSlideIndex)"
                  >
                    {{ opt }}
                  </button>
                </div>
              </div>
            </div>

            <div class="slide-nav">
              <button
                type="button"
                class="nav-btn nav-btn--secondary"
                :disabled="currentSlideIndex === 0"
                @click="goToPreviousSlide"
              >
                ← Forrige del
              </button>

              <button
                v-if="!isLastSlide"
                type="button"
                class="nav-btn nav-btn--primary"
                :disabled="!canAdvanceFromCurrentSlide"
                @click="goToNextSlide"
              >
                Neste del →
              </button>
            </div>
          </article>
        </div>

        <div
          v-if="isLastSlide"
          class="learn-complete"
          :class="{ 'learn-complete--locked': !allQuestionsCorrect }"
        >
          <div>
            <p class="learn-complete__title">{{ allQuestionsCorrect ? 'Bra jobbet, detektiv!' : 'Fullfør læringssjekken' }}</p>
            <p class="learn-complete__body">
              {{ allQuestionsCorrect
                ? 'Du har svart riktig på alle spørsmålene og er klar for neste del av saken.'
                : 'Svar riktig på spørsmålene under hver del for å låse opp neste oppgave.'
              }}
            </p>
          </div>
          <button
            class="nav-btn nav-btn--learn-next"
            :disabled="!allQuestionsCorrect || !result?.correct"
            @click="$emit('next')"
          >
            {{ nextButtonLabel }}
          </button>
        </div>
      </div>
    </template>

  </section>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import FakeWebshop from '@/components/student/FakeWebshop.vue'

const props = defineProps({
  task:       { type: Object,  required: true },
  result:     { type: Object,  default: null },
  isLastTask: { type: Boolean, default: false },
})
const emit = defineEmits(['submitted', 'next'])

const content = computed(() => props.task?.contentJson ?? {})
const slides  = computed(() => content.value.slides ?? [])
const quiz    = computed(() => content.value.quiz   ?? [])

const THEME_INSTRUCTIONS = {
  FAKE_NEWS: 'Nå skal vi lære om falske nyheter. Les forklaringene nøye og gjør oppgaven under etterpå.',
  PHISHING_EMAIL: 'Nå skal vi lære om phishing. Se etter hva som avslører en falsk melding før du gjør oppgaven under.',
  AI_PHOTO: 'Nå skal vi lære om ekte, manipulerte og KI-lagde bilder. Se nøye på detaljene før du gjør oppgaven under.',
  PASSWORD: 'Nå skal vi lære om passord. Finn ut hva som gjør et passord svakt eller sterkt før du gjør oppgaven under.',
  MARKETPLACE: 'Nå skal vi lære om nettsvindel. Se hvordan falske butikker prøver å lure deg før du gjør oppgaven under.',
  SOCIAL_MEDIA: 'Nå skal vi lære om sosiale medier og manipulasjon. Les hvordan rykter og falske kontoer fungerer før du gjør oppgaven under.',
}

const phase      = ref('LEARN')
const submittedOnce = ref(false)
const currentSlideIndex = ref(0)

// 'UNANSWERED' | 'CORRECT' | 'WRONG'
const mayorImage = '/story_pictures/mayor-guide.png'
const primaryInstruction = computed(() => THEME_INSTRUCTIONS[props.task?.stopTheme] ?? 'Les forklaringene under og gjør oppgaven etterpå.')

const questionStates = ref({})
const selectedAnswers = ref({})
const currentSlide = computed(() => slides.value[currentSlideIndex.value] ?? null)
const currentQuestion = computed(() => questionForSlide(currentSlideIndex.value))
const isLastSlide = computed(() => currentSlideIndex.value === slides.value.length - 1)
const canAdvanceFromCurrentSlide = computed(() => {
  if (!currentQuestion.value) return true
  return questionStateFor(currentSlideIndex.value) === 'CORRECT'
})
const allQuestionsCorrect = computed(() => quiz.value.length > 0
  && quiz.value.every((_, index) => questionStateFor(index) === 'CORRECT'))
const nextButtonLabel = computed(() => {
  if (!allQuestionsCorrect.value) return 'Svar på alle spørsmål først'
  if (!props.result?.correct) return 'Lagrer...'
  return props.isLastTask ? 'Se oppsummering →' : 'Neste oppgave →'
})

const MARKETPLACE_VISUAL_EXAMPLES = {
  0: {
    shop: {
      siteName: 'sneaker-blitz.shop',
      eyebrow: 'Bare i dag',
      headline: 'Nike Air Max til 299 kr',
      tagline: 'Salget slutter om 10 minutter. Bestill før det er for sent.',
      productName: 'Nike Air Max 270',
      price: '299 kr',
      originalPrice: '2 599 kr',
      badges: ['90 % rabatt', 'Kun få igjen'],
      paymentText: 'Kun gavekort eller bankoverføring',
      contactText: 'Kontakt oss via DM på ShopChat',
      returnPolicyText: 'Ingen retur på kampanjevarer',
      sellerText: 'Solgt av Sneaker Blitz Global',
      shippingText: 'Sendes i dag hvis du bestiller nå',
      ratingText: '4.9 av 5 stjerner',
      notice: 'Vær ekstra forsiktig når en butikk prøver å få deg til å skynde deg.',
      ctaText: 'Kjøp nå',
    },
    callouts: [
      {
        key: 'domain',
        marker: '1',
        title: 'Ukjent nettadresse',
        text: 'Nettbutikken bruker et rart domenenavn som ikke ligner på en kjent norsk butikk.',
      },
      {
        key: 'price',
        marker: '2',
        title: 'For godt til å være sant',
        text: 'Kjempestor rabatt og “kun få igjen” prøver å få deg til å skynde deg.',
      },
      {
        key: 'payment',
        marker: '3',
        title: 'Utrygg betaling',
        text: 'Gavekort og bankoverføring gjør det vanskelig å få hjelp hvis butikken er falsk.',
      },
    ],
  },
  1: {
    comparisons: [
      {
        key: 'domain-safe',
        label: 'Ser mer troverdig ut',
        tone: 'safe',
        title: 'Nettadresse',
        value: 'komplett.no  •  elkjop.no',
        body: 'Kjente butikker bruker ofte korte, tydelige domenenavn som passer med navnet på butikken.',
      },
      {
        key: 'domain-risky',
        label: 'Bør sjekkes ekstra nøye',
        tone: 'risky',
        title: 'Nettadresse',
        value: 'billig-ps5.cc  •  supertilbud-now.xyz',
        body: 'Rare endelser og veldig “billig nå!”-navn er vanlige faresignaler i svindelbutikker.',
      },
      {
        key: 'payment-safe',
        label: 'Tryggere løsning',
        tone: 'safe',
        title: 'Betaling',
        value: 'Kort, Vipps eller kjent checkout',
        body: 'Vanlige betalingsløsninger gjør det lettere å klage eller få hjelp hvis noe går galt.',
      },
      {
        key: 'payment-risky',
        label: 'Mistenkelig løsning',
        tone: 'risky',
        title: 'Betaling',
        value: 'Send gavekortkode eller betal til privat konto',
        body: 'Svindlere liker betalingsmåter som er vanskelige å spore og nesten umulige å få tilbake.',
      },
    ],
  },
}

watch(() => props.task?.id, () => {
  phase.value         = 'LEARN'
  submittedOnce.value = false
  currentSlideIndex.value = 0
  questionStates.value = {}
  selectedAnswers.value = {}
}, { immediate: true })

function questionForSlide(index) {
  return quiz.value[index] ?? null
}

function questionStateFor(index) {
  return questionStates.value[index] ?? 'UNANSWERED'
}

function questionClass(index) {
  return {
    'question--correct': questionStateFor(index) === 'CORRECT',
    'question--wrong':   questionStateFor(index) === 'WRONG',
  }
}

function optionClass(opt, index) {
  const state = questionStateFor(index)
  if (state === 'UNANSWERED') return {}
  const question = questionForSlide(index)
  const isSelected = opt === selectedAnswers.value[index]
  const isCorrect  = opt === question?.correct
  if (state === 'CORRECT') return { 'option-btn--correct': isSelected }
  return {
    'option-btn--wrong':   isSelected && !isCorrect,
    'option-btn--correct': isCorrect,
  }
}

function submitAnswer(opt, index) {
  if (questionStateFor(index) === 'CORRECT') return
  const question = questionForSlide(index)
  if (!question) return

  selectedAnswers.value = { ...selectedAnswers.value, [index]: opt }
  const correct = opt === question.correct
  questionStates.value = { ...questionStates.value, [index]: correct ? 'CORRECT' : 'WRONG' }
  console.log('[LearningTask] Quiz q', index, '— answered:', opt, '— correct:', correct)

  if (correct) {
    setTimeout(completeIfAllCorrect, 900)
  } else {
    setTimeout(() => resetQuestion(index), 1200)
  }
}

function resetQuestion(index) {
  selectedAnswers.value = { ...selectedAnswers.value, [index]: null }
  questionStates.value = { ...questionStates.value, [index]: 'UNANSWERED' }
}

function goToPreviousSlide() {
  if (currentSlideIndex.value === 0) return
  currentSlideIndex.value -= 1
}

function goToNextSlide() {
  if (!canAdvanceFromCurrentSlide.value || isLastSlide.value) return
  currentSlideIndex.value += 1
}

function splitExample(example) {
  const [contentPart, commentPart] = String(example).split('||').map((part) => part.trim())
  return {
    content: contentPart || String(example).trim(),
    comment: commentPart || '',
  }
}

function buildNewsExample(examples = []) {
  const parsed = examples.map(splitExample)
  const headlineExample = parsed.find((item) => !item.content.startsWith('"')) ?? parsed[0]
  const bodyExample = parsed.find((item) => item.content.startsWith('"') && item.content.endsWith('"'))
  const comments = parsed.map((item) => item.comment).filter(Boolean)

  return {
    headline: headlineExample?.content ?? '',
    body: bodyExample?.content ? bodyExample.content.slice(1, -1) : '',
    comments,
  }
}

function marketplaceVisualFor(index) {
  return props.task?.stopTheme === 'MARKETPLACE' ? MARKETPLACE_VISUAL_EXAMPLES[index] ?? null : null
}

function completeIfAllCorrect() {
  if (allQuestionsCorrect.value && !submittedOnce.value) {
    submittedOnce.value = true
    console.log('[LearningTask] All quiz questions passed — submitting')
    emit('submitted', { quizPassed: true })
  }
}
</script>

<style scoped>
.learn-task {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.learn-shell,
.quiz-shell {
  width: min(100%, 860px);
  margin: 0 auto;
}

.learn-shell {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.learn-hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 220px;
  gap: var(--space-5);
  align-items: end;
  padding-bottom: var(--space-4);
  border-bottom: 1px solid var(--color-border);
}

.learn-hero__intro {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.learn-hero__eyebrow {
  margin: 0;
  font-size: var(--text-xs);
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--color-primary);
}

.learn-hero__lead {
  margin: 0;
  font-size: var(--text-lg);
  font-weight: 700;
  color: var(--color-heading);
  line-height: 1.5;
}

.learn-hero__briefing {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  background: var(--color-primary-soft);
  border: 1px solid var(--color-primary-soft-strong);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
}

.learn-hero__briefing::after {
  content: '';
  position: absolute;
  right: -12px;
  bottom: 22px;
  width: 22px;
  height: 22px;
  background: var(--color-primary-soft);
  border-right: 1px solid var(--color-primary-soft-strong);
  border-bottom: 1px solid var(--color-primary-soft-strong);
  transform: rotate(-45deg);
}

.learn-hero__briefing-label {
  margin: 0;
  font-size: var(--text-xs);
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--color-primary);
}

.learn-hero__title {
  margin: 0;
  font-size: var(--text-2xl);
  font-weight: 800;
  color: var(--color-heading);
  line-height: 1.2;
}

.learn-hero__body {
  margin: 0;
  font-size: var(--text-base);
  color: var(--color-text);
  line-height: 1.7;
}

.learn-hero__mayor {
  display: flex;
  justify-content: center;
  align-items: flex-end;
}

.learn-hero__mayor-image {
  width: min(100%, 210px);
  height: auto;
  display: block;
}

.learn-sections {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

/* ── Slide ── */
.slide {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.slide__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
}

.slide__step {
  font-size: var(--text-xs);
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--color-primary);
}

.slide__icon {
  font-size: 2rem;
  line-height: 1;
}

.slide__heading {
  margin: 0;
  font-size: var(--text-xl);
  font-weight: 700;
  color: var(--color-heading);
  line-height: 1.3;
}

.slide__body {
  margin: 0;
  font-size: var(--text-base);
  color: var(--color-text);
  line-height: 1.75;
}

.slide__examples {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.news-examples {
  display: grid;
  gap: var(--space-3);
}

.typed-examples {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  padding: var(--space-4);
  border-left: 4px solid var(--color-primary);
  background: var(--color-surface-soft-alt);
  border-radius: var(--radius-md);
}

.typed-examples__label {
  margin: 0;
  font-size: var(--text-xs);
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--color-primary);
}

.marketplace-visual {
  display: grid;
  gap: var(--space-3);
}

.marketplace-compare {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-3);
}

.marketplace-compare__card {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  padding: var(--space-4);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  box-shadow: var(--shadow-sm);
}

.marketplace-compare__card--safe {
  background: color-mix(in srgb, var(--color-success-light) 45%, white);
  border-color: var(--color-success);
}

.marketplace-compare__card--risky {
  background: color-mix(in srgb, var(--color-warning-light) 55%, white);
  border-color: var(--color-accent);
}

.marketplace-compare__eyebrow {
  margin: 0;
  font-size: var(--text-xs);
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--color-text-muted);
}

.marketplace-compare__title {
  margin: 0;
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-sm);
  font-weight: 800;
  color: var(--color-heading);
}

.marketplace-compare__marker {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 1.5rem;
  height: 1.5rem;
  border-radius: var(--radius-full);
  background: var(--color-accent);
  color: var(--color-text-on-dark);
  font-size: var(--text-xs);
  font-weight: 800;
  line-height: 1;
  flex-shrink: 0;
}

.marketplace-compare__value {
  margin: 0;
  font-size: var(--text-base);
  font-weight: 700;
  line-height: 1.45;
  color: var(--color-heading);
}

.marketplace-compare__body {
  margin: 0;
  font-size: var(--text-sm);
  line-height: 1.55;
  color: var(--color-text);
}

.marketplace-visual__frame {
  position: relative;
  padding: var(--space-2);
  border-radius: var(--radius-lg);
  background: linear-gradient(180deg, var(--color-surface-soft) 0%, var(--color-surface) 100%);
  border: 1px solid var(--color-border);
}

.news-example {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  padding: var(--space-4);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  box-shadow: 0 10px 24px rgba(20, 30, 48, 0.06);
}

.news-example__label {
  margin: 0;
  font-size: var(--text-xs);
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--color-primary);
}

.news-example__headline {
  margin: 0;
  font-size: var(--text-lg);
  font-weight: 800;
  line-height: 1.35;
  color: var(--color-heading);
}

.news-example__body {
  margin: 0;
  font-size: var(--text-base);
  line-height: 1.65;
  color: var(--color-text);
}

.news-example__comment {
  margin: 0;
  font-size: var(--text-sm);
  line-height: 1.6;
  color: var(--color-text-muted);
}

.news-comments {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-4);
  border-left: 4px solid var(--color-primary);
  background: var(--color-primary-soft);
  border-radius: var(--radius-md);
}

.news-comments__title {
  margin: 0;
  font-size: var(--text-sm);
  font-weight: 800;
  color: var(--color-heading);
}

.news-comments__list {
  margin: 0;
  padding-left: var(--space-5);
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.news-comments__item {
  font-size: var(--text-sm);
  line-height: 1.55;
  color: var(--color-text);
}

.news-example__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  padding-top: var(--space-2);
  border-top: 1px solid var(--color-border);
}

.news-example__source,
.news-example__tag {
  font-size: var(--text-xs);
  font-weight: 700;
  color: var(--color-text-muted);
}

.slide__examples-title {
  margin: 0;
  font-size: var(--text-sm);
  font-weight: 700;
  color: var(--color-heading);
}

.slide__list {
  margin: 0;
  padding-left: var(--space-5);
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.slide__list-item {
  font-size: var(--text-base);
  color: var(--color-text);
  line-height: 1.65;
}

.slide__notes {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-3);
}

.slide__note {
  position: relative;
  width: min(100%, 240px);
  padding: var(--space-4) var(--space-4) var(--space-3);
  background: #fff3a8;
  border: 1px solid #e8d268;
  border-radius: 8px;
  box-shadow: 0 8px 18px rgba(20, 30, 48, 0.1);
  color: #4d3a00;
  font-size: var(--text-sm);
  font-weight: 600;
  line-height: 1.55;
  transform: rotate(-1.2deg);
}

.slide__note:nth-child(even) {
  transform: rotate(1deg);
}

.slide__note-pin {
  position: absolute;
  top: 10px;
  right: 12px;
  width: 12px;
  height: 12px;
  border-radius: 999px;
  background: #ff7a59;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
}

.inline-question {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  box-shadow: 0 10px 24px rgba(20, 30, 48, 0.06);
  transition: border-color var(--transition-fast), box-shadow var(--transition-fast);
}

.inline-question.question--correct {
  border-color: var(--color-success);
}

.inline-question.question--wrong {
  border-color: var(--color-danger);
}

.inline-question__content {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  padding: var(--space-4);
}

.inline-question__eyebrow {
  margin: 0;
  font-size: var(--text-xs);
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--color-primary);
}

.inline-question__text {
  margin: 0;
  font-size: var(--text-lg);
  font-weight: 700;
  line-height: 1.4;
  color: var(--color-heading);
}

.inline-question__footer {
  position: sticky;
  bottom: 0;
  padding: var(--space-4);
  border-top: 1px solid var(--color-border);
  background: var(--color-surface);
}

.slide-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
}

.learn-complete {
  position: sticky;
  bottom: var(--space-4);
  z-index: 3;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  padding: var(--space-5);
  border: 1px solid var(--color-success);
  border-radius: var(--radius-lg);
  background: var(--color-success-light);
  box-shadow: var(--shadow-lg);
}

.learn-complete--locked {
  border-color: var(--color-border);
  background: var(--color-surface-soft);
}

.learn-complete--locked .learn-complete__title {
  color: var(--color-text-muted);
}

.learn-complete__title {
  margin: 0;
  font-size: var(--text-lg);
  font-weight: 800;
  color: var(--color-success);
}

.learn-complete__body {
  margin: var(--space-1) 0 0;
  font-size: var(--text-base);
  line-height: 1.55;
  color: var(--color-text);
}

/* ── Learn actions ── */
.learn-actions,
.slide-nav__btns {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-2);
}

.nav-btn {
  border: none;
  border-radius: var(--radius-md);
  padding: var(--space-3) var(--space-5);
  font-weight: 700;
  font-size: var(--text-base);
  cursor: pointer;
  transition: background var(--transition-fast), transform var(--transition-fast), box-shadow var(--transition-fast);
  min-height: 48px;
}

.nav-btn--start-quiz,
.nav-btn--learn-next,
.nav-btn--primary {
  background: var(--color-primary);
  color: var(--color-text-on-dark);
  box-shadow: 0 10px 22px rgba(47, 106, 255, 0.22);
}
.nav-btn--start-quiz:hover,
.nav-btn--learn-next:hover:not(:disabled),
.nav-btn--primary:hover:not(:disabled) { background: var(--color-btn-primary-hover); }

.nav-btn--secondary {
  background: var(--color-surface);
  color: var(--color-heading);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
}

.nav-btn--secondary:hover:not(:disabled) {
  background: var(--color-surface-soft);
}

.nav-btn:active { transform: scale(0.97); }
.nav-btn:disabled {
  cursor: not-allowed;
  opacity: 0.7;
  transform: none;
  box-shadow: none;
}

/* ── Quiz progress ── */
.quiz-progress {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}
.quiz-progress__label {
  font-size: var(--text-xs);
  font-weight: 600;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}
.quiz-progress__bar {
  height: 6px;
  background: var(--color-border);
  border-radius: var(--radius-full);
  overflow: hidden;
}
.quiz-progress__fill {
  height: 100%;
  background: var(--color-primary);
  border-radius: var(--radius-full);
  transition: width 0.4s ease;
}

/* ── Question card ── */
.question {
  display: flex;
  flex-direction: column;
  transition: border-color var(--transition-fast);
  overflow: hidden;
  min-height: 360px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-xl);
  box-shadow: 0 18px 40px rgba(20, 30, 48, 0.08);
}
.question--correct { border-color: var(--color-success); }
.question--wrong   { border-color: var(--color-danger); }

.question__content {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  padding: var(--space-6);
  flex: 1;
}

.question__text {
  margin: 0;
  font-size: var(--text-xl);
  font-weight: 700;
  color: var(--color-heading);
  line-height: 1.4;
}

/* ── Options ── */
.question__options {
  display: grid;
  gap: var(--space-2);
}

.question__footer {
  position: sticky;
  bottom: 0;
  background: var(--color-surface);
  border-top: 1px solid var(--color-border);
  padding: var(--space-4);
}

.option-btn {
  text-align: left;
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  border-radius: var(--radius-md);
  padding: var(--space-4);
  cursor: pointer;
  font-size: var(--text-base);
  font-weight: 500;
  line-height: 1.55;
  transition: border-color var(--transition-fast), background var(--transition-fast), transform var(--transition-fast), box-shadow var(--transition-fast);
}
.option-btn:hover:not(:disabled) {
  border-color: var(--color-primary);
  background: var(--color-primary-soft);
  box-shadow: 0 10px 18px rgba(47, 106, 255, 0.12);
  transform: translateY(-1px);
}
.option-btn:disabled { cursor: not-allowed; }

.option-btn--correct {
  border-color: var(--color-success);
  background: var(--color-success-light);
  color: var(--color-success);
  font-weight: 700;
}
.option-btn--wrong {
  border-color: var(--color-danger);
  background: var(--color-danger-light);
  color: var(--color-danger);
  animation: shake 0.35s ease;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  20%       { transform: translateX(-6px); }
  60%       { transform: translateX(5px); }
  80%       { transform: translateX(-3px); }
}

/* ── Feedback line ── */
.question__feedback {
  margin: 0;
  font-size: var(--text-sm);
  font-weight: 700;
}
.question__feedback--correct { color: var(--color-success); }
.question__feedback--wrong   { color: var(--color-danger); }

/* ── Transitions ── */
.question-slide-enter-active { transition: opacity 0.25s ease, transform 0.25s ease; }
.question-slide-leave-active { transition: opacity 0.15s ease, transform 0.15s ease; }
.question-slide-enter-from   { opacity: 0; transform: translateX(20px); }
.question-slide-leave-to     { opacity: 0; transform: translateX(-12px); }

.feedback-pop-enter-active { transition: opacity 0.2s ease, transform 0.2s ease; }
.feedback-pop-enter-from   { opacity: 0; transform: scale(0.85); }

@media (max-width: 768px) {
  .learn-hero {
    grid-template-columns: 1fr;
  }

  .learn-hero__mayor {
    justify-content: flex-start;
  }

  .learn-hero__briefing::after {
    display: none;
  }

  .question__content {
    padding: var(--space-5);
  }

  .question__footer {
    padding: var(--space-3);
  }

  .slide__heading,
  .question__text {
    font-size: var(--text-lg);
  }

  .learn-hero__title {
    font-size: var(--text-xl);
  }

  .slide__note {
    width: 100%;
  }

  .learn-complete {
    align-items: stretch;
    flex-direction: column;
  }

  .slide-nav {
    flex-direction: column;
  }

  .slide-nav .nav-btn {
    width: 100%;
  }

  .marketplace-compare {
    grid-template-columns: 1fr;
  }
}
</style>
