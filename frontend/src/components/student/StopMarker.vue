<template>
  <div
    class="stop-marker"
    :class="{
      'stop-marker--locked':    stop.locked && !flash,
      'stop-marker--completed': stop.completed,
      'stop-marker--unlocked':  !stop.locked && !stop.completed,
      'stop-marker--flash':     flash
    }"
    role="button"
    :tabindex="stop.locked ? -1 : 0"
    :aria-disabled="stop.locked"
    @click="handleClick"
    @keydown.enter="handleClick"
    @keydown.space.prevent="handleClick"
  >
    <span class="stop-marker__icon" aria-hidden="true">
      {{ stop.locked ? '🔒' : stop.completed ? '✅' : '📍' }}
    </span>
    <span class="stop-marker__name">{{ stop.name }}</span>
    <span class="stop-marker__count" v-if="!stop.completed">
      {{ stop.taskCount }} oppgaver
    </span>
    <button
      v-if="stop.xpClaimable"
      class="stop-marker__claim-btn"
      @click.stop="emit('claim-xp', stop)"
      aria-label="Krev ukentlig XP for dette stoppet"
    >
      ⚡ Hent XP
    </button>
  </div>
</template>

<script setup>
const props = defineProps({
  stop: {
    type: Object,
    required: true
  },
  flash: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['click', 'claim-xp'])

function handleClick() {
  if (!props.stop.locked) {
    emit('click', props.stop)
  }
}
</script>

<style scoped>
.stop-marker {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-1);
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-lg);
  border: 2px solid var(--color-border);
  background: var(--color-surface);
  cursor: pointer;
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
  min-width: 7rem;
}
.stop-marker:hover:not([aria-disabled="true"]) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}
.stop-marker[aria-disabled="true"] { opacity: 0.5; cursor: not-allowed; }

.stop-marker--unlocked  { border-color: var(--color-primary); }
.stop-marker--completed { border-color: var(--color-success); background: var(--color-success-light); }
.stop-marker--locked    { border-color: var(--color-border); }

@keyframes flash-border-red {
  0%   { border-color: var(--color-danger); box-shadow: 0 0 0 3px color-mix(in srgb, var(--color-danger) 25%, transparent); }
  50%  { border-color: var(--color-border); box-shadow: none; }
  100% { border-color: var(--color-danger); box-shadow: 0 0 0 3px color-mix(in srgb, var(--color-danger) 25%, transparent); }
}
.stop-marker--flash {
  animation: flash-border-red 0.35s ease-in-out 2;
  border-color: var(--color-danger);
}

.stop-marker__icon { font-size: var(--text-2xl); }
.stop-marker__name { font-size: var(--text-sm); font-weight: var(--font-semibold); color: var(--color-text); }
.stop-marker__count { font-size: var(--text-xs); color: var(--color-text-muted); }

.stop-marker__claim-btn {
  font-size: var(--text-xs);
  font-weight: var(--font-semibold);
  color: var(--color-primary);
  background: var(--color-primary-light);
  border: 1px solid var(--color-primary);
  border-radius: var(--radius-sm);
  padding: var(--space-1) var(--space-2);
  cursor: pointer;
  margin-top: var(--space-1);
  transition: background var(--transition-fast);
}
.stop-marker__claim-btn:hover {
  background: var(--color-primary);
  color: var(--color-surface);
}
</style>
