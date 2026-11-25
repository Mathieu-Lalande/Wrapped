package dev.wrapped.github;

/** Erreur technique côté GitHub (auth, quota, réseau, réponse inattendue). Le détail reste dans les logs. */
public class GitHubUnavailableException extends RuntimeException {

    public GitHubUnavailableException(String message) {
        super(message);
    }

    public GitHubUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
