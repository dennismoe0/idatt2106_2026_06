UPDATE tasks
SET content_json = '{
  "images": [
    {
      "id": "image_0",
      "src": "/story_pictures/photographer-task-1-manipulated-beach.png",
      "alt": "Barn leker på en strand med flere personer og hus i bakgrunnen. Bildet er endret med KI.",
      "label": "Bilde A",
      "correctType": "AI_GENERATED",
      "wrongFeedback": "Feil, se på barna i bakgrunnen. Er det noe rart her?",
      "correctFeedback": "Riktig! Barna i bildet er satt inn i bildet, mens resten av bildet er ekte."
    },
    {
      "id": "image_1",
      "src": "/story_pictures/photographer-task-1-ai-beach.png",
      "alt": "En strandpromenade med palmer, vei, strand og mennesker. Bildet er KI-generert.",
      "label": "Bilde B",
      "correctType": "AI_GENERATED",
      "wrongFeedback": "Feil, se på menneskene, bygningene og landskapet. Er det noe feil her?",
      "correctFeedback": "Riktig! Bygninger, landskap og mennesker har mange feil. Dette bildet er helt KI-generert."
    }
  ],
  "question": "Sorter hvert bilde: er det ekte eller KI-generert?",
  "explanation": "Se forklaringene under hvert bilde for hvorfor klassifiseringen stemmer."
}',
    correct_answer_json = '{"image_0": "AI_GENERATED", "image_1": "AI_GENERATED"}'
WHERE stop_id = (SELECT id FROM stops WHERE name = 'Fotografen')
  AND order_index = 2;

UPDATE tasks
SET content_json = '{
  "images": [
    {
      "id": "image_0",
      "src": "/story_pictures/photographer-task-2-real-taj.jpg",
      "alt": "Et ekte foto av Taj Mahal med hage, vannløp, besøkende og blå himmel.",
      "label": "Bilde A",
      "correctType": "REAL",
      "wrongFeedback": "Feil, se på lys, skygger og menneskene i bildet. Virker detaljene naturlige og konsistente?",
      "correctFeedback": "Riktig! Dette er et ekte foto. Lys, skygger og menneskene henger naturlig sammen."
    },
    {
      "id": "image_1",
      "src": "/story_pictures/photographer-task-2-manipulated-taj.png",
      "alt": "Taj Mahal med ekstra elementer som luftballong, helikopter, fugler, kamel og elefant lagt inn i scenen.",
      "label": "Bilde B",
      "correctType": "AI_GENERATED",
      "wrongFeedback": "Feil, se etter ting som ikke hører naturlig hjemme i scenen. Er det lagt til noe ekstra?",
      "correctFeedback": "Riktig! Bildet er endret med KI. Luftballong, helikopter og dyr er lagt inn i et ellers realistisk bilde."
    },
    {
      "id": "image_2",
      "src": "/story_pictures/photographer-task-2-ai-taj.png",
      "alt": "Et KI-generert bilde av Taj Mahal med et glattere og mer kunstig uttrykk.",
      "label": "Bilde C",
      "correctType": "AI_GENERATED",
      "wrongFeedback": "Feil, se på overflater, mennesker og bygninger. Virker de litt for glatte eller kunstige?",
      "correctFeedback": "Riktig! Hele scenen er KI-generert, med glatte detaljer og et kunstig preg."
    }
  ],
  "question": "Sorter hvert bilde: ekte eller KI-generert?",
  "explanation": "Se forklaringene under hvert bilde for hvorfor klassifiseringen stemmer."
}',
    correct_answer_json = '{"image_0": "REAL", "image_1": "AI_GENERATED", "image_2": "AI_GENERATED"}'
WHERE stop_id = (SELECT id FROM stops WHERE name = 'Fotografen')
  AND order_index = 3;

UPDATE tasks
SET content_json = '{
  "images": [
    {
      "id": "image_0",
      "src": "/story_pictures/photographer-task-3-manipulated-canal.png",
      "alt": "Panamakanalen med Miraflores Locks, cruiseskip, vann og mange mennesker. Bildet er endret med KI.",
      "label": "Bilde A",
      "correctType": "AI_GENERATED",
      "wrongFeedback": "Feil, se på menneskemengden og detaljene rundt skipet. Er alt naturlig plassert?",
      "correctFeedback": "Riktig! Bildet er endret med KI. Scenen bygger på et ekte sted, men innhold er endret og kan ikke brukes som sikkert bevis."
    },
    {
      "id": "image_1",
      "src": "/story_pictures/photographer-task-3-ai-canal.png",
      "alt": "Et KI-generert bilde av Miraflores Locks ved Panamakanalen med skip, bygning, vann og åser.",
      "label": "Bilde B",
      "correctType": "AI_GENERATED",
      "wrongFeedback": "Feil, se på skip, bygninger og vann. Virker detaljene helt realistiske?",
      "correctFeedback": "Riktig! Dette bildet er KI-generert. Hele scenen er kunstig laget selv om motivet ligner et ekte sted."
    },
    {
      "id": "image_2",
      "src": "/story_pictures/photographer-task-3-real-canal.jpg",
      "alt": "Et ekte foto av Miraflores Locks ved Panamakanalen med et cruiseskip og naturlige kameradetaljer.",
      "label": "Bilde C",
      "correctType": "REAL",
      "wrongFeedback": "Feil, se på lys, kamerastøy og små detaljer. Passer de sammen gjennom hele bildet?",
      "correctFeedback": "Riktig! Dette er det ekte bildet. Lys, kamerastøy og detaljene passer naturlig sammen."
    }
  ],
  "question": "Hvilket bilde kan vi stole på som ekte bevis?",
  "explanation": "Se forklaringene under hvert bilde for hvorfor klassifiseringen stemmer."
}',
    correct_answer_json = '{"image_0": "AI_GENERATED", "image_1": "AI_GENERATED", "image_2": "REAL"}'
WHERE stop_id = (SELECT id FROM stops WHERE name = 'Fotografen')
  AND order_index = 4;
