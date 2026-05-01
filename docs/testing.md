# Testing

## Summary

| Layer | Tests | Files | Coverage target |
|-------|-------|-------|----------------|
| Backend (JUnit 5) | 225 | 23 test classes | ≥50% (actual ~55–60%) |
| Frontend (Vitest) | 310 | 46 test files | ≥30% (actual ~35–40%) |
| E2E (Cypress) | — | 2+ flows | 2 critical paths |

---

## Backend

**Framework:** JUnit 5, Mockito, MockMvc, Spring Boot Test

**Run:**
```bash
cd backend && mvn test
```

**Coverage report (JaCoCo):**
```bash
cd backend && mvn verify
# Report at: backend/target/site/jacoco/index.html
```

### Test categories

**Controller tests** — MockMvc integration tests that start the Spring context and fire HTTP requests through the full controller → service → repository stack. Verify status codes, response body shape, and auth enforcement (unauthenticated / wrong-role requests return 401/403).

**Service tests** — Unit tests with Mockito. Repository dependencies are mocked. Tests verify business logic: XP calculation, stop unlock logic, answer evaluation routing, medal award conditions, rate limiting, etc.

**Answer checker tests** — Pure unit tests for each class in `service/answer/`. Each checker is tested with correct answers, incorrect answers, edge cases (empty input, malformed JSON), and boundary conditions (e.g. password strength thresholds).

**DataLoader test** — Verifies that seed data is inserted correctly on startup: expected number of stops, tasks per stop, task types, medals, and their relationships.

### Test class inventory

Located in `backend/src/test/java/no/ntnu/idatt2106/nettdetektivene/`:

- `controller/` — AuthControllerTest, GameControllerTest, ClassroomControllerTest, AvatarControllerTest, MedalControllerTest, NotebookControllerTest, NotificationControllerTest, SchoolControllerTest, WeeklyMysteryControllerTest
- `service/` — GameServiceTest, ClassroomServiceTest, AvatarServiceTest, NotebookServiceTest, WeeklyMysteryServiceTest, NotificationServiceTest
- `service/answer/` — FakeNewsAnswerCheckerTest, AiPhotoAnswerCheckerTest, PhishingEmailAnswerCheckerTest, MarketplaceAnswerCheckerTest, SocialMediaAnswerCheckerTest, PasswordAnswerCheckerTest, FinalBossAnswerCheckerTest
- `seed/` — DataLoaderTest

---

## Frontend

**Framework:** Vitest, @vue/test-utils, happy-dom (or jsdom)

**Run:**
```bash
cd frontend && npm run test
```

**Watch mode:**
```bash
cd frontend && npm run test:watch
```

**Coverage:**
```bash
cd frontend && npm run test:coverage
# Report at: frontend/coverage/index.html
```

### Test categories

**Component tests** — Mount components with `@vue/test-utils`. Verify rendered output, user interactions (clicks, input), prop handling, emitted events, and conditional rendering. Pinia stores are created fresh per test using `createTestingPinia()`.

**Store tests** — Test Pinia store actions in isolation. Mock the Axios service layer. Verify state mutations, action side effects (e.g. auth store clears token on logout), and getters.

### Test file locations

`frontend/src/__tests__/` — mirrors the `src/` structure:
- `components/` — one test file per component
- `stores/` — one test file per store
- `views/` — selected view tests

---

## E2E (Cypress)

**Run headless (CI):**
```bash
cd frontend && npm run cypress:run
```

**Run interactive (local development):**
```bash
cd frontend && npm run cypress:open
```

Cypress specs are in `frontend/cypress/e2e/`.

### Required flows

#### Flow 1 — Teacher: register and create classroom
1. Navigate to `/register`
2. Fill in email, password, first name, last name
3. Submit — assert redirect to teacher dashboard
4. Click "Create classroom"
5. Fill in name and description
6. Submit — assert classroom card appears with a join code

#### Flow 2 — Student: join classroom and complete a task
1. Navigate to `/student-login`
2. Enter a pre-seeded student username
3. Submit — assert redirect to student home
4. Enter classroom join code
5. Wait for approval (DevSeeder auto-approves in test environment)
6. Navigate to the map
7. Select Stop 1 (Nyhetskvartalet)
8. Complete the first task
9. Assert that progress updates (XP increased, task marked complete)

### CI integration

`npm run cypress:run` is part of the GitLab CI pipeline. Cypress runs against a locally started dev server (`vite preview` or `vite dev`) pointed at the test database. The pipeline fails if any Cypress test fails.

---

## Coverage targets

| Layer | Minimum | Target |
|-------|---------|--------|
| Backend | 50% | 55–60% |
| Frontend | 30% | 35–40% |

JaCoCo enforces the backend minimum: the Maven build fails if line coverage drops below 50%. Frontend coverage is checked manually; there is no hard CI gate, but the target should be maintained.
