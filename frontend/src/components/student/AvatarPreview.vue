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
        <h3>Forhåndsvisning</h3>
        <p>{{ previewLabel }}</p>
      </div>

      <div class="avatar-tags">
        <span class="avatar-tag">{{ formatOption(selections.gender || 'neutral') }}</span>
        <span class="avatar-tag">{{ formatOption(selections.skinColor || 'medium') }}</span>
        <span class="avatar-tag">{{ formatOption(selections.hairStyle || 'short') }}</span>
        <span class="avatar-tag">{{ formatOption(selections.outfit || 'detective-coat') }}</span>
      </div>

      <p class="avatar-note">
        Første testversjon med tre importerte SVG-forhåndsinnstillinger. Mer detaljerte lagdelte deler kan legges til senere.
      </p>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import neutralAvatar from '@/assets/avatar/presets/adventurer-neutral.svg'
import lightAvatar from '@/assets/avatar/presets/adventurer-light.svg'
import warmAvatar from '@/assets/avatar/presets/adventurer-warm.svg'
import { formatOption } from '@/utils/avatarOptions'

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
  return `${formatOption(outfit)} med ${formatOption(hair)} og ${formatOption(accessory)}`
})

const previewAlt = computed(() =>
  `Avatarforhåndsvisning med ${props.selections.skinColor || 'medium'} hudtone`
)
</script>

<style scoped>
.avatar-preview {
  width: 100%;
}

.avatar-card {
  display: grid;
  gap: var(--space-4);
  justify-items: center;
  padding: var(--space-6);
  border-radius: var(--radius-xl);
  background:
    radial-gradient(circle at top, var(--color-surface-glass-strong), var(--color-surface-glass)),
    linear-gradient(145deg, var(--color-surface-glass-strong), var(--color-primary-soft));
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-lg);
}

.avatar-illustration-wrap {
  width: min(100%, 18rem);
  aspect-ratio: 1 / 1;
  display: grid;
  place-items: center;
  padding: var(--space-3);
  border-radius: var(--radius-xl);
  background:
    radial-gradient(circle at top, var(--color-surface-glass-strong), var(--color-surface-glass)),
    linear-gradient(180deg, var(--color-surface-glass), var(--color-primary-soft));
}

.avatar-illustration {
  width: 100%;
  height: 100%;
  object-fit: contain;
  filter: drop-shadow(0 20px 28px var(--color-primary-focus-ring));
}

.avatar-meta {
  text-align: center;
}

.avatar-meta h3 {
  margin: 0 0 0.35rem;
  font-size: var(--text-base);
  font-weight: 700;
  color: var(--color-text);
}

.avatar-meta p {
  margin: 0;
  color: var(--color-text-muted);
  font-size: var(--text-sm);
}

.avatar-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
  justify-content: center;
}

.avatar-tag {
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-full);
  background: var(--color-primary-soft);
  color: var(--color-primary);
  font-size: var(--text-sm);
  font-weight: 700;
}

.avatar-note {
  margin: 0;
  text-align: center;
  color: var(--color-text-muted);
  font-size: var(--text-sm);
  line-height: 1.45;
}
</style>
