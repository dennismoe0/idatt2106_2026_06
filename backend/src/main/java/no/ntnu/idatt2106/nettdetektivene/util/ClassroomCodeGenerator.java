package no.ntnu.idatt2106.nettdetektivene.util;

import no.ntnu.idatt2106.nettdetektivene.repository.ClassroomRepository;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class ClassroomCodeGenerator {
    private static final String[] PREFIXES = {
        "fjord", "skog", "sol", "nord", "furu", "myr", "vik", "fjell", "gran", "hav"
    };

    private static final String[] SUFFIXES = {
        "tiger", "ulv", "orn", "rev", "gaupe", "elg", "falk", "oter", "hare", "bjorn"
    };

    private final SecureRandom random = new SecureRandom();

    public String generate(ClassroomRepository repository) {
        String code;
        do {
            code = PREFIXES[random.nextInt(PREFIXES.length)]
                + "-"
                + SUFFIXES[random.nextInt(SUFFIXES.length)];
        } while (repository.existsByJoinCode(code));
        return code;
    }
}
