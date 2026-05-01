package no.ntnu.idatt2106.nettdetektivene.util;

import no.ntnu.idatt2106.nettdetektivene.repository.SchoolRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Generates unique, human-readable join codes for schools.
 * Codes are composed of a Norwegian geographic prefix and a suffix
 * (e.g. {@code nord-barneskole}, {@code berg-01}). The generator retries
 * up to {@code MAX_ATTEMPTS} times before throwing an exception.
 */
@Component
public class SchoolCodeGenerator {
    private static final Logger log = LoggerFactory.getLogger(SchoolCodeGenerator.class);
    private static final int MAX_ATTEMPTS = 100;

    private static final String[] PREFIXES = {
        "nord", "syd", "øst", "vest", "sentrum", "bakke", "ås", "berg", "dal",
        "strand", "vik", "nes", "mo", "hov", "lund", "myr", "eng", "haug",
        "li", "tind", "fjell", "kyst", "holm", "voll", "tun", "mark", "grend"
    };

    private static final String[] SUFFIXES = {
        "a", "b", "c", "d", "e", "f",
        "ungdomsskole", "barneskole", "videregaende",
        "01", "02", "03", "04", "05", "06", "07", "08", "09", "10"
    };

    private final SecureRandom random = new SecureRandom();

    /**
     * Generates a unique school join code that does not yet exist in the database.
     *
     * @param repository the school repository used to check for uniqueness
     * @return a unique join code such as {@code nord-barneskole}
     * @throws IllegalStateException if a unique code cannot be found within the allowed attempts
     */
    public String generate(SchoolRepository repository) {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            String code = PREFIXES[random.nextInt(PREFIXES.length)]
                + "-"
                + SUFFIXES[random.nextInt(SUFFIXES.length)];
            if (!repository.existsByJoinCode(code)) {
                log.info("[SchoolCodeGenerator] Generated code: {}", code);
                return code;
            }
        }
        log.error("[SchoolCodeGenerator] Failed to generate unique code after {} attempts", MAX_ATTEMPTS);
        throw new IllegalStateException("Could not generate a unique school code");
    }
}
