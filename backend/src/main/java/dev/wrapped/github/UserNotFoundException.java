package dev.wrapped.github;

public class UserNotFoundException extends RuntimeException {

    private final String login;

    public UserNotFoundException(String login) {
        super("GitHub user not found: " + login);
        this.login = login;
    }

    public String getLogin() {
        return login;
    }
}
