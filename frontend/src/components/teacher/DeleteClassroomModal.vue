<template>
  <BaseModal :model-value="true" @update:model-value="$emit('cancel')" title="Slett klasserom">
    <p class="delete-modal__warning">
      Dette kan ikke angres. Alle elever mister tilgang til klasserommet.
    </p>
    <p class="delete-modal__prompt">
      Skriv inn <strong>{{ classroomName }}</strong> for å bekrefte:
    </p>
    <input
      v-model="typed"
      class="delete-modal__input"
      type="text"
      :placeholder="classroomName"
      autofocus
      @keydown.enter="submit"
    />
    <p v-if="error" class="delete-modal__error" role="alert">{{ error }}</p>
    <div class="delete-modal__actions">
      <button class="btn btn-outline" @click="$emit('cancel')">Avbryt</button>
      <button
        class="btn btn-danger"
        :disabled="typed !== classroomName || deleting"
        @click="submit"
      >
        {{ deleting ? 'Sletter...' : 'Slett klasserom' }}
      </button>
    </div>
  </BaseModal>
</template>

<script setup>
import { ref } from 'vue'
import BaseModal from '@/components/common/BaseModal.vue'

const props = defineProps({
  classroomName: { type: String, required: true }
})
const emit = defineEmits(['confirm', 'cancel'])

const typed = ref('')
const deleting = ref(false)
const error = ref(null)

async function submit() {
  if (typed.value !== props.classroomName) return
  deleting.value = true
  error.value = null
  try {
    await emit('confirm')
  } catch (err) {
    error.value = 'Sletting feilet. Prøv igjen.'
    console.error('[DeleteClassroomModal] Delete failed:', err)
  } finally {
    deleting.value = false
  }
}
</script>

<style scoped>
.delete-modal__warning {
  color: var(--color-danger);
  font-weight: var(--font-semibold);
  margin: 0 0 var(--space-3);
}
.delete-modal__prompt {
  margin: 0 0 var(--space-3);
  font-size: var(--text-sm);
}
.delete-modal__input {
  width: 100%;
  height: 44px;
  border: 2px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: 0 var(--space-3);
  font-family: inherit;
  font-size: var(--text-base);
  color: var(--color-text);
  background: var(--color-surface);
  box-sizing: border-box;
  outline: none;
  transition: border-color var(--transition-fast);
  margin-bottom: var(--space-3);
}
.delete-modal__input:focus { border-color: var(--color-danger); }
.delete-modal__error {
  color: var(--color-danger);
  font-size: var(--text-sm);
  margin-bottom: var(--space-3);
}
.delete-modal__actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
}
.btn {
  display: inline-flex;
  align-items: center;
  padding: var(--space-2) var(--space-5);
  border-radius: var(--radius-full);
  border: none;
  cursor: pointer;
  font-family: inherit;
  font-weight: var(--font-bold);
  font-size: var(--text-sm);
  transition: all var(--transition-fast);
}
.btn:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-outline {
  background: var(--color-surface);
  color: var(--color-primary);
  border: 2px solid var(--color-primary);
}
.btn-outline:hover:not(:disabled) { background: var(--color-primary-light); }
.btn-danger {
  background: var(--color-danger);
  color: #fff;
}
.btn-danger:hover:not(:disabled) { filter: brightness(0.9); }
</style>
