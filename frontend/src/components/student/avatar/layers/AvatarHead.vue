<template>
  <svg viewBox="0 0 120 160" xmlns="http://www.w3.org/2000/svg"
       style="position:absolute;inset:0;width:100%;height:100%">
    <circle cx="60" cy="52" r="32" :fill="skinColor" />
  </svg>
</template>
<script setup>
defineProps({ skinColor: { type: String, default: '#D08B5B' } })
</script>
