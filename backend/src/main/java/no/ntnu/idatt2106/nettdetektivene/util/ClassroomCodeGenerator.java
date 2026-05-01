package no.ntnu.idatt2106.nettdetektivene.util;

import no.ntnu.idatt2106.nettdetektivene.repository.ClassroomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Generates unique, human-readable join codes for classrooms.
 * Codes combine a Norwegian nature-themed prefix with an animal-themed suffix
 * (e.g. {@code fjord-ulv}, {@code skog-ørn}). The generator retries up to
 * {@code MAX_ATTEMPTS} times before throwing an exception.
 */
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

    /**
     * Generates a unique classroom join code that does not yet exist in the database.
     *
     * @param repository the classroom repository used to check for uniqueness
     * @return a unique join code such as {@code fjord-ulv}
     * @throws IllegalStateException if a unique code cannot be found within the allowed attempts
     */
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
