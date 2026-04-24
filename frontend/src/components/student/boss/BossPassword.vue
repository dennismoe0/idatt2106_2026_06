<template>
  <BossChoice :challenge="challenge" :submitted="submitted" @answer="$emit('answer', $event)" />
</template>

<script setup>
import BossChoice from '@/components/student/boss/BossChoice.vue'

defineProps({
  challenge: { type: Object, required: true },
  submitted: { type: Boolean, default: false },
})

defineEmits(['answer'])
</script>
