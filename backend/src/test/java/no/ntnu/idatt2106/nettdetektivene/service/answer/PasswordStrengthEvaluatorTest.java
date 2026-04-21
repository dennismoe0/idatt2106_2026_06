package no.ntnu.idatt2106.nettdetektivene.service.answer;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordStrengthEvaluatorTest {

    private final PasswordStrengthEvaluator evaluator = new PasswordStrengthEvaluator();

    @Test
    void blank_isWeak() {
        assertThat(evaluator.evaluate("")).isEqualTo("WEAK");
    }

    @Test
    void shortLower_isWeak() {
        assertThat(evaluator.evaluate("abc")).isEqualTo("WEAK");
    }

    @Test
    void eightCharMixed_isStrong() {
        assertThat(evaluator.evaluate("Tiger42!")).isEqualTo("STRONG");
    }

    @Test
    void twelveCharFull_isStrong() {
        assertThat(evaluator.evaluate("Tiger!Måne#42")).isEqualTo("STRONG");
    }

    @Test
    void norwegianLettersCountTowardsStrength() {
        assertThat(evaluator.evaluate("Ægir!måne42")).isEqualTo("STRONG");
    }
}
