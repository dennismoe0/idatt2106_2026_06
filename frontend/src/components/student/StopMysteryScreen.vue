<template>
  <div class="mystery-screen">
    <div class="mystery-card">
      <div class="mystery-card__tape" aria-hidden="true">HEMMELIG</div>

      <div v-if="imageSrc" class="mystery-card__image-wrap">
        <img :src="imageSrc" :alt="imageAlt || title" class="mystery-card__image" />
      </div>

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
  imageSrc: { type: String, default: '' },
  imageAlt: { type: String, default: '' },
})
defineEmits(['accept'])
</script>

<style scoped>
.mystery-screen {
  position: fixed;
  inset: 0;
  background: radial-gradient(ellipse at 40% 40%, var(--color-mystery-dark) 0%, var(--color-mystery-darkest) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  padding: var(--space-4);
}

.mystery-card {
  position: relative;
  background: var(--color-mystery-parchment);
  border-radius: 2px;
  padding: var(--space-6) var(--space-6) var(--space-6);
  width: min(100%, 760px);
  box-shadow:
    0 0 0 1px var(--color-mystery-border),
    0 4px 8px rgba(0,0,0,0.4),
    0 16px 48px rgba(0,0,0,0.6);
  transform: rotate(-0.5deg);
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  font-family: 'Georgia', 'Times New Roman', serif;
}

.mystery-card__image-wrap {
  margin: calc(var(--space-2) * -1) calc(var(--space-2) * -1) 0;
  border-radius: var(--radius-md);
  overflow: hidden;
  border: 1px solid var(--color-mystery-border);
  box-shadow: 0 8px 18px rgba(0,0,0,0.18);
  background: var(--color-mystery-dark);
}

.mystery-card__image {
  display: block;
  width: 100%;
  max-height: 360px;
  object-fit: cover;
  object-position: center;
}

.mystery-card__tape {
  position: absolute;
  top: -14px;
  left: 50%;
  transform: translateX(-50%) rotate(-1deg);
  background: var(--color-mystery-tape);
  color: var(--color-ink-body);
  font-family: ui-monospace, monospace;
  font-size: var(--text-xs);
  font-weight: 700;
  letter-spacing: 0.15em;
  padding: 4px 20px;
  border-top: 1px solid var(--color-mystery-tape-border);
  border-bottom: 1px solid var(--color-mystery-tape-border);
  white-space: nowrap;
  box-shadow: 0 2px 6px rgba(0,0,0,0.2);
}

.mystery-card__badge {
  display: inline-block;
  background: var(--color-mystery-dark);
  color: var(--color-mystery-gold);
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
  color: var(--color-mystery-dark);
  line-height: 1.2;
}

.mystery-card__divider {
  height: 2px;
  background: repeating-linear-gradient(
    90deg,
    var(--color-mystery-dark) 0px, var(--color-mystery-dark) 8px,
    transparent 8px, transparent 14px
  );
  opacity: 0.25;
}

.mystery-card__scenario {
  margin: 0;
  font-size: var(--text-base);
  color: var(--color-mystery-text);
  line-height: 1.75;
  white-space: pre-line;
}

.mystery-card__accept {
  align-self: flex-end;
  background: var(--color-mystery-dark);
  color: var(--color-mystery-gold);
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
.mystery-card__accept:hover  { background: var(--color-mystery-hover); }
.mystery-card__accept:active { transform: scale(0.97); }

@media (max-width: 640px) {
  .mystery-card {
    width: min(100%, 540px);
    padding: var(--space-5);
  }

  .mystery-card__image {
    max-height: 220px;
  }
}
</style>
