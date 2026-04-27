<template>
  <main class="auth-page">
    <div class="auth-card">
      <h1>Nettdetektivene</h1>
      <p class="auth-subtitle">Logg inn for å fortsette</p>

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
          <span v-if="errors.email" id="email-error" class="field-error" role="alert">
            {{ errors.email }}
          </span>
        </div>

        <div class="field">
          <label for="password">Passord</label>
          <input
            id="password"
            v-model="form.password"
            type="password"
            autocomplete="current-password"
            :aria-describedby="errors.password ? 'password-error' : undefined"
            :aria-invalid="!!errors.password"
            required
          />
          <span v-if="errors.password" id="password-error" class="field-error" role="alert">
            {{ errors.password }}
          </span>
        </div>

        <span v-if="serverError" class="field-error" role="alert">{{ serverError }}</span>

        <BaseButton type="submit" :loading="loading" style="width: 100%; margin-top: var(--space-4)">
          Logg inn
        </BaseButton>
      </form>

      <div class="auth-links">
        <RouterLink to="/register">Opprett lærerkonto</RouterLink>
        <span class="divider">·</span>
        <RouterLink to="/student-login">Elev? Bli med i klasse</RouterLink>
      </div>

      <div v-if="isDev" class="dev-logins">
        <p class="dev-logins__label">Dev hurtiglogg inn</p>
        <div class="dev-logins__buttons">
          <button class="dev-logins__btn" :disabled="loading" @click="quickLogin('grethe@teacher.no')">
            Grethe
          </button>
          <button class="dev-logins__btn" :disabled="loading" @click="quickLogin('ali@teacher.no')">
            Ali
          </button>
        </div>
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
const isDev = import.meta.env.DEV || import.meta.env.VITE_SHOW_DEV_TOOLS === 'true'

function validate() {
  errors.email = ''
  errors.password = ''
  let valid = true
  if (!form.email) { errors.email = 'E-post er påkrevd'; valid = false }
  else if (!/\S+@\S+\.\S+/.test(form.email)) { errors.email = 'Ugyldig e-postadresse'; valid = false }
  if (!form.password) { errors.password = 'Passord er påkrevd'; valid = false }
  return valid
}

async function handleSubmit() {
  serverError.value = ''
  if (!validate()) return
  loading.value = true
  try {
    await authStore.login({ email: form.email, password: form.password })
    router.push(authStore.isTeacher ? '/teacher' : '/')
  } catch (err) {
    console.error('[LoginView] Login failed:', err)
    serverError.value = err?.response?.data?.error || 'Innlogging feilet. Prøv igjen.'
  } finally {
    loading.value = false
  }
}

async function quickLogin(email) {
  serverError.value = ''
  loading.value = true
  try {
    await authStore.login({ email, password: 'password123' })
    console.log('[LoginView] Quick login as', email)
    router.push(authStore.isTeacher ? '/teacher' : '/')
  } catch (err) {
    console.error('[LoginView] Quick login failed for', email, err)
    serverError.value = 'Hurtiglogging feilet. Er dev-seeder kjørt?'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
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

.auth-links {
  margin-top: var(--space-6);
  text-align: center;
  font-size: var(--text-sm);
  color: var(--color-text-muted);
}
.auth-links a { color: var(--color-primary); font-weight: var(--font-medium); }
.divider { margin: 0 var(--space-2); }

.dev-logins {
  margin-top: var(--space-6);
  padding-top: var(--space-4);
  border-top: 1px dashed var(--color-border);
}
.dev-logins__label {
  text-align: center;
  font-size: var(--text-xs);
  color: var(--color-text-muted);
  margin: 0 0 var(--space-3);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}
.dev-logins__buttons {
  display: flex;
  gap: var(--space-3);
}
.dev-logins__btn {
  flex: 1;
  padding: var(--space-2) var(--space-3);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  color: var(--color-text);
  cursor: pointer;
  transition: background var(--transition-fast), border-color var(--transition-fast);
}
.dev-logins__btn:hover:not(:disabled) {
  background: var(--color-bg);
  border-color: var(--color-primary);
  color: var(--color-primary);
}
.dev-logins__btn:disabled { opacity: 0.5; cursor: not-allowed; }
</style>
