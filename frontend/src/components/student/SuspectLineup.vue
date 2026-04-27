<template>
  <Teleport to="body">
    <div class="lineup-backdrop">
      <div class="lineup" role="dialog" aria-modal="true" aria-label="Identifiser tyven">

        <template v-if="!revealed">
          <p class="lineup__eyebrow">🚔 IDENTIFISER TYVEN</p>
          <h2 class="lineup__title">Vi har nok bevis nå.<br>Hvem er tyven?</h2>
          <p class="lineup__sub">Les profilene og velg hvem du tror det er.</p>

          <div class="lineup__grid">
            <button
              v-for="suspect in SUSPECTS"
              :key="suspect.id"
              class="suspect-card"
              :class="{ 'suspect-card--selected': selected === suspect.id }"
              @click="selected = suspect.id"
            >
              <div class="suspect-card__portrait">
                <img
                  v-if="suspect.image"
                  :src="suspect.image"
                  :alt="suspect.name"
                  class="suspect-card__img"
                  @error="e => e.target.style.display = 'none'"
                />
                <span class="suspect-card__emoji">{{ suspect.emoji }}</span>
              </div>
              <strong class="suspect-card__name">{{ suspect.name }}</strong>
              <span class="suspect-card__role">{{ suspect.role }}</span>
            </button>
          </div>

          <button
            class="lineup__confirm"
            :disabled="selected === null"
            @click="reveal"
          >
            Jeg er sikker — avsløring!
          </button>
        </template>

        <template v-else>
          <div class="lineup__reveal">
            <p class="lineup__eyebrow lineup__eyebrow--arrested">🚨 ARRESTERT</p>
            <div class="lineup__reveal-portrait">
              <img
                v-if="THIEF.image"
                :src="THIEF.image"
                :alt="THIEF.name"
                class="lineup__reveal-img"
                @error="e => e.target.style.display = 'none'"
              />
              <span class="lineup__reveal-emoji">{{ THIEF.emoji }}</span>
            </div>
            <h2 class="lineup__reveal-name">{{ THIEF.name }}</h2>
            <p class="lineup__reveal-role">{{ THIEF.role }}</p>
            <p class="lineup__reveal-story">
              Malte Skygge eide nettkafeen der tyveriet ble planlagt og gjennomført.
              Han brukte stjålne passord og sendte phishing-e-poster for å komme inn i systemene.
            </p>
            <p class="lineup__reveal-result">
              🎉 Politiet har arrestert Malte Skygge.<br>
              Pengene til idrettsparken er sikret!
            </p>
            <button class="lineup__confirm" @click="$emit('chosen')">
              Videre til Datasenteret →
            </button>
          </div>
        </template>

      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref } from 'vue'
import { SUSPECTS, THIEF_ID } from '@/data/suspects.js'

defineEmits(['chosen'])

const THIEF = SUSPECTS[THIEF_ID]

const selected = ref(null)
const revealed = ref(false)

function reveal() {
  revealed.value = true
  console.log('[SuspectLineup] Student chose suspect:', selected.value, '— revealing Malte Skygge')
}
</script>

<style scoped>
.lineup-backdrop {
  position: fixed; inset: 0; z-index: 201;
  background: rgba(10,5,0,0.82);
  display: flex; align-items: center; justify-content: center;
  padding: var(--space-4);
  overflow-y: auto;
}
.lineup {
  background: var(--color-wood);
  border: 2px solid var(--color-wood-mid);
  border-radius: var(--radius-lg);
  padding: var(--space-8);
  max-width: 700px; width: 100%;
  text-align: center;
  color: var(--color-medal-gold-bg);
}
.lineup__eyebrow {
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: var(--color-gold);
  margin: 0 0 var(--space-2);
}
.lineup__eyebrow--arrested { color: var(--color-danger); }
.lineup__title {
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  margin: 0 0 var(--space-2);
  line-height: 1.3;
}
.lineup__sub {
  color: color-mix(in srgb, var(--color-medal-gold-bg) 65%, transparent);
  margin: 0 0 var(--space-6);
}
.lineup__grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: var(--space-3);
  margin-bottom: var(--space-6);
}
.suspect-card {
  background: var(--color-wood-mid);
  border: 2px solid var(--color-cork-dark);
  border-radius: var(--radius-md);
  padding: var(--space-3);
  cursor: pointer;
  display: flex; flex-direction: column; align-items: center; gap: var(--space-1);
  transition: border-color var(--transition-fast), background var(--transition-fast);
  color: var(--color-medal-gold-bg);
}
.suspect-card:hover { border-color: var(--color-gold); background: var(--color-cork-dark); }
.suspect-card--selected { border-color: var(--color-danger); background: var(--color-wood-mid); }
.suspect-card__portrait {
  width: 80px; height: 80px;
  border-radius: var(--radius-sm);
  overflow: hidden;
  display: flex; align-items: center; justify-content: center;
  background: var(--color-cork-dark);
  margin-bottom: var(--space-1);
  position: relative;
}
.suspect-card__img {
  width: 100%; height: 100%; object-fit: cover;
  position: absolute; inset: 0;
}
.suspect-card__emoji { font-size: 2.2rem; }
.suspect-card__name { font-size: var(--text-sm); font-weight: var(--font-bold); line-height: 1.2; }
.suspect-card__role { font-size: var(--text-xs); opacity: 0.55; }

.lineup__confirm {
  background: var(--color-medal-bronze-border);
  color: var(--color-text-on-dark);
  border: none;
  border-radius: var(--radius-md);
  padding: var(--space-3) var(--space-8);
  font-size: var(--text-base);
  font-weight: var(--font-bold);
  cursor: pointer;
  min-height: 44px;
  transition: background var(--transition-fast);
}
.lineup__confirm:disabled { opacity: 0.4; cursor: not-allowed; }
.lineup__confirm:not(:disabled):hover { background: var(--color-wood-mid); }

.lineup__reveal { display: flex; flex-direction: column; align-items: center; gap: var(--space-3); }
.lineup__reveal-portrait {
  width: 140px; height: 140px; border-radius: 50%;
  overflow: hidden; border: 3px solid var(--color-danger);
  display: flex; align-items: center; justify-content: center;
  position: relative;
}
.lineup__reveal-img {
  width: 100%; height: 100%; object-fit: cover;
  position: absolute; inset: 0;
}
.lineup__reveal-emoji { font-size: 5rem; }
.lineup__reveal-name { font-size: var(--text-3xl); font-weight: var(--font-bold); margin: 0; color: var(--color-danger); }
.lineup__reveal-role { opacity: 0.65; margin: 0; }
.lineup__reveal-story { max-width: 420px; line-height: 1.6; opacity: 0.85; }
.lineup__reveal-result { font-weight: var(--font-semibold); font-size: var(--text-lg); color: var(--color-gold); line-height: 1.5; }
</style>
