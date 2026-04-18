<template>
  <div class="profile-view">
    <StudentHeader title="Min profil" :show-back="true" :back-to="{ name: 'Home' }" />

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

      <!-- Name -->
      <h1 class="profile-view__name">{{ displayName }}</h1>

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
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useAvatarStore } from '@/stores/avatar'
import { useGameStore } from '@/stores/game'
import { useClassroomStore } from '@/stores/classroom'
import { useAuthStore } from '@/stores/auth'
import StudentHeader from '@/components/common/StudentHeader.vue'
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
  background: var(--color-bg);
}

.profile-view__body {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--space-8) var(--space-6);
  gap: var(--space-6);
  max-width: 32rem;
  margin: 0 auto;
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
}

.profile-avatar__img {
  width: 85%;
  height: 85%;
  object-fit: contain;
}

.profile-avatar__edit-btn {
  display: inline-flex;
  align-items: center;
  padding: var(--space-2) var(--space-5);
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
.profile-view__name {
  margin: 0;
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  color: var(--color-heading);
  text-align: center;
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

.profile-info__list {
  margin: 0;
  padding: 0;
}

.profile-info__row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-4) var(--space-5);
  gap: var(--space-4);
}

.profile-info__row + .profile-info__row {
  border-top: 1px solid var(--color-border);
}

.profile-info__label {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  font-weight: var(--font-medium);
}

.profile-info__value {
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  color: var(--color-text);
  text-align: right;
}
</style>
