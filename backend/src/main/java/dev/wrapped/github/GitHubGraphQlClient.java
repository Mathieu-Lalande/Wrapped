package dev.wrapped.github;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.MonthDay;
import java.time.Year;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import dev.wrapped.github.dto.ContributionsResponse;
import dev.wrapped.github.dto.GitHubContributions;
import dev.wrapped.github.dto.GraphQlRequest;

/**
 * Client GraphQL GitHub basé sur {@link RestClient}.
 * La requête est une ressource statique ; login et dates passent exclusivement par les
 * variables GraphQL (aucune concaténation de chaînes, donc pas d'injection GraphQL).
 */
@Component
public class GitHubGraphQlClient implements GitHubClient {

    private static final Logger log = LoggerFactory.getLogger(GitHubGraphQlClient.class);
    private static final String QUERY_RESOURCE = "graphql/contributions.graphql";
    private static final String NOT_FOUND_ERROR_TYPE = "NOT_FOUND";

    private final RestClient restClient;
    private final String query;

    public GitHubGraphQlClient(RestClient gitHubRestClient) {
        this.restClient = gitHubRestClient;
        this.query = loadQuery();
    }

    @Override
    public GitHubContributions fetchContributions(String login, Year year) {
        Map<String, Object> variables = Map.of(
                "login", login,
                // Instant#toString : ISO-8601 UTC avec secondes, ex. 2025-01-01T00:00:00Z (scalaire DateTime GitHub)
                "from", year.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC).toString(),
                "to", year.atMonthDay(MonthDay.of(12, 31)).atTime(23, 59, 59).toInstant(ZoneOffset.UTC).toString());

        ContributionsResponse response;
        try {
            response = restClient.post()
                    .uri("/graphql")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new GraphQlRequest(query, variables))
                    .retrieve()
                    .body(ContributionsResponse.class);
        } catch (RestClientResponseException e) {
            // Le corps de la réponse GitHub n'est jamais propagé au client : log serveur uniquement.
            log.error("Appel GitHub GraphQL en échec : HTTP {}", e.getStatusCode().value());
            throw new GitHubUnavailableException("GitHub HTTP " + e.getStatusCode().value(), e);
        } catch (RestClientException e) {
            log.error("Appel GitHub GraphQL en échec : {}", e.getClass().getSimpleName());
            throw new GitHubUnavailableException("GitHub call failed", e);
        }

        return toContributions(login, response);
    }

    static GitHubContributions toContributions(String login, ContributionsResponse response) {
        if (response == null) {
            throw new GitHubUnavailableException("Empty GitHub response");
        }
        List<ContributionsResponse.GraphQlError> errors =
                response.errors() == null ? List.of() : response.errors();
        ContributionsResponse.User user = response.data() == null ? null : response.data().user();

        if (user == null) {
            boolean notFound = errors.stream().anyMatch(e -> NOT_FOUND_ERROR_TYPE.equals(e.type()));
            if (notFound) {
                throw new UserNotFoundException(login);
            }
            log.error("Réponse GitHub sans utilisateur ({} erreur(s) GraphQL)", errors.size());
            throw new GitHubUnavailableException("GitHub returned no user");
        }
        if (!errors.isEmpty()) {
            log.warn("Réponse GitHub partielle : {} erreur(s) GraphQL", errors.size());
        }
        ContributionsResponse.ContributionsCollection collection = user.contributionsCollection();
        if (collection == null) {
            throw new GitHubUnavailableException("GitHub returned no contributions collection");
        }

        List<GitHubContributions.Day> days = collection.contributionCalendar() == null
                || collection.contributionCalendar().weeks() == null
                ? List.of()
                : collection.contributionCalendar().weeks().stream()
                        .filter(Objects::nonNull)
                        .flatMap(w -> w.contributionDays() == null ? Stream.empty() : w.contributionDays().stream())
                        .filter(d -> d != null && d.date() != null)
                        .map(d -> new GitHubContributions.Day(d.date(), d.contributionCount()))
                        .toList();

        List<GitHubContributions.RepositoryCommits> repos = collection.commitContributionsByRepository() == null
                ? List.of()
                : collection.commitContributionsByRepository().stream()
                        .filter(r -> r != null && r.repository() != null)
                        .map(GitHubGraphQlClient::toRepositoryCommits)
                        .toList();

        return new GitHubContributions(
                user.login(),
                user.name(),
                collection.totalCommitContributions(),
                collection.totalPullRequestContributions(),
                collection.totalIssueContributions(),
                days,
                repos);
    }

    private static GitHubContributions.RepositoryCommits toRepositoryCommits(
            ContributionsResponse.RepositoryContribution contribution) {
        ContributionsResponse.Language language = contribution.repository().primaryLanguage();
        int commits = contribution.contributions() == null ? 0 : contribution.contributions().totalCount();
        return new GitHubContributions.RepositoryCommits(
                contribution.repository().nameWithOwner(),
                language == null ? null : language.name(),
                language == null ? null : language.color(),
                commits);
    }

    private static String loadQuery() {
        try (InputStream in = new ClassPathResource(QUERY_RESOURCE).getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Requête GraphQL introuvable : " + QUERY_RESOURCE, e);
        }
    }

}
