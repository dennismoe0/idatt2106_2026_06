UPDATE tasks t
JOIN stops s ON s.id = t.stop_id
SET
  t.content_json = JSON_OBJECT(
    'email', JSON_OBJECT(
      'fromName', 'DNB',
      'fromEmail', 'kundevarsling@dnb-kundeservice.com',
      'subject', 'Viktig sikkerhetsvarsel: Vi har satt betalingen din på pause',
      'body', 'Hei Oliver,\n\nVi oppdaget et uvanlig forsøk på å gjennomføre en betaling fra kortet ditt på 4 890 kr til Steam Market. Dersom dette ikke ble gjort av deg, må du bekrefte kontoen din innen 30 minutter for å unngå midlertidig sperring av nettbanken.\n\nFor å stoppe betalingen må du logge inn med BankID og kontrollere opplysningene dine her: dnb-kontroll.com/bekreft\n\nHvis du ikke gjør dette i tide, kan kortet og kontoen din bli midlertidig låst av sikkerhetsavdelingen.\n\nMed vennlig hilsen\nDNB Kundeservice',
      'clues', JSON_ARRAY(
        JSON_OBJECT('id', 'sender', 'type', 'sender', 'label', 'kundevarsling@dnb-kundeservice.com', 'isClue', TRUE, 'explanation', 'Avsenderen ser ekte ut ved første blikk, men domenet er ikke dnb.no.'),
        JSON_OBJECT('id', 'link1', 'type', 'link', 'label', 'dnb-kontroll.com/bekreft', 'isClue', TRUE, 'explanation', 'Lenken peker til et annet domene enn banken sin offisielle nettside.'),
        JSON_OBJECT('id', 'urgency', 'type', 'text', 'label', 'innen 30 minutter', 'isClue', TRUE, 'explanation', 'Svindlere bruker tidspress for å få deg til å klikke før du rekker å sjekke.'),
        JSON_OBJECT('id', 'bankid', 'type', 'text', 'label', 'logge inn med BankID', 'isClue', TRUE, 'explanation', 'Phishing prøver ofte å få deg til å oppgi innlogging eller BankID på en falsk side.'),
        JSON_OBJECT('id', 'threat', 'type', 'text', 'label', 'kortet og kontoen din bli midlertidig låst', 'isClue', TRUE, 'explanation', 'Trusler om sperring eller låsing brukes for å skape panikk.'),
        JSON_OBJECT('id', 'greeting', 'type', 'text', 'label', 'Hei Oliver,', 'isClue', FALSE, 'explanation', 'At meldingen bruker navnet ditt betyr ikke at den er ekte. Navn kan være lett å finne eller gjette.'),
        JSON_OBJECT('id', 'merchant', 'type', 'text', 'label', 'Steam Market', 'isClue', FALSE, 'explanation', 'Et kjent navn eller sted i meldingen er ikke i seg selv bevis på svindel. Det er avsender, lenke og presset som avslører mest her.'),
        JSON_OBJECT('id', 'logo', 'type', 'branding', 'label', 'DNB Kundeservice', 'isClue', FALSE, 'explanation', 'Logo og avsendernavn alene er ikke nok. Svindlere kopierer ofte kjente merkevarer for å se troverdige ut.')
      )
    ),
    'explanation', 'E-posten ser profesjonell ut, men avsenderen og lenken er falske. Tidspresset er laget for å stresse deg til å gi fra deg BankID-opplysninger.'
  ),
  t.correct_answer_json = JSON_OBJECT('correctClueIds', JSON_ARRAY('sender', 'link1', 'urgency', 'bankid', 'threat'))
WHERE s.name = 'Postkontoret'
  AND t.task_type = 'PHISHING_EMAIL'
  AND t.order_index = 2;

UPDATE tasks t
JOIN stops s ON s.id = t.stop_id
SET
  t.content_json = JSON_OBJECT(
    'email', JSON_OBJECT(
      'fromName', 'Posten',
      'fromEmail', 'varsling@posten-levering.net',
      'subject', 'Pakken din er forsinket i terminal og trenger betaling',
      'body', 'Hei kunde!\n\nVi forsøkte å sende pakken din videre til utleveringsstedet, men sendingen er stoppet fordi det mangler et lite toll- og behandlingsgebyr på 19 kr. Betal i dag for å unngå at pakken blir sendt i retur til avsender.\n\nPakken vil bli slettet fra systemet hvis betalingen ikke registreres innen kl. 23.00.\n\nBetal gebyret her: posten-oppdatering.net/betaling\n\nHa bankkort klart når du åpner lenken, så går behandlingen raskere.\n\nHilsen Posten',
      'clues', JSON_ARRAY(
        JSON_OBJECT('id', 'sender', 'type', 'sender', 'label', 'varsling@posten-levering.net', 'isClue', TRUE, 'explanation', 'Adressen ligner på Posten, men bruker ikke det offisielle domenet posten.no.'),
        JSON_OBJECT('id', 'link1', 'type', 'link', 'label', 'posten-oppdatering.net/betaling', 'isClue', TRUE, 'explanation', 'Betalingslenken går til en side som ikke tilhører Posten.'),
        JSON_OBJECT('id', 'urgency', 'type', 'text', 'label', 'Betal i dag', 'isClue', TRUE, 'explanation', 'Kunstig hastverk er et vanlig grep i phishing.'),
        JSON_OBJECT('id', 'deadline', 'type', 'text', 'label', 'innen kl. 23.00', 'isClue', TRUE, 'explanation', 'En kort tidsfrist er laget for å få deg til å reagere før du tenker deg om.'),
        JSON_OBJECT('id', 'card', 'type', 'text', 'label', 'Ha bankkort klart', 'isClue', TRUE, 'explanation', 'Meldingen prøver å få deg klar til å oppgi betalingsinformasjon på en ukjent side.'),
        JSON_OBJECT('id', 'greeting', 'type', 'text', 'label', 'Hei kunde!', 'isClue', TRUE, 'explanation', 'En veldig generell hilsen kan være et tegn på at meldingen er sendt ut til mange uten å vite hvem du er.'),
        JSON_OBJECT('id', 'delivery', 'type', 'text', 'label', 'utleveringsstedet', 'isClue', FALSE, 'explanation', 'At meldingen nevner utleveringsstedet er ganske vanlig i ekte pakkemeldinger. Det er ikke det som avslører svindelen her.'),
        JSON_OBJECT('id', 'sender_name', 'type', 'sender_name', 'label', 'Posten', 'isClue', FALSE, 'explanation', 'Avsendernavnet kan se riktig ut selv når selve e-postadressen er falsk.')
      )
    ),
    'explanation', 'Dette ligner på en ekte pakkemelding, men både avsender og lenke er feil. Det lille gebyret og tidspresset er klassiske phishing-grep.'
  ),
  t.correct_answer_json = JSON_OBJECT('correctClueIds', JSON_ARRAY('sender', 'link1', 'urgency', 'deadline', 'card', 'greeting'))
WHERE s.name = 'Postkontoret'
  AND t.task_type = 'PHISHING_EMAIL'
  AND t.order_index = 3;

UPDATE tasks t
JOIN stops s ON s.id = t.stop_id
SET
  t.content_json = JSON_OBJECT(
    'email', JSON_OBJECT(
      'fromName', 'IT-support VGS',
      'fromEmail', 'it-support@skole-login.com',
      'subject', 'Kontoen din mister tilgang til Teams og Canvas i dag',
      'body', 'Hei elev,\n\nVi oppdaterer innloggingen for elever etter flere feilforsøk mot skolekontoer denne uka. For å beholde tilgang til Teams, Canvas og skolemail må du logge inn og bekrefte brukeren din før kl. 14.00 i dag.\n\nBruk skolepassordet ditt på nytt i portalen for å unngå at kontoen blir deaktivert automatisk.\n\nGå til elevportalen her: skole-login.com/verify\n\nDu kan ikke bruke vanlige skoleapper igjen før dette er gjort.\n\nMvh\nIT-support',
      'clues', JSON_ARRAY(
        JSON_OBJECT('id', 'sender', 'type', 'sender', 'label', 'it-support@skole-login.com', 'isClue', TRUE, 'explanation', 'Skolen ville brukt sitt eget domene, ikke skole-login.com.'),
        JSON_OBJECT('id', 'link1', 'type', 'link', 'label', 'skole-login.com/verify', 'isClue', TRUE, 'explanation', 'Lenken leder til et ukjent domene som kan stjele skoleinnloggingen din.'),
        JSON_OBJECT('id', 'urgency', 'type', 'text', 'label', 'før kl. 14.00 i dag', 'isClue', TRUE, 'explanation', 'Tidspress gjør det lettere å lure elever til å handle raskt.'),
        JSON_OBJECT('id', 'password', 'type', 'text', 'label', 'Bruk skolepassordet ditt på nytt', 'isClue', TRUE, 'explanation', 'Det er mistenkelig når en e-post ber deg skrive inn passordet ditt via en lenke.'),
        JSON_OBJECT('id', 'deactivated', 'type', 'text', 'label', 'kontoen blir deaktivert automatisk', 'isClue', TRUE, 'explanation', 'Trussel om å miste tilgang brukes for å stresse deg til å handle raskt.'),
        JSON_OBJECT('id', 'apps', 'type', 'text', 'label', 'Du kan ikke bruke vanlige skoleapper igjen før dette er gjort.', 'isClue', TRUE, 'explanation', 'Meldingen prøver å skremme deg med konsekvenser for å få deg til å klikke.'),
        JSON_OBJECT('id', 'greeting', 'type', 'text', 'label', 'Hei elev,', 'isClue', TRUE, 'explanation', 'En generell hilsen i stedet for navnet ditt kan være et tegn på at meldingen er masseutsendt phishing.'),
        JSON_OBJECT('id', 'services', 'type', 'text', 'label', 'Teams, Canvas og skolemail', 'isClue', FALSE, 'explanation', 'At meldingen nevner ekte tjenester du bruker gjør den ikke automatisk farlig. Det avgjørende er det falske domenet og presset om å logge inn.'),
        JSON_OBJECT('id', 'signature', 'type', 'text', 'label', 'Mvh', 'isClue', FALSE, 'explanation', 'En vanlig avslutning gjør ikke meldingen trygg. Du må fortsatt sjekke avsender og lenke.')
      )
    ),
    'explanation', 'Meldingen ser ut som en vanlig IT-beskjed, men domenet er feil og haster unødvendig. Slike e-poster bør alltid sjekkes i skolens offisielle kanaler før du klikker.'
  ),
  t.correct_answer_json = JSON_OBJECT('correctClueIds', JSON_ARRAY('sender', 'link1', 'urgency', 'password', 'deactivated', 'apps', 'greeting'))
WHERE s.name = 'Postkontoret'
  AND t.task_type = 'PHISHING_EMAIL'
  AND t.order_index = 4;

UPDATE tasks t
JOIN stops s ON s.id = t.stop_id
SET
  t.content_json = JSON_OBJECT(
    'purpose', 'Bruk det du lærte om phishing: sjekk avsender, lenke og kunstig hastverk. Riktig valg viser hvordan tyven kom inn i systemet.',
    'evidence', 'Her er meldingen som ble brukt for å lure en ansatt til å logge inn på en falsk side.',
    'question', 'Hva er det sterkeste phishing-sporet?',
    'options', JSON_ARRAY(
      JSON_OBJECT('id', 'wrong_domain', 'label', 'Lenken går til kommune-sikkerhet.net i stedet for kommunens ekte domene, og det betyr at siden kan være laget for å stjele innloggingen din', 'detail', '   '),
      JSON_OBJECT('id', 'no_emojis', 'label', 'E-posten inneholder ingen emojier', 'detail', '   '),
      JSON_OBJECT('id', 'knows_name', 'label', 'E-posten starter med Hei Kari', 'detail', '   ')
    ),
    'explanation', 'Riktig. Feil domene er et tydelig phishing-spor, fordi svindlere ofte lager nettsider som ligner på ekte innlogginger. Loggene viser at lenken ble åpnet fra nettverket til Xoo Inn Cafe.',
    'email', JSON_OBJECT(
      'fromName', 'Trondheim kommune IT',
      'fromEmail', 'varsling@kommune-sikkerhet.net',
      'subject', 'Viktig: kontoen din må sikres i dag',
      'body', 'Hei Kari,\n\nVi har registrert en sikkerhetsfeil på kontoen din etter uvanlig aktivitet i natt. For å beholde tilgang til e-post og lønnssystem må du bekrefte brukeren din før kl. 13.00 i dag.\n\nLogg inn her: kommune-sikkerhet.net/bekreft\n\nHilsen IT-avdelingen'
    )
  ),
  t.correct_answer_json = JSON_OBJECT('selected', 'wrong_domain')
WHERE s.name = 'Postkontoret'
  AND t.task_type = 'CLUE_RIDDLE'
  AND t.order_index = 5;
