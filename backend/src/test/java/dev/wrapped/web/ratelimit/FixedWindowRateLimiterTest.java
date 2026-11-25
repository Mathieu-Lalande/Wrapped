package dev.wrapped.web.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

import dev.wrapped.config.WrappedProperties;

class FixedWindowRateLimiterTest {

    private final AtomicLong nanos = new AtomicLong();
    private final FixedWindowRateLimiter limiter = new FixedWindowRateLimiter(
            new WrappedProperties.RateLimit(2, Duration.ofSeconds(60)), nanos::get);

    @Test
    void allowsUpToLimitThenRejectsWithRetryAfter() {
        assertThat(limiter.tryAcquire("1.1.1.1").isAllowed()).isTrue();
        assertThat(limiter.tryAcquire("1.1.1.1").isAllowed()).isTrue();

        nanos.addAndGet(Duration.ofSeconds(20).toNanos());
        FixedWindowRateLimiter.Decision rejected = limiter.tryAcquire("1.1.1.1");

        assertThat(rejected.isAllowed()).isFalse();
        assertThat(rejected.retryAfterSeconds()).isEqualTo(40);
    }

    @Test
    void keysAreIndependent() {
        limiter.tryAcquire("1.1.1.1");
        limiter.tryAcquire("1.1.1.1");

        assertThat(limiter.tryAcquire("2.2.2.2").isAllowed()).isTrue();
    }

    @Test
    void windowResetsAfterExpiry() {
        limiter.tryAcquire("1.1.1.1");
        limiter.tryAcquire("1.1.1.1");
        assertThat(limiter.tryAcquire("1.1.1.1").isAllowed()).isFalse();

        nanos.addAndGet(Duration.ofSeconds(61).toNanos());

        assertThat(limiter.tryAcquire("1.1.1.1").isAllowed()).isTrue();
    }

    @Test
    void rejectsInvalidConfiguration() {
        assertThatThrownBy(() -> new FixedWindowRateLimiter(
                new WrappedProperties.RateLimit(0, Duration.ofSeconds(1)), nanos::get))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
