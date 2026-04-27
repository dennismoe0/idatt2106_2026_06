<template>
  <div class="mini-task">
    <p class="mini-task__q">Hvilken artikkel er ekte?</p>
    <div class="mini-task__articles">
      <label
        v-for="(article, i) in challenge.articles"
        :key="i"
        class="mini-article"
        :class="{ 'mini-article--selected': picks[i] === true }"
      >
        <strong>{{ article.headline }}</strong>
        <span class="mini-article__source">{{ article.source }}</span>
        <div class="mini-article__btns">
          <button :disabled="submitted" :class="{ selected: picks[i] === true }"  @click="set(i, true)">Ekte</button>
          <button :disabled="submitted" :class="{ selected: picks[i] === false }" @click="set(i, false)">Falsk</button>
        </div>
      </label>
    </div>
    <button class="mini-task__submit" :disabled="submitted || !isReady" @click="submit">Bekreft</button>
  </div>
</template>
<script setup>
import { ref, computed } from 'vue'
const props = defineProps({ challenge: Object, submitted: Boolean })
const emit = defineEmits(['answer'])
const picks = ref({})
const isReady = computed(() => props.challenge.articles?.every((_, i) => picks.value[i] !== undefined))
function set(i, val) { picks.value = { ...picks.value, [i]: val } }
function submit() {
  const ans = {}
  props.challenge.articles.forEach((_, i) => { ans[`article_${i}`] = picks.value[i] })
  emit('answer', ans)
}
</script>
<style scoped>
.mini-task { display: grid; gap: var(--space-3); }
.mini-task__q { font-weight: var(--font-semibold); margin: 0; }
.mini-task__articles { display: grid; gap: var(--space-3); grid-template-columns: repeat(auto-fit, minmax(220px,1fr)); }
.mini-article { border: 2px solid var(--color-border); border-radius: var(--radius-md); padding: var(--space-3); display: grid; gap: var(--space-2); }
.mini-article__source { font-size: var(--text-xs); color: var(--color-text-muted); }
.mini-article__btns { display: flex; gap: var(--space-2); }
.mini-article__btns button { border: 1px solid var(--color-border); background: var(--color-surface); border-radius: var(--radius-sm); padding: var(--space-1) var(--space-2); cursor: pointer; }
.mini-article__btns button.selected { border-color: var(--color-primary); background: var(--color-primary-soft); color: var(--color-primary-dark); }
.mini-article__btns button:disabled { opacity: 0.5; cursor: not-allowed; }
.mini-task__submit { background: var(--color-primary); color: var(--color-text-on-dark); border: none; border-radius: var(--radius-md); padding: var(--space-2) var(--space-5); font-weight: var(--font-bold); cursor: pointer; justify-self: start; min-height: 44px; }
.mini-task__submit:disabled { opacity: 0.4; cursor: not-allowed; }
</style>
