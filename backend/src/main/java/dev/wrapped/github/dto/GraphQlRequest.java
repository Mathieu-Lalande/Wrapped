package dev.wrapped.github.dto;

import java.util.Map;

/** Corps d'une requête GraphQL : la requête est statique, les entrées passent par {@code variables}. */
public record GraphQlRequest(String query, Map<String, Object> variables) {
}
