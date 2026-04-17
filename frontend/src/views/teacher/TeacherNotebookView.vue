<template>
  <main class="teacher-notebook">
    <header class="teacher-notebook__header">
      <RouterLink :to="backRoute" class="teacher-notebook__back">← Tilbake</RouterLink>
      <div>
        <h1 class="teacher-notebook__title">📝 Notatblokk</h1>
        <p class="teacher-notebook__student">{{ studentName || 'Elev' }}</p>
      </div>
    </header>

    <div v-if="loading" class="teacher-notebook__state" aria-live="polite">
      <span class="teacher-notebook__spinner" aria-hidden="true" />
      <p>Laster notatblokk…</p>
    </div>

    <div v-else-if="error" class="teacher-notebook__state teacher-notebook__state--error" role="alert">
      <p>{{ error }}</p>
      <button class="teacher-notebook__retry" @click="load">Prøv igjen</button>
    </div>

    <div v-else-if="grouped.length === 0" class="teacher-notebook__state">
      <p>Eleven har ikke fullført noen stopp ennå.</p>
    </div>

    <div v-else class="teacher-notebook__stops">
      <section v-for="group in grouped" :key="group.stopId" class="stop-section">
        <h2 class="stop-section__heading">
          <span class="stop-section__num" aria-hidden="true">{{ group.stopOrder }}</span>
          {{ group.stopName }}
        </h2>

        <div v-if="group.autoTip" class="auto-tip">
          <p class="auto-tip__label">🔍 Detektivrapporten</p>
          <p class="auto-tip__content">{{ group.autoTip.content }}</p>
        </div>

        <div v-if="group.reflections.length" class="reflections">
          <h3 class="reflections__heading">Elevens observasjoner</h3>
          <div v-for="r in group.reflections" :key="r.id" class="reflection-entry">
            <p class="reflection-entry__text">{{ r.content }}</p>
            <time class="reflection-entry__date">{{ formatDate(r.createdAt) }}</time>
          </div>
        </div>
        <p v-else class="reflections__empty">Ingen observasjoner skrevet ennå.</p>
      </section>
    </div>
  </main>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { notebookService } from '@/services/notebookService'

const route = useRoute()
const studentId = Number(route.params.studentId)
const studentName = route.query.studentName || ''
const classroomId = route.query.classroomId ? Number(route.query.classroomId) : null
const backRoute = classroomId
  ? { name: 'ClassroomDetail', params: { id: classroomId } }
  : { name: 'Dashboard' }

const rawEntries = ref([])
const loading = ref(true)
const error = ref(null)

const grouped = computed(() => {
  const map = new Map()
  for (const e of rawEntries.value) {
    if (!map.has(e.stopId)) {
      map.set(e.stopId, { stopId: e.stopId, stopName: e.stopName, stopOrder: e.stopOrder, autoTip: null, reflections: [] })
    }
    const g = map.get(e.stopId)
    if (e.entryType === 'AUTO_TIP') g.autoTip = e
    else g.reflections.push(e)
  }
  return [...map.values()].sort((a, b) => a.stopOrder - b.stopOrder)
})

async function load() {
  loading.value = true
  error.value = null
  try {
    const { data } = await notebookService.getStudentEntries(studentId)
    rawEntries.value = data
    console.log('[TeacherNotebookView] Loaded', data.length, 'entries for student', studentId)
  } catch (err) {
    console.error('[TeacherNotebookView] Failed to load:', err)
    error.value = err?.response?.status === 403
      ? 'Denne eleven er ikke i noen av klassene dine.'
      : 'Kunne ikke laste notatblokk. Prøv igjen.'
  } finally {
    loading.value = false
  }
}

function formatDate(isoString) {
  if (!isoString) return ''
  return new Intl.DateTimeFormat('nb-NO', { day: 'numeric', month: 'long', year: 'numeric' }).format(new Date(isoString))
}

onMounted(load)
</script>

<style scoped>
.teacher-notebook {
  min-height: 100vh; padding: var(--space-6); background: var(--color-bg);
}
.teacher-notebook__header { margin-bottom: var(--space-8); }
.teacher-notebook__back {
  display: inline-flex; align-items: center; gap: var(--space-2);
  font-size: var(--text-sm); color: var(--color-primary); text-decoration: none;
  margin-bottom: var(--space-4);
}
.teacher-notebook__back:hover { text-decoration: underline; }
.teacher-notebook__title { margin: 0 0 var(--space-1); font-size: var(--text-3xl); color: var(--color-heading); }
.teacher-notebook__student { margin: 0; font-size: var(--text-lg); color: var(--color-text-muted); }

.teacher-notebook__state {
  display: flex; flex-direction: column; align-items: center;
  justify-content: center; gap: var(--space-3); min-height: 16rem;
  text-align: center; color: var(--color-text-muted);
}
.teacher-notebook__state--error { color: var(--color-danger); }
.teacher-notebook__spinner {
  width: 32px; height: 32px;
  border: 3px solid var(--color-border); border-top-color: var(--color-primary);
  border-radius: 50%; animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
.teacher-notebook__retry {
  padding: var(--space-2) var(--space-4); background: var(--color-primary); color: #fff;
  border: none; border-radius: var(--radius-md); cursor: pointer;
  font-size: var(--text-sm); font-weight: var(--font-semibold);
}

.teacher-notebook__stops { display: flex; flex-direction: column; gap: var(--space-6); max-width: 720px; }

.stop-section {
  background: var(--color-surface); border: 1px solid var(--color-border);
  border-radius: var(--radius-md); padding: var(--space-6);
  display: flex; flex-direction: column; gap: var(--space-4);
}
.stop-section__heading {
  margin: 0; font-size: var(--text-xl); color: var(--color-heading);
  display: flex; align-items: center; gap: var(--space-3);
}
.stop-section__num {
  display: inline-flex; align-items: center; justify-content: center;
  width: 28px; height: 28px; border-radius: 50%;
  background: var(--color-primary); color: #fff;
  font-size: var(--text-sm); font-weight: var(--font-bold); flex-shrink: 0;
}
.auto-tip {
  background: var(--color-primary-soft); border-left: 3px solid var(--color-primary);
  border-radius: 0 var(--radius-sm) var(--radius-sm) 0; padding: var(--space-4);
}
.auto-tip__label {
  margin: 0 0 var(--space-2); font-size: var(--text-sm); font-weight: var(--font-semibold);
  color: var(--color-primary); text-transform: uppercase; letter-spacing: 0.05em;
}
.auto-tip__content { margin: 0; font-size: var(--text-base); color: var(--color-text); line-height: 1.6; }
.reflections__heading {
  margin: 0 0 var(--space-3); font-size: var(--text-sm); font-weight: var(--font-semibold);
  color: var(--color-text-muted); text-transform: uppercase; letter-spacing: 0.05em;
}
.reflections__empty { margin: 0; font-size: var(--text-sm); color: var(--color-text-muted); font-style: italic; }
.reflection-entry { border-left: 2px solid var(--color-border-strong); padding-left: var(--space-3); margin-bottom: var(--space-3); }
.reflection-entry__text { margin: 0 0 var(--space-1); font-size: var(--text-base); color: var(--color-text); line-height: 1.5; }
.reflection-entry__date { font-size: var(--text-xs); color: var(--color-text-muted); font-style: italic; }
</style>
