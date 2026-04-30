import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import FakeNewsTask from '@/components/student/FakeNewsTask.vue'

// ─── Fixtures ──────────────────────────────────────────────────────────────

const TWO_ARTICLE_TASK = {
  id: 1,
  guidanceText: 'Les overskrift og kilde før du bestemmer deg.',
  contentJson: {
    articles: [
      {
        headline: 'Kommunen holder skolene åpne etter snøfallet',
        body: 'Brøytemannskapene har jobbet gjennom natten.',
        source: 'Trondheim kommune',
        isReal: true,
      },
      {
        headline: 'Alle skoler i Norge stenger i morgen',
        body: 'En anonym kilde sier at regjeringen har bestemt dette.',
        source: 'NorskNyhet24.info',
        isReal: false,
      },
    ],
  },
}

const THREE_ARTICLE_TASK = {
  id: 2,
  guidanceText: 'Tre artikler — finn den falske.',
  contentJson: {
    articles: [
      { headline: 'A', body: 'a', source: 'nrk.no',   isReal: true  },
      { headline: 'B', body: 'b', source: 'vg.no',    isReal: true  },
      { headline: 'C', body: 'c', source: 'fake.xyz',  isReal: false },
    ],
  },
}

// Server response shapes
const WRONG_RESULT = {
  correct: false,
  explanation: 'Den første artikkelen var ekte.',
  stopCompleted: false,
  medalEarned: null,
  // Server echoes back the answer shape so the component can derive the correct index
  article_0: true,
  article_1: false,
}

const CORRECT_RESULT = {
  correct: true,
  explanation: 'Riktig! NorskNyhet24 er ikke en pålitelig kilde.',
  stopCompleted: false,
  medalEarned: null,
}

const STOP_COMPLETED_RESULT = {
  correct: true,
  explanation: 'Godt jobbet!',
  stopCompleted: true,
  medalEarned: { id: 1, name: 'Nyhetsjeger', description: 'Fullfør Nyhetskvartalet.' },
}

const EXPLICIT_INDEX_RESULT = {
  correct: false,
  explanation: 'Feil.',
  stopCompleted: false,
  medalEarned: null,
  correctArticleIndex: 1,
}

// ─── Mount helper ──────────────────────────────────────────────────────────

function mountTask(task = TWO_ARTICLE_TASK, extraProps = {}) {
  return mount(FakeNewsTask, {
    props: { task, result: null, isLastTask: false, ...extraProps },
  })
}

// ─── Tests ─────────────────────────────────────────────────────────────────

describe('FakeNewsTask.vue', () => {
  beforeEach(() => {
    vi.spyOn(Math, 'random').mockReturnValue(0.99)
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  // ── Rendering ───────────────────────────────────────────────────────────

  describe('rendering', () => {
    it('viser guidanceText', () => {
      const wrapper = mountTask()
      expect(wrapper.find('.fake-news-task__guidance').text()).toBe(TWO_ARTICLE_TASK.guidanceText)
    })

    it('viser riktig antall artikkelkort', () => {
      const wrapper = mountTask()
      expect(wrapper.findAll('.article-card')).toHaveLength(2)
    })

    it('viser riktig antall artikkelkort for tre-artikkel-oppgave', () => {
      const wrapper = mountTask(THREE_ARTICLE_TASK)
      expect(wrapper.findAll('.article-card')).toHaveLength(3)
    })

    it('viser overskrift i hvert kort', () => {
      const wrapper = mountTask()
      const headlines = wrapper.findAll('.newspaper-clipping__headline')
      expect(headlines[0].text()).toBe('Kommunen holder skolene åpne etter snøfallet')
      expect(headlines[1].text()).toBe('Alle skoler i Norge stenger i morgen')
    })

    it('viser kilde i hvert kort', () => {
      const wrapper = mountTask()
      const bylines = wrapper.findAll('.newspaper-clipping__byline')
      expect(bylines[0].text()).toContain('Trondheim kommune')
      expect(bylines[1].text()).toContain('NorskNyhet24.info')
    })

    it('skjuler result-panel ved oppstart', () => {
      const wrapper = mountTask()
      expect(wrapper.find('.fake-news-task__result').exists()).toBe(false)
    })

    it('viser result-panel når result-prop er satt', () => {
      const wrapper = mountTask(TWO_ARTICLE_TASK, { result: CORRECT_RESULT })
      expect(wrapper.find('.fake-news-task__result').exists()).toBe(true)
    })
  })

  // ── pickCard / single-select ────────────────────────────────────────────

  describe('pickCard — enkeltvalg', () => {
    it('emitter submitted med riktig svar-objekt ved klikk', async () => {
      const wrapper = mountTask()
      await wrapper.findAll('.article-card')[1].trigger('click')

      expect(wrapper.emitted('submitted')).toHaveLength(1)
      // Index 1 er valgt som falsk → article_1: false, article_0: true
      expect(wrapper.emitted('submitted')[0][0]).toEqual({
        article_0: true,
        article_1: false,
      })
    })

    it('setter article_N: false kun for valgt indeks, true for alle andre', async () => {
      const wrapper = mountTask(THREE_ARTICLE_TASK)
      await wrapper.findAll('.article-card')[2].trigger('click')

      expect(wrapper.emitted('submitted')[0][0]).toEqual({
        article_0: true,
        article_1: true,
        article_2: false,
      })
    })

    it('emitter submitted ved tastatur Enter', async () => {
      const wrapper = mountTask()
      await wrapper.findAll('.article-card')[0].trigger('keydown.enter')
      expect(wrapper.emitted('submitted')).toHaveLength(1)
    })

    it('emitter submitted ved tastatur Space', async () => {
      const wrapper = mountTask()
      await wrapper.findAll('.article-card')[0].trigger('keydown.space')
      expect(wrapper.emitted('submitted')).toHaveLength(1)
    })

    it('emitter ikke submitted etter at result er satt (re-klikk blokkert)', async () => {
      const wrapper = mountTask(TWO_ARTICLE_TASK, { result: CORRECT_RESULT })
      await wrapper.findAll('.article-card')[0].trigger('click')
      expect(wrapper.emitted('submitted')).toBeFalsy()
    })

    it('setter aria-pressed til true på valgt kort', async () => {
      const wrapper = mountTask()
      const cards = wrapper.findAll('.article-card')
      await cards[0].trigger('click')
      expect(cards[0].attributes('aria-pressed')).toBe('true')
      expect(cards[1].attributes('aria-pressed')).toBe('false')
    })

    it('nullstiller chosenIndex og animasjoner ved task-bytte', async () => {
      const wrapper = mountTask()
      await wrapper.findAll('.article-card')[1].trigger('click')
      await wrapper.setProps({ task: { ...TWO_ARTICLE_TASK, id: 99 } })

      const cards = wrapper.findAll('.article-card')
      cards.forEach(card => {
        expect(card.attributes('aria-pressed')).toBe('false')
      })
    })
  })

  // ── getCorrectIndex — regresjonstest ────────────────────────────────────

  describe('getCorrectIndex — regressiontest (blokkerende PR-feil)', () => {
    it('fremhever riktig kort ved feil svar via server-respons-objekt', async () => {
      const wrapper = mountTask()
      // Velg kort 0 (feil — det er det ekte)
      await wrapper.findAll('.article-card')[0].trigger('click')
      await wrapper.setProps({ result: WRONG_RESULT })

      // Etter shake-timeout (400ms) skal kort 1 (article_1: false) få article-card--correct
      // Vi simulerer timeout
      vi.useFakeTimers()
      vi.runAllTimers()
      await wrapper.vm.$nextTick()

      const cards = wrapper.findAll('.article-card')
      expect(cards[0].classes()).toContain('article-card--wrong')   // valgt, feil
      expect(cards[1].classes()).toContain('article-card--correct') // det riktige

      vi.useRealTimers()
    })

    it('bruker correctArticleIndex fra server når den er et tall', async () => {
      const wrapper = mountTask()
      await wrapper.findAll('.article-card')[0].trigger('click')
      await wrapper.setProps({ result: EXPLICIT_INDEX_RESULT })

      vi.useFakeTimers()
      vi.runAllTimers()
      await wrapper.vm.$nextTick()

      // correctArticleIndex: 1 → kort 1 skal fremheves som riktig
      expect(wrapper.findAll('.article-card')[1].classes()).toContain('article-card--correct')

      vi.useRealTimers()
    })

    it('bruker findIndex-fallback når correctArticleIndex mangler', async () => {
      // WRONG_RESULT har ikke correctArticleIndex — skal bruke article_N: false
      const wrapper = mountTask()
      await wrapper.findAll('.article-card')[0].trigger('click')
      await wrapper.setProps({ result: WRONG_RESULT })

      vi.useFakeTimers()
      vi.runAllTimers()
      await wrapper.vm.$nextTick()

      // article_1: false i WRONG_RESULT → indeks 1 er korrekt
      expect(wrapper.findAll('.article-card')[1].classes()).toContain('article-card--correct')

      vi.useRealTimers()
    })
  })

  // ── articleClass ────────────────────────────────────────────────────────

  describe('articleClass — kort-tilstand', () => {
    it('valgt kort får article-card--chosen før result', async () => {
      const wrapper = mountTask()
      await wrapper.findAll('.article-card')[1].trigger('click')
      expect(wrapper.findAll('.article-card')[1].classes()).toContain('article-card--chosen')
    })

    it('korrekt valgt kort får article-card--correct ved riktig svar', async () => {
      const wrapper = mountTask()
      await wrapper.findAll('.article-card')[1].trigger('click')
      await wrapper.setProps({ result: CORRECT_RESULT })
      expect(wrapper.findAll('.article-card')[1].classes()).toContain('article-card--correct')
    })

    it('øvrige kort får article-card--muted ved riktig svar', async () => {
      const wrapper = mountTask()
      await wrapper.findAll('.article-card')[1].trigger('click')
      await wrapper.setProps({ result: CORRECT_RESULT })
      expect(wrapper.findAll('.article-card')[0].classes()).toContain('article-card--muted')
    })

    it('valgt feil kort får article-card--wrong ved feil svar', async () => {
      const wrapper = mountTask()
      await wrapper.findAll('.article-card')[0].trigger('click')
      await wrapper.setProps({ result: WRONG_RESULT })
      expect(wrapper.findAll('.article-card')[0].classes()).toContain('article-card--wrong')
    })
  })

  // ── Result-visning ──────────────────────────────────────────────────────

  describe('result-visning', () => {
    it('viser ✅ Riktig! ved korrekt svar', () => {
      const wrapper = mountTask(TWO_ARTICLE_TASK, { result: CORRECT_RESULT })
      expect(wrapper.find('.fake-news-task__result-label').text()).toContain('Riktig!')
    })

    it('viser ❌ Ikke helt riktig ved feil svar', () => {
      const wrapper = mountTask(TWO_ARTICLE_TASK, { result: WRONG_RESULT })
      expect(wrapper.find('.fake-news-task__result-label').text()).toContain('Ikke helt riktig')
    })

    it('viser forklaringstekst fra result.explanation', () => {
      const wrapper = mountTask(TWO_ARTICLE_TASK, { result: CORRECT_RESULT })
      expect(wrapper.find('.fake-news-task__explanation').text()).toBe(CORRECT_RESULT.explanation)
    })

    it('viser stopp-fullført-melding når result.stopCompleted er true', () => {
      const wrapper = mountTask(TWO_ARTICLE_TASK, { result: STOP_COMPLETED_RESULT })
      expect(wrapper.find('.fake-news-task__stop-msg').exists()).toBe(true)
    })

    it('skjuler stopp-fullført-melding når result.stopCompleted er false', () => {
      const wrapper = mountTask(TWO_ARTICLE_TASK, { result: CORRECT_RESULT })
      expect(wrapper.find('.fake-news-task__stop-msg').exists()).toBe(false)
    })

    it('viser Neste oppgave → når isLastTask er false', () => {
      const wrapper = mountTask(TWO_ARTICLE_TASK, { result: CORRECT_RESULT, isLastTask: false })
      expect(wrapper.find('.next-btn').text()).toContain('Neste oppgave')
    })

    it('viser Videre til sammendrag → når isLastTask er true', () => {
      const wrapper = mountTask(TWO_ARTICLE_TASK, { result: CORRECT_RESULT, isLastTask: true })
      expect(wrapper.find('.next-btn').text()).toContain('Videre til sammendrag')
    })

    it('emitter next-event ved klikk på neste-knapp', async () => {
      const wrapper = mountTask(TWO_ARTICLE_TASK, { result: CORRECT_RESULT })
      await wrapper.find('.next-btn').trigger('click')
      expect(wrapper.emitted('next')).toHaveLength(1)
    })

    it('viser Prøv igjen og emitter retry ved feil svar', async () => {
      const wrapper = mountTask(TWO_ARTICLE_TASK, { result: WRONG_RESULT })
      expect(wrapper.find('.next-btn').text()).toContain('Prøv igjen')

      await wrapper.find('.next-btn').trigger('click')
      expect(wrapper.emitted('retry')).toHaveLength(1)
      expect(wrapper.emitted('next')).toBeFalsy()
    })
  })

  // ── mastheadBrand / extractDomainOrName ─────────────────────────────────

  describe('mastheadBrand — masthead-tekst', () => {
    it('viser kildenavn som-det-er for vanlige navn (Trondheim kommune)', () => {
      const wrapper = mountTask()
      const brands = wrapper.findAll('.newspaper-clipping__brand')
      // text-transform: uppercase i CSS — test mot uppercase
      expect(brands[0].text()).toBe('TRONDHEIM KOMMUNE')
    })

    it('viser domenenavn uten www for URL-kilder (nrk.no)', () => {
      const wrapper = mountTask(THREE_ARTICLE_TASK)
      expect(wrapper.findAll('.newspaper-clipping__brand')[0].text()).toBe('NRK.NO')
    })

    it('håndterer percent-encodet kilde uten å vise %20', () => {
      const encodedTask = {
        id: 3,
        guidanceText: 'Test',
        contentJson: {
          articles: [
            { headline: 'Test', body: 'Test', source: 'Trondheim%20kommune', isReal: true },
          ],
        },
      }
      const wrapper = mountTask(encodedTask)
      expect(wrapper.find('.newspaper-clipping__brand').text()).not.toContain('%20')
      expect(wrapper.find('.newspaper-clipping__brand').text()).toBe('TRONDHEIM KOMMUNE')
    })

    it('viser NYHETER som fallback for tom kilde', () => {
      const noSourceTask = {
        id: 4,
        guidanceText: 'Test',
        contentJson: {
          articles: [{ headline: 'Test', body: 'Test', source: '', isReal: false }],
        },
      }
      const wrapper = mountTask(noSourceTask)
      expect(wrapper.find('.newspaper-clipping__brand').text()).toBe('NYHETER')
    })
  })

  // ── Kant-tilfeller ──────────────────────────────────────────────────────

  describe('kant-tilfeller', () => {
    it('håndterer tom artikkelliste uten å krasje', () => {
      const emptyTask = { id: 99, guidanceText: 'Test', contentJson: { articles: [] } }
      const wrapper = mountTask(emptyTask)
      expect(wrapper.findAll('.article-card')).toHaveLength(0)
    })

    it('håndterer manglende contentJson uten å krasje', () => {
      const noContentTask = { id: 100, guidanceText: 'Test', contentJson: null }
      const wrapper = mountTask(noContentTask)
      expect(wrapper.findAll('.article-card')).toHaveLength(0)
    })
  })
})
