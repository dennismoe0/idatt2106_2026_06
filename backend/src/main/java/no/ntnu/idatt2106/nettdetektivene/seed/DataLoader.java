package no.ntnu.idatt2106.nettdetektivene.seed;

import com.fasterxml.jackson.core.JsonProcessingException;
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
                 "Avslør hvilke nyheter som er ekte og hvilke som prøver å lure deg.",
                 "FAKE_NEWS", 1, false,
                 "Falske nyheter bruker gjerne skremmende overskrifter og anonyme kilder. Sjekk alltid hvem som har skrevet saken: er nettadressen til et kjent mediehus? Søk opp saken på andre seriøse nettsteder for å se om historien stemmer. Overdrevne påstander uten dokumentasjon er et varseltegn.",
                 "Tyven hadde på seg en mørk jakke, rød hette og lyse sko."),
            stop("Postkontoret",
                 "Undersøk e-poster og lær hvordan svindelforsøk kan se ut.",
                 "PHISHING_EMAIL", 2, false,
                 "Phishing-e-poster later som de er fra banker, skoler eller kjente selskaper for å lure deg til å gi fra deg passord eller penger. Se etter skrivefeil, ukjente avsenderadresser og lenker der nettadressen ikke stemmer med avsenderen. En ekte avsender ber aldri om passord eller betalingsinformasjon via e-post.",
                 "Ordføreren mottok en farlig e-post fra adressen hjelp@by-service.net."),
            stop("Fotografen",
                 "Se etter spor i bilder og lær å kjenne igjen manipulasjon.",
                 "AI_PHOTO", 3, false,
                 "Bilder kan manipuleres og AI kan lage realistiske falske bilder. Se etter unaturlige detaljer: rare fingre, jevne bakgrunner og uskarp tekst er vanlige feil. Du kan bruke omvendt bildesøk til å sjekke om et bilde er tatt ut av en helt annen sammenheng enn det påstår.",
                 "Et ekte bilde viser tyven med en konvolutt utenfor en nettkafé i Bytorget."),
            stop("Passordbanken",
                 "Bygg sterke passord og beskytt kontoene dine.",
                 "PASSWORD", 4, false,
                 "Et sterkt passord er langt, tilfeldig og unikt for hver konto du bruker. En rekke tilfeldige ord er lettere å huske og vanskeligere å knekke enn korte passord med spesialtegn. Del aldri passordet ditt med andre, og bruk aldri samme passord på flere nettsteder.",
                 null),
            stop("Markedsplassen",
                 "Vurder annonser, betalinger og trygg handel på nett.",
                 "MARKETPLACE", 5, false,
                 "Svindel på nett bruker priser som er for gode til å være sanne, krever betaling på forhånd og har vage eller kopierte produktbeskrivelser. Sjekk alltid selgerprofilen og les tilbakemeldinger fra andre kjøpere. Betal aldri med gavekort eller kryptovaluta — det er nesten umulig å spore.",
                 "Svindelbutikken «best-deals-city.xyz» var registrert på en adresse ved Bytorget."),
            stop("Den sosiale møteplassen",
                 "Ta gode valg i meldinger, kommentarer og deling.",
                 "SOCIAL_MEDIA", 6, false,
                 "Sosiale medier viser deg mest det du allerede er enig i, noe som kan gjøre det vanskelig å se helhetsbildet. Fremmede som tar kontakt og raskt ber om personlig informasjon kan ha skjulte hensikter. Del aldri telefonnummer, adresse, passord eller bilder du ikke vil at alle skal se.",
                 "En falsk konto på Fjesbok.no ble opprettet fra nettkafeen på Bytorget."),
            stop("Datasenteret",
                 "Bruk alt du har lært i den siste digitale saken.",
                 "FINAL_BOSS", 7, true,
                 "Du har nå lært de viktigste detektivferdighetene: gjenkjenne falske nyheter, phishing-e-poster, manipulerte bilder, svake passord, nettsvindel og sosiale medier-feller. Den viktigste regelen er å stoppe og tenke én ekstra gang før du klikker, deler eller svarer på noe du er usikker på.",
                 null)
        ));

        Stop newsStop   = stops.get(0);
        Stop mailStop   = stops.get(1);
        Stop photoStop  = stops.get(2);
        Stop pwdStop    = stops.get(3);
        Stop marketStop = stops.get(4);
        Stop socialStop = stops.get(5);

        List<Task> tasks = new ArrayList<>();
        tasks.addAll(List.of(
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
                "Klikk på alle mistenkelige deler av e-posten.",
                "DNB Kundeservice",
                "support@dnb-kundeservice.com",
                "Viktig: Bekreft kontoen din",
                "Kjære kunde, kontoen din blir stengt om 2 timer. Klikk HER umiddelbart og bekreft BankID-informasjonen din.",
                List.of(
                    new Clue("sender", "sender", "support@dnb-kundeservice.com", true, "Avsenderdomenet er ikke dnb.no — det er et falskt domene."),
                    new Clue("link1", "link", "Klikk HER", true, "Lenker som ikke viser URL er et klassisk phishing-triks."),
                    new Clue("urgency", "text", "umiddelbart", true, "Hastverk brukes for å hindre deg i å tenke deg om."),
                    new Clue("brand", "text", "DNB Kundeservice", false, null)
                ),
                "Avsenderadressen er ikke dnb.no, meldingen haster kunstig og ber deg klikke på en mistenkelig lenke."
            ),
            phishingTask(
                mailStop,
                2,
                "Pakkemelding",
                "Klikk på alle mistenkelige deler av e-posten.",
                "Posten Norge",
                "pakke@posten-levering.net",
                "Pakken din mangler porto",
                "Hei! Du skylder 19 kroner i porto. Betal innen i kveld for å unngå at pakken returneres. Betal her.",
                List.of(
                    new Clue("sender", "sender", "pakke@posten-levering.net", true, "Avsenderadressen ligner Posten, men er ikke offisiell (posten.no)."),
                    new Clue("link1", "link", "Betal her", true, "Lenker til betalingssider fra ukjente avsendere bør aldri klikkes."),
                    new Clue("urgency", "text", "innen i kveld", true, "Hastverk brukes for å stresse deg til å handle uten å tenke."),
                    new Clue("brand", "text", "Posten Norge", false, null)
                ),
                "Avsenderadressen ligner på Posten, men er ikke offisiell. Små gebyrer og hastverk brukes ofte i svindel."
            ),
            phishingTask(
                mailStop,
                3,
                "Skolekonto",
                "Klikk på alle mistenkelige deler av e-posten.",
                "IT-avdelingen",
                "it-hjelp@skole-login.com",
                "Passordet ditt utløper i dag",
                "Logg inn med skolebrukeren din på lenken under for å beholde tilgang til Teams og e-post. Logg inn her.",
                List.of(
                    new Clue("sender", "sender", "it-hjelp@skole-login.com", true, "Skole-IT bruker skolens eget domene — ikke skole-login.com."),
                    new Clue("link1", "link", "Logg inn her", true, "En lenke fra ukjent domene kan stjele innloggingsinformasjonen din."),
                    new Clue("brand", "text", "IT-avdelingen", false, null)
                ),
                "E-posten ber om innlogging via et ukjent domene. IT-meldinger bør sjekkes mot skolens offisielle kanaler."
            ),
            finalBossTask(stops.get(6))
        ));
        tasks.addAll(List.of(
            aiPhotoTask(photoStop, 1, "Parkbilder", "Er bildet ekte, KI-generert eller manipulert?",
                """
                {
                  "images": [
                    { "id": "image_0", "src": "", "alt": "En person sitter på en benk i en park. Fingrene ser litt rare ut.", "label": "Bilde A" },
                    { "id": "image_1", "src": "", "alt": "Utsikt over en by tatt fra et vindu. Normalt mobilbilde.", "label": "Bilde B" }
                  ],
                  "question": "Sorter hvert bilde: er det ekte, KI-generert eller manipulert?"
                }
                """,
                "{\"image_0\": \"AI_GENERATED\", \"image_1\": \"REAL\"}"),
            aiPhotoTask(photoStop, 2, "Bytorget", "Finn hvilket bilde som er ekte og kan brukes som bevis.",
                """
                {
                  "images": [
                    { "id": "image_0", "src": "", "alt": "En person på et torg. Bakgrunnen gjentar seg tydelig.", "label": "Bilde A" },
                    { "id": "image_1", "src": "", "alt": "Et mobilbilde av samme torg. Normalt lys og naturlig bakgrunn.", "label": "Bilde B" },
                    { "id": "image_2", "src": "", "alt": "Et bilde der ansiktet er urealistisk glatt og jevnt.", "label": "Bilde C" }
                  ],
                  "question": "Sorter hvert bilde: ekte, KI-generert eller manipulert?"
                }
                """,
                "{\"image_0\": \"AI_GENERATED\", \"image_1\": \"REAL\", \"image_2\": \"MANIPULATED\"}"),
            aiPhotoTask(photoStop, 3, "Bevisbildet", "Kun ett bilde kan brukes som ekte bevis. Finn det.",
                """
                {
                  "images": [
                    { "id": "image_0", "src": "", "alt": "Bilde med uvanlige skygger som ikke stemmer med lyskilden.", "label": "Bilde A" },
                    { "id": "image_1", "src": "", "alt": "Bilde der teksten på skiltene i bakgrunnen er uskarp og uleselig.", "label": "Bilde B" },
                    { "id": "image_2", "src": "", "alt": "Et klart mobilbilde. Alle detaljer ser naturlige ut.", "label": "Bilde C" }
                  ],
                  "question": "Hvilket bilde kan vi stole på som ekte bevis?"
                }
                """,
                "{\"image_0\": \"MANIPULATED\", \"image_1\": \"AI_GENERATED\", \"image_2\": \"REAL\"}"),
            passwordTask(pwdStop, 1, "Velg det tryggeste passordet", "Finn ut hvilket passord som er best.",
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
            passwordTask(pwdStop, 2, "Gjør passordet bedre", "Velg det passordet som er best forbedret.",
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
            passwordTask(pwdStop, 3, "Bygg et sterkt passord", "Bruk brikkene til å lage et passord som er sterkt nok.",
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
                "{\"minStrength\": \"STRONG\"}"),
            marketplaceTask(marketStop, 1, "Sneaker-blitz.shop", "Finn det tydeligste faresignalet i nettbutikken.",
                """
                {
                  "type": "IDENTIFY",
                  "siteName": "sneaker-blitz.shop",
                  "question": "Hva er det tydeligste faresignalet her?",
                  "options": [
                    { "id": "cheap", "text": "Prisen er altfor lav sammenlignet med vanlige butikker" },
                    { "id": "colors", "text": "Butikken bruker sterke farger og store overskrifter" },
                    { "id": "shipping", "text": "Nettsiden lover rask levering" }
                  ],
                  "mockup": {
                    "eyebrow": "Kun i dag",
                    "headline": "Eksklusive sneakers til 79 kr",
                    "tagline": "90 % rabatt og bare noen få minutter igjen.",
                    "productName": "Street Runner X",
                    "price": "79 kr",
                    "originalPrice": "1 499 kr",
                    "ctaText": "Kjøp nå",
                    "badges": ["90 % rabatt", "Begrenset antall"],
                    "notice": "Betal raskt for å sikre varen din."
                  },
                  "explanation": "Ekstreme rabatter og kunstig hastverk er vanlige faresignaler i nettsvindel."
                }
                """,
                "{\"selected\": \"cheap\"}",
                "Se etter priser, betaling og hastverk før du handler."),
            marketplaceTask(marketStop, 2, "tech-deals-market.net", "Velg det mest mistenkelige tegnet før du betaler.",
                """
                {
                  "type": "IDENTIFY",
                  "siteName": "tech-deals-market.net",
                  "question": "Hva bør gjøre deg mest skeptisk?",
                  "options": [
                    { "id": "giftcard", "text": "Butikken vil bare ha betaling med gavekort eller krypto" },
                    { "id": "sale", "text": "Det står at det er sommersalg" },
                    { "id": "rating", "text": "Produktet har mange stjerner" }
                  ],
                  "mockup": {
                    "eyebrow": "Ekspresssalg",
                    "headline": "Spillkonsoll til halv pris",
                    "tagline": "Kun alternative betalingsmåter godtas.",
                    "productName": "PlayBox Ultra",
                    "price": "2 199 kr",
                    "originalPrice": "4 399 kr",
                    "ctaText": "Betal nå",
                    "badges": ["Kun gavekort", "Ingen refusjon"],
                    "notice": "Kortbetaling er midlertidig utilgjengelig."
                  },
                  "explanation": "Betaling med gavekort eller krypto er vanskelig å spore og brukes ofte i svindel."
                }
                """,
                "{\"selected\": \"giftcard\"}",
                "Velg det mest mistenkelige tegnet før du betaler."),
            marketplaceTask(marketStop, 3, "Hvilken butikk virker falsk?", "Sammenlign fire butikker og velg den mest mistenkelige.",
                """
                {
                  "type": "RANK",
                  "question": "Hvilken nettbutikk virker mest sannsynlig å være svindel?",
                  "sites": [
                    {
                      "id": "site-a",
                      "name": "friluftshuset.no",
                      "badge": "Kort og Klarna",
                      "description": "Tydelig returinfo og organisasjonsnummer."
                    },
                    {
                      "id": "site-b",
                      "name": "merkevarer-outlet-fast.com",
                      "badge": "Kun forskuddsbetaling",
                      "description": "Ekstreme rabatter, mangler kontaktinfo og presser deg til å betale raskt."
                    },
                    {
                      "id": "site-c",
                      "name": "spillsonen.no",
                      "badge": "Kundeservice",
                      "description": "Viser åpningstider, adresse og vanlige betalingsvalg."
                    },
                    {
                      "id": "site-d",
                      "name": "bokbyen.no",
                      "badge": "Trygg betaling",
                      "description": "Har anmeldelser, leveringsvilkår og kjent domene."
                    }
                  ],
                  "explanation": "Nettbutikken med ekstreme rabatter, dårlig kontaktinfo og forskuddsbetaling er den mest mistenkelige."
                }
                """,
                "{\"selected\": \"site-b\"}",
                "Velg nettstedet du ville styrt unna."),
            socialMediaTask(socialStop, 1, "Del eller vent?", "Velg riktig handling.",
                """
                {
                  "type": "CHOOSE_ACTION",
                  "post": {
                    "username": "TrondheimNytt", "handle": "@trondheim_nytt", "avatar": "📰",
                    "content": "DELE DETTE NÅ!!! Ordførerens pengeskandal er MYE VERRE enn noen tror 😱😱😱 Anonym kilde avslører det ingen tør si høyt!!!",
                    "likes": 2847, "comments": 431, "timestamp": "3 timer siden", "verified": false
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
                "{\"action\": \"CHECK_SOURCES\"}"),
            socialMediaTask(socialStop, 2, "Følelser på sosiale medier", "Identifiser hvilke følelser innlegget prøver å skape.",
                """
                {
                  "type": "CHOOSE_ACTION",
                  "post": {
                    "username": "Bekymret Borger", "handle": "@bekymret_borger_99", "avatar": "😤",
                    "content": "Politiet gjør INGENTING. Byen vår er UTRYGG. Del dette til ALLE du kjenner så vi kan stoppe dette galskapet en gang for alle!!!",
                    "likes": 9432, "comments": 2109, "timestamp": "1 time siden", "verified": false
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
                "{\"action\": \"CHECK_SOURCES\"}"),
            socialMediaTask(socialStop, 3, "Finn det mest illegitime innlegget", "Velg innlegget med minst troverdighet.",
                """
                {
                  "type": "IDENTIFY_WORST",
                  "question": "Hvilket innlegg er mest illegitimt?",
                  "posts": [
                    { "id": "post_0", "username": "Ordførerens kontor", "handle": "@ordforer_trondheim", "avatar": "🏛️",
                      "content": "Kommunen jobber aktivt med saken. Vi informerer fortløpende på kommunens offisielle nettside.",
                      "likes": 312, "comments": 44, "timestamp": "1 time siden", "verified": true },
                    { "id": "post_1", "username": "SannhetsJegeren99", "handle": "@sannhet99", "avatar": "👁️",
                      "content": "JEG VET HVEM TYVEN ER!! Myndighetene prøver å dekke over sannheten!! Del dette til ALLE du kjenner FØR de sletter det 🔥🔥🔥",
                      "likes": 18432, "comments": 2341, "timestamp": "45 min siden", "verified": false },
                    { "id": "post_2", "username": "Lokal Reporter", "handle": "@lokal_reporter", "avatar": "📝",
                      "content": "Politiet bekrefter at etterforskningen pågår. Ingen mistenkte er offentlig navngitt ennå.",
                      "likes": 891, "comments": 123, "timestamp": "2 timer siden", "verified": false }
                  ],
                  "explanation": "Innlegg 2 (SannhetsJegeren99) bruker kapslås, udokumenterte påstander, konspirasjonsspråk og oppfordrer til hastedeling."
                }
                """,
                "{\"selected\": \"post_1\"}")
        ));
        taskRepository.saveAll(tasks);

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
        task.setCorrectAnswerJson("""
            {
              "article_0": true,
              "article_1": false
            }
            """);
        task.setGuidanceText("Les overskrift, kilde og detaljer før du bestemmer hvilke artikler som er ekte.");
        return task;
    }

    record Clue(String id, String type, String label, boolean isClue, String explanation) {}

    private Task phishingTask(
        Stop stop,
        int orderIndex,
        String title,
        String description,
        String fromName,
        String fromEmail,
        String subject,
        String body,
        List<Clue> clues,
        String explanation
    ) {
        Task task = baseTask(stop, orderIndex, title, description, TaskType.PHISHING_EMAIL);
        task.setContentJson(phishingContentJson(fromName, fromEmail, subject, body, clues, explanation));
        List<String> requiredClueIds = clues.stream()
            .filter(Clue::isClue)
            .map(Clue::id)
            .toList();
        try {
            task.setCorrectAnswerJson(objectMapper.writeValueAsString(
                objectMapper.createObjectNode().set("clues", objectMapper.valueToTree(requiredClueIds))
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
        task.setGuidanceText("Les innlegget nøye. Tenk over hvilke følelser det prøver å skape.");
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
              "articles": [
                { "headline": "Pengene er funnet i utlandet", "source": "NRK.no", "body": "Politiet bekrefter at etterforskerne har sporet transaksjonen." },
                { "headline": "AVSLØRT: Ordføreren stjal pengene SELV!!!!", "source": "SannNyhet.xyz", "body": "Anonym kilde sier at ordføreren er den egentlige tyven og at politiet dekker over det." }
              ]
            },
            {
              "id": 1,
              "type": "AI_PHOTO",
              "systemName": "Bildekontroll",
              "description": "Stopp falske bevis",
              "images": [
                { "src": "/tasks/ai-photo/boss-b.jpg", "alt": "Bilde av en person ved datamaskin", "label": "Bilde A" }
              ]
            },
            {
              "id": 2,
              "type": "PHISHING_EMAIL",
              "systemName": "E-postskjold",
              "description": "Stopp nye phishing-forsøk",
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
              ]
            },
            {
              "id": 3,
              "type": "MARKETPLACE",
              "systemName": "Butikksjekk",
              "description": "Stopp svindelside som samler data",
              "question": "Hva er galt med denne nettsiden?",
              "options": [
                { "id": "a", "text": "Ingenting, den ser legitim ut" },
                { "id": "b", "text": "URL-en er falsk og betalingsvalget er utrygt" },
                { "id": "c", "text": "Kun prisen er for lav" }
              ]
            },
            {
              "id": 4,
              "type": "SOCIAL_MEDIA",
              "systemName": "Sosial signaljakt",
              "description": "Stopp ryktespredning",
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
              ]
            },
            {
              "id": 5,
              "type": "PASSWORD",
              "systemName": "Hovedlåsen",
              "description": "Lås opp den digitale safe og redd pengene",
              "question": "Hvilket passord er sterkt nok til å sikre den redde kontoen?",
              "options": [
                { "id": "a", "value": "admin123" },
                { "id": "b", "value": "Trondheim" },
                { "id": "c", "value": "S0l!Bj0rn#77" },
                { "id": "d", "value": "passord" }
              ]
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
