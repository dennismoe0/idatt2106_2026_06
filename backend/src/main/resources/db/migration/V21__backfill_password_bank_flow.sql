UPDATE tasks t
JOIN stops s ON s.id = t.stop_id
SET
  t.title = 'Velg det tryggeste passordet',
  t.description = 'Finn ut hvilket passord som er best.',
  t.difficulty = 1,
  t.task_type = 'PASSWORD',
  t.content_json = JSON_OBJECT(
    'type', 'CHOICE',
    'question', 'Hvilket passord er tryggest?',
    'options', JSON_ARRAY(
      JSON_OBJECT('id', 'a', 'value', 'Ola123'),
      JSON_OBJECT('id', 'b', 'value', 'Emma2014'),
      JSON_OBJECT('id', 'c', 'value', 'Katt'),
      JSON_OBJECT('id', 'd', 'value', 'F!sk3Taco#92')
    ),
    'explanation', 'F!sk3Taco#92 er sterkest fordi det er langt og blander store og små bokstaver, tall og spesialtegn. Navn og årstall er svake.'
  ),
  t.correct_answer_json = JSON_OBJECT('selected', 'd'),
  t.guidance_text = 'Tenk på lengde, variasjon og om passordet inneholder personlig informasjon.'
WHERE s.theme = 'PASSWORD'
  AND t.order_index = 2;

INSERT INTO tasks (stop_id, classroom_id, title, description, difficulty, task_type, content_json, guidance_text, order_index, correct_answer_json)
SELECT
  s.id,
  NULL,
  'Gjør passordet bedre',
  'Velg det passordet som er best forbedret.',
  1,
  'PASSWORD',
  JSON_OBJECT(
    'type', 'CHOICE',
    'question', 'Noen har prøvd å gjøre passordet ''Sander2015'' sterkere. Hvilken versjon er best?',
    'options', JSON_ARRAY(
      JSON_OBJECT('id', 'a', 'value', 'sander2015'),
      JSON_OBJECT('id', 'b', 'value', 'Sander2015!'),
      JSON_OBJECT('id', 'c', 'value', 'S@nder_2015#'),
      JSON_OBJECT('id', 'd', 'value', 'SolKatt!Fjord#22')
    ),
    'explanation', 'SolKatt!Fjord#22 er den beste varianten fordi den ikke inneholder noe personlig, er lang og blander store og små bokstaver, tall og spesialtegn. En slik passordfrase er vanskelig å gjette, selv om noen kjenner deg.'
  ),
  'Tenk på lengde, variasjon og om passordet inneholder personlig informasjon.',
  3,
  JSON_OBJECT('selected', 'd')
FROM stops s
WHERE s.theme = 'PASSWORD'
  AND NOT EXISTS (
    SELECT 1
    FROM tasks existing
    WHERE existing.stop_id = s.id
      AND existing.order_index = 3
  );

UPDATE tasks t
JOIN stops s ON s.id = t.stop_id
SET
  t.title = 'Gjør passordet bedre',
  t.description = 'Velg det passordet som er best forbedret.',
  t.difficulty = 1,
  t.task_type = 'PASSWORD',
  t.content_json = JSON_OBJECT(
    'type', 'CHOICE',
    'question', 'Noen har prøvd å gjøre passordet ''Sander2015'' sterkere. Hvilken versjon er best?',
    'options', JSON_ARRAY(
      JSON_OBJECT('id', 'a', 'value', 'sander2015'),
      JSON_OBJECT('id', 'b', 'value', 'Sander2015!'),
      JSON_OBJECT('id', 'c', 'value', 'S@nder_2015#'),
      JSON_OBJECT('id', 'd', 'value', 'SolKatt!Fjord#22')
    ),
    'explanation', 'SolKatt!Fjord#22 er den beste varianten fordi den ikke inneholder noe personlig, er lang og blander store og små bokstaver, tall og spesialtegn. En slik passordfrase er vanskelig å gjette, selv om noen kjenner deg.'
  ),
  t.correct_answer_json = JSON_OBJECT('selected', 'd'),
  t.guidance_text = 'Tenk på lengde, variasjon og om passordet inneholder personlig informasjon.'
WHERE s.theme = 'PASSWORD'
  AND t.order_index = 3;

INSERT INTO tasks (stop_id, classroom_id, title, description, difficulty, task_type, content_json, guidance_text, order_index, correct_answer_json)
SELECT
  s.id,
  NULL,
  'Bygg et sterkt passord',
  'Bruk brikkene til å lage et passord som er sterkt nok.',
  1,
  'PASSWORD',
  JSON_OBJECT(
    'type', 'BUILDER',
    'question', 'Bygg et passord som er sterkt nok til å låse opp bankboksen',
    'words', JSON_ARRAY('Tiger', 'Måne', 'Pizza', 'Hund', 'Sol', 'Isbjørn', 'Fjord'),
    'symbols', JSON_ARRAY('!', '#', '@', '?', '&', '*'),
    'numbers', JSON_ARRAY('7', '42', '99', '3', '2026'),
    'maxLength', 24,
    'minStrength', 'STRONG',
    'explanation', 'Et sterkt passord er langt, bruker store og små bokstaver, tall og spesialtegn, og inneholder ikke personlig informasjon.'
  ),
  'Tenk på lengde, variasjon og om passordet inneholder personlig informasjon.',
  4,
  JSON_OBJECT('minStrength', 'STRONG')
FROM stops s
WHERE s.theme = 'PASSWORD'
  AND NOT EXISTS (
    SELECT 1
    FROM tasks existing
    WHERE existing.stop_id = s.id
      AND existing.order_index = 4
  );

UPDATE tasks t
JOIN stops s ON s.id = t.stop_id
SET
  t.title = 'Bygg et sterkt passord',
  t.description = 'Bruk brikkene til å lage et passord som er sterkt nok.',
  t.difficulty = 1,
  t.task_type = 'PASSWORD',
  t.content_json = JSON_OBJECT(
    'type', 'BUILDER',
    'question', 'Bygg et passord som er sterkt nok til å låse opp bankboksen',
    'words', JSON_ARRAY('Tiger', 'Måne', 'Pizza', 'Hund', 'Sol', 'Isbjørn', 'Fjord'),
    'symbols', JSON_ARRAY('!', '#', '@', '?', '&', '*'),
    'numbers', JSON_ARRAY('7', '42', '99', '3', '2026'),
    'maxLength', 24,
    'minStrength', 'STRONG',
    'explanation', 'Et sterkt passord er langt, bruker store og små bokstaver, tall og spesialtegn, og inneholder ikke personlig informasjon.'
  ),
  t.correct_answer_json = JSON_OBJECT('minStrength', 'STRONG'),
  t.guidance_text = 'Tenk på lengde, variasjon og om passordet inneholder personlig informasjon.'
WHERE s.theme = 'PASSWORD'
  AND t.order_index = 4;

INSERT INTO tasks (stop_id, classroom_id, title, description, difficulty, task_type, content_json, guidance_text, order_index, correct_answer_json)
SELECT
  s.id,
  NULL,
  'Gåtespor: Passordet i loggen',
  'Det siste sporet handler om passordet tyven brukte på en reservekonto.',
  1,
  'CLUE_RIDDLE',
  JSON_OBJECT(
    'purpose', 'Du bruker det du lærte om passord for å lese et siste digitalt spor. Et lekket passord kan avsløre både vaner og hvem kontoen er knyttet til.',
    'evidence', 'Reservekontoen brukte passordet XooInnAdmin2019.',
    'evidencePassword', 'XooInnAdmin2019',
    'question', 'Hva forteller passordet oss?',
    'options', JSON_ARRAY(
      JSON_OBJECT('id', 'random_strong', 'label', 'Det er et sterkt tilfeldig passord', 'detail', 'Det er ikke tilfeldig: det inneholder sted, rolle og årstall.'),
      JSON_OBJECT('id', 'cafe_admin', 'label', 'Noen med admin-tilgang på Xoo Inn Cafe laget eller kjente kontoen', 'detail', 'Passordet peker mot stedet og en administratorrolle.'),
      JSON_OBJECT('id', 'no_clue', 'label', 'Passord gir aldri etterforskningsspor', 'detail', 'Passord kan ofte avsløre vaner og koblinger.')
    ),
    'explanation', 'Riktig. Passordet peker mot noen med admin-kobling til Xoo Inn Cafe. Dette er det siste sporet før Datasenteret.'
  ),
  'Bruk det du nettopp lærte til å løse en liten sak. Svaret gir et spor du trenger i Datasenteret.',
  5,
  JSON_OBJECT('selected', 'cafe_admin')
FROM stops s
WHERE s.theme = 'PASSWORD'
  AND NOT EXISTS (
    SELECT 1
    FROM tasks existing
    WHERE existing.stop_id = s.id
      AND existing.order_index = 5
  );

UPDATE tasks t
JOIN stops s ON s.id = t.stop_id
SET
  t.title = 'Gåtespor: Passordet i loggen',
  t.description = 'Det siste sporet handler om passordet tyven brukte på en reservekonto.',
  t.difficulty = 1,
  t.task_type = 'CLUE_RIDDLE',
  t.content_json = JSON_OBJECT(
    'purpose', 'Du bruker det du lærte om passord for å lese et siste digitalt spor. Et lekket passord kan avsløre både vaner og hvem kontoen er knyttet til.',
    'evidence', 'Reservekontoen brukte passordet XooInnAdmin2019.',
    'evidencePassword', 'XooInnAdmin2019',
    'question', 'Hva forteller passordet oss?',
    'options', JSON_ARRAY(
      JSON_OBJECT('id', 'random_strong', 'label', 'Det er et sterkt tilfeldig passord', 'detail', 'Det er ikke tilfeldig: det inneholder sted, rolle og årstall.'),
      JSON_OBJECT('id', 'cafe_admin', 'label', 'Noen med admin-tilgang på Xoo Inn Cafe laget eller kjente kontoen', 'detail', 'Passordet peker mot stedet og en administratorrolle.'),
      JSON_OBJECT('id', 'no_clue', 'label', 'Passord gir aldri etterforskningsspor', 'detail', 'Passord kan ofte avsløre vaner og koblinger.')
    ),
    'explanation', 'Riktig. Passordet peker mot noen med admin-kobling til Xoo Inn Cafe. Dette er det siste sporet før Datasenteret.'
  ),
  t.correct_answer_json = JSON_OBJECT('selected', 'cafe_admin'),
  t.guidance_text = 'Bruk det du nettopp lærte til å løse en liten sak. Svaret gir et spor du trenger i Datasenteret.'
WHERE s.theme = 'PASSWORD'
  AND t.order_index = 5;
