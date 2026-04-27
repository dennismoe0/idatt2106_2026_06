import { describe, it, expect } from 'vitest'
import {
  RESERVED_REPORT_TITLES,
  buildGeneralPages,
  buildLevelPagesForGroup,
  estimateTextUnits,
  normalizeText,
  packBlocks,
  splitTextIntoChunks,
} from '@/utils/notebookPagination'

describe('normalizeText', () => {
  it('returns an empty string for nullish input', () => {
    expect(normalizeText(null)).toBe('')
    expect(normalizeText(undefined)).toBe('')
  })

  it('normalizes \\r\\n and \\r to \\n and trims', () => {
    expect(normalizeText('  a\r\nb\rc  ')).toBe('a\nb\nc')
  })

  it('coerces non-string values to strings', () => {
    expect(normalizeText(42)).toBe('42')
  })
})

describe('estimateTextUnits', () => {
  it('returns 1 for empty / blank text', () => {
    expect(estimateTextUnits('')).toBe(1)
    expect(estimateTextUnits('   \n  ')).toBe(1)
  })

  it('scales with length using the given charsPerUnit', () => {
    const text = 'a'.repeat(74)
    expect(estimateTextUnits(text, 74)).toBe(1)

    const longer = 'a'.repeat(150)
    // ceil(150 / 74) = 3
    expect(estimateTextUnits(longer, 74)).toBe(3)
  })

  it('adds one unit per line break', () => {
    // two short lines => ceil(chars/74)=1 + 1 line break = 2
    expect(estimateTextUnits('hello\nworld', 74)).toBe(2)
  })

  it('never returns less than 1', () => {
    expect(estimateTextUnits('x', 1000)).toBe(1)
  })
})

describe('splitTextIntoChunks', () => {
  it('returns [""] for empty input', () => {
    expect(splitTextIntoChunks('', 10)).toEqual([''])
  })

  it('returns a single chunk when text fits', () => {
    expect(splitTextIntoChunks('short line', 50)).toEqual(['short line'])
  })

  it('prefers whitespace boundaries when splitting', () => {
    const chunks = splitTextIntoChunks('one two three four', 8)
    // Each chunk should be <= 8 chars after trim and contain no partial words
    for (const chunk of chunks) {
      expect(chunk.length).toBeLessThanOrEqual(8)
    }
    // Joined content (ignoring whitespace) should equal the original words
    expect(chunks.join(' ').replace(/\s+/g, ' ').trim()).toBe('one two three four')
  })

  it('hard-splits words longer than maxChars', () => {
    const chunks = splitTextIntoChunks('abcdefghij', 4)
    expect(chunks).toEqual(['abcd', 'efgh', 'ij'])
  })
})

describe('packBlocks', () => {
  it('returns [[]] when given no blocks', () => {
    expect(packBlocks([], 10)).toEqual([[]])
  })

  it('fits blocks on a single page when under capacity', () => {
    const blocks = [
      { key: 'a', units: 2 },
      { key: 'b', units: 3 },
    ]
    expect(packBlocks(blocks, 10)).toEqual([blocks])
  })

  it('spills to a new page when capacity is exceeded', () => {
    const blocks = [
      { key: 'a', units: 6 },
      { key: 'b', units: 6 },
    ]
    const pages = packBlocks(blocks, 10)
    expect(pages).toHaveLength(2)
    expect(pages[0]).toEqual([blocks[0]])
    expect(pages[1]).toEqual([blocks[1]])
  })

  it('treats a missing units field as 1', () => {
    const blocks = [{ key: 'a' }, { key: 'b' }, { key: 'c' }]
    const pages = packBlocks(blocks, 2)
    expect(pages).toHaveLength(2)
    expect(pages[0]).toHaveLength(2)
    expect(pages[1]).toHaveLength(1)
  })

  it('keeps an oversized block alone on its own page rather than splitting it', () => {
    const blocks = [
      { key: 'small', units: 1 },
      { key: 'huge', units: 99 },
      { key: 'small2', units: 1 },
    ]
    const pages = packBlocks(blocks, 10)
    expect(pages).toHaveLength(3)
    expect(pages[0]).toEqual([blocks[0]])
    expect(pages[1]).toEqual([blocks[1]])
    expect(pages[2]).toEqual([blocks[2]])
  })
})

describe('buildLevelPagesForGroup', () => {
  it('produces a single locked placeholder page when no group exists', () => {
    const pages = buildLevelPagesForGroup(3, undefined)
    expect(pages).toHaveLength(1)
    const [page] = pages
    expect(page.kind).toBe('stop')
    expect(page.title).toBe(RESERVED_REPORT_TITLES[3])
    expect(page.subtitle).toBe('Nivå 3')
    expect(page.eyebrow).toBe('Reservert rapportside')
    expect(page.showComposer).toBe(false)
    expect(page.stopId).toBeNull()
    expect(page.blocks).toHaveLength(1)
    expect(page.blocks[0].type).toBe('empty')
  })

  it('uses a generic title when the stop order has no reserved mapping', () => {
    const pages = buildLevelPagesForGroup(99, undefined)
    expect(pages[0].title).toBe('Oppdrag 99')
  })

  it('builds an empty "Feltnotater" page for a group with no content', () => {
    const group = { stopId: 42, stopName: 'Kaffebaren', reflections: [] }
    const [page] = buildLevelPagesForGroup(1, group)
    expect(page.eyebrow).toBe('Feltnotater')
    expect(page.title).toBe('Kaffebaren')
    expect(page.stopId).toBe(42)
    expect(page.showComposer).toBe(true)
    expect(page.blocks[0].type).toBe('empty')
  })

  it('includes the tip and reflections in order, with tip first', () => {
    const group = {
      stopId: 7,
      autoTip: { id: 't1', content: 'Tip here', createdAt: '2026-04-01' },
      reflections: [
        { id: 'r1', content: 'First thought', createdAt: '2026-04-02' },
        { id: 'r2', content: 'Second thought', createdAt: '2026-04-03' },
      ],
    }
    const pages = buildLevelPagesForGroup(1, group)
    const allBlocks = pages.flatMap((p) => p.blocks)
    expect(allBlocks[0].type).toBe('tip')
    expect(allBlocks[1].type).toBe('reflection')
    expect(allBlocks[1].id).toBe('r1')
    expect(allBlocks[2].id).toBe('r2')
  })

  it('sets partLabel and splits across pages when content overflows', () => {
    const longContent = 'x'.repeat(2000)
    const group = {
      stopId: 1,
      reflections: Array.from({ length: 5 }, (_, i) => ({
        id: `r${i}`,
        content: longContent,
        createdAt: `2026-04-0${i + 1}`,
      })),
    }
    const pages = buildLevelPagesForGroup(1, group)
    expect(pages.length).toBeGreaterThan(1)
    expect(pages[0].partLabel).toBe(`Del 1 av ${pages.length}`)
    // showComposer only on last page
    expect(pages[pages.length - 1].showComposer).toBe(true)
    expect(pages[0].showComposer).toBe(false)
  })
})

describe('buildGeneralPages', () => {
  it('returns a single empty page when there are no notes', () => {
    const pages = buildGeneralPages([])
    expect(pages).toHaveLength(1)
    expect(pages[0].kind).toBe('general')
    expect(pages[0].blocks[0].type).toBe('empty')
    expect(pages[0].showComposer).toBe(true)
  })

  it('orders notes by createdAt ascending', () => {
    const notes = [
      { id: 'b', content: 'newer', createdAt: '2026-04-03' },
      { id: 'a', content: 'older', createdAt: '2026-04-01' },
      { id: 'c', content: 'newest', createdAt: '2026-04-05' },
    ]
    const pages = buildGeneralPages(notes)
    const ids = pages.flatMap((p) => p.blocks).map((b) => b.id)
    expect(ids).toEqual(['a', 'b', 'c'])
  })

  it('does not mutate the input notes array', () => {
    const notes = [
      { id: 'b', content: 'newer', createdAt: '2026-04-03' },
      { id: 'a', content: 'older', createdAt: '2026-04-01' },
    ]
    const snapshot = notes.map((n) => n.id)
    buildGeneralPages(notes)
    expect(notes.map((n) => n.id)).toEqual(snapshot)
  })
})
