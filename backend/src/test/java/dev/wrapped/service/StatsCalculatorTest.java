package dev.wrapped.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import dev.wrapped.domain.DayStat;
import dev.wrapped.domain.LanguageStat;
import dev.wrapped.domain.Streak;
import dev.wrapped.github.dto.GitHubContributions.RepositoryCommits;

class StatsCalculatorTest {

    private final StatsCalculator calculator = new StatsCalculator();

    /** Construit des jours consécutifs à partir de {@code start} avec les comptes donnés. */
    private static List<DayStat> days(LocalDate start, int... counts) {
        List<DayStat> result = new ArrayList<>();
        for (int i = 0; i < counts.length; i++) {
            result.add(new DayStat(start.plusDays(i), counts[i]));
        }
        return result;
    }

    private static final LocalDate JAN_1 = LocalDate.of(2025, 1, 1);

    @Nested
    class LongestStreak {

        @Test
        void returnsLongestRunOfActiveDays() {
            List<DayStat> days = days(JAN_1, 1, 2, 0, 3, 1, 4, 0, 1);

            Streak streak = calculator.longestStreak(days);

            assertThat(streak).isEqualTo(new Streak(3, LocalDate.of(2025, 1, 4), LocalDate.of(2025, 1, 6)));
        }

        @Test
        void keepsEarliestStreakOnTie() {
            List<DayStat> days = days(JAN_1, 1, 1, 0, 1, 1);

            assertThat(calculator.longestStreak(days).start()).isEqualTo(JAN_1);
        }

        @Test
        void missingDayInCalendarBreaksStreak() {
            List<DayStat> days = List.of(
                    new DayStat(JAN_1, 1),
                    new DayStat(JAN_1.plusDays(1), 1),
                    new DayStat(JAN_1.plusDays(3), 1));

            assertThat(calculator.longestStreak(days).days()).isEqualTo(2);
        }

        @Test
        void streakRunningUntilLastDayIsCounted() {
            assertThat(calculator.longestStreak(days(JAN_1, 0, 1, 1, 1)).days()).isEqualTo(3);
        }

        @Test
        void emptyYearHasNoStreak() {
            assertThat(calculator.longestStreak(days(JAN_1, 0, 0, 0))).isEqualTo(Streak.none());
            assertThat(calculator.longestStreak(List.of())).isEqualTo(Streak.none());
        }
    }

    @Nested
    class CurrentStreak {

        @Test
        void countsBackFromReferenceDate() {
            List<DayStat> days = days(JAN_1, 1, 0, 1, 1, 1);
            LocalDate reference = JAN_1.plusDays(4);

            Streak streak = calculator.currentStreak(days, reference, false);

            assertThat(streak).isEqualTo(new Streak(3, JAN_1.plusDays(2), reference));
        }

        @Test
        void todayWithoutContributionDoesNotBreakStreakWhenDayInProgress() {
            List<DayStat> days = days(JAN_1, 1, 1, 0);
            LocalDate today = JAN_1.plusDays(2);

            Streak streak = calculator.currentStreak(days, today, true);

            assertThat(streak).isEqualTo(new Streak(2, JAN_1, JAN_1.plusDays(1)));
        }

        @Test
        void referenceDayWithoutContributionBreaksStreakForPastYear() {
            List<DayStat> days = days(JAN_1, 1, 1, 0);

            assertThat(calculator.currentStreak(days, JAN_1.plusDays(2), false)).isEqualTo(Streak.none());
        }

        @Test
        void emptyYearHasNoCurrentStreak() {
            assertThat(calculator.currentStreak(List.of(), JAN_1, true)).isEqualTo(Streak.none());
        }
    }

    @Nested
    class Rankings {

        @Test
        void favoriteWeekdayIsTheDayWithMostContributions() {
            // 2025-01-01 est un mercredi
            List<DayStat> days = days(JAN_1, 5, 1, 1, 1, 1, 1, 2, 5);

            assertThat(calculator.favoriteWeekday(days).day()).isEqualTo(DayOfWeek.WEDNESDAY);
            assertThat(calculator.favoriteWeekday(days).contributions()).isEqualTo(10);
        }

        @Test
        void favoriteWeekdayIsNullWhenNoContribution() {
            assertThat(calculator.favoriteWeekday(days(JAN_1, 0, 0))).isNull();
        }

        @Test
        void mostActiveMonth() {
            List<DayStat> days = List.of(
                    new DayStat(LocalDate.of(2025, 1, 10), 3),
                    new DayStat(LocalDate.of(2025, 3, 10), 4),
                    new DayStat(LocalDate.of(2025, 3, 11), 1));

            assertThat(calculator.mostActiveMonth(days).month()).isEqualTo(Month.MARCH);
            assertThat(calculator.mostActiveMonth(days).contributions()).isEqualTo(5);
            assertThat(calculator.mostActiveMonth(List.of())).isNull();
        }

        @Test
        void weekendShare() {
            // 2025-01-04 samedi, 2025-01-05 dimanche, 2025-01-06 lundi
            List<DayStat> days = days(LocalDate.of(2025, 1, 4), 1, 1, 2);

            assertThat(calculator.weekendShare(days)).isEqualTo(0.5);
            assertThat(calculator.weekendShare(List.of())).isZero();
        }

        @Test
        void topRepositoriesSortedByCommitsAndLimited() {
            List<RepositoryCommits> repos = List.of(
                    new RepositoryCommits("a/one", "Java", "#b07219", 5),
                    new RepositoryCommits("a/two", null, null, 50),
                    new RepositoryCommits("a/three", "Go", "#00ADD8", 20),
                    new RepositoryCommits("a/four", "Go", "#00ADD8", 1),
                    new RepositoryCommits("a/zero", "Go", "#00ADD8", 0));

            assertThat(calculator.topRepositories(repos, 3))
                    .extracting(r -> r.nameWithOwner())
                    .containsExactly("a/two", "a/three", "a/one");
            assertThat(calculator.topRepositoryShare(repos)).isEqualTo(50.0 / 76);
        }
    }

    @Nested
    class TopLanguages {

        @Test
        void languagesAreWeightedByCommitsAcrossRepositories() {
            List<RepositoryCommits> repos = List.of(
                    new RepositoryCommits("a/api", "Java", "#b07219", 30),
                    new RepositoryCommits("a/web", "TypeScript", "#3178c6", 40),
                    new RepositoryCommits("a/lib", "Java", "#b07219", 20),
                    new RepositoryCommits("a/docs", null, null, 100),
                    new RepositoryCommits("a/cli", "Go", "#00ADD8", 10));

            List<LanguageStat> languages = calculator.topLanguages(repos, 5);

            assertThat(languages).containsExactly(
                    new LanguageStat("Java", "#b07219", 50, 50.0),
                    new LanguageStat("TypeScript", "#3178c6", 40, 40.0),
                    new LanguageStat("Go", "#00ADD8", 10, 10.0));
            assertThat(calculator.distinctLanguages(repos)).isEqualTo(3);
        }

        @Test
        void percentagesAreRoundedToOneDecimal() {
            List<RepositoryCommits> repos = List.of(
                    new RepositoryCommits("a/x", "Rust", "#dea584", 1),
                    new RepositoryCommits("a/y", "Zig", "#ec915c", 2));

            assertThat(calculator.topLanguages(repos, 5))
                    .extracting(LanguageStat::percentage)
                    .containsExactly(66.7, 33.3);
        }

        @Test
        void limitIsApplied() {
            List<RepositoryCommits> repos = List.of(
                    new RepositoryCommits("a/1", "A", null, 3),
                    new RepositoryCommits("a/2", "B", null, 2),
                    new RepositoryCommits("a/3", "C", null, 1));

            assertThat(calculator.topLanguages(repos, 2)).extracting(LanguageStat::name).containsExactly("A", "B");
        }

        @Test
        void noLanguageWhenNoRepositoryOrNoCommit() {
            assertThat(calculator.topLanguages(List.of(), 5)).isEmpty();
            assertThat(calculator.topLanguages(
                    List.of(new RepositoryCommits("a/x", "Java", "#b07219", 0)), 5)).isEmpty();
        }
    }
}
