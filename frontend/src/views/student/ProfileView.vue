<template>
  <CorkBoardPage page-title="Min profil" :back-to="{ name: 'Home' }">

    <div class="profile-view__body">

      <!-- Avatar -->
      <section class="profile-avatar" aria-label="Avatar">
        <div class="profile-avatar__frame">
          <img
            :src="avatarImage"
            class="profile-avatar__img"
            :alt="`Avatar for ${displayName}`"
          />
        </div>
        <RouterLink :to="{ name: 'Avatar' }" class="profile-avatar__edit-btn">
          Rediger avatar
        </RouterLink>
      </section>

      <!-- Display name (editable) -->
      <section class="profile-name-section" aria-label="Navn">
        <div v-if="!editingName" class="profile-name-display">
          <h1 class="profile-view__name">{{ displayName }}</h1>
          <button
            class="profile-name-display__edit"
            @click="startEditing"
            aria-label="Rediger visningsnavn"
          >✏️</button>
        </div>

        <form v-else class="profile-name-edit" @submit.prevent="saveName" novalidate>
          <input
            ref="nameInput"
            v-model="nameDraft"
            class="profile-name-edit__input"
            type="text"
            maxlength="50"
            aria-label="Nytt visningsnavn"
            :disabled="savingName"
          />
          <div class="profile-name-edit__actions">
            <button type="submit" class="profile-name-edit__save" :disabled="savingName || !nameValid">
              {{ savingName ? '…' : 'Lagre' }}
            </button>
            <button type="button" class="profile-name-edit__cancel" :disabled="savingName" @click="cancelEditing">
              Avbryt
            </button>
          </div>
          <p v-if="nameError" class="profile-name-edit__error" role="alert">{{ nameError }}</p>
        </form>
      </section>

      <!-- Stats -->
      <section class="profile-stats" aria-label="Statistikk">
        <div class="profile-stats__pill">
          <span class="profile-stats__icon" aria-hidden="true">🕵️</span>
          <span class="profile-stats__value">{{ level }}</span>
          <span class="profile-stats__label">Nivå</span>
        </div>
        <div class="profile-stats__pill">
          <span class="profile-stats__icon" aria-hidden="true">⚡</span>
          <span class="profile-stats__value">{{ xp }}</span>
          <span class="profile-stats__label">XP</span>
        </div>
        <div class="profile-stats__pill">
          <span class="profile-stats__icon" aria-hidden="true">⭐</span>
          <span class="profile-stats__value">{{ starBalance }}</span>
          <span class="profile-stats__label">Stjerner</span>
        </div>
      </section>

      <!-- Info -->
      <section class="profile-info" aria-label="Profilinformasjon">
        <dl class="profile-info__list">
          <div class="profile-info__row">
            <dt class="profile-info__label">Visningsnavn i klassen</dt>
            <dd class="profile-info__value">{{ displayName }}</dd>
          </div>
          <div class="profile-info__row" v-if="username">
            <dt class="profile-info__label">Brukernavn</dt>
            <dd class="profile-info__value">{{ username }}</dd>
          </div>
        </dl>
      </section>

    </div>
  </CorkBoardPage>
</template>

<script setup>
import { computed, ref, nextTick, onMounted } from 'vue'
import { useAvatarStore } from '@/stores/avatar'
import { useGameStore } from '@/stores/game'
import { useClassroomStore } from '@/stores/classroom'
import { useAuthStore } from '@/stores/auth'
import CorkBoardPage from '@/components/common/CorkBoardPage.vue'
import neutralAvatar from '@/assets/avatar/presets/adventurer-neutral.svg'
import lightAvatar from '@/assets/avatar/presets/adventurer-light.svg'
import warmAvatar from '@/assets/avatar/presets/adventurer-warm.svg'

const avatarStore = useAvatarStore()
const gameStore = useGameStore()
const classroomStore = useClassroomStore()
const authStore = useAuthStore()

const displayName = computed(() => classroomStore.displayName || 'Detektiv')
const level = computed(() => gameStore.level)
const xp = computed(() => gameStore.xp)
const starBalance = computed(() => gameStore.starBalance)

const username = computed(() => {
  const email = authStore.email
  if (!email) return null
  return email.endsWith('@student.local') ? email.replace('@student.local', '') : email.split('@')[0]
})

const avatarImage = computed(() => {
  const skin = avatarStore.avatar?.skinColor
  if (skin === 'light') return lightAvatar
  if (skin === 'dark') return warmAvatar
  return neutralAvatar
})

// Editable display name
const editingName = ref(false)
const nameInput = ref(null)
const nameDraft = ref('')
const savingName = ref(false)
const nameError = ref('')

const nameValid = computed(() => nameDraft.value.trim().length >= 2)

function startEditing() {
  nameDraft.value = displayName.value
  nameError.value = ''
  editingName.value = true
  nextTick(() => nameInput.value?.focus())
}

function cancelEditing() {
  editingName.value = false
  nameError.value = ''
}

async function saveName() {
  if (!nameValid.value) return
  savingName.value = true
  nameError.value = ''
  try {
    await classroomStore.updateMyDisplayName(nameDraft.value.trim())
    editingName.value = false
    console.log('[ProfileView] Display name saved:', nameDraft.value.trim())
  } catch (err) {
    console.error('[ProfileView] Failed to save display name:', err)
    nameError.value = 'Kunne ikke lagre. Prøv igjen.'
  } finally {
    savingName.value = false
  }
}

onMounted(async () => {
  console.log('[ProfileView] Loading profile data')
  await Promise.allSettled([
    avatarStore.fetchAvatar().catch(err => console.warn('[ProfileView] Avatar load failed:', err)),
    gameStore.fetchProfile().catch(err => console.warn('[ProfileView] Profile stats load failed:', err)),
  ])
})
</script>

<style scoped>
.profile-view {
  min-height: 100vh;
}

.profile-view__body {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--space-8) var(--space-6);
  gap: var(--space-6);
  max-width: 32rem;
  margin: 0 auto;
  box-sizing: border-box;
  width: 100%;
}

/* ── Avatar ── */
.profile-avatar {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-4);
}

.profile-avatar__frame {
  width: 10rem;
  height: 10rem;
  border-radius: var(--radius-full);
  background: var(--color-primary-soft);
  border: 3px solid var(--color-primary);
  display: grid;
  place-items: center;
  overflow: hidden;
  box-shadow: var(--shadow-lg);
  flex-shrink: 0;
}

.profile-avatar__img {
  width: 85%;
  height: 85%;
  object-fit: contain;
}

.profile-avatar__edit-btn {
  display: inline-flex;
  align-items: center;
  white-space: nowrap;
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-md);
  background: var(--color-primary);
  color: var(--color-surface);
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  text-decoration: none;
  transition: background var(--transition-fast);
}
.profile-avatar__edit-btn:hover { background: var(--color-primary-dark); }
.profile-avatar__edit-btn:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}

/* ── Name ── */
.profile-name-section {
  width: 100%;
  display: flex;
  justify-content: center;
}

.profile-name-display {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.profile-view__name {
  margin: 0;
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  color: var(--color-heading);
  text-align: center;
}

.profile-name-display__edit {
  background: none;
  border: none;
  cursor: pointer;
  font-size: var(--text-base);
  padding: var(--space-1);
  border-radius: var(--radius-sm);
  line-height: 1;
  transition: background var(--transition-fast);
}
.profile-name-display__edit:hover { background: var(--color-border); }

.profile-name-edit {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-3);
}

.profile-name-edit__input {
  width: 100%;
  max-width: 18rem;
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-md);
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
  color: var(--color-text);
  background: var(--color-surface);
  text-align: center;
  box-sizing: border-box;
}
.profile-name-edit__input:focus {
  outline: 2px solid var(--color-primary);
  outline-offset: 1px;
}

.profile-name-edit__actions {
  display: flex;
  gap: var(--space-2);
}

.profile-name-edit__save,
.profile-name-edit__cancel {
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  cursor: pointer;
  border: 1px solid transparent;
  transition: background var(--transition-fast);
}

.profile-name-edit__save {
  background: var(--color-primary);
  color: var(--color-surface);
}
.profile-name-edit__save:hover:not(:disabled) { background: var(--color-primary-dark); }
.profile-name-edit__save:disabled { opacity: 0.5; cursor: not-allowed; }

.profile-name-edit__cancel {
  background: var(--color-surface);
  color: var(--color-text);
  border-color: var(--color-border);
}
.profile-name-edit__cancel:hover:not(:disabled) { background: var(--color-bg); }

.profile-name-edit__error {
  color: var(--color-danger);
  font-size: var(--text-sm);
  margin: 0;
}

/* ── Stats ── */
.profile-stats {
  display: flex;
  gap: var(--space-4);
  flex-wrap: wrap;
  justify-content: center;
  width: 100%;
}

.profile-stats__pill {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-1);
  flex: 1;
  min-width: 5rem;
  padding: var(--space-4) var(--space-3);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.profile-stats__icon { font-size: var(--text-xl); }

.profile-stats__value {
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  color: var(--color-primary);
}

.profile-stats__label {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
  font-weight: var(--font-medium);
}

/* ── Info ── */
.profile-info {
  width: 100%;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.profile-info__list { margin: 0; padding: 0; }

.profile-info__row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-4) var(--space-4);
  gap: var(--space-6);
  min-width: 0;
}

.profile-info__row + .profile-info__row {
  border-top: 1px solid var(--color-border);
}

.profile-info__label {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  font-weight: var(--font-medium);
  flex-shrink: 0;
}

.profile-info__value {
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  color: var(--color-text);
  text-align: right;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
