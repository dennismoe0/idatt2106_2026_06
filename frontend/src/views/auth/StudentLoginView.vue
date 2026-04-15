<template>
  <main class="student-login-page">
    <div class="student-login-card">
      <h1>Elevinnlogging</h1>
      <p class="subtitle">Logg inn med brukernavn for å fortsette</p>

      <form class="student-login-form" @submit.prevent="handleSubmit" novalidate>
        <div class="field">
          <label for="username">Brukernavn</label>
          <input
            id="username"
            v-model="form.username"
            type="text"
            autocomplete="username"
            :aria-describedby="errors.username ? 'username-error' : undefined"
            :aria-invalid="!!errors.username"
            required
          />
          <span v-if="errors.username" id="username-error" class="error" role="alert">
            {{ errors.username }}
          </span>
        </div>

        <span v-if="serverError" class="error" role="alert">{{ serverError }}</span>

        <BaseButton class="submit-btn" type="submit" :loading="loading">
          Logg inn som elev
        </BaseButton>
      </form>

      <div class="auth-links">
        <RouterLink to="/login">Lærer? Logg inn her</RouterLink>
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

const form = reactive({ username: '' })
const errors = reactive({ username: '' })
const serverError = ref('')
const loading = ref(false)

function validate() {
  errors.username = ''
  if (!form.username.trim()) {
    errors.username = 'Brukernavn er påkrevd'
    return false
  }
  return true
}

async function handleSubmit() {
  serverError.value = ''
  if (!validate()) return

  const normalizedUsername = form.username.trim()
  const hasSeenIntro = localStorage.getItem('hasSeenIntro') === 'true'

  loading.value = true
  try {
    await authStore.studentLogin(normalizedUsername)
    await router.push({ name: hasSeenIntro ? 'Home' : 'Intro' })
  } catch (err) {
    console.error('[StudentLoginView] Student login failed:', err)
    serverError.value = err?.response?.data?.error || 'Innlogging feilet. Prøv igjen.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.student-login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: var(--color-bg);
  padding: var(--space-4);
}

.student-login-card {
  width: min(100%, 28rem);
  padding: var(--space-8);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  box-shadow: var(--shadow-lg);
}

h1 {
  font-size: var(--text-3xl);
  color: var(--color-primary);
  margin-bottom: var(--space-2);
}

.subtitle {
  color: var(--color-text-muted);
  margin-bottom: var(--space-6);
}

.student-login-form {
  display: grid;
  gap: var(--space-3);
}

.field {
  display: grid;
  gap: var(--space-1);
}

label {
  font-weight: var(--font-semibold);
  font-size: var(--text-sm);
}

input {
  padding: var(--space-3);
  border: 2px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: var(--text-base);
  transition: border-color var(--transition-fast);
}

input:focus {
  border-color: var(--color-primary);
  outline: none;
}

.submit-btn {
  margin-top: var(--space-1);
}

.error {
  color: var(--color-danger);
  font-size: var(--text-sm);
}

input[aria-invalid='true'] {
  border-color: var(--color-danger);
}

.auth-links {
  margin-top: var(--space-6);
  text-align: center;
  font-size: var(--text-sm);
  color: var(--color-text-muted);
}

.auth-links a {
  color: var(--color-primary);
  font-weight: var(--font-medium);
}
</style>
