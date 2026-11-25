package dev.wrapped.github;

import java.time.Year;

import dev.wrapped.github.dto.GitHubContributions;

/**
 * Port d'accès à GitHub. L'implémentation est un détail d'infrastructure,
 * ce qui permet de la mocker dans tous les tests.
 */
public interface GitHubClient {

    /**
     * @throws UserNotFoundException      si le login n'existe pas
     * @throws GitHubUnavailableException en cas d'erreur d'appel GitHub
     */
    GitHubContributions fetchContributions(String login, Year year);
}
