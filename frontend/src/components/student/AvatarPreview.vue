<template>
  <div class="avatar-preview">
    <AvatarComposer :selections="selections" :size="size" />
  </div>
</template>

<script setup>
import AvatarComposer from '@/components/student/avatar/AvatarComposer.vue'

defineProps({
  selections: { type: Object, required: true },
  size: { type: Number, default: 120 },
})
</script>

<style scoped>
.avatar-preview { display: inline-block; }
</style>
