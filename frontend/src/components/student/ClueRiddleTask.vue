<template>
  <section class="clue-board" :style="boardStyle">
    <div class="clue-board__shade" aria-hidden="true" />

    <div class="clue-board__content">
      <header class="clue-board__header">
        <p class="clue-board__eyebrow">Gåtespor {{ content.caseNumber ?? '' }}</p>
        <h2>{{ task.title }}</h2>
        <p>{{ task.description }}</p>
      </header>

      <main class="clue-board__workspace">
        <aside class="case-file" aria-label="Saksmappe">
          <span class="case-file__label">Oppdrag</span>
          <p v-if="content.purpose">{{ content.purpose }}</p>
          <span class="case-file__label">Bevismappe</span>
          <p>{{ content.evidence }}</p>
          <div v-if="passwordValue" class="password-chip">
            <span>Passord</span>
            <strong>{{ passwordValue }}</strong>
          </div>
          <ul v-if="timeline.length" class="timeline">
            <li v-for="entry in timeline" :key="entry.time + entry.text">
              <span>{{ entry.time }}</span>
              <p>{{ entry.text }}</p>
            </li>
          </ul>
        </aside>

        <section class="investigation-panel" aria-label="Gåteoppgave">
          <div class="question-strip">
            <span>{{ selectionMode === 'multi' ? 'Velg alle som passer' : 'Velg ett svar' }}</span>
            <h3>{{ content.question }}</h3>
          </div>

          <div class="evidence-grid" :class="`evidence-grid--${variant}`">
            <button
              v-for="(option, index) in options"
              :key="option.id"
              type="button"
              class="evidence-card"
              :class="[
                `evidence-card--${variant}`,
                { 'evidence-card--selected': isSelected(option.id) },
              ]"
              :disabled="!!result"
              :aria-pressed="isSelected(option.id)"
              @click="toggleOption(option.id)"
            >
              <span v-if="isSelected(option.id)" class="evidence-card__selected-mark" aria-hidden="true">Valgt</span>
              <span class="evidence-card__pin" aria-hidden="true" />
              <span class="evidence-card__badge">{{ option.letter ?? optionBadge(index) }}</span>

              <template v-if="variant === 'photo'">
                <img
                  v-if="option.imageUrl"
                  class="evidence-card__image"
                  :src="option.imageUrl"
                  :alt="option.alt ?? option.label"
                />
                <div v-else class="evidence-card__image evidence-card__image--placeholder">
                  {{ option.label }}
                </div>
                <strong>{{ option.label }}</strong>
                <span v-if="option.detail">{{ option.detail }}</span>
              </template>

              <template v-else-if="variant === 'email'">
                <div class="mail-card">
                  <span>Fra: {{ option.from }}</span>
                  <span>Til: {{ option.to ?? 'ordforer@dyreby.kommune' }}</span>
                  <strong>{{ option.subject }}</strong>
                  <p>{{ option.body }}</p>
                  <small v-if="option.flag">{{ option.flag }}</small>
                </div>
              </template>

              <template v-else-if="variant === 'shop'">
                <div class="shop-card">
                  <span class="shop-card__url">{{ option.url }}</span>
                  <strong>{{ option.headline }}</strong>
                  <p>{{ option.body }}</p>
                  <div class="shop-card__meta">
                    <span>{{ option.price }}</span>
                    <span>{{ option.payment }}</span>
                  </div>
                </div>
              </template>

              <template v-else-if="variant === 'social'">
                <div class="social-card">
                  <div class="social-card__head">
                    <span class="social-card__avatar">{{ option.initials }}</span>
                    <div>
                      <strong>{{ option.username }}</strong>
                      <span>{{ option.handle }}</span>
                    </div>
                    <mark v-if="option.verified">verifisert</mark>
                  </div>
                  <p>{{ option.body }}</p>
                  <small>{{ option.metrics }}</small>
                </div>
              </template>

              <template v-else-if="variant === 'password'">
                <div class="password-card">
                  <strong>{{ option.label }}</strong>
                  <span>{{ option.detail }}</span>
                </div>
              </template>

              <template v-else>
                <strong>{{ option.label }}</strong>
                <span>{{ option.detail }}</span>
              </template>
            </button>
          </div>

          <button
            v-if="!result"
            type="button"
            class="submit-btn"
            :disabled="!canSubmit"
            @click="submit"
          >
            Sjekk sporet
          </button>
        </section>
      </main>

      <Transition name="case-result">
        <section
          v-if="result"
          class="case-result"
          :class="result.correct ? 'case-result--correct' : 'case-result--wrong'"
          role="status"
          aria-live="polite"
        >
          <div>
            <p class="case-result__label">{{ result.correct ? 'Spor bekreftet' : 'Sporet holder ikke' }}</p>
            <h3>{{ result.correct ? correctFeedbackTitle : 'Se nærmere på beviset' }}</h3>
            <p>{{ resultMessage }}</p>
          </div>

          <template v-if="result.correct">
            <div v-if="resultLines.length" class="result-lines">
              <span>Hva fant vi ut?</span>
              <p v-for="line in resultLines" :key="line">{{ line }}</p>
            </div>

            <div class="case-explanation">
              <article v-if="content.logic" class="case-explanation__logic">
                <span>Forklaring</span>
                <p>{{ content.logic }}</p>
              </article>

              <article v-if="content.elimination" class="case-explanation__elimination">
                <span>{{ content.elimination === 'Ingen fjernes.' ? 'Mistenkte igjen' : 'Mistenkt eliminert' }}</span>
                <p>{{ content.elimination }}</p>
              </article>

              <article v-if="content.final" class="case-explanation__final">
                <span>Endelig konklusjon</span>
                <p>{{ content.final }}</p>
              </article>
            </div>

            <article v-if="content.clue" class="clue-reveal">
              <span>Clue låst opp</span>
              <div>
                <strong>Spor til mistenktmappen</strong>
                <p>{{ content.clue }}</p>
              </div>
            </article>
          </template>

          <div class="case-result__actions">
            <button v-if="!result.correct" type="button" @click="resetAnswer">Prøv igjen</button>
            <button v-else type="button" @click="$emit('next')">
              {{ isLastTask ? 'Videre til sammendrag' : 'Neste oppgave' }}
            </button>
          </div>
        </section>
      </Transition>
    </div>
  </section>
</template>

<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  task: { type: Object, required: true },
  result: { type: Object, default: null },
  isLastTask: { type: Boolean, default: false },
})

const emit = defineEmits(['submitted', 'next', 'tryAgain'])

const selected = ref(new Set())
const content = computed(() => props.task?.contentJson ?? {})
const options = computed(() => content.value.options ?? [])
const variant = computed(() => content.value.variant ?? 'news')
const selectionMode = computed(() => content.value.selectionMode ?? 'single')
const timeline = computed(() => content.value.timeline ?? [])
const passwordValue = computed(() => content.value.password ?? content.value.evidencePassword ?? '')
const resultLines = computed(() => splitLines(content.value.result))
const correctFeedbackTitle = computed(() => content.value.correctTitle ?? 'Riktig')
const boardStyle = computed(() => ({
  '--clue-board-bg': `url("${content.value.backgroundImage ?? '/story_pictures/clue-board-bg.png'}")`,
}))
const canSubmit = computed(() => selected.value.size > 0)
const selectedOption = computed(() => {
  const [first] = selected.value
  return options.value.find((option) => option.id === first) ?? null
})
const resultMessage = computed(() => {
  if (!props.result) return ''
  if (props.result.correct) return props.result.explanation
  return selectedOption.value?.detail
    ? `Ikke helt. ${selectedOption.value.detail}`
    : 'Ikke helt. Sjekk kilden, tidspunktet og hvilket spor som faktisk kan bekreftes.'
})

watch(() => props.task?.id, () => {
  selected.value = new Set()
}, { immediate: true })

watch(() => props.result, (value) => {
  if (!value) selected.value = new Set()
})

function toggleOption(optionId) {
  const next = new Set(selected.value)
  if (selectionMode.value === 'multi') {
    if (next.has(optionId)) next.delete(optionId)
    else next.add(optionId)
  } else {
    next.clear()
    next.add(optionId)
  }
  selected.value = next
}

function isSelected(optionId) {
  return selected.value.has(optionId)
}

function submit() {
  if (!canSubmit.value) return
  const answer = selectionMode.value === 'multi'
    ? { selected: [...selected.value] }
    : { selected: [...selected.value][0] }
  emit('submitted', answer)
}

function resetAnswer() {
  selected.value = new Set()
  emit('tryAgain')
}

function optionBadge(index) {
  return String.fromCharCode(65 + index)
}

function splitLines(value) {
  if (Array.isArray(value)) return value
  return String(value ?? '')
    .split('\n')
    .map((line) => line.trim())
    .filter(Boolean)
}
</script>

<style scoped>
.clue-board {
  position: relative;
  min-height: calc(100vh - 54px);
  overflow: hidden;
  border-radius: 0;
  background-image: var(--clue-board-bg);
  background-position: center;
  background-size: cover;
  color: #f7ead0;
  isolation: isolate;
}

.clue-board__shade {
  position: absolute;
  inset: 0;
  z-index: -1;
  background:
    linear-gradient(90deg, rgba(0, 0, 0, 0.58), rgba(0, 0, 0, 0.18) 46%, rgba(0, 0, 0, 0.5)),
    linear-gradient(180deg, rgba(0, 0, 0, 0.12), rgba(0, 0, 0, 0.44));
}

.clue-board__content {
  display: grid;
  gap: 1rem;
  padding: clamp(4rem, 7vw, 5.5rem) clamp(1rem, 3vw, 2.5rem) clamp(1rem, 2.5vw, 2rem);
}

.clue-board__header {
  max-width: 58rem;
}

.clue-board__eyebrow,
.case-file__label,
.case-result__label,
.result-lines span,
.case-explanation span,
.clue-reveal > span {
  margin: 0;
  color: #f3c873;
  font-size: 0.78rem;
  font-weight: 800;
  letter-spacing: 0;
  text-transform: uppercase;
}

.clue-board h2,
.clue-board h3,
.clue-board p {
  margin-top: 0;
}

.clue-board h2 {
  margin-bottom: 0.55rem;
  color: #fff4dd;
  font-size: clamp(1.7rem, 3vw, 2.45rem);
  line-height: 1.1;
}

.clue-board__header p {
  max-width: 52rem;
  margin-bottom: 0;
  color: #f1dec0;
  font-size: 1rem;
  line-height: 1.55;
}

.clue-board__workspace {
  display: grid;
  grid-template-columns: minmax(15rem, 0.8fr) minmax(0, 2fr);
  gap: 1rem;
  align-items: start;
}

.case-file,
.investigation-panel,
.case-result {
  border: 1px solid rgba(255, 238, 205, 0.38);
  border-radius: 8px;
  background: rgba(4, 5, 7, 0.66);
  box-shadow: 0 18px 42px rgba(0, 0, 0, 0.32);
  backdrop-filter: blur(7px);
}

.case-file {
  display: grid;
  gap: 1rem;
  padding: 1rem;
}

.case-file p {
  margin-bottom: 0;
  color: #f7ead0;
  line-height: 1.55;
}

.password-chip {
  display: grid;
  gap: 0.35rem;
  padding: 0.8rem;
  border: 1px dashed rgba(255, 227, 179, 0.5);
  border-radius: 6px;
  background: rgba(255, 244, 221, 0.1);
}

.password-chip span {
  color: #d6b98a;
  font-size: 0.75rem;
  font-weight: 800;
  text-transform: uppercase;
}

.password-chip strong {
  color: #fff4dd;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: clamp(1rem, 2vw, 1.3rem);
}

.timeline {
  display: grid;
  gap: 0.65rem;
  margin: 0;
  padding: 0;
  list-style: none;
}

.timeline li {
  display: grid;
  grid-template-columns: 3rem 1fr;
  gap: 0.6rem;
  align-items: start;
}

.timeline span {
  color: #f3c873;
  font-weight: 800;
}

.timeline p {
  color: #f1dec0;
}

.investigation-panel {
  display: grid;
  gap: 1rem;
  padding: 1rem;
}

.question-strip {
  display: grid;
  gap: 0.35rem;
}

.question-strip span {
  color: #d6b98a;
  font-size: 0.8rem;
  font-weight: 800;
  text-transform: uppercase;
}

.question-strip h3 {
  margin-bottom: 0;
  color: #fff4dd;
  font-size: clamp(1.25rem, 2.3vw, 1.75rem);
  line-height: 1.2;
}

.evidence-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0.85rem;
}

.evidence-grid--email,
.evidence-grid--shop,
.evidence-grid--social {
  grid-template-columns: repeat(auto-fit, minmax(15.5rem, 1fr));
}

.evidence-card {
  position: relative;
  min-height: 12rem;
  display: grid;
  align-content: start;
  gap: 0.65rem;
  padding: 1rem;
  border: 2px solid rgba(73, 40, 22, 0.4);
  border-radius: 6px;
  background: #ead4ad;
  color: #2a1a10;
  text-align: left;
  cursor: pointer;
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.28);
  transition: transform 0.16s ease, border-color 0.16s ease, box-shadow 0.16s ease;
}

.evidence-card:hover:not(:disabled) {
  transform: translateY(-3px) rotate(-0.25deg);
  box-shadow: 0 18px 34px rgba(0, 0, 0, 0.36);
}

.evidence-card:disabled {
  cursor: default;
}

.evidence-card--selected {
  border-color: #ffffff;
  background:
    linear-gradient(0deg, rgba(17, 94, 89, 0.16), rgba(17, 94, 89, 0.16)),
    #fff1c2;
  transform: translateY(-4px);
  box-shadow:
    0 0 0 5px #0f766e,
    0 0 0 9px rgba(255, 255, 255, 0.9),
    0 22px 42px rgba(0, 0, 0, 0.42);
}

.evidence-card--selected::after {
  content: "";
  position: absolute;
  inset: 0.45rem;
  border: 3px solid rgba(15, 118, 110, 0.9);
  border-radius: 4px;
  pointer-events: none;
}

.evidence-card__selected-mark {
  position: absolute;
  top: 0.55rem;
  right: 0.55rem;
  z-index: 2;
  display: inline-grid;
  place-items: center;
  min-width: 4.6rem;
  min-height: 2rem;
  padding: 0.2rem 0.55rem;
  border: 2px solid #fff;
  border-radius: 999px;
  background: #0f766e;
  color: #fff;
  font-size: 0.78rem;
  font-weight: 900;
  text-transform: uppercase;
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.32);
}

.evidence-card__selected-mark::before {
  content: "✓";
  margin-right: 0.3rem;
}

.evidence-card__pin {
  position: absolute;
  top: 0.45rem;
  left: 50%;
  width: 0.8rem;
  height: 0.8rem;
  border-radius: 50%;
  background: #a5241f;
  box-shadow: 0 2px 0 rgba(0, 0, 0, 0.28);
}

.evidence-card__badge {
  display: inline-grid;
  place-items: center;
  width: 2rem;
  height: 2rem;
  border-radius: 50%;
  background: #2d2117;
  color: #ffe4a3;
  font-weight: 900;
}

.evidence-card strong {
  color: #25180f;
  font-size: 1rem;
  line-height: 1.3;
}

.evidence-card span {
  line-height: 1.38;
}

.evidence-card__image {
  width: 100%;
  aspect-ratio: 4 / 3;
  object-fit: cover;
  border-radius: 4px;
  background: #20150f;
}

.evidence-card__image--placeholder {
  display: grid;
  place-items: center;
  padding: 1rem;
  color: #ffe4a3;
  font-weight: 900;
}

.mail-card,
.shop-card,
.social-card,
.password-card {
  display: grid;
  gap: 0.55rem;
}

.mail-card span,
.mail-card small,
.shop-card__url,
.social-card small {
  color: #5f4631;
  font-size: 0.82rem;
  font-weight: 700;
}

.mail-card p,
.shop-card p,
.social-card p {
  margin-bottom: 0;
  color: #2a1a10;
  line-height: 1.45;
}

.shop-card__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 0.45rem;
}

.shop-card__meta span {
  padding: 0.28rem 0.45rem;
  border-radius: 4px;
  background: rgba(45, 33, 23, 0.1);
  font-weight: 800;
}

.social-card__head {
  display: grid;
  grid-template-columns: 2.4rem 1fr auto;
  gap: 0.65rem;
  align-items: center;
}

.social-card__avatar {
  display: grid;
  place-items: center;
  width: 2.4rem;
  height: 2.4rem;
  border-radius: 50%;
  background: #2d2117;
  color: #ffe4a3;
  font-weight: 900;
}

.social-card__head div {
  display: grid;
}

.social-card mark {
  padding: 0.2rem 0.45rem;
  border-radius: 4px;
  background: #d7f4db;
  color: #165822;
  font-size: 0.72rem;
  font-weight: 800;
}

.submit-btn,
.case-result__actions button {
  justify-self: start;
  min-height: 46px;
  padding: 0.75rem 1.15rem;
  border: 0;
  border-radius: 6px;
  background: #f3c873;
  color: #22160e;
  font-weight: 900;
  cursor: pointer;
}

.submit-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.case-result {
  display: grid;
  gap: 1rem;
  padding: 1rem;
}

.case-result--correct {
  border-color: rgba(133, 220, 150, 0.75);
}

.case-result--wrong {
  border-color: rgba(255, 132, 112, 0.75);
}

.case-result h3 {
  margin-bottom: 0.35rem;
  color: #fff4dd;
  font-size: 1.35rem;
}

.case-result p {
  margin-bottom: 0;
  color: #f1dec0;
  line-height: 1.55;
}

.result-lines,
.case-explanation {
  display: grid;
  gap: 0.75rem;
}

.result-lines {
  padding: 0.85rem;
  border-radius: 6px;
  background: rgba(255, 244, 221, 0.1);
}

.case-explanation {
  grid-template-columns: repeat(auto-fit, minmax(13rem, 1fr));
}

.case-explanation article,
.clue-reveal {
  display: grid;
  gap: 0.4rem;
  padding: 0.85rem;
  border-radius: 6px;
  background: rgba(255, 244, 221, 0.08);
}

.case-explanation__elimination {
  border: 1px solid rgba(255, 198, 105, 0.5);
  background: rgba(99, 57, 12, 0.34) !important;
}

.case-explanation__final {
  border: 1px solid rgba(133, 220, 150, 0.72);
  background: rgba(12, 82, 43, 0.34) !important;
}

.clue-reveal {
  grid-template-columns: minmax(7rem, auto) 1fr;
  align-items: start;
  border: 1px solid rgba(133, 220, 150, 0.72);
  background: rgba(7, 58, 45, 0.58);
}

.clue-reveal strong {
  display: block;
  margin-bottom: 0.25rem;
  color: #fff4dd;
}

.case-result__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.case-result-enter-active,
.case-result-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.case-result-enter-from,
.case-result-leave-to {
  opacity: 0;
  transform: translateY(0.5rem);
}

@media (max-width: 920px) {
  .clue-board__workspace {
    grid-template-columns: 1fr;
  }

  .evidence-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .clue-board {
    border-radius: 0;
  }

  .clue-board__content {
    padding: 0.85rem;
  }

  .submit-btn,
  .case-result__actions button {
    width: 100%;
  }
}
</style>
