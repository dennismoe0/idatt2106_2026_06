<template>
  <CorkBoardPage page-title="Medaljer" :back-to="{ name: 'Home' }">
    <div class="medals">
      <p class="medals__subtitle" v-if="!loading && !error">
        {{ earnedCount }} av {{ allMedals.length }} opptjent
      </p>

      <div v-if="loading" class="medals__loading" aria-live="polite">
        <span class="medals__spinner" aria-hidden="true" />
        <p>Laster medaljer…</p>
      </div>

      <div v-else-if="error" class="medals__error" role="alert">
        <p>{{ error }}</p>
        <button class="medals__retry" @click="load">Prøv igjen</button>
      </div>

      <ul v-else class="medals__grid" aria-label="Medaljer">
        <li
          v-for="medal in allMedals"
          :key="medal.id"
          class="pinned-note medals__card"
          :class="{ 'medals__card--locked': !medal.earnedAt }"
          :style="`--card-rotate: ${cardRotation(medal.id)}deg`"
          :aria-label="medal.earnedAt ? medal.name : medal.name + ' (ikke opptjent ennå)'"
        >
          <span class="medals__icon" aria-hidden="true">{{ medal.earnedAt ? '🏅' : '🔒' }}</span>
          <h2 class="medals__name">{{ medal.name }}</h2>
          <p class="medals__desc">{{ medal.description }}</p>
          <time v-if="medal.earnedAt" class="medals__date" :datetime="medal.earnedAt">
            Opptjent {{ formatDate(medal.earnedAt) }}
          </time>
          <span v-else class="medals__hint">Fullfør stoppen for å vinne!</span>
        </li>
      </ul>
    </div>
  </CorkBoardPage>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import CorkBoardPage from '@/components/common/CorkBoardPage.vue'
import { useGameStore } from '@/stores/game'

const gameStore = useGameStore()
const allMedals = ref([])
const loading   = ref(true)
const error     = ref(null)

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

function cardRotation(id) {
  return (((id * 7) % 7) - 3) / 2
}

function formatDate(isoString) {
  if (!isoString) return ''
  return new Intl.DateTimeFormat('nb-NO', { day: 'numeric', month: 'long', year: 'numeric' }).format(new Date(isoString))
}

onMounted(load)
</script>

<style scoped>
.medals__subtitle {
  margin: 0 0 var(--space-6);
  font-size: var(--text-base);
  color: var(--color-cork-dark);
  font-weight: 600;
}

.medals__loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-3);
  min-height: 16rem;
  justify-content: center;
  color: var(--color-cork-dark);
}

.medals__spinner {
  width: 32px;
  height: 32px;
  border: 3px solid rgba(0,0,0,0.1);
  border-top-color: var(--color-cork-dark);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.medals__error {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-3);
  color: var(--color-danger);
}

.medals__retry {
  padding: var(--space-2) var(--space-4);
  background: var(--color-wood);
  color: var(--color-gold);
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  font-size: var(--text-sm);
  font-weight: 700;
}
.medals__retry:hover { background: var(--color-wood-mid); }

.medals__grid {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: clamp(var(--space-4), 3vw, var(--space-8));
}

.medals__card {
  transform: rotate(var(--card-rotate, 0deg));
  transition: transform var(--transition-normal), box-shadow var(--transition-normal);
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
}
.medals__card:hover {
  transform: rotate(0deg) scale(1.03) translateY(-4px);
  box-shadow: 4px 6px 18px rgba(0,0,0,0.4);
}
.medals__card--locked {
  opacity: 0.5;
  filter: grayscale(60%);
}

.medals__icon { font-size: 2.25rem; line-height: 1; }
.medals__name { margin: 0; font-size: var(--text-lg); font-weight: 700; color: var(--color-wood); }
.medals__desc { margin: 0; font-size: var(--text-sm); color: #555; line-height: 1.4; }
.medals__date, .medals__hint {
  display: block;
  font-size: var(--text-xs);
  color: #777;
  font-style: italic;
}
</style>
