<template>
  <main class="home-view">
    <header class="home-view__header">
      <h1 class="home-view__title">Hei, {{ studentName }}</h1>
    </header>

    <section class="home-view__grid" aria-label="Studentmeny">
      <CorkboardCard
        v-for="card in cards"
        :key="card.title"
        :title="card.title"
        :icon="card.icon"
        :route="card.route"
        :locked="card.locked"
      />
    </section>
  </main>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import CorkboardCard from '@/components/student/CorkboardCard.vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const studentName = computed(() => formatDisplayName(authStore.email))

const cards = [
  { title: 'Kart', icon: '🗺️', route: '/map', locked: false },
  { title: 'Medaljer', icon: '🏅', locked: true },
  { title: 'Notatblokk', icon: '📝', locked: true },
  { title: 'Avatar', icon: '🕵️', route: '/avatar', locked: false },
  { title: 'Ledertavle', icon: '📊', locked: true },
  { title: 'Ukens Mysterium', icon: '🧩', locked: true },
  { title: 'Hjelp', icon: '💡', locked: true },
]

function formatDisplayName(email) {
  if (!email) return 'Detektiv'

  const base = email.endsWith('@student.local')
    ? email.replace('@student.local', '')
    : email.split('@')[0]

  return (
    base
      .split(/[._-]+/)
      .filter(Boolean)
      .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
      .join(' ') || 'Detektiv'
  )
}

onMounted(() => {
  console.log('[HomeView] Loaded student corkboard for:', studentName.value, 'cards:', cards.length)
})
</script>

<style scoped>
.home-view {
  min-height: 100vh;
  padding: var(--space-6);
  background: var(--color-bg);
}

.home-view__header {
  margin-bottom: var(--space-6);
}

.home-view__title {
  margin: 0;
  color: var(--color-text);
  font-size: var(--text-2xl);
}

.home-view__grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: var(--space-4);
}

@media (max-width: 640px) {
  .home-view {
    padding: var(--space-4);
  }
}
</style>
