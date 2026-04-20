# Sprint 2 — Krisase: PasswordTask.vue (Frontend)
*Part of: 2026-04-20-sprint2-game-logic-design.md*

You own the **frontend** for Stop 4 (Passordbanken).
Backend validation and seeds are done by **Higgins**.

Read section 2.2 of the main design doc for the contentJson contract.

There are two task subtypes:
- **CHOICE** (tasks 1 & 2): Pick the strongest password from a list
- **BUILDER** (task 3): Assemble a password from word/symbol/number tiles, live strength meter

---

## Architecture

`PasswordTask.vue` is the top-level component. It reads `contentJson.type` and renders either:
- `PasswordChoice.vue` — for CHOICE tasks
- `PasswordBuilder.vue` — for BUILDER tasks

Both child components emit `answer` with their payload. `PasswordTask.vue` relays it as `submitted`.

---

## PasswordTask.vue (parent)

File: `frontend/src/components/student/PasswordTask.vue`

```vue
<template>
  <section class="task-card">
    <h2>Passordbanken</h2>
    <p class="guidance">{{ task.guidanceText }}</p>

    <PasswordChoice
      v-if="taskSubtype === 'CHOICE'"
      :content="task.contentJson"
      :disabled="!!result"
      @answer="handleAnswer"
    />

    <PasswordBuilder
      v-else-if="taskSubtype === 'BUILDER'"
      :content="task.contentJson"
      :disabled="!!result"
      @answer="handleAnswer"
    />

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
          🎉 Du fullførte Passordbanken!
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
import { computed } from 'vue'
import PasswordChoice  from '@/components/student/PasswordChoice.vue'
import PasswordBuilder from '@/components/student/PasswordBuilder.vue'

const props = defineProps({
  task:       { type: Object,  required: true },
  result:     { type: Object,  default: null },
  isLastTask: { type: Boolean, default: false },
})
const emit = defineEmits(['submitted', 'next'])

const taskSubtype = computed(() => props.task?.contentJson?.type ?? 'CHOICE')

function handleAnswer(answer) {
  console.log('[PasswordTask] Submitting:', answer)
  emit('submitted', answer)
}
</script>

<style scoped>
.task-card { display: grid; gap: var(--space-4); }
.guidance  { margin: 0; color: var(--color-text-muted); }

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

## PasswordChoice.vue

File: `frontend/src/components/student/PasswordChoice.vue`

```vue
<template>
  <div class="choice">
    <p class="choice__question">{{ content.question }}</p>

    <div class="choice__options">
      <button
        v-for="opt in content.options"
        :key="opt.id"
        class="choice__option"
        :class="{ 'choice__option--selected': selected === opt.id }"
        :disabled="disabled"
        @click="selected = opt.id"
      >
        <span class="choice__option-value">{{ opt.value }}</span>
      </button>
    </div>

    <button
      class="submit-btn"
      :disabled="disabled || !selected"
      @click="$emit('answer', { selected })"
    >
      Send svar
    </button>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  content:  { type: Object,  required: true },
  disabled: { type: Boolean, default: false },
})
defineEmits(['answer'])

const selected = ref(null)
watch(() => props.content, () => { selected.value = null })
</script>

<style scoped>
.choice { display: grid; gap: var(--space-4); }
.choice__question { margin: 0; font-weight: var(--font-semibold); font-size: var(--text-base); }
.choice__options { display: grid; gap: var(--space-3); grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); }
.choice__option {
  border: 2px solid var(--color-border);
  background: var(--color-surface);
  border-radius: var(--radius-md);
  padding: var(--space-4) var(--space-4);
  cursor: pointer; text-align: left;
  transition: border-color var(--transition-fast), background var(--transition-fast);
}
.choice__option:disabled { opacity: 0.5; cursor: not-allowed; }
.choice__option--selected {
  border-color: var(--color-primary);
  background: var(--color-primary-light);
}
.choice__option-value {
  font-family: monospace;
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  color: var(--color-text);
  word-break: break-all;
}
.submit-btn {
  justify-self: start;
  border: 1px solid var(--color-border); background: var(--color-surface);
  border-radius: var(--radius-md); padding: var(--space-2) var(--space-4);
  cursor: pointer;
}
.submit-btn:disabled { opacity: 0.5; cursor: not-allowed; }
</style>
```

---

## PasswordBuilder.vue

File: `frontend/src/components/student/PasswordBuilder.vue`

### Strength algorithm (client-side, matches backend exactly)

```javascript
function evaluateStrength(password) {
  if (!password) return { level: 'WEAK', score: 0 }
  let score = 0
  if (password.length >= 12)     score += 3
  else if (password.length >= 8) score += 2
  else if (password.length >= 6) score += 1
  if (/[A-ZÆØÅ]/.test(password)) score++
  if (/[a-zæøå]/.test(password)) score++
  if (/[0-9]/.test(password))    score++
  if (/[^A-Za-z0-9æøåÆØÅ]/.test(password)) score++
  const level = score <= 3 ? 'WEAK' : score <= 5 ? 'MEDIUM' : 'STRONG'
  return { level, score }
}
```

### Component

```vue
<template>
  <div class="builder">
    <p class="builder__question">{{ content.question }}</p>

    <!-- Password display -->
    <div class="builder__preview" :aria-label="'Passordet ditt: ' + password || 'Tomt'">
      <span v-if="password" class="builder__password">{{ password }}</span>
      <span v-else class="builder__placeholder">Klikk på brikker for å bygge passordet ditt...</span>
      <button
        v-if="password"
        class="builder__clear"
        :disabled="disabled"
        aria-label="Slett alt"
        @click="password = ''"
      >✕</button>
    </div>

    <!-- Strength meter -->
    <div class="builder__strength" :aria-label="`Styrke: ${strengthLabel}`">
      <div class="strength-bar">
        <div
          class="strength-bar__fill"
          :class="`strength-bar__fill--${strength.level.toLowerCase()}`"
          :style="{ width: strengthPercent + '%' }"
        ></div>
      </div>
      <span class="strength-label" :class="`strength-label--${strength.level.toLowerCase()}`">
        {{ strengthLabel }}
      </span>
    </div>

    <!-- Tile groups -->
    <div class="builder__section">
      <p class="builder__section-title">📝 Ord</p>
      <div class="builder__tiles">
        <button
          v-for="word in content.words"
          :key="word"
          class="tile"
          :disabled="disabled"
          @click="password += word"
        >{{ word }}</button>
      </div>
    </div>

    <div class="builder__section">
      <p class="builder__section-title">🔣 Tegn</p>
      <div class="builder__tiles">
        <button
          v-for="sym in content.symbols"
          :key="sym"
          class="tile tile--symbol"
          :disabled="disabled"
          @click="password += sym"
        >{{ sym }}</button>
      </div>
    </div>

    <div class="builder__section">
      <p class="builder__section-title">🔢 Tall</p>
      <div class="builder__tiles">
        <button
          v-for="num in content.numbers"
          :key="num"
          class="tile tile--number"
          :disabled="disabled"
          @click="password += num"
        >{{ num }}</button>
      </div>
    </div>

    <button
      class="submit-btn"
      :disabled="disabled || strength.level !== 'STRONG'"
      @click="$emit('answer', { password })"
    >
      {{ strength.level === 'STRONG' ? 'Bruk dette passordet →' : 'Passordet er ikke sterkt nok ennå' }}
    </button>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  content:  { type: Object,  required: true },
  disabled: { type: Boolean, default: false },
})
defineEmits(['answer'])

const password = ref('')

watch(() => props.content, () => { password.value = '' })

function evaluateStrength(pw) {
  if (!pw) return { level: 'WEAK', score: 0 }
  let score = 0
  if (pw.length >= 12)     score += 3
  else if (pw.length >= 8) score += 2
  else if (pw.length >= 6) score += 1
  if (/[A-ZÆØÅ]/.test(pw)) score++
  if (/[a-zæøå]/.test(pw)) score++
  if (/[0-9]/.test(pw))    score++
  if (/[^A-Za-z0-9æøåÆØÅ]/.test(pw)) score++
  const level = score <= 3 ? 'WEAK' : score <= 5 ? 'MEDIUM' : 'STRONG'
  return { level, score }
}

const strength = computed(() => evaluateStrength(password.value))

const strengthLabel = computed(() => ({
  WEAK:   'Svakt',
  MEDIUM: 'Middels',
  STRONG: 'Sterkt ✓',
}[strength.value.level]))

const strengthPercent = computed(() => Math.min(100, Math.round((strength.value.score / 7) * 100)))
</script>

<style scoped>
.builder { display: grid; gap: var(--space-4); }
.builder__question { margin: 0; font-weight: var(--font-semibold); }

/* Password preview */
.builder__preview {
  min-height: 56px;
  display: flex; align-items: center; justify-content: space-between; gap: var(--space-3);
  background: var(--color-bg);
  border: 2px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: var(--space-3) var(--space-4);
  font-family: monospace;
}
.builder__password { font-size: var(--text-lg); font-weight: var(--font-bold); word-break: break-all; }
.builder__placeholder { color: var(--color-text-muted); font-family: inherit; font-style: italic; }
.builder__clear {
  flex-shrink: 0; background: none; border: none;
  color: var(--color-text-muted); font-size: var(--text-lg); cursor: pointer; padding: 4px;
}

/* Strength meter */
.builder__strength { display: flex; align-items: center; gap: var(--space-3); }
.strength-bar {
  flex: 1; height: 8px; background: var(--color-border);
  border-radius: var(--radius-full); overflow: hidden;
}
.strength-bar__fill {
  height: 100%; border-radius: var(--radius-full);
  transition: width 0.3s ease, background-color 0.3s ease;
}
.strength-bar__fill--weak   { background: var(--color-danger); }
.strength-bar__fill--medium { background: var(--color-warning); }
.strength-bar__fill--strong { background: var(--color-success); }
.strength-label { font-size: var(--text-sm); font-weight: var(--font-bold); min-width: 80px; text-align: right; }
.strength-label--weak   { color: var(--color-danger); }
.strength-label--medium { color: var(--color-warning); }
.strength-label--strong { color: var(--color-success); }

/* Tile sections */
.builder__section { display: grid; gap: var(--space-2); }
.builder__section-title { margin: 0; font-size: var(--text-sm); font-weight: var(--font-semibold); color: var(--color-text-muted); }
.builder__tiles { display: flex; flex-wrap: wrap; gap: var(--space-2); }
.tile {
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  border-radius: var(--radius-md);
  padding: var(--space-2) var(--space-3);
  cursor: pointer; font-size: var(--text-base);
  font-weight: var(--font-semibold);
  transition: background var(--transition-fast), border-color var(--transition-fast), transform var(--transition-fast);
}
.tile:hover:not(:disabled) { background: var(--color-primary-light); border-color: var(--color-primary); transform: translateY(-1px); }
.tile:disabled { opacity: 0.5; cursor: not-allowed; }
.tile--symbol { font-family: monospace; font-size: var(--text-lg); color: var(--color-danger); }
.tile--number { font-family: monospace; font-size: var(--text-lg); color: #1D4ED8; }

/* Submit */
.submit-btn {
  background: var(--color-primary); color: var(--color-text-on-dark);
  border: none; border-radius: var(--radius-md);
  padding: var(--space-3) var(--space-6);
  font-weight: var(--font-bold); font-size: var(--text-base);
  cursor: pointer; justify-self: start;
  transition: background var(--transition-fast);
}
.submit-btn:disabled { opacity: 0.4; cursor: not-allowed; background: var(--color-text-muted); }
.submit-btn:not(:disabled):hover { background: var(--color-btn-primary-hover); }
</style>
```
