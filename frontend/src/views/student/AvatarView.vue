<template>
  <main class="avatar-view">
    <div class="avatar-shell">
      <header class="avatar-header">
        <BackButton label="Tilbake" />
        <div>
          <p class="eyebrow">Elevprofil</p>
          <h1>Bygg din detektiv</h1>
        </div>
      </header>

      <section v-if="isLoading" class="state-card">
        <LoadingSpinner size="lg" />
        <p>Laster avatar...</p>
      </section>

      <section v-else-if="loadError" class="state-card state-card--error">
        <h2>Kunne ikke laste avatar</h2>
        <p>{{ loadError }}</p>
        <BaseButton @click="loadAvatarPage">Prøv igjen</BaseButton>
      </section>

      <section v-else class="avatar-layout">
        <!-- LEFT: live preview -->
        <div class="preview-panel">
          <div class="preview-stage">
            <AvatarComposer :selections="form" :size="200" />
          </div>
          <div v-if="saveMessage" class="feedback feedback--success">{{ saveMessage }}</div>
          <div v-if="saveError"   class="feedback feedback--error">{{ saveError }}</div>
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
          <section class="control-section">
            <label class="control-label">Kjønn</label>
            <GenderToggle v-model="form.gender" />
          </section>

          <section class="control-section">
            <label class="control-label">Hudtone</label>
            <SwatchGrid v-model="form.skinColor" :swatches="SKIN_COLORS" shape="circle" :locked="[]" />
          </section>

          <section class="control-section">
            <label class="control-label">Frisyre</label>
            <ShapeGrid
              v-model="form.hairStyle"
              :variants="HAIR_STYLES"
              :preview-component="AvatarHairPreview"
              :preview-props="{ hairColor: form.hairColor }"
              variant-prop="hairStyle"
              :locked="[]"
            />
          </section>

          <section class="control-section">
            <label class="control-label">Hårfarge</label>
            <SwatchGrid v-model="form.hairColor" :swatches="HAIR_COLORS" shape="circle" :locked="lockedHairColors" />
          </section>

          <section class="control-section">
            <label class="control-label">Øyne</label>
            <ShapeGrid
              v-model="form.eyeStyle"
              :variants="EYE_STYLES"
              :preview-component="AvatarEyesPreview"
              :preview-props="{ eyeColor: form.eyeColor }"
              variant-prop="eyeStyle"
              :locked="[]"
            />
          </section>

          <section class="control-section">
            <label class="control-label">Øyenfarge</label>
            <SwatchGrid v-model="form.eyeColor" :swatches="EYE_COLORS" shape="circle" :locked="[]" />
          </section>

          <section class="control-section">
            <label class="control-label">Antrekk</label>
            <ShapeGrid
              v-model="form.outfit"
              :variants="OUTFITS"
              :preview-component="AvatarOutfitPreview"
              :preview-props="{ outfitColor: form.outfitColor }"
              variant-prop="outfit"
              :locked="[]"
            />
          </section>

          <section class="control-section">
            <label class="control-label">Antrekksfarge</label>
            <SwatchGrid v-model="form.outfitColor" :swatches="OUTFIT_COLORS" shape="square" :locked="[]" />
          </section>

          <section class="control-section">
            <label class="control-label">Tilbehør</label>
            <div class="pill-row">
              <button
                v-for="acc in ACCESSORIES"
                :key="acc"
                class="pill"
                :class="{ 'pill--active': form.accessory === acc }"
                @click="form.accessory = acc"
              >
                {{ formatOption(acc) }}
              </button>
            </div>
          </section>
        </div>
      </section>
    </div>
  </main>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import AvatarComposer from '@/components/student/avatar/AvatarComposer.vue'
import AvatarHair     from '@/components/student/avatar/layers/AvatarHair.vue'
import AvatarEyes     from '@/components/student/avatar/layers/AvatarEyes.vue'
import AvatarOutfit   from '@/components/student/avatar/layers/AvatarOutfit.vue'
import SwatchGrid     from '@/components/student/avatar/controls/SwatchGrid.vue'
import ShapeGrid      from '@/components/student/avatar/controls/ShapeGrid.vue'
import GenderToggle   from '@/components/student/avatar/controls/GenderToggle.vue'
import BackButton     from '@/components/common/BackButton.vue'
import BaseButton     from '@/components/common/BaseButton.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import { useAvatarStore } from '@/stores/avatar'
import {
  SKIN_COLORS, HAIR_COLORS, EYE_COLORS, OUTFIT_COLORS,
  HAIR_STYLES, EYE_STYLES, OUTFITS, ACCESSORIES, formatOption,
} from '@/utils/avatarOptions'

// ShapeGrid needs the component objects directly
const AvatarHairPreview   = AvatarHair
const AvatarEyesPreview   = AvatarEyes
const AvatarOutfitPreview = AvatarOutfit

const avatarStore = useAvatarStore()

const FIELDS = ['gender','skinColor','hairStyle','hairColor','eyeStyle','eyeColor','outfit','outfitColor','accessory']

function empty() { return Object.fromEntries(FIELDS.map(k => [k, ''])) }

const form           = reactive(empty())
const originalAvatar = ref(empty())
const isLoading      = ref(true)
const isSaving       = ref(false)
const loadError      = ref('')
const saveError      = ref('')
const saveMessage    = ref('')

// Last 2 hair colours are unlockable (demo — real unlock system queries backend)
const lockedHairColors = HAIR_COLORS.slice(6)

const hasChanges = computed(() => FIELDS.some(k => form[k] !== originalAvatar.value[k]))

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
    const updated = await avatarStore.updateAvatar({ ...form })
    FIELDS.forEach(k => { form[k] = updated?.[k] ?? form[k] })
    originalAvatar.value = { ...form }
    saveMessage.value = 'Avatar lagret!'
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
.avatar-view {
  min-height: 100vh;
  padding: var(--space-8) var(--space-4) var(--space-12);
  background:
    radial-gradient(circle at top left, var(--color-primary-soft-strong), transparent 30%),
    linear-gradient(180deg, var(--color-surface-soft) 0%, var(--color-surface-soft-alt) 100%);
}
.avatar-shell { max-width: 78rem; margin: 0 auto; }
.avatar-header { display: grid; gap: var(--space-4); margin-bottom: var(--space-8); }
.eyebrow {
  margin: 0 0 0.35rem; text-transform: uppercase;
  letter-spacing: 0.14em; font-size: 0.76rem;
  font-weight: 700; color: var(--color-primary);
}
.avatar-header h1 { margin: 0; font-size: clamp(2rem, 4vw, 3.4rem); color: var(--color-text); }

.avatar-layout { display: grid; gap: var(--space-6); align-items: start; }
@media (min-width: 860px) {
  .avatar-layout { grid-template-columns: 260px 1fr; }
}

.preview-panel {
  display: flex; flex-direction: column; align-items: center; gap: var(--space-4);
  padding: var(--space-6); border-radius: var(--radius-xl);
  background: var(--color-surface-glass); border: 1px solid var(--color-border);
  box-shadow: var(--shadow-lg); backdrop-filter: blur(18px);
  position: sticky; top: var(--space-4);
}
.preview-stage {
  background: radial-gradient(circle, var(--color-surface-glass-strong), var(--color-primary-soft));
  border-radius: var(--radius-xl); padding: var(--space-6);
  display: flex; align-items: center; justify-content: center;
}

.controls-panel {
  display: flex; flex-direction: column; gap: var(--space-6);
  padding: var(--space-6); border-radius: var(--radius-xl);
  background: var(--color-surface-glass); border: 1px solid var(--color-border);
  box-shadow: var(--shadow-lg); backdrop-filter: blur(18px);
}
.control-section { display: flex; flex-direction: column; gap: var(--space-2); }
.control-label {
  font-size: var(--text-sm); font-weight: var(--font-bold);
  text-transform: uppercase; letter-spacing: 0.08em; color: var(--color-text-muted);
}
.pill-row { display: flex; flex-wrap: wrap; gap: var(--space-2); }
.pill {
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-full); border: 1.5px solid var(--color-border);
  background: var(--color-surface); color: var(--color-text-muted);
  font-size: var(--text-sm); font-weight: var(--font-medium); cursor: pointer;
  transition: all var(--transition-fast);
}
.pill--active {
  border-color: var(--color-primary); background: var(--color-primary-soft);
  color: var(--color-primary); font-weight: var(--font-semibold);
}

.state-card {
  min-height: 18rem; display: grid; place-items: center; gap: var(--space-3);
  text-align: center; padding: var(--space-6); border-radius: var(--radius-xl);
  background: var(--color-surface-glass); border: 1px solid var(--color-border);
}
.state-card--error { border-color: var(--color-danger-soft); }
.feedback {
  width: 100%; padding: var(--space-3); border-radius: var(--radius-md);
  font-size: var(--text-sm); font-weight: 600; text-align: center;
}
.feedback--success { background: var(--color-success-soft); color: var(--color-success); }
.feedback--error   { background: var(--color-danger-soft);  color: var(--color-danger); }
</style>
