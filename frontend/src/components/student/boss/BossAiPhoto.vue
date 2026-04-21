<template>
  <div class="mini-task">
    <div v-for="(img, i) in challenge.images" :key="i" class="mini-photo">
      <div class="mini-photo__img-wrap">
        <img v-if="img.src" :src="img.src" :alt="img.alt" class="mini-photo__img" />
        <div v-else class="mini-photo__placeholder">{{ img.label }}</div>
      </div>
      <div class="mini-photo__options">
        <button v-for="opt in IMAGE_TYPES" :key="opt.value"
          :class="{ selected: picks[i] === opt.value }"
          :disabled="submitted"
          @click="set(i, opt.value)">{{ opt.label }}</button>
      </div>
    </div>
    <button class="mini-task__submit" :disabled="submitted || !isReady" @click="submit">Bekreft</button>
  </div>
</template>
<script setup>
import { ref, computed } from 'vue'
const IMAGE_TYPES = [
  { value: 'REAL',         label: 'Ekte' },
  { value: 'AI_GENERATED', label: 'KI-generert' },
  { value: 'MANIPULATED',  label: 'Manipulert' },
]
const props = defineProps({ challenge: Object, submitted: Boolean })
const emit = defineEmits(['answer'])
const picks = ref({})
const isReady = computed(() => props.challenge.images?.every((_, i) => picks.value[i] !== undefined))
function set(i, val) { picks.value = { ...picks.value, [i]: val } }
function submit() {
  const ans = {}
  props.challenge.images.forEach((_, i) => { ans[`image_${i}`] = picks.value[i] })
  emit('answer', ans)
}
</script>
<style scoped>
.mini-task { display: grid; gap: var(--space-3); }
.mini-photo { display: grid; gap: var(--space-2); }
.mini-photo__img-wrap { border-radius: var(--radius-md); overflow: hidden; background: var(--color-surface); border: 1px solid var(--color-border); }
.mini-photo__img { width: 100%; max-height: 200px; object-fit: cover; display: block; }
.mini-photo__placeholder { height: 140px; display: flex; align-items: center; justify-content: center; color: var(--color-text-muted); font-size: var(--text-lg); }
.mini-photo__options { display: flex; gap: var(--space-2); flex-wrap: wrap; }
.mini-photo__options button { border: 1px solid var(--color-border); background: var(--color-surface); border-radius: var(--radius-sm); padding: var(--space-1) var(--space-3); cursor: pointer; }
.mini-photo__options button.selected { border-color: var(--color-primary); background: var(--color-primary-soft); color: var(--color-primary-dark); }
.mini-photo__options button:disabled { opacity: 0.5; cursor: not-allowed; }
.mini-task__submit { background: var(--color-primary); color: var(--color-text-on-dark); border: none; border-radius: var(--radius-md); padding: var(--space-2) var(--space-5); font-weight: var(--font-bold); cursor: pointer; justify-self: start; min-height: 44px; }
.mini-task__submit:disabled { opacity: 0.4; cursor: not-allowed; }
</style>
