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
  gap: 1rem;
}

.guidance {
  margin: 0;
  color: #475569;
}

.articles {
  display: grid;
  gap: 1rem;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
}

.article-card {
  border: 1px solid #cbd5e1;
  border-radius: 10px;
  padding: 1rem;
  background: #fff;
}

.article-card h3 {
  margin-top: 0;
}

.source {
  color: #64748b;
  font-size: 0.9rem;
}

.actions {
  display: flex;
  gap: 0.75rem;
  margin-top: 1rem;
}

button {
  border: 1px solid #94a3b8;
  background: #fff;
  border-radius: 8px;
  padding: 0.4rem 0.8rem;
  cursor: pointer;
}

button.selected {
  border-color: #0f766e;
  background: #ccfbf1;
  color: #134e4a;
}

.submit-btn {
  justify-self: start;
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
