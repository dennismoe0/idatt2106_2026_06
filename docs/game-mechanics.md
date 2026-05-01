# Game Mechanics

## Story

A mysterious criminal has stolen the town's digital budget. Students take on the role of junior detectives and must investigate 7 locations across the city to gather clues and unmask the culprit. Each location teaches one internet-safety skill. Solving all 7 stops reveals the criminal's identity.

---

## The 7 Stops

Stops unlock sequentially. Completing stop N unlocks stop N+1. The final stop (Datasenteret) requires all six previous stops to be completed.

| # | Stop | Theme | Task type |
|---|------|-------|-----------|
| 1 | Nyhetskvartalet | Fake news detection | FAKE_NEWS |
| 2 | Fotografen | AI / manipulated image detection | AI_PHOTO |
| 3 | Postkontoret | Phishing email identification | PHISHING_EMAIL |
| 4 | Markedsplassen | Fake marketplace / scam detection | MARKETPLACE |
| 5 | Den sosiale møteplassen | Social media manipulation | SOCIAL_MEDIA |
| 6 | Passordbanken | Password strength evaluation | PASSWORD |
| 7 | Datasenteret | Final Boss — all six types combined | FINAL_BOSS |

---

## Task structure per stop

Each stop contains tasks in a fixed order:

1. **LEARN** — Interactive slides with embedded Q&A that introduce the topic. Always the first task.
2. **Domain tasks** — 2–3 tasks matching the stop's theme (e.g. FAKE_NEWS tasks at Nyhetskvartalet).
3. **CLUE_RIDDLE** — A riddle that, when solved, unlocks the next story clue and the next stop. Always the last task.

All task content is stored as a JSON payload (`content_json`) alongside the task type, so each task type can carry its own data structure without requiring separate tables.

---

## Scoring

### XP
- Completing a task awards XP according to the task's configured value.
- Completing a full stop (all tasks including the clue riddle) awards a stop-completion XP bonus.
- **Weekly XP bonus:** Once per stop per calendar week, a student can claim an additional XP reward by calling the claim-xp endpoint. This encourages returning students without penalising those who completed the stop in a previous week.

### Stars
- Stars are a secondary currency.
- Awarded for correct answers on certain task types and for completing stops.
- Spent in the avatar shop to purchase cosmetic options.

---

## Progression

- Stops are locked by default and unlock one at a time as the previous stop is completed.
- Completing a stop awards a medal specific to that stop.
- Medals unlock avatar options (see Avatar system below).
- Students cannot skip stops.

---

## Boss fight — Datasenteret (Stop 7)

The final stop is a multi-phase boss fight that combines all six previous skill areas.

**Intro:** "Backup-planen har startet!" — an emergency scenario plays out in an animated sequence before the challenges begin.

**Challenge sequence:** Six mini-challenges play in order, one per skill area:

1. FAKE_NEWS
2. AI_PHOTO
3. PHISHING_EMAIL
4. MARKETPLACE
5. SOCIAL_MEDIA
6. PASSWORD

**Retry logic:** Each challenge allows one failed attempt. On the first failure the student can try again. On the second failure the game auto-advances to the next challenge regardless of the answer. This prevents students from getting permanently stuck.

**Completion:** Solving all challenges (or getting through all retries) triggers a victory screen and awards the final medals.

---

## Avatar system

The avatar is a modular SVG paper-doll with the following layers rendered in order:

1. Gender base
2. Skin tone
3. Eyes
4. Hair
5. Outfit
6. Hat
7. Accessory

**Free options** are available from the start. **Premium options** require stars to purchase in the avatar shop. **Medal-locked options** become available only after the student earns a specific medal.

**Color picker:** An advanced color customisation option that must be purchased from the shop before it becomes available.

The avatar is visible on the student's profile and throughout the UI.

---

## Leaderboard

Three leaderboard scopes are available:

| Scope | Description |
|-------|-------------|
| Classroom | All students in the same classroom, ranked by total XP |
| School | All classrooms in the same school, ranked by average class XP |
| Global | All classrooms across all schools |

The leaderboard is accessible from the student's LeaderboardView and shows rank, display name, and XP.

---

## Weekly Mystery

A crowdsourced mystery system where students submit their own mystery scenarios.

### Student flow
1. A student submits a mystery with a title and description from the UkasMysteriumView.
2. If the teacher activates it for the classroom, it appears as the active weekly mystery for all students.
3. Any student can attempt to solve the active mystery by submitting an answer.
4. Correct answers award stars and XP.

### Teacher moderation flow
1. Teacher sees all pending submissions in MysteryModerationView.
2. Teacher can approve, edit, reject, or activate a submission for the classroom.
3. Only one mystery can be active per classroom at a time.

---

## Notebook

Each student has a personal notebook that accumulates content throughout the game.

| Entry type | How it's added |
|------------|---------------|
| Clue | Auto-added when a CLUE_RIDDLE task is completed |
| Tip | Auto-added from LEARN task content |
| Reflection | Student writes manually, tied to a specific stop |
| General note | Student writes manually, not tied to a stop |

The notebook is accessible from the student HUD at any time. Teachers can view any student's notebook in read-only mode from ClassroomDetailView.

---

## Teacher controls

| Feature | Where |
|---------|-------|
| Create / delete classroom | DashboardView |
| Share join code | ClassroomDetailView |
| Approve / kick students | ClassroomDetailView |
| View student progress | ClassroomDetailView |
| View student notebook | ClassroomDetailView |
| Moderate weekly mysteries | MysteryModerationView |
| Mute background music for classroom | ClassroomDetailView |
| Manage school membership | via school endpoints |
