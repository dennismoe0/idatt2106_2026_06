<template>
  <section class="learn-task">
    <template v-if="phase === 'LEARN'">
      <div class="learn-shell">
        <header v-if="currentSlideIndex === 0" class="learn-hero">
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
          <div class="learn-progress">
            <p class="learn-progress__label">Læringsdel {{ currentSlideIndex + 1 }} / {{ slides.length }}</p>
            <div class="learn-progress__steps" aria-hidden="true">
              <span
                v-for="(_, index) in slides"
                :key="`step-${index}`"
                class="learn-progress__step"
                :class="{
                  'learn-progress__step--active': index === currentSlideIndex,
                  'learn-progress__step--done': questionStateFor(index) === 'CORRECT',
                }"
              />
            </div>
          </div>

          <Transition name="slide-page" mode="out-in">
            <article
              v-if="currentSlide"
              :key="`${task.id}-slide-${currentSlideIndex}`"
              class="slide learn-page"
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
              <div v-else-if="task.stopTheme === 'FAKE_NEWS' && newsVisualFor(currentSlideIndex)" class="news-visual">
                <div v-if="newsVisualFor(currentSlideIndex).domains" class="news-domains">
                  <div class="news-domains__column news-domains__column--safe">
                    <p class="news-domains__heading">
                      <span class="news-domains__pill news-domains__pill--safe">Trygge domenenavn</span>
                    </p>
                    <article
                      v-for="site in newsVisualFor(currentSlideIndex).domains.safe"
                      :key="site.url"
                      class="news-domains__card news-domains__card--safe"
                    >
                      <div class="news-domains__bar news-domains__bar--safe">
                        <span class="news-domains__lock" aria-hidden="true">🔒</span>
                        <span class="news-domains__url">{{ site.url }}</span>
                      </div>
                      <p class="news-domains__name">{{ site.name }}</p>
                      <p class="news-domains__tag">{{ site.tag }}</p>
                    </article>
                  </div>

                  <div class="news-domains__column news-domains__column--risky">
                    <p class="news-domains__heading">
                      <span class="news-domains__pill news-domains__pill--risky">Mistenkelige domenenavn</span>
                    </p>
                    <article
                      v-for="site in newsVisualFor(currentSlideIndex).domains.risky"
                      :key="site.url"
                      class="news-domains__card news-domains__card--risky"
                    >
                      <div class="news-domains__bar news-domains__bar--risky">
                        <span class="news-domains__lock" aria-hidden="true">⚠️</span>
                        <span class="news-domains__url">{{ site.url }}</span>
                      </div>
                      <p class="news-domains__name">{{ site.name }}</p>
                      <p class="news-domains__tag">{{ site.tag }}</p>
                    </article>
                  </div>
                </div>

                <div v-if="newsVisualFor(currentSlideIndex).crossCheck" class="news-search">
                  <div class="news-search__bar">
                    <span class="news-search__icon" aria-hidden="true">🔍</span>
                    <span class="news-search__query">{{ newsVisualFor(currentSlideIndex).crossCheck.query }}</span>
                  </div>
                  <p class="news-search__count">
                    Viser 4 treff fra ulike kilder
                  </p>
                  <div class="news-search__results">
                    <article
                      v-for="hit in newsVisualFor(currentSlideIndex).crossCheck.hits"
                      :key="hit.key"
                      class="news-search__hit"
                      :class="`news-search__hit--${hit.tone}`"
                    >
                      <div class="news-search__hit-top">
                        <span class="news-search__source">{{ hit.source }}</span>
                        <span
                          class="news-search__badge"
                          :class="`news-search__badge--${hit.tone}`"
                        >
                          {{ hit.badge }}
                        </span>
                      </div>
                      <p class="news-search__url">{{ hit.url }} · {{ hit.time }}</p>
                      <p class="news-search__title">{{ hit.headline }}</p>
                      <p class="news-search__snippet">{{ hit.snippet }}</p>
                    </article>
                  </div>
                  <div class="news-search__verdict">
                    <p class="news-search__verdict-title">{{ newsVisualFor(currentSlideIndex).crossCheck.verdict.title }}</p>
                    <p class="news-search__verdict-body">{{ newsVisualFor(currentSlideIndex).crossCheck.verdict.body }}</p>
                  </div>
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
              <div
                v-else-if="photoVisualFor(currentSlideIndex)"
                class="photo-compare"
                :class="{ 'photo-compare--single': photoVisualFor(currentSlideIndex).length === 1 }"
              >
                <article
                  v-for="image in photoVisualFor(currentSlideIndex)"
                  :key="image.key"
                  class="photo-compare__item"
                >
                  <div class="photo-compare__image-wrap">
                    <img class="photo-compare__image" :src="image.src" :alt="image.alt" />
                  </div>
                  <div class="photo-compare__content">
                    <p class="photo-compare__eyebrow">{{ image.eyebrow }}</p>
                    <h4 class="photo-compare__title">{{ image.title }}</h4>
                    <p class="photo-compare__body">{{ image.body }}</p>
                  </div>
                </article>
              </div>
              <div v-else-if="socialMediaVisualFor(currentSlideIndex)" class="social-learn">
                <article
                  v-for="card in socialMediaVisualFor(currentSlideIndex)"
                  :key="card.key"
                  class="social-learn__card"
                  :class="card.kind === 'profile' ? 'social-learn__card--profile' : 'social-learn__card--post'"
                >
                  <div class="social-learn__platform">
                    <span class="social-learn__platform-dot" aria-hidden="true" />
                    {{ card.platform }}
                  </div>

                  <template v-if="card.kind === 'profile'">
                    <div class="social-learn__profile-top">
                      <span class="social-learn__avatar" :style="socialAvatarStyle(card.username)">
                        {{ socialInitials(card.username) }}
                      </span>
                      <div class="social-learn__identity">
                        <div class="social-learn__name-row">
                          <p class="social-learn__name">{{ card.username }}</p>
                          <span v-if="card.verified" class="social-learn__verified" aria-label="Verifisert konto">✓</span>
                        </div>
                        <p class="social-learn__handle">{{ card.handle }}</p>
                      </div>
                    </div>
                    <div class="social-learn__profile-metrics">
                      <span>{{ card.posts }}</span>
                      <span>{{ card.followers }}</span>
                      <span>{{ card.following }}</span>
                    </div>
                    <p class="social-learn__profile-note">{{ card.note }}</p>
                  </template>

                  <template v-else>
                    <div class="social-learn__post-top">
                      <span class="social-learn__avatar" :style="socialAvatarStyle(card.username)">
                        {{ socialInitials(card.username) }}
                      </span>
                      <div class="social-learn__identity">
                        <div class="social-learn__name-row">
                          <p class="social-learn__name">{{ card.username }}</p>
                          <span v-if="card.verified" class="social-learn__verified" aria-label="Verifisert konto">✓</span>
                        </div>
                        <p class="social-learn__meta">{{ card.handle }} · {{ card.timestamp }}</p>
                      </div>
                    </div>
                    <p class="social-learn__content">{{ card.content }}</p>
                    <div class="social-learn__stats">
                      <span>♡ {{ card.likes }}</span>
                      <span>💬 {{ card.comments }}</span>
                      <span>↗ {{ card.shares }}</span>
                    </div>
                  </template>

                  <div class="social-learn__callout">
                    <p class="social-learn__callout-title">{{ card.calloutTitle }}</p>
                    <p class="social-learn__callout-body">{{ card.calloutBody }}</p>
                  </div>
                </article>
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

            <div
              v-if="currentSlide.checks?.length && !shouldHideChecks(currentSlideIndex)"
              class="slide__notes"
              :class="{ 'slide__notes--highlight': shouldHighlightSocialChecks(currentSlideIndex) }"
            >
              <div
                v-if="shouldHighlightSocialChecks(currentSlideIndex)"
                class="slide__note-box"
              >
                <p class="slide__note-box-title">Slik kan det se ut i virkeligheten</p>
                <p
                  v-for="check in currentSlide.checks"
                  :key="check"
                  class="slide__note-box-item"
                >
                  {{ check }}
                </p>
              </div>
              <div
                v-else
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
                    v-for="opt in currentQuestionOptions"
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
          </Transition>
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
const shuffledOptionsByQuestion = ref({})
const currentSlide = computed(() => slides.value[currentSlideIndex.value] ?? null)
const currentQuestion = computed(() => questionForSlide(currentSlideIndex.value))
const currentQuestionOptions = computed(() => shuffledOptionsFor(currentSlideIndex.value))
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

const SOCIAL_MEDIA_VISUAL_EXAMPLES = {
  0: [
    {
      key: 'panic-post',
      kind: 'post',
      platform: 'Tweety.no',
      username: 'RykteRadar',
      handle: '@rykteradar',
      timestamp: '2 min siden',
      content: 'DEL NÅ før dette blir slettet!!! Rektor skal visst ta mobilen fra alle allerede fra i morgen 😡 Ingen tør å si det offentlig ennå!',
      likes: '1 284',
      comments: '219',
      shares: '406',
      calloutTitle: 'Prøver å stresse deg',
      calloutBody: 'Når et innlegg roper at du må dele med en gang, er målet ofte å få deg til å reagere før du rekker å sjekke om det stemmer.',
    },
    {
      key: 'school-rumour',
      kind: 'post',
      platform: 'Fjesbok.no',
      username: 'Anonym elevsnakk',
      handle: '@elevsnakk24',
      timestamp: '14 min siden',
      content: 'Jeg har hørt at prøven er lekket til 7B, men ingen vil si det høyt. Hvis dette er sant er det sykt urettferdig.',
      likes: '342',
      comments: '97',
      shares: '51',
      calloutTitle: 'Rykter uten bevis sprer seg fort',
      calloutBody: 'Formuleringer som “jeg har hørt” eller “noen sier” høres ekte ut, men de viser ikke at noen faktisk vet om påstanden stemmer.',
    },
  ],
  1: [
    {
      key: 'fake-dm',
      kind: 'post',
      platform: 'Tweety.no',
      username: 'GamerLoke99',
      handle: '@gamerloke99_',
      timestamp: 'Nå',
      content: 'Hei! Jeg går også på skolen din. Hva heter kontaktlæreren din igjen? Send Snapen din så kan jeg forklare hva som skjer 👀',
      likes: '12',
      comments: '0',
      shares: '0',
      calloutTitle: 'Blir personlig veldig fort',
      calloutBody: 'Falske kontoer prøver ofte å bygge tillit raskt og spør om detaljer som kan brukes til å virke mer ekte neste gang.',
    },
    {
      key: 'suspicious-profile',
      kind: 'profile',
      platform: 'Fjesbok.no',
      username: 'Emma.clvgs_real',
      handle: '@emma.clvgs_real',
      posts: '2 innlegg',
      followers: '14 følgere',
      following: '387 følger',
      note: 'Nytt profilbilde, nesten ingen innlegg og veldig mange følg-forespørsler på kort tid.',
      calloutTitle: 'Profilen ser tynn ut',
      calloutBody: 'Få innlegg, lite historikk og ubalanse mellom følgere og følger kan være tegn på at kontoen er laget for å virke ekte, ikke for å være ekte.',
    },
  ],
}

const NEWS_VISUAL_EXAMPLES = {
  1: {
    domains: {
      query: '"skoler stenger trondheim"',
      safe: [
        { url: 'nrk.no/trondelag', name: 'NRK Trøndelag', tag: 'Lisensiert allmennkringkaster', icon: '🟦' },
        { url: 'vg.no', name: 'VG', tag: 'Norsk avis med redaksjon', icon: '🟥' },
        { url: 'trondheim.kommune.no', name: 'Trondheim kommune', tag: 'Offentlig nettsted (.kommune.no)', icon: '🟩' },
      ],
      risky: [
        { url: 'supernytt24.xyz', name: 'Supernytt24', tag: 'Ukjent avisside med mistenkelig domenenavn', icon: '⚠️' },
        { url: 'deldettenaa.blog', name: 'Del dette nå', tag: 'Bloggsted uten redaktør', icon: '⚠️' },
        { url: 'sannhet-nyheter-online.net', name: 'Sannhet Nyheter', tag: 'Framstår som et mediehus', icon: '⚠️' },
      ],
    },
  },
  2: {
    crossCheck: {
      query: '"alle skoler stenger før 12" trondheim',
      verdict: {
        title: 'Vurder helheten',
        body: 'Tre seriøse kilder avkrefter eller avviser saken. Bare ett ukjent nettsted publiserer den dramatiske versjonen.',
      },
      hits: [
        {
          key: 'nrk',
          tone: 'safe',
          source: 'NRK Trøndelag',
          url: 'nrk.no/trondelag',
          time: 'Oppdatert i dag 09:42',
          headline: 'Ingen meldinger om stenging — kommunen avkrefter rykter',
          snippet: 'Vi har vært i kontakt med Trondheim kommune som bekrefter at skolene følger ordinær timeplan.',
          badge: 'Bekreftet ekte',
        },
        {
          key: 'kommune',
          tone: 'safe',
          source: 'Trondheim kommune',
          url: 'trondheim.kommune.no/skole',
          time: 'Publisert 08:15',
          headline: 'Skolene følger ordinær timeplan i dag',
          snippet: 'Det sirkulerer en falsk melding i sosiale medier. Følg meldinger fra skolen din i Visma Flyt.',
          badge: 'Offentlig kilde',
        },
        {
          key: 'adressa',
          tone: 'safe',
          source: 'Adresseavisen',
          url: 'adressa.no',
          time: '10 min siden',
          headline: 'Falsk melding om skolestenging spres i sosiale medier',
          snippet: 'Lokalavisen advarer mot å dele saken videre uten å sjekke kilden først.',
          badge: 'Lokalavis',
        },
        {
          key: 'fishy',
          tone: 'risky',
          source: 'Supernytt24',
          url: 'supernytt24.xyz/breaking',
          time: 'For 4 min siden',
          headline: 'SJOKK! ALLE SKOLER STENGES FØR KL. 12 — DEL NÅ!',
          snippet: 'En hemmelig kilde i rådhuset sier at alle allerede vet sannheten…',
          badge: 'Eneste sted saken finnes',
        },
      ],
    },
  },
}

const PHOTO_VISUAL_EXAMPLES = {
  1: [
    {
      key: 'ai-generated',
      src: '/story_pictures/photographer-ai-paris-cafe.png',
      eyebrow: 'KI-generert bilde',
      title: 'Et bilde kan se ekte ut uten å være tatt med kamera',
      body: 'KI kan lage realistiske scener med mennesker, steder og lys som virker troverdige ved første blikk. Legg merke til hånden som holder koppen - fingrene ser ikke naturlige ut. Ansiktene i bakgrunnen er også uklare og merkelige, noe som er typisk for KI-genererte bilder.',
      alt: 'Et KI-generert bilde av en mann på en kafé i Paris med Eiffeltårnet i bakgrunnen.',
    },
  ],
  2: [
    {
      key: 'real',
      src: '/story_pictures/photographer-real-playground.jpg',
      eyebrow: 'Ekte bilde',
      title: 'Ekte bilder viser en virkelig hendelse',
      body: 'Dette er et vanlig foto fra stedet. Det kan være justert i farger, men innholdet er ikke endret.',
      alt: 'Et ekte foto av to barn på en lekeplass der en person henger fra en metallstang.',
    },
    {
      key: 'manipulated',
      src: '/story_pictures/photographer-manipulated-playground.png',
      eyebrow: 'Manipulert bilde',
      title: 'Personer er lagt til i etterkant',
      body: 'Bildet bygger på den samme scenen, men flere mennesker er lagt inn. Da kan bildet gi et annet inntrykk av hva som faktisk skjedde. Ser vi nøye etter, er ansiktene til noen av personene merkelige.',
      alt: 'Et manipulert foto fra samme lekeplass der flere personer er lagt til rundt personen som henger fra metallstangen.',
    },
  ],
}

watch(() => props.task?.id, () => {
  phase.value         = 'LEARN'
  submittedOnce.value = false
  currentSlideIndex.value = 0
  questionStates.value = {}
  selectedAnswers.value = {}
  shuffledOptionsByQuestion.value = buildShuffledOptionsMap(quiz.value)
}, { immediate: true })

function questionForSlide(index) {
  return quiz.value[index] ?? null
}

function shuffledOptionsFor(index) {
  return shuffledOptionsByQuestion.value[index] ?? questionForSlide(index)?.options ?? []
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
  scrollToLearningTop()
}

function goToNextSlide() {
  if (!canAdvanceFromCurrentSlide.value || isLastSlide.value) return
  currentSlideIndex.value += 1
  scrollToLearningTop()
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

function photoVisualFor(index) {
  return props.task?.stopTheme === 'AI_PHOTO' ? PHOTO_VISUAL_EXAMPLES[index] ?? null : null
}

function socialMediaVisualFor(index) {
  return props.task?.stopTheme === 'SOCIAL_MEDIA' ? SOCIAL_MEDIA_VISUAL_EXAMPLES[index] ?? null : null
}

function newsVisualFor(index) {
  return props.task?.stopTheme === 'FAKE_NEWS' ? NEWS_VISUAL_EXAMPLES[index] ?? null : null
}

function socialInitials(username) {
  const words = String(username ?? '')
    .trim()
    .split(/\s+/)
    .filter(Boolean)

  if (!words.length) return 'SM'
  if (words.length === 1) return words[0].replace(/[^a-zA-Z0-9ÆØÅæøå]/g, '').slice(0, 2).toUpperCase() || 'SM'

  return words.slice(0, 2).map(word => word[0] ?? '').join('').toUpperCase()
}

function socialAvatarStyle(username) {
  const seed = Array.from(String(username ?? ''))
    .reduce((sum, char) => sum + char.charCodeAt(0), 0)
  const hue = seed % 360
  const secondaryHue = (hue + 32) % 360

  return {
    background: `linear-gradient(135deg, hsl(${hue} 80% 74%) 0%, hsl(${secondaryHue} 62% 56%) 100%)`,
  }
}

function shouldHighlightSocialChecks(index) {
  return props.task?.stopTheme === 'SOCIAL_MEDIA' && index === 2
}

function shouldHideChecks(index) {
  return props.task?.stopTheme === 'SOCIAL_MEDIA' && index === 2
}

function completeIfAllCorrect() {
  if (allQuestionsCorrect.value && !submittedOnce.value) {
    submittedOnce.value = true
    console.log('[LearningTask] All quiz questions passed — submitting')
    emit('submitted', { quizPassed: true })
  }
}

function buildShuffledOptionsMap(questions) {
  const shuffledEntries = []
  let previousCorrectIndex = null

  questions.forEach((question, index) => {
    const { options, correctIndex } = shuffleQuestionOptions(question, previousCorrectIndex)
    shuffledEntries.push([index, options])
    previousCorrectIndex = correctIndex
  })

  return Object.fromEntries(shuffledEntries)
}

function shuffleQuestionOptions(question, previousCorrectIndex = null) {
  const options = Array.isArray(question?.options) ? [...question.options] : []
  const correctOption = question?.correct

  if (!correctOption || options.length < 2) {
    return {
      options: shuffleList(options),
      correctIndex: options.findIndex(option => option === correctOption),
    }
  }

  const incorrectOptions = shuffleList(options.filter(option => option !== correctOption))
  const candidateIndexes = options
    .map((_, index) => index)
    .filter(index => !isMiddleIndex(index, options.length))

  if (!candidateIndexes.length) {
    const shuffled = shuffleList(options)
    return {
      options: shuffled,
      correctIndex: shuffled.findIndex(option => option === correctOption),
    }
  }

  const preferredIndexes = candidateIndexes.filter(index => index !== previousCorrectIndex)
  const usableIndexes = preferredIndexes.length ? preferredIndexes : candidateIndexes
  const correctIndex = usableIndexes[Math.floor(Math.random() * usableIndexes.length)]

  const arrangedOptions = Array(options.length).fill(null)
  arrangedOptions[correctIndex] = correctOption

  let incorrectIndex = 0
  for (let i = 0; i < arrangedOptions.length; i += 1) {
    if (arrangedOptions[i] === null) {
      arrangedOptions[i] = incorrectOptions[incorrectIndex]
      incorrectIndex += 1
    }
  }

  return { options: arrangedOptions, correctIndex }
}

function shuffleList(items) {
  const shuffled = [...items]
  for (let i = shuffled.length - 1; i > 0; i -= 1) {
    const randomIndex = Math.floor(Math.random() * (i + 1))
    ;[shuffled[i], shuffled[randomIndex]] = [shuffled[randomIndex], shuffled[i]]
  }
  return shuffled
}

function isMiddleIndex(index, length) {
  if (length < 3) return false
  if (length % 2 === 1) return index === Math.floor(length / 2)
  return index === (length / 2) - 1 || index === length / 2
}

function scrollToLearningTop() {
  if (typeof window !== 'undefined') {
    window.scrollTo({ top: 0, behavior: 'smooth' })
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

.learn-progress {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.learn-progress__label {
  margin: 0;
  font-size: var(--text-xs);
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--color-primary);
}

.learn-progress__steps {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(0, 1fr));
  gap: var(--space-2);
}

.learn-progress__step {
  height: 10px;
  border-radius: var(--radius-full);
  background: var(--color-border);
}

.learn-progress__step--active {
  background: var(--color-primary);
  box-shadow: 0 0 0 4px color-mix(in srgb, var(--color-primary-light) 45%, transparent);
}

.learn-progress__step--done {
  background: var(--color-success);
}

/* ── Slide ── */
.slide {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.learn-page {
  min-height: 70vh;
  padding: var(--space-6);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-xl);
  background: linear-gradient(180deg, var(--color-surface) 0%, var(--color-bg) 100%);
  box-shadow: var(--shadow-lg);
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

.news-visual {
  display: grid;
  gap: var(--space-5);
  margin-top: var(--space-2);
}

.news-examples {
  display: grid;
  gap: var(--space-3);
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

.news-domains {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: var(--space-3);
}

.news-domains__column {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.news-domains__heading {
  margin: 0;
}

.news-domains__pill {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.3rem 0.75rem;
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: 800;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.news-domains__pill--safe {
  background: color-mix(in srgb, var(--color-success-light) 70%, white);
  color: var(--color-success);
  border: 1px solid color-mix(in srgb, var(--color-success) 40%, transparent);
}

.news-domains__pill--risky {
  background: color-mix(in srgb, var(--color-danger-light) 70%, white);
  color: var(--color-danger);
  border: 1px solid color-mix(in srgb, var(--color-danger) 40%, transparent);
}

.news-domains__card {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
}

.news-domains__card--safe {
  border-color: color-mix(in srgb, var(--color-success) 28%, var(--color-border));
}

.news-domains__card--risky {
  border-color: color-mix(in srgb, var(--color-danger) 30%, var(--color-border));
}

.news-domains__bar {
  display: flex;
  align-items: center;
  gap: 0.45rem;
  padding: 0.45rem 0.75rem;
  border-radius: var(--radius-full);
  font-family: 'SF Mono', 'JetBrains Mono', ui-monospace, monospace;
  font-size: var(--text-sm);
  font-weight: 600;
  word-break: break-all;
}

.news-domains__bar--safe {
  background: color-mix(in srgb, var(--color-success-light) 75%, white);
  color: var(--color-success-dark);
  border: 1px solid color-mix(in srgb, var(--color-success) 35%, transparent);
}

.news-domains__bar--risky {
  background: color-mix(in srgb, var(--color-danger-light) 70%, white);
  color: var(--color-danger-dark);
  border: 1px solid color-mix(in srgb, var(--color-danger) 35%, transparent);
}

.news-domains__lock {
  font-size: 0.95rem;
}

.news-domains__url {
  flex: 1;
}

.news-domains__name {
  margin: 0;
  font-size: var(--text-sm);
  font-weight: 800;
  color: var(--color-heading);
}

.news-domains__tag {
  margin: 0;
  font-size: var(--text-xs);
  color: var(--color-text-muted);
  letter-spacing: 0.04em;
}

.news-search {
  display: grid;
  gap: var(--space-3);
  padding: var(--space-4);
  border-radius: var(--radius-xl);
  background: linear-gradient(180deg, #ffffff 0%, #f5f7fb 100%);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
}

.news-search__bar {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: 0.65rem 0.9rem;
  border-radius: var(--radius-full);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  box-shadow: 0 1px 3px rgba(20, 30, 48, 0.05);
}

.news-search__icon {
  font-size: 1rem;
  color: var(--color-text-muted);
}

.news-search__query {
  font-family: 'SF Mono', 'JetBrains Mono', ui-monospace, monospace;
  font-size: var(--text-sm);
  color: var(--color-heading);
  font-weight: 600;
}

.news-search__count {
  margin: 0;
  font-size: var(--text-xs);
  color: var(--color-text-muted);
  letter-spacing: 0.04em;
}

.news-search__results {
  display: grid;
  gap: var(--space-2);
}

.news-search__hit {
  display: grid;
  gap: 0.25rem;
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
  background: var(--color-surface);
}

.news-search__hit--safe {
  border-color: color-mix(in srgb, var(--color-success) 28%, var(--color-border));
  background: color-mix(in srgb, var(--color-success-light) 35%, white);
}

.news-search__hit--risky {
  border-color: color-mix(in srgb, var(--color-danger) 35%, var(--color-border));
  background: color-mix(in srgb, var(--color-danger-light) 35%, white);
}

.news-search__hit-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.news-search__source {
  font-size: var(--text-xs);
  font-weight: 800;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--color-text-muted);
}

.news-search__badge {
  padding: 0.18rem 0.6rem;
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: 800;
  letter-spacing: 0.04em;
}

.news-search__badge--safe {
  background: color-mix(in srgb, var(--color-success-light) 75%, white);
  color: var(--color-success);
  border: 1px solid color-mix(in srgb, var(--color-success) 38%, transparent);
}

.news-search__badge--risky {
  background: var(--color-danger);
  color: var(--color-text-on-dark);
}

.news-search__url {
  margin: 0;
  font-family: 'SF Mono', 'JetBrains Mono', ui-monospace, monospace;
  font-size: var(--text-xs);
  color: var(--color-text-muted);
  word-break: break-all;
}

.news-search__title {
  margin: 0;
  font-size: var(--text-base);
  font-weight: 800;
  color: var(--color-heading);
  line-height: 1.35;
}

.news-search__hit--risky .news-search__title {
  color: var(--color-danger-dark);
}

.news-search__snippet {
  margin: 0;
  font-size: var(--text-sm);
  line-height: 1.55;
  color: var(--color-text);
}

.news-search__verdict {
  display: grid;
  gap: 0.25rem;
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-md);
  background: color-mix(in srgb, var(--color-primary-soft) 70%, white);
  border-left: 4px solid var(--color-primary);
}

.news-search__verdict-title {
  margin: 0;
  font-size: var(--text-sm);
  font-weight: 800;
  color: var(--color-heading);
}

.news-search__verdict-body {
  margin: 0;
  font-size: var(--text-sm);
  line-height: 1.6;
  color: var(--color-text);
}

@media (min-width: 720px) {
  .news-domains {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
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

.photo-compare {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-3);
}

.photo-compare--single {
  grid-template-columns: 1fr;
}

.photo-compare__item {
  display: grid;
  gap: var(--space-3);
  overflow: hidden;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  box-shadow: 0 12px 28px rgba(20, 30, 48, 0.08);
}

.photo-compare__image-wrap {
  aspect-ratio: 4 / 3;
  background: var(--color-surface-soft);
  overflow: hidden;
}

.photo-compare__image {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.photo-compare__content {
  display: grid;
  gap: var(--space-2);
  padding: 0 var(--space-4) var(--space-4);
}

.photo-compare__eyebrow,
.photo-compare__title,
.photo-compare__body {
  margin: 0;
}

.photo-compare__eyebrow {
  font-size: var(--text-xs);
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--color-primary);
}

.photo-compare__title {
  font-size: var(--text-base);
  font-weight: 800;
  line-height: 1.35;
  color: var(--color-heading);
}

.photo-compare__body {
  font-size: var(--text-sm);
  line-height: 1.6;
  color: var(--color-text);
}

.social-learn {
  display: grid;
  gap: var(--space-3);
}

.social-learn__card {
  display: grid;
  gap: var(--space-3);
  padding: var(--space-4);
  border-radius: var(--radius-lg);
  border: 1px solid #cfe0ff;
  background: linear-gradient(180deg, #ffffff 0%, #f7fbff 100%);
  box-shadow: 0 12px 28px rgba(34, 72, 140, 0.08);
}

.social-learn__card--profile {
  border-color: #d8e6ef;
  background: linear-gradient(180deg, #ffffff 0%, #f8fbfc 100%);
}

.social-learn__platform {
  display: inline-flex;
  align-items: center;
  gap: 0.45rem;
  justify-self: start;
  border-radius: var(--radius-full);
  padding: 0.35rem 0.8rem;
  background: #dbeafe;
  color: #1d5fa8;
  font-size: var(--text-xs);
  font-weight: 800;
  letter-spacing: 0.04em;
}

.social-learn__platform-dot {
  width: 0.55rem;
  height: 0.55rem;
  border-radius: 50%;
  background: currentColor;
}

.social-learn__post-top,
.social-learn__profile-top {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.social-learn__avatar {
  width: 3rem;
  height: 3rem;
  display: grid;
  place-items: center;
  border-radius: 50%;
  color: white;
  font-size: 0.95rem;
  font-weight: 800;
  letter-spacing: 0.08em;
  flex-shrink: 0;
  box-shadow: 0 8px 18px rgba(34, 72, 140, 0.16);
}

.social-learn__identity {
  min-width: 0;
  display: grid;
  gap: 0.2rem;
}

.social-learn__name-row {
  display: flex;
  align-items: center;
  gap: 0.45rem;
  flex-wrap: wrap;
}

.social-learn__name,
.social-learn__meta,
.social-learn__content,
.social-learn__stats,
.social-learn__handle,
.social-learn__profile-note,
.social-learn__callout-title,
.social-learn__callout-body,
.social-learn__profile-metrics {
  margin: 0;
}

.social-learn__name {
  font-size: var(--text-base);
  font-weight: 800;
  color: var(--color-heading);
}

.social-learn__verified {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 1.1rem;
  height: 1.1rem;
  border-radius: 50%;
  background: #2f6aff;
  color: white;
  font-size: 0.72rem;
  font-weight: 800;
}

.social-learn__handle,
.social-learn__meta {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
}

.social-learn__content {
  font-size: var(--text-base);
  line-height: 1.65;
  color: var(--color-text);
}

.social-learn__stats,
.social-learn__profile-metrics {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-3);
  padding-top: var(--space-2);
  border-top: 1px solid #d8e5f7;
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  font-weight: 700;
}

.social-learn__profile-note {
  font-size: var(--text-sm);
  line-height: 1.6;
  color: var(--color-text);
}

.social-learn__callout {
  display: grid;
  gap: 0.35rem;
  padding: var(--space-3);
  border-radius: var(--radius-md);
  background: #fff3cd;
  border: 1px solid #f0d98a;
}

.social-learn__callout-title {
  font-size: var(--text-sm);
  font-weight: 800;
  color: #6f4e00;
}

.social-learn__callout-body {
  font-size: var(--text-sm);
  line-height: 1.55;
  color: #6f4e00;
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

.slide__notes--highlight {
  display: block;
}

.slide__note-box {
  display: grid;
  gap: var(--space-3);
  padding: var(--space-5);
  border-radius: var(--radius-xl);
  background:
    radial-gradient(circle at top right, rgba(255, 255, 255, 0.55), transparent 28%),
    linear-gradient(135deg, #fff2a8 0%, #ffd6a5 48%, #ffb4c8 100%);
  border: 2px solid #f0ae58;
  box-shadow: 0 16px 34px rgba(196, 116, 30, 0.16);
}

.slide__note-box-title,
.slide__note-box-item {
  margin: 0;
}

.slide__note-box-title {
  font-size: var(--text-sm);
  font-weight: 800;
  color: #6a3e00;
}

.slide__note-box-item {
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-lg);
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(146, 88, 18, 0.16);
  font-size: var(--text-base);
  line-height: 1.65;
  color: #593600;
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
  padding: var(--space-3) var(--space-6);
  font-weight: 700;
  font-size: var(--text-base);
  cursor: pointer;
  transition: background var(--transition-fast), transform var(--transition-fast), box-shadow var(--transition-fast);
  min-height: 48px;
}

.nav-btn--start-quiz,
.nav-btn--primary {
  background: var(--color-primary);
  color: var(--color-text-on-dark);
  box-shadow: 0 10px 22px rgba(47, 106, 255, 0.22);
}

.nav-btn--learn-next {
  background: var(--color-primary);
  color: var(--color-text-on-dark);
  box-shadow: 0 10px 22px rgba(47, 106, 255, 0.22);
  flex-shrink: 0;
  white-space: nowrap;
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

.slide-page-enter-active,
.slide-page-leave-active {
  transition: opacity 0.24s ease, transform 0.24s ease;
}

.slide-page-enter-from {
  opacity: 0;
  transform: translateX(24px);
}

.slide-page-leave-to {
  opacity: 0;
  transform: translateX(-24px);
}

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

  .learn-page {
    min-height: auto;
    padding: var(--space-4);
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

  .marketplace-compare,
  .photo-compare {
    grid-template-columns: 1fr;
  }
}

@media (min-width: 720px) {
  .social-learn {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
