<template>
  <div class="mystery-screen">
    <div class="mystery-card">
      <div class="mystery-card__tape" aria-hidden="true">HEMMELIG</div>

      <span class="mystery-card__badge">{{ badge }}</span>
      <h2 class="mystery-card__title">{{ title }}</h2>

      <div class="mystery-card__divider" aria-hidden="true" />

      <p class="mystery-card__scenario">{{ scenario }}</p>

      <button class="mystery-card__accept" @click="$emit('accept')">
        Godta oppdraget →
      </button>
    </div>
  </div>
</template>

<script setup>
defineProps({
  badge:    { type: String, required: true },
  title:    { type: String, required: true },
  scenario: { type: String, required: true },
})
defineEmits(['accept'])
</script>

<style scoped>
.mystery-screen {
  position: fixed;
  inset: 0;
  background: radial-gradient(ellipse at 40% 40%, #1a1209 0%, #0d0b07 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  padding: var(--space-4);
}

.mystery-card {
  position: relative;
  background: #f5efdf;
  border-radius: 2px;
  padding: var(--space-8) var(--space-8) var(--space-6);
  width: min(100%, 480px);
  box-shadow:
    0 0 0 1px #c8b89a,
    0 4px 8px rgba(0,0,0,0.4),
    0 16px 48px rgba(0,0,0,0.6);
  transform: rotate(-0.5deg);
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  font-family: 'Georgia', 'Times New Roman', serif;
}

.mystery-card__tape {
  position: absolute;
  top: -14px;
  left: 50%;
  transform: translateX(-50%) rotate(-1deg);
  background: rgba(255,220,50,0.85);
  color: #333;
  font-family: ui-monospace, monospace;
  font-size: var(--text-xs);
  font-weight: 700;
  letter-spacing: 0.15em;
  padding: 4px 20px;
  border-top: 1px solid rgba(200,160,0,0.5);
  border-bottom: 1px solid rgba(200,160,0,0.5);
  white-space: nowrap;
  box-shadow: 0 2px 6px rgba(0,0,0,0.2);
}

.mystery-card__badge {
  display: inline-block;
  background: #1a1209;
  color: #f5c842;
  font-family: ui-monospace, monospace;
  font-size: var(--text-xs);
  font-weight: 700;
  letter-spacing: 0.12em;
  padding: 4px 12px;
  border-radius: 2px;
  align-self: flex-start;
}

.mystery-card__title {
  margin: 0;
  font-size: var(--text-2xl);
  font-weight: 700;
  color: #1a1209;
  line-height: 1.2;
}

.mystery-card__divider {
  height: 2px;
  background: repeating-linear-gradient(
    90deg,
    #1a1209 0px, #1a1209 8px,
    transparent 8px, transparent 14px
  );
  opacity: 0.25;
}

.mystery-card__scenario {
  margin: 0;
  font-size: var(--text-base);
  color: #2c1f0e;
  line-height: 1.75;
}

.mystery-card__accept {
  align-self: flex-end;
  background: #1a1209;
  color: #f5c842;
  border: none;
  border-radius: 2px;
  padding: var(--space-3) var(--space-6);
  font-family: ui-monospace, monospace;
  font-size: var(--text-sm);
  font-weight: 700;
  letter-spacing: 0.05em;
  cursor: pointer;
  transition: background var(--transition-fast), transform var(--transition-fast);
  margin-top: var(--space-2);
}
.mystery-card__accept:hover  { background: #3d2b0a; }
.mystery-card__accept:active { transform: scale(0.97); }
</style>
