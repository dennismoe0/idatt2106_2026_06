UPDATE stops
SET clue_text = CASE name
  WHEN 'Nyhetskvartalet' THEN 'Basser Gravling var ikke aktiv på noen systemer da nyheten ble publisert.'
  WHEN 'Fotografen' THEN 'Hoppesprett var på cafeen, men hun var ikke aktiv på nettet. Hun var på cafeen med venner.'
  WHEN 'Postkontoret' THEN 'Ulvan Bites har ikke tilkobling til Dyr og Frosker AS. Han er usannsynlig tyven.'
  WHEN 'Markedsplassen' THEN 'Pondus Grisling har ikke tilgang til Dyr og Frosker AS sitt lokale i Kjøttmeisgata, og han var live uten pause mens nettbutikken ble laget.'
  WHEN 'Den sosiale møteplassen' THEN 'Millie Mus og Snikrev Revesen er våre to hovedmistenkte.'
  WHEN 'Passordbanken' THEN 'Innloggingstidspunktet passer med Snikrev, ikke med Millie.'
  ELSE clue_text
END
WHERE name IN ('Nyhetskvartalet', 'Fotografen', 'Postkontoret', 'Markedsplassen', 'Den sosiale møteplassen', 'Passordbanken');

UPDATE tasks
SET title = 'Gåtespor: Falsk nyhet',
    description = 'En nyhet prøver å villede etterforskningen. Du må finne den falske og se hvem som kan kobles til tidspunktet den ble lagt ut.',
    content_json = JSON_OBJECT(
      'purpose', 'En nyhet prøver å villede etterforskningen. Du må bruke kildekritikk for å finne den falske nyheten: sjekk hvem som publiserer, om påstanden har bevis, og om språket prøver å styre mistanken mot noen uten dokumentasjon.',
      'evidence', 'Når den falske nyheten er funnet, sjekker du tidspunktet artikkelen ble publisert. Systemet kan da vise hvem som var logget på nettverket til Xoo Inn Cafe da nyheten ble lagt ut.',
      'evidencePassword', NULL,
      'question', 'Hvilken nyhet er falsk?',
      'options', JSON_ARRAY(
        JSON_OBJECT('id', 'municipality_account', 'label', 'Kommunen bekrefter sperret konto', 'detail', 'Publisert på kommunens egen side med rolig språk, tidspunkt og kontaktperson.', 'source', 'dyreby.kommune.no', 'body', 'Kommunen bekrefter at prosjektkontoen er sperret mens politiet undersøker digitale spor.'),
        JSON_OBJECT('id', 'foreign_hacker_group', 'label', 'EKSTRA: Tyven er en utenlandsk hackergruppe', 'detail', 'Saken skylder på noen langt borte uten kilde, dokumentasjon eller spor som kan kontrolleres.', 'source', 'DårligNytt24', 'body', 'Anonyme eksperter sier at en ukjent hackergruppe står bak. Ingen lokale spor trenger undersøkes.'),
        JSON_OBJECT('id', 'police_digital_tracks', 'label', 'Politiet undersøker digitale spor etter overføringen', 'detail', 'Kort og etterprøvbar melding fra politiets kanal.', 'source', 'politiet.no', 'body', 'Politiet ber innbyggere vente på bekreftet informasjon mens digitale logger gjennomgås.')
      ),
      'explanation', 'Riktig. Nyheten prøver å skylde på noen langt borte uten bevis.',
      'caseNumber', 1,
      'variant', 'news',
      'result', JSON_ARRAY('Du sjekker tidspunktet artikkelen ble publisert.', 'Systemet viser hvem som var logget på nettverket til Xoo Inn Cafe.', 'Alle mistenkte var på Xoo Inn Cafe den kvelden', 'unntatt én.'),
      'logic', 'Basser Gravling var på familieselskap da den falske artikkelen ble lagt ut, og er sannsynlig helt uskyldig i saken.',
      'elimination', 'Basser Gravling fjernes.',
      'clue', 'Basser Gravling var ikke aktiv på noen systemer da nyheten ble publisert.',
      'backgroundImage', '/story_pictures/clue-board-bg.png'
    ),
    correct_answer_json = JSON_OBJECT('selected', 'foreign_hacker_group')
WHERE stop_id = (SELECT id FROM stops WHERE name = 'Nyhetskvartalet')
  AND order_index = 5;

UPDATE tasks
SET title = 'Gåtespor: Ekte bilde',
    description = 'Det har blitt spredt mange bilder av Hoppesprett på Xoo Cafe med en PC. Mange i landsbyen beskylder henne for å være tyven. Hoppesprett er fortvilet og sier hun aldri ville gjort noe sånt. Hun forklarer også at hun var på cafeen med to venninner. Hun mener bildene som er spredt ikke kan være ekte. Som etterforsker trenger vi at du finner ut om bildene er ekte eller ikke.',
    content_json = JSON_OBJECT(
      'purpose', 'Som etterforsker trenger vi at du finner ut om bildene er ekte eller ikke. Bildene brukes som bevis mot Hoppesprett, så du må undersøke dem nøye før landsbyen får trekke en konklusjon.',
      'evidence', 'Du må avgjøre om bildene er ekte eller ikke. Du kan velge flere enn ett bilde, fordi flere bilder kan være falske samtidig.',
      'evidencePassword', NULL,
      'question', 'Du må avgjøre om bildene er ekte eller ikke. Hva er ekte, og hva er ikke ekte? Du kan velge flere enn ett bilde.',
      'options', JSON_ARRAY(
        JSON_OBJECT('id', 'photo_a', 'label', 'Bilde A', 'imageUrl', '/story_pictures/clue-photo-hoppesprett-a.png', 'alt', 'Falskt KI-generert bilde av Hoppesprett ved en PC på Xoo Cafe.'),
        JSON_OBJECT('id', 'photo_b', 'label', 'Bilde B', 'imageUrl', '/story_pictures/clue-photo-hoppesprett-b.png', 'alt', 'Falskt KI-generert bilde av Hoppesprett med kode på en laptop.'),
        JSON_OBJECT('id', 'photo_c', 'label', 'Bilde C', 'imageUrl', '/story_pictures/clue-photo-hoppesprett-c.png', 'alt', 'Falskt KI-generert bilde av Hoppesprett ved en laptop i cafeen.')
      ),
      'explanation', 'Riktig. Alle bildene er KI-genererte og er falske!',
      'caseNumber', 2,
      'variant', 'photo',
      'selectionMode', 'multi',
      'result', JSON_ARRAY('Alle bildene av Hoppesprett er falske.', 'Hun var på cafe med venner.', 'Vennene til Hoppesprett kan bekrefte dette!'),
      'logic', 'Bildene lyver, og Hoppesprett var ikke aktiv på nettet på tidspunktet for tyveriet og spredningen av de falske nyhetene.',
      'elimination', 'Hoppesprett fjernes.',
      'clue', 'Hoppesprett var på cafeen, men hun var ikke aktiv på nettet. Hun var på cafeen med venner.',
      'backgroundImage', '/story_pictures/clue-board-bg.png'
    ),
    correct_answer_json = JSON_OBJECT('selected', JSON_ARRAY('photo_a', 'photo_b', 'photo_c'))
WHERE stop_id = (SELECT id FROM stops WHERE name = 'Fotografen')
  AND order_index = 5;

UPDATE tasks
SET title = 'Gåtespor: Phishing',
    description = 'En e-post ble brukt for å få tilgang til kontoer. Denne e-posten var med på å stjele informasjon som ble brukt til å stjele pengene fra ordføreren.',
    content_json = JSON_OBJECT(
      'purpose', 'På dagen tyveriet skjedde, fikk ordføreren tre e-poster. En av e-postene ser mistenkelig ut og er sannsynligvis brukt av tyven.',
      'evidence', 'To av e-postene kommer fra vanlige kontoer: hare.haresen@zoo.com og frosk.kvekk@dyremail.com. Den tredje kommer fra patterOGjur@zoo.xz og prøver å få borgemesteren til å gi fra seg informasjon. Se etter faretegnene du lærte i læringsfasen.',
      'evidencePassword', NULL,
      'question', 'På dagen tyveriet skjedde, fikk ordføreren tre e-poster. Hvilken av e-postene ser mistenkelig ut? Denne er sannsynligvis brukt av tyven!',
      'options', JSON_ARRAY(
        JSON_OBJECT('id', 'hare_meeting', 'label', 'A', 'detail', 'Dette er en normal møteavtale uten lenker, trusler eller innloggingskrav.', 'from', 'hare.haresen@zoo.com', 'subject', 'Møte om parken', 'body', 'Hei! Jeg legger ved agendaen til møtet om idrettsparken. Vi sees kl. 14.', 'flag', 'Ingen tydelige faretegn.'),
        JSON_OBJECT('id', 'frosk_invoice', 'label', 'B', 'detail', 'Dette er en vanlig fakturabeskjed fra en kjent avsender.', 'from', 'frosk.kvekk@dyremail.com', 'subject', 'Kvittering for kontorrekvisita', 'body', 'Hei, her er kvitteringen for forrige bestilling. Ta kontakt hvis noe ikke stemmer.', 'flag', 'Rolig språk og ingen passordforespørsel.'),
        JSON_OBJECT('id', 'scam_login', 'label', 'C', 'detail', 'Feil domene, truende tidsfrist og lenke som ber om påloggingsinformasjon.', 'from', 'patterOGjur@zoo.xz', 'subject', 'VIKTIG: Oppdater kontoen din nå', 'body', 'Kontoen din blir stengt innen 30 minutter. Klikk her og bekreft passord og bankinformasjon.', 'flag', 'Faretegn: ukjent avsender, hastepress og innloggingslenke.')
      ),
      'explanation', 'Riktig! Denne mailen har mange faretegn.',
      'caseNumber', 3,
      'variant', 'email',
      'result', JSON_ARRAY('Denne mailen er brukt til å stjele informasjon fra borgemesteren.', 'Mailen blir sporet tilbake til Dyr og Frosker AS.'),
      'logic', 'Ulvan Bites har ingen tilkobling til Dyr og Frosker AS. Han er ikke ansatt og driver heller ikke handel med selskapet. Det er derfor usannsynlig at Ulvan Bites er tyven!',
      'elimination', 'Ulvan Bites fjernes.',
      'clue', 'Ulvan Bites har ikke tilkobling til Dyr og Frosker AS. Han er usannsynlig tyven.',
      'backgroundImage', '/story_pictures/clue-board-bg.png'
    ),
    correct_answer_json = JSON_OBJECT('selected', 'scam_login')
WHERE stop_id = (SELECT id FROM stops WHERE name = 'Postkontoret')
  AND order_index = 5;

UPDATE tasks
SET title = 'Gåtespor: Falsk butikk',
    description = 'En falsk butikk ble laget. Den falske butikken tror vi stjal informasjonen til borgemesteren! Siste uke handlet borgemesteren på disse tre sidene. Vi må avgjøre om noen av butikkene er falske!',
    content_json = JSON_OBJECT(
      'purpose', 'En falsk butikk ble laget, og vi tror den stjal informasjonen til borgemesteren. Siste uke handlet borgemesteren på tre sider. Du må avgjøre om noen av butikkene er falske.',
      'evidence', 'Her ser du tre nettsider i samme format som resten av Markedsplassen. En er en åpenbar svindel, mens to er realistiske og troverdige. Det er mulig å velge flere sider som falske, men bare én av dem er faktisk falsk.',
      'evidencePassword', NULL,
      'question', 'Hvilke av disse nettsidene ser falske ut?',
      'options', JSON_ARRAY(
        JSON_OBJECT('id', 'sportslageret', 'label', 'A', 'detail', 'Butikken har tydelig kontaktinfo, realistiske priser og vanlig kortbetaling.', 'url', 'sportslageret.no', 'headline', 'Treningsutstyr til idrettsparken', 'body', 'Organisasjonsnummer, returregler og kundeservice er lett å finne.', 'price', '499 kr', 'payment', 'Kort og faktura'),
        JSON_OBJECT('id', 'superdeal', 'label', 'B', 'detail', 'Urealistisk rabatt, uklart domene, dårlig språk og betaling før varen finnes.', 'url', 'super-sport-kupp.xyz', 'headline', 'SUPER DEAL! 90% RABATT!', 'body', 'Kun i dag. Ingen retur. Betal med gavekort før pakken sendes.', 'price', '29 kr', 'payment', 'Gavekort'),
        JSON_OBJECT('id', 'lokaltrykk', 'label', 'C', 'detail', 'Lokal butikk med normal bestilling, hentepunkt og kontaktperson.', 'url', 'lokaltrykk-dyreby.no', 'headline', 'Drakter med klubbtrykk', 'body', 'Bestilling kan hentes i butikk, og prisen matcher lignende butikker.', 'price', '349 kr', 'payment', 'Kort')
      ),
      'explanation', 'Riktig. Denne nettsiden ser veldig mistenkelig ut.',
      'caseNumber', 4,
      'variant', 'shop',
      'selectionMode', 'multi',
      'result', JSON_ARRAY('Butikken ble opprettet fra Dyr og Frosker AS sine lokaler i Kjøttmeisgata 67a.', 'Pondus Grisling er ansatt i Dyr og Frosker AS, men har ikke tilgang til lokalene i Kjøttmeisgata.'),
      'logic', 'Pondus Grisling har ikke tilgang til kontorene. Dessuten hadde han livestream fra soverommet sitt da butikken ble opprettet. Det er sannsynligvis ikke Grisling.',
      'elimination', 'Pondus Grisling fjernes.',
      'clue', 'Pondus har ikke tilgang til Dyr og Frosker AS sitt lokale i Kjøttmeisgata, og han var live uten pause mens nettbutikken ble laget.',
      'backgroundImage', '/story_pictures/clue-board-bg.png'
    ),
    correct_answer_json = JSON_OBJECT('selected', JSON_ARRAY('superdeal'))
WHERE stop_id = (SELECT id FROM stops WHERE name = 'Markedsplassen')
  AND order_index = 5;

UPDATE tasks
SET title = 'Gåtespor: Falsk konto',
    description = 'Det er mye misinformasjon som er ute og går i byen! Innbyggerne er usikre og forvirret. Hva er sann informasjon, og hva er ikke sant?',
    content_json = JSON_OBJECT(
      'purpose', 'Det er mye misinformasjon som er ute og går i byen. Innbyggerne er usikre og forvirret, og vi må finne ut hva som er sann informasjon og hva som ikke er det.',
      'evidence', 'Hjelp oss å finne hvilken konto som sprer falsk informasjon, slik at vi kan undersøke nærmere. To innlegg viser til informasjon som kan kontrolleres, mens én konto sprer informasjon som ikke er verifiserbar.',
      'evidencePassword', NULL,
      'question', 'Hvilken konto sprer falsk informasjon?',
      'options', JSON_ARRAY(
        JSON_OBJECT('id', 'truth_hunter', 'label', 'A', 'detail', 'Påstanden er alvorlig, men kontoen viser ingen kilde, dokumentasjon eller etterprøvbar informasjon.', 'username', 'Sannhetsjegeren', 'handle', '@jegerwebtest', 'initials', 'SJ', 'body', 'Jeg har bevis for at ordføreren samarbeidet med tyven. De skjuler det for deg.', 'metrics', '215 likerklikk · 42 kommentarer · 98 delinger', 'verified', false),
        JSON_OBJECT('id', 'knut_kalorm', 'label', 'B', 'detail', 'Knut peker til kommunens åpne møteprotokoll, som kan sjekkes.', 'username', 'Knut Kålorm', 'handle', '@knut_kalorm', 'initials', 'KK', 'body', 'Møteprotokollen fra kommunen ligger ute nå. Der står bare at kontoen er sperret mens saken undersøkes.', 'metrics', '54 likerklikk · 8 kommentarer · 6 delinger', 'verified', true),
        JSON_OBJECT('id', 'bjorn_bamse', 'label', 'C', 'detail', 'Bjørn viser til politiets oppdatering og ber folk vente på bekreftet informasjon.', 'username', 'Bjørn Bamse', 'handle', '@bamse_bjorn', 'initials', 'BB', 'body', 'Politiet sier at digitale spor undersøkes. Ikke heng ut folk før de vet mer.', 'metrics', '88 likerklikk · 11 kommentarer · 12 delinger', 'verified', true)
      ),
      'explanation', 'Riktig. Denne kontoen sprer informasjon som ikke er verifiserbar og sannsynlig falsk.',
      'caseNumber', 5,
      'variant', 'social',
      'result', JSON_ARRAY('Vi sporer kontoen tilbake til Jeger og Mus-foreningen sin PC!'),
      'logic', 'Jeger og Mus-konferansen har samling flere ganger i året. Dette gjør det tydelig hvem våre to mistenkte er!\nSnikrev Revesen og Millie Mus er begge medlemmer og aktive deltakere på konferansene og samlingene til Jeger og Mus.',
      'elimination', 'Ingen fjernes.',
      'clue', 'Millie Mus og Snikrev Revesen er våre to hovedmistenkte.',
      'backgroundImage', '/story_pictures/clue-board-bg.png'
    ),
    correct_answer_json = JSON_OBJECT('selected', 'truth_hunter')
WHERE stop_id = (SELECT id FROM stops WHERE name = 'Den sosiale møteplassen')
  AND order_index = 5;

UPDATE tasks
SET title = 'Gåtespor: Siste spor',
    description = 'Nå er bare to mistenkte igjen: Millie Mus og Snikrev Revesen. Begge sier de brukte PC-en hos Jeger og Mus-foreningen. Du finner innloggingsloggen til kontoen som spredte falsk informasjon.',
    content_json = JSON_OBJECT(
      'purpose', 'Nå er bare to mistenkte igjen: Millie Mus og Snikrev Revesen. Begge sier de brukte PC-en hos Jeger og Mus-foreningen.',
      'evidence', 'Du finner innloggingsloggen til kontoen som spredte falsk informasjon. Passordet er Muserbest123, men spørsmålet er hva vi faktisk kan konkludere ut fra passordet og loggen.',
      'evidencePassword', 'Muserbest123',
      'question', 'Hva kan vi faktisk konkludere ut fra passordet og loggen?',
      'options', JSON_ARRAY(
        JSON_OBJECT('id', 'millie_proof', 'label', 'Dette beviser at Millie Mus er tyven', 'detail', 'Passordet peker mot Mus, men passord alene er ikke nok bevis.'),
        JSON_OBJECT('id', 'snikrev_proof', 'label', 'Dette beviser at Snikrev Revesen er tyven', 'detail', 'Tidslinjen peker mot Snikrev, men du må først kombinere flere spor.'),
        JSON_OBJECT('id', 'combine_evidence', 'label', 'Dette er et spor, men vi må kombinere det med annen informasjon', 'detail', 'Riktig tenkemåte: passordet er bare ett spor. Loggen avgjør hva det betyr.')
      ),
      'explanation', 'Riktig. Et passord alene er ikke nok bevis.',
      'caseNumber', 6,
      'variant', 'password',
      'password', 'Muserbest123',
      'timeline', JSON_ARRAY(JSON_OBJECT('time', '18:57', 'text', 'Innlogging OK'), JSON_OBJECT('time', '18:58', 'text', 'Tilgang til konto'), JSON_OBJECT('time', '19:03', 'text', 'Nytt innlegg publisert')),
      'result', JSON_ARRAY('Du ser nærmere på loggene.', 'Kontoen ble brukt samtidig som Millie jobbet med bibliotekssystemet.', 'Snikrev Revesen hadde ingen aktivitet registrert i dette tidsrommet.'),
      'logic', 'Passordet peker mot Mus, men det viktigste er tidslinjen:\nMillie kunne ikke være på to steder samtidig.',
      'final', 'Snikrev Revesen er tyven.',
      'clue', 'Innloggingstidspunktet passer med Snikrev, ikke med Millie.',
      'backgroundImage', '/story_pictures/clue-board-bg.png'
    ),
    correct_answer_json = JSON_OBJECT('selected', 'combine_evidence')
WHERE stop_id = (SELECT id FROM stops WHERE name = 'Passordbanken')
  AND order_index = 5;
