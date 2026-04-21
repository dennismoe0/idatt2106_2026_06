<template>
  <section class="task-card">
    <h2>Markedsplassen</h2>
    <p class="guidance">{{ task.guidanceText }}</p>

    <template v-if="taskSubtype === 'IDENTIFY'">
      <div class="site-preview">
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

          <div v-else-if="renderMode === 'html'" class="site-preview__mockup">
            <div class="site-preview__mockup-hero">
              <p class="site-preview__eyebrow">{{ mockupContent.eyebrow }}</p>
              <h3 class="site-preview__headline">{{ mockupContent.headline }}</h3>
              <p class="site-preview__tagline">{{ mockupContent.tagline }}</p>
            </div>

            <div class="site-preview__product-card">
              <div class="site-preview__thumb" aria-hidden="true">🛒</div>

              <div class="site-preview__product-copy">
                <p class="site-preview__product-name">{{ mockupContent.productName }}</p>

                <div class="site-preview__price-row">
                  <span class="site-preview__price">{{ mockupContent.price }}</span>
                  <span v-if="mockupContent.originalPrice" class="site-preview__old-price">
                    {{ mockupContent.originalPrice }}
                  </span>
                </div>

                <div v-if="mockupContent.badges.length" class="site-preview__badges">
                  <span
                    v-for="badge in mockupContent.badges"
                    :key="badge"
                    class="site-preview__badge"
                  >
                    {{ badge }}
                  </span>
                </div>

                <button type="button" class="site-preview__cta" disabled>
                  {{ mockupContent.ctaText }}
                </button>
              </div>
            </div>

            <p class="site-preview__notice">{{ mockupContent.notice }}</p>
          </div>

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
      :disabled="selected === null"
      @click="submit"
    >
      Send svar
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
import { computed, ref, watch } from 'vue'

const props = defineProps({
  task: { type: Object, required: true },
  result: { type: Object, default: null },
  isLastTask: { type: Boolean, default: false },
})

const emit = defineEmits(['submitted', 'next'])

const selected = ref(null)

const contentJson = computed(() => props.task?.contentJson ?? {})
const taskSubtype = computed(() => normalizeSubtype(contentJson.value.type))
const renderMode = computed(() => normalizeRenderMode(contentJson.value.renderMode))
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
    ctaText: mockup.ctaText ?? 'Kjøp nå',
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
  },
  { immediate: true },
)

function submit() {
  if (selected.value === null) {
    return
  }

  console.log('[MarketplaceTask] Submitting:', selected.value)
  emit('submitted', { selected: selected.value })
}

function normalizeSubtype(type) {
  return String(type ?? 'IDENTIFY').toUpperCase() === 'RANK' ? 'RANK' : 'IDENTIFY'
}

function normalizeRenderMode(renderMode) {
  return String(renderMode ?? 'image').toLowerCase() === 'html' ? 'html' : 'image'
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

.guidance {
  margin: 0;
  color: var(--color-text-muted);
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

.site-preview__mockup {
  display: grid;
  gap: var(--space-4);
  padding: var(--space-4);
}

.site-preview__mockup-hero {
  display: grid;
  gap: var(--space-2);
}

.site-preview__eyebrow {
  margin: 0;
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  color: var(--color-danger);
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.site-preview__headline {
  margin: 0;
  font-size: clamp(1.25rem, 3vw, 1.9rem);
  line-height: 1.1;
}

.site-preview__tagline,
.site-preview__notice {
  margin: 0;
  color: var(--color-text-muted);
}

.site-preview__product-card {
  display: grid;
  grid-template-columns: minmax(88px, 120px) 1fr;
  gap: var(--space-4);
  padding: var(--space-3);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  background: rgba(255, 255, 255, 0.6);
}

.site-preview__thumb {
  display: grid;
  place-items: center;
  min-height: 104px;
  border-radius: var(--radius-md);
  background:
    radial-gradient(circle at top, rgba(255, 255, 255, 0.75), transparent 65%),
    linear-gradient(135deg, var(--color-primary-light), var(--color-bg));
  font-size: 2.6rem;
}

.site-preview__product-copy {
  display: grid;
  gap: var(--space-2);
  align-content: start;
}

.site-preview__product-name {
  margin: 0;
  font-weight: var(--font-semibold);
}

.site-preview__price-row {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: var(--space-2);
}

.site-preview__price {
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  color: var(--color-danger);
}

.site-preview__old-price {
  color: var(--color-text-muted);
  text-decoration: line-through;
}

.site-preview__badges {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.site-preview__badge {
  padding: var(--space-1) var(--space-2);
  border-radius: var(--radius-full);
  background: var(--color-warning-light);
  color: var(--color-text);
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
}

.site-preview__cta {
  justify-self: start;
  border: none;
  border-radius: var(--radius-md);
  padding: var(--space-2) var(--space-4);
  background: var(--color-danger);
  color: var(--color-text-on-dark);
  font-weight: var(--font-semibold);
  cursor: not-allowed;
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

@media (max-width: 680px) {
  .site-preview__product-card {
    grid-template-columns: 1fr;
  }

  .site-preview__thumb {
    min-height: 88px;
  }
}
</style>
