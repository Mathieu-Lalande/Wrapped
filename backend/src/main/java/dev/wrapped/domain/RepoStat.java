package dev.wrapped.domain;

/**
 * @param language      {@code null} si le repo n'a pas de langage principal
 * @param languageColor couleur GitHub du langage (ex. {@code #b07219}), ou {@code null}
 */
public record RepoStat(String nameWithOwner, int commits, String language, String languageColor) {
}
