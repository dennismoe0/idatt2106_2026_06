<template>
  <main class="task-view">
    <StudentHeader title="Oppgaver" :back-to="{ name: preferredMap }" />

    <StopSummary
      v-if="showSummary"
      :tasks="tasks"
      :task-results="taskResults"
      @retry="handleRetry"
      @back-to-map="goToMap"
    />

    <template v-else>
    <p v-if="isMockMode" class="mock-badge">Mock mode aktiv (backend/store ikke klar)</p>

    <p v-if="loading">Laster oppgaver...</p>
    <p v-else-if="error" class="error">{{ error }}</p>
    <p v-else-if="!currentTask">Ingen oppgaver funnet for dette stoppet.</p>

    <section v-else>
      <!-- Progress dots -->
      <div class="task-dots" role="list" :aria-label="`Oppgave ${currentTaskIndex + 1} av ${tasks.length}`">
        <span
          v-for="(t, i) in tasks"
          :key="t.id"
          class="dot"
          role="listitem"
          :class="{
            'dot--current': i === currentTaskIndex && !taskResults[t.id],
            'dot--correct': taskResults[t.id]?.correct === true,
            'dot--wrong':   taskResults[t.id] && !taskResults[t.id].correct
          }"
          :aria-label="`Oppgave ${i + 1}${taskResults[t.id] ? (taskResults[t.id].correct ? ': riktig' : ': feil') : ''}`"
        />
      </div>

      <FakeNewsTask
        v-if="currentTask.taskType === 'FAKE_NEWS'"
        :task="currentTask"
        :result="result"
        :is-last-task="currentTaskIndex === tasks.length - 1"
        @submitted="handleSubmit"
        @next="goNext"
        @back-to-map="goToMap"
      />

      <PhishingEmailTask
        v-else-if="currentTask.taskType === 'PHISHING_EMAIL'"
        :task="currentTask"
        :result="result"
        :is-last-task="currentTaskIndex === tasks.length - 1"
        @submitted="handleSubmit"
        @next="goNext"
        @back-to-map="goToMap"
      />

      <FinalBossTask
        v-else-if="currentTask.taskType === 'FINAL_BOSS'"
        :task="currentTask"
        :result="result"
        :is-last-task="currentTaskIndex === tasks.length - 1"
        @submitted="handleSubmit"
        @next="goNext"
        @try-again="result = null"
        @back-to-map="goToMap"
      />

      <p v-else class="error">
        Ukjent taskType: {{ currentTask.taskType }}
      </p>
    </section>
    </template>

    <ConfettiOverlay :active="confettiMode" />
    <MedalToast :medal="medalToast" />
  </main>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useGameStore } from '@/stores/game'
import { useClassroomStore } from '@/stores/classroom'
import StudentHeader from '@/components/common/StudentHeader.vue'
import FakeNewsTask from '@/components/student/FakeNewsTask.vue'
import PhishingEmailTask from '@/components/student/PhishingEmailTask.vue'
import FinalBossTask from '@/components/student/FinalBossTask.vue'
import ConfettiOverlay from '@/components/common/ConfettiOverlay.vue'
import MedalToast from '@/components/common/MedalToast.vue'
import StopSummary from '@/components/student/StopSummary.vue'
import { useSound } from '@/composables/useSound'

const { playCorrect, playWrong, playFanfare } = useSound()

const route = useRoute()
const router = useRouter()
const gameStore = useGameStore()
const classroomStore = useClassroomStore()

const tasks = ref([])
const currentTaskIndex = ref(0)
const result = ref(null)
const taskResults = ref({}) // keyed by task.id → SubmitAnswerResponse
const loading = ref(false)
const error = ref('')
const isMockMode = ref(false)
// 'correct' = per-answer burst, 'stop' = big stop-completion blast, false = off
const confettiMode = ref(false)
const medalToast = ref(null)
const showSummary = ref(false)
let confettiTimer = null
let medalTimer = null

const preferredMap = localStorage.getItem('mapView') === 'simple' ? 'Map' : 'WorldMap'

const stopId = computed(() => Number(route.query.stopId ?? 0) || null)
const classroomId = computed(() => {
  const fromStore = Number(classroomStore.currentClassroomId ?? 0)
  const fromQuery = Number(route.query.classroomId ?? 0)
  const fromLocalStorage = Number(localStorage.getItem('classroomId') ?? 0)
  return fromStore || fromQuery || fromLocalStorage || null
})
const currentTask = computed(() => tasks.value[currentTaskIndex.value] ?? null)

const MOCK_TASKS = [
  {
    id: 1001,
    stopId: 1,
    taskType: 'FAKE_NEWS',
    guidanceText: 'Marker hver artikkel som ekte eller falsk.',
    contentJson: {
      articles: [
        {
          headline: 'Trondheim kommune deler ut gratis nettbrett til alle elever',
          body: 'Kommunen tester ny digital satsing i fire bydeler.',
          source: 'Adresseavisen'
        },
        {
          headline: 'Forskere fant usynlig energi i skolemelk',
          body: 'Artikkelen påstår at melk gir superkrefter etter klokken 19.',
          source: 'nyheter24-ekte.no'
        }
      ]
    },
    mockCorrectAnswer: { article_0: true, article_1: false },
    mockExplanation: 'Den andre artikkelen bruker en userios kilde og usannsynlige påstander.'
  },
  {
    id: 1002,
    stopId: 1,
    taskType: 'PHISHING_EMAIL',
    guidanceText: 'Velg tryggeste handling når du får mistenkelig e-post.',
    contentJson: {
      email: {
        fromName: 'DNB Kundeservice',
        fromEmail: 'support@dnb-kundeservice.com',
        subject: 'Viktig: Bekreft kontoen din',
        body: 'Klikk her innen 24 timer for å unngå sperring av kontoen.'
      }
    },
    mockCorrectAnswer: { action: 'REPORT' },
    mockExplanation: 'Avsenderdomenet er ikke dnb.no, derfor bør e-posten rapporteres.'
  }
]

onMounted(loadTasks)

async function loadTasks() {
  if (!stopId.value) {
    console.warn('[TaskView] No stopId found in URL query')
    error.value = 'Mangler stopId i URL.'
    return
  }

  if (!classroomId.value) {
    console.warn('[TaskView] No classroomId — redirecting to join')
    router.push({ name: 'JoinClassroom' })
    return
  }

  console.log('[TaskView] Loading tasks — stopId:', stopId.value, 'classroomId:', classroomId.value)
  loading.value = true
  error.value = ''

  try {
    tasks.value = (await gameStore.fetchTasks(stopId.value, classroomId.value))
      .map(t => ({
        ...t,
        contentJson: typeof t.contentJson === 'string' ? JSON.parse(t.contentJson) : t.contentJson
      }))
    console.log('[TaskView] Loaded', tasks.value.length, 'tasks from API')
    isMockMode.value = false
  } catch (apiError) {
    console.error('[TaskView] Failed to fetch tasks.', apiError)
    if (import.meta.env.DEV) {
      tasks.value = MOCK_TASKS.filter((task) => task.stopId === stopId.value)
      console.log('[TaskView] Mock mode — loaded', tasks.value.length, 'mock tasks')
      isMockMode.value = true
    } else {
      error.value = 'Oppgaver utilgjengelige. Prøv igjen.'
    }
  } finally {
    loading.value = false
  }
}

async function handleSubmit(answer) {
  if (!currentTask.value) return

  console.log('[TaskView] Submitting answer for task:', currentTask.value.id, 'type:', currentTask.value.taskType)

  try {
    result.value = await gameStore.submitAnswer(currentTask.value.id, answer, classroomId.value)
    console.log('[TaskView] Submit result — correct:', result.value.correct, 'stopCompleted:', result.value.stopCompleted)
    handleCelebration(result.value)
    isMockMode.value = false
  } catch (apiError) {
    console.error('[TaskView] Failed to submit answer.', apiError)
    if (import.meta.env.DEV) {
      result.value = buildMockResult(currentTask.value, answer)
      handleCelebration(result.value)
      isMockMode.value = true
    } else {
      error.value = 'Kunne ikke sende svar. Prøv igjen.'
    }
  }
}

function buildMockResult(task, answer) {
  const expected = task.mockCorrectAnswer ?? {}
  const keys = Object.keys(expected)
  const correct = keys.every((key) => answer[key] === expected[key])

  return {
    correct,
    score: correct ? 100 : 40,
    starsEarned: correct ? 1 : 0,
    xpEarned: correct ? 20 : 0,
    explanation: task.mockExplanation ?? 'Sammenlign svaret ditt med trygg kildekritikk.',
    stopCompleted: currentTaskIndex.value === tasks.value.length - 1,
    medalEarned: currentTaskIndex.value === tasks.value.length - 1
      ? { id: 1, name: 'Nyhetsdetektiv', description: 'Du fullførte stoppet i mock-modus.' }
      : null
  }
}

function handleCelebration(submitResult) {
  clearTimeout(confettiTimer)

  if (submitResult?.correct) {
    if (submitResult.stopCompleted) {
      confettiMode.value = 'stop'
      playFanfare()
    } else {
      confettiMode.value = 'correct'
      playCorrect()
    }
    confettiTimer = setTimeout(() => { confettiMode.value = false }, 3200)
  } else {
    confettiMode.value = false
    playWrong()
  }

  if (submitResult?.medalEarned) {
    medalToast.value = submitResult.medalEarned
    clearTimeout(medalTimer)
    medalTimer = setTimeout(() => { medalToast.value = null }, 4000)
  }
}

onBeforeUnmount(() => {
  clearTimeout(confettiTimer)
  clearTimeout(medalTimer)
})

function goNext() {
  if (result.value && currentTask.value) {
    taskResults.value[currentTask.value.id] = result.value
  }
  if (currentTaskIndex.value < tasks.value.length - 1) {
    currentTaskIndex.value += 1
    result.value = null
    console.log('[TaskView] Advancing to task', currentTaskIndex.value + 1, 'of', tasks.value.length)
    return
  }
  console.log('[TaskView] All tasks done — showing summary')
  showSummary.value = true
}

function handleRetry() {
  showSummary.value = false
  currentTaskIndex.value = 0
  result.value = null
  taskResults.value = {}
  console.log('[TaskView] Retrying stop')
}

function goToMap() {
  const pref = localStorage.getItem('mapView') === 'simple' ? 'Map' : 'WorldMap'
  console.log('[TaskView] Returning to map — preference:', pref)
  router.push({ name: pref })
}
</script>

<style scoped>
.task-view {
  max-width: 960px;
  margin: 0 auto;
  padding: var(--space-4);
  display: grid;
  gap: var(--space-4);
}

.task-dots {
  display: flex;
  gap: var(--space-2);
  justify-content: center;
  padding: var(--space-2) 0;
}

.dot {
  width: 12px;
  height: 12px;
  border-radius: var(--radius-full);
  background: var(--color-border);
  border: 2px solid transparent;
  transition: background var(--transition-fast), transform var(--transition-fast);
}
.dot--current {
  border-color: var(--color-primary);
  background: var(--color-primary-light);
  transform: scale(1.2);
}
.dot--correct { background: var(--color-success); }
.dot--wrong   { background: var(--color-danger); }

.mock-badge {
  color: var(--color-warning);
  background: var(--color-warning-light);
  border: 1px solid var(--color-accent);
  border-radius: var(--radius-md);
  padding: var(--space-2) var(--space-3);
  width: fit-content;
}

.error {
  color: var(--color-danger);
}
</style>
