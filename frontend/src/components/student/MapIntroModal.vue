<template>
  <Teleport to="body">
    <div
      v-if="modelValue"
      class="map-intro-modal__backdrop"
      role="dialog"
      aria-modal="true"
      :aria-labelledby="titleId"
      @click.self="emit('dismiss')"
    >
      <section class="map-intro-modal">
        <button class="map-intro-modal__close" type="button" aria-label="Lukk" @click="emit('dismiss')">
          ✕
        </button>

        <div class="map-intro-modal__dots" role="tablist" aria-label="Slide-fremdrift">
          <button
            v-for="(slide, index) in slides"
            :key="slide.title"
            class="map-intro-modal__dot"
            :class="{ 'map-intro-modal__dot--active': index === currentSlide }"
            :aria-selected="index === currentSlide"
            :aria-label="`Slide ${index + 1}`"
            role="tab"
            type="button"
            @click="goTo(index)"
          />
        </div>

        <article class="map-intro-modal__slide" aria-live="polite">
          <div class="map-intro-modal__emoji" aria-hidden="true">
            {{ slides[currentSlide].emoji }}
          </div>

          <h2 :id="titleId" class="map-intro-modal__title">{{ slides[currentSlide].title }}</h2>

          <p class="map-intro-modal__body">
            {{ slides[currentSlide].body }}
          </p>
        </article>

        <footer class="map-intro-modal__footer">
          <button
            class="map-intro-modal__nav map-intro-modal__nav--secondary"
            type="button"
            :disabled="currentSlide === 0"
            @click="prev"
          >
            ← Tilbake
          </button>

          <button
            v-if="currentSlide < slides.length - 1"
            class="map-intro-modal__nav map-intro-modal__nav--primary"
            type="button"
            @click="next"
          >
            Neste →
          </button>
          <button
            v-else
            class="map-intro-modal__nav map-intro-modal__nav--primary"
            type="button"
            @click="emit('dismiss')"
          >
            Start etterforskningen →
          </button>
        </footer>
      </section>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['dismiss'])

const slides = [
  {
    emoji: '🕵️‍♀️',
    title: 'Ordføreren trenger hjelp',
    body: 'Pengene som skulle bygge den nye idrettsparken er stjålet fra prosjektkontoen til ordføreren.',
  },
  {
    emoji: '🌀',
    title: 'Internettbyen peker i alle retninger',
    body: 'Hele Internettbyen peker i forskjellige retninger, men ingen vet hva som egentlig skjedde.',
  },
  {
    emoji: '🔎',
    title: 'Du må følge sporene',
    body: 'Som nettdetektiv må du utforske kartet, løse oppgaver og samle informasjon for å finne ut hvem som står bak.',
  },
  {
    emoji: '🗝️',
    title: 'Nye områder låses opp underveis',
    body: 'Noen områder er låst, du må fullføre oppgaver for å komme deg videre.',
  },
]

const currentSlide = ref(0)
const titleId = 'map-intro-modal-title'

watch(
  () => props.modelValue,
  (isOpen) => {
    if (isOpen) {
      currentSlide.value = 0
    }
  }
)

function goTo(index) {
  currentSlide.value = index
}

function next() {
  if (currentSlide.value < slides.length - 1) {
    currentSlide.value += 1
  }
}

function prev() {
  if (currentSlide.value > 0) {
    currentSlide.value -= 1
  }
}
</script>

<style scoped>
.map-intro-modal__backdrop {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: clamp(1rem, 3vw, 2rem);
  background: var(--color-backdrop);
  backdrop-filter: blur(2px);
}

.map-intro-modal {
  position: relative;
  width: min(100%, 45rem);
  min-height: min(29rem, 78vh);
  display: grid;
  grid-template-rows: auto 1fr auto;
  gap: clamp(1rem, 2.5vw, 1.5rem);
  padding: clamp(1.4rem, 3vw, 2.15rem);
  border-radius: 1.35rem;
  background: var(--color-surface);
  box-shadow: 0 18px 50px color-mix(in srgb, var(--color-text) 16%, transparent);
}

.map-intro-modal__close {
  position: absolute;
  top: 1.25rem;
  right: 1.25rem;
  width: 2.5rem;
  height: 2.5rem;
  border: none;
  border-radius: 999px;
  background: transparent;
  color: var(--color-text-muted);
  font-size: 1.125rem;
  cursor: pointer;
  transition: background-color var(--transition-fast), color var(--transition-fast);
}

.map-intro-modal__close:hover {
  background: color-mix(in srgb, var(--color-border-strong) 24%, transparent);
  color: var(--color-text);
}

.map-intro-modal__dots {
  display: flex;
  justify-content: center;
  gap: 0.85rem;
  padding-top: 0.15rem;
}

.map-intro-modal__dot {
  width: 1rem;
  height: 1rem;
  border: none;
  border-radius: 999px;
  background: var(--color-border);
  cursor: pointer;
  transition: transform var(--transition-fast), background-color var(--transition-fast);
}

.map-intro-modal__dot:hover {
  transform: scale(1.08);
}

.map-intro-modal__dot--active {
  background: var(--color-primary);
}

.map-intro-modal__slide {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  padding: clamp(0.25rem, 1vw, 0.75rem) clamp(0.25rem, 1.5vw, 0.85rem);
  text-align: center;
}

.map-intro-modal__emoji {
  font-size: clamp(3rem, 5vw, 4rem);
  line-height: 1;
}

.map-intro-modal__title {
  margin: 0;
  color: var(--color-primary-dark);
  font-size: clamp(1.7rem, 3vw, 2.4rem);
  font-weight: 800;
  line-height: 1.1;
}

.map-intro-modal__body {
  max-width: 32rem;
  margin: 0;
  color: var(--color-text);
  font-size: clamp(1rem, 1.7vw, 1.25rem);
  line-height: 1.55;
}

.map-intro-modal__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding-top: 1.15rem;
  border-top: 1px solid var(--color-border);
}

.map-intro-modal__nav {
  min-width: 10.25rem;
  min-height: 3.35rem;
  padding: 0.78rem 1.45rem;
  border-radius: 999px;
  border: 2px solid transparent;
  font: inherit;
  font-size: 1rem;
  font-weight: 700;
  cursor: pointer;
  transition:
    transform var(--transition-fast),
    box-shadow var(--transition-fast),
    background-color var(--transition-fast),
    color var(--transition-fast),
    border-color var(--transition-fast);
}

.map-intro-modal__nav:hover:not(:disabled) {
  transform: translateY(-1px);
}

.map-intro-modal__nav:disabled {
  cursor: not-allowed;
}

.map-intro-modal__nav--secondary {
  background: var(--color-surface);
  color: var(--color-border-strong);
  border-color: var(--color-border);
}

.map-intro-modal__nav--secondary:hover:not(:disabled) {
  color: var(--color-text-muted);
  border-color: var(--color-border-strong);
}

.map-intro-modal__nav--primary {
  background: var(--color-primary);
  color: var(--color-text-on-dark);
  box-shadow: 0 12px 26px color-mix(in srgb, var(--color-primary) 22%, transparent);
}

.map-intro-modal__nav--primary:hover:not(:disabled) {
  background: var(--color-primary-dark);
  box-shadow: 0 16px 30px color-mix(in srgb, var(--color-primary) 28%, transparent);
}

@media (max-width: 640px) {
  .map-intro-modal {
    min-height: auto;
    padding: 1.25rem;
    gap: 1rem;
    border-radius: 1.25rem;
  }

  .map-intro-modal__dots {
    gap: 0.65rem;
  }

  .map-intro-modal__dot {
    width: 0.85rem;
    height: 0.85rem;
  }

  .map-intro-modal__slide {
    gap: 1rem;
  }

  .map-intro-modal__footer {
    flex-direction: column-reverse;
    align-items: stretch;
  }

  .map-intro-modal__nav {
    width: 100%;
    min-width: 0;
  }
}
</style>
