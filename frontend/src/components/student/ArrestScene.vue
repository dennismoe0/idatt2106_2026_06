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
    radial-gradient(circle at top, rgba(15, 76, 129, 0.18), transparent 35%),
    rgba(15, 23, 42, 0.8);
}

.arrest-popup__panel {
  width: min(100%, 34rem);
  display: grid;
  gap: var(--space-5);
  padding: clamp(1.5rem, 4vw, 2.25rem);
  border: 2px solid rgba(47, 26, 8, 0.18);
  border-radius: 1.25rem;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.92), rgba(255, 248, 223, 0.98)),
    linear-gradient(180deg, #fff9ea 0%, #fff3c8 100%);
  box-shadow:
    0 24px 60px rgba(15, 23, 42, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.7);
}

.arrest-popup__badge {
  width: fit-content;
  margin: 0;
  padding: 0.4rem 0.7rem;
  border-radius: 999px;
  background: #7f1d1d;
  color: #fff7ed;
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
  color: #2f1a08;
  font-size: clamp(1.75rem, 4vw, 2.3rem);
  line-height: 1.08;
}

.arrest-popup__body {
  margin: 0;
  max-width: 32ch;
  color: #4b341b;
  font-size: var(--text-base);
  line-height: 1.65;
}

.arrest-popup__button {
  width: 100%;
  min-height: 48px;
  padding: 0.95rem 1.2rem;
  border: 0;
  border-radius: 0.9rem;
  background: linear-gradient(180deg, #155e95 0%, #0f4c81 100%);
  color: #f8fafc;
  font: inherit;
  font-size: var(--text-base);
  font-weight: var(--font-bold);
  cursor: pointer;
  box-shadow: 0 10px 24px rgba(15, 76, 129, 0.28);
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
}

.arrest-popup__button:hover {
  transform: translateY(-1px);
  box-shadow: 0 14px 28px rgba(15, 76, 129, 0.34);
}

.arrest-popup__button:focus-visible {
  outline: 3px solid rgba(21, 94, 149, 0.28);
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
