<template>
  <main class="classroom-detail">
    <header class="classroom-detail__header">
      <button class="btn btn-ghost btn-sm" @click="router.push({ name: 'Dashboard' })">← Tilbake</button>
      <div class="classroom-detail__title">
        <h1>{{ classroom?.name ?? 'Klasserom' }}</h1>
        <div v-if="classroom?.joinCode" class="join-code">
          Kode: <strong>{{ classroom.joinCode }}</strong>
          <button class="btn btn-secondary btn-sm" @click="copyCode">{{ codeCopied ? 'Kopiert!' : 'Kopier' }}</button>
        </div>
      </div>
      <button
        class="mute-all-btn"
        :class="{ 'mute-all-btn--active': musicMuted }"
        :title="musicMuted ? 'Slå på lyd for alle elever' : 'Demp lyd for alle elever'"
        @click="toggleMusicMuted"
      >
        {{ musicMuted ? '🔇 Lyd av (alle)' : '🔊 Demp alle' }}
      </button>
      <RouterLink
        :to="{ name: 'WeeklyMysteryManage', params: { classroomId } }"
        class="btn btn-primary btn-sm"
      >
        Ukens Mysterium
      </RouterLink>
    </header>

    <LoadingSpinner v-if="loading" />
    <p v-else-if="error" class="detail-error">{{ error }}</p>

    <section v-else>
      <p class="student-count">{{ students.length }} elev{{ students.length !== 1 ? 'er' : '' }}</p>

      <div class="student-table">
        <!-- Header row -->
        <div class="student-table__header">
          <span>Elev</span>
          <span>Status</span>
          <span>Nåværende stopp</span>
          <span>Oppgaver fullført</span>
          <span>Sist fullførte stopp</span>
          <span>Handlinger</span>
        </div>

        <div v-for="student in students" :key="student.userId" class="student-table__row">
          <!-- Name -->
          <span class="student-name">
            {{ student.displayName }}
            <span class="student-username">{{ student.username }}</span>
          </span>

          <!-- Status -->
          <span class="badge" :class="`badge--${student.status.toLowerCase()}`">
            {{ statusLabel(student.status) }}
          </span>

          <!-- Current stop -->
          <span class="stop-cell">
            <template v-if="currentStop(student)">
              {{ stopIcon(currentStop(student).theme) }} {{ currentStop(student).name }}
            </template>
            <span v-else class="cell-empty">—</span>
          </span>

          <!-- Tasks progress -->
          <span class="progress-cell">
            <template v-if="studentProgress(student)">
              <strong>{{ studentProgress(student).completedTasks }}</strong>
              / {{ studentProgress(student).totalTasks }}
            </template>
            <span v-else class="cell-empty">—</span>
          </span>

          <!-- Last completed stop -->
          <span class="stop-cell">
            <template v-if="lastCompletedStop(student)">
              {{ stopIcon(lastCompletedStop(student).theme) }} {{ lastCompletedStop(student).name }}
            </template>
            <span v-else class="cell-empty">—</span>
          </span>

          <!-- Actions -->
          <div class="student-actions">
            <RouterLink
              :to="{ name: 'TeacherNotebook', params: { studentId: student.userId }, query: { studentName: student.displayName, classroomId: classroomId } }"
              class="action-btn"
            >
              Notatblokk
            </RouterLink>
            <button
              v-if="student.status === 'PENDING'"
              class="action-btn action-btn--approve"
              @click="approve(student.userId)"
            >Godkjenn</button>
            <button
              v-if="student.status !== 'KICKED'"
              class="action-btn action-btn--danger"
              @click="openKickModal(student)"
            >Kast ut</button>
          </div>
        </div>
      </div>

      <p v-if="students.length === 0" class="student-empty">Ingen elever har meldt seg på enda.</p>

      <!-- Detailed leaderboard section inside the same view -->
      <section class="leaderboard-section">
        <h2 class="leaderboard-title">Ledertavle</h2>
        <LoadingSpinner v-if="lbLoading" />
        <template v-else>
          <LeaderboardTable
            :aria-label="'Ledertavle for klassen'"
            :entries="leaderboard"
            :current-student-id="null"
            empty-label="Ingen elever å vise enda."
          />
        </template>
        <p v-if="lbError" class="leaderboard-error">{{ lbError }}</p>
      </section>
    </section>

    <!-- Kick confirmation modal -->
    <BaseModal
      v-if="kickTarget"
      :model-value="true"
      @update:model-value="kickTarget = null"
      title="Kast ut elev"
    >
      <p>Er du sikker på at du vil kaste ut <strong>{{ kickTarget.displayName }}</strong>?</p>
      <p class="kick-warning">Eleven kan søke om å bli med igjen, men du må godkjenne dem på nytt.</p>
      <div class="modal-actions">
        <button class="btn btn-secondary" @click="kickTarget = null">Avbryt</button>
        <button class="btn btn-danger" @click="kick(kickTarget.userId)">Ja, kast ut</button>
      </div>
    </BaseModal>
  </main>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { useClassroomStore } from '@/stores/classroom'
import { classroomService } from '@/services/classroomService'
import BaseButton from '@/components/common/BaseButton.vue'
import BaseModal from '@/components/common/BaseModal.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import LeaderboardTable from '@/components/student/LeaderboardTable.vue'
import { gameService } from '@/services/gameService'

const route = useRoute()
const router = useRouter()
const classroomStore = useClassroomStore()
const classroomId = Number(route.params.id)

const loading = ref(false)
const error = ref('')
const kickTarget = ref(null)
const codeCopied = ref(false)
const musicMuted = ref(false)
let pollInterval = null

// Leaderboard state
const leaderboard = ref([])
const lbLoading = ref(false)
const lbError = ref('')

// Stops state (for mapping current stop name + icon)
const stops = ref([])
const stopsError = ref('')

const students = computed(() => classroomStore.students)
const classroom = computed(() =>
  classroomStore.classrooms.find(c => c.id === classroomId) ?? null
)

function statusLabel(status) {
  const labels = { PENDING: 'Venter', APPROVED: 'Godkjent', KICKED: 'Kastet ut' }
  return labels[status] ?? status
}

onMounted(async () => {
  console.log('[ClassroomDetailView] Mounted — classroomId:', classroomId)
  if (classroomStore.classrooms.length === 0) {
    await classroomStore.fetchMyClassrooms()
  }
  await Promise.all([loadStudents(), loadLeaderboard(), loadStops()])
  pollInterval = setInterval(async () => {
    await Promise.allSettled([loadStudents(), loadLeaderboard()])
  }, 5000)
  console.log('[ClassroomDetailView] Polling started every 5s (students + leaderboard); stops fetched once')
})

onUnmounted(() => {
  clearInterval(pollInterval)
  console.log('[ClassroomDetailView] Polling stopped')
})

async function loadStudents() {
  if (students.value.length === 0) loading.value = true
  try {
    await classroomStore.fetchStudents(classroomId)
  } catch (err) {
    console.error('[ClassroomDetailView] Failed to fetch students:', err)
    error.value = 'Kunne ikke hente elevliste.'
  } finally {
    loading.value = false
  }
}

async function loadLeaderboard() {
  if (leaderboard.value.length === 0) lbLoading.value = true
  lbError.value = ''
  try {
    const { data } = await gameService.getLeaderboard(classroomId)
    leaderboard.value = Array.isArray(data) ? data : []
  } catch (err) {
    console.error('[ClassroomDetailView] Failed to fetch leaderboard:', err)
    lbError.value = 'Kunne ikke hente ledertavlen.'
  } finally {
    lbLoading.value = false
  }
}

async function loadStops() {
  try {
    const { data } = await gameService.getStops(classroomId)
    // Ensure sorted by orderIndex ascending
    stops.value = [...data].sort((a, b) => a.orderIndex - b.orderIndex)
  } catch (err) {
    console.error('[ClassroomDetailView] Failed to fetch stops:', err)
    stopsError.value = 'Kunne ikke hente stoppene.'
  }
}

function studentProgress(student) {
  // Match by displayName (as provided by leaderboard API)
  return leaderboard.value.find((e) => e.displayName === student.displayName) || null
}

function progressTitle(student) {
  const e = studentProgress(student)
  if (!e) return 'Fremdrift ukjent'
  if (e.totalTasks <= 0) return 'Ingen oppgaver'
}

function currentStopByEntry(entry) {
  if (!stops.value || stops.value.length === 0) return null
  let cumulative = 0
  const total = stops.value.reduce((acc, s) => acc + (s.taskCount ?? 0), 0)
  if (entry.completedTasks >= total) return null // all done
  for (const stop of stops.value) {
    const count = stop.taskCount ?? 0
    if (entry.completedTasks < cumulative + count) {
      return stop
    }
    cumulative += count
  }
  return stops.value[stops.value.length - 1] || null
}

function currentStop(student) {
  const e = studentProgress(student)
  if (!e) return null
  return currentStopByEntry(e)
}

// Compute the last fully completed stop for an entry (null if none completed yet)
function lastCompletedStopByEntry(entry) {
  if (!stops.value || stops.value.length === 0) return null
  const completed = Number(entry?.completedTasks ?? 0)
  if (completed <= 0) return null

  let cumulative = 0
  let last = null
  for (const stop of stops.value) {
    const count = stop.taskCount ?? 0
    cumulative += count
    if (completed >= cumulative) {
      last = stop
    } else {
      break
    }
  }
  return last
}

function lastCompletedStop(student) {
  const e = studentProgress(student)
  if (!e) return null
  return lastCompletedStopByEntry(e)
}

function stopIcon(theme) {
  const map = {
    FAKE_NEWS: '📰',
    PHISHING_EMAIL: '✉️',
    AI_PHOTO: '📷',
    PASSWORD: '🔑',
    MARKETPLACE: '🛒',
    SOCIAL_MEDIA: '💬',
    FINAL_BOSS: '🏆',
  }
  return map[theme] || '📍'
}

async function approve(studentId) {
  console.log('[ClassroomDetailView] Approving student:', studentId)
  try {
    await classroomStore.updateStudentStatus(classroomId, studentId, 'APPROVED')
    await loadStudents()
  } catch (err) {
    console.error('[ClassroomDetailView] Failed to approve student:', err)
    error.value = 'Kunne ikke godkjenne elev.'
  }
}

function openKickModal(student) {
  kickTarget.value = student
}

async function kick(studentId) {
  console.log('[ClassroomDetailView] Kicking student:', studentId)
  try {
    await classroomStore.updateStudentStatus(classroomId, studentId, 'KICKED')
    kickTarget.value = null
    await loadStudents()
  } catch (err) {
    console.error('[ClassroomDetailView] Failed to kick student:', err)
    error.value = 'Kunne ikke kaste ut elev.'
  }
}

async function toggleMusicMuted() {
  const next = !musicMuted.value
  try {
    await classroomService.setMusicMuted(classroomId, next)
    musicMuted.value = next
    console.log('[ClassroomDetailView] Music muted set to', next)
  } catch (err) {
    console.error('[ClassroomDetailView] Failed to set music muted:', err)
  }
}

async function copyCode() {
  const code = classroom.value?.joinCode ?? ''
  try {
    await navigator.clipboard.writeText(code)
    codeCopied.value = true
    console.log('[ClassroomDetailView] Join code copied')
    setTimeout(() => { codeCopied.value = false }, 2000)
  } catch (err) {
    console.warn('[ClassroomDetailView] Clipboard write failed:', err)
  }
}
</script>

<style scoped>
.classroom-detail {
  padding: var(--space-8);
  max-width: 900px;
  margin: 0 auto;
}
.classroom-detail__header {
  margin-bottom: var(--space-6);
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: var(--space-3);
}
.mute-all-btn {
  align-self: center;
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  cursor: pointer;
  white-space: nowrap;
  flex-shrink: 0;
  transition: background var(--transition-fast), color var(--transition-fast);
}
.mute-all-btn:hover { background: var(--color-border); }
.mute-all-btn--active {
  background: var(--color-danger);
  color: var(--color-text-on-dark);
  border-color: var(--color-danger);
}
.mute-all-btn--active:hover { opacity: 0.85; }
.classroom-detail__title {
  display: flex;
  align-items: baseline;
  gap: var(--space-6);
  flex-wrap: wrap;
}
.join-code {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  color: var(--color-text-muted);
  font-size: var(--text-sm);
}
.student-count {
  color: var(--color-text-muted);
  font-size: var(--text-sm);
  margin-bottom: var(--space-3);
}
/* ---- Student table ---- */
.student-table {
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  overflow: hidden;
  font-size: var(--text-sm);
}

.student-table__header,
.student-table__row {
  display: grid;
  grid-template-columns: 2fr 1fr 2fr 1.2fr 2fr 1.8fr;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
}

.student-table__header {
  background: var(--color-surface-soft);
  border-bottom: 1px solid var(--color-border);
  font-size: var(--text-xs);
  font-weight: var(--font-semibold);
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.student-table__row {
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-border);
}
.student-table__row:last-child {
  border-bottom: none;
}
.student-table__row:hover {
  background: var(--color-surface-soft);
}

.student-name {
  font-weight: var(--font-medium);
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.student-username {
  font-size: var(--text-xs);
  font-weight: 400;
  color: var(--color-text-muted);
}

.badge {
  display: inline-block;
  padding: 2px var(--space-2);
  border-radius: var(--radius-sm);
  font-size: var(--text-xs);
  font-weight: var(--font-semibold);
  white-space: nowrap;
}
.badge--pending  { background: var(--color-warning-light); color: var(--color-warning); }
.badge--approved { background: var(--color-success-light); color: var(--color-success); }
.badge--kicked   { background: var(--color-danger-light);  color: var(--color-danger); }

.stop-cell {
  font-size: var(--text-xs);
  color: var(--color-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.progress-cell {
  font-size: var(--text-sm);
  color: var(--color-text);
}
.progress-cell strong {
  color: var(--color-primary);
}

.cell-empty {
  color: var(--color-text-muted);
}

.student-actions {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 30px;
  padding: 0 var(--space-3);
  font-size: var(--text-xs);
  font-weight: var(--font-semibold);
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  color: var(--color-text);
  cursor: pointer;
  white-space: nowrap;
  text-decoration: none;
  transition: background 0.15s, border-color 0.15s;
}
.action-btn:hover {
  background: var(--color-surface-soft);
  border-color: var(--color-border-strong);
}
.action-btn--approve {
  background: var(--color-success-light);
  border-color: var(--color-success);
  color: var(--color-success);
}
.action-btn--approve:hover {
  background: var(--color-success);
  color: #fff;
}
.action-btn--danger {
  background: var(--color-danger-light);
  border-color: var(--color-danger);
  color: var(--color-danger);
}
.action-btn--danger:hover {
  background: var(--color-danger);
  color: #fff;
}

.student-empty {
  color: var(--color-text-muted);
  padding: var(--space-6);
  text-align: center;
}
.detail-error {
  color: var(--color-danger);
}
.kick-warning {
  color: var(--color-text-muted);
  font-size: var(--text-sm);
  margin-top: var(--space-1);
}
.modal-actions {
  display: flex;
  gap: var(--space-3);
  justify-content: flex-end;
  margin-top: var(--space-4);
}
</style>
