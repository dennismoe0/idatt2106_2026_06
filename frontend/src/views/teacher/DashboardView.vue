<template>
  <div class="dashboard-layout">
    <!-- Sidebar -->
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
          <div class="user-name">{{ authStore.user?.email ?? '' }}</div>
          <div class="user-school">{{ schoolStore.school?.name ?? 'Ingen skole' }}</div>
        </div>
      </div>

      <nav class="sidebar-nav">
        <ul>
          <li>
            <router-link to="/teacher" class="nav-link active">
              <span class="nav-icon">🏠</span> Mine klasser
            </router-link>
          </li>
          <li>
            <router-link to="/teacher/notifications" class="nav-link" data-testid="notifications-link">
              <span class="nav-icon">🔔</span> Varsler
              <span
                  v-if="notificationStore.unreadCount > 0"
                  class="notification-badge"
                  data-testid="notification-badge"
              >
                {{ notificationStore.unreadCount }}
              </span>
            </router-link>
          </li>
          <li>
            <router-link to="/teacher/settings" class="nav-link">
              <span class="nav-icon">⚙️</span> Innstillinger
            </router-link>
          </li>
        </ul>
      </nav>

      <div class="sidebar-footer">
        <button class="logout-btn" @click="authStore.logout()">← Logg ut</button>
      </div>
    </aside>

    <!-- Main content -->
    <main class="main-content">
      <div class="page-header">
        <div>
          <h1 class="page-title">Mine klasser 👋</h1>
          <p class="page-sub">
            Hei {{ authStore.user?.email ?? 'Lærer' }}! Du har
            {{ classrooms.length }} aktive klasse{{ classrooms.length !== 1 ? 'r' : '' }}
          </p>
        </div>
        <button class="btn btn-primary" @click="openCreateModal">+ Opprett ny klasse</button>
      </div>

      <!-- Loading state -->
      <div v-if="loading" class="loading-state">
        <LoadingSpinner />
      </div>

      <!-- Error state -->
      <div v-else-if="error" class="error-banner">
        {{ error }}
      </div>

      <template v-else>
        <!-- School section -->
        <div class="school-section">
          <div class="section-label">Skoletilknytning</div>

          <!-- Teacher has a school -->
          <SchoolOverview
              v-if="schoolStore.school"
              :school="schoolStore.school"
              :classrooms="schoolClassrooms"
              @copy-code="copySchoolCode"
          />

          <!-- No school yet -->
          <div v-else class="no-school-banner">
            <div class="no-school-text">
              <strong>Du er ikke koblet til en skole.</strong>
              <span>Opprett en skole eller bli med i en eksisterende for å se sammenligninger mellom klasser.</span>
            </div>
            <button class="btn btn-primary btn-sm" @click="showSchoolModal = true">
              Koble til skole
            </button>
          </div>
        </div>

        <!-- Classroom grid -->
        <div class="section-label">Dine klasser</div>
        <div class="classrooms-grid">
          <a
              v-for="classroom in classrooms"
              :key="classroom.id"
              class="classroom-card"
              @click.prevent="goToClassroom(classroom.id)"
              href="#"
          >
            <div class="card-header">
              <div class="card-name">{{ classroom.name }}</div>
              <div class="card-code">{{ classroom.joinCode }}</div>
            </div>
            <div v-if="classroom.description" class="card-desc">{{ classroom.description }}</div>
            <div class="card-meta">Opprettet {{ formatDate(classroom.createdAt) }}</div>
            <div class="card-footer">
              <span class="btn btn-primary btn-sm">Se klassen →</span>
              <router-link
                  :to="{ name: 'WeeklyMysteryManage', params: { classroomId: classroom.id } }"
                  class="btn btn-mystery btn-sm"
                  @click.stop
              >
                🕯️ Ukens mysterium
              </router-link>
            </div>
          </a>

          <!-- New classroom card -->
          <button class="new-classroom-card" @click="openCreateModal">
            <span class="plus-icon">+</span>
            <span class="new-label">Opprett ny klasse</span>
            <span class="new-sub">Få en ny klassekode på sekunder</span>
          </button>
        </div>
      </template>
    </main>

    <!-- School Setup Modal -->
    <SchoolSetupModal v-model="showSchoolModal" @school-set="onSchoolSet" />

    <!-- Create Classroom Modal -->
    <BaseModal v-if="showCreateModal" :model-value="true" @update:modelValue="closeCreateModal" title="Opprett nytt klasserom">
      <template #default>
        <!-- Success view: show join code -->
        <div v-if="createdClassroom" class="join-code-result">
          <div class="success-icon">🎉</div>
          <p class="success-msg">Klasserommet <strong>{{ createdClassroom.name }}</strong> er opprettet!</p>
          <p class="join-code-label">Klassekode</p>
          <div class="join-code-display">
            <span class="join-code-text">{{ createdClassroom.joinCode }}</span>
            <button class="copy-btn" @click="copyCode(createdClassroom.joinCode)">
              {{ copied ? '✓ Kopiert!' : '📋 Kopier' }}
            </button>
          </div>
          <p class="join-code-hint">Del denne koden med elevene dine så de kan bli med i klasserommet.</p>
        </div>

        <!-- Create form -->
        <form v-else @submit.prevent="submitCreate" class="create-form">
          <div class="form-group">
            <label class="form-label">Klassenavn *</label>
            <input
                v-model="createForm.name"
                class="form-input"
                type="text"
                placeholder="f.eks. 7A — Blindern skole"
                required
                autofocus
            />
          </div>
          <div class="form-group">
            <label class="form-label">Beskrivelse <span class="optional">(valgfri)</span></label>
            <input
                v-model="createForm.description"
                class="form-input"
                type="text"
                placeholder="f.eks. Vår 2026"
            />
          </div>
          <div v-if="createError" class="form-error">{{ createError }}</div>
          <div class="form-actions">
            <button type="button" class="btn btn-outline" @click="closeCreateModal">Avbryt</button>
            <button type="submit" class="btn btn-primary" :disabled="creating">
              <span v-if="creating">Oppretter...</span>
              <span v-else>Opprett klasserom</span>
            </button>
          </div>
        </form>
      </template>
    </BaseModal>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useClassroomStore } from '@/stores/classroom'
import { useNotificationStore } from '@/stores/notification'
import { useSchoolStore } from '@/stores/school'
import BaseModal from '@/components/common/BaseModal.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import SchoolSetupModal from '@/components/teacher/SchoolSetupModal.vue'
import SchoolOverview from '@/components/teacher/SchoolOverview.vue'

const router = useRouter()
const authStore = useAuthStore()
const classroomStore = useClassroomStore()
const notificationStore = useNotificationStore()
const schoolStore = useSchoolStore()
const showSchoolModal = ref(false)
const schoolClassrooms = ref([])

const loading = ref(false)
const error = ref(null)
const classrooms = ref([])

const showCreateModal = ref(false)
const creating = ref(false)
const createError = ref(null)
const createdClassroom = ref(null)
const copied = ref(false)
const createForm = ref({ name: '', description: '' })

onMounted(async () => {
  loading.value = true
  error.value = null
  try {
    await classroomStore.fetchMyClassrooms()
    classrooms.value = classroomStore.classrooms
  } catch (e) {
    console.error('[Dashboard] Failed to load classrooms:', e)
    error.value = 'Kunne ikke laste klasserom. Prøv igjen.'
  } finally {
    loading.value = false
  }

  try {
    await notificationStore.fetchUnreadCount()
  } catch (notificationErr) {
    console.warn('[Dashboard] Failed to load notification count:', notificationErr)
  }

  // Try to load school data (teacher may not have a school yet)
  try {
    await schoolStore.fetchMySchool()
    if (schoolStore.school) {
      await schoolStore.fetchSchoolClassrooms()
      schoolClassrooms.value = schoolStore.classrooms
    }
  } catch (schoolErr) {
    console.warn('[Dashboard] School not found or teacher has no school:', schoolErr)
    // Not an error state — teacher simply hasn't joined a school yet
  }
})

function goToClassroom(id) {
  router.push({ name: 'ClassroomDetail', params: { id } })
}

function openCreateModal() {
  createForm.value = { name: '', description: '' }
  createError.value = null
  createdClassroom.value = null
  copied.value = false
  showCreateModal.value = true
}

function closeCreateModal() {
  if (createdClassroom.value) {
    classroomStore.fetchMyClassrooms().then(() => {
      classrooms.value = classroomStore.classrooms
    })
  }
  showCreateModal.value = false
}

async function submitCreate() {
  createError.value = null
  creating.value = true
  try {
    createdClassroom.value = await classroomStore.createClassroom(createForm.value)
  } catch (e) {
    console.error('[Dashboard] Failed to create classroom:', e)
    createError.value = e?.response?.data?.error ?? 'Noe gikk galt. Prøv igjen.'
  } finally {
    creating.value = false
  }
}

async function copyCode(code) {
  try {
    await navigator.clipboard.writeText(code)
    copied.value = true
    setTimeout(() => { copied.value = false }, 2000)
  } catch (e) {
    console.warn('[Dashboard] Clipboard write failed:', e)
  }
}

async function onSchoolSet() {
  try {
    await schoolStore.fetchMySchool()
    await schoolStore.fetchSchoolClassrooms()
    schoolClassrooms.value = schoolStore.classrooms
    console.log('[Dashboard] School classrooms reloaded after school-set, count:', schoolClassrooms.value.length)
  } catch (e) {
    console.error('[Dashboard] Failed to load school classrooms:', e)
  }
}

async function copySchoolCode(code) {
  try {
    await navigator.clipboard.writeText(code)
    console.log('[Dashboard] School join code copied to clipboard')
  } catch (e) {
    console.warn('[Dashboard] Failed to copy school code:', e)
  }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return d.toLocaleDateString('nb-NO', { day: 'numeric', month: 'long', year: 'numeric' })
}
</script>

<style scoped>
/* ─── Layout ─────────────────────────────────────────────── */
.dashboard-layout {
  display: grid;
  grid-template-columns: 240px 1fr;
  min-height: 100vh;
  font-family: var(--font-sans),sans-serif;
  background: var(--color-bg);
  color: var(--color-text);
}

/* ─── Sidebar ─────────────────────────────────────────────── */
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
  border-bottom: 1px solid rgba(255, 255, 255, 0.12);
}
.logo-icon  { font-size: 28px; }
.logo-title { font-size: var(--text-sm); font-weight: 900; }
.logo-sub   { font-size: var(--text-xs); opacity: 0.65; font-weight: 600; }

.sidebar-user {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4);
  border-bottom: 1px solid rgba(255, 255, 255, 0.12);
}
.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--color-accent);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}
.user-name   { font-size: var(--text-sm); font-weight: 800; }
.user-school { font-size: var(--text-xs); opacity: 0.65; }

.sidebar-nav { padding: var(--space-3) 0; flex: 1; }
.sidebar-nav ul { list-style: none; margin: 0; padding: 0; }

.nav-link {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: 11px 20px;
  color: rgba(255, 255, 255, 0.75);
  text-decoration: none;
  font-size: var(--text-sm);
  font-weight: 700;
  border-left: 3px solid transparent;
  transition: all var(--transition-fast);
}
.nav-link:hover,
.nav-link.active {
  background: rgba(255, 255, 255, 0.1);
  color: var(--color-text-on-dark);
  border-left-color: var(--color-accent);
}
.nav-icon { width: 20px; text-align: center; font-size: 16px; }
.notification-badge {
  margin-left: auto;
  min-width: 22px;
  height: 22px;
  border-radius: var(--radius-full);
  background: var(--color-accent);
  color: var(--color-text-on-dark);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0 6px;
  font-size: var(--text-xs);
  font-weight: 900;
}

.sidebar-footer {
  padding: var(--space-4);
  border-top: 1px solid rgba(255, 255, 255, 0.12);
}
.logout-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.6);
  font-family: inherit;
  font-size: var(--text-xs);
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: var(--space-2);
  transition: color var(--transition-fast);
}
.logout-btn:hover { color: var(--color-text-on-dark); }

/* ─── Main ────────────────────────────────────────────────── */
.main-content {
  padding: 28px 32px 64px;
  background: var(--color-bg);
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 28px;
}
.page-title { font-size: var(--text-2xl); font-weight: 900; margin: 0 0 var(--space-1); }
.page-sub   { font-size: var(--text-sm); color: var(--color-text-muted); margin: 0; }

.section-label {
  font-size: var(--text-xs);
  font-weight: 800;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.6px;
  margin-bottom: 14px;
}

/* ─── Classroom grid ──────────────────────────────────────── */
.classrooms-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 18px;
}

.classroom-card {
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  padding: 22px;
  box-shadow: var(--shadow-md);
  border: 2px solid transparent;
  text-decoration: none;
  color: var(--color-text);
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  transition: all var(--transition-normal);
  cursor: pointer;
}
.classroom-card:hover {
  border-color: var(--color-primary);
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
}
.card-name {
  font-size: var(--text-lg);
  font-weight: 900;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.card-code {
  background: var(--color-primary-light);
  color: var(--color-primary);
  border-radius: var(--radius-md);
  padding: 3px 10px;
  font-size: var(--text-xs);
  font-weight: 800;
  font-family: monospace;
  letter-spacing: 1px;
  white-space: nowrap;
  flex-shrink: 0;
}

.card-desc { font-size: var(--text-sm); color: var(--color-text-muted); }
.card-meta { font-size: var(--text-xs); color: var(--color-text-muted); opacity: 0.75; }

.card-footer {
  margin-top: auto;
  padding-top: var(--space-3);
  border-top: 1px solid var(--color-border);
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}

/* ─── New classroom card ──────────────────────────────────── */
.new-classroom-card {
  background: var(--color-bg);
  border-radius: var(--radius-xl);
  padding: 22px;
  box-shadow: var(--shadow-sm);
  border: 3px dashed var(--color-border);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  min-height: 180px;
  cursor: pointer;
  color: var(--color-text-muted);
  font-family: inherit;
  transition: all var(--transition-normal);
}
.new-classroom-card:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
  background: var(--color-primary-light);
}
.plus-icon { font-size: 36px; font-weight: 900; line-height: 1; }
.new-label { font-size: var(--text-base); font-weight: 800; }
.new-sub   { font-size: var(--text-xs); }

/* ─── Buttons ─────────────────────────────────────────────── */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  padding: var(--space-3) 22px;
  border-radius: var(--radius-full);
  border: none;
  cursor: pointer;
  font-family: inherit;
  font-weight: 800;
  font-size: var(--text-sm);
  transition: all var(--transition-fast);
  text-decoration: none;
}
.btn-primary {
  background: var(--color-btn-primary-bg);
  color: var(--color-btn-primary-fg);
  box-shadow: 0 4px 12px var(--color-btn-primary-shadow);
}
.btn-primary:hover:not(:disabled) {
  background: var(--color-btn-primary-hover);
  transform: translateY(-1px);
}
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-outline {
  background: var(--color-surface);
  color: var(--color-primary);
  border: 2px solid var(--color-primary);
}
.btn-outline:hover { background: var(--color-primary-light); }
.btn-sm { padding: var(--space-2) var(--space-4); font-size: var(--text-xs); }

.btn-mystery {
  background: var(--color-accent-soft, #FFF3E0);
  color: var(--color-accent-dark, #C05621);
  border: 1.5px solid var(--color-accent-light, #FBBF79);
}
.btn-mystery:hover {
  background: var(--color-accent-light, #FBBF79);
  color: var(--color-accent-dark, #C05621);
  transform: translateY(-1px);
}

/* Sidebar disabled state — teacher has no classrooms yet */
.nav-link--disabled {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 11px 20px;
  color: rgba(255, 255, 255, 0.35);
  font-size: var(--text-sm);
  font-weight: 700;
  border-left: 3px solid transparent;
  cursor: not-allowed;
  user-select: none;
}

/* ─── Loading / Error ─────────────────────────────────────── */
.loading-state {
  display: flex;
  justify-content: center;
  padding: 60px 0;
}
.error-banner {
  background: var(--color-danger-light);
  color: var(--color-danger);
  border: 1px solid var(--color-danger);
  border-radius: var(--radius-lg);
  padding: var(--space-4) 20px;
  font-weight: 700;
  font-size: var(--text-sm);
}

/* ─── Modal internals ─────────────────────────────────────── */
.modal-title {
  font-size: var(--text-xl);
  font-weight: 900;
  margin: 0;
}

.create-form { display: flex; flex-direction: column; gap: var(--space-4); }
.form-group  { display: flex; flex-direction: column; gap: var(--space-2); }
.form-label  { font-size: var(--text-sm); font-weight: 700; color: var(--color-text); }
.optional    { font-size: var(--text-xs); font-weight: 600; color: var(--color-text-muted); }
.form-input {
  width: 100%;
  height: 48px;
  border: 2px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: 0 14px;
  font-family: inherit;
  font-size: var(--text-base);
  color: var(--color-text);
  background: var(--color-surface);
  outline: none;
  transition: border-color var(--transition-fast), box-shadow var(--transition-fast);
  box-sizing: border-box;
}
.form-input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--color-primary-focus-ring);
}
.form-error {
  background: var(--color-danger-light);
  color: var(--color-danger);
  border-radius: var(--radius-md);
  padding: var(--space-3) 14px;
  font-size: var(--text-sm);
  font-weight: 700;
}
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  padding-top: var(--space-1);
}

/* ─── Join code result ────────────────────────────────────── */
.join-code-result {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-3);
  text-align: center;
  padding: var(--space-2) 0;
}
.success-icon { font-size: 48px; line-height: 1; }
.success-msg  { font-size: var(--text-base); font-weight: 700; color: var(--color-text); margin: 0; }
.join-code-label {
  font-size: var(--text-xs);
  font-weight: 800;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin: 0;
}
.join-code-display {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  background: var(--color-primary-light);
  border-radius: var(--radius-lg);
  padding: var(--space-3) 18px;
}
.join-code-text {
  font-family: monospace;
  font-size: var(--text-2xl);
  font-weight: 900;
  color: var(--color-primary);
  letter-spacing: 2px;
}
.copy-btn {
  background: var(--color-primary);
  color: var(--color-text-on-dark);
  border: none;
  border-radius: var(--radius-md);
  padding: var(--space-2) 14px;
  font-family: inherit;
  font-size: var(--text-xs);
  font-weight: 800;
  cursor: pointer;
  transition: background var(--transition-fast);
  white-space: nowrap;
}
.copy-btn:hover { background: var(--color-btn-primary-hover); }
.join-code-hint {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  margin: 0;
  max-width: 320px;
}

/* ─── Responsive ──────────────────────────────────────────── */
@media (max-width: 768px) {
  .dashboard-layout {
    grid-template-columns: 1fr;
  }
  .sidebar {
    position: static;
    height: auto;
  }
  .main-content {
    padding: 20px var(--space-4) 60px;
  }
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-3);
  }
}

/* ─── School section ──────────────────────────────────────── */
.school-section {
  margin-bottom: var(--space-8);
}
.no-school-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  padding: var(--space-4) var(--space-6);
  box-shadow: var(--shadow-md);
  gap: var(--space-4);
  flex-wrap: wrap;
}
.no-school-text {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  font-size: var(--text-sm);
  color: var(--color-text-muted);
}
.no-school-text strong { color: var(--color-text); }
</style>
