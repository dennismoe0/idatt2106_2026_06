<template>
  <CorkBoardPage page-title="Ledertavle" :back-to="{ name: 'Home' }">
    <div class="leaderboard-view">
      <section v-if="loading" class="leaderboard-view__state" aria-live="polite">
        <span class="leaderboard-view__spinner" aria-hidden="true" />
        <p>Laster ledertavle…</p>
      </section>

      <section
        v-else-if="error"
        class="leaderboard-view__state leaderboard-view__state--error"
        role="alert"
      >
        <p>{{ error }}</p>
        <button class="leaderboard-view__retry" @click="load">Prøv igjen</button>
      </section>

      <template v-else>
        <section class="leaderboard-hero" aria-label="Om ledertavlen">
          <div class="leaderboard-hero__copy">
            <p class="leaderboard-hero__eyebrow">Nettdetektivene · Klassekamp</p>
            <h2 class="leaderboard-hero__title">Sammenlign deg selv med klassen</h2>
            <p class="leaderboard-hero__lead">
              Vi viser fem detektiver om gangen, med din plass markert i tabellen dersom du er blant top 5 i klassen. 
            </p>
          </div>
        </section>

        <section class="leaderboard-stats" aria-label="Din plassering">
          <article class="leaderboard-stat">
            <div>
              <p class="leaderboard-stat__label">Klasse</p>
              <strong class="leaderboard-stat__value">{{ ownClassroomName }}</strong>
            </div>
          </article>

          <article class="leaderboard-stat">
            <div>
              <p class="leaderboard-stat__label">Din plassering</p>
              <strong class="leaderboard-stat__value">{{ currentRankLabel }}</strong>
            </div>
          </article>

          <article class="leaderboard-stat">
            <div>
              <p class="leaderboard-stat__label">Antall oppgaver klassen har løst</p>
              <strong class="leaderboard-stat__value">{{ classCompletedTasksLabel }}</strong>
            </div>
          </article>
        </section>

        <section class="leaderboard-room" aria-label="Din klasse">
          <div class="leaderboard-room__head">
            <div>
              <p class="leaderboard-room__kicker">Ledertavle</p>
              <h2 class="leaderboard-room__title">{{ ownClassroomName }}</h2>
            </div>
            <p class="leaderboard-room__note">Kun topp fem elever vises her.</p>
          </div>

          <LeaderboardTable
            aria-label="Din klasse — ledertavle"
            :current-avatar="avatarStore.avatar"
            :current-student-id="currentStudentId"
            empty-label="Ingen godkjente elever ennå."
            :entries="comparisonEntries"
            :start-rank="comparisonStartRank"
          />
        </section>
      </template>
    </div>
  </CorkBoardPage>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import CorkBoardPage from '@/components/common/CorkBoardPage.vue'
import LeaderboardTable from '@/components/student/LeaderboardTable.vue'
import { useAvatarStore } from '@/stores/avatar'
import { useAuthStore } from '@/stores/auth'
import { useGameStore } from '@/stores/game'
import { useClassroomStore } from '@/stores/classroom'

const authStore = useAuthStore()
const gameStore = useGameStore()
const classroomStore = useClassroomStore()
const avatarStore = useAvatarStore()

const loading = ref(true)
const error = ref(null)

const schoolLeaderboard = computed(() => gameStore.schoolLeaderboard)
const currentStudentId = computed(() => authStore.userId)
const myClassroomId = computed(() => classroomStore.currentClassroomId)

const ownEntries = computed(() =>
  schoolLeaderboard.value.filter((entry) => entry.classroomId === myClassroomId.value),
)

const ownClassroomName = computed(() => ownEntries.value[0]?.classroomName ?? 'Din klasse')

const currentRank = computed(() => {
  const index = ownEntries.value.findIndex((entry) => entry.studentId === currentStudentId.value)
  return index >= 0 ? index + 1 : null
})

const currentRankLabel = computed(() => {
  if (!currentRank.value) return 'Ikke på listen'
  return `#${currentRank.value}`
})

const comparisonEntries = computed(() => {
  return ownEntries.value.slice(0, 5)
})

const comparisonStartRank = computed(() => 1)

const classCompletedTasks = computed(() => {
  return ownEntries.value.reduce((total, entry) => total + (Number(entry.completedTasks) || 0), 0)
})

const classCompletedTasksLabel = computed(() => {
  const count = classCompletedTasks.value
  return `${count} ${count === 1 ? 'oppgave' : 'oppgaver'}`
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

onMounted(load)
</script>

<style scoped>
.leaderboard-view {
  display: flex;
  flex-direction: column;
  gap: clamp(var(--space-4), 2vw, var(--space-8));
  width: min(100%, 1160px);
  margin: 0 auto;
  color: var(--color-medals-text-on-cork);
}

.leaderboard-hero {
  position: relative;
  overflow: hidden;
  padding: clamp(1.4rem, 4vw, 2.25rem);
  border-radius: 28px;
  background:
    radial-gradient(circle at top left, var(--color-medals-hero-glow), transparent 40%),
    radial-gradient(circle at 80% 20%, var(--color-medals-hero-glow-soft), transparent 28%),
    var(--color-medals-hero-panel);
  border: 3px solid var(--color-medals-hero-border);
  box-shadow:
    inset 0 0 0 2px var(--color-medals-hero-shadow-inner),
    0 18px 30px var(--color-medals-hero-shadow);
}

.leaderboard-hero::after {
  content: '';
  position: absolute;
  inset: 0;
  background:
    linear-gradient(90deg, transparent 0, var(--color-medals-hero-overlay) 20%, transparent 36%),
    repeating-linear-gradient(
      90deg,
      var(--color-medals-hero-overlay-soft),
      var(--color-medals-hero-overlay-soft) 10px,
      transparent 10px,
      transparent 22px
    );
  pointer-events: none;
}

.leaderboard-hero__copy {
  position: relative;
  z-index: 1;
}

.leaderboard-hero__eyebrow,
.leaderboard-room__kicker {
  margin: 0 0 var(--space-2);
  font-size: 0.75rem;
  font-weight: 800;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--color-medals-hero-kicker);
}

.leaderboard-hero__title {
  margin: 0;
  font-size: clamp(1.8rem, 4vw, 2.8rem);
  line-height: 1.05;
  color: var(--color-medals-hero-title);
}

.leaderboard-hero__lead {
  margin: var(--space-4) 0 0;
  max-width: 46rem;
  font-size: clamp(1.1rem, 1.6vw, 1.22rem);
  line-height: 1.65;
  color: var(--color-medals-hero-body);
}

.leaderboard-view__state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-3);
  min-height: 16rem;
  text-align: center;
  color: var(--color-text-muted);
}

.leaderboard-view__state--error {
  color: var(--color-danger);
}

.leaderboard-view__spinner {
  width: 36px;
  height: 36px;
  border: 3px solid var(--color-medals-state-spinner);
  border-top-color: var(--color-medals-text-on-cork);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.leaderboard-view__retry {
  border: none;
  border-radius: 999px;
  padding: 0.75rem 1.25rem;
  background: var(--color-medals-button-bg);
  color: var(--color-medals-button-text);
  cursor: pointer;
  font-weight: 800;
  box-shadow: 0 8px 16px var(--color-medals-button-shadow);
}

.leaderboard-stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--space-4);
}

.leaderboard-stat,
.leaderboard-room {
  position: relative;
  overflow: hidden;
  background: var(--color-medals-card-bg);
  border: 2px solid var(--color-medals-card-border);
  box-shadow: 0 14px 24px var(--color-medals-card-shadow);
}

.leaderboard-stat {
  display: flex;
  align-items: center;
  padding: 1.25rem 1.35rem;
  border-radius: 24px;
  color: var(--color-medals-card-text);
}

.leaderboard-stat::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(130deg, var(--color-medals-card-gloss), transparent 55%);
  pointer-events: none;
}

.leaderboard-stat__label {
  margin: 0 0 0.25rem;
  font-size: clamp(0.95rem, 1.2vw, 1.05rem);
  font-weight: 800;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: var(--color-medals-card-label);
}

.leaderboard-stat__value {
  font-size: clamp(1.05rem, 2vw, 1.35rem);
  color: var(--color-medals-card-title);
}

.leaderboard-room {
  display: grid;
  gap: var(--space-4);
  padding: clamp(1rem, 2vw, 1.5rem);
  border-radius: 30px;
  background: var(--color-medals-room-bg);
  border-color: var(--color-medals-room-border);
  box-shadow: inset 0 0 0 1px var(--color-medals-room-inner-border);
}

.leaderboard-room__head {
  display: flex;
  justify-content: space-between;
  align-items: end;
  gap: var(--space-4);
}

.leaderboard-room__title {
  margin: 0;
  font-size: clamp(1.4rem, 3vw, 2rem);
  color: var(--color-medals-text-on-cork);
}

.leaderboard-room__note {
  margin: 0;
  max-width: 26rem;
  text-align: right;
  font-size: clamp(1.05rem, 1.4vw, 1.15rem);
  line-height: 1.55;
  color: var(--color-medals-room-note);
}

@media (max-width: 900px) {
  .leaderboard-stats {
    grid-template-columns: 1fr;
  }

  .leaderboard-room__head {
    flex-direction: column;
    align-items: flex-start;
  }

  .leaderboard-room__note {
    text-align: left;
  }
}

@media (max-width: 560px) {
  .leaderboard-view {
    gap: var(--space-4);
  }

  .leaderboard-hero,
  .leaderboard-room {
    border-radius: 22px;
  }

}
</style>
