<template>
  <section class="learn-task">
    <template v-if="phase === 'LEARN'">
      <div class="learn-shell">
        <header class="learn-hero">
          <div class="learn-hero__intro">
            <p class="learn-hero__eyebrow">Briefing fra borgermesteren</p>
            <h2 class="learn-hero__title">{{ task.title || 'Før du starter oppgaven' }}</h2>
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
            v-for="(slide, index) in slides"
            :key="`${task.id}-slide-${index}`"
            class="slide"
          >
            <div class="slide__header">
              <span class="slide__step">Del {{ index + 1 }}</span>
              <span class="slide__icon" aria-hidden="true">{{ slide.icon ?? '📖' }}</span>
            </div>
            <h3 class="slide__heading">{{ slide.heading }}</h3>
            <p class="slide__body">{{ slide.body }}</p>

            <div v-if="slide.examples?.length" class="slide__examples">
              <p class="slide__examples-title">Slik kan det se ut i virkeligheten</p>
              <div v-if="task.stopTheme === 'FAKE_NEWS'" class="news-examples">
                <article
                  v-for="example in slide.examples"
                  :key="example"
                  class="news-example"
                >
                  <p class="news-example__label">{{ parseNewsExample(example).label }}</p>
                  <h4 class="news-example__headline">{{ parseNewsExample(example).headline }}</h4>
                  <p v-if="parseNewsExample(example).body" class="news-example__body">
                    {{ parseNewsExample(example).body }}
                  </p>
                  <div class="news-example__meta">
                    <span class="news-example__source">Nyhetsstrøm</span>
                    <span class="news-example__tag">Eksempel</span>
                  </div>
                </article>
              </div>
              <ul v-else class="slide__list">
                <li v-for="example in slide.examples" :key="example" class="slide__list-item">
                  {{ example }}
                </li>
              </ul>
            </div>

            <div v-if="slide.checks?.length" class="slide__notes">
              <div
                v-for="check in slide.checks"
                :key="check"
                class="slide__note"
              >
                <span class="slide__note-pin" aria-hidden="true" />
                {{ check }}
              </div>
            </div>
          </article>
        </div>

        <div class="learn-actions">
          <button
            class="nav-btn nav-btn--start-quiz"
            @click="phase = 'QUIZ'"
          >
            Jeg har lest dette, start quiz 🧠
          </button>
        </div>
      </div>
    </template>

    <!-- DONE phase -->
    <template v-else-if="phase === 'DONE'">
      <div class="done-card">
        <span class="done-card__icon" aria-hidden="true">🎉</span>
        <h3 class="done-card__heading">Quiz fullført!</h3>
        <p class="done-card__body">Du svarte riktig på alle spørsmålene. Bra jobbet, detektiv!</p>
      </div>
      <div class="slide-nav__btns" style="justify-content: flex-end">
        <button class="nav-btn nav-btn--start-quiz" @click="$emit('next')">
          {{ isLastTask ? 'Se oppsummering →' : 'Neste oppgave →' }}
        </button>
      </div>
    </template>

    <!-- QUIZ phase -->
    <template v-else>
      <div class="quiz-shell">
        <div class="quiz-progress">
          <span class="quiz-progress__label">Spørsmål {{ quizIndex + 1 }} av {{ quiz.length }}</span>
          <div class="quiz-progress__bar">
            <div class="quiz-progress__fill" :style="{ width: `${(quizIndex / quiz.length) * 100}%` }" />
          </div>
        </div>

        <Transition name="question-slide" mode="out-in">
          <div :key="quizIndex" class="question" :class="questionClass">
            <div class="question__content">
              <p class="question__text">{{ currentQuestion.question }}</p>

              <Transition name="feedback-pop">
                <p v-if="questionState === 'CORRECT'" class="question__feedback question__feedback--correct">
                  ✅ Riktig!
                </p>
                <p v-else-if="questionState === 'WRONG'" class="question__feedback question__feedback--wrong">
                  ❌ Prøv igjen
                </p>
              </Transition>
            </div>

            <div class="question__footer">
              <div class="question__options" role="group" :aria-label="currentQuestion.question">
                <button
                  v-for="opt in currentQuestion.options"
                  :key="opt"
                  class="option-btn"
                  :class="optionClass(opt)"
                  :disabled="questionState !== 'UNANSWERED'"
                  @click="submitAnswer(opt)"
                >
                  {{ opt }}
                </button>
              </div>
            </div>
          </div>
        </Transition>
      </div>
    </template>
  </section>
</template>

<script setup>
import { computed, ref, watch } from 'vue'

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
const quizIndex  = ref(0)

// 'UNANSWERED' | 'CORRECT' | 'WRONG'
const questionState   = ref('UNANSWERED')
const selectedAnswer  = ref(null)

const currentQuestion = computed(() => quiz.value[quizIndex.value]    ?? {})
const mayorImage = '/story_pictures/mayor-guide.png'
const primaryInstruction = computed(() => THEME_INSTRUCTIONS[props.task?.stopTheme] ?? 'Les forklaringene under og gjør oppgaven etterpå.')

const questionClass = computed(() => ({
  'question--correct': questionState.value === 'CORRECT',
  'question--wrong':   questionState.value === 'WRONG',
}))

watch(() => props.task?.id, () => {
  phase.value         = 'LEARN'
  quizIndex.value     = 0
  questionState.value = 'UNANSWERED'
  selectedAnswer.value = null
}, { immediate: true })

function optionClass(opt) {
  if (questionState.value === 'UNANSWERED') return {}
  const isSelected = opt === selectedAnswer.value
  const isCorrect  = opt === currentQuestion.value.correct
  if (questionState.value === 'CORRECT') return { 'option-btn--correct': isSelected }
  return {
    'option-btn--wrong':   isSelected && !isCorrect,
    'option-btn--correct': isCorrect,
  }
}

function submitAnswer(opt) {
  if (questionState.value !== 'UNANSWERED') return
  selectedAnswer.value = opt
  const correct = opt === currentQuestion.value.correct
  questionState.value = correct ? 'CORRECT' : 'WRONG'
  console.log('[LearningTask] Quiz q', quizIndex.value, '— answered:', opt, '— correct:', correct)

  if (correct) {
    setTimeout(advanceQuiz, 900)
  } else {
    setTimeout(resetQuestion, 1200)
  }
}

function resetQuestion() {
  selectedAnswer.value = null
  questionState.value  = 'UNANSWERED'
}

function parseNewsExample(example) {
  const [rawLabel, ...rest] = String(example).split(':')
  const label = rawLabel?.trim() || 'Eksempel'
  const content = rest.join(':').trim()

  if (label.toLowerCase().includes('artikkeltekst')) {
    return {
      label,
      headline: 'Ubekreftet påstand fra ukjent kilde',
      body: content,
    }
  }

  return {
    label,
    headline: content || example,
    body: '',
  }
}

function advanceQuiz() {
  if (quizIndex.value < quiz.value.length - 1) {
    quizIndex.value++
    questionState.value  = 'UNANSWERED'
    selectedAnswer.value = null
  } else {
    console.log('[LearningTask] All quiz questions passed — submitting')
    emit('submitted', { quizPassed: true })
    phase.value = 'DONE'
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

.nav-btn--start-quiz {
  background: var(--color-primary);
  color: var(--color-text-on-dark);
  box-shadow: 0 10px 22px rgba(47, 106, 255, 0.22);
}
.nav-btn--start-quiz:hover { background: var(--color-btn-primary-hover); }
.nav-btn:active { transform: scale(0.97); }

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

/* ── Done card ── */
.done-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-3);
  text-align: center;
  padding: var(--space-7);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-xl);
  box-shadow: 0 18px 40px rgba(20, 30, 48, 0.08);
}
.done-card__icon { font-size: 2.5rem; line-height: 1; }
.done-card__heading {
  margin: 0;
  font-size: var(--text-xl);
  font-weight: 700;
  color: var(--color-success);
}
.done-card__body {
  margin: 0;
  font-size: var(--text-base);
  color: var(--color-text);
  line-height: 1.65;
}

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

  .question__content,
  .done-card {
    padding: var(--space-5);
  }

  .question__footer {
    padding: var(--space-3);
  }

  .slide__heading,
  .question__text,
  .done-card__heading {
    font-size: var(--text-lg);
  }

  .learn-hero__title {
    font-size: var(--text-xl);
  }

  .slide__note {
    width: 100%;
  }
}
</style>
