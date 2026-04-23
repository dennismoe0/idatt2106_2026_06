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
              <div v-for="item in group.items" :key="item.id" class="shop-swatch-wrap">
                <template v-if="confirmingId === item.id">
                  <div class="shop-confirm">
                    <button type="button" class="shop-confirm__yes" :disabled="isPurchasing" @click="doPurchase(item)">
                      Ja ⭐{{ item.starPrice }}
                    </button>
                    <button type="button" class="shop-confirm__no" @click="confirmingId = null">Avbryt</button>
                  </div>
                </template>
                <template v-else>
                  <div
                    class="shop-swatch"
                    :class="{
                      'shop-swatch--owned':  item.purchased,
                      'shop-swatch--locked': !item.purchased && starBalance < item.starPrice,
                    }"
                    :style="{ background: item.optionValue }"
                    @click="!item.purchased && (confirmingId = item.id)"
                  >
                    <span v-if="item.purchased" class="shop-swatch__check">✓</span>
                  </div>
                  <div class="shop-swatch__price">
                    <span v-if="item.purchased" class="shop-swatch__price--owned">✓</span>
                    <span v-else class="shop-swatch__price--cost">⭐ {{ item.starPrice }}</span>
                  </div>
                </template>
              </div>
            </div>
          </template>

          <!-- ITEM CARDS (hairStyle, outfit) -->
          <template v-else>
            <div class="shop-cards">
              <div
                v-for="item in group.items"
                :key="item.id"
                class="shop-card"
                :class="{
                  'shop-card--owned':  item.purchased,
                  'shop-card--locked': !item.purchased && starBalance < item.starPrice,
                }"
              >
                <div class="shop-corner shop-corner-tl"></div>
                <div class="shop-corner shop-corner-tr"></div>
                <div class="shop-corner shop-corner-bl"></div>
                <div class="shop-corner shop-corner-br"></div>

                <div class="shop-preview">
                  <component
                    :is="previewComponentFor(item.optionType)"
                    v-bind="previewPropsFor(item)"
                    width="76"
                    height="101"
                  />
                  <div v-if="item.purchased" class="shop-preview__stamp">
                    <div class="shop-stamp-text">ANSKAFFET</div>
                  </div>
                </div>

                <div class="shop-item-name">{{ formatOption(item.optionValue) }}</div>

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
                  :class="{ 'shop-ticket--locked': starBalance < item.starPrice }"
                  @click="starBalance >= item.starPrice && (confirmingId = item.id)"
                >
                  <div class="shop-ticket__price">
                    <span class="shop-ticket__star">⭐</span>
                    <span class="shop-ticket__amount">{{ item.starPrice }}</span>
                  </div>
                  <div class="shop-ticket__action">Kjøp</div>
                </div>
              </div>
            </div>
          </template>

        </div>
      </section>

      <p v-if="purchaseError" class="shop-feedback-error">{{ purchaseError }}</p>
    </div>
  </main>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useAvatarStore } from '@/stores/avatar'
import { useGameStore }   from '@/stores/game'
import AvatarHair    from '@/components/student/avatar/layers/AvatarHair.vue'
import AvatarOutfit  from '@/components/student/avatar/layers/AvatarOutfit.vue'
import BackButton    from '@/components/common/BackButton.vue'
import BaseButton    from '@/components/common/BaseButton.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import { formatOption } from '@/utils/avatarOptions'

const avatarStore = useAvatarStore()
const gameStore   = useGameStore()

const isLoading    = ref(true)
const loadError    = ref('')
const confirmingId = ref(null)
const isPurchasing = ref(false)
const purchaseError = ref('')

const starBalance = computed(() => gameStore.starBalance)

const itemGroups = computed(() => {
  const items = avatarStore.shopItems
  const groups = [
    { label: 'Hårfarger', key: 'hairColor' },
    { label: 'Frisyrer',  key: 'hairStyle' },
    { label: 'Antrekk',   key: 'outfit' },
  ]
  return groups
    .map(g => ({ label: g.label, key: g.key, items: items.filter(i => i.optionType === g.key) }))
    .filter(g => g.items.length > 0)
})

function previewComponentFor(optionType) {
  if (optionType === 'hairStyle') return AvatarHair
  if (optionType === 'outfit')    return AvatarOutfit
  return null
}

function previewPropsFor(item) {
  if (item.optionType === 'hairStyle') return { hairStyle: item.optionValue, hairColor: '#8B4513' }
  if (item.optionType === 'outfit')    return { outfit: item.optionValue, outfitColor: '#2563eb' }
  return {}
}

async function loadShop() {
  console.log('[ShopView] Loading shop...')
  isLoading.value = true
  loadError.value = ''
  try {
    await avatarStore.fetchShop()
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
  padding: 20px 16px 28px;
  max-width: 640px; margin: 0 auto;
  display: flex; flex-direction: column; gap: 0;
}

/* === SECTION === */
.shop-section        { display: block; margin-bottom: 32px; }
.shop-section__inner { display: inline-block; width: fit-content; max-width: 100%; }

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

/* === HAIR COLOR SWATCHES === */
.shop-swatches   { display: flex; flex-wrap: wrap; gap: 8px; }
.shop-swatch-wrap{ display: flex; flex-direction: column; align-items: center; gap: 3px; min-width: 40px; }
.shop-swatch {
  width: 40px; height: 40px; border-radius: 50%;
  border: 2px solid rgba(200,160,64,.3);
  box-shadow: 0 2px 6px rgba(0,0,0,.5), inset 0 1px 0 rgba(255,255,255,.1);
  cursor: pointer; position: relative;
  display: flex; align-items: center; justify-content: center;
  background: transparent;
}
.shop-swatch--owned  { border-color: rgba(34,197,94,.5); cursor: default; }
.shop-swatch--locked { cursor: not-allowed; }
.shop-swatch-wrap .shop-confirm { min-width: 92px; }

/* hover states */
.shop-swatch:not(.shop-swatch--owned):not(.shop-swatch--locked):hover {
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
  position: absolute; bottom: -3px; right: -3px;
  width: 14px; height: 14px; border-radius: 50%;
  background: #22c55e; border: 1.5px solid #1a1209;
  font-size: 8px; color: #fff;
  display: flex; align-items: center; justify-content: center;
}
.shop-swatch__price        { font-size: 7.5px; }
.shop-swatch__price--owned { color: #4ade80; }
.shop-swatch__price--cost  { color: #f5c518; }

/* === ITEM CARDS === */
.shop-cards { display: flex; flex-wrap: wrap; gap: 10px; }
.shop-card {
  width: fit-content;
  border-radius: 3px;
  padding: 8px;
  display: flex; flex-direction: column; align-items: stretch;
  background:
    repeating-linear-gradient(45deg, rgba(255,255,255,.012) 0px, rgba(255,255,255,.012) 1px, transparent 1px, transparent 7px),
    linear-gradient(160deg, #2c1c0a, #1e1206);
  border: 1px solid rgba(200,160,64,.4);
  box-shadow: inset 0 1px 0 rgba(200,160,64,.12), 0 5px 16px rgba(0,0,0,.5);
  position: relative;
}
.shop-card--owned  { border-color: rgba(34,197,94,.35); }
.shop-card--locked { opacity: .42; filter: saturate(.35); }

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
  width: 80px; height: 106px;
  background: linear-gradient(160deg, #0c0804, #1a1007);
  border: 1px solid rgba(200,160,64,.2);
  border-radius: 2px;
  display: flex; align-items: center; justify-content: center;
  position: relative; overflow: hidden; margin-bottom: 7px;
  box-shadow: inset 0 3px 10px rgba(0,0,0,.7);
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
  font-size: 8.5px; color: #f0ddb8; font-weight: 800;
  letter-spacing: .1em; text-transform: uppercase;
  border-top: 1px solid rgba(200,160,64,.2);
  border-bottom: 1px solid rgba(200,160,64,.2);
  padding: 3px 0; margin-bottom: 7px;
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
  padding: 5px 7px;
  display: flex; align-items: center; justify-content: center; gap: 2px;
  flex-shrink: 0;
}
.shop-ticket__star   { font-size: 10px; line-height: 1; }
.shop-ticket__amount { font-size: 9px; font-weight: 900; color: #f5c518; }
.shop-ticket__action {
  flex: 1;
  background: linear-gradient(180deg, #d4a832, #a87220);
  color: #1a1209; font-size: 8px; font-weight: 900;
  letter-spacing: .1em; text-transform: uppercase;
  display: flex; align-items: center; justify-content: center; padding: 5px 8px;
}
.shop-ticket--locked { box-shadow: none; cursor: not-allowed; pointer-events: none; }
.shop-ticket--locked .shop-ticket__price  { background: rgba(80,80,80,.1); border-color: rgba(120,120,120,.3); }
.shop-ticket--locked .shop-ticket__amount { color: #777; }
.shop-ticket--locked .shop-ticket__action { background: linear-gradient(180deg, #4a4a4a, #333); color: #666; }

/* owned footer */
.shop-owned-footer {
  display: flex; align-items: center; justify-content: center; gap: 4px;
  background: rgba(34,197,94,.1); border: 1px solid rgba(34,197,94,.35);
  border-radius: 2px; padding: 5px 8px;
  font-size: 8px; font-weight: 900; letter-spacing: .1em;
  text-transform: uppercase; color: #4ade80;
}

/* confirm buttons */
.shop-confirm     { display: flex; gap: 4px; }
.shop-confirm__yes {
  flex: 1; padding: 5px 6px; border: none; border-radius: 2px; cursor: pointer;
  background: linear-gradient(180deg, #22c55e, #16a34a);
  color: #fff; font-size: 7.5px; font-weight: 900; text-transform: uppercase; letter-spacing: .06em;
  box-shadow: 0 2px 0 #0f7030;
}
.shop-confirm__yes:disabled { opacity: 0.5; cursor: not-allowed; }
.shop-confirm__no {
  flex: 1; padding: 5px 6px; border: 1px solid rgba(200,160,64,.3); border-radius: 2px; cursor: pointer;
  background: rgba(255,255,255,.05); color: #c8a040;
  font-size: 7.5px; font-weight: 700; text-transform: uppercase; letter-spacing: .06em;
}
.shop-ticket:not(.shop-ticket--locked):hover .shop-ticket__action {
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
