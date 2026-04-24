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

        <section class="leaderboard-switcher" aria-label="Velg ledertavlevisning">
          <div class="leaderboard-tabs" role="tablist" aria-label="Velg ledertavlevisning">
            <button
              class="leaderboard-tab"
              :class="{ 'leaderboard-tab--active': activeScope === 'local' }"
              type="button"
              role="tab"
              :aria-label="localScopeAriaLabel"
              :aria-selected="activeScope === 'local'"
              @click="activeScope = 'local'"
            >
              <span class="leaderboard-tab__eyebrow">SAMMENLIGN DEG MED ANDRE PÅ</span>
              <span class="leaderboard-tab__main">{{ ownSchoolButtonLabel }}</span>
            </button>
            <button
              class="leaderboard-tab"
              :class="{ 'leaderboard-tab--active': activeScope === 'global' }"
              type="button"
              role="tab"
              :aria-label="globalScopeAriaLabel"
              :aria-selected="activeScope === 'global'"
              @click="activeScope = 'global'"
            >
              <span class="leaderboard-tab__eyebrow">SAMMENLIGN DEG MED</span>
              <span class="leaderboard-tab__main">RESTEN AV VERDEN</span>
            </button>
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
              <p class="leaderboard-room__school">{{ ownSchoolLabel }}</p>
            </div>
            <p class="leaderboard-room__note">Kun topp fem elever vises her.</p>
          </div>

          <LeaderboardTable
            aria-label="Din klasse — ledertavle"
            :current-avatar="avatarStore.avatar"
            :current-student-id="currentStudentId"
            empty-label="Ingen godkjente elever ennå."
            :entries="comparisonEntries"
          />
        </section>

        <section
          class="leaderboard-school"
          :aria-label="activeSectionTitle"
        >
          <div v-if="activeClassrooms.length > 0" class="leaderboard-school__stack">
            <section
              v-for="group in activeClassrooms"
              :key="`${activeScope}-${group.classroomId}`"
              class="leaderboard-room"
              :aria-label="group.classroomName"
            >
              <div class="leaderboard-room__head">
                <div>
                  <p class="leaderboard-room__kicker">Klasse</p>
                  <h3 class="leaderboard-room__title">{{ group.classroomName }}</h3>
                  <p class="leaderboard-room__school">{{ group.schoolName }}</p>
                </div>
                <p class="leaderboard-room__note">Topp fem elever vises her.</p>
              </div>

              <LeaderboardTable
                :aria-label="`${group.classroomName} — ledertavle`"
                :current-avatar="avatarStore.avatar"
                :current-student-id="currentStudentId"
                empty-label="Ingen elever å vise."
                :entries="group.entries"
                :start-rank="1"
              />
            </section>
          </div>

          <section
            v-else
            class="leaderboard-view__state leaderboard-view__state--compact"
            aria-live="polite"
          >
            <p>{{ activeEmptyLabel }}</p>
          </section>
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
const activeScope = ref('local')

const localLeaderboard = computed(() => gameStore.schoolLeaderboard)
const globalLeaderboard = computed(() => gameStore.globalLeaderboard)
const currentStudentId = computed(() => authStore.userId)
const myClassroomId = computed(() => classroomStore.currentClassroomId)

const ownEntries = computed(() =>
  sortLeaderboardEntries(
    localLeaderboard.value.filter((entry) => entry.classroomId === myClassroomId.value),
  ),
)

const ownClassroomName = computed(() => ownEntries.value[0]?.classroomName ?? 'Din klasse')
const ownSchoolName = computed(() => {
  const localEntry = ownEntries.value[0]
  if (localEntry?.schoolName) return localEntry.schoolName

  const globalEntry = globalLeaderboard.value.find((entry) => entry.classroomId === myClassroomId.value)
  return globalEntry?.schoolName ?? 'Ukjent skole'
})
const ownSchoolLabel = computed(() => ownSchoolName.value || 'Ukjent skole')
const ownSchoolButtonLabel = computed(() => compactSchoolLabel(ownSchoolLabel.value))
const localScopeAriaLabel = computed(() => `Sammenlign deg med andre pa ${ownSchoolLabel.value}`)
const globalScopeAriaLabel = computed(() => 'Sammenlign deg med resten av verden')

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

const localClassrooms = computed(() => {
  return groupLeaderboardEntries(localLeaderboard.value)
})

const globalClassrooms = computed(() => {
  return groupLeaderboardEntries(globalLeaderboard.value)
})

const activeClassrooms = computed(() => {
  return activeScope.value === 'local' ? localClassrooms.value : globalClassrooms.value
})

const activeSectionTitle = computed(() => {
  return activeScope.value === 'local'
    ? 'Andre klasser pa samme skole'
    : 'Klasser fra hele verden'
})

const activeEmptyLabel = computed(() => {
  return activeScope.value === 'local'
    ? 'Ingen andre klasser pa samme skole a vise enda.'
    : 'Ingen andre klasser i verden a vise enda.'
})

function groupLeaderboardEntries(entries) {
  const classrooms = new Map()

  for (const entry of entries) {
    if (entry.classroomId === myClassroomId.value) continue

    if (!classrooms.has(entry.classroomId)) {
      classrooms.set(entry.classroomId, {
        classroomId: entry.classroomId,
        classroomName: entry.classroomName,
        schoolName: entry.schoolName ?? 'Ukjent skole',
        entries: [],
      })
    }

    classrooms.get(entry.classroomId).entries.push(entry)
  }

  return [...classrooms.values()].map((group) => ({
    ...group,
    entries: sortLeaderboardEntries(group.entries).slice(0, 5),
  })).sort((a, b) => {
    const schoolDiff = a.schoolName.localeCompare(b.schoolName, 'nb')
    if (schoolDiff !== 0) return schoolDiff
    return a.classroomName.localeCompare(b.classroomName, 'nb')
  })
}

const classCompletedTasks = computed(() => {
  return ownEntries.value.reduce((total, entry) => total + (Number(entry.completedTasks) || 0), 0)
})

const classCompletedTasksLabel = computed(() => {
  const count = classCompletedTasks.value
  return `${count} ${count === 1 ? 'oppgave' : 'oppgaver'}`
})

function sortLeaderboardEntries(entries) {
  return [...entries].sort((a, b) => {
    const completedDiff = (Number(b.completedTasks) || 0) - (Number(a.completedTasks) || 0)
    if (completedDiff !== 0) return completedDiff

    return String(a.displayName ?? '').localeCompare(String(b.displayName ?? ''), 'nb')
  })
}

function compactSchoolLabel(schoolName) {
  return schoolName
    .replace(/videregående skole/gi, 'VGS')
    .replace(/videregaende skole/gi, 'VGS')
    .trim()
}

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

    await Promise.all([
      gameStore.fetchSchoolLeaderboard(classroomId),
      gameStore.fetchGlobalLeaderboard(classroomId),
    ])
    console.log(
      '[LeaderboardView] Loaded local/global entries',
      localLeaderboard.value.length,
      globalLeaderboard.value.length,
    )
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
}

.leaderboard-hero {
  position: relative;
  overflow: hidden;
  padding: clamp(1.4rem, 4vw, 2.25rem);
  border-radius: var(--radius-2xl);
  color: var(--color-medals-text-on-cork);
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
  color: var(--color-medals-text-on-cork);
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

.leaderboard-switcher {
  width: 100%;
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
  border-radius: var(--radius-2xl);
  color: var(--color-ink);
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
  color: var(--color-wood);
}

.leaderboard-stat__value {
  font-size: clamp(1.05rem, 2vw, 1.35rem);
  color: var(--color-ink);
}

.leaderboard-room {
  display: grid;
  gap: var(--space-4);
  padding: clamp(1rem, 2vw, 1.5rem);
  border-radius: var(--radius-2xl);
  color: var(--color-medals-text-on-cork);
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

.leaderboard-room__school {
  margin: var(--space-2) 0 0;
  font-size: clamp(0.95rem, 1.2vw, 1.05rem);
  line-height: 1.45;
  color: color-mix(in srgb, var(--color-medals-text-on-cork) 82%, white);
}

.leaderboard-room__note {
  margin: 0;
  max-width: 26rem;
  text-align: right;
  font-size: clamp(1.05rem, 1.4vw, 1.15rem);
  line-height: 1.55;
  color: var(--color-medals-text-on-cork);
}

.leaderboard-school {
  display: grid;
  gap: var(--space-4);
  color: var(--color-medals-text-on-cork);
}

.leaderboard-tabs {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-2);
  width: 100%;
}

.leaderboard-tab {
  display: grid;
  align-content: center;
  justify-items: center;
  height: 60px;
  max-height: 60px;
  border: 2px solid color-mix(in srgb, var(--color-medals-card-border) 88%, white);
  border-radius: var(--radius-2xl);
  padding: 0.5rem 1rem 0.55rem;
  background:
    linear-gradient(135deg, rgba(255, 247, 231, 0.96), rgba(244, 228, 198, 0.94)),
    var(--color-medals-card-bg);
  color: var(--color-wood);
  font-weight: var(--font-bold);
  text-align: center;
  text-transform: uppercase;
  cursor: pointer;
  box-shadow:
    0 14px 24px var(--color-medals-card-shadow),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
  transition:
    background 0.18s ease,
    border-color 0.18s ease,
    transform 0.18s ease,
    box-shadow 0.18s ease;
}

.leaderboard-tab__eyebrow {
  display: block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 0.72rem;
  font-weight: 800;
  letter-spacing: 0.12em;
  line-height: 1.05;
}

.leaderboard-tab__main {
  display: block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: clamp(1rem, 1.65vw, 1.16rem);
  font-weight: 900;
  letter-spacing: 0.08em;
  line-height: 1.02;
}

.leaderboard-tab:hover {
  transform: translateY(-1px);
  border-color: color-mix(in srgb, var(--color-gold) 45%, white);
}

.leaderboard-tab--active {
  background:
    radial-gradient(circle at top, rgba(255, 245, 196, 0.98), rgba(255, 245, 196, 0.18) 56%),
    linear-gradient(145deg, rgba(252, 233, 175, 0.98), rgba(219, 168, 73, 0.96));
  border-color: color-mix(in srgb, #c98d28 72%, white);
  color: #4a2b08;
  box-shadow:
    0 18px 28px rgba(59, 31, 8, 0.22),
    inset 0 0 0 1px rgba(255, 247, 220, 0.92);
}

.leaderboard-school__stack {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.leaderboard-view__state--compact {
  min-height: auto;
  padding: 1.2rem;
  border: 1px dashed rgba(255, 240, 211, 0.24);
  border-radius: var(--radius-xl);
  color: var(--color-medals-text-on-cork);
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
    border-radius: var(--radius-2xl);
  }

  .leaderboard-tabs {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
