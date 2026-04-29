UPDATE tasks
SET content_json = '{
  "images": [
    {
      "id": "image_0",
      "src": "/story_pictures/photographer-task-1-manipulated-beach.png",
      "alt": "Barn leker på en strand med flere personer og hus i bakgrunnen. Bildet er manipulert med KI.",
      "label": "Bilde A",
      "explanation": "Dette bildet er manipulert med KI. Det kan se ut som et vanlig strandfoto, men innholdet er endret slik at scenen ikke er et pålitelig bevis på hva som faktisk skjedde."
    },
    {
      "id": "image_1",
      "src": "/story_pictures/photographer-task-1-ai-beach.png",
      "alt": "En strandpromenade med palmer, vei, strand og mennesker. Bildet er KI-generert.",
      "label": "Bilde B",
      "explanation": "Dette bildet er KI-generert. Hele scenen er laget kunstig, selv om lys, strand og bygninger kan virke realistiske ved første blikk."
    }
  ],
  "question": "Sorter hvert bilde: er det ekte, KI-generert eller manipulert?"
}',
    correct_answer_json = '{"image_0": "MANIPULATED", "image_1": "AI_GENERATED"}'
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
      "explanation": "Dette er det ekte bildet. Det har naturlige kameradetaljer, vanlige variasjoner i mennesker og omgivelser, og scenen virker konsistent uten ekstra elementer som er lagt inn."
    },
    {
      "id": "image_1",
      "src": "/story_pictures/photographer-task-2-manipulated-taj.png",
      "alt": "Taj Mahal med ekstra elementer som luftballong, helikopter, fugler, kamel og elefant lagt inn i scenen.",
      "label": "Bilde B",
      "explanation": "Dette bildet er manipulert. Det bygger på den samme scenen, men flere elementer er lagt til etterpå, som luftballong, helikopter, dyr og ekstra personer."
    },
    {
      "id": "image_2",
      "src": "/story_pictures/photographer-task-2-ai-taj.png",
      "alt": "Et KI-generert bilde av Taj Mahal med et glattere og mer kunstig uttrykk.",
      "label": "Bilde C",
      "explanation": "Dette bildet er KI-generert. Det ligner på et fotografi, men hele scenen er laget kunstig og har et glattere, mer konstruert preg enn originalfotoet."
    }
  ],
  "question": "Sorter hvert bilde: ekte, KI-generert eller manipulert?"
}',
    correct_answer_json = '{"image_0": "REAL", "image_1": "MANIPULATED", "image_2": "AI_GENERATED"}'
WHERE stop_id = (SELECT id FROM stops WHERE name = 'Fotografen')
  AND order_index = 3;

UPDATE tasks
SET content_json = '{
  "images": [
    {
      "id": "image_0",
      "src": "/story_pictures/photographer-task-3-manipulated-canal.png",
      "alt": "Panamakanalen med Miraflores Locks, cruiseskip, vann og mange mennesker. Bildet er manipulert.",
      "label": "Bilde A",
      "explanation": "Dette bildet er manipulert. Det bygger på en realistisk scene, men innholdet er endret slik at bildet ikke kan brukes som et sikkert bevis alene."
    },
    {
      "id": "image_1",
      "src": "/story_pictures/photographer-task-3-ai-canal.png",
      "alt": "Et KI-generert bilde av Miraflores Locks ved Panamakanalen med skip, bygning, vann og åser.",
      "label": "Bilde B",
      "explanation": "Dette bildet er KI-generert. Det prøver å ligne et ekte foto fra samme sted, men hele scenen er kunstig laget."
    },
    {
      "id": "image_2",
      "src": "/story_pictures/photographer-task-3-real-canal.jpg",
      "alt": "Et ekte foto av Miraflores Locks ved Panamakanalen med et cruiseskip og naturlige kameradetaljer.",
      "label": "Bilde C",
      "explanation": "Dette er det ekte bildet. Det har naturlig lys, kamerastøy og små uperfekte detaljer som passer sammen gjennom hele scenen."
    }
  ],
  "question": "Hvilket bilde kan vi stole på som ekte bevis?"
}',
    correct_answer_json = '{"image_0": "MANIPULATED", "image_1": "AI_GENERATED", "image_2": "REAL"}'
WHERE stop_id = (SELECT id FROM stops WHERE name = 'Fotografen')
  AND order_index = 4;
