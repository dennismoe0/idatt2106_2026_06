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

          <div class="leaderboard-table-wrap">
            <table class="leaderboard-table" aria-label="Din klasse — ledertavle">
              <thead>
                <tr>
                  <th scope="col" class="leaderboard-table__rank-col">Plass</th>
                  <th scope="col">Elev</th>
                  <th scope="col" class="leaderboard-table__progress-col">Fremdrift</th>
                </tr>
              </thead>
              <tbody v-if="ownEntries.length > 0">
                <tr
                  v-for="(entry, index) in ownEntries"
                  :key="entryKey(entry, index)"
                  :class="rowClasses(entry, index)"
                >
                  <td class="leaderboard-table__rank-cell">
                    <span class="leaderboard-rank">
                      <span
                        v-if="medalIcon(index)"
                        class="leaderboard-rank__medal"
                        aria-hidden="true"
                      >
                        {{ medalIcon(index) }}
                      </span>
                      <span v-if="!medalIcon(index)" class="leaderboard-rank__number">{{
                        index + 1
                      }}</span>
                    </span>
                  </td>
                  <td class="leaderboard-table__student-cell">
                    <div class="leaderboard-student">
                      <div class="leaderboard-avatar-shell" aria-hidden="true">
                        <AvatarPreview
                          :selections="avatarSelections(entry)"
                          :size="48"
                          class="leaderboard-avatar"
                        />
                      </div>
                      <div class="leaderboard-student__text">
                        <span class="leaderboard-student__name">{{ entry.displayName }}</span>
                        <span v-if="isCurrentStudent(entry)" class="leaderboard-student__badge"
                          >Deg</span
                        >
                      </div>
                    </div>
                  </td>
                  <td class="leaderboard-table__progress-cell">
                    <div class="leaderboard-progress">
                      <span class="leaderboard-progress__pill">
                        {{ entry.completedTasks }} / {{ entry.totalTasks }}
                      </span>
                      <span class="leaderboard-progress__meta">
                        {{ progressPercent(entry) }}%
                      </span>
                    </div>
                  </td>
                </tr>
              </tbody>
              <tbody v-else>
                <tr>
                  <td colspan="3" class="leaderboard-table__empty">Ingen godkjente elever ennå.</td>
                </tr>
              </tbody>
            </table>
          </div>
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

              <div class="leaderboard-table-wrap">
                <table
                  class="leaderboard-table"
                  :aria-label="`${group.classroomName} — ledertavle`"
                >
                  <thead>
                    <tr>
                      <th scope="col" class="leaderboard-table__rank-col">Plass</th>
                      <th scope="col">Elev</th>
                      <th scope="col" class="leaderboard-table__progress-col">Fremdrift</th>
                    </tr>
                  </thead>
                  <tbody v-if="group.entries.length > 0">
                    <tr
                      v-for="(entry, index) in group.entries"
                      :key="entryKey(entry, index, group.classroomId)"
                      :class="rowClasses(entry, index)"
                    >
                      <td class="leaderboard-table__rank-cell">
                        <span class="leaderboard-rank">
                          <span
                            v-if="medalIcon(index)"
                            class="leaderboard-rank__medal"
                            aria-hidden="true"
                          >
                            {{ medalIcon(index) }}
                          </span>
                          <span v-if="!medalIcon(index)" class="leaderboard-rank__number">{{
                            index + 1
                          }}</span>
                        </span>
                      </td>
                      <td class="leaderboard-table__student-cell">
                        <div class="leaderboard-student">
                          <div class="leaderboard-avatar-shell" aria-hidden="true">
                            <AvatarPreview
                              :selections="avatarSelections(entry)"
                              :size="48"
                              class="leaderboard-avatar"
                            />
                          </div>
                          <div class="leaderboard-student__text">
                            <span class="leaderboard-student__name">{{ entry.displayName }}</span>
                          </div>
                        </div>
                      </td>
                      <td class="leaderboard-table__progress-cell">
                        <div class="leaderboard-progress">
                          <span class="leaderboard-progress__pill">
                            {{ entry.completedTasks }} / {{ entry.totalTasks }}
                          </span>
                          <span class="leaderboard-progress__meta">
                            {{ progressPercent(entry) }}%
                          </span>
                        </div>
                      </td>
                    </tr>
                  </tbody>
                  <tbody v-else>
                    <tr>
                      <td colspan="3" class="leaderboard-table__empty">Ingen elever å vise.</td>
                    </tr>
                  </tbody>
                </table>
              </div>
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
import AvatarPreview from '@/components/student/AvatarPreview.vue'
import { useAvatarStore } from '@/stores/avatar'
import { useGameStore } from '@/stores/game'
import { useClassroomStore } from '@/stores/classroom'

const gameStore = useGameStore()
const classroomStore = useClassroomStore()
const avatarStore = useAvatarStore()

const loading = ref(true)
const error = ref(null)

const DEFAULT_LEADERBOARD_AVATAR = Object.freeze({
  gender: 'neutral',
  eyeColor: '#4a3000',
  eyeStyle: 'round',
  skinColor: '#D08B5B',
  hairColor: '#8B4513',
  hairStyle: 'short',
  outfit: 'detective-coat',
  outfitColor: '#6B4A2F',
  hatColor: 'none',
  accessory: 'badge',
})

const schoolLeaderboard = computed(() => gameStore.schoolLeaderboard)
const myClassroomId = computed(() => classroomStore.currentClassroomId)
const myDisplayName = computed(() => classroomStore.displayName ?? '')

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

function medalIcon(index) {
  if (index === 0) return '🥇'
  if (index === 1) return '🥈'
  if (index === 2) return '🥉'
  return ''
}

function rowClasses(entry, index) {
  return {
    'leaderboard-table__row--gold': index === 0,
    'leaderboard-table__row--silver': index === 1,
    'leaderboard-table__row--bronze': index === 2,
    'leaderboard-table__row--me': isCurrentStudent(entry),
  }
}

function isCurrentStudent(entry) {
  return entry.displayName === myDisplayName.value
}

function progressPercent(entry) {
  const totalTasks = Number(entry.totalTasks) || 0
  if (totalTasks === 0) return 0

  return Math.round(((Number(entry.completedTasks) || 0) / totalTasks) * 100)
}

function entryCountLabel(count) {
  return `${count} ${count === 1 ? 'elev' : 'elever'}`
}

function avatarSelections(entry) {
  const source = isCurrentStudent(entry) && avatarStore.avatar ? avatarStore.avatar : entry.avatar
  const merged = { ...DEFAULT_LEADERBOARD_AVATAR }

  for (const [key, value] of Object.entries(source ?? {})) {
    if (value !== null && value !== undefined && value !== '') {
      merged[key] = value
    }
  }

  return merged
}

function entryKey(entry, index, classroomId = entry.classroomId) {
  return `${classroomId}-${entry.displayName}-${index}`
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

.leaderboard-table-wrap {
  min-width: 0;
  overflow-x: auto;
  border: 1px solid rgba(122, 78, 26, 0.16);
  border-radius: calc(var(--radius-xl) - 2px);
  background: color-mix(in srgb, var(--color-note-bg) 94%, white);
}

.leaderboard-table {
  width: 100%;
  min-width: 0;
  border-collapse: separate;
  border-spacing: 0;
  table-layout: fixed;
}

.leaderboard-table th,
.leaderboard-table td {
  padding: 1.1rem 1rem;
  text-align: left;
  vertical-align: middle;
}

.leaderboard-table thead th {
  background: color-mix(in srgb, var(--color-cork-light) 28%, white);
  color: var(--color-ink);
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
  border-bottom: 1px solid rgba(59, 31, 8, 0.18);
}

.leaderboard-table thead th:first-child {
  border-top-left-radius: calc(var(--radius-xl) - 3px);
}

.leaderboard-table thead th:last-child {
  border-top-right-radius: calc(var(--radius-xl) - 3px);
}

.leaderboard-table__rank-col {
  width: 4.2rem;
}

.leaderboard-table__progress-col {
  width: 9.5rem;
}

.leaderboard-table tbody td {
  color: var(--color-wood);
  border-bottom: 1px solid rgba(59, 31, 8, 0.12);
}

.leaderboard-table th + th,
.leaderboard-table td + td {
  border-left: 1px solid rgba(59, 31, 8, 0.08);
}

.leaderboard-table tbody tr:last-child td {
  border-bottom: none;
}

.leaderboard-table
  tbody
  tr:nth-child(odd):not(.leaderboard-table__row--gold):not(.leaderboard-table__row--silver):not(
    .leaderboard-table__row--bronze
  ):not(.leaderboard-table__row--me)
  td {
  background: color-mix(in srgb, var(--color-surface) 92%, var(--color-note-bg));
}

.leaderboard-table
  tbody
  tr:nth-child(even):not(.leaderboard-table__row--gold):not(.leaderboard-table__row--silver):not(
    .leaderboard-table__row--bronze
  ):not(.leaderboard-table__row--me)
  td {
  background: color-mix(in srgb, var(--color-note-bg) 88%, white);
}

.leaderboard-table__row--gold td {
  background: #fff1bf;
}

.leaderboard-table__row--gold td:first-child {
  box-shadow: inset 5px 0 0 var(--color-medal-gold-border);
}

.leaderboard-table__row--silver td {
  background: #edf1f5;
}

.leaderboard-table__row--silver td:first-child {
  box-shadow: inset 5px 0 0 var(--color-medal-silver-border);
}

.leaderboard-table__row--bronze td {
  background: #f3dfcc;
}

.leaderboard-table__row--bronze td:first-child {
  box-shadow: inset 5px 0 0 var(--color-medal-bronze-border);
}

.leaderboard-table__row--me td {
  background: #fff7df;
}

.leaderboard-table__row--me td:first-child {
  box-shadow: inset 5px 0 0 var(--color-accent);
}

.leaderboard-table__rank-cell {
  white-space: nowrap;
  font-variant-numeric: tabular-nums;
}

.leaderboard-rank {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.45rem;
  font-weight: var(--font-bold);
  color: var(--color-ink);
  width: 100%;
}

.leaderboard-rank__medal {
  font-size: 3rem;
  line-height: 1;
}

.leaderboard-rank__number {
  min-width: 1.25rem;
  font-size: 1.4rem;
}

.leaderboard-table__student-cell,
.leaderboard-table__progress-cell {
  min-width: 0;
}

.leaderboard-student {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  min-width: 0;
}

.leaderboard-avatar-shell {
  width: 4.35rem;
  height: 4.35rem;
  flex-shrink: 0;
  display: grid;
  place-items: end center;
  overflow: hidden;
  border-radius: var(--radius-full);
  background: color-mix(in srgb, var(--color-surface) 85%, var(--color-note-bg));
  border: 2px solid rgba(122, 78, 26, 0.28);
  box-shadow: 0 2px 6px rgba(59, 31, 8, 0.12);
}

.leaderboard-avatar {
  display: block;
  transform: translateY(0.72rem) scale(1.18);
}

.leaderboard-student__text {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
  min-width: 0;
}

.leaderboard-student__name {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--color-ink);
  font-weight: var(--font-bold);
  font-size: 1.28rem;
  line-height: 1.25;
}

.leaderboard-student__badge {
  display: inline-flex;
  align-self: flex-start;
  padding: 0.15rem 0.5rem;
  border-radius: var(--radius-full);
  background: color-mix(in srgb, var(--color-accent) 22%, white);
  color: var(--color-wood);
  font-size: 0.72rem;
  font-weight: var(--font-semibold);
}

.leaderboard-progress {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 0.25rem;
  font-variant-numeric: tabular-nums;
}

.leaderboard-progress__pill {
  display: inline-flex;
  justify-content: center;
  min-width: 4.8rem;
  padding: 0.38rem 0.8rem;
  border-radius: var(--radius-full);
  background: color-mix(in srgb, var(--color-gold) 22%, white);
  color: var(--color-wood);
  border: 1px solid rgba(122, 78, 26, 0.18);
  font-weight: var(--font-bold);
  font-size: 1.05rem;
  white-space: nowrap;
}

.leaderboard-progress__meta {
  color: var(--color-ink-subtle);
  font-size: var(--text-sm);
}

.leaderboard-table__empty {
  text-align: center;
  color: var(--color-ink-faint);
  font-style: italic;
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

  .leaderboard-table th,
  .leaderboard-table td {
    padding: 0.75rem 0.65rem;
  }

  .leaderboard-table__rank-col {
    width: 3.6rem;
  }

  .leaderboard-table__progress-col {
    width: 7.5rem;
  }

  .leaderboard-avatar {
    transform: translateY(0.38rem) scale(1);
  }

  .leaderboard-avatar-shell {
    width: 3.4rem;
    height: 3.4rem;
  }

  .leaderboard-rank__medal {
    font-size: 2.1rem;
  }

  .leaderboard-student__name {
    font-size: 1.08rem;
  }

  .leaderboard-rank__number {
    font-size: 1.15rem;
  }

  .leaderboard-progress__meta {
    display: none;
  }
}
</style>
