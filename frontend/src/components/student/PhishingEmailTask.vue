<template>
  <section class="phishing-task">
    <header class="phishing-task__header">
      <div class="phishing-task__header-icon" aria-hidden="true">📧</div>
      <div class="phishing-task__header-text">
        <h2 class="phishing-task__title">{{ task.stop?.name ?? 'Postkontoret' }}</h2>
        <p class="phishing-task__guidance">{{ task.guidanceText }}</p>
      </div>
    </header>

    <div class="phishing-task__workspace">
      <aside class="phishing-task__brief">
        <p class="phishing-task__brief-label">Oppdrag</p>
        <p class="phishing-task__brief-text">Klikk på alle delene av e-posten som virker mistenkelige før du sender svaret ditt.</p>
        <p class="phishing-task__brief-label">Status</p>
        <p class="phishing-task__brief-text">
          {{ flagged.size === 0 ? 'Ingen deler markert ennå.' : `Du har markert ${flagged.size} del${flagged.size === 1 ? '' : 'er'}.` }}
        </p>

        <div v-if="flagged.size > 0 && !result" class="phishing-task__chips" aria-live="polite">
          <span
            v-for="id in [...flagged]"
            :key="id"
            class="phishing-task__chip"
          >🚩 {{ clueLabel(id) }}</span>
        </div>
      </aside>

      <div class="phishing-task__panel">
        <p class="phishing-task__question">Klikk på de delene du synes er mistenkelige.</p>

        <article class="phishing-task__email" data-peek-trigger>
          <div class="phishing-task__window">
            <span class="phishing-task__window-dot" aria-hidden="true" />
            <span class="phishing-task__window-dot" aria-hidden="true" />
            <span class="phishing-task__window-dot" aria-hidden="true" />
            <span class="phishing-task__window-title">E-post</span>
          </div>

          <div class="phishing-task__meta">
            <div class="phishing-task__from-row">
              <span class="phishing-task__field-label">Fra:</span>
              <span class="phishing-task__sender-name">{{ email.fromName }}</span>
              <button
                v-if="senderClue"
                class="clue-btn"
                :class="{ 'clue-btn--flagged': flagged.has(senderClue.id), 'clue-btn--correct': isFeedbackCorrect(senderClue.id), 'clue-btn--missed': isFeedbackMissed(senderClue.id) }"
                :aria-pressed="flagged.has(senderClue.id)"
                :disabled="!!result"
                @click="toggleClue(senderClue.id)"
                :aria-label="`Flagg avsenderadresse som mistenkelig: ${senderClue.label}`"
              >&lt;{{ senderClue.label }}&gt;</button>
              <span v-else class="phishing-task__sender-email">&lt;{{ email.fromEmail }}&gt;</span>
            </div>

            <div class="phishing-task__subject-row">
              <span class="phishing-task__field-label">Emne:</span>
              <span>{{ email.subject }}</span>
            </div>
          </div>

          <p class="phishing-task__body">
            <template v-for="seg in bodySegments" :key="seg.key">
              <button
                v-if="seg.clueId"
                class="clue-btn"
                :class="{ 'clue-btn--flagged': flagged.has(seg.clueId), 'clue-btn--correct': isFeedbackCorrect(seg.clueId), 'clue-btn--missed': isFeedbackMissed(seg.clueId) }"
                :aria-pressed="flagged.has(seg.clueId)"
                :disabled="!!result"
                @click="toggleClue(seg.clueId)"
                :aria-label="`Flagg som mistenkelig: ${seg.text}`"
              >{{ seg.text }}</button>
              <span v-else>{{ seg.text }}</span>
            </template>
          </p>
        </article>

        <button
          v-if="!result"
          class="phishing-task__submit"
          :disabled="flagged.size === 0"
          @click="submit"
        >
          Send svar
        </button>
      </div>
    </div>

    <!-- Feedback after submit -->
    <Transition name="result-slide">
      <div
        v-if="result"
        class="pinned-note phishing-task__result"
        :class="result.correct ? 'phishing-task__result--correct' : 'phishing-task__result--wrong'"
        role="status"
        aria-live="polite"
        style="--card-rotate: 0.4deg"
      >
        <p class="phishing-task__result-label">
          {{ result.correct ? '✅ Riktig!' : '❌ Ikke helt riktig' }}
        </p>
        <p class="phishing-task__explanation">{{ result.explanation }}</p>
        <div v-if="resultStats.length" class="phishing-task__stats">
          <p
            v-for="stat in resultStats"
            :key="stat.key"
            class="phishing-task__stat"
            :class="`phishing-task__stat--${stat.tone}`"
          >
            <span class="phishing-task__stat-value">{{ stat.value }}</span>
            <span>{{ stat.label }}</span>
          </p>
        </div>

        <!-- Per-clue explanations -->
        <ul v-if="revealedClues.length" class="phishing-task__clue-list">
          <li v-for="clue in revealedClues" :key="clue.id" class="phishing-task__clue-item">
            <span :class="clue.iconClass">
              {{ clue.icon }}
            </span>
            <strong>{{ clue.label }}</strong>: {{ clue.explanation }}
          </li>
        </ul>

        <p v-if="result.stopCompleted" class="phishing-task__stop-msg">🎉 Du fullførte stoppet!</p>
        <button v-if="!result.correct" class="next-btn" @click="$emit('retry')">
          Prøv igjen
        </button>
        <button v-else class="next-btn" @click="$emit('next')">
          {{ isLastTask ? 'Videre til sammendrag →' : 'Neste oppgave →' }}
        </button>
      </div>
    </Transition>
  </section>
</template>

<script setup>
import { computed, reactive, watch } from 'vue'

const props = defineProps({
  task:       { type: Object,  required: true },
  result:     { type: Object,  default: null },
  isLastTask: { type: Boolean, default: false }
})

const emit = defineEmits(['submitted', 'next', 'retry', 'backToMap'])

const flagged = reactive(new Set())

const email     = computed(() => props.task?.contentJson?.email ?? {})
const allClues  = computed(() => email.value.clues ?? [])
const senderClue = computed(() => allClues.value.find(c => c.type === 'sender') ?? null)
const clueFeedbackById = computed(() => {
  const entries = props.result?.phishingClues ?? []
  return new Map(entries.map(clue => [clue.id, clue]))
})

watch(() => props.task?.id, () => { flagged.clear() }, { immediate: true })

const bodySegments = computed(() => {
  const body = email.value.body ?? ''
  const textClues = allClues.value.filter(c => c.type === 'text' || c.type === 'link')
  let segments = [{ text: body, clueId: null, key: 'body-start' }]
  for (const clue of textClues) {
    segments = segments.flatMap((seg, segIdx) => {
      if (seg.clueId !== null) return [seg]
      const idx = seg.text.indexOf(clue.label)
      if (idx === -1) return [seg]
      const parts = []
      if (idx > 0) parts.push({ text: seg.text.slice(0, idx), clueId: null, key: `${segIdx}-pre` })
      parts.push({ text: clue.label, clueId: clue.id, key: clue.id })
      const after = seg.text.slice(idx + clue.label.length)
      if (after) parts.push({ text: after, clueId: null, key: `${segIdx}-post` })
      return parts
    })
  }
  return segments
})

const correctClueIds = computed(() => new Set(props.result?.correctClueIds ?? []))

function isFeedbackCorrect(id) {
  if (!props.result) return false
  return flagged.has(id) && correctClueIds.value.has(id)
}

function isFeedbackMissed(id) {
  if (!props.result) return false
  return correctClueIds.value.has(id) && !flagged.has(id)
}

const revealedClues = computed(() => {
  if (!props.result) return []
  return allClues.value
    .filter(c => flagged.has(c.id))
    .map(c => {
      const isCorrectClue = correctClueIds.value.has(c.id)
      const wasFlagged = flagged.has(c.id)

      if (isCorrectClue && wasFlagged) {
        return {
          id: c.id,
          label: c.label,
          explanation: clueFeedbackById.value.get(c.id)?.explanation ?? '',
          icon: '✅',
          iconClass: 'phishing-task__clue-icon--ok'
        }
      }

      if (wasFlagged) {
        const clueFeedback = clueFeedbackById.value.get(c.id)
        if (clueFeedback?.isClue) {
          return {
            id: c.id,
            label: c.label,
            explanation: clueFeedback.explanation,
            icon: '💡',
            iconClass: 'phishing-task__clue-icon--optional'
          }
        }

        return {
          id: c.id,
          label: c.label,
          explanation: clueFeedback?.explanation ?? '',
          icon: '❌',
          iconClass: 'phishing-task__clue-icon--wrong'
        }
      }
    })
})

const resultStats = computed(() => {
  if (!props.result) return []

  const flaggedIds = [...flagged]
  const correctCount = flaggedIds.filter(id => correctClueIds.value.has(id)).length
  const wrongCount = flaggedIds.filter(id => !correctClueIds.value.has(id)).length
  const missedCount = [...correctClueIds.value].filter(id => !flagged.has(id)).length

  const stats = []

  if (correctCount > 0) {
    stats.push({
      key: 'correct',
      tone: 'correct',
      value: correctCount,
      label: correctCount === 1 ? 'riktig valg' : 'riktige valg',
    })
  }

  if (wrongCount > 0) {
    stats.push({
      key: 'wrong',
      tone: 'wrong',
      value: wrongCount,
      label: wrongCount === 1 ? 'feil valg' : 'feil valg',
    })
  }

  if (missedCount > 0) {
    stats.push({
      key: 'missed',
      tone: 'missed',
      value: missedCount,
      label: missedCount === 1 ? 'riktig valg manglet' : 'riktige valg manglet',
    })
  }

  return stats
})

function toggleClue(id) {
  if (props.result) return
  if (flagged.has(id)) {
    flagged.delete(id)
  } else {
    flagged.add(id)
  }
  console.log('[PhishingEmailTask] Toggled clue', id, '— flagged:', [...flagged])
}

function clueLabel(id) {
  return allClues.value.find(c => c.id === id)?.label ?? id
}

function submit() {
  if (flagged.size === 0) return
  const flaggedClueIds = [...flagged]
  console.log('[PhishingEmailTask] Submitting flaggedClueIds:', flaggedClueIds)
  emit('submitted', { flaggedClueIds })
}
</script>

<style scoped>
.phishing-task {
  display: grid;
  gap: var(--space-4);
}

.phishing-task__header {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-lg);
  background: linear-gradient(135deg, var(--color-primary-soft) 0%, var(--color-surface) 100%);
  border: 1.5px solid var(--color-primary-soft-strong);
}

.phishing-task__header-icon {
  flex-shrink: 0;
  font-size: 2rem;
  line-height: 1;
}

.phishing-task__header-text {
  display: grid;
  gap: var(--space-1);
}

.phishing-task__title {
  margin: 0;
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  color: var(--color-heading);
  line-height: 1.2;
}

.phishing-task__guidance {
  margin: 0;
  color: var(--color-text-muted);
  line-height: 1.4;
}

.phishing-task__workspace {
  display: grid;
  grid-template-columns: minmax(220px, 280px) minmax(0, 1fr);
  gap: var(--space-4);
  align-items: start;
}

.phishing-task__brief,
.phishing-task__panel,
.phishing-task__result {
  border: 2px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  box-shadow: 0 14px 28px color-mix(in srgb, var(--color-primary-focus-ring) 20%, transparent);
}

.phishing-task__brief,
.phishing-task__panel {
  padding: var(--space-4);
}

.phishing-task__brief {
  display: grid;
  gap: var(--space-3);
  position: sticky;
  top: var(--space-4);
}

.phishing-task__brief-label {
  margin: 0;
  color: var(--color-text-muted);
  font-size: var(--text-xs);
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.phishing-task__brief-text {
  margin: 0;
  color: var(--color-ink);
  line-height: 1.5;
}

.phishing-task__panel {
  display: grid;
  gap: var(--space-3);
}

.phishing-task__question {
  margin: 0;
  font-weight: var(--font-semibold);
  color: var(--color-heading);
}

.phishing-task__email {
  display: grid;
  gap: var(--space-3);
  padding: var(--space-4);
  border: 2px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.phishing-task__window {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  padding-bottom: var(--space-3);
  border-bottom: 1px solid var(--color-border);
}

.phishing-task__window-dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: var(--color-border-strong);
}

.phishing-task__window-title {
  margin-left: var(--space-1);
  color: var(--color-text-muted);
  font-size: var(--text-xs);
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.phishing-task__meta {
  display: grid;
  gap: var(--space-2);
}

.phishing-task__from-row,
.phishing-task__subject-row {
  display: flex;
  align-items: baseline;
  flex-wrap: wrap;
  gap: var(--space-1);
}

.phishing-task__field-label {
  font-weight: 700;
  color: var(--color-text-muted);
  flex-shrink: 0;
}

.phishing-task__sender-name { color: var(--color-ink-body); }
.phishing-task__sender-email { color: var(--color-ink-subtle); font-family: monospace; }

.phishing-task__subject-row span:last-child {
  font-weight: 600;
  color: var(--color-ink-body);
}

.phishing-task__body {
  margin: var(--space-2) 0 0;
  line-height: 1.7;
  color: var(--color-ink-body);
}

.clue-btn {
  display: inline;
  background: var(--color-clue-bg);
  border: 2px solid var(--color-clue-border);
  border-radius: 8px;
  padding: 2px 7px;
  color: inherit;
  font: inherit;
  cursor: pointer;
  transition: background var(--transition-fast), border-color var(--transition-fast), box-shadow var(--transition-fast), transform var(--transition-fast);
}
.clue-btn:hover:not(:disabled) {
  background: var(--color-clue-hover-bg);
  border-color: var(--color-primary);
  box-shadow: 0 8px 18px color-mix(in srgb, var(--color-primary-focus-ring) 25%, transparent);
  transform: translateY(-1px);
}
.clue-btn:focus-visible {
  outline: 3px solid var(--color-gold);
  outline-offset: 2px;
}
.clue-btn--flagged {
  background: var(--color-clue-flagged-bg);
  border-color: var(--color-danger);
  color: var(--color-clue-flagged-text);
  font-weight: 600;
}
.clue-btn--correct {
  background: var(--color-clue-correct-bg);
  border-color: var(--color-success);
  color: var(--color-clue-correct-text);
}
.clue-btn--missed {
  background: var(--color-clue-missed-bg);
  border-color: var(--color-clue-missed-border);
  animation: clue-pulse 0.6s ease-out;
}
@keyframes clue-pulse {
  0%, 100% { transform: scale(1); }
  50%       { transform: scale(1.05); }
}

.phishing-task__chips {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}
.phishing-task__chip {
  background: var(--color-danger-light);
  color: var(--color-danger-dark);
  border: 1px solid var(--color-danger);
  border-radius: var(--radius-full);
  padding: 5px 10px;
  font-size: var(--text-xs);
  font-weight: 700;
}

.phishing-task__submit {
  align-self: flex-start;
  background: var(--color-primary);
  color: var(--color-text-on-dark);
  border: 0;
  border-radius: var(--radius-md);
  padding: var(--space-2) var(--space-6);
  font-size: var(--text-base);
  font-weight: 700;
  cursor: pointer;
  min-height: 44px;
  transition: background var(--transition-fast), transform var(--transition-fast);
}
.phishing-task__submit:hover:not(:disabled) { background: var(--color-primary-dark); transform: translateY(-1px); }
.phishing-task__submit:disabled { opacity: 0.5; cursor: not-allowed; }
.phishing-task__submit:focus-visible { outline: 3px solid var(--color-primary-soft-strong); outline-offset: 2px; }

.phishing-task__result {
  display: grid;
  gap: var(--space-3);
  padding: var(--space-4);
}
.phishing-task__result--correct { border-color: var(--color-success); }
.phishing-task__result--wrong   { border-color: var(--color-danger); }

.phishing-task__result-label {
  margin: 0;
  font-size: var(--text-lg);
  font-weight: 700;
}
.phishing-task__result--correct .phishing-task__result-label { color: var(--color-success); }
.phishing-task__result--wrong   .phishing-task__result-label { color: var(--color-danger); }

.phishing-task__explanation { margin: 0; font-size: var(--text-sm); color: var(--color-ink); line-height: 1.5; }
.phishing-task__stop-msg { margin: 0; font-weight: 600; color: var(--color-success); }

.phishing-task__stats {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.phishing-task__stat {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-full);
  font-size: var(--text-sm);
  font-weight: 700;
}

.phishing-task__stat-value {
  display: inline-grid;
  place-items: center;
  min-width: 1.8rem;
  height: 1.8rem;
  padding: 0 0.3rem;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.88);
  font-weight: 800;
}

.phishing-task__stat--correct {
  color: var(--color-success-dark);
  background: var(--color-success-light);
  border-color: var(--color-success);
}

.phishing-task__stat--wrong {
  color: var(--color-danger-dark);
  background: var(--color-danger-light);
  border-color: var(--color-danger);
}

.phishing-task__stat--missed {
  color: var(--color-warning);
  background: var(--color-warning-light);
  border-color: var(--color-warning);
}

.phishing-task__clue-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}
.phishing-task__clue-item {
  display: flex;
  gap: var(--space-2);
  font-size: var(--text-sm);
  color: var(--color-ink);
  line-height: 1.4;
}
.phishing-task__clue-icon--ok     { flex-shrink: 0; }
.phishing-task__clue-icon--wrong  { flex-shrink: 0; }
.phishing-task__clue-icon--missed { flex-shrink: 0; }
.phishing-task__clue-icon--optional { flex-shrink: 0; }

.next-btn {
  align-self: flex-start;
  background: var(--color-primary);
  color: var(--color-text-on-dark);
  border: none;
  border-radius: var(--radius-md);
  padding: var(--space-2) var(--space-6);
  font-weight: 700;
  font-size: var(--text-base);
  cursor: pointer;
  min-height: 44px;
  transition: background var(--transition-fast), transform var(--transition-fast);
}
.next-btn:hover  { background: var(--color-primary-dark); }
.next-btn:active { transform: scale(0.98); }
.next-btn:focus-visible { outline: 3px solid var(--color-primary-soft-strong); outline-offset: 2px; }

.result-slide-enter-active { transition: transform 0.3s ease, opacity 0.3s ease; }
.result-slide-leave-active { transition: transform 0.2s ease, opacity 0.2s ease; }
.result-slide-enter-from   { transform: translateY(-12px); opacity: 0; }
.result-slide-leave-to     { transform: translateY(-8px);  opacity: 0; }

@media (max-width: 920px) {
  .phishing-task__workspace {
    grid-template-columns: 1fr;
  }

  .phishing-task__brief {
    position: static;
  }
}
</style>
