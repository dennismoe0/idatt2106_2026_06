<template>
  <div class="arrest-popup" role="dialog" aria-modal="true" aria-labelledby="arrest-popup-title">
    <div class="arrest-popup__panel">
      <p class="arrest-popup__badge">SAK OPPDATERT</p>

      <div class="arrest-popup__content">
        <h2 id="arrest-popup-title" class="arrest-popup__title">{{ scene.title }}</h2>
        <p class="arrest-popup__body">{{ scene.body }}</p>
      </div>

      <button class="arrest-popup__button arrest-scene__continue" @click="$emit('continue')">
        {{ scene.buttonText }}
      </button>
    </div>
  </div>
</template>

<script setup>
defineProps({
  scene: {
    type: Object,
    required: true,
  },
})

defineEmits(['continue'])
</script>

<style scoped>
.arrest-popup {
  position: fixed;
  inset: 0;
  z-index: 40;
  display: grid;
  place-items: center;
  padding: clamp(1rem, 4vw, 2rem);
  background:
    radial-gradient(circle at top, color-mix(in srgb, var(--color-primary-dark) 18%, transparent), transparent 35%),
    color-mix(in srgb, var(--color-text) 80%, transparent);
}

.arrest-popup__panel {
  width: min(100%, 34rem);
  display: grid;
  gap: var(--space-5);
  padding: clamp(1.5rem, 4vw, 2.25rem);
  border: 2px solid var(--color-dossier-photo-border-soft);
  border-radius: 1.25rem;
  background:
    linear-gradient(135deg, color-mix(in srgb, var(--color-dossier-white) 92%, transparent), color-mix(in srgb, var(--color-dossier-paper) 98%, transparent)),
    linear-gradient(180deg, var(--color-dossier-paper-bottom) 0%, var(--color-dossier-paper-soft) 100%);
  box-shadow:
    0 24px 60px color-mix(in srgb, var(--color-text) 30%, transparent),
    inset 0 1px 0 color-mix(in srgb, var(--color-dossier-white) 70%, transparent);
}

.arrest-popup__badge {
  width: fit-content;
  margin: 0;
  padding: 0.4rem 0.7rem;
  border-radius: 999px;
  background: var(--color-dossier-danger);
  color: var(--color-dossier-paper-bottom);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.arrest-popup__content {
  display: grid;
  gap: var(--space-3);
}

.arrest-popup__title {
  margin: 0;
  color: var(--color-dossier-frame);
  font-size: clamp(1.75rem, 4vw, 2.3rem);
  line-height: 1.08;
}

.arrest-popup__body {
  margin: 0;
  max-width: 32ch;
  color: var(--color-dossier-ink-body);
  font-size: var(--text-base);
  line-height: 1.65;
}

.arrest-popup__button {
  width: 100%;
  min-height: 48px;
  padding: 0.95rem 1.2rem;
  border: 0;
  border-radius: 0.9rem;
  background: linear-gradient(180deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
  color: var(--color-text-on-dark);
  font: inherit;
  font-size: var(--text-base);
  font-weight: var(--font-bold);
  cursor: pointer;
  box-shadow: 0 10px 24px color-mix(in srgb, var(--color-primary-dark) 28%, transparent);
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
}

.arrest-popup__button:hover {
  transform: translateY(-1px);
  box-shadow: 0 14px 28px color-mix(in srgb, var(--color-primary-dark) 34%, transparent);
}

.arrest-popup__button:focus-visible {
  outline: 3px solid color-mix(in srgb, var(--color-primary) 28%, transparent);
  outline-offset: 2px;
}

@media (max-width: 640px) {
  .arrest-popup__panel {
    gap: var(--space-4);
    padding: 1.25rem;
  }

  .arrest-popup__body {
    max-width: none;
  }
}
</style>
