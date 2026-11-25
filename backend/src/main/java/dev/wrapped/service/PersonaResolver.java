package dev.wrapped.service;

import org.springframework.stereotype.Component;

import dev.wrapped.domain.Persona;
import dev.wrapped.domain.PersonaSignals;
import dev.wrapped.domain.PersonaType;

/**
 * Déduit un persona à partir des statistiques, via des règles déterministes évaluées dans l'ordre :
 * la première règle satisfaite l'emporte.
 */
@Component
public class PersonaResolver {

    static final int MARATHON_STREAK_DAYS = 30;
    static final int MIN_ACTIVITY_FOR_TRAIT = 10;
    static final int POLYGLOT_LANGUAGES = 4;
    static final double WEEKEND_SHARE = 0.4;
    static final int MIN_CONTRIBUTIONS_FOR_WEEKEND = 20;
    static final double LASER_FOCUS_SHARE = 0.7;
    static final int MIN_COMMITS_FOR_FOCUS = 20;

    public Persona resolve(PersonaSignals s) {
        return Persona.of(resolveType(s));
    }

    PersonaType resolveType(PersonaSignals s) {
        if (s.totalContributions() == 0) {
            return PersonaType.HIBERNATING;
        }
        if (s.longestStreakDays() >= MARATHON_STREAK_DAYS) {
            return PersonaType.MARATHONER;
        }
        if (s.pullRequests() >= MIN_ACTIVITY_FOR_TRAIT && s.pullRequests() * 2 >= s.commits()) {
            return PersonaType.TEAM_PLAYER;
        }
        if (s.issues() >= MIN_ACTIVITY_FOR_TRAIT && s.issues() >= s.pullRequests()) {
            return PersonaType.BUG_HUNTER;
        }
        if (s.distinctLanguages() >= POLYGLOT_LANGUAGES) {
            return PersonaType.POLYGLOT;
        }
        if (s.totalContributions() >= MIN_CONTRIBUTIONS_FOR_WEEKEND && s.weekendShare() >= WEEKEND_SHARE) {
            return PersonaType.WEEKEND_WARRIOR;
        }
        if (s.commits() >= MIN_COMMITS_FOR_FOCUS && s.topRepoShare() >= LASER_FOCUS_SHARE) {
            return PersonaType.LASER_FOCUS;
        }
        return PersonaType.STEADY_BUILDER;
    }
}
