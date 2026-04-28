<template>
  <div class="dossier-page">
    <DetectiveBar :back-to="{ name: 'Home' }" page-title="Mistenktmappe" />

    <main class="dossier-shell">
      <section class="dossier-hero" aria-labelledby="dossier-title">
        <div>
          <p class="dossier-hero__eyebrow">Saksmappe 07</p>
          <h1 id="dossier-title">Mistenktmappe</h1>
          <p>
            Les vitneutsagn, sjekk alibi og koble spor fra gåteoppgavene.
            Finn hvem som passer med alle bevisene, ikke bare det som ser mistenkelig ut først.
          </p>
        </div>

        <div class="dossier-hero__actions">
          <RouterLink class="dossier-map-link" :to="{ name: preferredMap }">
            ← Til kartet
          </RouterLink>

          <div class="dossier-tabs" role="tablist" aria-label="Velg mappevisning">
            <button
              type="button"
              :class="{ 'is-active': activePanel === 'suspects' }"
              role="tab"
              :aria-selected="activePanel === 'suspects'"
              @click="activePanel = 'suspects'"
            >
              Mistenkte
            </button>
            <button
              type="button"
              :class="{ 'is-active': activePanel === 'clues' }"
              role="tab"
              :aria-selected="activePanel === 'clues'"
              @click="activePanel = 'clues'"
            >
              Sporbrett
            </button>
          </div>
        </div>
      </section>

      <p v-if="error" class="dossier-alert" role="alert">{{ error }}</p>

      <section v-if="activePanel === 'suspects'" class="dossier-layout" aria-label="Mistenkte og vitneutsagn">
        <div class="suspect-strip" aria-label="Velg mistenkt">
          <button
            v-for="suspect in SUSPECTS"
            :key="suspect.id"
            type="button"
            class="suspect-tile"
            :class="{ 'is-selected': selectedSuspect.id === suspect.id }"
            :style="{ '--suspect-color': suspect.color }"
            @click="selectedSuspectId = suspect.id"
          >
            <span class="suspect-tile__pin" aria-hidden="true"></span>
            <img :src="suspect.image" :alt="`${suspect.name}, ${suspect.role}`" @error="hideBrokenImage" />
            <span class="suspect-tile__name">{{ suspect.name }}</span>
            <span class="suspect-tile__role">{{ suspect.role }}</span>
          </button>
        </div>

        <article class="case-file" :style="{ '--suspect-color': selectedSuspect.color }">
          <div class="case-file__photo">
            <img :src="selectedSuspect.image" :alt="`${selectedSuspect.name}, ${selectedSuspect.role}`" @error="hideBrokenImage" />
            <span>{{ selectedSuspect.badge }}</span>
          </div>

          <div class="case-file__body">
            <div class="case-file__topline">
              <div>
                <p class="case-file__label">Mistenkt</p>
                <h2>{{ selectedSuspect.name }}</h2>
                <p>{{ selectedSuspect.role }}</p>
              </div>
              <div class="suspicion-meter" :aria-label="`Mistankemåler ${selectedSuspect.suspicion} av 3`">
                <span
                  v-for="level in 3"
                  :key="level"
                  :class="{ 'is-lit': level <= selectedSuspect.suspicion }"
                ></span>
              </div>
            </div>

            <section class="witness-card" aria-label="Vitneutsagn">
              <span class="witness-card__tag">{{ selectedSuspect.witnessTag }}</span>
              <h3>Vitneutsagn</h3>
              <blockquote>“{{ selectedSuspect.quote }}”</blockquote>
              <p>{{ selectedSuspect.checked }}</p>
            </section>

            <div class="evidence-columns">
              <section>
                <h3>Dette kan peke mot</h3>
                <ul>
                  <li v-for="item in selectedSuspect.pointsToward" :key="item">{{ item }}</li>
                </ul>
              </section>
              <section>
                <h3>Dette kan peke vekk</h3>
                <ul>
                  <li v-for="item in selectedSuspect.pointsAway" :key="item">{{ item }}</li>
                </ul>
              </section>
            </div>
          </div>
        </article>
      </section>

      <section v-else class="clue-board" aria-label="Spor fra gåteoppgavene">
        <div class="clue-board__intro">
          <h2>Sporbrett</h2>
          <p>
            Hvert spor kommer fra en læringsoppgave. Når du fullfører et stopp,
            havner sporet her så du kan sammenligne det med vitneutsagnene.
          </p>
          <button type="button" class="clue-board__refresh" :disabled="loading" @click="loadNotebookClues">
            {{ loading ? 'Oppdaterer...' : 'Oppdater spor' }}
          </button>
        </div>

        <div class="clue-grid">
          <article
            v-for="slot in clueCards"
            :key="slot.stopOrder"
            class="clue-card"
            :class="{ 'is-locked': !slot.content }"
          >
            <span class="clue-card__icon" aria-hidden="true">{{ slot.icon }}</span>
            <p class="clue-card__skill">{{ slot.skill }}</p>
            <h3>{{ slot.stopName }}</h3>
            <p v-if="slot.content" class="clue-card__content">{{ slot.content }}</p>
            <p v-else class="clue-card__locked">Løs gåteoppgaven på dette stoppet for å låse opp sporet.</p>
          </article>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import DetectiveBar from '@/components/common/DetectiveBar.vue'
import { useNotebookStore } from '@/stores/notebook'
import { CASE_CLUE_SLOTS, SUSPECTS } from '@/data/suspectDossier'

const route = useRoute()
const notebookStore = useNotebookStore()
const activePanel = ref(route.query.panel === 'clues' ? 'clues' : 'suspects')
const selectedSuspectId = ref(SUSPECTS[0].id)
const loading = ref(false)
const error = ref('')
const preferredMap = computed(() => localStorage.getItem('mapView') === 'simple' ? 'Map' : 'WorldMap')

const selectedSuspect = computed(() =>
  SUSPECTS.find(suspect => suspect.id === selectedSuspectId.value) ?? SUSPECTS[0]
)

const unlockedByStopOrder = computed(() => {
  const map = new Map()
  for (const group of notebookStore.grouped) {
    if (group.autoTip?.content) {
      map.set(group.stopOrder, group.autoTip.content)
    }
  }
  return map
})

const clueCards = computed(() =>
  CASE_CLUE_SLOTS.map(slot => ({
    ...slot,
    content: unlockedByStopOrder.value.get(slot.stopOrder) ?? '',
  }))
)

function hideBrokenImage(event) {
  event.currentTarget.style.display = 'none'
}

async function loadNotebookClues() {
  loading.value = true
  error.value = ''
  try {
    await notebookStore.fetchEntries()
  } catch (err) {
    console.error('[SuspectDossierView] Failed to load notebook clues:', err)
    error.value = 'Kunne ikke hente spor akkurat nå. Prøv igjen fra notatblokken eller kartet.'
  } finally {
    loading.value = false
  }
}

onMounted(loadNotebookClues)
</script>

<style scoped>
.dossier-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at 18% 12%, var(--color-dossier-glow), transparent 28rem),
    linear-gradient(135deg, var(--color-dossier-bg-start) 0%, var(--color-dossier-bg-mid) 48%, var(--color-dossier-bg-end) 100%);
  color: var(--color-dossier-ink);
}

.dossier-shell {
  width: min(1180px, calc(100% - 2rem));
  margin: 0 auto;
  padding: clamp(1rem, 2.5vw, 2rem) 0 3rem;
}

.dossier-hero {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1rem;
  color: var(--color-dossier-text-on-dark);
}

.dossier-hero__eyebrow {
  margin: 0 0 0.25rem;
  font-size: 0.75rem;
  font-weight: 900;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--color-dossier-gold);
}

.dossier-hero h1 {
  margin: 0;
  font-size: clamp(2rem, 6vw, 4rem);
  line-height: 1;
}

.dossier-hero p {
  max-width: 42rem;
  margin: 0.7rem 0 0;
  font-size: clamp(1rem, 2vw, 1.2rem);
  line-height: 1.5;
  color: var(--color-dossier-text-on-dark-muted);
}

.dossier-tabs {
  display: flex;
  gap: 0.5rem;
  padding: 0.35rem;
  background: var(--color-dossier-panel-tint);
  border: 1px solid var(--color-dossier-panel-border);
  border-radius: 8px;
  flex-shrink: 0;
}

.dossier-hero__actions {
  display: grid;
  gap: 0.65rem;
  justify-items: end;
  flex-shrink: 0;
}

.dossier-map-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 44px;
  padding: 0.65rem 1rem;
  border-radius: 8px;
  background: var(--color-dossier-paper);
  color: var(--color-dossier-ink-deep);
  border: 2px solid var(--color-dossier-link-border);
  box-shadow: 0 5px 0 var(--color-dossier-shadow-black);
  font-weight: 900;
  text-decoration: none;
}

.dossier-map-link:hover {
  transform: translateY(-1px);
}

.dossier-tabs button,
.clue-board__refresh {
  min-height: 44px;
  border: 0;
  border-radius: 6px;
  padding: 0.65rem 1rem;
  font: inherit;
  font-weight: 900;
  cursor: pointer;
}

.dossier-tabs button {
  color: var(--color-dossier-text-on-dark);
  background: transparent;
}

.dossier-tabs button.is-active {
  color: var(--color-dossier-ink-strong);
  background: var(--color-dossier-gold);
}

.dossier-alert {
  padding: 0.8rem 1rem;
  margin: 0 0 1rem;
  border-radius: 8px;
  background: var(--color-dossier-alert-bg);
  color: var(--color-dossier-danger);
  font-weight: 800;
}

.dossier-layout {
  display: grid;
  grid-template-columns: 17rem 1fr;
  gap: 1rem;
  align-items: start;
}

.suspect-strip {
  display: grid;
  gap: 0.75rem;
}

.suspect-tile {
  --suspect-color: var(--color-dossier-suspect-default);
  position: relative;
  display: grid;
  grid-template-columns: 4.2rem 1fr;
  grid-template-rows: auto auto;
  gap: 0.12rem 0.7rem;
  align-items: center;
  min-height: 5.2rem;
  padding: 0.7rem;
  text-align: left;
  background: var(--color-dossier-paper);
  border: 3px solid transparent;
  border-radius: 8px;
  box-shadow: 0 8px 0 var(--color-dossier-shadow);
  cursor: pointer;
}

.suspect-tile:hover,
.suspect-tile.is-selected {
  border-color: var(--suspect-color);
  transform: translateY(-1px);
}

.suspect-tile__pin {
  position: absolute;
  top: 0.35rem;
  right: 0.5rem;
  width: 0.75rem;
  height: 0.75rem;
  border-radius: 50%;
  background: var(--suspect-color);
  box-shadow: 0 2px 0 var(--color-dossier-shadow-black-soft);
}

.suspect-tile img {
  grid-row: 1 / 3;
  width: 4.2rem;
  height: 4.2rem;
  object-fit: cover;
  object-position: top;
  border-radius: 8px;
  border: 2px solid var(--color-dossier-photo-border);
  background: var(--color-dossier-paper-soft);
}

.suspect-tile__name {
  font-weight: 900;
  color: var(--color-dossier-ink-strong);
  line-height: 1.1;
}

.suspect-tile__role {
  color: var(--color-dossier-ink-muted);
  font-size: 0.9rem;
}

.case-file {
  --suspect-color: var(--color-dossier-suspect-default);
  display: grid;
  grid-template-columns: minmax(13rem, 19rem) 1fr;
  gap: clamp(1rem, 2vw, 1.5rem);
  padding: clamp(1rem, 2.5vw, 1.5rem);
  background:
    linear-gradient(90deg, var(--color-dossier-file-rule) 1px, transparent 1px),
    linear-gradient(var(--color-dossier-paper-top), var(--color-dossier-paper-bottom));
  background-size: 24px 24px, auto;
  border: 4px solid var(--color-dossier-frame);
  border-radius: 8px;
  box-shadow: 0 14px 0 var(--color-dossier-shadow-strong);
}

.case-file__photo {
  align-self: start;
  padding: 0.65rem;
  background: var(--color-dossier-white);
  border: 2px solid var(--color-dossier-photo-border-soft);
  border-radius: 8px;
  transform: rotate(-1deg);
  box-shadow: 0 10px 18px var(--color-dossier-shadow-photo);
}

.case-file__photo img {
  display: block;
  width: 100%;
  aspect-ratio: 4 / 5;
  object-fit: cover;
  object-position: top;
  border-radius: 5px;
  background: var(--color-dossier-paper-soft);
}

.case-file__photo span {
  display: block;
  margin-top: 0.55rem;
  padding: 0.45rem;
  border-radius: 6px;
  background: var(--suspect-color);
  color: var(--color-dossier-white);
  text-align: center;
  font-weight: 900;
}

.case-file__topline {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1rem;
}

.case-file__label {
  margin: 0 0 0.2rem;
  font-size: 0.72rem;
  text-transform: uppercase;
  letter-spacing: 0.16em;
  color: var(--suspect-color);
  font-weight: 900;
}

.case-file h2 {
  margin: 0;
  font-size: clamp(1.8rem, 4vw, 3rem);
  line-height: 1;
  color: var(--color-dossier-ink-strong);
}

.case-file__topline p {
  margin: 0.35rem 0 0;
  color: var(--color-dossier-ink-muted);
  font-weight: 800;
}

.suspicion-meter {
  display: flex;
  gap: 0.3rem;
  align-items: start;
  padding-top: 0.35rem;
}

.suspicion-meter span {
  width: 1.1rem;
  height: 1.1rem;
  border-radius: 50%;
  background: var(--color-dossier-meter);
  border: 2px solid var(--color-dossier-meter-border);
}

.suspicion-meter span.is-lit {
  background: var(--suspect-color);
  border-color: var(--color-dossier-ink-strong);
}

.witness-card,
.evidence-columns section,
.clue-card,
.clue-board__intro {
  border-radius: 8px;
  border: 2px solid var(--color-dossier-section-border);
  background: var(--color-dossier-section-bg);
}

.witness-card {
  position: relative;
  padding: 1rem;
  margin-bottom: 1rem;
}

.witness-card__tag {
  position: absolute;
  top: -0.8rem;
  right: 1rem;
  padding: 0.35rem 0.65rem;
  border-radius: 999px;
  background: var(--suspect-color);
  color: var(--color-dossier-white);
  font-size: 0.8rem;
  font-weight: 900;
}

.witness-card h3,
.evidence-columns h3,
.clue-board__intro h2,
.clue-card h3 {
  margin: 0 0 0.55rem;
  color: var(--color-dossier-ink-strong);
}

.witness-card blockquote {
  margin: 0 0 0.75rem;
  padding-left: 0.9rem;
  border-left: 6px solid var(--suspect-color);
  color: var(--color-dossier-ink-warm);
  font-size: 1.18rem;
  font-weight: 900;
  line-height: 1.45;
}

.witness-card p {
  margin: 0;
  color: var(--color-dossier-ink-body);
  line-height: 1.55;
}

.evidence-columns {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 1rem;
}

.evidence-columns section {
  padding: 1rem;
}

.evidence-columns ul {
  margin: 0;
  padding-left: 1.1rem;
  color: var(--color-dossier-ink-body);
  line-height: 1.55;
}

.evidence-columns li + li {
  margin-top: 0.4rem;
}

.clue-board {
  display: grid;
  gap: 1rem;
}

.clue-board__intro {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 0.75rem 1rem;
  align-items: center;
  padding: 1.1rem;
  background: var(--color-dossier-paper);
}

.clue-board__intro p {
  margin: 0;
  max-width: 48rem;
  color: var(--color-dossier-ink-soft);
  line-height: 1.5;
}

.clue-board__refresh {
  grid-row: 1 / 3;
  grid-column: 2;
  color: var(--color-dossier-white);
  background: var(--color-dossier-teal);
}

.clue-board__refresh:disabled {
  opacity: 0.6;
  cursor: wait;
}

.clue-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 1rem;
}

.clue-card {
  min-height: 12rem;
  padding: 1rem;
  background: var(--color-dossier-card);
  box-shadow: 0 8px 0 var(--color-dossier-shadow-soft);
}

.clue-card.is-locked {
  filter: saturate(0.55);
}

.clue-card__icon {
  display: inline-grid;
  place-items: center;
  width: 3rem;
  height: 3rem;
  margin-bottom: 0.75rem;
  border-radius: 8px;
  background: var(--color-dossier-gold);
  font-size: 1.6rem;
}

.clue-card__skill {
  margin: 0 0 0.15rem;
  color: var(--color-dossier-teal);
  font-size: 0.78rem;
  font-weight: 900;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.clue-card__content,
.clue-card__locked {
  margin: 0.65rem 0 0;
  color: var(--color-dossier-ink-body);
  line-height: 1.5;
  font-weight: 700;
}

.clue-card__locked {
  color: var(--color-dossier-locked);
}

@media (max-width: 950px) {
  .dossier-hero,
  .case-file,
  .dossier-layout,
  .clue-board__intro {
    grid-template-columns: 1fr;
  }

  .dossier-layout {
    display: block;
  }

  .suspect-strip {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    margin-bottom: 1rem;
  }

  .clue-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .clue-board__refresh {
    grid-row: auto;
    grid-column: auto;
    justify-self: start;
  }
}

@media (max-width: 620px) {
  .dossier-tabs,
  .evidence-columns,
  .suspect-strip,
  .clue-grid {
    grid-template-columns: 1fr;
  }

  .dossier-tabs {
    display: grid;
    width: 100%;
  }

  .dossier-hero__actions {
    justify-items: stretch;
  }

  .case-file__topline {
    display: block;
  }
}
</style>
