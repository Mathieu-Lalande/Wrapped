package dev.wrapped.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Component;

import dev.wrapped.domain.DayStat;
import dev.wrapped.domain.LanguageStat;
import dev.wrapped.domain.MonthStat;
import dev.wrapped.domain.RepoStat;
import dev.wrapped.domain.Streak;
import dev.wrapped.domain.WeekdayStat;
import dev.wrapped.github.dto.GitHubContributions.RepositoryCommits;

/**
 * Calculs statistiques purs (sans état, sans I/O).
 * Les listes de jours doivent être triées par date croissante.
 */
@Component
public class StatsCalculator {

    public int total(List<DayStat> days) {
        return days.stream().mapToInt(DayStat::count).sum();
    }

    /** Plus longue suite de jours consécutifs avec au moins une contribution (la plus ancienne en cas d'égalité). */
    public Streak longestStreak(List<DayStat> days) {
        Streak best = Streak.none();
        LocalDate start = null;
        LocalDate previous = null;
        int length = 0;

        for (DayStat day : days) {
            boolean active = day.count() > 0;
            boolean contiguous = previous != null && day.date().equals(previous.plusDays(1));
            if (active) {
                if (length > 0 && contiguous) {
                    length++;
                } else {
                    start = day.date();
                    length = 1;
                }
                if (length > best.days()) {
                    best = new Streak(length, start, day.date());
                }
            } else {
                length = 0;
            }
            previous = day.date();
        }
        return best;
    }

    /**
     * Série en cours à la date de référence.
     *
     * @param referenceDate dernier jour pris en compte (aujourd'hui, ou le 31/12 pour une année passée)
     * @param referenceDayInProgress si {@code true}, un jour de référence sans contribution ne casse pas
     *                               la série (la journée n'est pas terminée)
     */
    public Streak currentStreak(List<DayStat> days, LocalDate referenceDate, boolean referenceDayInProgress) {
        Map<LocalDate, Integer> byDate = new LinkedHashMap<>();
        days.forEach(d -> byDate.put(d.date(), d.count()));

        LocalDate cursor = referenceDate;
        if (referenceDayInProgress && byDate.getOrDefault(cursor, 0) == 0) {
            cursor = cursor.minusDays(1);
        }
        LocalDate end = cursor;
        int length = 0;
        while (byDate.getOrDefault(cursor, 0) > 0) {
            length++;
            cursor = cursor.minusDays(1);
        }
        return length == 0 ? Streak.none() : new Streak(length, end.minusDays(length - 1L), end);
    }

    /** Jour de la semaine cumulant le plus de contributions ; {@code null} si aucune. Égalité : lundi d'abord. */
    public WeekdayStat favoriteWeekday(List<DayStat> days) {
        Map<DayOfWeek, Integer> totals = new EnumMap<>(DayOfWeek.class);
        days.forEach(d -> totals.merge(d.date().getDayOfWeek(), d.count(), Integer::sum));

        WeekdayStat best = null;
        for (DayOfWeek dow : DayOfWeek.values()) {
            int value = totals.getOrDefault(dow, 0);
            if (value > 0 && (best == null || value > best.contributions())) {
                best = new WeekdayStat(dow, value);
            }
        }
        return best;
    }

    /** Mois cumulant le plus de contributions ; {@code null} si aucune. Égalité : le plus tôt. */
    public MonthStat mostActiveMonth(List<DayStat> days) {
        Map<Month, Integer> totals = new EnumMap<>(Month.class);
        days.forEach(d -> totals.merge(d.date().getMonth(), d.count(), Integer::sum));

        MonthStat best = null;
        for (Month month : Month.values()) {
            int value = totals.getOrDefault(month, 0);
            if (value > 0 && (best == null || value > best.contributions())) {
                best = new MonthStat(month, value);
            }
        }
        return best;
    }

    /** Part (0–1) des contributions faites le week-end. */
    public double weekendShare(List<DayStat> days) {
        int total = total(days);
        if (total == 0) {
            return 0;
        }
        int weekend = days.stream()
                .filter(d -> d.date().getDayOfWeek() == DayOfWeek.SATURDAY
                        || d.date().getDayOfWeek() == DayOfWeek.SUNDAY)
                .mapToInt(DayStat::count)
                .sum();
        return (double) weekend / total;
    }

    public List<RepoStat> topRepositories(List<RepositoryCommits> repos, int limit) {
        return repos.stream()
                .filter(r -> r.commits() > 0)
                .sorted(Comparator.comparingInt(RepositoryCommits::commits).reversed()
                        .thenComparing(RepositoryCommits::nameWithOwner))
                .limit(limit)
                .map(r -> new RepoStat(r.nameWithOwner(), r.commits(), r.languageName(), r.languageColor()))
                .toList();
    }

    /** Part (0–1) des commits du repo le plus actif. */
    public double topRepositoryShare(List<RepositoryCommits> repos) {
        int total = repos.stream().mapToInt(RepositoryCommits::commits).sum();
        if (total == 0) {
            return 0;
        }
        int max = repos.stream().mapToInt(RepositoryCommits::commits).max().orElse(0);
        return (double) max / total;
    }

    /**
     * Langages pondérés par le nombre de commits de chaque repo (langage principal du repo).
     * Les repos sans langage principal sont ignorés.
     */
    public List<LanguageStat> topLanguages(List<RepositoryCommits> repos, int limit) {
        Map<String, Integer> commitsByLanguage = new LinkedHashMap<>();
        Map<String, String> colorByLanguage = new LinkedHashMap<>();
        for (RepositoryCommits repo : repos) {
            if (repo.languageName() == null || repo.commits() <= 0) {
                continue;
            }
            commitsByLanguage.merge(repo.languageName(), repo.commits(), Integer::sum);
            if (repo.languageColor() != null) {
                colorByLanguage.putIfAbsent(repo.languageName(), repo.languageColor());
            }
        }
        int total = commitsByLanguage.values().stream().mapToInt(Integer::intValue).sum();
        if (total == 0) {
            return List.of();
        }

        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(commitsByLanguage.entrySet());
        sorted.sort(Map.Entry.<String, Integer>comparingByValue().reversed()
                .thenComparing(Map.Entry.comparingByKey()));

        return sorted.stream()
                .limit(limit)
                .map(e -> new LanguageStat(e.getKey(), colorByLanguage.get(e.getKey()), e.getValue(),
                        percentage(e.getValue(), total)))
                .toList();
    }

    public int distinctLanguages(List<RepositoryCommits> repos) {
        return (int) repos.stream()
                .filter(r -> r.commits() > 0)
                .map(RepositoryCommits::languageName)
                .filter(Objects::nonNull)
                .distinct()
                .count();
    }

    private static double percentage(int part, int total) {
        return BigDecimal.valueOf(part * 100.0 / total).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
