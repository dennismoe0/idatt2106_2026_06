<template>
  <div class="tutorial">
    <div class="pinned-note tutorial__card" style="--card-rotate: -0.5deg">
      <h2 class="tutorial__title">{{ title }}</h2>

      <div class="tutorial__body">
        <AvatarPreview
          :selections="avatarStore.avatar ?? {}"
          :size="80"
          class="tutorial__avatar"
          aria-hidden="true"
        />
        <p class="tutorial__text">{{ instructions }}</p>
      </div>

      <div class="tutorial__actions">
        <button class="tutorial__start-btn" @click="$emit('start')">
          Start! →
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useAvatarStore } from '@/stores/avatar'
import AvatarPreview from '@/components/student/AvatarPreview.vue'

defineProps({
  title:        { type: String, required: true },
  instructions: { type: String, required: true }
})

defineEmits(['start'])

const avatarStore = useAvatarStore()
</script>

<style scoped>
.tutorial {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: var(--space-4);
  width: 100%;
  box-sizing: border-box;
}

.tutorial__card {
  transform: rotate(var(--card-rotate, 0deg));
  width: min(100%, 560px);
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.tutorial__title {
  margin: 0;
  font-size: var(--text-xl);
  font-weight: 700;
  color: var(--color-ink);
  text-align: center;
}

.tutorial__body {
  display: flex;
  align-items: flex-start;
  gap: var(--space-5);
}

.tutorial__avatar {
  width: 96px;
  height: 96px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.tutorial__text {
  margin: 0;
  font-size: var(--text-base);
  color: var(--color-ink);
  line-height: 1.6;
}

.tutorial__actions { display: flex; justify-content: center; }

.tutorial__start-btn {
  background: var(--color-wood);
  color: var(--color-gold);
  border: none;
  border-radius: var(--radius-md);
  padding: var(--space-3) var(--space-8);
  font-size: var(--text-lg);
  font-weight: 700;
  cursor: pointer;
  min-height: 44px;
  transition: background var(--transition-fast), transform var(--transition-fast);
}
.tutorial__start-btn:hover  { background: var(--color-wood-mid); }
.tutorial__start-btn:active { transform: scale(0.98); }
.tutorial__start-btn:focus-visible { outline: 3px solid var(--color-gold); outline-offset: 2px; }

@media (max-width: 480px) {
  .tutorial__body { flex-direction: column; align-items: center; }
}
</style>
