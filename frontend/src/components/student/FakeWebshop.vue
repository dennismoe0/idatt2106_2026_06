<template>
  <article class="fake-shop">
    <div class="fake-shop__browser">
      <span class="fake-shop__browser-dot" />
      <span class="fake-shop__browser-dot" />
      <span class="fake-shop__browser-dot" />
      <div class="fake-shop__nav">
        <span class="fake-shop__nav-lock" aria-hidden="true">🔒</span>
        <component
          :is="fieldTag('domain')"
          class="fake-shop__nav-url"
          :class="fieldClass('domain')"
          v-bind="fieldAttrs('domain')"
          @click="handleToggle('domain')"
        >
          <span v-if="fieldMarker('domain')" class="fake-shop__marker">{{ fieldMarker('domain') }}</span>
          {{ fieldText('domain', siteName) }}
        </component>
      </div>
    </div>

    <header class="fake-shop__hero">
      <p class="fake-shop__eyebrow">{{ eyebrow }}</p>
      <h3 class="fake-shop__headline">{{ headline }}</h3>
      <p class="fake-shop__tagline">{{ tagline }}</p>
      <div class="fake-shop__trust-row">
        <component
          :is="fieldTag('seller')"
          class="fake-shop__trust-pill"
          :class="fieldClass('seller')"
          v-bind="fieldAttrs('seller')"
          @click="handleToggle('seller')"
        >
          <span v-if="fieldMarker('seller')" class="fake-shop__marker">{{ fieldMarker('seller') }}</span>
          {{ fieldText('seller', sellerText) }}
        </component>
        <component
          :is="fieldTag('shipping')"
          class="fake-shop__trust-pill"
          :class="fieldClass('shipping')"
          v-bind="fieldAttrs('shipping')"
          @click="handleToggle('shipping')"
        >
          <span v-if="fieldMarker('shipping')" class="fake-shop__marker">{{ fieldMarker('shipping') }}</span>
          {{ fieldText('shipping', shippingText) }}
        </component>
      </div>
    </header>

    <section class="fake-shop__product">
      <div class="fake-shop__gallery">
        <img
          v-if="productImageUrl"
          :src="productImageUrl"
          :alt="productName || ''"
          class="fake-shop__product-img"
        />
        <div v-else class="fake-shop__img-placeholder" aria-hidden="true">🛍️</div>
      </div>

      <div class="fake-shop__details">
        <component
          :is="fieldTag('productName')"
          class="fake-shop__product-name"
          :class="fieldClass('productName')"
          v-bind="fieldAttrs('productName')"
          @click="handleToggle('productName')"
        >
          <span v-if="fieldMarker('productName')" class="fake-shop__marker">{{ fieldMarker('productName') }}</span>
          {{ fieldText('productName', productName) }}
        </component>

        <div class="fake-shop__price-row">
          <component
            :is="fieldTag('price')"
            class="fake-shop__price"
            :class="fieldClass('price')"
            v-bind="fieldAttrs('price')"
            @click="handleToggle('price')"
          >
            <span v-if="fieldMarker('price')" class="fake-shop__marker">{{ fieldMarker('price') }}</span>
            {{ fieldText('price', price) }}
          </component>
          <p v-if="originalPrice" class="fake-shop__old-price">{{ originalPrice }}</p>
        </div>

        <div v-if="badges.length" class="fake-shop__badges">
          <span v-for="badge in badges" :key="badge" class="fake-shop__badge">
            {{ badge }}
          </span>
        </div>

        <div class="fake-shop__facts">
          <div class="fake-shop__fact">
            <span class="fake-shop__fact-label">Betaling</span>
            <component
              :is="fieldTag('payment')"
              class="fake-shop__fact-value"
              :class="fieldClass('payment')"
              v-bind="fieldAttrs('payment')"
              @click="handleToggle('payment')"
            >
              <span v-if="fieldMarker('payment')" class="fake-shop__marker">{{ fieldMarker('payment') }}</span>
              {{ fieldText('payment', paymentText) }}
            </component>
          </div>

          <div class="fake-shop__fact">
            <span class="fake-shop__fact-label">Kontakt</span>
            <component
              :is="fieldTag('contact')"
              class="fake-shop__fact-value"
              :class="fieldClass('contact')"
              v-bind="fieldAttrs('contact')"
              @click="handleToggle('contact')"
            >
              <span v-if="fieldMarker('contact')" class="fake-shop__marker">{{ fieldMarker('contact') }}</span>
              {{ fieldText('contact', contactText) }}
            </component>
          </div>

          <div class="fake-shop__fact">
            <span class="fake-shop__fact-label">Retur</span>
            <component
              :is="fieldTag('returnPolicy')"
              class="fake-shop__fact-value"
              :class="fieldClass('returnPolicy')"
              v-bind="fieldAttrs('returnPolicy')"
              @click="handleToggle('returnPolicy')"
            >
              <span v-if="fieldMarker('returnPolicy')" class="fake-shop__marker">{{ fieldMarker('returnPolicy') }}</span>
              {{ fieldText('returnPolicy', returnPolicyText) }}
            </component>
          </div>
        </div>

        <p class="fake-shop__notice">{{ notice }}</p>

        <button type="button" class="fake-shop__cta" disabled>
          {{ ctaText }}
        </button>
      </div>
    </section>
  </article>
</template>

<script setup>
defineOptions({
  name: 'FakeWebshop',
})

const props = defineProps({
  siteName: { type: String, default: 'ukjent-butikk.example' },
  eyebrow: { type: String, default: 'Begrenset kampanje' },
  headline: { type: String, default: 'Supertilbud bare i dag' },
  tagline: {
    type: String,
    default: 'Sjekk nøye før du handler på nettsteder du ikke kjenner.',
  },
  productName: { type: String, default: 'Populært produkt' },
  price: { type: String, default: '99 kr' },
  originalPrice: { type: String, default: '' },
  badges: { type: Array, default: () => [] },
  paymentText: { type: String, default: 'Visa / Mastercard' },
  contactText: { type: String, default: 'kontakt@butikk.no' },
  returnPolicyText: { type: String, default: '14 dagers returrett' },
  sellerText: { type: String, default: 'Solgt av Nordisk Butikk AS' },
  shippingText: { type: String, default: 'Levering 2-4 virkedager' },
  ratingText: { type: String, default: '4.8 av 5 stjerner' },
  notice: {
    type: String,
    default: 'Sjekk alltid betalingsvalg, kontaktinfo og returregler før du handler.',
  },
  ctaText: { type: String, default: 'Legg i handlekurv' },
  productImageUrl: { type: String, default: '' },
  clickableElements: { type: Array, default: () => [] },
  flaggedElements: { type: Set, default: () => new Set() },
  feedbackStates: { type: Object, default: () => ({}) },
  fieldMarkers: { type: Object, default: () => ({}) },
  disabled: { type: Boolean, default: false },
})

const emit = defineEmits(['toggle'])

function elementById(id) {
  return props.clickableElements.find((element) => element.id === id) ?? null
}

function fieldText(id, fallback) {
  const label = elementById(id)?.label
  return typeof label === 'string' && label.trim().length > 0 ? label : fallback
}

function fieldTag(id) {
  return elementById(id) ? 'button' : 'span'
}

function fieldAttrs(id) {
  if (!elementById(id)) return {}
  return {
    type: 'button',
    disabled: props.disabled,
    'aria-pressed': props.flaggedElements.has(id),
  }
}

function fieldClass(id) {
  const state = props.feedbackStates[id]
  return {
    'fake-shop__field': Boolean(elementById(id)),
    'fake-shop__field--flagged': state === 'flagged',
    'fake-shop__field--correct': state === 'correct',
    'fake-shop__field--wrong': state === 'wrong',
    'fake-shop__field--missed': state === 'missed',
    'fake-shop__field--focus': state === 'focus',
  }
}

function fieldMarker(id) {
  return props.fieldMarkers[id] ?? ''
}

function handleToggle(id) {
  if (!elementById(id) || props.disabled) return
  emit('toggle', id)
}
</script>

<style scoped>
.fake-shop {
  display: grid;
  gap: var(--space-4);
  padding: var(--space-4);
  border-radius: var(--radius-lg);
  background: linear-gradient(180deg, var(--color-surface) 0%, var(--color-bg) 100%);
  border: 1px solid var(--color-border);
}

.fake-shop__browser {
  display: grid;
  grid-template-columns: auto auto auto minmax(0, 1fr);
  gap: var(--space-2);
  align-items: center;
}

.fake-shop__browser-dot {
  width: 10px;
  height: 10px;
  border-radius: var(--radius-full);
  background: var(--color-border-strong);
}

.fake-shop__nav {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  min-width: 0;
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-md);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
}

.fake-shop__nav-lock {
  flex-shrink: 0;
}

.fake-shop__nav-url {
  min-width: 0;
  color: var(--color-text-muted);
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: var(--text-sm);
  word-break: break-all;
}

.fake-shop__hero {
  display: grid;
  gap: var(--space-2);
}

.fake-shop__trust-row {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.fake-shop__eyebrow {
  margin: 0;
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--color-danger);
}

.fake-shop__headline {
  margin: 0;
  color: var(--color-heading);
  font-size: clamp(1.35rem, 3vw, 2.1rem);
  line-height: 1.1;
}

.fake-shop__tagline {
  margin: 0;
  color: var(--color-text-muted);
  line-height: 1.55;
}

.fake-shop__trust-pill {
  display: inline-flex;
  align-items: center;
  padding: var(--space-1) var(--space-2);
  border-radius: var(--radius-full);
  background: var(--color-success-light);
  color: var(--color-heading);
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
}

.fake-shop__product {
  display: grid;
  grid-template-columns: clamp(120px, 28%, 260px) minmax(0, 1fr);
  gap: var(--space-4);
  padding: var(--space-4);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
}

.fake-shop__gallery {
  display: grid;
  gap: var(--space-2);
  align-self: start;
}

.fake-shop__product-img {
  display: block;
  width: 100%;
  height: auto;
  border-radius: var(--radius-md);
}

.fake-shop__img-placeholder {
  display: grid;
  place-items: center;
  aspect-ratio: 1 / 1;
  border-radius: var(--radius-md);
  background:
    radial-gradient(circle at top, color-mix(in srgb, var(--color-surface) 75%, transparent), transparent 60%),
    linear-gradient(135deg, var(--color-primary-light), var(--color-bg));
  font-size: 3rem;
}

.fake-shop__details {
  display: grid;
  gap: var(--space-3);
  align-content: start;
}

.fake-shop__product-name {
  margin: 0;
  color: var(--color-heading);
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  line-height: 1.3;
}

.fake-shop__price-row {
  display: flex;
  align-items: baseline;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.fake-shop__price {
  margin: 0;
  color: var(--color-danger);
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
}

.fake-shop__old-price {
  margin: 0;
  color: var(--color-text-muted);
  text-decoration: line-through;
}

.fake-shop__badges {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.fake-shop__badge {
  padding: var(--space-1) var(--space-2);
  border-radius: var(--radius-full);
  background: var(--color-warning-light);
  color: var(--color-text);
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
}

.fake-shop__facts {
  display: grid;
  gap: var(--space-2);
  padding: var(--space-3);
  border-radius: var(--radius-md);
  background: var(--color-surface-soft);
  border: 1px solid var(--color-border);
}

.fake-shop__fact {
  display: grid;
  gap: var(--space-1);
}

.fake-shop__fact-label {
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--color-text-muted);
}

.fake-shop__fact-value {
  color: var(--color-text);
  line-height: 1.5;
}

.fake-shop__notice {
  margin: 0;
  color: var(--color-text-muted);
  line-height: 1.5;
}

.fake-shop__cta {
  justify-self: start;
  border: none;
  border-radius: var(--radius-md);
  padding: var(--space-3) var(--space-4);
  background: var(--color-danger);
  color: var(--color-text-on-dark);
  font-weight: var(--font-semibold);
  cursor: not-allowed;
}

.fake-shop__field {
  display: inline;
  text-align: left;
  border: 1.5px dashed var(--color-border);
  border-radius: 3px;
  padding: 1px 5px;
  background: color-mix(in srgb, var(--color-surface) 90%, transparent);
  color: inherit;
  font: inherit;
  cursor: pointer;
  transition: background var(--transition-fast), border-color var(--transition-fast);
}

.fake-shop__trust-pill.fake-shop__field,
.fake-shop__summary-value.fake-shop__field {
  display: inline-flex;
}

.fake-shop__field:hover:not(:disabled) {
  background: var(--color-warning-light);
  border-style: solid;
}

.fake-shop__field:disabled {
  cursor: not-allowed;
}

.fake-shop__field--flagged {
  background: var(--color-danger-light);
  border-color: var(--color-danger);
  border-style: solid;
  color: var(--color-danger);
  font-weight: var(--font-semibold);
}

.fake-shop__field--correct {
  background: var(--color-success-light);
  border-color: var(--color-success);
  border-style: solid;
  color: var(--color-success);
}

.fake-shop__field--wrong {
  background: var(--color-danger-light);
  border-color: var(--color-danger);
  border-style: solid;
  color: var(--color-danger);
}

.fake-shop__field--missed {
  background: var(--color-warning-light);
  border-color: var(--color-warning);
  border-style: solid;
}

.fake-shop__field--focus {
  background: color-mix(in srgb, var(--color-warning-light) 70%, white);
  border-color: var(--color-accent);
  border-style: solid;
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--color-warning-light) 45%, transparent);
}

.fake-shop__marker {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 1.35rem;
  height: 1.35rem;
  margin-right: var(--space-2);
  border-radius: var(--radius-full);
  background: var(--color-accent);
  color: var(--color-text-on-dark);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  line-height: 1;
  vertical-align: middle;
}

@media (max-width: 680px) {
  .fake-shop__browser {
    grid-template-columns: auto auto auto;
  }

  .fake-shop__nav {
    grid-column: 1 / -1;
  }

  .fake-shop__product {
    grid-template-columns: 1fr;
  }

  .fake-shop__product-img,
  .fake-shop__img-placeholder {
    aspect-ratio: unset;
  }
}
</style>
