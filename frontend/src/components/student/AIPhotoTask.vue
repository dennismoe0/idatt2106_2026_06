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
