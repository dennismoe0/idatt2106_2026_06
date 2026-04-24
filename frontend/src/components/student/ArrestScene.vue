<template>
  <div class="arrest-scene" role="dialog" aria-modal="true" aria-labelledby="arrest-scene-title">
    <div class="arrest-scene__card">
      <p class="arrest-scene__badge">AVGJØRENDE GJENNOMBRUDD</p>
      <h2 id="arrest-scene-title" class="arrest-scene__title">{{ scene.title }}</h2>
      <p class="arrest-scene__body">{{ scene.body }}</p>
      <button class="arrest-scene__continue" @click="$emit('continue')">{{ scene.buttonText }}</button>
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
  width: min(100%, 560px);
  display: grid;
  gap: var(--space-4);
  padding: clamp(var(--space-5), 4vw, var(--space-8));
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

.arrest-scene__title {
  margin: 0;
  font-size: var(--text-2xl);
}

.arrest-scene__body {
  margin: 0;
  line-height: 1.6;
  color: var(--color-text);
}

.arrest-scene__continue {
  justify-self: start;
  border: none;
  border-radius: var(--radius-md);
  padding: var(--space-3) var(--space-5);
  min-height: 44px;
  background: var(--color-primary);
  color: var(--color-text-on-dark);
  font-weight: var(--font-bold);
  cursor: pointer;
}
</style>
