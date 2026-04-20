<template>
  <main class="feide-page">
    <div class="feide-card">

      <!-- Language picker (decorative, matches real Feide layout) -->
      <div class="feide-lang" aria-hidden="true">
        <span class="feide-lang__flag">🇳🇴</span>
        Norsk
        <span class="feide-lang__chevron">&#8964;</span>
      </div>

      <!-- Title -->
      <h1 class="feide-title">Logg inn med Feide</h1>

      <!-- Icon + description -->
      <div class="feide-desc">
        <svg class="feide-lock" viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true" focusable="false">
          <!-- Arch / shackle -->
          <path d="M18 30V20C18 10.6 24.3 4 32 4C39.7 4 46 10.6 46 20V30"
                stroke="#4a7eb5" stroke-width="5.5" stroke-linecap="round" fill="none"/>
          <!-- Lock body -->
          <rect x="10" y="29" width="44" height="31" rx="5" fill="#4a7eb5"/>
          <!-- Windows -->
          <rect x="18" y="36" width="7" height="6" rx="1.5" fill="white" opacity="0.85"/>
          <rect x="29" y="36" width="7" height="6" rx="1.5" fill="white" opacity="0.85"/>
          <rect x="39" y="36" width="7" height="6" rx="1.5" fill="white" opacity="0.85"/>
          <!-- Door -->
          <path d="M27.5 60V51C27.5 49.9 28.4 49 29.5 49H34.5C35.6 49 36.5 49.9 36.5 51V60" fill="white" opacity="0.7"/>
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
            <span class="feide-label__hint" title="Bruk ditt fornavn og etternavn, f.eks. 'anna.hansen'" aria-label="Hjelp">&#9432;</span>
          </label>

          <div class="feide-input-wrap" :class="{ 'feide-input-wrap--error': errors.username }">
            <input
              id="username"
              v-model="form.username"
              type="text"
              placeholder=""
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

      <!-- Help accordion -->
      <details class="feide-help">
        <summary class="feide-help__summary">
          Trenger du hjelp?
          <span class="feide-help__icon" aria-hidden="true">+</span>
        </summary>
        <div class="feide-help__body">
          <RouterLink to="/login" class="feide-teacher-link">
            Lærer? Logg inn her
          </RouterLink>
        </div>
      </details>
    </div>

    <p class="feide-sikt">Feide leveres av <strong>Sikt</strong></p>
  </main>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useClassroomStore } from '@/stores/classroom'

const router = useRouter()
const authStore = useAuthStore()
const classroomStore = useClassroomStore()

function postLoginDestination() {
  if (!classroomStore.currentClassroomId) return { name: 'JoinClassroom' }
  if (classroomStore.approvalStatus !== 'APPROVED') return { name: 'WaitingRoom' }
  const hasSeenIntro = localStorage.getItem('hasSeenIntro') === 'true'
  return { name: hasSeenIntro ? 'Home' : 'Intro' }
}

onMounted(() => {
  if (authStore.isAuthenticated && authStore.isStudent) {
    console.log('[StudentLoginView] Already logged in as student — redirecting')
    router.replace(postLoginDestination())
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
  loading.value = true
  try {
    await authStore.studentLogin(username)
    await classroomStore.fetchMyClassroom()
    const dest = postLoginDestination()
    console.log('[StudentLoginView] Login success — navigating to', dest.name)
    await router.push(dest)
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
  position: relative;
  width: min(100%, 29rem);
  background: #fff;
  border-radius: 6px;
  padding: var(--space-8) var(--space-8) var(--space-6);
  box-shadow: 0 2px 12px rgba(0,0,0,0.13);
}

/* ── Language switcher ── */
.feide-lang {
  position: absolute;
  top: var(--space-4);
  right: var(--space-4);
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: var(--text-sm);
  color: #555;
  cursor: default;
  user-select: none;
}
.feide-lang__flag { font-size: 1rem; }
.feide-lang__chevron { font-size: 0.7rem; opacity: 0.7; }

/* ── Title ── */
.feide-title {
  font-size: 1.6rem;
  font-weight: 700;
  color: #111;
  margin: 0 0 var(--space-6);
  letter-spacing: -0.01em;
  line-height: 1.2;
}

/* ── Icon + desc ── */
.feide-desc {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  margin-bottom: var(--space-6);
}
.feide-lock {
  width: 48px;
  height: 48px;
  flex-shrink: 0;
}
.feide-desc__text {
  margin: 0;
  font-size: var(--text-sm);
  color: #444;
  line-height: 1.5;
}

/* ── Divider ── */
.feide-divider {
  border: none;
  border-top: 1px solid #e0e0e0;
  margin: var(--space-6) 0;
}

/* ── Field ── */
.feide-field {
  display: grid;
  gap: var(--space-2);
  margin-bottom: var(--space-6);
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
  line-height: 1;
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
  padding: var(--space-3) var(--space-4);
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
  margin-bottom: var(--space-4);
}

/* ── Continue button ── */
.feide-btn {
  width: 100%;
  padding: var(--space-3) var(--space-4);
  background: #6b9fcb;
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
  min-height: 46px;
}
.feide-btn:hover:not(:disabled) { background: #5a8dbc; }
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

/* ── Help accordion ── */
.feide-help {
  margin: 0;
}
.feide-help__summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  list-style: none;
  cursor: pointer;
  font-size: var(--text-sm);
  color: #333;
  padding: var(--space-1) 0;
  user-select: none;
}
.feide-help__summary::-webkit-details-marker { display: none; }
.feide-help__summary::marker { display: none; }
.feide-help__icon {
  font-size: 1.1rem;
  color: #555;
  line-height: 1;
  transition: transform 0.2s;
}
details[open] .feide-help__icon { transform: rotate(45deg); }

.feide-help__body {
  padding: var(--space-3) 0 var(--space-1);
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
