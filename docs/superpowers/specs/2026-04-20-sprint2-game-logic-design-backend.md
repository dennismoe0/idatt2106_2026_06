# Sprint 2 — Backend: Kasper + Higgins + Christian
*Part of: 2026-04-20-sprint2-game-logic-design.md*

Each backend dev owns:
1. One new `check*Answer()` method in `GameService.java`
2. Seed data in `DataLoader.java`

The existing pattern for reference is `checkFakeNewsAnswer()` and `checkPhishingEmailAnswer()` — follow the same style exactly.

**Important: do NOT touch any other part of `GameService` except adding your `check*Answer()` method and wiring it into `checkAnswer()`.** Dennis owns the `SubmitAnswerResponse` changes and `checkAnswer()` dispatch structure.

---

## KASPER: Stop 3 (AI_PHOTO) + Stop 6 (SOCIAL_MEDIA)

### 1. Add to `checkAnswer()` in GameService

Wire your two methods into the existing dispatch (after the PHISHING_EMAIL block):

```java
if (task.getTaskType() == TaskType.AI_PHOTO) {
    return checkAiPhotoAnswer(correctAnswer, answer);
}
if (task.getTaskType() == TaskType.SOCIAL_MEDIA) {
    return checkSocialMediaAnswer(correctAnswer, answer);
}
```

### 2. `checkAiPhotoAnswer()`

Each image key in correctAnswer (`image_0`, `image_1`, etc.) must match the submitted value exactly (case-insensitive string comparison).

```java
private boolean checkAiPhotoAnswer(JsonNode correctAnswer, Map<String, Object> answer) {
    Iterator<Map.Entry<String, JsonNode>> fields = correctAnswer.fields();
    while (fields.hasNext()) {
        Map.Entry<String, JsonNode> field = fields.next();
        Object submitted = answer.get(field.getKey());
        if (submitted == null) {
            log.warn("[GameService] AI_PHOTO answer missing key: {}", field.getKey());
            return false;
        }
        if (!field.getValue().asText().equalsIgnoreCase(String.valueOf(submitted))) {
            return false;
        }
    }
    return true;
}
```

### 3. `checkSocialMediaAnswer()`

Two variants based on what key is present in correctAnswer:
- `"action"` key → CHOOSE_ACTION subtype (case-insensitive string match)
- `"selected"` key → IDENTIFY_WORST subtype (case-insensitive string match)

```java
private boolean checkSocialMediaAnswer(JsonNode correctAnswer, Map<String, Object> answer) {
    if (!correctAnswer.path("action").isMissingNode()) {
        // CHOOSE_ACTION subtype
        Object submitted = answer.get("action");
        if (submitted == null || correctAnswer.path("action").isMissingNode()) {
            return false;
        }
        return correctAnswer.path("action").asText().equalsIgnoreCase(String.valueOf(submitted));
    }
    if (!correctAnswer.path("selected").isMissingNode()) {
        // IDENTIFY_WORST subtype
        Object submitted = answer.get("selected");
        if (submitted == null) {
            return false;
        }
        return correctAnswer.path("selected").asText().equalsIgnoreCase(String.valueOf(submitted));
    }
    log.warn("[GameService] SOCIAL_MEDIA correctAnswer has neither 'action' nor 'selected' key");
    return false;
}
```

### 4. DataLoader — Stop 3 seed tasks (AI_PHOTO)

Add a helper method:
```java
private Task aiPhotoTask(Stop stop, int orderIndex, String title, String description,
                          String contentJson, String correctAnswerJson) {
    Task task = baseTask(stop, orderIndex, title, description, TaskType.AI_PHOTO);
    task.setContentJson(contentJson);
    task.setCorrectAnswerJson(correctAnswerJson);
    task.setGuidanceText("Se nøye på detaljene i hvert bilde: hender, bakgrunn, lys og skygger.");
    return task;
}
```

Add 3 tasks for Stop 3 (index 2 in the stops list = `stops.get(2)`):

```java
Stop photoStop = stops.get(2);

aiPhotoTask(photoStop, 1,
    "Parkbilder",
    "Er bildet ekte, KI-generert eller manipulert?",
    """
    {
      "images": [
        {
          "id": "image_0",
          "src": "",
          "alt": "En person sitter på en benk i en park. Fingrene ser litt rare ut.",
          "label": "Bilde A"
        },
        {
          "id": "image_1",
          "src": "",
          "alt": "Utsikt over en by tatt fra et vindu. Normalt mobilbilde.",
          "label": "Bilde B"
        }
      ],
      "question": "Sorter hvert bilde: er det ekte, KI-generert eller manipulert?"
    }
    """,
    """
    { "image_0": "AI_GENERATED", "image_1": "REAL" }
    """
),

aiPhotoTask(photoStop, 2,
    "Bytorget",
    "Finn hvilket bilde som er ekte og kan brukes som bevis.",
    """
    {
      "images": [
        {
          "id": "image_0",
          "src": "",
          "alt": "En person på et torg. Bakgrunnen gjentar seg tydelig.",
          "label": "Bilde A"
        },
        {
          "id": "image_1",
          "src": "",
          "alt": "Et mobilbilde av samme torg. Normalt lys og naturlig bakgrunn.",
          "label": "Bilde B"
        },
        {
          "id": "image_2",
          "src": "",
          "alt": "Et bilde der ansiktet er urealistisk glatt og jevnt.",
          "label": "Bilde C"
        }
      ],
      "question": "Sorter hvert bilde: ekte, KI-generert eller manipulert?"
    }
    """,
    """
    { "image_0": "AI_GENERATED", "image_1": "REAL", "image_2": "MANIPULATED" }
    """
),

aiPhotoTask(photoStop, 3,
    "Bevisbildet",
    "Kun ett bilde kan brukes som ekte bevis. Finn det.",
    """
    {
      "images": [
        {
          "id": "image_0",
          "src": "",
          "alt": "Bilde med uvanlige skygger som ikke stemmer med lyskilden.",
          "label": "Bilde A"
        },
        {
          "id": "image_1",
          "src": "",
          "alt": "Bilde der teksten på skiltene i bakgrunnen er uskarp og uleselig.",
          "label": "Bilde B"
        },
        {
          "id": "image_2",
          "src": "",
          "alt": "Et klart mobilbilde. Alle detaljer ser naturlige ut.",
          "label": "Bilde C"
        }
      ],
      "question": "Hvilket bilde kan vi stole på som ekte bevis?"
    }
    """,
    """
    { "image_0": "MANIPULATED", "image_1": "AI_GENERATED", "image_2": "REAL" }
    """
)
```

### 5. DataLoader — Stop 6 seed tasks (SOCIAL_MEDIA)

Add helper method:
```java
private Task socialMediaTask(Stop stop, int orderIndex, String title, String description,
                              String contentJson, String correctAnswerJson) {
    Task task = baseTask(stop, orderIndex, title, description, TaskType.SOCIAL_MEDIA);
    task.setContentJson(contentJson);
    task.setCorrectAnswerJson(correctAnswerJson);
    task.setGuidanceText("Les innlegget nøye. Tenk over hvilke følelser det prøver å skape.");
    return task;
}
```

Add 3 tasks for Stop 6 (`stops.get(5)`):

```java
Stop socialStop = stops.get(5);

socialMediaTask(socialStop, 1,
    "Del eller vent?",
    "Velg riktig handling.",
    """
    {
      "type": "CHOOSE_ACTION",
      "post": {
        "username": "TrondheimNytt",
        "handle": "@trondheim_nytt",
        "avatar": "📰",
        "content": "DELE DETTE NÅ!!! Ordførerens pengeskandal er MYE VERRE enn noen tror 😱😱😱 Anonym kilde avslører det ingen tør si høyt!!!",
        "likes": 2847,
        "comments": 431,
        "timestamp": "3 timer siden",
        "verified": false
      },
      "question": "Hva bør du gjøre med dette innlegget?",
      "options": [
        { "id": "SHARE",         "text": "Del det videre med en gang" },
        { "id": "WAIT",          "text": "Vent og se om det dukker opp andre steder" },
        { "id": "CHECK_SOURCES", "text": "Sjekk kilden og faktasjekk før du gjør noe" },
        { "id": "ASK_ADULT",     "text": "Spør en voksen" }
      ],
      "explanation": "Kapslås, utropstegn, anonym kilde og oppfordring til hastedeling er alle tegn på manipulerende innhold."
    }
    """,
    """{ "action": "CHECK_SOURCES" }"""
),

socialMediaTask(socialStop, 2,
    "Følelser på sosiale medier",
    "Identifiser hvilke følelser innlegget prøver å skape.",
    """
    {
      "type": "CHOOSE_ACTION",
      "post": {
        "username": "Bekymret Borger",
        "handle": "@bekymret_borger_99",
        "avatar": "😤",
        "content": "Politiet gjør INGENTING. Byen vår er UTRYGG. Del dette til ALLE du kjenner så vi kan stoppe dette galskapet en gang for alle!!!",
        "likes": 9432,
        "comments": 2109,
        "timestamp": "1 time siden",
        "verified": false
      },
      "question": "Hva bør du gjøre?",
      "options": [
        { "id": "SHARE",         "text": "Del med en gang — dette er viktig!" },
        { "id": "CHECK_SOURCES", "text": "Sjekk om det er sant før du deler" },
        { "id": "IGNORE",        "text": "Ignorer innlegget" },
        { "id": "ASK_ADULT",     "text": "Spør en voksen om råd" }
      ],
      "explanation": "Innlegget bruker sinne, kapslås og gruppepress for å få deg til å dele raskt uten å tenke."
    }
    """,
    """{ "action": "CHECK_SOURCES" }"""
),

socialMediaTask(socialStop, 3,
    "Finn det mest illegitime innlegget",
    "Velg innlegget med minst troverdighet.",
    """
    {
      "type": "IDENTIFY_WORST",
      "question": "Hvilket innlegg er mest illegitimt?",
      "posts": [
        {
          "id": "post_0",
          "username": "Ordførerens kontor",
          "handle": "@ordforer_trondheim",
          "avatar": "🏛️",
          "content": "Kommunen jobber aktivt med saken. Vi informerer fortløpende på kommunens offisielle nettside.",
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
          "content": "JEG VET HVEM TYVEN ER!! Myndighetene prøver å dekke over sannheten!! Del dette til ALLE du kjenner FØR de sletter det 🔥🔥🔥",
          "likes": 18432,
          "comments": 2341,
          "timestamp": "45 min siden",
          "verified": false
        },
        {
          "id": "post_2",
          "username": "Lokal Reporter",
          "handle": "@lokal_reporter",
          "avatar": "📝",
          "content": "Politiet bekrefter at etterforskningen pågår. Ingen mistenkte er offentlig navngitt ennå.",
          "likes": 891,
          "comments": 123,
          "timestamp": "2 timer siden",
          "verified": false
        }
      ],
      "explanation": "Innlegg 2 (SannhetsJegeren99) bruker kapslås, udokumenterte påstander, konspirasjonsspråk og oppfordrer til hastedeling — klassiske tegn på manipulerende innhold."
    }
    """,
    """{ "selected": "post_1" }"""
)
```

---

## HIGGINS: Stop 4 (PASSWORD)

### 1. Add to `checkAnswer()` in GameService

```java
if (task.getTaskType() == TaskType.PASSWORD) {
    return checkPasswordAnswer(correctAnswer, answer, task.getContentJson());
}
```

Note: `checkPasswordAnswer` needs access to `contentJson` to determine the subtype (CHOICE vs BUILDER).

### 2. `checkPasswordAnswer()`

```java
private boolean checkPasswordAnswer(JsonNode correctAnswer, Map<String, Object> answer,
                                     String contentJson) {
    try {
        JsonNode content = objectMapper.readTree(contentJson);
        String subtype = content.path("type").asText("CHOICE");

        if ("BUILDER".equals(subtype)) {
            return checkPasswordBuilderAnswer(correctAnswer, answer);
        } else {
            // CHOICE subtype
            Object submitted = answer.get("selected");
            if (submitted == null || correctAnswer.path("selected").isMissingNode()) {
                return false;
            }
            return correctAnswer.path("selected").asText()
                                .equalsIgnoreCase(String.valueOf(submitted));
        }
    } catch (JsonProcessingException e) {
        log.error("[GameService] Failed to parse PASSWORD contentJson", e);
        return false;
    }
}

private boolean checkPasswordBuilderAnswer(JsonNode correctAnswer, Map<String, Object> answer) {
    String requiredStrength = correctAnswer.path("minStrength").asText("STRONG");
    Object submittedPassword = answer.get("password");
    if (submittedPassword == null) {
        log.warn("[GameService] PASSWORD BUILDER answer missing 'password' key");
        return false;
    }
    String strength = evaluatePasswordStrength(String.valueOf(submittedPassword));
    log.info("[GameService] PASSWORD BUILDER submitted strength={} required={}", strength, requiredStrength);

    // Map strength levels to numeric for >= comparison
    int submitted = strengthLevel(strength);
    int required  = strengthLevel(requiredStrength);
    return submitted >= required;
}

private String evaluatePasswordStrength(String password) {
    if (password == null || password.isBlank()) return "WEAK";
    int score = 0;
    if (password.length() >= 12)     score += 3;
    else if (password.length() >= 8) score += 2;
    else if (password.length() >= 6) score += 1;
    if (password.matches(".*[A-ZÆØÅ].*"))          score++;
    if (password.matches(".*[a-zæøå].*"))          score++;
    if (password.matches(".*[0-9].*"))             score++;
    if (password.matches(".*[^A-Za-z0-9æøåÆØÅ].*")) score++;
    if (score <= 3) return "WEAK";
    if (score <= 5) return "MEDIUM";
    return "STRONG";
}

private int strengthLevel(String level) {
    return switch (level.toUpperCase()) {
        case "STRONG" -> 3;
        case "MEDIUM" -> 2;
        default       -> 1;
    };
}
```

### 3. DataLoader — Stop 4 seed tasks (PASSWORD)

Add helper:
```java
private Task passwordTask(Stop stop, int orderIndex, String title, String description,
                           String contentJson, String correctAnswerJson) {
    Task task = baseTask(stop, orderIndex, title, description, TaskType.PASSWORD);
    task.setContentJson(contentJson);
    task.setCorrectAnswerJson(correctAnswerJson);
    task.setGuidanceText("Tenk på lengde, variasjon og om passordet inneholder personlig informasjon.");
    return task;
}
```

Add 3 tasks for Stop 4 (`stops.get(3)`):

```java
Stop passwordStop = stops.get(3);

passwordTask(passwordStop, 1,
    "Velg det tryggeste passordet",
    "Finn ut hvilket passord som er best.",
    """
    {
      "type": "CHOICE",
      "question": "Hvilket passord er tryggest?",
      "options": [
        { "id": "a", "value": "Ola123" },
        { "id": "b", "value": "Emma2014" },
        { "id": "c", "value": "Katt" },
        { "id": "d", "value": "F!sk3Taco#92" }
      ],
      "explanation": "F!sk3Taco#92 er sterkest fordi det er langt og blander store og små bokstaver, tall og spesialtegn. Navn og årstall er svake."
    }
    """,
    """{ "selected": "d" }"""
),

passwordTask(passwordStop, 2,
    "Gjør passordet bedre",
    "Velg det passordet som er best forbedret.",
    """
    {
      "type": "CHOICE",
      "question": "Noen har prøvd å gjøre passordet 'Sander2015' sterkere. Hvilken versjon er best?",
      "options": [
        { "id": "a", "value": "sander2015" },
        { "id": "b", "value": "Sander2015!" },
        { "id": "c", "value": "S@nder_2O15#" },
        { "id": "d", "value": "SolKatt!Fjord#22" }
      ],
      "explanation": "SolKatt!Fjord#22 er sterkest fordi det ikke inneholder personlig informasjon, er langt og blander tegn godt."
    }
    """,
    """{ "selected": "d" }"""
),

passwordTask(passwordStop, 3,
    "Bygg et sterkt passord",
    "Bruk brikkene til å lage et passord som er sterkt nok.",
    """
    {
      "type": "BUILDER",
      "question": "Bygg et passord som er sterkt nok til å låse opp bankboksen",
      "words":   ["Tiger", "Måne", "Pizza", "Hund", "Sol", "Isbjørn", "Fjord"],
      "symbols": ["!", "#", "@", "?", "&", "*"],
      "numbers": ["7", "42", "99", "3", "2026"],
      "minStrength": "STRONG",
      "explanation": "Et sterkt passord er langt, bruker store og små bokstaver, tall og spesialtegn, og inneholder ikke personlig informasjon."
    }
    """,
    """{ "minStrength": "STRONG" }"""
)
```

---

## CHRISTIAN: Stop 5 (MARKETPLACE)

### 1. Add to `checkAnswer()` in GameService

```java
if (task.getTaskType() == TaskType.MARKETPLACE) {
    return checkMarketplaceAnswer(correctAnswer, answer);
}
```

### 2. `checkMarketplaceAnswer()`

Both IDENTIFY and RANK subtypes use `"selected"` key — same validation for both.

```java
private boolean checkMarketplaceAnswer(JsonNode correctAnswer, Map<String, Object> answer) {
    Object submitted = answer.get("selected");
    if (submitted == null || correctAnswer.path("selected").isMissingNode()) {
        log.warn("[GameService] MARKETPLACE answer missing 'selected' key");
        return false;
    }
    return correctAnswer.path("selected").asText()
                        .equalsIgnoreCase(String.valueOf(submitted));
}
```

### 3. DataLoader — Stop 5 seed tasks (MARKETPLACE)

Add helper:
```java
private Task marketplaceTask(Stop stop, int orderIndex, String title, String description,
                              String contentJson, String correctAnswerJson) {
    Task task = baseTask(stop, orderIndex, title, description, TaskType.MARKETPLACE);
    task.setContentJson(contentJson);
    task.setCorrectAnswerJson(correctAnswerJson);
    task.setGuidanceText("Sjekk URL, priser, kontaktinfo og betalingsvalg nøye.");
    return task;
}
```

Add 3 tasks for Stop 5 (`stops.get(4)`):

```java
Stop marketStop = stops.get(4);

marketplaceTask(marketStop, 1,
    "Nettbutikk-vurdering",
    "Hva er det mest mistenkelige med denne nettsiden?",
    """
    {
      "type": "IDENTIFY",
      "imageUrl": "",
      "siteName": "super-deals-norge.xyz",
      "question": "Hva er mest mistenkelig med denne nettsiden?",
      "options": [
        { "id": "a", "text": "Prisen er for lav til å være sann — 90% rabatt" },
        { "id": "b", "text": "Siden mangler kontaktinformasjon og adresse" },
        { "id": "c", "text": "URL-en er ikke et kjent norsk nettsted (.xyz er uvanlig)" },
        { "id": "d", "text": "Alt av dette er mistenkelig" }
      ],
      "explanation": "Alle tre tegnene er varselsignaler: ekstremt lav pris, manglende kontaktinfo og ukjent domeneelevnavn."
    }
    """,
    """{ "selected": "d" }"""
),

marketplaceTask(marketStop, 2,
    "Betalingsvarsel",
    "Velg hva du bør gjøre.",
    """
    {
      "type": "IDENTIFY",
      "imageUrl": "",
      "siteName": "billig-elektronikk.cc",
      "question": "Nettsiden ber deg betale med gavekort. Hva bør du gjøre?",
      "options": [
        { "id": "a", "text": "Kjøp med gavekort — det er raskest" },
        { "id": "b", "text": "Undersøk siden nærmere på internett før du gjør noe" },
        { "id": "c", "text": "Ikke kjøp — betaling med gavekort er et klassisk svindeltriks" },
        { "id": "d", "text": "Send dem en e-post for å dobbeltsjekke" }
      ],
      "explanation": "Betaling med gavekort er nesten alltid svindel — pengene er umulige å spore og aldri mulig å få tilbake."
    }
    """,
    """{ "selected": "c" }"""
),

marketplaceTask(marketStop, 3,
    "Finn svindelsiden",
    "Hvilken av de fire sidene er mest sannsynlig svindel?",
    """
    {
      "type": "RANK",
      "question": "Hvilken av disse nettstedene er mest sannsynlig svindel?",
      "sites": [
        { "id": "a", "name": "komplett.no",          "imageUrl": "" },
        { "id": "b", "name": "netthandel-billig.cc", "imageUrl": "" },
        { "id": "c", "name": "elkjop.no",            "imageUrl": "" },
        { "id": "d", "name": "finn.no",              "imageUrl": "" }
      ],
      "explanation": "netthandel-billig.cc bruker et uvanlig toppdomene (.cc), er ikke et kjent norsk nettsted og har ingen kjent historikk."
    }
    """,
    """{ "selected": "b" }"""
)
```

---

## All Backend Devs: Integration Checklist

After implementing your part:

- [ ] `./mvnw test` passes — no compilation errors
- [ ] Start app locally, hit `GET /api/game/stops/{stopId}/tasks` and verify your tasks appear
- [ ] Submit a correct answer via `POST /api/game/tasks/{taskId}/submit` and verify `correct: true`
- [ ] Submit a wrong answer and verify `correct: false`
- [ ] Verify `stopCompleted: true` fires after the 3rd task of your stop
- [ ] Verify a medal is awarded on stop completion (check DB `student_medals` table)
- [ ] For PASSWORD BUILDER: submit `{ "password": "abc" }` and verify `correct: false`, submit `{ "password": "Tiger!Måne#42" }` and verify `correct: true`
