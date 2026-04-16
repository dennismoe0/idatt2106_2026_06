<template>
  <main class="avatar-view">
    <div class="avatar-shell">
      <header class="avatar-header">
        <BackButton label="Tilbake" />
        <div>
          <p class="eyebrow">Elevprofil</p>
          <h1>Bygg din detektiv</h1>
          <p class="subtitle">
            Velg stil, se endringene med en gang, og lagre avataren din før du går tilbake til kartet.
          </p>
        </div>
      </header>

      <section v-if="isLoading" class="state-card">
        <LoadingSpinner size="lg" />
        <p>Laster avatar og tilpasningsvalg...</p>
      </section>

      <section v-else-if="loadError" class="state-card state-card--error">
        <h2>Kunne ikke laste avatar</h2>
        <p>{{ loadError }}</p>
        <BaseButton @click="loadAvatarPage">Prøv igjen</BaseButton>
      </section>

      <section v-else class="avatar-layout">
        <div class="avatar-panel avatar-panel--preview">
          <AvatarPreview :selections="form" />

          <div v-if="saveMessage" class="feedback feedback--success">
            {{ saveMessage }}
          </div>
          <div v-if="saveError" class="feedback feedback--error">
            {{ saveError }}
          </div>
        </div>

        <form class="avatar-panel avatar-panel--form" @submit.prevent="saveAvatar">
          <div class="selector-grid">
            <label v-for="field in fields" :key="field.key" class="selector-field">
              <span>{{ field.label }}</span>
              <select v-model="form[field.key]">
                <option
                  v-for="option in options[field.key] || []"
                  :key="option"
                  :value="option"
                >
                  {{ formatOption(option) }}
                </option>
              </select>
            </label>
          </div>

          <div class="actions">
            <BaseButton
              type="button"
              variant="secondary"
              :disabled="isSaving || !hasChanges"
              @click="resetForm"
            >
              Tilbakestill
            </BaseButton>
            <BaseButton
              type="submit"
              :loading="isSaving"
              :disabled="!hasChanges"
            >
              Lagre avatar
            </BaseButton>
          </div>
        </form>
      </section>
    </div>
  </main>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import AvatarPreview from '@/components/student/AvatarPreview.vue'
import BackButton from '@/components/common/BackButton.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import { useAvatarStore } from '@/stores/avatar'
import { formatOption } from '@/utils/avatarOptions'

const avatarStore = useAvatarStore()

const fields = [
  { key: 'gender', label: 'Kjønn' },
  { key: 'eyeColor', label: 'Øyenfarge' },
  { key: 'skinColor', label: 'Hudtone' },
  { key: 'hairColor', label: 'Hårfarge' },
  { key: 'hairStyle', label: 'Frisyre' },
  { key: 'outfit', label: 'Antrekk' },
  { key: 'outfitColor', label: 'Antrekksfarge' },
  { key: 'hatColor', label: 'Hattefarge' },
  { key: 'accessory', label: 'Tilbehør' },
]

const form = reactive(createEmptyAvatar())
const originalAvatar = ref(createEmptyAvatar())
const isLoading = ref(true)
const isSaving = ref(false)
const loadError = ref('')
const saveError = ref('')
const saveMessage = ref('')

const options = computed(() => avatarStore.options || {})
const hasChanges = computed(() =>
  fields.some(({ key }) => form[key] !== originalAvatar.value[key])
)

function createEmptyAvatar() {
  return {
    gender: '',
    eyeColor: '',
    skinColor: '',
    hairColor: '',
    hairStyle: '',
    outfit: '',
    outfitColor: '',
    hatColor: '',
    accessory: '',
  }
}

function applyAvatar(target, source) {
  for (const { key } of fields) {
    target[key] = source?.[key] ?? ''
  }
}

function resetForm() {
  applyAvatar(form, originalAvatar.value)
  saveError.value = ''
  saveMessage.value = ''
}

async function loadAvatarPage() {
  console.log('[AvatarView] Loading avatar and options')
  isLoading.value = true
  loadError.value = ''
  saveError.value = ''

  try {
    const [avatar, fetchedOptions] = await Promise.all([
      avatarStore.fetchAvatar(),
      avatarStore.fetchOptions(),
    ])

    applyAvatar(form, avatar)
    console.log('[AvatarView] Loaded avatar:', avatar)

    for (const { key } of fields) {
      if (!form[key] && fetchedOptions[key]?.length) {
        form[key] = fetchedOptions[key][0]
      }
    }
    originalAvatar.value = { ...form }
  } catch (error) {
    console.error('[AvatarView] Failed to load avatar:', error)
    loadError.value = error.response?.data?.error || error.response?.data?.message || 'Noe gikk galt ved lasting av avatardata.'
  } finally {
    isLoading.value = false
  }
}

async function saveAvatar() {
  console.log('[AvatarView] Saving avatar:', { ...form })
  isSaving.value = true
  saveError.value = ''
  saveMessage.value = ''

  try {
    const updatedAvatar = await avatarStore.updateAvatar({ ...form })
    applyAvatar(form, updatedAvatar)
    originalAvatar.value = { ...form }
    console.log('[AvatarView] Avatar saved')
    saveMessage.value = 'Avatar lagret.'
  } catch (error) {
    console.error('[AvatarView] Failed to save avatar:', error)
    saveError.value = error.response?.data?.error || error.response?.data?.message || 'Kunne ikke lagre avatar.'
  } finally {
    isSaving.value = false
  }
}

loadAvatarPage()
</script>

<style scoped>
.avatar-view {
  min-height: 100vh;
  padding: var(--space-8) var(--space-4) var(--space-12);
  background:
    radial-gradient(circle at top left, var(--color-primary-soft-strong), transparent 30%),
    radial-gradient(circle at bottom right, var(--color-accent-soft), transparent 28%),
    linear-gradient(180deg, var(--color-surface-soft) 0%, var(--color-surface-soft-alt) 100%);
}

.avatar-shell {
  max-width: 78rem;
  margin: 0 auto;
}

.avatar-header {
  display: grid;
  gap: var(--space-4);
  margin-bottom: var(--space-8);
}

.eyebrow {
  margin: 0 0 0.35rem;
  text-transform: uppercase;
  letter-spacing: 0.14em;
  font-size: 0.76rem;
  font-weight: 700;
  color: var(--color-primary);
}

.avatar-header h1 {
  margin: 0;
  font-size: clamp(2rem, 4vw, 3.4rem);
  line-height: 0.95;
  color: var(--color-text);
}

.subtitle {
  max-width: 42rem;
  margin: var(--space-3) 0 0;
  color: var(--color-text-muted);
  font-size: var(--text-base);
}

.avatar-layout {
  display: grid;
  gap: var(--space-6);
}

.avatar-panel,
.state-card {
  padding: var(--space-6);
  border-radius: var(--radius-xl);
  background: var(--color-surface-glass);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-lg);
  backdrop-filter: blur(18px);
}

.avatar-panel--preview {
  display: grid;
  gap: var(--space-4);
  align-content: start;
}

.avatar-panel--form {
  display: grid;
  gap: var(--space-6);
}

.selector-grid {
  display: grid;
  gap: var(--space-4);
}

.selector-field {
  display: grid;
  gap: var(--space-2);
}

.selector-field span {
  font-size: var(--text-sm);
  font-weight: 700;
  color: var(--color-text);
}

.selector-field select {
  width: 100%;
  padding: var(--space-4);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border-strong);
  background: var(--color-surface);
  color: var(--color-text);
  font-size: var(--text-base);
  outline: none;
}

.selector-field select:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 4px var(--color-primary-focus-ring);
}

.actions {
  display: flex;
  gap: var(--space-3);
  justify-content: flex-end;
  flex-wrap: wrap;
}

.state-card {
  min-height: 18rem;
  display: grid;
  place-items: center;
  gap: var(--space-3);
  text-align: center;
}

.state-card p,
.state-card h2 {
  margin: 0;
}

.state-card--error {
  color: var(--color-danger);
}

.feedback {
  padding: var(--space-4);
  border-radius: var(--radius-lg);
  font-size: var(--text-sm);
  font-weight: 600;
}

.feedback--success {
  background: var(--color-success-soft);
  color: var(--color-success);
}

.feedback--error {
  background: var(--color-danger-soft);
  color: var(--color-danger);
}

@media (min-width: 860px) {
  .avatar-layout {
    grid-template-columns: minmax(18rem, 24rem) minmax(0, 1fr);
    align-items: start;
  }

  .selector-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
