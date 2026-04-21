package no.ntnu.idatt2106.nettdetektivene.service.answer;

import org.springframework.stereotype.Component;

@Component
public class PasswordStrengthEvaluator {

    public String evaluate(String password) {
        if (password == null || password.isBlank()) {
            return "WEAK";
        }

        int score = 0;
        if (password.length() >= 12) {
            score += 3;
        } else if (password.length() >= 8) {
            score += 2;
        } else if (password.length() >= 6) {
            score += 1;
        }

        if (password.matches(".*[A-ZÆØÅ].*")) {
            score++;
        }
        if (password.matches(".*[a-zæøå].*")) {
            score++;
        }
        if (password.matches(".*[0-9].*")) {
            score++;
        }
        if (password.matches(".*[^A-Za-z0-9æøåÆØÅ].*")) {
            score++;
        }

        if (score <= 3) {
            return "WEAK";
        }
        if (score <= 5) {
            return "MEDIUM";
        }
        return "STRONG";
    }

    public int strengthLevel(String level) {
        return switch (level.toUpperCase()) {
            case "STRONG" -> 3;
            case "MEDIUM" -> 2;
            default -> 1;
        };
    }
}
