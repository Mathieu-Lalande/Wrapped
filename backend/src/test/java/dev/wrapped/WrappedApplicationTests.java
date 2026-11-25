package dev.wrapped;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import dev.wrapped.github.GitHubClient;
import dev.wrapped.github.dto.GitHubContributions;
import dev.wrapped.service.WrappedService;

/**
 * Charge le contexte complet ; le client GitHub est remplacé par un mock (aucun appel réseau).
 */
@SpringBootTest(properties = "wrapped.github.token=test-token")
class WrappedApplicationTests {

    @MockitoBean
    private GitHubClient gitHubClient;

    @Autowired
    private WrappedService wrappedService;

    @Test
    void resultsAreCachedPerCaseInsensitiveLoginAndYear() {
        when(gitHubClient.fetchContributions(any(), any()))
                .thenReturn(new GitHubContributions("octocat", null, 0, 0, 0, List.of(), List.of()));

        var first = wrappedService.getWrapped("octocat", 2024);
        var second = wrappedService.getWrapped("OctoCat", 2024);

        assertThat(second).isSameAs(first);
        verify(gitHubClient, times(1)).fetchContributions(eq("octocat"), any());
    }
}
