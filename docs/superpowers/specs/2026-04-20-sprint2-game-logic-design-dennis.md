# Sprint 2 — Dennis: Clue System + Final Boss
*Part of: 2026-04-20-sprint2-game-logic-design.md*

---

## SECTION A: Clue System

### A1. Flyway Migration — `V10__add_clue_text_to_stops.sql`

```sql
ALTER TABLE stops ADD COLUMN clue_text TEXT NULL;
```

### A2. Stop Entity — add field

File: `backend/src/main/java/.../entity/Stop.java`

Add after `autoTip` field:
```java
@Column(columnDefinition = "TEXT")
private String clueText;
```

Lombok `@Getter @Setter` already on class, so no explicit getter/setter needed.

### A3. DataLoader — add clue texts

In `DataLoader.java`, the `stop()` helper currently takes 6 params. Add `clueText` as the 7th:

```java
private Stop stop(String name, String description, String theme,
                  int orderIndex, boolean finalBoss,
                  String autoTip, String clueText) {
    Stop stop = new Stop();
    stop.setName(name);
    stop.setDescription(description);
    stop.setTheme(theme);
    stop.setOrderIndex(orderIndex);
    stop.setFinalBoss(finalBoss);
    stop.setAutoTip(autoTip);
    stop.setClueText(clueText);
    return stop;
}
```

Update all 7 `stop(...)` calls in `run()` to pass clueText as last arg:

```java
stop("Nyhetskvartalet", "...", "FAKE_NEWS", 1, false,
     "Falske nyheter bruker gjerne...",
     "Tyven hadde på seg en mørk jakke, rød hette og lyse sko."),

stop("Postkontoret", "...", "PHISHING_EMAIL", 2, false,
     "Phishing-e-poster later som...",
     "Ordføreren mottok en farlig e-post fra adressen hjelp@by-service.net."),

stop("Fotografen", "...", "AI_PHOTO", 3, false,
     "Bilder kan manipuleres...",
     "Et ekte bilde viser tyven med en konvolutt utenfor en nettkafé i Bytorget."),

stop("Passordbanken", "...", "PASSWORD", 4, false,
     "Et sterkt passord er...",
     null),   // ← null: this stop shows SuspectReveal instead

stop("Markedsplassen", "...", "MARKETPLACE", 5, false,
     "Svindel på nett bruker priser...",
     "Svindelbutikken «best-deals-city.xyz» var registrert på en adresse ved Bytorget."),

stop("Den sosiale møteplassen", "...", "SOCIAL_MEDIA", 6, false,
     "Sosiale medier viser deg...",
     "En falsk konto på Fjesbok.no ble opprettet fra nettkafeen på Bytorget."),

stop("Datasenteret", "...", "FINAL_BOSS", 7, true,
     "Du har nå lært...",
     null),   // ← null: final boss, no clue
```

### A4. SubmitAnswerResponse — add clueText and showSuspectReveal

File: `dto/game/SubmitAnswerResponse.java`

Replace existing record with:
```java
public record SubmitAnswerResponse(
    boolean correct,
    int score,
    String explanation,
    boolean stopCompleted,
    MedalDto medalEarned,
    int starsEarned,
    int xpEarned,
    String clueText,           // null unless stopCompleted and stop has clueText
    boolean showSuspectReveal  // true only when Passordbanken (orderIndex 4) completes
) {}
```

### A5. GameService — populate clueText and showSuspectReveal

In `GameService.submitAnswer()`, find where `SubmitAnswerResponse` is constructed (near the end of the method). It currently looks like:

```java
return new SubmitAnswerResponse(
    correct, score, explanation, stopCompleted,
    medalEarned.orElse(null), starsEarned, xpEarned
);
```

Replace with:
```java
String clueText = null;
boolean showSuspectReveal = false;
if (stopCompleted) {
    Stop completedStop = task.getStop();
    if (completedStop.getOrderIndex() == 4) {
        showSuspectReveal = true;
    } else {
        clueText = completedStop.getClueText(); // may be null, that's fine
    }
}

return new SubmitAnswerResponse(
    correct, score, explanation, stopCompleted,
    medalEarned.orElse(null), starsEarned, xpEarned,
    clueText, showSuspectReveal
);
```

### A6. gameService.js (frontend) — pass through new fields

File: `frontend/src/services/gameService.js`

The `submitAnswer` function returns the API response directly. No change needed — the `clueText` and `showSuspectReveal` fields will appear automatically in the response object.

### A7. game store — no changes needed

`game.js` passes the response through. `TaskView.vue` reads `result.value` directly.

### A8. TaskView.vue — show ClueRevealModal and SuspectLineup

In `TaskView.vue`, add these refs and update the template:

**Script additions** (in `<script setup>`):
```javascript
import ClueRevealModal from '@/components/student/ClueRevealModal.vue'
import SuspectLineup from '@/components/student/SuspectLineup.vue'

const showClueModal = ref(false)
const showSuspectLineup = ref(false)
```

Update `handleCelebration` to also trigger clue/suspect display:
```javascript
function handleCelebration(submitResult) {
  // ... existing confetti/sound logic unchanged ...

  if (submitResult?.stopCompleted) {
    if (submitResult.showSuspectReveal) {
      // Small delay so confetti fires first
      setTimeout(() => { showSuspectLineup.value = true }, 1200)
    } else if (submitResult.clueText) {
      setTimeout(() => { showClueModal.value = true }, 1200)
    }
  }
}

function handleClueModalClosed() {
  showClueModal.value = false
  // Let the normal flow continue (StopSummary via goNext)
}

function handleSuspectChosen() {
  showSuspectLineup.value = false
  // StopSummary will show next time student clicks "Neste" in the task result
}
```

**Template additions** (inside `<main class="task-view">`):
```vue
<ClueRevealModal
  v-if="showClueModal"
  :clue-text="result?.clueText"
  @close="handleClueModalClosed"
/>

<SuspectLineup
  v-if="showSuspectLineup"
  @chosen="handleSuspectChosen"
/>
```

### A9. ClueRevealModal.vue

File: `frontend/src/components/student/ClueRevealModal.vue`

```vue
<template>
  <Teleport to="body">
    <div class="clue-modal-backdrop" @click.self="$emit('close')">
      <div class="clue-modal" role="dialog" aria-modal="true" aria-label="Nytt spor funnet">

        <p class="clue-modal__eyebrow">🔍 NYTT SPOR FUNNET</p>
        <div class="clue-modal__tape clue-modal__tape--left" aria-hidden="true"></div>
        <div class="clue-modal__tape clue-modal__tape--right" aria-hidden="true"></div>

        <div class="clue-modal__card">
          <p class="clue-modal__text">{{ clueText }}</p>
        </div>

        <button class="clue-modal__btn" @click="$emit('close')">
          Legg i notatblokk og fortsett →
        </button>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
defineProps({ clueText: { type: String, required: true } })
defineEmits(['close'])
</script>

<style scoped>
.clue-modal-backdrop {
  position: fixed; inset: 0; z-index: 200;
  background: rgba(0,0,0,0.65);
  display: flex; align-items: center; justify-content: center;
  padding: var(--space-4);
}
.clue-modal {
  position: relative;
  background: #FEF9C3;
  border-radius: var(--radius-sm);
  padding: var(--space-8) var(--space-8) var(--space-6);
  max-width: 480px; width: 100%;
  text-align: center;
  box-shadow: 0 8px 32px rgba(0,0,0,0.45);
  transform: rotate(-1.5deg);
}
.clue-modal__eyebrow {
  margin: 0 0 var(--space-4);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  letter-spacing: 0.15em;
  color: #92400E;
  text-transform: uppercase;
}
.clue-modal__card {
  background: #FFFBEB;
  border: 2px dashed #B45309;
  border-radius: var(--radius-md);
  padding: var(--space-4) var(--space-6);
  margin-bottom: var(--space-6);
}
.clue-modal__text {
  margin: 0;
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: #1C1917;
  line-height: 1.5;
}
.clue-modal__tape {
  position: absolute;
  top: -14px;
  width: 60px; height: 28px;
  background: rgba(251,191,36,0.65);
  border-radius: 3px;
}
.clue-modal__tape--left  { left: 20%; transform: rotate(-3deg); }
.clue-modal__tape--right { right: 20%; transform: rotate(2deg); }
.clue-modal__btn {
  background: #B45309;
  color: #fff;
  border: none;
  border-radius: var(--radius-md);
  padding: var(--space-3) var(--space-6);
  font-size: var(--text-base);
  font-weight: var(--font-bold);
  cursor: pointer;
  transition: background var(--transition-fast);
}
.clue-modal__btn:hover { background: #92400E; }
</style>
```

### A10. SuspectLineup.vue

File: `frontend/src/components/student/SuspectLineup.vue`

```vue
<template>
  <Teleport to="body">
    <div class="lineup-backdrop">
      <div class="lineup" role="dialog" aria-modal="true" aria-label="Identifiser tyven">

        <template v-if="!revealed">
          <p class="lineup__eyebrow">🚔 IDENTIFISER TYVEN</p>
          <h2 class="lineup__title">Vi har nok bevis nå.<br>Hvem er tyven?</h2>
          <p class="lineup__sub">Les profilene og velg hvem du tror det er.</p>

          <div class="lineup__grid">
            <button
              v-for="suspect in SUSPECTS"
              :key="suspect.id"
              class="suspect-card"
              :class="{ 'suspect-card--selected': selected === suspect.id }"
              @click="selected = suspect.id"
            >
              <span class="suspect-card__avatar">{{ suspect.avatar }}</span>
              <strong class="suspect-card__name">{{ suspect.name }}</strong>
              <span class="suspect-card__role">{{ suspect.role }}</span>
            </button>
          </div>

          <button
            class="lineup__confirm"
            :disabled="selected === null"
            @click="reveal"
          >
            Jeg er sikker — avsløring!
          </button>
        </template>

        <template v-else>
          <div class="lineup__reveal">
            <p class="lineup__eyebrow lineup__eyebrow--arrested">🚨 ARRESTERT</p>
            <span class="lineup__reveal-avatar">{{ THIEF.avatar }}</span>
            <h2 class="lineup__reveal-name">{{ THIEF.name }}</h2>
            <p class="lineup__reveal-role">{{ THIEF.role }}</p>
            <p class="lineup__reveal-story">
              Malte Skygge eide nettkafeen der tyveriet ble planlagt og gjennomført.
              Han brukte stjålne passord og sendte phishing-e-poster for å komme inn i systemene.
            </p>
            <p class="lineup__reveal-result">
              🎉 Politiet har arrestert Malte Skygge.<br>
              Pengene til idrettsparken er sikret!
            </p>
            <button class="lineup__confirm" @click="$emit('chosen')">
              Videre til Datasenteret →
            </button>
          </div>
        </template>

      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref } from 'vue'

const emit = defineEmits(['chosen'])

const SUSPECTS = [
  { id: 0, name: 'Birger Bakmann',  role: 'IT-konsulent',          avatar: '🧑‍💻' },
  { id: 1, name: 'Sunniva Strand',  role: 'Reisende journalist',   avatar: '📸'  },
  { id: 2, name: 'Malte Skygge',    role: 'Nettkafé-eier',         avatar: '☕'  },
  { id: 3, name: 'Frida Frost',     role: 'Skolebibliotekar',      avatar: '📚'  },
  { id: 4, name: 'Ronnie Raske',    role: 'Leveransebud',          avatar: '📦'  },
  { id: 5, name: 'Tore Tunnel',     role: 'Anonym blogger',        avatar: '🕶️'  },
  { id: 6, name: 'Kaja Klar',       role: 'Ordførerens assistent', avatar: '🗂️'  },
]
const THIEF = SUSPECTS[2] // Malte Skygge

const selected = ref(null)
const revealed = ref(false)

function reveal() {
  // Always reveal the thief regardless of who student picked — narrative beat
  revealed.value = true
  console.log('[SuspectLineup] Student chose suspect:', selected.value, '— revealing Malte Skygge')
}
</script>

<style scoped>
.lineup-backdrop {
  position: fixed; inset: 0; z-index: 201;
  background: rgba(10,5,0,0.82);
  display: flex; align-items: center; justify-content: center;
  padding: var(--space-4);
  overflow-y: auto;
}
.lineup {
  background: #1C1510;
  border: 2px solid #78350F;
  border-radius: var(--radius-lg);
  padding: var(--space-8);
  max-width: 700px; width: 100%;
  text-align: center;
  color: #FEF3C7;
}
.lineup__eyebrow {
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: #F59E0B;
  margin: 0 0 var(--space-2);
}
.lineup__eyebrow--arrested { color: #EF4444; }
.lineup__title {
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  margin: 0 0 var(--space-2);
  line-height: 1.3;
}
.lineup__sub {
  color: rgba(254,243,199,0.65);
  margin: 0 0 var(--space-6);
}
.lineup__grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: var(--space-3);
  margin-bottom: var(--space-6);
}
.suspect-card {
  background: #292015;
  border: 2px solid #44332A;
  border-radius: var(--radius-md);
  padding: var(--space-3);
  cursor: pointer;
  display: flex; flex-direction: column; align-items: center; gap: var(--space-1);
  transition: border-color var(--transition-fast), background var(--transition-fast);
  color: #FEF3C7;
}
.suspect-card:hover { border-color: #F59E0B; background: #3A2A15; }
.suspect-card--selected { border-color: #EF4444; background: #3A1515; }
.suspect-card__avatar { font-size: 2.2rem; }
.suspect-card__name { font-size: var(--text-sm); font-weight: var(--font-bold); line-height: 1.2; }
.suspect-card__role { font-size: var(--text-xs); color: rgba(254,243,199,0.55); }

.lineup__confirm {
  background: #B45309;
  color: #fff;
  border: none;
  border-radius: var(--radius-md);
  padding: var(--space-3) var(--space-8);
  font-size: var(--text-base);
  font-weight: var(--font-bold);
  cursor: pointer;
  transition: background var(--transition-fast);
}
.lineup__confirm:disabled { opacity: 0.4; cursor: not-allowed; }
.lineup__confirm:not(:disabled):hover { background: #92400E; }

/* Reveal state */
.lineup__reveal { display: flex; flex-direction: column; align-items: center; gap: var(--space-3); }
.lineup__reveal-avatar { font-size: 5rem; }
.lineup__reveal-name { font-size: var(--text-3xl); font-weight: var(--font-bold); margin: 0; color: #EF4444; }
.lineup__reveal-role { color: rgba(254,243,199,0.65); margin: 0; }
.lineup__reveal-story { max-width: 420px; line-height: 1.6; color: rgba(254,243,199,0.85); }
.lineup__reveal-result { font-weight: var(--font-semibold); font-size: var(--text-lg); color: #F59E0B; line-height: 1.5; }
</style>
```

---

## SECTION B: Final Boss — Backend

### B1. GameService — checkFinalBossAnswer + helper

Add to `GameService.java` alongside the other `check*` methods:

```java
private boolean checkFinalBossAnswer(JsonNode correctAnswer, Map<String, Object> answer) {
    for (int i = 0; i < 6; i++) {
        String key = "challenge_" + i;
        JsonNode challengeCorrect = correctAnswer.path(key);
        if (challengeCorrect.isMissingNode()) {
            log.warn("[GameService] FINAL_BOSS correctAnswer missing key: {}", key);
            return false;
        }
        Object raw = answer.get(key);
        if (raw == null) {
            log.warn("[GameService] FINAL_BOSS submitted answer missing key: {}", key);
            return false;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> challengeAnswer = (Map<String, Object>) raw;
        if (!checkChallengeAnswer(challengeCorrect, challengeAnswer)) {
            log.info("[GameService] FINAL_BOSS challenge {} incorrect", i);
            return false;
        }
    }
    return true;
}

private boolean checkChallengeAnswer(JsonNode correct, Map<String, Object> answer) {
    // Dispatch based on which key is present in correctAnswer
    if (!correct.path("action").isMissingNode()) {
        return checkPhishingEmailAnswer(correct, answer);
    }
    if (!correct.path("selected").isMissingNode()) {
        Object submitted = answer.get("selected");
        if (submitted == null) return false;
        return correct.path("selected").asText().equalsIgnoreCase(String.valueOf(submitted));
    }
    // article_N keys → fake news style
    if (correct.fieldNames().hasNext()) {
        String firstKey = correct.fieldNames().next();
        if (firstKey.startsWith("article_")) return checkFakeNewsAnswer(correct, answer);
        if (firstKey.startsWith("image_"))   return checkAiPhotoAnswer(correct, answer);
    }
    log.warn("[GameService] checkChallengeAnswer: unrecognised correct answer shape");
    return false;
}
```

Also wire it into `checkAnswer()`:
```java
if (task.getTaskType() == TaskType.FINAL_BOSS) {
    return checkFinalBossAnswer(correctAnswer, answer);
}
```

### B2. DataLoader — Final Boss seed task

Add to the `taskRepository.saveAll(List.of(...))` call in `DataLoader.java`:

```java
finalBossTask(stops.get(6))
```

Add helper method:
```java
private Task finalBossTask(Stop stop) {
    Task task = baseTask(stop, 1, "Stopp backup-planen",
        "Bruk alt du har lært for å stoppe tyvens automatiske reserveplan.", TaskType.FINAL_BOSS);
    task.setGuidanceText("Du har 6 sikkerhetssystemer å stoppe. Ta dem ett av gangen.");
    task.setContentJson(FINAL_BOSS_CONTENT_JSON); // define as private static final String below
    task.setCorrectAnswerJson("""
        {
          "challenge_0": { "article_0": true, "article_1": false },
          "challenge_1": { "image_0": "AI_GENERATED" },
          "challenge_2": { "action": "REPORT" },
          "challenge_3": { "selected": "b" },
          "challenge_4": { "action": "CHECK_SOURCES" },
          "challenge_5": { "selected": "c" }
        }
        """);
    return task;
}
```

Define `FINAL_BOSS_CONTENT_JSON` as a `private static final String` in DataLoader — use the full contentJson from section 2.5 of the main design doc.

### B3. GameService — strip correctAnswer from FINAL_BOSS contentJson

The existing sanitization in `GameService` strips `explanation`, `isReal`, `correctAction` etc. from contentJson. For FINAL_BOSS, the challenges array contains no `correctAnswer` fields in the client-facing version (the correct answers are only in `correctAnswerJson`), so no extra stripping is needed. Verify the existing sanitizer doesn't break the nested `challenges` array structure.

---

## SECTION C: FinalBossTask.vue

File: `frontend/src/components/student/FinalBossTask.vue`

```vue
<template>
  <section class="boss">
    <!-- Intro screen -->
    <div v-if="phase === 'intro'" class="boss__intro">
      <p class="boss__siren" aria-hidden="true">🚨</p>
      <h2 class="boss__title">Backup-planen har startet!</h2>
      <p class="boss__body">{{ task.contentJson.intro }}</p>
      <div class="boss__systems-preview">
        <div v-for="c in challenges" :key="c.id" class="boss__system-chip">
          <span>{{ SYSTEM_ICONS[c.type] }}</span>
          <span>{{ c.systemName }}</span>
        </div>
      </div>
      <button class="boss__btn boss__btn--start" @click="phase = 'challenge'">
        Start etterforskning →
      </button>
    </div>

    <!-- Active challenge -->
    <template v-else-if="phase === 'challenge'">
      <!-- Progress bar -->
      <div class="boss__progress" :aria-label="`System ${currentIdx + 1} av ${challenges.length}`">
        <div
          v-for="(c, i) in challenges"
          :key="c.id"
          class="boss__progress-step"
          :class="{
            'boss__progress-step--done':    answers[i] !== undefined,
            'boss__progress-step--current': i === currentIdx && answers[i] === undefined
          }"
        >
          <span class="boss__progress-icon">{{ SYSTEM_ICONS[c.type] }}</span>
          <span class="boss__progress-label">{{ c.systemName }}</span>
        </div>
      </div>

      <!-- System header -->
      <div class="boss__system-header">
        <p class="boss__system-number">Sikkerhetssystem {{ currentIdx + 1 }}/{{ challenges.length }}</p>
        <h3 class="boss__system-name">{{ currentChallenge.systemName }}</h3>
        <p class="boss__system-desc">{{ currentChallenge.description }}</p>
      </div>

      <!-- Challenge renderers -->
      <BossFakeNews
        v-if="currentChallenge.type === 'FAKE_NEWS'"
        :challenge="currentChallenge"
        :submitted="answers[currentIdx] !== undefined"
        @answer="recordAnswer"
      />
      <BossAiPhoto
        v-else-if="currentChallenge.type === 'AI_PHOTO'"
        :challenge="currentChallenge"
        :submitted="answers[currentIdx] !== undefined"
        @answer="recordAnswer"
      />
      <BossPhishing
        v-else-if="currentChallenge.type === 'PHISHING_EMAIL'"
        :challenge="currentChallenge"
        :submitted="answers[currentIdx] !== undefined"
        @answer="recordAnswer"
      />
      <BossMarketplace
        v-else-if="currentChallenge.type === 'MARKETPLACE'"
        :challenge="currentChallenge"
        :submitted="answers[currentIdx] !== undefined"
        @answer="recordAnswer"
      />
      <BossSocial
        v-else-if="currentChallenge.type === 'SOCIAL_MEDIA'"
        :challenge="currentChallenge"
        :submitted="answers[currentIdx] !== undefined"
        @answer="recordAnswer"
      />
      <BossPassword
        v-else-if="currentChallenge.type === 'PASSWORD'"
        :challenge="currentChallenge"
        :submitted="answers[currentIdx] !== undefined"
        @answer="recordAnswer"
      />

      <!-- System stopped / next button (shown after answering) -->
      <Transition name="result-slide">
        <div v-if="answers[currentIdx] !== undefined" class="boss__system-stopped">
          <p class="boss__stopped-label">✅ System stoppet!</p>
          <button
            v-if="currentIdx < challenges.length - 1"
            class="boss__btn"
            @click="currentIdx++"
          >
            Neste system →
          </button>
          <button
            v-else
            class="boss__btn boss__btn--finish"
            @click="submitAll"
          >
            Send alle svar →
          </button>
        </div>
      </Transition>
    </template>

    <!-- Result screen -->
    <div v-else-if="phase === 'result'" class="boss__result">
      <template v-if="result?.correct">
        <p class="boss__result-emoji">🎉</p>
        <h2 class="boss__result-title">Du stoppet backup-planen!</h2>
        <p class="boss__result-body">
          Pengene til idrettsparken er reddet.<br>Internettbyen er trygg igjen.
        </p>
        <p class="boss__result-title2">Du er en Mesterdetektiv!</p>
      </template>
      <template v-else>
        <p class="boss__result-emoji">⚡</p>
        <h2 class="boss__result-title">Ikke helt riktig</h2>
        <p class="boss__result-body">{{ result?.explanation }}</p>
      </template>
      <button class="boss__btn" @click="$emit('next')">
        Se oppsummering →
      </button>
    </div>
  </section>
</template>

<script setup>
import { ref, computed } from 'vue'
// Sub-challenge components defined inline below (or as separate files if preferred)
import BossFakeNews    from '@/components/student/boss/BossFakeNews.vue'
import BossAiPhoto     from '@/components/student/boss/BossAiPhoto.vue'
import BossPhishing    from '@/components/student/boss/BossPhishing.vue'
import BossMarketplace from '@/components/student/boss/BossMarketplace.vue'
import BossSocial      from '@/components/student/boss/BossSocial.vue'
import BossPassword    from '@/components/student/boss/BossPassword.vue'

const SYSTEM_ICONS = {
  FAKE_NEWS:      '📰',
  AI_PHOTO:       '📷',
  PHISHING_EMAIL: '📧',
  MARKETPLACE:    '🛒',
  SOCIAL_MEDIA:   '💬',
  PASSWORD:       '🔐',
}

const props = defineProps({
  task:   { type: Object, required: true },
  result: { type: Object, default: null },
})
const emit = defineEmits(['submitted', 'next'])

const phase      = ref('intro')   // 'intro' | 'challenge' | 'result'
const currentIdx = ref(0)
const answers    = ref({})        // index → answer object

const challenges     = computed(() => props.task.contentJson.challenges ?? [])
const currentChallenge = computed(() => challenges.value[currentIdx.value])

function recordAnswer(answer) {
  answers.value[currentIdx.value] = answer
  console.log('[FinalBossTask] challenge', currentIdx.value, 'answered:', answer)
}

function submitAll() {
  const payload = {}
  for (let i = 0; i < challenges.value.length; i++) {
    payload[`challenge_${i}`] = answers.value[i]
  }
  console.log('[FinalBossTask] submitting all challenges:', payload)
  emit('submitted', payload)
}

// Watch for result arriving after submit
import { watch } from 'vue'
watch(() => props.result, (r) => {
  if (r !== null) phase.value = 'result'
})
</script>

<style scoped>
.boss { display: grid; gap: var(--space-4); }

/* Intro */
.boss__intro { text-align: center; display: grid; gap: var(--space-4); }
.boss__siren { font-size: 4rem; margin: 0; }
.boss__title { font-size: var(--text-2xl); font-weight: var(--font-bold); margin: 0; color: var(--color-danger); }
.boss__body  { color: var(--color-text-muted); margin: 0; }
.boss__systems-preview {
  display: flex; flex-wrap: wrap; gap: var(--space-2); justify-content: center;
}
.boss__system-chip {
  display: flex; align-items: center; gap: 6px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-full);
  padding: var(--space-1) var(--space-3);
  font-size: var(--text-sm);
}

/* Progress bar */
.boss__progress {
  display: flex; gap: var(--space-2); overflow-x: auto; padding-bottom: var(--space-2);
}
.boss__progress-step {
  flex: 1; min-width: 70px;
  display: flex; flex-direction: column; align-items: center; gap: 4px;
  padding: var(--space-2);
  border-bottom: 3px solid var(--color-border);
  opacity: 0.5;
  transition: all var(--transition-fast);
  font-size: var(--text-xs);
  text-align: center;
}
.boss__progress-step--done    { border-color: var(--color-success); opacity: 1; color: var(--color-success); }
.boss__progress-step--current { border-color: var(--color-primary); opacity: 1; }
.boss__progress-icon { font-size: 1.4rem; }

/* System header */
.boss__system-header { display: grid; gap: var(--space-1); }
.boss__system-number { margin: 0; font-size: var(--text-xs); font-weight: var(--font-bold); letter-spacing: 0.1em; text-transform: uppercase; color: var(--color-danger); }
.boss__system-name   { margin: 0; font-size: var(--text-xl); font-weight: var(--font-bold); }
.boss__system-desc   { margin: 0; color: var(--color-text-muted); }

/* Stopped banner */
.boss__system-stopped {
  background: var(--color-success-light);
  border: 2px solid var(--color-success);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  display: flex; align-items: center; justify-content: space-between; gap: var(--space-4);
}
.boss__stopped-label { margin: 0; font-weight: var(--font-bold); color: var(--color-success); font-size: var(--text-lg); }

/* Buttons */
.boss__btn {
  background: var(--color-primary); color: var(--color-text-on-dark);
  border: none; border-radius: var(--radius-md);
  padding: var(--space-3) var(--space-6);
  font-size: var(--text-base); font-weight: var(--font-bold);
  cursor: pointer;
  transition: background var(--transition-fast);
}
.boss__btn:hover { background: var(--color-btn-primary-hover); }
.boss__btn--start  { justify-self: center; }
.boss__btn--finish { background: var(--color-success); }
.boss__btn--finish:hover { background: #15803D; }

/* Result */
.boss__result { text-align: center; display: grid; gap: var(--space-4); }
.boss__result-emoji { font-size: 5rem; margin: 0; }
.boss__result-title { font-size: var(--text-2xl); font-weight: var(--font-bold); margin: 0; }
.boss__result-title2 { font-size: var(--text-xl); font-weight: var(--font-bold); color: #B45309; margin: 0; }
.boss__result-body  { color: var(--color-text-muted); margin: 0; line-height: 1.6; }

.result-slide-enter-active { transition: transform 0.3s ease, opacity 0.3s ease; }
.result-slide-enter-from   { transform: translateY(-10px); opacity: 0; }
</style>
```

### C1. Boss Sub-Challenge Components

Create folder: `frontend/src/components/student/boss/`

Each sub-component receives `challenge` (the challenge object from contentJson) and `submitted` (boolean). Emits `answer` with the answer payload when student submits. They are deliberately simple — no fancy inline result, just the core interaction. The parent `FinalBossTask.vue` shows the "System stoppet!" banner.

**BossFakeNews.vue** — `frontend/src/components/student/boss/BossFakeNews.vue`
```vue
<template>
  <div class="mini-task">
    <p class="mini-task__q">{{ challenge.articles ? 'Hvilken artikkel er ekte?' : challenge.question }}</p>
    <div class="mini-task__articles">
      <label
        v-for="(article, i) in challenge.articles"
        :key="i"
        class="mini-article"
        :class="{ 'mini-article--selected': picks[i] === true }"
      >
        <strong>{{ article.headline }}</strong>
        <span class="mini-article__source">{{ article.source }}</span>
        <div class="mini-article__btns">
          <button :disabled="submitted" :class="{ selected: picks[i] === true }"  @click="picks[i] = true">Ekte</button>
          <button :disabled="submitted" :class="{ selected: picks[i] === false }" @click="picks[i] = false">Falsk</button>
        </div>
      </label>
    </div>
    <button class="mini-task__submit" :disabled="submitted || !isReady" @click="submit">Bekreft</button>
  </div>
</template>
<script setup>
import { ref, computed } from 'vue'
const props = defineProps({ challenge: Object, submitted: Boolean })
const emit = defineEmits(['answer'])
const picks = ref({})
const isReady = computed(() =>
  props.challenge.articles?.every((_, i) => picks.value[i] !== undefined)
)
function submit() {
  const ans = {}
  props.challenge.articles.forEach((_, i) => { ans[`article_${i}`] = picks.value[i] })
  emit('answer', ans)
}
</script>
<style scoped>
.mini-task { display: grid; gap: var(--space-3); }
.mini-task__q { font-weight: var(--font-semibold); margin: 0; }
.mini-task__articles { display: grid; gap: var(--space-3); grid-template-columns: repeat(auto-fit, minmax(220px,1fr)); }
.mini-article { border: 2px solid var(--color-border); border-radius: var(--radius-md); padding: var(--space-3); display: grid; gap: var(--space-2); cursor: default; }
.mini-article__source { font-size: var(--text-xs); color: var(--color-text-muted); }
.mini-article__btns { display: flex; gap: var(--space-2); }
.mini-article__btns button { border: 1px solid var(--color-border); background: var(--color-surface); border-radius: var(--radius-sm); padding: var(--space-1) var(--space-2); cursor: pointer; }
.mini-article__btns button.selected { border-color: var(--color-primary); background: var(--color-primary-light); color: var(--color-primary-dark); }
.mini-article__btns button:disabled { opacity: 0.5; cursor: not-allowed; }
.mini-task__submit { background: var(--color-primary); color: #fff; border: none; border-radius: var(--radius-md); padding: var(--space-2) var(--space-5); font-weight: var(--font-bold); cursor: pointer; justify-self: start; }
.mini-task__submit:disabled { opacity: 0.4; cursor: not-allowed; }
</style>
```

**BossAiPhoto.vue** — same pattern, emits `{ image_0: 'AI_GENERATED' }` etc.
```vue
<template>
  <div class="mini-task">
    <div v-for="(img, i) in challenge.images" :key="i" class="mini-photo">
      <div class="mini-photo__img-wrap">
        <img v-if="img.src" :src="img.src" :alt="img.alt" class="mini-photo__img" />
        <div v-else class="mini-photo__placeholder">{{ img.label }}</div>
      </div>
      <div class="mini-photo__options">
        <button v-for="opt in IMAGE_TYPES" :key="opt.value"
          :class="{ selected: picks[i] === opt.value }"
          :disabled="submitted"
          @click="picks[i] = opt.value">{{ opt.label }}</button>
      </div>
    </div>
    <button class="mini-task__submit" :disabled="submitted || !isReady" @click="submit">Bekreft</button>
  </div>
</template>
<script setup>
import { ref, computed } from 'vue'
const IMAGE_TYPES = [
  { value: 'REAL',          label: 'Ekte' },
  { value: 'AI_GENERATED',  label: 'KI-generert' },
  { value: 'MANIPULATED',   label: 'Manipulert' },
]
const props = defineProps({ challenge: Object, submitted: Boolean })
const emit = defineEmits(['answer'])
const picks = ref({})
const isReady = computed(() => props.challenge.images?.every((_, i) => picks.value[i] !== undefined))
function submit() {
  const ans = {}
  props.challenge.images.forEach((_, i) => { ans[`image_${i}`] = picks.value[i] })
  emit('answer', ans)
}
</script>
<style scoped>
.mini-task { display: grid; gap: var(--space-3); }
.mini-photo { display: grid; gap: var(--space-2); }
.mini-photo__img-wrap { border-radius: var(--radius-md); overflow: hidden; background: var(--color-surface); border: 1px solid var(--color-border); }
.mini-photo__img { width: 100%; max-height: 200px; object-fit: cover; display: block; }
.mini-photo__placeholder { height: 140px; display: flex; align-items: center; justify-content: center; color: var(--color-text-muted); font-size: var(--text-lg); }
.mini-photo__options { display: flex; gap: var(--space-2); flex-wrap: wrap; }
.mini-photo__options button { border: 1px solid var(--color-border); background: var(--color-surface); border-radius: var(--radius-sm); padding: var(--space-1) var(--space-3); cursor: pointer; }
.mini-photo__options button.selected { border-color: var(--color-primary); background: var(--color-primary-light); color: var(--color-primary-dark); }
.mini-photo__options button:disabled { opacity: 0.5; cursor: not-allowed; }
.mini-task__submit { background: var(--color-primary); color: #fff; border: none; border-radius: var(--radius-md); padding: var(--space-2) var(--space-5); font-weight: var(--font-bold); cursor: pointer; justify-self: start; }
.mini-task__submit:disabled { opacity: 0.4; cursor: not-allowed; }
</style>
```

**BossPhishing.vue** — shows email, student picks action from 4 options, emits `{ action: 'REPORT' }`:
```vue
<template>
  <div class="mini-task">
    <div class="mini-email">
      <p><strong>Fra:</strong> {{ challenge.email.fromName }} &lt;{{ challenge.email.fromEmail }}&gt;</p>
      <p><strong>Emne:</strong> {{ challenge.email.subject }}</p>
      <p class="mini-email__body">{{ challenge.email.body }}</p>
    </div>
    <p class="mini-task__q">{{ challenge.question || 'Hva bør du gjøre?' }}</p>
    <div class="mini-task__options">
      <button v-for="opt in challenge.options" :key="opt.id"
        :class="{ selected: pick === opt.id }"
        :disabled="submitted"
        @click="pick = opt.id">{{ opt.text }}</button>
    </div>
    <button class="mini-task__submit" :disabled="submitted || !pick" @click="$emit('answer', { action: pick })">Bekreft</button>
  </div>
</template>
<script setup>
import { ref } from 'vue'
const props = defineProps({ challenge: Object, submitted: Boolean })
defineEmits(['answer'])
const pick = ref(null)
</script>
<style scoped>
.mini-task { display: grid; gap: var(--space-3); }
.mini-task__q { font-weight: var(--font-semibold); margin: 0; }
.mini-email { border: 1px solid var(--color-border); border-radius: var(--radius-md); padding: var(--space-3); background: var(--color-surface); font-size: var(--text-sm); display: grid; gap: var(--space-1); }
.mini-email p { margin: 0; }
.mini-email__body { padding-top: var(--space-2); border-top: 1px solid var(--color-border); }
.mini-task__options { display: grid; grid-template-columns: repeat(auto-fit, minmax(140px,1fr)); gap: var(--space-2); }
.mini-task__options button { border: 1px solid var(--color-border); background: var(--color-surface); border-radius: var(--radius-sm); padding: var(--space-2) var(--space-3); cursor: pointer; }
.mini-task__options button.selected { border-color: var(--color-primary); background: var(--color-primary-light); color: var(--color-primary-dark); }
.mini-task__options button:disabled { opacity: 0.5; cursor: not-allowed; }
.mini-task__submit { background: var(--color-primary); color: #fff; border: none; border-radius: var(--radius-md); padding: var(--space-2) var(--space-5); font-weight: var(--font-bold); cursor: pointer; justify-self: start; }
.mini-task__submit:disabled { opacity: 0.4; cursor: not-allowed; }
</style>
```

**BossMarketplace.vue** and **BossSocial.vue** and **BossPassword.vue** all follow the same micro-pattern:
- Show the relevant content (image/post/options)
- Single or multiple choice buttons
- Emit `{ selected: 'X' }` or `{ action: 'X' }`

They are identical in structure to `BossPhishing.vue` — just swap the content display. Dennis should scaffold all 6 sub-components in one pass, they're mostly copy-paste with minor content differences.

---

## SECTION D: TaskView.vue — Final Registration

Dennis adds all new imports and `v-else-if` blocks to `TaskView.vue`:

```vue
<!-- Add to imports in <script setup> -->
import AIPhotoTask    from '@/components/student/AIPhotoTask.vue'
import PasswordTask   from '@/components/student/PasswordTask.vue'
import MarketplaceTask from '@/components/student/MarketplaceTask.vue'
import SocialMediaTask from '@/components/student/SocialMediaTask.vue'
import FinalBossTask  from '@/components/student/FinalBossTask.vue'
import ClueRevealModal from '@/components/student/ClueRevealModal.vue'
import SuspectLineup  from '@/components/student/SuspectLineup.vue'
```

```vue
<!-- In template, after PhishingEmailTask block -->
<AIPhotoTask
  v-else-if="currentTask.taskType === 'AI_PHOTO'"
  :task="currentTask"
  :result="result"
  :is-last-task="currentTaskIndex === tasks.length - 1"
  @submitted="handleSubmit"
  @next="goNext"
/>

<PasswordTask
  v-else-if="currentTask.taskType === 'PASSWORD'"
  :task="currentTask"
  :result="result"
  :is-last-task="currentTaskIndex === tasks.length - 1"
  @submitted="handleSubmit"
  @next="goNext"
/>

<MarketplaceTask
  v-else-if="currentTask.taskType === 'MARKETPLACE'"
  :task="currentTask"
  :result="result"
  :is-last-task="currentTaskIndex === tasks.length - 1"
  @submitted="handleSubmit"
  @next="goNext"
/>

<SocialMediaTask
  v-else-if="currentTask.taskType === 'SOCIAL_MEDIA'"
  :task="currentTask"
  :result="result"
  :is-last-task="currentTaskIndex === tasks.length - 1"
  @submitted="handleSubmit"
  @next="goNext"
/>

<FinalBossTask
  v-else-if="currentTask.taskType === 'FINAL_BOSS'"
  :task="currentTask"
  :result="result"
  @submitted="handleSubmit"
  @next="goNext"
/>

<!-- Add at end of <main>, before ConfettiOverlay -->
<ClueRevealModal
  v-if="showClueModal"
  :clue-text="result?.clueText"
  @close="handleClueModalClosed"
/>
<SuspectLineup
  v-if="showSuspectLineup"
  @chosen="handleSuspectChosen"
/>
```
