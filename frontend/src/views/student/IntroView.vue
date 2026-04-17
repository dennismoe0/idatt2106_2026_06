<template>
  <main class="intro-page">
    <button class="intro-page__skip" @click="skip">
      Hopp over
    </button>

    <section class="intro-carousel">
      <div class="intro-carousel__dots" role="tablist" aria-label="Slide-fremdrift">
        <button
          v-for="(_, i) in slides"
          :key="i"
          class="intro-carousel__dot"
          :class="{ 'intro-carousel__dot--active': i === current }"
          :aria-selected="i === current"
          :aria-label="`Slide ${i + 1}`"
          role="tab"
          @click="goTo(i)"
        />
      </div>

      <div class="intro-carousel__track" aria-live="polite">
        <article class="intro-slide">
          <div class="intro-slide__illustration" aria-hidden="true">
            {{ slides[current].emoji }}
          </div>

          <h1 class="intro-slide__title">{{ slides[current].title }}</h1>

          <p
            v-for="(line, i) in slides[current].body"
            :key="i"
            class="intro-slide__body"
          >
            {{ line }}
          </p>

          <ul v-if="slides[current].bullets" class="intro-slide__bullets">
            <li
              v-for="(bullet, i) in slides[current].bullets"
              :key="i"
              class="intro-slide__bullet"
            >
              <span class="intro-slide__bullet-icon" aria-hidden="true">{{ bullet.icon }}</span>
              <span>{{ bullet.text }}</span>
            </li>
          </ul>
        </article>
      </div>

      <!-- Navigation -->
      <nav class="intro-carousel__nav" aria-label="Karuselnavigasjon">
        <button
          class="intro-carousel__prev"
          :disabled="current === 0"
          @click="prev"
          aria-label="Forrige slide"
        >
          ← Tilbake
        </button>

        <button
          v-if="current < slides.length - 1"
          class="intro-carousel__next"
          @click="next"
          aria-label="Neste slide"
        >
          Neste →
        </button>
        <button
          v-else
          class="intro-carousel__start"
          @click="startGame"
        >
          Start oppdraget 🕵️
        </button>
      </nav>
    </section>
  </main>
</template>

<script setup>

import { ref } from 'vue'
import { useRouter, onBeforeRouteLeave } from 'vue-router'

const router = useRouter()

const current = ref(0)

const slides = [
  {
    emoji: '🕵️',
    title: 'Du er en detektiv!',
    body: [
      'Velkommen til Nettdetektivene! Du har blitt rekruttert til en hemmelig organisasjon som beskytter folk på internett.',
      'Som detektiv er det din jobb å avsløre lureri, falske nyheter og digitale feller.'
    ]
  },
  {
    emoji: '🦹',
    title: 'Hva er oppdraget?',
    body: [
      'En mystisk datatyv sprer falsk informasjon og prøver å lure folk i Internettbyen. Du må stoppe han!'
    ],
    bullets: [
      { icon: '📰', text: 'Avsløre falske nyheter og clickbait' },
      { icon: '📧', text: 'Gjenkjenne farlige e-poster' },
      { icon: '🔒', text: 'Lage trygge passord' },
      { icon: '🛒', text: 'Skille mellom trygge og useriøse nettsider' }
    ]
  },
  {
    emoji: '🗺️',
    title: 'Slik fungerer kartet',
    body: [
      'Kartet viser steder i Internettbyen. Hvert stoppested har oppgaver du må løse for å komme videre.'
    ],
    bullets: [
      { icon: '🟢', text: 'Grønne stoppesteder er klare til å utforskes' },
      { icon: '🔒', text: 'Låste stoppesteder åpnes når du fullfører det forrige' },
      { icon: '🏅', text: 'Fullfør alle oppgavene på et stopp og få en medalje!' }
    ]
  },
  {
    emoji: '🚀',
    title: 'Klar for oppdraget?',
    body: [
      'Husk: en god detektiv stiller alltid spørsmål, sjekker kildene sine og tenker seg om to ganger.',
      'Lykke til, detektiv!'
    ]
  }
]

function goTo(index) {
  current.value = index
  console.log('[IntroView] Navigated to slide', index + 1)
}

function next() {
  if (current.value < slides.length - 1) {
    current.value += 1
    console.log('[IntroView] Advanced to slide', current.value + 1)
  }
}

function prev() {
  if (current.value > 0) {
    current.value -= 1
    console.log('[IntroView] Went back to slide', current.value + 1)
  }
}

onBeforeRouteLeave(() => {
  localStorage.setItem('hasSeenIntro', 'true')
})

function skip() {
  console.log('[IntroView] Skipped intro')
  localStorage.setItem('hasSeenIntro', 'true')
  router.replace({ name: 'Home' })
}

function startGame() {
  console.log('[IntroView] Intro completed — setting hasSeenIntro')
  localStorage.setItem('hasSeenIntro', 'true')
  router.replace({ name: 'Home' })
}
</script>

<style scoped>
.intro-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-4);
  background: var(--color-bg);
  position: relative;
  font-family: var(--font-sans),sans-serif;
}

.intro-page__skip {
  position: absolute;
  top: var(--space-4);
  right: var(--space-4);
  background: none;
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-full);
  padding: var(--space-2) var(--space-4);
  font-family: var(--font-sans),sans-serif;
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  color: var(--color-text-muted);
  cursor: pointer;
  transition: border-color var(--transition-fast), color var(--transition-fast);
}

.intro-page__skip:hover {
  border-color: var(--color-border-strong);
  color: var(--color-text);
}

.intro-carousel {
  width: min(100%, 520px);
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
  padding: var(--space-8);
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

.intro-carousel__dots {
  display: flex;
  justify-content: center;
  gap: var(--space-2);
}

.intro-carousel__dot {
  width: 10px;
  height: 10px;
  border-radius: var(--radius-full);
  border: none;
  background: var(--color-border);
  cursor: pointer;
  padding: 0;
  transition: background var(--transition-fast), transform var(--transition-fast);
}

.intro-carousel__dot--active {
  background: var(--color-primary);
  transform: scale(1.3);
}

.intro-carousel__track {
  min-height: 320px;
  display: flex;
  align-items: flex-start;
}

.intro-slide {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.intro-slide__illustration {
  font-size: 4rem;
  line-height: 1;
  text-align: center;
  margin-bottom: var(--space-2);
}

.intro-slide__title {
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  color: var(--color-heading);
  text-align: center;
  margin: 0;
}

.intro-slide__body {
  font-size: var(--text-base);
  color: var(--color-text);
  line-height: 1.6;
  text-align: center;
  margin: 0;
}

.intro-slide__bullets {
  list-style: none;
  margin: var(--space-2) 0 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.intro-slide__bullet {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  background: var(--color-surface-soft);
  border-radius: var(--radius-md);
  padding: var(--space-2) var(--space-3);
  font-size: var(--text-sm);
  color: var(--color-text);
  font-weight: var(--font-semibold);
}

.intro-slide__bullet-icon {
  font-size: var(--text-lg);
  flex-shrink: 0;
}

.intro-carousel__nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  padding-top: var(--space-2);
  border-top: 1px solid var(--color-border);
}

.intro-carousel__prev {
  background: none;
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-full);
  padding: var(--space-2) var(--space-4);
  font-family: var(--font-sans),sans-serif;
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  color: var(--color-text-muted);
  cursor: pointer;
  transition: border-color var(--transition-fast), color var(--transition-fast);
}

.intro-carousel__prev:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

.intro-carousel__prev:not(:disabled):hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.intro-carousel__next,
.intro-carousel__start {
  border: none;
  border-radius: var(--radius-full);
  padding: var(--space-2) var(--space-6);
  font-family: var(--font-sans),sans-serif;
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
  cursor: pointer;
  transition: background var(--transition-fast), transform var(--transition-fast);
}

.intro-carousel__next {
  background: var(--color-primary);
  color: var(--color-text-on-dark);
  box-shadow: 0 4px 12px var(--color-btn-primary-shadow);
}

.intro-carousel__next:hover {
  background: var(--color-btn-primary-hover);
  transform: translateY(-1px);
}

.intro-carousel__start {
  background: var(--color-accent);
  color: var(--color-text-on-dark);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.intro-carousel__start:hover {
  background: var(--color-accent-dark);
  transform: translateY(-1px);
}

@media (max-width: 480px) {
  .intro-carousel {
    padding: var(--space-6);
  }

  .intro-slide__illustration {
    font-size: 3rem;
  }

  .intro-slide__title {
    font-size: var(--text-xl);
  }
}
</style>
