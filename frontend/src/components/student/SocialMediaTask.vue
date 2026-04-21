<template>
  <section class="task-card">
    <h2>Den sosiale møteplassen</h2>
    <p class="guidance">{{ task.guidanceText }}</p>

    <div class="post-shell">
      <div class="post-platform">{{ platformLabel }}</div>
      <div class="post-card" role="article" :aria-label="`Innlegg fra ${post.username ?? 'ukjent bruker'}`">
        <span class="post-avatar" aria-hidden="true">{{ post.avatar ?? '👤' }}</span>
        <div class="post-body">
          <div class="post-header">
            <span class="post-name">{{ post.username }}</span>
          </div>
          <p class="post-content">{{ post.content }}</p>
        </div>
      </div>
    </div>

    <p class="question">{{ content.question }}</p>
    <div class="options" role="group" :aria-label="content.question">
      <button
        v-for="opt in content.options"
        :key="opt.id"
        class="option-btn"
        :class="{ 'option-btn--selected': selected === opt.id }"
        :disabled="!!result"
        :aria-pressed="selected === opt.id"
        @click="selected = opt.id"
      >
        {{ opt.text }}
      </button>
    </div>

    <button
      v-if="!result"
      class="submit-btn"
      :disabled="!selected"
      @click="submit"
    >
      Send svar
    </button>

    <Transition name="result-slide">
      <div
        v-if="result"
        class="inline-result"
        :class="result.correct ? 'inline-result--correct' : 'inline-result--wrong'"
        role="status"
        aria-live="polite"
      >
        <p class="inline-result__label">{{ result.correct ? 'Riktig!' : 'Ikke helt riktig' }}</p>
        <p class="inline-result__explanation">{{ result.explanation }}</p>
        <p v-if="result.stopCompleted" class="inline-result__stop">Du fullførte Den sosiale møteplassen!</p>
        <div class="inline-result__actions">
          <button class="next-btn" @click="$emit('next')">
            {{ isLastTask ? 'Videre til sammendrag' : 'Neste oppgave' }}
          </button>
        </div>
      </div>
    </Transition>
  </section>
</template>

<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  task: { type: Object, required: true },
  result: { type: Object, default: null },
  isLastTask: { type: Boolean, default: false },
})

const emit = defineEmits(['submitted', 'next'])

const selected = ref(null)
const content = computed(() => props.task?.contentJson ?? {})
const post = computed(() => content.value.post ?? {})
const platformLabel = computed(() => post.value.platform ?? 'Sosialt medium')

watch(() => props.task?.id, () => {
  selected.value = null
}, { immediate: true })

function submit() {
  if (!selected.value) return
  emit('submitted', { selected: selected.value })
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

.post-shell {
  display: grid;
  gap: var(--space-2);
}

.post-platform {
  justify-self: start;
  border-radius: var(--radius-full);
  padding: var(--space-1) var(--space-3);
  background: var(--color-primary-light);
  color: var(--color-primary-dark);
  font-size: var(--text-xs);
  font-weight: var(--font-semibold);
}

.post-card {
  border: 2px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  background: var(--color-surface);
  display: flex;
  gap: var(--space-3);
}

.post-avatar {
  font-size: 2rem;
  line-height: 1;
  flex-shrink: 0;
}

.post-body {
  flex: 1;
  min-width: 0;
}

.post-header {
  display: flex;
  align-items: baseline;
  gap: var(--space-2);
  margin-bottom: var(--space-1);
}

.post-name {
  font-weight: var(--font-bold);
  font-size: var(--text-sm);
  color: var(--color-text);
}

.post-content {
  margin: 0;
  color: var(--color-text);
  line-height: 1.5;
  font-size: var(--text-sm);
}

.question {
  margin: 0;
  font-weight: var(--font-semibold);
  color: var(--color-text);
}

.options {
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
  font-weight: var(--font-medium);
  transition: border-color var(--transition-fast), background var(--transition-fast);
}

.option-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.option-btn--selected {
  border-color: var(--color-primary);
  background: var(--color-primary-light);
  color: var(--color-primary-dark);
  font-weight: var(--font-semibold);
}

.submit-btn {
  justify-self: start;
  background: var(--color-primary);
  color: var(--color-text-on-dark);
  border: none;
  border-radius: var(--radius-md);
  padding: var(--space-2) var(--space-6);
  font-weight: var(--font-semibold);
  font-size: var(--text-base);
  cursor: pointer;
  transition: background var(--transition-fast);
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.submit-btn:hover:not(:disabled) {
  background: var(--color-btn-primary-hover);
}

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

.inline-result--correct .inline-result__label {
  color: var(--color-success);
}

.inline-result--wrong .inline-result__label {
  color: var(--color-danger);
}

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

.inline-result__actions {
  padding-top: var(--space-2);
}

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

.next-btn:hover {
  background: var(--color-btn-primary-hover);
}

.next-btn:active {
  transform: scale(0.98);
}

.result-slide-enter-active {
  transition: transform 0.3s ease, opacity 0.3s ease;
}

.result-slide-enter-from {
  transform: translateY(-12px);
  opacity: 0;
}
</style>
