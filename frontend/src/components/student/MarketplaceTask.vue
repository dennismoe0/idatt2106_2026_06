<template>
  <section class="task-card">
    <header class="mp-header">
      <div class="mp-header__icon" aria-hidden="true">🛒</div>
      <div class="mp-header__text">
        <h2 class="mp-header__title">{{ task.stop?.name ?? 'Markedsplassen' }}</h2>
        <p class="mp-header__guidance">{{ task.guidanceText ?? 'Sjekk URL, priser, kontaktinfo og betalingsvalg nøye.' }}</p>
      </div>
    </header>

    <!-- CLICK_SUSPICIOUS: interactive fake webshop with flaggable elements -->
    <template v-if="taskSubtype === 'CLICK_SUSPICIOUS'">
      <p class="question">{{ contentJson.question ?? 'Klikk på de delene du synes er mistenkelige' }}</p>

      <div data-peek-trigger>
        <FakeWebshop
          :site-name="siteLabel"
          :eyebrow="mockupContent.eyebrow"
          :headline="mockupContent.headline"
          :tagline="mockupContent.tagline"
          :product-name="mockupContent.productName"
          :price="mockupContent.price"
          :original-price="mockupContent.originalPrice"
          :badges="mockupContent.badges"
          :payment-text="mockupContent.paymentText"
          :contact-text="mockupContent.contactText"
          :return-policy-text="mockupContent.returnPolicyText"
          :notice="mockupContent.notice"
          :cta-text="mockupContent.ctaText"
          :product-image-url="mockupContent.productImageUrl"
          :clickable-elements="csElements"
          :flagged-elements="flagged"
          :feedback-states="feedbackStates"
          :disabled="!!result"
          @toggle="toggle"
        />
      </div>

      <div v-if="flagged.size > 0 && !result" class="cs-chips" aria-live="polite">
        <span v-for="id in [...flagged]" :key="id" class="cs-chip">🚩 {{ elementLabel(id) }}</span>
      </div>

      <!-- Per-element result explanations -->
      <ul v-if="result" class="cs-result-list">
        <li
          v-for="el in csElements"
          :key="el.id"
          class="cs-result-list__item"
          :class="`cs-result-item--${elementResultState(el)}`"
        >
          <span :class="elementResultIcon(el)">{{ elementResultEmoji(el) }}</span>
          <span><strong>{{ el.label }}</strong>: {{ el.explanation }}</span>
        </li>
      </ul>
    </template>

    <template v-else-if="taskSubtype === 'IDENTIFY'">
      <div class="site-preview" data-peek-trigger>
        <div class="site-preview__url-bar">
          <span class="site-preview__lock" aria-hidden="true">🔓</span>
          <span class="site-preview__url">{{ siteLabel }}</span>
        </div>

        <div class="site-preview__img-wrap">
          <img
            v-if="contentJson.imageUrl"
            :src="contentJson.imageUrl"
            :alt="`Skjermbilde av ${siteLabel}`"
            class="site-preview__img"
          />

          <FakeWebshop
            v-else-if="renderMode === 'html'"
            :site-name="siteLabel"
            :eyebrow="mockupContent.eyebrow"
            :headline="mockupContent.headline"
            :tagline="mockupContent.tagline"
            :product-name="mockupContent.productName"
            :price="mockupContent.price"
            :original-price="mockupContent.originalPrice"
            :badges="mockupContent.badges"
            :payment-text="mockupContent.paymentText"
            :contact-text="mockupContent.contactText"
            :notice="mockupContent.notice"
            :cta-text="mockupContent.ctaText"
            :product-image-url="mockupContent.productImageUrl"
          />

          <div v-else class="site-preview__placeholder">
            <span aria-hidden="true">🛍️</span>
            <span>{{ siteLabel }}</span>
          </div>
        </div>
      </div>

      <p class="question">{{ questionText }}</p>

      <div class="options">
        <button
          v-for="opt in identifyOptions"
          :key="opt.id"
          type="button"
          class="option-btn"
          :class="{ 'option-btn--selected': selected === opt.id }"
          :disabled="!!result"
          @click="selected = opt.id"
        >
          {{ opt.text }}
        </button>
      </div>
    </template>

    <template v-else-if="taskSubtype === 'RANK'">
      <p class="question">{{ questionText }}</p>

      <div class="sites-grid">
        <button
          v-for="site in rankedSites"
          :key="site.id"
          type="button"
          class="site-card"
          data-peek-trigger
          :class="{ 'site-card--selected': selected === site.id }"
          :disabled="!!result"
          @click="selected = site.id"
        >
          <div class="site-card__img-wrap">
            <img
              v-if="site.imageUrl"
              :src="site.imageUrl"
              :alt="`Skjermbilde av ${site.name}`"
              class="site-card__img"
            />
            <div v-else class="site-card__placeholder" aria-hidden="true">🛍️</div>
          </div>

          <div class="site-card__body">
            <span class="site-card__url">{{ site.name }}</span>
            <span v-if="site.badge" class="site-card__badge">{{ site.badge }}</span>
            <p v-if="site.description" class="site-card__meta">{{ site.description }}</p>
          </div>
        </button>
      </div>
    </template>

    <button
      v-if="!result"
      type="button"
      class="submit-btn"
      :disabled="taskSubtype !== 'CLICK_SUSPICIOUS' && selected === null"
      @click="submit"
    >
      {{ taskSubtype === 'CLICK_SUSPICIOUS' && flagged.size === 0 ? 'Ingen er mistenkelige →' : 'Send svar' }}
    </button>

    <Transition name="result-slide">
      <div
        v-if="result"
        class="inline-result"
        :class="result.correct ? 'inline-result--correct' : 'inline-result--wrong'"
        role="status"
        aria-live="polite"
      >
        <p class="inline-result__label">
          {{ result.correct ? '✅ Riktig!' : '❌ Ikke helt riktig' }}
        </p>
        <p class="inline-result__explanation">{{ result.explanation }}</p>
        <p v-if="result.stopCompleted" class="inline-result__stop">
          🎉 Du fullførte Markedsplassen!
        </p>
        <div class="inline-result__actions">
          <button type="button" class="next-btn" @click="$emit('next')">
            {{ isLastTask ? 'Videre til sammendrag →' : 'Neste oppgave →' }}
          </button>
        </div>
      </div>
    </Transition>
  </section>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import FakeWebshop from '@/components/student/FakeWebshop.vue'

const props = defineProps({
  task: { type: Object, required: true },
  result: { type: Object, default: null },
  isLastTask: { type: Boolean, default: false },
})

const emit = defineEmits(['submitted', 'next'])

const selected = ref(null)
const flagged  = reactive(new Set())

const contentJson = computed(() => props.task?.contentJson ?? {})
const taskSubtype = computed(() => normalizeSubtype(contentJson.value.type))
const csElements  = computed(() => contentJson.value.elements ?? [])
const renderMode = computed(() => normalizeRenderMode(contentJson.value))
const questionText = computed(() => contentJson.value.question ?? 'Hva er det tryggeste valget?')
const siteLabel = computed(() =>
  contentJson.value.siteName ??
  contentJson.value.siteUrl ??
  contentJson.value.url ??
  'ukjent-butikk.example',
)

const identifyOptions = computed(() => normalizeOptions(contentJson.value.options))
const rankedSites = computed(() => normalizeSites(contentJson.value.sites))

const mockupContent = computed(() => {
  const mockup = contentJson.value.mockup ?? {}
  const badges = Array.isArray(mockup.badges)
    ? mockup.badges.filter(hasDisplayValue)
    : []

  return {
    eyebrow: mockup.eyebrow ?? 'Begrenset kampanje',
    headline: mockup.headline ?? contentJson.value.headline ?? 'Supertilbud bare i dag',
    tagline:
      mockup.tagline ??
      contentJson.value.tagline ??
      'Sjekk nøye før du handler på nettsteder du ikke kjenner.',
    productName: mockup.productName ?? contentJson.value.productName ?? 'Populært produkt',
    price: mockup.price ?? contentJson.value.price ?? '99 kr',
    originalPrice: mockup.originalPrice ?? contentJson.value.originalPrice ?? '',
    paymentText:
      mockup.paymentText ??
      contentJson.value.paymentText ??
      'Betaling: Western Union / Gavekort',
    contactText:
      mockup.contactText ??
      contentJson.value.contactText ??
      'Kontakt: ingen info tilgjengelig',
    returnPolicyText:
      mockup.returnPolicyText ??
      contentJson.value.returnPolicyText ??
      'Retur: ingen informasjon tilgjengelig',
    ctaText: mockup.ctaText ?? 'Kjøp nå',
    productImageUrl:
      mockup.productImageUrl ??
      contentJson.value.productImageUrl ??
      '/marketplace/air-max-270.png',
    badges,
    notice:
      mockup.notice ??
      contentJson.value.notice ??
      'Betaling og kontaktinfo bør alltid sjekkes før du handler.',
  }
})

watch(
  () => props.task?.id,
  () => {
    selected.value = null
    flagged.clear()
  },
  { immediate: true },
)

function toggle(id) {
  if (props.result) return
  if (flagged.has(id)) flagged.delete(id)
  else flagged.add(id)
  console.log('[MarketplaceTask] Toggled element', id, '— flagged:', [...flagged])
}

function elementLabel(id) {
  return csElements.value.find(e => e.id === id)?.label ?? id
}

const correctIds = computed(() => {
  // Prefer ids from the response if backend provides them, else derive from
  // the task's own elements (each carries an `isSuspicious` flag).
  const fromResult = props.result?.correctElementIds
  if (Array.isArray(fromResult) && fromResult.length > 0) {
    return new Set(fromResult)
  }
  return new Set(
    csElements.value
      .filter(el => el?.isSuspicious === true)
      .map(el => el.id),
  )
})

function elementResultEmoji(el) {
  if (!props.result) return ''
  const wasFlagged = flagged.has(el.id)
  if (wasFlagged && correctIds.value.has(el.id)) return '✅'
  if (wasFlagged && !correctIds.value.has(el.id)) return '❌'
  if (!wasFlagged && correctIds.value.has(el.id)) return '⚠️'
  return '✓'
}

function elementResultState(el) {
  if (!props.result) return 'ok'
  const wasFlagged = flagged.has(el.id)
  if (wasFlagged && correctIds.value.has(el.id)) return 'correct'
  if (wasFlagged && !correctIds.value.has(el.id)) return 'wrong'
  if (!wasFlagged && correctIds.value.has(el.id)) return 'missed'
  return 'ok'
}

function elementResultIcon(el) {
  if (!props.result) return ''
  const wasFlagged = flagged.has(el.id)
  if (wasFlagged && correctIds.value.has(el.id)) return 'cs-icon--correct'
  if (wasFlagged && !correctIds.value.has(el.id)) return 'cs-icon--wrong'
  if (!wasFlagged && correctIds.value.has(el.id)) return 'cs-icon--missed'
  return 'cs-icon--ok'
}

const feedbackStates = computed(() =>
  Object.fromEntries(csElements.value.map((element) => {
    const wasFlagged = flagged.has(element.id)
    const isCorrect = correctIds.value.has(element.id)

    if (!props.result) {
      return [element.id, wasFlagged ? 'flagged' : '']
    }
    if (wasFlagged && isCorrect) return [element.id, 'correct']
    if (wasFlagged && !isCorrect) return [element.id, 'wrong']
    if (!wasFlagged && isCorrect) return [element.id, 'missed']
    return [element.id, '']
  }))
)

function submit() {
  if (taskSubtype.value === 'CLICK_SUSPICIOUS') {
    const flaggedElementIds = [...flagged]
    console.log('[MarketplaceTask] CLICK_SUSPICIOUS submitting:', flaggedElementIds)
    emit('submitted', { flaggedElementIds })
    return
  }
  if (selected.value === null) return
  console.log('[MarketplaceTask] Submitting:', selected.value)
  emit('submitted', { selected: selected.value })
}

function normalizeSubtype(type) {
  const upper = String(type ?? 'CLICK_SUSPICIOUS').toUpperCase()
  if (upper === 'RANK') return 'RANK'
  if (upper === 'IDENTIFY') return 'IDENTIFY'
  if (upper === 'CLICK_SUSPICIOUS') return 'CLICK_SUSPICIOUS'
  console.warn('[MarketplaceTask] Unknown task subtype:', type, '— defaulting to CLICK_SUSPICIOUS')
  return 'CLICK_SUSPICIOUS'
}

function normalizeRenderMode(content) {
  const explicitMode = String(content?.renderMode ?? '').toLowerCase()

  if (explicitMode === 'html') {
    return 'html'
  }

  if (explicitMode === 'image') {
    return 'image'
  }

  const hasMockup = content?.mockup && typeof content.mockup === 'object'
  const hasImage = typeof content?.imageUrl === 'string' && content.imageUrl.trim().length > 0

  return hasMockup && !hasImage ? 'html' : 'image'
}

function normalizeOptions(options) {
  if (!Array.isArray(options)) {
    return []
  }

  return options.map((option, index) => {
    if (typeof option === 'string') {
      return {
        id: `option-${index + 1}`,
        text: option,
      }
    }

    return {
      id: option?.id ?? option?.value ?? `option-${index + 1}`,
      text: option?.text ?? option?.label ?? option?.title ?? `Valg ${index + 1}`,
    }
  })
}

function normalizeSites(sites) {
  if (!Array.isArray(sites)) {
    return []
  }

  return sites.map((site, index) => ({
    id: site?.id ?? `site-${index + 1}`,
    name: site?.name ?? site?.url ?? site?.siteName ?? `butikk-${index + 1}.example`,
    imageUrl: site?.imageUrl ?? site?.thumbnailUrl ?? '',
    description: site?.description ?? site?.tagline ?? '',
    badge: site?.badge ?? site?.label ?? '',
  }))
}

function hasDisplayValue(value) {
  return typeof value === 'string' ? value.trim().length > 0 : Boolean(value)
}
</script>

<style scoped>
.task-card {
  display: grid;
  gap: var(--space-4);
}

/* ── Marketplace header banner ──────────────────────────────── */
.mp-header {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-lg);
  background: linear-gradient(135deg, var(--color-primary-soft) 0%, var(--color-surface) 100%);
  border: 1.5px solid var(--color-primary-soft-strong);
}

.mp-header__icon {
  flex-shrink: 0;
  font-size: 2rem;
  line-height: 1;
}

.mp-header__text {
  display: grid;
  gap: var(--space-1);
  min-width: 0;
}

.mp-header__title {
  margin: 0;
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  color: var(--color-heading);
  line-height: 1.2;
}

.mp-header__guidance {
  margin: 0;
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  line-height: 1.4;
}

.question {
  margin: 0;
  font-weight: var(--font-semibold);
}

.site-preview {
  border: 2px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  background: var(--color-surface);
}

.site-preview__url-bar {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-3);
  background: var(--color-bg);
  border-bottom: 1px solid var(--color-border);
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: var(--text-sm);
}

.site-preview__lock {
  color: var(--color-warning);
}

.site-preview__url {
  color: var(--color-text-muted);
  word-break: break-all;
}

.site-preview__img-wrap {
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.4), transparent),
    var(--color-surface);
}

.site-preview__img {
  display: block;
  width: 100%;
  max-height: 280px;
  object-fit: cover;
}

.site-preview__placeholder {
  min-height: 180px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  color: var(--color-text-muted);
  font-size: var(--text-sm);
}

.site-preview__placeholder span:first-child {
  font-size: 3rem;
}

.options,
.sites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--space-3);
}

.option-btn,
.submit-btn {
  border: 2px solid var(--color-border);
  background: var(--color-surface);
  border-radius: var(--radius-md);
  padding: var(--space-3) var(--space-4);
  cursor: pointer;
  text-align: left;
  line-height: 1.4;
  transition:
    border-color var(--transition-fast),
    background var(--transition-fast),
    transform var(--transition-fast);
}

.option-btn:hover:not(:disabled),
.submit-btn:hover:not(:disabled) {
  border-color: var(--color-primary);
}

.option-btn:disabled,
.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.option-btn--selected {
  border-color: var(--color-primary);
  background: var(--color-primary-light);
  color: var(--color-primary-dark);
  font-weight: var(--font-semibold);
}

.submit-btn {
  justify-self: start;
}

.site-card {
  display: flex;
  flex-direction: column;
  padding: 0;
  overflow: hidden;
  text-align: left;
  cursor: pointer;
  border: 2px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-surface);
  transition:
    border-color var(--transition-fast),
    box-shadow var(--transition-fast),
    transform var(--transition-fast);
}

.site-card:hover:not(:disabled) {
  border-color: var(--color-primary);
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.12);
  transform: translateY(-2px);
}

.site-card:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.site-card--selected {
  border-color: var(--color-primary);
  background: var(--color-primary-light);
}

.site-card__img-wrap {
  background: var(--color-bg);
  border-bottom: 1px solid var(--color-border);
}

.site-card__img {
  display: block;
  width: 100%;
  height: 124px;
  object-fit: cover;
}

.site-card__placeholder {
  display: grid;
  place-items: center;
  height: 96px;
  font-size: 2.5rem;
}

.site-card__body {
  display: grid;
  gap: var(--space-2);
  padding: var(--space-3);
}

.site-card__url {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: var(--text-sm);
  color: var(--color-text);
  word-break: break-all;
}

.site-card__badge {
  justify-self: start;
  padding: var(--space-1) var(--space-2);
  border-radius: var(--radius-full);
  background: var(--color-warning-light);
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
}

.site-card__meta {
  margin: 0;
  color: var(--color-text-muted);
  line-height: 1.4;
}

.inline-result {
  display: grid;
  gap: var(--space-2);
  padding: var(--space-4) var(--space-6);
  border-radius: var(--radius-lg);
}

.inline-result--correct {
  background: var(--color-success-light);
  border: 2px solid var(--color-success);
}

.inline-result--wrong {
  background: var(--color-danger-light);
  border: 2px solid var(--color-danger);
}

.inline-result__label {
  margin: 0;
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
}

.inline-result--correct .inline-result__label {
  color: var(--color-success);
}

.inline-result--wrong .inline-result__label {
  color: var(--color-danger);
}

.inline-result__explanation {
  margin: 0;
  color: var(--color-text);
  line-height: 1.5;
}

.inline-result__stop {
  margin: 0;
  font-weight: var(--font-semibold);
  color: var(--color-success);
}

.inline-result__actions {
  padding-top: var(--space-2);
}

.next-btn {
  border: none;
  border-radius: var(--radius-md);
  padding: var(--space-2) var(--space-6);
  background: var(--color-primary);
  color: var(--color-text-on-dark);
  font-weight: var(--font-semibold);
  font-size: var(--text-base);
  cursor: pointer;
  transition: background var(--transition-fast), transform var(--transition-fast);
}

.next-btn:hover {
  background: var(--color-btn-primary-hover);
}

.next-btn:active {
  transform: scale(0.98);
}

.result-slide-enter-active,
.result-slide-leave-active {
  transition: transform 0.3s ease, opacity 0.3s ease;
}

.result-slide-enter-from,
.result-slide-leave-to {
  transform: translateY(-12px);
  opacity: 0;
}

@keyframes clue-pulse {
  0%, 100% { transform: scale(1); }
  50%       { transform: scale(1.04); }
}

/* Chips */
.cs-chips { display: flex; flex-wrap: wrap; gap: var(--space-2); }
.cs-chip {
  background: var(--color-danger);
  color: #fff;
  border-radius: var(--radius-full);
  padding: 2px 10px;
  font-size: var(--text-xs);
  font-weight: 600;
}

/* Per-element result list */
.cs-result-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}
.cs-result-list__item {
  display: flex;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  font-size: var(--text-sm);
  color: var(--color-text);
  line-height: 1.45;
}
.cs-result-item--correct {
  border-color: #3b82f6;
  background: #eff6ff;
}
.cs-result-item--wrong {
  border-color: var(--color-danger);
  background: var(--color-danger-light);
}
.cs-result-item--missed {
  border-color: #93c5fd;
  background: #f0f7ff;
}
.cs-result-item--ok {
  opacity: 0.75;
}
.cs-icon--correct,
.cs-icon--wrong,
.cs-icon--missed,
.cs-icon--ok {
  flex-shrink: 0;
  font-size: 1.1em;
  line-height: 1.4;
}
.cs-icon--ok { opacity: 0.55; }

</style>
