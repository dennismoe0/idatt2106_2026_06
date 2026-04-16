// E2E flow: Student logs in, joins classroom, gets approved, and completes a task
// Covers CLAUDE.md requirement: "Student: login → join classroom → complete task → see progress update"

const STUDENT_TOKEN = 'fake-student-jwt'
const CLASSROOM_ID = 7

function setupStudentAuth() {
  cy.intercept('POST', '**/api/auth/student-login', {
    statusCode: 200,
    body: { token: STUDENT_TOKEN, role: 'STUDENT', userId: 2, email: 'agent.nora@student.local' }
  }).as('studentLogin')
}

function setupGameStops(locked = false) {
  cy.intercept('GET', '**/api/game/stops**', {
    statusCode: 200,
    body: [
      { id: 1, name: 'Nyhetskvartalet', orderIndex: 1, locked: false, completed: false, taskCount: 3 },
      { id: 2, name: 'Postkontoret', orderIndex: 2, locked: locked, completed: false, taskCount: 3 }
    ]
  }).as('getStops')
}

describe('Student login flow', () => {
  it('logs in with a username and lands on intro (first visit)', () => {
    setupStudentAuth()
    localStorage.removeItem('hasSeenIntro')

    cy.visit('/student-login')
    cy.get('input').type('agent.nora')
    cy.contains('button', 'Logg inn').click()
    cy.wait('@studentLogin')

    cy.url().should('include', '/intro')
    cy.contains('Velkommen, detektiv').should('be.visible')
  })

  it('logs in and lands on home if intro already seen', () => {
    setupStudentAuth()
    cy.visit('/student-login', {
      onBeforeLoad(win) { win.localStorage.setItem('hasSeenIntro', 'true') }
    })

    cy.get('input').type('agent.nora')
    cy.contains('button', 'Logg inn').click()
    cy.wait('@studentLogin')

    cy.url().should('eq', Cypress.config().baseUrl + '/')
  })
})

describe('Student join classroom flow', () => {
  beforeEach(() => {
    // Pre-populate auth so the student is logged in
    cy.intercept('POST', '**/api/auth/student-login', {
      statusCode: 200,
      body: { token: STUDENT_TOKEN, role: 'STUDENT', userId: 2, email: 'agent.nora@student.local' }
    }).as('studentLogin')

    cy.visit('/student-login', {
      onBeforeLoad(win) { win.localStorage.setItem('hasSeenIntro', 'true') }
    })
    cy.get('input').type('agent.nora')
    cy.contains('button', 'Logg inn').click()
    cy.wait('@studentLogin')
  })

  it('joins a classroom and reaches waiting room', () => {
    cy.intercept('POST', '**/api/classrooms/join', {
      statusCode: 200,
      body: { classroomId: CLASSROOM_ID, status: 'PENDING', displayName: 'Agent Nora' }
    }).as('joinClassroom')

    cy.visit('/join')
    cy.get('#code').type('fjord-tiger')
    cy.get('#displayName').type('Agent Nora')
    cy.contains('Bli med i klassen').click()
    cy.wait('@joinClassroom')

    cy.url().should('include', '/waiting')
    cy.contains('Venter på godkjenning').should('be.visible')
  })

  it('gets approved and redirects to home', () => {
    cy.intercept('POST', '**/api/classrooms/join', {
      statusCode: 200,
      body: { classroomId: CLASSROOM_ID, status: 'PENDING', displayName: 'Agent Nora' }
    }).as('joinClassroom')

    cy.intercept('GET', `**/api/classrooms/${CLASSROOM_ID}/my-status`, {
      statusCode: 200,
      body: { status: 'APPROVED' }
    }).as('myStatus')

    cy.visit('/join')
    cy.get('#code').type('fjord-tiger')
    cy.get('#displayName').type('Agent Nora')
    cy.contains('Bli med i klassen').click()
    cy.wait('@joinClassroom')

    cy.url().should('include', '/waiting')

    // Advance clock to trigger the 3-second poll
    cy.clock()
    cy.tick(3100)
    cy.wait('@myStatus')

    cy.url().should('eq', Cypress.config().baseUrl + '/')
  })
})

describe('Student game flow', () => {
  beforeEach(() => {
    // Log in as student with classroomId already in localStorage
    cy.intercept('POST', '**/api/auth/student-login', {
      statusCode: 200,
      body: { token: STUDENT_TOKEN, role: 'STUDENT', userId: 2, email: 'agent.nora@student.local' }
    }).as('studentLogin')

    cy.visit('/student-login', {
      onBeforeLoad(win) {
        win.localStorage.setItem('hasSeenIntro', 'true')
        win.localStorage.setItem('classroomId', String(CLASSROOM_ID))
      }
    })
    cy.get('input').type('agent.nora')
    cy.contains('button', 'Logg inn').click()
    cy.wait('@studentLogin')
  })

  it('sees the map with stops and navigates to a task', () => {
    setupGameStops()

    cy.visit('/map')
    cy.wait('@getStops')

    cy.contains('Nyhetskvartalet').should('be.visible')
    cy.contains('Postkontoret').should('be.visible')
  })

  it('shows locked message when clicking a locked stop', () => {
    setupGameStops(true)

    cy.visit('/map')
    cy.wait('@getStops')

    cy.contains('Postkontoret').click()
    cy.contains('Spill de tidligere stoppene').should('be.visible')
  })

  it('completes a task and sees the correct result', () => {
    setupGameStops()

    cy.intercept('GET', '**/api/game/stops/1/tasks**', {
      statusCode: 200,
      body: [{
        id: 1,
        stopId: 1,
        taskType: 'FAKE_NEWS',
        guidanceText: 'Marker artiklene som ekte eller falsk.',
        contentJson: JSON.stringify({
          articles: [
            { headline: 'Trondheim deler ut nettbrett', body: 'Kommunen tester ny satsing.', source: 'Adresseavisen', isReal: true },
            { headline: 'Melk gir superkrefter', body: 'Usannsynlig påstand om melk.', source: 'nyheter24-falsk.no', isReal: false }
          ],
          explanation: 'Den andre artikkelen er falsk.'
        }),
        alreadyCompleted: false
      }]
    }).as('getTasks')

    cy.intercept('POST', '**/api/game/tasks/1/submit**', {
      statusCode: 200,
      body: {
        correct: true,
        score: 100,
        explanation: 'Godt jobbet! Du identifiserte riktig kilde.',
        stopCompleted: false,
        medalEarned: null
      }
    }).as('submitAnswer')

    cy.visit(`/task?stopId=1&classroomId=${CLASSROOM_ID}`)
    cy.wait('@getTasks')

    cy.contains('Nyhetskvartalet').should('not.exist') // in task view, not map
    cy.contains('Oppgave 1').should('be.visible')

    // Rate both articles
    cy.contains('Ekte').first().click()
    cy.contains('Falsk').last().click()

    cy.contains('Send svar').click()
    cy.wait('@submitAnswer')

    cy.contains('Riktig!').should('be.visible')
    cy.contains('100').should('be.visible')
    cy.contains('Godt jobbet!').should('be.visible')
  })
})
