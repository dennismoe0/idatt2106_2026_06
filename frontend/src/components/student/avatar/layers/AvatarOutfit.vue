<template>
  <svg viewBox="0 0 120 160" xmlns="http://www.w3.org/2000/svg"
       style="position:absolute;inset:0;width:100%;height:100%">
    <!-- detective-coat -->
    <template v-if="outfit === 'detective-coat'">
      <rect x="28" y="92" width="64" height="64" rx="4" :fill="outfitColor" />
      <rect x="18" y="92" width="18" height="50" rx="8" :fill="outfitColor" />
      <rect x="84" y="92" width="18" height="50" rx="8" :fill="outfitColor" />
      <polygon points="54,92 60,108 66,92" :fill="darken(outfitColor)" />
    </template>
    <!-- hoodie -->
    <template v-else-if="outfit === 'hoodie'">
      <rect x="32" y="92" width="56" height="64" rx="6" :fill="outfitColor" />
      <rect x="20" y="92" width="16" height="46" rx="8" :fill="outfitColor" />
      <rect x="84" y="92" width="16" height="46" rx="8" :fill="outfitColor" />
      <ellipse cx="60" cy="92" rx="14" ry="8" :fill="darken(outfitColor)" />
    </template>
    <!-- sweater -->
    <template v-else-if="outfit === 'sweater'">
      <rect x="32" y="92" width="56" height="64" rx="4" :fill="outfitColor" />
      <rect x="20" y="92" width="16" height="46" rx="8" :fill="outfitColor" />
      <rect x="84" y="92" width="16" height="46" rx="8" :fill="outfitColor" />
      <rect x="44" y="92" width="32" height="6" rx="2" :fill="darken(outfitColor)" />
    </template>
    <!-- uniform -->
    <template v-else-if="outfit === 'uniform'">
      <rect x="32" y="92" width="56" height="64" rx="4" :fill="outfitColor" />
      <rect x="20" y="92" width="16" height="46" rx="8" :fill="outfitColor" />
      <rect x="84" y="92" width="16" height="46" rx="8" :fill="outfitColor" />
      <rect x="57" y="94" width="6" height="28" rx="2" :fill="darken(outfitColor)" />
      <circle cx="60" cy="100" r="2.5" fill="#FFD700" />
      <circle cx="60" cy="110" r="2.5" fill="#FFD700" />
    </template>
    <!-- raincoat -->
    <template v-else>
      <rect x="30" y="92" width="60" height="64" rx="6" :fill="outfitColor" />
      <rect x="18" y="92" width="18" height="50" rx="8" :fill="outfitColor" />
      <rect x="84" y="92" width="18" height="50" rx="8" :fill="outfitColor" />
      <path d="M 40 92 Q 60 102 80 92" :stroke="darken(outfitColor)" stroke-width="2" fill="none" />
    </template>
  </svg>
</template>
<script setup>
defineProps({ outfit: { type: String, default: 'detective-coat' }, outfitColor: { type: String, default: '#2563eb' } })

function darken(hex) {
  if (!/^#[0-9a-fA-F]{6}$/.test(hex)) {
    console.warn('[AvatarOutfit] darken() received non-hex color:', hex)
    return hex
  }
  const n = parseInt(hex.slice(1), 16)
  const r = Math.max(0, (n >> 16) - 40)
  const g = Math.max(0, ((n >> 8) & 0xff) - 40)
  const b = Math.max(0, (n & 0xff) - 40)
  return '#' + [r, g, b].map(c => c.toString(16).padStart(2, '0')).join('')
}
</script>
