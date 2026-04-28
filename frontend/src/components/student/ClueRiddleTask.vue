<template>
  <section class="clue-riddle">
    <header class="clue-riddle__intro">
      <p class="clue-riddle__label">Gåteoppgave</p>
      <h2>{{ task.title }}</h2>
      <p>{{ task.description }}</p>
    </header>

    <section class="clue-riddle__why" aria-label="Hvorfor oppgaven er viktig">
      <p>{{ content.purpose }}</p>
    </section>

    <article class="clue-riddle__evidence">
      <span>Bevis</span>
      <p v-if="content.evidence">{{ content.evidence }}</p>

      <article v-if="emailEvidence" class="clue-riddle__email" aria-label="Mistenkelig e-post">
        <div class="clue-riddle__email-row">
          <span class="clue-riddle__email-label">Fra:</span>
          <span class="clue-riddle__email-value">
            {{ emailEvidence.fromName }}
            <span v-if="emailEvidence.fromEmail">&lt;{{ emailEvidence.fromEmail }}&gt;</span>
          </span>
        </div>
        <div class="clue-riddle__email-row">
          <span class="clue-riddle__email-label">Emne:</span>
          <span class="clue-riddle__email-value">{{ emailEvidence.subject }}</span>
        </div>
        <p class="clue-riddle__email-body">{{ emailEvidence.body }}</p>
      </article>

      <div v-if="socialPost" class="clue-riddle__social-shell" :style="postThemeStyle">
        <div class="clue-riddle__social-platform">
          <span class="clue-riddle__social-platform-dot" aria-hidden="true" />
          {{ getPlatformLabel(socialPost.platform) }}
        </div>

        <div class="clue-riddle__social-card" role="article" :aria-label="`Innlegg fra ${socialPost.username ?? 'ukjent bruker'}`">
          <div class="clue-riddle__social-stripe" aria-hidden="true" />
          <span class="clue-riddle__social-avatar" :style="getAvatarStyle(socialPost.username, socialPost.platform)" aria-hidden="true">
            {{ getInitials(socialPost.username) }}
          </span>

          <div class="clue-riddle__social-body">
            <div class="clue-riddle__social-header">
              <div class="clue-riddle__social-identity">
                <span class="clue-riddle__social-name">{{ socialPost.username }}</span>
                <span
                  v-if="socialPost.verified"
                  class="clue-riddle__social-verified"
                  aria-label="Verifisert konto"
                  title="Verifisert konto"
                >
                  <span class="clue-riddle__social-verified-check" aria-hidden="true">✓</span>
                </span>
              </div>

              <div class="clue-riddle__social-subline">
                <span class="clue-riddle__social-handle">{{ socialPost.handle || getHandle(socialPost.username) }}</span>
                <span class="clue-riddle__social-separator" aria-hidden="true">•</span>
                <span class="clue-riddle__social-timestamp">{{ socialPost.timestamp || 'I dag' }}</span>
              </div>
            </div>

            <p class="clue-riddle__social-content">{{ socialPost.content }}</p>

            <div class="clue-riddle__social-actions" aria-label="Innleggsaktivitet">
              <span>♡ {{ formatMetric(socialPost.likes) }}</span>
              <span>💬 {{ formatMetric(socialPost.comments) }}</span>
              <span>↗ {{ formatMetric(socialPost.shares) }}</span>
            </div>

            <div v-if="socialPost.clueTitle || socialPost.clueText" class="clue-riddle__social-clue">
              <p v-if="socialPost.clueTitle" class="clue-riddle__social-clue-title">{{ socialPost.clueTitle }}</p>
              <p v-if="socialPost.clueText" class="clue-riddle__social-clue-text">{{ socialPost.clueText }}</p>
            </div>
          </div>
        </div>
      </div>
    </article>

    <div class="clue-riddle__question">
      <h3>{{ content.question }}</h3>
      <div class="clue-riddle__options">
        <button
          v-for="option in options"
          :key="option.id"
          class="clue-riddle__option"
          :class="{ 'clue-riddle__option--selected': selected === option.id }"
          :disabled="!!result"
          @click="selected = option.id"
        >
          <strong>{{ option.label }}</strong>
          <span v-if="option.detail">{{ option.detail }}</span>
        </button>
      </div>
    </div>

    <button v-if="!result" class="clue-riddle__submit" :disabled="!selected" @click="submit">
      Sjekk svaret
    </button>

    <Transition name="riddle-result">
      <div
        v-if="result"
        class="clue-riddle__result"
        :class="result.correct ? 'clue-riddle__result--correct' : 'clue-riddle__result--wrong'"
        role="status"
        aria-live="polite"
      >
        <h3>{{ result.correct ? 'Spor funnet!' : 'Ikke helt ennå' }}</h3>
        <p>{{ result.explanation }}</p>
        <div class="clue-riddle__actions">
          <button v-if="!result.correct" @click="$emit('tryAgain')">Prøv igjen</button>
          <button v-else @click="$emit('next')">
            {{ isLastTask ? 'Videre til sammendrag' : 'Neste oppgave' }}
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
  isLastTask: { type: Boolean, default: false }
})

const emit = defineEmits(['submitted', 'next', 'tryAgain'])

const selected = ref('')
const content = computed(() => props.task?.contentJson ?? {})
const options = computed(() => content.value.options ?? [])
const socialPost = computed(() => content.value.socialPost ?? null)
const emailEvidence = computed(() => content.value.email ?? null)
const postThemeStyle = computed(() => getPostThemeStyle(socialPost.value?.platform))

watch(() => props.task?.id, () => {
  selected.value = ''
}, { immediate: true })

watch(() => props.result, (value) => {
  if (!value) selected.value = ''
})

function submit() {
  if (!selected.value) return
  emit('submitted', { selected: selected.value })
}

function formatMetric(value) {
  if (value == null || value === '') return ''
  return typeof value === 'number' ? value.toLocaleString('nb-NO') : String(value)
}

function getPlatformTheme(platform) {
  const label = String(platform ?? 'Sosialt medium').toLowerCase()
  if (label.includes('facebook') || label.includes('fjesbok')) {
    return {
      accent: 'var(--color-social-facebook)',
      accentSoft: 'color-mix(in srgb, var(--color-social-facebook) 18%, white)',
      stripe: 'linear-gradient(90deg, var(--color-social-facebook) 0%, var(--color-social-facebook-light) 100%)',
    }
  }
  if (label === 'x' || label.includes('twitter') || label.includes('tweety')) {
    return {
      accent: 'var(--color-social-x)',
      accentSoft: 'color-mix(in srgb, var(--color-social-x) 10%, white)',
      stripe: 'linear-gradient(90deg, var(--color-social-x-soft) 0%, var(--color-social-x) 100%)',
    }
  }

  return {
    accent: 'var(--color-primary)',
    accentSoft: 'var(--color-primary-light)',
    stripe: 'linear-gradient(90deg, var(--color-primary) 0%, var(--color-primary-dark) 100%)',
  }
}

function getPostThemeStyle(platform) {
  const theme = getPlatformTheme(platform)
  return {
    '--post-accent': theme.accent,
    '--post-accent-soft': theme.accentSoft,
    '--post-stripe': theme.stripe,
  }
}

function getPlatformLabel(platform) {
  const label = String(platform ?? 'Sosialt medium').trim()
  const lowered = label.toLowerCase()
  if (lowered.includes('facebook') || lowered.includes('fjesbok')) return 'Fjesbok.no'
  if (lowered === 'x' || lowered.includes('twitter') || lowered.includes('tweety')) return 'Tweety.no'
  return label
}

function getInitials(username) {
  const source = String(username ?? '')
    .trim()
    .split(/\s+/)
    .filter(Boolean)

  if (!source.length) return 'SM'
  if (source.length === 1) {
    return source[0].replace(/[^a-zA-Z0-9ÆØÅæøå]/g, '').slice(0, 2).toUpperCase() || 'SM'
  }

  return source
    .slice(0, 2)
    .map(part => part[0] ?? '')
    .join('')
    .toUpperCase()
}

function getHandle(username) {
  const normalized = String(username ?? 'bruker')
    .trim()
    .toLowerCase()
    .replace(/\s+/g, '')
    .replace(/[^a-z0-9_.æøå-]/gi, '')

  return `@${normalized || 'bruker'}`
}

function getAvatarStyle(username, platform) {
  const seedText = `${username ?? ''}|${platform ?? ''}`
  let total = 0

  for (const char of seedText) total += char.charCodeAt(0)

  const hue = total % 360
  const secondaryHue = (hue + 36) % 360

  return {
    background: `linear-gradient(135deg, hsl(${hue} 76% 72%) 0%, hsl(${secondaryHue} 62% 58%) 100%)`,
  }
}
</script>

<style scoped>
.clue-riddle {
  display: grid;
  gap: var(--space-4);
}

.clue-riddle__intro,
.clue-riddle__why,
.clue-riddle__evidence,
.clue-riddle__result {
  border-radius: var(--radius-lg);
  padding: var(--space-4);
}

.clue-riddle__intro {
  border: 2px solid var(--color-primary);
  background: var(--color-primary-light);
}

.clue-riddle__label {
  margin: 0 0 var(--space-1);
  color: var(--color-primary-dark);
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
  text-transform: uppercase;
}

.clue-riddle h2,
.clue-riddle h3,
.clue-riddle p {
  margin-top: 0;
}

.clue-riddle__intro p,
.clue-riddle__why p,
.clue-riddle__evidence p,
.clue-riddle__result p {
  margin-bottom: 0;
  line-height: 1.55;
}

.clue-riddle__why {
  border: 1px solid var(--color-border);
  background: var(--color-surface);
}

.clue-riddle__evidence {
  border: 2px dashed var(--color-warning);
  background: var(--color-warning-light);
  display: grid;
  gap: var(--space-3);
}

.clue-riddle__evidence span {
  display: inline-block;
  padding: var(--space-1) var(--space-2);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-text);
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
}

.clue-riddle__question {
  display: grid;
  gap: var(--space-3);
}

.clue-riddle__email {
  display: grid;
  gap: var(--space-2);
  border: 2px solid color-mix(in srgb, var(--color-primary) 20%, var(--color-border));
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  padding: var(--space-4);
  box-shadow: 0 10px 20px color-mix(in srgb, var(--color-primary) 8%, transparent);
}

.clue-riddle__email-row {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
  align-items: baseline;
}

.clue-riddle__email-label {
  font-weight: var(--font-bold);
  color: var(--color-primary-dark);
}

.clue-riddle__email-value {
  color: var(--color-text);
  font-weight: var(--font-semibold);
}

.clue-riddle__email-value span {
  color: var(--color-text-muted);
  font-family: monospace;
}

.clue-riddle__email-body {
  white-space: pre-line;
  color: var(--color-text);
  line-height: 1.65;
}

.clue-riddle__social-shell {
  display: grid;
  gap: var(--space-2);
}

.clue-riddle__social-platform {
  display: inline-flex;
  align-items: center;
  gap: 0.45rem;
  justify-self: start;
  border-radius: var(--radius-full);
  padding: var(--space-1) var(--space-3);
  background: var(--post-accent-soft);
  color: var(--post-accent);
  font-size: var(--text-xs);
  font-weight: var(--font-semibold);
}

.clue-riddle__social-platform-dot {
  width: 0.55rem;
  height: 0.55rem;
  border-radius: 50%;
  background: currentColor;
  box-shadow: 0 0 0 3px color-mix(in srgb, currentColor 18%, transparent);
}

.clue-riddle__social-card {
  position: relative;
  overflow: hidden;
  border: 2px solid color-mix(in srgb, var(--post-accent) 18%, var(--color-border));
  border-radius: var(--radius-lg);
  padding: calc(var(--space-4) + 10px) var(--space-4) var(--space-4);
  background: var(--color-surface);
  display: flex;
  gap: var(--space-3);
  box-shadow: 0 12px 24px color-mix(in srgb, var(--post-accent) 10%, transparent);
}

.clue-riddle__social-stripe {
  position: absolute;
  inset: 0 0 auto 0;
  height: 8px;
  background: var(--post-stripe);
}

.clue-riddle__social-avatar {
  width: 3rem;
  height: 3rem;
  display: grid;
  place-items: center;
  border-radius: 50%;
  font-size: 0.9rem;
  font-weight: var(--font-bold);
  letter-spacing: 0.08em;
  color: white;
  line-height: 1;
  flex-shrink: 0;
  border: 2px solid color-mix(in srgb, var(--post-accent) 20%, white);
  box-shadow: 0 6px 14px color-mix(in srgb, var(--post-accent) 14%, transparent);
}

.clue-riddle__social-body {
  flex: 1;
  min-width: 0;
}

.clue-riddle__social-header {
  display: grid;
  gap: 0.15rem;
  margin-bottom: var(--space-1);
}

.clue-riddle__social-identity {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.clue-riddle__social-name {
  font-weight: var(--font-bold);
  font-size: var(--text-sm);
  color: var(--color-text);
}

.clue-riddle__social-verified {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-full);
  width: 1.1rem;
  height: 1.1rem;
  background: var(--post-accent);
  color: white;
}

.clue-riddle__social-verified-check {
  font-size: 0.7rem;
  font-weight: var(--font-bold);
  line-height: 1;
}

.clue-riddle__social-subline {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  color: var(--color-text-muted);
  font-size: var(--text-xs);
  min-width: 0;
}

.clue-riddle__social-content {
  font-size: var(--text-base);
  line-height: 1.6;
  color: var(--color-text);
}

.clue-riddle__social-actions {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-3);
  margin-top: var(--space-3);
  color: var(--color-text-muted);
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
}

.clue-riddle__social-clue {
  margin-top: var(--space-3);
  border: 1px solid color-mix(in srgb, var(--post-accent) 22%, var(--color-border));
  border-radius: var(--radius-md);
  background: color-mix(in srgb, var(--post-accent-soft) 55%, white);
  padding: var(--space-3);
}

.clue-riddle__social-clue-title {
  margin: 0 0 var(--space-1);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: var(--post-accent);
}

.clue-riddle__social-clue-text {
  margin: 0;
  color: var(--color-text);
  line-height: 1.5;
}

.clue-riddle__options {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: var(--space-3);
}

.clue-riddle__option {
  min-height: 7rem;
  display: grid;
  align-content: start;
  gap: var(--space-2);
  padding: var(--space-3);
  border: 2px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-surface);
  color: var(--color-text);
  text-align: left;
  cursor: pointer;
}

.clue-riddle__option span {
  color: var(--color-text-muted);
  line-height: 1.4;
}

.clue-riddle__option--selected {
  border-color: var(--color-primary);
  background: var(--color-primary-light);
}

.clue-riddle__submit,
.clue-riddle__actions button {
  justify-self: start;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 44px;
  padding: var(--space-2) var(--space-6);
  border: none;
  border-radius: var(--radius-md);
  background: var(--color-primary);
  color: var(--color-text-on-dark);
  font-weight: var(--font-semibold);
  font-size: var(--text-base);
  cursor: pointer;
  transition: background var(--transition-fast), transform var(--transition-fast), opacity var(--transition-fast);
}

.clue-riddle__submit:disabled,
.clue-riddle__option:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.clue-riddle__submit:hover:not(:disabled),
.clue-riddle__actions button:hover:not(:disabled) {
  background: var(--color-btn-primary-hover);
}

.clue-riddle__submit:active:not(:disabled),
.clue-riddle__actions button:active:not(:disabled) {
  transform: scale(0.98);
}

.clue-riddle__result {
  display: grid;
  gap: var(--space-3);
}

.clue-riddle__result--correct {
  border: 2px solid var(--color-success);
  background: var(--color-success-light);
}

.clue-riddle__result--wrong {
  border: 2px solid var(--color-danger);
  background: var(--color-danger-light);
}

.clue-riddle__actions {
  display: flex;
  gap: var(--space-3);
}

.riddle-result-enter-active { transition: opacity 0.3s ease, transform 0.3s ease; }
.riddle-result-leave-active { transition: opacity 0.2s ease, transform 0.2s ease; }
.riddle-result-enter-from,
.riddle-result-leave-to { opacity: 0; transform: translateY(-10px); }
</style>
