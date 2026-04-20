<template>
  <div class="school-overview">
    <div class="school-header">
      <div class="school-info">
        <div class="school-label">Din skole</div>
        <div class="school-name">{{ school.name }}</div>
      </div>
      <div class="school-code">
        <span class="code-label">Invitasjonskode</span>
        <span class="code-value">{{ school.joinCode }}</span>
        <button class="copy-btn" @click="$emit('copy-code', school.joinCode)">
          📋 Kopier
        </button>
      </div>
    </div>

    <div v-if="classrooms.length === 0" class="empty-state">
      <p>Ingen klasser er koblet til skolen ennå. Opprett en klasse for å komme i gang.</p>
    </div>

    <div v-else class="classrooms-grid">
      <div
        v-for="classroom in classrooms"
        :key="classroom.classroomId"
        class="classroom-card"
      >
        <div class="card-top">
          <div class="card-name">{{ classroom.name }}</div>
          <div class="student-count">{{ classroom.studentCount }} elev{{ classroom.studentCount !== 1 ? 'er' : '' }}</div>
        </div>
        <div v-if="classroom.description" class="card-desc">{{ classroom.description }}</div>

        <div class="leaderboard">
          <div class="leaderboard-title">Topp 5</div>
          <div v-if="classroom.top5.length === 0" class="empty-lb">Ingen elever ennå</div>
          <ol v-else class="lb-list">
            <li
              v-for="(entry, i) in classroom.top5"
              :key="i"
              class="lb-entry"
            >
              <span class="lb-rank">{{ i + 1 }}.</span>
              <span class="lb-name">{{ entry.displayName }}</span>
              <span class="lb-score">{{ entry.completedTasks }}/{{ entry.totalTasks }}</span>
            </li>
          </ol>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  school: { type: Object, required: true },
  classrooms: { type: Array, required: true }
})

defineEmits(['copy-code'])
</script>

<style scoped>
.school-overview { display: flex; flex-direction: column; gap: var(--space-6); }

.school-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--color-primary);
  color: #fff;
  border-radius: var(--radius-xl);
  padding: var(--space-4) var(--space-6);
  gap: var(--space-4);
  flex-wrap: wrap;
}

.school-label { font-size: var(--text-xs); opacity: 0.75; font-weight: var(--font-semibold); }
.school-name  { font-size: var(--text-xl); font-weight: var(--font-bold); }

.school-code {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  background: rgba(255,255,255,0.15);
  border-radius: var(--radius-lg);
  padding: var(--space-2) var(--space-4);
}
.code-label { font-size: var(--text-xs); opacity: 0.75; }
.code-value {
  font-family: monospace;
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  letter-spacing: 1.5px;
}
.copy-btn {
  background: rgba(255,255,255,0.25);
  color: #fff;
  border: none;
  border-radius: var(--radius-md);
  padding: var(--space-1) var(--space-3);
  font-size: var(--text-xs);
  font-weight: var(--font-semibold);
  cursor: pointer;
  font-family: inherit;
}
.copy-btn:hover { background: rgba(255,255,255,0.35); }

.empty-state {
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  padding: var(--space-6);
  text-align: center;
  color: var(--color-text-muted);
  font-size: var(--text-sm);
}

.classrooms-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--space-4);
}

.classroom-card {
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  padding: var(--space-4);
  box-shadow: var(--shadow-md);
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-2);
}
.card-name {
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
}
.student-count {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
  white-space: nowrap;
}
.card-desc {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
}

.leaderboard {
  border-top: 1px solid var(--color-border);
  padding-top: var(--space-3);
}
.leaderboard-title {
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: var(--space-2);
}
.empty-lb { font-size: var(--text-sm); color: var(--color-text-muted); }
.lb-list { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: var(--space-1); }
.lb-entry {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-sm);
}
.lb-rank  { color: var(--color-text-muted); width: 18px; flex-shrink: 0; }
.lb-name  { flex: 1; font-weight: var(--font-semibold); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.lb-score { color: var(--color-text-muted); white-space: nowrap; }
</style>
