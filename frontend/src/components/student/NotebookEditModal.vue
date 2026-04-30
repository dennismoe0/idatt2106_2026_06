<template>
  <BaseModal
    :model-value="open"
    :title="title"
    @update:modelValue="(v) => !v && emit('cancel')"
  >
    <div class="notebook-modal">
      <textarea
        v-model="text"
        class="notebook-modal__textarea"
        rows="6"
        :maxlength="maxLength"
        :aria-label="title"
        autofocus
      />
      <p class="notebook-modal__count">{{ text.length }}/{{ maxLength }}</p>
      <div class="notebook-modal__actions">
        <button
          class="notebook-modal__button notebook-modal__button--primary"
          type="button"
          :disabled="!text.trim() || saving"
          @click="emit('save', text.trim())"
        >
          {{ saving ? 'Lagrer...' : 'Lagre' }}
        </button>
        <button
          class="notebook-modal__button"
          type="button"
          @click="emit('cancel')"
        >
          Avbryt
        </button>
      </div>
      <p v-if="error" class="notebook-modal__error" role="alert">{{ error }}</p>
    </div>
  </BaseModal>
</template>

<script setup>
import { ref, watch } from 'vue'
import BaseModal from '@/components/common/BaseModal.vue'

const props = defineProps({
  open: { type: Boolean, required: true },
  title: { type: String, required: true },
  initialContent: { type: String, default: '' },
  saving: { type: Boolean, default: false },
  error: { type: String, default: null },
  maxLength: { type: Number, default: 500 },
})

const emit = defineEmits(['save', 'cancel'])

const text = ref(props.initialContent)

// Reset text whenever the modal is opened (initialContent reflects target).
watch(
  () => props.open,
  (isOpen) => {
    if (isOpen) text.value = props.initialContent
  },
)
</script>

<style scoped>
.notebook-modal {
  display: flex;
  flex-direction: column;
  gap: 0.65rem;
}

.notebook-modal__textarea {
  width: 100%;
  box-sizing: border-box;
  resize: none;
  padding: 0.9rem 1rem;
  border-radius: 12px;
  border: 1px solid color-mix(in srgb, var(--color-journal-leather-light) 28%, transparent);
  background: var(--color-journal-modal-surface);
  color: var(--color-journal-modal-ink);
  font-family: inherit;
  font-size: 0.96rem;
  line-height: 1.5;
}

.notebook-modal__textarea:focus {
  outline: none;
  border-color: color-mix(in srgb, var(--color-journal-accent) 70%, transparent);
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--color-journal-accent) 18%, transparent);
}

.notebook-modal__count {
  margin: 0;
  text-align: right;
  font-size: 0.78rem;
  color: color-mix(in srgb, var(--color-journal-ink) 65%, transparent);
}

.notebook-modal__actions {
  display: flex;
  gap: 0.6rem;
  flex-wrap: wrap;
}

.notebook-modal__button {
  padding: 0.72rem 1rem;
  border-radius: 999px;
  border: 1px solid color-mix(in srgb, var(--color-journal-leather-top) 24%, transparent);
  background: color-mix(in srgb, var(--color-journal-parchment-light) 75%, transparent);
  color: var(--color-journal-ink-soft);
  font-size: 0.9rem;
  font-weight: 700;
  cursor: pointer;
}

.notebook-modal__button--primary {
  background: linear-gradient(135deg, var(--color-journal-leather-top), var(--color-journal-leather-bottom));
  color: var(--color-journal-parchment-light);
  border-color: transparent;
}

.notebook-modal__button:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.notebook-modal__error {
  margin: 0;
  font-size: 0.86rem;
  color: var(--color-journal-error);
}

@media (max-width: 640px) {
  .notebook-modal__button {
    width: 100%;
  }
}
</style>
