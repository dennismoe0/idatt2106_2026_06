<template>
  <CorkBoardPage page-title="Medaljer" :back-to="{ name: 'Home' }">
    <div class="medals">
      <div v-if="loading" class="medals__state" aria-live="polite">
        <span class="medals__spinner" aria-hidden="true" />
        <p>Laster troférommet...</p>
      </div>

      <div v-else-if="error" class="medals__state medals__state--error" role="alert">
        <p>{{ error }}</p>
        <button class="medals__retry" @click="load">Prøv igjen</button>
      </div>

      <template v-else>
        <section class="medals__hero">
          <div class="medals__hero-copy">
            <p class="medals__eyebrow">Nettdetektivene · Bragder</p>
            <h2 class="medals__title">Velkommen til medaljeskapet ditt</h2>
            <p class="medals__lead">
              Hver medalje er et bevis på at du har løst et nytt mysterium som nettdetektiven. Fortsett jakten, fyll skapet og bli en ekte superdetektiv.
            </p>
          </div>

          <div class="medals__hero-display" aria-hidden="true">
            <div class="medals__spotlight" />
            <div class="medals__podium">
              <span class="medals__podium-ribbon medals__podium-ribbon--left">TOPP DETEKTIV</span>
              <div class="medals__showcase-medal">🏅</div>
              <span class="medals__podium-ribbon medals__podium-ribbon--right">HELTEMODUS</span>
            </div>
          </div>
        </section>

        <section class="medals__dashboard" aria-label="Framgang">
          <article class="medals__stat">
            <span class="medals__stat-icon" aria-hidden="true">🏆</span>
            <div>
              <p class="medals__stat-label">Opptjent</p>
              <strong class="medals__stat-value">{{ earnedCount }} / {{ allMedals.length }}</strong>
            </div>
          </article>

          <article class="medals__stat">
            <span class="medals__stat-icon" aria-hidden="true">✨</span>
            <div>
              <p class="medals__stat-label">Fullført</p>
              <strong class="medals__stat-value">{{ completionPercent }}%</strong>
            </div>
          </article>

          <article class="medals__stat">
            <span class="medals__stat-icon" aria-hidden="true">📅</span>
            <div>
              <p class="medals__stat-label">Siste trofe</p>
              <strong class="medals__stat-value">{{ latestEarnedLabel }}</strong>
            </div>
          </article>
        </section>

        <section class="medals__progress-card" aria-label="Fremdrift mot fullt skap">
          <div class="medals__progress-copy">
            <p class="medals__progress-title">Skapet fylles opp</p>
            <p class="medals__progress-text">
              {{ earnedCount === allMedals.length
                ? 'Alle medaljene er på plass. Dette rommet er klart for mesterdetektiver.'
                : `Du mangler ${remainingCount} medalje${remainingCount === 1 ? '' : 'r'} for å fylle hele skapet.` }}
            </p>
          </div>

          <div class="medals__meter" aria-hidden="true">
            <div class="medals__meter-fill" :style="{ width: `${completionPercent}%` }" />
          </div>
        </section>

        <section class="medals__locker-room" aria-labelledby="achievement-lockers-title">
          <div class="medals__section-head">
            <div>
              <p class="medals__section-kicker">Troférommet</p>
              <h2 id="achievement-lockers-title" class="medals__section-title">Bragder</h2>
            </div>
            <p class="medals__section-note">Her finner du alle de glitrende medaljene dine.</p>
          </div>

          <ul class="medals__locker-grid" aria-label="Alle medaljer">
            <li
              v-for="medal in allMedals"
              :key="medal.id"
              class="medals__locker"
              :class="{
                'medals__locker--earned': medal.earnedAt,
                'medals__locker--locked': !medal.earnedAt,
              }"
              :aria-label="medal.earnedAt ? `${medal.name}, opptjent ${formatDate(medal.earnedAt)}` : `${medal.name}, ikke opptjent ennå`"
            >
              <div class="medals__locker-frame">
                <span class="medals__locker-number">#{{ medal.id }}</span>
                <span class="medals__locker-shine" aria-hidden="true" />

                <div class="medals__medal-plate" aria-hidden="true">
                  <span class="medals__medal-icon">{{ medal.earnedAt ? medalIcon(medal.id) : '🔒' }}</span>
                </div>

                <div class="medals__locker-body">
                  <h3 class="medals__locker-title">{{ medal.name }}</h3>
                  <p class="medals__locker-desc">{{ medal.description }}</p>
                  <time v-if="medal.earnedAt" class="medals__locker-date" :datetime="medal.earnedAt">
                    Opptjent {{ formatDate(medal.earnedAt) }}
                  </time>
                  <span v-else class="medals__locker-hint">Løs et nytt stopp for å åpne denne plassen.</span>
                </div>
              </div>
            </li>
          </ul>
        </section>
      </template>
    </div>
  </CorkBoardPage>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import CorkBoardPage from '@/components/common/CorkBoardPage.vue'
import { useGameStore } from '@/stores/game'

const gameStore = useGameStore()
const allMedals = ref([])
const loading = ref(true)
const error = ref(null)

const earnedMedals = computed(() => allMedals.value.filter(medal => medal.earnedAt))
const earnedCount = computed(() => earnedMedals.value.length)
const remainingCount = computed(() => Math.max(allMedals.value.length - earnedCount.value, 0))
const completionPercent = computed(() => {
  if (!allMedals.value.length) return 0
  return Math.round((earnedCount.value / allMedals.value.length) * 100)
})

const latestEarnedMedal = computed(() => {
  return [...earnedMedals.value]
    .sort((a, b) => new Date(b.earnedAt).getTime() - new Date(a.earnedAt).getTime())[0] ?? null
})

const latestEarnedLabel = computed(() => {
  if (!latestEarnedMedal.value) return 'Ingen medaljer ennå'
  return `${latestEarnedMedal.value.name} · ${formatDate(latestEarnedMedal.value.earnedAt)}`
})

async function load() {
  loading.value = true
  error.value = null

  try {
    allMedals.value = await gameStore.fetchAllMedals()
    console.log('[MedalsView] Loaded', allMedals.value.length, 'medals,', earnedCount.value, 'earned')
  } catch (err) {
    console.error('[MedalsView] Failed to load medals:', err)
    error.value = 'Kunne ikke laste medaljene. Prøv igjen.'
  } finally {
    loading.value = false
  }
}

function medalIcon(id) {
  const icons = ['🏅', '🥇', '⭐', '🕵️', '🛡️', '🔍', '💎', '🚀']
  return icons[id % icons.length]
}

function formatDate(isoString) {
  if (!isoString) return ''

  return new Intl.DateTimeFormat('nb-NO', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  }).format(new Date(isoString))
}

onMounted(load)
</script>

<style scoped>
.medals {
  max-width: 1160px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: clamp(var(--space-4), 2vw, var(--space-8));
  color: var(--color-medals-text-on-cork);
}

.medals__state {
  min-height: 18rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-3);
  text-align: center;
  color: var(--color-ink);
}

.medals__state--error {
  color: var(--color-danger);
}

.medals__spinner {
  width: 36px;
  height: 36px;
  border: 3px solid var(--color-medals-state-spinner);
  border-top-color: var(--color-medals-text-on-cork);
  border-radius: 50%;
  animation: medals-spin 0.8s linear infinite;
}

@keyframes medals-spin {
  to { transform: rotate(360deg); }
}

.medals__retry {
  border: none;
  border-radius: 999px;
  padding: 0.75rem 1.25rem;
  background: var(--color-medals-button-bg);
  color: var(--color-medals-button-text);
  font-weight: 800;
  cursor: pointer;
  box-shadow: 0 8px 16px var(--color-medals-button-shadow);
}

.medals__hero {
  position: relative;
  overflow: hidden;
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(280px, 0.8fr);
  gap: clamp(var(--space-4), 3vw, var(--space-10));
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

.medals__hero::after {
  content: '';
  position: absolute;
  inset: 0;
  background:
    linear-gradient(90deg, transparent 0, var(--color-medals-hero-overlay) 20%, transparent 36%),
    repeating-linear-gradient(90deg, var(--color-medals-hero-overlay-soft), var(--color-medals-hero-overlay-soft) 10px, transparent 10px, transparent 22px);
  pointer-events: none;
}

.medals__hero-copy,
.medals__hero-display {
  position: relative;
  z-index: 1;
}

.medals__eyebrow,
.medals__section-kicker {
  margin: 0 0 var(--space-2);
  font-size: 0.75rem;
  font-weight: 800;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--color-medals-hero-kicker);
}

.medals__title {
  margin: 0;
  font-size: clamp(1.8rem, 4vw, 2.8rem);
  line-height: 1.05;
  color: var(--color-medals-hero-title);
}

.medals__lead {
  margin: var(--space-4) 0 0;
  max-width: 34rem;
  font-size: 1rem;
  line-height: 1.6;
  color: var(--color-medals-hero-body);
}

.medals__hero-display {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 250px;
}

.medals__spotlight {
  position: absolute;
  inset: 4% 14% auto;
  height: 82%;
  background: radial-gradient(circle at 50% 10%, var(--color-medals-spotlight-core), var(--color-medals-spotlight-edge) 50%, transparent 72%);
  filter: blur(2px);
}

.medals__podium {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: min(100%, 300px);
  min-height: 220px;
  margin-top: 1rem;
}

.medals__showcase-medal {
  position: relative;
  z-index: 1;
  width: 136px;
  height: 136px;
  display: grid;
  place-items: center;
  font-size: 4.25rem;
  border-radius: 50%;
  background: var(--color-medals-showcase-medal);
  box-shadow:
    0 0 0 10px var(--color-medals-showcase-ring),
    0 18px 28px var(--color-medals-showcase-shadow);
}

.medals__showcase-medal::before,
.medals__showcase-medal::after {
  content: '';
  position: absolute;
  top: -38px;
  width: 28px;
  height: 86px;
  border-radius: 999px;
  z-index: -1;
}

.medals__showcase-medal::before {
  left: 28px;
  background: var(--color-medals-showcase-ribbon-left);
  transform: rotate(10deg);
}

.medals__showcase-medal::after {
  right: 28px;
  background: var(--color-medals-showcase-ribbon-right);
  transform: rotate(-10deg);
}

.medals__podium-ribbon {
  position: absolute;
  top: 1.5rem;
  padding: 0.45rem 0.85rem;
  border-radius: 999px;
  font-size: 0.72rem;
  font-weight: 800;
  letter-spacing: 0.06em;
  color: var(--color-medals-podium-ribbon-text);
  background: var(--color-medals-podium-ribbon-bg);
  box-shadow: 0 8px 16px var(--color-medals-podium-ribbon-shadow);
}

.medals__podium-ribbon--left {
  left: 0.2rem;
  transform: rotate(-7deg);
}

.medals__podium-ribbon--right {
  right: 0.2rem;
  transform: rotate(7deg);
}

.medals__dashboard {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--space-4);
}

.medals__stat,
.medals__progress-card {
  position: relative;
  overflow: hidden;
  border-radius: 24px;
  background: var(--color-medals-card-bg);
  border: 2px solid var(--color-medals-card-border);
  box-shadow: 0 14px 24px var(--color-medals-card-shadow);
}

.medals__stat {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.2rem 1.25rem;
  color: var(--color-medals-card-text);
}

.medals__stat::after,
.medals__progress-card::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(130deg, var(--color-medals-card-gloss), transparent 55%);
  pointer-events: none;
}

.medals__stat-icon {
  width: 3rem;
  height: 3rem;
  flex-shrink: 0;
  display: grid;
  place-items: center;
  border-radius: 18px;
  font-size: 1.5rem;
  background: var(--color-medals-stat-icon-bg);
  box-shadow: inset 0 -3px 6px var(--color-medals-stat-icon-shadow);
}

.medals__stat-label {
  margin: 0 0 0.25rem;
  font-size: 0.78rem;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--color-medals-card-label);
}

.medals__stat-value {
  font-size: clamp(1.05rem, 2vw, 1.35rem);
  color: var(--color-medals-card-title);
}

.medals__progress-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  padding: 1.2rem 1.35rem;
  color: var(--color-medals-card-text);
}

.medals__progress-title {
  margin: 0 0 0.35rem;
  font-size: 1rem;
  font-weight: 900;
}

.medals__progress-text {
  margin: 0;
  max-width: 38rem;
  line-height: 1.5;
}

.medals__meter {
  width: min(260px, 40%);
  min-width: 160px;
  height: 18px;
  border-radius: 999px;
  background: var(--color-medals-meter-track);
  box-shadow: inset 0 2px 4px var(--color-medals-meter-shadow);
}

.medals__meter-fill {
  height: 100%;
  border-radius: inherit;
  background: var(--color-medals-meter-fill);
  box-shadow: 0 0 16px var(--color-medals-meter-glow);
  transition: width var(--transition-normal);
}

.medals__locker-room {
  padding: clamp(1rem, 2vw, 1.5rem);
  border-radius: 30px;
  background:
    var(--color-medals-room-bg);
  border: 2px solid var(--color-medals-room-border);
  box-shadow: inset 0 0 0 1px var(--color-medals-room-inner-border);
}

.medals__section-head {
  display: flex;
  justify-content: space-between;
  align-items: end;
  gap: var(--space-4);
  margin-bottom: var(--space-4);
}

.medals__section-title {
  margin: 0;
  font-size: clamp(1.4rem, 3vw, 2rem);
  color: var(--color-medals-text-on-cork);
}

.medals__section-note {
  margin: 0;
  max-width: 26rem;
  text-align: right;
  line-height: 1.5;
  color: var(--color-medals-room-note);
}

.medals__locker-grid {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: var(--space-4);
}

.medals__locker {
  min-height: 300px;
}

.medals__locker-frame {
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 1rem;
  border-radius: 24px;
  background:
    var(--color-medals-locker-bg);
  border: 3px solid var(--color-medals-locker-border);
  box-shadow:
    inset 0 0 0 4px var(--color-medals-locker-inner-border),
    0 14px 24px var(--color-medals-locker-shadow);
  transition: transform var(--transition-normal), box-shadow var(--transition-normal), filter var(--transition-normal);
}

.medals__locker-frame::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    linear-gradient(90deg, var(--color-medals-locker-panel), transparent 16%, transparent 84%, var(--color-medals-locker-panel)),
    linear-gradient(180deg, var(--color-medals-locker-highlight), transparent 26%);
  pointer-events: none;
}

.medals__locker:hover .medals__locker-frame {
  transform: translateY(-6px) rotate(-0.5deg);
  box-shadow:
    inset 0 0 0 4px var(--color-medals-locker-inner-border),
    0 18px 30px var(--color-medals-locker-shadow-hover);
}

.medals__locker--locked .medals__locker-frame {
  filter: grayscale(0.3) saturate(0.7);
  background:
    var(--color-medals-locker-bg-locked);
}

.medals__locker-number {
  position: absolute;
  top: 0.8rem;
  right: 0.8rem;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 2.5rem;
  padding: 0.25rem 0.5rem;
  border-radius: 999px;
  background: var(--color-medals-locker-number-bg);
  font-size: 0.72rem;
  font-weight: 800;
  color: var(--color-medals-locker-number-text);
}

.medals__locker-shine {
  position: absolute;
  top: -18%;
  left: -28%;
  width: 70%;
  height: 180%;
  background: linear-gradient(180deg, transparent, var(--color-medals-locker-shine), transparent);
  transform: rotate(18deg);
  opacity: 0.55;
  pointer-events: none;
}

.medals__medal-plate {
  position: relative;
  z-index: 1;
  width: 112px;
  height: 112px;
  margin: 1rem auto 1.1rem;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: var(--color-medals-locker-medal);
  box-shadow:
    inset 0 5px 8px var(--color-medals-locker-medal-shadow-inner),
    0 10px 18px var(--color-medals-locker-medal-shadow);
}

.medals__locker--locked .medals__medal-plate {
  background: var(--color-medals-locker-medal-locked);
}

.medals__medal-icon {
  font-size: 3rem;
  line-height: 1;
  filter: drop-shadow(0 2px 2px var(--color-medals-locker-icon-shadow));
}

.medals__locker-body {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  gap: 0.65rem;
  text-align: center;
  color: var(--color-medals-locker-text);
}

.medals__locker-title {
  margin: 0;
  font-size: 1.15rem;
  font-weight: 900;
  line-height: 1.2;
}

.medals__locker-desc {
  margin: 0;
  min-height: 3.6em;
  line-height: 1.5;
  color: var(--color-medals-locker-desc);
}

.medals__locker-date,
.medals__locker-hint {
  display: inline-flex;
  justify-content: center;
  margin-top: auto;
  padding: 0.55rem 0.75rem;
  border-radius: 999px;
  font-size: 0.83rem;
  font-weight: 700;
}

.medals__locker-date {
  color: var(--color-medals-success-text);
  background: var(--color-medals-locker-date-bg);
}

.medals__locker-hint {
  color: var(--color-medals-locker-hint-text);
  background: var(--color-medals-locker-hint-bg);
}

@media (max-width: 900px) {
  .medals__hero {
    grid-template-columns: 1fr;
  }

  .medals__dashboard {
    grid-template-columns: 1fr;
  }

  .medals__progress-card,
  .medals__section-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .medals__meter {
    width: 100%;
  }

  .medals__section-note {
    text-align: left;
  }
}

@media (max-width: 560px) {
  .medals {
    gap: var(--space-4);
  }

  .medals__hero,
  .medals__locker-room {
    border-radius: 22px;
  }

  .medals__podium-ribbon {
    font-size: 0.64rem;
  }

  .medals__locker-grid {
    grid-template-columns: 1fr;
  }
}
</style>
