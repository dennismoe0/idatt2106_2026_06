<template>
  <div
    class="mystery-screen"
    :class="{ 'mystery-screen--img': !!imageSrc }"
    :style="imageSrc ? { backgroundImage: `url(${imageSrc})` } : {}"
    role="dialog"
    aria-modal="true"
    :aria-label="imageAlt || title"
  >
    <!-- Dark gradient overlay — heavier at bottom for text legibility -->
    <div v-if="imageSrc" class="mystery-overlay" aria-hidden="true" />

<!-- Content: centered when no image, lower-third when image present -->
    <div class="mystery-content">
      <button class="mystery-content__back" @click="$emit('back')">
        ← Tilbake til kartet
      </button>
      <span class="mystery-content__badge">{{ badge }}</span>
      <h2 class="mystery-content__title">{{ title }}</h2>
      <div class="mystery-content__divider" aria-hidden="true" />
      <p class="mystery-content__scenario">{{ scenario }}</p>
      <button class="mystery-content__accept" @click="$emit('accept')">
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
defineEmits(['accept', 'back'])
</script>

<style scoped>
/* ── Stage ── */
.mystery-screen {
  position: fixed;
  inset: 0;
  z-index: 100;
  overflow: hidden;

  /* Fallback for stops without an image yet */
  background: radial-gradient(ellipse at 40% 40%, var(--color-mystery-dark) 0%, var(--color-mystery-darkest) 100%);

  /* When image is set via :style, these kick in */
  background-size: cover;
  background-position: center top;
  background-repeat: no-repeat;

  /* No-image layout: centred */
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-6);

  animation: mystery-in 0.65s ease both;
}

/* Image mode: full-height left panel */
.mystery-screen--img {
  align-items: stretch;
  justify-content: flex-start;
  padding: 0;
}

@keyframes mystery-in {
  from { opacity: 0; }
  to   { opacity: 1; }
}

/* ── Gradient overlay (only when image present) ── */
.mystery-overlay {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    /* bottom-heavy darkness for text legibility */
    linear-gradient(
      to bottom,
      rgba(0, 0, 0, 0.04) 0%,
      rgba(0, 0, 0, 0.08) 35%,
      rgba(0, 0, 0, 0.58) 62%,
      rgba(0, 0, 0, 0.88) 100%
    ),
    /* edge vignette — keeps attention on centre */
    radial-gradient(ellipse at 50% 40%, transparent 45%, rgba(0, 0, 0, 0.28) 100%);
}


/* ── Text content ── */
.mystery-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  font-family: 'Georgia', 'Times New Roman', serif;

  /* No-image mode: constrained centred card feel */
  width: min(100%, 540px);

  animation: content-rise 0.55s 0.2s ease both;
}

/* Image mode: panel fills full height, fixed % of viewport width */
.mystery-screen--img .mystery-content {
  width: 44vw;
  min-width: 280px;
  max-width: 680px;
  height: 100%;
  padding: 6vh 4vw;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  gap: 2vh;
  background: rgba(0, 0, 0, 0.52);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
  overflow-y: auto;
}

@keyframes content-rise {
  from { opacity: 0; transform: translateY(22px); }
  to   { opacity: 1; transform: translateY(0); }
}

/* ── Badge ── */
.mystery-content__badge {
  display: inline-block;
  background: var(--color-mystery-gold);
  color: var(--color-mystery-dark);
  font-family: ui-monospace, monospace;
  font-size: var(--text-xs);
  font-weight: 700;
  letter-spacing: 0.12em;
  padding: 4px 12px;
  border-radius: 2px;
  align-self: flex-start;
}

.mystery-content__back {
  align-self: flex-start;
  background: var(--color-mystery-gold);
  color: var(--color-mystery-dark);
  border: 2px solid rgba(0, 0, 0, 0.22);
  border-radius: 999px;
  padding: var(--space-2) var(--space-5);
  font-size: var(--text-sm);
  font-weight: 700;
  letter-spacing: 0.03em;
  cursor: pointer;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.28);
  transition: background var(--transition-fast), border-color var(--transition-fast), transform var(--transition-fast), box-shadow var(--transition-fast);
}

.mystery-content__back:hover {
  background: #f5e08a;
  border-color: rgba(0, 0, 0, 0.32);
  box-shadow: 0 10px 22px rgba(0, 0, 0, 0.36);
  transform: translateY(-1px);
}

.mystery-content__back:active {
  transform: scale(0.97);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.25);
}

.mystery-screen:not(.mystery-screen--img) .mystery-content__back {
  color: var(--color-mystery-dark);
  border-color: rgba(0, 0, 0, 0.22);
}

/* ── Title ── */
.mystery-content__title {
  margin: 0;
  font-size: clamp(1.4rem, 4vh, 2.6rem);
  font-weight: 700;
  line-height: 1.15;
  color: #ffffff;
  text-shadow: 0 2px 20px rgba(0, 0, 0, 0.95), 0 1px 5px rgba(0, 0, 0, 0.8);
  max-width: 680px;
}

/* No-image fallback: use parchment tone instead of stark white */
.mystery-screen:not(.mystery-screen--img) .mystery-content__title {
  color: var(--color-mystery-parchment);
  text-shadow: none;
}

/* ── Dashed gold divider ── */
.mystery-content__divider {
  height: 2px;
  width: min(100%, 400px);
  background: repeating-linear-gradient(
    90deg,
    rgba(245, 200, 66, 0.75) 0px, rgba(245, 200, 66, 0.75) 8px,
    transparent 8px, transparent 14px
  );
}

/* ── Scenario ── */
.mystery-content__scenario {
  margin: 0;
  font-family: 'Special Elite', 'Courier New', monospace;
  font-size: clamp(1rem, 2.2vh, 1.6rem);
  line-height: 1.75;
  max-width: 620px;
  white-space: pre-line;
  color: rgba(255, 255, 255, 0.95);
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.8);
}

.mystery-screen:not(.mystery-screen--img) .mystery-content__scenario {
  color: var(--color-mystery-text);
  text-shadow: none;
}

/* ── Accept button ── */
.mystery-content__accept {
  align-self: flex-start;
  margin-top: var(--space-2);
  background: var(--color-mystery-gold);
  color: var(--color-mystery-dark);
  border: none;
  border-radius: 2px;
  padding: var(--space-3) var(--space-6);
  font-family: ui-monospace, monospace;
  font-size: var(--text-sm);
  font-weight: 700;
  letter-spacing: 0.06em;
  cursor: pointer;
  transition: background var(--transition-fast), transform var(--transition-fast);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.4);
}
.mystery-content__accept:hover  { background: #f5e08a; }
.mystery-content__accept:active { transform: scale(0.97); }

/* ── Mobile ── */
/* Mobile/tablet: drop the side-panel, use full-width bottom sheet instead */
@media (max-width: 768px) {
  .mystery-screen--img {
    align-items: flex-end;
  }

  .mystery-screen--img .mystery-content {
    width: 100%;
    max-width: 100%;
    height: auto;
    justify-content: flex-start;
    padding: var(--space-8) var(--space-5) var(--space-6);
    gap: var(--space-3);
  }

  .mystery-content__title {
    font-size: clamp(1.4rem, 5vw, 2rem);
  }

  .mystery-content__scenario {
    font-size: clamp(1rem, 3.5vw, 1.3rem);
  }
}
</style>
