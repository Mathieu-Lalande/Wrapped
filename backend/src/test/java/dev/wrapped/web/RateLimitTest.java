package dev.wrapped.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import dev.wrapped.domain.Persona;
import dev.wrapped.domain.PersonaType;
import dev.wrapped.domain.Streak;
import dev.wrapped.domain.Totals;
import dev.wrapped.domain.WrappedResponse;
import dev.wrapped.service.WrappedService;

@WebMvcTest(controllers = WrappedController.class, properties = {
        "wrapped.rate-limit.max-requests=2",
        "wrapped.rate-limit.window=1h"
})
class RateLimitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WrappedService wrappedService;

    @Test
    void returns429WithRetryAfterAndCorsHeadersOnceLimitIsReached() throws Exception {
        when(wrappedService.resolveYear(any())).thenReturn(2025);
        when(wrappedService.getWrapped(anyString(), anyInt())).thenReturn(new WrappedResponse(
                "octocat", null, 2025, new Totals(0, 0, 0, 0), List.of(), Streak.none(), Streak.none(),
                null, null, List.of(), List.of(), Persona.of(PersonaType.HIBERNATING)));

        String origin = "http://localhost:5173";
        mockMvc.perform(get("/api/wrapped/octocat").header(HttpHeaders.ORIGIN, origin)).andExpect(status().isOk());
        mockMvc.perform(get("/api/wrapped/octocat").header(HttpHeaders.ORIGIN, origin)).andExpect(status().isOk());

        mockMvc.perform(get("/api/wrapped/octocat").header(HttpHeaders.ORIGIN, origin))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists(HttpHeaders.RETRY_AFTER))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin))
                .andExpect(jsonPath("$.status").value(429));
    }
}
