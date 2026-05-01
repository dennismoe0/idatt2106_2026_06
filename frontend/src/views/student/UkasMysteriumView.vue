<script setup>
import { computed } from 'vue'
import { useClassroomStore } from '@/stores/classroom'
import DetectiveBar from '@/components/common/DetectiveBar.vue'
import ActiveMysteryWidget from '@/components/student/ActiveMysteryWidget.vue'

const classroomStore = useClassroomStore()
const classroomId = computed(() => Number(classroomStore.currentClassroomId))
</script>

<template>
  <div class="mysterium-view cork-board-bg">
    <DetectiveBar :back-to="{ name: 'Home' }" page-title="Ukas Mysterium" />

    <main class="mysterium-view__main">
      <header class="mysterium-view__header">
        <h1 class="mysterium-view__title">Ukas Mysterium</h1>
        <p class="mysterium-view__desc">
          Hvert mysterium er sendt inn av en elev i klassen din og godkjent av læreren.
          Svar riktig og tjen stjerner og XP!
        </p>
      </header>

      <ActiveMysteryWidget v-if="classroomId" :classroom-id="classroomId" />
      <div v-else class="mysterium-view__no-class">
        <p>Du er ikke i en klasse ennå.</p>
      </div>
    </main>
  </div>
</template>

<style scoped>
.mysterium-view {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.mysterium-view__main {
  flex: 1;
  max-width: 700px;
  width: 100%;
  margin: 0 auto;
  padding: var(--space-6) var(--space-4) var(--space-10);
}

.mysterium-view__header {
  margin-bottom: var(--space-6);
}

.mysterium-view__title {
  font-family: 'Special Elite', serif;
  font-size: clamp(1.8rem, 5vw, 2.6rem);
  font-weight: var(--font-bold);
  color: var(--color-gold);
  margin: 0 0 var(--space-3);
  text-shadow: 0 2px 6px rgba(0, 0, 0, 0.4);
}

.mysterium-view__desc {
  font-size: var(--text-base);
  color: var(--color-medals-text-on-cork);
  opacity: 0.85;
  line-height: 1.55;
  margin: 0;
}

.mysterium-view__no-class {
  background: var(--color-note-bg);
  border: 2px dashed var(--color-note-border);
  border-radius: var(--radius-md);
  padding: var(--space-6);
  text-align: center;
  color: var(--color-wood);
  font-size: var(--text-base);
  box-shadow: 0 4px 0 rgba(0, 0, 0, 0.2);
}
</style>
