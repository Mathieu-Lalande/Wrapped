package dev.wrapped.domain;

public record Persona(PersonaType code, String title, String description) {

    public static Persona of(PersonaType type) {
        return new Persona(type, type.title(), type.description());
    }
}
