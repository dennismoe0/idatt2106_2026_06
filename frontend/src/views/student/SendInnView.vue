<script setup>
import { ref, computed } from 'vue'
import { RouterLink } from 'vue-router'
import { weeklyMysteryService } from '@/services/weeklyMysteryService.js'
import { useClassroomStore } from '@/stores/classroom.js'

const classroomStore = useClassroomStore()
const classroomId = computed(() => classroomStore.currentClassroomId)

const title = ref('')
const description = ref('')
const imageUrl = ref('')
const dragActive = ref(false)
const submitted = ref(false)
const submitting = ref(false)
const errorMsg = ref('')

function resizeToDataUrl(file) {
  return new Promise((resolve) => {
    const reader = new FileReader()
    reader.onload = (ev) => {
      const img = new Image()
      img.onload = () => {
        const MAX = 800
        const scale = Math.min(1, MAX / Math.max(img.width, img.height))
        const canvas = document.createElement('canvas')
        canvas.width  = Math.round(img.width  * scale)
        canvas.height = Math.round(img.height * scale)
        canvas.getContext('2d').drawImage(img, 0, 0, canvas.width, canvas.height)
        resolve(canvas.toDataURL('image/jpeg', 0.75))
      }
      img.src = ev.target.result
    }
    reader.readAsDataURL(file)
  })
}

async function onDrop(e) {
  e.preventDefault()
  dragActive.value = false
  const file = e.dataTransfer.files[0]
  if (file && file.type.startsWith('image/'))
    imageUrl.value = await resizeToDataUrl(file)
}
async function onFileInput(e) {
  const file = e.target.files[0]
  if (file)
    imageUrl.value = await resizeToDataUrl(file)
}

function clearImage() {
  imageUrl.value = ''
}

function resetForm() {
  title.value = ''
  description.value = ''
  imageUrl.value = ''
  submitted.value = false
  errorMsg.value = ''
}

async function submitForm() {
  if (!title.value.trim()) {
    errorMsg.value = 'Tittel er påkrevd'
    return
  }
  if (!classroomId.value) {
    errorMsg.value = 'Ingen aktiv klasse funnet'
    return
  }
  submitting.value = true
  errorMsg.value = ''
  try {
    await weeklyMysteryService.submitMystery(
      classroomId.value,
      title.value,
      description.value,
      imageUrl.value
    )
    submitted.value = true
  } catch (err) {
    console.error('[SendInnView] submit failed', err)
    errorMsg.value = 'Noe gikk galt. Prøv igjen.'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="sendin">

    <div class="sendin__header">
      <h1 class="sendin__title">Send inn mysterium</h1>
      <p class="sendin__lead">
        Fant du noe rart på nett? Send det til læreren — det kan bli Ukas Mysterium!
      </p>
    </div>

    <!-- Success state -->
    <div v-if="submitted" class="sendin__success">
      <span class="sendin__success-icon" aria-hidden="true">🕵️</span>
      <h2 class="sendin__success-title">Sendt inn!</h2>
      <p class="sendin__success-body">Læreren din vil se på det du sendte inn snart.</p>
      <div class="sendin__success-actions">
        <button class="sendin__btn sendin__btn--secondary" @click="resetForm">
          Send inn et nytt
        </button>
        <RouterLink :to="{ name: 'Home' }" class="sendin__btn sendin__btn--primary">
          Til forsiden
        </RouterLink>
      </div>
    </div>

    <!-- Form -->
    <form v-else class="sendin__form" @submit.prevent="submitForm" novalidate>

      <div class="sendin__field">
        <label class="sendin__label" for="mystery-title">
          Tittel <span class="sendin__required" aria-hidden="true">*</span>
        </label>
        <input
          id="mystery-title"
          v-model="title"
          class="sendin__input"
          maxlength="200"
          placeholder="Gi mysteriet en tittel"
          required
          autocomplete="off"
        />
      </div>

      <div class="sendin__field">
        <label class="sendin__label" for="mystery-desc">Beskriv hva du fant</label>
        <textarea
          id="mystery-desc"
          v-model="description"
          class="sendin__textarea"
          rows="5"
          placeholder="Hva er mistenkelig? Hvorfor synes du dette er rart?"
        />
      </div>

      <div class="sendin__field">
        <span class="sendin__label" id="img-label">Last opp bilde (valgfritt)</span>

        <div
          class="sendin__dropzone"
          :class="{ 'sendin__dropzone--active': dragActive }"
          role="button"
          tabindex="0"
          aria-labelledby="img-label"
          @dragover.prevent="dragActive = true"
          @dragleave="dragActive = false"
          @drop="onDrop"
          @click="$refs.fileInput.click()"
          @keydown.enter.space.prevent="$refs.fileInput.click()"
        >
          <template v-if="imageUrl">
            <img :src="imageUrl" alt="Forhåndsvisning av valgt bilde" class="sendin__preview" />
            <button
              type="button"
              class="sendin__clear-img"
              aria-label="Fjern bilde"
              @click.stop="clearImage"
            >✕ Fjern</button>
          </template>
          <template v-else>
            <span class="sendin__dropzone-icon" aria-hidden="true">🖼️</span>
            <span class="sendin__dropzone-text">Dra og slipp bilde her, eller klikk for å velge</span>
            <span class="sendin__dropzone-hint">JPG, PNG, GIF — alle størrelser OK</span>
          </template>
          <input
            ref="fileInput"
            type="file"
            accept="image/*"
            class="sendin__file-hidden"
            @change="onFileInput"
          />
        </div>
      </div>

      <p v-if="errorMsg" role="alert" class="sendin__error">{{ errorMsg }}</p>

      <div class="sendin__actions">
        <RouterLink :to="{ name: 'Home' }" class="sendin__btn sendin__btn--ghost">
          ← Tilbake
        </RouterLink>
        <button type="submit" class="sendin__btn sendin__btn--primary" :disabled="submitting">
          {{ submitting ? 'Sender...' : 'Send inn til lærer' }}
        </button>
      </div>

    </form>

  </div>
</template>

<style scoped>
.sendin {
  max-width: 760px;
  margin: 0 auto;
  padding: 2rem 1.25rem 3rem;
}

/* ── Header ── */
.sendin__header {
  margin-bottom: 2rem;
}
.sendin__title {
  font-size: clamp(1.5rem, 4vw, 2rem);
  font-weight: 800;
  color: var(--color-heading);
  margin: 0 0 0.4rem;
}
.sendin__lead {
  color: var(--color-text-muted);
  font-size: 1rem;
  margin: 0;
}

/* ── Form ── */
.sendin__form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 1rem;
  padding: 2rem;
  box-shadow: var(--shadow-sm);
}

.sendin__field {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.sendin__label {
  font-weight: 600;
  font-size: 0.9rem;
  color: var(--color-text);
}
.sendin__required {
  color: var(--color-danger);
  margin-left: 0.15rem;
}

.sendin__input,
.sendin__textarea {
  padding: 0.625rem 0.875rem;
  border: 1.5px solid var(--color-border-strong);
  border-radius: 0.5rem;
  font-size: 1rem;
  font-family: inherit;
  color: var(--color-text);
  background: var(--color-surface);
  resize: vertical;
  transition: border-color 0.15s;
}
.sendin__input:focus,
.sendin__textarea:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--color-primary-focus-ring);
}

/* ── Drop zone ── */
.sendin__dropzone {
  position: relative;
  border: 2px dashed var(--color-border-strong);
  border-radius: 0.75rem;
  padding: 2rem 1.5rem;
  text-align: center;
  cursor: pointer;
  transition: border-color 0.15s, background 0.15s;
  min-height: 140px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.4rem;
  background: var(--color-surface-soft);
}
.sendin__dropzone:hover,
.sendin__dropzone:focus {
  border-color: var(--color-primary);
  background: var(--color-primary-soft);
  outline: none;
}
.sendin__dropzone--active {
  border-color: var(--color-primary);
  background: var(--color-primary-soft);
}
.sendin__dropzone-icon { font-size: 2rem; }
.sendin__dropzone-text {
  font-weight: 600;
  color: var(--color-text);
  font-size: 0.95rem;
}
.sendin__dropzone-hint {
  font-size: 0.8rem;
  color: var(--color-text-muted);
}
.sendin__preview {
  max-height: 220px;
  max-width: 100%;
  border-radius: 0.5rem;
  object-fit: contain;
}
.sendin__clear-img {
  margin-top: 0.5rem;
  background: none;
  border: 1px solid var(--color-danger);
  color: var(--color-danger);
  border-radius: 0.375rem;
  padding: 0.25rem 0.75rem;
  font-size: 0.8rem;
  cursor: pointer;
  font-family: inherit;
}
.sendin__file-hidden { display: none; }

/* ── Actions ── */
.sendin__actions {
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
  flex-wrap: wrap;
}

.sendin__btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0.75rem 1.75rem;
  border-radius: 0.625rem;
  font-size: 0.95rem;
  font-weight: 700;
  font-family: inherit;
  cursor: pointer;
  text-decoration: none;
  border: none;
  transition: opacity 0.15s, background 0.15s;
}
.sendin__btn--primary {
  background: var(--color-primary);
  color: #fff;
}
.sendin__btn--primary:hover { background: var(--color-primary-dark); }
.sendin__btn--primary:disabled { opacity: 0.55; cursor: not-allowed; }
.sendin__btn--secondary {
  background: var(--color-surface-soft);
  color: var(--color-text);
  border: 1.5px solid var(--color-border-strong);
}
.sendin__btn--ghost {
  background: none;
  color: var(--color-text-muted);
  border: 1.5px solid var(--color-border);
}
.sendin__btn--ghost:hover { color: var(--color-text); border-color: var(--color-border-strong); }

/* ── Error ── */
.sendin__error {
  color: var(--color-danger);
  font-size: 0.875rem;
  margin: 0;
  padding: 0.5rem 0.75rem;
  background: var(--color-danger-soft);
  border-radius: 0.375rem;
}

/* ── Success ── */
.sendin__success {
  text-align: center;
  padding: 3rem 2rem;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 1rem;
  box-shadow: var(--shadow-sm);
}
.sendin__success-icon {
  font-size: 3.5rem;
  display: block;
  margin-bottom: 1rem;
}
.sendin__success-title {
  font-size: 1.5rem;
  font-weight: 800;
  color: var(--color-heading);
  margin: 0 0 0.5rem;
}
.sendin__success-body {
  color: var(--color-text-muted);
  margin: 0 0 2rem;
}
.sendin__success-actions {
  display: flex;
  gap: 0.75rem;
  justify-content: center;
  flex-wrap: wrap;
}
</style>
