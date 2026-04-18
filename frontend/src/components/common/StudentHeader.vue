<template>
  <header class="student-header">
    <button v-if="showBack" class="student-header__back" @click="goBack" aria-label="Tilbake">
      ← Tilbake
    </button>
    <span v-else class="student-header__spacer" />

    <span class="student-header__title">{{ title }}</span>

    <div class="student-header__right">
      <PlayerHud />
      <SoundControls />
    </div>
  </header>
</template>

<script setup>
import { useRouter } from 'vue-router'
import SoundControls from '@/components/common/SoundControls.vue'
import PlayerHud from '@/components/common/PlayerHud.vue'

const router = useRouter()

const props = defineProps({
  title:    { type: String,  default: '' },
  showBack: { type: Boolean, default: true },
  backTo:   { type: Object,  default: null }
})

function goBack() {
  if (props.backTo) router.push(props.backTo)
  else router.back()
}
</script>

<style scoped>
.student-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-3) var(--space-6);
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-border);
  position: sticky;
  top: 0;
  z-index: 100;
}

.student-header__back {
  background: none;
  border: none;
  color: var(--color-primary);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  cursor: pointer;
  padding: var(--space-1) var(--space-2);
  border-radius: var(--radius-sm);
  min-width: 6rem;
  text-align: left;
  transition: background var(--transition-fast);
}
.student-header__back:hover { background: var(--color-primary-light); }

.student-header__spacer { min-width: 6rem; }

.student-header__title {
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
  color: var(--color-text);
}

.student-header__right {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  min-width: 6rem;
  justify-content: flex-end;
}

</style>
