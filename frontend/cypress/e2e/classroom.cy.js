// E2E flow: Teacher creates classroom and sees join code
// Covers CLAUDE.md requirement: "Teacher: register → login → create classroom → see join code"

const TEACHER = { email: 'teacher@test.no', password: 'password123', userId: 1 }
const CLASSROOM = { id: 1, name: 'Testklasse 7A', joinCode: 'fjord-tiger', description: '', createdAt: '2026-04-16T10:00:00' }

function loginAsTeacher() {
  cy.intercept('POST', '**/api/auth/login', {
    statusCode: 200,
    body: { token: 'fake-teacher-jwt', role: 'TEACHER', userId: TEACHER.userId, email: TEACHER.email }
  }).as('login')

  cy.intercept('GET', '**/api/classrooms', { statusCode: 200, body: [] }).as('getClassrooms')

  cy.visit('/login')
  cy.get('input[type="email"]').type(TEACHER.email)
  cy.get('input[type="password"]').type(TEACHER.password)
  cy.get('button[type="submit"]').click()
  cy.wait('@login')
  cy.wait('@getClassrooms')
  cy.url().should('include', '/teacher')
}

describe('Teacher classroom flow', () => {
  it('registers a new teacher account', () => {
    cy.intercept('POST', '**/api/auth/register', {
      statusCode: 201,
      body: { token: 'new-teacher-jwt', role: 'TEACHER', userId: 99, email: 'new@test.no' }
    }).as('register')

    cy.intercept('GET', '**/api/classrooms', { statusCode: 200, body: [] }).as('getClassrooms')

    cy.visit('/register')
    cy.get('input[type="email"]').type('new@test.no')
    cy.get('input[type="password"]').type('password123')
    cy.get('button[type="submit"]').click()
    cy.wait('@register')
    cy.url().should('include', '/teacher')
  })

  it('logs in and sees the dashboard', () => {
    loginAsTeacher()
    cy.contains('Mine klasser').should('be.visible')
    cy.contains('Opprett').should('be.visible')
  })

  it('creates a classroom and sees the join code', () => {
    loginAsTeacher()

    cy.intercept('POST', '**/api/classrooms', {
      statusCode: 201,
      body: CLASSROOM
    }).as('createClassroom')

    cy.intercept('GET', '**/api/classrooms', {
      statusCode: 200,
      body: [CLASSROOM]
    }).as('getClassroomsAfterCreate')

    // Open create modal
    cy.contains('+ Opprett ny klasse').click()
    cy.contains('Opprett nytt klasserom').should('be.visible')

    // Fill form
    cy.get('.form-input').first().type('Testklasse 7A')
    cy.contains('button', 'Opprett klasserom').click()
    cy.wait('@createClassroom')

    // Join code is shown
    cy.contains('fjord-tiger').should('be.visible')
    cy.contains('Kopier').should('be.visible')
  })

  it('navigates to classroom detail and approves a student', () => {
    loginAsTeacher()

    cy.intercept('GET', '**/api/classrooms', {
      statusCode: 200,
      body: [CLASSROOM]
    }).as('getClassroomsLoaded')

    cy.intercept('GET', '**/api/classrooms/1/students', {
      statusCode: 200,
      body: [{ userId: 5, classroomId: 1, displayName: 'Agent Nora', status: 'PENDING' }]
    }).as('getStudents')

    cy.intercept('PUT', '**/api/classrooms/1/students/5', {
      statusCode: 200,
      body: { userId: 5, classroomId: 1, displayName: 'Agent Nora', status: 'APPROVED' }
    }).as('approveStudent')

    cy.wait('@getClassroomsLoaded')

    cy.visit('/teacher/classrooms/1')
    cy.wait('@getStudents')

    cy.contains('Agent Nora').should('be.visible')
    cy.contains('Venter').should('be.visible')

    cy.contains('Godkjenn').click()
    cy.wait('@approveStudent')

    cy.contains('Godkjent').should('be.visible')
  })
})
