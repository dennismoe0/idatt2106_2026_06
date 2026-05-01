<template>
  <div class="mini-task">
    <p class="mini-task__q">Hvilke deler av denne annonsen er mistenkelige?</p>
    <div class="mini-shop">
      <div class="mini-shop__domain-bar">
        <span class="mini-shop__domain-label">🔗</span>
        <span class="mini-shop__domain-url">{{ challenge.mockup?.url }}</span>
      </div>
      <div class="mini-shop__card">
        <img
          v-if="challenge.mockup?.image"
          :src="challenge.mockup.image"
          :alt="challenge.mockup?.title"
          class="mini-shop__img"
        />
        <div class="mini-shop__details">
          <p class="mini-shop__title">{{ challenge.mockup?.title }}</p>
          <p class="mini-shop__price">{{ challenge.mockup?.price }}</p>
          <p class="mini-shop__seller">Selger: {{ challenge.mockup?.seller }}</p>
          <p v-if="challenge.mockup?.payment" class="mini-shop__payment">Betaling: {{ challenge.mockup.payment }}</p>
        </div>
      </div>
    </div>
    <p class="mini-task__elements-label">Trykk på elementene som er røde flagg:</p>
    <div class="mini-shop__elements">
      <button
        v-for="el in challenge.elements"
        :key="el.id"
        class="mini-shop__element"
        :class="{ 'mini-shop__element--flagged': flagged.has(el.id) }"
        :disabled="submitted"
        @click="toggle(el.id)"
      >{{ el.label }}</button>
    </div>
    <button class="mini-task__submit" :disabled="submitted || flagged.size === 0" @click="submit">
      Bekreft
    </button>
  </div>
</template>

<script setup>
import { reactive } from 'vue'

const props = defineProps({ challenge: { type: Object, required: true }, submitted: { type: Boolean, default: false } })
const emit = defineEmits(['answer'])

const flagged = reactive(new Set())

function toggle(id) {
  if (flagged.has(id)) flagged.delete(id)
  else flagged.add(id)
}

function submit() {
  const ans = {}
  props.challenge.elements.forEach((el) => { ans[el.id] = flagged.has(el.id) })
  emit('answer', ans)
}
</script>

<style scoped>
.mini-task { display: grid; gap: var(--space-3); }
.mini-task__q { font-weight: var(--font-semibold); margin: 0; }
.mini-task__elements-label { margin: 0; font-size: var(--text-sm); color: var(--color-text-muted); }
.mini-task__submit { background: var(--color-primary); color: var(--color-text-on-dark); border: none; border-radius: var(--radius-md); padding: var(--space-2) var(--space-5); font-weight: var(--font-bold); cursor: pointer; justify-self: start; min-height: 44px; }
.mini-task__submit:disabled { opacity: 0.4; cursor: not-allowed; }

.mini-shop { display: grid; gap: var(--space-2); }
.mini-shop__domain-bar { display: flex; align-items: center; gap: var(--space-2); background: var(--color-surface); border: 1px solid var(--color-border); border-radius: var(--radius-sm); padding: var(--space-1) var(--space-2); font-size: var(--text-xs); }
.mini-shop__domain-url { font-family: monospace; color: var(--color-text-muted); word-break: break-all; }
.mini-shop__card { display: flex; gap: var(--space-3); border: 1px solid var(--color-border); border-radius: var(--radius-md); padding: var(--space-3); background: var(--color-surface); }
.mini-shop__img { width: 100px; height: 100px; object-fit: cover; border-radius: var(--radius-sm); flex-shrink: 0; }
.mini-shop__details { display: grid; gap: var(--space-1); align-content: start; }
.mini-shop__title { margin: 0; font-weight: var(--font-semibold); font-size: var(--text-sm); }
.mini-shop__price { margin: 0; font-size: var(--text-lg); font-weight: var(--font-bold); color: var(--color-danger); }
.mini-shop__seller,
.mini-shop__payment { margin: 0; font-size: var(--text-xs); color: var(--color-text-muted); }

.mini-shop__elements { display: flex; flex-wrap: wrap; gap: var(--space-2); }
.mini-shop__element { border: 2px solid var(--color-border); background: var(--color-surface); border-radius: var(--radius-sm); padding: var(--space-1) var(--space-3); cursor: pointer; font-size: var(--text-sm); transition: all var(--transition-fast); }
.mini-shop__element--flagged { background: var(--color-danger-light); border-color: var(--color-danger); color: var(--color-danger-dark, #991b1b); font-weight: var(--font-semibold); }
.mini-shop__element:disabled { opacity: 0.5; cursor: not-allowed; }
</style>
