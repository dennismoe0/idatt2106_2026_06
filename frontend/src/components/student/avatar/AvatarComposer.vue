<template>
  <div class="avatar-canvas" :style="canvasStyle">
    <AvatarBody     :skin-color="s.skinColor"   :gender="s.gender" />
    <AvatarOutfit   :outfit="s.outfit"           :outfit-color="s.outfitColor" />
    <AvatarHead     :skin-color="s.skinColor" />
    <AvatarHair     :hair-style="s.hairStyle"    :hair-color="s.hairColor" />
    <AvatarEyes     :eye-style="s.eyeStyle"      :eye-color="s.eyeColor" />
    <AvatarAccessory :accessory="s.accessory" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import AvatarBody      from './layers/AvatarBody.vue'
import AvatarHead      from './layers/AvatarHead.vue'
import AvatarHair      from './layers/AvatarHair.vue'
import AvatarEyes      from './layers/AvatarEyes.vue'
import AvatarOutfit    from './layers/AvatarOutfit.vue'
import AvatarAccessory from './layers/AvatarAccessory.vue'

const props = defineProps({
  selections: { type: Object, required: true },
  size: { type: Number, default: 160 },
})

const s = computed(() => props.selections)
const canvasStyle = computed(() => ({
  position: 'relative',
  width: props.size + 'px',
  height: Math.round(props.size * 160 / 120) + 'px',
}))
</script>
