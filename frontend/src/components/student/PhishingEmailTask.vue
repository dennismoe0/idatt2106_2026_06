<template>
  <section class="task-card">
    <h2>Phishing-epost</h2>
    <p class="guidance">{{ task.guidanceText }}</p>

    <article class="email-card" :class="{ 'email-card--answered': result }">
      <p><strong>Fra:</strong> {{ email.fromName }}</p>
      <button
        class="flag-toggle"
        :class="{ flagged: flaggedItems.includes('fromEmail') }"
        :disabled="!!result"
        @click="toggleFlag('fromEmail')"
      >{{ email.fromEmail }}</button>
      <p><strong>Emne:</strong> {{ email.subject }}</p>
      <p class="body">{{ email.body }}</p>
    </article>

    <div class="actions">
      <button
        v-for="opt in ACTION_OPTIONS"
        :key="opt.value"
        :class="{ selected: action === opt.value }"
        :disabled="!!result"
        @click="action = opt.value"
      >{{ opt.label }}</button>
    </div>

    <button v-if="!result" class="submit-btn" :disabled="!action" @click="submit">
      Send svar
    </button>

    <!-- Inline result -->
    <Transition name="result-slide">
      <div
        v-if="result"
        class="inline-result"
        :class="result.correct ? 'inline-result--correct' : 'inline-result--wrong'"
        role="status"
        aria-live="polite"
      >
        <p class="inline-result__label">
          {{ result.correct ? '✅ Riktig!' : '❌ Ikke helt riktig' }}
        </p>
        <p class="inline-result__explanation">{{ result.explanation }}</p>
        <p v-if="result.stopCompleted" class="inline-result__stop">
          🎉 Du fullførte stoppet!
        </p>
        <div class="inline-result__actions">
          <button class="next-btn" @click="$emit('next')">
            {{ isLastTask ? 'Se oppsummering →' : 'Neste oppgave →' }}
          </button>
        </div>
      </div>
    </Transition>
  </section>
</template>

<script setup>
import { computed, ref, watch } from 'vue'

const ACTION_OPTIONS = [
  { value: 'REPLY',     label: 'Svar' },
  { value: 'REPORT',    label: 'Rapporter' },
  { value: 'ASK_ADULT', label: 'Spør en voksen' },
  { value: 'DELETE',    label: 'Slett' },
]

const props = defineProps({
  task:       { type: Object,  required: true },
  result:     { type: Object,  default: null },
  isLastTask: { type: Boolean, default: false }
})

const emit = defineEmits(['submitted', 'next', 'backToMap'])

const flaggedItems = ref([])
const action = ref('')

const email = computed(() => props.task?.contentJson?.email ?? {})

watch(() => props.task?.id, () => {
  flaggedItems.value = []
  action.value = ''
}, { immediate: true })

function toggleFlag(item) {
  flaggedItems.value = flaggedItems.value.includes(item)
    ? flaggedItems.value.filter(v => v !== item)
    : [...flaggedItems.value, item]
}

function submit() {
  if (!action.value) return
  console.log('[PhishingEmailTask] Submitting — action:', action.value, 'flagged:', flaggedItems.value)
  emit('submitted', { action: action.value, flagged: flaggedItems.value })
}
</script>

<style scoped>
.task-card {
  display: grid;
  gap: var(--space-4);
}

.guidance {
  margin: 0;
  color: var(--color-text-muted);
}

.email-card {
  border: 2px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  background: var(--color-surface);
  transition: border-color var(--transition-fast);
}
.email-card--answered { border-color: var(--color-text-muted); opacity: 0.85; }

.body { white-space: pre-wrap; }

.flag-toggle {
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  border-radius: var(--radius-sm);
  padding: var(--space-1) var(--space-2);
  cursor: pointer;
  transition: background var(--transition-fast), border-color var(--transition-fast);
}
.flag-toggle:disabled { cursor: not-allowed; }
.flagged {
  border-color: var(--color-warning);
  background: var(--color-warning-light);
}

.actions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: var(--space-3);
}

.actions button,
.submit-btn {
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  border-radius: var(--radius-md);
  padding: var(--space-2) var(--space-3);
  cursor: pointer;
  transition: background var(--transition-fast), border-color var(--transition-fast);
}
.actions button:disabled { opacity: 0.5; cursor: not-allowed; }
.actions button.selected {
  border-color: var(--color-primary);
  background: var(--color-primary-light);
  color: var(--color-primary-dark);
}
.submit-btn:disabled { opacity: 0.5; cursor: not-allowed; }

/* Inline result — same as FakeNewsTask */
.inline-result {
  border-radius: var(--radius-lg);
  padding: var(--space-4) var(--space-6);
  display: grid;
  gap: var(--space-2);
}
.inline-result--correct {
  background: var(--color-success-light);
  border: 2px solid var(--color-success);
}
.inline-result--wrong {
  background: var(--color-danger-light);
  border: 2px solid var(--color-danger);
}

.inline-result__label {
  margin: 0;
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
}
.inline-result--correct .inline-result__label { color: var(--color-success); }
.inline-result--wrong   .inline-result__label { color: var(--color-danger); }

.inline-result__explanation {
  margin: 0;
  color: var(--color-text);
  line-height: 1.5;
}

.inline-result__stop {
  margin: 0;
  font-weight: var(--font-semibold);
  color: var(--color-success);
}

.inline-result__actions { padding-top: var(--space-2); }

.next-btn {
  background: var(--color-primary);
  color: var(--color-text-on-dark);
  border: none;
  border-radius: var(--radius-md);
  padding: var(--space-2) var(--space-6);
  font-weight: var(--font-semibold);
  font-size: var(--text-base);
  cursor: pointer;
  transition: background var(--transition-fast), transform var(--transition-fast);
}
.next-btn:hover  { background: var(--color-btn-primary-hover); }
.next-btn:active { transform: scale(0.98); }

.result-slide-enter-active { transition: transform 0.3s ease, opacity 0.3s ease; }
.result-slide-leave-active { transition: transform 0.2s ease, opacity 0.2s ease; }
.result-slide-enter-from   { transform: translateY(-12px); opacity: 0; }
.result-slide-leave-to     { transform: translateY(-8px);  opacity: 0; }
</style>
