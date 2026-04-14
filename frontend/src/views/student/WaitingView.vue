<template>
  <main class="waiting-page">
    <section class="waiting-card">
      <h1>Venter på godkjenning</h1>
      <p>
        Forespørselen din er sendt til læreren. Når du er godkjent, kan du fortsette inn i klassen.
      </p>

      <p v-if="pendingJoin?.displayName" class="waiting-detail">
        Du er registrert som <strong>{{ pendingJoin.displayName }}</strong>
        <span v-if="pendingJoin.code"> med klassekode <strong>{{ pendingJoin.code }}</strong></span>.
      </p>

      <RouterLink class="waiting-link" to="/login">Tilbake til innlogging</RouterLink>
    </section>
  </main>
</template>

<script setup>
import { storeToRefs } from 'pinia'
import { useClassroomStore } from '@/stores/classroom'

const classroomStore = useClassroomStore()
const { pendingJoin } = storeToRefs(classroomStore)
</script>

<style scoped>
.waiting-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-4);
  background: var(--color-bg);
}

.waiting-card {
  width: min(100%, 460px);
  padding: var(--space-8);
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
}

h1 {
  margin-bottom: var(--space-4);
  color: var(--color-primary);
  font-size: var(--text-3xl);
}

p {
  color: var(--color-text);
  line-height: 1.5;
}

.waiting-detail {
  margin-top: var(--space-4);
  color: var(--color-text-muted);
}

.waiting-link {
  display: inline-block;
  margin-top: var(--space-6);
  color: var(--color-primary);
  font-weight: var(--font-medium);
}
</style>
