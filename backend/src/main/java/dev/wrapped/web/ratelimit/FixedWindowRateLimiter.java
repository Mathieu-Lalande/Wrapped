package dev.wrapped.web.ratelimit;

import java.util.Objects;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;

import dev.wrapped.config.WrappedProperties;

/**
 * Rate limiter « fenêtre fixe » en mémoire, par clé (ici l'adresse IP).
 * <p>
 * Volontairement simple : adapté à une instance unique. Pour plusieurs instances,
 * il faudrait un stockage partagé (Redis, Bucket4j…).
 */
public final class FixedWindowRateLimiter {

    /** Borne mémoire : évite qu'un flot d'IP différentes fasse grossir la map sans limite. */
    private static final long MAX_TRACKED_KEYS = 100_000;
    private static final long NANOS_PER_SECOND = 1_000_000_000L;

    private final int maxRequests;
    private final long windowNanos;
    private final Ticker ticker;
    private final Cache<String, Window> windows;

    public FixedWindowRateLimiter(WrappedProperties.RateLimit config, Ticker ticker) {
        Objects.requireNonNull(config, "config");
        if (config.maxRequests() <= 0 || config.window() == null || config.window().isNegative()
                || config.window().isZero()) {
            throw new IllegalArgumentException("Configuration de rate limiting invalide");
        }
        this.maxRequests = config.maxRequests();
        this.windowNanos = config.window().toNanos();
        this.ticker = ticker;
        this.windows = Caffeine.newBuilder()
                .ticker(ticker)
                .expireAfterWrite(config.window())
                .maximumSize(MAX_TRACKED_KEYS)
                .build();
    }

    public Decision tryAcquire(String key) {
        long now = ticker.read();
        Window window = windows.asMap().compute(key, (k, current) ->
                current == null || now - current.startNanos() >= windowNanos
                        ? new Window(now, 1)
                        : current.increment());

        if (window.count() <= maxRequests) {
            return Decision.allowed();
        }
        long remainingNanos = window.startNanos() + windowNanos - now;
        long retryAfterSeconds = Math.max(1, ceilDiv(remainingNanos, NANOS_PER_SECOND));
        return Decision.rejected(retryAfterSeconds);
    }

    private static long ceilDiv(long dividend, long divisor) {
        return Math.ceilDiv(dividend, divisor);
    }

    private record Window(long startNanos, int count) {
        Window increment() {
            // Plafonné pour éviter tout débordement d'entier sous attaque prolongée.
            return new Window(startNanos, count == Integer.MAX_VALUE ? count : count + 1);
        }
    }

    public record Decision(boolean isAllowed, long retryAfterSeconds) {

        static Decision allowed() {
            return new Decision(true, 0);
        }

        static Decision rejected(long retryAfterSeconds) {
            return new Decision(false, retryAfterSeconds);
        }
    }
}
