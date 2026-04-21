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
                 "Avslør hvilke nyheter som er ekte og hvilke som prøver å lure deg.",
                 "FAKE_NEWS", 1, false,
                 "Falske nyheter bruker gjerne skremmende overskrifter og anonyme kilder. Sjekk alltid hvem som har skrevet saken: er nettadressen til et kjent mediehus? Søk opp saken på andre seriøse nettsteder for å se om historien stemmer. Overdrevne påstander uten dokumentasjon er et varseltegn."),
            stop("Postkontoret",
                 "Undersøk e-poster og lær hvordan svindelforsøk kan se ut.",
                 "PHISHING_EMAIL", 2, false,
                 "Phishing-e-poster later som de er fra banker, skoler eller kjente selskaper for å lure deg til å gi fra deg passord eller penger. Se etter skrivefeil, ukjente avsenderadresser og lenker der nettadressen ikke stemmer med avsenderen. En ekte avsender ber aldri om passord eller betalingsinformasjon via e-post."),
            stop("Fotografen",
                 "Se etter spor i bilder og lær å kjenne igjen manipulasjon.",
                 "AI_PHOTO", 3, false,
                 "Bilder kan manipuleres og AI kan lage realistiske falske bilder. Se etter unaturlige detaljer: rare fingre, jevne bakgrunner og uskarp tekst er vanlige feil. Du kan bruke omvendt bildesøk til å sjekke om et bilde er tatt ut av en helt annen sammenheng enn det påstår."),
            stop("Passordbanken",
                 "Bygg sterke passord og beskytt kontoene dine.",
                 "PASSWORD", 4, false,
                 "Et sterkt passord er langt, tilfeldig og unikt for hver konto du bruker. En rekke tilfeldige ord er lettere å huske og vanskeligere å knekke enn korte passord med spesialtegn. Del aldri passordet ditt med andre, og bruk aldri samme passord på flere nettsteder."),
            stop("Markedsplassen",
                 "Vurder annonser, betalinger og trygg handel på nett.",
                 "MARKETPLACE", 5, false,
                 "Svindel på nett bruker priser som er for gode til å være sanne, krever betaling på forhånd og har vage eller kopierte produktbeskrivelser. Sjekk alltid selgerprofilen og les tilbakemeldinger fra andre kjøpere. Betal aldri med gavekort eller kryptovaluta - det er nesten umulig å spore."),
            stop("Den sosiale møteplassen",
                 "Ta gode valg i meldinger, kommentarer og deling.",
                 "SOCIAL_MEDIA", 6, false,
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
                          "alt": "En person sitter pÃ¥ en benk i en park. Fingrene ser litt rare ut.",
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
                          "alt": "En person pÃ¥ et torg. Bakgrunnen gjentar seg tydelig.",
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
                          "alt": "Bilde der teksten pÃ¥ skiltene i bakgrunnen er uskarp og uleselig.",
                          "label": "Bilde B"
                        },
                        {
                          "id": "image_2",
                          "src": "",
                          "alt": "Et klart mobilbilde. Alle detaljer ser naturlige ut.",
                          "label": "Bilde C"
                        }
                      ],
                      "question": "Hvilket bilde kan vi stole pÃ¥ som ekte bevis?"
                    }
                    """,
                """
                    { "image_0": "MANIPULATED", "image_1": "AI_GENERATED", "image_2": "REAL" }
                    """
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
                      "explanation": "F!sk3Taco#92 er sterkest fordi det er langt og blander store og smÃ¥ bokstaver, tall og spesialtegn. Navn og Ã¥rstall er svake."
                    }
                    """,
                """
                    { "selected": "d" }
                    """
            ),
            passwordTask(
                passwordStop,
                2,
                "GjÃ¸r passordet bedre",
                "Velg det passordet som er best forbedret.",
                """
                    {
                      "type": "CHOICE",
                      "question": "Noen har prÃ¸vd Ã¥ gjÃ¸re passordet 'Sander2015' sterkere. Hvilken versjon er best?",
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
                "Bruk brikkene til Ã¥ lage et passord som er sterkt nok.",
                """
                    {
                      "type": "BUILDER",
                      "question": "Bygg et passord som er sterkt nok til Ã¥ lÃ¥se opp bankboksen",
                      "words": ["Tiger", "MÃ¥ne", "Pizza", "Hund", "Sol", "IsbjÃ¸rn", "Fjord"],
                      "symbols": ["!", "#", "@", "?", "&", "*"],
                      "numbers": ["7", "42", "99", "3", "2026"],
                      "minStrength": "STRONG",
                      "explanation": "Et sterkt passord er langt, bruker store og smÃ¥ bokstaver, tall og spesialtegn, og inneholder ikke personlig informasjon."
                    }
                    """,
                """
                    { "minStrength": "STRONG" }
                    """
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
                        "avatar": "ðŸ“°",
                        "content": "DELE DETTE NÃ…!!! OrdfÃ¸rerens pengeskandal er MYE VERRE enn noen tror ðŸ˜±ðŸ˜±ðŸ˜± Anonym kilde avslÃ¸rer det ingen tÃ¸r si hÃ¸yt!!!",
                        "likes": 2847,
                        "comments": 431,
                        "timestamp": "3 timer siden",
                        "verified": false
                      },
                      "question": "Hva bÃ¸r du gjÃ¸re med dette innlegget?",
                      "options": [
                        { "id": "SHARE", "text": "Del det videre med en gang" },
                        { "id": "WAIT", "text": "Vent og se om det dukker opp andre steder" },
                        { "id": "CHECK_SOURCES", "text": "Sjekk kilden og faktasjekk fÃ¸r du gjÃ¸r noe" },
                        { "id": "ASK_ADULT", "text": "SpÃ¸r en voksen" }
                      ],
                      "explanation": "KapslÃ¥s, utropstegn, anonym kilde og oppfordring til hastedeling er alle tegn pÃ¥ manipulerende innhold."
                    }
                    """,
                """
                    { "action": "CHECK_SOURCES" }
                    """
            ),
            socialMediaTask(
                socialStop,
                2,
                "FÃ¸lelser pÃ¥ sosiale medier",
                "Identifiser hvilke fÃ¸lelser innlegget prÃ¸ver Ã¥ skape.",
                """
                    {
                      "type": "CHOOSE_ACTION",
                      "post": {
                        "username": "Bekymret Borger",
                        "handle": "@bekymret_borger_99",
                        "avatar": "ðŸ˜¤",
                        "content": "Politiet gjÃ¸r INGENTING. Byen vÃ¥r er UTRYGG. Del dette til ALLE du kjenner sÃ¥ vi kan stoppe dette galskapet en gang for alle!!!",
                        "likes": 9432,
                        "comments": 2109,
                        "timestamp": "1 time siden",
                        "verified": false
                      },
                      "question": "Hva bÃ¸r du gjÃ¸re?",
                      "options": [
                        { "id": "SHARE", "text": "Del med en gang â€” dette er viktig!" },
                        { "id": "CHECK_SOURCES", "text": "Sjekk om det er sant fÃ¸r du deler" },
                        { "id": "IGNORE", "text": "Ignorer innlegget" },
                        { "id": "ASK_ADULT", "text": "SpÃ¸r en voksen om rÃ¥d" }
                      ],
                      "explanation": "Innlegget bruker sinne, kapslÃ¥s og gruppepress for Ã¥ fÃ¥ deg til Ã¥ dele raskt uten Ã¥ tenke."
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
                          "username": "OrdfÃ¸rerens kontor",
                          "handle": "@ordforer_trondheim",
                          "avatar": "ðŸ›ï¸",
                          "content": "Kommunen jobber aktivt med saken. Vi informerer fortlÃ¸pende pÃ¥ kommunens offisielle nettside.",
                          "likes": 312,
                          "comments": 44,
                          "timestamp": "1 time siden",
                          "verified": true
                        },
                        {
                          "id": "post_1",
                          "username": "SannhetsJegeren99",
                          "handle": "@sannhet99",
                          "avatar": "ðŸ‘ï¸",
                          "content": "JEG VET HVEM TYVEN ER!! Myndighetene prÃ¸ver Ã¥ dekke over sannheten!! Del dette til ALLE du kjenner FÃ˜R de sletter det ðŸ”¥ðŸ”¥ðŸ”¥",
                          "likes": 18432,
                          "comments": 2341,
                          "timestamp": "45 min siden",
                          "verified": false
                        },
                        {
                          "id": "post_2",
                          "username": "Lokal Reporter",
                          "handle": "@lokal_reporter",
                          "avatar": "ðŸ“",
                          "content": "Politiet bekrefter at etterforskningen pÃ¥gÃ¥r. Ingen mistenkte er offentlig navngitt ennÃ¥.",
                          "likes": 891,
                          "comments": 123,
                          "timestamp": "2 timer siden",
                          "verified": false
                        }
                      ],
                      "explanation": "Innlegg 2 (SannhetsJegeren99) bruker kapslÃ¥s, udokumenterte pÃ¥stander, konspirasjonssprÃ¥k og oppfordrer til hastedeling â€” klassiske tegn pÃ¥ manipulerende innhold."
                    }
                    """,
                """
                    { "selected": "post_1" }
                    """
            )
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
        task.setGuidanceText("Se nÃ¸ye pÃ¥ detaljene i hvert bilde: hender, bakgrunn, lys og skygger.");
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
        task.setGuidanceText("Les innlegget nÃ¸ye. Tenk over hvilke fÃ¸lelser det prÃ¸ver Ã¥ skape.");
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
        task.setGuidanceText("Tenk pÃ¥ lengde, variasjon og om passordet inneholder personlig informasjon.");
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
