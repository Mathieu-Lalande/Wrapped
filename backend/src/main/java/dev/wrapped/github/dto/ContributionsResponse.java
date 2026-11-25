package dev.wrapped.github.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Mapping 1:1 de la réponse GraphQL brute (records imbriqués).
 * Les propriétés JSON inconnues sont ignorées (comportement par défaut de l'ObjectMapper Spring Boot).
 */
public record ContributionsResponse(Data data, List<GraphQlError> errors) {

    public record GraphQlError(String type, String message) {
    }

    public record Data(User user) {
    }

    public record User(String login, String name, ContributionsCollection contributionsCollection) {
    }

    public record ContributionsCollection(
            int totalCommitContributions,
            int totalPullRequestContributions,
            int totalIssueContributions,
            ContributionCalendar contributionCalendar,
            List<RepositoryContribution> commitContributionsByRepository) {
    }

    public record ContributionCalendar(List<Week> weeks) {
    }

    public record Week(List<ContributionDay> contributionDays) {
    }

    public record ContributionDay(LocalDate date, int contributionCount) {
    }

    public record RepositoryContribution(Repository repository, Contributions contributions) {
    }

    public record Repository(String nameWithOwner, Language primaryLanguage) {
    }

    public record Language(String name, String color) {
    }

    public record Contributions(int totalCount) {
    }
}
