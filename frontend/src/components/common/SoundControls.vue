<template>
  <div class="sound-controls" :class="{ 'sound-controls--muted': audioStore.muted }">
    <button
      class="mute-btn"
      :aria-label="audioStore.muted ? 'Slå på lyd' : 'Demp lyd'"
      :title="audioStore.muted ? 'Slå på lyd' : 'Demp lyd'"
      @click="audioStore.toggleMute"
    >
      <span aria-hidden="true">{{ audioStore.muted ? '🔇' : volumeIcon }}</span>
    </button>

    <input
      type="range"
      class="volume-slider"
      min="0"
      max="1"
      step="0.05"
      :value="audioStore.volume"
      :disabled="audioStore.muted"
      aria-label="Volum"
      @input="e => audioStore.setVolume(parseFloat(e.target.value))"
    />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useAudioStore } from '@/stores/audio'

const audioStore = useAudioStore()

const volumeIcon = computed(() => {
  if (audioStore.volume === 0) return '🔇'
  if (audioStore.volume < 0.4) return '🔈'
  if (audioStore.volume < 0.75) return '🔉'
  return '🔊'
})
</script>

<style scoped>
.sound-controls {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.mute-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-size: var(--text-lg);
  padding: var(--space-1);
  border-radius: var(--radius-sm);
  line-height: 1;
  transition: transform var(--transition-fast);
}
.mute-btn:hover { transform: scale(1.15); }
.mute-btn:active { transform: scale(0.95); }

.volume-slider {
  -webkit-appearance: none;
  appearance: none;
  width: 72px;
  height: 4px;
  border-radius: 2px;
  background: var(--color-border);
  outline: none;
  cursor: pointer;
  transition: opacity var(--transition-fast);
}
.volume-slider:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}
.volume-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  width: 14px;
  height: 14px;
  border-radius: var(--radius-full);
  background: var(--color-primary);
  cursor: pointer;
  transition: transform var(--transition-fast);
}
.volume-slider:not(:disabled)::-webkit-slider-thumb:hover {
  transform: scale(1.25);
}
.volume-slider::-moz-range-thumb {
  width: 14px;
  height: 14px;
  border-radius: var(--radius-full);
  background: var(--color-primary);
  border: none;
  cursor: pointer;
}
.sound-controls--muted .volume-slider { opacity: 0.35; }
</style>
