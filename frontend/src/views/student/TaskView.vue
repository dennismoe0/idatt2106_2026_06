<template>
  <div class="task-view">
    <DetectiveBar :back-to="{ name: preferredMap }" :page-title="stopName" />

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
          <!-- Replay tutorial button (always visible) -->
          <button
            class="task-view__replay-btn"
            @click="showTutorial = true"
            aria-label="Se oppgaveforklaringen på nytt"
          >
            Se oppgaven på nytt 🔁
          </button>

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

          <!-- Task layout: expanded avatar + task content -->
          <div class="task-view__layout">
            <!-- Expanded avatar (animates down from bar) -->
            <div class="task-view__avatar-wrap">
              <AvatarPreview
                :selections="avatarStore.avatar ?? {}"
                :size="100"
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
          </div>
        </section>
      </template>
    </div>

    <ClueRevealModal
      v-if="showClueModal"
      :clue-text="result?.clueText"
      @close="handleClueModalClosed"
    />
    <SuspectLineup
      v-if="showSuspectLineup"
      @chosen="handleSuspectChosen"
    />
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
import FinalBossTask from '@/components/student/FinalBossTask.vue'
import ClueRevealModal from '@/components/student/ClueRevealModal.vue'
import SuspectLineup from '@/components/student/SuspectLineup.vue'
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
const showClueModal    = ref(false)
const showSuspectLineup = ref(false)
const showSummary      = ref(false)
const showTutorial     = ref(false)
const showMystery      = ref(false)
let confettiTimer = null
let medalTimer    = null

const preferredMap = localStorage.getItem('mapView') === 'simple' ? 'Map' : 'WorldMap'

const stopId = computed(() => Number(route.query.stopId ?? 0) || null)
const classroomId = computed(() => {
  const fromStore       = Number(classroomStore.currentClassroomId ?? 0)
  const fromQuery       = Number(route.query.classroomId ?? 0)
  const fromLocalStorage = Number(localStorage.getItem('classroomId') ?? 0)
  return fromStore || fromQuery || fromLocalStorage || null
})
const currentTask = computed(() => tasks.value[currentTaskIndex.value] ?? null)
const stopName    = computed(() => tasks.value[0]?.stopName ?? 'Oppgaver')

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
  }
}

const THEME_EMOJI = {
  FAKE_NEWS:     '📰',
  PHISHING_EMAIL:'📧',
  AI_PHOTO:      '📷',
  PASSWORD:      '🔐',
  MARKETPLACE:   '🛒',
  SOCIAL_MEDIA:  '📱',
  FINAL_BOSS:    '💻',
}

const STOP_MYSTERY_TITLES = {
  1: 'Et spor i nyhetsstrømmen',
  2: 'Ukjent avsender',
  3: 'Bildet lyver',
  4: 'Passordlekkasje',
  5: 'Svindel på nett',
  6: 'Falsk venn',
  7: 'Datasenteret er hacket',
}

const STOP_IMAGES = {
  1: { src: '/story_pictures/news-quarter-start.png', alt: 'Nyhetskvartalet med dyredetektiver, skjermer og aviser om de forsvunne idrettsparkpengene' },
  2: { src: '/story_pictures/post-office-start.png', alt: 'Postkontoret med mistenkelige meldinger, brev og digitale spor' },
  3: { src: '/story_pictures/photographer-start.png', alt: 'Fotografen med bevisbilder, kameraer og mistenkelige detaljer i et foto' },
  4: { src: '/story_pictures/password-bank-start.png', alt: 'Passordbanken med hvelv, digitale låser og spor etter svake passord' },
  5: { src: '/story_pictures/marketplace-start.png', alt: 'Markedsplassen med mistenkelige butikker, falske tilbud og svindelspor' },
  6: { src: '/story_pictures/social-media-start.png', alt: 'Den sosiale møteplassen med meldinger, rykter og falske kontoer' },
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

function checkMystery() {
  if (!stopId.value || !tasks.value.length) return
  if (mysteryScenario.value) {
    showMystery.value = true
    console.log('[TaskView] Showing mystery screen for stop', stopId.value)
    return
  }
  checkTutorial()
}

function acceptMystery() {
  showMystery.value = false
  console.log('[TaskView] Mystery accepted for stop', stopId.value)
  checkTutorial()
}

function checkTutorial() {
  if (!stopId.value || !tasks.value.length) return
  const taskType = tasks.value[0]?.taskType
  if (!taskType) return
  const key = `tutorial_seen_stop_${stopId.value}`
  if (!localStorage.getItem(key)) {
    showTutorial.value = true
  }
}

function startTasks() {
  const key = `tutorial_seen_stop_${stopId.value}`
  localStorage.setItem(key, '1')
  showTutorial.value = false
  console.log('[TaskView] Tutorial dismissed for stop', stopId.value)
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
  const correct = keys.every(key => answer[key] === expected[key])
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
    },
    {
      id: 3001,
      stopId: 3,
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
      stopId: 4,
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
      id: 6001,
      stopId: 6,
      taskType: 'SOCIAL_MEDIA',
      guidanceText: 'Les innlegget nøye og velg den tryggeste handlingen.',
      contentJson: {
        post: {
          platform: 'Fjesbok',
          username: 'BesteFriend99',
          avatar: '👤',
          content: 'Hei! Jeg vant en premie og trenger telefonnummeret ditt for å sende den.'
        },
        question: 'Hva gjør du?',
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
      stopId: 6,
      taskType: 'SOCIAL_MEDIA',
      guidanceText: 'Les innlegget nøye og velg den tryggeste handlingen.',
      contentJson: {
        post: {
          platform: 'Fjesbok',
          username: 'Nyhetshjelperen',
          avatar: '📢',
          content: 'Alle må dele dette nå! Skolen stenger i morgen for alltid. Ingen andre tør å si sannheten.'
        },
        question: 'Hva gjør du?',
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
      stopId: 6,
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
      stopId: 5,
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
      stopId: 5,
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
      stopId: 5,
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
  if (submitResult?.stopCompleted) {
    if (submitResult.showSuspectReveal) {
      setTimeout(() => { showSuspectLineup.value = true }, 1200)
    } else if (submitResult.clueText) {
      setTimeout(() => { showClueModal.value = true }, 1200)
    }
  }
}

function handleClueModalClosed() {
  showClueModal.value = false
}
function handleSuspectChosen() {
  showSuspectLineup.value = false
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
  display: flex;
  flex-direction: column;
  min-height: 100vh;
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

.task-view__replay-btn {
  display: block;
  margin: 0 auto var(--space-3);
  background: rgba(255, 255, 255, 0.85);
  color: var(--color-heading);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: var(--space-2) var(--space-4);
  font-size: var(--text-sm);
  font-weight: 600;
  cursor: pointer;
  min-height: 44px;
  transition: background var(--transition-fast), box-shadow var(--transition-fast), border-color var(--transition-fast);
  box-shadow: 0 10px 20px rgba(20, 30, 48, 0.06);
}
.task-view__replay-btn:hover {
  background: #ffffff;
  border-color: var(--color-primary-soft-strong);
  box-shadow: 0 14px 26px rgba(20, 30, 48, 0.1);
}
.task-view__replay-btn:focus-visible { outline: 3px solid var(--color-gold); outline-offset: 2px; }

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

/* Progress dots */
.task-dots {
  display: flex;
  gap: var(--space-2);
  justify-content: center;
  padding: var(--space-2) 0 var(--space-4);
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

/* Task layout: avatar beside content */
.task-view__layout {
  display: flex;
  align-items: flex-start;
  gap: var(--space-6);
}

.task-view__avatar-wrap {
  flex-shrink: 0;
  animation: avatar-drop-in 0.4s cubic-bezier(0.34, 1.56, 0.64, 1) both;
  transform-origin: top center;
}
@keyframes avatar-drop-in {
  from { transform: scale(0.3) translateY(-60px); opacity: 0; }
  to   { transform: scale(1) translateY(0);       opacity: 1; }
}

.task-view__avatar {
  width: 96px;
  height: 96px;
  border-radius: 50%;
  object-fit: cover;
}

.task-view__content { flex: 1; min-width: 0; }

@media (max-width: 640px) {
  .task-view__layout { flex-direction: column; align-items: center; }
  .task-view__avatar { width: 72px; height: 72px; }
}
</style>
