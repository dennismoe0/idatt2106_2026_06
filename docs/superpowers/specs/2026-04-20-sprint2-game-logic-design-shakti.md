# Sprint 2 — Shakti: MarketplaceTask.vue (Frontend)
*Part of: 2026-04-20-sprint2-game-logic-design.md*

You own the **frontend** for Stop 5 (Markedsplassen).
Backend validation and seeds are done by **Christian**.

Read section 2.3 of the main design doc for the contentJson contract.

Two task subtypes:
- **IDENTIFY** (tasks 1 & 2): Image of a fake webshop + multiple choice question
- **RANK** (task 3): Four site cards, pick the scam

Start with C (image + MC). If time allows, upgrade IDENTIFY to show actual HTML mockup of a fake site.

---

## MarketplaceTask.vue

File: `frontend/src/components/student/MarketplaceTask.vue`

```vue
<template>
  <section class="task-card">
    <h2>Markedsplassen</h2>
    <p class="guidance">{{ task.guidanceText }}</p>

    <!-- IDENTIFY subtype -->
    <template v-if="taskSubtype === 'IDENTIFY'">
      <!-- Webshop screenshot or placeholder -->
      <div class="site-preview">
        <div class="site-preview__url-bar">
          <span class="site-preview__lock">🔓</span>
          <span class="site-preview__url">{{ contentJson.siteName }}</span>
        </div>
        <div class="site-preview__img-wrap">
          <img
            v-if="contentJson.imageUrl"
            :src="contentJson.imageUrl"
            :alt="`Skjermbilde av ${contentJson.siteName}`"
            class="site-preview__img"
          />
          <div v-else class="site-preview__placeholder">
            <span aria-hidden="true">🛒</span>
            <span>{{ contentJson.siteName }}</span>
          </div>
        </div>
      </div>

      <p class="question">{{ contentJson.question }}</p>

      <div class="options">
        <button
          v-for="opt in contentJson.options"
          :key="opt.id"
          class="option-btn"
          :class="{ 'option-btn--selected': selected === opt.id }"
          :disabled="!!result"
          @click="selected = opt.id"
        >
          {{ opt.text }}
        </button>
      </div>
    </template>

    <!-- RANK subtype -->
    <template v-else-if="taskSubtype === 'RANK'">
      <p class="question">{{ contentJson.question }}</p>
      <div class="sites-grid">
        <button
          v-for="site in contentJson.sites"
          :key="site.id"
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
          <span class="site-card__url">{{ site.name }}</span>
        </button>
      </div>
    </template>

    <button
      v-if="!result"
      class="submit-btn"
      :disabled="!selected"
      @click="submit"
    >
      Send svar
    </button>

    <!-- Inline result -->
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
          <button class="next-btn" @click="$emit('next')">
            {{ isLastTask ? 'Videre til sammendrag →' : 'Neste oppgave →' }}
          </button>
        </div>
      </div>
    </Transition>
  </section>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  task:       { type: Object,  required: true },
  result:     { type: Object,  default: null },
  isLastTask: { type: Boolean, default: false },
})
const emit = defineEmits(['submitted', 'next'])

const selected    = ref(null)
const contentJson = computed(() => props.task?.contentJson ?? {})
const taskSubtype = computed(() => contentJson.value.type ?? 'IDENTIFY')

watch(() => props.task?.id, () => { selected.value = null }, { immediate: true })

function submit() {
  if (!selected.value) return
  console.log('[MarketplaceTask] Submitting:', selected.value)
  emit('submitted', { selected: selected.value })
}
</script>

<style scoped>
.task-card { display: grid; gap: var(--space-4); }
.guidance  { margin: 0; color: var(--color-text-muted); }
.question  { margin: 0; font-weight: var(--font-semibold); }

/* Site preview (IDENTIFY) */
.site-preview {
  border: 2px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  background: var(--color-surface);
}
.site-preview__url-bar {
  display: flex; align-items: center; gap: var(--space-2);
  background: var(--color-bg);
  border-bottom: 1px solid var(--color-border);
  padding: var(--space-2) var(--space-3);
  font-size: var(--text-sm);
  font-family: monospace;
}
.site-preview__lock { color: var(--color-warning); }
.site-preview__url  { color: var(--color-text-muted); }
.site-preview__img { width: 100%; max-height: 260px; object-fit: cover; display: block; }
.site-preview__placeholder {
  height: 180px; display: flex; flex-direction: column;
  align-items: center; justify-content: center; gap: var(--space-2);
  color: var(--color-text-muted); font-size: var(--text-sm);
}
.site-preview__placeholder span:first-child { font-size: 3rem; }

/* Answer options (IDENTIFY) */
.options {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--space-3);
}
.option-btn {
  border: 2px solid var(--color-border);
  background: var(--color-surface);
  border-radius: var(--radius-md);
  padding: var(--space-3) var(--space-4);
  cursor: pointer; text-align: left; line-height: 1.4;
  transition: border-color var(--transition-fast), background var(--transition-fast);
}
.option-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.option-btn--selected {
  border-color: var(--color-primary);
  background: var(--color-primary-light);
  color: var(--color-primary-dark);
  font-weight: var(--font-semibold);
}

/* Site grid (RANK) */
.sites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--space-3);
}
.site-card {
  border: 2px solid var(--color-border);
  background: var(--color-surface);
  border-radius: var(--radius-md);
  padding: 0;
  cursor: pointer; overflow: hidden;
  display: flex; flex-direction: column;
  transition: border-color var(--transition-fast), box-shadow var(--transition-fast);
  text-align: left;
}
.site-card:hover:not(:disabled) { border-color: var(--color-primary); box-shadow: 0 2px 8px rgba(0,0,0,0.15); }
.site-card:disabled { opacity: 0.5; cursor: not-allowed; }
.site-card--selected { border-color: var(--color-primary); background: var(--color-primary-light); }
.site-card__img-wrap { background: var(--color-bg); border-bottom: 1px solid var(--color-border); }
.site-card__img { width: 100%; height: 120px; object-fit: cover; display: block; }
.site-card__placeholder {
  height: 90px; display: flex; align-items: center; justify-content: center;
  font-size: 2.5rem;
}
.site-card__url {
  padding: var(--space-2) var(--space-3);
  font-family: monospace; font-size: var(--text-sm);
  color: var(--color-text); word-break: break-all;
}

/* Submit */
.submit-btn {
  justify-self: start;
  border: 1px solid var(--color-border); background: var(--color-surface);
  border-radius: var(--radius-md); padding: var(--space-2) var(--space-4);
  cursor: pointer; transition: background var(--transition-fast);
}
.submit-btn:disabled { opacity: 0.5; cursor: not-allowed; }

/* Inline result */
.inline-result { border-radius: var(--radius-lg); padding: var(--space-4) var(--space-6); display: grid; gap: var(--space-2); }
.inline-result--correct { background: var(--color-success-light); border: 2px solid var(--color-success); }
.inline-result--wrong   { background: var(--color-danger-light);  border: 2px solid var(--color-danger); }
.inline-result__label { margin: 0; font-size: var(--text-lg); font-weight: var(--font-bold); }
.inline-result--correct .inline-result__label { color: var(--color-success); }
.inline-result--wrong   .inline-result__label { color: var(--color-danger); }
.inline-result__explanation { margin: 0; color: var(--color-text); line-height: 1.5; }
.inline-result__stop { margin: 0; font-weight: var(--font-semibold); color: var(--color-success); }
.inline-result__actions { padding-top: var(--space-2); }
.next-btn {
  background: var(--color-primary); color: var(--color-text-on-dark);
  border: none; border-radius: var(--radius-md);
  padding: var(--space-2) var(--space-6);
  font-weight: var(--font-semibold); font-size: var(--text-base); cursor: pointer;
  transition: background var(--transition-fast), transform var(--transition-fast);
}
.next-btn:hover  { background: var(--color-btn-primary-hover); }
.next-btn:active { transform: scale(0.98); }
.result-slide-enter-active { transition: transform 0.3s ease, opacity 0.3s ease; }
.result-slide-enter-from   { transform: translateY(-12px); opacity: 0; }
</style>
```

---

## Upgrade path (if time): HTML mockup for IDENTIFY

If time allows, replace the `<img>` in IDENTIFY with an actual styled fake webshop. Make a `FakeWebshop.vue` component that renders a mini HTML page using the data from `contentJson`:

```vue
<!-- FakeWebshop.vue rough structure -->
<div class="fake-shop">
  <div class="fake-shop__nav">{{ siteName }}</div>
  <div class="fake-shop__product">
    <div class="fake-shop__img-placeholder">🛍️</div>
    <div class="fake-shop__details">
      <h3>{{ productName }}</h3>
      <p class="fake-shop__price">KR {{ price }},-</p>
      <p class="fake-shop__payment">Betaling: Western Union / Gavekort</p>
      <p class="fake-shop__contact">Kontakt: ingen info tilgjengelig</p>
    </div>
  </div>
</div>
```

This can be toggled with a feature flag in contentJson: `"renderMode": "html"` vs `"renderMode": "image"`. Default is `"image"`.
