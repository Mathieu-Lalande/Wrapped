package dev.wrapped.domain;

import java.util.List;

/**
 * Réponse de l'API : un « Wrapped » annuel complet.
 *
 * @param favoriteWeekday {@code null} si aucune contribution sur l'année
 * @param mostActiveMonth {@code null} si aucune contribution sur l'année
 */
public record WrappedResponse(
        String login,
        String name,
        int year,
        Totals totals,
        List<DayStat> calendar,
        Streak longestStreak,
        Streak currentStreak,
        WeekdayStat favoriteWeekday,
        MonthStat mostActiveMonth,
        List<RepoStat> topRepositories,
        List<LanguageStat> topLanguages,
        Persona persona) {

    public WrappedResponse {
        calendar = List.copyOf(calendar);
        topRepositories = List.copyOf(topRepositories);
        topLanguages = List.copyOf(topLanguages);
    }
}
