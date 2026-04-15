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
  gap: 1rem;
}

.guidance {
  margin: 0;
  color: #475569;
}

.email-card {
  border: 1px solid #cbd5e1;
  border-radius: 10px;
  padding: 1rem;
  background: #fff;
}

.body {
  white-space: pre-wrap;
}

.flag-toggle {
  border: 1px solid #94a3b8;
  background: #fff;
  border-radius: 6px;
  padding: 0.3rem 0.6rem;
  cursor: pointer;
}

.flagged {
  border-color: #ea580c;
  background: #ffedd5;
}

.actions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 0.75rem;
}

.actions button,
.submit-btn {
  border: 1px solid #94a3b8;
  background: #fff;
  border-radius: 8px;
  padding: 0.5rem 0.8rem;
  cursor: pointer;
}

.actions button.selected {
  border-color: #0f766e;
  background: #ccfbf1;
  color: #134e4a;
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
