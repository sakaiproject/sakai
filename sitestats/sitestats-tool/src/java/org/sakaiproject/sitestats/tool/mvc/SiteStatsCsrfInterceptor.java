package org.sakaiproject.sitestats.tool.mvc;

import java.util.Locale;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class SiteStatsCsrfInterceptor implements HandlerInterceptor {

    private static final Set<String> UNSAFE_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");

    private final SakaiCsrfTokens csrfTokens;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!UNSAFE_METHODS.contains(request.getMethod().toUpperCase(Locale.ROOT))) {
            return true;
        }

        if (csrfTokens.matches(request.getParameter(SakaiCsrfTokens.REQUEST_PARAMETER))) {
            return true;
        }

        throw new InvalidSakaiCsrfTokenException();
    }
}
