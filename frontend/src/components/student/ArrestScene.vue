<template>
  <div class="arrest-scene" role="dialog" aria-modal="true" aria-labelledby="arrest-scene-title">
    <div class="arrest-scene__card">
      <p class="arrest-scene__badge">AVGJØRENDE GJENNOMBRUDD</p>
      <div class="arrest-scene__content">
        <h2 id="arrest-scene-title" class="arrest-scene__title">{{ scene.title }}</h2>
        <p class="arrest-scene__body">{{ scene.body }}</p>
      </div>
      <div class="arrest-scene__actions">
        <button class="arrest-scene__continue" @click="$emit('continue')">{{ scene.buttonText }}</button>
      </div>
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
.arrest-scene {
  position: fixed;
  inset: 0;
  z-index: 40;
  display: grid;
  place-items: center;
  padding: var(--space-4);
  background: rgba(15, 23, 42, 0.72);
}

.arrest-scene__card {
  width: min(100%, 680px);
  display: grid;
  gap: clamp(var(--space-4), 3vw, var(--space-6));
  padding: clamp(var(--space-6), 5vw, var(--space-9));
  border-radius: var(--radius-xl);
  background: linear-gradient(180deg, var(--color-warning-light) 0%, var(--color-surface) 100%);
  box-shadow: var(--shadow-lg);
}

.arrest-scene__badge {
  margin: 0;
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  letter-spacing: 0.12em;
  color: var(--color-danger);
}

.arrest-scene__content {
  display: grid;
  gap: var(--space-3);
  max-width: 34rem;
}

.arrest-scene__title {
  margin: 0;
  max-width: 16ch;
  font-size: clamp(1.75rem, 4vw, 2.25rem);
  line-height: 1.15;
}

.arrest-scene__body {
  margin: 0;
  line-height: 1.6;
  color: var(--color-text);
  max-width: 34rem;
}

.arrest-scene__actions {
  display: flex;
  justify-content: flex-start;
  margin-top: var(--space-1);
  padding-top: var(--space-4);
  border-top: 1px solid rgba(15, 23, 42, 0.08);
}

.arrest-scene__continue {
  border: none;
  border-radius: var(--radius-md);
  padding: 0.85rem 1.4rem;
  min-height: 44px;
  background: var(--color-primary);
  color: var(--color-text-on-dark);
  font-weight: var(--font-bold);
  cursor: pointer;
}

@media (max-width: 640px) {
  .arrest-scene {
    padding: var(--space-3);
  }

  .arrest-scene__card {
    padding: var(--space-5);
  }

  .arrest-scene__content,
  .arrest-scene__body {
    max-width: none;
  }

  .arrest-scene__continue {
    width: 100%;
  }
}
</style>
