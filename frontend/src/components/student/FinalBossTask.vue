<template>
  <section class="boss">
    <!-- Intro -->
    <div v-if="phase === 'intro'" class="boss__intro">
      <p class="boss__siren" aria-hidden="true">🚨</p>
      <h2 class="boss__title">Backup-planen har startet!</h2>
      <p class="boss__body">{{ task.contentJson?.intro }}</p>
      <div class="boss__systems-preview">
        <div v-for="c in challenges" :key="c.id" class="boss__system-chip">
          <span>{{ SYSTEM_ICONS[c.type] }}</span>
          <span>{{ c.systemName }}</span>
        </div>
      </div>
      <button class="boss__btn boss__btn--start" @click="phase = 'challenge'">
        Start etterforskning →
      </button>
    </div>

    <!-- Active challenge -->
    <template v-else-if="phase === 'challenge'">
      <div class="boss__progress" :aria-label="`System ${currentIdx + 1} av ${challenges.length}`">
        <div
          v-for="(c, i) in challenges"
          :key="c.id"
          class="boss__progress-step"
          :class="{
            'boss__progress-step--done':    answers[i] !== undefined,
            'boss__progress-step--current': i === currentIdx && answers[i] === undefined
          }"
        >
          <span class="boss__progress-icon">{{ SYSTEM_ICONS[c.type] }}</span>
          <span class="boss__progress-label">{{ c.systemName }}</span>
        </div>
      </div>

      <div class="boss__system-header">
        <p class="boss__system-number">Sikkerhetssystem {{ currentIdx + 1 }}/{{ challenges.length }}</p>
        <h3 class="boss__system-name">{{ currentChallenge.systemName }}</h3>
        <p class="boss__system-desc">{{ currentChallenge.description }}</p>
      </div>

      <BossFakeNews
        v-if="currentChallenge.type === 'FAKE_NEWS'"
        :challenge="currentChallenge"
        :submitted="answers[currentIdx] !== undefined"
        @answer="recordAnswer"
      />
      <BossAiPhoto
        v-else-if="currentChallenge.type === 'AI_PHOTO'"
        :challenge="currentChallenge"
        :submitted="answers[currentIdx] !== undefined"
        @answer="recordAnswer"
      />
      <BossPhishing
        v-else-if="currentChallenge.type === 'PHISHING_EMAIL'"
        :challenge="currentChallenge"
        :submitted="answers[currentIdx] !== undefined"
        @answer="recordAnswer"
      />
      <BossChoice
        v-else
        :challenge="currentChallenge"
        :submitted="answers[currentIdx] !== undefined"
        @answer="recordAnswer"
      />

      <Transition name="result-slide">
        <div v-if="answers[currentIdx] !== undefined" class="boss__system-stopped">
          <p class="boss__stopped-label">✅ System stoppet!</p>
          <button
            v-if="currentIdx < challenges.length - 1"
            class="boss__btn"
            @click="currentIdx++"
          >Neste system →</button>
          <button
            v-else
            class="boss__btn boss__btn--finish"
            @click="submitAll"
          >Send alle svar →</button>
        </div>
      </Transition>
    </template>

    <!-- Result -->
    <div v-else-if="phase === 'result'" class="boss__result">
      <template v-if="result?.correct">
        <p class="boss__result-emoji">🎉</p>
        <h2 class="boss__result-title">Du stoppet backup-planen!</h2>
        <p class="boss__result-body">Pengene til idrettsparken er reddet.<br>Internettbyen er trygg igjen.</p>
        <p class="boss__result-title2">Du er en Mesterdetektiv!</p>
      </template>
      <template v-else>
        <p class="boss__result-emoji">⚡</p>
        <h2 class="boss__result-title">Ikke helt riktig</h2>
        <p class="boss__result-body">{{ result?.explanation }}</p>
      </template>
      <button class="boss__btn" @click="$emit('next')">Se oppsummering →</button>
    </div>
  </section>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import BossFakeNews from '@/components/student/boss/BossFakeNews.vue'
import BossAiPhoto  from '@/components/student/boss/BossAiPhoto.vue'
import BossPhishing from '@/components/student/boss/BossPhishing.vue'
import BossChoice   from '@/components/student/boss/BossChoice.vue'

const SYSTEM_ICONS = {
  FAKE_NEWS:      '📰',
  AI_PHOTO:       '📷',
  PHISHING_EMAIL: '📧',
  MARKETPLACE:    '🛒',
  SOCIAL_MEDIA:   '💬',
  PASSWORD:       '🔐',
}

const props = defineProps({
  task:   { type: Object, required: true },
  result: { type: Object, default: null },
})
const emit = defineEmits(['submitted', 'next'])

const phase      = ref('intro')
const currentIdx = ref(0)
const answers    = ref({})

const challenges       = computed(() => props.task.contentJson?.challenges ?? [])
const currentChallenge = computed(() => challenges.value[currentIdx.value])

function recordAnswer(answer) {
  answers.value = { ...answers.value, [currentIdx.value]: answer }
  console.log('[FinalBossTask] challenge', currentIdx.value, 'answered:', answer)
}

function submitAll() {
  const payload = {}
  for (let i = 0; i < challenges.value.length; i++) {
    payload[`challenge_${i}`] = answers.value[i]
  }
  console.log('[FinalBossTask] submitting all:', payload)
  emit('submitted', payload)
}

watch(() => props.result, (r) => {
  if (r !== null) phase.value = 'result'
})
</script>

<style scoped>
.boss { display: grid; gap: var(--space-4); }

.boss__intro { text-align: center; display: grid; gap: var(--space-4); }
.boss__siren { font-size: 4rem; margin: 0; }
.boss__title { font-size: var(--text-2xl); font-weight: var(--font-bold); margin: 0; color: var(--color-danger); }
.boss__body  { color: var(--color-text-muted); margin: 0; }
.boss__systems-preview { display: flex; flex-wrap: wrap; gap: var(--space-2); justify-content: center; }
.boss__system-chip {
  display: flex; align-items: center; gap: 6px;
  background: var(--color-surface); border: 1px solid var(--color-border);
  border-radius: var(--radius-full); padding: var(--space-1) var(--space-3); font-size: var(--text-sm);
}

.boss__progress { display: flex; gap: var(--space-2); overflow-x: auto; padding-bottom: var(--space-2); }
.boss__progress-step {
  flex: 1; min-width: 70px;
  display: flex; flex-direction: column; align-items: center; gap: 4px;
  padding: var(--space-2); border-bottom: 3px solid var(--color-border);
  opacity: 0.5; transition: all var(--transition-fast); font-size: var(--text-xs); text-align: center;
}
.boss__progress-step--done    { border-color: var(--color-success); opacity: 1; color: var(--color-success); }
.boss__progress-step--current { border-color: var(--color-primary); opacity: 1; }
.boss__progress-icon { font-size: 1.4rem; }

.boss__system-header { display: grid; gap: var(--space-1); }
.boss__system-number { margin: 0; font-size: var(--text-xs); font-weight: var(--font-bold); letter-spacing: 0.1em; text-transform: uppercase; color: var(--color-danger); }
.boss__system-name   { margin: 0; font-size: var(--text-xl); font-weight: var(--font-bold); }
.boss__system-desc   { margin: 0; color: var(--color-text-muted); }

.boss__system-stopped {
  background: var(--color-success-light); border: 2px solid var(--color-success);
  border-radius: var(--radius-lg); padding: var(--space-4);
  display: flex; align-items: center; justify-content: space-between; gap: var(--space-4);
}
.boss__stopped-label { margin: 0; font-weight: var(--font-bold); color: var(--color-success); font-size: var(--text-lg); }

.boss__btn {
  background: var(--color-primary); color: var(--color-text-on-dark);
  border: none; border-radius: var(--radius-md);
  padding: var(--space-3) var(--space-6); font-size: var(--text-base); font-weight: var(--font-bold);
  cursor: pointer; min-height: 44px; transition: background var(--transition-fast);
}
.boss__btn:hover { background: var(--color-primary-dark); }
.boss__btn--start  { justify-self: center; }
.boss__btn--finish { background: var(--color-success); }
.boss__btn--finish:hover { background: var(--color-success-light); filter: brightness(0.85); }

.boss__result { text-align: center; display: grid; gap: var(--space-4); }
.boss__result-emoji  { font-size: 5rem; margin: 0; }
.boss__result-title  { font-size: var(--text-2xl); font-weight: var(--font-bold); margin: 0; }
.boss__result-title2 { font-size: var(--text-xl); font-weight: var(--font-bold); color: var(--color-medal-bronze-border); margin: 0; }
.boss__result-body   { color: var(--color-text-muted); margin: 0; line-height: 1.6; }

.result-slide-enter-active { transition: transform 0.3s ease, opacity 0.3s ease; }
.result-slide-enter-from   { transform: translateY(-10px); opacity: 0; }
</style>
