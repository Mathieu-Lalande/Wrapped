package dev.wrapped.web;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import dev.wrapped.github.GitHubUnavailableException;
import dev.wrapped.github.UserNotFoundException;
import dev.wrapped.web.ratelimit.RateLimitExceededException;

/**
 * Traduit toutes les erreurs en {@link ProblemDetail} (RFC 9457).
 * Aucun message technique, message GitHub brut ou stacktrace n'est renvoyé au client.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String TYPE_BASE = "urn:github-wrapped:problem:";

    @ExceptionHandler(UserNotFoundException.class)
    ProblemDetail handleUserNotFound(UserNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "user-not-found", "Utilisateur introuvable",
                "Aucun utilisateur GitHub ne correspond à ce login.");
    }

    @ExceptionHandler(RateLimitExceededException.class)
    ResponseEntity<ProblemDetail> handleRateLimit(RateLimitExceededException ex) {
        ProblemDetail body = problem(HttpStatus.TOO_MANY_REQUESTS, "rate-limited", "Trop de requêtes",
                "Merci de patienter avant de réessayer.");
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(ex.getRetryAfterSeconds()))
                .body(body);
    }

    @ExceptionHandler(GitHubUnavailableException.class)
    ProblemDetail handleGitHubUnavailable(GitHubUnavailableException ex) {
        log.warn("GitHub indisponible : {}", ex.getMessage());
        return problem(HttpStatus.BAD_GATEWAY, "github-unavailable", "GitHub indisponible",
                "Impossible de récupérer les données GitHub pour le moment. Réessayez plus tard.");
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception ex) {
        log.error("Erreur inattendue", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "internal-error", "Erreur interne",
                "Une erreur inattendue est survenue.");
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<Map<String, String>> errors = ex.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> Map.of(
                                "parameter", String.valueOf(result.getMethodParameter().getParameterName()),
                                "message", messageOf(error))))
                .toList();
        ProblemDetail body = problem(HttpStatus.BAD_REQUEST, "invalid-request", "Requête invalide",
                "Un ou plusieurs paramètres sont invalides.");
        body.setProperty("errors", errors);
        return handleExceptionInternal(ex, body, headers, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        // Le message par défaut recopie la valeur saisie : on renvoie un message générique.
        ProblemDetail body = problem(HttpStatus.BAD_REQUEST, "invalid-request", "Requête invalide",
                "Le paramètre '" + ex.getPropertyName() + "' a un format invalide.");
        return handleExceptionInternal(ex, body, headers, HttpStatus.BAD_REQUEST, request);
    }

    private static String messageOf(MessageSourceResolvable error) {
        return error.getDefaultMessage() == null ? "valeur invalide" : error.getDefaultMessage();
    }

    private static ProblemDetail problem(HttpStatus status, String type, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create(TYPE_BASE + type));
        problem.setTitle(title);
        return problem;
    }
}
