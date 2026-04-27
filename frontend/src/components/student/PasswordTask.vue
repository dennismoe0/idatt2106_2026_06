<template>
  <section class="task-card">
    <h2>Passordbanken</h2>
    <p class="guidance">{{ task.guidanceText }}</p>

    <!-- CHOICE: pick the strongest password -->
    <template v-if="type === 'CHOICE'">
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
          <span class="option-btn__marker" aria-hidden="true"></span>
          <span class="option-btn__body">
            <span class="option-btn__label">Passord {{ String(opt.id).toUpperCase() }}</span>
            <code class="option-code">{{ opt.value }}</code>
          </span>
        </button>
      </div>
    </template>

    <!-- BUILDER: assemble a strong password from tiles -->
    <template v-else-if="type === 'BUILDER'">
      <p class="question">{{ content.question }}</p>

      <div class="builder__display" aria-live="polite">
        <span class="builder__kicker">Hvelvkonsoll</span>
        <span class="builder__password" :aria-label="`Bygget passord: ${builtPassword || 'tomt'}`">
          {{ builtPassword || '—' }}
        </span>
        <div
          class="builder__strength"
          :class="`builder__strength--${strengthKey}`"
          aria-label="Passordstyrke"
        >
          <span class="builder__strength-bar">
            <span class="builder__strength-fill" :style="{ width: strengthWidth }"></span>
          </span>
          <span class="builder__strength-label">{{ strengthLabel }}</span>
        </div>
        <div class="builder__limits" aria-live="polite">
          <span>{{ parts.length }} / {{ maxPartsLabel }} brikker</span>
          <span>{{ builtPassword.length }} / {{ maxLengthLabel }} tegn</span>
        </div>
      </div>

      <div class="builder__tile-bank">
        <div class="builder__tile-section">
          <span class="builder__tile-heading">Ord</span>
          <div class="builder__tiles">
            <button
              v-for="w in content.words" :key="'w_' + w"
              class="tile tile--word"
              :disabled="isTileDisabled(w)"
              @click="addPart(w)"
            >{{ w }}</button>
          </div>
        </div>
        <div class="builder__tile-section">
          <span class="builder__tile-heading">Tall</span>
          <div class="builder__tiles">
            <button
              v-for="n in content.numbers" :key="'n_' + n"
              class="tile tile--number"
              :disabled="isTileDisabled(n)"
              @click="addPart(n)"
            >{{ n }}</button>
          </div>
        </div>
        <div class="builder__tile-section">
          <span class="builder__tile-heading">Symbol</span>
          <div class="builder__tiles">
            <button
              v-for="s in content.symbols" :key="'s_' + s"
              class="tile tile--symbol"
              :disabled="isTileDisabled(s)"
              @click="addPart(s)"
            >{{ s }}</button>
          </div>
        </div>
      </div>

      <button class="clear-btn" :disabled="!!result || parts.length === 0" @click="parts = []">
        Tøm
      </button>
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
        <p class="inline-result__label">{{ result.correct ? '✅ Riktig!' : '❌ Ikke helt riktig' }}</p>
        <p class="inline-result__explanation">{{ result.explanation }}</p>
        <p v-if="result.stopCompleted" class="inline-result__stop">🎉 Du fullførte Passordbanken!</p>
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

const selected = ref(null)
const parts    = ref([])

const content = computed(() => props.task?.contentJson ?? {})
const type    = computed(() => content.value.type ?? 'CHOICE')
const maxParts = computed(() => Number.isFinite(Number(content.value.maxParts)) ? Number(content.value.maxParts) : Infinity)
const maxLength = computed(() => Number.isFinite(Number(content.value.maxLength)) ? Number(content.value.maxLength) : Infinity)
const maxPartsLabel = computed(() => Number.isFinite(maxParts.value) ? maxParts.value : '∞')
const maxLengthLabel = computed(() => Number.isFinite(maxLength.value) ? maxLength.value : '∞')

watch(() => props.task?.id, () => {
  selected.value = null
  parts.value = []
}, { immediate: true })

const builtPassword = computed(() => parts.value.join(''))

function containsPitfall(pw) {
  const normalizedPassword = String(pw).toLowerCase()
  return (content.value.pitfalls ?? []).some(pitfall => {
    const normalizedPitfall = String(pitfall).toLowerCase()
    return normalizedPitfall && normalizedPassword.includes(normalizedPitfall)
  })
}

function evaluateStrength(pw) {
  if (!pw || containsPitfall(pw)) return 'WEAK'
  let score = 0
  if (pw.length >= 12)     score += 3
  else if (pw.length >= 8) score += 2
  else if (pw.length >= 6) score += 1
  if (/[A-ZÆØÅ]/.test(pw))              score++
  if (/[a-zæøå]/.test(pw))              score++
  if (/[0-9]/.test(pw))                 score++
  if (/[^A-Za-z0-9æøåÆØÅ]/.test(pw))   score++
  if (score <= 3) return 'WEAK'
  if (score <= 5) return 'MEDIUM'
  return 'STRONG'
}

const strengthKey   = computed(() => evaluateStrength(builtPassword.value).toLowerCase())
const strengthLabel = computed(() => ({ weak: 'Svakt', medium: 'Middels', strong: 'Klar - sterkt' }[strengthKey.value]))
const strengthWidth = computed(() => ({ weak: '33%', medium: '66%', strong: '100%' }[strengthKey.value]))

const isReady = computed(() => {
  if (type.value === 'BUILDER') return strengthKey.value === 'strong'
  return !!selected.value
})

function canAddPart(p) {
  return !props.result
    && parts.value.length < maxParts.value
    && builtPassword.value.length + String(p).length <= maxLength.value
}

function isTileDisabled(p) { return !canAddPart(p) }

function addPart(p) {
  if (!canAddPart(p)) return
  parts.value = [...parts.value, p]
}

function submit() {
  if (!isReady.value) return
  const answer = type.value === 'BUILDER'
    ? { password: builtPassword.value, parts: [...parts.value], strength: evaluateStrength(builtPassword.value) }
    : { selected: selected.value }
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

/* ── CHOICE options ── */
.options {
  display: grid;
  gap: var(--space-3);
}

.option-btn {
  display: grid;
  grid-template-columns: auto 1fr;
  align-items: center;
  gap: var(--space-3);
  text-align: left;
  border: 2px solid var(--color-border);
  background: linear-gradient(180deg, var(--color-surface), var(--color-bg));
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  cursor: pointer;
  box-shadow: var(--shadow-sm);
  transition: border-color var(--transition-fast), background var(--transition-fast), transform var(--transition-fast), box-shadow var(--transition-fast);
  font-size: var(--text-sm);
}
.option-btn:hover:not(:disabled) {
  border-color: var(--color-primary);
  box-shadow: var(--shadow-md);
  transform: translateY(-1px);
}
.option-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.option-btn--selected {
  border-color: var(--color-primary);
  background: var(--color-primary-light);
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--color-primary) 18%, transparent);
}
.option-btn__marker {
  width: 18px;
  height: 18px;
  border: 2px solid var(--color-border);
  border-radius: var(--radius-full);
  background: var(--color-surface);
  box-shadow: inset 0 0 0 4px var(--color-surface);
}
.option-btn--selected .option-btn__marker {
  border-color: var(--color-primary);
  background: var(--color-primary);
}
.option-btn__body {
  display: grid;
  gap: var(--space-1);
  min-width: 0;
}
.option-btn__label {
  color: var(--color-text-muted);
  font-size: var(--text-xs);
  font-weight: var(--font-semibold);
  text-transform: uppercase;
}

.option-code {
  font-family: monospace;
  font-size: var(--text-lg);
  letter-spacing: 0.05em;
  color: var(--color-text);
  overflow-wrap: anywhere;
}

/* ── BUILDER ── */
.builder__display {
  border: 1px solid color-mix(in srgb, var(--color-primary) 40%, var(--color-border));
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  background: linear-gradient(180deg, color-mix(in srgb, var(--color-primary) 8%, var(--color-surface)), var(--color-surface));
  display: grid;
  gap: var(--space-3);
  box-shadow: var(--shadow-md);
}

.builder__kicker,
.builder__tile-heading {
  color: var(--color-text-muted);
  font-size: var(--text-xs);
  font-weight: var(--font-semibold);
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.builder__password {
  font-family: monospace;
  font-size: 1.6rem;
  font-weight: var(--font-bold);
  letter-spacing: 0.08em;
  word-break: break-all;
  color: var(--color-text);
  min-height: 1.6em;
  padding: var(--space-3);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-bg);
}

.builder__strength { display: flex; align-items: center; gap: var(--space-2); }
.builder__strength-bar {
  flex: 1;
  height: 8px;
  background: var(--color-border);
  border-radius: var(--radius-full);
  overflow: hidden;
}
.builder__strength-fill {
  display: block;
  height: 100%;
  border-radius: var(--radius-full);
  transition: width 0.3s ease;
}
.builder__strength--weak   .builder__strength-fill { background: var(--color-danger); }
.builder__strength--medium .builder__strength-fill { background: var(--color-warning); }
.builder__strength--strong .builder__strength-fill { background: var(--color-success); }
.builder__strength-label {
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  white-space: nowrap;
}
.builder__strength--weak   .builder__strength-label { color: var(--color-danger); }
.builder__strength--medium .builder__strength-label { color: var(--color-warning); }
.builder__strength--strong .builder__strength-label { color: var(--color-success); }

.builder__limits {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2) var(--space-4);
  color: var(--color-text-muted);
  font-size: var(--text-xs);
  font-weight: var(--font-semibold);
}

.builder__tile-bank {
  display: grid;
  gap: var(--space-3);
}

.builder__tile-section {
  display: grid;
  gap: var(--space-2);
  padding: var(--space-3);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
}

.builder__tiles {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}
.tile {
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  border-radius: var(--radius-md);
  padding: var(--space-2) var(--space-3);
  cursor: pointer;
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  transition: background var(--transition-fast), transform var(--transition-fast);
}
.tile:hover:not(:disabled) { transform: translateY(-2px); }
.tile:disabled { opacity: 0.35; cursor: not-allowed; filter: grayscale(0.4); }
.tile--word   { background: var(--color-primary-light); border-color: var(--color-primary); color: var(--color-primary-dark); }
.tile--number { background: var(--color-success-light); border-color: var(--color-success); color: var(--color-success); }
.tile--symbol { background: var(--color-warning-light); border-color: var(--color-warning); color: var(--color-warning); }

.clear-btn {
  justify-self: start;
  background: transparent;
  border: 1px solid var(--color-danger);
  color: var(--color-danger);
  border-radius: var(--radius-md);
  padding: var(--space-1) var(--space-3);
  cursor: pointer;
  font-size: var(--text-sm);
}
.clear-btn:disabled { opacity: 0.4; cursor: not-allowed; }

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
</style>
