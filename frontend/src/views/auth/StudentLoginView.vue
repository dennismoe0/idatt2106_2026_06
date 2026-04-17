<template>
  <main class="feide-page">
    <div class="feide-card">

      <!-- Header -->
      <h1 class="feide-title">Logg inn med Feide</h1>

      <!-- Icon + description -->
      <div class="feide-desc">
        <svg class="feide-lock" viewBox="0 0 64 64" aria-hidden="true" focusable="false">
          <rect x="8" y="30" width="48" height="30" rx="5" fill="#4a7eb5"/>
          <path d="M20 30V22C20 13.16 27.16 6 36 6s0 0 0 0C36 6 44 13.16 44 22V30"
                stroke="#4a7eb5" stroke-width="6" fill="none" stroke-linecap="round"/>
          <!-- building silhouette inside lock body -->
          <rect x="27" y="38" width="10" height="14" rx="1" fill="white" opacity="0.9"/>
          <rect x="23" y="42" width="18" height="2" fill="white" opacity="0.6"/>
          <rect x="29" y="44" width="6" height="8" fill="#4a7eb5"/>
        </svg>
        <p class="feide-desc__text">
          Du trenger å logge inn via Feide for å bruke Nettdetektivene.
        </p>
      </div>

      <hr class="feide-divider" />

      <!-- Form -->
      <form @submit.prevent="handleSubmit" novalidate>
        <div class="feide-field">
          <label for="username" class="feide-label">
            Elevnavn
            <span class="feide-label__hint" title="Bruk ditt fornavn og etternavn, f.eks. 'ola-nordmann'">ⓘ</span>
          </label>

          <div class="feide-input-wrap" :class="{ 'feide-input-wrap--error': errors.username }">
            <input
              id="username"
              v-model="form.username"
              type="text"
              placeholder="fornavn.etternavn"
              autocomplete="username"
              autocorrect="off"
              autocapitalize="none"
              :aria-describedby="errors.username ? 'username-error' : 'username-hint'"
              :aria-invalid="!!errors.username"
              required
            />
          </div>
          <p id="username-hint" class="feide-input-hint">
            Eksempel: <em>anna.hansen</em> — må være unikt i klassen
          </p>
          <span v-if="errors.username" id="username-error" class="feide-error" role="alert">
            {{ errors.username }}
          </span>
        </div>

        <span v-if="serverError" class="feide-error feide-error--server" role="alert">
          {{ serverError }}
        </span>

        <button type="submit" class="feide-btn" :disabled="loading">
          <span v-if="loading" class="feide-btn__spinner" aria-hidden="true" />
          {{ loading ? 'Logger inn…' : 'Fortsett' }}
        </button>
      </form>

      <hr class="feide-divider" />

      <div class="feide-footer-links">
        <RouterLink to="/login" class="feide-teacher-link">
          Lærer? Logg inn her
        </RouterLink>
      </div>
    </div>

    <p class="feide-sikt">Feide leveres av <strong>Sikt</strong></p>
  </main>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

onMounted(() => {
  if (authStore.isAuthenticated && authStore.isStudent) {
    console.log('[StudentLoginView] Already logged in as student — redirecting to home')
    const hasSeenIntro = localStorage.getItem('hasSeenIntro') === 'true'
    router.replace({ name: hasSeenIntro ? 'Home' : 'Intro' })
  }
})

const form = reactive({ username: '' })
const errors = reactive({ username: '' })
const serverError = ref('')
const loading = ref(false)

function validate() {
  errors.username = ''
  const val = form.username.trim()
  if (!val) {
    errors.username = 'Elevnavn er påkrevd'
    return false
  }
  if (val.length < 2) {
    errors.username = 'Elevnavnet er for kort'
    return false
  }
  if (!/^[a-zA-ZæøåÆØÅ0-9._-]+$/.test(val)) {
    errors.username = 'Bare bokstaver, tall, punktum og bindestrek er tillatt'
    return false
  }
  return true
}

async function handleSubmit() {
  serverError.value = ''
  if (!validate()) return

  const username = form.username.trim()
  const hasSeenIntro = localStorage.getItem('hasSeenIntro') === 'true'

  loading.value = true
  try {
    await authStore.studentLogin(username)
    console.log('[StudentLoginView] Login success, hasSeenIntro:', hasSeenIntro)
    await router.push({ name: hasSeenIntro ? 'Home' : 'Intro' })
  } catch (err) {
    console.error('[StudentLoginView] Student login failed:', err)
    const status = err?.response?.status
    if (status === 409) {
      serverError.value = 'Dette elevnavnet er allerede i bruk. Velg et annet.'
    } else {
      serverError.value = err?.response?.data?.error || 'Innlogging feilet. Prøv igjen.'
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* ── Page ── */
.feide-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-4);
  background: #d5e0ee;
  padding: var(--space-4);
}

/* ── Card ── */
.feide-card {
  width: min(100%, 26rem);
  background: #fff;
  border-radius: 6px;
  padding: var(--space-7) var(--space-7) var(--space-5);
  box-shadow: 0 2px 12px rgba(0,0,0,0.12);
}

/* ── Title ── */
.feide-title {
  font-size: 1.75rem;
  font-weight: 800;
  color: #111;
  margin: 0 0 var(--space-5);
  letter-spacing: -0.01em;
}

/* ── Icon + desc ── */
.feide-desc {
  display: flex;
  align-items: flex-start;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}
.feide-lock {
  width: 44px;
  height: 44px;
  flex-shrink: 0;
}
.feide-desc__text {
  margin: 0;
  font-size: var(--text-sm);
  color: #333;
  line-height: 1.5;
  padding-top: 2px;
}

/* ── Divider ── */
.feide-divider {
  border: none;
  border-top: 1px solid #ddd;
  margin: var(--space-4) 0;
}

/* ── Field ── */
.feide-field {
  display: grid;
  gap: var(--space-1);
  margin-bottom: var(--space-4);
}

.feide-label {
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  color: #222;
  display: flex;
  align-items: center;
  gap: var(--space-1);
}
.feide-label__hint {
  color: #4a7eb5;
  cursor: help;
  font-size: var(--text-base);
}

.feide-input-wrap {
  border: 1.5px solid #bbb;
  border-radius: 4px;
  background: #fff;
  display: flex;
  align-items: center;
  transition: border-color 0.15s;
}
.feide-input-wrap:focus-within { border-color: #4a7eb5; }
.feide-input-wrap--error { border-color: var(--color-danger); }

.feide-input-wrap input {
  width: 100%;
  border: none;
  outline: none;
  padding: var(--space-3) var(--space-3);
  font-size: var(--text-base);
  background: transparent;
  color: #111;
}

.feide-input-hint {
  margin: 0;
  font-size: 0.75rem;
  color: #888;
}

/* ── Errors ── */
.feide-error {
  font-size: var(--text-sm);
  color: var(--color-danger);
}
.feide-error--server {
  display: block;
  margin-bottom: var(--space-3);
}

/* ── Continue button ── */
.feide-btn {
  width: 100%;
  padding: var(--space-3) var(--space-4);
  background: #4a7eb5;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  transition: background 0.15s, transform 0.1s;
  min-height: 48px;
}
.feide-btn:hover:not(:disabled) { background: #3a6ea5; }
.feide-btn:active:not(:disabled) { transform: scale(0.99); }
.feide-btn:disabled { opacity: 0.65; cursor: not-allowed; }

.feide-btn__spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255,255,255,0.4);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
  flex-shrink: 0;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* ── Footer links ── */
.feide-footer-links {
  text-align: center;
}
.feide-teacher-link {
  font-size: var(--text-sm);
  color: #4a7eb5;
  text-decoration: none;
}
.feide-teacher-link:hover { text-decoration: underline; }

/* ── Sikt credit ── */
.feide-sikt {
  font-size: 0.8rem;
  color: #555;
  margin: 0;
}
</style>
