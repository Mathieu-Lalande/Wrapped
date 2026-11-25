package dev.wrapped.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.wrapped.domain.DayStat;
import dev.wrapped.domain.PersonaType;
import dev.wrapped.domain.Streak;
import dev.wrapped.domain.WrappedResponse;
import dev.wrapped.github.GitHubClient;
import dev.wrapped.github.UserNotFoundException;
import dev.wrapped.github.dto.GitHubContributions;
import dev.wrapped.github.dto.GitHubContributions.Day;
import dev.wrapped.github.dto.GitHubContributions.RepositoryCommits;

@ExtendWith(MockitoExtension.class)
class WrappedServiceTest {

    /** « Aujourd'hui » figé au 10 mars 2026 (UTC). */
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-03-10T12:00:00Z"), ZoneOffset.UTC);

    @Mock
    private GitHubClient gitHubClient;

    private WrappedService service;

    @BeforeEach
    void setUp() {
        service = new WrappedService(gitHubClient, new StatsCalculator(), new PersonaResolver(), CLOCK);
    }

    private static List<Day> zeroDays(LocalDate from, LocalDate toInclusive) {
        List<Day> days = new ArrayList<>();
        for (LocalDate d = from; !d.isAfter(toInclusive); d = d.plusDays(1)) {
            days.add(new Day(d, 0));
        }
        return days;
    }

    @Test
    void emptyYearProducesZeroStatsAndHibernatingPersona() {
        // Calendrier GitHub : semaines complètes, débordant sur l'année précédente/suivante.
        List<Day> days = zeroDays(LocalDate.of(2024, 12, 29), LocalDate.of(2026, 1, 3));
        when(gitHubClient.fetchContributions("octo", Year.of(2025)))
                .thenReturn(new GitHubContributions("octo", null, 0, 0, 0, days, List.of()));

        WrappedResponse wrapped = service.getWrapped("octo", 2025);

        assertThat(wrapped.totals().contributions()).isZero();
        assertThat(wrapped.calendar()).hasSize(365)
                .extracting(DayStat::date)
                .allMatch(d -> d.getYear() == 2025);
        assertThat(wrapped.longestStreak()).isEqualTo(Streak.none());
        assertThat(wrapped.currentStreak()).isEqualTo(Streak.none());
        assertThat(wrapped.favoriteWeekday()).isNull();
        assertThat(wrapped.mostActiveMonth()).isNull();
        assertThat(wrapped.topRepositories()).isEmpty();
        assertThat(wrapped.topLanguages()).isEmpty();
        assertThat(wrapped.persona().code()).isEqualTo(PersonaType.HIBERNATING);
    }

    @Test
    void ongoingYearIgnoresFutureDaysAndKeepsStreakWhenTodayIsEmpty() {
        List<Day> days = new ArrayList<>(zeroDays(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)));
        days.set(days.indexOf(new Day(LocalDate.of(2026, 3, 8), 0)), new Day(LocalDate.of(2026, 3, 8), 2));
        days.set(days.indexOf(new Day(LocalDate.of(2026, 3, 9), 0)), new Day(LocalDate.of(2026, 3, 9), 3));
        // Jour futur : ne doit pas être compté
        days.set(days.indexOf(new Day(LocalDate.of(2026, 6, 1), 0)), new Day(LocalDate.of(2026, 6, 1), 99));
        List<RepositoryCommits> repos = List.of(new RepositoryCommits("octo/app", "Java", "#b07219", 5));
        when(gitHubClient.fetchContributions(eq("octo"), any()))
                .thenReturn(new GitHubContributions("octo", "Octo Cat", 5, 0, 0, days, repos));

        WrappedResponse wrapped = service.getWrapped("octo", 2026);

        assertThat(wrapped.calendar()).last().extracting(DayStat::date).isEqualTo(LocalDate.of(2026, 3, 10));
        assertThat(wrapped.totals().contributions()).isEqualTo(5);
        assertThat(wrapped.currentStreak())
                .isEqualTo(new Streak(2, LocalDate.of(2026, 3, 8), LocalDate.of(2026, 3, 9)));
        assertThat(wrapped.longestStreak().days()).isEqualTo(2);
        assertThat(wrapped.topRepositories()).singleElement()
                .satisfies(r -> assertThat(r.nameWithOwner()).isEqualTo("octo/app"));
        assertThat(wrapped.topLanguages()).singleElement()
                .satisfies(l -> assertThat(l.percentage()).isEqualTo(100.0));
        assertThat(wrapped.name()).isEqualTo("Octo Cat");
    }

    @Test
    void resolveYearDefaultsToCurrentYear() {
        assertThat(service.resolveYear(null)).isEqualTo(2026);
        assertThat(service.resolveYear(2020)).isEqualTo(2020);
    }

    @Test
    void propagatesUserNotFound() {
        when(gitHubClient.fetchContributions(eq("ghost"), any())).thenThrow(new UserNotFoundException("ghost"));

        assertThatThrownBy(() -> service.getWrapped("ghost", 2025)).isInstanceOf(UserNotFoundException.class);
    }
}
