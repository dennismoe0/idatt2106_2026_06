<template>
  <CorkBoardPage page-title="Notatblokk" :back-to="{ name: 'Home' }">
    <div v-if="showJournalOverlay" class="journal-overlay" role="dialog" aria-label="Notatblokk">
      <div class="journal-cover-stage" :class="{ 'is-opening': isOpening }" :style="coverStageStyle">
        <div class="journal-cover-stage__spread" aria-hidden="true">
          <div class="journal-cover-stage__inside"></div>
          <div class="journal-cover-stage__pages"></div>
          <div class="journal-cover-stage__spine"></div>
        </div>

        <button class="journal-cover" type="button" @click="openJournal" aria-label="Åpne notatblokk">
          <span class="journal-cover__sigil" aria-hidden="true">
            <span class="journal-cover__sigil-eye"></span>
          </span>
          <span class="journal-cover__badge">Arkiv 03</span>
          <span class="journal-cover__title">MYSTISK JOURNAL</span>
          <span class="journal-cover__hint">Trykk for å åpne</span>
        </button>
      </div>
    </div>

    <div class="journal-reader" :class="{ 'is-revealed': !showJournalOverlay }">
      <div class="journal-reader__toolbar">
        <!-- spread status removed per request -->
      </div>

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

          <div class="journal-bookstage" :style="bookStageStyle">
            <div class="journal-bookstage__shadow" aria-hidden="true"></div>
            <div class="journal-bookstage__spine" aria-hidden="true"></div>

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
    <BaseModal
      :model-value="editingReflectionId !== null"
      title="Rediger observasjon"
      @update:modelValue="handleReflectionEditModalToggle"
    >
      <div class="journal-modal">
        <textarea
          v-model="editReflectionContent"
          class="journal-modal__textarea"
          rows="6"
          maxlength="500"
          aria-label="Rediger observasjon"
          autofocus
        />
        <p class="journal-modal__count">{{ editReflectionContent.length }}/500</p>
        <div class="journal-modal__actions">
          <button
            class="journal-modal__button journal-modal__button--primary"
            type="button"
            :disabled="!editReflectionContent.trim() || saving"
            @click="saveCurrentReflectionEdit"
          >
            {{ saving ? 'Lagrer...' : 'Lagre' }}
          </button>
          <button class="journal-modal__button" type="button" @click="cancelReflectionEdit">
            Avbryt
          </button>
        </div>
        <p v-if="saveError" class="journal-modal__error" role="alert">{{ saveError }}</p>
      </div>
    </BaseModal>

            <div v-if="turnLeaf" class="journal-leaf" :class="`journal-leaf--${turnLeaf.direction}`" aria-hidden="true">
              <article class="journal-paper journal-paper--leaf-face journal-paper--leaf-front" :data-page="turnLeaf.front.number">
                <NotebookJournalPage :page="turnLeaf.front" :side="turnLeaf.frontSide" />
              </article>
              <article class="journal-paper journal-paper--leaf-face journal-paper--leaf-back" :data-page="turnLeaf.back.number">
                <NotebookJournalPage :page="turnLeaf.back" :side="turnLeaf.backSide" />
              </article>
            </div>
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

        <div class="journal-nav">
          <div class="journal-nav__meta">
            <!-- Page label removed per design request -->
          </div>
        </div>
      </template>
    </div>

    <BaseModal :model-value="editingStop !== null" title="Ny observasjon" @update:modelValue="handleObservationModalToggle">
      <div class="journal-modal">
        <textarea
          v-model="newContent"
          class="journal-modal__textarea"
          rows="6"
          maxlength="500"
          aria-label="Ny observasjon"
          autofocus
        />
        <p class="journal-modal__count">{{ newContent.length }}/500</p>
        <div class="journal-modal__actions">
          <button class="journal-modal__button journal-modal__button--primary" type="button" :disabled="!newContent.trim() || saving" @click="submitCurrentObservation">
            {{ saving ? 'Lagrer...' : 'Lagre' }}
          </button>
          <button class="journal-modal__button" type="button" @click="cancelEdit">Avbryt</button>
        </div>
        <p v-if="saveError" class="journal-modal__error" role="alert">{{ saveError }}</p>
      </div>
    </BaseModal>

    <BaseModal :model-value="addingNote" title="Nytt notat" @update:modelValue="handleGeneralNoteModalToggle">
      <div class="journal-modal">
        <textarea
          v-model="newNoteContent"
          class="journal-modal__textarea"
          rows="6"
          maxlength="500"
          aria-label="Nytt generelt notat"
          autofocus
        />
        <p class="journal-modal__count">{{ newNoteContent.length }}/500</p>
        <div class="journal-modal__actions">
          <button class="journal-modal__button journal-modal__button--primary" type="button" :disabled="!newNoteContent.trim() || saving" @click="submitGeneralNote">
            {{ saving ? 'Lagrer...' : 'Lagre' }}
          </button>
          <button class="journal-modal__button" type="button" @click="cancelAddNote">Avbryt</button>
        </div>
        <p v-if="saveError" class="journal-modal__error" role="alert">{{ saveError }}</p>
      </div>
    </BaseModal>

    <BaseModal :model-value="editingNoteId !== null" title="Rediger notat" @update:modelValue="handleEditNoteModalToggle">
      <div class="journal-modal">
        <textarea
          v-model="editContent"
          class="journal-modal__textarea"
          rows="6"
          maxlength="500"
          aria-label="Rediger notat"
          autofocus
        />
        <p class="journal-modal__count">{{ editContent.length }}/500</p>
        <div class="journal-modal__actions">
          <button class="journal-modal__button journal-modal__button--primary" type="button" :disabled="!editContent.trim() || saving" @click="saveCurrentEdit">
            {{ saving ? 'Lagrer...' : 'Lagre' }}
          </button>
          <button class="journal-modal__button" type="button" @click="cancelNoteEdit">Avbryt</button>
        </div>
        <p v-if="saveError" class="journal-modal__error" role="alert">{{ saveError }}</p>
      </div>
    </BaseModal>
  </CorkBoardPage>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import BaseModal from '@/components/common/BaseModal.vue'
import CorkBoardPage from '@/components/common/CorkBoardPage.vue'
import NotebookJournalPage from '@/components/student/NotebookJournalPage.vue'
import { useSound } from '@/composables/useSound'
import { useNotebookStore } from '@/stores/notebook'

const notebookStore = useNotebookStore()
const { playPageTurn } = useSound()

const grouped = computed(() => notebookStore.grouped)
const generalNotes = computed(() => notebookStore.generalNotes)

const loading = ref(true)
const error = ref(null)
const saving = ref(false)
const saveError = ref(null)

const editingStop = ref(null)
const newContent = ref('')
const addingNote = ref(false)
const newNoteContent = ref('')
const editingNoteId = ref(null)
const editContent = ref('')
const editingReflectionId = ref(null)
const editReflectionContent = ref('')

const showJournalOverlay = ref(true)
const isOpening = ref(false)
const animationDuration = 980
const pageTurnDuration = 820
const spreadIndex = ref(0)
const turnState = ref(null)
const viewport = ref({ width: 1280, height: 900 })

const RESERVED_REPORT_COUNT = 7
const BOOK_ASPECT_RATIO = 1.58
const BOOK_WIDTH_SCALE = 1.25
const RESERVED_REPORT_TITLES = {
  1: 'Et spor i nyhetsstrømmen',
  2: 'Ukjent avsender',
  3: 'Bildet lyver',
  4: 'Passordlekkasje',
  5: 'Svindel på nett',
  6: 'Falsk venn',
  7: 'Datasenteret er hacket'
}

let openTimer = null
let turnMidTimer = null
let turnEndTimer = null

function normalizeText(value) {
  return String(value ?? '')
    .replace(/\r\n?/g, '\n')
    .trim()
}

function estimateTextUnits(text, charsPerUnit = 74) {
  const normalized = normalizeText(text)
  if (!normalized) return 1

  const lineBreaks = (normalized.match(/\n/g) || []).length
  return Math.max(1, Math.ceil(normalized.length / charsPerUnit) + lineBreaks)
}

function splitTextIntoChunks(text, maxChars) {
  const normalized = normalizeText(text)
  if (!normalized) return ['']

  const tokens = normalized.split(/(\s+)/).filter(Boolean)
  const chunks = []
  let current = ''

  for (const token of tokens) {
    const isWhitespace = /^\s+$/.test(token)

    if (!isWhitespace && token.length > maxChars) {
      if (current.trim()) {
        chunks.push(current.trim())
        current = ''
      }

      for (let index = 0; index < token.length; index += maxChars) {
        chunks.push(token.slice(index, index + maxChars))
      }
      continue
    }

    const candidate = `${current}${token}`
    if (candidate.trim().length > maxChars && current.trim()) {
      chunks.push(current.trim())
      current = isWhitespace ? '' : token.trimStart()
    } else {
      current = candidate
    }
  }

  if (current.trim()) {
    chunks.push(current.trim())
  }

  return chunks
}

function packBlocks(blocks, capacity) {
  if (!blocks.length) return [[]]

  const pages = []
  let currentPage = []
  let usedUnits = 0

  for (const block of blocks) {
    const blockUnits = block.units ?? 1
    if (currentPage.length && usedUnits + blockUnits > capacity) {
      pages.push(currentPage)
      currentPage = []
      usedUnits = 0
    }

    currentPage.push(block)
    usedUnits += blockUnits
  }

  if (currentPage.length) {
    pages.push(currentPage)
  }

  return pages
}

function createBlankSeed(key = 'blank-tail') {
  return {
    key,
    kind: 'blank',
    eyebrow: 'Tom side',
    title: 'Neste spor kommer snart',
    subtitle: 'La noen linjer stå åpne til neste mysterium.',
    partLabel: null,
    doodle: '?? // blekk // røde tråder'
  }
}

function createBlankPage(pageIndex) {
  return {
    ...createBlankSeed(`blank-${pageIndex}`),
    number: pageIndex + 2
  }
}

function buildTipBlocks(autoTip) {
  return [
    {
      key: `tip-${autoTip.id}`,
      type: 'tip',
      label: 'Låst opp rapport',
      content: autoTip.content,
      createdAt: autoTip.createdAt,
      units: Math.min(11, 3 + estimateTextUnits(autoTip.content, 76))
    }
  ]
}

function buildReflectionBlocks(reflection) {
  return [
    {
      key: `reflection-${reflection.id}`,
      id: reflection.id,
      type: 'reflection',
      label: 'Observasjon',
      content: reflection.content,
      createdAt: reflection.createdAt,
      units: Math.min(11, 3 + estimateTextUnits(reflection.content, 72)),
      showControls: true,
      source: reflection
    }
  ]
}
function buildGeneralNoteBlocks(note) {
  return [
    {
      key: `general-note-${note.id}`,
      id: note.id,
      type: 'note',
      label: 'Notat',
      content: note.content,
      createdAt: note.createdAt,
      units: Math.min(11, 3 + estimateTextUnits(note.content, 68)),
      showControls: true,
      source: note
    }
  ]
}

function buildLevelPagesForGroup(stopOrder, group) {
  const title =
    group?.stopName ||
    RESERVED_REPORT_TITLES[stopOrder] ||
    `Oppdrag ${stopOrder}`

  const blocks = []

  // 1. Level note (autoTip) first — shown at the top of each level section
  if (group?.autoTip) {
    blocks.push(...buildTipBlocks(group.autoTip))
  }

  // 2. User's own observations for this level
  for (const reflection of group?.reflections ?? []) {
    blocks.push(...buildReflectionBlocks(reflection))
  }

  // 3. Empty state
  if (!blocks.length) {
    blocks.push({
      key: `level-empty-${stopOrder}`,
      type: 'empty',
      content: group
        ? 'Ingen observasjoner ennå. Skriv ned rare detaljer før de forsvinner.'
        : 'Fullfør dette nivået for å låse opp rapporten og legge til observasjoner.',
      units: 5
    })
  }

  const pages = packBlocks(blocks, 12)
  const stopId = group?.stopId ?? null

  return pages.map((pageBlocks, index) => ({
    key: `level-${stopOrder}-${index}`,
    kind: 'stop',
    eyebrow: group?.autoTip
      ? 'Fullført nivå'
      : group
        ? 'Feltnotater'
        : 'Reservert rapportside',
    title,
    subtitle: `Nivå ${stopOrder}`,
    partLabel: pages.length > 1 ? `Del ${index + 1} av ${pages.length}` : null,
    blocks: pageBlocks,
    showComposer: stopId !== null && index === pages.length - 1,
    stopId,
    doodle: group?.autoTip
      ? `△ // spor // nivå ${stopOrder}`
      : `låst // oppdrag ${stopOrder} // venter`
  }))
}

function buildGeneralPages(notes) {
  const blocks = []

  const orderedNotes = [...notes].sort((a, b) => {
    const aTime = a?.createdAt ? new Date(a.createdAt).getTime() : 0
    const bTime = b?.createdAt ? new Date(b.createdAt).getTime() : 0
    return aTime - bTime
  })

  for (const note of orderedNotes) {
    blocks.push(...buildGeneralNoteBlocks(note))
  }

  if (!blocks.length) {
    blocks.push({
      key: 'general-empty',
      type: 'empty',
      content: 'Ingen frie notater ennå. Skriv den første teorien din her.',
      units: 5
    })
  }

  const pages = packBlocks(blocks, 11)

  return pages.map((pageBlocks, index) => ({
    key: `general-${index}`,
    kind: 'general',
    eyebrow: 'Frie notater',
    title: 'Løse tråder og raske tanker',
    subtitle: 'Her samler du alt som ikke passer andre steder.',
    partLabel: pages.length > 1 ? `Del ${index + 1} av ${pages.length}` : null,
    blocks: pageBlocks,
    showComposer: index === pages.length - 1,
  }))
}

// ADD this:
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
const canGoPrev = computed(() => spreadIndex.value > 0 && !turnState.value)
const canGoNext = computed(() => spreadIndex.value < spreadCount.value - 1 && !turnState.value)
const isCompactLayout = computed(() => viewport.value.width <= 900)

const bookHeight = computed(() => {
  const chromeReserve = isCompactLayout.value ? 282 : 214
  const availableHeight = viewport.value.height - chromeReserve
  const availableWidth = viewport.value.width - (viewport.value.width <= 640 ? 32 : 72)
  return Math.round(Math.min(720, Math.max(220, Math.min(availableHeight, availableWidth / BOOK_ASPECT_RATIO))))
})

const bookWidth = computed(() => Math.round(bookHeight.value * BOOK_ASPECT_RATIO * BOOK_WIDTH_SCALE))
const bookStageStyle = computed(() => ({
  width: `${bookWidth.value}px`,
  height: `${bookHeight.value}px`,
  '--turn-ms': `${pageTurnDuration}ms`
}))

const coverHeight = computed(() => {
  const availableHeight = viewport.value.height - 150
  const availableWidth = viewport.value.width - 96
  return Math.round(Math.min(470, Math.max(260, Math.min(availableHeight, availableWidth / 1.56))))
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
  } catch (err) {
    console.error('[NotebookView] Failed to load:', err)
    error.value = 'Kunne ikke laste notatblokk. Prøv igjen.'
  } finally {
    loading.value = false
  }
}

function startEdit(stopId) {
  editingStop.value = stopId
  newContent.value = ''
  saveError.value = null
}

function cancelEdit() {
  editingStop.value = null
  newContent.value = ''
  saveError.value = null
}

function startAddNote() {
  addingNote.value = true
  newNoteContent.value = ''
  saveError.value = null
}

function cancelAddNote() {
  addingNote.value = false
  newNoteContent.value = ''
  saveError.value = null
}

function startNoteEdit(note) {
  editingNoteId.value = note.id
  editContent.value = note.content
  saveError.value = null
}

function cancelNoteEdit() {
  editingNoteId.value = null
  editContent.value = ''
  saveError.value = null
}

// ADD after cancelNoteEdit():

function startReflectionEdit(reflection) {
  editingReflectionId.value = reflection.id
  editReflectionContent.value = reflection.content
  saveError.value = null
}

function cancelReflectionEdit() {
  editingReflectionId.value = null
  editReflectionContent.value = ''
  saveError.value = null
}

async function saveCurrentReflectionEdit() {
  const id = editingReflectionId.value
  if (!id) return
  const content = editReflectionContent.value.trim()
  if (!content) return

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

function handleReflectionEditModalToggle(nextValue) {
  if (!nextValue) cancelReflectionEdit()
}

async function submitReflection(stopId) {
  const content = newContent.value.trim()
  if (!content) return

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

async function submitCurrentObservation() {
  if (!editingStop.value) return
  await submitReflection(editingStop.value)
}

async function submitGeneralNote() {
  const content = newNoteContent.value.trim()
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

async function saveEdit(id) {
  const content = editContent.value.trim()
  if (!content) return

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

async function saveCurrentEdit() {
  if (!editingNoteId.value) return
  await saveEdit(editingNoteId.value)
}

async function removeNote(id) {
  try {
    await notebookStore.deleteNote(id)
  } catch (err) {
    console.error('[NotebookView] Failed to delete note:', err)
  }
}

function handleObservationModalToggle(nextValue) {
  if (!nextValue) cancelEdit()
}

function handleGeneralNoteModalToggle(nextValue) {
  if (!nextValue) cancelAddNote()
}

function handleEditNoteModalToggle(nextValue) {
  if (!nextValue) cancelNoteEdit()
}

function openJournal() {
  if (isOpening.value) return

  playPageTurn()
  isOpening.value = true
  clearOpenTimer()
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
  queueTurn('next')
}

function goPrev() {
  if (!canGoPrev.value) return
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

onMounted(() => {
  updateViewport()
  load()
  window.addEventListener('resize', updateViewport)
  window.addEventListener('keydown', handleKeydown)
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

.journal-reader__toolbar {
  width: min(100%, 1320px);
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-4);
  color: #f6ecd9;
}

.journal-reader__status {
  margin: 0;
  padding: 0.58rem 0.86rem;
  border-radius: 999px;
  font-size: 0.84rem;
  color: #ffe9bc;
  background: rgba(55, 29, 14, 0.65);
  border: 1px solid rgba(255, 226, 170, 0.18);
  white-space: nowrap;
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
  color: #f9efd9;
  background: linear-gradient(145deg, rgba(60, 33, 17, 0.85), rgba(31, 15, 8, 0.88));
  border: 1px solid rgba(255, 227, 166, 0.14);
  border-radius: 28px;
  box-shadow: 0 24px 48px rgba(0, 0, 0, 0.26);
}

.journal-state--error {
  color: #ffd4cc;
}

.journal-state__spinner {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  border: 3px solid rgba(255, 255, 255, 0.16);
  border-top-color: #ffe1a6;
  animation: spin 0.8s linear infinite;
}

.journal-state__retry {
  padding: 0.7rem 1.1rem;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, #7c4a24, #5a2f17);
  color: #ffefca;
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
  width: min(100%, 1320px);
  max-width: 100%;
  min-height: 80vh;
  height: min(84vh, 980px);
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
    linear-gradient(180deg, #3f2110, #683918 28%, #2e170b 72%, #71411f);
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
  border: 1px solid rgba(124, 88, 38, 0.2);
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.18), transparent 8%),
    linear-gradient(180deg, #f5ead2, #ecdcb9 46%, #e8d5af);
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
  color: rgba(91, 63, 31, 0.72);
}

.journal-paper--left::after {
  left: 22px;
}

.journal-paper--right::after {
  right: 22px;
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
  border-radius: inherit;
  background: linear-gradient(90deg, rgba(44, 23, 9, 0.24), transparent 36%, rgba(32, 17, 8, 0.28));
  opacity: 0.5;
}

.journal-leaf--next {
  right: 18px;
  transform-origin: left center;
  animation: turn-next var(--turn-ms) cubic-bezier(0.2, 0.78, 0.2, 1) forwards;
}

.journal-leaf--back {
  left: 18px;
  transform-origin: right center;
  animation: turn-back var(--turn-ms) cubic-bezier(0.2, 0.78, 0.2, 1) forwards;
}

.journal-paper--leaf-face {
  position: absolute;
  inset: 0;
  margin: 0;
  backface-visibility: hidden;
  transform-style: preserve-3d;
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

  45% {
    transform: rotateY(-92deg) translateZ(18px);
    box-shadow: 0 18px 28px rgba(0, 0, 0, 0.22);
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

  45% {
    transform: rotateY(92deg) translateZ(18px);
    box-shadow: 0 18px 28px rgba(0, 0, 0, 0.22);
  }

  100% {
    transform: rotateY(180deg) translateZ(0);
    box-shadow: 0 6px 14px rgba(0, 0, 0, 0.08);
  }
}

.journal-nav {
  width: min(100%, 1320px);
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
}

/* Arrow controls placed to left/right of the book stage */
.journal-nav__arrow {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 999px;
  border: 1px solid rgba(255, 227, 170, 0.12);
  background: linear-gradient(135deg, rgba(112, 64, 30, 0.9), rgba(62, 32, 15, 0.96));
  color: #fff1ce;
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

.journal-nav__meta {
  min-width: 0;
  text-align: center;
  color: #f8ecd5;
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
    linear-gradient(145deg, #8a552f, #653819 58%, #43200f),
    radial-gradient(circle at top left, rgba(255, 255, 255, 0.14), transparent 32%);
  box-shadow: inset -18px 0 26px rgba(30, 13, 6, 0.22);
  transform: rotateY(6deg) scaleX(0.96);
}

.journal-cover-stage__pages {
  right: 0;
  overflow: hidden;
  background:
    linear-gradient(90deg, rgba(204, 182, 137, 0.44), rgba(255, 249, 235, 0.12) 8%, transparent 16%),
    linear-gradient(180deg, #f7efdd, #ede0c2);
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
  background: linear-gradient(180deg, #5d3518, #73421e 42%, #4a2712);
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
  cursor: pointer;
  color: #f9efd8;
  text-align: left;
  transform-origin: left center;
  transform-style: preserve-3d;
  backface-visibility: hidden;
  background:
    radial-gradient(circle at top left, rgba(255, 255, 255, 0.2), transparent 26%),
    linear-gradient(145deg, #784623, #562d17 58%, #351a0e);
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
  color: rgba(247, 233, 205, 0.74);
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
  color: rgba(255, 242, 214, 0.92);
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

.journal-modal {
  display: flex;
  flex-direction: column;
  gap: 0.65rem;
}

.journal-modal__textarea {
  width: 100%;
  box-sizing: border-box;
  resize: none;
  padding: 0.9rem 1rem;
  border-radius: 12px;
  border: 1px solid rgba(131, 92, 40, 0.28);
  background: rgba(255, 253, 247, 0.96);
  color: #382617;
  font-family: inherit;
  font-size: 0.96rem;
  line-height: 1.5;
}

.journal-modal__textarea:focus {
  outline: none;
  border-color: rgba(121, 77, 26, 0.48);
  box-shadow: 0 0 0 3px rgba(173, 127, 71, 0.12);
}

.journal-modal__count {
  margin: 0;
  text-align: right;
  font-size: 0.78rem;
  color: rgba(87, 59, 29, 0.65);
}

.journal-modal__actions {
  display: flex;
  gap: 0.6rem;
  flex-wrap: wrap;
}

.journal-modal__button {
  padding: 0.72rem 1rem;
  border-radius: 999px;
  border: 1px solid rgba(120, 70, 29, 0.24);
  background: rgba(255, 248, 231, 0.75);
  color: #523115;
  font-size: 0.9rem;
  font-weight: 700;
  cursor: pointer;
}

.journal-modal__button--primary {
  background: linear-gradient(135deg, #7f4b21, #5b3117);
  color: #ffefca;
  border-color: transparent;
}

.journal-modal__button:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.journal-modal__error {
  margin: 0;
  font-size: 0.86rem;
  color: #9e2a21;
}

@media (max-width: 900px) {
  .journal-reader__toolbar,
  .journal-nav {
    flex-direction: column;
    align-items: stretch;
  }

  .journal-reader__status,
  .journal-nav__meta {
    text-align: left;
  }
}

@media (max-width: 640px) {
  .journal-reader {
    gap: var(--space-3);
  }

  .journal-bookstage {
    --paper-gap: 8px;
    padding: 12px;
  }

  .journal-bookstage__spine {
    top: 12px;
    bottom: 12px;
    width: 22px;
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

  .journal-modal__button {
    width: 100%;
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