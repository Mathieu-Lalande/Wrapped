package dev.wrapped.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Configuration(proxyBeanMethods = false)
public class GitHubClientConfig {

    @Bean
    RestClient gitHubRestClient(RestClient.Builder builder, WrappedProperties properties) {
        WrappedProperties.GitHub github = properties.github();
        if (!StringUtils.hasText(github.token())) {
            throw new IllegalStateException(
                    "La variable d'environnement GITHUB_TOKEN est absente : voir README / .env.example");
        }
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(github.connectTimeout());
        requestFactory.setReadTimeout(github.readTimeout());

        return builder
                .baseUrl(github.baseUrl().toString())
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + github.token())
                .defaultHeader(HttpHeaders.USER_AGENT, "github-wrapped")
                .build();
    }
}
