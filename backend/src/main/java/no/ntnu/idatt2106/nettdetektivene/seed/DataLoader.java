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

        // LEARN tasks — first task (orderIndex 1) for every non-boss stop
        tasks.addAll(List.of(
            learnTask(newsStop, 1, "Lær om falske nyheter", "Les kortene og svar riktig på alle spørsmål for å gå videre.",
                learnContentJson(
                    new Slide("📰", "Hva er falske nyheter?", "Falske nyheter er artikler som ser ekte ut men inneholder usannheter, overdrivelser eller rene løgner. De lages for å spre frykt, påvirke meninger eller få klikk."),
                    new Slide("🔍", "Slik kjenner du dem igjen", "Se etter anonyme kilder ('en kilde sier...'), skremmende overskrifter med store bokstaver, domener du ikke kjenner igjen, og nyheter som ikke finnes på andre seriøse nettsteder."),
                    new Slide("🧠", "Sjekk alltid kilden", "Søk opp nyheten på kjente medier som NRK, VG eller Dagbladet. En ekte nyhet kan bekreftes av flere uavhengige kilder."),
                    new Quiz("q1", "Hva er det første du bør se etter i en nyhetsartikkel?", new String[]{"Domenet og kilden", "Fargen på overskriften", "Antall delinger"}, "Domenet og kilden"),
                    new Quiz("q2", "Hva er et varseltegn i en overskrift?", new String[]{"Rolig og saklig språk", "Store bokstaver og skremmende ordvalg", "Kort og presis tekst"}, "Store bokstaver og skremmende ordvalg"),
                    new Quiz("q3", "Hva bør du gjøre om du er usikker på en nyhet?", new String[]{"Dele den for å advare andre", "Ignorere den alltid", "Sjekke den på andre seriøse nettsteder"}, "Sjekke den på andre seriøse nettsteder")
                )
            ),
            learnTask(mailStop, 1, "Lær om phishing", "Les kortene og svar riktig på alle spørsmål for å gå videre.",
                learnContentJson(
                    new Slide("📧", "Hva er phishing?", "Phishing er e-poster som later som de er fra banker, offentlige kontorer eller kjente selskaper for å lure deg til å gi fra deg passord, personnummer eller penger."),
                    new Slide("🎣", "Slik ser phishing ut", "Se etter: feil domene i avsenderadressen, hasteoppfordringer ('kontoen din stenges om 2 timer'), lenker der URL-en ikke stemmer, og forespørsel om personlig informasjon."),
                    new Slide("🛡️", "Slik beskytter du deg", "Klikk aldri på lenker i mistenkelige e-poster. Gå direkte til nettbanken din selv. En ekte bank vil aldri be deg bekrefte passord via e-post."),
                    new Quiz("q1", "Hva er phishing?", new String[]{"Å prøve mange passord automatisk", "E-poster som later som å komme fra pålitelige kilder for å stjele informasjon", "Spam-reklame"}, "E-poster som later som å komme fra pålitelige kilder for å stjele informasjon"),
                    new Quiz("q2", "Hva er et varseltegn i en e-post?", new String[]{"Avsenderen er på norsk", "Hasteord og lenker til ukjente sider", "E-posten har et bilde"}, "Hasteord og lenker til ukjente sider"),
                    new Quiz("q3", "Hva bør du gjøre med en mistenkelig e-post?", new String[]{"Svare og spørre om det er ekte", "Slette den og gå direkte til nettstedet selv", "Videresende til venner"}, "Slette den og gå direkte til nettstedet selv")
                )
            ),
            learnTask(photoStop, 1, "Lær om KI-bilder", "Les kortene og svar riktig på alle spørsmål for å gå videre.",
                learnContentJson(
                    new Slide("🤖", "KI-genererte bilder", "Kunstig intelligens kan lage realistiske bilder av folk og steder som ikke finnes. Disse brukes til falske bevis, falske nyheter og manipulasjon."),
                    new Slide("🔎", "Slik avslører du KI-bilder", "Se etter: merkelige fingre, urealistisk glatt hud, bakgrunner som gjentar seg, uskarp eller meningsløs tekst, og usannsynlig perfekte detaljer."),
                    new Slide("🖼️", "KI-generert vs. manipulert", "Et manipulert bilde er et ekte bilde som er endret. Et KI-generert bilde er laget helt fra bunnen av AI. Begge kan brukes til å lure deg."),
                    new Quiz("q1", "Hva er vanlige feil i KI-genererte bilder?", new String[]{"For mange farger", "Merkelige hender og urealistisk glatt hud", "For lav bildekvalitet"}, "Merkelige hender og urealistisk glatt hud"),
                    new Quiz("q2", "Hva skiller et KI-generert bilde fra et manipulert bilde?", new String[]{"KI-bilder er alltid svart-hvitt", "KI-bilder er laget av AI, manipulerte er ekte bilder som er endret", "Manipulerte bilder har alltid bedre kvalitet"}, "KI-bilder er laget av AI, manipulerte er ekte bilder som er endret"),
                    new Quiz("q3", "Hva bør du gjøre om du er usikker på et bilde?", new String[]{"Dele det for å få andres mening", "Bruke omvendt bildesøk for å sjekke opprinnelsen", "Ignorere det"}, "Bruke omvendt bildesøk for å sjekke opprinnelsen")
                )
            ),
            learnTask(pwdStop, 1, "Lær om passord", "Les kortene og svar riktig på alle spørsmål for å gå videre.",
                learnContentJson(
                    new Slide("🔐", "Hva gjør et passord sterkt?", "Et sterkt passord er langt (minst 12 tegn), bruker store og små bokstaver, tall og spesialtegn, og inneholder ikke navn, fødselsdato eller andre personlige opplysninger."),
                    new Slide("⚠️", "Vanlige feil", "De svakeste passordene: eget navn, fødselsdato, 'passord', '123456', kjæledyrets navn. Hackere bruker programmer som prøver millioner av kombinasjoner per sekund."),
                    new Slide("💡", "Passordfrase-trikset", "En rekke tilfeldige ord er lettere å huske og vanskeligere å knekke: 'Hest-Sol-Fjord-42!' er mye sterkere enn 'P@ss1'. Bruk aldri samme passord på flere nettsteder."),
                    new Quiz("q1", "Hva gjør et passord sterkest?", new String[]{"Det er enkelt å huske", "Det er langt og bruker ulike tegn uten personlig info", "Det inneholder navn og fødselsdato"}, "Det er langt og bruker ulike tegn uten personlig info"),
                    new Quiz("q2", "Hvilket av disse er et svakt passord?", new String[]{"Sol!Fjord#42Hest", "Ola2010", "hX9!wP$3mQ"}, "Ola2010"),
                    new Quiz("q3", "Hva er en passordfrase?", new String[]{"Et langt ord", "En rekke tilfeldige ord som danner et langt passord", "Passordet til telefonen"}, "En rekke tilfeldige ord som danner et langt passord")
                )
            ),
            learnTask(marketStop, 1, "Lær om nettsvindel", "Les kortene og svar riktig på alle spørsmål for å gå videre.",
                learnContentJson(
                    new Slide("🛒", "Nettsalg-svindel", "Svindlere lager falske nettbutikker for å ta pengene dine. Produktet kommer aldri, og noen ganger stjeler de betalingsinformasjonen din."),
                    new Slide("🚩", "Varseltegn i nettbutikker", "Se etter: urealistisk lave priser, ukjente domener (.xyz, .cc), betaling med gavekort eller Western Union, og manglende kontaktinformasjon."),
                    new Slide("✅", "Trygg netthandel", "Handle kun på kjente nettsteder. Sjekk at adressen starter med 'https'. Betal med kort — da har du bedre sjanse til å få pengene tilbake."),
                    new Quiz("q1", "Hva er et varseltegn på en useriøs nettbutikk?", new String[]{"De har mange produkter", "De krever betaling med gavekort", "De tilbyr gratis frakt"}, "De krever betaling med gavekort"),
                    new Quiz("q2", "Hva gjør betaling med gavekort risikabelt?", new String[]{"Det er saktere", "Pengene er nesten umulige å spore og få tilbake", "Du får ikke kvittering"}, "Pengene er nesten umulige å spore og få tilbake"),
                    new Quiz("q3", "Hva bør du gjøre om en nettbutikk virker mistenkelig?", new String[]{"Kjøp og håp det ordner seg", "Be venner handle der først", "Søk opp butikken og les anmeldelser"}, "Søk opp butikken og les anmeldelser")
                )
            ),
            learnTask(socialStop, 1, "Lær om sosiale medier", "Les kortene og svar riktig på alle spørsmål for å gå videre.",
                learnContentJson(
                    new Slide("📱", "Sosiale medier og manipulasjon", "Sosiale medier kan brukes til å spre feilinformasjon og påvirke hva du tenker. Innhold som vekker sterke følelser spres mye raskere enn faktabasert innhold."),
                    new Slide("🎭", "Falske kontoer", "Fremmede som raskt vil bli nære venner, noen som ber om personlig informasjon, eller 'kjente' som oppfører seg rart — kan alle være falske kontoer."),
                    new Slide("🤔", "Tenk før du deler", "Innlegg med mange utropstegn, kapslås og 'del NÅ!' forsøker å hindre deg i å tenke. Stopp, pust, sjekk kilden — del aldri noe du ikke har bekreftet."),
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
                3,
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
                4,
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
                2,
                "Bankvarsel",
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
                3,
                "Pakkemelding",
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
                4,
                "Skolekonto",
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
            aiPhotoTask(photoStop, 2, "Parkbilder", "Er bildet ekte, KI-generert eller manipulert?",
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
            aiPhotoTask(photoStop, 3, "Bytorget", "Finn hvilket bilde som er ekte og kan brukes som bevis.",
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
            aiPhotoTask(photoStop, 4, "Bevisbildet", "Kun ett bilde kan brukes som ekte bevis. Finn det.",
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
                    { "id": "domain",  "label": "sneaker-blitz.shop",      "explanation": "Domenet er ukjent og bruker .shop, ikke .no. Ekte norske butikker har som regel .no-adresser.", "isSuspicious": true },
                    { "id": "payment", "label": "Western Union / Gavekort", "explanation": "Seriøse nettbutikker aksepterer ikke gavekort eller Western Union — slik betaling er nesten umulig å spore.", "isSuspicious": true },
                    { "id": "contact", "label": "kontakt@sneaker-blitz.shop", "explanation": "Kontaktadressen bruker det ukjente domenet, men det er ikke det sterkeste varseltegnet alene.", "isSuspicious": false },
                    { "id": "price",   "label": "299",                      "explanation": "Veldig lav pris er et tegn, men ikke et klikkbart element i seg selv for denne oppgaven.", "isSuspicious": false }
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
                    { "id": "domain",  "label": "billig-elektronikk.cc",          "explanation": "Domenet .cc er uvanlig for norske butikker, og 'billig-elektronikk' er et generisk navn uten synlig firma bak.", "isSuspicious": true },
                    { "id": "contact", "label": "ingen informasjon tilgjengelig",  "explanation": "Manglende kontaktinformasjon er et alvorlig varseltegn. Lovlige butikker har alltid adresse og telefon.", "isSuspicious": true },
                    { "id": "payment", "label": "Visa / Mastercard",               "explanation": "Kortbetaling er ikke mistenkelig i seg selv — det er en standard betalingsmåte.", "isSuspicious": false },
                    { "id": "price",   "label": "1 499",                           "explanation": "Prisen er veldig lav, men prisen alene er ikke det primære varseltegnet her.", "isSuspicious": false }
                  ],
                  "explanation": "Domenet er ukjent og det mangler kontaktinformasjon — to alvorlige varseltegn."
                }
                """,
                "{\"correctElementIds\": [\"domain\", \"contact\"]}",
                "Mangler kontaktinfo og ukjent domene er alvorlige varseltegn."),
            marketplaceTask(marketStop, 4, "Er dette trygt?", "Sjekk nøye — er noe mistenkelig her?",
                """
                {
                  "type": "CLICK_SUSPICIOUS",
                  "siteName": "komplett.no",
                  "question": "Klikk på det som er mistenkelig — eller send tomt svar om alt ser bra ut.",
                  "mockup": {
                    "headline": "Samsung Galaxy S24",
                    "tagline": "Rask levering. 30 dagers returrett.",
                    "productName": "Samsung Galaxy S24 256GB",
                    "price": "8 490",
                    "originalPrice": "",
                    "badges": [],
                    "paymentText": "Betaling: Visa, Mastercard, Vipps",
                    "contactText": "Kontakt: 23 05 52 00 | kundeservice@komplett.no"
                  },
                  "elements": [
                    { "id": "domain",  "label": "komplett.no",                            "explanation": "komplett.no er en kjent og lovlig norsk nettbutikk med lang historikk.", "isSuspicious": false },
                    { "id": "payment", "label": "Visa, Mastercard, Vipps",                "explanation": "Standard og trygge betalingsmåter — ingenting mistenkelig her.", "isSuspicious": false },
                    { "id": "contact", "label": "23 05 52 00 | kundeservice@komplett.no", "explanation": "Full og tydelig kontaktinformasjon er et tegn på en seriøs aktør.", "isSuspicious": false }
                  ],
                  "explanation": "Dette er en legitim nettbutikk. Det riktige svaret var å ikke flagge noe — noen ganger er alt trygt!"
                }
                """,
                "{\"correctElementIds\": []}",
                "Les alt nøye — noen ganger er alt trygt og du skal ikke flagge noe."),
            socialMediaTask(socialStop, 2, "Del eller vent?", "Velg riktig handling.",
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
            socialMediaTask(socialStop, 3, "Følelser på sosiale medier", "Identifiser hvilke følelser innlegget prøver å skape.",
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
            socialMediaTask(socialStop, 4, "Finn det mest illegitime innlegget", "Velg innlegget med minst troverdighet.",
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

    record Slide(String icon, String heading, String body) {}

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
            n.put("body", s.body());
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
