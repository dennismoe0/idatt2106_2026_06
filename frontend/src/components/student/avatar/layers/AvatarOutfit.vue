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
    <!-- bomber: short jacket, ribbed collar, cuffs, bottom band -->
    <template v-else-if="outfit === 'bomber'">
      <rect x="30" y="92" width="60" height="44" rx="4" :fill="outfitColor" />
      <rect x="18" y="92" width="16" height="40" rx="8" :fill="outfitColor" />
      <rect x="86" y="92" width="16" height="40" rx="8" :fill="outfitColor" />
      <ellipse cx="60" cy="92" rx="14" ry="6" :fill="darken(outfitColor)" />
      <rect x="16" y="126" width="20" height="6" rx="2" :fill="darken(outfitColor)" />
      <rect x="84" y="126" width="20" height="6" rx="2" :fill="darken(outfitColor)" />
      <rect x="30" y="130" width="60" height="7" rx="3" :fill="darken(outfitColor)" />
    </template>

    <!-- trench-coat: long coat with belt and V lapels -->
    <template v-else-if="outfit === 'trench-coat'">
      <rect x="28" y="92" width="64" height="66" rx="4" :fill="outfitColor" />
      <rect x="16" y="92" width="18" height="54" rx="8" :fill="outfitColor" />
      <rect x="86" y="92" width="18" height="54" rx="8" :fill="outfitColor" />
      <rect x="26" y="116" width="68" height="6" rx="2" :fill="darken(outfitColor)" />
      <rect x="57" y="113" width="6" height="12" rx="1" fill="#c8a040" />
      <polygon points="54,92 46,112 54,116 60,106" :fill="darken(outfitColor)" />
      <polygon points="66,92 74,112 66,116 60,106" :fill="darken(outfitColor)" />
    </template>

    <!-- cyber-suit: fitted suit with neon accent lines -->
    <template v-else-if="outfit === 'cyber-suit'">
      <rect x="30" y="92" width="60" height="66" rx="6" :fill="outfitColor" />
      <rect x="18" y="92" width="16" height="52" rx="8" :fill="outfitColor" />
      <rect x="86" y="92" width="16" height="52" rx="8" :fill="outfitColor" />
      <rect x="57" y="94" width="6" height="58" rx="1" :fill="darken(outfitColor)" />
      <rect x="50" y="92" width="20" height="7" rx="2" :fill="darken(outfitColor)" />
      <rect x="30" y="112" width="60" height="2" rx="1" fill="#00E5FF" />
      <rect x="30" y="126" width="60" height="2" rx="1" fill="#00E5FF" />
      <rect x="18" y="112" width="12" height="2" rx="1" fill="#00E5FF" />
      <rect x="90" y="112" width="12" height="2" rx="1" fill="#00E5FF" />
    </template>

    <!-- robe: long wide-sleeved robe with gold belt -->
    <template v-else-if="outfit === 'robe'">
      <rect x="24" y="90" width="72" height="68" rx="4" :fill="outfitColor" />
      <rect x="10" y="90" width="22" height="58" rx="10" :fill="outfitColor" />
      <rect x="88" y="90" width="22" height="58" rx="10" :fill="outfitColor" />
      <rect x="24" y="114" width="72" height="5" rx="2" fill="#c8a040" />
      <rect x="58" y="90" width="4" height="24" rx="1" :fill="darken(outfitColor)" />
    </template>

    <!-- cape: undershirt with wide flowing cape panels and gold clasp -->
    <template v-else-if="outfit === 'cape'">
      <polygon points="14,93 36,93 26,158 8,150" :fill="darken(outfitColor)" />
      <polygon points="84,93 106,93 112,158 94,150" :fill="darken(outfitColor)" />
      <rect x="34" y="92" width="52" height="60" rx="4" :fill="outfitColor" />
      <rect x="22" y="92" width="14" height="44" rx="8" :fill="outfitColor" />
      <rect x="84" y="92" width="14" height="44" rx="8" :fill="outfitColor" />
      <circle cx="60" cy="95" r="4" fill="#c8a040" />
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
