package dev.wrapped.domain;

/**
 * Indicateurs agrégés servant à déduire le persona.
 *
 * @param weekendShare part (0–1) des contributions faites le samedi ou le dimanche
 * @param topRepoShare part (0–1) des commits du repo le plus actif parmi les repos remontés
 */
public record PersonaSignals(
        int totalContributions,
        int commits,
        int pullRequests,
        int issues,
        int longestStreakDays,
        int distinctLanguages,
        double weekendShare,
        double topRepoShare) {
}
