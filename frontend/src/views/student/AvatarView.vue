<template>
  <main class="avatar-view">
    <div class="avatar-shell">
      <header class="avatar-header">
        <BackButton label="Tilbake" />
        <div>
          <p class="eyebrow">Student Profile</p>
          <h1>Build Your Detective</h1>
          <p class="subtitle">
            Choose your look, preview changes live, and save your avatar before heading back to the map.
          </p>
        </div>
      </header>

      <section v-if="isLoading" class="state-card">
        <LoadingSpinner size="lg" />
        <p>Laster avatar og tilpasningsvalg...</p>
      </section>

      <section v-else-if="loadError" class="state-card state-card--error">
        <h2>Could not load avatar</h2>
        <p>{{ loadError }}</p>
        <BaseButton @click="loadAvatarPage">Try again</BaseButton>
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
              Reset
            </BaseButton>
            <BaseButton
              type="submit"
              :loading="isSaving"
              :disabled="!hasChanges"
            >
              Save avatar
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

const avatarStore = useAvatarStore()

const fields = [
  { key: 'gender', label: 'Gender' },
  { key: 'eyeColor', label: 'Eye color' },
  { key: 'skinColor', label: 'Skin color' },
  { key: 'hairColor', label: 'Hair color' },
  { key: 'hairStyle', label: 'Hair style' },
  { key: 'outfit', label: 'Outfit' },
  { key: 'outfitColor', label: 'Outfit color' },
  { key: 'hatColor', label: 'Hat color' },
  { key: 'accessory', label: 'Accessory' },
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

function formatOption(value) {
  return value
    .split('-')
    .map(part => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ')
}

function resetForm() {
  applyAvatar(form, originalAvatar.value)
  saveError.value = ''
  saveMessage.value = ''
}

async function loadAvatarPage() {
  isLoading.value = true
  loadError.value = ''
  saveError.value = ''

  try {
    const [avatar, fetchedOptions] = await Promise.all([
      avatarStore.fetchAvatar(),
      avatarStore.fetchOptions(),
    ])

    applyAvatar(form, avatar)
    originalAvatar.value = { ...form }

    for (const { key } of fields) {
      if (!form[key] && fetchedOptions[key]?.length) {
        form[key] = fetchedOptions[key][0]
      }
    }
    originalAvatar.value = { ...form }
  } catch (error) {
    loadError.value = error.response?.data?.error || error.response?.data?.message || 'Something went wrong while loading avatar data.'
  } finally {
    isLoading.value = false
  }
}

async function saveAvatar() {
  isSaving.value = true
  saveError.value = ''
  saveMessage.value = ''

  try {
    const updatedAvatar = await avatarStore.updateAvatar({ ...form })
    applyAvatar(form, updatedAvatar)
    originalAvatar.value = { ...form }
    saveMessage.value = 'Avatar saved successfully.'
  } catch (error) {
    saveError.value = error.response?.data?.error || error.response?.data?.message || 'Could not save avatar.'
  } finally {
    isSaving.value = false
  }
}

loadAvatarPage()
</script>

<style scoped>
.avatar-view {
  min-height: 100vh;
  padding: 2rem 1rem 3rem;
  background:
    radial-gradient(circle at top left, rgba(106, 146, 255, 0.22), transparent 30%),
    radial-gradient(circle at bottom right, rgba(243, 195, 77, 0.2), transparent 28%),
    linear-gradient(180deg, #f6f8ff 0%, #eef3ff 100%);
}

.avatar-shell {
  max-width: 78rem;
  margin: 0 auto;
}

.avatar-header {
  display: grid;
  gap: 1rem;
  margin-bottom: 1.75rem;
}

.eyebrow {
  margin: 0 0 0.35rem;
  text-transform: uppercase;
  letter-spacing: 0.14em;
  font-size: 0.76rem;
  font-weight: 700;
  color: #5e76c8;
}

.avatar-header h1 {
  margin: 0;
  font-size: clamp(2rem, 4vw, 3.4rem);
  line-height: 0.95;
  color: #1d2e49;
}

.subtitle {
  max-width: 42rem;
  margin: 0.75rem 0 0;
  color: #5f6f8b;
  font-size: 1.02rem;
}

.avatar-layout {
  display: grid;
  gap: 1.5rem;
}

.avatar-panel,
.state-card {
  padding: 1.4rem;
  border-radius: 1.5rem;
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(78, 112, 185, 0.12);
  box-shadow: 0 20px 48px rgba(40, 61, 98, 0.1);
  backdrop-filter: blur(18px);
}

.avatar-panel--preview {
  display: grid;
  gap: 1rem;
  align-content: start;
}

.avatar-panel--form {
  display: grid;
  gap: 1.5rem;
}

.selector-grid {
  display: grid;
  gap: 1rem;
}

.selector-field {
  display: grid;
  gap: 0.45rem;
}

.selector-field span {
  font-size: 0.92rem;
  font-weight: 700;
  color: #29405f;
}

.selector-field select {
  width: 100%;
  padding: 0.9rem 1rem;
  border-radius: 1rem;
  border: 1px solid rgba(89, 119, 187, 0.24);
  background: #fff;
  color: #22324d;
  font-size: 0.98rem;
  outline: none;
}

.selector-field select:focus {
  border-color: #4f7cff;
  box-shadow: 0 0 0 4px rgba(79, 124, 255, 0.14);
}

.actions {
  display: flex;
  gap: 0.85rem;
  justify-content: flex-end;
  flex-wrap: wrap;
}

.state-card {
  min-height: 18rem;
  display: grid;
  place-items: center;
  gap: 0.75rem;
  text-align: center;
}

.state-card p,
.state-card h2 {
  margin: 0;
}

.state-card--error {
  color: #7f2530;
}

.feedback {
  padding: 0.9rem 1rem;
  border-radius: 1rem;
  font-size: 0.95rem;
  font-weight: 600;
}

.feedback--success {
  background: rgba(76, 175, 120, 0.12);
  color: #1d6a44;
}

.feedback--error {
  background: rgba(216, 88, 96, 0.12);
  color: #9f2238;
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
