/**
 * Pure pagination / block-packing helpers for the notebook journal view.
 *
 * These are kept framework-free (no Vue imports) so they can be unit-tested
 * in isolation and reused.
 */

export const RESERVED_REPORT_COUNT = 7

export const RESERVED_REPORT_TITLES = {
  1: 'Et spor i nyhetsstrømmen',
  2: 'Bildet lyver',
  3: 'Ukjent avsender',
  4: 'Svindel på nett',
  5: 'Falsk venn',
  6: 'Passordlekkasje',
  7: 'Datasenteret er hacket'
}

export function normalizeText(value) {
  return String(value ?? '')
    .replace(/\r\n?/g, '\n')
    .trim()
}

/**
 * Rough estimate of how many vertical "layout units" a piece of text will
 * consume on a journal page. Used by packBlocks() to decide page breaks.
 */
export function estimateTextUnits(text, charsPerUnit = 74) {
  const normalized = normalizeText(text)
  if (!normalized) return 1

  const lineBreaks = (normalized.match(/\n/g) || []).length
  return Math.max(1, Math.ceil(normalized.length / charsPerUnit) + lineBreaks)
}

/**
 * Split a long text into chunks that fit a given max character budget,
 * preferring to break on whitespace. Words longer than maxChars are
 * hard-split.
 */
export function splitTextIntoChunks(text, maxChars) {
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

/**
 * Greedy bin-packer. Places blocks onto sequential pages so that the sum of
 * block.units per page does not exceed capacity. A single block whose units
 * exceed capacity still lands alone on its own page (no splitting here).
 */
export function packBlocks(blocks, capacity, maxBlocksPerPage = 2) {
  if (!blocks.length) return [[]]

  const pages = []
  let currentPage = []
  let usedUnits = 0

  for (const block of blocks) {
    const blockUnits = block.units ?? 1
    const overUnits = currentPage.length && usedUnits + blockUnits > capacity
    const overCount = currentPage.length >= maxBlocksPerPage
    if (overUnits || overCount) {
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

export function createBlankSeed(key = 'blank-tail') {
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

export function createBlankPage(pageIndex) {
  return {
    ...createBlankSeed(`blank-${pageIndex}`),
    number: pageIndex + 2
  }
}

export function buildTipBlocks(autoTip) {
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

export function buildClueBlocks(autoClue) {
  return [
    {
      key: `clue-${autoClue.id}`,
      type: 'clue',
      label: 'Spor',
      content: autoClue.content,
      createdAt: autoClue.createdAt,
      units: Math.min(11, 3 + estimateTextUnits(autoClue.content, 76))
    }
  ]
}

export function buildReflectionBlocks(reflection) {
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

export function buildGeneralNoteBlocks(note) {
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

export function buildLevelPagesForGroup(stopOrder, group) {
  const title =
    group?.stopName ||
    RESERVED_REPORT_TITLES[stopOrder] ||
    `Oppdrag ${stopOrder}`

  // Tip and clue are always rendered together on the first page of the
  // level, regardless of length, so the unlocked report and its associated
  // clue stay visually paired (see e.g. the post-office level which has a
  // long tip + clue pair that previously overflowed onto separate pages).
  const headerBlocks = []
  if (group?.autoTip) {
    headerBlocks.push(...buildTipBlocks(group.autoTip))
  }
  if (group?.autoClue) {
    headerBlocks.push(...buildClueBlocks(group.autoClue))
  }

  const reflectionBlocks = []
  for (const reflection of group?.reflections ?? []) {
    reflectionBlocks.push(...buildReflectionBlocks(reflection))
  }

  let pages
  if (headerBlocks.length) {
    // First page is dedicated to tip+clue; remaining reflections paginate
    // normally on subsequent pages.
    const reflectionPages = reflectionBlocks.length
      ? packBlocks(reflectionBlocks, 12)
      : []
    pages = [headerBlocks, ...reflectionPages]
  } else if (reflectionBlocks.length) {
    pages = packBlocks(reflectionBlocks, 12)
  } else {
    pages = [[
      {
        key: `level-empty-${stopOrder}`,
        type: 'empty',
        content: group
          ? 'Ingen observasjoner ennå. Skriv ned rare detaljer før de forsvinner.'
          : 'Fullfør dette nivået for å låse opp rapporten og legge til observasjoner.',
        units: 5
      }
    ]]
  }
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

export function buildGeneralPages(notes) {
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
    subtitle: null,
    partLabel: pages.length > 1 ? `Del ${index + 1} av ${pages.length}` : null,
    blocks: pageBlocks,
    showComposer: index === pages.length - 1,
  }))
}
