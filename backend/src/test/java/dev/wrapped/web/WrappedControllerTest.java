package dev.wrapped.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import dev.wrapped.domain.DayStat;
import dev.wrapped.domain.Persona;
import dev.wrapped.domain.PersonaType;
import dev.wrapped.domain.Streak;
import dev.wrapped.domain.Totals;
import dev.wrapped.domain.WrappedResponse;
import dev.wrapped.github.GitHubUnavailableException;
import dev.wrapped.github.UserNotFoundException;
import dev.wrapped.service.WrappedService;

@WebMvcTest(controllers = WrappedController.class)
class WrappedControllerTest {

    private static final String ALLOWED_ORIGIN = "http://localhost:5173";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WrappedService wrappedService;

    @BeforeEach
    void setUp() {
        when(wrappedService.resolveYear(any())).thenAnswer(inv -> {
            Integer year = inv.getArgument(0);
            return year != null ? year : Year.now(ZoneOffset.UTC).getValue();
        });
    }

    private static WrappedResponse sample(String login, int year) {
        return new WrappedResponse(login, "Octo", year, new Totals(3, 2, 1, 0),
                List.of(new DayStat(LocalDate.of(year, 1, 1), 3)),
                new Streak(1, LocalDate.of(year, 1, 1), LocalDate.of(year, 1, 1)), Streak.none(),
                null, null, List.of(), List.of(), Persona.of(PersonaType.STEADY_BUILDER));
    }

    @Test
    void returnsWrappedForValidRequest() throws Exception {
        when(wrappedService.getWrapped("octocat", 2025)).thenReturn(sample("octocat", 2025));

        mockMvc.perform(get("/api/wrapped/octocat").param("year", "2025"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.login").value("octocat"))
                .andExpect(jsonPath("$.totals.contributions").value(3))
                .andExpect(jsonPath("$.calendar[0].date").value("2025-01-01"))
                .andExpect(jsonPath("$.persona.code").value("STEADY_BUILDER"));
    }

    @Test
    void defaultsToCurrentYear() throws Exception {
        int currentYear = Year.now(ZoneOffset.UTC).getValue();
        when(wrappedService.getWrapped("octocat", currentYear)).thenReturn(sample("octocat", currentYear));

        mockMvc.perform(get("/api/wrapped/octocat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(currentYear));
    }

    @ParameterizedTest
    @ValueSource(strings = {"bad_login", "a.b", "1234567890123456789012345678901234567890", "%3Cscript%3E"})
    void rejectsInvalidLogin(String login) throws Exception {
        mockMvc.perform(get("/api/wrapped/" + login).param("year", "2025"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0].parameter").value("login"));

        verify(wrappedService, never()).getWrapped(anyString(), anyInt());
    }

    @ParameterizedTest
    @ValueSource(strings = {"2007", "2999", "-1"})
    void rejectsYearOutOfRange(String year) throws Exception {
        mockMvc.perform(get("/api/wrapped/octocat").param("year", year))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].parameter").value("year"));

        verify(wrappedService, never()).getWrapped(anyString(), anyInt());
    }

    @Test
    void rejectsNonNumericYearWithoutEchoingInput() throws Exception {
        mockMvc.perform(get("/api/wrapped/octocat").param("year", "abc<script>"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(content().string(not(containsString("<script>"))));
    }

    @Test
    void unknownUserReturnsCleanNotFound() throws Exception {
        when(wrappedService.getWrapped(eq("ghost"), anyInt())).thenThrow(new UserNotFoundException("ghost"));

        mockMvc.perform(get("/api/wrapped/ghost").param("year", "2025"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Utilisateur introuvable"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(content().string(not(containsString("GitHub user not found"))));
    }

    @Test
    void gitHubFailureDoesNotLeakDetails() throws Exception {
        when(wrappedService.getWrapped(eq("octocat"), anyInt()))
                .thenThrow(new GitHubUnavailableException("GitHub HTTP 401 Bad credentials"));

        mockMvc.perform(get("/api/wrapped/octocat").param("year", "2025"))
                .andExpect(status().isBadGateway())
                .andExpect(content().string(not(containsString("401"))))
                .andExpect(content().string(not(containsString("credentials"))));
    }

    @Test
    void unexpectedErrorReturnsGeneric500() throws Exception {
        when(wrappedService.getWrapped(eq("octocat"), anyInt()))
                .thenThrow(new IllegalStateException("NullPointer in secret.internal.Class"));

        mockMvc.perform(get("/api/wrapped/octocat").param("year", "2025"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(not(containsString("secret.internal"))));
    }

    @Test
    void corsAllowsConfiguredOrigin() throws Exception {
        when(wrappedService.getWrapped("octocat", 2025)).thenReturn(sample("octocat", 2025));

        mockMvc.perform(get("/api/wrapped/octocat").param("year", "2025").header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN));
    }

    @Test
    void corsRejectsOtherOrigins() throws Exception {
        mockMvc.perform(options("/api/wrapped/octocat")
                        .header(HttpHeaders.ORIGIN, "https://evil.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden());
    }
}
