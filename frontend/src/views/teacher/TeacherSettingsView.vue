<template>
  <div class="dashboard-layout">
    <aside class="sidebar">
      <div class="sidebar-logo">
        <span class="logo-icon">🔍</span>
        <div>
          <div class="logo-title">Nettdetektivene</div>
          <div class="logo-sub">Lærerportal</div>
        </div>
      </div>

      <div class="sidebar-user">
        <div class="user-avatar">👩‍🏫</div>
        <div>
          <div class="user-name">{{ authStore.user?.email ?? authStore.email ?? '' }}</div>
          <div class="user-school">{{ schoolStore.school?.name ?? 'Ingen skole' }}</div>
        </div>
      </div>

      <nav class="sidebar-nav">
        <ul>
          <li>
            <router-link to="/teacher" class="nav-link">
              <span class="nav-icon">🏠</span> Mine klasser
            </router-link>
          </li>
          <li>
            <router-link to="/teacher/notifications" class="nav-link">
              <span class="nav-icon">🔔</span> Varsler
              <span v-if="notificationStore.unreadCount > 0" class="notification-badge">
                {{ notificationStore.unreadCount }}
              </span>
            </router-link>
          </li>
          <li>
            <router-link to="/teacher/settings" class="nav-link active">
              <span class="nav-icon">⚙️</span> Innstillinger
            </router-link>
          </li>
        </ul>
      </nav>

      <div class="sidebar-footer">
        <button class="logout-btn" @click="handleLogout">← Logg ut</button>
      </div>
    </aside>
    <main class="settings-page">
      <header class="settings-header">
        <div>
          <RouterLink to="/teacher" class="back-link">← Tilbake til dashboard</RouterLink>
          <h1>Innstillinger</h1>
        </div>
      </header>

      <section class="settings-section">
        <h2 class="section-title">🔊 Lydvolum for klasser</h2>
        <p class="section-desc">
          Juster eller demp lyden for elever i hvert klasserom.
        </p>

        <div v-if="classroomStore.loading" class="state-card">Laster klasserom...</div>
        <div v-else-if="classrooms.length === 0" class="state-card muted">Ingen klasserom funnet.</div>

        <div v-else class="volume-list">
          <div
            v-for="classroom in classrooms"
            :key="classroom.id"
            class="volume-card"
          >
            <div class="volume-card__info">
              <span class="volume-card__name">{{ classroom.name }}</span>
              <span class="volume-card__code">{{ classroom.joinCode }}</span>
            </div>

            <div class="volume-card__controls">
              <button
                class="mute-btn"
                :class="{ 'mute-btn--active': volumeSettings[classroom.id]?.muted }"
                :title="volumeSettings[classroom.id]?.muted ? 'Slå på lyd' : 'Demp lyd'"
                @click="toggleMute(classroom.id)"
              >
                {{ volumeSettings[classroom.id]?.muted ? '🔇' : '🔊' }}
              </button>

              <input
                type="range"
                min="0"
                max="100"
                step="5"
                class="volume-slider"
                :disabled="volumeSettings[classroom.id]?.muted"
                :value="volumeSettings[classroom.id]?.volume ?? 80"
                @input="setVolume(classroom.id, Number($event.target.value))"
              />

              <span class="volume-value">
                {{ volumeSettings[classroom.id]?.muted ? 'Dempet' : `${volumeSettings[classroom.id]?.volume ?? 80}%` }}
              </span>
            </div>
          </div>
        </div>

        <div class="volume-actions">
          <button class="btn btn-outline btn-sm" @click="muteAll">🔇 Demp alle</button>
          <button class="btn btn-outline btn-sm" @click="unmuteAll">🔊 Slå på alle</button>
          <button
            class="btn btn-primary btn-sm"
            :disabled="savingVolume"
            @click="saveVolumeSettings"
          >
            {{ savingVolume ? 'Lagrer...' : 'Lagre lydinnstillinger' }}
          </button>
        </div>
        <p v-if="volumeSaveMsg" class="save-msg" :class="{ 'save-msg--error': volumeSaveError }">
          {{ volumeSaveMsg }}
        </p>
      </section>

      <section class="settings-section">
        <h2 class="section-title">👤 Konto</h2>
        <p class="section-desc">Logget inn som <strong>{{ authStore.user?.email ?? authStore.email }}</strong>.</p>
        <button class="btn btn-danger" @click="handleLogout">← Logg ut</button>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useAudioStore } from '@/stores/audio'
import { useAuthStore } from '@/stores/auth'
import { useClassroomStore } from '@/stores/classroom'
import { useNotificationStore } from '@/stores/notification'
import { useSchoolStore } from '@/stores/school'

const router = useRouter()
const audioStore = useAudioStore()
const authStore = useAuthStore()
const classroomStore = useClassroomStore()
const notificationStore = useNotificationStore()
const schoolStore = useSchoolStore()

// ── Classrooms ────────────────────────────────────────────────
const classrooms = computed(() => classroomStore.classrooms ?? [])

// ── Volume ────────────────────────────────────────────────────
// Shape: { [classroomId]: { volume: number, muted: boolean } }
const VOLUME_KEY = 'teacher_volume_settings'

const volumeSettings = reactive(loadVolumeFromStorage())

function loadVolumeFromStorage() {
  try {
    return JSON.parse(localStorage.getItem(VOLUME_KEY) ?? '{}')
  } catch {
    return {}
  }
}

function ensureDefaults(classroomId) {
  if (!volumeSettings[classroomId]) {
    volumeSettings[classroomId] = { volume: 80, muted: false }
  }
}

function setVolume(classroomId, value) {
  ensureDefaults(classroomId)
  volumeSettings[classroomId].volume = value
  volumeSettings[classroomId].muted = value === 0
  applyToAudioStore()
}

function toggleMute(classroomId) {
  ensureDefaults(classroomId)
  volumeSettings[classroomId].muted = !volumeSettings[classroomId].muted
  applyToAudioStore()
}

function muteAll() {
  classrooms.value.forEach(c => { ensureDefaults(c.id); volumeSettings[c.id].muted = true })
  applyToAudioStore()
}

function unmuteAll() {
  classrooms.value.forEach(c => { ensureDefaults(c.id); volumeSettings[c.id].muted = false })
  applyToAudioStore()
}

function applyToAudioStore() {
  const active = classrooms.value
  .map(c => volumeSettings[c.id])
  .filter(s => s && !s.muted)
  const masterVolume = active.length > 0
    ? Math.min(...active.map(s => s.volume)) / 100
    : 0
  audioStore.setVolume(masterVolume)
}

const savingVolume = ref(false)
const volumeSaveMsg = ref('')
const volumeSaveError = ref(false)

async function saveVolumeSettings() {
  savingVolume.value = true
  volumeSaveMsg.value = ''
  volumeSaveError.value = false
  try {
    // Persist locally so settings survive page reload
    localStorage.setItem(VOLUME_KEY, JSON.stringify(volumeSettings))

    const active = classrooms.value
    .map(c => volumeSettings[c.id])
    .filter(s => s && !s.muted)

    const masterVolume = active.length > 0
      ? Math.min(...active.map(s => s.volume)) / 100
      : 0

    audioStore.setVolume(masterVolume)

    volumeSaveMsg.value = '✓ Lydinnstillinger lagret.'
    // eslint-disable-next-line no-unused-vars
  } catch (err) {
    volumeSaveError.value = true
    volumeSaveMsg.value = 'Noe gikk galt. Prøv igjen.'
  } finally {
    savingVolume.value = false
    setTimeout(() => { volumeSaveMsg.value = '' }, 4000)
  }
}

function handleLogout() {
  authStore.logout()
  router.push({ name: 'Login' })
}

onMounted(async () => {
  try {
    await classroomStore.fetchMyClassrooms()
    // Seed defaults for any classrooms not yet in storage
    classrooms.value.forEach(c => ensureDefaults(c.id))
  } catch (err) {
    console.warn('[Settings] Could not load classrooms:', err)
  }
  try {
    await notificationStore.fetchUnreadCount()
  } catch { /* empty */ }
  try {
    await schoolStore.fetchMySchool()
  } catch { /* empty */ }
})
</script>

<style scoped>
.dashboard-layout {
  display: grid;
  grid-template-columns: 240px 1fr;
  min-height: 100vh;
  font-family: var(--font-sans), sans-serif;
  background: var(--color-bg);
  color: var(--color-text);
}

.sidebar {
  background: var(--color-primary-dark);
  color: var(--color-text-on-dark);
  display: flex;
  flex-direction: column;
  position: sticky;
  top: 0;
  height: 100vh;
}
.sidebar-logo {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4);
  border-bottom: 1px solid rgba(255,255,255,0.12);
}
.logo-icon { font-size: 28px; }
.logo-title { font-size: var(--text-sm); font-weight: 900; }
.logo-sub   { font-size: var(--text-xs); opacity: 0.65; font-weight: 600; }
.sidebar-user {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4);
  border-bottom: 1px solid rgba(255,255,255,0.12);
}
.user-avatar {
  width: 40px; height: 40px;
  border-radius: 50%;
  background: var(--color-accent);
  display: flex; align-items: center; justify-content: center;
  font-size: 20px; flex-shrink: 0;
}
.user-name   { font-size: var(--text-sm); font-weight: 800; }
.user-school { font-size: var(--text-xs); opacity: 0.65; }
.sidebar-nav { padding: var(--space-3) 0; flex: 1; }
.sidebar-nav ul { list-style: none; margin: 0; padding: 0; }
.nav-link {
  display: flex; align-items: center; gap: var(--space-3);
  padding: 11px 20px;
  color: rgba(255,255,255,0.75);
  text-decoration: none;
  font-size: var(--text-sm); font-weight: 700;
  border-left: 3px solid transparent;
  transition: all var(--transition-fast);
}
.nav-link:hover, .nav-link.active {
  background: rgba(255,255,255,0.1);
  color: var(--color-text-on-dark);
  border-left-color: var(--color-accent);
}
.nav-icon { width: 20px; text-align: center; font-size: 16px; }
.notification-badge {
  margin-left: auto;
  min-width: 22px; height: 22px;
  border-radius: var(--radius-full);
  background: var(--color-accent);
  color: var(--color-text-on-dark);
  display: inline-flex; align-items: center; justify-content: center;
  padding: 0 6px;
  font-size: var(--text-xs); font-weight: 900;
}
.sidebar-footer {
  padding: var(--space-4);
  border-top: 1px solid rgba(255,255,255,0.12);
}
.logout-btn {
  background: none; border: none; cursor: pointer;
  color: rgba(255,255,255,0.6);
  font-family: inherit; font-size: var(--text-xs); font-weight: 700;
  display: flex; align-items: center; gap: var(--space-2);
  transition: color var(--transition-fast);
}
.logout-btn:hover { color: var(--color-text-on-dark); }

.settings-page {
  padding: 32px;
  min-height: 100vh;
  background: var(--color-bg);
}
.settings-header {
  max-width: 720px;
  margin: 0 auto 28px;
}
.settings-header h1 {
  margin: 8px 0 0;
  font-size: var(--text-2xl);
  font-weight: 900;
}
.back-link {
  color: var(--color-primary);
  font-size: var(--text-sm);
  font-weight: 800;
  text-decoration: none;
}
.back-link:hover { text-decoration: underline; }

.settings-section {
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-md);
  padding: 28px;
  max-width: 720px;
  margin: 0 auto 24px;
}
.section-title {
  font-size: var(--text-lg);
  font-weight: 900;
  margin: 0 0 6px;
}
.section-desc {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  margin: 0 0 20px;
}

/* ── Volume ─────────────────────────────────────────────────── */
.volume-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-bottom: 18px;
}
.volume-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  background: var(--color-bg);
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 14px 18px;
  flex-wrap: wrap;
}
.volume-card__info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 130px;
}
.volume-card__name {
  font-weight: 800;
  font-size: var(--text-sm);
}
.volume-card__code {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
  font-family: monospace;
  letter-spacing: 1px;
}
.volume-card__controls {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 200px;
}
.mute-btn {
  font-size: 20px;
  background: none;
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-md);
  width: 38px; height: 38px;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer;
  transition: border-color var(--transition-fast), background var(--transition-fast);
  flex-shrink: 0;
}
.mute-btn:hover { border-color: var(--color-primary); background: var(--color-primary-light); }
.mute-btn--active { border-color: var(--color-danger); background: var(--color-danger-light, #FEE2E2); }
.volume-slider {
  flex: 1;
  accent-color: var(--color-primary);
  cursor: pointer;
  height: 4px;
}
.volume-slider:disabled { opacity: 0.35; cursor: not-allowed; }
.volume-value {
  font-size: var(--text-xs);
  font-weight: 800;
  color: var(--color-text-muted);
  min-width: 52px;
  text-align: right;
}
.volume-actions {
  display: flex;
  gap: var(--space-3);
  flex-wrap: wrap;
  align-items: center;
  margin-top: 4px;
}


/* ── Feedback messages ──────────────────────────────────────── */
.save-msg {
  font-size: var(--text-sm);
  font-weight: 700;
  color: var(--color-success, #16a34a);
  margin: 6px 0 0;
}
.save-msg--error { color: var(--color-danger); }

.state-card {
  color: var(--color-text-muted);
  font-weight: 700;
  font-size: var(--text-sm);
  padding: var(--space-4) 0;
}

/* ── Buttons ────────────────────────────────────────────────── */
.btn {
  display: inline-flex; align-items: center; justify-content: center;
  gap: var(--space-2);
  padding: var(--space-3) 18px;
  border-radius: var(--radius-full);
  border: none; cursor: pointer;
  font-family: inherit; font-weight: 800; font-size: var(--text-sm);
  transition: all var(--transition-fast);
  text-decoration: none;
}
.btn:disabled { cursor: not-allowed; opacity: 0.6; }
.btn-primary {
  background: var(--color-btn-primary-bg);
  color: var(--color-btn-primary-fg);
}
.btn-primary:hover:not(:disabled) { background: var(--color-btn-primary-hover); }
.btn-outline {
  background: var(--color-surface);
  color: var(--color-primary);
  border: 2px solid var(--color-primary);
}
.btn-outline:hover { background: var(--color-primary-light); }
.btn-danger {
  background: var(--color-danger);
  color: var(--color-text-on-dark);
}
.btn-sm { padding: var(--space-2) var(--space-4); font-size: var(--text-xs); }

/* ── Responsive ─────────────────────────────────────────────── */
@media (max-width: 768px) {
  .dashboard-layout { grid-template-columns: 1fr; }
  .sidebar { position: static; height: auto; }
  .settings-page { padding: 20px var(--space-4); }
  .volume-card { flex-direction: column; align-items: flex-start; }
  .volume-actions { flex-direction: column; align-items: stretch; }
}
</style>
