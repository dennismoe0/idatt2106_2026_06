<template>
  <component
    :is="componentTag"
    class="corkboard-card"
    :class="{ 'corkboard-card--locked': locked }"
    v-bind="linkProps"
    :aria-disabled="locked ? 'true' : undefined"
    @click="handleClick"
  >
    <span class="corkboard-card__icon" aria-hidden="true">{{ icon }}</span>
    <h2 class="corkboard-card__title">{{ title }}</h2>
    <p v-if="locked" class="corkboard-card__status">Kommer snart</p>
  </component>
</template>

<script setup>
import { computed } from 'vue'
import { RouterLink } from 'vue-router'

const props = defineProps({
  title: { type: String, required: true },
  icon: { type: String, default: '📌' },
  route: { type: String, default: '' },
  locked: { type: Boolean, default: false },
})

const isInteractive = computed(() => !props.locked && !!props.route)
const componentTag = computed(() => (isInteractive.value ? RouterLink : 'article'))
const linkProps = computed(() => (isInteractive.value ? { to: props.route } : {}))

function handleClick() {
  if (props.locked) {
    console.log('[CorkboardCard] Locked card clicked:', props.title)
    return
  }

  if (props.route) {
    console.log('[CorkboardCard] Navigating to route:', props.route)
    return
  }

  console.log('[CorkboardCard] Card clicked without route:', props.title)
}
</script>

<style scoped>
.corkboard-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 10rem;
  gap: var(--space-2);
  padding: var(--space-6);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-surface);
  color: var(--color-text);
  text-decoration: none;
  text-align: center;
}

.corkboard-card--locked {
  opacity: 0.6;
  background: var(--color-background-mute);
}

.corkboard-card__icon {
  font-size: 2rem;
}

.corkboard-card__title {
  margin: 0;
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
}

.corkboard-card__status {
  margin: 0;
  color: var(--color-text-muted);
  font-size: var(--text-sm);
}
</style>
