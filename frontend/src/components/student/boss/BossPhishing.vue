<template>
  <div class="mini-task">
    <div class="mini-email">
      <p><strong>Fra:</strong> {{ challenge.email.fromName }} &lt;{{ challenge.email.fromEmail }}&gt;</p>
      <p><strong>Emne:</strong> {{ challenge.email.subject }}</p>
      <p class="mini-email__body">{{ challenge.email.body }}</p>
    </div>
    <p class="mini-task__q">{{ challenge.question ?? 'Hva bør du gjøre?' }}</p>
    <div class="mini-task__options">
      <button v-for="opt in challenge.options" :key="opt.id"
        :class="{ selected: pick === opt.id }"
        :disabled="submitted"
        @click="pick = opt.id">{{ opt.text }}</button>
    </div>
    <button class="mini-task__submit" :disabled="submitted || !pick" @click="$emit('answer', { action: pick })">Bekreft</button>
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
.mini-email { border: 1px solid var(--color-border); border-radius: var(--radius-md); padding: var(--space-3); background: var(--color-surface); font-size: var(--text-sm); display: grid; gap: var(--space-1); }
.mini-email p { margin: 0; }
.mini-email__body { padding-top: var(--space-2); border-top: 1px solid var(--color-border); }
.mini-task__options { display: grid; grid-template-columns: repeat(auto-fit, minmax(140px,1fr)); gap: var(--space-2); }
.mini-task__options button { border: 1px solid var(--color-border); background: var(--color-surface); border-radius: var(--radius-sm); padding: var(--space-2) var(--space-3); cursor: pointer; }
.mini-task__options button.selected { border-color: var(--color-primary); background: var(--color-primary-soft); color: var(--color-primary-dark); }
.mini-task__options button:disabled { opacity: 0.5; cursor: not-allowed; }
.mini-task__submit { background: var(--color-primary); color: var(--color-text-on-dark); border: none; border-radius: var(--radius-md); padding: var(--space-2) var(--space-5); font-weight: var(--font-bold); cursor: pointer; justify-self: start; min-height: 44px; }
.mini-task__submit:disabled { opacity: 0.4; cursor: not-allowed; }
</style>
