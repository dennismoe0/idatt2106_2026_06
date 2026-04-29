<template>
  <main class="auth-page">
    <div class="auth-card">
      <h1>Opprett lærerkonto</h1>
      <p class="auth-subtitle">Registrer deg som lærer</p>

      <form @submit.prevent="handleSubmit" novalidate>
        <div class="field">
          <label for="email">E-post</label>
          <input
            id="email"
            v-model="form.email"
            type="email"
            autocomplete="email"
            :aria-describedby="errors.email ? 'email-error' : undefined"
            :aria-invalid="!!errors.email"
            required
          />
          <span v-if="errors.email" id="email-error" class="field-error" role="alert">{{ errors.email }}</span>
        </div>

        <div class="field">
          <label for="password">Passord <span class="field-hint">(minst 8 tegn)</span></label>
          <input
            id="password"
            v-model="form.password"
            type="password"
            autocomplete="new-password"
            :aria-describedby="errors.password ? 'password-error' : 'password-hint'"
            :aria-invalid="!!errors.password"
            required
          />
          <span id="password-hint" class="field-hint">Minst 8 tegn</span>
          <span v-if="errors.password" id="password-error" class="field-error" role="alert">{{ errors.password }}</span>
        </div>

        <span v-if="serverError" class="field-error" role="alert">{{ serverError }}</span>

        <BaseButton type="submit" :loading="loading" style="width: 100%; margin-top: var(--space-4)">
          Opprett konto
        </BaseButton>
      </form>

      <div class="auth-links">
        <RouterLink to="/teacher-login">Har du allerede en konto? Logg inn</RouterLink>
      </div>
    </div>
  </main>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import BaseButton from '@/components/common/BaseButton.vue'

const router = useRouter()
const authStore = useAuthStore()

const form = reactive({ email: '', password: '' })
const errors = reactive({ email: '', password: '' })
const serverError = ref('')
const loading = ref(false)

function validate() {
  errors.email = ''
  errors.password = ''
  let valid = true
  if (!form.email) { errors.email = 'E-post er påkrevd'; valid = false }
  else if (!/\S+@\S+\.\S+/.test(form.email)) { errors.email = 'Ugyldig e-postadresse'; valid = false }
  if (!form.password) { errors.password = 'Passord er påkrevd'; valid = false }
  else if (form.password.length < 8) { errors.password = 'Passord må være minst 8 tegn'; valid = false }
  return valid
}

async function handleSubmit() {
  serverError.value = ''
  if (!validate()) return
  loading.value = true
  try {
    await authStore.register({ email: form.email, password: form.password })
    router.push('/teacher')
  } catch (err) {
    console.error('[RegisterView] Registration failed:', err)
    serverError.value = err?.response?.data?.error || 'Registrering feilet. Prøv igjen.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex; align-items: center; justify-content: center;
  background: var(--color-bg);
  padding: var(--space-4);
}
.auth-card {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  padding: var(--space-8);
  width: min(100%, 420px);
}
h1 { font-size: var(--text-3xl); color: var(--color-primary); margin-bottom: var(--space-2); }
.auth-subtitle { color: var(--color-text-muted); margin-bottom: var(--space-6); }
.field { display: flex; flex-direction: column; gap: var(--space-1); margin-bottom: var(--space-4); }
label { font-weight: var(--font-semibold); font-size: var(--text-sm); }
.field-hint { font-size: var(--text-xs); color: var(--color-text-muted); }
input {
  padding: var(--space-3);
  border: 2px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: var(--text-base);
  transition: border-color var(--transition-fast);
}
input:focus { border-color: var(--color-primary); outline: none; }
input[aria-invalid="true"] { border-color: var(--color-danger); }
.field-error { color: var(--color-danger); font-size: var(--text-sm); }
.auth-links { margin-top: var(--space-6); text-align: center; font-size: var(--text-sm); }
.auth-links a { color: var(--color-primary); font-weight: var(--font-medium); }
</style>
