package dev.wrapped.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.Comparator;
import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import dev.wrapped.config.CacheConfig;
import dev.wrapped.domain.DayStat;
import dev.wrapped.domain.PersonaSignals;
import dev.wrapped.domain.Streak;
import dev.wrapped.domain.Totals;
import dev.wrapped.domain.WrappedResponse;
import dev.wrapped.github.GitHubClient;
import dev.wrapped.github.dto.GitHubContributions;

/**
 * Orchestration : récupère les contributions GitHub et construit le Wrapped.
 */
@Service
public class WrappedService {

    static final int TOP_REPOSITORIES = 3;
    static final int TOP_LANGUAGES = 5;

    private final GitHubClient gitHubClient;
    private final StatsCalculator calculator;
    private final PersonaResolver personaResolver;
    private final Clock clock;

    public WrappedService(GitHubClient gitHubClient, StatsCalculator calculator,
                          PersonaResolver personaResolver, Clock clock) {
        this.gitHubClient = gitHubClient;
        this.calculator = calculator;
        this.personaResolver = personaResolver;
        this.clock = clock;
    }

    /** Année demandée, ou année courante (UTC) par défaut. */
    public int resolveYear(Integer requestedYear) {
        return requestedYear != null ? requestedYear : Year.now(clock).getValue();
    }

    /**
     * Les logins GitHub sont insensibles à la casse : la clé de cache est normalisée.
     */
    @Cacheable(cacheNames = CacheConfig.WRAPPED_CACHE, key = "#login.toLowerCase() + ':' + #year")
    public WrappedResponse getWrapped(String login, int year) {
        Year targetYear = Year.of(year);
        GitHubContributions data = gitHubClient.fetchContributions(login, targetYear);

        LocalDate today = LocalDate.now(clock);
        LocalDate lastDayOfYear = targetYear.atDay(targetYear.length());
        boolean ongoingYear = !today.isAfter(lastDayOfYear);
        LocalDate referenceDate = ongoingYear ? today : lastDayOfYear;

        // Le calendrier GitHub couvre des semaines complètes : on ne garde que l'année visée,
        // jusqu'à la date de référence (pas de jours futurs).
        List<DayStat> days = data.days().stream()
                .filter(d -> d.date().getYear() == year && !d.date().isAfter(referenceDate))
                .map(d -> new DayStat(d.date(), d.contributionCount()))
                .sorted(Comparator.comparing(DayStat::date))
                .toList();

        Totals totals = new Totals(
                calculator.total(days),
                data.totalCommitContributions(),
                data.totalPullRequestContributions(),
                data.totalIssueContributions());
        Streak longest = calculator.longestStreak(days);
        Streak current = calculator.currentStreak(days, referenceDate, ongoingYear);

        PersonaSignals signals = new PersonaSignals(
                totals.contributions(),
                totals.commits(),
                totals.pullRequests(),
                totals.issues(),
                longest.days(),
                calculator.distinctLanguages(data.commitsByRepository()),
                calculator.weekendShare(days),
                calculator.topRepositoryShare(data.commitsByRepository()));

        return new WrappedResponse(
                data.login(),
                data.name(),
                year,
                totals,
                days,
                longest,
                current,
                calculator.favoriteWeekday(days),
                calculator.mostActiveMonth(days),
                calculator.topRepositories(data.commitsByRepository(), TOP_REPOSITORIES),
                calculator.topLanguages(data.commitsByRepository(), TOP_LANGUAGES),
                personaResolver.resolve(signals));
    }
}
