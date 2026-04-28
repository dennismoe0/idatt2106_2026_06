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

      <ul class="student-list">
        <li v-for="student in students" :key="student.userId" class="student-row">
          <span class="student-name">
            {{ student.displayName }}
            <span class="student-username">{{ student.username }}</span>
          </span>
          <span class="badge" :class="`badge--${student.status.toLowerCase()}`">
            {{ statusLabel(student.status) }}
          </span>
          <div class="student-actions">
            <RouterLink
              :to="{ name: 'TeacherNotebook', params: { studentId: student.userId }, query: { studentName: student.displayName, classroomId: classroomId } }"
              class="btn btn-secondary btn-sm"
            >
              Se Notatblokk
            </RouterLink>
            <!-- Compact progress tracker next to the notepad link -->
            <span class="progress-pill" :title="progressTitle(student)">
              <template v-if="studentProgress(student)">
                {{ studentProgress(student).completedTasks }} / {{ studentProgress(student).totalTasks }}
              </template>
              <template v-else>
                —
              </template>
            </span>
            <span v-if="currentStop(student)" class="stop-chip" :title="currentStop(student).name">
              <span class="stop-chip__icon">{{ stopIcon(currentStop(student).theme) }}</span>
              <span class="stop-chip__text">{{ currentStop(student).name }}</span>
            </span>
            <BaseButton
              v-if="student.status === 'PENDING'"
              size="sm"
              class="btn btn-primary btn-sm"
              @click="approve(student.userId)"
            >Godkjenn</BaseButton>
            <BaseButton
              v-if="student.status !== 'KICKED'"
              size="sm"
              variant="danger"
              class="btn btn-danger btn-sm"
              @click="openKickModal(student)"
            >Kast ut</BaseButton>
          </div>
        </li>
      </ul>

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
.student-list {
  list-style: none;
  padding: 0;
  display: grid;
  gap: var(--space-2);
}
.student-row {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-3) var(--space-4);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
}
.student-name {
  flex: 1;
  font-weight: var(--font-medium);
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}
.student-username {
  font-size: var(--text-xs);
  font-weight: 400;
  color: var(--color-text-muted);
}
.badge {
  padding: var(--space-1) var(--space-2);
  border-radius: var(--radius-sm);
  font-size: var(--text-xs);
  font-weight: var(--font-semibold);
  white-space: nowrap;
}
.badge--pending  { background: var(--color-warning-light); color: var(--color-warning); }
.badge--approved { background: var(--color-success-light); color: var(--color-success); }
.badge--kicked   { background: var(--color-danger-light);  color: var(--color-danger); }
.student-actions {
  display: flex;
  gap: var(--space-2);
}
.progress-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: var(--text-xs);
  color: var(--color-text);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-full);
  padding: 2px 8px;
  white-space: nowrap;
}
.stop-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: var(--text-xs);
  color: var(--color-text);
  background: var(--color-surface);
  border: 1px dashed var(--color-border);
  border-radius: var(--radius-full);
  padding: 2px 10px;
  white-space: nowrap;
}
.stop-chip__icon {
  font-size: 14px;
  line-height: 1;
}
.stop-chip__text {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
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
