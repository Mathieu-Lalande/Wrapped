package dev.wrapped.domain;

import java.time.LocalDate;

/**
 * Série de jours consécutifs avec au moins une contribution.
 * {@code start} et {@code end} valent {@code null} quand {@code days == 0}.
 */
public record Streak(int days, LocalDate start, LocalDate end) {

    private static final Streak NONE = new Streak(0, null, null);

    public static Streak none() {
        return NONE;
    }
}
