package no.ntnu.idatt2106.nettdetektivene.util;

import no.ntnu.idatt2106.nettdetektivene.repository.ClassroomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class ClassroomCodeGenerator {
    private static final Logger log = LoggerFactory.getLogger(ClassroomCodeGenerator.class);
    private static final int MAX_ATTEMPTS = 100;

    private static final String[] PREFIXES = {
        "fjord", "skog", "sol", "nord", "furu", "myr", "vik", "fjell", "gran", "hav",
        "dal", "hei", "li", "lund", "mark", "nes", "øy", "sti", "tind", "voll",
        "berg", "bekk", "eng", "foss", "glimt", "holme", "kyst", "lyng", "mo", "sjø",
        "strand", "topp", "tun", "vidde", "aker", "bris", "dag", "eik", "grotte", "haug"
    };

    private static final String[] SUFFIXES = {
        "tiger", "ulv", "ørn", "rev", "gaupe", "elg", "falk", "oter", "hare", "bjørn",
        "ravn", "mink", "sel", "rein", "hubro", "ugle", "lerke", "spurv", "trost", "laks",
        "hval", "nise", "marihøne", "bie", "humle", "maur", "grevling", "hjort", "ekorn", "rype",
        "terne", "and", "svane", "geit", "sau", "hest", "hund", "katt", "mus", "rotte"
    };

    private final SecureRandom random = new SecureRandom();

    public String generate(ClassroomRepository repository) {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            String code = PREFIXES[random.nextInt(PREFIXES.length)]
                + "-"
                + SUFFIXES[random.nextInt(SUFFIXES.length)];
            if (!repository.existsByJoinCode(code)) {
                return code;
            }
        }

        log.error("Failed to generate unique classroom code after {} attempts", MAX_ATTEMPTS);
        throw new IllegalStateException("Could not generate a unique classroom code");
    }
}
