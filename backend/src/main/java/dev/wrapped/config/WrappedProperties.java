package dev.wrapped.config;

import java.net.URI;
import java.time.Duration;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration applicative typée et immuable (préfixe {@code wrapped}).
 */
@ConfigurationProperties(prefix = "wrapped")
public record WrappedProperties(GitHub github, Cors cors, RateLimit rateLimit) {

    /**
     * @param token token GitHub, injecté via la variable d'environnement {@code GITHUB_TOKEN}
     */
    public record GitHub(URI baseUrl, String token, Duration connectTimeout, Duration readTimeout) {

        @Override
        public String toString() {
            // Ne jamais exposer le token dans les logs.
            return "GitHub[baseUrl=" + baseUrl + ", token=***, connectTimeout=" + connectTimeout
                    + ", readTimeout=" + readTimeout + "]";
        }
    }

    public record Cors(List<String> allowedOrigins) {
    }

    public record RateLimit(int maxRequests, Duration window) {
    }
}
