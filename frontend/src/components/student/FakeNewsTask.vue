<template>
  <section class="task-card">
    <h2>Falske nyheter</h2>
    <p class="guidance">{{ task.guidanceText }}</p>

    <div class="articles">
      <article
        v-for="(article, index) in articles"
        :key="index"
        class="article-card"
      >
        <h3>{{ article.headline }}</h3>
        <p class="source">{{ article.source }}</p>
        <p>{{ article.body }}</p>

        <div class="actions">
          <button
            :class="{ selected: answers[`article_${index}`] === true }"
            :aria-label="`Marker artikkel ${index + 1} som ekte`"
            @click="setAnswer(index, true)"
          >
            Ekte
          </button>
          <button
            :class="{ selected: answers[`article_${index}`] === false }"
            :aria-label="`Marker artikkel ${index + 1} som falsk`"
            @click="setAnswer(index, false)"
          >
            Falsk
          </button>
        </div>
      </article>
    </div>

    <button class="submit-btn" :disabled="!isReady" @click="submit">
      Send svar
    </button>
  </section>
</template>

<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  task: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['submitted'])

const answers = ref({})

const articles = computed(() => props.task?.contentJson?.articles ?? [])

watch(
  () => props.task?.id,
  () => {
    answers.value = {}
  },
  { immediate: true }
)

const isReady = computed(() => {
  if (articles.value.length === 0) return false
  return articles.value.every((_, index) => answers.value[`article_${index}`] !== undefined)
})

function setAnswer(index, value) {
  answers.value[`article_${index}`] = value
}

function submit() {
  if (!isReady.value) return
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

.articles {
  display: grid;
  gap: var(--space-4);
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
}

.article-card {
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  background: var(--color-surface);
}

.article-card h3 {
  margin-top: 0;
}

.source {
  color: var(--color-text-muted);
  font-size: var(--text-sm);
}

.actions {
  display: flex;
  gap: var(--space-3);
  margin-top: var(--space-4);
}

button {
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  border-radius: var(--radius-md);
  padding: var(--space-1) var(--space-3);
  cursor: pointer;
}

button.selected {
  border-color: var(--color-primary);
  background: var(--color-primary-light);
  color: var(--color-primary-dark);
}

.submit-btn {
  justify-self: start;
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
