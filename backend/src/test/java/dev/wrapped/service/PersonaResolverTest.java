package dev.wrapped.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import dev.wrapped.domain.Persona;
import dev.wrapped.domain.PersonaSignals;
import dev.wrapped.domain.PersonaType;

class PersonaResolverTest {

    private final PersonaResolver resolver = new PersonaResolver();

    static Stream<Arguments> cases() {
        return Stream.of(
                Arguments.of(new PersonaSignals(0, 0, 0, 0, 0, 0, 0, 0), PersonaType.HIBERNATING),
                Arguments.of(new PersonaSignals(500, 400, 50, 50, 30, 5, 0.5, 0.9), PersonaType.MARATHONER),
                Arguments.of(new PersonaSignals(100, 40, 20, 0, 5, 1, 0, 1), PersonaType.TEAM_PLAYER),
                Arguments.of(new PersonaSignals(100, 80, 5, 12, 5, 1, 0, 1), PersonaType.BUG_HUNTER),
                Arguments.of(new PersonaSignals(100, 100, 0, 0, 5, 4, 0, 0.3), PersonaType.POLYGLOT),
                Arguments.of(new PersonaSignals(100, 100, 0, 0, 5, 1, 0.4, 0.3), PersonaType.WEEKEND_WARRIOR),
                Arguments.of(new PersonaSignals(100, 100, 0, 0, 5, 1, 0.1, 0.7), PersonaType.LASER_FOCUS),
                Arguments.of(new PersonaSignals(10, 10, 0, 0, 2, 1, 0.1, 0.5), PersonaType.STEADY_BUILDER));
    }

    @ParameterizedTest
    @MethodSource("cases")
    void resolvesPersonaFromSignals(PersonaSignals signals, PersonaType expected) {
        assertThat(resolver.resolve(signals).code()).isEqualTo(expected);
    }

    @Test
    void personaCarriesDisplayTexts() {
        Persona persona = resolver.resolve(new PersonaSignals(0, 0, 0, 0, 0, 0, 0, 0));

        assertThat(persona.title()).isEqualTo(PersonaType.HIBERNATING.title());
        assertThat(persona.description()).isNotBlank();
    }
}
