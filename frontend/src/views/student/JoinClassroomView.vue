<template>
  <main class="join-page">
    <section class="join-card">
      <h1>Bli med i en klasse</h1>
      <p class="join-subtitle">
        Skriv inn klassekoden du har fått av læreren din, og velg navnet du vil vises som.
      </p>

      <form @submit.prevent="handleSubmit" novalidate>
        <div class="field">
          <label for="code">Klassekode</label>
          <input
            id="code"
            v-model="form.code"
            type="text"
            inputmode="text"
            autocomplete="off"
            placeholder="F.eks. FJORD-TIGER"
            :aria-describedby="errors.code ? 'code-error' : 'code-help'"
            :aria-invalid="!!errors.code"
            required
            @input="formatCode"
          />
          <span id="code-help" class="field-help">Koden skrives vanligvis med bokstaver og bindestrek.</span>
          <span v-if="errors.code" id="code-error" class="field-error" role="alert">{{ errors.code }}</span>
        </div>

        <div class="field">
          <label for="displayName">Visningsnavn</label>
          <input
            id="displayName"
            v-model="form.displayName"
            type="text"
            autocomplete="nickname"
            placeholder="F.eks. Detektiv Ola"
            :aria-describedby="errors.displayName ? 'display-name-error' : 'display-name-help'"
            :aria-invalid="!!errors.displayName"
            required
          />
          <span id="display-name-help" class="field-help">
            Dette navnet vises når læreren skal godkjenne deg.
          </span>
          <span v-if="errors.displayName" id="display-name-error" class="field-error" role="alert">
            {{ errors.displayName }}
          </span>
        </div>

        <span v-if="serverError" class="field-error" role="alert">{{ serverError }}</span>

        <BaseButton type="submit" :loading="loading" class="submit-button">
          Bli med i klassen
        </BaseButton>
      </form>

      <RouterLink class="back-link" to="/login">Tilbake til innlogging</RouterLink>
    </section>
  </main>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import BaseButton from '@/components/common/BaseButton.vue'
import { useClassroomStore } from '@/stores/classroom'

const router = useRouter()
const classroomStore = useClassroomStore()

const form = reactive({
  code: '',
  displayName: ''
})

const errors = reactive({
  code: '',
  displayName: ''
})

const loading = ref(false)
const serverError = ref('')

function formatCode() {
  form.code = form.code.toUpperCase().replace(/\s+/g, '')
}

function validate() {
  errors.code = ''
  errors.displayName = ''

  let valid = true

  if (!form.code.trim()) {
    errors.code = 'Klassekode er påkrevd'
    valid = false
  }

  if (!form.displayName.trim()) {
    errors.displayName = 'Visningsnavn er påkrevd'
    valid = false
  } else if (form.displayName.trim().length < 2) {
    errors.displayName = 'Visningsnavnet må være minst 2 tegn'
    valid = false
  } else if (form.displayName.trim().length > 20) {
    errors.displayName = 'Visningsnavnet kan ikke være mer enn 20 tegn'
    valid = false
  }

  return valid
}

async function handleSubmit() {
  serverError.value = ''

  if (!validate()) return

  loading.value = true

  try {
    await classroomStore.joinClassroom({
      code: form.code,
      displayName: form.displayName
    })
    router.push('/waiting')
  } catch (err) {
    console.error('[JoinClassroom] handleSubmit failed:', err)
    serverError.value = err?.response?.data?.error || 'Kunne ikke bli med i klassen. Prøv igjen.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.join-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-4);
  background: var(--color-bg);
}

.join-card {
  width: min(100%, 460px);
  padding: var(--space-8);
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
}

.join-card h1 {
  margin-bottom: var(--space-2);
  color: var(--color-primary);
  font-size: var(--text-3xl);
}

.join-subtitle {
  margin-bottom: var(--space-6);
  color: var(--color-text-muted);
}

.field {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  margin-bottom: var(--space-4);
}

label {
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
}

input {
  padding: var(--space-3);
  border: 2px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: var(--text-base);
  transition: border-color var(--transition-fast);
}

input:focus {
  outline: none;
  border-color: var(--color-primary);
}

input[aria-invalid='true'] {
  border-color: var(--color-danger);
}

.field-help {
  color: var(--color-text-muted);
  font-size: var(--text-xs);
}

.field-error {
  color: var(--color-danger);
  font-size: var(--text-sm);
}

.back-link {
  display: inline-block;
  margin-top: var(--space-6);
  color: var(--color-primary);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
}

.submit-button {
  width: 100%;
  margin-top: var(--space-4);
}
</style>
