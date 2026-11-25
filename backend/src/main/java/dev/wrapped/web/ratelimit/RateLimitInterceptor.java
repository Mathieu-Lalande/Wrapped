package dev.wrapped.web.ratelimit;

import org.springframework.web.cors.CorsUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Applique le rate limiting par IP cliente.
 * <p>
 * Sécurité : l'IP vient de {@link HttpServletRequest#getRemoteAddr()} et non de l'en-tête
 * {@code X-Forwarded-For}, falsifiable par le client. Derrière un reverse proxy de confiance,
 * activer {@code server.forward-headers-strategy} pour que {@code getRemoteAddr()} reflète
 * l'IP réelle.
 */
public class RateLimitInterceptor implements HandlerInterceptor {

    private final FixedWindowRateLimiter rateLimiter;

    public RateLimitInterceptor(FixedWindowRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (CorsUtils.isPreFlightRequest(request)) {
            return true;
        }
        FixedWindowRateLimiter.Decision decision = rateLimiter.tryAcquire(request.getRemoteAddr());
        if (!decision.isAllowed()) {
            throw new RateLimitExceededException(decision.retryAfterSeconds());
        }
        return true;
    }
}
