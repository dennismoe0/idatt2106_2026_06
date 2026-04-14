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
  border: 1px solid #cbd5e1;
  border-radius: 12px;
  padding: 1rem;
  background: #fff;
  display: grid;
  gap: 0.75rem;
}

.score {
  margin: 0;
  color: #334155;
}

.stop-done {
  color: #15803d;
  font-weight: 600;
}

.medal {
  border: 1px solid #facc15;
  background: #fef9c3;
  border-radius: 8px;
  padding: 0.75rem;
}

.medal p {
  margin-bottom: 0;
}

.actions {
  display: flex;
  gap: 0.75rem;
}

.actions button {
  border: 1px solid #94a3b8;
  background: #fff;
  border-radius: 8px;
  padding: 0.5rem 0.8rem;
  cursor: pointer;
}
</style>
