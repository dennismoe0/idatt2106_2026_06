<template>
  <section class="result-card">
    <h2>{{ result.correct ? 'Riktig!' : 'Ikke helt riktig' }}</h2>
    <p class="score">Poeng: {{ result.score }}</p>
    <p>{{ result.explanation }}</p>

    <p v-if="result.stopCompleted" class="stop-done">Du fullførte stoppet!</p>

    <article v-if="result.medalEarned" class="medal">
      <strong>Ny medalje:</strong> {{ result.medalEarned.name }}
      <p>{{ result.medalEarned.description }}</p>
    </article>

    <div class="actions">
      <button @click="$emit('next')">Neste oppgave</button>
      <button @click="$emit('backToMap')">Tilbake til kart</button>
    </div>
  </section>
</template>

<script setup>
defineProps({
  result: {
    type: Object,
    required: true
  }
})

defineEmits(['next', 'backToMap'])
</script>

<style scoped>
.result-card {
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  background: var(--color-surface);
  display: grid;
  gap: var(--space-3);
}

.score {
  margin: 0;
  color: var(--color-text);
}

.stop-done {
  color: var(--color-success);
  font-weight: var(--font-semibold);
}

.medal {
  border: 1px solid var(--color-accent);
  background: var(--color-accent-light);
  border-radius: var(--radius-md);
  padding: var(--space-3);
}

.medal p {
  margin-bottom: 0;
}

.actions {
  display: flex;
  gap: var(--space-3);
}

.actions button {
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  border-radius: var(--radius-md);
  padding: var(--space-2) var(--space-3);
  cursor: pointer;
}
</style>
