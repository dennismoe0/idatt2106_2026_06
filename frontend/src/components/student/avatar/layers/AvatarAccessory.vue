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

    <!-- deerstalker: Sherlock Holmes detective hat with front/back peaks and ear flaps -->
    <template v-else-if="accessory === 'deerstalker'">
      <rect x="26" y="24" width="14" height="18" rx="6" fill="#3d3d3d" />
      <rect x="80" y="24" width="14" height="18" rx="6" fill="#3d3d3d" />
      <rect x="36" y="8" width="48" height="24" rx="10" fill="#3d3d3d" />
      <polygon points="36,28 52,28 48,36 36,34" fill="#3d3d3d" />
      <polygon points="84,28 68,28 72,36 84,34" fill="#3d3d3d" />
      <rect x="36" y="27" width="48" height="4" fill="#5a3a1a" />
    </template>

    <!-- fedora: classic wide-brimmed noir detective hat -->
    <template v-else-if="accessory === 'fedora'">
      <rect x="40" y="8" width="40" height="24" rx="4" fill="#1a1a1a" />
      <rect x="50" y="8" width="20" height="5" rx="2" fill="#111111" />
      <rect x="40" y="27" width="40" height="4" rx="1" fill="#8B4513" />
      <rect x="24" y="28" width="72" height="8" rx="3" fill="#1a1a1a" />
    </template>

    <!-- lightsaber-blue: energy blade held in left hand -->
    <template v-else-if="accessory === 'lightsaber-blue'">
      <rect x="14" y="116" width="18" height="4" rx="1" fill="#666666" />
      <rect x="18" y="116" width="10" height="20" rx="2" fill="#888888" />
      <rect x="19" y="133" width="8" height="4" rx="2" fill="#555555" />
      <rect x="21" y="44" width="6" height="74" rx="3" fill="#4488FF" />
      <rect x="22.5" y="46" width="3" height="70" rx="1.5" fill="#aaccFF" opacity="0.7" />
    </template>

    <!-- lightsaber-red: energy blade held in left hand -->
    <template v-else-if="accessory === 'lightsaber-red'">
      <rect x="14" y="116" width="18" height="4" rx="1" fill="#666666" />
      <rect x="18" y="116" width="10" height="20" rx="2" fill="#888888" />
      <rect x="19" y="133" width="8" height="4" rx="2" fill="#555555" />
      <rect x="21" y="44" width="6" height="74" rx="3" fill="#FF3333" />
      <rect x="22.5" y="46" width="3" height="70" rx="1.5" fill="#FFaaaa" opacity="0.7" />
    </template>

    <!-- lightsaber-green: energy blade held in left hand -->
    <template v-else-if="accessory === 'lightsaber-green'">
      <rect x="14" y="116" width="18" height="4" rx="1" fill="#666666" />
      <rect x="18" y="116" width="10" height="20" rx="2" fill="#888888" />
      <rect x="19" y="133" width="8" height="4" rx="2" fill="#555555" />
      <rect x="21" y="44" width="6" height="74" rx="3" fill="#44CC44" />
      <rect x="22.5" y="46" width="3" height="70" rx="1.5" fill="#aaffaa" opacity="0.7" />
    </template>
  </svg>
</template>
<script setup>
defineProps({ accessory: { type: String, default: 'none' } })
</script>
