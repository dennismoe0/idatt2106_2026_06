UPDATE tasks t
JOIN stops s ON s.id = t.stop_id
SET
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
  t.correct_answer_json = JSON_OBJECT('minStrength', 'STRONG')
WHERE s.theme = 'PASSWORD'
  AND t.order_index = 4
  AND t.task_type = 'PASSWORD';
