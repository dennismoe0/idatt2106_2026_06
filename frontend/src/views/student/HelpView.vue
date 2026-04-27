<template>
  <CorkBoardPage page-title="Hjelp & regler" :back-to="{ name: 'Home' }">
    <div class="help-view">
      <section class="help-view__hero" aria-labelledby="help-title">
        <p class="help-view__eyebrow">Nettdetektivene · Feltmanual</p>
        <h1 id="help-title" class="help-view__title">Slik spiller du</h1>
        <p class="help-view__lead">
          Du er detektiv i Internettbyen. Penger som skulle gå til den nye
          idrettsparken har forsvunnet, og du må samle spor gjennom oppdragene
          for å finne ut hvem som står bak.
        </p>
      </section>

      <section class="help-card" aria-labelledby="map-title">
        <div class="help-card__icon" aria-hidden="true">🗺️</div>
        <div>
          <h2 id="map-title" class="help-card__title">Bruk kartet</h2>
          <ol class="help-list">
            <li>Klikk på et åpent stopp på kartet for å flytte detektiven dit.</li>
            <li>Trykk på <strong>Start bane</strong> for å begynne oppdraget.</li>
            <li>Fullfør ett stopp for å låse opp neste del av etterforskningen.</li>
            <li>Gå tilbake til hjemmesiden når du vil sjekke notater, medaljer eller profil.</li>
          </ol>
        </div>
      </section>

      <section class="help-card" aria-labelledby="missions-title">
        <div class="help-card__icon" aria-hidden="true">🎯</div>
        <div>
          <h2 id="missions-title" class="help-card__title">Oppdragene</h2>
          <div class="help-stops">
            <article
              v-for="stop in stops"
              :key="stop.name"
              class="help-stop"
            >
              <span class="help-stop__icon" aria-hidden="true">{{ stop.icon }}</span>
              <div>
                <h3 class="help-stop__name">{{ stop.name }}</h3>
                <p class="help-stop__description">{{ stop.description }}</p>
              </div>
            </article>
          </div>
        </div>
      </section>

      <section class="help-grid" aria-label="Spillregler">
        <article class="help-note">
          <span class="help-note__pin" aria-hidden="true"></span>
          <h2 class="help-note__title">Spor og saksmappe</h2>
          <p>
            Hvert stopp gir deg ny kunnskap og spor. Bruk notatblokken til å se
            tipsene du har samlet og skrive egne refleksjoner underveis.
          </p>
        </article>

        <article class="help-note help-note--gold">
          <span class="help-note__pin" aria-hidden="true"></span>
          <h2 class="help-note__title">Stjerner og medaljer</h2>
          <p>
            Riktige svar gir XP og stjerner. Når du fullfører et stopp får du en
            medalje som viser at du har løst en del av saken.
          </p>
        </article>

        <article class="help-note help-note--teal">
          <span class="help-note__pin" aria-hidden="true"></span>
          <h2 class="help-note__title">Ukens mysterium</h2>
          <p>
            Læreren kan aktivere ekstra mysterier fra klassen. Sjekk Ukas
            Mysterium på hjemmesiden for flere saker å løse.
          </p>
        </article>
      </section>

      <div class="help-view__actions">
        <RouterLink :to="{ name: 'Home' }" class="help-view__home-link">
          Tilbake til hjemmesiden
        </RouterLink>
      </div>
    </div>
  </CorkBoardPage>
</template>

<script setup>
import CorkBoardPage from '@/components/common/CorkBoardPage.vue'
import { RouterLink } from 'vue-router'

const stops = [
  {
    name: 'Nyhetskvartalet',
    icon: '📰',
    description: 'Finn ut hvilke nyheter som er troverdige, og hvilke som prøver å lure deg.',
  },
  {
    name: 'Fotografen',
    icon: '📷',
    description: 'Se etter tegn på ekte bilder, KI-bilder og manipulerte bilder.',
  },
  {
    name: 'Postkontoret',
    icon: '📧',
    description: 'Avslør mistenkelige e-poster, rare lenker og falske avsendere.',
  },
  {
    name: 'Markedsplassen',
    icon: '🛒',
    description: 'Undersøk nettbutikker og finn røde flagg før noen blir svindlet.',
  },
  {
    name: 'Den sosiale møteplassen',
    icon: '💬',
    description: 'Vurder innlegg og meldinger før du deler dem videre.',
  },
  {
    name: 'Passordbanken',
    icon: '🔐',
    description: 'Lær hva som gjør et passord sterkt, trygt og vanskelig å gjette.',
  },
  {
    name: 'Datasenteret',
    icon: '🏆',
    description: 'Bruk alt du har lært i sluttoppdraget for å stoppe datatyven.',
  },
]
</script>

<style scoped>
.help-view {
  max-width: 1080px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: clamp(var(--space-4), 2vw, var(--space-7));
  color: var(--color-ink);
}

.help-view__hero,
.help-card,
.help-note {
  position: relative;
  border: 1px solid rgba(122, 78, 26, 0.24);
  box-shadow:
    3px 6px 18px rgba(59, 31, 8, 0.18),
    inset 0 0 0 1px rgba(255, 255, 255, 0.24);
}

.help-view__hero {
  overflow: hidden;
  border-radius: var(--radius-lg);
  padding: clamp(var(--space-5), 5vw, var(--space-8));
  background:
    radial-gradient(circle at 86% 24%, rgba(239, 180, 92, 0.28), transparent 30%),
    linear-gradient(135deg, var(--color-wood) 0%, var(--color-wood-mid) 52%, var(--color-cork-dark) 100%);
  color: var(--color-medal-gold-bg);
}

.help-view__hero::after {
  content: '?';
  position: absolute;
  right: clamp(var(--space-4), 8vw, var(--space-10));
  bottom: -2.75rem;
  font-family: 'Special Elite', serif;
  font-size: clamp(7rem, 18vw, 13rem);
  line-height: 1;
  color: rgba(255, 255, 255, 0.07);
}

.help-view__eyebrow {
  margin: 0 0 var(--space-2);
  color: var(--color-gold);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.help-view__title {
  margin: 0;
  font-family: 'Special Elite', serif;
  font-size: clamp(2rem, 5vw, 3.5rem);
  color: var(--color-medal-gold-bg);
}

.help-view__lead {
  max-width: 42rem;
  margin: var(--space-3) 0 0;
  color: rgba(255, 251, 235, 0.82);
  font-size: var(--text-lg);
  line-height: 1.7;
}

.help-card {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: var(--space-4);
  border-radius: var(--radius-lg);
  padding: clamp(var(--space-4), 4vw, var(--space-6));
  background:
    linear-gradient(180deg, color-mix(in srgb, var(--color-note-bg) 96%, white) 0%, var(--color-note-chosen-bg) 100%);
}

.help-card__icon {
  width: 3.5rem;
  height: 3.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: linear-gradient(180deg, #fff6d0 0%, #ffd776 100%);
  box-shadow: 0 8px 18px rgba(186, 124, 20, 0.22);
  font-size: 1.75rem;
}

.help-card__title,
.help-note__title {
  margin: 0 0 var(--space-3);
  font-family: 'Special Elite', serif;
  color: var(--color-wood);
}

.help-list {
  margin: 0;
  padding-left: var(--space-5);
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  color: var(--color-ink-body);
  line-height: 1.65;
}

.help-stops {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-3);
}

.help-stop {
  display: flex;
  gap: var(--space-3);
  align-items: flex-start;
  min-height: 100%;
  padding: var(--space-3);
  border: 1px dashed rgba(122, 78, 26, 0.28);
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.34);
}

.help-stop__icon {
  flex-shrink: 0;
  font-size: 1.5rem;
}

.help-stop__name {
  margin: 0 0 var(--space-1);
  font-size: var(--text-base);
  font-weight: var(--font-bold);
  color: var(--color-wood);
}

.help-stop__description {
  margin: 0;
  color: var(--color-ink-subtle);
  font-size: var(--text-sm);
  line-height: 1.5;
}

.help-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--space-4);
}

.help-note {
  padding: var(--space-5);
  border-radius: 2px;
  background: #7c3aed;
  color: var(--color-text-on-dark);
  transform: rotate(-0.8deg);
}

.help-note:nth-child(2) {
  transform: rotate(0.9deg);
}

.help-note:nth-child(3) {
  transform: rotate(-0.4deg);
}

.help-note--gold {
  background: #b45309;
}

.help-note--teal {
  background: #0f766e;
}

.help-note__pin {
  position: absolute;
  top: -9px;
  left: 50%;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: var(--color-red-pin);
  box-shadow:
    0 3px 6px rgba(0, 0, 0, 0.55),
    inset 0 1px 2px rgba(255, 255, 255, 0.25);
  transform: translateX(-50%);
}

.help-note__title {
  color: var(--color-text-on-dark);
}

.help-note p {
  margin: 0;
  color: rgba(255, 255, 255, 0.82);
  line-height: 1.6;
}

.help-view__actions {
  display: flex;
  justify-content: center;
  padding-top: var(--space-2);
}

.help-view__home-link {
  min-height: 44px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md);
  padding: var(--space-2) var(--space-6);
  background: linear-gradient(180deg, #ffd76a 0%, var(--color-gold) 100%);
  color: var(--color-wood);
  font-weight: var(--font-bold);
  text-decoration: none;
  box-shadow: 0 6px 16px rgba(59, 31, 8, 0.22);
  transition:
    transform var(--transition-fast),
    box-shadow var(--transition-fast);
}

.help-view__home-link:hover,
.help-view__home-link:focus-visible {
  transform: translateY(-2px);
  box-shadow: 0 10px 22px rgba(59, 31, 8, 0.28);
}

.help-view__home-link:focus-visible {
  outline: 3px solid var(--color-focus);
  outline-offset: 3px;
}

@media (max-width: 820px) {
  .help-stops,
  .help-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 560px) {
  .help-card {
    grid-template-columns: 1fr;
  }

  .help-card__icon {
    width: 3rem;
    height: 3rem;
  }
}
</style>
