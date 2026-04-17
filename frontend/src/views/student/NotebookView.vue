<template>
  <main class="notebook-view">
    <header class="notebook-view__header">
      <RouterLink :to="{ name: 'Home' }" class="notebook-view__back">← Tilbake</RouterLink>
      <div>
        <h1 class="notebook-view__title">📝 Notatblokk</h1>
        <p v-if="!loading && !error" class="notebook-view__subtitle">
          {{ grouped.length }} stopp fullført
        </p>
      </div>
    </header>

    <div v-if="loading" class="notebook-view__state" aria-live="polite">
      <span class="notebook-view__spinner" aria-hidden="true" />
      <p>Laster notatblokk…</p>
    </div>

    <div v-else-if="error" class="notebook-view__state notebook-view__state--error" role="alert">
      <p>{{ error }}</p>
      <button class="notebook-view__retry" @click="load">Prøv igjen</button>
    </div>

    <template v-else>
      <!-- General notes section -->
      <section class="general-notes">
        <h2 class="general-notes__heading">📌 Generelle notater</h2>

        <div v-if="generalNotes.length" class="general-notes__list">
          <div v-for="note in generalNotes" :key="note.id" class="general-note">
            <template v-if="editingNoteId === note.id">
              <textarea
                v-model="editContent"
                class="general-note__textarea"
                rows="3"
                maxlength="500"
                aria-label="Rediger notat"
              />
              <p class="general-note__count">{{ editContent.length }}/500</p>
              <div class="general-note__actions">
                <button class="general-note__save" :disabled="!editContent.trim() || saving" @click="saveEdit(note.id)">
                  {{ saving ? 'Lagrer…' : 'Lagre' }}
                </button>
                <button class="general-note__cancel" @click="cancelNoteEdit">Avbryt</button>
              </div>
              <p v-if="saveError" class="general-note__error" role="alert">{{ saveError }}</p>
            </template>
            <template v-else>
              <p class="general-note__text">{{ note.content }}</p>
              <div class="general-note__meta">
                <time class="general-note__date">{{ formatDate(note.createdAt) }}</time>
                <div class="general-note__controls">
                  <button class="general-note__edit-btn" @click="startNoteEdit(note)">Rediger</button>
                  <button class="general-note__delete-btn" @click="removeNote(note.id)">Slett</button>
                </div>
              </div>
            </template>
          </div>
        </div>

        <div v-if="addingNote" class="general-notes__add-form">
          <textarea
            v-model="newNoteContent"
            class="general-note__textarea"
            placeholder="Skriv et generelt notat her…"
            rows="3"
            maxlength="500"
            aria-label="Nytt generelt notat"
            autofocus
          />
          <p class="general-note__count">{{ newNoteContent.length }}/500</p>
          <div class="general-note__actions">
            <button class="general-note__save" :disabled="!newNoteContent.trim() || saving" @click="submitGeneralNote">
              {{ saving ? 'Lagrer…' : 'Lagre' }}
            </button>
            <button class="general-note__cancel" @click="cancelAddNote">Avbryt</button>
          </div>
          <p v-if="saveError" class="general-note__error" role="alert">{{ saveError }}</p>
        </div>
        <button v-else class="add-note-btn" @click="startAddNote">+ Legg til notat</button>
      </section>

      <!-- Stop sections -->
      <div v-if="grouped.length === 0 && generalNotes.length === 0" class="notebook-view__state">
        <span style="font-size:3rem" aria-hidden="true">🔍</span>
        <p class="notebook-view__empty-title">Ingen notater ennå.</p>
        <p class="notebook-view__empty-sub">Fullfør et stopp på kartet for å låse opp din første detektivrapport!</p>
      </div>

      <div v-if="grouped.length" class="notebook-view__stops">
        <section v-for="group in grouped" :key="group.stopId" class="stop-section">
          <h2 class="stop-section__heading">
            <span class="stop-section__num" aria-hidden="true">{{ group.stopOrder }}</span>
            {{ group.stopName }}
          </h2>

          <div v-if="group.autoTip" class="auto-tip">
            <p class="auto-tip__label">🔍 Detektivrapporten</p>
            <p class="auto-tip__content">{{ group.autoTip.content }}</p>
          </div>

          <div v-if="group.reflections.length" class="reflections">
            <h3 class="reflections__heading">Mine observasjoner</h3>
            <div v-for="r in group.reflections" :key="r.id" class="reflection-entry">
              <p class="reflection-entry__text">{{ r.content }}</p>
              <time class="reflection-entry__date">{{ formatDate(r.createdAt) }}</time>
            </div>
          </div>

          <div v-if="editingStop === group.stopId" class="add-reflection">
            <textarea
              v-model="newContent"
              class="add-reflection__textarea"
              placeholder="Skriv din observasjon her…"
              rows="3"
              maxlength="500"
              aria-label="Ny observasjon"
            />
            <p class="add-reflection__count">{{ newContent.length }}/500</p>
            <div class="add-reflection__actions">
              <button
                class="add-reflection__save"
                :disabled="!newContent.trim() || saving"
                @click="submitReflection(group.stopId)"
              >{{ saving ? 'Lagrer…' : 'Lagre' }}</button>
              <button class="add-reflection__cancel" @click="cancelEdit">Avbryt</button>
            </div>
            <p v-if="saveError" class="add-reflection__error" role="alert">{{ saveError }}</p>
          </div>
          <button v-else class="add-observation-btn" @click="startEdit(group.stopId)">
            + Legg til observasjon
          </button>
        </section>
      </div>
    </template>
  </main>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useNotebookStore } from '@/stores/notebook'

const notebookStore = useNotebookStore()
const grouped = computed(() => notebookStore.grouped)
const generalNotes = computed(() => notebookStore.generalNotes)
const loading = ref(true)
const error = ref(null)

// Stop reflection state
const editingStop = ref(null)
const newContent = ref('')

// General note state
const addingNote = ref(false)
const newNoteContent = ref('')
const editingNoteId = ref(null)
const editContent = ref('')

const saving = ref(false)
const saveError = ref(null)

async function load() {
  loading.value = true
  error.value = null
  try {
    await notebookStore.fetchEntries()
    console.log('[NotebookView] Loaded', grouped.value.length, 'stop groups,', generalNotes.value.length, 'general notes')
  } catch (err) {
    console.error('[NotebookView] Failed to load:', err)
    error.value = 'Kunne ikke laste notatblokk. Prøv igjen.'
  } finally {
    loading.value = false
  }
}

// Stop reflection handlers
function startEdit(stopId) {
  editingStop.value = stopId
  newContent.value = ''
  saveError.value = null
}

function cancelEdit() {
  editingStop.value = null
  newContent.value = ''
  saveError.value = null
}

async function submitReflection(stopId) {
  const content = newContent.value.trim()
  if (!content) return
  saving.value = true
  saveError.value = null
  try {
    await notebookStore.addReflection(stopId, content)
    cancelEdit()
  } catch (err) {
    console.error('[NotebookView] Failed to save reflection:', err)
    saveError.value = 'Kunne ikke lagre. Prøv igjen.'
  } finally {
    saving.value = false
  }
}

// General note handlers
function startAddNote() {
  addingNote.value = true
  newNoteContent.value = ''
  saveError.value = null
}

function cancelAddNote() {
  addingNote.value = false
  newNoteContent.value = ''
  saveError.value = null
}

async function submitGeneralNote() {
  const content = newNoteContent.value.trim()
  if (!content) return
  saving.value = true
  saveError.value = null
  try {
    await notebookStore.addGeneralNote(content)
    cancelAddNote()
  } catch (err) {
    console.error('[NotebookView] Failed to save general note:', err)
    saveError.value = 'Kunne ikke lagre. Prøv igjen.'
  } finally {
    saving.value = false
  }
}

function startNoteEdit(note) {
  editingNoteId.value = note.id
  editContent.value = note.content
  saveError.value = null
}

function cancelNoteEdit() {
  editingNoteId.value = null
  editContent.value = ''
  saveError.value = null
}

async function saveEdit(id) {
  const content = editContent.value.trim()
  if (!content) return
  saving.value = true
  saveError.value = null
  try {
    await notebookStore.updateNote(id, content)
    cancelNoteEdit()
  } catch (err) {
    console.error('[NotebookView] Failed to update note:', err)
    saveError.value = 'Kunne ikke lagre. Prøv igjen.'
  } finally {
    saving.value = false
  }
}

async function removeNote(id) {
  try {
    await notebookStore.deleteNote(id)
  } catch (err) {
    console.error('[NotebookView] Failed to delete note:', err)
  }
}

function formatDate(isoString) {
  if (!isoString) return ''
  return new Intl.DateTimeFormat('nb-NO', {
    day: 'numeric', month: 'long', year: 'numeric'
  }).format(new Date(isoString))
}

onMounted(load)
</script>

<style scoped>
.notebook-view {
  min-height: 100vh;
  padding: var(--space-6);
  background: var(--color-bg);
}

.notebook-view__header { margin-bottom: var(--space-8); }

.notebook-view__back {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-sm);
  color: var(--color-primary);
  text-decoration: none;
  margin-bottom: var(--space-4);
}
.notebook-view__back:hover { text-decoration: underline; }

.notebook-view__title {
  margin: 0 0 var(--space-1);
  font-size: var(--text-3xl);
  color: var(--color-heading);
}
.notebook-view__subtitle { margin: 0; font-size: var(--text-base); color: var(--color-text-muted); }

.notebook-view__state {
  display: flex; flex-direction: column; align-items: center;
  justify-content: center; gap: var(--space-3); min-height: 16rem;
  text-align: center; color: var(--color-text-muted);
}
.notebook-view__state--error { color: var(--color-danger); }
.notebook-view__spinner {
  width: 32px; height: 32px;
  border: 3px solid var(--color-border); border-top-color: var(--color-primary);
  border-radius: 50%; animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
.notebook-view__retry {
  padding: var(--space-2) var(--space-4);
  background: var(--color-primary); color: #fff;
  border: none; border-radius: var(--radius-md); cursor: pointer;
  font-size: var(--text-sm); font-weight: var(--font-semibold);
}
.notebook-view__retry:hover { background: var(--color-primary-dark); }
.notebook-view__empty-title {
  margin: 0; font-size: var(--text-lg); font-weight: var(--font-semibold); color: var(--color-text);
}
.notebook-view__empty-sub { margin: 0; font-size: var(--text-sm); }

/* General notes */
.general-notes {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: var(--space-6);
  display: flex; flex-direction: column; gap: var(--space-4);
  max-width: 720px;
  margin-bottom: var(--space-6);
}
.general-notes__heading {
  margin: 0; font-size: var(--text-xl); color: var(--color-heading);
}
.general-notes__list {
  display: flex; flex-direction: column; gap: var(--space-3);
}
.general-note {
  border-left: 2px solid var(--color-primary);
  padding-left: var(--space-3);
}
.general-note__text {
  margin: 0 0 var(--space-1); font-size: var(--text-base); color: var(--color-text); line-height: 1.5;
}
.general-note__meta {
  display: flex; align-items: center; justify-content: space-between; gap: var(--space-2);
}
.general-note__date { font-size: var(--text-xs); color: var(--color-text-muted); font-style: italic; }
.general-note__controls { display: flex; gap: var(--space-2); }
.general-note__edit-btn,
.general-note__delete-btn {
  padding: var(--space-1) var(--space-2);
  font-size: var(--text-xs); font-weight: var(--font-semibold);
  border-radius: var(--radius-sm); cursor: pointer; border: 1px solid var(--color-border);
  background: transparent;
}
.general-note__edit-btn { color: var(--color-primary); }
.general-note__edit-btn:hover { background: var(--color-primary-soft); }
.general-note__delete-btn { color: var(--color-danger); }
.general-note__delete-btn:hover { background: color-mix(in srgb, var(--color-danger) 10%, transparent); }

.general-note__textarea {
  width: 100%; border: 1.5px solid var(--color-border); border-radius: var(--radius-sm);
  padding: var(--space-3); font-size: var(--text-base); font-family: inherit;
  resize: vertical; color: var(--color-text); background: var(--color-surface); box-sizing: border-box;
}
.general-note__textarea:focus { outline: none; border-color: var(--color-primary); }
.general-note__count { margin: var(--space-1) 0 0; font-size: var(--text-xs); color: var(--color-text-muted); text-align: right; }
.general-note__actions { display: flex; gap: var(--space-3); margin-top: var(--space-3); }
.general-note__save {
  padding: var(--space-2) var(--space-6); background: var(--color-primary); color: #fff;
  border: none; border-radius: var(--radius-md);
  font-size: var(--text-sm); font-weight: var(--font-semibold); cursor: pointer;
  transition: background var(--transition-fast);
}
.general-note__save:hover:not(:disabled) { background: var(--color-primary-dark); }
.general-note__save:disabled { opacity: 0.55; cursor: not-allowed; }
.general-note__cancel {
  padding: var(--space-2) var(--space-4); background: transparent;
  color: var(--color-text-muted); border: 1px solid var(--color-border);
  border-radius: var(--radius-md); font-size: var(--text-sm); cursor: pointer;
}
.general-note__error { margin: var(--space-2) 0 0; font-size: var(--text-sm); color: var(--color-danger); }

.add-note-btn {
  align-self: flex-start; padding: var(--space-2) var(--space-4);
  background: transparent; color: var(--color-primary);
  border: 1px dashed var(--color-primary); border-radius: var(--radius-md);
  font-size: var(--text-sm); font-weight: var(--font-semibold); cursor: pointer;
  transition: background var(--transition-fast), border-style var(--transition-fast);
}
.add-note-btn:hover { background: var(--color-primary-soft); border-style: solid; }

/* Stop sections */
.notebook-view__stops {
  display: flex; flex-direction: column; gap: var(--space-6); max-width: 720px;
}

.stop-section {
  background: var(--color-surface); border: 1px solid var(--color-border);
  border-radius: var(--radius-md); padding: var(--space-6);
  display: flex; flex-direction: column; gap: var(--space-4);
}
.stop-section__heading {
  margin: 0; font-size: var(--text-xl); color: var(--color-heading);
  display: flex; align-items: center; gap: var(--space-3);
}
.stop-section__num {
  display: inline-flex; align-items: center; justify-content: center;
  width: 28px; height: 28px; border-radius: 50%;
  background: var(--color-primary); color: #fff;
  font-size: var(--text-sm); font-weight: var(--font-bold); flex-shrink: 0;
}

.auto-tip {
  background: var(--color-primary-soft);
  border-left: 3px solid var(--color-primary);
  border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
  padding: var(--space-4);
}
.auto-tip__label {
  margin: 0 0 var(--space-2); font-size: var(--text-sm); font-weight: var(--font-semibold);
  color: var(--color-primary); text-transform: uppercase; letter-spacing: 0.05em;
}
.auto-tip__content { margin: 0; font-size: var(--text-base); color: var(--color-text); line-height: 1.6; }

.reflections__heading {
  margin: 0 0 var(--space-3); font-size: var(--text-sm); font-weight: var(--font-semibold);
  color: var(--color-text-muted); text-transform: uppercase; letter-spacing: 0.05em;
}
.reflection-entry {
  border-left: 2px solid var(--color-border-strong);
  padding-left: var(--space-3); margin-bottom: var(--space-3);
}
.reflection-entry__text { margin: 0 0 var(--space-1); font-size: var(--text-base); color: var(--color-text); line-height: 1.5; }
.reflection-entry__date { font-size: var(--text-xs); color: var(--color-text-muted); font-style: italic; }

.add-reflection__textarea {
  width: 100%; border: 1.5px solid var(--color-border); border-radius: var(--radius-sm);
  padding: var(--space-3); font-size: var(--text-base); font-family: inherit;
  resize: vertical; color: var(--color-text); background: var(--color-surface); box-sizing: border-box;
}
.add-reflection__textarea:focus { outline: none; border-color: var(--color-primary); }
.add-reflection__count { margin: var(--space-1) 0 0; font-size: var(--text-xs); color: var(--color-text-muted); text-align: right; }
.add-reflection__actions { display: flex; gap: var(--space-3); margin-top: var(--space-3); }
.add-reflection__save {
  padding: var(--space-2) var(--space-6); background: var(--color-primary); color: #fff;
  border: none; border-radius: var(--radius-md);
  font-size: var(--text-sm); font-weight: var(--font-semibold); cursor: pointer;
  transition: background var(--transition-fast);
}
.add-reflection__save:hover:not(:disabled) { background: var(--color-primary-dark); }
.add-reflection__save:disabled { opacity: 0.55; cursor: not-allowed; }
.add-reflection__cancel {
  padding: var(--space-2) var(--space-4); background: transparent;
  color: var(--color-text-muted); border: 1px solid var(--color-border);
  border-radius: var(--radius-md); font-size: var(--text-sm); cursor: pointer;
}
.add-reflection__error { margin: var(--space-2) 0 0; font-size: var(--text-sm); color: var(--color-danger); }

.add-observation-btn {
  align-self: flex-start; padding: var(--space-2) var(--space-4);
  background: transparent; color: var(--color-primary);
  border: 1px dashed var(--color-primary); border-radius: var(--radius-md);
  font-size: var(--text-sm); font-weight: var(--font-semibold); cursor: pointer;
  transition: background var(--transition-fast), border-style var(--transition-fast);
}
.add-observation-btn:hover { background: var(--color-primary-soft); border-style: solid; }

@media (max-width: 480px) {
  .notebook-view { padding: var(--space-4); }
  .notebook-view__stops, .general-notes { max-width: 100%; }
}
</style>
