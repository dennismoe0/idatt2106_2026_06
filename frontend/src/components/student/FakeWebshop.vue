<template>
  <div class="fake-shop">
    <div class="fake-shop__nav">{{ siteName }}</div>

    <div class="fake-shop__hero">
      <p class="fake-shop__eyebrow">{{ eyebrow }}</p>
      <h3 class="fake-shop__headline">{{ headline }}</h3>
      <p class="fake-shop__tagline">{{ tagline }}</p>
    </div>

    <div class="fake-shop__product">
      <div class="fake-shop__img-placeholder" aria-hidden="true">🛍️</div>

      <div class="fake-shop__details">
        <h3 class="fake-shop__product-name">{{ productName }}</h3>

        <div class="fake-shop__price-row">
          <p class="fake-shop__price">KR {{ price }},-</p>
          <p v-if="originalPrice" class="fake-shop__old-price">{{ originalPrice }}</p>
        </div>

        <div v-if="badges.length" class="fake-shop__badges">
          <span v-for="badge in badges" :key="badge" class="fake-shop__badge">
            {{ badge }}
          </span>
        </div>

        <p class="fake-shop__payment">{{ paymentText }}</p>
        <p class="fake-shop__contact">{{ contactText }}</p>
        <p class="fake-shop__notice">{{ notice }}</p>

        <button type="button" class="fake-shop__cta" disabled>
          {{ ctaText }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
defineOptions({
  name: 'FakeWebshop',
})

defineProps({
  siteName: { type: String, default: 'ukjent-butikk.example' },
  eyebrow: { type: String, default: 'Begrenset kampanje' },
  headline: { type: String, default: 'Supertilbud bare i dag' },
  tagline: {
    type: String,
    default: 'Sjekk nøye før du handler på nettsteder du ikke kjenner.',
  },
  productName: { type: String, default: 'Populært produkt' },
  price: { type: String, default: '99' },
  originalPrice: { type: String, default: '' },
  badges: { type: Array, default: () => [] },
  paymentText: { type: String, default: 'Betaling: Western Union / Gavekort' },
  contactText: { type: String, default: 'Kontakt: ingen info tilgjengelig' },
  notice: {
    type: String,
    default: 'Betaling og kontaktinfo bør alltid sjekkes før du handler.',
  },
  ctaText: { type: String, default: 'Kjøp nå' },
})
</script>

<style scoped>
.fake-shop {
  display: grid;
  gap: var(--space-4);
  padding: var(--space-4);
}

.fake-shop__nav {
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.7);
  color: var(--color-text);
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: var(--text-sm);
  word-break: break-all;
}

.fake-shop__hero {
  display: grid;
  gap: var(--space-2);
}

.fake-shop__eyebrow {
  margin: 0;
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  color: var(--color-danger);
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.fake-shop__headline {
  margin: 0;
  font-size: clamp(1.25rem, 3vw, 1.9rem);
  line-height: 1.1;
}

.fake-shop__tagline,
.fake-shop__payment,
.fake-shop__contact,
.fake-shop__notice {
  margin: 0;
  color: var(--color-text-muted);
}

.fake-shop__product {
  display: grid;
  grid-template-columns: minmax(88px, 120px) 1fr;
  gap: var(--space-4);
  padding: var(--space-3);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  background: rgba(255, 255, 255, 0.6);
}

.fake-shop__img-placeholder {
  display: grid;
  place-items: center;
  min-height: 104px;
  border-radius: var(--radius-md);
  background:
    radial-gradient(circle at top, rgba(255, 255, 255, 0.75), transparent 65%),
    linear-gradient(135deg, var(--color-primary-light), var(--color-bg));
  font-size: 2.6rem;
}

.fake-shop__details {
  display: grid;
  gap: var(--space-2);
  align-content: start;
}

.fake-shop__product-name {
  margin: 0;
  font-weight: var(--font-semibold);
}

.fake-shop__price-row {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: var(--space-2);
}

.fake-shop__price {
  margin: 0;
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  color: var(--color-danger);
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

.fake-shop__cta {
  justify-self: start;
  border: none;
  border-radius: var(--radius-md);
  padding: var(--space-2) var(--space-4);
  background: var(--color-danger);
  color: var(--color-text-on-dark);
  font-weight: var(--font-semibold);
  cursor: not-allowed;
}

@media (max-width: 680px) {
  .fake-shop__product {
    grid-template-columns: 1fr;
  }

  .fake-shop__img-placeholder {
    min-height: 88px;
  }
}
</style>
