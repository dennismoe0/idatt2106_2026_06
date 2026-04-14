<template>
  <main>
    <h1>Hjem (kommer snart)</h1>
    <button @click="authStore.logout()">Logg ut</button>
  </main>
</template>

<script setup>
import { useAuthStore } from '@/stores/auth'
const authStore = useAuthStore()
</script>
