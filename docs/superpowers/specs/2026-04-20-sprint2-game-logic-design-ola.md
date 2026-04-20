# Sprint 2 — Ola: AIPhotoTask.vue + SocialMediaTask.vue (Frontend)
*Part of: 2026-04-20-sprint2-game-logic-design.md*

You own the **frontend** for Stop 3 (Fotografen) and Stop 6 (Den sosiale møteplassen).
Backend validation and seeds for these stops are done by **Kasper**.

Read section 2.1 and 2.4 of the main design doc for the contentJson contracts you must implement against.

---

## Stop 3: AIPhotoTask.vue

File: `frontend/src/components/student/AIPhotoTask.vue`

### What it does
- Receives `task.contentJson.images` (array of image objects with `id`, `src`, `alt`, `label`)
- For each image: show the image (or placeholder if `src` is empty/null during dev)
- Student classifies each image as one of: **Ekte** / **KI-generert** / **Manipulert**
- Submit disabled until every image has a classification
- On submit: emits `submitted` with `{ image_0: 'AI_GENERATED', image_1: 'REAL', ... }`
- On result: shows inline result with explanation

### Component

```vue
<template>
  <section class="task-card">
    <h2>Fotografen – Bildeanalyse</h2>
    <p class="guidance">{{ task.guidanceText }}</p>

    <div class="images-grid">
      <div
        v-for="(img, index) in images"
        :key="img.id"
        class="image-card"
        :class="{
          'image-card--answered': !!result,
          'image-card--correct':  result?.correct,
          'image-card--wrong':    result && !result.correct
        }"
      >
        <!-- Image or placeholder -->
        <div class="image-card__img-wrap">
          <img
            v-if="img.src"
            :src="img.src"
            :alt="img.alt"
            class="image-card__img"
          />
          <div v-else class="image-card__placeholder" :aria-label="img.alt">
            <span aria-hidden="true">🖼️</span>
            <span>{{ img.label }}</span>
          </div>
        </div>

        <p class="image-card__label">{{ img.label }}</p>

        <!-- Classification buttons -->
        <div class="image-card__options" role="group" :aria-label="`Klassifiser ${img.label}`">
          <button
            v-for="opt in IMAGE_TYPES"
            :key="opt.value"
            class="type-btn"
            :class="{ 'type-btn--selected': answers[`image_${index}`] === opt.value }"
            :disabled="!!result"
            :aria-pressed="answers[`image_${index}`] === opt.value"
            @click="setAnswer(index, opt.value)"
          >
            {{ opt.label }}
          </button>
        </div>
      </div>
    </div>

    <button
      v-if="!result"
      class="submit-btn"
      :disabled="!isReady"
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
          🎉 Du fullførte Fotografen!
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

const IMAGE_TYPES = [
  { value: 'REAL',         label: 'Ekte' },
  { value: 'AI_GENERATED', label: 'KI-generert' },
  { value: 'MANIPULATED',  label: 'Manipulert' },
]

const props = defineProps({
  task:       { type: Object,  required: true },
  result:     { type: Object,  default: null },
  isLastTask: { type: Boolean, default: false },
})

const emit = defineEmits(['submitted', 'next'])

const answers = ref({})
const images  = computed(() => props.task?.contentJson?.images ?? [])

// Reset when task changes
watch(() => props.task?.id, () => { answers.value = {} }, { immediate: true })

const isReady = computed(() =>
  images.value.length > 0 &&
  images.value.every((_, i) => answers.value[`image_${i}`] !== undefined)
)

function setAnswer(index, value) {
  answers.value[`image_${index}`] = value
}

function submit() {
  if (!isReady.value) return
  console.log('[AIPhotoTask] Submitting:', answers.value)
  emit('submitted', { ...answers.value })
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

.images-grid {
  display: grid;
  gap: var(--space-4);
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
}

/* Image card */
.image-card {
  border: 2px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  background: var(--color-surface);
  display: grid;
  gap: var(--space-3);
  transition: border-color var(--transition-fast);
}
.image-card--answered { border-color: var(--color-text-muted); opacity: 0.85; }
.image-card--correct  { border-color: var(--color-success); background: var(--color-success-light); }
.image-card--wrong    { border-color: var(--color-danger);  background: var(--color-danger-light); }

/* Image / placeholder */
.image-card__img-wrap {
  border-radius: var(--radius-md);
  overflow: hidden;
  background: var(--color-bg);
  border: 1px solid var(--color-border);
}
.image-card__img {
  width: 100%;
  max-height: 220px;
  object-fit: cover;
  display: block;
}
.image-card__placeholder {
  height: 160px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  color: var(--color-text-muted);
  font-size: var(--text-sm);
}
.image-card__placeholder span:first-child { font-size: 3rem; }

.image-card__label {
  margin: 0;
  font-weight: var(--font-semibold);
  font-size: var(--text-sm);
  color: var(--color-text-muted);
}

/* Type selection buttons */
.image-card__options {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}
.type-btn {
  flex: 1;
  min-width: 80px;
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  border-radius: var(--radius-md);
  padding: var(--space-2) var(--space-2);
  cursor: pointer;
  font-size: var(--text-sm);
  transition: background var(--transition-fast), border-color var(--transition-fast);
}
.type-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.type-btn--selected {
  border-color: var(--color-primary);
  background: var(--color-primary-light);
  color: var(--color-primary-dark);
  font-weight: var(--font-semibold);
}

/* Submit */
.submit-btn {
  justify-self: start;
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  border-radius: var(--radius-md);
  padding: var(--space-2) var(--space-4);
  cursor: pointer;
  transition: background var(--transition-fast);
}
.submit-btn:disabled { opacity: 0.5; cursor: not-allowed; }

/* Inline result — copy from FakeNewsTask.vue */
.inline-result {
  border-radius: var(--radius-lg);
  padding: var(--space-4) var(--space-6);
  display: grid;
  gap: var(--space-2);
}
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
  font-weight: var(--font-semibold); font-size: var(--text-base);
  cursor: pointer;
  transition: background var(--transition-fast), transform var(--transition-fast);
}
.next-btn:hover  { background: var(--color-btn-primary-hover); }
.next-btn:active { transform: scale(0.98); }
.result-slide-enter-active { transition: transform 0.3s ease, opacity 0.3s ease; }
.result-slide-enter-from   { transform: translateY(-12px); opacity: 0; }
</style>
```

---

## Stop 6: SocialMediaTask.vue + SocialPost.vue

### SocialPost.vue — reusable post card

File: `frontend/src/components/student/SocialPost.vue`

```vue
<template>
  <article
    class="social-post"
    :class="{
      'social-post--selected':  selected,
      'social-post--clickable': clickable,
    }"
    :role="clickable ? 'button' : 'article'"
    :tabindex="clickable ? 0 : undefined"
    :aria-pressed="clickable ? selected : undefined"
    @click="clickable && $emit('click')"
    @keydown.enter="clickable && $emit('click')"
    @keydown.space.prevent="clickable && $emit('click')"
  >
    <!-- Header -->
    <div class="social-post__header">
      <span class="social-post__avatar" aria-hidden="true">{{ post.avatar }}</span>
      <div class="social-post__meta">
        <span class="social-post__name">
          {{ post.username }}
          <span v-if="post.verified" class="social-post__verified" title="Verifisert konto" aria-label="Verifisert">✓</span>
        </span>
        <span class="social-post__handle">{{ post.handle }}</span>
      </div>
      <span class="social-post__time">{{ post.timestamp }}</span>
    </div>

    <!-- Content -->
    <p class="social-post__content">{{ post.content }}</p>

    <!-- Engagement -->
    <div class="social-post__footer">
      <span>❤️ {{ formatNum(post.likes) }}</span>
      <span>💬 {{ formatNum(post.comments) }}</span>
      <span>🔁 {{ formatNum(post.shares ?? 0) }}</span>
    </div>
  </article>
</template>

<script setup>
defineProps({
  post:      { type: Object,  required: true },
  selected:  { type: Boolean, default: false },
  clickable: { type: Boolean, default: false },
})
defineEmits(['click'])

function formatNum(n) {
  if (n >= 1000) return (n / 1000).toFixed(1) + 'k'
  return String(n)
}
</script>

<style scoped>
.social-post {
  background: var(--color-surface);
  border: 2px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  display: grid;
  gap: var(--space-3);
  transition: border-color var(--transition-fast), background var(--transition-fast);
}
.social-post--clickable { cursor: pointer; }
.social-post--clickable:hover { border-color: var(--color-primary); }
.social-post--selected {
  border-color: var(--color-primary);
  background: var(--color-primary-light);
}
.social-post--clickable:focus-visible {
  outline: 2px solid var(--color-focus);
  outline-offset: 2px;
}

.social-post__header {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}
.social-post__avatar {
  font-size: 2rem;
  width: 44px; height: 44px;
  display: flex; align-items: center; justify-content: center;
  background: var(--color-bg);
  border-radius: var(--radius-full);
  border: 1px solid var(--color-border);
  flex-shrink: 0;
}
.social-post__meta {
  flex: 1;
  display: flex; flex-direction: column; gap: 2px;
}
.social-post__name {
  font-weight: var(--font-bold);
  font-size: var(--text-sm);
}
.social-post__verified {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 16px; height: 16px;
  background: #2563EB;
  color: #fff;
  border-radius: var(--radius-full);
  font-size: 10px;
  margin-left: 4px;
}
.social-post__handle {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
}
.social-post__time {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
  flex-shrink: 0;
}
.social-post__content {
  margin: 0;
  font-size: var(--text-base);
  line-height: 1.55;
}
.social-post__footer {
  display: flex;
  gap: var(--space-4);
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  padding-top: var(--space-2);
  border-top: 1px solid var(--color-border);
}
</style>
```

---

### SocialMediaTask.vue

File: `frontend/src/components/student/SocialMediaTask.vue`

```vue
<template>
  <section class="task-card">
    <div class="social-header">
      <span class="social-header__logo" aria-hidden="true">📘</span>
      <span class="social-header__name">Fjesbok.no</span>
    </div>
    <h2>{{ taskSubtype === 'CHOOSE_ACTION' ? 'Hva bør du gjøre?' : 'Finn det mest illegitime innlegget' }}</h2>
    <p class="guidance">{{ task.guidanceText }}</p>

    <!-- CHOOSE_ACTION: single post + action buttons -->
    <template v-if="taskSubtype === 'CHOOSE_ACTION'">
      <SocialPost :post="singlePost" />
      <p class="question">{{ contentJson.question }}</p>
      <div class="action-options">
        <button
          v-for="opt in contentJson.options"
          :key="opt.id"
          class="action-btn"
          :class="{ 'action-btn--selected': action === opt.id }"
          :disabled="!!result"
          @click="action = opt.id"
        >
          {{ opt.text }}
        </button>
      </div>
      <button v-if="!result" class="submit-btn" :disabled="!action" @click="submitAction">
        Send svar
      </button>
    </template>

    <!-- IDENTIFY_WORST: multiple posts, pick one -->
    <template v-else-if="taskSubtype === 'IDENTIFY_WORST'">
      <p class="question">{{ contentJson.question }}</p>
      <div class="posts-list">
        <SocialPost
          v-for="post in contentJson.posts"
          :key="post.id"
          :post="post"
          :selected="selectedPost === post.id"
          :clickable="!result"
          @click="selectedPost = post.id"
        />
      </div>
      <button v-if="!result" class="submit-btn" :disabled="!selectedPost" @click="submitIdentify">
        Send svar
      </button>
    </template>

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
          🎉 Du fullførte Den sosiale møteplassen!
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
import SocialPost from '@/components/student/SocialPost.vue'

const props = defineProps({
  task:       { type: Object,  required: true },
  result:     { type: Object,  default: null },
  isLastTask: { type: Boolean, default: false },
})
const emit = defineEmits(['submitted', 'next'])

const action       = ref('')
const selectedPost = ref(null)

const contentJson  = computed(() => props.task?.contentJson ?? {})
const taskSubtype  = computed(() => contentJson.value.type ?? 'IDENTIFY_WORST')
const singlePost   = computed(() => contentJson.value.post ?? {})

watch(() => props.task?.id, () => {
  action.value       = ''
  selectedPost.value = null
}, { immediate: true })

function submitAction() {
  if (!action.value) return
  console.log('[SocialMediaTask] Submitting action:', action.value)
  emit('submitted', { action: action.value })
}

function submitIdentify() {
  if (!selectedPost.value) return
  console.log('[SocialMediaTask] Submitting selected post:', selectedPost.value)
  emit('submitted', { selected: selectedPost.value })
}
</script>

<style scoped>
.task-card { display: grid; gap: var(--space-4); }

.social-header {
  display: flex; align-items: center; gap: var(--space-2);
  background: #1877F2;
  color: #fff;
  border-radius: var(--radius-md);
  padding: var(--space-2) var(--space-4);
  width: fit-content;
}
.social-header__logo { font-size: 1.4rem; }
.social-header__name { font-weight: var(--font-bold); font-size: var(--text-base); }

.guidance { margin: 0; color: var(--color-text-muted); }
.question { margin: 0; font-weight: var(--font-semibold); }

.posts-list { display: grid; gap: var(--space-3); }

.action-options {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: var(--space-3);
}
.action-btn {
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  border-radius: var(--radius-md);
  padding: var(--space-3) var(--space-3);
  cursor: pointer;
  text-align: left;
  line-height: 1.4;
  transition: background var(--transition-fast), border-color var(--transition-fast);
}
.action-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.action-btn--selected {
  border-color: var(--color-primary);
  background: var(--color-primary-light);
  color: var(--color-primary-dark);
  font-weight: var(--font-semibold);
}

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
  font-weight: var(--font-semibold); font-size: var(--text-base);
  cursor: pointer;
  transition: background var(--transition-fast), transform var(--transition-fast);
}
.next-btn:hover  { background: var(--color-btn-primary-hover); }
.next-btn:active { transform: scale(0.98); }
.result-slide-enter-active { transition: transform 0.3s ease, opacity 0.3s ease; }
.result-slide-enter-from   { transform: translateY(-12px); opacity: 0; }
</style>
```
