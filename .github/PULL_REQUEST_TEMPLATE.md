## What does this PR do?

<!-- One or two sentences. -->

## Type

- [ ] Feature
- [ ] Bug fix
- [ ] Refactor
- [ ] Chore / config
- [ ] Docs

## Related issue / user story

Closes #

## Checklist

**General**
- [ ] Branches off `dev`, targets `dev`
- [ ] Self-reviewed the diff
- [ ] No secrets, tokens, or PII committed
- [ ] Logging added (SLF4J INFO/WARN/ERROR on backend, console.log/warn/error on frontend)

**Backend** _(skip if frontend-only)_
- [ ] DTOs used — no raw JPA entities in responses
- [ ] Business logic in service layer, controller stays thin
- [ ] New endpoints have tests (unit or MockMvc integration)
- [ ] JaCoCo coverage not significantly reduced

**Frontend** _(skip if backend-only)_
- [ ] Composition API (`<script setup>`) — no Options API
- [ ] CSS uses `var(--color-*)` tokens — no hardcoded hex values
- [ ] Components tested with Vitest where logic is non-trivial

## Screenshots / demo

<!-- If UI changes, add a screenshot or short description of how to test it. -->
