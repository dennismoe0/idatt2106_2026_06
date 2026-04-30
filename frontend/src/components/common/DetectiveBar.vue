<template>
  <header class="detective-bar">
    <RouterLink
      v-if="backTo"
      :to="backTo"
      class="detective-bar__back"
      aria-label="Gå tilbake"
    >←</RouterLink>
    <span v-else class="detective-bar__spacer" aria-hidden="true" />

    <span class="detective-bar__avatar detective-bar__avatar--fallback" aria-hidden="true">🕵️</span>

    <span class="detective-bar__name">Detektiv {{ displayName }}</span>

    <span class="detective-bar__title" :aria-label="`Siden: ${pageTitle}`">{{ pageTitle }}</span>

    <div class="detective-bar__hud" aria-label="Spillerstatus">
      <span title="XP">⚡{{ xp }}</span>
      <span title="Stjerner">⭐{{ stars }}</span>
    </div>

    <SoundControls />
  </header>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useGameStore } from '@/stores/game'
import { useClassroomStore } from '@/stores/classroom'
import SoundControls from '@/components/common/SoundControls.vue'

const props = defineProps({
  backTo:    { type: [Object, String], default: null },
  pageTitle: { type: String, default: '' }
})

const authStore      = useAuthStore()
const gameStore      = useGameStore()
const classroomStore = useClassroomStore()

function formatDisplayName(email) {
  if (!email) return 'Detektiv'
  return email.endsWith('@student.local')
    ? email.replace('@student.local', '')
    : email.split('@')[0]
}

const displayName = computed(() =>
  classroomStore.displayName || formatDisplayName(authStore.email)
)
const xp    = computed(() => gameStore.xp ?? 0)
const stars = computed(() => gameStore.starBalance ?? 0)

onMounted(async () => {
  try {
    await gameStore.fetchProfile()
  } catch (err) {
    console.warn('[DetectiveBar] Could not fetch profile:', err)
  }
})
</script>

<style scoped>
.detective-bar {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  gap: var(--space-2);
  height: 48px;
  padding: 0 var(--space-4);
  background: linear-gradient(135deg, var(--color-wood) 0%, var(--color-wood-mid) 100%);
  color: var(--color-gold);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.4);
}

.detective-bar__back {
  color: var(--color-gold);
  text-decoration: none;
  font-size: var(--text-lg);
  opacity: 0.8;
  min-width: 44px;
  min-height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
  transition: opacity var(--transition-fast);
}
.detective-bar__back:hover { opacity: 1; }
.detective-bar__back:focus-visible {
  outline: 3px solid var(--color-gold);
  outline-offset: 2px;
}

.detective-bar__spacer { min-width: 44px; }

.detective-bar__avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  border: 2px solid var(--color-gold);
  object-fit: cover;
  flex-shrink: 0;
}
.detective-bar__avatar--fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  background: rgba(255, 255, 255, 0.1);
}

.detective-bar__name {
  font-weight: 700;
  font-size: var(--text-sm);
  letter-spacing: 0.3px;
  white-space: nowrap;
}

.detective-bar__title {
  flex: 1;
  text-align: center;
  font-size: var(--text-xs);
  text-transform: uppercase;
  letter-spacing: 1.5px;
  opacity: 0.95;
}

.detective-bar__hud {
  display: flex;
  gap: var(--space-3);
  font-size: var(--text-sm);
  opacity: 0.75;
  white-space: nowrap;
}
</style>
