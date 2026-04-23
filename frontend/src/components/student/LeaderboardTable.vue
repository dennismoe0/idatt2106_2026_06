<template>
  <div class="leaderboard-table-wrap">
    <table class="leaderboard-table" :aria-label="ariaLabel">
      <thead>
        <tr>
          <th scope="col" class="leaderboard-table__rank-col">Plass</th>
          <th scope="col">Elev</th>
          <th scope="col" class="leaderboard-table__progress-col">Fremdrift</th>
        </tr>
      </thead>
      <tbody v-if="entries.length > 0">
        <tr
          v-for="(entry, index) in entries"
          :key="entryKey(entry, index)"
          :class="rowClasses(entry, index)"
        >
          <td class="leaderboard-table__rank-cell">
            <span class="leaderboard-rank">
              <span
                v-if="medalIcon(entryRank(index))"
                class="leaderboard-rank__medal"
                aria-hidden="true"
              >
                {{ medalIcon(entryRank(index)) }}
              </span>
              <span v-if="!medalIcon(entryRank(index))" class="leaderboard-rank__number">
                {{ entryRank(index) }}
              </span>
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
                <span v-if="isCurrentStudent(entry)" class="leaderboard-student__badge">Deg</span>
              </div>
            </div>
          </td>
          <td class="leaderboard-table__progress-cell">
            <div class="leaderboard-progress">
              <span class="leaderboard-progress__pill">
                {{ entry.completedTasks }} / {{ entry.totalTasks }}
              </span>
              <span class="leaderboard-progress__meta">{{ progressPercent(entry) }}%</span>
            </div>
          </td>
        </tr>
      </tbody>
      <tbody v-else>
        <tr>
          <td colspan="3" class="leaderboard-table__empty">{{ emptyLabel }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import AvatarPreview from '@/components/student/AvatarPreview.vue'

const props = defineProps({
  ariaLabel: { type: String, required: true },
  currentAvatar: { type: Object, default: null },
  currentStudentId: { type: Number, default: null },
  emptyLabel: { type: String, default: 'Ingen elever å vise.' },
  entries: { type: Array, default: () => [] },
  startRank: { type: Number, default: 1 },
})

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

function entryRank(index) {
  return props.startRank + index
}

function medalIcon(rank) {
  if (rank === 1) return '🥇'
  if (rank === 2) return '🥈'
  if (rank === 3) return '🥉'
  return ''
}

function isCurrentStudent(entry) {
  return entry.studentId === props.currentStudentId
}

function rowClasses(entry, index) {
  const rank = entryRank(index)

  return {
    'leaderboard-table__row--gold': rank === 1,
    'leaderboard-table__row--silver': rank === 2,
    'leaderboard-table__row--bronze': rank === 3,
    'leaderboard-table__row--me': isCurrentStudent(entry),
  }
}

function progressPercent(entry) {
  const totalTasks = Number(entry.totalTasks) || 0
  if (totalTasks === 0) return 0

  return Math.round(((Number(entry.completedTasks) || 0) / totalTasks) * 100)
}

function avatarSelections(entry) {
  const source = isCurrentStudent(entry) && props.currentAvatar ? props.currentAvatar : entry.avatar
  const merged = { ...DEFAULT_LEADERBOARD_AVATAR }

  for (const [key, value] of Object.entries(source ?? {})) {
    if (value !== null && value !== undefined && value !== '') {
      merged[key] = value
    }
  }

  return merged
}

function entryKey(entry, index, classroomId = entry.classroomId) {
  return `${classroomId}-${entry.studentId ?? index}`
}
</script>

<style scoped>
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
  background: var(--color-medal-gold-bg);
}

.leaderboard-table__row--gold td:first-child {
  box-shadow: inset 5px 0 0 var(--color-medal-gold-border);
}

.leaderboard-table__row--silver td {
  background: var(--color-medal-silver-bg);
}

.leaderboard-table__row--silver td:first-child {
  box-shadow: inset 5px 0 0 var(--color-medal-silver-border);
}

.leaderboard-table__row--bronze td {
  background: var(--color-medal-bronze-bg);
}

.leaderboard-table__row--bronze td:first-child {
  box-shadow: inset 5px 0 0 var(--color-medal-bronze-border);
}

.leaderboard-table__row--me td {
  background: var(--color-leaderboard-me-bg);
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
  width: 100%;
  font-weight: var(--font-bold);
  color: var(--color-ink);
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
  /* AvatarComposer leaves extra space above the figure; offset it so the avatar fills the circle shell better. */
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

@media (max-width: 560px) {
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
