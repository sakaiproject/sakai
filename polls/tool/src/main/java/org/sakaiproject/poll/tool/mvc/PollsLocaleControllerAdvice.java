package org.sakaiproject.poll.tool.mvc;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

@ControllerAdvice(basePackages = "org.sakaiproject.poll.tool.mvc")
@RequiredArgsConstructor
public class PollsLocaleControllerAdvice {

    private final PollsLocaleService pollsLocaleService;

    @ModelAttribute("locale")
    public Locale locale(HttpServletRequest request, HttpServletResponse response) {
        Locale locale = pollsLocaleService.getLocaleForCurrentSiteAndUser();
        LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
        if (localeResolver != null) {
            localeResolver.setLocale(request, response, locale);
        }
        return locale;
    }
}
