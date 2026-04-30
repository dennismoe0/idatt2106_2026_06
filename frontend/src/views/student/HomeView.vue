<template>
  <div class="home">

    <!-- Fixed HUD: sound controls + logout, always on top regardless of board scale -->
    <div class="home__hud">
      <SoundControls />
      <button class="home__logout" @click="handleLogout">Logg ut</button>
    </div>

    <!-- Standing detective easel — scaled to fill viewport via JS -->
    <div id="home-easel" class="home__easel">

      <!-- Top rail -->
      <div class="home__rail">
        <span class="home__rail-label">Detektiv {{ studentName }} · Sak #042 · Nettdetektivene</span>
        <span class="home__knob" aria-hidden="true"></span>
        <span class="home__knob" aria-hidden="true"></span>
      </div>

      <!-- Wooden frame -->
      <div class="home__frame">
        <main class="home__board" aria-label="Studentmeny">

          <!-- Red threads — z-index 3, rendered above cards -->
          <svg class="home__threads" viewBox="0 0 784 425" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
            <!-- hero(391,166) → each card pin -->
            <path d="M 391 166 Q 240 122 99 24"   fill="none" stroke="#9B2226" stroke-width="1.5" opacity=".85" stroke-linecap="round"/>
            <path d="M 391 166 Q 392 102 392 18"  fill="none" stroke="#9B2226" stroke-width="1.5" opacity=".85" stroke-linecap="round"/>
            <path d="M 391 166 Q 540 120 679 24"  fill="none" stroke="#9B2226" stroke-width="1.5" opacity=".85" stroke-linecap="round"/>
            <path d="M 391 166 Q 245 172 100 150" fill="none" stroke="#9B2226" stroke-width="1.5" opacity=".85" stroke-linecap="round"/>
            <path d="M 391 166 Q 540 172 683 146" fill="none" stroke="#9B2226" stroke-width="1.5" opacity=".85" stroke-linecap="round"/>
            <path d="M 391 166 Q 240 232 97 276"  fill="none" stroke="#9B2226" stroke-width="1.5" opacity=".85" stroke-linecap="round"/>
            <path d="M 391 166 Q 540 230 683 272" fill="none" stroke="#9B2226" stroke-width="1.5" opacity=".85" stroke-linecap="round"/>
            <path d="M 391 166 Q 308 258 239 310" fill="none" stroke="#9B2226" stroke-width="1.5" opacity=".85" stroke-linecap="round"/>
            <path d="M 391 166 Q 470 256 539 306" fill="none" stroke="#9B2226" stroke-width="1.5" opacity=".85" stroke-linecap="round"/>
            <!-- card pin dots -->
            <circle cx="99"  cy="24"  r="4.5" fill="#c02020" opacity=".92"/>
            <circle cx="392" cy="18"  r="4.5" fill="#c02020" opacity=".92"/>
            <circle cx="679" cy="24"  r="4.5" fill="#c02020" opacity=".92"/>
            <circle cx="100" cy="150" r="4.5" fill="#c02020" opacity=".92"/>
            <circle cx="683" cy="146" r="4.5" fill="#c02020" opacity=".92"/>
            <circle cx="97"  cy="276" r="4.5" fill="#c02020" opacity=".92"/>
            <circle cx="683" cy="272" r="4.5" fill="#c02020" opacity=".92"/>
            <circle cx="239" cy="310" r="4.5" fill="#c02020" opacity=".92"/>
            <circle cx="539" cy="306" r="4.5" fill="#c02020" opacity=".92"/>
            <!-- gold hero pin -->
            <circle cx="391" cy="166" r="8.5" fill="#EFB45C" stroke="#a86a06" stroke-width="1.4" opacity=".95"/>
            <circle cx="389" cy="164" r="3.2" fill="#fff8cc" opacity=".65"/>
          </svg>

          <!-- Hero: Til kartet (gold pin, outside clip-path via .home__hw wrapper) -->
          <div class="home__hw" style="left:317px;top:168px;transform:rotate(-.4deg);">
            <RouterLink :to="mapEntryRoute" class="home__hero">
              <span class="home__badge" aria-hidden="true">AKTIV SAK</span>
              <span class="home__h-icon" aria-hidden="true">🗺️</span>
              <span class="home__h-title">Til kartet</span>
              <span class="home__h-sub">Fortsett etterforskningen →</span>
            </RouterLink>
            <span class="home__hpin" aria-hidden="true"></span>
          </div>

          <!-- Nav cards (pin is AFTER .home__card in DOM → renders on top, never clipped) -->
          <div
            v-for="card in navCards"
            :key="card.key"
            class="home__cw"
            :style="card.wrapStyle"
          >
            <RouterLink
              :to="card.route"
              class="home__card"
              :style="card.cardStyle"
              :aria-label="card.title"
            >
              <span class="home__c-icon" aria-hidden="true">{{ card.icon }}</span>
              <span class="home__c-label">{{ card.title }}</span>
            </RouterLink>
            <span class="home__ipin" aria-hidden="true"></span>
          </div>

        </main>
      </div>

      <!-- Easel legs (go off-screen at bottom — board fills viewport) -->
      <svg class="home__legs" viewBox="0 0 820 130" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
        <defs>
          <linearGradient id="hv-lg" x1="0%" y1="0%" x2="100%" y2="0%">
            <stop offset="0%"   stop-color="#5a2c10"/>
            <stop offset="40%"  stop-color="#3a1808"/>
            <stop offset="100%" stop-color="#2a1005"/>
          </linearGradient>
          <linearGradient id="hv-bg" x1="0%" y1="0%" x2="0%" y2="100%">
            <stop offset="0%"   stop-color="#5a2c10"/>
            <stop offset="100%" stop-color="#2a1005"/>
          </linearGradient>
        </defs>
        <rect x="-3" y="0" width="11" height="128" rx="4" fill="url(#hv-lg)" transform="rotate(-9, 140, 0) translate(140,0)"/>
        <rect x="-3" y="0" width="11" height="128" rx="4" fill="url(#hv-lg)" transform="rotate(9, 668, 0) translate(668,0)"/>
        <rect x="405" y="0" width="8" height="90"  rx="3" fill="#2a1005" opacity=".5"/>
        <rect x="125" y="82" width="568" height="9" rx="4" fill="url(#hv-bg)"/>
        <ellipse cx="128" cy="126" rx="12" ry="5" fill="#111" opacity=".7"/>
        <ellipse cx="690" cy="126" rx="12" ry="5" fill="#111" opacity=".7"/>
        <ellipse cx="409" cy="89"  rx="8"  ry="4" fill="#111" opacity=".5"/>
      </svg>

    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useClassroomStore } from '@/stores/classroom'
import SoundControls from '@/components/common/SoundControls.vue'
import { buildMapIntroRoute, hasSeenMapIntro as getHasSeenMapIntro } from '@/utils/mapIntro'

const authStore = useAuthStore()
const classroomStore = useClassroomStore()
const router = useRouter()

const preferredMap = computed(() =>
  localStorage.getItem('mapView') === 'simple' ? 'Map' : 'WorldMap'
)
const hasSeenMapIntro = computed(() => getHasSeenMapIntro())
const mapEntryRoute = computed(() =>
  hasSeenMapIntro.value
    ? { name: preferredMap.value }
    : buildMapIntroRoute(preferredMap.value)
)
const studentName = computed(() =>
  classroomStore.displayName || formatDisplayName(authStore.email)
)

// Unique torn-edge clip-paths per card
const CP = {
  medaljer:      'polygon(0% 5%,5% 2%,11% 5%,18% 1%,26% 4%,35% 0%,44% 4%,53% 1%,62% 5%,71% 0%,80% 3%,90% 1%,100% 4%,100% 96%,93% 100%,83% 96%,73% 100%,63% 96%,52% 100%,42% 96%,31% 100%,21% 97%,11% 100%,4% 96%,0% 100%)',
  notatblokk:    'polygon(0% 4%,7% 0%,14% 4%,22% 1%,31% 5%,40% 0%,50% 4%,60% 0%,70% 4%,80% 1%,90% 4%,100% 2%,100% 97%,90% 100%,80% 97%,70% 100%,60% 96%,49% 100%,38% 97%,27% 100%,17% 96%,8% 100%,0% 97%)',
  mistenktmappe: 'polygon(0% 6%,4% 2%,10% 5%,17% 0%,25% 4%,34% 1%,43% 5%,52% 0%,62% 4%,72% 1%,82% 5%,91% 0%,100% 3%,100% 97%,95% 100%,84% 97%,74% 100%,64% 97%,54% 100%,44% 96%,33% 100%,22% 97%,12% 100%,5% 97%,0% 100%)',
  profil:        'polygon(0% 3%,6% 0%,13% 4%,21% 1%,30% 5%,39% 0%,49% 3%,59% 1%,69% 5%,79% 0%,89% 3%,100% 1%,100% 96%,91% 100%,81% 96%,71% 100%,61% 97%,51% 100%,41% 96%,31% 100%,21% 97%,12% 100%,5% 96%,0% 100%)',
  ledertavle:    'polygon(0% 5%,8% 1%,16% 5%,25% 0%,34% 4%,44% 1%,54% 5%,64% 0%,74% 4%,84% 1%,93% 5%,100% 2%,100% 97%,93% 100%,82% 96%,72% 100%,62% 97%,52% 100%,42% 96%,31% 100%,21% 97%,11% 100%,4% 97%,0% 100%)',
  butikk:        'polygon(0% 4%,5% 0%,12% 3%,20% 1%,29% 5%,38% 0%,48% 4%,58% 1%,68% 5%,77% 0%,87% 3%,96% 1%,100% 4%,100% 96%,92% 100%,81% 97%,70% 100%,59% 96%,48% 100%,37% 97%,26% 100%,16% 96%,7% 100%,0% 97%)',
  ukasmysterium: 'polygon(0% 6%,6% 2%,13% 6%,20% 1%,28% 5%,37% 0%,46% 4%,55% 1%,65% 5%,74% 0%,84% 4%,93% 1%,100% 5%,100% 98%,94% 100%,84% 97%,74% 100%,63% 97%,53% 100%,42% 97%,32% 100%,22% 97%,12% 100%,5% 97%,0% 100%)',
  sendinn:       'polygon(0% 3%,7% 0%,15% 4%,23% 0%,32% 4%,41% 1%,51% 5%,61% 0%,71% 4%,81% 1%,91% 4%,100% 0%,100% 97%,92% 100%,82% 96%,72% 100%,61% 97%,51% 100%,40% 96%,30% 100%,20% 97%,10% 100%,4% 97%,0% 100%)',
  hjelp:         'polygon(0% 5%,4% 1%,11% 4%,19% 0%,28% 4%,37% 1%,47% 5%,57% 0%,67% 4%,77% 1%,87% 5%,96% 0%,100% 3%,100% 97%,95% 100%,85% 96%,75% 100%,65% 97%,55% 100%,45% 96%,35% 100%,25% 97%,15% 100%,6% 97%,0% 100%)',
}

/*
  Card layout (board 784×425, hero at left=317 top=168 w=149):
  ipin: top=-8px h=13px → pin_center_y = card.top - 2
  pin_center_x = card.left + card.width / 2
  SVG dots match these coordinates.
*/
const navCards = [
  { key: 'medaljer',      title: 'Medaljer',         icon: '🏅', route: { name: 'Medals' },        left: 34,  top: 26,  rot: -2.5, width: 130, color: '#B45309', cp: CP.medaljer },
  { key: 'ukasmysterium', title: 'Ukas Mysterium',   icon: '🧩', route: { name: 'UkasMysterium' }, left: 322, top: 20,  rot: -1,   width: 140, color: '#7C3AED', cp: CP.ukasmysterium },
  { key: 'notatblokk',    title: 'Notatblokk',       icon: '📝', route: { name: 'Notebook' },      left: 614, top: 26,  rot:  1.8, width: 130, color: '#0E7490', cp: CP.notatblokk },
  { key: 'mistenktmappe', title: 'Mistenktmappe',    icon: '🗂️', route: { name: 'SuspectDossier' }, left: 28, top: 152, rot: -1.5, width: 145, color: '#9B2226', cp: CP.mistenktmappe },
  { key: 'hjelp',         title: 'Hjelp',            icon: '💡', route: { name: 'Help' },           left: 618, top: 148, rot: -2,   width: 130, color: '#92400E', cp: CP.hjelp },
  { key: 'ledertavle',    title: 'Ledertavle',       icon: '📊', route: { name: 'Leaderboard' },   left: 32,  top: 278, rot:  1.3, width: 130, color: '#1D4ED8', cp: CP.ledertavle },
  { key: 'profil',        title: 'Profil',           icon: '🕵️', route: { name: 'Profile' },       left: 618, top: 274, rot:  2,   width: 130, color: '#6D28D9', cp: CP.profil },
  { key: 'butikk',        title: 'Butikk',           icon: '🏪', route: { name: 'Shop' },           left: 174, top: 312, rot: -1.8, width: 130, color: '#065F46', cp: CP.butikk },
  { key: 'sendinn',       title: 'Send inn mysterium', icon: '🔍', route: { name: 'SendInn' },      left: 474, top: 308, rot:  1.5, width: 130, color: '#0F766E', cp: CP.sendinn },
].map(c => ({
  ...c,
  wrapStyle: { left: c.left + 'px', top: c.top + 'px', transform: `rotate(${c.rot}deg)` },
  cardStyle:  { '--card-bg': c.color, '--cp': c.cp, width: c.width + 'px' },
}))

function formatDisplayName(email) {
  if (!email) return 'Ukjent'
  const base = email.endsWith('@student.local')
    ? email.replace('@student.local', '')
    : email.split('@')[0]
  return (
    base.split(/[._-]+/).filter(Boolean)
      .map(p => p.charAt(0).toUpperCase() + p.slice(1))
      .join(' ') || 'Detektiv'
  )
}

function handleLogout() {
  console.log('[HomeView] Student logout')
  authStore.logout()
  router.push({ name: 'StudentLogin' })
}

function scaleEasel() {
  const el = document.getElementById('home-easel')
  if (!el) return
  // Design dimensions: 820px wide, 489px tall (rail+frame+board — legs go off-screen)
  const s = Math.min(
    window.innerWidth  * 0.92 / 820,
    window.innerHeight * 0.93 / 489
  )
  el.style.transform = `scale(${s})`
  console.log(`[HomeView] Easel scale: ${s.toFixed(3)} (${window.innerWidth}×${window.innerHeight})`)
}

onMounted(() => {
  if (!localStorage.getItem('hasSeenIntro')) {
    console.log('[HomeView] First visit — redirecting to intro')
    router.replace({ name: 'Intro' })
    return
  }
  console.log('[HomeView] Corkboard loaded for:', studentName.value)
  scaleEasel()
  window.addEventListener('resize', scaleEasel)
})

onUnmounted(() => {
  window.removeEventListener('resize', scaleEasel)
})
</script>

<style scoped>
/* ── Full-screen room ── */
.home {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  display: flex;
  justify-content: center;
  padding-top: 2.5vh;

  /* Office room photo as background */
  background: url('/office_background.webp') center center / cover no-repeat;
}

/* ── HUD overlay (sound + logout) — fixed, above the scaled easel ── */
.home__hud {
  position: fixed;
  top: 1vh;
  right: 1.5vw;
  z-index: 100;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.home__logout {
  background: rgba(30, 14, 4, 0.72);
  border: 1px solid rgba(239, 180, 92, 0.35);
  color: rgba(254, 243, 199, 0.75);
  font-size: 0.72rem;
  font-family: inherit;
  font-weight: 500;
  padding: 0.35rem 0.85rem;
  border-radius: 4px;
  cursor: pointer;
  backdrop-filter: blur(4px);
  transition: color 0.15s, border-color 0.15s;
}
.home__logout:hover {
  color: #FEF3C7;
  border-color: #EFB45C;
}

/* ── Easel — fixed 820px design width, scaled via JS ── */
.home__easel {
  flex-shrink: 0;
  transform-origin: top center;
  display: flex;
  flex-direction: column;
  align-items: center;
  filter: drop-shadow(0 40px 80px rgba(0, 0, 0, 0.85))
          drop-shadow(0 0 120px rgba(0, 0, 0, 0.5));
}

/* ── Top rail ── */
.home__rail {
  width: 820px;
  height: 28px;
  background: linear-gradient(to bottom, #5c2e0e, #3a1808 45%, #2e1205);
  border-radius: 4px 4px 0 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: inset 0 1px 2px rgba(255, 180, 80, 0.15);
}
.home__rail-label {
  font-family: 'Special Elite', 'Courier New', monospace;
  font-size: 10px;
  letter-spacing: 0.22em;
  text-transform: uppercase;
  color: rgba(255, 200, 120, 0.5);
}
.home__knob {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: radial-gradient(circle at 38% 34%, #6a3a18, #2a1005);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.6), inset 0 1px 1px rgba(255, 180, 80, 0.2);
}

/* ── Wooden frame ── */
.home__frame {
  width: 820px;
  padding: 18px;
  background: linear-gradient(160deg, #6a3010 0%, #3a1808 25%, #4e2410 55%, #2a1205 78%, #5a2c10 100%);
  box-shadow:
    inset 2px  2px 4px rgba(255, 160, 60, 0.08),
    inset -2px -2px 4px rgba(0, 0, 0, 0.5);
  position: relative;
}
.home__frame::before,
.home__frame::after {
  content: '';
  position: absolute;
  width: 22px;
  height: 22px;
  background: rgba(0, 0, 0, 0.35);
  border-radius: 2px;
}
.home__frame::before { top: 6px; left: 6px; }
.home__frame::after  { top: 6px; right: 6px; }

/* ── Cork board ── */
.home__board {
  position: relative;
  width: 784px;
  height: 425px;
  background-color: #A87230;
  background-image:
    repeating-linear-gradient(0deg,  rgba(0,0,0,.042) 0, rgba(0,0,0,.042) 1px, transparent 1px, transparent 5px),
    repeating-linear-gradient(90deg, rgba(0,0,0,.028) 0, rgba(0,0,0,.028) 1px, transparent 1px, transparent 8px),
    radial-gradient(ellipse at 18% 22%, rgba(215,172,72,.38) 0%, transparent 50%),
    radial-gradient(ellipse at 82% 78%, rgba(95,52,8,.38)    0%, transparent 50%);
  box-shadow:
    inset 0 0 0 6px  rgba(90, 50, 16, 0.88),
    inset 0 0 0 10px rgba(56, 30,  8, 0.92);
  overflow: hidden;
}

/* ── SVG threads — behind cards ── */
.home__threads {
  position: absolute;
  top: 0; left: 0;
  width: 784px;
  height: 425px;
  z-index: 0;
  pointer-events: none;
}

/* ── Card wrapper — positions + rotates the pair (card + pin) ── */
.home__cw {
  position: absolute;
  z-index: 1;
  cursor: pointer;
  transform-origin: top center;
}
.home__cw:hover { z-index: 5; }
.home__cw:hover .home__card {
  filter: drop-shadow(4px 8px 20px rgba(0, 0, 0, 0.78)) !important;
  transform: scale(1.08);
}
.home__cw:focus-within { z-index: 5; }
.home__cw:focus-within .home__card {
  box-shadow: 0 0 0 3px rgba(239, 180, 92, 0.85);
}

/* ── Card visual (clip-path lives here; pin does NOT — avoids clipping) ── */
.home__card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  /* width set inline via cardStyle */
  height: 95px;
  gap: 5px;
  padding: 8px 10px 12px;
  text-decoration: none;
  background: var(--card-bg, #B45309);
  background-image: repeating-linear-gradient(
    180deg,
    transparent 0, transparent 14px,
    rgba(255, 255, 255, 0.10) 14px, rgba(255, 255, 255, 0.10) 15px
  );
  color: #fff;
  font-family: 'Special Elite', 'Courier New', monospace;
  clip-path: var(--cp);
  filter: drop-shadow(2px 4px 9px rgba(0, 0, 0, 0.62));
  transition: filter 0.15s, transform 0.15s;
  outline: none;
}

/* ── Red pushpin — sibling AFTER .home__card, never clipped ── */
.home__ipin {
  position: absolute;
  top: -8px;
  left: 50%;
  transform: translateX(-50%);
  width: 13px;
  height: 13px;
  border-radius: 50%;
  z-index: 2;
  background: radial-gradient(circle at 38% 34%, #e03030, #7a1818);
  box-shadow: 0 2px 5px rgba(0,0,0,.65), inset 0 1px 2px rgba(255,255,255,.2);
  pointer-events: none;
}

.home__c-icon  { font-size: 28px; line-height: 1; display: block; }
.home__c-label {
  font-size: 11px;
  font-weight: bold;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  line-height: 1.2;
  text-align: center;
  color: rgba(255, 255, 255, 0.9);
  display: block;
}

/* ── Hero wrapper ── */
.home__hw {
  position: absolute;
  z-index: 4;
  cursor: pointer;
  transform-origin: top center;
}
.home__hw:hover { z-index: 10; }
.home__hw:hover .home__hero {
  filter: drop-shadow(4px 9px 22px rgba(0, 0, 0, 0.8)) !important;
  transform: scale(1.06);
}
.home__hw:focus-within .home__hero {
  box-shadow: 0 0 0 3px rgba(239, 180, 92, 0.85);
}

/* ── Hero card ── */
.home__hero {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;
  padding: 12px 16px;
  width: 149px;
  text-align: center;
  text-decoration: none;
  background: #B91C1C;
  background-image: repeating-linear-gradient(
    180deg,
    transparent 0, transparent 14px,
    rgba(255, 255, 255, 0.09) 14px, rgba(255, 255, 255, 0.09) 15px
  );
  color: #fff;
  font-family: 'Special Elite', 'Courier New', monospace;
  clip-path: polygon(
    0% 3%,4% 0%,11% 3%,19% 0%,28% 3%,38% 0%,48% 3%,58% 0%,68% 3%,78% 0%,88% 3%,96% 0%,100% 3%,
    100% 97%,94% 100%,84% 97%,74% 100%,63% 97%,53% 100%,42% 97%,32% 100%,21% 97%,11% 100%,4% 97%,0% 100%
  );
  filter: drop-shadow(2px 4px 12px rgba(0, 0, 0, 0.65));
  transition: filter 0.15s, transform 0.15s;
  outline: none;
}

/* ── Gold hero pin — sibling AFTER .home__hero ── */
.home__hpin {
  position: absolute;
  top: -9px;
  left: 50%;
  transform: translateX(-50%);
  width: 15px;
  height: 15px;
  border-radius: 50%;
  z-index: 2;
  background: radial-gradient(circle at 38% 34%, #ffe066, #c07c08);
  box-shadow: 0 2px 5px rgba(0,0,0,.75), inset 0 1px 1px rgba(255,255,255,.3);
  pointer-events: none;
}

.home__badge {
  display: inline-block;
  background: rgba(0, 0, 0, 0.3);
  color: rgba(255, 255, 255, 0.75);
  font-size: 6px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  padding: 2px 7px;
  border-radius: 1px;
  margin-bottom: 5px;
  font-family: monospace;
  border: 1px solid rgba(255, 255, 255, 0.2);
}
.home__h-icon  { font-size: 25px; display: block; margin-bottom: 3px; }
.home__h-title { font-size: 12px; font-weight: bold; display: block; text-transform: uppercase; letter-spacing: 0.04em; }
.home__h-sub   { font-size: 7px; display: block; text-transform: uppercase; letter-spacing: 0.06em; color: rgba(255,255,255,.82); margin-top: 3px; }

/* ── Easel legs ── */
.home__legs {
  display: block;
  width: 820px;
  height: 130px;
  overflow: visible;
  margin-top: -2px;
}
</style>
