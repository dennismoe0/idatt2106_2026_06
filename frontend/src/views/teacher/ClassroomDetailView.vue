<template>
  <main class="classroom-detail">
    <header class="classroom-detail__header">
      <button class="back-btn" @click="router.push({ name: 'Dashboard' })">← Tilbake</button>
      <div class="classroom-detail__title">
        <h1>{{ classroom?.name ?? 'Klasserom' }}</h1>
        <div v-if="classroom?.joinCode" class="join-code">
          Kode: <strong>{{ classroom.joinCode }}</strong>
          <button class="copy-btn" @click="copyCode">{{ codeCopied ? 'Kopiert!' : 'Kopier' }}</button>
        </div>
      </div>
    </header>

    <LoadingSpinner v-if="loading" />
    <p v-else-if="error" class="detail-error">{{ error }}</p>

    <section v-else>
      <p class="student-count">{{ students.length }} elev{{ students.length !== 1 ? 'er' : '' }}</p>

      <ul class="student-list">
        <li v-for="student in students" :key="student.userId" class="student-row">
          <span class="student-name">{{ student.displayName }}</span>
          <span class="badge" :class="`badge--${student.status.toLowerCase()}`">
            {{ statusLabel(student.status) }}
          </span>
          <div class="student-actions">
            <RouterLink
              :to="{ name: 'TeacherNotebook', params: { studentId: student.userId }, query: { studentName: student.displayName, classroomId: classroomId } }"
              class="notebook-link"
            >
              Se Notatblokk
            </RouterLink>
            <BaseButton
              v-if="student.status === 'PENDING'"
              size="sm"
              @click="approve(student.userId)"
            >Godkjenn</BaseButton>
            <BaseButton
              v-if="student.status !== 'KICKED'"
              size="sm"
              variant="danger"
              @click="openKickModal(student)"
            >Kast ut</BaseButton>
          </div>
        </li>
      </ul>

      <p v-if="students.length === 0" class="student-empty">Ingen elever har meldt seg på enda.</p>
    </section>

    <!-- Kick confirmation modal -->
    <BaseModal
      v-if="kickTarget"
      :model-value="true"
      @update:model-value="kickTarget = null"
      title="Kast ut elev"
    >
      <p>Er du sikker på at du vil kaste ut <strong>{{ kickTarget.displayName }}</strong>?</p>
      <p class="kick-warning">Eleven kan ikke melde seg på igjen.</p>
      <div class="modal-actions">
        <button class="btn btn-outline" @click="kickTarget = null">Avbryt</button>
        <button class="btn btn-danger" @click="kick(kickTarget.userId)">Ja, kast ut</button>
      </div>
    </BaseModal>
  </main>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { useClassroomStore } from '@/stores/classroom'
import BaseButton from '@/components/common/BaseButton.vue'
import BaseModal from '@/components/common/BaseModal.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'

const route = useRoute()
const router = useRouter()
const classroomStore = useClassroomStore()
const classroomId = Number(route.params.id)

const loading = ref(false)
const error = ref('')
const kickTarget = ref(null)
const codeCopied = ref(false)
let pollInterval = null

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
  await loadStudents()
  pollInterval = setInterval(loadStudents, 5000)
  console.log('[ClassroomDetailView] Polling started every 5s')
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
}
.back-btn {
  background: none;
  border: none;
  color: var(--color-primary);
  cursor: pointer;
  font-size: var(--text-sm);
  padding: 0;
  margin-bottom: var(--space-3);
}
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
.copy-btn {
  background: none;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  padding: var(--space-1) var(--space-2);
  cursor: pointer;
  font-size: var(--text-xs);
  color: var(--color-text);
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
.notebook-link {
  font-size: var(--text-sm);
  color: var(--color-primary);
  text-decoration: none;
  padding: var(--space-1) var(--space-3);
  border: 1px solid var(--color-primary);
  border-radius: var(--radius-md);
  white-space: nowrap;
}
.notebook-link:hover {
  background: var(--color-primary-soft);
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
.btn {
  display: inline-flex;
  align-items: center;
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-full);
  border: none;
  cursor: pointer;
  font-family: inherit;
  font-weight: 800;
  font-size: var(--text-sm);
}
.btn-outline {
  background: var(--color-surface);
  color: var(--color-primary);
  border: 2px solid var(--color-primary);
}
.btn-danger {
  background: var(--color-danger);
  color: var(--color-text-on-dark);
}
</style>
