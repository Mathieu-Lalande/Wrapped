package dev.wrapped.domain;

/**
 * @param percentage part des commits (0–100, arrondie à 1 décimale) parmi les commits
 *                   des repos ayant un langage principal
 */
public record LanguageStat(String name, String color, int commits, double percentage) {
}
