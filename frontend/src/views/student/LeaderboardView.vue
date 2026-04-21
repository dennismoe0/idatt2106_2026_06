<template>
  <CorkBoardPage page-title="Ledertavle" :back-to="{ name: 'Home' }">
    <div class="leaderboard-view">

    <div v-if="loading" class="leaderboard-view__state" aria-live="polite">
      <span class="leaderboard-view__spinner" aria-hidden="true" />
      <p>Laster ledertavle…</p>
    </div>

    <div v-else-if="error" class="leaderboard-view__state leaderboard-view__state--error" role="alert">
      <p>{{ error }}</p>
      <button class="leaderboard-view__retry" @click="load">Prøv igjen</button>
    </div>

    <template v-else>
      <!-- Own classroom section always first -->
      <section class="leaderboard-section leaderboard-section--own" aria-label="Din klasse">
        <h2 class="leaderboard-section__title">
          🏫 {{ ownClassroomName }}
          <span class="leaderboard-section__badge">Din klasse</span>
        </h2>
        <ol class="leaderboard-list" aria-label="Din klasse — ledertavle">
          <li
            v-for="(entry, index) in ownEntries"
            :key="entry.displayName"
            class="leaderboard-entry"
            :class="{
              'leaderboard-entry--gold':   index === 0,
              'leaderboard-entry--silver': index === 1,
              'leaderboard-entry--bronze': index === 2,
              'leaderboard-entry--me':     entry.displayName === myDisplayName,
            }"
          >
            <span class="leaderboard-entry__rank" aria-hidden="true">{{ rankLabel(index) }}</span>
            <span class="leaderboard-entry__name">{{ entry.displayName }}</span>
            <span class="leaderboard-entry__score">
              {{ entry.completedTasks }}
              <span class="leaderboard-entry__score-label"> / {{ entry.totalTasks }}</span>
            </span>
          </li>
          <li v-if="ownEntries.length === 0" class="leaderboard-entry leaderboard-entry--empty">
            Ingen godkjente elever ennå.
          </li>
        </ol>
      </section>

      <!-- Other classrooms in school -->
      <template v-if="otherClassrooms.length > 0">
        <h2 class="leaderboard-view__school-title">Andre klasser på skolen</h2>
        <section
          v-for="group in otherClassrooms"
          :key="group.classroomId"
          class="leaderboard-section"
          :aria-label="group.classroomName"
        >
          <h3 class="leaderboard-section__title">🏫 {{ group.classroomName }}</h3>
          <ol class="leaderboard-list" :aria-label="`${group.classroomName} — ledertavle`">
            <li
              v-for="(entry, index) in group.entries"
              :key="entry.displayName"
              class="leaderboard-entry"
              :class="{
                'leaderboard-entry--gold':   index === 0,
                'leaderboard-entry--silver': index === 1,
                'leaderboard-entry--bronze': index === 2,
              }"
            >
              <span class="leaderboard-entry__rank" aria-hidden="true">{{ rankLabel(index) }}</span>
              <span class="leaderboard-entry__name">{{ entry.displayName }}</span>
              <span class="leaderboard-entry__score">
                {{ entry.completedTasks }}
                <span class="leaderboard-entry__score-label"> / {{ entry.totalTasks }}</span>
              </span>
            </li>
          </ol>
        </section>
      </template>
    </template>
    </div>
  </CorkBoardPage>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import CorkBoardPage from '@/components/common/CorkBoardPage.vue'
import { useGameStore } from '@/stores/game'
import { useClassroomStore } from '@/stores/classroom'

const gameStore = useGameStore()
const classroomStore = useClassroomStore()

const loading = ref(true)
const error = ref(null)

const schoolLeaderboard = computed(() => gameStore.schoolLeaderboard)
const myClassroomId   = computed(() => classroomStore.currentClassroomId)
const myDisplayName   = computed(() => classroomStore.displayName ?? '')

// Entries belonging to the student's own classroom, pre-sorted by score DESC
const ownEntries = computed(() =>
  schoolLeaderboard.value.filter(e => e.classroomId === myClassroomId.value)
)

// The name of the student's own classroom (first own entry, fallback empty)
const ownClassroomName = computed(() =>
  ownEntries.value[0]?.classroomName ?? 'Din klasse'
)

// Group remaining entries by classroom, preserving score order within each group
const otherClassrooms = computed(() => {
  const seen = new Map()
  for (const entry of schoolLeaderboard.value) {
    if (entry.classroomId === myClassroomId.value) continue
    if (!seen.has(entry.classroomId)) {
      seen.set(entry.classroomId, { classroomId: entry.classroomId, classroomName: entry.classroomName, entries: [] })
    }
    seen.get(entry.classroomId).entries.push(entry)
  }
  return [...seen.values()]
})

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
    await gameStore.fetchSchoolLeaderboard(classroomId)
    console.log('[LeaderboardView] Loaded', schoolLeaderboard.value.length, 'entries')
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
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
  max-width: 640px;
}

.leaderboard-view__school-title {
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  color: var(--color-cork-dark);
  margin: var(--space-2) 0 0;
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
  background: var(--color-primary); color: var(--color-text-on-dark, #fff);
  border: none; border-radius: var(--radius-md); cursor: pointer;
  font-size: var(--text-sm); font-weight: var(--font-semibold);
}

/* ── Sections ── */
.leaderboard-section {
  background: var(--color-note-bg);
  border: 1.5px solid var(--color-note-border);
  border-radius: 6px;
  padding: var(--space-4);
  box-shadow: 2px 3px 10px rgba(0,0,0,0.25);
  position: relative;
}
.leaderboard-section--own {
  border-color: var(--color-cork-dark);
  border-width: 2px;
}

.leaderboard-section__title {
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  color: var(--color-heading);
  margin: 0 0 var(--space-4);
  display: flex;
  align-items: center;
  gap: var(--space-2);
}
.leaderboard-section__badge {
  background: var(--color-primary);
  color: var(--color-text-on-dark);
  border-radius: var(--radius-full);
  padding: 2px var(--space-2);
  font-size: var(--text-xs);
  font-weight: var(--font-semibold);
}

/* ── List ── */
.leaderboard-list {
  list-style: none;
  margin: 0; padding: 0;
  display: flex; flex-direction: column; gap: var(--space-2);
}

.leaderboard-entry {
  display: flex; align-items: center; gap: var(--space-4);
  padding: var(--space-3) var(--space-4);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  transition: box-shadow var(--transition-fast);
}
.leaderboard-entry:hover { box-shadow: var(--shadow-sm); }
.leaderboard-entry--empty {
  color: var(--color-text-muted);
  font-style: italic;
  justify-content: center;
}

.leaderboard-entry--gold   { border-color: var(--color-medal-gold-border);   background: var(--color-medal-gold-bg); }
.leaderboard-entry--silver { border-color: var(--color-medal-silver-border); background: var(--color-medal-silver-bg); }
.leaderboard-entry--bronze { border-color: var(--color-medal-bronze-border); background: var(--color-medal-bronze-bg); }
.leaderboard-entry--me     { outline: 2px solid var(--color-primary); outline-offset: 1px; }

.leaderboard-entry__rank {
  font-size: var(--text-lg); font-weight: var(--font-bold);
  min-width: 2.5rem; text-align: center; flex-shrink: 0;
}
.leaderboard-entry__name {
  flex: 1; font-size: var(--text-base); font-weight: var(--font-semibold);
  color: var(--color-heading); min-width: 0;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.leaderboard-entry__score {
  font-size: var(--text-base); font-weight: var(--font-bold);
  color: var(--color-primary); flex-shrink: 0;
}
.leaderboard-entry__score-label {
  font-weight: var(--font-normal); font-size: var(--text-sm);
  color: var(--color-text-muted);
}

@media (max-width: 480px) {
  .leaderboard-view { padding: var(--space-4); }
}
</style>
