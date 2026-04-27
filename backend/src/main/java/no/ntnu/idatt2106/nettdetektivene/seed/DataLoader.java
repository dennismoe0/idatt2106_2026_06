package no.ntnu.idatt2106.nettdetektivene.seed;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.List;
import java.util.Map;

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
            syncExistingMysteryContent();
            return;
        }

        List<Stop> stops = stopRepository.saveAll(List.of(
            stop("Nyhetskvartalet",
                 "Avslør hvilke nyheter som er ekte og hvilke som prøver å lure deg.",
                 "FAKE_NEWS", 1, false,
                 "Falske nyheter bruker gjerne skremmende overskrifter og anonyme kilder. Sjekk alltid hvem som har skrevet saken: er nettadressen til et kjent mediehus? Søk opp saken på andre seriøse nettsteder for å se om historien stemmer. Overdrevne påstander uten dokumentasjon er et varseltegn."),
            stop("Postkontoret",
                 "Undersøk e-poster og lær hvordan svindelforsøk kan se ut.",
                 "PHISHING_EMAIL", 3, false,
                 "Phishing-e-poster later som de er fra banker, skoler eller kjente selskaper for å lure deg til å gi fra deg passord eller penger. Se etter skrivefeil, ukjente avsenderadresser og lenker der nettadressen ikke stemmer med avsenderen. En ekte avsender ber aldri om passord eller betalingsinformasjon via e-post."),
            stop("Fotografen",
                 "Se etter spor i bilder og lær å kjenne igjen manipulasjon.",
                 "AI_PHOTO", 2, false,
                 "Bilder kan manipuleres og AI kan lage realistiske falske bilder. Se etter unaturlige detaljer: rare fingre, jevne bakgrunner og uskarp tekst er vanlige feil. Du kan bruke omvendt bildesøk til å sjekke om et bilde er tatt ut av en helt annen sammenheng enn det påstår."),
            stop("Passordbanken",
                 "Bygg sterke passord og beskytt kontoene dine.",
                 "PASSWORD", 6, false,
                 "Et sterkt passord er langt, tilfeldig og unikt for hver konto du bruker. En rekke tilfeldige ord er lettere å huske og vanskeligere å knekke enn korte passord med spesialtegn. Del aldri passordet ditt med andre, og bruk aldri samme passord på flere nettsteder."),
            stop("Markedsplassen",
                 "Vurder annonser, betalinger og trygg handel på nett.",
                 "MARKETPLACE", 4, false,
                 "Svindel på nett bruker priser som er for gode til å være sanne, krever betaling på forhånd og har vage eller kopierte produktbeskrivelser. Sjekk alltid selgerprofilen og les tilbakemeldinger fra andre kjøpere. Betal aldri med gavekort eller kryptovaluta - det er nesten umulig å spore."),
            stop("Den sosiale møteplassen",
                 "Ta gode valg i meldinger, kommentarer og deling.",
                 "SOCIAL_MEDIA", 5, false,
                 "Sosiale medier viser deg mest det du allerede er enig i, noe som kan gjøre det vanskelig å se helhetsbildet. Fremmede som tar kontakt og raskt ber om personlig informasjon kan ha skjulte hensikter. Del aldri telefonnummer, adresse, passord eller bilder du ikke vil at alle skal se."),
            stop("Datasenteret",
                 "Bruk alt du har lært i den siste digitale saken.",
                 "FINAL_BOSS", 7, true,
                 "Du har nå lært de viktigste detektivferdighetene: gjenkjenne falske nyheter, phishing-e-poster, manipulerte bilder, svake passord, nettsvindel og sosiale medier-feller. Den viktigste regelen er å stoppe og tenke én ekstra gang før du klikker, deler eller svarer på noe du er usikker på.")
        ));

        Stop newsStop = stops.get(0);
        Stop mailStop = stops.get(1);
        Stop photoStop = stops.get(2);
        Stop passwordStop = stops.get(3);
        Stop marketStop = stops.get(4);
        Stop socialStop = stops.get(5);
        Stop finalBossStop = stops.get(6);

        taskRepository.saveAll(List.of(
            fakeNewsTask(
                newsStop,
                1,
                "Vinterstengte skoler",
                "Finn ut hvilken av de to nyhetssakene som er ekte.",
                """
                    {
                      "articles": [
                        {
                          "headline": "Kommunen holder skolene åpne etter snøfallet",
                          "body": "Kommunen melder at brøytemannskapene har jobbet gjennom natten. Elever bes følge vanlig skolerute, men beregne ekstra tid.",
                          "source": "Trondheim kommune",
                          "isReal": true
                        },
                        {
                          "headline": "Alle skoler i Norge stenger i morgen på grunn av kulde",
                          "body": "En anonym kilde sier at regjeringen har bestemt at alle skoler må holde stengt, men ingen offentlige kanaler har bekreftet dette.",
                          "source": "NorskNyhet24.info",
                          "isReal": false
                        }
                      ],
                      "explanation": "Den andre artikkelen er falsk fordi den bruker en anonym kilde, mangler offentlig bekreftelse og kommer fra en ukjent nettadresse."
                    }
                    """
            ),
            fakeNewsTask(
                newsStop,
                2,
                "Mobilforbud på buss",
                "Sjekk kilder og detaljer før du bestemmer deg.",
                """
                    {
                      "articles": [
                        {
                          "headline": "AtB tester stille sone på utvalgte bussruter",
                          "body": "AtB opplyser at ordningen er frivillig og skal testes på tre ruter i to uker før den evalueres.",
                          "source": "AtB pressemelding",
                          "isReal": true
                        },
                        {
                          "headline": "Nå blir mobiltelefon forbudt på alle busser",
                          "body": "Passasjerer som bruker mobil kan få bot allerede i dag, ifølge et innlegg som deles mye på sosiale medier.",
                          "source": "DelDetteNå.net",
                          "isReal": false
                        }
                      ],
                      "explanation": "Den andre artikkelen er falsk fordi den overdriver, mangler offisiell kilde og prøver å få leseren til å dele raskt."
                    }
                    """
            ),
            fakeNewsTask(
                newsStop,
                3,
                "Gratis spillvaluta",
                "Avgjør om saken er troverdig.",
                """
                    {
                      "articles": [
                        {
                          "headline": "Spillselskap advarer mot falske gavekort",
                          "body": "Selskapet ber spillere aldri oppgi passord på nettsider som lover gratis valuta utenfor spillets egen butikk.",
                          "source": "Spillselskapets sikkerhetsblogg",
                          "isReal": true
                        },
                        {
                          "headline": "Hemmelig lenke gir alle barn gratis spillpenger",
                          "body": "Du må bare logge inn med brukernavn og passord før midnatt. Tilbudet finnes ikke på den offisielle nettsiden.",
                          "source": "GameBonusGratis.xyz",
                          "isReal": false
                        }
                      ],
                      "explanation": "Den andre artikkelen er falsk fordi den lover en urealistisk premie og ber om innlogging på en uoffisiell side."
                    }
                    """
            ),
            clueRiddleTask(
                newsStop,
                4,
                "Gåtespor: Hva skal vi ikke overse?",
                "Etter nyhetsoppgavene bruker du kildekritikk til å finne et spor til Datasenteret.",
                "Hvorfor løser du denne? Fordi falske nyheter ofte får oss til å se på det mest dramatiske først. Detektiver må se etter den lille detaljen som faktisk kan sjekkes.",
                "Et vitne så tyven ved Bytorget: mørk jakke, noe rødt på hodet, lyse sko og en stor bærepose med bibliotekslogo.",
                "Hvilken detalj er det viktigste sporet videre?",
                """
                    [
                      { "id": "red_hat", "label": "Noe rødt på hodet" },
                      { "id": "library_bag", "label": "Bæreposen med bibliotekslogo" },
                      { "id": "light_shoes", "label": "Lyse sko" }
                    ]
                    """,
                "library_bag",
                "Riktig. Klær kan ligne på mange personer, men bibliotekslogoen peker mot noen med kobling til biblioteket."
            ),
            phishingTask(
                mailStop,
                1,
                "Bankvarsel",
                "Velg hva du bør gjøre med e-posten.",
                "DNB Kundeservice",
                "support@dnb-kundeservice.com",
                "Viktig: Bekreft kontoen din",
                "Kjære kunde, kontoen din blir stengt om 2 timer. Klikk på lenken og bekreft BankID-informasjonen din.",
                List.of("fromEmail", "link", "urgency"),
                "Avsenderadressen er ikke dnb.no, meldingen haster kunstig og ber deg klikke på en mistenkelig lenke."
            ),
            phishingTask(
                mailStop,
                2,
                "Pakkemelding",
                "Rapporter meldingen hvis den virker mistenkelig.",
                "Posten Norge",
                "pakke@posten-levering.net",
                "Pakken din mangler porto",
                "Hei! Betal 19 kroner innen i kveld for å unngå at pakken returneres. Betal her.",
                List.of("fromEmail", "payment", "urgency"),
                "Avsenderadressen ligner på Posten, men er ikke offisiell. Små gebyrer og hastverk brukes ofte i svindel."
            ),
            phishingTask(
                mailStop,
                3,
                "Skolekonto",
                "Bestem riktig handling.",
                "IT-avdelingen",
                "it-hjelp@skole-login.com",
                "Passordet ditt utløper i dag",
                "Logg inn med skolebrukeren din på lenken under for å beholde tilgang til Teams og e-post.",
                List.of("fromEmail", "loginRequest", "link"),
                "E-posten ber om innlogging via et ukjent domene. IT-meldinger bør sjekkes mot skolens offisielle kanaler."
            ),
            clueRiddleTask(
                mailStop,
                4,
                "Gåtespor: Hvem kjente lånehistorikken?",
                "Etter e-postoppgavene undersøker du hvilken privat informasjon svindleren brukte.",
                "Hvorfor løser du denne? Fordi phishing ofte bruker ekte detaljer for å virke troverdig. Spørsmålet er hvem som kunne vite detaljen.",
                "Svindel-e-posten nevnte en bok ordføreren lånte for 3 år siden.",
                "Hvor bør du lete etter hvem som kunne vite dette?",
                """
                    [
                      { "id": "stream_chat", "label": "I chatten til en streamer" },
                      { "id": "library_registry", "label": "I bibliotekets låneregister" },
                      { "id": "market_reviews", "label": "I anmeldelser av nettbutikker" }
                    ]
                    """,
                "library_registry",
                "Riktig. Lånehistorikk finnes i bibliotekets systemer, så sporet peker mot noen med tilgang der."
            ),
            aiPhotoTask(
                photoStop,
                1,
                "Parkbilder",
                "Er bildet ekte, KI-generert eller manipulert?",
                """
                    {
                      "images": [
                        {
                          "id": "image_0",
                          "src": "",
                          "alt": "En person sitter på en benk i en park. Fingrene ser litt rare ut.",
                          "label": "Bilde A"
                        },
                        {
                          "id": "image_1",
                          "src": "",
                          "alt": "Utsikt over en by tatt fra et vindu. Normalt mobilbilde.",
                          "label": "Bilde B"
                        }
                      ],
                      "question": "Sorter hvert bilde: er det ekte, KI-generert eller manipulert?"
                    }
                    """,
                """
                    { "image_0": "AI_GENERATED", "image_1": "REAL" }
                    """
            ),
            aiPhotoTask(
                photoStop,
                2,
                "Bytorget",
                "Finn hvilket bilde som er ekte og kan brukes som bevis.",
                """
                    {
                      "images": [
                        {
                          "id": "image_0",
                          "src": "",
                          "alt": "En person på et torg. Bakgrunnen gjentar seg tydelig.",
                          "label": "Bilde A"
                        },
                        {
                          "id": "image_1",
                          "src": "",
                          "alt": "Et mobilbilde av samme torg. Normalt lys og naturlig bakgrunn.",
                          "label": "Bilde B"
                        },
                        {
                          "id": "image_2",
                          "src": "",
                          "alt": "Et bilde der ansiktet er urealistisk glatt og jevnt.",
                          "label": "Bilde C"
                        }
                      ],
                      "question": "Sorter hvert bilde: ekte, KI-generert eller manipulert?"
                    }
                    """,
                """
                    { "image_0": "AI_GENERATED", "image_1": "REAL", "image_2": "MANIPULATED" }
                    """
            ),
            aiPhotoTask(
                photoStop,
                3,
                "Bevisbildet",
                "Kun ett bilde kan brukes som ekte bevis. Finn det.",
                """
                    {
                      "images": [
                        {
                          "id": "image_0",
                          "src": "",
                          "alt": "Bilde med uvanlige skygger som ikke stemmer med lyskilden.",
                          "label": "Bilde A"
                        },
                        {
                          "id": "image_1",
                          "src": "",
                          "alt": "Bilde der teksten på skiltene i bakgrunnen er uskarp og uleselig.",
                          "label": "Bilde B"
                        },
                        {
                          "id": "image_2",
                          "src": "",
                          "alt": "Et klart mobilbilde. Alle detaljer ser naturlige ut.",
                          "label": "Bilde C"
                        }
                      ],
                      "question": "Hvilket bilde kan vi stole på som ekte bevis?"
                    }
                    """,
                """
                    { "image_0": "MANIPULATED", "image_1": "AI_GENERATED", "image_2": "REAL" }
                    """
            ),
            clueRiddleTask(
                photoStop,
                4,
                "Gåtespor: Når ble bildet tatt?",
                "Etter bildeoppgavene bruker du bevisbildet til å forstå tidslinjen.",
                "Hvorfor løser du denne? Bilder kan lure oss, men ekte overvåkningsbilder kan vise tid og sted. Det hjelper oss å sjekke forklaringer.",
                "Et ekte overvåkningsbilde viser en person som går inn i Internettbyens Bibliotek kl. 20:47, etter stengetid.",
                "Hva betyr dette for etterforskningen?",
                """
                    [
                      { "id": "anyone", "label": "Hvem som helst kunne gått inn" },
                      { "id": "after_hours_access", "label": "Tyven trengte tilgang etter stengetid" },
                      { "id": "photo_fake", "label": "Bildet kan ikke brukes" }
                    ]
                    """,
                "after_hours_access",
                "Riktig. Etter stengetid peker mot noen med nøkkel, kode eller ansatt-tilgang."
            ),
            passwordTask(
                passwordStop,
                1,
                "Velg det tryggeste passordet",
                "Finn ut hvilket passord som er best.",
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
                """
                    { "selected": "d" }
                    """
            ),
            passwordTask(
                passwordStop,
                2,
                "Gjør passordet bedre",
                "Velg det passordet som er best forbedret.",
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
                """
                    { "selected": "d" }
                    """
            ),
            passwordTask(
                passwordStop,
                3,
                "Bygg et sterkt passord",
                "Bruk brikkene til å lage et passord som er sterkt nok.",
                """
                    {
                      "type": "BUILDER",
                      "question": "Bygg et passord som er sterkt nok til å låse opp bankboksen",
                      "words": ["Tiger", "Måne", "Pizza", "Hund", "Sol", "Isbjørn", "Fjord"],
                      "symbols": ["!", "#", "@", "?", "&", "*"],
                      "numbers": ["7", "42", "99", "3", "2026"],
                      "minStrength": "STRONG",
                      "explanation": "Et sterkt passord er langt, bruker store og små bokstaver, tall og spesialtegn, og inneholder ikke personlig informasjon."
                    }
                    """,
                """
                    { "minStrength": "STRONG" }
                    """
            ),
            clueRiddleTask(
                passwordStop,
                4,
                "Gåtespor: Hvem lagde passordet?",
                "Etter passordoppgavene bruker du passordspor til å finne hvem som hadde kontroll.",
                "Hvorfor løser du denne? Passord kan avsløre vaner, roller og gamle systemer. Et passord er ikke bare en nøkkel - det kan også være et spor.",
                "Tyvekontoen brukte passordet BibliotekAdmin2019. Det ble satt under en IT-oppgradering i 2019.",
                "Hvem peker dette sterkest mot?",
                """
                    [
                      { "id": "random_customer", "label": "En tilfeldig kunde" },
                      { "id": "active_2019_staff", "label": "En ansatt som var med på IT-oppgraderingen i 2019" },
                      { "id": "gaming_friend", "label": "En gaming-venn" }
                    ]
                    """,
                "active_2019_staff",
                "Riktig. Passordet peker mot noen som jobbet med bibliotekets IT-oppgradering i 2019."
            ),
            marketplaceTask(
                marketStop,
                1,
                "Nettbutikk-vurdering",
                "Hva er det mest mistenkelige med denne nettsiden?",
                """
                    {
                      "type": "IDENTIFY",
                      "imageUrl": "",
                      "siteName": "super-deals-norge.xyz",
                      "question": "Hva er mest mistenkelig med denne nettsiden?",
                      "options": [
                        { "id": "a", "text": "Prisen er for lav til å være sann - 90% rabatt" },
                        { "id": "b", "text": "Siden mangler kontaktinformasjon og adresse" },
                        { "id": "c", "text": "URL-en er ikke et kjent norsk nettsted (.xyz er uvanlig)" },
                        { "id": "d", "text": "Alt av dette er mistenkelig" }
                      ],
                      "explanation": "Alle tre tegnene er varselsignaler: ekstremt lav pris, manglende kontaktinfo og ukjent domene."
                    }
                    """,
                """
                    { "selected": "d" }
                    """
            ),
            marketplaceTask(
                marketStop,
                2,
                "Betalingsvarsel",
                "Velg hva du bør gjøre.",
                """
                    {
                      "type": "IDENTIFY",
                      "imageUrl": "",
                      "siteName": "billig-elektronikk.cc",
                      "question": "Nettsiden ber deg betale med gavekort. Hva bør du gjøre?",
                      "options": [
                        { "id": "a", "text": "Kjøp med gavekort - det er raskest" },
                        { "id": "b", "text": "Undersøk siden nærmere på internett før du gjør noe" },
                        { "id": "c", "text": "Ikke kjøp - betaling med gavekort er et klassisk svindeltriks" },
                        { "id": "d", "text": "Send dem en e-post for å dobbeltsjekke" }
                      ],
                      "explanation": "Betaling med gavekort er nesten alltid svindel - pengene er vanskelige å spore og sjelden mulige å få tilbake."
                    }
                    """,
                """
                    { "selected": "c" }
                    """
            ),
            marketplaceTask(
                marketStop,
                3,
                "Finn svindelsiden",
                "Hvilken av de fire sidene er mest sannsynlig svindel?",
                """
                    {
                      "type": "RANK",
                      "question": "Hvilken av disse nettstedene er mest sannsynlig svindel?",
                      "sites": [
                        { "id": "a", "name": "komplett.no", "imageUrl": "" },
                        { "id": "b", "name": "netthandel-billig.cc", "imageUrl": "" },
                        { "id": "c", "name": "elkjop.no", "imageUrl": "" },
                        { "id": "d", "name": "finn.no", "imageUrl": "" }
                      ],
                      "explanation": "netthandel-billig.cc bruker et uvanlig toppdomene (.cc), er ikke et kjent norsk nettsted og har ingen kjent historikk."
                    }
                    """,
                """
                    { "selected": "b" }
                    """
            ),
            socialMediaTask(
                socialStop,
                1,
                "Del eller vent?",
                "Velg riktig handling.",
                """
                    {
                      "type": "CHOOSE_ACTION",
                      "post": {
                        "username": "TrondheimNytt",
                        "handle": "@trondheim_nytt",
                        "avatar": "📰",
                        "content": "DELE DETTE NÅ!!! Ordførerens pengeskandal er MYE VERRE enn noen tror 😱😱😱 Anonym kilde avslører det ingen tør si høyt!!!",
                        "likes": 2847,
                        "comments": 431,
                        "timestamp": "3 timer siden",
                        "verified": false
                      },
                      "question": "Hva bør du gjøre med dette innlegget?",
                      "options": [
                        { "id": "SHARE", "text": "Del det videre med en gang" },
                        { "id": "WAIT", "text": "Vent og se om det dukker opp andre steder" },
                        { "id": "CHECK_SOURCES", "text": "Sjekk kilden og faktasjekk før du gjør noe" },
                        { "id": "ASK_ADULT", "text": "Spør en voksen" }
                      ],
                      "explanation": "Kapslås, utropstegn, anonym kilde og oppfordring til hastedeling er alle tegn på manipulerende innhold."
                    }
                    """,
                """
                    { "action": "CHECK_SOURCES" }
                    """
            ),
            socialMediaTask(
                socialStop,
                2,
                "Følelser på sosiale medier",
                "Identifiser hvilke følelser innlegget prøver å skape.",
                """
                    {
                      "type": "CHOOSE_ACTION",
                      "post": {
                        "username": "Bekymret Borger",
                        "handle": "@bekymret_borger_99",
                        "avatar": "😤",
                        "content": "Politiet gjør INGENTING. Byen vår er UTRYGG. Del dette til ALLE du kjenner så vi kan stoppe dette galskapet en gang for alle!!!",
                        "likes": 9432,
                        "comments": 2109,
                        "timestamp": "1 time siden",
                        "verified": false
                      },
                      "question": "Hva bør du gjøre?",
                      "options": [
                        { "id": "SHARE", "text": "Del med en gang - dette er viktig!" },
                        { "id": "CHECK_SOURCES", "text": "Sjekk om det er sant før du deler" },
                        { "id": "IGNORE", "text": "Ignorer innlegget" },
                        { "id": "ASK_ADULT", "text": "Spør en voksen om råd" }
                      ],
                      "explanation": "Innlegget bruker sinne, kapslås og gruppepress for å få deg til å dele raskt uten å tenke."
                    }
                    """,
                """
                    { "action": "CHECK_SOURCES" }
                    """
            ),
            socialMediaTask(
                socialStop,
                3,
                "Finn det mest illegitime innlegget",
                "Velg innlegget med minst troverdighet.",
                """
                    {
                      "type": "IDENTIFY_WORST",
                      "question": "Hvilket innlegg er mest illegitimt?",
                      "posts": [
                        {
                          "id": "post_0",
                          "username": "Ordførerens kontor",
                          "handle": "@ordforer_trondheim",
                          "avatar": "🏛️",
                          "content": "Kommunen jobber aktivt med saken. Vi informerer fortløpende på kommunens offisielle nettside.",
                          "likes": 312,
                          "comments": 44,
                          "timestamp": "1 time siden",
                          "verified": true
                        },
                        {
                          "id": "post_1",
                          "username": "SannhetsJegeren99",
                          "handle": "@sannhet99",
                          "avatar": "👁️",
                          "content": "JEG VET HVEM TYVEN ER!! Myndighetene prøver å dekke over sannheten!! Del dette til ALLE du kjenner FØR de sletter det 🔥🔥🔥",
                          "likes": 18432,
                          "comments": 2341,
                          "timestamp": "45 min siden",
                          "verified": false
                        },
                        {
                          "id": "post_2",
                          "username": "Lokal Reporter",
                          "handle": "@lokal_reporter",
                          "avatar": "📝",
                          "content": "Politiet bekrefter at etterforskningen pågår. Ingen mistenkte er offentlig navngitt ennå.",
                          "likes": 891,
                          "comments": 123,
                          "timestamp": "2 timer siden",
                          "verified": false
                        }
                      ],
                      "explanation": "Innlegg 2 (SannhetsJegeren99) bruker kapslås, udokumenterte påstander, konspirasjonsspråk og oppfordrer til hastedeling - klassiske tegn på manipulerende innhold."
                    }
                    """,
                """
                    { "selected": "post_1" }
                    """
            ),
            finalBossTask(finalBossStop)
        ));

        medalRepository.saveAll(List.of(
            medal("Nyhetsjeger", "Fullfør Nyhetskvartalet.", stops.get(0)),
            medal("E-postetterforsker", "Fullfør Postkontoret.", stops.get(1)),
            medal("Bildegransker", "Fullfør Fotografen.", stops.get(2)),
            medal("Passordvokter", "Fullfør Passordbanken.", stops.get(3)),
            medal("Trygg handler", "Fullfør Markedsplassen.", stops.get(4)),
            medal("Sosial speider", "Fullfør Den sosiale møteplassen.", stops.get(5)),
            medal("Datasenterhelt", "Fullfør Datasenteret.", stops.get(6))
        ));
    }

    private void syncExistingMysteryContent() {
        List<Stop> existingStops = stopRepository.findAllByOrderByOrderIndexAsc();
        Map<String, Integer> originalOrder = Map.of(
            "Nyhetskvartalet", 1,
            "Fotografen", 2,
            "Postkontoret", 3,
            "Markedsplassen", 4,
            "Den sosiale møteplassen", 5,
            "Passordbanken", 6,
            "Datasenteret", 7
        );

        boolean changedOrder = false;
        for (Stop stop : existingStops) {
            Integer expectedOrder = originalOrder.get(stop.getName());
            if (expectedOrder != null && !expectedOrder.equals(stop.getOrderIndex())) {
                stop.setOrderIndex(expectedOrder);
                changedOrder = true;
            }
        }
        if (changedOrder) {
            stopRepository.saveAll(existingStops);
        }

        existingStops.stream()
            .filter(stop -> "Datasenteret".equals(stop.getName()))
            .findFirst()
            .filter(stop -> taskRepository.countByStop_Id(stop.getId()) == 0)
            .ifPresent(stop -> taskRepository.save(finalBossTask(stop)));
    }

    private Stop stop(String name, String description, String theme, int orderIndex, boolean finalBoss, String autoTip) {
        Stop stop = new Stop();
        stop.setName(name);
        stop.setDescription(description);
        stop.setTheme(theme);
        stop.setOrderIndex(orderIndex);
        stop.setFinalBoss(finalBoss);
        stop.setAutoTip(autoTip);
        return stop;
    }

    private Task fakeNewsTask(Stop stop, int orderIndex, String title, String description, String contentJson) {
        Task task = baseTask(stop, orderIndex, title, description, TaskType.FAKE_NEWS);
        task.setContentJson(contentJson);
        task.setCorrectAnswerJson("""
            {
              "article_0": true,
              "article_1": false
            }
            """);
        task.setGuidanceText("Les overskrift, kilde og detaljer før du bestemmer hvilke artikler som er ekte.");
        return task;
    }

    private Task phishingTask(
        Stop stop,
        int orderIndex,
        String title,
        String description,
        String fromName,
        String fromEmail,
        String subject,
        String body,
        List<String> suspiciousElements,
        String explanation
    ) {
        Task task = baseTask(stop, orderIndex, title, description, TaskType.PHISHING_EMAIL);
        task.setContentJson(phishingContentJson(
            fromName,
            fromEmail,
            subject,
            body,
            suspiciousElements,
            explanation
        ));
        task.setCorrectAnswerJson("""
            {
              "action": "REPORT"
            }
            """);
        task.setGuidanceText("Se nøye på avsender, lenker, hastverk og hva e-posten ber deg gjøre.");
        return task;
    }

    private Task aiPhotoTask(
        Stop stop,
        int orderIndex,
        String title,
        String description,
        String contentJson,
        String correctAnswerJson
    ) {
        Task task = baseTask(stop, orderIndex, title, description, TaskType.AI_PHOTO);
        task.setContentJson(contentJson);
        task.setCorrectAnswerJson(correctAnswerJson);
        task.setGuidanceText("Se nøye på detaljene i hvert bilde: hender, bakgrunn, lys og skygger.");
        return task;
    }

    private Task marketplaceTask(
        Stop stop,
        int orderIndex,
        String title,
        String description,
        String contentJson,
        String correctAnswerJson
    ) {
        Task task = baseTask(stop, orderIndex, title, description, TaskType.MARKETPLACE);
        task.setContentJson(contentJson);
        task.setCorrectAnswerJson(correctAnswerJson);
        task.setGuidanceText("Sjekk URL, priser, kontaktinfo og betalingsvalg nøye.");
        return task;
    }

    private Task socialMediaTask(
        Stop stop,
        int orderIndex,
        String title,
        String description,
        String contentJson,
        String correctAnswerJson
    ) {
        Task task = baseTask(stop, orderIndex, title, description, TaskType.SOCIAL_MEDIA);
        task.setContentJson(contentJson);
        task.setCorrectAnswerJson(correctAnswerJson);
        task.setGuidanceText("Les innlegget nøye. Tenk over hvilke følelser det prøver å skape.");
        return task;
    }

    private Task passwordTask(
        Stop stop,
        int orderIndex,
        String title,
        String description,
        String contentJson,
        String correctAnswerJson
    ) {
        Task task = baseTask(stop, orderIndex, title, description, TaskType.PASSWORD);
        task.setContentJson(contentJson);
        task.setCorrectAnswerJson(correctAnswerJson);
        task.setGuidanceText("Tenk på lengde, variasjon og om passordet inneholder personlig informasjon.");
        return task;
    }

    private Task finalBossTask(Stop stop) {
        Task task = baseTask(
            stop,
            1,
            "Hvem tok pengene?",
            "Bruk det du har lært til å lese spor, sjekke vitner og velge riktig mistenkt.",
            TaskType.FINAL_BOSS
        );
        task.setContentJson("""
            {
              "intro": "Ordførerens idrettspark-penger er stjålet. Les sporene og vitneutsagnene. Hvem passer med alle bevisene?",
              "clues": [
                {
                  "stop": "Nyhetskvartalet",
                  "lesson": "Ikke heng deg opp i det mest synlige.",
                  "clue": "Tyven ble sett nær Bytorget med mørk jakke, noe rødt på hodet, lyse sko og en stor bærepose med bibliotekslogo."
                },
                {
                  "stop": "Fotografen",
                  "lesson": "Bilder kan gi tidslinje.",
                  "clue": "Overvåkningsbildet viser en person som går inn i Internettbyens Bibliotek kl. 20:47, etter stengetid."
                },
                {
                  "stop": "Postkontoret",
                  "lesson": "Phishing bruker personlig informasjon.",
                  "clue": "E-posten inneholdt info om en bok ordføreren lånte for 3 år siden. Den historikken ligger i bibliotekets låneregister."
                },
                {
                  "stop": "Markedsplassen",
                  "lesson": "Nettbutikker legger igjen tekniske spor.",
                  "clue": "Den falske nettbutikken ble registrert fra bibliotekets IP kl. 21:14, fra ansatt-PC-en."
                },
                {
                  "stop": "Den sosiale møteplassen",
                  "lesson": "Falske kontoer kan røpe vaner.",
                  "clue": "Kontoen bokelskeren_99 fulgte bare biblioteks-sider og ble opprettet med bibliotekets abonnements-epost."
                },
                {
                  "stop": "Passordbanken",
                  "lesson": "Passord kan peke på hvem som lagde kontoen.",
                  "clue": "Tyvekontoen brukte passordet BibliotekAdmin2019, satt av den ansatte som var med på IT-oppgraderingen i 2019."
                }
              ],
              "motive": "Idrettsparkens budsjett ville stenge biblioteket. Millie ville flytte pengene anonymt til bibliotekets fond. Hun trodde hun reddet kunnskap, men valgte en ulovlig vei.",
              "explanation": "Millie Mus er den eneste som passer med alle sporene: bibliotekspose, adgang etter stengetid, låneregister, ansatt-PC, bibliotek-epost og passordet fra IT-oppgraderingen."
            }
            """);
        task.setCorrectAnswerJson("""
            {
              "culprit": "millie-mus"
            }
            """);
        task.setGuidanceText("Les ett spor om gangen. Kryss ut mistenkte som ikke passer med bevisene.");
        return task;
    }

    private String phishingContentJson(
        String fromName,
        String fromEmail,
        String subject,
        String body,
        List<String> suspiciousElements,
        String explanation
    ) {
        ObjectNode email = objectMapper.createObjectNode();
        email.put("fromName", fromName);
        email.put("fromEmail", fromEmail);
        email.put("subject", subject);
        email.put("body", body);
        email.set("suspiciousElements", objectMapper.valueToTree(suspiciousElements));
        email.put("correctAction", "REPORT");

        ObjectNode root = objectMapper.createObjectNode();
        root.set("email", email);
        root.put("explanation", explanation);

        try {
            return objectMapper.writeValueAsString(root);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to build phishing task seed content", exception);
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
}
