package dev.wrapped.domain;

/**
 * @param contributions total annuel (somme du calendrier de contributions)
 */
public record Totals(int contributions, int commits, int pullRequests, int issues) {
}
