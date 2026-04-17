<template>
  <main class="home-view">
    <header class="home-view__header">
      <h1 class="home-view__title">Hei, {{ studentName }}</h1>
      <button type="button" class="home-view__logout" @click="handleLogout">Logg ut</button>
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
import { useRouter } from 'vue-router'
import CorkboardCard from '@/components/student/CorkboardCard.vue'
import { useAuthStore } from '@/stores/auth'
import { useClassroomStore } from '@/stores/classroom'

const authStore = useAuthStore()
const classroomStore = useClassroomStore()
const router = useRouter()
const studentName = computed(() =>
  classroomStore.displayName || formatDisplayName(authStore.email)
)

const cards = [
  { title: 'Kart', icon: '🗺️', route: { name: 'Map' }, locked: false },
  { title: 'Medaljer', icon: '🏅', route: { name: 'Medals' }, locked: false },
  { title: 'Notatblokk', icon: '📝', route: { name: 'Notebook' }, locked: false },
  { title: 'Avatar', icon: '🕵️', route: { name: 'Avatar' }, locked: false },
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

async function handleLogout() {
  console.log('[HomeView] Student logout')
  authStore.logout()
  await router.push({ name: 'StudentLogin' })
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
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  margin-bottom: var(--space-6);
}

.home-view__title {
  margin: 0;
  color: var(--color-text);
  font-size: var(--text-2xl);
}

.home-view__logout {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 44px;
  padding: var(--space-2) var(--space-4);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background-color: var(--color-surface);
  color: var(--color-text);
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
  cursor: pointer;
  transition:
    background-color var(--transition-fast),
    border-color var(--transition-fast);
}

.home-view__logout:hover {
  background-color: var(--color-bg);
}

.home-view__logout:focus-visible {
  outline: 2px solid var(--color-focus);
  outline-offset: 2px;
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

  .home-view__header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
