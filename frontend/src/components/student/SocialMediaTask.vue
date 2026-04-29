<template>
  <section class="task-card" :class="{ 'task-card--social-stop': isSocialStop }">
    <div v-if="isSocialStop" class="task-card__header-box">
      <h2 class="task-card__title--social-stop">{{ task.stopName ?? 'Den sosiale møteplassen' }}</h2>
      <p class="guidance guidance--social-stop">{{ task.guidanceText }}</p>
    </div>
    <template v-else>
      <h2>{{ task.stopName ?? 'Den sosiale møteplassen' }}</h2>
      <p class="guidance">{{ task.guidanceText }}</p>
    </template>

    <template v-if="isIdentifyWorstTask">
      <div class="prompt-panel">
        <p class="question">{{ content.question }}</p>
        <p class="ranking-hint">Ranger innleggene fra minst troverdig (1) til mest troverdig ({{ posts.length }}).</p>
      </div>

      <div class="post-list">
        <div
          v-for="postItem in posts"
          :key="postItem.id"
          class="post-shell"
          :style="getPostThemeStyle(postItem.platform)"
        >
          <div class="post-shell__toolbar">
            <div class="post-platform">
              <span class="post-platform__dot" aria-hidden="true" />
              {{ getPlatformLabel(postItem.platform) }}
            </div>
            <label class="rank-picker">
              <span class="rank-picker__label">Rangering</span>
              <select
                class="rank-picker__select"
                :disabled="!!result"
                :value="rankings[postItem.id] ?? ''"
                @change="assignRank(postItem.id, $event.target.value)"
              >
                <option value="">Velg</option>
                <option v-for="rank in rankingOptions" :key="rank" :value="rank">{{ rank }}</option>
              </select>
            </label>
          </div>

          <div class="post-card" role="article" data-peek-trigger :aria-label="`Innlegg fra ${postItem.username ?? 'ukjent bruker'}`">
            <div class="post-card__stripe" aria-hidden="true" />
            <span class="post-avatar" :style="getAvatarStyle(postItem.username, postItem.platform)" aria-hidden="true">
              {{ getInitials(postItem.username) }}
            </span>
            <div class="post-body">
              <div class="post-header">
                <div class="post-identity">
                  <span class="post-name">{{ postItem.username }}</span>
                  <span
                    v-if="postItem.verified"
                    class="post-verified"
                    aria-label="Verifisert konto"
                    title="Verifisert konto"
                  >
                    <span class="post-verified__check" aria-hidden="true">✓</span>
                  </span>
                </div>
                <div class="post-subline">
                  <span class="post-handle">{{ getHandle(postItem.username) }}</span>
                  <span class="post-separator" aria-hidden="true">•</span>
                  <span class="post-timestamp">{{ getDisplayTimestamp(postItem) }}</span>
                </div>
              </div>
              <p class="post-content">{{ postItem.content }}</p>
              <div class="post-actions" aria-label="Innleggsaktivitet">
                <span class="post-action-stat">♡ {{ formatMetric(getMetric(postItem, 'likes')) }}</span>
                <span class="post-action-stat">💬 {{ formatMetric(getMetric(postItem, 'comments')) }}</span>
                <span class="post-action-stat">↗ {{ formatMetric(getMetric(postItem, 'shares')) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>

    <template v-else>
      <div class="post-shell" :style="postThemeStyle">
        <div class="post-platform">
          <span class="post-platform__dot" aria-hidden="true" />
          {{ getPlatformLabel(platformLabel) }}
        </div>
        <div class="post-card" role="article" data-peek-trigger :aria-label="`Innlegg fra ${post.username ?? 'ukjent bruker'}`">
          <div class="post-card__stripe" aria-hidden="true" />
          <span class="post-avatar" :style="getAvatarStyle(post.username, post.platform)" aria-hidden="true">
            {{ getInitials(post.username) }}
          </span>
          <div class="post-body">
            <div class="post-header">
              <div class="post-identity">
                <span class="post-name">{{ post.username }}</span>
                <span
                  v-if="post.verified"
                  class="post-verified"
                  aria-label="Verifisert konto"
                  title="Verifisert konto"
                >
                  <span class="post-verified__check" aria-hidden="true">✓</span>
                </span>
              </div>
              <div class="post-subline">
                <span class="post-handle">{{ getHandle(post.username) }}</span>
                <span class="post-separator" aria-hidden="true">•</span>
                <span class="post-timestamp">{{ getDisplayTimestamp(post) }}</span>
              </div>
            </div>
            <p class="post-content">{{ post.content }}</p>
            <div class="post-actions" aria-label="Innleggsaktivitet">
              <span class="post-action-stat">♡ {{ formatMetric(getMetric(post, 'likes')) }}</span>
              <span class="post-action-stat">💬 {{ formatMetric(getMetric(post, 'comments')) }}</span>
              <span class="post-action-stat">↗ {{ formatMetric(getMetric(post, 'shares')) }}</span>
            </div>
          </div>
        </div>
      </div>

      <p class="question">{{ content.question }}</p>
      <div class="options" role="group" :aria-label="content.question">
        <button
          v-for="opt in content.options"
          :key="opt.id"
          class="option-btn"
          :class="{ 'option-btn--selected': selected === opt.id }"
          :disabled="!!result"
          :aria-pressed="selected === opt.id"
          @click="selected = opt.id"
        >
          {{ opt.text }}
        </button>
      </div>
    </template>

    <button
      v-if="!result"
      class="submit-btn"
      :disabled="isIdentifyWorstTask ? !isRankingComplete : !selected"
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
          <span v-if="result.correct">✅ Riktig!</span>
          <span v-else>❌ Ikke helt riktig</span>
        </p>
        <p class="inline-result__explanation">{{ result.explanation }}</p>
        <p v-if="result.stopCompleted" class="inline-result__stop">🎉 Du fullførte {{ task.stopName ?? 'stoppet' }}!</p>
        <div class="inline-result__actions">
          <button v-if="!result.correct" class="next-btn" @click="$emit('retry')">
            Prøv igjen
          </button>
          <button v-else class="next-btn" @click="$emit('next')">
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
  isLastTask: { type: Boolean, default: false },
})

const emit = defineEmits(['submitted', 'next', 'retry'])

const PLATFORM_THEMES = {
  instagram: {
    accent: 'var(--color-social-instagram)',
    accentSoft: 'color-mix(in srgb, var(--color-social-instagram) 16%, white)',
    stripe: 'linear-gradient(90deg, var(--color-social-instagram-orange) 0%, var(--color-social-instagram) 45%, var(--color-social-instagram-purple) 72%, var(--color-social-instagram-blue) 100%)',
  },
  tiktok: {
    accent: 'var(--color-social-x)',
    accentSoft: 'color-mix(in srgb, var(--color-social-tiktok-cyan) 22%, white)',
    stripe: 'linear-gradient(90deg, var(--color-social-tiktok-cyan) 0%, var(--color-social-x) 48%, var(--color-social-tiktok-pink) 100%)',
  },
  facebook: {
    accent: 'var(--color-social-facebook)',
    accentSoft: 'color-mix(in srgb, var(--color-social-facebook) 18%, white)',
    stripe: 'linear-gradient(90deg, var(--color-social-facebook) 0%, var(--color-social-facebook-light) 100%)',
  },
  x: {
    accent: 'var(--color-social-x)',
    accentSoft: 'color-mix(in srgb, var(--color-social-x) 10%, white)',
    stripe: 'linear-gradient(90deg, var(--color-social-x-soft) 0%, var(--color-social-x) 100%)',
  },
  linkedin: {
    accent: 'var(--color-social-linkedin)',
    accentSoft: 'color-mix(in srgb, var(--color-social-linkedin) 18%, white)',
    stripe: 'linear-gradient(90deg, var(--color-social-linkedin) 0%, var(--color-social-linkedin-light) 100%)',
  },
  youtube: {
    accent: '#FF0033',
    accentSoft: 'color-mix(in srgb, #FF0033 16%, white)',
    stripe: 'linear-gradient(90deg, #FF0033 0%, #FF5A5F 100%)',
  },
  snap: {
    accent: '#D5AA00',
    accentSoft: 'color-mix(in srgb, #FFD400 22%, white)',
    stripe: 'linear-gradient(90deg, #FFD400 0%, #FFEE88 100%)',
  },
}

const selected = ref(null)
const rankings = ref({})
const content = computed(() => props.task?.contentJson ?? {})
const taskSubtype = computed(() => String(content.value.type ?? 'CHOOSE_ACTION').toUpperCase())
const posts = computed(() => Array.isArray(content.value.posts) ? content.value.posts : [])
const isIdentifyWorstTask = computed(() => taskSubtype.value === 'IDENTIFY_WORST' || posts.value.length > 0)
const post = computed(() => content.value.post ?? {})
const platformLabel = computed(() => post.value.platform ?? 'Sosialt medium')
const isSocialStop = computed(() => (props.task?.stopName ?? '') === 'Den sosiale møteplassen')
const rankingOptions = computed(() => Array.from({ length: posts.value.length }, (_, index) => index + 1))
const rankedWorstPostId = computed(() => {
  const entry = Object.entries(rankings.value).find(([, rank]) => Number(rank) === 1)
  return entry?.[0] ?? null
})
const isRankingComplete = computed(() => {
  if (!isIdentifyWorstTask.value || !posts.value.length) return false
  const assigned = posts.value
    .map(postItem => Number(rankings.value[postItem.id]))
    .filter(rank => Number.isInteger(rank) && rank >= 1 && rank <= posts.value.length)

  return assigned.length === posts.value.length && new Set(assigned).size === posts.value.length
})

function getPlatformTheme(platform) {
  const label = String(platform ?? 'Sosialt medium').toLowerCase()
  if (label.includes('instagram')) return PLATFORM_THEMES.instagram
  if (label.includes('tiktok')) return PLATFORM_THEMES.tiktok
  if (label.includes('facebook')) return PLATFORM_THEMES.facebook
  if (label === 'x' || label.includes('twitter')) return PLATFORM_THEMES.x
  if (label.includes('linkedin')) return PLATFORM_THEMES.linkedin
  if (label.includes('youtube')) return PLATFORM_THEMES.youtube
  if (label.includes('snap')) return PLATFORM_THEMES.snap

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

const postThemeStyle = computed(() => getPostThemeStyle(platformLabel.value))

watch(() => props.task?.id, () => {
  selected.value = null
  rankings.value = {}
}, { immediate: true })

function formatMetric(value) {
  if (value == null || value === '') return ''
  return typeof value === 'number' ? value.toLocaleString('nb-NO') : String(value)
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

function getDisplayTimestamp(postItem) {
  return postItem?.timestamp || '2 timer siden'
}

function getFallbackNumber(postItem, seedOffset = 0) {
  const seedText = `${postItem?.id ?? ''}|${postItem?.username ?? ''}|${postItem?.content ?? ''}|${seedOffset}`
  let total = 0

  for (const char of seedText) total += char.charCodeAt(0)

  return total
}

function getMetric(postItem, metric) {
  if (postItem?.[metric] != null && postItem[metric] !== '') return postItem[metric]

  const seed = getFallbackNumber(postItem, metric.length)
  if (metric === 'likes') return 24 + (seed % 180)
  if (metric === 'comments') return 3 + (seed % 36)
  if (metric === 'shares') return 1 + (seed % 18)
  return ''
}

function getAvatarStyle(username, platform) {
  const seed = getFallbackNumber({ username, content: platform })
  const hue = seed % 360
  const secondaryHue = (hue + 36) % 360

  return {
    background: `linear-gradient(135deg, hsl(${hue} 76% 72%) 0%, hsl(${secondaryHue} 62% 58%) 100%)`,
  }
}

function assignRank(postId, rawValue) {
  const value = Number(rawValue)
  const next = { ...rankings.value }

  if (!Number.isInteger(value) || value < 1) {
    delete next[postId]
    rankings.value = next
    return
  }

  for (const [otherPostId, rank] of Object.entries(next)) {
    if (otherPostId !== postId && Number(rank) === value) {
      delete next[otherPostId]
    }
  }

  next[postId] = value
  rankings.value = next
}

function submit() {
  if (isIdentifyWorstTask.value) {
    if (!isRankingComplete.value || !rankedWorstPostId.value) return
    // Backend only validates which post was ranked worst (rank 1); the full order is UI-only for now.
    const answer = { selected: rankedWorstPostId.value }
    if (import.meta.env.DEV) console.log('[SocialMediaTask] Submitting:', answer)
    emit('submitted', answer)
    return
  }

  if (!selected.value) return
  const answer = { selected: selected.value }
  if (import.meta.env.DEV) console.log('[SocialMediaTask] Submitting:', answer)
  emit('submitted', answer)
}
</script>

<style scoped>
.task-card {
  display: grid;
  gap: var(--space-4);
}

.task-card--social-stop {
  gap: var(--space-3);
}

.task-card__header-box {
  display: grid;
  gap: var(--space-2);
  padding: var(--space-4) var(--space-5);
  border-radius: var(--radius-xl);
  border: 2px solid color-mix(in srgb, var(--color-primary) 28%, var(--color-border));
  background:
    linear-gradient(145deg, color-mix(in srgb, var(--color-primary-light) 78%, white) 0%, color-mix(in srgb, var(--color-surface) 92%, white) 100%);
  box-shadow:
    0 18px 34px rgba(16, 37, 63, 0.12),
    inset 0 1px 0 rgba(255, 255, 255, 0.45);
}

.guidance {
  margin: 0;
  color: var(--color-text-muted);
}

.task-card__title--social-stop {
  margin: 0;
  font-size: clamp(1.8rem, 6vw, 2.5rem);
  line-height: 1.1;
  font-weight: var(--font-bold);
  color: var(--color-heading);
  text-shadow: 0 1px 8px color-mix(in srgb, var(--color-primary) 16%, transparent);
}

.guidance--social-stop {
  margin: 0;
  font-size: calc(var(--text-base) + 8px);
  line-height: 1.5;
  font-weight: var(--font-semibold);
  color: var(--color-text);
  text-shadow: 0 1px 8px color-mix(in srgb, var(--color-primary) 10%, transparent);
}

.post-shell {
  display: grid;
  gap: var(--space-2);
}

.post-shell__toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-3);
  flex-wrap: wrap;
}

.post-list {
  display: grid;
  gap: var(--space-4);
}

.post-platform {
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
  letter-spacing: 0.02em;
}

.post-platform__dot {
  width: 0.55rem;
  height: 0.55rem;
  border-radius: 50%;
  background: currentColor;
  box-shadow: 0 0 0 3px color-mix(in srgb, currentColor 18%, transparent);
}

.post-card {
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

.post-card__stripe {
  position: absolute;
  inset: 0 0 auto 0;
  height: 8px;
  background: var(--post-stripe);
}

.post-avatar {
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

.post-body {
  flex: 1;
  min-width: 0;
}

.post-header {
  display: grid;
  gap: 0.15rem;
  margin-bottom: var(--space-1);
}

.post-identity {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.post-name {
  font-weight: var(--font-bold);
  font-size: var(--text-sm);
  color: var(--color-text);
}

.post-subline {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  color: var(--color-text-muted);
  font-size: var(--text-xs);
  min-width: 0;
}

.post-handle,
.post-timestamp {
  white-space: nowrap;
}

.post-separator {
  opacity: 0.65;
}

.post-verified {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-full);
  width: 1.1rem;
  height: 1.1rem;
  background: var(--post-accent);
  color: white;
  box-shadow: 0 0 0 2px color-mix(in srgb, var(--post-accent-soft) 75%, white);
}

.post-verified__check {
  font-size: 0.7rem;
  font-weight: var(--font-bold);
  line-height: 1;
}

.post-content {
  margin: 0;
  color: var(--color-text);
  line-height: 1.5;
  font-size: var(--text-sm);
}

.post-actions {
  margin-top: var(--space-3);
  padding-top: var(--space-2);
  border-top: 1px solid color-mix(in srgb, var(--post-accent) 10%, var(--color-border));
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-3);
  color: var(--color-text-muted);
  font-size: var(--text-xs);
}

.post-action-stat {
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  font-weight: var(--font-medium);
}

.prompt-panel {
  display: grid;
  gap: var(--space-2);
  padding: var(--space-4);
  border: 2px solid color-mix(in srgb, var(--color-primary) 22%, var(--color-border));
  border-radius: var(--radius-lg);
  background:
    linear-gradient(135deg, color-mix(in srgb, var(--color-primary-light) 78%, white) 0%, white 100%);
  box-shadow: 0 10px 24px color-mix(in srgb, var(--color-primary) 10%, transparent);
}

.ranking-hint {
  margin: 0;
  color: color-mix(in srgb, var(--color-text) 82%, black);
  font-size: var(--text-sm);
  line-height: 1.6;
  font-weight: var(--font-medium);
}

.rank-picker {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  color: var(--color-text-muted);
  font-size: var(--text-xs);
  font-weight: var(--font-semibold);
}

.rank-picker__label {
  white-space: nowrap;
}

.rank-picker__select {
  min-width: 5rem;
  border: 2px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: var(--space-1) var(--space-2);
  background: var(--color-surface);
  color: var(--color-text);
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
}

.rank-picker__select:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.question {
  margin: 0;
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  color: color-mix(in srgb, var(--color-heading) 88%, black);
  line-height: 1.3;
}

.options {
  display: grid;
  gap: var(--space-2);
}

.option-btn {
  text-align: left;
  border: 2px solid var(--color-border);
  background: var(--color-surface);
  border-radius: var(--radius-md);
  padding: var(--space-3) var(--space-4);
  cursor: pointer;
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  transition: border-color var(--transition-fast), background var(--transition-fast);
}

.option-btn:disabled {
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
  background: var(--color-primary);
  color: var(--color-text-on-dark);
  border: none;
  border-radius: var(--radius-md);
  padding: var(--space-2) var(--space-6);
  font-weight: var(--font-semibold);
  font-size: var(--text-base);
  cursor: pointer;
  transition: background var(--transition-fast);
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.submit-btn:hover:not(:disabled) {
  background: var(--color-btn-primary-hover);
}

.inline-result {
  border-radius: var(--radius-lg);
  padding: var(--space-4) var(--space-6);
  display: grid;
  gap: var(--space-2);
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
  background: var(--color-primary);
  color: var(--color-text-on-dark);
  border: none;
  border-radius: var(--radius-md);
  padding: var(--space-2) var(--space-6);
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

.result-slide-enter-active {
  transition: transform 0.3s ease, opacity 0.3s ease;
}

.result-slide-enter-from {
  transform: translateY(-12px);
  opacity: 0;
}

@media (max-width: 640px) {
  .post-card {
    padding-inline: var(--space-3);
  }

  .post-actions {
    gap: var(--space-2);
  }
}
</style>
