<template>
  <svg viewBox="0 0 120 160" xmlns="http://www.w3.org/2000/svg"
       style="position:absolute;inset:0;width:100%;height:100%">
    <!-- torso -->
    <rect x="36" y="90" width="48" height="60" rx="8" :fill="skinColor" />
    <!-- arms -->
    <rect x="22" y="90" width="16" height="44" rx="8" :fill="skinColor" />
    <rect x="82" y="90" width="16" height="44" rx="8" :fill="skinColor" />

  </svg>
</template>
<script setup>
defineProps({ skinColor: { type: String, default: '#D08B5B' }, gender: { type: String, default: 'neutral' } })
</script>
