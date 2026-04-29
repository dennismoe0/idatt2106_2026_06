<template>
  <section class="journal-page-content" :class="[`journal-page-content--${side}`, `journal-page-content--${page.kind}`]">
    <p class="journal-page-content__doodle" aria-hidden="true">{{ page.doodle }}</p>

    <header class="journal-page-content__header">
      <p class="journal-page-content__eyebrow">{{ page.eyebrow }}</p>
      <h3 class="journal-page-content__title">{{ page.title }}</h3>
      <p v-if="page.subtitle" class="journal-page-content__subtitle">{{ page.subtitle }}</p>
      <p v-if="page.partLabel" class="journal-page-content__part">{{ page.partLabel }}</p>

      <button
        v-if="interactive && page.kind === 'stop' && page.showComposer && page.stopId != null"
        class="journal-page-content__add"
        type="button"
        aria-label="Legg til observasjon"
        title="Legg til observasjon"
        @click="startStopEdit?.(page.stopId)"
      >
        <span aria-hidden="true">+</span>
      </button>

      <button
        v-else-if="interactive && page.kind === 'general' && page.showComposer"
        class="journal-page-content__add"
        type="button"
        aria-label="Legg til notat"
        title="Legg til notat"
        @click="startAddNote?.()"
      >
        <span aria-hidden="true">+</span>
      </button>
    </header>

    <div v-if="page.kind === 'report'" class="journal-page-content__body journal-page-content__body--report">
      <div v-if="page.showStamp" class="journal-stamp" :class="{ 'journal-stamp--locked': !page.unlocked }">{{ page.stamp }}</div>

      <div class="journal-report-card" :class="{ 'journal-report-card--locked': !page.unlocked }">
        <p class="journal-report-card__label">{{ page.unlocked ? 'Ny rapport sikret' : 'Siden holdes av til neste belønning' }}</p>
        <p class="journal-report-card__text">{{ page.content }}</p>
      </div>

      <p v-if="page.createdAt" class="journal-page-content__timestamp">Arkivert {{ formatDate(page.createdAt) }}</p>
    </div>

    <div v-else-if="page.kind === 'stop' || page.kind === 'general'" class="journal-page-content__body">
      <div class="journal-page-content__blocks">
        <template v-for="block in page.blocks" :key="block.key">
          <article v-if="block.type === 'tip'" class="journal-entry-card journal-entry-card--tip">
            <p class="journal-entry-card__label">{{ block.label }}</p>
            <div class="journal-entry-card__scroll">
              <p class="journal-entry-card__text">{{ block.content }}</p>
              <p v-if="block.continuedFromPrevious || block.continuesToNext" class="journal-entry-card__continuation">
                {{ continuationText(block) }}
              </p>
            </div>
          </article>

          <article v-else-if="block.type === 'clue'" class="journal-entry-card journal-entry-card--clue">
            <p class="journal-entry-card__label journal-entry-card__label--clue">
              <span class="journal-entry-card__clue-icon" aria-hidden="true">🔎</span>
              {{ block.label }}
            </p>
            <div class="journal-entry-card__scroll">
              <p class="journal-entry-card__text">{{ block.content }}</p>
              <p v-if="block.continuedFromPrevious || block.continuesToNext" class="journal-entry-card__continuation">
                {{ continuationText(block) }}
              </p>
            </div>
          </article>

          <article v-else-if="block.type === 'reflection'" class="journal-entry-card">
            <p class="journal-entry-card__label">{{ block.label }}</p>
            <div class="journal-entry-card__scroll">
              <p class="journal-entry-card__text">{{ block.content }}</p>
            </div>
            <div class="journal-entry-card__footer">
              <time v-if="block.createdAt" class="journal-entry-card__meta">
                {{ formatDate(block.createdAt) }}
              </time>
              <div v-if="interactive && block.showControls" class="journal-entry-card__actions">
                <button
                  class="journal-text-button"
                  type="button"
                  @click="startReflectionEdit?.(block.source)"
                >Rediger</button>
                <button
                  class="journal-text-button journal-text-button--danger"
                  type="button"
                  @click="removeReflection?.(block.id)"
                >Slett</button>
              </div>
            </div>
          </article>

          <article v-else-if="block.type === 'note'" class="journal-entry-card journal-entry-card--general">
            <p class="journal-entry-card__label">{{ block.label }}</p>
            <div class="journal-entry-card__scroll">
              <p class="journal-entry-card__text">{{ block.content }}</p>
              <p v-if="block.continuedFromPrevious || block.continuesToNext" class="journal-entry-card__continuation">
                {{ continuationText(block) }}
              </p>
            </div>
            <div class="journal-entry-card__footer">
              <time v-if="block.createdAt" class="journal-entry-card__meta">{{ formatDate(block.createdAt) }}</time>
              <div v-if="interactive && block.showControls" class="journal-entry-card__actions">
                <button class="journal-text-button" type="button" @click="startNoteEdit?.(block.source)">Rediger</button>
                <button class="journal-text-button journal-text-button--danger" type="button" @click="removeNote?.(block.id)">Slett</button>
              </div>
            </div>
          </article>

          <article v-else-if="block.type === 'writing-space'" class="journal-entry-card journal-entry-card--writing">
            <p class="journal-entry-card__label">{{ block.label }}</p>
            <div class="journal-entry-card__writing-space" aria-hidden="true"></div>
          </article>

          <p v-else class="journal-empty">{{ block.content }}</p>
        </template>
      </div>
    </div>

    <div v-else class="journal-page-content__body journal-page-content__body--blank">
      <p class="journal-empty">Denne siden er klar for neste funn.</p>
      <div class="journal-blank-lines" aria-hidden="true"></div>
      <p class="journal-page-content__quote">"Noen ganger skjuler det viktigste sporet seg i margen."</p>
    </div>
  </section>
</template>

<script setup>
defineProps({
  page: {
    type: Object,
    required: true
  },
  side: {
    type: String,
    default: 'right'
  },
  interactive: {
    type: Boolean,
    default: false
  },
  startStopEdit: Function,
  startAddNote: Function,
  startNoteEdit: Function,
  removeNote: Function,
  startReflectionEdit: Function,
  removeReflection: Function
})

function continuationText(block) {
  if (block.continuedFromPrevious && block.continuesToNext) return 'Fortsetter fra forrige side og videre på neste side.'
  if (block.continuedFromPrevious) return 'Fortsetter fra forrige side.'
  if (block.continuesToNext) return 'Fortsetter på neste side.'
  return ''
}

function formatDate(isoString) {
  if (!isoString) return ''

  return new Intl.DateTimeFormat('nb-NO', {
    day: 'numeric',
    month: 'short',
    year: 'numeric'
  }).format(new Date(isoString))
}
</script>

<style scoped>
.journal-page-content {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: clamp(0.45rem, 1.2vh, 1rem);
  height: 100%;
  padding: clamp(0.7rem, 2vh, 1.35rem) clamp(0.6rem, 2vw, 1.2rem) clamp(1.4rem, 4vh, 3rem);
  color: var(--color-journal-ink);
  font-family: Georgia, 'Times New Roman', serif;
  overflow: hidden;
  container-type: inline-size;
}

.journal-page-content::before {
  content: '';
  position: absolute;
  top: 1rem;
  bottom: 1rem;
  width: 1px;
  background: linear-gradient(180deg, var(--color-journal-rule-soft), var(--color-journal-rule-strong), var(--color-journal-rule-soft));
}

.journal-page-content--left::before {
  left: 1rem;
}

.journal-page-content--right::before {
  right: 1rem;
}

.journal-page-content__doodle {
  position: absolute;
  bottom: 2rem;
  right: 1.5rem;
  margin: 0;
  font-size: 0.62rem;
  letter-spacing: 0.08em;
  color: color-mix(in srgb, var(--color-journal-ink) 28%, transparent);
  font-family: 'Segoe Print', 'Bradley Hand', 'Comic Sans MS', cursive;
  transform: rotate(-5deg);
}

.journal-page-content--left .journal-page-content__doodle {
  right: auto;
  left: 1.5rem;
  transform: rotate(4deg);
}

.journal-page-content__header {
  position: relative;
  z-index: 1;
  padding-inline: 0.65rem;
  padding-right: 3.2rem;
}

.journal-page-content__add {
  position: absolute;
  top: 0;
  right: 0.65rem;
  width: 38px;
  height: 38px;
  border-radius: 999px;
  border: 1px solid color-mix(in srgb, var(--color-journal-highlight) 22%, transparent);
  background: linear-gradient(135deg, var(--color-journal-leather-top), var(--color-journal-leather-bottom));
  color: var(--color-journal-parchment-light);
  font-size: 1.25rem;
  font-weight: 700;
  line-height: 1;
  cursor: pointer;
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.22);
  transition: transform 160ms ease, box-shadow 160ms ease;
}

.journal-page-content__add:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 18px rgba(0, 0, 0, 0.26);
}

.journal-page-content__add:focus-visible {
  outline: 3px solid var(--color-journal-highlight);
  outline-offset: 2px;
}

.journal-page-content__eyebrow {
  margin: 0 0 0.25rem;
  font-size: 0.68rem;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: color-mix(in srgb, var(--color-journal-ink) 78%, transparent);
}

.journal-page-content__title {
  margin: 0;
  font-size: clamp(1rem, 1.9vw, 1.3rem);
  line-height: 1.14;
  color: var(--color-journal-ink-strong);
  font-family: 'Segoe Print', 'Bradley Hand', 'Comic Sans MS', cursive;
}

.journal-page-content__subtitle {
  margin: 0.25rem 0 0;
  font-size: 0.88rem;
  color: color-mix(in srgb, var(--color-journal-ink) 80%, transparent);
}

.journal-page-content__part {
  margin: 0.32rem 0 0;
  font-size: 0.74rem;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: color-mix(in srgb, var(--color-journal-ink) 62%, transparent);
}

.journal-page-content__body {
  position: relative;
  z-index: 1;
  flex: 1 1 auto;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: clamp(0.45rem, 1.2vh, 0.9rem);
  padding: 0 clamp(0.35rem, 1.2vw, 0.65rem);
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: rgba(124, 85, 33, 0.35) transparent;
}

.journal-page-content__body::-webkit-scrollbar {
  width: 6px;
}

.journal-page-content__body::-webkit-scrollbar-thumb {
  background: rgba(124, 85, 33, 0.3);
  border-radius: 999px;
}

.journal-page-content__body--report {
  justify-content: flex-start;
}

.journal-page-content__body--blank {
  justify-content: center;
}

.journal-page-content__blocks {
  flex: 0 0 auto;
  display: flex;
  flex-direction: column;
  gap: clamp(0.4rem, 1vh, 0.8rem);
  min-height: 0;
}

.journal-stamp {
  align-self: flex-start;
  padding: 0.25rem 0.58rem;
  border: 2px solid var(--color-journal-stamp-border);
  color: var(--color-journal-stamp);
  font-size: 0.72rem;
  font-weight: 800;
  letter-spacing: 0.15em;
  transform: rotate(-5deg);
}

.journal-stamp--locked {
  border-color: var(--color-journal-locked-border);
  color: var(--color-journal-locked);
}

.journal-report-card,
.journal-entry-card {
  padding: clamp(0.55rem, 1.3vw, 0.88rem) clamp(0.6rem, 1.5vw, 0.96rem);
  border-radius: 14px;
  background: rgba(255, 250, 242, 0.42);
  border: 1px solid rgba(138, 96, 44, 0.18);
  box-shadow: 0 4px 12px rgba(118, 88, 39, 0.04);
}

.journal-entry-card {
  position: relative;
  display: flex;
  flex-direction: column;
  /* Cards size to their content; the page body scrolls if the total overflows */
  flex: 0 0 auto;
  max-height: none;
}

.journal-entry-card__scroll {
  flex: 0 0 auto;
  overflow: visible;
}

.journal-report-card--locked {
  background: rgba(244, 236, 219, 0.42);
}

.journal-entry-card--tip {
  background: rgba(242, 233, 217, 0.5);
}

.journal-entry-card--clue {
  background:
    linear-gradient(135deg, var(--color-journal-clue-bg-top), var(--color-journal-clue-bg-bottom));
  border-color: var(--color-journal-clue-border);
  box-shadow:
    0 4px 12px rgba(118, 88, 39, 0.08),
    inset 0 0 0 1px var(--color-journal-clue-inner);
}

.journal-entry-card__label--clue {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  color: var(--color-journal-stamp);
  font-weight: 800;
}

.journal-entry-card__clue-icon {
  font-size: 0.9rem;
  line-height: 1;
}

.journal-report-card__label,
.journal-entry-card__label {
  margin: 0 0 0.3rem;
  font-size: 0.68rem;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: color-mix(in srgb, var(--color-journal-ink) 70%, transparent);
}

.journal-report-card__text,
.journal-entry-card__text {
  margin: 0;
  font-size: clamp(0.78rem, 1.1vw + 0.55rem, 0.92rem);
  line-height: 1.55;
  /* Wrap normally; only break unbroken strings (e.g. 500x "M") via overflow-wrap. */
  overflow-wrap: anywhere;
  word-break: normal;
  hyphens: auto;
}

.journal-entry-card__continuation {
  margin: 0.35rem 0 0;
  font-size: 0.72rem;
  color: color-mix(in srgb, var(--color-journal-ink) 62%, transparent);
  font-style: italic;
}

.journal-entry-card__footer {
  flex: 0 0 auto;
  margin-top: 0.45rem;
  padding-top: 0.45rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.55rem;
  background: transparent;
}

.journal-entry-card__meta {
  font-size: 0.74rem;
  color: color-mix(in srgb, var(--color-journal-ink) 68%, transparent);
  font-style: italic;
}

.journal-entry-card__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.journal-entry-card--writing {
  min-height: 7.25rem;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  background: rgba(255, 253, 248, 0.34);
  border-style: dashed;
}

.journal-entry-card__writing-space {
  flex: 1;
  min-height: 5.4rem;
  margin-top: 0.35rem;
  border-radius: 10px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.18), rgba(247, 239, 222, 0.12)),
    radial-gradient(circle at top right, rgba(188, 142, 86, 0.08), transparent 40%);
}

.journal-page-content__timestamp {
  margin: auto 0 0;
  font-size: 0.74rem;
  color: color-mix(in srgb, var(--color-journal-ink) 66%, transparent);
  font-style: italic;
}

.journal-button,
.journal-text-button {
  cursor: pointer;
  transition: transform 180ms ease, background 180ms ease, opacity 180ms ease;
}

.journal-button {
  align-self: flex-start;
  padding: 0.62rem 0.92rem;
  border-radius: 999px;
  border: 1px dashed color-mix(in srgb, var(--color-journal-accent) 60%, transparent);
  background: color-mix(in srgb, var(--color-journal-parchment-light) 60%, transparent);
  color: var(--color-journal-ink-soft);
  font-size: 0.84rem;
  font-weight: 700;
}

.journal-button:hover,
.journal-text-button:hover {
  transform: translateY(-1px);
}

.journal-text-button {
  padding: 0;
  border: none;
  background: transparent;
  color: var(--color-journal-accent);
  font-size: 0.8rem;
  font-weight: 700;
}

.journal-text-button--danger {
  color: var(--color-journal-danger);
}

.journal-empty {
  margin: 0;
  padding: 0.8rem 0.95rem;
  border-radius: 14px;
  border: 1px dashed color-mix(in srgb, var(--color-journal-ink) 28%, transparent);
  background: color-mix(in srgb, var(--color-journal-paper-top) 42%, transparent);
  color: color-mix(in srgb, var(--color-journal-ink) 74%, transparent);
  font-size: 0.84rem;
  line-height: 1.48;
}

.journal-blank-lines {
  height: 8rem;
  border-radius: 14px;
  background:
    repeating-linear-gradient(
      to bottom,
      transparent 0 1.25rem,
      rgba(158, 122, 71, 0.2) 1.25rem 1.34rem
    );
}

.journal-page-content__quote {
  margin: 0;
  font-size: 0.9rem;
  line-height: 1.55;
  color: color-mix(in srgb, var(--color-journal-ink) 84%, transparent);
  font-family: 'Segoe Print', 'Bradley Hand', 'Comic Sans MS', cursive;
}

/* Ensure long, unbroken notes don't force layout overflow and always wrap */
.journal-report-card,
.journal-entry-card,
.journal-page-content__blocks {
  min-width: 0;
}

.journal-report-card__text,
.journal-entry-card__text,
.journal-empty,
.journal-page-content__quote {
  white-space: pre-wrap;
  overflow-wrap: anywhere;
  word-break: normal;
  hyphens: auto;
  max-width: 100%;
  display: block;
}

@media (max-width: 640px) {
  .journal-page-content {
    padding: 0.95rem 0.8rem 1.6rem;
  }

  .journal-page-content__title {
    font-size: 0.95rem;
  }

  .journal-page-content__subtitle,
  .journal-report-card__text,
  .journal-entry-card__text,
  .journal-page-content__quote {
    font-size: 0.8rem;
  }

  .journal-entry-card__footer {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
