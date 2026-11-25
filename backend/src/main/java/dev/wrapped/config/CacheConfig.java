package dev.wrapped.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Active le cache ; le provider (Caffeine) et le TTL sont définis dans application.yml
 * via {@code spring.cache.caffeine.spec}.
 */
@Configuration(proxyBeanMethods = false)
@EnableCaching
public class CacheConfig {

    public static final String WRAPPED_CACHE = "wrapped";
}
