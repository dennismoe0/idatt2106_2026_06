<template>
  <svg v-if="hairStyle !== 'bald'" viewBox="0 0 120 160" xmlns="http://www.w3.org/2000/svg"
       style="position:absolute;inset:0;width:100%;height:100%">
    <path :d="PATHS[hairStyle] || PATHS.short" :fill="hairColor" />
  </svg>
</template>
<script setup>
const PATHS = {
  short:    'M 28 48 Q 28 14 60 14 Q 92 14 92 48 L 88 52 Q 84 24 60 24 Q 36 24 32 52 Z',
  long:     'M 28 48 Q 28 14 60 14 Q 92 14 92 48 L 92 90 Q 88 100 84 98 L 84 52 Q 80 24 60 24 Q 40 24 36 52 L 36 98 Q 32 100 28 90 Z',
  curly:    'M 28 50 Q 26 14 60 14 Q 94 14 92 50 Q 98 44 96 36 Q 100 52 92 56 Q 96 62 88 66 Q 80 24 60 24 Q 40 24 32 66 Q 24 62 28 56 Q 20 52 24 36 Q 22 44 28 50 Z',
  ponytail: 'M 28 48 Q 28 14 60 14 Q 92 14 92 48 L 88 52 Q 84 24 60 24 Q 36 24 32 52 Z M 86 52 Q 94 60 90 100 Q 86 104 84 100 Q 88 64 82 56 Z',
  buzz:     'M 30 52 Q 30 18 60 18 Q 90 18 90 52 Q 86 28 60 28 Q 34 28 30 52 Z',
  braids:   'M 28 48 Q 28 14 60 14 Q 92 14 92 48 L 88 52 Q 84 24 60 24 Q 36 24 32 52 Z M 28 56 Q 22 80 26 110 Q 28 116 32 110 Q 30 84 34 60 Z M 92 56 Q 98 80 94 110 Q 92 116 88 110 Q 90 84 86 60 Z',
  bun:      'M 28 48 Q 28 14 60 14 Q 92 14 92 48 L 88 52 Q 84 24 60 24 Q 36 24 32 52 Z M 48 16 Q 60 4 72 16 Q 72 28 60 28 Q 48 28 48 16 Z',
  afro:     'M 18 52 Q 16 8 60 8 Q 104 8 102 52 Q 102 36 88 30 Q 88 14 60 14 Q 32 14 32 30 Q 18 36 18 52 Z',
}
defineProps({ hairStyle: { type: String, default: 'short' }, hairColor: { type: String, default: '#8B4513' } })
</script>
