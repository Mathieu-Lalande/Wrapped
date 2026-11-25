package dev.wrapped.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.github.benmanes.caffeine.cache.Ticker;

import dev.wrapped.web.ratelimit.FixedWindowRateLimiter;
import dev.wrapped.web.ratelimit.RateLimitInterceptor;

/**
 * CORS restreint aux origines configurées + rate limiting par IP sur l'API.
 * <p>
 * Le rate limiting est un {@code HandlerInterceptor} (et non un filtre servlet) : Spring MVC
 * applique la configuration CORS avant les interceptors, donc une réponse 429 porte bien
 * les en-têtes CORS et reste lisible par le front.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(WrappedProperties.class)
public class WebConfig implements WebMvcConfigurer {

    private static final String API_PATTERN = "/api/**";

    private final WrappedProperties properties;
    private final RateLimitInterceptor rateLimitInterceptor;

    public WebConfig(WrappedProperties properties) {
        this.properties = properties;
        this.rateLimitInterceptor = new RateLimitInterceptor(
                new FixedWindowRateLimiter(properties.rateLimit(), Ticker.systemTicker()));
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping(API_PATTERN)
                .allowedOrigins(properties.cors().allowedOrigins().toArray(String[]::new))
                .allowedMethods(HttpMethod.GET.name())
                .allowCredentials(false)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor).addPathPatterns("/api/wrapped/**");
    }
}
