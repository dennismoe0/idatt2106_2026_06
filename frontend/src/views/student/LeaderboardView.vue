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
        <section class="leaderboard-intro" aria-label="Om ledertavlen">
          <p class="leaderboard-intro__eyebrow">Klasserom</p>
          <h2 class="leaderboard-intro__title">Følg fremdriften i din klasse</h2>
          <p class="leaderboard-intro__text">
            Her ser du hvor mange oppgaver hver elev har løst. Din rad er markert, så den er lett å
            finne.
          </p>
        </section>

        <section class="leaderboard-section leaderboard-section--own" aria-label="Din klasse">
          <header class="leaderboard-section__header">
            <div>
              <h2 class="leaderboard-section__title">
                <span aria-hidden="true">🏫</span>
                {{ ownClassroomName }}
              </h2>
              <p class="leaderboard-section__meta">{{ entryCountLabel(ownEntries.length) }}</p>
            </div>
            <span class="leaderboard-section__badge">Din klasse</span>
          </header>

          <LeaderboardTable
            aria-label="Din klasse — ledertavle"
            :current-avatar="avatarStore.avatar"
            :current-student-id="currentStudentId"
            empty-label="Ingen godkjente elever ennå."
            :entries="ownEntries"
          />
        </section>

        <section
          v-if="otherClassrooms.length > 0"
          class="leaderboard-group"
          aria-label="Andre klasser"
        >
          <header class="leaderboard-group__header">
            <h2 class="leaderboard-view__school-title">Andre klasser på skolen</h2>
          </header>

          <div class="leaderboard-group__grid">
            <section
              v-for="group in otherClassrooms"
              :key="group.classroomId"
              class="leaderboard-section"
              :aria-label="group.classroomName"
            >
              <header class="leaderboard-section__header">
                <div>
                  <h3 class="leaderboard-section__title">
                    <span aria-hidden="true">🏫</span>
                    {{ group.classroomName }}
                  </h3>
                  <p class="leaderboard-section__meta">
                    {{ entryCountLabel(group.entries.length) }}
                  </p>
                </div>
              </header>

              <LeaderboardTable
                :aria-label="`${group.classroomName} — ledertavle`"
                :current-avatar="avatarStore.avatar"
                :current-student-id="currentStudentId"
                empty-label="Ingen elever å vise."
                :entries="group.entries"
              />
            </section>
          </div>
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

const otherClassrooms = computed(() => {
  const classrooms = new Map()

  for (const entry of schoolLeaderboard.value) {
    if (entry.classroomId === myClassroomId.value) continue

    if (!classrooms.has(entry.classroomId)) {
      classrooms.set(entry.classroomId, {
        classroomId: entry.classroomId,
        classroomName: entry.classroomName,
        entries: [],
      })
    }

    classrooms.get(entry.classroomId).entries.push(entry)
  }

  return [...classrooms.values()]
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

function entryCountLabel(count) {
  return `${count} ${count === 1 ? 'elev' : 'elever'}`
}

onMounted(load)
</script>

<style scoped>
.leaderboard-view {
  display: grid;
  gap: var(--space-6);
  width: min(100%, 72rem);
  margin: 0 auto;
}

.leaderboard-intro {
  position: relative;
  overflow: hidden;
  padding: clamp(var(--space-4), 3vw, var(--space-6));
  border-radius: var(--radius-xl);
  border: 1px solid color-mix(in srgb, var(--color-note-border) 82%, white);
  background:
    radial-gradient(
      circle at top right,
      color-mix(in srgb, var(--color-gold) 36%, transparent),
      transparent 34%
    ),
    linear-gradient(
      180deg,
      color-mix(in srgb, var(--color-note-bg) 96%, white),
      color-mix(in srgb, var(--color-gold) 10%, var(--color-note-bg))
    ),
    repeating-linear-gradient(0deg, rgba(122, 78, 26, 0.04) 0 1px, transparent 1px 26px);
  box-shadow: 0 8px 18px rgba(59, 31, 8, 0.12);
}

.leaderboard-intro__eyebrow {
  margin: 0 0 var(--space-2);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--color-red-pin);
}

.leaderboard-intro__title {
  margin: 0;
  font-size: clamp(1.35rem, 2vw, 1.75rem);
  color: var(--color-ink);
}

.leaderboard-intro__text {
  margin: var(--space-2) 0 0;
  max-width: 44rem;
  line-height: 1.55;
  color: var(--color-ink-subtle);
}

.leaderboard-view__school-title {
  margin: 0;
  font-size: clamp(1.15rem, 2vw, 1.4rem);
  font-weight: var(--font-bold);
  color: var(--color-ink);
}

.leaderboard-group {
  display: grid;
  gap: var(--space-4);
}

.leaderboard-group__header {
  display: grid;
  gap: var(--space-1);
}

.leaderboard-group__grid {
  display: grid;
  gap: var(--space-4);
  grid-template-columns: repeat(auto-fit, minmax(min(100%, 22rem), 1fr));
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
  width: 32px;
  height: 32px;
  border: 3px solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.leaderboard-view__retry {
  padding: var(--space-2) var(--space-4);
  background: var(--color-cork-dark);
  color: var(--color-text-on-dark, #fff);
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  box-shadow: 0 6px 14px rgba(59, 31, 8, 0.14);
}

.leaderboard-section {
  display: grid;
  gap: var(--space-4);
  min-width: 0;
  padding: clamp(var(--space-3), 2.5vw, var(--space-6));
  border: 1.5px solid var(--color-note-border);
  border-radius: var(--radius-xl);
  background:
    radial-gradient(circle at top left, rgba(255, 255, 255, 0.55), transparent 30%),
    linear-gradient(
      180deg,
      color-mix(in srgb, var(--color-note-bg) 96%, white),
      color-mix(in srgb, var(--color-gold) 10%, var(--color-note-bg))
    ),
    var(--color-note-bg);
  box-shadow: 0 10px 22px rgba(59, 31, 8, 0.12);
}

.leaderboard-section--own {
  border-color: color-mix(in srgb, var(--color-gold) 58%, var(--color-note-border));
  background:
    radial-gradient(
      circle at top right,
      color-mix(in srgb, var(--color-gold) 34%, transparent),
      transparent 28%
    ),
    linear-gradient(
      180deg,
      color-mix(in srgb, var(--color-note-bg) 94%, white),
      color-mix(in srgb, var(--color-gold) 18%, var(--color-note-bg))
    ),
    var(--color-note-bg);
}

.leaderboard-section__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-3);
}

.leaderboard-section__title {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin: 0;
  font-size: clamp(1.05rem, 1.5vw, 1.2rem);
  font-weight: var(--font-bold);
  color: var(--color-ink);
}

.leaderboard-section__title span[aria-hidden='true'] {
  font-size: 4rem;
  line-height: 1;
  flex-shrink: 0;
}

.leaderboard-section__meta {
  margin: var(--space-1) 0 0;
  color: var(--color-ink-faint);
  font-size: var(--text-sm);
}

.leaderboard-section__badge {
  flex-shrink: 0;
  padding: 0.4rem 0.75rem;
  border-radius: var(--radius-full);
  background: linear-gradient(
    180deg,
    color-mix(in srgb, var(--color-red-pin) 82%, white),
    var(--color-red-pin)
  );
  color: var(--color-text-on-dark);
  font-size: var(--text-xs);
  font-weight: var(--font-semibold);
  box-shadow: 0 4px 10px rgba(59, 31, 8, 0.18);
}

@media (max-width: 720px) {
  .leaderboard-group__grid {
    grid-template-columns: 1fr;
  }

  .leaderboard-section__header {
    flex-direction: column;
    align-items: flex-start;
  }

  .leaderboard-section__badge {
    align-self: flex-start;
  }

  .leaderboard-section__title span[aria-hidden='true'] {
    font-size: 2.8rem;
  }
}

@media (max-width: 560px) {
  .leaderboard-view {
    gap: var(--space-4);
  }
}
</style>
