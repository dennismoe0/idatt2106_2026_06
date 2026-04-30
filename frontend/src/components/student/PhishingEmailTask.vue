<template>
  <section class="phishing-task">
    <p class="phishing-task__guidance">{{ task.guidanceText }}</p>

    <!-- Email card -->
    <article class="pinned-note phishing-task__email" style="--card-rotate: -0.3deg" data-peek-trigger>
      <!-- From row -->
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

      <!-- Subject -->
      <div class="phishing-task__subject-row">
        <span class="phishing-task__field-label">Emne:</span>
        <span>{{ email.subject }}</span>
      </div>

      <!-- Body with inline clue spans -->
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

    <!-- Clue flag chips (visual feedback) -->
    <div v-if="flagged.size > 0 && !result" class="phishing-task__chips" aria-live="polite">
      <span
        v-for="id in [...flagged]"
        :key="id"
        class="phishing-task__chip"
      >🚩 {{ clueLabel(id) }}</span>
    </div>

    <!-- Submit button -->
    <button
      v-if="!result"
      class="phishing-task__submit"
      :disabled="flagged.size === 0"
      @click="submit"
    >
      Send svar
    </button>

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
import { computed, reactive, ref, watch } from 'vue'

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
    .filter(c => correctClueIds.value.has(c.id) || flagged.has(c.id))
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

      return {
        id: c.id,
        label: c.label,
        explanation: clueFeedbackById.value.get(c.id)?.explanation ?? '',
        icon: '🔎',
        iconClass: 'phishing-task__clue-icon--missed'
      }
    })
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
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.phishing-task__guidance {
  margin: 0;
  color: var(--color-cork-dark);
  font-weight: 600;
}

.phishing-task__email {
  transform: rotate(var(--card-rotate, 0deg));
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  font-size: var(--text-sm);
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
  color: var(--color-wood);
  flex-shrink: 0;
}

.phishing-task__sender-name { color: var(--color-ink-body); }
.phishing-task__sender-email { color: var(--color-ink-subtle); font-family: monospace; }

.phishing-task__subject-row span:last-child {
  font-weight: 600;
  color: #333;
}

.phishing-task__body {
  margin: var(--space-2) 0 0;
  line-height: 1.7;
  color: var(--color-ink-body);
}

/* Clue inline buttons */
.clue-btn {
  display: inline;
  background: var(--color-clue-bg);
  border: 1.5px dashed var(--color-clue-border);
  border-radius: 3px;
  padding: 1px 5px;
  color: inherit;
  font: inherit;
  cursor: pointer;
  transition: background var(--transition-fast), border-color var(--transition-fast);
}
.clue-btn:hover:not(:disabled) {
  background: var(--color-clue-hover-bg);
  border-style: solid;
}
.clue-btn:focus-visible {
  outline: 3px solid var(--color-gold);
  outline-offset: 2px;
}
.clue-btn--flagged {
  background: var(--color-clue-flagged-bg);
  border-color: var(--color-danger);
  border-style: solid;
  color: var(--color-clue-flagged-text);
  font-weight: 600;
}
.clue-btn--correct {
  background: var(--color-clue-correct-bg);
  border-color: var(--color-success);
  border-style: solid;
  color: var(--color-clue-correct-text);
}
.clue-btn--missed {
  background: var(--color-clue-missed-bg);
  border-color: var(--color-clue-missed-border);
  border-style: solid;
  animation: clue-pulse 0.6s ease-out;
}
@keyframes clue-pulse {
  0%, 100% { transform: scale(1); }
  50%       { transform: scale(1.05); }
}

/* Chips */
.phishing-task__chips {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}
.phishing-task__chip {
  background: var(--color-danger);
  color: #fff;
  border-radius: var(--radius-full);
  padding: 2px 10px;
  font-size: var(--text-xs);
  font-weight: 600;
}

/* Submit */
.phishing-task__submit {
  align-self: flex-start;
  background: var(--color-wood);
  color: var(--color-gold);
  border: none;
  border-radius: var(--radius-md);
  padding: var(--space-2) var(--space-6);
  font-size: var(--text-base);
  font-weight: 700;
  cursor: pointer;
  min-height: 44px;
  transition: background var(--transition-fast);
}
.phishing-task__submit:hover:not(:disabled) { background: var(--color-wood-mid); }
.phishing-task__submit:disabled { opacity: 0.5; cursor: not-allowed; }
.phishing-task__submit:focus-visible { outline: 3px solid var(--color-gold); outline-offset: 2px; }

/* Result note */
.phishing-task__result {
  transform: rotate(var(--card-rotate, 0deg));
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
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

/* Clue reveal list */
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

/* Slide transition */
.result-slide-enter-active { transition: transform 0.3s ease, opacity 0.3s ease; }
.result-slide-leave-active { transition: transform 0.2s ease, opacity 0.2s ease; }
.result-slide-enter-from   { transform: translateY(-12px); opacity: 0; }
.result-slide-leave-to     { transform: translateY(-8px);  opacity: 0; }
</style>
