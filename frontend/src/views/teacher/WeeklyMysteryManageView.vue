<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { weeklyMysteryService } from '@/services/weeklyMysteryService.js'

const route = useRoute()
const classroomId = Number(route.params.classroomId)

const submissions = ref([])
const loading = ref(true)
const editing = ref(null)
const editForm = ref({
  title: '',
  description: '',
  imageUrl: '',
  mysteryType: 'REAL_OR_FAKE',
  questionText: '',
  correctAnswer: 'FAKE',
  teacherComment: '',
  rewardStars: 5,
  rewardXp: 50,
})

onMounted(async () => {
  console.log('[WeeklyMysteryManageView] mounted — classroomId:', classroomId)
  try {
    submissions.value = await weeklyMysteryService.getSubmissions(classroomId)
    console.log('[WeeklyMysteryManageView] loaded submissions:', submissions.value.length)
  } catch (err) {
    console.error('[WeeklyMysteryManageView] load failed', err)
  } finally {
    loading.value = false
  }
})

function openEdit(mystery) {
  console.log('[WeeklyMysteryManageView] openEdit id:', mystery.id)
  editing.value = mystery.id
  editForm.value = {
    title: mystery.title || '',
    description: mystery.description || '',
    imageUrl: mystery.imageUrl || '',
    mysteryType: mystery.mysteryType || 'REAL_OR_FAKE',
    questionText: mystery.questionText || '',
    correctAnswer: mystery.correctAnswer || 'FAKE',
    teacherComment: mystery.teacherComment || '',
    rewardStars: mystery.rewardStars ?? 5,
    rewardXp: mystery.rewardXp ?? 50,
  }
}

async function saveEdit() {
  console.log('[WeeklyMysteryManageView] saveEdit id:', editing.value)
  try {
    const updated = await weeklyMysteryService.editMystery(editing.value, editForm.value)
    const idx = submissions.value.findIndex(s => s.id === editing.value)
    if (idx !== -1) submissions.value[idx] = updated
    editing.value = null
    console.log('[WeeklyMysteryManageView] edit saved, status now:', updated.status)
  } catch (err) {
    console.error('[WeeklyMysteryManageView] edit failed', err)
  }
}

async function activate(id) {
  console.log('[WeeklyMysteryManageView] activate id:', id)
  try {
    await weeklyMysteryService.activateMystery(id, classroomId)
    submissions.value = submissions.value.map(s => ({ ...s, featured: s.id === id }))
    console.log('[WeeklyMysteryManageView] activated mystery:', id)
  } catch (err) {
    console.error('[WeeklyMysteryManageView] activate failed', err)
  }
}

async function reject(id) {
  console.log('[WeeklyMysteryManageView] reject id:', id)
  try {
    await weeklyMysteryService.rejectMystery(id)
    submissions.value = submissions.value.map(s =>
      s.id === id ? { ...s, status: 'REJECTED' } : s
    )
    console.log('[WeeklyMysteryManageView] rejected mystery:', id)
  } catch (err) {
    console.error('[WeeklyMysteryManageView] reject failed', err)
  }
}
</script>

<template>
  <div class="mystery-manage">
    <h1 class="mystery-manage__heading">Ukens Mysterium</h1>
    <p class="mystery-manage__intro">
      Innsendte mysterier fra elevene dine. Rediger, godkjenn og aktiver ett som ukas mysterium.
    </p>

    <div v-if="loading" class="mystery-manage__loading">Laster...</div>

    <div v-else-if="submissions.length === 0" class="mystery-manage__empty">
      Ingen innsendte mysterier ennå.
    </div>

    <ul v-else class="mystery-manage__list">
      <li
        v-for="sub in submissions"
        :key="sub.id"
        class="mystery-manage__item"
        :class="{ 'mystery-manage__item--active': sub.featured }"
      >
        <div class="mystery-manage__item-header">
          <span
            class="mystery-manage__status"
            :class="`mystery-manage__status--${sub.status.toLowerCase()}`"
          >
            {{ sub.status }}
          </span>
          <span v-if="sub.featured" class="mystery-manage__active-badge">AKTIV UKE</span>
        </div>

        <h3 class="mystery-manage__title">{{ sub.title }}</h3>
        <p class="mystery-manage__by">Innsendt av: {{ sub.submittedByDisplayName }}</p>
        <p v-if="sub.description" class="mystery-manage__desc">{{ sub.description }}</p>
        <img
          v-if="sub.imageUrl"
          :src="sub.imageUrl"
          alt=""
          class="mystery-manage__img"
        />

        <div class="mystery-manage__actions">
          <button @click="openEdit(sub)" class="btn-secondary">Rediger</button>
          <button
            v-if="sub.status === 'APPROVED' && !sub.featured"
            @click="activate(sub.id)"
            class="btn-primary"
          >
            Gjør aktiv
          </button>
          <button
            v-if="sub.status === 'PENDING'"
            @click="reject(sub.id)"
            class="btn-danger"
          >
            Avvis
          </button>
        </div>

        <div v-if="editing === sub.id" class="mystery-manage__edit-form">
          <label class="edit-label">
            Tittel
            <input v-model="editForm.title" class="edit-input" />
          </label>
          <label class="edit-label">
            Spørsmål til elevene
            <input
              v-model="editForm.questionText"
              class="edit-input"
              placeholder="Er dette ekte eller falsk?"
            />
          </label>
          <label class="edit-label">
            Riktig svar
            <select v-model="editForm.correctAnswer" class="edit-input">
              <option value="REAL">Ekte (REAL)</option>
              <option value="FAKE">Falsk (FAKE)</option>
            </select>
          </label>
          <label class="edit-label">
            Lærerkommentar (vises til eleven etter svar)
            <textarea
              v-model="editForm.teacherComment"
              class="edit-textarea"
              rows="3"
              placeholder="Forklar hvorfor dette er ekte/falskt..."
            />
          </label>
          <label class="edit-label">
            Stjerner (1–10)
            <input
              v-model.number="editForm.rewardStars"
              type="number"
              min="1"
              max="10"
              class="edit-input edit-input--narrow"
            />
          </label>
          <label class="edit-label">
            XP (10–200)
            <input
              v-model.number="editForm.rewardXp"
              type="number"
              min="10"
              max="200"
              class="edit-input edit-input--narrow"
            />
          </label>
          <div class="edit-actions">
            <button @click="saveEdit" class="btn-primary">Lagre og godkjenn</button>
            <button @click="editing = null" class="btn-secondary">Avbryt</button>
          </div>
        </div>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.mystery-manage {
  max-width: 800px;
  margin: 0 auto;
  padding: var(--space-8, 2rem) var(--space-4, 1rem);
}

.mystery-manage__heading {
  font-size: var(--text-2xl, 1.5rem);
  font-weight: var(--font-bold, 700);
  margin-bottom: var(--space-2, 0.5rem);
}

.mystery-manage__intro {
  color: var(--color-text-muted, #6b7280);
  margin-bottom: var(--space-6, 1.5rem);
}

.mystery-manage__loading,
.mystery-manage__empty {
  color: var(--color-text-muted, #6b7280);
  padding: var(--space-6, 1.5rem);
  text-align: center;
}

.mystery-manage__list {
  list-style: none;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-5, 1.25rem);
}

.mystery-manage__item {
  border: 2px solid var(--color-border, #e5e7eb);
  border-radius: var(--radius-lg, 1rem);
  padding: var(--space-5, 1.25rem);
  background: var(--color-surface, #ffffff);
}

.mystery-manage__item--active {
  border-color: var(--color-success, #22c55e);
}

.mystery-manage__item-header {
  display: flex;
  gap: var(--space-3, 0.75rem);
  align-items: center;
  margin-bottom: var(--space-2, 0.5rem);
}

.mystery-manage__status {
  font-size: var(--text-xs, 0.75rem);
  font-weight: var(--font-bold, 700);
  padding: var(--space-1, 0.25rem) var(--space-3, 0.75rem);
  border-radius: var(--radius-full, 999px);
}

.mystery-manage__status--pending {
  background: var(--color-warning-light, #fef3c7);
  color: var(--color-warning, #92400e);
}

.mystery-manage__status--approved {
  background: var(--color-success-light, #dcfce7);
  color: var(--color-success-dark, #166534);
}

.mystery-manage__status--rejected {
  background: var(--color-danger-light, #fee2e2);
  color: var(--color-danger-dark, #991b1b);
}

.mystery-manage__active-badge {
  font-size: var(--text-xs, 0.75rem);
  font-weight: var(--font-bold, 700);
  color: var(--color-success, #16a34a);
  letter-spacing: 0.05em;
}

.mystery-manage__title {
  font-size: var(--text-lg, 1.1rem);
  font-weight: var(--font-bold, 700);
  margin: var(--space-1, 0.25rem) 0;
}

.mystery-manage__by {
  font-size: var(--text-sm, 0.85rem);
  color: var(--color-text-muted, #6b7280);
  margin-bottom: var(--space-2, 0.5rem);
}

.mystery-manage__desc {
  margin-bottom: var(--space-3, 0.75rem);
}

.mystery-manage__img {
  width: 100%;
  max-height: 200px;
  object-fit: cover;
  border-radius: var(--radius-md, 0.5rem);
  margin: var(--space-3, 0.75rem) 0;
}

.mystery-manage__actions {
  display: flex;
  gap: var(--space-3, 0.75rem);
  flex-wrap: wrap;
  margin-top: var(--space-3, 0.75rem);
}

.mystery-manage__edit-form {
  margin-top: var(--space-4, 1rem);
  border-top: 1px solid var(--color-border, #e5e7eb);
  padding-top: var(--space-4, 1rem);
  display: flex;
  flex-direction: column;
  gap: var(--space-3, 0.75rem);
}

.edit-label {
  display: flex;
  flex-direction: column;
  gap: var(--space-1, 0.25rem);
  font-size: var(--text-sm, 0.9rem);
  font-weight: var(--font-medium, 500);
  color: var(--color-text, #111827);
}

.edit-input,
.edit-textarea {
  display: block;
  width: 100%;
  padding: var(--space-2, 0.5rem) var(--space-3, 0.75rem);
  border: 1px solid var(--color-border, #d1d5db);
  border-radius: var(--radius-md, 0.5rem);
  font-family: inherit;
  font-size: var(--text-base, 0.95rem);
  background: var(--color-surface, #ffffff);
  color: var(--color-text, #111827);
  box-sizing: border-box;
}

.edit-input--narrow {
  max-width: 120px;
}

.edit-textarea {
  resize: vertical;
}

.edit-actions {
  display: flex;
  gap: var(--space-3, 0.75rem);
  margin-top: var(--space-1, 0.25rem);
}

.btn-primary {
  padding: var(--space-2, 0.5rem) var(--space-5, 1.25rem);
  background: var(--color-primary, #4f46e5);
  color: var(--color-text-on-dark, #ffffff);
  border: none;
  border-radius: var(--radius-md, 0.5rem);
  cursor: pointer;
  font-weight: var(--font-semibold, 600);
  font-family: inherit;
  font-size: var(--text-sm, 0.9rem);
}

.btn-primary:hover {
  opacity: 0.9;
}

.btn-secondary {
  padding: var(--space-2, 0.5rem) var(--space-5, 1.25rem);
  background: var(--color-surface, #ffffff);
  color: var(--color-text, #111827);
  border: 1px solid var(--color-border, #d1d5db);
  border-radius: var(--radius-md, 0.5rem);
  cursor: pointer;
  font-family: inherit;
  font-size: var(--text-sm, 0.9rem);
}

.btn-secondary:hover {
  background: var(--color-surface-hover, #f9fafb);
}

.btn-danger {
  padding: var(--space-2, 0.5rem) var(--space-5, 1.25rem);
  background: var(--color-danger, #dc2626);
  color: var(--color-text-on-dark, #ffffff);
  border: none;
  border-radius: var(--radius-md, 0.5rem);
  cursor: pointer;
  font-family: inherit;
  font-size: var(--text-sm, 0.9rem);
}

.btn-danger:hover {
  opacity: 0.9;
}
</style>
