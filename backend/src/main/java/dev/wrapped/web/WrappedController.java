package dev.wrapped.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.wrapped.domain.WrappedResponse;
import dev.wrapped.service.WrappedService;
import dev.wrapped.web.validation.ValidYear;
import jakarta.validation.constraints.Pattern;

/**
 * Controller mince : validation des entrées (validation de méthode intégrée à Spring MVC 6.1+,
 * déclenchée par les contraintes sur les paramètres) puis délégation au service.
 */
@RestController
@RequestMapping("/api/wrapped")
public class WrappedController {

    static final String LOGIN_REGEX = "^[a-zA-Z0-9-]{1,39}$";

    private final WrappedService wrappedService;

    public WrappedController(WrappedService wrappedService) {
        this.wrappedService = wrappedService;
    }

    @GetMapping("/{login}")
    public WrappedResponse getWrapped(
            @PathVariable @Pattern(regexp = LOGIN_REGEX, message = "doit être un login GitHub valide") String login,
            @RequestParam(required = false) @ValidYear Integer year) {
        return wrappedService.getWrapped(login, wrappedService.resolveYear(year));
    }
}
