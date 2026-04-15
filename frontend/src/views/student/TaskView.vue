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

    <div v-if="showConfetti" class="confetti-overlay">🎉</div>
    <aside v-if="medalToast" class="medal-toast">
      <strong>Medalje låst opp:</strong> {{ medalToast.name }}
      <p>{{ medalToast.description }}</p>
    </aside>
  </main>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useGameStore } from '@/stores/game'
import { useClassroomStore } from '@/stores/classroom'
import FakeNewsTask from '@/components/student/FakeNewsTask.vue'
import PhishingEmailTask from '@/components/student/PhishingEmailTask.vue'
import TaskResult from '@/components/student/TaskResult.vue'

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
    error.value = 'Mangler stopId i URL.'
    return
  }

  if (!classroomId.value) {
    error.value = 'Mangler classroomId for å hente oppgaver.'
    return
  }

  loading.value = true
  error.value = ''

  try {
    tasks.value = await gameStore.fetchTasks(stopId.value, classroomId.value)
    isMockMode.value = false
  } catch (apiError) {
    console.warn('[TaskView] Failed to fetch tasks, switching to mock mode.', apiError)
    tasks.value = MOCK_TASKS.filter((task) => task.stopId === stopId.value)
    isMockMode.value = true
  } finally {
    loading.value = false
  }
}

async function handleSubmit(answer) {
  if (!currentTask.value) return

  try {
    result.value = await gameStore.submitAnswer(currentTask.value.id, answer, classroomId.value)
    handleCelebration(result.value)
    isMockMode.value = false
  } catch (apiError) {
    console.warn('[TaskView] Failed to submit answer, using local mock evaluator.', apiError)
    result.value = buildMockResult(currentTask.value, answer)
    handleCelebration(result.value)
    isMockMode.value = true
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
    window.setTimeout(() => {
      showConfetti.value = false
    }, 3000)
  }

  if (submitResult?.medalEarned) {
    medalToast.value = submitResult.medalEarned
    window.setTimeout(() => {
      medalToast.value = null
    }, 4000)
  }
}

function goNext() {
  if (currentTaskIndex.value < tasks.value.length - 1) {
    currentTaskIndex.value += 1
    result.value = null
    return
  }
  goToMap()
}

function goToMap() {
  if (router.hasRoute('Map')) {
    router.push('/map')
    return
  }
  router.push('/')
}
</script>

<style scoped>
.task-view {
  max-width: 960px;
  margin: 0 auto;
  padding: 1rem;
  display: grid;
  gap: 1rem;
}

.progress {
  font-weight: 600;
}

.mock-badge {
  color: #92400e;
  background: #fef3c7;
  border: 1px solid #fcd34d;
  border-radius: 8px;
  padding: 0.5rem 0.75rem;
  width: fit-content;
}

.error {
  color: #b91c1c;
}

.confetti-overlay {
  position: fixed;
  inset: 0;
  display: grid;
  place-items: center;
  font-size: 4rem;
  pointer-events: none;
  animation: pop 0.35s ease-out;
}

.medal-toast {
  position: fixed;
  right: 1rem;
  bottom: 1rem;
  max-width: 320px;
  border: 1px solid #facc15;
  background: #fef9c3;
  border-radius: 10px;
  padding: 0.75rem;
}

.medal-toast p {
  margin: 0.5rem 0 0;
}

@keyframes pop {
  from {
    opacity: 0;
    transform: scale(0.85);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}
</style>
