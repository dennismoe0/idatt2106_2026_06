<template>
  <div class="task-view">
    <DetectiveBar :back-to="{ name: preferredMap }" :page-title="stopName" />

    <RouterLink
      class="task-view__dossier-link"
      :to="{ name: 'SuspectDossier' }"
      aria-label="Åpne mistenktmappe"
    >
      🗂 Mistenktmappe
    </RouterLink>

    <!-- Mystery scenario screen -->
    <StopMysteryScreen
      v-if="showMystery && mysteryScenario"
      :badge="mysteryScenario.badge"
      :title="mysteryScenario.title"
      :scenario="mysteryScenario.scenario"
      :image-src="mysteryScenario.imageSrc"
      :image-alt="mysteryScenario.imageAlt"
      @back="goToMap"
      @accept="acceptMystery"
    />

    <!-- Tutorial screen gate -->
    <div v-else-if="showTutorial" class="task-view__tutorial-wrap cork-board-bg">
      <TutorialScreen
        :title="tutorialTitle"
        :instructions="tutorialInstructions"
        @start="startTasks"
      />
    </div>

    <!-- Task area -->
    <div
      v-else
      class="task-view__main"
      :class="currentTask?.taskType === 'LEARN' ? 'task-view__main--clean' : 'cork-board-bg'"
      @mouseover="handlePeekHover"
      @mouseout="handlePeekOut"
    >

      <StopSummary
        v-if="showSummary"
        :tasks="tasks"
        :task-results="taskResults"
        @retry="handleRetry"
        @back-to-map="goToMap"
      />

      <template v-else>
        <p v-if="isMockMode" class="task-view__mock-badge">Mock mode aktiv (backend/store ikke klar)</p>

        <p v-if="loading" class="task-view__state">Laster oppgaver...</p>
        <p v-else-if="error" class="task-view__state task-view__state--error">{{ error }}</p>
        <p v-else-if="!currentTask" class="task-view__state">Ingen oppgaver funnet for dette stoppet.</p>

        <section v-else class="task-view__section">
          <!-- Progress dots + replay button -->
          <div class="task-view__meta">
            <div class="task-dots" role="list">
              <template v-for="(t, i) in tasks" :key="t.id">
                <span
                  v-if="t.taskType !== 'LEARN'"
                  class="dot"
                  role="listitem"
                  :class="{
                    'dot--current': i === currentTaskIndex && !taskResults[t.id],
                    'dot--correct': taskResults[t.id]?.correct === true,
                    'dot--wrong':   taskResults[t.id] && !taskResults[t.id].correct
                  }"
                  :aria-label="`Oppgave ${i + 1}${taskResults[t.id] ? (taskResults[t.id].correct ? ': riktig' : ': feil') : ''}`"
                />
              </template>
            </div>
            <button class="task-view__replay-btn" @click="replayIntro" :title="mysteryScenario ? 'Se historien på nytt' : 'Se oppgaveteksten på nytt'">
              ↩ Intro
            </button>
          </div>

          <!-- Avatar: in-flow spacer, hidden during peek so cards don't shift -->
          <div
            v-if="currentTask?.taskType !== 'LEARN'"
            class="task-view__avatar-wrap"
            :class="{ 'task-view__avatar-wrap--peekmode': !!peekState }"
          >
            <AvatarPreview
              :selections="avatarStore.avatar ?? {}"
              :size="160"
              class="task-view__avatar"
              aria-hidden="true"
            />
          </div>

          <!-- Task component -->
          <div class="task-view__content">
              <LearningTask
                v-if="currentTask.taskType === 'LEARN'"
                :task="currentTask"
                :result="result"
                :is-last-task="currentTaskIndex === tasks.length - 1"
                @submitted="handleSubmit"
                @next="goNext"
                @retry="result = null"
              />

              <FakeNewsTask
                v-else-if="currentTask.taskType === 'FAKE_NEWS'"
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

              <AIPhotoTask
                v-else-if="currentTask.taskType === 'AI_PHOTO'"
                :task="currentTask"
                :result="result"
                :is-last-task="currentTaskIndex === tasks.length - 1"
                @submitted="handleSubmit"
                @next="goNext"
              />

              <PasswordTask
                v-else-if="currentTask.taskType === 'PASSWORD'"
                :task="currentTask"
                :result="result"
                :is-last-task="currentTaskIndex === tasks.length - 1"
                @submitted="handleSubmit"
                @next="goNext"
              />

              <SocialMediaTask
                v-else-if="currentTask.taskType === 'SOCIAL_MEDIA'"
                :task="currentTask"
                :result="result"
                :is-last-task="currentTaskIndex === tasks.length - 1"
                @submitted="handleSubmit"
                @next="goNext"
              />

              <MarketplaceTask
                v-else-if="currentTask.taskType === 'MARKETPLACE'"
                :task="currentTask"
                :result="result"
                :is-last-task="currentTaskIndex === tasks.length - 1"
                @submitted="handleSubmit"
                @next="goNext"
              />

              <ClueRiddleTask
                v-else-if="currentTask.taskType === 'CLUE_RIDDLE'"
                :task="currentTask"
                :result="result"
                :is-last-task="currentTaskIndex === tasks.length - 1"
                @submitted="handleSubmit"
                @next="goNext"
                @try-again="result = null"
              />

              <FinalBossTask
                v-else-if="currentTask.taskType === 'FINAL_BOSS'"
                :task="currentTask"
                :result="result"
                @submitted="handleSubmit"
                @next="goNext"
              />

              <p v-else class="task-view__state task-view__state--error">
                Ukjent taskType: {{ currentTask.taskType }}
              </p>
            </div>
        </section>
      </template>
    </div>

    <!-- Peek avatar: fixed overlay, teleported so it doesn't affect layout -->
    <Teleport to="body">
      <div
        v-if="peekState && currentTask?.taskType !== 'LEARN'"
        class="task-view__peek-avatar"
        :style="avatarPeekStyle"
      >
        <AvatarPreview
          :selections="avatarStore.avatar ?? {}"
          :size="avatarPeekSize"
          aria-hidden="true"
        />
      </div>
    </Teleport>
    <ArrestScene
      v-if="arrestScene"
      :scene="arrestScene"
      @continue="advanceArrestScene"
    />

    <Transition name="stored-clue">
      <div v-if="storedClueModal" class="stored-clue-backdrop" role="presentation">
        <section
          class="stored-clue-modal"
          role="dialog"
          aria-modal="true"
          aria-labelledby="stored-clue-title"
        >
          <p class="stored-clue-modal__eyebrow">Spor lagret</p>
          <h2 id="stored-clue-title">Gåten er løst!</h2>
          <p class="stored-clue-modal__intro">
            Bra jobbet. Dette sporet er nå lagt inn i
            <strong>Mistenktmappe → Sporbrett</strong>, så du kan bruke det når du sammenligner mistenkte.
          </p>

          <article class="stored-clue-modal__card">
            <span>{{ storedClueModal.stopName }}</span>
            <p>{{ storedClueModal.clue }}</p>
          </article>

          <div class="stored-clue-modal__actions">
            <button type="button" class="stored-clue-modal__primary" @click="continueAfterStoredClue">
              Fortsett
            </button>
            <button
              v-if="!storedClueModal.requiresEndFlow"
              type="button"
              class="stored-clue-modal__secondary"
              @click="openClueBoard"
            >
              Åpne sporbrett
            </button>
          </div>
        </section>
      </div>
    </Transition>

    <ConfettiOverlay :active="confettiMode" />
    <MedalToast :medal="medalToast" />
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useGameStore } from '@/stores/game'
import { useClassroomStore } from '@/stores/classroom'
import { useAvatarStore } from '@/stores/avatar'
import DetectiveBar from '@/components/common/DetectiveBar.vue'
import TutorialScreen from '@/components/student/TutorialScreen.vue'
import LearningTask from '@/components/student/LearningTask.vue'
import StopMysteryScreen from '@/components/student/StopMysteryScreen.vue'
import FakeNewsTask from '@/components/student/FakeNewsTask.vue'
import AIPhotoTask from '@/components/student/AIPhotoTask.vue'
import PasswordTask from '@/components/student/PasswordTask.vue'
import SocialMediaTask from '@/components/student/SocialMediaTask.vue'
import MarketplaceTask from '@/components/student/MarketplaceTask.vue'
import PhishingEmailTask from '@/components/student/PhishingEmailTask.vue'
import ClueRiddleTask from '@/components/student/ClueRiddleTask.vue'
import FinalBossTask from '@/components/student/FinalBossTask.vue'
import ArrestScene from '@/components/student/ArrestScene.vue'
import ConfettiOverlay from '@/components/common/ConfettiOverlay.vue'
import MedalToast from '@/components/common/MedalToast.vue'
import StopSummary from '@/components/student/StopSummary.vue'
import { useSound } from '@/composables/useSound'
import AvatarPreview from '@/components/student/AvatarPreview.vue'

const { playCorrect, playWrong, playFanfare } = useSound()

const route          = useRoute()
const router         = useRouter()
const gameStore      = useGameStore()
const classroomStore = useClassroomStore()
const avatarStore    = useAvatarStore()

const tasks            = ref([])
const currentTaskIndex = ref(0)
const result           = ref(null)
const taskResults      = ref({})
const loading          = ref(false)
const error            = ref('')
const isMockMode       = ref(false)
const confettiMode     = ref(false)
const medalToast       = ref(null)
const showSummary      = ref(false)
const showTutorial     = ref(false)
const showMystery      = ref(false)
const arrestSceneStep  = ref(-1)
const peekState        = ref(null)
const storedClueModal  = ref(null)
let confettiTimer = null
let medalTimer    = null
let peekOutTimer  = null

const preferredMap = computed(() => localStorage.getItem('mapView') === 'simple' ? 'Map' : 'WorldMap')

const stopId = computed(() => Number(route.query.stopId ?? 0) || null)
const classroomId = computed(() => {
  const fromStore       = Number(classroomStore.currentClassroomId ?? 0)
  const fromQuery       = Number(route.query.classroomId ?? 0)
  const fromLocalStorage = Number(localStorage.getItem('classroomId') ?? 0)
  return fromStore || fromQuery || fromLocalStorage || null
})
const currentTask = computed(() => tasks.value[currentTaskIndex.value] ?? null)
const stopName    = computed(() => tasks.value[0]?.stopName ?? 'Oppgaver')
const arrestScenes = [
  {
    title: 'Tyven er arrestert!',
    body: 'Etterforskningen i Passordbanken avslørte hvordan tyven brukte svake passord og stjålne innlogginger. Politiet har tatt hovedmistenkte inn til avhør.',
    buttonText: 'Hva skjer nå?',
  },
  {
    title: 'Reserveplanen er i gang',
    body: 'Før arrestasjonen rakk tyven å aktivere en reserveplan i Datasenteret. Hvis du ikke stopper den nå, kan sporene etter de stjålne idrettsparkpengene bli slettet.',
    buttonText: 'Til Datasenteret',
  },
]
const arrestScene = computed(() => arrestScenes[arrestSceneStep.value] ?? null)

const avatarPeekSize = computed(() => {
  if (!peekState.value) return 160
  return Math.round(160 * peekState.value.scale)
})

const avatarPeekStyle = computed(() => {
  if (!peekState.value) return {}
  const { centerX, targetTop } = peekState.value
  const sz = avatarPeekSize.value
  return {
    position: 'fixed',
    left: `${centerX - sz / 2}px`,
    top: `${targetTop - sz * 0.73}px`,
    width: `${sz}px`,
    zIndex: 1,
    pointerEvents: 'none',
    transition: 'top 0.3s cubic-bezier(0.34, 1.56, 0.64, 1), left 0.3s ease, width 0.25s ease',
  }
})

const TUTORIAL_TEXTS = {
  LEARN: {
    title: 'Lær før du løser',
    instructions: 'Les gjennom kortene og svar riktig på alle spørsmålene for å gå videre til oppgavene.'
  },
  FAKE_NEWS: {
    title: 'Finn den falske nyheten',
    instructions: 'Du vil se to nyhetsartikler. Én av dem er falsk. Les overskrift, kilde og brødtekst nøye — klikk på den du tror er falsk!'
  },
  PHISHING_EMAIL: {
    title: 'Spot mistenkelige deler',
    instructions: 'Du vil lese en e-post. Klikk på delene du synes er mistenkelige — for eksempel avsenderen, lenker eller hastefraser. Klikk "Send svar" når du er ferdig.'
  },
  AI_PHOTO: {
    title: 'Ekte, KI-generert eller manipulert?',
    instructions: 'Du vil se bilder. For hvert bilde — velg om det er ekte, KI-generert eller manipulert. Se etter rare fingre, glatte bakgrunner og uskarp tekst som avslører KI.'
  },
  PASSWORD: {
    title: 'Bygg et sterkt passord',
    instructions: 'Du vil velge det tryggeste passordet, eller sette sammen ditt eget. Et sterkt passord er langt, bruker store og små bokstaver, tall og spesialtegn — og inneholder ikke personlig informasjon.'
  },
  MARKETPLACE: {
    title: 'Avdekk svindel på nett',
    instructions: 'Du vil se falske nettbutikker. Klikk på de delene som virker mistenkelige — domene, pris, betalingsvalg, kontaktinfo. Noen sider er helt trygge!'
  },
  SOCIAL_MEDIA: {
    title: 'Tenk før du deler',
    instructions: 'Du vil se innlegg fra sosiale medier. Tenk på kilden, språket og hasteoppfordringer. Sjekk alltid fakta før du deler videre.'
  },
  CLUE_RIDDLE: {
    title: 'Løs gåtesporet',
    instructions: 'Nå bruker du det du nettopp lærte i en liten etterforskningsoppgave. Les hvorfor oppgaven er viktig, sjekk beviset og velg svaret som gir best digitalt spor.'
  }
}

const THEME_EMOJI = {
  FAKE_NEWS:     '📰',
  PHISHING_EMAIL:'📧',
  AI_PHOTO:      '📷',
  PASSWORD:      '🔐',
  MARKETPLACE:   '🛒',
  SOCIAL_MEDIA:  '📱',
  CLUE_RIDDLE:   '🕵️',
  FINAL_BOSS:    '💻',
}

const STOP_MYSTERY_TITLES = {
  1: 'Et spor i nyhetsstrømmen',
  2: 'Bildet lyver',
  3: 'Ukjent avsender',
  4: 'Svindel på nett',
  5: 'Falsk venn',
  6: 'Passordlekkasje',
  7: 'Datasenteret er hacket',
}

const STOP_IMAGES = {
  1: { src: '/story_pictures/news-quarter-start.png', alt: 'Nyhetskvartalet med dyredetektiver, skjermer og aviser om de forsvunne idrettsparkpengene' },
  2: { src: '/story_pictures/photographer-start.png', alt: 'Fotografen med bevisbilder, kameraer og mistenkelige detaljer i et foto' },
  3: { src: '/story_pictures/post-office-start.png', alt: 'Postkontoret med mistenkelige meldinger, brev og digitale spor' },
  4: { src: '/story_pictures/marketplace-start.png', alt: 'Markedsplassen med mistenkelige butikker, falske tilbud og svindelspor' },
  5: { src: '/story_pictures/social-media-start.png', alt: 'Den sosiale møteplassen med meldinger, rykter og falske kontoer' },
  6: { src: '/story_pictures/password-bank-start.png', alt: 'Passordbanken med hvelv, digitale låser og spor etter svake passord' },
}

const mysteryScenario = computed(() => {
  const t = tasks.value[0]
  if (!t?.stopDescription) return null
  const emoji = THEME_EMOJI[t.stopTheme] ?? '🔍'
  const img   = STOP_IMAGES[t.stopOrderIndex]
  return {
    badge:    `${emoji} OPPDRAG ${t.stopOrderIndex}`,
    title:    STOP_MYSTERY_TITLES[t.stopOrderIndex] ?? t.stopName,
    scenario: t.stopDescription,
    imageSrc: img?.src ?? '',
    imageAlt: img?.alt ?? '',
  }
})

const tutorialTitle = computed(() =>
  TUTORIAL_TEXTS[currentTask.value?.taskType]?.title ?? 'Hva er oppgaven?'
)
const tutorialInstructions = computed(() =>
  TUTORIAL_TEXTS[currentTask.value?.taskType]?.instructions ?? 'Les oppgaven nøye og svar på best mulig måte.'
)

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
      .map(t => ({ ...t, contentJson: typeof t.contentJson === 'string' ? JSON.parse(t.contentJson) : t.contentJson }))
    currentTaskIndex.value = firstIncompleteTaskIndex(tasks.value)
    console.log('[TaskView] Loaded', tasks.value.length, 'tasks from API')
    isMockMode.value = false
  } catch (apiError) {
    console.error('[TaskView] Failed to fetch tasks.', apiError)
    if (import.meta.env.DEV) {
      tasks.value = buildMockTasks().filter((task) => task.stopId === stopId.value)
      console.log('[TaskView] Mock mode — loaded', tasks.value.length, 'mock tasks')
      isMockMode.value = true
    } else {
      error.value = 'Oppgaver utilgjengelige. Prøv igjen.'
    }
  } finally {
    loading.value = false
    checkMystery()
  }
}

function firstIncompleteTaskIndex(loadedTasks) {
  const index = loadedTasks.findIndex(task => !task.completed)
  return index >= 0 ? index : 0
}

function checkMystery() {
  if (!stopId.value || !tasks.value.length) return
  const mysteryKey = `mystery_seen_stop_${stopId.value}`
  if (mysteryScenario.value && !localStorage.getItem(mysteryKey)) {
    showMystery.value = true
    console.log('[TaskView] Showing mystery screen for stop', stopId.value)
    return
  }
  checkTutorial()
}

function acceptMystery() {
  showMystery.value = false
  localStorage.setItem(`mystery_seen_stop_${stopId.value}`, '1')
  console.log('[TaskView] Mystery accepted for stop', stopId.value)
  checkTutorial()
}

function checkTutorial() {
  if (!stopId.value || !tasks.value.length) return
  const taskType = tasks.value[0]?.taskType
  if (!taskType) return
  const key = tutorialSeenKey()
  if (!localStorage.getItem(key)) {
    showTutorial.value = true
  }
}

function startTasks() {
  const key = tutorialSeenKey()
  localStorage.setItem(key, '1')
  showTutorial.value = false
  console.log('[TaskView] Tutorial dismissed for stop', stopId.value)
}

function tutorialSeenKey() {
  return `tutorial_seen_classroom_${classroomId.value}_stop_${stopId.value}`
}

function replayIntro() {
  if (mysteryScenario.value) {
    showMystery.value = true
  } else {
    showTutorial.value = true
  }
  console.log('[TaskView] Replaying intro for stop', stopId.value)
}

async function handleSubmit(answer) {
  if (!currentTask.value) return
  const submittedTask = currentTask.value
  console.log('[TaskView] Submitting answer for task:', submittedTask.id, 'type:', submittedTask.taskType)
  try {
    result.value = await gameStore.submitAnswer(submittedTask.id, answer, classroomId.value)
    console.log('[TaskView] Submit result — correct:', result.value.correct, 'stopCompleted:', result.value.stopCompleted)
    handleCelebration(result.value)
    maybeShowStoredClueModal(submittedTask, result.value)
    isMockMode.value = false
  } catch (apiError) {
    console.error('[TaskView] Failed to submit answer.', apiError)
    if (import.meta.env.DEV) {
      result.value = buildMockResult(submittedTask, answer)
      handleCelebration(result.value)
      maybeShowStoredClueModal(submittedTask, result.value)
      isMockMode.value = true
    } else {
      error.value = 'Kunne ikke sende svar. Prøv igjen.'
    }
  }
}

function maybeShowStoredClueModal(task, submitResult) {
  if (
    task?.taskType !== 'CLUE_RIDDLE'
    || !submitResult?.correct
    || !submitResult?.stopCompleted
  ) {
    return
  }

  storedClueModal.value = {
    stopName: task.stopName ?? 'Nytt spor',
    clue: submitResult.clueText
      || submitResult.explanation
      || task.contentJson?.evidence
      || 'Et nytt spor er lagret i sporbrettet.',
    requiresEndFlow: submitResult.showSuspectReveal === true,
  }
}

function buildMockResult(task, answer) {
  const expected = task.mockCorrectAnswer ?? null
  const keys = expected ? Object.keys(expected) : []
  const correct = keys.length > 0 && keys.every(key => answer[key] === expected[key])
  return {
    correct,
    score: correct ? 100 : 40,
    starsEarned: correct ? 1 : 0,
    xpEarned: correct ? 20 : 0,
    explanation: correct
      ? (task.mockExplanation ?? 'Bra jobbet.')
      : (task.mockWrongExplanation ?? 'Dette stemmer ikke med sporet. Prøv igjen og se nærmere på beviset.'),
    stopCompleted: currentTaskIndex.value === tasks.value.length - 1,
    showSuspectReveal: correct
      && currentTaskIndex.value === tasks.value.length - 1
      && task.taskType === 'CLUE_RIDDLE'
      && task.stopTheme === 'PASSWORD',
    medalEarned: currentTaskIndex.value === tasks.value.length - 1
      ? { id: 1, name: 'Nyhetsdetektiv', description: 'Du fullførte stoppet i mock-modus.' }
      : null
  }
}

function buildMockTasks() {
  return [
    {
      id: 1000,
      stopId: 1,
      taskType: 'LEARN',
      guidanceText: 'Les kortene og svar på spørsmålene for å gå videre.',
      contentJson: {
        slides: [
          { id: 's1', icon: '📰', heading: 'Hva er falske nyheter?', body: 'Falske nyheter er artikler som ser ekte ut men inneholder løgner eller overdrivelser. De spres for å villede, skape frykt eller påvirke meninger.' },
          { id: 's2', icon: '🔍', heading: 'Slik avslører du dem', body: 'Sjekk kilden: er domenet kjent? Sjekk datoen: er dette gammelt? Søk etter samme nyhet på andre seriøse nettsteder. Sterke følelsesmessige overskrifter er et varseltegn.' },
          { id: 's3', icon: '🧠', heading: 'Tenk kritisk', body: 'Hvem tjener på at du tror på dette? Er bildet tatt ut av kontekst? Del aldri en nyhet du ikke har sjekket – du kan spre feilinformasjon videre.' }
        ],
        quiz: [
          {
            id: 'q1',
            question: 'Hva er det første du bør sjekke når du ser en nyhet?',
            options: ['Domenet og kilden', 'Fargen på overskriften', 'Antall likes og delinger'],
            correct: 'Domenet og kilden'
          },
          {
            id: 'q2',
            question: 'Hva er et typisk kjennetegn på falske nyheter?',
            options: ['Kjedelig overskrift', 'Sterk følelsesmessig overskrift som skaper frykt', 'Artikkelen har ingen bilder'],
            correct: 'Sterk følelsesmessig overskrift som skaper frykt'
          },
          {
            id: 'q3',
            question: 'Hva bør du gjøre før du deler en nyhet?',
            options: ['Dele den med en gang', 'Sjekke den på andre seriøse kilder', 'Se på hvem som har likt den'],
            correct: 'Sjekke den på andre seriøse kilder'
          }
        ]
      },
      mockCorrectAnswer: { quizPassed: true },
      mockExplanation: 'Du har lært det grunnleggende om falske nyheter!'
    },
    {
      id: 1001,
      stopId: 1,
      taskType: 'FAKE_NEWS',
      guidanceText: 'Marker hver artikkel som ekte eller falsk.',
      contentJson: {
        articles: [
          {
            headline: 'Trondheim kommune deler ut gratis nettbrett til alle elever',
            ingress: 'Kommunen tester en begrenset digital satsing i noen bydeler.',
            body: 'Kommunen tester ny digital satsing i fire bydeler.',
            source: 'Adresseavisen',
            date: '2026-01-14'
          },
          {
            headline: 'Forskere fant usynlig energi i skolemelk',
            ingress: 'Artikkelen lover superkrefter, men viser ikke til seriøs forskning.',
            body: 'Artikkelen påstår at melk gir superkrefter etter klokken 19.',
            source: 'nyheter24-ekte.no',
            date: '2026-01-14'
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
    },
    {
      id: 3001,
      stopId: 2,
      taskType: 'AI_PHOTO',
      guidanceText: 'Sorter hvert bilde: er det ekte, KI-generert eller manipulert?',
      contentJson: {
        images: [
          { id: 'image_0', src: '', alt: 'En person på en benk — fingrene ser litt rare ut', label: 'Bilde A' },
          { id: 'image_1', src: '', alt: 'Utsikt over en by tatt fra et vindu — normalt mobilbilde', label: 'Bilde B' }
        ]
      },
      mockCorrectAnswer: { image_0: 'AI_GENERATED', image_1: 'REAL' },
      mockExplanation: 'Bilde A er KI-generert — legg merke til de unaturlige fingrene og den glatte bakgrunnen.'
    },
    {
      id: 4001,
      stopId: 6,
      taskType: 'PASSWORD',
      guidanceText: 'Finn ut hvilket passord som er best.',
      contentJson: {
        type: 'CHOICE',
        question: 'Hvilket passord er tryggest?',
        options: [
          { id: 'a', value: 'Ola123' },
          { id: 'b', value: 'Emma2014' },
          { id: 'c', value: 'Katt' },
          { id: 'd', value: 'F!sk3Taco#92' }
        ],
        explanation: 'F!sk3Taco#92 er sterkest fordi det er langt og blander store og små bokstaver, tall og spesialtegn.'
      },
      mockCorrectAnswer: { selected: 'd' },
      mockExplanation: 'F!sk3Taco#92 er sterkest fordi det er langt og blander store og små bokstaver, tall og spesialtegn.'
    },
    {
      id: 4002,
      stopId: 6,
      taskType: 'CLUE_RIDDLE',
      guidanceText: 'Bruk passordsporet til å finne den beste forklaringen.',
      contentJson: {
        purpose: 'Svake passord kan avsløre både sted, rolle og vaner. Det hjelper deg å koble kontoen til riktig miljø.',
        evidence: 'Reservekontoen brukte passordet XooInnAdmin2019.',
        question: 'Hva forteller passordet oss?',
        options: [
          {
            id: 'random_strong',
            label: 'Det er et sterkt tilfeldig passord',
            detail: 'Det er ikke tilfeldig: det inneholder sted, rolle og årstall.',
          },
          {
            id: 'cafe_admin',
            label: 'Noen med admin-tilgang på Xoo Inn Cafe laget eller kjente kontoen',
            detail: 'Passordet peker mot stedet og en administratorrolle.',
          },
          {
            id: 'no_clue',
            label: 'Passord gir aldri etterforskningsspor',
            detail: 'Passord kan ofte avsløre vaner og koblinger.',
          },
        ],
      },
      mockCorrectAnswer: { selected: 'cafe_admin' },
      mockExplanation: 'Riktig. Passordet peker mot noen med admin-kobling til Xoo Inn Cafe.',
      mockWrongExplanation: 'Se etter hva i passordet som peker direkte mot både stedet og rollen.',
    },
    {
      id: 6001,
      stopId: 5,
      taskType: 'SOCIAL_MEDIA',
      guidanceText: 'Les innlegget nøye og velg den tryggeste handlingen.',
      contentJson: {
        post: {
          platform: 'Fjesbok',
          username: 'BesteFriend99',
          avatar: '👤',
          content: 'Hei! Jeg vant en premie og trenger telefonnummeret ditt for å sende den.'
        },
        question: 'Hva er det tryggeste du bør gjøre nå?',
        options: [
          { id: 'reply', text: 'Svar med telefonnummeret mitt' },
          { id: 'ignore', text: 'Ignorer meldingen' },
          { id: 'report', text: 'Rapporter og blokker kontoen' },
          { id: 'ask', text: 'Spør hvem det er' }
        ]
      },
      mockCorrectAnswer: { selected: 'report' },
      mockExplanation: 'Fremmede som ber om personinfo er et varseltegn. Rapporter og blokker.'
    },
    {
      id: 6002,
      stopId: 5,
      taskType: 'SOCIAL_MEDIA',
      guidanceText: 'Les innlegget nøye og velg den tryggeste handlingen.',
      contentJson: {
        post: {
          platform: 'Fjesbok',
          username: 'Nyhetshjelperen',
          avatar: '📢',
          content: 'Alle må dele dette nå! Skolen stenger i morgen for alltid. Ingen andre tør å si sannheten.'
        },
        question: 'Hva er smartest å gjøre før du reagerer på dette?',
        options: [
          { id: 'share', text: 'Del innlegget videre med en gang' },
          { id: 'ignore', text: 'Ignorer innlegget' },
          { id: 'report', text: 'Rapporter innlegget som falskt eller skadelig' },
          { id: 'ask', text: 'Sjekk om informasjonen stemmer før du gjør noe' }
        ]
      },
      mockCorrectAnswer: { selected: 'ask' },
      mockExplanation: 'Ikke del virale påstander før du har sjekket om de stemmer.'
    },
    {
      id: 6003,
      stopId: 5,
      taskType: 'SOCIAL_MEDIA',
      guidanceText: 'Les innlegget nøye og velg den tryggeste handlingen.',
      contentJson: {
        post: {
          platform: 'Fjesbok',
          username: 'Venn123',
          avatar: '🧑',
          content: 'Kan du sende meg passordet ditt? Jeg skal bare hjelpe deg å logge inn raskt.'
        },
        question: 'Hva gjør du?',
        options: [
          { id: 'reply', text: 'Sender passordet mitt i chatten' },
          { id: 'ignore', text: 'Lar være å svare' },
          { id: 'report', text: 'Rapporterer meldingen med en gang' },
          { id: 'ask', text: 'Sier nei og forklarer at passord aldri skal deles' }
        ]
      },
      mockCorrectAnswer: { selected: 'ask' },
      mockExplanation: 'Selv venner skal ikke ha passordet ditt. Den tryggeste handlingen er å si nei og aldri dele det.'
    },
    {
      id: 5001,
      stopId: 4,
      taskType: 'MARKETPLACE',
      guidanceText: 'Se etter priser, betaling og hastverk før du handler.',
      contentJson: {
        type: 'IDENTIFY',
        siteName: 'sneaker-blitz.shop',
        question: 'Hva er det tydeligste faresignalet her?',
        options: [
          { id: 'cheap', text: 'Prisen er altfor lav sammenlignet med vanlige butikker' },
          { id: 'colors', text: 'Butikken bruker sterke farger og store overskrifter' },
          { id: 'shipping', text: 'Nettsiden lover rask levering' }
        ],
        mockup: {
          eyebrow: 'Kun i dag',
          headline: 'Eksklusive sneakers til 79 kr',
          tagline: '90 % rabatt og bare noen få minutter igjen.',
          productName: 'Street Runner X',
          price: '79 kr',
          originalPrice: '1 499 kr',
          ctaText: 'Kjøp nå',
          badges: ['90 % rabatt', 'Begrenset antall'],
          notice: 'Betal raskt for å sikre varen din.'
        }
      },
      mockCorrectAnswer: { selected: 'cheap' },
      mockExplanation: 'Ekstreme rabatter og kunstig hastverk er vanlige faresignaler i nettsvindel.'
    },
    {
      id: 5002,
      stopId: 4,
      taskType: 'MARKETPLACE',
      guidanceText: 'Velg det mest mistenkelige tegnet før du betaler.',
      contentJson: {
        type: 'IDENTIFY',
        siteName: 'tech-deals-market.net',
        question: 'Hva bør gjøre deg mest skeptisk?',
        options: [
          { id: 'giftcard', text: 'Butikken vil bare ha betaling med gavekort eller krypto' },
          { id: 'sale', text: 'Det står at det er sommersalg' },
          { id: 'rating', text: 'Produktet har mange stjerner' }
        ],
        mockup: {
          eyebrow: 'Ekspresssalg',
          headline: 'Spillkonsoll til halv pris',
          tagline: 'Kun alternative betalingsmåter godtas.',
          productName: 'PlayBox Ultra',
          price: '2 199 kr',
          originalPrice: '4 399 kr',
          ctaText: 'Betal nå',
          badges: ['Kun gavekort', 'Ingen refusjon'],
          notice: 'Kortbetaling er midlertidig utilgjengelig.'
        }
      },
      mockCorrectAnswer: { selected: 'giftcard' },
      mockExplanation: 'Betaling med gavekort eller krypto er vanskelig å spore og brukes ofte i svindel.'
    },
    {
      id: 5003,
      stopId: 4,
      taskType: 'MARKETPLACE',
      guidanceText: 'Velg nettstedet du ville styrt unna.',
      contentJson: {
        type: 'RANK',
        question: 'Hvilken nettbutikk virker mest sannsynlig å være svindel?',
        sites: [
          {
            id: 'site-a',
            name: 'friluftshuset.no',
            badge: 'Kort og Klarna',
            description: 'Tydelig returinfo og organisasjonsnummer.'
          },
          {
            id: 'site-b',
            name: 'merkevarer-outlet-fast.com',
            badge: 'Kun forskuddsbetaling',
            description: 'Ekstreme rabatter, mangler kontaktinfo og presser deg til å betale raskt.'
          },
          {
            id: 'site-c',
            name: 'spillsonen.no',
            badge: 'Kundeservice',
            description: 'Viser åpningstider, adresse og vanlige betalingsvalg.'
          },
          {
            id: 'site-d',
            name: 'bokbyen.no',
            badge: 'Trygg betaling',
            description: 'Har anmeldelser, leveringsvilkår og kjent domene.'
          }
        ]
      },
      mockCorrectAnswer: { selected: 'site-b' },
      mockExplanation: 'Nettbutikken med ekstreme rabatter, dårlig kontaktinfo og forskuddsbetaling er den mest mistenkelige.'
    }
  ]
}

function handleCelebration(submitResult) {
  clearTimeout(confettiTimer)
  if (submitResult?.correct) {
    confettiMode.value = submitResult.stopCompleted ? 'stop' : 'correct'
    submitResult.stopCompleted ? playFanfare() : playCorrect()
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

function shouldShowArrestScene() {
  return result.value?.correct
    && result.value?.stopCompleted
    && result.value?.showSuspectReveal
    && currentTask.value?.taskType === 'CLUE_RIDDLE'
}

async function advanceArrestScene() {
  if (arrestSceneStep.value < arrestScenes.length - 1) {
    arrestSceneStep.value += 1
    return
  }
  arrestSceneStep.value = -1
  router.push({ name: preferredMap.value })
}

onBeforeUnmount(() => {
  clearTimeout(confettiTimer)
  clearTimeout(medalTimer)
  clearTimeout(peekOutTimer)
})

function handlePeekHover(e) {
  const trigger = e.target.closest('[data-peek-trigger]')
  if (!trigger) return
  clearTimeout(peekOutTimer)
  const rect = trigger.getBoundingClientRect()
  const scale = Math.min(Math.max(rect.width / 250, 0.5), 1.3)
  peekState.value = { centerX: rect.left + rect.width / 2, targetTop: rect.top, scale }
}

function handlePeekOut(e) {
  if (!e.relatedTarget?.closest?.('[data-peek-trigger]')) {
    clearTimeout(peekOutTimer)
    peekOutTimer = setTimeout(() => { peekState.value = null }, 80)
  }
}

function goNext() {
  if (result.value && currentTask.value) {
    taskResults.value[currentTask.value.id] = result.value
  }
  if (shouldShowArrestScene()) {
    arrestSceneStep.value = 0
    return
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

function continueAfterStoredClue() {
  storedClueModal.value = null
  goNext()
}

function openClueBoard() {
  storedClueModal.value = null
  router.push({ name: 'SuspectDossier', query: { panel: 'clues' } })
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
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.task-view__dossier-link {
  position: fixed;
  right: 1rem;
  top: 4rem;
  z-index: 90;
  display: inline-flex;
  align-items: center;
  min-height: 44px;
  padding: 0.65rem 0.9rem;
  border: 2px solid rgba(47, 26, 8, 0.32);
  border-radius: 8px;
  background: #fff8df;
  color: #3b1f08;
  box-shadow: 0 6px 0 rgba(47, 26, 8, 0.28);
  font-weight: 900;
  text-decoration: none;
}

.task-view__dossier-link:hover {
  transform: translateY(-1px);
}

.task-view__tutorial-wrap,
.task-view__main {
  flex: 1;
  padding: var(--space-6) clamp(var(--space-4), 5vw, var(--space-10));
}

.task-view__main--clean {
  background:
    radial-gradient(circle at top right, rgba(244, 201, 76, 0.18), transparent 30%),
    linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
}

.task-view__state { text-align: center; padding: var(--space-8); color: var(--color-text); }
.task-view__state--error { color: var(--color-danger); }

.task-view__mock-badge {
  color: var(--color-warning);
  background: var(--color-warning-light);
  border: 1px solid var(--color-accent);
  border-radius: var(--radius-md);
  padding: var(--space-2) var(--space-3);
  width: fit-content;
  margin: 0 auto var(--space-4);
}

/* Progress meta row: dots + replay button */
.task-view__meta {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-4);
  padding: var(--space-2) 0 var(--space-4);
}

.task-view__replay-btn {
  background: none;
  border: 1px solid rgba(30, 41, 59, 0.18);
  border-radius: var(--radius-full);
  padding: 2px var(--space-3);
  font-size: var(--text-xs);
  font-weight: 600;
  color: var(--color-text-muted);
  cursor: pointer;
  transition: color var(--transition-fast), border-color var(--transition-fast);
  white-space: nowrap;
}
.task-view__replay-btn:hover {
  color: var(--color-primary);
  border-color: var(--color-primary);
}

/* Progress dots */
.task-dots {
  display: flex;
  gap: var(--space-2);
  justify-content: center;
}
.dot {
  width: 12px;
  height: 12px;
  border-radius: var(--radius-full);
  background: rgba(30, 41, 59, 0.16);
  border: 2px solid transparent;
  transition: background var(--transition-fast), transform var(--transition-fast);
}
.dot--current { border-color: var(--color-primary); background: var(--color-primary-soft); transform: scale(1.2); }
.dot--correct { background: var(--color-success); }
.dot--wrong   { background: var(--color-danger); }

/* Avatar shown above task content, centered */
.task-view__avatar-wrap {
  display: flex;
  justify-content: center;
  padding-bottom: var(--space-2);
}
/* Keep layout space but hide the in-flow avatar while peek overlay is active */
.task-view__avatar-wrap--peekmode { visibility: hidden; }

/* Peek overlay: teleported to body, lives behind task cards via z-index */
.task-view__peek-avatar { display: block; }

.task-view__avatar {
  animation: avatar-drop-in 0.45s cubic-bezier(0.34, 1.56, 0.64, 1) both;
  filter: drop-shadow(0 6px 12px rgba(0,0,0,0.35));
}

@keyframes avatar-drop-in {
  from { transform: translateY(-30px); opacity: 0; }
  to   { transform: translateY(0);     opacity: 1; }
}

.task-view__content { flex: 1; min-width: 0; }

.stored-clue-backdrop {
  position: fixed;
  inset: 0;
  z-index: 210;
  display: grid;
  place-items: center;
  padding: clamp(1rem, 4vw, 2rem);
  background: rgba(26, 14, 4, 0.78);
  overflow-y: auto;
}

.stored-clue-modal {
  width: min(32rem, 100%);
  max-height: min(100%, 42rem);
  overflow-y: auto;
  padding: clamp(1.25rem, 4vw, 2rem);
  border: 4px solid #2f1a08;
  border-radius: 10px;
  background:
    linear-gradient(90deg, rgba(255, 255, 255, 0.42) 1px, transparent 1px),
    linear-gradient(#fff4d0, #fff9e8);
  background-size: 24px 24px, auto;
  box-shadow: 0 18px 0 rgba(0, 0, 0, 0.28);
  color: #2b1808;
}

.stored-clue-modal__eyebrow {
  width: fit-content;
  margin: 0 0 var(--space-2);
  padding: 0.35rem 0.65rem;
  border-radius: 999px;
  background: #0f766e;
  color: #fff;
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.stored-clue-modal h2 {
  margin: 0 0 var(--space-2);
  font-size: clamp(1.8rem, 6vw, 3rem);
  line-height: 1;
}

.stored-clue-modal__intro {
  margin: 0;
  color: #4b341b;
  font-size: var(--text-base);
  line-height: 1.45;
}

.stored-clue-modal__card {
  margin: var(--space-5) 0;
  padding: clamp(1rem, 3vw, 1.25rem);
  border: 3px dashed #b45309;
  border-radius: 8px;
  background: #fffbeb;
}

.stored-clue-modal__card span {
  display: inline-block;
  margin-bottom: var(--space-2);
  color: #92400e;
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
  text-transform: uppercase;
}

.stored-clue-modal__card p {
  margin: 0;
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  line-height: 1.45;
}

.stored-clue-modal__actions {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.stored-clue-modal__actions button {
  flex: 1 1 12rem;
  min-height: 44px;
  padding: var(--space-2) var(--space-5);
  border-radius: 8px;
  font: inherit;
  font-weight: var(--font-bold);
  cursor: pointer;
}

.stored-clue-modal__primary {
  border: 0;
  background: #0f766e;
  color: #fff;
}

.stored-clue-modal__secondary {
  border: 2px solid #2f1a08;
  background: #fff8df;
  color: #2f1a08;
}

.stored-clue-enter-active,
.stored-clue-leave-active {
  transition: opacity 0.18s ease;
}

.stored-clue-enter-active .stored-clue-modal,
.stored-clue-leave-active .stored-clue-modal {
  transition: transform 0.22s ease, opacity 0.18s ease;
}

.stored-clue-enter-from,
.stored-clue-leave-to {
  opacity: 0;
}

.stored-clue-enter-from .stored-clue-modal,
.stored-clue-leave-to .stored-clue-modal {
  opacity: 0;
  transform: translateY(18px) scale(0.96);
}

@media (max-width: 640px) {
  .task-view__dossier-link {
    position: static;
    align-self: flex-end;
    margin: 0.75rem 1rem 0;
  }

  .task-view__layout { flex-direction: column; align-items: center; }
  .task-view__avatar { width: 72px; height: 72px; }
  .stored-clue-modal {
    box-shadow: 0 10px 0 rgba(0, 0, 0, 0.24);
  }

  .stored-clue-modal__actions { display: grid; }
}

.task-view__content { width: 100%; position: relative; z-index: 2; }
</style>
