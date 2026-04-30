<template>
  <CorkBoardPage page-title="Notatblokk" :back-to="{ name: 'Home' }">
    <div v-if="showJournalOverlay" class="journal-overlay" role="dialog" aria-label="Notatblokk">
      <div class="journal-cover-stage" :class="{ 'is-opening': isOpening }" :style="coverStageStyle">
        <div class="journal-cover-stage__spread" aria-hidden="true">
          <div class="journal-cover-stage__inside"></div>
          <div class="journal-cover-stage__pages"></div>
          <div class="journal-cover-stage__spine"></div>
        </div>

        <div class="journal-cover" aria-hidden="true">
          <span class="journal-cover__sigil" aria-hidden="true">
            <span class="journal-cover__sigil-eye"></span>
          </span>
          <span class="journal-cover__badge">Arkiv 03</span>
          <span class="journal-cover__title">MYSTISK JOURNAL</span>
          <span class="journal-cover__hint">Åpner...</span>
        </div>
      </div>
    </div>

    <div class="journal-reader" :class="{ 'is-revealed': !showJournalOverlay }">
      <div v-if="loading" class="journal-state" aria-live="polite">
        <span class="journal-state__spinner" aria-hidden="true" />
        <p>Laster notatblokk...</p>
      </div>

      <div v-else-if="error" class="journal-state journal-state--error" role="alert">
        <p>{{ error }}</p>
        <button class="journal-state__retry" type="button" @click="load">Prøv igjen</button>
      </div>

      <template v-else>
        <div class="journal-reader__stage-shell">
          <button
            class="journal-nav__arrow journal-nav__arrow--left"
            type="button"
            :disabled="!canGoPrev"
            @click="goPrev"
            aria-label="Forrige side"
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
              <path d="M15 18L9 12L15 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>

          <div
            class="journal-bookstage"
            :class="{ 'journal-bookstage--kindle': isKindleMode }"
            :style="bookStageStyle"
          >
            <div class="journal-bookstage__shadow" aria-hidden="true"></div>
            <div v-if="!isKindleMode" class="journal-bookstage__spine" aria-hidden="true"></div>

            <article
              v-if="isKindleMode"
              class="journal-paper journal-paper--single"
              :data-page="kindlePage.number"
            >
              <NotebookJournalPage
                :page="kindlePage"
                side="right"
                interactive
                :start-stop-edit="startEdit"
                :start-add-note="startAddNote"
                :start-note-edit="startNoteEdit"
                :remove-note="removeNote"
                :start-reflection-edit="startReflectionEdit"
                :remove-reflection="removeReflection"
              />
            </article>

            <template v-else>
              <article class="journal-paper journal-paper--left" :data-page="currentSpread.left.number">
                <NotebookJournalPage
                  :page="currentSpread.left"
                  side="left"
                  interactive
                  :start-stop-edit="startEdit"
                  :start-add-note="startAddNote"
                  :start-note-edit="startNoteEdit"
                  :remove-note="removeNote"
                  :start-reflection-edit="startReflectionEdit"
                  :remove-reflection="removeReflection"
                />
              </article>

              <article class="journal-paper journal-paper--right" :data-page="currentSpread.right.number">
                <NotebookJournalPage
                  :page="currentSpread.right"
                  side="right"
                  interactive
                  :start-stop-edit="startEdit"
                  :start-add-note="startAddNote"
                  :start-note-edit="startNoteEdit"
                  :remove-note="removeNote"
                  :start-reflection-edit="startReflectionEdit"
                  :remove-reflection="removeReflection"
                />
              </article>

              <div v-if="turnLeaf" class="journal-leaf" :class="`journal-leaf--${turnLeaf.direction}`" aria-hidden="true">
                <article class="journal-paper journal-paper--leaf-face journal-paper--leaf-front" :data-page="turnLeaf.front.number">
                  <NotebookJournalPage :page="turnLeaf.front" :side="turnLeaf.frontSide" />
                </article>
                <article class="journal-paper journal-paper--leaf-face journal-paper--leaf-back" :data-page="turnLeaf.back.number">
                  <NotebookJournalPage :page="turnLeaf.back" :side="turnLeaf.backSide" />
                </article>
              </div>
            </template>
          </div>

          <button
            class="journal-nav__arrow journal-nav__arrow--right"
            type="button"
            :disabled="!canGoNext"
            @click="goNext"
            aria-label="Neste side"
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
              <path d="M9 6L15 12L9 18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>
      </template>
    </div>

    <NotebookEditModal
      :open="editingReflectionId !== null"
      title="Rediger observasjon"
      :initial-content="editingReflectionContent"
      :saving="saving"
      :error="saveError"
      @save="saveCurrentReflectionEdit"
      @cancel="cancelReflectionEdit"
    />

    <NotebookEditModal
      :open="editingStop !== null"
      title="Ny observasjon"
      :saving="saving"
      :error="saveError"
      @save="submitCurrentObservation"
      @cancel="cancelEdit"
    />

    <NotebookEditModal
      :open="addingNote"
      title="Nytt notat"
      :saving="saving"
      :error="saveError"
      @save="submitGeneralNote"
      @cancel="cancelAddNote"
    />

    <NotebookEditModal
      :open="editingNoteId !== null"
      title="Rediger notat"
      :initial-content="editingNoteContent"
      :saving="saving"
      :error="saveError"
      @save="saveCurrentEdit"
      @cancel="cancelNoteEdit"
    />
  </CorkBoardPage>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import CorkBoardPage from '@/components/common/CorkBoardPage.vue'
import NotebookEditModal from '@/components/student/NotebookEditModal.vue'
import NotebookJournalPage from '@/components/student/NotebookJournalPage.vue'
import { useSound } from '@/composables/useSound'
import { useNotebookStore } from '@/stores/notebook'
import {
  RESERVED_REPORT_COUNT,
  buildGeneralPages,
  buildLevelPagesForGroup,
  createBlankPage,
  createBlankSeed,
} from '@/utils/notebookPagination'

const notebookStore = useNotebookStore()
const { playPageTurn } = useSound()

const grouped = computed(() => notebookStore.grouped)
const generalNotes = computed(() => notebookStore.generalNotes)

const loading = ref(true)
const error = ref(null)
const saving = ref(false)
const saveError = ref(null)

const editingStop = ref(null)
const addingNote = ref(false)
const editingNoteId = ref(null)
const editingNoteContent = ref('')
const editingReflectionId = ref(null)
const editingReflectionContent = ref('')

const showJournalOverlay = ref(true)
const isOpening = ref(false)
const animationDuration = 980
const openHoldMs = 220
const pageTurnDuration = 900
const spreadIndex = ref(0)
const turnState = ref(null)
const viewport = ref({ width: 1280, height: 900 })

const BOOK_ASPECT_RATIO = 1.58
const BOOK_WIDTH_SCALE = 1.25

let openTimer = null
let autoOpenTimer = null
let turnMidTimer = null
let turnEndTimer = null


const allLevelPages = computed(() => {
  const groupByOrder = new Map(
    grouped.value.map((g) => [g.stopOrder, g])
  )
  return Array.from({ length: RESERVED_REPORT_COUNT }, (_, i) => {
    const stopOrder = i + 1
    return buildLevelPagesForGroup(stopOrder, groupByOrder.get(stopOrder))
  }).flat()
})

const paginatedGeneralPages = computed(() => buildGeneralPages(generalNotes.value))

const journalPages = computed(() => {
  const pages = [...paginatedGeneralPages.value, ...allLevelPages.value]

  if (pages.length < 2) {
    pages.push(createBlankSeed('blank-fallback'))
  }

  if (pages.length % 2 !== 0) {
    pages.push(createBlankSeed('blank-tail'))
  }

  return pages.map((page, index) => ({
    ...page,
    number: index + 2
  }))
})

const spreadCount = computed(() => Math.max(1, Math.ceil(journalPages.value.length / 2)))
const pageCount = computed(() => Math.max(1, journalPages.value.length))
const isKindleMode = computed(() => viewport.value.width <= 720)
const kindlePageIndex = ref(0)
const canGoPrev = computed(() => {
  if (turnState.value) return false
  return isKindleMode.value ? kindlePageIndex.value > 0 : spreadIndex.value > 0
})
const canGoNext = computed(() => {
  if (turnState.value) return false
  return isKindleMode.value
    ? kindlePageIndex.value < pageCount.value - 1
    : spreadIndex.value < spreadCount.value - 1
})
const isCompactLayout = computed(() => viewport.value.width <= 900)

const bookHeight = computed(() => {
  const isNarrow = viewport.value.width <= 640
  const chromeReserve = isCompactLayout.value ? 282 : 214
  const availableHeight = viewport.value.height - chromeReserve
  const horizontalPadding = isNarrow ? 24 : 72
  const availableWidth = viewport.value.width - horizontalPadding
  const widthRatio = isKindleMode.value
    ? 0.72
    : BOOK_ASPECT_RATIO * BOOK_WIDTH_SCALE
  const widthLimitedHeight = availableWidth / widthRatio
  const minHeight = isKindleMode.value ? 360 : 220
  return Math.round(Math.min(820, Math.max(minHeight, Math.min(availableHeight, widthLimitedHeight))))
})

const bookWidth = computed(() => {
  const ratio = isKindleMode.value ? 0.72 : BOOK_ASPECT_RATIO * BOOK_WIDTH_SCALE
  return Math.round(bookHeight.value * ratio)
})
const bookStageStyle = computed(() => ({
  width: `${bookWidth.value}px`,
  height: `${bookHeight.value}px`,
  '--turn-ms': `${pageTurnDuration}ms`
}))

const coverHeight = computed(() => {
  const isNarrow = viewport.value.width <= 640
  const availableHeight = viewport.value.height - (isNarrow ? 110 : 150)
  const availableWidth = viewport.value.width - (isNarrow ? 32 : 96)
  const minHeight = isNarrow ? 200 : 260
  const maxHeight = isNarrow ? 360 : 470
  return Math.round(Math.min(maxHeight, Math.max(minHeight, Math.min(availableHeight, availableWidth / 1.56))))
})

const coverWidth = computed(() => Math.round(coverHeight.value * 0.78))
const coverStageStyle = computed(() => ({
  '--anim-ms': `${animationDuration}ms`,
  '--cover-width': `${coverWidth.value}px`,
  '--cover-height': `${coverHeight.value}px`
}))

function getPageAt(pageIndex) {
  return journalPages.value[pageIndex] || createBlankPage(pageIndex)
}

function getSpread(targetSpreadIndex) {
  const pageIndex = targetSpreadIndex * 2
  return {
    left: getPageAt(pageIndex),
    right: getPageAt(pageIndex + 1)
  }
}

const currentSpread = computed(() => getSpread(spreadIndex.value))
const kindlePage = computed(() => getPageAt(kindlePageIndex.value))

const turnLeaf = computed(() => {
  if (!turnState.value) return null

  const fromSpread = getSpread(turnState.value.from)
  const toSpread = getSpread(turnState.value.to)

  if (turnState.value.direction === 'next') {
    return {
      direction: 'next',
      front: fromSpread.right,
      back: toSpread.left,
      frontSide: 'right',
      backSide: 'left'
    }
  }

  return {
    direction: 'back',
    front: fromSpread.left,
    back: toSpread.right,
    frontSide: 'left',
    backSide: 'right'
  }
})

function clearOpenTimer() {
  if (openTimer) {
    clearTimeout(openTimer)
    openTimer = null
  }
  if (autoOpenTimer) {
    clearTimeout(autoOpenTimer)
    autoOpenTimer = null
  }
}

function clearTurnTimers() {
  if (turnMidTimer) {
    clearTimeout(turnMidTimer)
    turnMidTimer = null
  }

  if (turnEndTimer) {
    clearTimeout(turnEndTimer)
    turnEndTimer = null
  }
}

function updateViewport() {
  viewport.value = {
    width: window.innerWidth,
    height: window.innerHeight
  }
}

async function load() {
  loading.value = true
  error.value = null

  try {
    await notebookStore.fetchEntries()
    console.log('[NotebookView] Loaded', grouped.value.length, 'stop groups,', generalNotes.value.length, 'general notes')
  } catch (err) {
    console.error('[NotebookView] Failed to load:', err)
    error.value = 'Kunne ikke laste notatblokk. Prøv igjen.'
  } finally {
    loading.value = false
  }
}

function startEdit(stopId) {
  editingStop.value = stopId
  saveError.value = null
}

function cancelEdit() {
  editingStop.value = null
  saveError.value = null
}

function startAddNote() {
  addingNote.value = true
  saveError.value = null
}

function cancelAddNote() {
  addingNote.value = false
  saveError.value = null
}

function startNoteEdit(note) {
  editingNoteId.value = note.id
  editingNoteContent.value = note.content
  saveError.value = null
}

function cancelNoteEdit() {
  editingNoteId.value = null
  editingNoteContent.value = ''
  saveError.value = null
}

function startReflectionEdit(reflection) {
  editingReflectionId.value = reflection.id
  editingReflectionContent.value = reflection.content
  saveError.value = null
}

function cancelReflectionEdit() {
  editingReflectionId.value = null
  editingReflectionContent.value = ''
  saveError.value = null
}

async function saveCurrentReflectionEdit(content) {
  const id = editingReflectionId.value
  if (!id || !content) return

  saving.value = true
  saveError.value = null
  try {
    await notebookStore.updateNote(id, content)
    cancelReflectionEdit()
  } catch (err) {
    console.error('[NotebookView] Failed to update reflection:', err)
    saveError.value = 'Kunne ikke lagre. Prøv igjen.'
  } finally {
    saving.value = false
  }
}

async function removeReflection(id) {
  try {
    await notebookStore.deleteNote(id)
  } catch (err) {
    console.error('[NotebookView] Failed to delete reflection:', err)
  }
}

async function submitCurrentObservation(content) {
  const stopId = editingStop.value
  if (!stopId || !content) return

  saving.value = true
  saveError.value = null

  try {
    await notebookStore.addReflection(stopId, content)
    cancelEdit()
  } catch (err) {
    console.error('[NotebookView] Failed to save reflection:', err)
    saveError.value = 'Kunne ikke lagre. Prøv igjen.'
  } finally {
    saving.value = false
  }
}

async function submitGeneralNote(content) {
  if (!content) return

  saving.value = true
  saveError.value = null

  try {
    await notebookStore.addGeneralNote(content)
    cancelAddNote()
  } catch (err) {
    console.error('[NotebookView] Failed to save general note:', err)
    saveError.value = 'Kunne ikke lagre. Prøv igjen.'
  } finally {
    saving.value = false
  }
}

async function saveCurrentEdit(content) {
  const id = editingNoteId.value
  if (!id || !content) return

  saving.value = true
  saveError.value = null

  try {
    await notebookStore.updateNote(id, content)
    cancelNoteEdit()
  } catch (err) {
    console.error('[NotebookView] Failed to update note:', err)
    saveError.value = 'Kunne ikke lagre. Prøv igjen.'
  } finally {
    saving.value = false
  }
}

async function removeNote(id) {
  try {
    await notebookStore.deleteNote(id)
  } catch (err) {
    console.error('[NotebookView] Failed to delete note:', err)
  }
}

function openJournal() {
  if (isOpening.value) return

  playPageTurn()
  isOpening.value = true
  openTimer = setTimeout(() => {
    showJournalOverlay.value = false
    isOpening.value = false
    openTimer = null
  }, animationDuration)
}

function queueTurn(direction) {
  if (turnState.value) return

  const target = direction === 'next' ? spreadIndex.value + 1 : spreadIndex.value - 1
  if (target < 0 || target >= spreadCount.value) return

  playPageTurn()
  clearTurnTimers()

  turnState.value = {
    direction,
    from: spreadIndex.value,
    to: target
  }

  turnMidTimer = setTimeout(() => {
    spreadIndex.value = target
    turnMidTimer = null
  }, Math.round(pageTurnDuration * 0.48))

  turnEndTimer = setTimeout(() => {
    turnState.value = null
    turnEndTimer = null
  }, pageTurnDuration)
}

function goNext() {
  if (!canGoNext.value) return
  if (isKindleMode.value) {
    kindlePageIndex.value += 1
    return
  }
  queueTurn('next')
}

function goPrev() {
  if (!canGoPrev.value) return
  if (isKindleMode.value) {
    kindlePageIndex.value -= 1
    return
  }
  queueTurn('back')
}

function handleKeydown(event) {
  if (showJournalOverlay.value || turnState.value) return

  const tagName = event.target?.tagName
  if (tagName === 'TEXTAREA' || tagName === 'INPUT' || event.target?.isContentEditable) return

  if (event.key === 'ArrowRight') {
    event.preventDefault()
    goNext()
  }

  if (event.key === 'ArrowLeft') {
    event.preventDefault()
    goPrev()
  }
}

watch(spreadCount, (nextCount) => {
  if (spreadIndex.value > nextCount - 1) {
    spreadIndex.value = Math.max(0, nextCount - 1)
  }
})

watch(pageCount, (nextCount) => {
  if (kindlePageIndex.value > nextCount - 1) {
    kindlePageIndex.value = Math.max(0, nextCount - 1)
  }
})

watch(isKindleMode, (kindle) => {
  if (kindle) {
    kindlePageIndex.value = Math.min(pageCount.value - 1, spreadIndex.value * 2)
  } else {
    spreadIndex.value = Math.min(spreadCount.value - 1, Math.floor(kindlePageIndex.value / 2))
  }
})

onMounted(() => {
  updateViewport()
  load()
  window.addEventListener('resize', updateViewport)
  window.addEventListener('keydown', handleKeydown)
  // Auto-play the book opening animation as soon as the view is mounted so
  // pupils don't need to click anything to enter the journal.
  autoOpenTimer = setTimeout(() => {
    openJournal()
    autoOpenTimer = null
  }, openHoldMs)
})

onBeforeUnmount(() => {
  clearOpenTimer()
  clearTurnTimers()
  window.removeEventListener('resize', updateViewport)
  window.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped>
.journal-reader {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  min-height: calc(100dvh - 48px - (1.5 * var(--space-6)));
  max-height: 100dvh;
  width: 100%;
  padding: 0 0.75rem 0.75rem;
  box-sizing: border-box;
  opacity: 0;
  transform: scale(0.985) translateY(18px);
  transition: opacity 420ms ease, transform 420ms ease;
  overflow: hidden;
}

.journal-reader.is-revealed {
  opacity: 1;
  transform: none;
}

.journal-reader__stage-shell {
  flex: 1;
  min-height: 0;
  width: 100%;
  padding: 0 0.85rem;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.journal-state {
  width: min(100%, 1280px);
  min-height: 16rem;
  margin: auto;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: var(--space-3);
  text-align: center;
  color: var(--color-journal-parchment);
  background: linear-gradient(145deg, rgba(60, 33, 17, 0.85), rgba(31, 15, 8, 0.88));
  border: 1px solid color-mix(in srgb, var(--color-journal-highlight) 14%, transparent);
  border-radius: 28px;
  box-shadow: 0 24px 48px rgba(0, 0, 0, 0.26);
}

.journal-state--error {
  color: var(--color-journal-error-soft);
}

.journal-state__spinner {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  border: 3px solid rgba(255, 255, 255, 0.16);
  border-top-color: var(--color-journal-highlight);
  animation: spin 0.8s linear infinite;
}

.journal-state__retry {
  padding: 0.7rem 1.1rem;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, var(--color-journal-retry-top), var(--color-journal-retry-bottom));
  color: var(--color-journal-parchment-light);
  font-weight: 700;
  cursor: pointer;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.journal-bookstage {
  --paper-gap: 12px;
  position: relative;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0;
  max-width: 100%;
  padding: 18px;
  perspective: 2800px;
  border-radius: 34px;
  background:
    radial-gradient(circle at 20% 18%, rgba(255, 222, 167, 0.09), transparent 22%),
    radial-gradient(circle at 82% 12%, rgba(184, 69, 31, 0.14), transparent 18%),
    linear-gradient(160deg, rgba(56, 28, 13, 0.96), rgba(24, 12, 6, 0.98));
  box-shadow:
    inset 0 1px 0 rgba(255, 238, 210, 0.08),
    0 32px 56px rgba(0, 0, 0, 0.34);
}

.journal-bookstage--kindle {
  grid-template-columns: 1fr;
  perspective: none;
  padding: 14px;
  border-radius: 26px;
}

.journal-paper--single {
  margin: 0;
  border-radius: 18px;
  overflow: hidden;
  min-width: 0;
}

.journal-paper--single :deep(.journal-page-content) {
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: rgba(124, 85, 33, 0.4) transparent;
}

.journal-paper--single :deep(.journal-page-content)::-webkit-scrollbar {
  width: 8px;
}

.journal-paper--single :deep(.journal-page-content)::-webkit-scrollbar-thumb {
  background: rgba(124, 85, 33, 0.35);
  border-radius: 999px;
}

.journal-paper--single :deep(.journal-page-content__blocks) {
  flex: 0 0 auto;
  overflow: visible;
}

.journal-paper--single :deep(.journal-entry-card) {
  flex: 0 0 auto;
  max-height: none;
}

.journal-paper--single :deep(.journal-entry-card__scroll) {
  overflow: visible;
  padding-right: 0;
}

.journal-bookstage__shadow {
  position: absolute;
  inset: auto 10% -16px;
  height: 30px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(0, 0, 0, 0.36), transparent 72%);
  filter: blur(8px);
}

.journal-bookstage__spine {
  position: absolute;
  top: 18px;
  bottom: 18px;
  left: 50%;
  width: 28px;
  transform: translateX(-50%);
  border-radius: 999px;
  background:
    linear-gradient(180deg, rgba(253, 214, 152, 0.16), transparent 16%),
    linear-gradient(180deg, var(--color-journal-spine-top), var(--color-journal-spine-mid) 28%, var(--color-journal-spine-dark) 72%, var(--color-journal-spine-edge));
  box-shadow:
    inset 0 0 0 1px rgba(255, 233, 190, 0.08),
    0 10px 24px rgba(0, 0, 0, 0.22);
  z-index: 2;
}

.journal-paper {
  position: relative;
  z-index: 1;
  min-width: 0;
  min-height: 100%;
  overflow: hidden;
  display: flex;
  border: 1px solid color-mix(in srgb, var(--color-journal-leather-light) 20%, transparent);
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.18), transparent 8%),
    linear-gradient(180deg, var(--color-journal-paper-top), var(--color-journal-paper-mid) 46%, var(--color-journal-paper-bottom));
  box-shadow:
    inset 0 0 0 1px rgba(255, 255, 255, 0.22),
    0 12px 24px rgba(84, 58, 26, 0.1);
}

.journal-paper > * {
  width: 100%;
  height: 100%;
}

.journal-paper--left {
  margin-right: var(--paper-gap);
  border-radius: 22px 10px 10px 24px;
}

.journal-paper--right {
  margin-left: var(--paper-gap);
  border-radius: 10px 24px 24px 10px;
}

.journal-paper::before {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    radial-gradient(circle at 12% 16%, rgba(184, 129, 58, 0.18), transparent 18%),
    radial-gradient(circle at 88% 84%, rgba(135, 83, 27, 0.12), transparent 16%);
  opacity: 0.85;
}

.journal-paper::after {
  content: attr(data-page);
  position: absolute;
  bottom: 14px;
  font-size: 0.8rem;
  letter-spacing: 0.1em;
  color: color-mix(in srgb, var(--color-journal-ink) 72%, transparent);
}

.journal-paper--left::after {
  left: 22px;
}

.journal-paper--right::after {
  right: 22px;
}

.journal-paper--single::after {
  left: 4%;
  transform: translateX(-50%);
}

.journal-leaf {
  position: absolute;
  top: 18px;
  bottom: 18px;
  width: calc(50% - var(--paper-gap));
  transform-style: preserve-3d;
  pointer-events: none;
  z-index: 5;
  will-change: transform;
}

.journal-leaf::after {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  border-radius: 18px;
  background: linear-gradient(90deg, rgba(44, 23, 9, 0.32), transparent 38%, rgba(32, 17, 8, 0.32));
  opacity: 0;
  animation: leaf-shade var(--turn-ms) ease-in-out forwards;
}

.journal-leaf--next {
  right: 18px;
  transform-origin: left center;
  animation: turn-next var(--turn-ms) cubic-bezier(0.45, 0.05, 0.25, 1) forwards;
}

.journal-leaf--back {
  left: 18px;
  transform-origin: right center;
  animation: turn-back var(--turn-ms) cubic-bezier(0.45, 0.05, 0.25, 1) forwards;
}

.journal-paper--leaf-face {
  position: absolute;
  inset: 0;
  margin: 0;
  backface-visibility: hidden;
  transform-style: preserve-3d;
  border-radius: 10px 24px 24px 10px;
}

.journal-leaf--back .journal-paper--leaf-face {
  border-radius: 24px 10px 10px 24px;
}

.journal-paper--leaf-back {
  transform: rotateY(180deg);
}

.journal-paper--leaf-front::after,
.journal-paper--leaf-back::after {
  right: 22px;
  left: auto;
}

.journal-leaf--back .journal-paper--leaf-front::after,
.journal-leaf--back .journal-paper--leaf-back::after {
  left: 22px;
  right: auto;
}

@keyframes turn-next {
  0% {
    transform: rotateY(0deg) translateZ(0);
    box-shadow: 0 6px 14px rgba(0, 0, 0, 0.1);
  }

  25% {
    transform: rotateY(-46deg) translateZ(20px);
    box-shadow: -8px 16px 24px rgba(0, 0, 0, 0.22);
  }

  50% {
    transform: rotateY(-92deg) translateZ(28px);
    box-shadow: 0 24px 36px rgba(0, 0, 0, 0.28);
  }

  75% {
    transform: rotateY(-138deg) translateZ(20px);
    box-shadow: 8px 16px 24px rgba(0, 0, 0, 0.22);
  }

  100% {
    transform: rotateY(-180deg) translateZ(0);
    box-shadow: 0 6px 14px rgba(0, 0, 0, 0.08);
  }
}

@keyframes turn-back {
  0% {
    transform: rotateY(0deg) translateZ(0);
    box-shadow: 0 6px 14px rgba(0, 0, 0, 0.1);
  }

  25% {
    transform: rotateY(46deg) translateZ(20px);
    box-shadow: 8px 16px 24px rgba(0, 0, 0, 0.22);
  }

  50% {
    transform: rotateY(92deg) translateZ(28px);
    box-shadow: 0 24px 36px rgba(0, 0, 0, 0.28);
  }

  75% {
    transform: rotateY(138deg) translateZ(20px);
    box-shadow: -8px 16px 24px rgba(0, 0, 0, 0.22);
  }

  100% {
    transform: rotateY(180deg) translateZ(0);
    box-shadow: 0 6px 14px rgba(0, 0, 0, 0.08);
  }
}

@keyframes leaf-shade {
  0%, 100% { opacity: 0; }
  50% { opacity: 0.85; }
}

/* Arrow controls placed to left/right of the book stage */
.journal-nav__arrow {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 999px;
  border: 1px solid color-mix(in srgb, var(--color-journal-highlight) 12%, transparent);
  background: linear-gradient(135deg, rgba(112, 64, 30, 0.9), rgba(62, 32, 15, 0.96));
  color: var(--color-journal-parchment-bright);
  cursor: pointer;
  font-weight: 700;
  transition: transform 160ms ease, box-shadow 160ms ease, opacity 160ms ease;
}

.journal-nav__arrow:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

@media (max-width: 640px) {
  .journal-nav__arrow {
    width: 44px;
    height: 44px;
  }
}

.journal-overlay {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 60;
  background: radial-gradient(circle at center, rgba(14, 7, 4, 0.62), rgba(12, 6, 2, 0.2));
}

.journal-cover-stage {
  position: relative;
  width: calc(var(--cover-width) * 2);
  height: var(--cover-height);
  perspective: 2400px;
  transform-style: preserve-3d;
}

.journal-cover-stage__spread {
  position: absolute;
  inset: 0;
  transform: translateX(-20%) scale(0.94);
  transform-style: preserve-3d;
  transition: transform var(--anim-ms) cubic-bezier(0.2, 0.86, 0.24, 1);
}

.journal-cover-stage__inside,
.journal-cover-stage__pages {
  position: absolute;
  top: 0;
  bottom: 0;
  width: 50%;
  opacity: 0;
  border-radius: 14px;
  transition:
    opacity calc(var(--anim-ms) * 0.3) ease,
    transform var(--anim-ms) cubic-bezier(0.2, 0.86, 0.24, 1);
}

.journal-cover-stage__inside {
  left: 0;
  background:
    linear-gradient(145deg, var(--color-journal-cover-inside-top), var(--color-journal-cover-inside-mid) 58%, var(--color-journal-cover-inside-bottom)),
    radial-gradient(circle at top left, rgba(255, 255, 255, 0.14), transparent 32%);
  box-shadow: inset -18px 0 26px rgba(30, 13, 6, 0.22);
  transform: rotateY(6deg) scaleX(0.96);
}

.journal-cover-stage__pages {
  right: 0;
  overflow: hidden;
  background:
    linear-gradient(90deg, rgba(204, 182, 137, 0.44), rgba(255, 249, 235, 0.12) 8%, transparent 16%),
    linear-gradient(180deg, var(--color-journal-pages-top), var(--color-journal-pages-bottom));
  box-shadow:
    inset 0 0 0 1px rgba(133, 95, 43, 0.12),
    inset -26px 0 36px rgba(167, 140, 90, 0.16);
  transform: rotateY(-6deg) scaleX(0.98);
}

.journal-cover-stage__spine {
  position: absolute;
  top: 10px;
  bottom: 10px;
  left: 50%;
  width: 16px;
  transform: translateX(-50%);
  border-radius: 999px;
  opacity: 0;
  background: linear-gradient(180deg, var(--color-journal-spine-cover-top), var(--color-journal-spine-cover-mid) 42%, var(--color-journal-spine-cover-dark));
  transition: opacity calc(var(--anim-ms) * 0.26) ease;
}

.journal-cover {
  position: absolute;
  top: 0;
  right: 0;
  width: 50%;
  height: 100%;
  padding: clamp(22px, 4vw, 36px);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: flex-start;
  border: none;
  border-radius: 18px;
  color: var(--color-journal-parchment-warm);
  text-align: left;
  transform-origin: left center;
  transform-style: preserve-3d;
  backface-visibility: hidden;
  background:
    radial-gradient(circle at top left, rgba(255, 255, 255, 0.2), transparent 26%),
    linear-gradient(145deg, var(--color-journal-leather-light), var(--color-journal-leather-mid) 58%, var(--color-journal-leather-dark));
  box-shadow:
    inset 0 0 0 1px rgba(255, 244, 221, 0.1),
    inset 12px 0 22px rgba(255, 209, 145, 0.06),
    0 18px 32px rgba(0, 0, 0, 0.22);
  transition:
    transform var(--anim-ms) cubic-bezier(0.17, 0.88, 0.24, 1),
    box-shadow calc(var(--anim-ms) * 0.45) ease;
}

.journal-cover::before {
  content: '';
  position: absolute;
  inset: 14px;
  border: 1px solid rgba(246, 223, 181, 0.34);
  border-radius: 12px;
}

.journal-cover__sigil,
.journal-cover__badge,
.journal-cover__title,
.journal-cover__hint {
  position: relative;
  z-index: 1;
  transform: translateZ(34px);
}

.journal-cover__sigil {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 2px solid rgba(250, 225, 173, 0.38);
  box-shadow: inset 0 0 0 1px rgba(255, 247, 232, 0.12);
}

.journal-cover__sigil::before {
  content: '';
  width: 32px;
  height: 32px;
  border: 2px solid rgba(252, 234, 197, 0.86);
  transform: rotate(45deg);
}

.journal-cover__sigil-eye {
  position: absolute;
  width: 16px;
  height: 8px;
  border-radius: 999px;
  border: 2px solid rgba(252, 234, 197, 0.9);
}

.journal-cover__sigil-eye::before {
  content: '';
  position: absolute;
  inset: 1px;
  margin: auto;
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: rgba(252, 234, 197, 0.96);
}

.journal-cover__badge {
  font-size: 0.88rem;
  letter-spacing: 0.22em;
  text-transform: uppercase;
  color: color-mix(in srgb, var(--color-journal-parchment-warm) 74%, transparent);
}

.journal-cover__title {
  align-self: center;
  max-width: 8ch;
  font-size: clamp(1.5rem, 4vw, 2.2rem);
  line-height: 1.05;
  letter-spacing: 0.16em;
  font-weight: 900;
}

.journal-cover__hint {
  font-size: 1rem;
  color: color-mix(in srgb, var(--color-journal-parchment-warm) 92%, transparent);
}

.journal-cover-stage.is-opening .journal-cover-stage__spread {
  transform: translateX(0) scale(1);
}

.journal-cover-stage.is-opening .journal-cover-stage__inside,
.journal-cover-stage.is-opening .journal-cover-stage__pages,
.journal-cover-stage.is-opening .journal-cover-stage__spine {
  opacity: 1;
}

.journal-cover-stage.is-opening .journal-cover-stage__inside,
.journal-cover-stage.is-opening .journal-cover-stage__pages {
  transform: rotateY(0deg) scaleX(1);
}

.journal-cover-stage.is-opening .journal-cover {
  transform: rotateY(-165deg);
  box-shadow:
    inset 0 0 0 1px rgba(255, 255, 255, 0.12),
    18px 16px 28px rgba(0, 0, 0, 0.18);
}

@media (max-width: 640px) {
  .journal-reader {
    gap: var(--space-3);
  }

  .journal-reader__stage-shell {
    padding: 0 0.35rem;
    gap: 0.25rem;
  }

  .journal-bookstage {
    --paper-gap: 6px;
    padding: 10px;
    min-height: auto;
    border-radius: 22px;
    box-shadow:
      inset 0 1px 0 rgba(255, 238, 210, 0.08),
      0 18px 32px rgba(0, 0, 0, 0.3);
  }

  .journal-bookstage__spine {
    top: 10px;
    bottom: 10px;
    width: 18px;
  }

  .journal-paper--left {
    border-radius: 14px 8px 8px 16px;
  }

  .journal-paper--right {
    border-radius: 8px 16px 16px 8px;
  }

  .journal-paper--leaf-face {
    border-radius: 8px 16px 16px 8px;
  }

  .journal-leaf--back .journal-paper--leaf-face {
    border-radius: 16px 8px 8px 16px;
  }

  .journal-paper--left::after,
  .journal-leaf--back .journal-paper--leaf-front::after,
  .journal-leaf--back .journal-paper--leaf-back::after {
    left: 16px;
  }

  .journal-paper--right::after,
  .journal-paper--leaf-front::after,
  .journal-paper--leaf-back::after {
    right: 16px;
  }

  .journal-leaf {
    top: 10px;
    bottom: 10px;
  }

  .journal-leaf--next {
    right: 10px;
  }

  .journal-leaf--back {
    left: 10px;
  }

  .journal-cover-stage {
    perspective: 1600px;
  }

  .journal-cover {
    padding: clamp(14px, 4vw, 22px);
  }
}

@media (max-width: 420px) {
  .journal-reader__stage-shell {
    padding: 0 0.15rem;
  }

  .journal-nav__arrow {
    width: 38px;
    height: 38px;
  }

  .journal-bookstage {
    --paper-gap: 4px;
    padding: 8px;
    border-radius: 18px;
  }

  .journal-bookstage__spine {
    width: 14px;
    top: 8px;
    bottom: 8px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .journal-reader,
  .journal-cover-stage__spread,
  .journal-cover-stage__inside,
  .journal-cover-stage__pages,
  .journal-cover-stage__spine,
  .journal-cover,
  .journal-leaf,
  .journal-state__spinner {
    transition-duration: 1ms !important;
    animation-duration: 1ms !important;
  }
}
</style>

<style>
body:has(.journal-reader) {
  overflow: hidden;
}
</style>
