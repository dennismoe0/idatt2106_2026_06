<template>
  <main class="avatar-view">
    <header class="avatar-header">
      <BackButton label="Tilbake" />
      <div class="avatar-header__title">
        <div class="avatar-header__eyebrow">Detektivbyrået</div>
        <h1 class="avatar-header__name">🎭 Bygg din detektiv</h1>
      </div>
      <span v-if="saveMessage" class="avatar-save-pill">✓ Lagret!</span>
    </header>

    <section v-if="isLoading" class="avatar-state-card">
      <LoadingSpinner size="lg" />
      <p>Laster avatar...</p>
    </section>

    <section v-else-if="loadError" class="avatar-state-card">
      <h2>Kunne ikke laste avatar</h2>
      <p>{{ loadError }}</p>
      <BaseButton @click="loadAvatarPage">Prøv igjen</BaseButton>
    </section>

    <div v-else class="avatar-layout">

      <!-- LEFT: live preview (sticky) -->
      <div class="preview-panel">
        <div class="preview-stage">
          <AvatarComposer :selections="displayForm" :size="300" />
        </div>
        <div v-if="isPreviewingAny" class="avatar-preview-badge">✨ Forhåndsvisning</div>
        <div v-if="saveMessage" class="avatar-feedback avatar-feedback--success">{{ saveMessage }}</div>
        <div v-if="saveError"   class="avatar-feedback avatar-feedback--error">{{ saveError }}</div>
        <BaseButton
          :loading="isSaving"
          :disabled="!hasChanges"
          style="width:100%"
          @click="saveAvatar"
        >
          Lagre avatar
        </BaseButton>
        <BaseButton
          variant="secondary"
          :disabled="isSaving || !hasChanges"
          style="width:100%"
          @click="resetForm"
        >
          Tilbakestill
        </BaseButton>
      </div>

      <!-- RIGHT: controls -->
      <div class="controls-panel">

        <div class="av-section">
          <div class="av-rule"><div class="av-rule-line"></div><div class="av-rule-label">✦ KJØNN ✦</div><div class="av-rule-line-r"></div></div>
          <GenderToggle v-model="form.gender" />
        </div>

        <div class="av-section">
          <div class="av-rule"><div class="av-rule-line"></div><div class="av-rule-label">✦ HUDTONE ✦</div><div class="av-rule-line-r"></div></div>
          <SwatchGrid v-model="form.skinColor" :swatches="SKIN_COLORS" shape="circle" />
        </div>

        <div class="av-section">
          <div class="av-rule"><div class="av-rule-line"></div><div class="av-rule-label">✦ HÅR ✦</div><div class="av-rule-line-r"></div></div>
          <div class="av-sub-group">
            <div class="av-sub-label">Frisyre</div>
            <ShapeGrid
              :model-value="form.hairStyle"
              @update:model-value="handleSelect('hairStyle', $event)"
              :variants="hairStyles"
              :previewable="shopPreviewableHairStyle"
              :preview-value="previewOverrides.hairStyle ?? ''"
              @preview="handlePreview('hairStyle', $event)"
              :preview-component="AvatarHairPreview"
              :preview-props="{ hairColor: displayForm.hairColor }"
              variant-prop="hairStyle"
              :medal-locked="hairStylesLocked"
            />
          </div>
          <div class="av-sub-group">
            <div class="av-sub-label">Hårfarge</div>
            <SwatchGrid
              v-model="form.hairColor"
              :swatches="hairColors"
              shape="circle"
              :medal-locked="hairColorsLocked"
            />
            <input
              v-if="avatarStore.colorPickerUnlocked"
              type="color"
              :value="form.hairColor"
              @input="form.hairColor = $event.target.value"
              class="color-picker"
              title="Velg hvilken som helst hårfarge"
            />
          </div>
        </div>

        <div class="av-section">
          <div class="av-rule"><div class="av-rule-line"></div><div class="av-rule-label">✦ ØYNE ✦</div><div class="av-rule-line-r"></div></div>
          <div class="av-sub-group">
            <div class="av-sub-label">Øyeform</div>
            <ShapeGrid
              v-model="form.eyeStyle"
              :variants="EYE_STYLES"
              :preview-component="AvatarEyesPreview"
              :preview-props="{ eyeColor: form.eyeColor }"
              variant-prop="eyeStyle"
            />
          </div>
          <div class="av-sub-group">
            <div class="av-sub-label">Øyenfarge</div>
            <SwatchGrid v-model="form.eyeColor" :swatches="EYE_COLORS" shape="circle" />
          </div>
        </div>

        <div class="av-section">
          <div class="av-rule"><div class="av-rule-line"></div><div class="av-rule-label">✦ ANTREKK ✦</div><div class="av-rule-line-r"></div></div>
          <div class="av-sub-group">
            <div class="av-sub-label">Stil</div>
            <ShapeGrid
              :model-value="form.outfit"
              @update:model-value="handleSelect('outfit', $event)"
              :variants="outfits"
              :previewable="shopPreviewableOutfit"
              :preview-value="previewOverrides.outfit ?? ''"
              @preview="handlePreview('outfit', $event)"
              :preview-component="AvatarOutfitPreview"
              :preview-props="{ outfitColor: displayForm.outfitColor }"
              variant-prop="outfit"
              :medal-locked="outfitsLocked"
            />
          </div>
          <div class="av-sub-group">
            <div class="av-sub-label">Farge</div>
            <SwatchGrid v-model="form.outfitColor" :swatches="OUTFIT_COLORS" shape="square" />
          </div>
        </div>

        <div class="av-section">
          <div class="av-rule"><div class="av-rule-line"></div><div class="av-rule-label">✦ TILBEHØR ✦</div><div class="av-rule-line-r"></div></div>
          <ShapeGrid
            :model-value="form.accessory"
            @update:model-value="handleSelect('accessory', $event)"
            :variants="accessories"
            :previewable="shopPreviewableAccessory"
            :preview-value="previewOverrides.accessory ?? ''"
            @preview="handlePreview('accessory', $event)"
            :preview-component="AvatarAccessoryPreview"
            :preview-props="{ skinColor: displayForm.skinColor }"
            variant-prop="accessory"
            :medal-locked="accessoriesLocked"
          />
        </div>

        <button class="shop-link" @click="router.push('/shop')">
          🏪 Finn flere stiler i butikken →
        </button>

      </div>
    </div>
  </main>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import AvatarComposer         from '@/components/student/avatar/AvatarComposer.vue'
import AvatarHairPreview      from '@/components/student/avatar/layers/AvatarHair.vue'
import AvatarEyesPreview      from '@/components/student/avatar/layers/AvatarEyes.vue'
import AvatarOutfitPreview    from '@/components/student/avatar/layers/AvatarOutfit.vue'
import AvatarAccessoryPreview from '@/components/student/avatar/layers/AvatarAccessory.vue'
import SwatchGrid      from '@/components/student/avatar/controls/SwatchGrid.vue'
import ShapeGrid       from '@/components/student/avatar/controls/ShapeGrid.vue'
import GenderToggle    from '@/components/student/avatar/controls/GenderToggle.vue'
import BackButton      from '@/components/common/BackButton.vue'
import BaseButton      from '@/components/common/BaseButton.vue'
import LoadingSpinner  from '@/components/common/LoadingSpinner.vue'
import { useAvatarStore } from '@/stores/avatar'
import { SKIN_COLORS, EYE_COLORS, OUTFIT_COLORS, EYE_STYLES } from '@/utils/avatarOptions'

const avatarStore = useAvatarStore()
const router      = useRouter()

const FIELDS = ['gender','skinColor','hairStyle','hairColor','eyeStyle','eyeColor','outfit','outfitColor','accessory']

function empty() { return Object.fromEntries(FIELDS.map(k => [k, ''])) }

const form           = reactive(empty())
const originalAvatar = ref(empty())
const isLoading      = ref(true)
const isSaving       = ref(false)
const loadError      = ref('')
const saveError      = ref('')
const saveMessage    = ref('')

const hairStyles   = computed(() => avatarStore.available.hairStyle  ?? [])
const outfits      = computed(() => avatarStore.available.outfit     ?? [])
const accessories  = computed(() => avatarStore.available.accessory  ?? [])
const hairColors   = computed(() => avatarStore.available.hairColor  ?? [])

const hairStylesLocked  = computed(() => avatarStore.getMedalLockedForField('hairStyle'))
const outfitsLocked     = computed(() => avatarStore.getMedalLockedForField('outfit'))
const accessoriesLocked = computed(() => avatarStore.getMedalLockedForField('accessory'))
const hairColorsLocked  = computed(() => avatarStore.getMedalLockedForField('hairColor'))

const hasChanges = computed(() => FIELDS.some(k => form[k] !== originalAvatar.value[k]))

// Preview system — clicking unowned shop items shows them on avatar without saving
const previewOverrides = reactive({})
const displayForm      = computed(() => ({ ...form, ...previewOverrides }))
const isPreviewingAny  = computed(() => Object.keys(previewOverrides).length > 0)

function handleSelect(field, value) {
  form[field] = value
  delete previewOverrides[field]
}
function handlePreview(field, value) {
  console.log('[AvatarView] Preview', field, value)
  previewOverrides[field] = value
}

function shopPreviewableFor(field) {
  const owned             = avatarStore.available[field] ?? []
  const medalLockedValues = avatarStore.getMedalLockedForField(field).map(i => i.value)
  return avatarStore.shopItems
    .filter(i => i.optionType === field)
    .map(i => i.optionValue)
    .filter(v => !owned.includes(v) && !medalLockedValues.includes(v))
}
const shopPreviewableHairStyle  = computed(() => shopPreviewableFor('hairStyle'))
const shopPreviewableOutfit     = computed(() => shopPreviewableFor('outfit'))
const shopPreviewableAccessory  = computed(() => shopPreviewableFor('accessory'))

function resetForm() {
  FIELDS.forEach(k => { form[k] = originalAvatar.value[k] })
  saveError.value = ''
  saveMessage.value = ''
}

async function loadAvatarPage() {
  console.log('[AvatarView] Loading avatar')
  isLoading.value = true
  loadError.value = ''
  try {
    const [avatar] = await Promise.all([
      avatarStore.fetchAvatar(),
      avatarStore.fetchOptions(),
      avatarStore.fetchShop(),
    ])
    FIELDS.forEach(k => { form[k] = avatar?.[k] ?? '' })
    originalAvatar.value = { ...form }
    console.log('[AvatarView] Loaded avatar:', avatar)
  } catch (err) {
    console.error('[AvatarView] Load failed:', err)
    loadError.value = err?.response?.data?.error || 'Noe gikk galt ved lasting av avatardata.'
  } finally {
    isLoading.value = false
  }
}

async function saveAvatar() {
  console.log('[AvatarView] Saving:', { ...form })
  isSaving.value = true
  saveError.value = ''
  saveMessage.value = ''
  try {
    await avatarStore.updateAvatar({ ...form })
    originalAvatar.value = { ...form }
    saveMessage.value = 'Avatar lagret!'
    setTimeout(() => { saveMessage.value = '' }, 3000)
    console.log('[AvatarView] Saved')
  } catch (err) {
    console.error('[AvatarView] Save failed:', err)
    saveError.value = err?.response?.data?.error || 'Kunne ikke lagre avatar.'
  } finally {
    isSaving.value = false
  }
}

loadAvatarPage()
</script>

<style scoped>
/* ============================================================
   CSS VARIABLE OVERRIDES — rewire the design token system
   so all child components (ShapeGrid, SwatchGrid, GenderToggle,
   BaseButton) automatically inherit the dark detective palette.
   ============================================================ */
.avatar-view {
  /* surfaces → warm dark brown */
  --color-surface:            #2c1c0a;
  --color-surface-alt:        #231508;
  --color-surface-soft:       #1a1209;
  --color-surface-soft-alt:   #1e1206;
  --color-surface-glass:      rgba(44, 28, 10, 0.90);
  --color-surface-glass-strong: rgba(44, 28, 10, 0.96);
  /* text → cream / gold-tinted */
  --color-text:               #f5e6c8;
  --color-text-muted:         rgba(200, 160, 64, 0.60);
  --color-text-on-dark:       #f5e6c8;
  /* primary → gold */
  --color-primary:            #c8a040;
  --color-primary-light:      #d4b055;
  --color-primary-dark:       #a07820;
  --color-primary-soft:       rgba(200, 160, 64, 0.10);
  --color-primary-soft-strong: rgba(200, 160, 64, 0.18);
  --color-primary-focus-ring: rgba(200, 160, 64, 0.20);
  /* borders */
  --color-border:             rgba(200, 160, 64, 0.30);
  /* base */
  --color-bg:                 #1a1209;
  --shadow-lg:                0 8px 32px rgba(0,0,0,0.6);

  min-height: 100vh;
  background: linear-gradient(160deg, #1a1209 0%, #251709 50%, #1a1209 100%);
  color: #f5e6c8;
}

/* === HEADER (same style as shop) === */
.avatar-header {
  position: sticky; top: 0; z-index: 10;
  display: flex; align-items: center; gap: 12px;
  padding: 10px 16px;
  background: linear-gradient(90deg, #0e0b06, #1a1007, #0e0b06);
  border-bottom: 1px solid rgba(200,160,64,.35);
  box-shadow: 0 4px 16px rgba(0,0,0,.6);
}
.avatar-header__title { flex: 1; display: flex; flex-direction: column; gap: 1px; }
.avatar-header__eyebrow {
  font-size: 7px; text-transform: uppercase; letter-spacing: .18em;
  color: rgba(200,160,64,.55); font-weight: 700;
}
.avatar-header__name {
  margin: 0;
  font-size: 14px; font-weight: 900; color: #f5e6c8;
  text-transform: uppercase; letter-spacing: .06em;
}
.avatar-save-pill {
  padding: 4px 12px; border-radius: 20px;
  background: rgba(34,197,94,.15); border: 1px solid rgba(34,197,94,.4);
  color: #4ade80; font-size: 0.75rem; font-weight: 700; white-space: nowrap;
}

/* === STATE CARDS === */
.avatar-state-card {
  min-height: 16rem; display: grid; place-items: center; gap: 12px;
  text-align: center; padding: 32px; border-radius: 4px;
  background: rgba(255,255,255,.04); border: 1px solid rgba(200,160,64,.2);
  max-width: 860px; margin: 40px auto;
}

/* === LAYOUT === */
.avatar-layout {
  display: grid;
  gap: 24px;
  padding: 24px 20px 64px;
  max-width: 1100px;
  margin: 0 auto;
  align-items: start;
}
@media (min-width: 680px) {
  .avatar-layout { grid-template-columns: 260px 1fr; }
}
@media (min-width: 900px) {
  .avatar-layout { grid-template-columns: 310px 1fr; }
}
@media (min-width: 1200px) {
  .avatar-layout { grid-template-columns: 360px 1fr; }
}

/* === PREVIEW PANEL === */
.preview-panel {
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
/* corner brackets on preview panel */
.preview-panel::before,
.preview-panel::after {
  content: ''; position: absolute;
  width: 9px; height: 9px;
}
.preview-stage {
  background: linear-gradient(160deg, #2e1c0c, #3d2510);
  border: 1px solid rgba(200,160,64,.2);
  border-radius: 2px;
  display: flex; align-items: center; justify-content: center;
  width: 100%;
  aspect-ratio: 3/4;
  position: relative;
  overflow: hidden;
  box-shadow: inset 0 2px 8px rgba(0,0,0,.5);
}
.preview-stage::before {
  content: ''; position: absolute;
  top: 0; left: 0; right: 0; height: 1px;
  background: linear-gradient(90deg, transparent, rgba(200,160,64,.22), transparent);
}
.avatar-feedback {
  width: 100%; padding: 6px; border-radius: 2px;
  font-size: 10px; font-weight: 800; text-align: center;
  letter-spacing: .08em; text-transform: uppercase;
}
.avatar-feedback--success {
  background: rgba(34,197,94,.1); border: 1px solid rgba(34,197,94,.35); color: #4ade80;
}
.avatar-feedback--error {
  background: rgba(239,68,68,.1); border: 1px solid rgba(239,68,68,.35); color: #f87171;
}

/* === CONTROLS PANEL === */
.controls-panel { display: flex; flex-direction: column; gap: 0; }

/* === SECTION RULE HEADERS (same as shop) === */
.av-section { margin-bottom: 22px; }
.av-rule {
  display: flex; align-items: center; gap: 8px; margin-bottom: 12px;
}
.av-rule-line {
  flex: 1; height: 1px; min-width: 8px;
  background: linear-gradient(90deg, transparent, rgba(200,160,64,.5));
}
.av-rule-line-r {
  flex: 1; height: 1px; min-width: 8px;
  background: linear-gradient(90deg, rgba(200,160,64,.5), transparent);
}
.av-rule-label {
  background: rgba(200,160,64,.08); border: 1px solid rgba(200,160,64,.4);
  border-radius: 2px; padding: 3px 12px;
  font-size: 10px; font-weight: 900; text-transform: uppercase;
  letter-spacing: .18em; color: #c8a040; white-space: nowrap; flex-shrink: 0;
}
.av-sub-group { margin-bottom: 12px; }
.av-sub-label {
  font-size: 11px; font-weight: 800; text-transform: uppercase;
  letter-spacing: .12em; color: rgba(200,160,64,.65); margin-bottom: 8px;
}

/* === COLOR PICKER === */
.color-picker {
  width: 40px; height: 32px; padding: 2px;
  border: 1px solid rgba(200,160,64,.4); border-radius: 2px;
  cursor: pointer; margin-top: 6px; background: transparent;
}

/* === SHOP LINK === */
.shop-link {
  display: flex; align-items: center; justify-content: center; gap: 6px;
  width: 100%; padding: 9px 12px; margin-top: 4px;
  background: rgba(200,160,64,.05);
  border: 1px dashed rgba(200,160,64,.35); border-radius: 2px;
  color: rgba(200,160,64,.65);
  font-size: 10px; font-weight: 700; letter-spacing: .08em;
  text-transform: uppercase; cursor: pointer;
  transition: background 0.15s, border-color 0.15s, color 0.15s;
}
.shop-link:hover {
  background: rgba(200,160,64,.10);
  border-color: rgba(200,160,64,.55);
  color: #c8a040;
}

/* === CHILD COMPONENT SURFACE OVERRIDES ===
   CSS variable cascade handles colors automatically.
   These :deep() rules patch backgrounds that bypass the token system.
   IMPORTANT: never override display/layout on child component roots —
   that collapses ShapeGrid's internal grid (tiles become 0-width dots). */
:deep(.shape-grid) {
  grid-template-columns: repeat(auto-fill, minmax(80px, 1fr)) !important;
}

.avatar-preview-badge {
  width: 100%; padding: 5px 0; text-align: center; border-radius: 2px;
  background: rgba(200,160,64,.08); border: 1px solid rgba(200,160,64,.3);
  font-size: 9px; font-weight: 900; letter-spacing: .12em; text-transform: uppercase;
  color: rgba(200,160,64,.8);
}
:deep(.shape-tile) {
  background: linear-gradient(160deg, #2c1c0a, #1e1206) !important;
  border-color: rgba(200,160,64,.25) !important;
}
:deep(.shape-tile--selected) {
  border-color: #c8a040 !important;
  background: linear-gradient(160deg, #3a2410, #2c1c0a) !important;
  box-shadow: 0 0 0 2px rgba(200,160,64,.2) !important;
}
:deep(.shape-tile__preview) {
  background: linear-gradient(160deg, #2e1c0c, #3d2510) !important;
}
:deep(.gender-btn) {
  background: linear-gradient(160deg, #2c1c0a, #1e1206) !important;
  border: 1px solid rgba(200,160,64,.3) !important;
  color: rgba(200,160,64,.7) !important;
  border-radius: 2px; padding: 6px 14px;
  font-size: 11px; font-weight: 700; cursor: pointer;
}
:deep(.gender-btn--active) {
  background: linear-gradient(160deg, #3a2410, #2c1c0a) !important;
  border-color: #c8a040 !important;
  color: #c8a040 !important;
  box-shadow: 0 0 0 2px rgba(200,160,64,.15);
}
</style>
