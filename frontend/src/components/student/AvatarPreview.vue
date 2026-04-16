<template>
  <section class="avatar-preview" aria-label="Avatar preview">
    <div class="avatar-card">
      <div class="avatar-illustration-wrap">
        <img
          :src="avatarImage"
          class="avatar-illustration"
          :alt="previewAlt"
        />
      </div>

      <div class="avatar-meta">
        <h3>Live Preview</h3>
        <p>{{ previewLabel }}</p>
      </div>

      <div class="avatar-tags">
        <span class="avatar-tag">{{ formatOption(selections.gender || 'neutral') }}</span>
        <span class="avatar-tag">{{ formatOption(selections.skinColor || 'medium') }}</span>
        <span class="avatar-tag">{{ formatOption(selections.hairStyle || 'short') }}</span>
        <span class="avatar-tag">{{ formatOption(selections.outfit || 'detective-coat') }}</span>
      </div>

      <p class="avatar-note">
        Initial test version using three imported SVG presets. More detailed layered parts can be added later.
      </p>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import neutralAvatar from '@/assets/avatar/presets/adventurer-neutral.svg'
import lightAvatar from '@/assets/avatar/presets/adventurer-light.svg'
import warmAvatar from '@/assets/avatar/presets/adventurer-warm.svg'

const props = defineProps({
  selections: {
    type: Object,
    required: true,
  },
})

const avatarImage = computed(() => {
  switch (props.selections.skinColor) {
    case 'light':
      return lightAvatar
    case 'dark':
      return warmAvatar
    case 'medium':
    default:
      return neutralAvatar
  }
})

const previewLabel = computed(() => {
  const outfit = props.selections.outfit || 'detective-coat'
  const hair = props.selections.hairStyle || 'short'
  const accessory = props.selections.accessory || 'badge'
  return `${formatOption(outfit)} with ${formatOption(hair)} hair and ${formatOption(accessory)}`
})

const previewAlt = computed(() =>
  `Avatar preview with ${props.selections.skinColor || 'medium'} skin tone`
)

function formatOption(value) {
  return value
    .split('-')
    .map(part => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ')
}
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

.avatar-illustration-wrap {
  width: min(100%, 18rem);
  aspect-ratio: 1 / 1;
  display: grid;
  place-items: center;
  padding: 0.75rem;
  border-radius: 1.4rem;
  background:
    radial-gradient(circle at top, rgba(255, 255, 255, 0.9), rgba(236, 241, 255, 0.75)),
    linear-gradient(180deg, rgba(255, 255, 255, 0.7), rgba(232, 239, 255, 0.95));
}

.avatar-illustration {
  width: 100%;
  height: 100%;
  object-fit: contain;
  filter: drop-shadow(0 20px 28px rgba(38, 58, 93, 0.15));
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

.avatar-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  justify-content: center;
}

.avatar-tag {
  padding: 0.45rem 0.7rem;
  border-radius: 999px;
  background: rgba(79, 124, 255, 0.12);
  color: #2b4675;
  font-size: 0.82rem;
  font-weight: 700;
}

.avatar-note {
  margin: 0;
  text-align: center;
  color: #6b7b96;
  font-size: 0.85rem;
  line-height: 1.45;
}
</style>
