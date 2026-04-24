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
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@Order(1)
@RequiredArgsConstructor
public class DataLoader implements ApplicationRunner {

    private final StopRepository stopRepository;
    private final TaskRepository taskRepository;
    private final MedalRepository medalRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (stopRepository.count() > 0) {
            return;
        }

        List<Stop> stops = stopRepository.saveAll(List.of(
            stop("Nyhetskvartalet",
                 "Noen prøver å spre kaos etter at penger som skulle gå til den nye idrettsparken plutselig forsvant fra ordførerens prosjektkonto. Nå dukker det opp dramatiske artikler som peker i alle retninger, og folk i byen begynner å skylde på feil personer.\n\nHvis vi skal finne ut hva som faktisk skjedde med ordføreren og pengene, må vi først lære å skille ekte nyheter fra falske. Klarer du å stoppe løgnene før de blir til \"sannheten\" alle tror på?",
                 "FAKE_NEWS", 1, false,
                 "Falske nyheter bruker gjerne skremmende overskrifter og anonyme kilder. Sjekk alltid hvem som har skrevet saken: er nettadressen til et kjent mediehus? Søk opp saken på andre seriøse nettsteder for å se om historien stemmer. Overdrevne påstander uten dokumentasjon er et varseltegn.",
                 "Tyven hadde på seg en mørk jakke, rød hette og lyse sko."),
            stop("Fotografen",
                 "Nå hevder flere at de har funnet \"bevisbildet\" som viser hvem som sto ved rådhuset den kvelden pengene forsvant. Problemet er at bildet som deles kan være manipulert, eller til og med laget av KI.\n\nHvis vi skal komme nærmere tyven, må vi vite om bildet er ekte eller bare et nytt forsøk på å villede etterforskningen. Dette oppdraget handler om å lære å se forskjell på ekte spor og falske bevis.",
                 "AI_PHOTO", 2, false,
                 "Bilder kan manipuleres og AI kan lage realistiske falske bilder. Se etter unaturlige detaljer: rare fingre, jevne bakgrunner og uskarp tekst er vanlige feil. Du kan bruke omvendt bildesøk til å sjekke om et bilde er tatt ut av en helt annen sammenheng enn det påstår.",
                 "Et ekte bilde viser tyven med en konvolutt utenfor en nettkafé i Bytorget."),
            stop("Postkontoret",
                 "Et nytt spor har dukket opp: noen i kommunen fikk en e-post som så helt ekte ut, klikket på lenken og mistet kontroll over kontoen sin. Det kan være akkurat slik tyven kom seg inn i systemene rundt ordførerens prosjekt.\n\nFor å komme videre i saken må vi forstå hvordan phishing faktisk fungerer. Hvis du lærer å avsløre falske e-poster, kan du finne ut hvordan tyven åpnet døren innenfra.",
                 "PHISHING_EMAIL", 3, false,
                 "Phishing-e-poster later som de er fra banker, skoler eller kjente selskaper for å lure deg til å gi fra deg passord eller penger. Se etter skrivefeil, ukjente avsenderadresser og lenker der nettadressen ikke stemmer med avsenderen. En ekte avsender ber aldri om passord eller betalingsinformasjon via e-post.",
                 "Ordføreren mottok en farlig e-post fra adressen hjelp@by-service.net."),
            stop("Markedsplassen",
                 "Et nytt spor peker mot en falsk nettbutikk og et domene registrert nær Bytorget. Det ser ut som tyven brukte svindelsider for å samle inn penger og informasjon, kanskje som en del av planen rundt pengene som forsvant.\n\nFor å koble svindelen til hovedsaken må du lære hvordan falske nettbutikker avsløres. Hvis du finner hva som er galt med sidene, kan vi koble sporene nærmere personen bak hele planen.",
                 "MARKETPLACE", 4, false,
                 "Svindel på nett bruker priser som er for gode til å være sanne, krever betaling på forhånd og har vage eller kopierte produktbeskrivelser. Sjekk alltid selgerprofilen og les tilbakemeldinger fra andre kjøpere. Betal aldri med gavekort eller kryptovaluta — det er nesten umulig å spore.",
                 "Svindelbutikken «best-deals-city.xyz» var registrert på en adresse ved Bytorget."),
            stop("Den sosiale møteplassen",
                 "Nå vet vi at noen også har brukt falske kontoer for å kontakte elever og spre rykter om saken. Målet virker å være å få folk til å dele feil informasjon, peke mot feil mistenkte og holde den ekte tyven skjult litt lenger.\n\nDerfor må du lære hvordan manipulasjon i sosiale medier ser ut. Hvis du avslører de falske kontoene og ryktene, får vi det siste sporet vi trenger før konfrontasjonen med tyven.",
                 "SOCIAL_MEDIA", 5, false,
                 "Sosiale medier viser deg mest det du allerede er enig i, noe som kan gjøre det vanskelig å se helhetsbildet. Fremmede som tar kontakt og raskt ber om personlig informasjon kan ha skjulte hensikter. Del aldri telefonnummer, adresse, passord eller bilder du ikke vil at alle skal se.",
                 "En falsk konto på Fjesbok.no ble opprettet fra nettkafeen på Bytorget."),
            stop("Passordbanken",
                 "Etterforskerne tror nå at tyven ikke bare lurte folk, men også brukte stjålne innlogginger for å bevege seg videre i systemene. Noen brukte svake passord, og det ga tyven en enklere vei mot ordførerens prosjektkonto.\n\nSkal vi forstå hvordan innbruddet skjedde, må vi lære hva som gjør et passord lett å knekke og hva som faktisk beskytter en konto. Jo bedre du blir her, jo nærmere kommer vi hvordan tyven jobbet.",
                 "PASSWORD", 6, false,
                 "Et sterkt passord er langt, tilfeldig og unikt for hver konto du bruker. En rekke tilfeldige ord er lettere å huske og vanskeligere å knekke enn korte passord med spesialtegn. Del aldri passordet ditt med andre, og bruk aldri samme passord på flere nettsteder.",
                 null),
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
                        "Falske nyheter er laget for å få deg til å tro på noe som ikke stemmer. De kan se ut som vanlige nyheter, men prøver ofte å få deg til å bli redd, sint eller supernysgjerrig så du klikker og deler uten å tenke deg om.",
                        new String[]{
                            "SJOKK! ALLE SKOLER STENGES FØR KL. 12 - DEL NÅ! || Høres dramatisk ut, men betyr ikke at saken er ekte.",
                            "\"En hemmelig kilde i rådhuset sier at alle allerede vet sannheten.\" || Dette er svakere enn når en skole, kommune eller avis sier hvem som faktisk står bak informasjonen."
                        },
                        new String[]{
                            "Ikke stol på en sak bare fordi overskriften ser viktig ut.",
                            "Spør: Hvem sier dette, og hvordan vet de det?"
                        }
                    ),
                    new Slide(
                        "",
                        "Hva bør du sjekke først?",
                        "Domenenavn",
                        "Se først på hvem som har publisert saken. Ekte nyheter kommer ofte fra kjente steder som NRK.no, VG.no eller kommunen sin egen nettside. Nakne rare domenenavn kan være et tegn på at noen prøver å se seriøse ut uten å være det.",
                        new String[]{
                            "Troverdig eksempel: nrk.no, vg.no, trondheim.kommune.no",
                            "Mistenkelig eksempel: supernytt24.xyz, deldettenaa.blog, sannhet-nyheter-online.net"
                        },
                        new String[]{
                            "Sjekk alltid nettadressen, ikke bare logoen.",
                            "Hvis domenet ser rart ut, bør du bli ekstra skeptisk."
                        }
                    ),
                    new Slide(
                        "",
                        "Hvordan kan du dobbeltsjekke?",
                        "Kildesjekk",
                        "Hvis en sak er viktig og ekte, finnes den ofte flere steder. Søk opp samme påstand hos andre seriøse avsendere, eller gå til nettsiden til skolen, kommunen eller politiet hvis det er de saken handler om.",
                        new String[]{
                            "Hvis noen sier at alle skoler stenger i morgen, sjekk skolens meldingstjeneste eller kommunen sin nettside.",
                            "Hvis bare én ukjent side skriver om noe kjempestort, er det et tegn på at noe ikke stemmer."
                        },
                        new String[]{
                            "Store påstander trenger sterke bevis.",
                            "Del først når du har sjekket at det stemmer."
                        }
                    ),
                    new Quiz("q1", "Hva er det første du bør se etter i en nyhetsartikkel?", new String[]{"Domenet og kilden", "Fargen på overskriften", "Antall delinger"}, "Domenet og kilden"),
                    new Quiz("q2", "Hva er et varseltegn i en overskrift?", new String[]{"Rolig og saklig språk", "Store bokstaver og skremmende ordvalg", "Kort og presis tekst"}, "Store bokstaver og skremmende ordvalg"),
                    new Quiz("q3", "Hva bør du gjøre om du er usikker på en nyhet?", new String[]{"Dele den for å advare andre", "Ignorere den alltid", "Sjekke den på andre seriøse nettsteder"}, "Sjekke den på andre seriøse nettsteder")
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
                    new Quiz("q1", "Hva er phishing?", new String[]{"Å prøve mange passord automatisk", "E-poster som later som å komme fra pålitelige kilder for å stjele informasjon", "Spam-reklame"}, "E-poster som later som å komme fra pålitelige kilder for å stjele informasjon"),
                    new Quiz("q2", "Hva er et varseltegn i en e-post?", new String[]{"Avsenderen er på norsk", "Hasteord og lenker til ukjente sider", "E-posten har et bilde"}, "Hasteord og lenker til ukjente sider"),
                    new Quiz("q3", "Hva bør du gjøre med en mistenkelig e-post?", new String[]{"Svare og spørre om det er ekte", "Slette den og gå direkte til nettstedet selv", "Videresende til venner"}, "Slette den og gå direkte til nettstedet selv")
                )
            ),
            learnTask(photoStop, 1, "Lær om KI-bilder", "Les kortene og svar riktig på alle spørsmål for å gå videre.",
                learnContentJson(
                    new Slide(
                        "",
                        "Hva er et KI-bilde?",
                        "Et KI-bilde er laget av et dataprogram. Det kan se ekte ut ved første øyekast, men det viser ofte mennesker eller situasjoner som aldri har eksistert på ordentlig.",
                        new String[]{
                            "Bildebeskrivelse: \"Vaktkamera viser tyven utenfor rådhuset kl. 22.14\" kan være falskt hvis personen eller stedet aldri har eksistert slik på bildet.",
                            "Bevislinje: \"Dette bildet beviser hvem som tok pengene\" er ikke nok i seg selv hvis bildet egentlig er laget av KI."
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
                        "Hva er forskjellen på KI og manipulering?",
                        "Et manipulert bilde starter ofte som et ekte bilde som noen har endret etterpå. Et KI-bilde er vanligvis laget helt fra bunnen av, og da kan mange ting i hele bildet se litt rare ut samtidig.",
                        new String[]{
                            "Manipulert: Et ekte klassebilde der noen har byttet ansiktet til en elev.",
                            "KI-generert: Et helt nytt bilde av en hendelse som aldri skjedde."
                        },
                        new String[]{
                            "Begge deler kan brukes for å lure deg.",
                            "Spør alltid om bildet kan sjekkes andre steder."
                        }
                    ),
                    new Quiz("q1", "Hva er vanlige feil i KI-genererte bilder?", new String[]{"For mange farger", "Merkelige hender og urealistisk glatt hud", "For lav bildekvalitet"}, "Merkelige hender og urealistisk glatt hud"),
                    new Quiz("q2", "Hva skiller et KI-generert bilde fra et manipulert bilde?", new String[]{"KI-bilder er alltid svart-hvitt", "KI-bilder er laget av AI, manipulerte er ekte bilder som er endret", "Manipulerte bilder har alltid bedre kvalitet"}, "KI-bilder er laget av AI, manipulerte er ekte bilder som er endret"),
                    new Quiz("q3", "Hva bør du gjøre om du er usikker på et bilde?", new String[]{"Dele det for å få andres mening", "Bruke omvendt bildesøk for å sjekke opprinnelsen", "Ignorere det"}, "Bruke omvendt bildesøk for å sjekke opprinnelsen")
                )
            ),
            learnTask(pwdStop, 1, "Lær om passord", "Les kortene og svar riktig på alle spørsmål for å gå videre.",
                learnContentJson(
                    new Slide(
                        "",
                        "Hva gjør et passord sterkt?",
                        "Et sterkt passord er langt og vanskelig å gjette. Det skal helst være noe som ikke handler om deg, så andre ikke kan finne det ut bare ved å kjenne navnet ditt, laget du spiller på eller fødselsåret ditt.",
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
                        "Hackere bruker programmer som prøver masse vanlige passord veldig fort. Hvis passordet ditt ligner på noe mange andre også bruker, kan det knekkes mye raskere enn du tror.",
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
                        "En passordfrase er flere ord satt sammen med tall eller tegn. Den kan være lettere å huske enn en rotete kode, men samtidig mye tryggere hvis du velger ord som ikke handler om deg.",
                        new String[]{
                            "Eksempel: Hest!Maanelys42Fjord",
                            "Ikke så bra: Oliver!Trondheim2014 fordi det handler om deg"
                        },
                        new String[]{
                            "Lag noe langt og litt rart.",
                            "Bruk forskjellig passord på forskjellige kontoer."
                        }
                    ),
                    new Quiz("q1", "Hva gjør et passord sterkest?", new String[]{"Det er enkelt å huske", "Det er langt og bruker ulike tegn uten personlig info", "Det inneholder navn og fødselsdato"}, "Det er langt og bruker ulike tegn uten personlig info"),
                    new Quiz("q2", "Hvilket av disse er et svakt passord?", new String[]{"Sol!Fjord#42Hest", "Ola2010", "hX9!wP$3mQ"}, "Ola2010"),
                    new Quiz("q3", "Hva er en passordfrase?", new String[]{"Et langt ord", "En rekke tilfeldige ord som danner et langt passord", "Passordet til telefonen"}, "En rekke tilfeldige ord som danner et langt passord")
                )
            ),
            learnTask(marketStop, 1, "Lær om nettsvindel", "Les kortene og svar riktig på alle spørsmål for å gå videre.",
                learnContentJson(
                    new Slide(
                        "",
                        "Hvordan ser nettsvindel ut?",
                        "En falsk nettbutikk prøver å få deg til å kjøpe noe raskt før du rekker å tenke deg om. Derfor bruker den ofte kjempestore rabatter og ord som \"kun i dag\" eller \"bare 2 igjen\".",
                        new String[]{
                            "Produktside: \"Vinterjakke før 2 499 kr - nå 199 kr - bare 2 igjen!\" er et klassisk lokketilbud.",
                            "Banner: \"SALGET SLUTTER OM 10 MINUTTER\" som kommer tilbake hver gang du åpner siden, er et dårlig tegn."
                        },
                        new String[]{
                            "Hvis tilbudet virker altfor godt, er det ofte fordi noe er galt.",
                            "Svindlere prøver å få deg til å skynde deg."
                        }
                    ),
                    new Slide(
                        "",
                        "Hvilke røde flagg bør du se etter?",
                        "Se på domenet, kontaktinformasjonen og betalingsmåten. Ekte butikker vil vanligvis vise hvem de er, hvordan du kan kontakte dem, og bruke normale betalingsløsninger.",
                        new String[]{
                            "Butikknavn: komplett.no og elkjop.no virker mer troverdige enn billig-ps5.cc eller supertilbud-now.xyz.",
                            "Betaling: \"Send beløpet med gavekortkode\" eller \"betal til privat konto\" er mistenkelig."
                        },
                        new String[]{
                            "Ukjent domene er et tegn du bør merke deg.",
                            "Rare betalingsmåter er ofte et stort varselsignal."
                        }
                    ),
                    new Slide(
                        "",
                        "Hvordan handler du tryggere?",
                        "Ta deg tid før du kjøper. Søk opp butikken, se om andre har erfaringer med den, og sjekk om kontaktinformasjonen faktisk ser ekte ut.",
                        new String[]{
                            "Se etter telefonnummer, ekte e-post og organisasjonsnavn.",
                            "Kortbetaling er tryggere enn gavekort fordi du lettere kan klage hvis noe går galt."
                        },
                        new String[]{
                            "Sjekk før du betaler.",
                            "Det er lov å vente litt før du bestemmer deg."
                        }
                    ),
                    new Quiz("q1", "Hva er et varseltegn på en useriøs nettbutikk?", new String[]{"De har mange produkter", "De krever betaling med gavekort", "De tilbyr gratis frakt"}, "De krever betaling med gavekort"),
                    new Quiz("q2", "Hva gjør betaling med gavekort risikabelt?", new String[]{"Det er saktere", "Pengene er nesten umulige å spore og få tilbake", "Du får ikke kvittering"}, "Pengene er nesten umulige å spore og få tilbake"),
                    new Quiz("q3", "Hva bør du gjøre om en nettbutikk virker mistenkelig?", new String[]{"Kjøp og håp det ordner seg", "Be venner handle der først", "Søk opp butikken og les anmeldelser"}, "Søk opp butikken og les anmeldelser")
                )
            ),
            learnTask(socialStop, 1, "Lær om sosiale medier", "Les kortene og svar riktig på alle spørsmål for å gå videre.",
                learnContentJson(
                    new Slide(
                        "",
                        "Hvorfor lures vi lettere i feeden?",
                        "På sosiale medier går ting fort, og vi scroller ofte uten å stoppe opp. Innlegg som gjør oss sinte, redde eller veldig nysgjerrige får ofte mest oppmerksomhet, og derfor er det lett å dele noe før vi har tenkt oss om.",
                        new String[]{
                            "Innlegg: \"DEL NÅ før dette blir slettet!!!\" prøver å få deg til å reagere fort i stedet for å sjekke først.",
                            "Skolerykte: \"Jeg har hørt at prøven er lekket til 7B\" kan spre seg raskt selv om ingen har vist bevis."
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
                          "body": "Brøytemannskapene har jobbet gjennom natten, og kommunen opplyser at skolene følger vanlig timeplan torsdag morgen. Elever og foresatte blir bedt om å beregne ekstra tid og følge meldinger fra skolen dersom busser blir forsinket.",
                          "source": "Trondheim kommune",
                          "author": "Ingrid Solberg",
                          "date": "2026-01-14",
                          "isReal": true
                        },
                        {
                          "headline": "SJOKK: Regjeringen stenger ALLE skoler i Norge før lunsj på grunn av iskald luft",
                          "body": "Ifølge SkoleRedning24 får alle elever fri allerede i dag, og flere lærere skal ha fått hemmelige SMS-er om å sende barna hjem. Ingen kommune eller skole har publisert noe om dette, men artikkelen hevder at vedtaket gjelder hele landet.",
                          "source": "SkoleRedning24",
                          "author": "Admin",
                          "date": "2026-01-14",
                          "isReal": false
                        },
                        {
                          "headline": "Foreldre raser etter snøkaos, men eksperter advarer mot å stole på skjermbilder alene",
                          "body": "Saken viser til flere delte skjermbilder av meldinger mellom foreldre, men oppgir ikke når de er sendt eller hvem som faktisk står bak dem. Artikkelen lenker til en side som ser ut som lokalavis, men ingen konkret skole eller kommune er sitert direkte.",
                          "source": "Trondheimnytt.com",
                          "author": "Nyhetsdesk",
                          "isReal": false
                        },
                        {
                          "headline": "Forskere: Ny snøspray over byen gjør at skolegårder blir helt isfrie på fem sekunder",
                          "body": "En ukjent blogg hevder at kommunen har testet en hemmelig kjemispray som smelter all is med én gang og gjør skolegårder varme resten av vinteren. Påstanden viser ikke til forskning, navn på eksperter eller noen steder teknologien faktisk brukes.",
                          "source": "Vintermirakel.blog",
                          "author": "Maks Nyhet",
                          "date": "2026-01-13",
                          "isReal": false
                        }
                      ],
                      "explanation": "Den ekte saken viser til en kjent offentlig kilde og konkrete råd. De falske artiklene bruker dramatiske ord, uklare kilder eller påstander som ikke henger sammen med virkeligheten."
                    }
                    """
            ),
            fakeNewsTask(
                newsStop,
                3,
                "Mobilforbud på buss",
                "Les overskriftene som om de dukket opp i feeden din på vei hjem fra skolen.",
                """
                    {
                      "articles": [
                        {
                          "headline": "AtB tester stille sone på utvalgte bussavganger i rushtiden",
                          "body": "AtB opplyser at ordningen skal testes på tre avganger i to uker for å se om flere ønsker en roligere busstur. Tiltaket er frivillig og gjelder bare bakerst i bussen på de aktuelle rutene.",
                          "source": "AtB pressemelding",
                          "author": "Marius Heggli",
                          "date": "2026-02-03",
                          "isReal": true
                        },
                        {
                          "headline": "NÅ KOMMER MOBILBOT: 1500 kroner hvis du ser på TikTok på bussen",
                          "body": "Flere innlegg hevder at kontrollører allerede deler ut bøter til ungdom som bruker mobil på buss til og fra skolen. Ingen viser til vedtak, dato eller noen offisiell melding fra AtB eller kommunen.",
                          "source": "DelDetVidere24",
                          "author": "Redaksjonen",
                          "date": "2026-02-03",
                          "isReal": false
                        },
                        {
                          "headline": "AtB vurderer strengere mobilregler etter klager fra voksne reisende",
                          "body": "Artikkelen hevder at transportselskapet vurderer å forby lyd og video på buss for alle under 18 år. Den viser til at 'flere passasjerer reagerer', men mangler dato for når vurderingen skal tas opp og oppgir bare en vag kilde omtalt som 'en person nær kollektivmiljøet'.",
                          "source": "atb-nyheter.net",
                          "author": "Signe Dahl",
                          "isReal": false
                        },
                        {
                          "headline": "Ny sensor i taket skal automatisk blokkere mobilsignaler på alle busser i Midt-Norge",
                          "body": "Artikkelen påstår at et nytt system kan lese skjermene til passasjerene og slå av internett for dem som ser på videoer. Den forklarer ikke hvordan dette skulle være lovlig eller teknisk mulig, og ingen seriøse kilder omtaler systemet.",
                          "source": "framtidsbuss.info",
                          "author": "TekTeam",
                          "date": "2026-02-02",
                          "isReal": false
                        }
                      ],
                      "explanation": "Den ekte saken er konkret og begrenset til en liten test. De falske artiklene mangler tydelige kilder, datoer eller beskriver løsninger som høres usannsynlige ut."
                    }
                    """
            ),
            fakeNewsTask(
                newsStop,
                4,
                "Gratis spillvaluta",
                "Fire saker lover ulike ting til spillere. Bare én tåler en kritisk sjekk.",
                """
                    {
                      "articles": [
                        {
                          "headline": "Spillselskap advarer mot falske gavekort og gratis valuta-lenker",
                          "body": "I en sikkerhetsmelding ber spillselskapet brukere ignorere sider som lover gratis valuta eller eksklusive skins mot innlogging. Selskapet minner om at all bonusvaluta deles ut inne i spillet eller på offisielle kampanjesider.",
                          "source": "Spillselskapets sikkerhetsblogg",
                          "author": "Security Team",
                          "date": "2026-03-08",
                          "isReal": true
                        },
                        {
                          "headline": "HEMMELIG PÅSKEKODE gir alle norske barn 50 000 spillmynter i kveld",
                          "body": "Saken lover at alle som logger inn før midnatt får gratis spillvaluta og sjeldne skins sendt direkte til kontoen. Kampanjen finnes ikke på spillets egne kanaler, og artikkelen prøver å presse leseren til å handle raskt.",
                          "source": "GameBonusGratis.xyz",
                          "author": "BonusNytt",
                          "date": "2026-03-08",
                          "isReal": false
                        },
                        {
                          "headline": "Kjent e-sportprofil sier nye bonusmynter kan hentes via ekstern partner",
                          "body": "Saken viser til en partnerkampanje som skal være koblet til en stor turnering, men nevner verken hvilken turnering eller hvilken partner som står bak. Det finnes heller ingen dato eller offisiell lenke til spillselskapet, bare en oppfordring om å registrere seg raskt for å ikke gå glipp av tilbudet.",
                          "source": "E-sportNorge24",
                          "author": "LiveDesk",
                          "isReal": false
                        },
                        {
                          "headline": "Skjult server i Sverige deler ut gratis skins hvis du oppgir passord og telefonnummer",
                          "body": "En ukjent side hevder at en privat server samarbeider med spillet og kan fylle opp kontoer med premium-innhold på sekunder. Påstanden er umulig å sjekke, og siden ber om både passord, telefonnummer og engangskode.",
                          "source": "UltraSkinDrop.net",
                          "author": "AK Gamer",
                          "date": "2026-03-07",
                          "isReal": false
                        }
                      ],
                      "explanation": "Den ekte saken advarer og viser til offisielle kanaler. De falske sakene lover urealistiske gevinster, bruker hastverk eller peker til uklare kampanjer uten sporbare kilder."
                    }
                    """
            ),
            phishingTask(
                mailStop,
                2,
                "Bankvarsel",
                "DNB",
                "kundevarsling@dnb-kundeservice.com",
                "Vi har satt betalingen din på pause",
                """
                Hei Oliver,

                Vi oppdaget et uvanlig forsøk på å gjennomføre en betaling fra kortet ditt på 4 890 kr til Steam Market. Dersom dette ikke ble gjort av deg, må du bekrefte kontoen din innen 30 minutter for å unngå midlertidig sperring av nettbanken.

                Kontroller opplysningene dine her: dnb-kontroll.com/bekreft

                Med vennlig hilsen
                DNB Kundeservice
                """,
                List.of(
                    new Clue("sender", "sender", "kundevarsling@dnb-kundeservice.com", true, "Avsenderen ser ekte ut ved første blikk, men domenet er ikke dnb.no."),
                    new Clue("link1", "link", "dnb-kontroll.com/bekreft", true, "Lenken peker til et annet domene enn banken sin offisielle nettside."),
                    new Clue("urgency", "text", "innen 30 minutter", true, "Svindlere bruker tidspress for å få deg til å klikke før du rekker å sjekke."),
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
                "Pakken din er forsinket i terminal",
                """
                Hei!

                Vi forsøkte å sende pakken din videre til utleveringsstedet, men sendingen er stoppet fordi det mangler et lite toll- og behandlingsgebyr på 19 kr. Betal i dag for å unngå at pakken blir sendt i retur til avsender.

                Betal gebyret her: posten-oppdatering.net/betaling

                Hilsen Posten
                """,
                List.of(
                    new Clue("sender", "sender", "varsling@posten-levering.net", true, "Adressen ligner på Posten, men bruker ikke det offisielle domenet posten.no."),
                    new Clue("link1", "link", "posten-oppdatering.net/betaling", true, "Betalingslenken går til en side som ikke tilhører Posten."),
                    new Clue("urgency", "text", "Betal i dag", true, "Kunstig hastverk er et vanlig grep i phishing."),
                    new Clue("sender_name", "sender_name", "Posten", false, "Avsendernavnet kan se riktig ut selv når selve e-postadressen er falsk.")
                ),
                "Dette ligner på en ekte pakkemelding, men både avsender og lenke er feil. Det lille gebyret og tidspresset er klassiske phishing-grep."
            ),
            phishingTask(
                mailStop,
                4,
                "Skolekonto",
                "IT-support VGS",
                "it-support@skole-login.com",
                "Kontoen din mister tilgang til Teams i dag",
                """
                Hei,

                Vi oppdaterer innloggingen for elever etter flere feilforsøk mot skolekontoer denne uka. For å beholde tilgang til Teams, Canvas og skolemail må du logge inn og bekrefte brukeren din før kl. 14.00 i dag.

                Gå til elevportalen her: skole-login.com/verify

                Mvh
                IT-support
                """,
                List.of(
                    new Clue("sender", "sender", "it-support@skole-login.com", true, "Skolen ville brukt sitt eget domene, ikke skole-login.com."),
                    new Clue("link1", "link", "skole-login.com/verify", true, "Lenken leder til et ukjent domene som kan stjele skoleinnloggingen din."),
                    new Clue("urgency", "text", "før kl. 14.00 i dag", true, "Tidspress gjør det lettere å lure elever til å handle raskt."),
                    new Clue("greeting", "text", "Hei,", false, "En vanlig hilsen er ikke i seg selv et tegn på svindel. Du må se på domenet og lenken også.")
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
                      "src": "",
                      "alt": "En person sitter på en benk i en park. Hånden som holder mobilen har unaturlige fingre, og kanten på jakken flyter litt inn i bakgrunnen.",
                      "label": "Bilde A",
                      "explanation": "Legg merke til hånden rundt mobilen: fingrene flyter sammen og får en form som ikke ser menneskelig ut. Jakken og benken glir også litt inn i hverandre ved kanten, noe som er typisk for KI-genererte bilder."
                    },
                    {
                      "id": "image_1",
                      "src": "",
                      "alt": "Utsikt over en by tatt fra et vindu. Bildet har naturlige refleksjoner, vanlig støy og realistiske linjer i bygningene.",
                      "label": "Bilde B",
                      "explanation": "Dette bildet har vanlige mobilkamerategn som litt støy i himmelen og naturlige refleksjoner i glasset. Linjene i bygningene og detaljene i bakgrunnen holder seg konsistente hele veien."
                    }
                  ],
                  "question": "Sorter hvert bilde: er det ekte, KI-generert eller manipulert?"
                }
                """,
                "{\"image_0\": \"AI_GENERATED\", \"image_1\": \"REAL\"}"),
            aiPhotoTask(photoStop, 3, "Bytorget", "Finn hvilket bilde som er ekte og kan brukes som bevis.",
                """
                {
                  "images": [
                    {
                      "id": "image_0",
                      "src": "",
                      "alt": "En person står på et bytorg. Flere vinduer og personer i bakgrunnen ser nesten identiske ut.",
                      "label": "Bilde A",
                      "explanation": "Bakgrunnen gjentar de samme mønstrene flere steder, særlig i vinduene og menneskene bak personen. Slike kopierte detaljer er et vanlig tegn på at bildet er generert av KI."
                    },
                    {
                      "id": "image_1",
                      "src": "",
                      "alt": "Et mobilbilde av samme torg med naturlig lys, vanlige skygger og litt uskarphet i bevegelse.",
                      "label": "Bilde B",
                      "explanation": "Her oppfører lyset seg naturlig, og små ting som bevegelsesuskarphet og skjeve skygger ser ekte ut. Ingenting i ansikter, klær eller bygninger bryter mønsteret vi forventer fra et vanlig mobilbilde."
                    },
                    {
                      "id": "image_2",
                      "src": "",
                      "alt": "Et portrett på torget der huden er veldig glatt, og ansiktet virker retusjert sammenlignet med resten av bildet.",
                      "label": "Bilde C",
                      "explanation": "Ansiktet er unaturlig glatt og nesten uten hudtekstur, mens resten av bildet fortsatt har støy og detaljer. Det tyder på at bildet er ekte i bunn, men at personen er manipulert etterpå."
                    }
                  ],
                  "question": "Sorter hvert bilde: ekte, KI-generert eller manipulert?"
                }
                """,
                "{\"image_0\": \"AI_GENERATED\", \"image_1\": \"REAL\", \"image_2\": \"MANIPULATED\"}"),
            aiPhotoTask(photoStop, 4, "Bevisbildet", "Kun ett bilde kan brukes som ekte bevis. Finn det.",
                """
                {
                  "images": [
                    {
                      "id": "image_0",
                      "src": "",
                      "alt": "Et bilde fra en gangvei der skyggen til personen faller i én retning, mens lyset på bakken tilsier en annen.",
                      "label": "Bilde A",
                      "explanation": "Skyggene peker i forskjellige retninger selv om scenen bare ser ut til å ha én lyskilde. Når lys og skygge ikke henger sammen, er bildet ofte manipulert."
                    },
                    {
                      "id": "image_1",
                      "src": "",
                      "alt": "Et bilde med skilt og tekst i bakgrunnen der bokstavene er rare, skeive og delvis uleselige.",
                      "label": "Bilde B",
                      "explanation": "Tekst er noe KI ofte sliter med, og her blir bokstavene uklare og meningsløse når du ser nærmere. Det gjør bildet lite troverdig som bevis."
                    },
                    {
                      "id": "image_2",
                      "src": "",
                      "alt": "Et klart mobilbilde fra samme sted med naturlige skygger, leselige skilt og vanlige detaljer i klær og ansikter.",
                      "label": "Bilde C",
                      "explanation": "Her er både tekst, skygger og små detaljer konsistente gjennom hele bildet. Det er akkurat slike naturlige feil og variasjoner vi forventer i et ekte mobilfoto."
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
                    { "id": "b", "value": "Emma2014" },
                    { "id": "c", "value": "Katt" },
                    { "id": "d", "value": "F!sk3Taco#92" }
                  ],
                  "explanation": "F!sk3Taco#92 er sterkest fordi det er langt og blander store og små bokstaver, tall og spesialtegn. Navn og årstall er svake."
                }
                """,
                "{\"selected\": \"d\"}"),
            passwordTask(pwdStop, 3, "Gjør passordet bedre", "Velg det passordet som er best forbedret.",
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
                "{\"selected\": \"d\"}"),
            passwordTask(pwdStop, 4, "Bygg et sterkt passord", "Bruk brikkene til å lage et passord som er sterkt nok.",
                """
                {
                  "type": "BUILDER",
                  "question": "Bygg et passord som er sterkt nok til å låse opp bankboksen",
                  "words": ["Tiger", "Måne", "Pizza", "Hund", "Sol", "Isbjørn", "Fjord"],
                  "symbols": ["!", "#", "@", "?", "&", "*"],
                  "numbers": ["7", "42", "99", "3", "2026"],
                  "pitfalls": ["OlaErBest", "2005", "hund"],
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
            socialMediaTask(socialStop, 2, "Del eller vent?", "Velg riktig handling.",
                """
                {
                  "type": "CHOOSE_ACTION",
                  "post": {
                    "username": "Maja 9B", "handle": "@maja_9b", "avatar": "📚",
                    "content": "DELE DETTE NÅ!!! Rektor skal visst forby alle mobiler fra mandag, også i storefri 😱 Kusina til venninna mi sier lærerne fikk beskjed i går kveld, men ingen voksne vil si noe ennå!!!",
                    "likes": 287, "comments": 46, "timestamp": "I dag kl. 08:14", "verified": false
                  },
                  "question": "Hva bør du gjøre med dette innlegget?",
                  "options": [
                    { "id": "SHARE", "text": "Del det videre med en gang" },
                    { "id": "WAIT", "text": "Vent og se om det dukker opp andre steder" },
                    { "id": "CHECK_SOURCES", "text": "Sjekk kilden og faktasjekk før du gjør noe" },
                    { "id": "ASK_ADULT", "text": "Spør en voksen" }
                  ],
                  "explanation": "Innlegget spiller på stress og rykter fra skolemiljøet, men viser ikke til noen faktisk beskjed fra skolen. Slike ting bør sjekkes i Visma, på skolens nettside eller med en lærer før du deler videre."
                }
                """,
                "{\"action\": \"CHECK_SOURCES\"}"),
            socialMediaTask(socialStop, 3, "Følelser på sosiale medier", "Identifiser hvilke følelser innlegget prøver å skape.",
                """
                {
                  "type": "CHOOSE_ACTION",
                  "post": {
                    "username": "LukasVG1", "handle": "@lukas_vg1", "avatar": "⚽",
                    "content": "Helt sykt hvis dette stemmer: noen sier prøven i samfunnsfag allerede er lekket i en Snap-gruppe 😡 Del så alle får vite hvor urettferdig skolen er!!!",
                    "likes": 613, "comments": 128, "timestamp": "I dag kl. 10:27", "verified": false
                  },
                  "question": "Hva bør du gjøre?",
                  "options": [
                    { "id": "SHARE", "text": "Del med en gang - dette er viktig!" },
                    { "id": "CHECK_SOURCES", "text": "Sjekk om det er sant før du deler" },
                    { "id": "IGNORE", "text": "Ignorer innlegget" },
                    { "id": "ASK_ADULT", "text": "Spør en voksen om råd" }
                  ],
                  "explanation": "Innlegget prøver å gjøre deg sint og få deg til å reagere før du vet om det faktisk stemmer. Når skole-rykter kobles med sterk språkbruk og press om å dele, bør du alltid sjekke først."
                }
                """,
                "{\"action\": \"CHECK_SOURCES\"}"),
            socialMediaTask(socialStop, 4, "Finn det mest illegitime innlegget", "Velg innlegget med minst troverdighet.",
                """
                {
                  "type": "IDENTIFY_WORST",
                  "question": "Hvilket innlegg er mest illegitimt?",
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
        taskRepository.saveAll(tasks);

        medalRepository.saveAll(List.of(
            medal("Nyhetsjeger", "Fullfør Nyhetskvartalet.", stops.get(0)),
            medal("Bildegransker", "Fullfør Fotografen.", stops.get(1)),
            medal("E-postetterforsker", "Fullfør Postkontoret.", stops.get(2)),
            medal("Trygg handler", "Fullfør Markedsplassen.", stops.get(3)),
            medal("Sosial speider", "Fullfør Den sosiale møteplassen.", stops.get(4)),
            medal("Passordvokter", "Fullfør Passordbanken.", stops.get(5)),
            medal("Datasenterhelt", "Fullfør Datasenteret.", stops.get(6))
        ));
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
        Task task = baseTask(stop, orderIndex, title,
            "Klikk på alle mistenkelige deler av e-posten.", TaskType.PHISHING_EMAIL);
        task.setContentJson(phishingContentJson(fromName, fromEmail, subject, body, clues, explanation));
        List<String> requiredClueIds = clues.stream()
            .filter(Clue::isClue)
            .map(Clue::id)
            .toList();
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
