UPDATE tasks t
JOIN stops s ON s.id = t.stop_id
SET
  t.content_json = JSON_OBJECT(
    'type', 'CHOICE',
    'question', 'Noen har prøvd å gjøre passordet ''Sander2015'' sterkere. Hvilken versjon er best?',
    'options', JSON_ARRAY(
      JSON_OBJECT('id', 'a', 'value', 'sander2015'),
      JSON_OBJECT('id', 'b', 'value', 'Sander2015!'),
      JSON_OBJECT('id', 'c', 'value', 'S@nder_2015#'),
      JSON_OBJECT('id', 'd', 'value', 'SolKatt!Fjord#22')
    ),
    'explanation', 'S@nder_2015# er den beste forbedringen av alternativene fordi det blander store og små bokstaver og legger til et spesialtegn. Det er fortsatt ikke et ideelt passord, siden navnet og årstallet fortsatt kan være lette å gjette.'
  ),
  t.correct_answer_json = JSON_OBJECT('selected', 'c')
WHERE s.theme = 'PASSWORD'
  AND t.order_index = 3;

UPDATE tasks t
JOIN stops s ON s.id = t.stop_id
SET
  t.content_json = JSON_OBJECT(
    'purpose', 'Du bruker det du lærte om passord for å lese et siste digitalt spor. Et lekket passord kan avsløre både vaner og hvem kontoen er knyttet til.',
    'evidence', 'Reservekontoen brukte passordet XooInnAdmin2019.',
    'question', 'Hva forteller passordet oss?',
    'options', JSON_ARRAY(
      JSON_OBJECT('id', 'random_strong', 'label', 'Det er et sterkt tilfeldig passord', 'detail', 'Det er ikke tilfeldig: det inneholder sted, rolle og årstall.'),
      JSON_OBJECT('id', 'cafe_admin', 'label', 'Noen med admin-tilgang på Xoo Inn Cafe laget eller kjente kontoen', 'detail', 'Passordet peker mot stedet og en administratorrolle.'),
      JSON_OBJECT('id', 'no_clue', 'label', 'Passord gir aldri etterforskningsspor', 'detail', 'Passord kan ofte avsløre vaner og koblinger.')
    ),
    'explanation', 'Riktig. Passordet peker mot noen med admin-kobling til Xoo Inn Cafe. Dette er det siste sporet før Datasenteret.'
  )
WHERE s.theme = 'PASSWORD'
  AND t.order_index = 5
  AND t.task_type = 'CLUE_RIDDLE';
