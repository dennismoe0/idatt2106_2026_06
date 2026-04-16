<template>
  <section class="avatar-preview" aria-label="Avatar preview">
    <div class="avatar-card">
      <div class="avatar-figure">
        <div class="avatar-head" :style="{ backgroundColor: toneColor }">
          <div class="avatar-hair" :class="`avatar-hair--${selections.hairStyle || 'short'}`" :style="{ backgroundColor: hairColor }"></div>
          <div class="avatar-face">
            <span class="avatar-eye" :style="{ backgroundColor: eyeColor }"></span>
            <span class="avatar-eye" :style="{ backgroundColor: eyeColor }"></span>
          </div>
        </div>

        <div class="avatar-body" :style="{ backgroundColor: outfitColor }">
          <div class="avatar-badge" v-if="selections.accessory === 'badge'">★</div>
          <div class="avatar-accessory avatar-accessory--glasses" v-else-if="selections.accessory === 'glasses'"></div>
          <div class="avatar-accessory avatar-accessory--magnifier" v-else-if="selections.accessory === 'magnifier'">◔</div>
        </div>

        <div
          v-if="selections.hatColor && selections.hatColor !== 'none'"
          class="avatar-hat"
          :style="{ backgroundColor: hatColor }"
        ></div>
      </div>

      <div class="avatar-meta">
        <h3>Live Preview</h3>
        <p>{{ previewLabel }}</p>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  selections: {
    type: Object,
    required: true,
  },
})

const colorMap = {
  blue: '#4f7cff',
  red: '#f25f5c',
  green: '#4caf78',
  yellow: '#f3c34d',
  black: '#2a2a2a',
  brown: '#7a5230',
  blonde: '#d8b25c',
  gray: '#8d99ae',
  grey: '#8d99ae',
  white: '#f4f4f4',
  light: '#f2d1b0',
  medium: '#cf9b76',
  dark: '#7b4d2e',
  none: 'transparent',
}

const eyeColor = computed(() => colorMap[props.selections.eyeColor] || '#5c677d')
const toneColor = computed(() => colorMap[props.selections.skinColor] || '#cf9b76')
const hairColor = computed(() => colorMap[props.selections.hairColor] || '#7a5230')
const outfitColor = computed(() => colorMap[props.selections.outfitColor] || '#4f7cff')
const hatColor = computed(() => colorMap[props.selections.hatColor] || 'transparent')

const previewLabel = computed(() => {
  const outfit = props.selections.outfit || 'outfit'
  const hair = props.selections.hairStyle || 'hair'
  const accessory = props.selections.accessory || 'no accessory'
  return `${outfit} with ${hair} hair and ${accessory}`
})
</script>

<style scoped>
.avatar-preview {
  width: 100%;
}

.avatar-card {
  display: grid;
  gap: 1rem;
  justify-items: center;
  padding: 1.5rem;
  border-radius: 1.5rem;
  background:
    radial-gradient(circle at top, rgba(255, 255, 255, 0.95), rgba(230, 238, 255, 0.88)),
    linear-gradient(145deg, rgba(255, 255, 255, 0.94), rgba(214, 229, 255, 0.86));
  border: 1px solid rgba(79, 124, 255, 0.18);
  box-shadow: 0 18px 42px rgba(58, 86, 130, 0.12);
}

.avatar-figure {
  position: relative;
  width: 14rem;
  height: 15rem;
  display: grid;
  place-items: center;
}

.avatar-head {
  position: absolute;
  top: 1.4rem;
  width: 6.5rem;
  height: 6.9rem;
  border-radius: 48% 48% 46% 46%;
  border: 4px solid rgba(42, 42, 42, 0.08);
}

.avatar-hair {
  position: absolute;
  top: -0.4rem;
  left: 0.2rem;
  width: 5.8rem;
  height: 2.8rem;
  border-radius: 1.6rem 1.6rem 0.8rem 0.8rem;
}

.avatar-hair--curly {
  height: 3.2rem;
  border-radius: 1.8rem;
}

.avatar-hair--ponytail::after {
  content: '';
  position: absolute;
  right: -0.6rem;
  top: 1.2rem;
  width: 0.9rem;
  height: 2.2rem;
  border-radius: 999px;
  background: inherit;
}

.avatar-hair--buzz {
  height: 1.2rem;
}

.avatar-face {
  position: absolute;
  top: 2.8rem;
  left: 1.2rem;
  display: flex;
  gap: 1.5rem;
}

.avatar-eye {
  width: 0.7rem;
  height: 0.7rem;
  border-radius: 999px;
  box-shadow: 0 0 0 3px rgba(255, 255, 255, 0.7);
}

.avatar-body {
  position: absolute;
  bottom: 1.2rem;
  width: 8.8rem;
  height: 7.2rem;
  border-radius: 1.6rem 1.6rem 1rem 1rem;
  border: 4px solid rgba(42, 42, 42, 0.08);
}

.avatar-hat {
  position: absolute;
  top: 0.8rem;
  width: 7rem;
  height: 1.8rem;
  border-radius: 999px 999px 0.8rem 0.8rem;
  border: 3px solid rgba(42, 42, 42, 0.08);
}

.avatar-badge,
.avatar-accessory {
  position: absolute;
}

.avatar-badge {
  right: 1rem;
  top: 1rem;
  font-size: 1.2rem;
  color: #fff7b3;
}

.avatar-accessory--glasses {
  top: -4.8rem;
  left: 1.35rem;
  width: 6rem;
  height: 1rem;
  border-top: 0.25rem solid rgba(42, 42, 42, 0.7);
}

.avatar-accessory--glasses::before,
.avatar-accessory--glasses::after {
  content: '';
  position: absolute;
  top: -0.15rem;
  width: 1.7rem;
  height: 1.2rem;
  border: 0.2rem solid rgba(42, 42, 42, 0.7);
  border-radius: 999px;
}

.avatar-accessory--glasses::before {
  left: 0.4rem;
}

.avatar-accessory--glasses::after {
  right: 0.4rem;
}

.avatar-accessory--magnifier {
  right: 0.8rem;
  bottom: 0.6rem;
  font-size: 1.4rem;
  color: rgba(255, 255, 255, 0.95);
}

.avatar-meta {
  text-align: center;
}

.avatar-meta h3 {
  margin: 0 0 0.35rem;
  font-size: 1.05rem;
  font-weight: 700;
  color: #20304a;
}

.avatar-meta p {
  margin: 0;
  color: #56657e;
  font-size: 0.95rem;
}
</style>
