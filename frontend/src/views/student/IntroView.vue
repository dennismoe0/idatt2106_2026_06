<template>
  <main class="intro-page">
    <section class="intro-card">
      <h1>Velkommen, detektiv</h1>
      <p class="intro-lead">
        Før du starter, får du en kort innføring i hvordan oppdragene fungerer.
      </p>
      <p class="intro-body">
        Les oppgavene nøye, samarbeid med klassen og bruk det du lærer til å avsløre falske spor og
        finne trygge valg på nett. Når du går videre, kan du bli med i et klasserom og starte
        første oppdrag.
      </p>

      <RouterLink class="intro-link" :to="{ name: 'JoinClassroom' }">Bli med i en klasse</RouterLink>
    </section>
  </main>
</template>

<script setup>
import { onMounted } from 'vue'
import { onBeforeRouteLeave } from 'vue-router'

onMounted(() => {
  console.log('[IntroView] Loaded')
})

onBeforeRouteLeave(() => {
  localStorage.setItem('hasSeenIntro', 'true')
})
</script>

<style scoped>
.intro-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: var(--color-bg);
  padding: var(--space-4);
}

.intro-card {
  width: min(100%, 40rem);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  padding: var(--space-8);
}

h1 {
  font-size: var(--text-3xl);
  color: var(--color-primary);
  margin-bottom: var(--space-2);
}

.intro-lead {
  color: var(--color-text);
  font-size: var(--text-lg);
  margin-bottom: var(--space-4);
}

.intro-body {
  color: var(--color-text-muted);
  margin-bottom: var(--space-6);
  line-height: 1.6;
}

.intro-link {
  color: var(--color-primary);
  font-weight: var(--font-medium);
}
</style>

