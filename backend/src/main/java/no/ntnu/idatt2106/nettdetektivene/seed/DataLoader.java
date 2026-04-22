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
                    new Slide("📰", "Hva er falske nyheter?", "Falske nyheter prøver å se ut som vanlige artikler, men bruker overdrivelser, rykter eller direkte løgner for å få deg til å reagere raskt. De spiller ofte på følelser som frykt, sinne eller sjokk i stedet for å vise tydelige fakta."),
                    new Slide("🔍", "Hva bør du sjekke først?", "Begynn med domenet og kilden. NRK.no, VG.no og trondheim.kommune.no er eksempler på troverdige avsendere, mens navn som supernytt24.xyz eller deldettenaa.blog bør få deg til å stoppe opp og sjekke ekstra nøye."),
                    new Slide("🧠", "Slik avslører du en svak sak", "Se etter anonyme kilder, sensasjonelle overskrifter og påstander som virker umulige, som at alle skoler i Norge stenger samme dag uten at noen offisielle kanaler sier noe. Hvis saken er ekte, skal du som regel kunne finne samme informasjon hos flere seriøse medier eller hos den som faktisk er ansvarlig."),
                    new Quiz("q1", "Hva er det første du bør se etter i en nyhetsartikkel?", new String[]{"Domenet og kilden", "Fargen på overskriften", "Antall delinger"}, "Domenet og kilden"),
                    new Quiz("q2", "Hva er et varseltegn i en overskrift?", new String[]{"Rolig og saklig språk", "Store bokstaver og skremmende ordvalg", "Kort og presis tekst"}, "Store bokstaver og skremmende ordvalg"),
                    new Quiz("q3", "Hva bør du gjøre om du er usikker på en nyhet?", new String[]{"Dele den for å advare andre", "Ignorere den alltid", "Sjekke den på andre seriøse nettsteder"}, "Sjekke den på andre seriøse nettsteder")
                )
            ),
            learnTask(mailStop, 1, "Lær om phishing", "Les kortene og svar riktig på alle spørsmål for å gå videre.",
                learnContentJson(
                    new Slide("📧", "Hva er phishing?", "Phishing er meldinger som later som de kommer fra en bank, skole eller kjent tjeneste for å lure deg til å gi fra deg passord, BankID eller betalingsinformasjon. De ser ofte ganske ekte ut ved første blikk, nettopp fordi de er laget for å stresse deg."),
                    new Slide("🎣", "Hva avslører en phishing-mail?", "Sjekk avsenderen nøye: dnb.no kan være ekte, men dnb-kundeservice.com er ikke det samme. Se også etter lenker som peker til rare domener, og formuleringer som 'bekreft innen 30 minutter' eller 'kontoen din sperres i dag'."),
                    new Slide("🛡️", "Hva bør du gjøre i praksis?", "Ikke klikk i selve e-posten hvis du blir usikker. Åpne heller nettleseren selv og gå direkte til riktig nettside, for eksempel dnb.no eller skolens innloggingsportal, og sjekk der om det faktisk finnes et varsel på kontoen din."),
                    new Quiz("q1", "Hva er phishing?", new String[]{"Å prøve mange passord automatisk", "E-poster som later som å komme fra pålitelige kilder for å stjele informasjon", "Spam-reklame"}, "E-poster som later som å komme fra pålitelige kilder for å stjele informasjon"),
                    new Quiz("q2", "Hva er et varseltegn i en e-post?", new String[]{"Avsenderen er på norsk", "Hasteord og lenker til ukjente sider", "E-posten har et bilde"}, "Hasteord og lenker til ukjente sider"),
                    new Quiz("q3", "Hva bør du gjøre med en mistenkelig e-post?", new String[]{"Svare og spørre om det er ekte", "Slette den og gå direkte til nettstedet selv", "Videresende til venner"}, "Slette den og gå direkte til nettstedet selv")
                )
            ),
            learnTask(photoStop, 1, "Lær om KI-bilder", "Les kortene og svar riktig på alle spørsmål for å gå videre.",
                learnContentJson(
                    new Slide("🤖", "Hva er et KI-bilde?", "Et KI-generert bilde kan se ekte ut, men personen, stedet eller situasjonen har aldri eksistert. Slike bilder kan brukes som falske bevis i nyheter, poster og meldinger."),
                    new Slide("🔎", "Hva skal du se etter?", "Se ekstra nøye på hender, tenner, tekst, smykker, bakgrunn og skygger. KI lager ofte små feil, som seks fingre, bokstaver som ikke blir ord, eller mønstre som gjentar seg unaturlig flere steder i bildet."),
                    new Slide("🖼️", "KI-generert eller manipulert?", "Et manipulert bilde starter ofte som et ekte bilde som noen har redigert etterpå, for eksempel ved å endre et ansikt eller fjerne noe fra bakgrunnen. Et KI-bilde er som regel laget fra bunnen av, og da ser du ofte mange små feil i hele bildet, ikke bare ett sted."),
                    new Quiz("q1", "Hva er vanlige feil i KI-genererte bilder?", new String[]{"For mange farger", "Merkelige hender og urealistisk glatt hud", "For lav bildekvalitet"}, "Merkelige hender og urealistisk glatt hud"),
                    new Quiz("q2", "Hva skiller et KI-generert bilde fra et manipulert bilde?", new String[]{"KI-bilder er alltid svart-hvitt", "KI-bilder er laget av AI, manipulerte er ekte bilder som er endret", "Manipulerte bilder har alltid bedre kvalitet"}, "KI-bilder er laget av AI, manipulerte er ekte bilder som er endret"),
                    new Quiz("q3", "Hva bør du gjøre om du er usikker på et bilde?", new String[]{"Dele det for å få andres mening", "Bruke omvendt bildesøk for å sjekke opprinnelsen", "Ignorere det"}, "Bruke omvendt bildesøk for å sjekke opprinnelsen")
                )
            ),
            learnTask(pwdStop, 1, "Lær om passord", "Les kortene og svar riktig på alle spørsmål for å gå videre.",
                learnContentJson(
                    new Slide("🔐", "Hva gjør et passord sterkt?", "Et sterkt passord er langt og vanskelig å gjette. Det bør ikke inneholde navnet ditt, fødselsåret ditt eller noe en venn enkelt kunne ha tenkt seg fram til."),
                    new Slide("⚠️", "Hva er typiske dårlige passord?", "Eksempler som Ola2012, passord123 eller Liverpool10 er svake fordi de er enkle å gjette eller finnes i vanlige passordlister. Hackere bruker automatiske verktøy som prøver slike kombinasjoner først."),
                    new Slide("💡", "Et bedre alternativ", "En passordfrase som Fjord!TacoMaane42 er mye tryggere enn korte passord som P@ss1. Det viktigste er at du lager noe langt, unikt og forskjellig fra konto til konto."),
                    new Quiz("q1", "Hva gjør et passord sterkest?", new String[]{"Det er enkelt å huske", "Det er langt og bruker ulike tegn uten personlig info", "Det inneholder navn og fødselsdato"}, "Det er langt og bruker ulike tegn uten personlig info"),
                    new Quiz("q2", "Hvilket av disse er et svakt passord?", new String[]{"Sol!Fjord#42Hest", "Ola2010", "hX9!wP$3mQ"}, "Ola2010"),
                    new Quiz("q3", "Hva er en passordfrase?", new String[]{"Et langt ord", "En rekke tilfeldige ord som danner et langt passord", "Passordet til telefonen"}, "En rekke tilfeldige ord som danner et langt passord")
                )
            ),
            learnTask(marketStop, 1, "Lær om nettsvindel", "Les kortene og svar riktig på alle spørsmål for å gå videre.",
                learnContentJson(
                    new Slide("🛒", "Hvordan ser nettsvindel ut?", "Svindelbutikker prøver å se ekte ut, men lokker ofte med varer som er altfor billige til å være sanne. Målet er enten å ta pengene dine eller få tak i kortinformasjonen din."),
                    new Slide("🚩", "Røde flagg du bør merke deg", "Vær skeptisk hvis domenet er ukjent, for eksempel super-sneakers.cc eller tilbud-kiosk.xyz, hvis kontaktinformasjon mangler, eller hvis butikken vil ha betaling med gavekort, krypto eller bankoverføring rett til en privatperson."),
                    new Slide("✅", "Hvordan handler du tryggere?", "Sjekk om butikken har ekte kontaktinfo, vanlige betalingsløsninger og anmeldelser som faktisk virker troverdige. Kortbetaling er tryggere enn gavekort fordi du har større sjanse til å få pengene tilbake hvis noe er galt."),
                    new Quiz("q1", "Hva er et varseltegn på en useriøs nettbutikk?", new String[]{"De har mange produkter", "De krever betaling med gavekort", "De tilbyr gratis frakt"}, "De krever betaling med gavekort"),
                    new Quiz("q2", "Hva gjør betaling med gavekort risikabelt?", new String[]{"Det er saktere", "Pengene er nesten umulige å spore og få tilbake", "Du får ikke kvittering"}, "Pengene er nesten umulige å spore og få tilbake"),
                    new Quiz("q3", "Hva bør du gjøre om en nettbutikk virker mistenkelig?", new String[]{"Kjøp og håp det ordner seg", "Be venner handle der først", "Søk opp butikken og les anmeldelser"}, "Søk opp butikken og les anmeldelser")
                )
            ),
            learnTask(socialStop, 1, "Lær om sosiale medier", "Les kortene og svar riktig på alle spørsmål for å gå videre.",
                learnContentJson(
                    new Slide("📱", "Hvorfor lures vi lettere i feeden?", "På sosiale medier går alt fort, og innlegg som gjør deg sint, redd eller veldig nysgjerrig blir ofte delt mest. Derfor må du være ekstra rolig akkurat når noe føles mest dramatisk."),
                    new Slide("🎭", "Hvordan ser en falsk konto ut?", "Det kan være en konto med veldig få ekte bilder, merkelig brukernavn eller noen som tar kontakt og blir veldig personlig veldig fort. Hvis en konto spør om telefonnummer, bilder eller innlogging, bør du stoppe med en gang."),
                    new Slide("🤔", "Hva bør du gjøre før du deler?", "Spør deg selv: Hvem har lagt ut dette, og hvordan vet de det? Hvis innlegget roper 'DEL NÅ', navngir folk uten bevis eller bare bygger på rykter fra skolen, er det et tegn på at du bør sjekke før du gjør noe."),
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
                "Finn den ekte saken blant tre artikler om snøkaos og skolehverdag.",
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
                          "headline": "Forskere: Ny snøspray over byen gjør at skolegårder blir helt isfrie på fem sekunder",
                          "body": "En ukjent blogg hevder at kommunen har testet en hemmelig kjemispray som smelter all is med én gang og gjør skolegårder varme resten av vinteren. Påstanden viser ikke til forskning, navn på eksperter eller noen steder teknologien faktisk brukes.",
                          "source": "Vintermirakel.blog",
                          "author": "Maks Nyhet",
                          "date": "2026-01-13",
                          "isReal": false
                        }
                      ],
                      "explanation": "Den ekte saken viser til en kjent offentlig kilde og konkrete råd. De falske artiklene bruker ukjente kilder, dramatiske ord og påstander som ikke gir mening i virkeligheten."
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
                          "headline": "Ny sensor i taket skal automatisk blokkere mobilsignaler på alle busser i Midt-Norge",
                          "body": "Artikkelen påstår at et nytt system kan lese skjermene til passasjerene og slå av internett for dem som ser på videoer. Den forklarer ikke hvordan dette skulle være lovlig eller teknisk mulig, og ingen seriøse kilder omtaler systemet.",
                          "source": "framtidsbuss.info",
                          "author": "TekTeam",
                          "date": "2026-02-02",
                          "isReal": false
                        }
                      ],
                      "explanation": "Den ekte saken er konkret og begrenset til en liten test. De falske artiklene går rett på frykt og sensasjon, men mangler troverdige kilder og beskriver ting som høres usannsynlige ut."
                    }
                    """
            ),
            fakeNewsTask(
                newsStop,
                4,
                "Gratis spillvaluta",
                "Tre saker lover ulike ting til spillere. Bare én tåler en kritisk sjekk.",
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
                          "headline": "Skjult server i Sverige deler ut gratis skins hvis du oppgir passord og telefonnummer",
                          "body": "En ukjent side hevder at en privat server samarbeider med spillet og kan fylle opp kontoer med premium-innhold på sekunder. Påstanden er umulig å sjekke, og siden ber om både passord, telefonnummer og engangskode.",
                          "source": "UltraSkinDrop.net",
                          "author": "AK Gamer",
                          "date": "2026-03-07",
                          "isReal": false
                        }
                      ],
                      "explanation": "Den ekte saken advarer og viser til offisielle kanaler. De falske sakene lover urealistiske gevinster, bruker hastverk og prøver å få deg til å gi fra deg innloggingsinformasjon."
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
                    new Clue("amount", "text", "4 890 kr til Steam Market", false, null)
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
                    new Clue("fee", "text", "19 kr", false, null)
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
                    new Clue("brand", "text", "IT-support VGS", false, null)
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
                    { "id": "contact", "label": "kontakt@sneaker-blitz.shop", "explanation": "Kontaktinfo finnes, så dette alene er ikke nok.", "isSuspicious": false },
                    { "id": "price",   "label": "299",                      "explanation": "Prisen er rar, men ikke det tydeligste klikkmålet her.", "isSuspicious": false }
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
                    { "id": "payment", "label": "Visa / Mastercard",               "explanation": "Vanlig kortbetaling er ikke mistenkelig i seg selv.", "isSuspicious": false },
                    { "id": "price",   "label": "1 499",                           "explanation": "Veldig lav pris er et hint, men ikke hovedsporet her.", "isSuspicious": false }
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
                    { "id": "domain",  "label": "komplett.no",                            "explanation": "Dette er et kjent norsk domenenavn.", "isSuspicious": false },
                    { "id": "payment", "label": "Visa, Mastercard, Vipps",                "explanation": "Betalingsmåtene ser helt normale ut.", "isSuspicious": false },
                    { "id": "contact", "label": "23 05 52 00 | kundeservice@komplett.no", "explanation": "Tydelig kontaktinfo er et godt tegn.", "isSuspicious": false }
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
        task.setCorrectAnswerJson(fakeNewsCorrectAnswerJson(contentJson));
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

    private String fakeNewsCorrectAnswerJson(String contentJson) {
        try {
            JsonNode root = objectMapper.readTree(contentJson);
            ArrayNode articles = (ArrayNode) root.path("articles");
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
