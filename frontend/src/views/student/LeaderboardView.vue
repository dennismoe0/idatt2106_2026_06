<template>
  <main class="leaderboard-view">
    <header class="leaderboard-view__header">
      <RouterLink :to="{ name: 'Home' }" class="leaderboard-view__back" aria-label="Tilbake til hjem">
        ← Tilbake
      </RouterLink>
      <h1 class="leaderboard-view__title">Ledertavle</h1>
    </header>

    <div v-if="loading" class="leaderboard-view__state" aria-live="polite">
      <span class="leaderboard-view__spinner" aria-hidden="true" />
      <p>Laster ledertavle…</p>
    </div>

    <div v-else-if="error" class="leaderboard-view__state leaderboard-view__state--error" role="alert">
      <p>{{ error }}</p>
      <button class="leaderboard-view__retry" @click="load">Prøv igjen</button>
    </div>

    <ol v-else class="leaderboard-list" aria-label="Ledertavle — topp 5">
      <li
        v-for="(entry, index) in top5"
        :key="entry.displayName"
        class="leaderboard-entry"
        :class="{
          'leaderboard-entry--gold':   index === 0,
          'leaderboard-entry--silver': index === 1,
          'leaderboard-entry--bronze': index === 2,
          'leaderboard-entry--me':     entry.displayName === myDisplayName,
        }"
      >
        <span class="leaderboard-entry__rank" aria-hidden="true">
          {{ rankLabel(index) }}
        </span>
        <span class="leaderboard-entry__name">{{ entry.displayName }}</span>
        <span class="leaderboard-entry__score">
          {{ entry.completedTasks }} / {{ entry.totalTasks }}
          <span class="leaderboard-entry__score-label"> stopp</span>
        </span>
      </li>
    </ol>
  </main>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useGameStore } from '@/stores/game'
import { useClassroomStore } from '@/stores/classroom'

const gameStore = useGameStore()
const classroomStore = useClassroomStore()

const loading = ref(true)
const error = ref(null)

const top5 = computed(() => gameStore.leaderboard.slice(0, 5))
const myDisplayName = computed(() => classroomStore.displayName ?? '')

async function load() {
  loading.value = true
  error.value = null
  try {
    const classroomId = classroomStore.currentClassroomId
    if (!classroomId) {
      error.value = 'Ingen aktiv klasse funnet. Logg inn igjen.'
      console.warn('[LeaderboardView] No classroomId in store')
      return
    }
    await gameStore.fetchLeaderboard(classroomId)
    console.log('[LeaderboardView] Loaded', top5.value.length, 'entries (top 5)')
  } catch (err) {
    console.error('[LeaderboardView] Failed to load leaderboard:', err)
    error.value = 'Kunne ikke laste ledertavlen. Prøv igjen.'
  } finally {
    loading.value = false
  }
}

function rankLabel(index) {
  if (index === 0) return '🥇'
  if (index === 1) return '🥈'
  if (index === 2) return '🥉'
  return `${index + 1}.`
}

onMounted(load)
</script>

<style scoped>
.leaderboard-view {
  min-height: 100vh;
  padding: var(--space-6);
  background: var(--color-bg);
}

.leaderboard-view__header { margin-bottom: var(--space-8); }

.leaderboard-view__back {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-sm);
  color: var(--color-primary);
  text-decoration: none;
  margin-bottom: var(--space-4);
}
.leaderboard-view__back:hover { text-decoration: underline; }

.leaderboard-view__title {
  margin: 0;
  font-size: var(--text-3xl);
  color: var(--color-heading);
}

.leaderboard-view__state {
  display: flex; flex-direction: column; align-items: center;
  justify-content: center; gap: var(--space-3); min-height: 16rem;
  text-align: center; color: var(--color-text-muted);
}
.leaderboard-view__state--error { color: var(--color-danger); }
.leaderboard-view__spinner {
  width: 32px; height: 32px;
  border: 3px solid var(--color-border); border-top-color: var(--color-primary);
  border-radius: 50%; animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
.leaderboard-view__retry {
  padding: var(--space-2) var(--space-4);
  background: var(--color-primary); color: var(--color-text-on-dark);
  border: none; border-radius: var(--radius-md); cursor: pointer;
  font-size: var(--text-sm); font-weight: var(--font-semibold);
}
.leaderboard-view__retry:hover { background: var(--color-primary-dark); }

.leaderboard-list {
  list-style: none;
  margin: 0; padding: 0;
  display: flex; flex-direction: column; gap: var(--space-2);
  max-width: 600px;
}

.leaderboard-entry {
  display: flex; align-items: center; gap: var(--space-4);
  padding: var(--space-4) var(--space-5);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  transition: box-shadow var(--transition-fast);
}
.leaderboard-entry:hover { box-shadow: var(--shadow-md); }

.leaderboard-entry--gold   { border-color: #f59e0b; background: #fffbeb; }
.leaderboard-entry--silver { border-color: #9ca3af; background: #f9fafb; }
.leaderboard-entry--bronze { border-color: #b45309; background: #fef3c7; }
.leaderboard-entry--me     { outline: 2px solid var(--color-primary); outline-offset: 1px; }

.leaderboard-entry__rank {
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  min-width: 2.5rem;
  text-align: center;
  flex-shrink: 0;
}

.leaderboard-entry__name {
  flex: 1;
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
  color: var(--color-heading);
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.leaderboard-entry__score {
  font-size: var(--text-base);
  font-weight: var(--font-bold);
  color: var(--color-primary);
  flex-shrink: 0;
}
.leaderboard-entry__score-label {
  font-weight: var(--font-normal);
  font-size: var(--text-sm);
  color: var(--color-text-muted);
}

@media (max-width: 480px) {
  .leaderboard-view { padding: var(--space-4); }
}
</style>
