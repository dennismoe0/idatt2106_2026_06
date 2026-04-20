# Sprint 2 — Full Game Logic Implementation Plan
**Nettdetektivene · Team 6 · IDATT2106 V2026**
Date: 2026-04-20 | Sprint window: Apr 22 PM – Apr 28

---

## 0. What Is Already Done (Sprint 1 baseline)

| Layer | Done |
|-------|------|
| DB schema | All 7 stops seeded, medals seeded, `autoTip` per stop |
| Backend task infra | `GameService.submitAnswer`, XP/stars, medal award, notebook auto-tip |
| Backend validation | `FAKE_NEWS` ✓ `PHISHING_EMAIL` ✓ |
| Frontend task infra | `TaskView.vue`, `StopSummary.vue`, `ConfettiOverlay`, `MedalToast` |
| Frontend components | `FakeNewsTask.vue` ✓ `PhishingEmailTask.vue` ✓ |
| Seeded tasks | Stop 1 (3 tasks) ✓ Stop 2 (3 tasks) ✓ |

**What Sprint 2 must deliver:**
- Stops 3–6: backend validation + task seeds + Vue components
- Stop 7 (Final Boss): dedicated `FinalBossTask.vue` + backend validation + seed
- Full clue/narrative system: DB column, reveal modal, suspect lineup at Stop 4
- All new components registered in `TaskView.vue`

---

## 1. Team Assignment

| Person | Role | Owns |
|--------|------|------|
| **Dennis** | Full-stack | Clue system (DB → frontend), `FinalBossTask.vue`, Final Boss BE validation + seed, `TaskView.vue` integration |
| **Ola** | Frontend | `AIPhotoTask.vue` (Stop 3), `SocialMediaTask.vue` + `SocialPost.vue` (Stop 6) |
| **Krisase** | Frontend | `PasswordTask.vue` + `PasswordStrengthMeter.vue` + `PasswordBuilder.vue` (Stop 4) |
| **Shakti** | Frontend | `MarketplaceTask.vue` (Stop 5) |
| **Kasper** | Backend | `checkAiPhotoAnswer()`, `checkSocialMediaAnswer()`, seeds for stops 3 & 6 |
| **Higgins** | Backend | `checkPasswordAnswer()`, `PasswordStrengthEvaluator`, seeds for stop 4 |
| **Christian** | Backend | `checkMarketplaceAnswer()`, seeds for stop 5 |

---

## 2. Shared Data Contracts

Every dev must read this section. The `contentJson` and `correctAnswerJson` shapes are the contract between backend seeds and frontend components.

### 2.1 AI_PHOTO (Stop 3)

**contentJson** (what the frontend receives — sensitive fields stripped by GameService):
```json
{
  "images": [
    {
      "id": "image_0",
      "src": "/tasks/ai-photo/stop3-task1-a.jpg",
      "alt": "Bilde av en person i park",
      "label": "Bilde A"
    },
    {
      "id": "image_1",
      "src": "/tasks/ai-photo/stop3-task1-b.jpg",
      "alt": "Bilde tatt med mobilkamera",
      "label": "Bilde B"
    }
  ],
  "question": "Sorter hvert bilde: er det ekte, KI-generert eller manipulert?"
}
```

**Full contentJson stored in DB** (extra fields stripped before sending to client):
```json
{
  "images": [
    { "id": "image_0", "src": "...", "alt": "...", "label": "Bilde A", "correctType": "AI_GENERATED" },
    { "id": "image_1", "src": "...", "alt": "...", "label": "Bilde B", "correctType": "REAL" }
  ],
  "question": "...",
  "explanation": "Det første bildet er KI-generert fordi fingrene ser rare ut og bakgrunnen gjentar seg."
}
```

**correctAnswerJson**:
```json
{ "image_0": "AI_GENERATED", "image_1": "REAL" }
```

**Answer submitted by frontend**:
```json
{ "image_0": "AI_GENERATED", "image_1": "REAL" }
```

Valid values for image type: `"REAL"` `"AI_GENERATED"` `"MANIPULATED"`

---

### 2.2 PASSWORD (Stop 4)

Two task subtypes controlled by `"type"` field in contentJson.

**Subtype CHOICE** (tasks 1 & 2):
```json
{
  "type": "CHOICE",
  "question": "Hvilket passord er tryggest?",
  "options": [
    { "id": "a", "value": "Ola123" },
    { "id": "b", "value": "Emma2014" },
    { "id": "c", "value": "Katt" },
    { "id": "d", "value": "F!sk3Taco#92" }
  ],
  "explanation": "F!sk3Taco#92 er sterkest fordi det er langt og blander tegn."
}
```

**correctAnswerJson** for CHOICE:
```json
{ "selected": "d" }
```

**Subtype BUILDER** (task 3):
```json
{
  "type": "BUILDER",
  "question": "Bygg et passord som er sterkt nok til å åpne bankboksen",
  "words": ["Tiger", "Måne", "Pizza", "Hund", "Sol", "Isbjørn"],
  "symbols": ["!", "#", "@", "?", "&", "*"],
  "numbers": ["7", "42", "99", "3", "2026"],
  "minStrength": "STRONG",
  "explanation": "Et sterkt passord er langt og blander store/små bokstaver, tall og tegn."
}
```

**correctAnswerJson** for BUILDER:
```json
{ "minStrength": "STRONG" }
```

**Answer submitted by frontend for BUILDER**:
```json
{ "password": "Tiger!Måne#42" }
```

Backend evaluates strength of submitted `password` string and checks it meets `minStrength`.

---

### 2.3 MARKETPLACE (Stop 5)

Two task subtypes: `IDENTIFY` (tasks 1 & 2) and `RANK` (task 3).

**Subtype IDENTIFY**:
```json
{
  "type": "IDENTIFY",
  "imageUrl": "/tasks/marketplace/stop5-task1.jpg",
  "siteName": "super-deals-norge.xyz",
  "question": "Hva er det mest mistenkelige med denne nettsiden?",
  "options": [
    { "id": "a", "text": "Prisen er for lav til å være sann" },
    { "id": "b", "text": "Siden mangler kontaktinformasjon" },
    { "id": "c", "text": "URL-en er merkelig og ikke et kjent nettsted" },
    { "id": "d", "text": "Alt av dette er mistenkelig" }
  ],
  "explanation": "Alle de tre tegnene er til stede."
}
```

**correctAnswerJson** for IDENTIFY: `{ "selected": "d" }`

**Subtype RANK**:
```json
{
  "type": "RANK",
  "question": "Hvilken av disse nettstedene er mest sannsynlig svindel?",
  "sites": [
    { "id": "a", "name": "komplett.no",            "imageUrl": "/tasks/marketplace/komplett.jpg" },
    { "id": "b", "name": "super-deals-norge.xyz",  "imageUrl": "/tasks/marketplace/scam1.jpg" },
    { "id": "c", "name": "elkjop.no",              "imageUrl": "/tasks/marketplace/elkjop.jpg" },
    { "id": "d", "name": "netthandel-billig.cc",   "imageUrl": "/tasks/marketplace/scam2.jpg" }
  ],
  "explanation": "netthandel-billig.cc bruker et ukjent toppdomene og har ingen kontaktinfo."
}
```

**correctAnswerJson** for RANK: `{ "selected": "b" }` (or whichever is the scam)

**Answer submitted**: `{ "selected": "c" }` (whichever option student picks)

---

### 2.4 SOCIAL_MEDIA (Stop 6)

Two task subtypes: `CHOOSE_ACTION` (task 1) and `IDENTIFY_WORST` (tasks 2 & 3).

**Subtype CHOOSE_ACTION**:
```json
{
  "type": "CHOOSE_ACTION",
  "post": {
    "username": "TrondheimNytt",
    "handle": "@trondheim_nytt",
    "avatar": "📰",
    "content": "DELE DETTE NÅ!!! Ordførerens pengeskandal er MYE VERRE enn noen tror 😱😱",
    "likes": 2847,
    "comments": 431,
    "timestamp": "3 timer siden",
    "verified": false
  },
  "question": "Hva bør du gjøre med dette innlegget?",
  "options": [
    { "id": "SHARE",        "text": "Del det videre med en gang" },
    { "id": "WAIT",         "text": "Vent og se om det dukker opp andre steder" },
    { "id": "CHECK_SOURCES","text": "Sjekk kilden og faktasjekk før du gjør noe" },
    { "id": "ASK_ADULT",    "text": "Spør en voksen" }
  ],
  "explanation": "Kapslås, utropstegn og vage påstander er tegn på manipulerende innhold."
}
```

**correctAnswerJson**: `{ "action": "CHECK_SOURCES" }`

**Answer submitted**: `{ "action": "SHARE" }` (whatever student picks)

**Subtype IDENTIFY_WORST**:
```json
{
  "type": "IDENTIFY_WORST",
  "question": "Hvilket innlegg er mest illegitimt?",
  "posts": [
    {
      "id": "post_0",
      "username": "Ordførerens kontor",
      "handle": "@ordforer_trondheim",
      "avatar": "🏛️",
      "content": "Kommunen jobber med saken. Vi informerer fortløpende på kommunens nettside.",
      "likes": 312,
      "comments": 44,
      "timestamp": "1 time siden",
      "verified": true
    },
    {
      "id": "post_1",
      "username": "SannhetsJegeren99",
      "handle": "@sannhet99",
      "avatar": "👁️",
      "content": "JEG VET HVEM TYVEN ER!! Del dette til alle du kjenner FØR de sletter det 🔥🔥🔥",
      "likes": 18432,
      "comments": 2341,
      "timestamp": "45 min siden",
      "verified": false
    }
  ],
  "explanation": "Innlegg 2 bruker kapslås, urgency og oppfordrer til deling uten bevis."
}
```

**correctAnswerJson**: `{ "selected": "post_1" }`

---

### 2.5 FINAL_BOSS (Stop 7)

One task. All 6 sub-challenges embedded in `contentJson`.

**contentJson** (stripped version sent to client — no `correctAnswer` inside challenges):
```json
{
  "intro": "Backup-planen har startet! Du har 6 sikkerhetssystemer å stoppe.",
  "challenges": [
    {
      "id": 0,
      "type": "FAKE_NEWS",
      "systemName": "Nyhetsfilter",
      "description": "Stopp spredning av falske nyheter",
      "articles": [
        { "id": "article_0", "headline": "Pengene er funnet i utlandet", "source": "NRK.no", "body": "Politiet bekrefter at etterforskerne har sporet transaksjonen." },
        { "id": "article_1", "headline": "AVSLØRT: Ordføreren stjal pengene SELV!!!!", "source": "SannNyhet.xyz", "body": "Anonym kilde sier at ordføreren er den egentlige tyven og at politiet dekker over det." }
      ]
    },
    {
      "id": 1,
      "type": "AI_PHOTO",
      "systemName": "Bildekontroll",
      "description": "Stopp falske bevis",
      "images": [
        { "id": "image_0", "src": "/tasks/ai-photo/boss-b.jpg", "alt": "Bilde av en person ved datamaskin", "label": "Bilde A" }
      ]
    },
    {
      "id": 2,
      "type": "PHISHING_EMAIL",
      "systemName": "E-postskjold",
      "description": "Stopp nye phishing-forsøk",
      "email": {
        "fromName": "Politiet",
        "fromEmail": "politi@norge-sikkerhet.com",
        "subject": "Du er mistenkt – svar umiddelbart",
        "body": "For å unngå arrestasjon, send personnummeret ditt til dette nummeret innen 1 time."
      }
    },
    {
      "id": 3,
      "type": "MARKETPLACE",
      "systemName": "Butikksjekk",
      "description": "Stopp svindelside som samler data",
      "imageUrl": "/tasks/marketplace/boss-site.jpg",
      "question": "Hva er galt med denne nettsiden?",
      "options": [
        { "id": "a", "text": "Ingenting, den ser legitim ut" },
        { "id": "b", "text": "URL-en er falsk og betalingsvalget er utrygt" },
        { "id": "c", "text": "Kun prisen er for lav" }
      ]
    },
    {
      "id": 4,
      "type": "SOCIAL_MEDIA",
      "systemName": "Sosial signaljakt",
      "description": "Stopp ryktespredning",
      "post": {
        "username": "DataTyvenEr",
        "handle": "@datatyven_er",
        "avatar": "🕵️",
        "content": "Nå vet vi HVEM som stjal pengene!! Del dette til alle FØR det slettes!!",
        "likes": 45210,
        "comments": 8832,
        "timestamp": "12 min siden",
        "verified": false
      },
      "question": "Hva bør du gjøre?",
      "options": [
        { "id": "SHARE",        "text": "Del videre med en gang" },
        { "id": "CHECK_SOURCES","text": "Sjekk kilden først" },
        { "id": "IGNORE",       "text": "Ignorer innlegget" }
      ]
    },
    {
      "id": 5,
      "type": "PASSWORD",
      "systemName": "Hovedlåsen",
      "description": "Lås opp den digitale safe og redd pengene",
      "question": "Hvilket passord er sterkt nok til å sikre den redde kontoen?",
      "options": [
        { "id": "a", "value": "admin123" },
        { "id": "b", "value": "Trondheim" },
        { "id": "c", "value": "S0l!Bj0rn#77" },
        { "id": "d", "value": "passord" }
      ]
    }
  ]
}
```

**correctAnswerJson** stored in DB:
```json
{
  "challenge_0": { "article_0": true, "article_1": false },
  "challenge_1": { "image_0": "AI_GENERATED" },
  "challenge_2": { "action": "REPORT" },
  "challenge_3": { "selected": "b" },
  "challenge_4": { "action": "CHECK_SOURCES" },
  "challenge_5": { "selected": "c" }
}
```

**Answer submitted by frontend**:
```json
{
  "challenge_0": { "article_0": true, "article_1": false },
  "challenge_1": { "image_0": "AI_GENERATED" },
  "challenge_2": { "action": "REPORT" },
  "challenge_3": { "selected": "b" },
  "challenge_4": { "action": "CHECK_SOURCES" },
  "challenge_5": { "selected": "c" }
}
```

---

## 3. Clue System — Narrative Red Thread

The 7 suspects (hardcoded in frontend — no DB table needed):

| # | Name | Role | Avatar |
|---|------|------|--------|
| 0 | Birger Bakmann | Byens IT-konsulent | 🧑‍💻 |
| 1 | Sunniva Strand | Reisende journalist | 📸 |
| **2** | **Malte Skygge** | **Nettkafé-eier** ← TYVEN | **☕** |
| 3 | Frida Frost | Skolebibliotekar | 📚 |
| 4 | Ronnie Raske | Leveransebud | 📦 |
| 5 | Tore Tunnel | Anonym blogger | 🕶️ |
| 6 | Kaja Klar | Ordførerens assistent | 🗂️ |

Clue text per stop (seeded into `stops.clue_text`):

| Stop | orderIndex | clue_text |
|------|-----------|-----------|
| Nyhetskvartalet | 1 | Tyven hadde på seg en mørk jakke, rød hette og lyse sko. |
| Postkontoret | 2 | Ordføreren mottok en farlig e-post fra adressen hjelp@by-service.net. |
| Fotografen | 3 | Et ekte bilde viser tyven med en konvolutt utenfor en nettkafé i Bytorget. |
| Passordbanken | 4 | *(No clue text — this stop triggers the SuspectReveal instead)* |
| Markedsplassen | 5 | Svindelbutikken «best-deals-city.xyz» var registrert på en adresse ved Bytorget. |
| Den sosiale møteplassen | 6 | En falsk konto på Fjesbok.no ble opprettet fra nettkafeen på Bytorget. |
| Datasenteret | 7 | *(Final boss — no clue needed)* |

Stop 4 (Passordbanken) sets `showSuspectReveal: true` in `SubmitAnswerResponse` instead of `clueText`.
