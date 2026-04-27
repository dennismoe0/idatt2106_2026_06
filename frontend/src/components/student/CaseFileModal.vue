<template>
  <Teleport to="body">
    <div class="casefile-backdrop" @click.self="$emit('close')">
      <div class="casefile" role="dialog" aria-modal="true" aria-label="Saksmappen din">

        <div class="casefile__header">
          <p class="casefile__eyebrow">🔎 DETEKTIVENS SAKSMAPPE</p>
          <button class="casefile__close" @click="$emit('close')" aria-label="Lukk">✕</button>
        </div>

        <div class="casefile__body">
          <div class="casefile__clues">
            <h3 class="casefile__section-title">Spor samlet inn</h3>
            <p v-if="!clues.length" class="casefile__empty">
              Ingen spor ennå. Fullfør stoppene på kartet for å samle bevis.
            </p>
            <div v-for="(clue, i) in clues" :key="i" class="casefile__clue-card">
              <span class="casefile__clue-num">{{ i + 1 }}</span>
              <p class="casefile__clue-text">{{ clue }}</p>
            </div>
          </div>

          <div class="casefile__suspects">
            <h3 class="casefile__section-title">Mistenkte</h3>
            <div class="casefile__suspect-grid">
              <div
                v-for="suspect in SUSPECTS"
                :key="suspect.id"
                class="casefile__suspect-chip"
              >
                <div class="casefile__suspect-portrait">
                  <img
                    v-if="suspect.image"
                    :src="suspect.image"
                    :alt="suspect.name"
                    class="casefile__suspect-img"
                    @error="e => e.target.style.display = 'none'"
                  />
                  <span v-else class="casefile__suspect-emoji">{{ suspect.emoji }}</span>
                </div>
                <span class="casefile__suspect-name">{{ suspect.name }}</span>
                <span class="casefile__suspect-role">{{ suspect.role }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="casefile__footer">
          <p v-if="!canAccuse" class="casefile__accuse-hint">
            Fullfør alle 6 stopp for å avsløre tyven ({{ clues.length }}/6 spor samlet).
          </p>
          <button
            v-else-if="!alreadyAccused"
            class="casefile__accuse-btn"
            @click="$emit('accuse')"
          >
            Jeg vet hvem det er — Pek ut tyven! →
          </button>
          <p v-else class="casefile__accuse-hint">Du har allerede pekt ut tyven. Gå til Datasenteret!</p>
        </div>

      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { computed } from 'vue'
import { useNotebookStore } from '@/stores/notebook'
import { SUSPECTS } from '@/data/suspects.js'

const props = defineProps({
  allStopsCompleted: { type: Boolean, default: false },
  alreadyAccused:    { type: Boolean, default: false },
})
defineEmits(['close', 'accuse'])

const notebookStore = useNotebookStore()

const clues = computed(() =>
  notebookStore.entries
    .filter(e => e.entryType === 'AUTO_TIP')
    .map(e => e.content)
)

const canAccuse = computed(() => props.allStopsCompleted)
</script>

<style scoped>
.casefile-backdrop {
  position: fixed; inset: 0; z-index: 300;
  background: rgba(10, 5, 0, 0.8);
  display: flex; align-items: center; justify-content: center;
  padding: var(--space-4);
}
.casefile {
  background: var(--color-cork);
  border: 2px solid var(--color-cork-dark);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  max-width: 860px; width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  display: flex; flex-direction: column; gap: var(--space-4);
  color: var(--color-wood);
}
.casefile__header {
  display: flex; justify-content: space-between; align-items: center;
}
.casefile__eyebrow {
  font-size: var(--text-xs); font-weight: var(--font-bold);
  letter-spacing: 0.15em; text-transform: uppercase;
  color: var(--color-wood); margin: 0;
}
.casefile__close {
  background: none; border: none; cursor: pointer;
  font-size: var(--text-lg); color: var(--color-wood);
  padding: var(--space-1); line-height: 1;
}
.casefile__body {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-6);
}
@media (max-width: 640px) {
  .casefile__body { grid-template-columns: 1fr; }
}
.casefile__section-title {
  font-family: 'Special Elite', serif;
  font-size: var(--text-base); font-weight: var(--font-bold);
  margin: 0 0 var(--space-3);
  color: var(--color-wood);
}
.casefile__empty { color: rgba(255, 255, 255, 0.8); font-size: var(--text-sm); margin: 0; }
.casefile__clue-card {
  display: flex; gap: var(--space-2); align-items: flex-start;
  background: var(--color-clue-bg); border: 1px dashed var(--color-medal-bronze-border);
  border-radius: var(--radius-sm); padding: var(--space-2) var(--space-3);
  margin-bottom: var(--space-2);
}
.casefile__clue-num {
  font-weight: var(--font-bold); color: var(--color-wood-mid);
  font-size: var(--text-xs); min-width: 16px;
}
.casefile__clue-text { margin: 0; font-size: var(--text-sm); line-height: 1.5; color: var(--color-wood); }
.casefile__suspect-grid {
  display: grid; grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: var(--space-2);
}
.casefile__suspect-chip {
  display: flex; flex-direction: column; align-items: center;
  gap: var(--space-1); text-align: center;
  background: var(--color-wood-mid); border-radius: var(--radius-sm);
  padding: var(--space-2);
}
.casefile__suspect-portrait {
  width: 56px; height: 56px; border-radius: 50%;
  overflow: hidden; background: var(--color-cork-dark);
  display: flex; align-items: center; justify-content: center;
  font-size: 1.6rem;
}
.casefile__suspect-img { width: 100%; height: 100%; object-fit: cover; }
.casefile__suspect-emoji { font-size: 1.6rem; }
.casefile__suspect-name { font-size: var(--text-xs); font-weight: var(--font-bold); color: var(--color-medal-gold-bg); line-height: 1.2; }
.casefile__suspect-role { font-size: 10px; opacity: 0.55; color: var(--color-medal-gold-bg); }
.casefile__footer {
  border-top: 1px solid var(--color-cork-dark);
  padding-top: var(--space-4);
  text-align: center;
}
.casefile__accuse-hint { color: rgba(255, 255, 255, 0.8); font-size: var(--text-sm); margin: 0; }
.casefile__accuse-btn {
  background: var(--color-danger); color: var(--color-text-on-dark);
  border: none; border-radius: var(--radius-md);
  padding: var(--space-3) var(--space-8);
  font-size: var(--text-base); font-weight: var(--font-bold);
  cursor: pointer; min-height: 44px;
  transition: background var(--transition-fast);
}
.casefile__accuse-btn:hover { filter: brightness(0.9); }
</style>
