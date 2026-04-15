<template>
  <section class="task-card">
    <h2>Phishing-epost</h2>
    <p class="guidance">{{ task.guidanceText }}</p>

    <article class="email-card">
      <p><strong>Fra:</strong> {{ email.fromName }}</p>
      <button
        class="flag-toggle"
        :class="{ flagged: flaggedItems.includes('fromEmail') }"
        @click="toggleFlag('fromEmail')"
      >
        {{ email.fromEmail }}
      </button>
      <p><strong>Emne:</strong> {{ email.subject }}</p>
      <p class="body">{{ email.body }}</p>
    </article>

    <div class="actions">
      <button :class="{ selected: action === 'REPLY' }" @click="action = 'REPLY'">Svar</button>
      <button :class="{ selected: action === 'REPORT' }" @click="action = 'REPORT'">Rapporter</button>
      <button :class="{ selected: action === 'ASK_ADULT' }" @click="action = 'ASK_ADULT'">Spør en voksen</button>
      <button :class="{ selected: action === 'DELETE' }" @click="action = 'DELETE'">Slett</button>
    </div>

    <button class="submit-btn" :disabled="!action" @click="submit">
      Send svar
    </button>
  </section>
</template>

<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  task: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['submitted'])

const flaggedItems = ref([])
const action = ref('')

const email = computed(() => props.task?.contentJson?.email ?? {})

watch(
  () => props.task?.id,
  () => {
    flaggedItems.value = []
    action.value = ''
  },
  { immediate: true }
)

function toggleFlag(item) {
  if (flaggedItems.value.includes(item)) {
    flaggedItems.value = flaggedItems.value.filter((value) => value !== item)
    return
  }
  flaggedItems.value = [...flaggedItems.value, item]
}

function submit() {
  if (!action.value) return
  emit('submitted', {
    action: action.value,
    flagged: flaggedItems.value
  })
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
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  background: var(--color-surface);
}

.body {
  white-space: pre-wrap;
}

.flag-toggle {
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  border-radius: var(--radius-sm);
  padding: var(--space-1) var(--space-2);
  cursor: pointer;
}

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
}

.actions button.selected {
  border-color: var(--color-primary);
  background: var(--color-primary-light);
  color: var(--color-primary-dark);
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
