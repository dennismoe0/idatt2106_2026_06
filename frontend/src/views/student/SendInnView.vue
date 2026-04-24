<script setup>
import { ref, computed } from 'vue'
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

function onDragOver(e) {
  e.preventDefault()
  dragActive.value = true
}
function onDragLeave() {
  dragActive.value = false
}
function onDrop(e) {
  e.preventDefault()
  dragActive.value = false
  const file = e.dataTransfer.files[0]
  if (file && file.type.startsWith('image/')) {
    const reader = new FileReader()
    reader.onload = (ev) => { imageUrl.value = ev.target.result }
    reader.readAsDataURL(file)
  }
}
function onFileInput(e) {
  const file = e.target.files[0]
  if (file) {
    const reader = new FileReader()
    reader.onload = (ev) => { imageUrl.value = ev.target.result }
    reader.readAsDataURL(file)
  }
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
  <div class="sendin-view">
    <h1 class="sendin-title">Send inn ditt mysterium</h1>
    <p class="sendin-desc">Fant du noe rart på nett? Del det med læreren din, og det kan bli Ukas Mysterium!</p>

    <div v-if="submitted" class="sendin-success">
      <span class="sendin-success__icon">🕵️</span>
      <p>Takk! Læreren din vil se på det du sendte inn.</p>
    </div>

    <form v-else class="sendin-form" @submit.prevent="submitForm">
      <label class="sendin-label">
        Tittel *
        <input
          v-model="title"
          class="sendin-input"
          maxlength="200"
          placeholder="Gi mysteriet en tittel"
          required
        />
      </label>

      <label class="sendin-label">
        Beskriv hva du fant
        <textarea
          v-model="description"
          class="sendin-textarea"
          rows="4"
          placeholder="Hva er mistenkelig? Hvorfor synes du dette er rart?"
        />
      </label>

      <div class="sendin-label">
        Last opp bilde (valgfritt)
        <div
          class="sendin-dropzone"
          :class="{ 'sendin-dropzone--active': dragActive }"
          @dragover="onDragOver"
          @dragleave="onDragLeave"
          @drop="onDrop"
          @click="$refs.fileInput.click()"
        >
          <img v-if="imageUrl" :src="imageUrl" alt="Forhåndsvisning" class="sendin-preview" />
          <span v-else>Dra og slipp bilde her, eller klikk for å velge</span>
          <input
            ref="fileInput"
            type="file"
            accept="image/*"
            class="sendin-hidden"
            @change="onFileInput"
          />
        </div>
      </div>

      <p v-if="errorMsg" class="sendin-error">{{ errorMsg }}</p>

      <button type="submit" class="sendin-btn" :disabled="submitting">
        {{ submitting ? 'Sender...' : 'Send inn til lærer' }}
      </button>
    </form>
  </div>
</template>

<style scoped>
.sendin-view {
  max-width: 640px;
  margin: 0 auto;
  padding: 2rem 1rem;
}
.sendin-title {
  font-size: 1.75rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
}
.sendin-desc {
  color: var(--color-text-muted, #666);
  margin-bottom: 2rem;
}
.sendin-form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}
.sendin-label {
  display: flex;
  flex-direction: column;
  gap: 0.375rem;
  font-weight: 600;
  font-size: 0.9rem;
}
.sendin-input,
.sendin-textarea {
  padding: 0.625rem 0.875rem;
  border: 2px solid var(--color-border, #ddd);
  border-radius: 0.5rem;
  font-size: 1rem;
  font-family: inherit;
  resize: vertical;
}
.sendin-dropzone {
  border: 2px dashed var(--color-border, #ccc);
  border-radius: 0.75rem;
  padding: 2rem;
  text-align: center;
  cursor: pointer;
  transition: border-color 0.2s, background 0.2s;
  min-height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.sendin-dropzone--active {
  border-color: var(--color-primary, #4f46e5);
  background: color-mix(in srgb, var(--color-primary, #4f46e5) 8%, transparent);
}
.sendin-preview {
  max-height: 200px;
  max-width: 100%;
  border-radius: 0.5rem;
}
.sendin-hidden {
  display: none;
}
.sendin-btn {
  padding: 0.875rem 2rem;
  background: var(--color-primary, #4f46e5);
  color: white;
  border: none;
  border-radius: 0.75rem;
  font-size: 1rem;
  font-weight: 700;
  cursor: pointer;
}
.sendin-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.sendin-error {
  color: #dc2626;
  font-size: 0.875rem;
}
.sendin-success {
  text-align: center;
  padding: 2rem;
  background: color-mix(in srgb, var(--color-success, #22c55e) 10%, transparent);
  border-radius: 1rem;
}
.sendin-success__icon {
  font-size: 3rem;
  display: block;
  margin-bottom: 0.75rem;
}
</style>
