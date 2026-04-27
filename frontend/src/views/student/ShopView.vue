<template>
  <main class="shop-view">
    <header class="shop-header">
      <BackButton label="Tilbake" />
      <div class="shop-header__title">
        <div class="shop-header__eyebrow">Detektivbyrået</div>
        <div class="shop-header__name">🏪 Utstyrslager</div>
      </div>
      <div class="shop-star-badge">
        <span>⭐</span>
        <span>{{ starBalance }} stjerner</span>
      </div>
    </header>

    <section v-if="isLoading" class="shop-state-card">
      <LoadingSpinner size="lg" />
      <p>Laster butikk...</p>
    </section>

    <section v-else-if="loadError" class="shop-state-card">
      <h2>Kunne ikke laste butikken</h2>
      <p>{{ loadError }}</p>
      <BaseButton @click="loadShop">Prøv igjen</BaseButton>
    </section>

    <div v-else class="shop-body">
      <div v-show="showScrollHint" class="shop-scroll-hint" aria-hidden="true">
        <div class="shop-scroll-hint__text">Mer å utforske</div>
        <div class="shop-scroll-hint__arrow">↓</div>
      </div>

      <!-- AVATAR SHOWCASE (left sticky column, desktop only) -->
      <div class="shop-showcase">
        <div class="shop-showcase__eyebrow">
          <span v-if="isPreviewingAny">✨ Forhåndsvisning</span>
          <span v-else>Din detektiv</span>
        </div>
        <div class="shop-showcase__stage">
          <AvatarComposer v-if="avatarStore.avatar" :selections="previewSelections" :size="200" />
          <div v-else class="shop-showcase__placeholder">?</div>
        </div>
        <p class="shop-showcase__hint">
          <span v-if="isPreviewingAny">Klikk Kjøp for å beholde</span>
          <span v-else>Klikk et plagg for å forhåndsvise</span>
        </p>
      </div>

      <!-- SHOP SECTIONS -->
      <div class="shop-sections">
      <section v-for="group in itemGroups" :key="group.key" class="shop-section">
        <div class="shop-section__inner">

          <div class="shop-rule">
            <div class="shop-rule-line"></div>
            <div class="shop-rule-label">✦ {{ group.label.toUpperCase() }} ✦</div>
            <div class="shop-rule-line-r"></div>
          </div>

          <!-- HAIR COLOR SWATCHES -->
          <template v-if="group.key === 'hairColor'">
            <div class="shop-swatches">
              <div
                v-for="item in group.items"
                :key="item.id"
                class="shop-swatch-wrap"
                :class="{ 'shop-swatch-wrap--shake': shakingId === item.id }"
              >
                <div
                  class="shop-swatch"
                  :class="{
                    'shop-swatch--owned':  item.purchased,
                    'shop-swatch--active': confirmingId === item.id,
                  }"
                  :style="{ background: item.optionValue }"
                  @click="setPreview(item)"
                >
                  <span v-if="item.purchased" class="shop-swatch__check">✓</span>
                </div>
                <template v-if="confirmingId === item.id">
                  <button type="button" class="shop-swatch__buy" :disabled="isPurchasing" @click="doPurchase(item)">Kjøp</button>
                  <button type="button" class="shop-swatch__cancel" @click="confirmingId = null">✕</button>
                </template>
                <template v-else>
                  <div
                    class="shop-swatch__price"
                    :class="{ 'shop-swatch__price--buyable': !item.purchased }"
                    @click="!item.purchased && onSwatchBuy(item)"
                  >
                    <span v-if="item.purchased" class="shop-swatch__price--owned">✓</span>
                    <span v-else-if="shakingId === item.id" class="shop-swatch__price--err">Ikke nok!</span>
                    <span v-else class="shop-swatch__price--cost">⭐ {{ item.starPrice }}</span>
                  </div>
                </template>
              </div>
            </div>
          </template>

          <!-- ITEM CARDS (hairStyle, outfit, accessory) -->
          <template v-else>
            <div class="shop-cards">
              <div
                v-for="item in group.items"
                :key="item.id"
                class="shop-card"
                :class="{
                  'shop-card--owned':  item.purchased,
                  'shop-card--locked': !item.purchased && starBalance < item.starPrice,
                  'shop-card--shake':  shakingId === item.id,
                }"
              >
                <div class="shop-corner shop-corner-tl"></div>
                <div class="shop-corner shop-corner-tr"></div>
                <div class="shop-corner shop-corner-bl"></div>
                <div class="shop-corner shop-corner-br"></div>

                <div class="shop-preview" @click="setPreview(item)">
                  <component
                    :is="previewComponentFor(item.optionType)"
                    v-bind="previewPropsFor(item)"
                  />
                  <div v-if="item.purchased" class="shop-preview__stamp">
                    <div class="shop-stamp-text">ANSKAFFET</div>
                  </div>
                </div>

                <div class="shop-item-name" @click="setPreview(item)">{{ formatOption(item.optionValue) }}</div>

                <div v-if="item.purchased" class="shop-owned-footer">✓ &nbsp;Kjøpt</div>
                <template v-else-if="confirmingId === item.id">
                  <div class="shop-confirm">
                    <button type="button" class="shop-confirm__yes" :disabled="isPurchasing" @click="doPurchase(item)">
                      Ja ⭐{{ item.starPrice }}
                    </button>
                    <button type="button" class="shop-confirm__no" @click="confirmingId = null">Avbryt</button>
                  </div>
                </template>
                <div
                  v-else
                  class="shop-ticket"
                  :class="{
                    'shop-ticket--locked': starBalance < item.starPrice,
                    'shop-ticket--shake':  shakingId === item.id,
                  }"
                  @click="onTicketClick(item)"
                >
                  <div class="shop-ticket__price">
                    <span class="shop-ticket__star">⭐</span>
                    <span class="shop-ticket__amount">{{ item.starPrice }}</span>
                  </div>
                  <div class="shop-ticket__action">
                    <template v-if="shakingId === item.id">Ikke nok!</template>
                    <template v-else>Kjøp</template>
                  </div>
                </div>
              </div>
            </div>
          </template>

        </div>
      </section>

      <p v-if="purchaseError" class="shop-feedback-error">{{ purchaseError }}</p>
      </div><!-- end shop-sections -->
    </div>
  </main>
</template>

<script setup>
import { ref, computed, reactive, onMounted, onUnmounted } from 'vue'
import { useAvatarStore } from '@/stores/avatar'
import { useGameStore }   from '@/stores/game'
import AvatarComposer  from '@/components/student/avatar/AvatarComposer.vue'
import AvatarHair      from '@/components/student/avatar/layers/AvatarHair.vue'
import AvatarOutfit    from '@/components/student/avatar/layers/AvatarOutfit.vue'
import AvatarAccessory from '@/components/student/avatar/layers/AvatarAccessory.vue'
import BackButton    from '@/components/common/BackButton.vue'
import BaseButton    from '@/components/common/BaseButton.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import { formatOption } from '@/utils/avatarOptions'

const avatarStore = useAvatarStore()
const gameStore   = useGameStore()

const isLoading       = ref(true)
const loadError       = ref('')
const confirmingId    = ref(null)
const shakingId       = ref(null)
const showScrollHint  = ref(true)

const previewOverrides  = reactive({})
const isPreviewingAny   = computed(() => Object.keys(previewOverrides).length > 0)
const AVATAR_DEFAULTS   = { gender: 'neutral', skinColor: '#FDDBB4', hairStyle: 'short', hairColor: '#1a1a1a', eyeStyle: 'round', eyeColor: '#4a3000', outfit: 'detective-coat', outfitColor: '#2563eb', accessory: 'none' }
const previewSelections = computed(() => {
  const av = avatarStore.avatar
  const base = av ? { gender: av.gender, skinColor: av.skinColor, hairStyle: av.hairStyle, hairColor: av.hairColor, eyeStyle: av.eyeStyle, eyeColor: av.eyeColor, outfit: av.outfit, outfitColor: av.outfitColor, accessory: av.accessory } : { ...AVATAR_DEFAULTS }
  return { ...base, ...previewOverrides }
})

function updateScrollHint() {
  const el = document.documentElement
  showScrollHint.value = el.scrollTop < el.scrollHeight - el.clientHeight - 80
}
onMounted(() => {
  window.addEventListener('scroll', updateScrollHint, { passive: true })
  updateScrollHint()
})
onUnmounted(() => window.removeEventListener('scroll', updateScrollHint))
const isPurchasing = ref(false)
const purchaseError = ref('')

const starBalance = computed(() => gameStore.starBalance)

const itemGroups = computed(() => {
  const items = avatarStore.shopItems
  const groups = [
    { label: 'Hårfarger', key: 'hairColor' },
    { label: 'Frisyrer',  key: 'hairStyle' },
    { label: 'Antrekk',   key: 'outfit' },
    { label: 'Tilbehør',  key: 'accessory' },
  ]
  return groups
    .map(g => ({ label: g.label, key: g.key, items: items.filter(i => i.optionType === g.key) }))
    .filter(g => g.items.length > 0)
})

function previewComponentFor(optionType) {
  if (optionType === 'hairStyle') return AvatarHair
  if (optionType === 'outfit')    return AvatarOutfit
  if (optionType === 'accessory') return AvatarAccessory
  return null
}

function previewPropsFor(item) {
  if (item.optionType === 'hairStyle') return { hairStyle: item.optionValue, hairColor: '#8B4513' }
  if (item.optionType === 'outfit')    return { outfit: item.optionValue, outfitColor: '#2563eb' }
  if (item.optionType === 'accessory') return { accessory: item.optionValue }
  return {}
}

function playErrorSound() {
  try {
    const ctx = new (window.AudioContext || window.webkitAudioContext)()
    const osc = ctx.createOscillator()
    const gain = ctx.createGain()
    osc.connect(gain)
    gain.connect(ctx.destination)
    osc.type = 'sawtooth'
    osc.frequency.value = 200
    gain.gain.setValueAtTime(0.2, ctx.currentTime)
    gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.25)
    osc.start()
    osc.stop(ctx.currentTime + 0.25)
    osc.onended = () => ctx.close()
  } catch (e) {}
}

let shakeTimer = null
function shakeItem(id) {
  if (shakeTimer) clearTimeout(shakeTimer)
  shakingId.value = id
  playErrorSound()
  shakeTimer = setTimeout(() => { if (shakingId.value === id) shakingId.value = null }, 900)
}

function setPreview(item) {
  previewOverrides[item.optionType] = item.optionValue
}

function onSwatchBuy(item) {
  if (item.purchased) return
  if (starBalance.value < item.starPrice) { shakeItem(item.id); return }
  confirmingId.value = confirmingId.value === item.id ? null : item.id
}

function onTicketClick(item) {
  if (starBalance.value < item.starPrice) { shakeItem(item.id); return }
  confirmingId.value = item.id
}

async function loadShop() {
  console.log('[ShopView] Loading shop...')
  isLoading.value = true
  loadError.value = ''
  try {
    await Promise.all([avatarStore.fetchShop(), avatarStore.fetchAvatar()])
    console.log('[ShopView] Shop loaded')
  } catch (err) {
    console.error('[ShopView] Load failed:', err)
    loadError.value = err?.response?.data?.error || 'Noe gikk galt.'
  } finally {
    isLoading.value = false
  }
}

async function doPurchase(item) {
  isPurchasing.value = true
  purchaseError.value = ''
  try {
    await avatarStore.purchaseItem(item.optionType, item.optionValue)
    gameStore.starBalance -= item.starPrice
    confirmingId.value = null
    console.log('[ShopView] Purchased', item.optionType, item.optionValue)
  } catch (err) {
    console.error('[ShopView] Purchase failed:', err)
    purchaseError.value = err?.response?.data?.error || 'Kjøp mislyktes.'
  } finally {
    isPurchasing.value = false
  }
}

loadShop()
</script>

<style scoped>
/* === BASE === */
.shop-view {
  min-height: 100vh;
  background: linear-gradient(160deg, #1a1209 0%, #251709 50%, #1a1209 100%);
  color: #f5e6c8;
}

/* === HEADER === */
.shop-header {
  position: sticky; top: 0; z-index: 10;
  display: flex; align-items: center; gap: 12px;
  padding: 10px 16px;
  background: linear-gradient(90deg, #0e0b06, #1a1007, #0e0b06);
  border-bottom: 1px solid rgba(200,160,64,.35);
  box-shadow: 0 4px 16px rgba(0,0,0,.6);
}
.shop-header__title { flex: 1; display: flex; flex-direction: column; gap: 1px; }
.shop-header__eyebrow {
  font-size: 7px; text-transform: uppercase; letter-spacing: .18em;
  color: rgba(200,160,64,.55); font-weight: 700;
}
.shop-header__name {
  font-size: 14px; font-weight: 900; color: #f5e6c8;
  text-transform: uppercase; letter-spacing: .06em;
}
.shop-star-badge {
  display: flex; align-items: center; gap: 4px;
  background: rgba(245,197,24,.1); border: 1px solid rgba(200,160,64,.4);
  border-radius: 20px; padding: 4px 12px;
  font-size: 0.8rem; font-weight: 700; color: #f5c518; white-space: nowrap;
}

/* === BODY === */
.shop-body {
  padding: 20px 16px 40px;
  max-width: 1100px; margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr;
  gap: 0;
}
@media (min-width: 800px) {
  .shop-body {
    grid-template-columns: 220px 1fr;
    gap: 28px;
    align-items: start;
    padding: 24px 20px 48px;
  }
}
@media (min-width: 1100px) {
  .shop-body { grid-template-columns: 260px 1fr; }
}

/* === SHOWCASE PANEL === */
.shop-showcase {
  display: none;
}
@media (min-width: 800px) {
  .shop-showcase {
    display: flex; flex-direction: column; align-items: center; gap: 10px;
    padding: 14px;
    background:
      repeating-linear-gradient(45deg, rgba(255,255,255,.012) 0px, rgba(255,255,255,.012) 1px, transparent 1px, transparent 7px),
      linear-gradient(160deg, #2c1c0a, #1e1206);
    border: 1px solid rgba(200,160,64,.4);
    border-radius: 3px;
    box-shadow: inset 0 1px 0 rgba(200,160,64,.12), 0 6px 20px rgba(0,0,0,.5);
    position: sticky;
    top: 52px;
  }
}
.shop-showcase__eyebrow {
  font-size: 8px; font-weight: 900; text-transform: uppercase;
  letter-spacing: .18em; color: rgba(200,160,64,.65);
  white-space: nowrap;
}
.shop-showcase__stage {
  background: linear-gradient(160deg, #2e1c0c, #3d2510);
  border: 1px solid rgba(200,160,64,.2);
  border-radius: 2px;
  display: flex; align-items: center; justify-content: center;
  width: 100%; aspect-ratio: 3/4;
  position: relative; overflow: hidden;
  box-shadow: inset 0 2px 8px rgba(0,0,0,.5);
}
.shop-showcase__stage::before {
  content: ''; position: absolute;
  top: 0; left: 0; right: 0; height: 1px;
  background: linear-gradient(90deg, transparent, rgba(200,160,64,.22), transparent);
}
.shop-showcase__placeholder {
  font-size: 48px; color: rgba(200,160,64,.2);
}
.shop-showcase__hint {
  font-size: 9px; color: rgba(200,160,64,.45); text-align: center;
  font-weight: 600; letter-spacing: .05em; margin: 0;
}

/* === SECTIONS WRAPPER === */
.shop-sections { display: flex; flex-direction: column; gap: 0; }

/* === SECTION === */
.shop-section        { display: flex; flex-direction: column; align-items: flex-start; margin-bottom: 32px; }
.shop-section__inner { width: 100%; }

/* === SCROLL HINT (right gutter, visible on wide screens only) === */
@keyframes bounce-hint {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(7px); }
}
.shop-scroll-hint {
  display: none;
  position: fixed;
  right: 24px;
  top: 50%;
  transform: translateY(-50%);
  flex-direction: column;
  align-items: center;
  gap: 10px;
  color: rgba(200,160,64,.4);
  pointer-events: none;
  z-index: 5;
}
@media (min-width: 1200px) {
  .shop-scroll-hint { display: flex; }
}
.shop-scroll-hint__text {
  font-size: 9px; font-weight: 700;
  letter-spacing: .18em; text-transform: uppercase;
  white-space: nowrap; text-align: center;
}
.shop-scroll-hint__arrow {
  font-size: 32px; line-height: 1;
  animation: bounce-hint 2s ease-in-out infinite;
}

/* === RULE HEADER === */
.shop-rule       { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; width: 100%; }
.shop-rule-line  { flex: 1; height: 1px; background: linear-gradient(90deg, transparent, rgba(200,160,64,.5)); min-width: 8px; }
.shop-rule-line-r{ flex: 1; height: 1px; background: linear-gradient(90deg, rgba(200,160,64,.5), transparent); min-width: 8px; }
.shop-rule-label {
  background: rgba(200,160,64,.08);
  border: 1px solid rgba(200,160,64,.4);
  border-radius: 2px;
  padding: 3px 10px;
  font-size: 8px; font-weight: 900;
  text-transform: uppercase; letter-spacing: .18em;
  color: #c8a040; white-space: nowrap; flex-shrink: 0;
}

/* === SHAKE ANIMATION === */
@keyframes shake {
  0%, 100% { transform: translateX(0); }
  15%  { transform: translateX(-7px); }
  35%  { transform: translateX(7px); }
  55%  { transform: translateX(-5px); }
  75%  { transform: translateX(5px); }
  90%  { transform: translateX(-2px); }
}
.shop-card--shake,
.shop-swatch-wrap--shake { animation: shake 0.4s ease-in-out; }

/* === HAIR COLOR SWATCHES === */
.shop-swatches   { display: flex; flex-wrap: wrap; gap: 12px; }
.shop-swatch-wrap{ display: flex; flex-direction: column; align-items: center; gap: 5px; min-width: 68px; }
.shop-swatch {
  width: 68px; height: 68px; border-radius: 50%;
  border: 2px solid rgba(200,160,64,.3);
  box-shadow: 0 2px 6px rgba(0,0,0,.5), inset 0 1px 0 rgba(255,255,255,.1);
  cursor: pointer; position: relative;
  display: flex; align-items: center; justify-content: center;
  background: transparent;
}
.shop-swatch--owned  { border-color: rgba(34,197,94,.5); cursor: default; }
.shop-swatch--active { border-color: rgba(200,160,64,.8); box-shadow: 0 0 0 3px rgba(200,160,64,.2); }
.shop-swatch__buy {
  width: 68px; padding: 5px 0;
  background: linear-gradient(180deg, #22c55e, #16a34a);
  color: #fff; border: none; border-radius: 2px; cursor: pointer;
  font-size: 9px; font-weight: 900; letter-spacing: .08em; text-transform: uppercase;
  box-shadow: 0 2px 0 #0f7030;
}
.shop-swatch__buy:disabled { opacity: 0.5; cursor: not-allowed; }
.shop-swatch__cancel {
  background: transparent; border: none;
  color: rgba(200,160,64,.6); font-size: 12px; cursor: pointer; padding: 0; line-height: 1;
}

/* hover states */
.shop-swatch:not(.shop-swatch--owned):hover {
  border-color: rgba(200,160,64,.7);
  box-shadow: 0 0 0 3px rgba(200,160,64,.15), 0 2px 6px rgba(0,0,0,.5);
}

/* focus visible for keyboard navigation */
.shop-swatch:focus-visible,
.shop-ticket:focus-visible,
.shop-confirm__yes:focus-visible,
.shop-confirm__no:focus-visible {
  outline: 2px solid rgba(200,160,64,.8);
  outline-offset: 2px;
}
.shop-swatch__check {
  position: absolute; bottom: -2px; right: -2px;
  width: 20px; height: 20px; border-radius: 50%;
  background: #22c55e; border: 1.5px solid #1a1209;
  font-size: 11px; color: #fff;
  display: flex; align-items: center; justify-content: center;
}
.shop-swatch__price        { font-size: 10px; }
.shop-swatch__price--owned { color: #4ade80; }
.shop-swatch__price--cost  { color: #f5c518; }
.shop-swatch__price--err   { color: #f87171; font-weight: 800; }
.shop-swatch__price--buyable { cursor: pointer; }
.shop-swatch__price--buyable:hover .shop-swatch__price--cost { color: #f5e030; }

/* === ITEM CARDS === */
.shop-cards { display: flex; flex-wrap: wrap; gap: 14px; }
.shop-card {
  width: fit-content;
  border-radius: 3px;
  padding: 10px;
  display: flex; flex-direction: column; align-items: stretch;
  background:
    repeating-linear-gradient(45deg, rgba(255,255,255,.012) 0px, rgba(255,255,255,.012) 1px, transparent 1px, transparent 7px),
    linear-gradient(160deg, #2c1c0a, #1e1206);
  border: 1px solid rgba(200,160,64,.4);
  box-shadow: inset 0 1px 0 rgba(200,160,64,.12), 0 5px 16px rgba(0,0,0,.5);
  position: relative;
}
.shop-card--owned  { border-color: rgba(34,197,94,.35); }
.shop-card--locked { /* no dimming — shake feedback on click instead */ }

/* corner brackets */
.shop-corner    { position: absolute; width: 9px; height: 9px; }
.shop-corner-tl { top:4px;    left:4px;   border-top:    1.5px solid rgba(200,160,64,.6); border-left:   1.5px solid rgba(200,160,64,.6); }
.shop-corner-tr { top:4px;    right:4px;  border-top:    1.5px solid rgba(200,160,64,.6); border-right:  1.5px solid rgba(200,160,64,.6); }
.shop-corner-bl { bottom:4px; left:4px;   border-bottom: 1.5px solid rgba(200,160,64,.6); border-left:   1.5px solid rgba(200,160,64,.6); }
.shop-corner-br { bottom:4px; right:4px;  border-bottom: 1.5px solid rgba(200,160,64,.6); border-right:  1.5px solid rgba(200,160,64,.6); }
.shop-card--owned .shop-corner-tl,
.shop-card--owned .shop-corner-tr,
.shop-card--owned .shop-corner-bl,
.shop-card--owned .shop-corner-br { border-color: rgba(34,197,94,.5); }

/* preview box */
.shop-preview {
  width: 136px; height: 181px;
  cursor: pointer;
  background: linear-gradient(160deg, #2e1c0c, #3d2510);
  border: 1px solid rgba(200,160,64,.2);
  border-radius: 2px;
  display: flex; align-items: center; justify-content: center;
  position: relative; overflow: hidden; margin-bottom: 10px;
  box-shadow: inset 0 2px 6px rgba(0,0,0,.35);
}
.shop-preview::before {
  content: ''; position: absolute;
  top: 0; left: 0; right: 0; height: 1px;
  background: linear-gradient(90deg, transparent, rgba(200,160,64,.22), transparent);
}
.shop-preview__stamp {
  position: absolute; inset: 0;
  background: rgba(8,6,3,.5);
  display: flex; align-items: center; justify-content: center;
}
.shop-stamp-text {
  border: 2px solid rgba(74,222,128,.75); border-radius: 3px;
  color: rgba(74,222,128,.9); font-size: 8px; font-weight: 900;
  letter-spacing: .12em; text-transform: uppercase;
  text-shadow: 0 0 8px rgba(74,222,128,.5);
  transform: rotate(-16deg); padding: 3px 6px; white-space: nowrap;
}

/* item name label tape */
.shop-item-name {
  font-size: 10px; color: #f0ddb8; font-weight: 800;
  letter-spacing: .1em; text-transform: uppercase;
  border-top: 1px solid rgba(200,160,64,.2);
  border-bottom: 1px solid rgba(200,160,64,.2);
  padding: 4px 0; margin-bottom: 8px;
  background: rgba(255,220,100,.03);
  text-align: center;
}

/* ticket buy button */
.shop-ticket {
  display: flex; border-radius: 2px; overflow: hidden;
  box-shadow: 0 3px 0 #7a5010, 0 4px 8px rgba(0,0,0,.5);
  cursor: pointer;
}
.shop-ticket__price {
  background: rgba(245,197,24,.15);
  border-right: 1.5px dashed rgba(200,160,64,.5);
  padding: 7px 10px;
  display: flex; align-items: center; justify-content: center; gap: 3px;
  flex-shrink: 0;
}
.shop-ticket__star   { font-size: 13px; line-height: 1; }
.shop-ticket__amount { font-size: 12px; font-weight: 900; color: #f5c518; }
.shop-ticket__action {
  flex: 1;
  background: linear-gradient(180deg, #d4a832, #a87220);
  color: #1a1209; font-size: 10px; font-weight: 900;
  letter-spacing: .1em; text-transform: uppercase;
  display: flex; align-items: center; justify-content: center; padding: 7px 10px;
}
.shop-ticket--locked { /* no dimming — shake on click instead */ }
.shop-ticket--shake .shop-ticket__action {
  background: linear-gradient(180deg, #ef4444, #b91c1c);
  color: #fff;
}

/* owned footer */
.shop-owned-footer {
  display: flex; align-items: center; justify-content: center; gap: 4px;
  background: rgba(34,197,94,.1); border: 1px solid rgba(34,197,94,.35);
  border-radius: 2px; padding: 7px 10px;
  font-size: 10px; font-weight: 900; letter-spacing: .1em;
  text-transform: uppercase; color: #4ade80;
}

/* confirm buttons */
.shop-confirm     { display: flex; gap: 5px; }
.shop-confirm__yes {
  flex: 1; padding: 7px 8px; border: none; border-radius: 2px; cursor: pointer;
  background: linear-gradient(180deg, #22c55e, #16a34a);
  color: #fff; font-size: 9px; font-weight: 900; text-transform: uppercase; letter-spacing: .06em;
  box-shadow: 0 2px 0 #0f7030;
}
.shop-confirm__yes:disabled { opacity: 0.5; cursor: not-allowed; }
.shop-confirm__no {
  flex: 1; padding: 7px 8px; border: 1px solid rgba(200,160,64,.3); border-radius: 2px; cursor: pointer;
  background: rgba(255,255,255,.05); color: #c8a040;
  font-size: 9px; font-weight: 700; text-transform: uppercase; letter-spacing: .06em;
}
.shop-ticket:not(.shop-ticket--shake):hover .shop-ticket__action {
  background: linear-gradient(180deg, #e6ba38, #b87c28);
}
.shop-confirm__yes:not(:disabled):hover { filter: brightness(1.1); }
.shop-confirm__no:hover { border-color: rgba(200,160,64,.6); color: #e6b84a; }

/* state card (loading/error) */
.shop-state-card {
  min-height: 16rem; display: grid; place-items: center; gap: var(--space-3);
  text-align: center; padding: var(--space-6); border-radius: 4px;
  background: rgba(255,255,255,.04); border: 1px solid rgba(200,160,64,.2);
}

/* error feedback */
.shop-feedback-error { color: #ef4444; margin-top: 12px; font-size: 0.85rem; }
</style>
