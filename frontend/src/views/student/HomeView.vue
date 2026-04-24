<template>
  <div class="home">

    <!-- Wooden-frame header -->
    <header class="home__header">
      <div class="home__identity">
        <p class="home__eyebrow">Nettdetektivene · Saksmappe</p>
        <h1 class="home__name">Detektiv {{ studentName }}</h1>
      </div>
      <button class="home__logout" @click="handleLogout">Logg ut</button>
    </header>

    <!-- Cork board -->
    <div class="home__board" role="main">

      <!-- Decorative red yarn / string -->
      <svg class="home__yarn" viewBox="0 0 1000 520" preserveAspectRatio="none" aria-hidden="true">
        <path d="M 80 55 Q 280 100 480 65 Q 660 30 850 110"
              fill="none" stroke="#9B2226" stroke-width="1.8" opacity="0.45" stroke-linecap="round"/>
        <path d="M 60 420 Q 320 360 580 400 Q 780 440 960 360"
              fill="none" stroke="#9B2226" stroke-width="1.2" opacity="0.3" stroke-linecap="round"/>
        <circle cx="80"  cy="55"  r="3.5" fill="#9B2226" opacity="0.55"/>
        <circle cx="480" cy="65"  r="3.5" fill="#9B2226" opacity="0.55"/>
        <circle cx="850" cy="110" r="3.5" fill="#9B2226" opacity="0.55"/>
        <circle cx="60"  cy="420" r="2.5" fill="#9B2226" opacity="0.4"/>
        <circle cx="580" cy="400" r="2.5" fill="#9B2226" opacity="0.4"/>
      </svg>

      <nav class="home__grid" aria-label="Studentmeny">

        <!-- Hero: Map -->
        <RouterLink :to="{ name: preferredMap }" class="home__note home__note--hero">
          <span class="home__pin home__pin--gold" aria-hidden="true"></span>
          <span class="home__note-badge" aria-hidden="true">AKTIV SAK</span>
          <span class="home__note-icon" aria-hidden="true">🗺️</span>
          <div class="home__note-body">
            <h2 class="home__note-title">Til kartet</h2>
            <p class="home__note-sub">Fortsett etterforskningen →</p>
          </div>
        </RouterLink>

        <!-- Active secondary notes -->
        <RouterLink
          v-for="card in activeCards"
          :key="card.title"
          :to="card.route"
          class="home__note"
          :style="{ '--note-bg': card.color }"
        >
          <span class="home__pin" aria-hidden="true"></span>
          <span class="home__note-icon" aria-hidden="true">{{ card.icon }}</span>
          <h2 class="home__note-title">{{ card.title }}</h2>
        </RouterLink>

        <!-- Locked notes -->
        <div
          v-for="card in lockedCards"
          :key="card.title"
          class="home__note home__note--locked"
          role="article"
          :aria-label="`${card.title} — kommer snart`"
        >
          <span class="home__pin home__pin--dark" aria-hidden="true"></span>
          <span class="home__note-icon" aria-hidden="true">{{ card.icon }}</span>
          <h2 class="home__note-title">{{ card.title }}</h2>
          <p class="home__note-soon">Kommer snart</p>
        </div>

      </nav>

      <ActiveMysteryWidget
        v-if="classroomStore.currentClassroomId"
        :classroom-id="Number(classroomStore.currentClassroomId)"
      />
    </div>

  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useClassroomStore } from '@/stores/classroom'
import ActiveMysteryWidget from '@/components/student/ActiveMysteryWidget.vue'

const authStore = useAuthStore()
const classroomStore = useClassroomStore()
const router = useRouter()

const preferredMap = localStorage.getItem('mapView') === 'simple' ? 'Map' : 'WorldMap'

const studentName = computed(() =>
  classroomStore.displayName || formatDisplayName(authStore.email)
)

const activeCards = [
  { title: 'Medaljer',    icon: '🏅', route: { name: 'Medals' },      color: '#B45309' },
  { title: 'Notatblokk', icon: '📝', route: { name: 'Notebook' },     color: '#0E7490' },
  { title: 'Profil',     icon: '🕵️', route: { name: 'Profile' },      color: '#6D28D9' },
  { title: 'Ledertavle', icon: '📊', route: { name: 'Leaderboard' },  color: '#1D4ED8' },
]

const lockedCards = [
  { title: 'Hjelp', icon: '💡' },
]

function formatDisplayName(email) {
  if (!email) return 'Ukjent'
  const base = email.endsWith('@student.local')
    ? email.replace('@student.local', '')
    : email.split('@')[0]
  return (
    base
      .split(/[._-]+/)
      .filter(Boolean)
      .map(p => p.charAt(0).toUpperCase() + p.slice(1))
      .join(' ') || 'Detektiv'
  )
}

function handleLogout() {
  console.log('[HomeView] Student logout')
  authStore.logout()
  router.push({ name: 'StudentLogin' })
}

onMounted(() => {
  if (!localStorage.getItem('hasSeenIntro')) {
    console.log('[HomeView] First visit — redirecting to intro')
    router.replace({ name: 'Intro' })
    return
  }
  console.log('[HomeView] Corkboard loaded for:', studentName.value)
})
</script>

<style scoped>
/* ── Detective colour palette (page-local) ───────────── */
.home {
  --cork:        #A87230;
  --cork-dark:   #7A4E1A;
  --cork-light:  #C49240;
  --wood:        #3B1F08;
  --wood-mid:    #5C3210;
  --gold:        #EFB45C;
  --red-pin:     #9B2226;
  --hero-red:    #B91C1C;

  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--wood);
}

/* ── Header — dark wood frame ────────────────────────── */
.home__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: 0.875rem 1.5rem;
  background: var(--wood);
  border-bottom: 4px solid var(--wood-mid);
  flex-shrink: 0;
}

.home__eyebrow {
  margin: 0 0 0.2rem;
  font-size: 0.625rem;
  font-weight: 700;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: var(--gold);
  opacity: 0.8;
}

.home__name {
  margin: 0;
  font-size: clamp(1.25rem, 4vw, 1.625rem);
  font-weight: 800;
  color: #FEF3C7;
  letter-spacing: -0.02em;
}

.home__logout {
  flex-shrink: 0;
  background: none;
  border: 1px solid rgba(239, 180, 92, 0.35);
  color: rgba(254, 243, 199, 0.55);
  font-size: 0.775rem;
  font-family: inherit;
  font-weight: 500;
  padding: 0.4rem 0.9rem;
  border-radius: 4px;
  cursor: pointer;
  transition: color 0.15s, border-color 0.15s;
}
.home__logout:hover {
  color: #FEF3C7;
  border-color: var(--gold);
}

/* ── Cork board ──────────────────────────────────────── */
.home__board {
  flex: 1;
  position: relative;
  padding: clamp(1.5rem, 4vw, 2.5rem) clamp(1rem, 3vw, 2rem);

  /* Cork texture */
  background-color: var(--cork);
  background-image:
    repeating-linear-gradient(
      0deg,
      rgba(0,0,0,0.045) 0,   rgba(0,0,0,0.045) 1px,
      transparent        1px, transparent        5px
    ),
    repeating-linear-gradient(
      90deg,
      rgba(0,0,0,0.03) 0,   rgba(0,0,0,0.03) 1px,
      transparent       1px, transparent       8px
    ),
    radial-gradient(ellipse at 15% 20%, rgba(210,168,70,0.45) 0%, transparent 55%),
    radial-gradient(ellipse at 80% 75%, rgba(100,60,10,0.45)  0%, transparent 55%),
    radial-gradient(ellipse at 50% 50%, rgba(176,120,50,0.2)  0%, transparent 70%);

  /* Wooden frame inset */
  box-shadow:
    inset 0 0 0 8px  var(--wood-mid),
    inset 0 0 0 12px var(--wood);
}

/* ── Yarn overlay ────────────────────────────────────── */
.home__yarn {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

/* ── Card grid ───────────────────────────────────────── */
.home__grid {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: clamp(1rem, 2.5vw, 1.75rem);
  align-items: start;
}

@media (max-width: 900px) {
  .home__grid { grid-template-columns: repeat(2, 1fr); }
}

/* ── Note (card) base ────────────────────────────────── */
.home__note {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 1.75rem 1rem 1.5rem;
  min-height: 150px;
  border-radius: 2px;

  background: var(--note-bg, #374151);
  color: #fff;
  text-decoration: none;
  text-align: center;

  /* Paper lift */
  box-shadow:
    2px 3px 8px  rgba(0,0,0,0.35),
    0   1px 2px  rgba(0,0,0,0.25),
    inset 0 0 0 1px rgba(255,255,255,0.06);

  /* Organic rotation — overridden per child below */
  transform-origin: top center;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
  will-change: transform;
}

/* Alternating rotations per slot */
.home__note:nth-child(1) { transform: rotate(-1.4deg); }
.home__note:nth-child(2) { transform: rotate(1.1deg);  }
.home__note:nth-child(3) { transform: rotate(-0.9deg); }
.home__note:nth-child(4) { transform: rotate(1.7deg);  }
.home__note:nth-child(5) { transform: rotate(-1.5deg); }
.home__note:nth-child(6) { transform: rotate(0.8deg);  }
.home__note:nth-child(7) { transform: rotate(-1.2deg); }

.home__note[href]:hover,
.home__note[href]:focus-visible {
  transform: rotate(0deg) scale(1.04) translateY(-4px) !important;
  box-shadow:
    4px 10px 24px rgba(0,0,0,0.45),
    0   2px  6px  rgba(0,0,0,0.3);
  z-index: 10;
  outline: none;
}
.home__note[href]:focus-visible {
  box-shadow:
    4px 10px 24px rgba(0,0,0,0.45),
    0 0 0 3px rgba(239,180,92,0.8);
}
.home__note[href]:active { transform: rotate(0deg) scale(0.98) !important; }

/* ── Hero note ───────────────────────────────────────── */
.home__note--hero {
  grid-column: span 2;
  flex-direction: row;
  align-items: center;
  gap: 1.25rem;
  padding: 2rem 1.75rem 2rem 2rem;
  min-height: 180px;
  text-align: left;
  background: var(--hero-red) !important;
}

@media (max-width: 900px) {
  .home__note--hero {
    grid-column: span 2;
  }
}

.home__note-badge {
  position: absolute;
  top: 0.6rem;
  right: 0.75rem;
  font-size: 0.55rem;
  font-weight: 700;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: rgba(255,255,255,0.6);
  border: 1px solid rgba(255,255,255,0.3);
  padding: 0.15rem 0.45rem;
  border-radius: 2px;
}

/* ── Push pin ────────────────────────────────────────── */
.home__pin {
  position: absolute;
  top: -9px;
  left: 50%;
  transform: translateX(-50%);
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: var(--red-pin);
  box-shadow:
    0 3px 6px rgba(0,0,0,0.55),
    inset 0 1px 2px rgba(255,255,255,0.25);
  z-index: 2;
  flex-shrink: 0;
}

.home__note--hero .home__pin {
  left: 50%;
}

.home__pin--gold {
  background: var(--gold);
  box-shadow: 0 3px 6px rgba(0,0,0,0.55), inset 0 1px 2px rgba(255,255,255,0.35);
}

.home__pin--dark {
  background: #5C5C5C;
}

/* ── Note content ────────────────────────────────────── */
.home__note-icon {
  font-size: 2.5rem;
  line-height: 1;
  flex-shrink: 0;
  filter: drop-shadow(0 1px 3px rgba(0,0,0,0.35));
}

.home__note--hero .home__note-icon {
  font-size: 3.25rem;
}

.home__note-body {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
}

.home__note-title {
  margin: 0;
  font-size: 1rem;
  font-weight: 700;
  color: #fff;
  letter-spacing: -0.01em;
  line-height: 1.2;
}

.home__note--hero .home__note-title {
  font-size: clamp(1.1rem, 3vw, 1.5rem);
}

.home__note-sub {
  margin: 0;
  font-size: 0.85rem;
  color: rgba(255,255,255,0.72);
  font-weight: 500;
}

/* ── Locked note ─────────────────────────────────────── */
.home__note--locked {
  background: #2C2420 !important;
  opacity: 0.5;
  cursor: not-allowed;
  pointer-events: none;
}

.home__note-soon {
  margin: 0;
  font-size: 0.7rem;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: rgba(255,255,255,0.4);
}
</style>
