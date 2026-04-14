describe('Authentication', () => {
  beforeEach(() => {
    cy.visit('/login')
  })

  it('shows login form', () => {
    cy.get('h1').should('contain', 'Nettdetektivene')
    cy.get('input[type="email"]').should('exist')
    cy.get('input[type="password"]').should('exist')
    cy.get('button[type="submit"]').should('exist')
  })

  it('shows validation errors on empty submit', () => {
    cy.get('button[type="submit"]').click()
    cy.get('[role="alert"]').should('contain', 'E-post er påkrevd')
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

  it('redirects student to / on successful login', () => {
    cy.intercept('POST', '**/api/auth/login', {
      statusCode: 200,
      body: { token: 'fake-jwt', role: 'STUDENT', userId: 2, email: 'student@student.local' }
    }).as('loginRequest')

    cy.get('input[type="email"]').type('student@student.local')
    cy.get('input[type="password"]').type('password123')
    cy.get('button[type="submit"]').click()

    cy.wait('@loginRequest')
    cy.url().should('eq', Cypress.config().baseUrl + '/')
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
