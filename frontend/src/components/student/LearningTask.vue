<template>
  <section class="learn-task">
    <p class="learn-task__guidance">{{ task.guidanceText }}</p>

    <template v-if="phase === 'LEARN'">
      <div class="learn-overview pinned-note" style="--card-rotate: 0.2deg">
        <h3 class="learn-overview__title">Før du starter quizen</h3>
        <p class="learn-overview__body">
          Les gjennom tipsene under som én samlet mini-guide. Målet er at du skal vite nøyaktig hva du skal se etter når du møter slike situasjoner i spillet.
        </p>
      </div>

      <div class="learn-sections">
        <article
          v-for="(slide, index) in slides"
          :key="`${task.id}-slide-${index}`"
          class="slide pinned-note"
          :style="`--card-rotate: ${index % 2 === 0 ? -0.35 : 0.25}deg`"
        >
          <span class="slide__icon" aria-hidden="true">{{ slide.icon ?? '📖' }}</span>
          <h3 class="slide__heading">{{ slide.heading }}</h3>
          <p class="slide__body">{{ slide.body }}</p>

          <div v-if="slide.examples?.length" class="slide__panel">
            <p class="slide__panel-title">Eksempler</p>
            <ul class="slide__list">
              <li v-for="example in slide.examples" :key="example" class="slide__list-item">
                {{ example }}
              </li>
            </ul>
          </div>

          <div v-if="slide.checks?.length" class="slide__panel slide__panel--accent">
            <p class="slide__panel-title">Husk dette</p>
            <ul class="slide__list">
              <li v-for="check in slide.checks" :key="check" class="slide__list-item">
                {{ check }}
              </li>
            </ul>
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
    </template>

    <!-- DONE phase -->
    <template v-else-if="phase === 'DONE'">
      <div class="done-card pinned-note" style="--card-rotate: 0.3deg">
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
      <div class="quiz-progress">
        <span class="quiz-progress__label">Spørsmål {{ quizIndex + 1 }} av {{ quiz.length }}</span>
        <div class="quiz-progress__bar">
          <div class="quiz-progress__fill" :style="{ width: `${(quizIndex / quiz.length) * 100}%` }" />
        </div>
      </div>

      <Transition name="question-slide" mode="out-in">
        <div :key="quizIndex" class="question pinned-note" :class="questionClass" style="--card-rotate: 0.2deg">
          <p class="question__text">{{ currentQuestion.question }}</p>

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

          <Transition name="feedback-pop">
            <p v-if="questionState === 'CORRECT'" class="question__feedback question__feedback--correct">
              ✅ Riktig!
            </p>
            <p v-else-if="questionState === 'WRONG'" class="question__feedback question__feedback--wrong">
              ❌ Prøv igjen
            </p>
          </Transition>
        </div>
      </Transition>
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

const phase      = ref('LEARN')
const quizIndex  = ref(0)

// 'UNANSWERED' | 'CORRECT' | 'WRONG'
const questionState   = ref('UNANSWERED')
const selectedAnswer  = ref(null)

const currentQuestion = computed(() => quiz.value[quizIndex.value]    ?? {})

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
  gap: var(--space-4);
}

.learn-task__guidance {
  margin: 0;
  color: var(--color-cork-dark);
  font-weight: 600;
}

/* ── Learn overview ── */
.learn-overview {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.learn-overview__title {
  margin: 0;
  font-size: var(--text-lg);
  font-weight: 700;
  color: var(--color-wood);
}

.learn-overview__body {
  margin: 0;
  font-size: var(--text-sm);
  color: var(--color-ink-body);
  line-height: 1.65;
}

.learn-sections {
  display: grid;
  gap: var(--space-4);
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
}

/* ── Slide ── */
.slide {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  transform: rotate(var(--card-rotate, 0deg));
}

.slide__icon {
  font-size: 2.5rem;
  line-height: 1;
}

.slide__heading {
  margin: 0;
  font-size: var(--text-lg);
  font-weight: 700;
  color: var(--color-wood);
  line-height: 1.3;
}

.slide__body {
  margin: 0;
  font-size: var(--text-sm);
  color: var(--color-ink-body);
  line-height: 1.65;
}

.slide__panel {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  padding: var(--space-3);
  border-radius: var(--radius-md);
  background: var(--color-surface-soft);
  border: 1px solid var(--color-border);
}

.slide__panel--accent {
  background: var(--color-primary-soft);
  border-color: var(--color-primary-soft-strong);
}

.slide__panel-title {
  margin: 0;
  font-size: var(--text-sm);
  font-weight: 700;
  color: var(--color-wood);
}

.slide__list {
  margin: 0;
  padding-left: var(--space-4);
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.slide__list-item {
  font-size: var(--text-sm);
  color: var(--color-ink-body);
  line-height: 1.55;
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
  padding: var(--space-2) var(--space-4);
  font-weight: 700;
  font-size: var(--text-sm);
  cursor: pointer;
  transition: background var(--transition-fast), transform var(--transition-fast);
  min-height: 40px;
}
.nav-btn--back {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  color: var(--color-text-muted);
}
.nav-btn--back:hover { background: var(--color-bg); }

.nav-btn--next,
.nav-btn--start-quiz {
  background: var(--color-wood);
  color: var(--color-gold);
}
.nav-btn--next:hover,
.nav-btn--start-quiz:hover { background: var(--color-wood-mid); }
.nav-btn--start-quiz {
  background: var(--color-primary);
  color: var(--color-text-on-dark);
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
  gap: var(--space-3);
  transform: rotate(var(--card-rotate, 0deg));
  transition: border-color var(--transition-fast);
}
.question--correct { border-color: var(--color-success); }
.question--wrong   { border-color: var(--color-danger); }

.question__text {
  margin: 0;
  font-size: var(--text-base);
  font-weight: 700;
  color: var(--color-wood);
  line-height: 1.4;
}

/* ── Options ── */
.question__options {
  display: grid;
  gap: var(--space-2);
}

.option-btn {
  text-align: left;
  border: 2px solid var(--color-border);
  background: var(--color-surface);
  border-radius: var(--radius-md);
  padding: var(--space-3) var(--space-4);
  cursor: pointer;
  font-size: var(--text-sm);
  font-weight: 500;
  line-height: 1.4;
  transition: border-color var(--transition-fast), background var(--transition-fast), transform var(--transition-fast);
}
.option-btn:hover:not(:disabled) {
  border-color: var(--color-primary);
  background: var(--color-primary-light);
  transform: translateX(3px);
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
  transform: rotate(var(--card-rotate, 0deg));
}
.done-card__icon { font-size: 2.5rem; line-height: 1; }
.done-card__heading {
  margin: 0;
  font-size: var(--text-lg);
  font-weight: 700;
  color: var(--color-success);
}
.done-card__body {
  margin: 0;
  font-size: var(--text-sm);
  color: var(--color-ink-body);
  line-height: 1.65;
}

/* ── Transitions ── */
.question-slide-enter-active { transition: opacity 0.25s ease, transform 0.25s ease; }
.question-slide-leave-active { transition: opacity 0.15s ease, transform 0.15s ease; }
.question-slide-enter-from   { opacity: 0; transform: translateX(20px) rotate(var(--card-rotate, 0deg)); }
.question-slide-leave-to     { opacity: 0; transform: translateX(-12px) rotate(var(--card-rotate, 0deg)); }

.feedback-pop-enter-active { transition: opacity 0.2s ease, transform 0.2s ease; }
.feedback-pop-enter-from   { opacity: 0; transform: scale(0.85); }
</style>
