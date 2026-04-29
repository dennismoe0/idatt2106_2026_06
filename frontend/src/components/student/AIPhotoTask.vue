<template>
  <section class="task-card">
    <h2>Fotografen – Bildeanalyse</h2>
    <p class="guidance">{{ task.guidanceText }}</p>

    <!-- FIND_ARTIFACTS sub-type: click on AI artefacts in the image -->
    <template v-if="subtype === 'FIND_ARTIFACTS'">
      <p class="question">{{ content.question ?? 'Klikk på tegn på KI-generering eller manipulasjon i bildet.' }}</p>

      <p class="find-progress">
        Funnet: <strong>{{ foundIds.size }} / {{ requiredFound }}</strong>
        <span v-if="foundIds.size >= requiredFound" class="find-progress__done"> — nok! Send svar.</span>
      </p>

      <div
        class="artifacts-wrap"
        :class="{ 'artifacts-wrap--done': !!result }"
        ref="containerRef"
        data-peek-trigger
        @click="handleClick"
      >
        <img
          v-if="content.image?.src"
          :src="content.image.src"
          :alt="content.image.alt"
          class="artifacts-img"
          draggable="false"
        />
        <div v-else class="artifacts-placeholder">
          <span aria-hidden="true">🖼️</span>
          <span>{{ content.image?.alt ?? 'Bilde mangler' }}</span>
        </div>

        <!-- Found artifact rings -->
        <div
          v-for="id in foundIds"
          :key="'found-' + id"
          class="artifact-ring artifact-ring--found"
          :style="{ left: artifactById(id)?.x + '%', top: artifactById(id)?.y + '%' }"
          :title="artifactById(id)?.label"
        />

        <!-- Misclick X markers -->
        <TransitionGroup name="miss-fade">
          <div
            v-for="m in misclicks"
            :key="m.id"
            class="artifact-miss"
            :style="{ left: m.x + '%', top: m.y + '%' }"
          >✗</div>
        </TransitionGroup>

        <!-- After result: reveal all artifact locations -->
        <template v-if="result">
          <div
            v-for="a in artifacts"
            :key="'reveal-' + a.id"
            class="artifact-ring"
            :class="foundIds.has(a.id) ? 'artifact-ring--correct' : 'artifact-ring--missed'"
            :style="{ left: a.x + '%', top: a.y + '%' }"
          />
        </template>
      </div>

      <!-- Post-result artifact explanations -->
      <ul v-if="result" class="artifact-list">
        <li v-for="a in artifacts" :key="'exp-' + a.id" class="artifact-list__item">
          <span :class="foundIds.has(a.id) ? 'artifact-list__icon--found' : 'artifact-list__icon--missed'">
            {{ foundIds.has(a.id) ? '✅' : '🔍' }}
          </span>
          <strong>{{ a.label }}</strong>: {{ a.explanation }}
        </li>
      </ul>
    </template>

    <!-- Default: classify each image -->
    <template v-else>
      <div class="images-grid">
        <div
          v-for="(img, index) in images"
          :key="img.id"
          class="image-card"
          data-peek-trigger
          :class="{
            'image-card--answered': !!result,
            'image-card--correct':  result?.correct,
            'image-card--wrong':    result && !result.correct
          }"
        >
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
    </template>

    <button
      v-if="!result"
      class="submit-btn"
      :disabled="!isReady"
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
          🎉 Du fullførte Fotografen!
        </p>
        <div class="inline-result__actions">
          <button v-if="!result.correct" class="next-btn" @click="$emit('retry')">
            Prøv igjen
          </button>
          <button v-else class="next-btn" @click="$emit('next')">
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

const emit = defineEmits(['submitted', 'next', 'retry'])

const content  = computed(() => props.task?.contentJson ?? {})
const subtype  = computed(() => (content.value.type ?? '').toUpperCase())
const images   = computed(() => content.value.images ?? [])
const artifacts      = computed(() => content.value.artifacts ?? [])
const requiredFound  = computed(() => content.value.requiredFound ?? artifacts.value.length)

// Classify mode state
const answers = ref({})

// Find-artifacts mode state
const foundIds    = ref(new Set())
const misclicks   = ref([])
const containerRef = ref(null)

watch(() => props.task?.id, () => {
  answers.value = {}
  foundIds.value = new Set()
  misclicks.value = []
}, { immediate: true })

function artifactById(id) {
  return artifacts.value.find(a => a.id === id) ?? null
}

function handleClick(e) {
  if (props.result || foundIds.value.size >= requiredFound.value) return
  const rect = containerRef.value.getBoundingClientRect()
  const x = ((e.clientX - rect.left) / rect.width) * 100
  const y = ((e.clientY - rect.top)  / rect.height) * 100

  const hit = artifacts.value.find(a => {
    const dx = x - a.x
    const dy = y - a.y
    return Math.sqrt(dx * dx + dy * dy) < (a.radius ?? 8) && !foundIds.value.has(a.id)
  })

  if (hit) {
    foundIds.value = new Set([...foundIds.value, hit.id])
    console.log('[AIPhotoTask] Artifact found:', hit.id)
  } else {
    const mc = { id: Date.now(), x, y }
    misclicks.value.push(mc)
    setTimeout(() => { misclicks.value = misclicks.value.filter(m => m.id !== mc.id) }, 700)
  }
}

const isReady = computed(() => {
  if (subtype.value === 'FIND_ARTIFACTS') return foundIds.value.size >= requiredFound.value
  return images.value.length > 0 && images.value.every((_, i) => answers.value[`image_${i}`] !== undefined)
})

function setAnswer(index, value) {
  answers.value[`image_${index}`] = value
}

function submit() {
  if (!isReady.value) return
  let answer
  if (subtype.value === 'FIND_ARTIFACTS') {
    answer = { foundArtifactIds: [...foundIds.value] }
  } else {
    answer = { ...answers.value }
  }
  console.log('[AIPhotoTask] Submitting:', answer)
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

.question {
  margin: 0;
  font-weight: var(--font-semibold);
  color: var(--color-text);
}

/* ── Find artifacts ── */
.find-progress {
  margin: 0;
  font-size: var(--text-sm);
  color: var(--color-text-muted);
}
.find-progress__done { color: var(--color-success); font-weight: var(--font-semibold); }

.artifacts-wrap {
  position: relative;
  cursor: crosshair;
  border-radius: var(--radius-lg);
  overflow: hidden;
  border: 2px solid var(--color-border);
  user-select: none;
}
.artifacts-wrap--done { cursor: default; }

.artifacts-img {
  display: block;
  width: 100%;
  max-height: 360px;
  object-fit: cover;
}

.artifacts-placeholder {
  height: 240px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  color: var(--color-text-muted);
  font-size: var(--text-sm);
  background: var(--color-surface);
}
.artifacts-placeholder span:first-child { font-size: 3rem; }

.artifact-ring {
  position: absolute;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  pointer-events: none;
}
.artifact-ring--found {
  border: 3px solid var(--color-success);
  background: rgba(34,197,94,0.18);
  animation: ring-pop 0.25s ease;
}
.artifact-ring--correct {
  border: 3px solid var(--color-success);
  background: rgba(34,197,94,0.12);
}
.artifact-ring--missed {
  border: 3px solid var(--color-warning);
  background: rgba(234,179,8,0.12);
}

@keyframes ring-pop {
  from { transform: translate(-50%, -50%) scale(0.4); opacity: 0; }
  to   { transform: translate(-50%, -50%) scale(1);   opacity: 1; }
}

.artifact-miss {
  position: absolute;
  transform: translate(-50%, -50%);
  color: var(--color-danger);
  font-size: var(--text-lg);
  font-weight: 700;
  pointer-events: none;
}

.miss-fade-enter-active { transition: opacity 0.15s; }
.miss-fade-leave-active { transition: opacity 0.5s ease 0.2s; }
.miss-fade-leave-to     { opacity: 0; }

.artifact-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}
.artifact-list__item {
  display: flex;
  gap: var(--space-2);
  font-size: var(--text-sm);
  color: var(--color-text);
  line-height: 1.4;
}
.artifact-list__icon--found  { flex-shrink: 0; }
.artifact-list__icon--missed { flex-shrink: 0; }

/* ── Classify mode ── */
.images-grid {
  display: grid;
  gap: var(--space-6);
  grid-template-columns: minmax(0, 1fr);
}

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

.image-card__img-wrap {
  border-radius: var(--radius-md);
  overflow: hidden;
  background: var(--color-bg);
  border: 1px solid var(--color-border);
}
.image-card__img {
  width: 100%;
  height: min(72vh, 760px);
  min-height: 420px;
  object-fit: contain;
  display: block;
}
.image-card__placeholder {
  height: min(72vh, 760px);
  min-height: 420px;
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
  padding: var(--space-2);
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

/* ── Submit ── */
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
.submit-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.submit-btn:hover:not(:disabled) { background: var(--color-btn-primary-hover); }

/* ── Inline result ── */
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

@media (max-width: 768px) {
  .image-card {
    padding: var(--space-3);
  }

  .image-card__img,
  .image-card__placeholder {
    height: min(68vh, 620px);
    min-height: 320px;
  }
}
</style>
