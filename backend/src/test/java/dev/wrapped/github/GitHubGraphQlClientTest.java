package dev.wrapped.github;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import java.time.LocalDate;
import java.time.Year;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import dev.wrapped.github.dto.GitHubContributions;

/**
 * Teste le client avec un serveur HTTP simulé : aucun appel réseau vers GitHub.
 */
class GitHubGraphQlClientTest {

    private MockRestServiceServer server;
    private GitHubGraphQlClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://api.github.test")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer test-token");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new GitHubGraphQlClient(builder.build());
    }

    @Test
    void sendsLoginAndDatesAsGraphQlVariablesAndMapsResponse() {
        server.expect(requestTo("https://api.github.test/graphql"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-token"))
                .andExpect(jsonPath("$.variables.login").value("octocat"))
                .andExpect(jsonPath("$.variables.from").value("2025-01-01T00:00:00Z"))
                .andExpect(jsonPath("$.variables.to").value("2025-12-31T23:59:59Z"))
                .andExpect(jsonPath("$.query", containsString("$login: String!")))
                .andExpect(content().string(not(containsString("user(login: \\\"octocat"))))
                .andRespond(withSuccess("""
                        {"data":{"user":{"login":"octocat","name":"The Octocat",
                          "contributionsCollection":{
                            "totalCommitContributions":12,
                            "totalPullRequestContributions":3,
                            "totalIssueContributions":1,
                            "contributionCalendar":{"weeks":[{"contributionDays":[
                              {"date":"2025-01-01","contributionCount":4,"weekday":3},
                              {"date":"2025-01-02","contributionCount":0}]}]},
                            "commitContributionsByRepository":[
                              {"repository":{"nameWithOwner":"octocat/hello","primaryLanguage":{"name":"Java","color":"#b07219"}},
                               "contributions":{"totalCount":10}},
                              {"repository":{"nameWithOwner":"octocat/notes","primaryLanguage":null},
                               "contributions":{"totalCount":2}}]}}}}
                        """, MediaType.APPLICATION_JSON));

        GitHubContributions result = client.fetchContributions("octocat", Year.of(2025));

        server.verify();
        assertThat(result.login()).isEqualTo("octocat");
        assertThat(result.totalCommitContributions()).isEqualTo(12);
        assertThat(result.days()).containsExactly(
                new GitHubContributions.Day(LocalDate.of(2025, 1, 1), 4),
                new GitHubContributions.Day(LocalDate.of(2025, 1, 2), 0));
        assertThat(result.commitsByRepository()).containsExactly(
                new GitHubContributions.RepositoryCommits("octocat/hello", "Java", "#b07219", 10),
                new GitHubContributions.RepositoryCommits("octocat/notes", null, null, 2));
    }

    @Test
    void unknownUserBecomesUserNotFound() {
        server.expect(requestTo("https://api.github.test/graphql"))
                .andRespond(withSuccess("""
                        {"data":{"user":null},"errors":[{"type":"NOT_FOUND","path":["user"],
                          "message":"Could not resolve to a User with the login of 'ghost'."}]}
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.fetchContributions("ghost", Year.of(2025)))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void otherGraphQlErrorBecomesUnavailable() {
        server.expect(requestTo("https://api.github.test/graphql"))
                .andRespond(withSuccess("""
                        {"data":null,"errors":[{"type":"RATE_LIMITED","message":"API rate limit exceeded"}]}
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.fetchContributions("octocat", Year.of(2025)))
                .isInstanceOf(GitHubUnavailableException.class);
    }

    @Test
    void httpErrorBecomesUnavailable() {
        server.expect(requestTo("https://api.github.test/graphql"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED).body("{\"message\":\"Bad credentials\"}"));

        assertThatThrownBy(() -> client.fetchContributions("octocat", Year.of(2025)))
                .isInstanceOf(GitHubUnavailableException.class);
    }
}
