<template>
  <svg viewBox="0 0 120 160" xmlns="http://www.w3.org/2000/svg"
       style="position:absolute;inset:0;width:100%;height:100%">
    <!-- round -->
    <template v-if="eyeStyle === 'round'">
      <ellipse cx="48" cy="54" rx="6" ry="6" fill="white" />
      <ellipse cx="48" cy="54" rx="3.5" ry="3.5" :fill="eyeColor" />
      <ellipse cx="72" cy="54" rx="6" ry="6" fill="white" />
      <ellipse cx="72" cy="54" rx="3.5" ry="3.5" :fill="eyeColor" />
    </template>
    <!-- narrow -->
    <template v-else-if="eyeStyle === 'narrow'">
      <ellipse cx="48" cy="55" rx="6" ry="4" fill="white" />
      <ellipse cx="48" cy="55" rx="3" ry="2.5" :fill="eyeColor" />
      <line x1="42" y1="51" x2="54" y2="52" stroke="#3d2314" stroke-width="1.5" stroke-linecap="round"/>
      <ellipse cx="72" cy="55" rx="6" ry="4" fill="white" />
      <ellipse cx="72" cy="55" rx="3" ry="2.5" :fill="eyeColor" />
      <line x1="66" y1="51" x2="78" y2="52" stroke="#3d2314" stroke-width="1.5" stroke-linecap="round"/>
    </template>
    <!-- wide -->
    <template v-else>
      <ellipse cx="48" cy="53" rx="6" ry="7.5" fill="white" />
      <ellipse cx="48" cy="53" rx="4" ry="4" :fill="eyeColor" />
      <ellipse cx="72" cy="53" rx="6" ry="7.5" fill="white" />
      <ellipse cx="72" cy="53" rx="4" ry="4" :fill="eyeColor" />
    </template>
    <!-- shared mouth -->
    <path d="M 52 68 Q 60 74 68 68" stroke="#8b4513" stroke-width="2" fill="none" stroke-linecap="round"/>
  </svg>
</template>
<script setup>
defineProps({ eyeStyle: { type: String, default: 'round' }, eyeColor: { type: String, default: '#4a3000' } })
</script>
