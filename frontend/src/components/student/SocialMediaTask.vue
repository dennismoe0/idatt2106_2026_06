<template>
  <section class="task-card">
    <h2 v-if="task.stopName">{{ task.stopName }}</h2>
    <p class="guidance">{{ task.guidanceText }}</p>

    <template v-if="isIdentifyWorstTask">
      <p class="question">{{ content.question }}</p>
      <p class="ranking-hint">Ranger innleggene fra minst troverdig (1) til mest troverdig ({{ posts.length }}).</p>

      <div class="post-list">
        <div
          v-for="postItem in posts"
          :key="postItem.id"
          class="post-shell"
          :style="getPostThemeStyle(postItem.platform)"
        >
          <div class="post-shell__toolbar">
            <div class="post-platform">{{ postItem.platform ?? 'Sosialt medium' }}</div>
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

          <div class="post-card" role="article" :aria-label="`Innlegg fra ${postItem.username ?? 'ukjent bruker'}`">
            <div class="post-card__stripe" aria-hidden="true" />
            <span class="post-avatar" aria-hidden="true">{{ postItem.avatar ?? '👤' }}</span>
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
              </div>
              <p class="post-content">{{ postItem.content }}</p>
              <p v-if="hasPostMeta(postItem)" class="post-meta">
                <span v-if="postItem.likes !== undefined && postItem.likes !== null">❤️ {{ formatMetric(postItem.likes) }}</span>
                <span v-if="postItem.likes !== undefined && postItem.likes !== null && postItem.timestamp">·</span>
                <span v-if="postItem.timestamp">{{ postItem.timestamp }}</span>
              </p>
            </div>
          </div>
        </div>
      </div>
    </template>

    <template v-else>
      <div class="post-shell" :style="postThemeStyle">
        <div class="post-platform">{{ platformLabel }}</div>
        <div class="post-card" role="article" :aria-label="`Innlegg fra ${post.username ?? 'ukjent bruker'}`">
          <div class="post-card__stripe" aria-hidden="true" />
          <span class="post-avatar" aria-hidden="true">{{ post.avatar ?? '👤' }}</span>
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
            </div>
            <p class="post-content">{{ post.content }}</p>
            <p v-if="hasPostMeta(post)" class="post-meta">
              <span v-if="post.likes !== undefined && post.likes !== null">❤️ {{ formatMetric(post.likes) }}</span>
              <span v-if="post.likes !== undefined && post.likes !== null && post.timestamp">·</span>
              <span v-if="post.timestamp">{{ post.timestamp }}</span>
            </p>
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
          <button class="next-btn" @click="$emit('next')">
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

const emit = defineEmits(['submitted', 'next'])

const selected = ref(null)
const rankings = ref({})
const content = computed(() => props.task?.contentJson ?? {})
const taskSubtype = computed(() => String(content.value.type ?? 'CHOOSE_ACTION').toUpperCase())
const isIdentifyWorstTask = computed(() => taskSubtype.value === 'IDENTIFY_WORST')
const post = computed(() => content.value.post ?? {})
const posts = computed(() => Array.isArray(content.value.posts) ? content.value.posts : [])
const platformLabel = computed(() => post.value.platform ?? 'Sosialt medium')
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
  if (label.includes('instagram')) {
    return {
      accent: '#E1306C',
      accentSoft: 'color-mix(in srgb, #E1306C 16%, white)',
      stripe: 'linear-gradient(90deg, #F58529 0%, #DD2A7B 45%, #8134AF 72%, #515BD4 100%)',
    }
  }

  if (label.includes('tiktok')) {
    return {
      accent: '#111111',
      accentSoft: 'color-mix(in srgb, #25F4EE 22%, white)',
      stripe: 'linear-gradient(90deg, #25F4EE 0%, #111111 48%, #FE2C55 100%)',
    }
  }

  if (label.includes('facebook')) {
    return {
      accent: '#1877F2',
      accentSoft: 'color-mix(in srgb, #1877F2 18%, white)',
      stripe: 'linear-gradient(90deg, #1877F2 0%, #4F8DF8 100%)',
    }
  }

  if (label === 'x' || label.includes('twitter')) {
    return {
      accent: '#111111',
      accentSoft: 'color-mix(in srgb, #111111 10%, white)',
      stripe: 'linear-gradient(90deg, #444444 0%, #111111 100%)',
    }
  }

  if (label.includes('linkedin')) {
    return {
      accent: '#0A66C2',
      accentSoft: 'color-mix(in srgb, #0A66C2 18%, white)',
      stripe: 'linear-gradient(90deg, #0A66C2 0%, #378FE9 100%)',
    }
  }

  if (label.includes('youtube')) {
    return {
      accent: '#FF0033',
      accentSoft: 'color-mix(in srgb, #FF0033 16%, white)',
      stripe: 'linear-gradient(90deg, #FF0033 0%, #FF5A5F 100%)',
    }
  }

  if (label.includes('snap')) {
    return {
      accent: '#D5AA00',
      accentSoft: 'color-mix(in srgb, #FFD400 22%, white)',
      stripe: 'linear-gradient(90deg, #FFD400 0%, #FFEE88 100%)',
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

const postThemeStyle = computed(() => getPostThemeStyle(platformLabel.value))

watch(() => props.task?.id, () => {
  selected.value = null
  rankings.value = {}
}, { immediate: true })

function formatMetric(value) {
  if (value === undefined || value === null || value === '') return ''
  return typeof value === 'number' ? value.toLocaleString('nb-NO') : String(value)
}

function hasPostMeta(postItem) {
  return postItem?.timestamp || postItem?.likes === 0 || !!postItem?.likes
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

.guidance {
  margin: 0;
  color: var(--color-text-muted);
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
  justify-self: start;
  border-radius: var(--radius-full);
  padding: var(--space-1) var(--space-3);
  background: var(--post-accent-soft);
  color: var(--post-accent);
  font-size: var(--text-xs);
  font-weight: var(--font-semibold);
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
  font-size: 1.6rem;
  line-height: 1;
  flex-shrink: 0;
  background:
    radial-gradient(circle at 30% 30%, white 0%, color-mix(in srgb, var(--post-accent-soft) 80%, white) 35%, var(--post-accent-soft) 100%);
  border: 2px solid color-mix(in srgb, var(--post-accent) 20%, white);
  box-shadow: 0 6px 14px color-mix(in srgb, var(--post-accent) 14%, transparent);
}

.post-body {
  flex: 1;
  min-width: 0;
}

.post-header {
  display: flex;
  align-items: baseline;
  gap: var(--space-2);
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

.post-meta {
  margin: var(--space-2) 0 0;
  color: var(--color-text-muted);
  line-height: 1.4;
  font-size: var(--text-xs);
}

.ranking-hint {
  margin: 0;
  color: var(--color-text-muted);
  font-size: var(--text-sm);
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
  font-weight: var(--font-semibold);
  color: var(--color-text);
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
</style>
