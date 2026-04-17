<template>
  <main class="medals-view">
    <header class="medals-view__header">
      <RouterLink :to="{ name: 'Home' }" class="medals-view__back" aria-label="Tilbake til hjem">
        ← Tilbake
      </RouterLink>
      <div class="medals-view__heading">
        <h1 class="medals-view__title">Dine Medaljer</h1>
        <p v-if="!loading && !error" class="medals-view__count">
          {{ earnedCount }} av {{ allMedals.length }} opptjent
        </p>
      </div>
    </header>

    <div v-if="loading" class="medals-view__state" aria-live="polite">
      <span class="medals-view__spinner" aria-hidden="true" />
      <p>Laster medaljer…</p>
    </div>

    <div v-else-if="error" class="medals-view__state medals-view__state--error" role="alert">
      <p>{{ error }}</p>
      <button class="medals-view__retry" @click="load">Prøv igjen</button>
    </div>

    <ul v-else class="medals-view__grid" aria-label="Medaljer">
      <li
        v-for="medal in allMedals"
        :key="medal.id"
        class="medal-card"
        :class="{ 'medal-card--locked': !medal.earnedAt }"
        :aria-label="medal.earnedAt ? medal.name : medal.name + ' (ikke opptjent ennå)'"
      >
        <span class="medal-card__icon" aria-hidden="true">
          {{ medal.earnedAt ? '🏅' : '🔒' }}
        </span>
        <div class="medal-card__body">
          <h2 class="medal-card__name">{{ medal.name }}</h2>
          <p class="medal-card__desc">{{ medal.description }}</p>
          <time v-if="medal.earnedAt" class="medal-card__date" :datetime="medal.earnedAt">
            Opptjent {{ formatDate(medal.earnedAt) }}
          </time>
          <span v-else class="medal-card__locked-label">Fullfør stoppen for å vinne!</span>
        </div>
      </li>
    </ul>
  </main>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useGameStore } from '@/stores/game'

const gameStore = useGameStore()

const allMedals = ref([])
const loading = ref(true)
const error = ref(null)

const earnedCount = computed(() => allMedals.value.filter(m => m.earnedAt).length)

async function load() {
  loading.value = true
  error.value = null
  try {
    allMedals.value = await gameStore.fetchAllMedals()
    console.log('[MedalsView] Loaded', allMedals.value.length, 'medals,', earnedCount.value, 'earned')
  } catch (err) {
    console.error('[MedalsView] Failed to load medals:', err)
    error.value = 'Kunne ikke laste medaljer. Prøv igjen.'
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
.medals-view {
  min-height: 100vh;
  padding: var(--space-6);
  background: var(--color-bg);
}

.medals-view__header { margin-bottom: var(--space-8); }

.medals-view__back {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-sm);
  color: var(--color-primary);
  text-decoration: none;
  margin-bottom: var(--space-4);
}
.medals-view__back:hover { text-decoration: underline; }

.medals-view__title {
  margin: 0 0 var(--space-1);
  font-size: var(--text-3xl);
  color: var(--color-heading);
}
.medals-view__count { margin: 0; font-size: var(--text-base); color: var(--color-text-muted); }

.medals-view__state {
  display: flex; flex-direction: column; align-items: center;
  justify-content: center; gap: var(--space-3); min-height: 16rem;
  text-align: center; color: var(--color-text-muted);
}
.medals-view__state--error { color: var(--color-danger); }
.medals-view__spinner {
  width: 32px; height: 32px;
  border: 3px solid var(--color-border); border-top-color: var(--color-primary);
  border-radius: 50%; animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
.medals-view__retry {
  padding: var(--space-2) var(--space-4);
  background: var(--color-primary); color: #fff;
  border: none; border-radius: var(--radius-md); cursor: pointer;
  font-size: var(--text-sm); font-weight: var(--font-semibold);
}
.medals-view__retry:hover { background: var(--color-primary-dark); }

.medals-view__grid {
  list-style: none; margin: 0; padding: 0;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: var(--space-4);
}

.medal-card {
  display: flex; align-items: flex-start; gap: var(--space-4);
  padding: var(--space-6);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  transition: box-shadow var(--transition-fast);
}
.medal-card:hover { box-shadow: var(--shadow-md); }

.medal-card--locked {
  opacity: 0.55;
  filter: grayscale(60%);
}

.medal-card__icon { font-size: 2.25rem; line-height: 1; flex-shrink: 0; }

.medal-card__body {
  display: flex; flex-direction: column; gap: var(--space-1); min-width: 0;
}

.medal-card__name {
  margin: 0; font-size: var(--text-lg); font-weight: var(--font-bold); color: var(--color-heading);
}

.medal-card__desc {
  margin: 0; font-size: var(--text-sm); color: var(--color-text-muted); line-height: 1.4;
}

.medal-card__date {
  display: block; margin-top: var(--space-2);
  font-size: var(--text-xs); color: var(--color-text-muted); font-style: italic;
}

.medal-card__locked-label {
  display: block; margin-top: var(--space-2);
  font-size: var(--text-xs); color: var(--color-text-muted); font-style: italic;
}

@media (max-width: 480px) {
  .medals-view { padding: var(--space-4); }
  .medals-view__grid { grid-template-columns: 1fr; }
}
</style>
