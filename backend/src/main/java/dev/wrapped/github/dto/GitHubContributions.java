package dev.wrapped.github.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Données de contributions d'un utilisateur, extraites de la réponse GraphQL GitHub.
 * Modèle « infra » : le service métier ne dépend que de ces records, pas du JSON brut.
 */
public record GitHubContributions(
        String login,
        String name,
        int totalCommitContributions,
        int totalPullRequestContributions,
        int totalIssueContributions,
        List<Day> days,
        List<RepositoryCommits> commitsByRepository) {

    public GitHubContributions {
        days = days == null ? List.of() : List.copyOf(days);
        commitsByRepository = commitsByRepository == null ? List.of() : List.copyOf(commitsByRepository);
    }

    public record Day(LocalDate date, int contributionCount) {
    }

    /**
     * @param languageName  peut être {@code null} (repo sans langage principal)
     * @param languageColor peut être {@code null}
     */
    public record RepositoryCommits(String nameWithOwner, String languageName, String languageColor, int commits) {
    }
}
