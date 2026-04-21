<template>
  <Teleport to="body">
    <div class="clue-modal-backdrop" @click.self="$emit('close')">
      <div class="clue-modal" role="dialog" aria-modal="true" aria-label="Nytt spor funnet">

        <p class="clue-modal__eyebrow">🔍 NYTT SPOR FUNNET</p>
        <div class="clue-modal__tape clue-modal__tape--left" aria-hidden="true"></div>
        <div class="clue-modal__tape clue-modal__tape--right" aria-hidden="true"></div>

        <div class="clue-modal__card">
          <p class="clue-modal__text">{{ clueText }}</p>
        </div>

        <button class="clue-modal__btn" @click="$emit('close')">
          Legg i notatblokk og fortsett →
        </button>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
defineProps({ clueText: { type: String, required: true } })
defineEmits(['close'])
</script>

<style scoped>
.clue-modal-backdrop {
  position: fixed; inset: 0; z-index: 200;
  background: rgba(0,0,0,0.65);
  display: flex; align-items: center; justify-content: center;
  padding: var(--space-4);
}
.clue-modal {
  position: relative;
  background: var(--color-clue-missed-bg);
  border-radius: var(--radius-sm);
  padding: var(--space-8) var(--space-8) var(--space-6);
  max-width: 480px; width: 100%;
  text-align: center;
  box-shadow: 0 8px 32px rgba(0,0,0,0.45);
  transform: rotate(-1.5deg);
}
.clue-modal__eyebrow {
  margin: 0 0 var(--space-4);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  letter-spacing: 0.15em;
  color: var(--color-ink);
  text-transform: uppercase;
}
.clue-modal__card {
  background: var(--color-clue-bg);
  border: 2px dashed var(--color-medal-bronze-border);
  border-radius: var(--radius-md);
  padding: var(--space-4) var(--space-6);
  margin-bottom: var(--space-6);
}
.clue-modal__text {
  margin: 0;
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: var(--color-wood);
  line-height: 1.5;
}
.clue-modal__tape {
  position: absolute;
  top: -14px;
  width: 60px; height: 28px;
  background: rgba(251,191,36,0.65);
  border-radius: 3px;
}
.clue-modal__tape--left  { left: 20%; transform: rotate(-3deg); }
.clue-modal__tape--right { right: 20%; transform: rotate(2deg); }
.clue-modal__btn {
  background: var(--color-medal-bronze-border);
  color: var(--color-text-on-dark);
  border: none;
  border-radius: var(--radius-md);
  padding: var(--space-3) var(--space-6);
  font-size: var(--text-base);
  font-weight: var(--font-bold);
  cursor: pointer;
  transition: background var(--transition-fast);
}
.clue-modal__btn:hover { background: var(--color-wood-mid); }
</style>
