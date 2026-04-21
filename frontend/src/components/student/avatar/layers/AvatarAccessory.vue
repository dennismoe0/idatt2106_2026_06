<template>
  <svg v-if="accessory !== 'none'" viewBox="0 0 120 160" xmlns="http://www.w3.org/2000/svg"
       style="position:absolute;inset:0;width:100%;height:100%">
    <!-- badge on chest -->
    <template v-if="accessory === 'badge'">
      <rect x="52" y="98" width="16" height="12" rx="3" fill="#FFD700" />
      <rect x="55" y="101" width="10" height="6" rx="1" fill="#B8860B" />
    </template>
    <!-- glasses over eyes -->
    <template v-else-if="accessory === 'glasses'">
      <rect x="38" y="50" width="18" height="12" rx="5" fill="none" stroke="#1f2937" stroke-width="2"/>
      <rect x="64" y="50" width="18" height="12" rx="5" fill="none" stroke="#1f2937" stroke-width="2"/>
      <line x1="56" y1="56" x2="64" y2="56" stroke="#1f2937" stroke-width="2"/>
      <line x1="20" y1="56" x2="38" y2="56" stroke="#1f2937" stroke-width="1.5"/>
      <line x1="82" y1="56" x2="100" y2="56" stroke="#1f2937" stroke-width="1.5"/>
    </template>
    <!-- magnifier in hand -->
    <template v-else-if="accessory === 'magnifier'">
      <circle cx="26" cy="118" r="10" fill="none" stroke="#1f2937" stroke-width="2.5"/>
      <line x1="33" y1="125" x2="42" y2="134" stroke="#1f2937" stroke-width="3" stroke-linecap="round"/>
    </template>
    <!-- hat on head -->
    <template v-else-if="accessory === 'hat'">
      <rect x="34" y="26" width="52" height="10" rx="3" fill="#1f2937" />
      <rect x="42" y="10" width="36" height="18" rx="4" fill="#1f2937" />
    </template>
  </svg>
</template>
<script setup>
defineProps({ accessory: { type: String, default: 'none' } })
</script>
