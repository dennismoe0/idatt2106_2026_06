<template>
  <div class="player-hud" aria-label="Spillerstatus">
    <span class="player-hud__item player-hud__level" :title="`Nivå ${level} — ${level} av 7 stopp fullført`">
      <span class="player-hud__icon" aria-hidden="true">🕵️</span>
      <span class="player-hud__value">Nivå {{ level }}</span>
    </span>

    <span class="player-hud__item player-hud__xp" :title="`${xp} XP totalt`">
      <span class="player-hud__icon" aria-hidden="true">⚡</span>
      <span class="player-hud__value">{{ xp }} XP</span>
    </span>

    <span class="player-hud__item player-hud__stars" :title="`${starBalance} stjerner`">
      <span class="player-hud__icon" aria-hidden="true">⭐</span>
      <span class="player-hud__value">{{ starBalance }}</span>
    </span>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useGameStore } from '@/stores/game'

const gameStore = useGameStore()

const level       = computed(() => gameStore.level)
const xp          = computed(() => gameStore.xp)
const starBalance = computed(() => gameStore.starBalance)

onMounted(async () => {
  try {
    await gameStore.fetchProfile()
  } catch (err) {
    console.warn('[PlayerHud] Could not load profile:', err)
  }
})
</script>

<style scoped>
.player-hud {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.player-hud__item {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  color: var(--color-text);
  white-space: nowrap;
}

.player-hud__icon {
  font-size: var(--text-base);
  line-height: 1;
}

.player-hud__value {
  color: var(--color-primary);
}
</style>
