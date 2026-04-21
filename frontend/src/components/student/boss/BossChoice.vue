<template>
  <div class="mini-task">
    <div v-if="challenge.post" class="mini-post">
      <span class="mini-post__avatar">{{ challenge.post.avatar }}</span>
      <div class="mini-post__content">
        <p class="mini-post__username">{{ challenge.post.username }} <span class="mini-post__handle">{{ challenge.post.handle }}</span></p>
        <p class="mini-post__text">{{ challenge.post.content }}</p>
        <p class="mini-post__meta">❤️ {{ challenge.post.likes?.toLocaleString() }} · 💬 {{ challenge.post.comments?.toLocaleString() }} · {{ challenge.post.timestamp }}</p>
      </div>
    </div>
    <p class="mini-task__q">{{ challenge.question }}</p>
    <div class="mini-task__options">
      <button
        v-for="opt in challenge.options"
        :key="opt.id"
        :class="{ selected: pick === opt.id }"
        :disabled="submitted"
        @click="pick = opt.id"
      >{{ opt.text ?? opt.value }}</button>
    </div>
    <button class="mini-task__submit" :disabled="submitted || !pick" @click="$emit('answer', { selected: pick })">Bekreft</button>
  </div>
</template>
<script setup>
import { ref } from 'vue'
const props = defineProps({ challenge: Object, submitted: Boolean })
defineEmits(['answer'])
const pick = ref(null)
</script>
<style scoped>
.mini-task { display: grid; gap: var(--space-3); }
.mini-task__q { font-weight: var(--font-semibold); margin: 0; }
.mini-post { display: flex; gap: var(--space-3); padding: var(--space-3); border: 1px solid var(--color-border); border-radius: var(--radius-md); background: var(--color-surface); }
.mini-post__avatar { font-size: 2rem; flex-shrink: 0; }
.mini-post__content { display: grid; gap: 4px; }
.mini-post__username { margin: 0; font-weight: var(--font-bold); font-size: var(--text-sm); }
.mini-post__handle { font-weight: 400; color: var(--color-text-muted); }
.mini-post__text { margin: 0; font-size: var(--text-sm); }
.mini-post__meta { margin: 0; font-size: var(--text-xs); color: var(--color-text-muted); }
.mini-task__options { display: grid; grid-template-columns: repeat(auto-fit, minmax(140px,1fr)); gap: var(--space-2); }
.mini-task__options button { border: 1px solid var(--color-border); background: var(--color-surface); border-radius: var(--radius-sm); padding: var(--space-2) var(--space-3); cursor: pointer; }
.mini-task__options button.selected { border-color: var(--color-primary); background: var(--color-primary-soft); color: var(--color-primary-dark); }
.mini-task__options button:disabled { opacity: 0.5; cursor: not-allowed; }
.mini-task__submit { background: var(--color-primary); color: var(--color-text-on-dark); border: none; border-radius: var(--radius-md); padding: var(--space-2) var(--space-5); font-weight: var(--font-bold); cursor: pointer; justify-self: start; min-height: 44px; }
.mini-task__submit:disabled { opacity: 0.4; cursor: not-allowed; }
</style>
