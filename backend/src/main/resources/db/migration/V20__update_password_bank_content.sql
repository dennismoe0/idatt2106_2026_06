UPDATE tasks t
JOIN stops s ON t.stop_id = s.id
SET t.content_json = JSON_OBJECT(
  'slides', JSON_ARRAY(
    JSON_OBJECT(
      'icon', '',
      'heading', 'Hva gjør et passord sterkt?',
      'exampleType', 'Eksempel',
      'body', 'Et sterkt passord er langt, unikt og vanskelig å gjette. Lengde betyr mye fordi hvert ekstra tegn gir angripere flere muligheter å prøve. Det bør også være laget av en blanding av store og små bokstaver, tall og tegn, eller av flere tilfeldige ord som ikke handler om deg. Det viktigste er at passordet ikke inneholder navn, brukernavn, lag, skole, kjæledyr eller årstall andre kan finne ut.',
      'examples', JSON_ARRAY(
        'Innlogging: Oliver2014 er svakt fordi det ligner på navn + årstall.',
        'Innlogging: Fotball123 er svakt fordi mange kunne ha gjettet det.',
        'Innlogging: Fjord!TacoMaane42 er mye sterkere fordi det er langt og ikke handler om deg.'
      ),
      'checks', JSON_ARRAY(
        'Langt er bedre enn kort.',
        'Unngå navn, årstall og enkle ord.'
      )
    ),
    JSON_OBJECT(
      'icon', '',
      'heading', 'Hvorfor er enkle passord farlige?',
      'exampleType', 'Eksempel',
      'body', 'Svake passord er farlige fordi angripere ikke trenger å gjette som mennesker. De bruker programmer som prøver tusenvis av vanlige passord, navn, årstall og mønstre på kort tid. Hvis du bruker samme passord flere steder, kan ett datainnbrudd også gi tilgang til andre kontoer. Et svakt passord kan derfor åpne døren videre, selv om bare én konto blir avslørt.',
      'examples', JSON_ARRAY(
        'Vanlige dårlige passord: passord123, 123456, qwerty',
        'Også dårlige: Emma2013 eller Liverpool10, fordi de er lette å gjette'
      ),
      'checks', JSON_ARRAY(
        'Det som er lett å huske, kan også være lett å gjette.',
        'Ikke bruk samme passord flere steder.'
      )
    ),
    JSON_OBJECT(
      'icon', '',
      'heading', 'Hva er en passordfrase?',
      'exampleType', 'Eksempel',
      'body', 'En passordfrase er et langt passord laget av flere ord, ofte med tall eller tegn mellom eller rundt ordene. Den kan være lettere å huske enn en kort og rotete kode, men fortsatt sterk fordi den blir lang. Gode passordfraser bruker tilfeldige ord som ikke forteller noe om deg, for eksempel ord som ikke hører naturlig sammen. Ikke bruk en kjent sangtekst, et sitat eller en setning andre kan gjette.',
      'examples', JSON_ARRAY(
        'Eksempel: Hest!Maanelys42Fjord',
        'Ikke så bra: Oliver!Trondheim2014 fordi det handler om deg'
      ),
      'checks', JSON_ARRAY(
        'Lag noe langt og litt rart.',
        'Bruk forskjellig passord på forskjellige kontoer.'
      )
    )
  ),
  'quiz', JSON_ARRAY(
    JSON_OBJECT(
      'id', 'q1',
      'question', 'Hva gjør et passord sterkest?',
      'options', JSON_ARRAY('Det er enkelt å huske', 'Det inneholder navn og fødselsdato', 'Det er langt og bruker ulike tegn uten personlig info'),
      'correct', 'Det er langt og bruker ulike tegn uten personlig info'
    ),
    JSON_OBJECT(
      'id', 'q2',
      'question', 'Hvilket av disse er et svakt passord?',
      'options', JSON_ARRAY('Sol!Fjord#42Hest', 'Ola2010', 'hX9!wP$3mQ'),
      'correct', 'Ola2010'
    ),
    JSON_OBJECT(
      'id', 'q3',
      'question', 'Hva er en passordfrase?',
      'options', JSON_ARRAY('En rekke tilfeldige ord som danner et langt passord', 'Et langt ord', 'Passordet til telefonen'),
      'correct', 'En rekke tilfeldige ord som danner et langt passord'
    )
  )
)
WHERE s.theme = 'PASSWORD'
  AND t.task_type = 'LEARN'
  AND t.order_index = 1;

UPDATE tasks t
JOIN stops s ON t.stop_id = s.id
SET t.content_json = JSON_SET(t.content_json, '$.maxLength', 24)
WHERE s.theme = 'PASSWORD'
  AND t.task_type = 'PASSWORD'
  AND JSON_UNQUOTE(JSON_EXTRACT(t.content_json, '$.type')) = 'BUILDER';
