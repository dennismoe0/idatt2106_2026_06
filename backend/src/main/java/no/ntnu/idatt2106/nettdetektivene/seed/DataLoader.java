package no.ntnu.idatt2106.nettdetektivene.seed;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import no.ntnu.idatt2106.nettdetektivene.entity.Medal;
import no.ntnu.idatt2106.nettdetektivene.entity.Stop;
import no.ntnu.idatt2106.nettdetektivene.entity.Task;
import no.ntnu.idatt2106.nettdetektivene.entity.TaskType;
import no.ntnu.idatt2106.nettdetektivene.repository.MedalRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.StopRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Order(1)
@RequiredArgsConstructor
public class DataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final StopRepository stopRepository;
    private final TaskRepository taskRepository;
    private final MedalRepository medalRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<Stop> stops = syncStops(List.of(
            stop("Nyhetskvartalet",
                 "Noen prøver å spre kaos etter at penger som skulle gå til den nye idrettsparken plutselig forsvant fra ordførerens prosjektkonto. Nå dukker det opp dramatiske artikler som peker i alle retninger, og folk i byen begynner å skylde på feil personer.\n\nHvis vi skal finne ut hva som faktisk skjedde med ordføreren og pengene, må vi først lære å skille ekte nyheter fra falske. Klarer du å stoppe løgnene før de blir til \"sannheten\" alle tror på?",
                 "FAKE_NEWS", 1, false,
                 "Falske nyheter bruker gjerne skremmende overskrifter og anonyme kilder. Sjekk alltid hvem som har skrevet saken: er nettadressen til et kjent mediehus? Søk opp saken på andre seriøse nettsteder for å se om historien stemmer. Overdrevne påstander uten dokumentasjon er et varseltegn.",
                 "Spor: Tyven la ut en falsk nyhet som prøvde å peke mot Xoo Inn Cafe. Artikkelen brukte sjokkord, ukjent kilde og hastedeling."),
            stop("Fotografen",
                 "Nå hevder flere at de har funnet \"bevisbildet\" som viser hvem som sto ved rådhuset den kvelden pengene forsvant. Problemet er at bildet som deles kan være manipulert, eller til og med laget av KI.\n\nHvis vi skal komme nærmere tyven, må vi vite om bildet er ekte eller bare et nytt forsøk på å villede etterforskningen. Dette oppdraget handler om å lære å se forskjell på ekte spor og falske bevis.",
                 "AI_PHOTO", 2, false,
                 "Bilder kan manipuleres og AI kan lage realistiske falske bilder. Se etter unaturlige detaljer: rare fingre, jevne bakgrunner og uskarp tekst er vanlige feil. Du kan bruke omvendt bildesøk til å sjekke om et bilde er tatt ut av en helt annen sammenheng enn det påstår.",
                 "Spor: Det troverdige bildet viser at en innlogging skjedde fra en PC ved disken på Xoo Inn Cafe."),
            stop("Postkontoret",
                 "Et nytt spor har dukket opp: noen i kommunen fikk en e-post som så helt ekte ut, klikket på lenken og mistet kontroll over kontoen sin. Det kan være akkurat slik tyven kom seg inn i systemene rundt ordførerens prosjekt.\n\nFor å komme videre i saken må vi forstå hvordan phishing faktisk fungerer. Hvis du lærer å avsløre falske e-poster, kan du finne ut hvordan tyven åpnet døren innenfra.",
                 "PHISHING_EMAIL", 3, false,
                 "Phishing-e-poster later som de er fra banker, skoler eller kjente selskaper for å lure deg til å gi fra deg passord eller penger. Se etter skrivefeil, ukjente avsenderadresser og lenker der nettadressen ikke stemmer med avsenderen. En ekte avsender ber aldri om passord eller betalingsinformasjon via e-post.",
                 "Spor: Phishing-lenken brukte feil domene, og loggene viser at den ble åpnet fra nettverket til Xoo Inn Cafe."),
            stop("Markedsplassen",
                 "Et nytt spor peker mot en falsk nettbutikk og et domene registrert nær Bytorget. Det ser ut som tyven brukte svindelsider for å samle inn penger og informasjon, kanskje som en del av planen rundt pengene som forsvant.\n\nFor å koble svindelen til hovedsaken må du lære hvordan falske nettbutikker avsløres. Hvis du finner hva som er galt med sidene, kan vi koble sporene nærmere personen bak hele planen.",
                 "MARKETPLACE", 4, false,
                 "Svindel på nett bruker priser som er for gode til å være sanne, krever betaling på forhånd og har vage eller kopierte produktbeskrivelser. Sjekk alltid selgerprofilen og les tilbakemeldinger fra andre kjøpere. Betal aldri med gavekort eller kryptovaluta — det er nesten umulig å spore.",
                 "Spor: Den falske nettbutikken ble registrert fra IP-adressen til Xoo Inn Cafe kl. 21:14."),
            stop("Den sosiale møteplassen",
                 "Nå vet vi at noen også har brukt falske kontoer for å kontakte elever og spre rykter om saken. Målet virker å være å få folk til å dele feil informasjon, peke mot feil mistenkte og holde den ekte tyven skjult litt lenger.\n\nDerfor må du lære hvordan manipulasjon i sosiale medier ser ut. Hvis du avslører de falske kontoene og ryktene, får vi det siste sporet vi trenger før konfrontasjonen med tyven.",
                 "SOCIAL_MEDIA", 5, false,
                 "Sosiale medier viser deg mest det du allerede er enig i, noe som kan gjøre det vanskelig å se helhetsbildet. Fremmede som tar kontakt og raskt ber om personlig informasjon kan ha skjulte hensikter. Del aldri telefonnummer, adresse, passord eller bilder du ikke vil at alle skal se.",
                 "Spor: Den falske kontoen ble opprettet med engangs-epost fra Xoo Inn Cafe sitt gjestenett."),
            stop("Passordbanken",
                 "Etterforskerne tror nå at tyven ikke bare lurte folk, men også brukte stjålne innlogginger for å bevege seg videre i systemene. Noen brukte svake passord, og det ga tyven en enklere vei mot ordførerens prosjektkonto.\n\nSkal vi forstå hvordan innbruddet skjedde, må vi lære hva som gjør et passord lett å knekke og hva som faktisk beskytter en konto. Jo bedre du blir her, jo nærmere kommer vi hvordan tyven jobbet.",
                 "PASSWORD", 6, false,
                 "Et sterkt passord er langt, tilfeldig og unikt for hver konto du bruker. En rekke tilfeldige ord er lettere å huske og vanskeligere å knekke enn korte passord med spesialtegn. Del aldri passordet ditt med andre, og bruk aldri samme passord på flere nettsteder.",
                 "Spor: Reservekontoen brukte passordet XooInnAdmin2019, som peker mot noen med admin-kobling til Xoo Inn Cafe."),
            stop("Datasenteret",
                 "Nå har vi nesten hele bildet: pengene for idrettsparken ble stjålet, byen ble forvirret med falske nyheter, kontoer ble kompromittert med phishing og svake passord, og falske spor ble spredd med bilder, nettbutikker og sosiale medier.\n\nTyven har aktivert en reserveplan fra datasenteret for å slette sporene sine en gang for alle. Nå må du bruke alt du har lært for å stanse systemene før sannheten forsvinner.",
                 "FINAL_BOSS", 7, true,
                 "Du har nå lært de viktigste detektivferdighetene: gjenkjenne falske nyheter, phishing-e-poster, manipulerte bilder, svake passord, nettsvindel og sosiale medier-feller. Den viktigste regelen er å stoppe og tenke én ekstra gang før du klikker, deler eller svarer på noe du er usikker på.",
                 null)
        ));

        Stop newsStop   = stops.get(0);
        Stop photoStop  = stops.get(1);
        Stop mailStop   = stops.get(2);
        Stop marketStop = stops.get(3);
        Stop socialStop = stops.get(4);
        Stop pwdStop    = stops.get(5);

        List<Task> tasks = new ArrayList<>();

        // LEARN tasks — first task (orderIndex 1) for every non-boss stop
        tasks.addAll(List.of(
            learnTask(newsStop, 1, "Lær om falske nyheter", "Les kortene og svar riktig på alle spørsmål for å gå videre.",
                learnContentJson(
                    new Slide(
                        "",
                        "Hva er falske nyheter?",
                        "Nyhetsartikkel",
                        "Falske nyheter er innhold som ser ut som ekte nyheter, men som er laget for å påvirke deg mer enn å informere deg. Målet er ofte at du skal reagere raskt med følelser som frykt, sinne eller stress, slik at du klikker eller deler før du rekker å sjekke om informasjonen stemmer. Derfor er første steg alltid å stoppe opp og spørre: Hvem står bak dette, og kan påstanden bevises?",
                        new String[]{
                            "SJOKK! ALLE SKOLER STENGES FØR KL. 12 - DEL NÅ! || Overskriften prøver å stresse deg til å dele med en gang i stedet for å sjekke fakta.",
                            "\"En hemmelig kilde i rådhuset sier at alle allerede vet sannheten.\" || Uklare kilder gjør saken svak. Troverdige saker viser hvem som uttaler seg og hvor informasjonen kommer fra."
                        },
                        new String[]{
                            "Sterke følelser er ikke det samme som sterke bevis.",
                            "Før du tror på saken: sjekk avsender, kilde og om påstanden kan bekreftes."
                        }
                    ),
                    new Slide(
                        "",
                        "Hva bør du sjekke først?",
                        "Domenenavn",
                        "Det første du bør sjekke er nettadressen (domenet) og hvem som faktisk har publisert saken. Falske sider prøver ofte å ligne på kjente medier med navn som ser troverdige ut ved første blikk, men domenet avslører dem. Se alltid på hele adressen, ikke bare logo, farger eller overskrift.",
                        new String[]{
                            "Troverdig eksempel: nrk.no, vg.no, trondheim.kommune.no || Kjente avsendere har tydelig identitet og sporbare kanaler.",
                            "Mistenkelig eksempel: supernytt24.xyz, deldettenaa.blog, sannhet-nyheter-online.net || Rare domener og sensasjonelle navn er vanlige faresignaler."
                        },
                        new String[]{
                            "Sjekk alltid hele URL-en, ikke bare hvordan siden ser ut.",
                            "Hvis domenet virker rart eller ukjent, stopp og dobbeltsjekk før du deler."
                        }
                    ),
                    new Slide(
                        "",
                        "Hvordan kan du dobbeltsjekke?",
                        "Kildesjekk",
                        "Når en påstand er stor, bør du finne den igjen hos flere seriøse kilder. En ekte nyhet kan vanligvis bekreftes i offisielle kanaler, for eksempel hos skole, kommune, politi eller etablerte medier. Hvis bare én ukjent side skriver om noe veldig dramatisk, er det et tydelig varseltegn.",
                        new String[]{
                            "Påstand: \"Alle skoler stenger i morgen\" || Sjekk skolens meldingstjeneste, kommunen sine nettsider eller andre kjente medier før du tror på det.",
                            "Hvis bare én ukjent side omtaler en stor hendelse || Behandle saken som usikker til du finner uavhengig bekreftelse."
                        },
                        new String[]{
                            "Store påstander krever flere troverdige kilder.",
                            "Del bare når du har sjekket at informasjonen faktisk stemmer."
                        }
                    ),
                    new Quiz("q1", "Hva kjennetegner ofte en falsk nyhet?", new String[]{"Den prøver å få deg til å reagere raskt med sterke følelser", "Den har alltid mange bilder", "Den er alltid veldig kort"}, "Den prøver å få deg til å reagere raskt med sterke følelser"),
                    new Quiz("q2", "Hva bør du sjekke først når du ser en ny sak?", new String[]{"Domenet og hvem som publiserte saken", "Hvor mange som allerede har delt den", "Om overskriften har emojier"}, "Domenet og hvem som publiserte saken"),
                    new Quiz("q3", "Hva gjør du hvis en stor påstand bare finnes på én ukjent side?", new String[]{"Del den raskt så andre blir advart", "Dobbeltsjekk hos seriøse kilder før du tror eller deler", "Stol på saken hvis den høres viktig ut"}, "Dobbeltsjekk hos seriøse kilder før du tror eller deler")
                )
            ),
            learnTask(mailStop, 1, "Lær om phishing", "Les kortene og svar riktig på alle spørsmål for å gå videre.",
                learnContentJson(
                    new Slide(
                        "",
                        "Hva er phishing?",
                        "Phishing er falske meldinger som prøver å lure deg til å gi fra deg passord, kortinformasjon eller annen privat info. De later ofte som de kommer fra banken din, Posten eller skolen fordi det gjør at du lettere får lyst til å stole på dem.",
                        new String[]{
                            "Eksempel: \"Kontoen din blir sperret i dag\" er laget for å gjøre deg stresset.",
                            "Eksempel: \"Betal 19 kr for å få pakken din\" høres lite ut, men kan være en svindelfelle."
                        },
                        new String[]{
                            "Phishing handler om å lure deg, ikke om å hjelpe deg.",
                            "Jo mer stress meldingen lager, jo mer forsiktig bør du være."
                        }
                    ),
                    new Slide(
                        "",
                        "Hva avslører en phishing-melding?",
                        "Se ekstra nøye på avsender og lenker. En ekte bank bruker sitt eget domene, mens en falsk melding kan bruke noe som ligner, men ikke er helt riktig.",
                        new String[]{
                            "Fra-linje: kunde@dnb.no er mer troverdig enn kunde@dnb-kundeservice.com.",
                            "Lenketekst: \"Spor pakken her\" kan se trygg ut, men hvis den peker til posten-oppdatering.net er det mistenkelig."
                        },
                        new String[]{
                            "Små forskjeller i domenet er viktige.",
                            "Ikke stol på en lenke bare fordi teksten ser pen ut."
                        }
                    ),
                    new Slide(
                        "",
                        "Hva bør du gjøre i praksis?",
                        "Hvis du er usikker, ikke trykk i meldingen. Gå heller selv til riktig nettside eller spør en voksen, lærer eller foresatt om hjelp før du gjør noe.",
                        new String[]{
                            "Hvis du får en rar bankmail, åpne nettleseren og skriv dnb.no selv.",
                            "Hvis du får en rar skolemail, sjekk skolens vanlige innlogging eller spør læreren din."
                        },
                        new String[]{
                            "Stopp før du klikker.",
                            "Sjekk i en kanal du vet er ekte."
                        }
                    ),
                    new Quiz("q1", "Hva menes det med phishing når du får en e-post eller melding som ser viktig ut?", new String[]{"At noen prøver mange passord automatisk på en konto", "At en falsk melding later som den kommer fra noen du stoler på for å lure deg til å gi fra deg informasjon eller klikke", "At en nettbutikk er utsolgt for varer", "At du får vanlig reklame fra en ekte avsender"}, "At en falsk melding later som den kommer fra noen du stoler på for å lure deg til å gi fra deg informasjon eller klikke"),
                    new Quiz("q2", "Hvilken kombinasjon av tegn gjør at en e-post bør føles ekstra mistenkelig før du gjør noe?", new String[]{"E-posten er kort og høflig, og du kjenner avsenderen", "Den bruker tidspress, ber deg klikke raskt og har en lenke eller adresse som ligner på noe ekte uten å være helt riktig", "Den har skolens farger og en logo", "Den kommer på dagtid når mange er på skolen"}, "Den bruker tidspress, ber deg klikke raskt og har en lenke eller adresse som ligner på noe ekte uten å være helt riktig"),
                    new Quiz("q3", "Hvis du får en mistenkelig melding om banken, pakken eller skolekontoen din, hva er det tryggeste første steget?", new String[]{"Svare på meldingen og spørre om den er ekte", "Trykke på lenken raskt for å sjekke hva som har skjedd", "Slette meldingen eller rapportere den, og gå til den ekte nettsiden eller appen selv hvis du må sjekke noe", "Sende meldingen videre til venner så de også får se den"}, "Slette meldingen eller rapportere den, og gå til den ekte nettsiden eller appen selv hvis du må sjekke noe")
                )
            ),
            learnTask(photoStop, 1, "Lær om KI-bilder", "Les kortene og svar riktig på alle spørsmål for å gå videre.",
                learnContentJson(
                    new Slide(
                        "",
                        "Hva er et KI-bilde?",
                        "Et KI-bilde er laget av et dataprogram. Det kan se ekte ut ved første øyekast, men det viser ofte mennesker eller situasjoner som aldri har eksistert på ordentlig.",
                        new String[]{
                        
                        },
                        new String[]{
                            "Et bilde kan se ekte ut uten å være ekte.",
                            "Du må se nøye, ikke bare raskt."
                        }
                    ),
                    new Slide(
                        "",
                        "Hva skal du se etter?",
                        "KI lager ofte små rare feil når den prøver å lage detaljer. Derfor er det lurt å se nøye på hender, ansikter, tekst på skilt, bakgrunnen og hvordan lys og skygger oppfører seg.",
                        new String[]{
                            "Eksempel: Fingre som flyter sammen eller ser ut som for mange fingre.",
                            "Eksempel: Bokstaver på et skilt som ikke blir et ekte ord.",
                            "Eksempel: Bakgrunn som gjentar samme mønster flere ganger."
                        },
                        new String[]{
                            "Se på små detaljer først.",
                            "Rare småfeil kan avsløre hele bildet."
                        }
                    ),
                    new Slide(
                        "",
                        "Hva er forskjellen på ekte og manipulert?",
                        "Et manipulert bilde kan starte som et ekte foto, men noen har endret innholdet etterpå, for eksempel ved å legge til personer, fjerne ting eller flytte detaljer.",
                        new String[]{
                            "Ekte: Bildet viser bare personene som faktisk var i scenen da bildet ble tatt.",
                            "Manipulert: Flere personer er lagt inn i samme scene etterpå, slik at bildet forteller en annen historie."
                        },
                        new String[]{
                            "Se etter om nye elementer passer med lys, skygger og skarphet.",
                            "Spør om bildet finnes i en original versjon."
                        }
                    ),
                    new Quiz("q1", "Hva er et KI-bilde?", new String[]{"Et bilde med for mange farger", "Et bilde laget av et dataprogram", "Bilde redigert på PC-en"}, "Et bilde laget av et dataprogram"),
                    new Quiz("q2", "Hva er vanlige feil i KI og manipulerte bilder?", new String[]{"For mange farger", "Merkelige hender og urealistisk glatt hud", "For lav bildekvalitet"}, "Merkelige hender og urealistisk glatt hud"),
                    new Quiz("q3", "Hva kan avsløre at et ekte bilde er manipulert etterpå?", new String[]{"Nye ting passer ikke med lys og skygger", "Bildet har farger", "Bildet er tatt ute"}, "Nye ting passer ikke med lys og skygger")
                )
            ),
            learnTask(pwdStop, 1, "Lær om passord", "Les kortene og svar riktig på alle spørsmål for å gå videre.",
                learnContentJson(
                    new Slide(
                        "",
                        "Hva gjør et passord sterkt?",
                        "Et sterkt passord er langt, unikt og vanskelig å gjette. Lengde betyr mye fordi hvert ekstra tegn gir angripere flere muligheter å prøve. Det bør også være laget av en blanding av store og små bokstaver, tall og tegn, eller av flere tilfeldige ord som ikke handler om deg. Det viktigste er at passordet ikke inneholder navn, brukernavn, lag, skole, kjæledyr eller årstall andre kan finne ut.",
                        new String[]{
                            "Innlogging: Oliver2014 er svakt fordi det ligner på navn + årstall.",
                            "Innlogging: Fotball123 er svakt fordi mange kunne ha gjettet det.",
                            "Innlogging: Fjord!TacoMaane42 er mye sterkere fordi det er langt og ikke handler om deg."
                        },
                        new String[]{
                            "Langt er bedre enn kort.",
                            "Unngå navn, årstall og enkle ord."
                        }
                    ),
                    new Slide(
                        "",
                        "Hvorfor er enkle passord farlige?",
                        "Svake passord er farlige fordi angripere ikke trenger å gjette som mennesker. De bruker programmer som prøver tusenvis av vanlige passord, navn, årstall og mønstre på kort tid. Hvis du bruker samme passord flere steder, kan ett datainnbrudd også gi tilgang til andre kontoer. Et svakt passord kan derfor åpne døren videre, selv om bare én konto blir avslørt.",
                        new String[]{
                            "Vanlige dårlige passord: passord123, 123456, qwerty",
                            "Også dårlige: Emma2013 eller Liverpool10, fordi de er lette å gjette"
                        },
                        new String[]{
                            "Det som er lett å huske, kan også være lett å gjette.",
                            "Ikke bruk samme passord flere steder."
                        }
                    ),
                    new Slide(
                        "",
                        "Hva er en passordfrase?",
                        "En passordfrase er et langt passord laget av flere ord, ofte med tall eller tegn mellom eller rundt ordene. Den kan være lettere å huske enn en kort og rotete kode, men fortsatt sterk fordi den blir lang. Gode passordfraser bruker tilfeldige ord som ikke forteller noe om deg, for eksempel ord som ikke hører naturlig sammen. Ikke bruk en kjent sangtekst, et sitat eller en setning andre kan gjette.",
                        new String[]{
                            "Eksempel: Hest!Maanelys42Fjord",
                            "Ikke så bra: Oliver!Trondheim2014 fordi det handler om deg"
                        },
                        new String[]{
                            "Lag noe langt og litt rart.",
                            "Bruk forskjellig passord på forskjellige kontoer."
                        }
                    ),
                    new Quiz("q1", "Hva gjør et passord sterkest?", new String[]{"Det er enkelt å huske", "Det inneholder navn og fødselsdato", "Det er langt og bruker ulike tegn uten personlig info"}, "Det er langt og bruker ulike tegn uten personlig info"),
                    new Quiz("q2", "Hvilket av disse er et svakt passord?", new String[]{"Sol!Fjord#42Hest", "Ola2010", "hX9!wP$3mQ"}, "Ola2010"),
                    new Quiz("q3", "Hva er en passordfrase?", new String[]{"En rekke tilfeldige ord som danner et langt passord", "Et langt ord", "Passordet til telefonen"}, "En rekke tilfeldige ord som danner et langt passord")
                )
            ),
            learnTask(marketStop, 1, "Lær om nettsvindel", "Les kortene og tren på å finne mistenkelige felt før du går videre.",
                learnContentJson(
                    new Slide(
                        "",
                  "Hva trener du på i Markedsplassen?",
                  "I Markedsplassen skal du klikke på feltene som faktisk er mistenkelige i en nettbutikk-mockup. Du trener på å lese informasjonen kritisk, ikke på å gjette ut fra design alene.",
                        new String[]{
                    "Eksempel: Butikken gadget-garagen.shop selger trådløse ørepropper til ekstrem rabatt og ber deg betale raskt.",
                    "Du vurderer felt som nettadresse, betaling, retur og kontaktinfo før du markerer noe."
                        },
                        new String[]{
                    "Målet er å markere mistenkelige felt i selve nettbutikken.",
                    "Se etter konkrete faresignaler, ikke bare en følelse av at noe er rart."
                        }
                    ),
                    new Slide(
                        "",
                  "Hvilke varselsignaler går igjen?",
                  "Se ekstra nøye på domene, betalingsmåte og kontaktinfo. Noen ganger er prisen normal, men betalingen utrygg. Andre ganger er kontaktfeltet det tydeligste problemet.",
                        new String[]{
                    "På retrohub-deals.net kan betaling se vanlig ut, men kontaktfeltet mangler både e-post, adresse og organisasjonsnummer.",
                    "På trendfunn-market.biz kan kontakt se grei ut, men butikken krever gavekort eller bankoverføring til privat konto."
                        },
                        new String[]{
                    "Røde flagg kan variere fra oppgave til oppgave.",
                    "Lav pris alene er ikke alltid nok til å markere et felt."
                        }
                    ),
                    new Slide(
                        "",
                  "Slik får du riktig i klikk-oppgaven",
                  "For å få riktig må du markere alle mistenkelige felt og unngå ekstra klikk på felt som ikke er mistenkelige. Tenk gjennom hvert felt før du sender.",
                        new String[]{
                    "Hvis domene og kontaktinfo er mistenkelige, klikker du bare de to feltene.",
                    "Hvis du i tillegg markerer returfeltet uten grunn, blir svaret feil."
                        },
                        new String[]{
                    "Spør deg selv: Er dette feltet faktisk et varselsignal i denne oppgaven?",
                    "Send først når du har både alle riktige og ingen ekstra markeringer."
                        }
                    ),
                new Quiz("q1", "Hva er målet i Markedsplassen-oppgaven?", new String[]{"Klikke på alle produkter med lav pris", "Klikke på feltene som virker mistenkelige", "Skrive en tekst om hvorfor butikken er falsk"}, "Klikke på feltene som virker mistenkelige"),
                new Quiz("q2", "Hvilken påstand stemmer best?", new String[]{"Lav pris betyr alltid at feltet skal markeres", "Du må vurdere hvert felt i kontekst", "Hvis designet er fint, er butikken trygg"}, "Du må vurdere hvert felt i kontekst"),
                new Quiz("q3", "Når blir svaret riktig i CLICK_SUSPICIOUS?", new String[]{"Når du markerer minst ett felt", "Når du markerer alle og bare de mistenkelige feltene", "Når du markerer de samme feltene som vennen din"}, "Når du markerer alle og bare de mistenkelige feltene")
                )
            ),
            learnTask(socialStop, 1, "Lær om sosiale medier", "Les kortene og svar riktig på alle spørsmål for å gå videre.",
                learnContentJson(
                    new Slide(
                        "",
                        "Hvorfor lures vi lettere i feeden?",
                        "På sosiale medier går ting fort, og vi scroller ofte uten å stoppe opp. Innlegg som gjør oss sinte, redde eller veldig nysgjerrige får ofte mest oppmerksomhet, og derfor er det lett å dele noe før vi har tenkt oss om.",
                        new String[]{
                            "Innlegg som roper \"DEL NÅ før dette blir slettet!!!\" prøver å stresse deg til å reagere før du sjekker om informasjonen stemmer.",
                            "Et skolerykte som \"Jeg har hørt at prøven er lekket til 7B\" kan spre seg raskt selv om ingen har vist bevis."
                        },
                        new String[]{
                            "Sterke følelser er ikke det samme som sterke bevis.",
                            "Jo mer dramatisk et innlegg er, jo roligere bør du bli."
                        }
                    ),
                    new Slide(
                        "",
                        "Hvordan kan en falsk konto se ut?",
                        "En falsk konto kan late som den er en elev, en gamer eller en kjent person. Den har ofte lite ekte informasjon, rare brukernavn eller prøver å bli veldig personlig veldig fort.",
                        new String[]{
                            "DM: \"Hei! Jeg går også på skolen din. Hva heter læreren din igjen? Send Snapen din\" kan være en falsk konto som tester deg.",
                            "Profil: Et rart brukernavn, få bilder og nesten ingen ekte venner kan være tegn på at kontoen later som."
                        },
                        new String[]{
                            "Du må ikke svare bare for å være høflig.",
                            "Rapporter og blokker hvis noe føles feil."
                        }
                    ),
                    new Slide(
                        "",
                        "Hva bør du gjøre før du deler?",
                        "Før du deler, spør: Hvem la dette ut, og vet de faktisk at det stemmer? Hvis et innlegg navngir folk uten bevis eller prøver å få deg til å dele med en gang, bør du stoppe og sjekke først.",
                        new String[]{
                            "Innlegg om skolen kan sjekkes i skolens meldinger eller ved å spørre en lærer.",
                            "Hvis noen skriver \"Det var sikkert Emil som gjorde det\", skal du ikke dele videre bare fordi andre allerede har gjort det."
                        },
                        new String[]{
                            "Ikke hjelp et rykte med å bli større.",
                            "Det tryggeste er å sjekke før du deler."
                        }
                    ),
                    new Quiz("q1", "Hva slags innhold spres raskest på sosiale medier?", new String[]{"Rolig faktabasert nyheter", "Innhold som vekker sterke følelser som sinne eller frykt", "Vitenskapelige artikler"}, "Innhold som vekker sterke følelser som sinne eller frykt"),
                    new Quiz("q2", "Hva bør du gjøre om en fremmed ber om personlig informasjon?", new String[]{"Svare høflig og gi informasjonen", "Avvise og rapportere kontoen", "Be dem spørre igjen"}, "Avvise og rapportere kontoen"),
                    new Quiz("q3", "Hva betyr det om et innlegg bruker kapslås og ber om hastedeling?", new String[]{"Innholdet er viktig og sant", "Avsenderen prøver å hindre deg i å tenke kritisk", "Det er bare en stil"}, "Avsenderen prøver å hindre deg i å tenke kritisk")
                )
            )
        ));

        tasks.addAll(List.of(
            fakeNewsTask(
                newsStop,
                2,
                "Vinterstengte skoler",
                "Finn den ekte saken blant fire artikler om snøkaos og skolehverdag.",
                """
                    {
                      "articles": [
                        {
                          "headline": "Trondheim kommune holder skolene åpne etter nattens snøfall",
                          "ingress": "Kommunen ber elever møte som vanlig, men beregne ekstra reisetid etter snøværet.",
                          "body": "Brøytemannskapene har jobbet gjennom natten, og kommunen opplyser at skolene følger vanlig timeplan torsdag morgen. Elever og foresatte blir bedt om å beregne ekstra tid og følge meldinger fra skolen dersom busser blir forsinket.",
                          "source": "Trondheim kommune",
                          "author": "Ingrid Solberg",
                          "date": "2026-01-14",
                          "isReal": true
                        },
                        {
                          "headline": "SJOKK: Regjeringen stenger ALLE skoler i Norge før lunsj på grunn av iskald luft",
                          "ingress": "Artikkelen hevder at et nasjonalt hastevedtak er tatt, men viser ikke til dokumenter eller navngitte kilder.",
                          "body": "Saken påstår at alle elever må hjem før lunsj på grunn av ekstremkulde, og viser til en anonym beredskapskilde. Ingen departementer, fylker eller kommuner er sitert med navn, og det finnes ingen lenke til vedtak eller melding i offisielle kanaler.",
                          "source": "ViktigeSkoleNytt24.xyz",
                          "author": "Admin",
                          "date": "2026-01-14",
                          "isReal": false
                        },
                        {
                          "headline": "Foreldre raser etter snøkaos, men eksperter advarer mot å stole på skjermbilder alene",
                          "ingress": "Saken viser til delte skjermbilder og sterke reaksjoner, men gir få muligheter for etterprøving.",
                          "body": "Artikkelen viser til skjermbilder fra foreldrenettverk og en lokal ekspert, men oppgir verken fullt navn, tidspunkt eller hvilken skole uttalelsene gjelder. Den omtaler kommunen indirekte, men uten sitat eller lenke til faktisk melding.",
                          "source": "TrondheimVarsel.blog",
                          "author": "Nyhetsdesk",
                          "date": "2026-01-14",
                          "isReal": false
                        },
                        {
                          "headline": "Forskere: Ny snøspray over byen gjør at skolegårder blir helt isfrie på fem sekunder",
                          "ingress": "Bloggen påstår at en hemmelig spray løser hele vinterproblemet på sekunder.",
                          "body": "Saken hevder at kommunen testet en ny spray natt til tirsdag, men oppgir ingen forskningsmiljøer, rapporter eller teststeder. Påstandene om varig effekt i minusgrader støttes ikke av målinger eller uttalelser fra fagpersoner.",
                          "source": "VinterFakta.info",
                          "author": "Maks Nyhet",
                          "date": "2026-01-13",
                          "isReal": false
                        }
                      ],
                      "explanation": "Den ekte saken har tydelig avsender, konkret tiltak og etterprøvbar informasjon. De falske sakene mangler navngitte kilder, vedtak eller faglig dokumentasjon, selv når de høres aktuelle ut."
                    }
                    """
            ),
            fakeNewsTask(
                newsStop,
                3,
                "Mobilforbud på buss",
                "Les overskriftene som i en vanlig feed, og se etter om saken har tydelige kilder og konkrete vedtak.",
                """
                    {
                      "articles": [
                        {
                          "headline": "AtB tester stille sone på utvalgte bussavganger i rushtiden",
                          "ingress": "Et begrenset prøveprosjekt skal undersøke om passasjerer ønsker roligere bussavganger.",
                          "body": "AtB opplyser at ordningen skal testes på tre avganger i to uker for å se om flere ønsker en roligere busstur. Tiltaket er frivillig og gjelder bare bakerst i bussen på de aktuelle rutene.",
                          "source": "AtB pressemelding",
                          "author": "Marius Heggli",
                          "date": "2026-02-03",
                          "isReal": true
                        },
                        {
                          "headline": "NÅ KOMMER MOBILBOT: 1500 kroner hvis du ser på TikTok på bussen",
                          "ingress": "Artikkelen viser til et internt notat om gebyr, men oppgir ikke hvor notatet kommer fra.",
                          "body": "Saken påstår at mobilbot innføres etter vinterferien og at kontrollører skal skrive ut gebyr på stedet. Den viser til et internt notat uten dokumentnummer, dato eller navn på avsender, og ingen offisielle kanaler bekrefter at et slikt vedtak er vedtatt. Artikkelen ber i tillegg lesere dele saken videre med en gang.",
                          "source": "RuteNytt Trondheim",
                          "author": "Nyhetsvakt",
                          "date": "2026-02-03",
                          "isReal": false
                        },
                        {
                          "headline": "AtB vurderer strengere mobilregler etter passasjerklager",
                          "ingress": "Saken virker troverdig, men blander påstander om forslag, vedtak og gjennomføring.",
                          "body": "Artikkelen skriver at AtB vurderer mobilfrie soner i hele bussen, men omtaler samtidig ordningen som om den allerede er besluttet. Den viser ikke til styresak, høringsdokument eller dato for behandling, og kildene omtales kun som ansatte i kollektivmiljøet.",
                          "source": "Midtbyen Tidende",
                          "author": "Signe Dahl",
                          "date": "2026-02-03",
                          "isReal": false
                        },
                        {
                          "headline": "Pilotprosjekt: app skal automatisk dempe mobillyd på buss i rushtiden",
                          "ingress": "Artikkelen beskriver en teknisk løsning, men gir ingen detaljer om personvern eller frivillighet.",
                          "body": "Saken hevder at passasjerer blir med i et lydfilter automatisk når de går om bord. Den forklarer ikke hvordan samtykke innhentes, hvilke linjer som deltar eller hvem som er ansvarlig for løsningen. Ingen lenker til prosjektbeskrivelse eller personvernerklæring er oppgitt.",
                          "source": "KollektivForum",
                          "author": "TekTeam",
                          "date": "2026-02-02",
                          "isReal": false
                        }
                      ],
                      "explanation": "Den ekte saken skiller tydelig mellom test og vedtak, med konkret omfang og rammer. De falske sakene mangler sporbar dokumentasjon og bruker uklare eller anonyme kilder, selv om de kan høres troverdige ut."
                    }
                    """
            ),
            fakeNewsTask(
                newsStop,
                4,
                "Gratis spillvaluta",
                "Fire saker lover ting til spillere. Bare én har tydelig avsender, verifiserbare kilder og trygg fremgangsmåte.",
                """
                    {
                      "articles": [
                        {
                          "headline": "Spillselskap advarer mot falske gavekort og gratis valuta-lenker",
                          "ingress": "Selskapet minner spillere om at ekte kampanjer bare deles via offisielle kanaler.",
                          "body": "I en sikkerhetsmelding ber spillselskapet brukere ignorere innlegg som lover gratis valuta mot innlogging eller deling av engangskoder. Meldingen viser til tidligere svindelforsøk og forklarer at kampanjer alltid publiseres i verifiserte kanaler og inne i spillets eget varslingssystem.",
                          "source": "Spillstudioets statusside",
                          "author": "Security Team",
                          "date": "2026-03-08",
                          "isReal": true
                        },
                        {
                          "headline": "Nordisk turnering åpner for bonusvaluta til seere som kobler konto før finalen",
                          "ingress": "Saken virker troverdig, men beskriver belønning uten å vise til offisielle turneringssider.",
                          "body": "Artikkelen hevder at seere får bonusvaluta gjennom en partnerkampanje under finalen, men oppgir ikke hvilken arrangør som står bak eller hvor reglene finnes. For å delta må brukeren koble konto via en ekstern side, uten at spillselskapet er sitert direkte.",
                          "source": "Nordic Esport Desk",
                          "author": "LiveDesk",
                          "date": "2026-03-08",
                          "isReal": false
                        },
                        {
                          "headline": "Community-arrangør deler kode for gratis skins til nye spillere denne helgen",
                          "ingress": "Saken bruker kjent miljøspråk, men blander fan-initiativ med påstått offisiell kampanje.",
                          "body": "Innlegget sier at en community-arrangør deler ut kodepakker på vegne av studioet, men kampanjen finnes ikke i studioets nyheter eller sosiale kanaler. Teksten ber spillere registrere e-post og telefonnummer for å få forhåndstilgang til kodene, uten å forklare hvorfor opplysningene trengs.",
                          "source": "Community Hub Norge",
                          "author": "Aina Berg",
                          "date": "2026-03-08",
                          "isReal": false
                        },
                        {
                          "headline": "Supportstrøm lover kompensasjonspakker etter serverfeil - krever verifisering av konto",
                          "ingress": "Saken ser hjelpsom ut, men krever innlogging i et skjema utenfor spillets egne systemer.",
                          "body": "Artikkelen hevder at support deler ut kompensasjon etter ustabile servere, men ber brukere sende brukernavn, passord og engangskode i et eget verifiseringsskjema. Ingen offisiell supportside ber om slike opplysninger, og det finnes ingen referanse til saksnummer eller driftsmelding.",
                          "source": "Supportkanalen LIVE",
                          "author": "AK Gamer",
                          "date": "2026-03-07",
                          "isReal": false
                        }
                      ],
                      "explanation": "Den ekte saken forklarer tydelig hvordan verifiserte kampanjer faktisk publiseres. De falske sakene mangler offisielle referanser, ber om unødvendig informasjon eller leder brukeren utenfor kjente kanaler."
                    }
                    """
            ),
            phishingTask(
                mailStop,
                2,
                "Bankvarsel",
                "DNB",
                "kundevarsling@dnb-kundeservice.com",
                "Viktig sikkerhetsvarsel: Vi har satt betalingen din på pause",
                """
                Hei Oliver,

                Vi oppdaget et uvanlig forsøk på å gjennomføre en betaling fra kortet ditt på 4 890 kr til Steam Market. Dersom dette ikke ble gjort av deg, må du bekrefte kontoen din innen 30 minutter for å unngå midlertidig sperring av nettbanken.

                For å stoppe betalingen må du logge inn med BankID og kontrollere opplysningene dine her: dnb-kontroll.com/bekreft

                Hvis du ikke gjør dette i tide, kan kortet og kontoen din bli midlertidig låst av sikkerhetsavdelingen.

                Med vennlig hilsen
                DNB Kundeservice
                """,
                List.of(
                    new Clue("sender", "sender", "kundevarsling@dnb-kundeservice.com", true, "Avsenderen ser ekte ut ved første blikk, men domenet er ikke dnb.no."),
                    new Clue("link1", "link", "dnb-kontroll.com/bekreft", true, "Lenken peker til et annet domene enn banken sin offisielle nettside."),
                    new Clue("urgency", "text", "innen 30 minutter", true, "Svindlere bruker tidspress for å få deg til å klikke før du rekker å sjekke."),
                    new Clue("greeting", "text", "Hei Oliver,", false, "En personlig hilsen kan virke troverdig, men er ikke nok alene. Sjekk alltid avsenderadresse og lenke."),
                    new Clue("amount", "text", "4 890 kr", false, "Beløpet alene beviser ikke at e-posten er falsk. Det er kombinasjonen av feil domene, lenke og tidspress som avslører svindelen."),
                    new Clue("merchant", "text", "Steam Market", false, "Navnet på en kjent tjeneste kan brukes i både ekte og falske varsler. Ikke vurder tjenesten alene."),
                    new Clue("signature", "text", "DNB Kundeservice", false, "Navn og signatur kan kopieres. De blir først nyttige når domenet og lenken også stemmer."),
                    new Clue("bankid", "text", "logge inn med BankID", true, "Phishing prøver ofte å få deg til å oppgi innlogging eller BankID på en falsk side."),
                    new Clue("threat", "text", "kortet og kontoen din bli midlertidig låst", true, "Trusler om sperring eller låsing brukes for å skape panikk."),
                    new Clue("logo", "branding", "DNB Kundeservice", false, "Logo og avsendernavn alene er ikke nok. Svindlere kopierer ofte kjente merkevarer for å se troverdige ut.")
                ),
                "E-posten ser profesjonell ut, men avsenderen og lenken er falske. Tidspresset er laget for å stresse deg til å gi fra deg BankID-opplysninger."
            ),
            phishingTask(
                mailStop,
                3,
                "Pakkemelding",
                "Posten",
                "varsling@posten-levering.net",
                "Pakken din er forsinket i terminal og trenger betaling",
                """
                Hei kunde!

                Vi forsøkte å sende pakken din videre til utleveringsstedet, men sendingen er stoppet fordi det mangler et lite toll- og behandlingsgebyr på 19 kr. Betal i dag for å unngå at pakken blir sendt i retur til avsender.

                Pakken vil bli slettet fra systemet hvis betalingen ikke registreres innen kl. 23.00.

                Betal gebyret her: posten-oppdatering.net/betaling

                Ha bankkort klart når du åpner lenken, så går behandlingen raskere.

                Hilsen Posten
                """,
                List.of(
                    new Clue("sender", "sender", "varsling@posten-levering.net", true, "Adressen ligner på Posten, men bruker ikke det offisielle domenet posten.no."),
                    new Clue("link1", "link", "posten-oppdatering.net/betaling", true, "Betalingslenken går til en side som ikke tilhører Posten."),
                    new Clue("urgency", "text", "Betal i dag", true, "Kunstig hastverk er et vanlig grep i phishing."),
                    new Clue("delivery_place", "text", "utleveringsstedet", false, "Et vanlig leveringsord er ikke mistenkelig i seg selv. Det viktige er hvor lenken peker."),
                    new Clue("fee", "text", "19 kr", false, "Et lite beløp er ikke farlig i seg selv. Svindlere bruker det fordi det får betalingen til å virke ufarlig."),
                    new Clue("return_sender", "text", "sendt i retur til avsender", false, "Retur til avsender kan skje i ekte pakkemeldinger også. Se etter feil domene og betalingslenke."),
                    new Clue("signature", "text", "Hilsen Posten", false, "En vanlig signatur kan kopieres og er ikke nok til å bevise at e-posten er ekte."),
                    new Clue("deadline", "text", "innen kl. 23.00", true, "En kort tidsfrist er laget for å få deg til å reagere før du tenker deg om."),
                    new Clue("card", "text", "Ha bankkort klart", true, "Meldingen prøver å få deg klar til å oppgi betalingsinformasjon på en ukjent side."),
                    new Clue("greeting", "text", "Hei kunde!", true, "En veldig generell hilsen kan være et tegn på at meldingen er sendt ut til mange uten å vite hvem du er."),
                    new Clue("delivery", "text", "utleveringsstedet", false, "At meldingen nevner utleveringsstedet er ganske vanlig i ekte pakkemeldinger. Det er ikke det som avslører svindelen her."),
                    new Clue("sender_name", "sender_name", "Posten", false, "Avsendernavnet kan se riktig ut selv når selve e-postadressen er falsk.")

                ),
                List.of("sender", "link1", "urgency", "deadline", "card", "greeting"),
                "Dette ligner på en ekte pakkemelding, men både avsender og lenke er feil. Det lille gebyret og tidspresset er klassiske phishing-grep."
            ),
            phishingTask(
                mailStop,
                4,
                "Skolekonto",
                "IT-support VGS",
                "it-support@skole-login.com",
                "Kontoen din mister tilgang til Teams og Canvas i dag",
                """
                Hei elev,

                Vi oppdaterer innloggingen for elever etter flere feilforsøk mot skolekontoer denne uka. For å beholde tilgang til Teams, Canvas og skolemail må du logge inn og bekrefte brukeren din før kl. 14.00 i dag.

                Bruk skolepassordet ditt på nytt i portalen for å unngå at kontoen blir deaktivert automatisk.

                Gå til elevportalen her: skole-login.com/verify

                Du kan ikke bruke vanlige skoleapper igjen før dette er gjort.

                Mvh
                IT-support
                """,
                List.of(
                    new Clue("sender", "sender", "it-support@skole-login.com", true, "Skolen ville brukt sitt eget domene, ikke skole-login.com."),
                    new Clue("link1", "link", "skole-login.com/verify", true, "Lenken leder til et ukjent domene som kan stjele skoleinnloggingen din."),
                    new Clue("urgency", "text", "før kl. 14.00 i dag", true, "Tidspress gjør det lettere å lure elever til å handle raskt."),
                    new Clue("greeting_generic", "text", "Hei,", false, "En vanlig hilsen er ikke i seg selv et tegn på svindel. Du må se på domenet og lenken også."),
                    new Clue("failed_attempts", "text", "flere feilforsøk", false, "Feilforsøk kan være en ekte grunn til et varsel. Her er problemet at e-posten sender deg til et ukjent domene."),
                    new Clue("school_tools", "text", "Teams, Canvas og skolemail", false, "Kjente skoletjenester kan nevnes i ekte meldinger også. Det er lenken og avsenderdomenet som avgjør her."),
                    new Clue("signature_role", "text", "IT-support", false, "Signaturen alene forteller ikke om meldingen er ekte. Svindlere kan skrive samme signatur som skolen."),
                    new Clue("password", "text", "Bruk skolepassordet ditt på nytt", true, "Det er mistenkelig når en e-post ber deg skrive inn passordet ditt via en lenke."),
                    new Clue("deactivated", "text", "kontoen blir deaktivert automatisk", true, "Trussel om å miste tilgang brukes for å stresse deg til å handle raskt."),
                    new Clue("apps", "text", "Du kan ikke bruke vanlige skoleapper igjen før dette er gjort.", true, "Meldingen prøver å skremme deg med konsekvenser for å få deg til å klikke."),
                    new Clue("greeting", "text", "Hei elev,", true, "En generell hilsen i stedet for navnet ditt kan være et tegn på at meldingen er masseutsendt phishing."),
                    new Clue("signature_close", "text", "Mvh", false, "En vanlig avslutning gjør ikke meldingen trygg. Du må fortsatt sjekke avsender og lenke.")
                ),
                "Meldingen ser ut som en vanlig IT-beskjed, men domenet er feil og haster unødvendig. Slike e-poster bør alltid sjekkes i skolens offisielle kanaler før du klikker."
            ),
            finalBossTask(stops.get(6))
        ));
        tasks.addAll(List.of(
            aiPhotoTask(photoStop, 2, "Parkbilder", "Er bildet ekte, KI-generert eller manipulert?",
                """
                {
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
                }
                """,
                "{\"image_0\": \"MANIPULATED\", \"image_1\": \"AI_GENERATED\"}"),
            aiPhotoTask(photoStop, 3, "Bytorget", "Finn hvilket bilde som er ekte og kan brukes som bevis.",
                """
                {
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
                }
                """,
                "{\"image_0\": \"REAL\", \"image_1\": \"MANIPULATED\", \"image_2\": \"AI_GENERATED\"}"),
            aiPhotoTask(photoStop, 4, "Bevisbildet", "Kun ett bilde kan brukes som ekte bevis. Finn det.",
                """
                {
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
                }
                """,
                "{\"image_0\": \"MANIPULATED\", \"image_1\": \"AI_GENERATED\", \"image_2\": \"REAL\"}"),
            passwordTask(pwdStop, 2, "Velg det tryggeste passordet", "Finn ut hvilket passord som er best.",
                """
                {
                  "type": "CHOICE",
                  "question": "Hvilket passord er tryggest?",
                  "options": [
                    { "id": "a", "value": "Ola123" },
                    { "id": "b", "value": "Australia2026" },
                    { "id": "c", "value": "Hei" },
                    { "id": "d", "value": "F!sk3Taco#92" }
                  ],
                  "explanation": "F!sk3Taco#92 er sterkest fordi det er langt og blander store og små bokstaver, tall og spesialtegn. Navn, årstall og annen personlig informasjon er svakt."
                }
                """,
                "{\"selected\": \"d\"}"),
            passwordTask(pwdStop, 3, "Gjør passordet bedre", "Velg det passordet som er best forbedret.",
                """
                {
                  "type": "CHOICE",
                  "question": "Noen har prøvd å gjøre passordet 'HeiPåDeg' sterkere. Hvilken versjon er best?",
                  "options": [
                    { "id": "a", "value": "HeiPåDeg123" },
                    { "id": "b", "value": "H@iPÅD4g!021" },
                    { "id": "c", "value": "Hei" },
                    { "id": "d", "value": "HalloPåDeg" }
                  ],
                  "explanation": "H@iPÅD4g!021 er den beste varianten her fordi den er lengre og blander store og små bokstaver, tall og spesialtegn. Samtidig bygger den fortsatt på en kjent frase, så et enda bedre passord ville vært mindre personlig og mer tilfeldig."
                }
                """,
                "{\"selected\": \"b\"}"),
            passwordTask(pwdStop, 4, "Bygg et sterkt passord", "Bruk brikkene til å lage et passord som er sterkt nok.",
                """
                {
                  "type": "BUILDER",
                  "question": "Bygg et passord som er sterkt nok til å låse opp bankboksen",
                  "words": ["Tiger", "Måne", "Pizza", "Hund", "Sol", "Isbjørn", "Fjord"],
                  "symbols": ["!", "#", "@", "?", "&", "*"],
                  "numbers": ["67", "42", "99", "3", "2026"],
                  "maxLength": 24,
                  "minStrength": "STRONG",
                  "explanation": "Et sterkt passord er langt, bruker store og små bokstaver, tall og spesialtegn, og inneholder ikke personlig informasjon."
                }
                """,
                "{\"minStrength\": \"STRONG\"}"),
            marketplaceTask(marketStop, 2, "Falsk sportsbutikk", "Klikk på de delene av nettstedet som virker mistenkelige.",
                """
                {
                  "type": "CLICK_SUSPICIOUS",
                  "siteName": "sneaker-blitz.shop",
                  "question": "Klikk på de delene du synes er mistenkelige.",
                  "mockup": {
                    "productImageUrl": "/marketplace/air-max-270.png",
                    "headline": "Nike Air Max — KUN I DAG!",
                    "tagline": "Salg slutter om 2 timer. Kun noen få igjen!",
                    "productName": "Nike Air Max 270",
                    "price": "299",
                    "originalPrice": "2 599",
                    "badges": ["90% RABATT", "GRATIS FRAKT"],
                    "paymentText": "Betaling: Western Union / Gavekort",
                    "contactText": "Kontakt: kontakt@sneaker-blitz.shop"
                  },
                  "elements": [
                    { "id": "domain",  "label": "sneaker-blitz.shop",      "explanation": "Ukjent domene er et klassisk varselsignal.", "isSuspicious": true },
                    { "id": "payment", "label": "Western Union / Gavekort", "explanation": "Denne betalingsmåten brukes ofte i svindel.", "isSuspicious": true },
                    { "id": "price",   "label": "299",                      "explanation": "Prisen er veldig lav, men i denne oppgaven er det domene og betaling som er de tydeligste faresignalene.", "isSuspicious": false },
                    { "id": "seller",  "label": "Solgt av Nordisk Butikk AS", "explanation": "Dette ser ganske vanlig ut alene og er ikke hovedproblemet her.", "isSuspicious": false },
                    { "id": "shipping","label": "Levering 2-4 virkedager", "explanation": "Vanlig leveringstid er ikke i seg selv et faresignal.", "isSuspicious": false }
                  ],
                  "explanation": "Domenet er ukjent og betalingsmåten (Western Union/gavekort) er klassiske svindeltegn."
                }
                """,
                "{\"correctElementIds\": [\"domain\", \"payment\"]}",
                "Sjekk URL, priser, kontaktinfo og betalingsvalg nøye."),
            marketplaceTask(marketStop, 3, "Elektronikksvindel", "Klikk på de delene av nettstedet som virker mistenkelige.",
                """
                {
                  "type": "CLICK_SUSPICIOUS",
                  "siteName": "billig-elektronikk.cc",
                  "question": "Klikk på de delene du synes er mistenkelige.",
                  "mockup": {
                    "productImageUrl": "/marketplace/playstation-5.png",
                    "headline": "PlayStation 5 — PÅ LAGER NÅ!",
                    "tagline": "Rask levering, super pris!",
                    "productName": "PlayStation 5",
                    "price": "1 499",
                    "originalPrice": "7 999",
                    "badges": ["80% RABATT"],
                    "paymentText": "Betaling: Visa / Mastercard",
                    "contactText": "Kontakt: ingen informasjon tilgjengelig"
                  },
                  "elements": [
                    { "id": "domain",  "label": "billig-elektronikk.cc",          "explanation": "Domenet virker generisk og lite troverdig.", "isSuspicious": true },
                    { "id": "contact", "label": "ingen informasjon tilgjengelig",  "explanation": "Seriøse butikker skjuler ikke kontaktinfo.", "isSuspicious": true },
                    { "id": "productName", "label": "PlayStation 5",               "explanation": "Produktnavnet i seg selv sier ikke at butikken er falsk.", "isSuspicious": false },
                    { "id": "payment", "label": "Visa / Mastercard",               "explanation": "Vanlig kortbetaling ser mer normalt ut og er ikke hovedproblemet her.", "isSuspicious": false },
                    { "id": "price",   "label": "1 499",                           "explanation": "Prisen er lav, men i denne oppgaven er det domenet og manglende kontaktinfo som avslører butikken best.", "isSuspicious": false }
                  ],
                  "explanation": "Domenet er ukjent og det mangler kontaktinformasjon — to alvorlige varseltegn."
                }
                """,
                "{\"correctElementIds\": [\"domain\", \"contact\"]}",
                "Mangler kontaktinfo og ukjent domene er alvorlige varseltegn."),
            marketplaceTask(marketStop, 4, "Outlet med skjulte feller", "Klikk på de delene av nettstedet som virker mistenkelige.",
                """
                {
                  "type": "CLICK_SUSPICIOUS",
                  "siteName": "streetwear-fast.net",
                  "question": "Klikk på de delene du synes er mistenkelige.",
                  "mockup": {
                    "productImageUrl": "/marketplace/urban-storm-jacket.png",
                    "headline": "Streetwear-jakke til spesialpris",
                    "tagline": "Midlertidig kampanje. Begrenset antall.",
                    "productName": "Urban Storm Jacket",
                    "price": "899 kr",
                    "originalPrice": "1 499 kr",
                    "badges": ["Populær", "Nyhet"],
                    "paymentText": "Kun bankoverføring før sending",
                    "contactText": "Kontakt oss kun via DM på ShopChat",
                    "returnPolicyText": "30 dagers retur med kvittering"
                  },
                  "elements": [
                    { "id": "payment", "label": "Kun bankoverføring før sending", "explanation": "Bare bankoverføring gjør det mye vanskeligere å få hjelp hvis noe går galt.", "isSuspicious": true },
                    { "id": "contact", "label": "Kontakt oss kun via DM på ShopChat", "explanation": "Seriøse butikker har vanligvis ordentlig kontaktinfo, ikke bare DM.", "isSuspicious": true },
                    { "id": "returnPolicy", "label": "30 dagers retur med kvittering", "explanation": "En tydelig returregel er et godt tegn, ikke et faresignal.", "isSuspicious": false },
                    { "id": "domain", "label": "streetwear-fast.net", "explanation": "Domenet kan se litt rart ut, men i denne oppgaven er det ikke det tydeligste faresignalet.", "isSuspicious": false },
                    { "id": "productName", "label": "Urban Storm Jacket", "explanation": "Produktnavnet ser helt vanlig ut og er ikke mistenkelig i seg selv.", "isSuspicious": false }
                  ],
                  "explanation": "Her er det betalingsmåten og den dårlige kontaktløsningen som avslører butikken."
                }
                """,
                "{\"correctElementIds\": [\"payment\", \"contact\"]}",
                "Se etter butikker som gjør det vanskelig å kontakte dem eller krever usikre betalingsmåter."),
            socialMediaTask(socialStop, 2, "Sjekk før du deler", "Velg den tryggeste handlingen før du reagerer på innlegget.",
                """
                {
                  "type": "CHOOSE_ACTION",
                  "post": {
                    "username": "Maja 9B", "handle": "@maja_9b", "avatar": "📚",
                    "content": "DELE DETTE NÅ!!! Rektor skal visst forby alle mobiler fra mandag, også i storefri 😱 Kusina til venninna mi sier lærerne fikk beskjed i går kveld, men ingen voksne vil si noe ennå!!!",
                    "likes": 287, "comments": 46, "timestamp": "I dag kl. 08:14", "verified": false
                  },
                  "question": "Hva er det tryggeste du bør gjøre med dette innlegget?",
                  "options": [
                    { "id": "SHARE", "text": "Del det videre med en gang" },
                    { "id": "WAIT", "text": "Vent og se om det dukker opp andre steder" },
                    { "id": "CHECK_SOURCES", "text": "Sjekk kilden og faktasjekk før du gjør noe" },
                    { "id": "ASK_ADULT", "text": "Spør en voksen" }
                  ],
                  "explanation": "Innlegget spiller på stress og rykter fra skolemiljøet, men viser ikke til noen faktisk beskjed fra skolen. Slike ting bør sjekkes i Visma, på skolens nettside eller med en lærer før du deler videre."
                }
                """,
                "{\"acceptedActions\": [\"CHECK_SOURCES\", \"ASK_ADULT\", \"WAIT\"]}"),
            socialMediaTask(socialStop, 3, "Stopp ryktespredningen", "Velg den tryggeste handlingen når et innlegg prøver å få deg til å reagere raskt.",
                """
                {
                  "type": "CHOOSE_ACTION",
                  "post": {
                    "username": "LukasVG1", "handle": "@lukas_vg1", "avatar": "⚽",
                    "content": "Helt sykt hvis dette stemmer: noen sier prøven i samfunnsfag allerede er lekket i en Snap-gruppe 😡 Del så alle får vite hvor urettferdig skolen er!!!",
                    "likes": 613, "comments": 128, "timestamp": "I dag kl. 10:27", "verified": false
                  },
                  "question": "Hva er smart å gjøre før du reagerer eller deler?",
                  "options": [
                    { "id": "SHARE", "text": "Del med en gang - dette er viktig!" },
                    { "id": "CHECK_SOURCES", "text": "Sjekk om det er sant før du deler" },
                    { "id": "IGNORE", "text": "Ignorer innlegget" },
                    { "id": "ASK_ADULT", "text": "Spør en voksen om råd" }
                  ],
                  "explanation": "Innlegget prøver å gjøre deg sint og få deg til å reagere før du vet om det faktisk stemmer. Når skole-rykter kobles med sterk språkbruk og press om å dele, bør du alltid sjekke først."
                }
                """,
                "{\"acceptedActions\": [\"CHECK_SOURCES\", \"ASK_ADULT\", \"IGNORE\"]}"),
            socialMediaTask(socialStop, 4, "Finn det mest illegitime innlegget", "Velg innlegget med minst troverdighet.",
                """
                {
                  "type": "IDENTIFY_WORST",
                  "question": "Hvilket innlegg er mest mistenkelig?",
                  "posts": [
                    { "id": "post_0", "username": "Charlottenlund vgs", "handle": "@charl_vgs", "avatar": "🏫",
                      "content": "Vi undersøker ryktene om innbruddet i medielaben. Elever får informasjon i løpet av dagen via Teams og skolens offisielle kanaler.",
                      "likes": 182, "comments": 19, "timestamp": "I dag kl. 09:02", "verified": true },
                    { "id": "post_1", "username": "Sannheten_kommer", "handle": "@ingenkanstoppeoss", "avatar": "👀",
                      "content": "JEG VET hvem som tok Mac-ene fra medierommet!! Skolen dekker det over for å beskytte en elev i 2STA. Del dette FØR de sletter sporene 🔥🔥🔥",
                      "likes": 1421, "comments": 304, "timestamp": "I dag kl. 09:18", "verified": false },
                    { "id": "post_2", "username": "Elevrådet CLVGS", "handle": "@elevrad_clvgs", "avatar": "🗣️",
                      "content": "Vi har spurt ledelsen om hva som har skjedd, men det er fortsatt lite bekreftet informasjon. Vent med å dele navn eller rykter om medelever.",
                      "likes": 409, "comments": 37, "timestamp": "I dag kl. 09:41", "verified": false }
                  ],
                  "explanation": "Innlegg 2 er minst troverdig fordi det navngir et rykte uten bevis, bruker kapslås og prøver å presse folk til å dele før noen får sjekket saken."
                }
                """,
                "{\"selected\": \"post_1\"}")
        ));

        tasks.addAll(List.of(
            clueRiddleTask(
                newsStop,
                5,
                "Gåtespor: Falsk nyhet fra tyven",
                "Tyven prøver å forvirre byen med en falsk artikkel. Finn artikkelen som ikke tåler kildekritikk.",
                "Du bruker det du lærte om falske nyheter: sjekk kilde, språk og om påstanden kan bekreftes andre steder. Riktig valg gir første spor til Datasenteret.",
                "Politiet fant tre artikler som ble delt like etter tyveriet. Én av dem ble laget av tyven for å peke mot feil sted.",
                null,
                "Hvilken artikkel er falsk?",
                """
                [
                  {
                    "id": "official_update",
                    "label": "Kommunen bekrefter: idrettspark-konto sperret etter mistenkelig overføring",
                    "detail": "Publisert på kommunens nettside med dato, kontaktperson og rolig språk."
                  },
                  {
                    "id": "fake_cafe_article",
                    "label": "SJOKK: Tyven skjulte pengene på Xoo Inn Cafe - DEL NÅ!",
                    "detail": "Ukjent side, ingen forfatter, bare anonyme kilder og store bokstaver."
                  },
                  {
                    "id": "police_brief",
                    "label": "Politiet undersøker digitale spor etter overføringen",
                    "detail": "Kort pressemelding med saksnummer og lenke til politiets kanal."
                  }
                ]
                """,
                "fake_cafe_article",
                "Riktig. Den falske saken bruker sjokkord, ukjent kilde og hastedeling. Men den nevner Xoo Inn Cafe, og det stedet blir et spor å undersøke videre."
            ),
            clueRiddleTask(
                photoStop,
                5,
                "Gåtespor: Hvilket bilde kan brukes?",
                "Noen har sendt inn tre bilder fra Xoo Inn Cafe. Finn bildet som faktisk kan brukes som bevis.",
                "Du bruker det du lærte om KI og manipulering: se etter rare hender, uleselig tekst, gjentatte mønstre og skygger som ikke stemmer.",
                "Overvåkingssystemet ved Xoo Inn Cafe tok bilder samme kveld som den falske nyheten ble delt.",
                null,
                "Hvilket bilde er mest troverdig som ekte bevis?",
                """
                [
                  {
                    "id": "ai_window",
                    "label": "Bilde A: personen ved vinduet har seks fingre og skiltet bak er uleselig",
                    "detail": "Flere små detaljer ser KI-genererte ut."
                  },
                  {
                    "id": "real_counter",
                    "label": "Bilde B: mobilbilde ved disken med leselig klokke, naturlige skygger og vanlig kamerastøy",
                    "detail": "Detaljene henger sammen på tvers av bildet."
                  },
                  {
                    "id": "edited_jacket",
                    "label": "Bilde C: jakken er skarpere enn resten og skyggen peker feil vei",
                    "detail": "Ser ut som noe er redigert inn."
                  }
                ]
                """,
                "real_counter",
                "Riktig. Bilde B er mest troverdig. Det viser at en innlogging skjedde fra en PC ved disken på Xoo Inn Cafe."
            ),
            clueRiddleTask(
                mailStop,
                5,
                "Gåtespor: Phishing-e-posten",
                "En ansatt i kommunen fikk en e-post før pengene forsvant. Finn tegnet som avslører at den er phishing.",
                "Du bruker det du lærte om phishing: sjekk avsender, lenke og kunstig hastverk. Riktig valg viser hvordan tyven kom inn i systemet.",
                "E-posten ba mottakeren bekrefte kontoen sin etter en påstått sikkerhetsfeil.",
                null,
                "Hva er det sterkeste phishing-sporet?",
                """
                [
                  {
                    "id": "wrong_domain",
                    "label": "Lenken går til kommune-sikkerhet.net i stedet for kommunens ekte domene, og det betyr at siden kan være laget for å stjele innloggingen din",
                    "detail": ""
                  },
                  {
                    "id": "no_emojis",
                    "label": "E-posten inneholder ingen emojier",
                    "detail": ""
                  },
                  {
                    "id": "knows_name",
                    "label": "E-posten starter med Hei Kari",
                    "detail": ""
                  }
                ]
                """,
                "wrong_domain",
                "Riktig. Feil domene er et tydelig phishing-spor, fordi svindlere ofte lager nettsider som ligner på ekte innlogginger. Loggene viser at lenken ble åpnet fra nettverket til Xoo Inn Cafe."
                ,
                """
                {
                  "email": {
                    "fromName": "Trondheim kommune IT",
                    "fromEmail": "varsling@kommune-sikkerhet.net",
                    "subject": "Viktig: kontoen din må sikres i dag",
                    "body": "Hei Kari,\\n\\nVi har registrert en sikkerhetsfeil på kontoen din etter uvanlig aktivitet i natt. For å beholde tilgang til e-post og lønnssystem må du bekrefte brukeren din før kl. 13.00 i dag.\\n\\nLogg inn her: kommune-sikkerhet.net/bekreft\\n\\nHilsen IT-avdelingen"
                  }
                }
                """
            ),
            clueRiddleTask(
                marketStop,
                5,
                "Gåtespor: Falsk nettbutikk",
                "Tyven brukte en falsk nettbutikk som lokkemiddel. Finn sporene som avslører hvor siden ble laget.",
                "Du bruker det du lærte om nettsvindel: sjekk domene, kontaktinfo og betaling. Riktig valg kobler svindelsiden til etterforskningen.",
                "Den falske butikken solgte idrettspark-effekter med enorm rabatt og ba folk betale før varen fantes.",
                null,
                "Hva er det viktigste tekniske sporet?",
                """
                [
                  {
                    "id": "ip_cafe",
                    "label": "Registreringsloggen viser IP-adressen til Xoo Inn Cafe kl. 21:14",
                    "detail": "IP-sporet viser hvor siden ble opprettet fra."
                  },
                  {
                    "id": "green_button",
                    "label": "Kjøp-knappen er grønn",
                    "detail": "Fargen på en knapp sier lite alene."
                  },
                  {
                    "id": "product_photo",
                    "label": "Produktbildet viser en fotball",
                    "detail": "Bildet kan være relevant for varen, men avslører ikke tyven."
                  }
                ]
                """,
                "ip_cafe",
                "Riktig. IP-adressen peker til Xoo Inn Cafe, samme sted som dukket opp i nyhetssporet og phishing-loggen."
            ),
            clueRiddleTask(
                socialStop,
                5,
                "Gåtespor: Falsk konto",
                "En falsk konto prøvde å få elever til å dele rykter. Finn detaljen som avslører hvor kontoen ble laget.",
                "Du bruker det du lærte om sosiale medier: sjekk profil, språk, hastverk og hva kontoen prøver å få deg til å gjøre.",
                "Kontoen skrev: 'Jeg vet hvem tyven er, del før politiet sletter bevisene!'",
                null,
                "Hva er det viktigste sporet fra kontoen?",
                """
                [
                  {
                    "id": "many_emojis",
                    "label": "Innlegget bruker mange emojis",
                    "detail": "Det kan være manipulerende, men peker ikke til et sted."
                  },
                  {
                    "id": "cafe_wifi_signup",
                    "label": "Kontoen ble opprettet med engangs-epost fra Xoo Inn Cafe sitt gjestenett",
                    "detail": "Opprettelsesloggen kobler kontoen til samme sted som de andre sporene, og innlegget prøver å få deg til å dele raskt og handle impulsivt."
                  },
                  {
                    "id": "short_username",
                    "label": "Brukernavnet er kort",
                    "detail": "Et kort navn er ikke nok til å avsløre en konto."
                  }
                ]
                """,
                "cafe_wifi_signup",
                "Riktig. Kontoen ble laget via Xoo Inn Cafe sitt gjestenett, og innlegget prøver å presse deg til å dele før du tenker. Nå peker nyhet, bilde, phishing, nettbutikk og sosial konto samme vei.",
                """
                {
                  "socialPost": {
                    "platform": "Tweety.no",
                    "username": "SannhetsJegeren99",
                    "handle": "@sannhet99",
                    "avatar": "👁️",
                    "content": "Jeg vet hvem tyven er, del før politiet sletter bevisene!",
                    "likes": 418,
                    "comments": 73,
                    "shares": 126,
                    "timestamp": "I dag kl. 21:03",
                    "verified": false,
                    "clueTitle": "Spor i kontoopprettelsen",
                    "clueText": "Kontoen ble opprettet med engangs-epost fra Xoo Inn Cafe sitt gjestenett."
                  }
                }
                """
            ),
            clueRiddleTask(
                pwdStop,
                5,
                "Gåtespor: Passordet i loggen",
                "Det siste sporet handler om passordet tyven brukte på en reservekonto.",
                "Du bruker det du lærte om passord for å lese et siste digitalt spor. Et lekket passord kan avsløre både vaner og hvem kontoen er knyttet til.",
                "Reservekontoen brukte passordet XooInnAdmin2019.",
                "XooInnAdmin2019",
                "Hva forteller passordet oss?",
                """
                [
                  {
                    "id": "random_strong",
                    "label": "Det er et sterkt tilfeldig passord",
                    "detail": "Det er ikke tilfeldig: det inneholder sted, rolle og årstall."
                  },
                  {
                    "id": "cafe_admin",
                    "label": "Noen med admin-tilgang på Xoo Inn Cafe laget eller kjente kontoen",
                    "detail": "Passordet peker mot stedet og en administratorrolle."
                  },
                  {
                    "id": "no_clue",
                    "label": "Passord gir aldri etterforskningsspor",
                    "detail": "Passord kan ofte avsløre vaner og koblinger."
                  }
                ]
                """,
                "cafe_admin",
                "Riktig. Passordet peker mot noen med admin-kobling til Xoo Inn Cafe. Dette er det siste sporet før Datasenteret."
            )
        ));
        syncTasks(tasks);

        syncMedals(List.of(
            medal("Nyhetsjeger", "Fullfør Nyhetskvartalet.", stops.get(0)),
            medal("Bildegransker", "Fullfør Fotografen.", stops.get(1)),
            medal("E-postetterforsker", "Fullfør Postkontoret.", stops.get(2)),
            medal("Trygg handler", "Fullfør Markedsplassen.", stops.get(3)),
            medal("Sosial speider", "Fullfør Den sosiale møteplassen.", stops.get(4)),
            medal("Passordvokter", "Fullfør Passordbanken.", stops.get(5)),
            medal("Datasenterhelt", "Fullfør Datasenteret.", stops.get(6)),
            medal("Ukens detektiv", "Fullførte sitt første ukentlige mysterium"),
            medal("Mysterium-mester", "Fullførte fem ukentlige mysterier riktig")
        ));
    }

    private List<Stop> syncStops(List<Stop> seededStops) {
        if (stopRepository.count() == 0) {
            return stopRepository.saveAll(seededStops);
        }

        Map<Integer, Stop> existingByOrderIndex = stopRepository.findAllByOrderByOrderIndexAsc().stream()
            .collect(Collectors.toMap(Stop::getOrderIndex, Function.identity(), (left, right) -> {
                log.warn("Duplicate stop seed key detected for orderIndex={}; keeping first id={} and ignoring id={}",
                    left.getOrderIndex(), left.getId(), right.getId());
                return left;
            }));

        List<Stop> mergedStops = new ArrayList<>();
        for (Stop seededStop : seededStops) {
            Stop existingStop = existingByOrderIndex.get(seededStop.getOrderIndex());
            if (existingStop == null) {
                mergedStops.add(seededStop);
                continue;
            }

            existingStop.setName(seededStop.getName());
            existingStop.setDescription(seededStop.getDescription());
            existingStop.setTheme(seededStop.getTheme());
            existingStop.setFinalBoss(seededStop.isFinalBoss());
            existingStop.setAutoTip(seededStop.getAutoTip());
            existingStop.setClueText(seededStop.getClueText());
            mergedStops.add(existingStop);
        }

        return stopRepository.saveAll(mergedStops);
    }

    private void syncTasks(List<Task> seededTasks) {
        if (taskRepository.count() == 0) {
            taskRepository.saveAll(seededTasks);
            return;
        }

        Map<String, Task> existingByKey = taskRepository.findAll().stream()
            .collect(Collectors.toMap(this::taskSeedKey, Function.identity(), (left, right) -> {
                log.warn("Duplicate task seed key detected for key={}; keeping first id={} and ignoring id={}",
                    taskSeedKey(left), left.getId(), right.getId());
                return left;
            }));

        List<Task> mergedTasks = new ArrayList<>();
        for (Task seededTask : seededTasks) {
            Task existingTask = existingByKey.get(taskSeedKey(seededTask));
            if (existingTask == null) {
                mergedTasks.add(seededTask);
                continue;
            }

            existingTask.setStop(seededTask.getStop());
            existingTask.setTitle(seededTask.getTitle());
            existingTask.setDescription(seededTask.getDescription());
            existingTask.setDifficulty(seededTask.getDifficulty());
            existingTask.setTaskType(seededTask.getTaskType());
            existingTask.setContentJson(seededTask.getContentJson());
            existingTask.setCorrectAnswerJson(seededTask.getCorrectAnswerJson());
            existingTask.setGuidanceText(seededTask.getGuidanceText());
            existingTask.setOrderIndex(seededTask.getOrderIndex());
            mergedTasks.add(existingTask);
        }

        taskRepository.saveAll(mergedTasks);
    }

    private void syncMedals(List<Medal> seededMedals) {
        if (medalRepository.count() == 0) {
            medalRepository.saveAll(seededMedals);
            return;
        }

        Map<String, Medal> existingByName = medalRepository.findAll().stream()
            .collect(Collectors.toMap(Medal::getName, Function.identity(), (left, right) -> {
                log.warn("Duplicate medal seed key detected for name={}; keeping first id={} and ignoring id={}",
                    left.getName(), left.getId(), right.getId());
                return left;
            }));

        List<Medal> mergedMedals = new ArrayList<>();
        for (Medal seededMedal : seededMedals) {
            Medal existingMedal = existingByName.get(seededMedal.getName());
            if (existingMedal == null) {
                mergedMedals.add(seededMedal);
                continue;
            }

            existingMedal.setDescription(seededMedal.getDescription());
            existingMedal.setStop(seededMedal.getStop());
            existingMedal.setImageUrl(seededMedal.getImageUrl());
            mergedMedals.add(existingMedal);
        }

        medalRepository.saveAll(mergedMedals);
    }

    private String taskSeedKey(Task task) {
        return task.getStop().getOrderIndex() + "|" + task.getOrderIndex() + "|" + task.getTaskType().name();
    }

    private Stop stop(String name, String description, String theme, int orderIndex, boolean finalBoss, String autoTip, String clueText) {
        Stop stop = new Stop();
        stop.setName(name);
        stop.setDescription(description);
        stop.setTheme(theme);
        stop.setOrderIndex(orderIndex);
        stop.setFinalBoss(finalBoss);
        stop.setAutoTip(autoTip);
        stop.setClueText(clueText);
        return stop;
    }

    private Task fakeNewsTask(Stop stop, int orderIndex, String title, String description, String contentJson) {
        Task task = baseTask(stop, orderIndex, title, description, TaskType.FAKE_NEWS);
        task.setContentJson(contentJson);
        task.setCorrectAnswerJson(fakeNewsCorrectAnswerJson(contentJson));
        task.setGuidanceText("Les overskrift, kilde og detaljer før du bestemmer hvilke artikler som er ekte.");
        return task;
    }

    record Clue(String id, String type, String label, boolean isClue, String explanation) {}

    record Slide(String icon, String heading, String exampleType, String body, String[] examples, String[] checks) {
        Slide(String icon, String heading, String body, String[] examples, String[] checks) {
            this(icon, heading, "Eksempel", body, examples, checks);
        }

        Slide(String icon, String heading, String body) {
            this(icon, heading, "Eksempel", body, new String[0], new String[0]);
        }
    }

    record Quiz(String id, String question, String[] options, String correct) {}

    private Task learnTask(Stop stop, int orderIndex, String title, String description, String contentJson) {
        Task task = baseTask(stop, orderIndex, title, description, TaskType.LEARN);
        task.setContentJson(contentJson);
        task.setCorrectAnswerJson("{\"quizPassed\": true}");
        task.setGuidanceText("Les kortene nøye og svar riktig på alle spørsmål.");
        return task;
    }

    private String learnContentJson(Slide s1, Slide s2, Slide s3, Quiz q1, Quiz q2, Quiz q3) {
        ObjectNode root = objectMapper.createObjectNode();

        ArrayNode slides = objectMapper.createArrayNode();
        for (Slide s : new Slide[]{s1, s2, s3}) {
            ObjectNode n = objectMapper.createObjectNode();
            n.put("icon", s.icon());
            n.put("heading", s.heading());
            n.put("exampleType", s.exampleType());
            n.put("body", s.body());
            ArrayNode examples = objectMapper.createArrayNode();
            for (String example : s.examples()) examples.add(example);
            n.set("examples", examples);
            ArrayNode checks = objectMapper.createArrayNode();
            for (String check : s.checks()) checks.add(check);
            n.set("checks", checks);
            slides.add(n);
        }
        root.set("slides", slides);

        ArrayNode quiz = objectMapper.createArrayNode();
        for (Quiz q : new Quiz[]{q1, q2, q3}) {
            ObjectNode n = objectMapper.createObjectNode();
            n.put("id", q.id());
            n.put("question", q.question());
            ArrayNode opts = objectMapper.createArrayNode();
            for (String opt : q.options()) opts.add(opt);
            n.set("options", opts);
            n.put("correct", q.correct());
            quiz.add(n);
        }
        root.set("quiz", quiz);

        try {
            return objectMapper.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to build learn task content JSON", e);
        }
    }

    private Task phishingTask(
        Stop stop,
        int orderIndex,
        String title,
        String fromName,
        String fromEmail,
        String subject,
        String body,
        List<Clue> clues,
        String explanation
    ) {
        List<String> requiredClueIds = clues.stream()
            .filter(Clue::isClue)
            .map(Clue::id)
            .toList();
        return phishingTask(stop, orderIndex, title, fromName, fromEmail, subject, body, clues, requiredClueIds, explanation);
    }

    private Task phishingTask(
        Stop stop,
        int orderIndex,
        String title,
        String fromName,
        String fromEmail,
        String subject,
        String body,
        List<Clue> clues,
        List<String> requiredClueIds,
        String explanation
    ) {
        Task task = baseTask(stop, orderIndex, title,
            "Klikk på alle mistenkelige deler av e-posten.", TaskType.PHISHING_EMAIL);
        task.setContentJson(phishingContentJson(fromName, fromEmail, subject, body, clues, explanation));
        try {
            task.setCorrectAnswerJson(objectMapper.writeValueAsString(
                objectMapper.createObjectNode().set("correctClueIds", objectMapper.valueToTree(requiredClueIds))
            ));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to build phishing correctAnswerJson", e);
        }
        task.setGuidanceText("Klikk på alle mistenkelige deler av e-posten og trykk 'Send svar'.");
        return task;
    }

    private String phishingContentJson(
        String fromName,
        String fromEmail,
        String subject,
        String body,
        List<Clue> clues,
        String explanation
    ) {
        ObjectNode email = objectMapper.createObjectNode();
        email.put("fromName", fromName);
        email.put("fromEmail", fromEmail);
        email.put("subject", subject);
        email.put("body", body);

        ArrayNode cluesNode = objectMapper.createArrayNode();
        for (Clue clue : clues) {
            ObjectNode c = objectMapper.createObjectNode();
            c.put("id", clue.id());
            c.put("type", clue.type());
            c.put("label", clue.label());
            c.put("isClue", clue.isClue());
            if (clue.explanation() != null) c.put("explanation", clue.explanation());
            else c.putNull("explanation");
            cluesNode.add(c);
        }
        email.set("clues", cluesNode);

        ObjectNode root = objectMapper.createObjectNode();
        root.set("email", email);
        root.put("explanation", explanation);

        try {
            return objectMapper.writeValueAsString(root);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to build phishing task seed content", exception);
        }
    }

    String fakeNewsCorrectAnswerJson(String contentJson) {
        try {
            JsonNode root = objectMapper.readTree(contentJson);
            JsonNode articlesNode = root.path("articles");
            if (!articlesNode.isArray()) {
                throw new IllegalStateException("Fake news seed content JSON is missing an 'articles' array");
            }
            ArrayNode articles = (ArrayNode) articlesNode;
            ObjectNode answer = objectMapper.createObjectNode();
            for (int i = 0; i < articles.size(); i++) {
                answer.put("article_" + i, articles.path(i).path("isReal").asBoolean(false));
            }
            return objectMapper.writeValueAsString(answer);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to build fake news correctAnswerJson", e);
        }
    }

    private Task baseTask(Stop stop, int orderIndex, String title, String description, TaskType taskType) {
        Task task = new Task();
        task.setStop(stop);
        task.setTitle(title);
        task.setDescription(description);
        task.setDifficulty(1);
        task.setTaskType(taskType);
        task.setOrderIndex(orderIndex);
        return task;
    }

    private Medal medal(String name, String description, Stop stop) {
        Medal medal = new Medal();
        medal.setName(name);
        medal.setDescription(description);
        medal.setStop(stop);
        medal.setImageUrl("/medals/stop-" + stop.getOrderIndex() + ".png");
        return medal;
    }

    private Medal medal(String name, String description) {
        Medal medal = new Medal();
        medal.setName(name);
        medal.setDescription(description);
        medal.setStop(null);
        medal.setImageUrl("/medals/mystery.png");
        return medal;
    }

    private Task aiPhotoTask(Stop stop, int orderIndex, String title, String description, String contentJson, String correctAnswerJson) {
        Task task = baseTask(stop, orderIndex, title, description, TaskType.AI_PHOTO);
        task.setContentJson(contentJson);
        task.setCorrectAnswerJson(correctAnswerJson);
        task.setGuidanceText("Se nøye på detaljene i hvert bilde: hender, bakgrunn, lys og skygger.");
        return task;
    }

    private Task passwordTask(Stop stop, int orderIndex, String title, String description, String contentJson, String correctAnswerJson) {
        Task task = baseTask(stop, orderIndex, title, description, TaskType.PASSWORD);
        task.setContentJson(contentJson);
        task.setCorrectAnswerJson(correctAnswerJson);
        task.setGuidanceText("Tenk på lengde, variasjon og om passordet inneholder personlig informasjon.");
        return task;
    }

    private Task marketplaceTask(
        Stop stop,
        int orderIndex,
        String title,
        String description,
        String contentJson,
        String correctAnswerJson,
        String guidanceText
    ) {
        Task task = baseTask(stop, orderIndex, title, description, TaskType.MARKETPLACE);
        task.setContentJson(contentJson);
        task.setCorrectAnswerJson(correctAnswerJson);
        task.setGuidanceText(guidanceText);
        return task;
    }

    private Task socialMediaTask(Stop stop, int orderIndex, String title, String description, String contentJson, String correctAnswerJson) {
        Task task = baseTask(stop, orderIndex, title, description, TaskType.SOCIAL_MEDIA);
        task.setContentJson(contentJson);
        task.setCorrectAnswerJson(correctAnswerJson);
        task.setGuidanceText("Les innlegget nøye og velg den tryggeste handlingen.");
        return task;
    }

    private Task clueRiddleTask(
        Stop stop,
        int orderIndex,
        String title,
        String description,
        String purpose,
        String evidence,
        String evidencePassword,
        String question,
        String optionsJson,
        String correctOptionId,
        String explanation
    ) {
        return clueRiddleTask(
            stop,
            orderIndex,
            title,
            description,
            purpose,
            evidence,
            evidencePassword,
            question,
            optionsJson,
            correctOptionId,
            explanation,
            null
        );
    }

    private Task clueRiddleTask(
        Stop stop,
        int orderIndex,
        String title,
        String description,
        String purpose,
        String evidence,
        String evidencePassword,
        String question,
        String optionsJson,
        String correctOptionId,
        String explanation,
        String supplementalContentJson
    ) {
        Task task = baseTask(stop, orderIndex, title, description, TaskType.CLUE_RIDDLE);
        ObjectNode content = objectMapper.createObjectNode();
        content.put("purpose", purpose);
        content.put("evidence", evidence);
        if (evidencePassword != null) {
            content.put("evidencePassword", evidencePassword);
        } else {
            content.putNull("evidencePassword");
        }
        content.put("question", question);
        content.set("options", readJsonNode(optionsJson, "clue riddle options"));
        content.put("explanation", explanation);

        if (supplementalContentJson != null && !supplementalContentJson.isBlank()) {
            JsonNode supplementalNode = readJsonNode(supplementalContentJson, "clue riddle supplemental content");
            if (!supplementalNode.isObject()) {
                throw new IllegalStateException("Clue riddle supplemental content must be a JSON object");
            }
            supplementalNode.fields().forEachRemaining(entry -> content.set(entry.getKey(), entry.getValue()));
        }

        task.setContentJson(writeJson(content, "Failed to encode clue riddle content"));
        task.setCorrectAnswerJson("""
            {
              "selected": %s
            }
            """.formatted(toJsonString(correctOptionId)));
        task.setGuidanceText("Bruk det du nettopp lærte til å løse en liten sak. Svaret gir et spor du trenger i Datasenteret.");
        return task;
    }

    private String toJsonString(String value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to encode task seed text", e);
        }
    }

    private JsonNode readJsonNode(String json, String context) {
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse " + context, e);
        }
    }

    private String writeJson(JsonNode node, String errorMessage) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(errorMessage, e);
        }
    }

    private static final String FINAL_BOSS_CONTENT_JSON = """
        {
          "intro": "Backup-planen har startet! Du har 6 sikkerhetssystemer å stoppe.",
          "challenges": [
            {
              "id": 0,
              "type": "FAKE_NEWS",
              "systemName": "Nyhetsfilter",
              "description": "Stopp spredning av falske nyheter",
              "failureExplanation": "Den virkelige artikkelen hadde troverdig kilde og rolig språk. Du må stanse artikkelen som prøver å skape panikk uten bevis.",
              "articles": [
                { "headline": "Pengene er funnet i utlandet", "source": "NRK.no", "body": "Politiet bekrefter at etterforskerne har sporet transaksjonen." },
                { "headline": "AVSLØRT: Ordføreren stjal pengene SELV!!!!", "source": "SannNyhet.xyz", "body": "Anonym kilde sier at ordføreren er den egentlige tyven og at politiet dekker over det." }
              ],
              "correctAnswer": { "article_0": true, "article_1": false }
            },
            {
              "id": 1,
              "type": "AI_PHOTO",
              "systemName": "Bildekontroll",
              "description": "Stopp falske bevis",
              "failureExplanation": "Se etter unaturlige detaljer. Bildet som skal stoppes er KI-laget og kan ikke brukes som ekte bevis.",
              "images": [
                { "src": "/tasks/ai-photo/boss-b.jpg", "alt": "Bilde av en person ved datamaskin", "label": "Bilde A" }
              ],
              "correctAnswer": { "image_0": "AI_GENERATED" }
            },
            {
              "id": 2,
              "type": "PHISHING_EMAIL",
              "systemName": "E-postskjold",
              "description": "Stopp nye phishing-forsøk",
              "failureExplanation": "Dette er en falsk trusselmelding. Den tryggeste handlingen er å rapportere den som phishing, ikke å svare eller klikke.",
              "email": {
                "fromName": "Politiet",
                "fromEmail": "politi@norge-sikkerhet.com",
                "subject": "Du er mistenkt – svar umiddelbart",
                "body": "For å unngå arrestasjon, send personnummeret ditt til dette nummeret innen 1 time."
              },
              "options": [
                { "id": "DELETE", "text": "Slett e-posten" },
                { "id": "REPORT", "text": "Rapporter som phishing" },
                { "id": "REPLY",  "text": "Svar med informasjon" },
                { "id": "OPEN",   "text": "Klikk på lenken" }
              ],
              "correctAnswer": { "action": "REPORT" }
            },
            {
              "id": 3,
              "type": "MARKETPLACE",
              "systemName": "Butikksjekk",
              "description": "Stopp svindelside som samler data",
              "failureExplanation": "Det tydeligste faresignalet er den falske nettadressen kombinert med utrygg betaling. Det er det som avslører svindelsiden.",
              "question": "Hva er galt med denne nettsiden?",
              "options": [
                { "id": "a", "text": "Ingenting, den ser legitim ut" },
                { "id": "b", "text": "URL-en er falsk og betalingsvalget er utrygt" },
                { "id": "c", "text": "Kun prisen er for lav" }
              ],
              "correctAnswer": { "selected": "b" }
            },
            {
              "id": 4,
              "type": "SOCIAL_MEDIA",
              "systemName": "Sosial signaljakt",
              "description": "Stopp ryktespredning",
              "failureExplanation": "Innlegget prøver å presse deg til å dele før du vet om det stemmer. Du må sjekke kilden før du gjør noe.",
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
                { "id": "SHARE",         "text": "Del videre med en gang" },
                { "id": "CHECK_SOURCES", "text": "Sjekk kilden først" },
                { "id": "IGNORE",        "text": "Ignorer innlegget" }
              ],
              "correctAnswer": { "selected": "CHECK_SOURCES" }
            },
            {
              "id": 5,
              "type": "PASSWORD",
              "systemName": "Hovedlåsen",
              "description": "Lås opp den digitale safe og redd pengene",
              "failureExplanation": "Den digitale safen krever et langt og uforutsigbart passord. Velg alternativet som er vanskeligst å gjette.",
              "question": "Hvilket passord er sterkt nok til å sikre den redde kontoen?",
              "options": [
                { "id": "a", "value": "admin123" },
                { "id": "b", "value": "Trondheim" },
                { "id": "c", "value": "S0l!Bj0rn#77" },
                { "id": "d", "value": "passord" }
              ],
              "correctAnswer": { "selected": "c" }
            }
          ]
        }
        """;

    private Task finalBossTask(Stop stop) {
        Task task = baseTask(stop, 1, "Stopp backup-planen",
            "Bruk alt du har lært for å stoppe tyvens automatiske reserveplan.", TaskType.FINAL_BOSS);
        task.setGuidanceText("Du har 6 sikkerhetssystemer å stoppe. Ta dem ett av gangen.");
        task.setContentJson(FINAL_BOSS_CONTENT_JSON);
        try {
            task.setCorrectAnswerJson(objectMapper.writeValueAsString(objectMapper.readTree("""
                {
                  "challenge_0": { "article_0": true, "article_1": false },
                  "challenge_1": { "image_0": "AI_GENERATED" },
                  "challenge_2": { "action": "REPORT" },
                  "challenge_3": { "selected": "b" },
                  "challenge_4": { "selected": "CHECK_SOURCES" },
                  "challenge_5": { "selected": "c" }
                }
                """)));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to build final boss correctAnswerJson", e);
        }
        return task;
    }
}
