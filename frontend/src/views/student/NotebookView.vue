<template>
  <CorkBoardPage page-title="Notatblokk" :back-to="{ name: 'Home' }">
    <div class="notebook">
      <div v-if="loading" class="notebook__state" aria-live="polite">
        <span class="notebook__spinner" aria-hidden="true" />
        <p>Laster notatblokk…</p>
      </div>

      <div v-else-if="error" class="notebook__state notebook__state--error" role="alert">
        <p>{{ error }}</p>
        <button class="notebook__retry" @click="load">Prøv igjen</button>
      </div>

      <template v-else>
        <!-- General notes -->
        <section class="pinned-note notebook__general" style="--card-rotate: -0.4deg">
          <h2 class="notebook__section-heading">📌 Generelle notater</h2>

          <div v-if="generalNotes.length" class="notebook__notes-list">
            <div v-for="note in generalNotes" :key="note.id" class="notebook__note">
              <template v-if="editingNoteId === note.id">
                <textarea
                  v-model="editContent"
                  class="notebook__textarea"
                  rows="3"
                  maxlength="500"
                  aria-label="Rediger notat"
                />
                <p class="notebook__char-count">{{ editContent.length }}/500</p>
                <div class="notebook__actions">
                  <button class="notebook__save-btn" :disabled="!editContent.trim() || saving" @click="saveEdit(note.id)">
                    {{ saving ? 'Lagrer…' : 'Lagre' }}
                  </button>
                  <button class="notebook__cancel-btn" @click="cancelNoteEdit">Avbryt</button>
                </div>
                <p v-if="saveError" class="notebook__error" role="alert">{{ saveError }}</p>
              </template>
              <template v-else>
                <p class="notebook__note-text">{{ note.content }}</p>
                <div class="notebook__note-meta">
                  <time class="notebook__date">{{ formatDate(note.createdAt) }}</time>
                  <div class="notebook__note-controls">
                    <button class="notebook__edit-btn" @click="startNoteEdit(note)">Rediger</button>
                    <button class="notebook__delete-btn" @click="removeNote(note.id)">Slett</button>
                  </div>
                </div>
              </template>
            </div>
          </div>

          <div v-if="addingNote" class="notebook__add-form">
            <textarea
              v-model="newNoteContent"
              class="notebook__textarea"
              placeholder="Skriv et generelt notat her…"
              rows="3"
              maxlength="500"
              aria-label="Nytt generelt notat"
              autofocus
            />
            <p class="notebook__char-count">{{ newNoteContent.length }}/500</p>
            <div class="notebook__actions">
              <button class="notebook__save-btn" :disabled="!newNoteContent.trim() || saving" @click="submitGeneralNote">
                {{ saving ? 'Lagrer…' : 'Lagre' }}
              </button>
              <button class="notebook__cancel-btn" @click="cancelAddNote">Avbryt</button>
            </div>
            <p v-if="saveError" class="notebook__error" role="alert">{{ saveError }}</p>
          </div>
          <button v-else class="notebook__add-btn" @click="startAddNote">+ Legg til notat</button>
        </section>

        <!-- Empty state -->
        <div v-if="grouped.length === 0 && generalNotes.length === 0" class="notebook__empty">
          <span style="font-size:3rem" aria-hidden="true">🔍</span>
          <p class="notebook__empty-title">Ingen notater ennå.</p>
          <p class="notebook__empty-sub">Fullfør et stopp på kartet for å låse opp din første detektivrapport!</p>
        </div>

        <!-- Stop sections -->
        <div v-if="grouped.length" class="notebook__stops">
          <section
            v-for="(group, idx) in grouped"
            :key="group.stopId"
            class="pinned-note notebook__stop"
            :style="`--card-rotate: ${(idx % 3 - 1) * 0.4}deg`"
          >
            <h2 class="notebook__stop-heading">
              <span class="notebook__stop-num" aria-hidden="true">{{ group.stopOrder }}</span>
              {{ group.stopName }}
            </h2>

            <div v-if="group.autoTip" class="notebook__auto-tip">
              <p class="notebook__tip-label">🔍 Detektivrapporten</p>
              <p class="notebook__tip-content">{{ group.autoTip.content }}</p>
            </div>

            <div v-if="group.reflections.length" class="notebook__reflections">
              <h3 class="notebook__reflections-heading">Mine observasjoner</h3>
              <div v-for="r in group.reflections" :key="r.id" class="notebook__reflection">
                <p class="notebook__reflection-text">{{ r.content }}</p>
                <time class="notebook__date">{{ formatDate(r.createdAt) }}</time>
              </div>
            </div>

            <div v-if="editingStop === group.stopId" class="notebook__add-form">
              <textarea
                v-model="newContent"
                class="notebook__textarea"
                placeholder="Skriv din observasjon her…"
                rows="3"
                maxlength="500"
                aria-label="Ny observasjon"
              />
              <p class="notebook__char-count">{{ newContent.length }}/500</p>
              <div class="notebook__actions">
                <button
                  class="notebook__save-btn"
                  :disabled="!newContent.trim() || saving"
                  @click="submitReflection(group.stopId)"
                >{{ saving ? 'Lagrer…' : 'Lagre' }}</button>
                <button class="notebook__cancel-btn" @click="cancelEdit">Avbryt</button>
              </div>
              <p v-if="saveError" class="notebook__error" role="alert">{{ saveError }}</p>
            </div>
            <button v-else class="notebook__add-btn" @click="startEdit(group.stopId)">
              + Legg til observasjon
            </button>
          </section>
        </div>
      </template>
    </div>
  </CorkBoardPage>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import CorkBoardPage from '@/components/common/CorkBoardPage.vue'
import { useNotebookStore } from '@/stores/notebook'

const notebookStore = useNotebookStore()
const grouped       = computed(() => notebookStore.grouped)
const generalNotes  = computed(() => notebookStore.generalNotes)
const loading  = ref(true)
const error    = ref(null)
const saving   = ref(false)
const saveError = ref(null)

const editingStop    = ref(null)
const newContent     = ref('')
const addingNote     = ref(false)
const newNoteContent = ref('')
const editingNoteId  = ref(null)
const editContent    = ref('')

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

function startEdit(stopId) { editingStop.value = stopId; newContent.value = ''; saveError.value = null }
function cancelEdit()       { editingStop.value = null;   newContent.value = ''; saveError.value = null }

async function submitReflection(stopId) {
  const content = newContent.value.trim()
  if (!content) return
  saving.value = true; saveError.value = null
  try {
    await notebookStore.addReflection(stopId, content)
    cancelEdit()
  } catch (err) {
    console.error('[NotebookView] Failed to save reflection:', err)
    saveError.value = 'Kunne ikke lagre. Prøv igjen.'
  } finally { saving.value = false }
}

function startAddNote()  { addingNote.value = true;  newNoteContent.value = ''; saveError.value = null }
function cancelAddNote() { addingNote.value = false; newNoteContent.value = ''; saveError.value = null }

async function submitGeneralNote() {
  const content = newNoteContent.value.trim()
  if (!content) return
  saving.value = true; saveError.value = null
  try {
    await notebookStore.addGeneralNote(content)
    cancelAddNote()
  } catch (err) {
    console.error('[NotebookView] Failed to save general note:', err)
    saveError.value = 'Kunne ikke lagre. Prøv igjen.'
  } finally { saving.value = false }
}

function startNoteEdit(note) { editingNoteId.value = note.id; editContent.value = note.content; saveError.value = null }
function cancelNoteEdit()    { editingNoteId.value = null;    editContent.value = ''; saveError.value = null }

async function saveEdit(id) {
  const content = editContent.value.trim()
  if (!content) return
  saving.value = true; saveError.value = null
  try {
    await notebookStore.updateNote(id, content)
    cancelNoteEdit()
  } catch (err) {
    console.error('[NotebookView] Failed to update note:', err)
    saveError.value = 'Kunne ikke lagre. Prøv igjen.'
  } finally { saving.value = false }
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
  return new Intl.DateTimeFormat('nb-NO', { day: 'numeric', month: 'long', year: 'numeric' }).format(new Date(isoString))
}

onMounted(load)
</script>

<style scoped>
.notebook { display: flex; flex-direction: column; gap: var(--space-6); }

.notebook__state {
  display: flex; flex-direction: column; align-items: center;
  gap: var(--space-3); min-height: 16rem; justify-content: center;
  color: var(--color-cork-dark);
}
.notebook__state--error { color: var(--color-danger); }
.notebook__spinner {
  width: 32px; height: 32px;
  border: 3px solid rgba(0,0,0,0.1); border-top-color: var(--color-cork-dark);
  border-radius: 50%; animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
.notebook__retry {
  padding: var(--space-2) var(--space-4); background: var(--color-wood);
  color: var(--color-gold); border: none; border-radius: var(--radius-md);
  font-size: var(--text-sm); font-weight: 700; cursor: pointer;
}

.notebook__general { max-width: 720px; }
.notebook__section-heading { margin: 0 0 var(--space-4); font-size: var(--text-xl); color: var(--color-wood); }

.notebook__notes-list { display: flex; flex-direction: column; gap: var(--space-3); margin-bottom: var(--space-4); }
.notebook__note { border-left: 2px solid var(--color-cork); padding-left: var(--space-3); }
.notebook__note-text { margin: 0 0 var(--space-1); font-size: var(--text-base); color: var(--color-text); line-height: 1.5; }
.notebook__note-meta { display: flex; align-items: center; justify-content: space-between; gap: var(--space-2); }
.notebook__date { font-size: var(--text-xs); color: #777; font-style: italic; }
.notebook__note-controls { display: flex; gap: var(--space-2); }
.notebook__edit-btn,
.notebook__delete-btn {
  padding: var(--space-1) var(--space-2); font-size: var(--text-xs); font-weight: 600;
  border-radius: var(--radius-sm); cursor: pointer; border: 1px solid var(--color-border); background: transparent;
}
.notebook__edit-btn { color: var(--color-primary); }
.notebook__edit-btn:hover { background: var(--color-primary-soft); }
.notebook__delete-btn { color: var(--color-danger); }
.notebook__delete-btn:hover { background: color-mix(in srgb, var(--color-danger) 10%, transparent); }

.notebook__textarea {
  width: 100%; border: 1.5px solid #D4A96A; border-radius: var(--radius-sm);
  padding: var(--space-3); font-size: var(--text-base); font-family: inherit;
  resize: vertical; color: var(--color-text); background: #FEFCF0; box-sizing: border-box;
}
.notebook__textarea:focus { outline: none; border-color: var(--color-cork-dark); }
.notebook__char-count { margin: var(--space-1) 0 0; font-size: var(--text-xs); color: #777; text-align: right; }
.notebook__actions { display: flex; gap: var(--space-3); margin-top: var(--space-3); }
.notebook__save-btn {
  padding: var(--space-2) var(--space-6); background: var(--color-wood); color: var(--color-gold);
  border: none; border-radius: var(--radius-md); font-size: var(--text-sm); font-weight: 700; cursor: pointer;
}
.notebook__save-btn:hover:not(:disabled) { background: var(--color-wood-mid); }
.notebook__save-btn:disabled { opacity: 0.55; cursor: not-allowed; }
.notebook__cancel-btn {
  padding: var(--space-2) var(--space-4); background: transparent;
  color: #777; border: 1px solid #D4A96A; border-radius: var(--radius-md); font-size: var(--text-sm); cursor: pointer;
}
.notebook__error { margin: var(--space-2) 0 0; font-size: var(--text-sm); color: var(--color-danger); }

.notebook__add-btn {
  align-self: flex-start; padding: var(--space-2) var(--space-4);
  background: transparent; color: var(--color-wood);
  border: 1px dashed var(--color-cork); border-radius: var(--radius-md);
  font-size: var(--text-sm); font-weight: 600; cursor: pointer;
}
.notebook__add-btn:hover { background: rgba(168, 114, 48, 0.1); border-style: solid; }

.notebook__empty {
  display: flex; flex-direction: column; align-items: center;
  gap: var(--space-3); text-align: center; color: var(--color-cork-dark); min-height: 12rem; justify-content: center;
}
.notebook__empty-title { margin: 0; font-size: var(--text-lg); font-weight: 700; }
.notebook__empty-sub   { margin: 0; font-size: var(--text-sm); }

.notebook__stops { display: flex; flex-direction: column; gap: var(--space-6); max-width: 720px; }

.notebook__stop { display: flex; flex-direction: column; gap: var(--space-4); }
.notebook__stop-heading {
  margin: 0; font-size: var(--text-xl); color: var(--color-wood);
  display: flex; align-items: center; gap: var(--space-3);
}
.notebook__stop-num {
  display: inline-flex; align-items: center; justify-content: center;
  width: 28px; height: 28px; border-radius: 50%;
  background: var(--color-wood); color: var(--color-gold);
  font-size: var(--text-sm); font-weight: 700; flex-shrink: 0;
}

.notebook__auto-tip {
  background: rgba(168, 114, 48, 0.12);
  border-left: 3px solid var(--color-cork);
  border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
  padding: var(--space-4);
}
.notebook__tip-label { margin: 0 0 var(--space-2); font-size: var(--text-sm); font-weight: 600; color: var(--color-cork-dark); text-transform: uppercase; letter-spacing: 0.05em; }
.notebook__tip-content { margin: 0; font-size: var(--text-base); color: var(--color-text); line-height: 1.6; }

.notebook__reflections-heading { margin: 0 0 var(--space-3); font-size: var(--text-sm); font-weight: 600; color: #777; text-transform: uppercase; letter-spacing: 0.05em; }
.notebook__reflection { border-left: 2px solid #D4A96A; padding-left: var(--space-3); margin-bottom: var(--space-3); }
.notebook__reflection-text { margin: 0 0 var(--space-1); font-size: var(--text-base); color: var(--color-text); line-height: 1.5; }
</style>
