<template>
  <main class="task-view">
    <header>
      <h1>Oppgaver</h1>
      <p v-if="stopId">Stopp {{ stopId }}</p>
      <p v-if="isMockMode" class="mock-badge">Mock mode aktiv (backend/store ikke klar)</p>
    </header>

    <p v-if="loading">Laster oppgaver...</p>
    <p v-else-if="error" class="error">{{ error }}</p>
    <p v-else-if="!currentTask">Ingen oppgaver funnet for dette stoppet.</p>

    <section v-else>
      <p class="progress">Oppgave {{ currentTaskIndex + 1 }} / {{ tasks.length }}</p>

      <FakeNewsTask
        v-if="currentTask.taskType === 'FAKE_NEWS' && !result"
        :task="currentTask"
        @submitted="handleSubmit"
      />

      <PhishingEmailTask
        v-else-if="currentTask.taskType === 'PHISHING_EMAIL' && !result"
        :task="currentTask"
        @submitted="handleSubmit"
      />

      <p v-else-if="!result" class="error">
        Ukjent taskType: {{ currentTask.taskType }}
      </p>

      <TaskResult
        v-if="result"
        :result="result"
        @next="goNext"
        @backToMap="goToMap"
      />
    </section>

    <ConfettiOverlay :active="showConfetti" />
    <MedalToast :medal="medalToast" />
  </main>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useGameStore } from '@/stores/game'
import { useClassroomStore } from '@/stores/classroom'
import FakeNewsTask from '@/components/student/FakeNewsTask.vue'
import PhishingEmailTask from '@/components/student/PhishingEmailTask.vue'
import TaskResult from '@/components/student/TaskResult.vue'
import ConfettiOverlay from '@/components/common/ConfettiOverlay.vue'
import MedalToast from '@/components/common/MedalToast.vue'

const route = useRoute()
const router = useRouter()
const gameStore = useGameStore()
const classroomStore = useClassroomStore()

const tasks = ref([])
const currentTaskIndex = ref(0)
const result = ref(null)
const loading = ref(false)
const error = ref('')
const isMockMode = ref(false)
const showConfetti = ref(false)
const medalToast = ref(null)
let confettiTimer = null
let medalTimer = null

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
    console.warn('[TaskView] No classroomId available (store/query/localStorage)')
    error.value = 'Mangler classroomId for å hente oppgaver.'
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
    explanation: task.mockExplanation ?? 'Sammenlign svaret ditt med trygg kildekritikk.',
    stopCompleted: currentTaskIndex.value === tasks.value.length - 1,
    medalEarned: currentTaskIndex.value === tasks.value.length - 1
      ? { id: 1, name: 'Nyhetsdetektiv', description: 'Du fullførte stoppet i mock-modus.' }
      : null
  }
}

function handleCelebration(submitResult) {
  if (submitResult?.stopCompleted) {
    showConfetti.value = true
    clearTimeout(confettiTimer)
    confettiTimer = setTimeout(() => { showConfetti.value = false }, 3000)
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
  if (currentTaskIndex.value < tasks.value.length - 1) {
    currentTaskIndex.value += 1
    result.value = null
    console.log('[TaskView] Advancing to task', currentTaskIndex.value + 1, 'of', tasks.value.length)
    return
  }
  console.log('[TaskView] All tasks done — navigating to map')
  goToMap()
}

function goToMap() {
  router.push('/map')
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

.progress {
  font-weight: var(--font-semibold);
}

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

