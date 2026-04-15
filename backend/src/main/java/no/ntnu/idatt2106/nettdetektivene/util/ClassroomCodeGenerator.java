package no.ntnu.idatt2106.nettdetektivene.util;

import no.ntnu.idatt2106.nettdetektivene.repository.ClassroomRepository;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class ClassroomCodeGenerator {
    private static final String[] PREFIXES = {
        "fjord", "skog", "sol", "nord", "furu", "myr", "vik", "fjell", "gran", "hav",
        "dal", "hei", "li", "lund", "mark", "nes", "oy", "sti", "tind", "voll",
        "berg", "bekk", "eng", "foss", "glimt", "holme", "kyst", "lyng", "mo", "sjo",
        "strand", "topp", "tun", "vidde", "aker", "bris", "dag", "eik", "grot", "haug"
    };

    private static final String[] SUFFIXES = {
        "tiger", "ulv", "orn", "rev", "gaupe", "elg", "falk", "oter", "hare", "bjorn",
        "ravn", "mink", "sel", "rein", "hubro", "ugle", "lerke", "spurv", "troste", "laks",
        "hval", "nise", "marihona", "bie", "humle", "maur", "grevling", "hjort", "ekorn", "rype",
        "terne", "and", "svane", "geit", "sau", "hest", "hund", "katt", "mus", "rotte"
    };

    private final SecureRandom random = new SecureRandom();

    public String generate(ClassroomRepository repository) {
        String code;
        do {
            // Keep this pool comfortably larger than expected Sprint 1 usage to avoid collision-heavy retries.
            code = PREFIXES[random.nextInt(PREFIXES.length)]
                + "-"
                + SUFFIXES[random.nextInt(SUFFIXES.length)];
        } while (repository.existsByJoinCode(code));
        return code;
    }
}
