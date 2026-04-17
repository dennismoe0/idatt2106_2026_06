<template>
  <RouterView />
  <DevNav v-if="isDev" />
</template>

<script setup>
import DevNav from '@/components/common/DevNav.vue'
const isDev = import.meta.env.DEV
</script>
