<script setup>
import { computed } from 'vue'
import { useClassroomStore } from '@/stores/classroom'
import ActiveMysteryWidget from '@/components/student/ActiveMysteryWidget.vue'

const classroomStore = useClassroomStore()
const classroomId = computed(() => Number(classroomStore.currentClassroomId))
</script>

<template>
  <div class="mysterium-view">
    <h1 class="mysterium-view__title">Ukas Mysterium</h1>
    <p class="mysterium-view__desc">
      Hvert mysterium er sendt inn av en elev i klassen din og godkjent av læreren.
      Svar riktig og tjen stjerner og XP!
    </p>
    <ActiveMysteryWidget v-if="classroomId" :classroom-id="classroomId" />
    <p v-else class="mysterium-view__no-class">Du er ikke i en klasse ennå.</p>
  </div>
</template>

<style scoped>
.mysterium-view {
  max-width: 680px;
  margin: 0 auto;
  padding: 2rem 1rem;
}
.mysterium-view__title {
  font-size: 1.75rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
}
.mysterium-view__desc {
  color: var(--color-text-muted, #6b7280);
  margin-bottom: 2rem;
}
.mysterium-view__no-class {
  color: var(--color-text-muted, #6b7280);
}
</style>
