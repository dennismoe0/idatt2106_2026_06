<template>
  <div class="cork-board-page">
    <DetectiveBar :back-to="backTo" :page-title="pageTitle" />
    <main class="cork-board-page__content cork-board-bg">
      <slot />
    </main>
  </div>
</template>

<script setup>
import DetectiveBar from '@/components/common/DetectiveBar.vue'

defineProps({
  backTo:    { type: [Object, String], default: null },
  pageTitle: { type: String, required: true }
})
</script>

<style scoped>
.cork-board-page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}
.cork-board-page__content {
  flex: 1;
  padding: var(--space-6) clamp(var(--space-4), 5vw, var(--space-10));
}
</style>
