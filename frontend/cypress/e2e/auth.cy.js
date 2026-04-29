describe('Student front page', () => {
  beforeEach(() => {
    cy.visit('/login')
  })

  it('shows the Feide-style student login by default', () => {
    cy.get('h1').should('contain', 'Logg inn med Feide')
    cy.get('#username').should('exist')
    cy.contains('a', 'Gå til lærerinnlogging').should('have.attr', 'href', '/teacher-login')
    cy.get('button[type="submit"]').should('exist')
  })

  it('shows validation errors on empty submit', () => {
    cy.get('button[type="submit"]').click()
    cy.get('[role="alert"]').should('contain', 'Elevnavn er påkrevd')
  })

  it('navigates to the teacher login page from the student front page', () => {
    cy.contains('a', 'Gå til lærerinnlogging').click()
    cy.url().should('include', '/teacher-login')
    cy.get('input[type="email"]').should('exist')
  })

  it('redirects student to intro on successful login', () => {
    cy.intercept('POST', '**/api/auth/student-login', {
      statusCode: 200,
      body: { token: 'fake-student-jwt', role: 'STUDENT', userId: 2, email: 'student@student.local' }
    }).as('studentLogin')

    cy.intercept('GET', '**/api/classrooms/mine', {
      statusCode: 200,
      body: { classroomId: 7, displayName: 'Agent Nora', status: 'APPROVED' }
    }).as('myClassroom')

    cy.get('#username').type('agent.nora')
    cy.get('button[type="submit"]').click()

    cy.wait('@studentLogin')
    cy.wait('@myClassroom')
    cy.url().should('include', '/intro')
  })
})

describe('Teacher authentication', () => {
  beforeEach(() => {
    cy.visit('/teacher-login')
  })

  it('shows the teacher login form', () => {
    cy.get('h1').should('contain', 'Lærerinnlogging')
    cy.get('input[type="email"]').should('exist')
    cy.get('input[type="password"]').should('exist')
  })

  it('shows server error on wrong credentials', () => {
    cy.intercept('POST', '**/api/auth/login', {
      statusCode: 401,
      body: { error: 'Invalid credentials' }
    }).as('loginRequest')

    cy.get('input[type="email"]').type('teacher@test.no')
    cy.get('input[type="password"]').type('wrongpassword')
    cy.get('button[type="submit"]').click()

    cy.wait('@loginRequest')
    cy.get('[role="alert"]').should('contain', 'Invalid credentials')
  })

  it('redirects teacher to /teacher on successful login', () => {
    cy.intercept('POST', '**/api/auth/login', {
      statusCode: 200,
      body: { token: 'fake-jwt', role: 'TEACHER', userId: 1, email: 'teacher@test.no' }
    }).as('loginRequest')

    cy.get('input[type="email"]').type('teacher@test.no')
    cy.get('input[type="password"]').type('password123')
    cy.get('button[type="submit"]').click()

    cy.wait('@loginRequest')
    cy.url().should('include', '/teacher')
  })
})

describe('Registration', () => {
  it('shows validation error for short password', () => {
    cy.visit('/register')
    cy.get('input[type="email"]').type('teacher@test.no')
    cy.get('input[type="password"]').type('short')
    cy.get('button[type="submit"]').click()
    cy.get('[role="alert"]').should('contain', '8 tegn')
  })

  it('redirects to /teacher after successful registration', () => {
    cy.intercept('POST', '**/api/auth/register', {
      statusCode: 201,
      body: { token: 'new-token', role: 'TEACHER', userId: 3, email: 'newteacher@test.no' }
    }).as('registerRequest')

    cy.visit('/register')
    cy.get('input[type="email"]').type('newteacher@test.no')
    cy.get('input[type="password"]').type('password123')
    cy.get('button[type="submit"]').click()

    cy.wait('@registerRequest')
    cy.url().should('include', '/teacher')
  })
})
