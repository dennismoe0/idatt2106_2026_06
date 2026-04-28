<template>
  <CorkBoardPage page-title="Hjelp & regler" :back-to="{ name: 'Home' }">
    <div class="help-view">
      <section class="help-view__hero" aria-labelledby="help-title">
        <p class="help-view__eyebrow">Nettdetektivene · Feltmanual</p>
        <h1 id="help-title" class="help-view__title">Slik spiller du</h1>
        <p class="help-view__lead">
          Penger som skulle gå til den nye idrettsparken i Internettbyen er
          borte. Som nettdetektiv skal du undersøke kilder, bilder, e-poster,
          nettsider og kontoer for å finne ut hva som skjedde.
        </p>
      </section>

      <section class="help-card" aria-labelledby="map-title">
        <div class="help-card__header">
          <div class="help-card__icon" aria-hidden="true">🗺️</div>
          <h2 id="map-title" class="help-card__title">Bruk kartet</h2>
        </div>
        <ol class="help-list">
          <li>Start med første åpne stopp. Nye steder låses opp når du fullfører saken der du er.</li>
          <li>Les oppdragsteksten før du svarer. Den forteller hva du skal undersøke.</li>
          <li>Se etter detaljer som kilde, dato, avsender, lenker, bilder, kontoer og passordvalg.</li>
          <li>Bruk tilbakemeldingene etter svar for å forstå hva som var trygt eller mistenkelig.</li>
        </ol>
      </section>

      <section class="help-card" aria-labelledby="missions-title">
        <div class="help-card__header">
          <div class="help-card__icon" aria-hidden="true">🎯</div>
          <h2 id="missions-title" class="help-card__title">Oppdragene</h2>
        </div>
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
      </section>

      <section class="help-grid" aria-label="Spillregler">
        <article class="help-note">
          <span class="help-note__pin" aria-hidden="true"></span>
          <h2 class="help-note__title">Spor og saksmappe</h2>
          <p>
            Etter hvert stopp får du et nytt spor i saken. Notatblokken hjelper
            deg å huske hva du har lært, og hvilke ledetråder som peker videre.
          </p>
        </article>

        <article class="help-note help-note--gold">
          <span class="help-note__pin" aria-hidden="true"></span>
          <h2 class="help-note__title">Stjerner og medaljer</h2>
          <p>
            Du får belønning når du løser ekte oppdrag. Medaljene viser hvilke
            deler av etterforskningen du har klart å fullføre.
          </p>
        </article>

        <article class="help-note help-note--teal">
          <span class="help-note__pin" aria-hidden="true"></span>
          <h2 class="help-note__title">Ukens mysterium</h2>
          <p>
            Her kan klassen få ekstra saker å undersøke. Bruk samme metode som
            i hovedspillet: stopp opp, sjekk kilden og velg trygg handling.
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

const stops = [
  {
    name: 'Nyhetskvartalet',
    icon: '📰',
    description: 'Sammenlign artikler og se etter kilde, dato, forfatter og overskrifter som prøver å skremme eller lure deg.',
  },
  {
    name: 'Fotografen',
    icon: '📷',
    description: 'Undersøk bilder nøye. Se etter rare detaljer, unaturlige skygger, uleselig tekst eller andre tegn på KI og manipulering.',
  },
  {
    name: 'Postkontoret',
    icon: '📧',
    description: 'Les e-poster som en detektiv. Sjekk avsender, lenker, hastverk, trusler og hva meldingen prøver å få deg til å gjøre.',
  },
  {
    name: 'Markedsplassen',
    icon: '🛒',
    description: 'Vurder om en nettbutikk virker trygg. Se på nettadresse, kontaktinfo, pris, anmeldelser og betalingsmåter før du stoler på den.',
  },
  {
    name: 'Den sosiale møteplassen',
    icon: '💬',
    description: 'Se om innlegg bruker sinne, frykt eller gruppepress. Velg trygge svar som å vente, sjekke kilder eller spørre en voksen.',
  },
  {
    name: 'Passordbanken',
    icon: '🔐',
    description: 'Lag og vurder passord. Unngå navn, brukernavn, fødselsår, enkle mønstre og passord du bruker flere steder.',
  },
  {
    name: 'Datasenteret',
    icon: '🏆',
    description: 'Sluttoppdraget tester alt du har lært. Du må bruke metodene fra de andre stoppene for å sikre saken helt til slutt.',
  },
]
</script>

<style scoped>
.help-view {
  max-width: 1080px;
  margin: 0 auto;
  padding: clamp(var(--space-3), 3vw, var(--space-6));
  display: flex;
  flex-direction: column;
  gap: clamp(var(--space-4), 3vw, var(--space-8));
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
  padding: clamp(var(--space-6), 6vw, var(--space-10));
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
  margin: var(--space-4) 0 0;
  color: rgba(255, 251, 235, 0.82);
  font-size: var(--text-lg);
  line-height: 1.7;
}

.help-card {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  border-radius: var(--radius-lg);
  padding: clamp(var(--space-6), 5vw, var(--space-8));
  background:
    linear-gradient(180deg, color-mix(in srgb, var(--color-note-bg) 96%, white) 0%, var(--color-note-chosen-bg) 100%);
}

.help-card__header {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.help-card__icon {
  width: 3.5rem;
  height: 3.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--color-medals-stat-icon-bg);
  box-shadow: 0 8px 18px rgba(186, 124, 20, 0.22);
  font-size: 1.75rem;
}

.help-card__title,
.help-note__title {
  margin: 0;
  font-family: 'Special Elite', serif;
  color: var(--color-wood);
}

.help-card__title {
  font-size: clamp(1.5rem, 3vw, 2rem);
  letter-spacing: 0.02em;
}

.help-note__title {
  margin-bottom: var(--space-4);
}

.help-list {
  margin: 0;
  padding-left: var(--space-6);
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  color: var(--color-ink-body);
  line-height: 1.65;
}

.help-stops {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: clamp(var(--space-4), 2.5vw, var(--space-6));
}

.help-stop {
  display: flex;
  gap: var(--space-4);
  align-items: flex-start;
  padding: var(--space-4);
  border: 1px dashed rgba(122, 78, 26, 0.28);
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.34);
}

.help-stop__icon {
  flex-shrink: 0;
  font-size: 1.5rem;
}

.help-stop__name {
  margin: 0 0 var(--space-2);
  font-size: var(--text-base);
  font-weight: var(--font-bold);
  color: var(--color-wood);
}

.help-stop__description {
  margin: 0;
  color: var(--color-ink-subtle);
  font-size: var(--text-sm);
  line-height: 1.6;
}

.help-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: clamp(var(--space-6), 3vw, var(--space-8));
}

.help-note {
  padding: clamp(var(--space-8), 4vw, var(--space-10));
  min-height: 12rem;
  border-radius: 2px;
  background: var(--color-primary);
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
  background: var(--color-medal-bronze-border);
}

.help-note--teal {
  background: var(--color-clue-correct-text);
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
  line-height: 1.7;
}

.help-view__actions {
  display: flex;
  justify-content: center;
  padding: var(--space-3) 0 var(--space-6);
}

.help-view__home-link {
  min-height: 44px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md);
  padding: var(--space-2) var(--space-6);
  background: var(--color-medals-button-bg);
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
  .help-card__header {
    align-items: flex-start;
  }

  .help-card__icon {
    width: 3rem;
    height: 3rem;
  }
}
</style>
