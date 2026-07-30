package org.sakaiproject.sitestats.tool.mvc;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.util.api.LocaleService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;

@ControllerAdvice(basePackages = "org.sakaiproject.sitestats.tool.mvc")
@Slf4j
public class SiteStatsControllerAdvice {

    private final LocaleService localeService;
    private final MessageSource messageSource;
    private final SakaiCsrfTokens csrfTokens;
    private final ServerConfigurationService serverConfigurationService;
    private final UserTimeService userTimeService;

    public SiteStatsControllerAdvice(LocaleService localeService, MessageSource messageSource,
            SakaiCsrfTokens csrfTokens, ServerConfigurationService serverConfigurationService,
            @Qualifier("org.sakaiproject.time.api.UserTimeService") UserTimeService userTimeService) {
        this.localeService = localeService;
        this.messageSource = messageSource;
        this.csrfTokens = csrfTokens;
        this.serverConfigurationService = serverConfigurationService;
        this.userTimeService = userTimeService;
    }

    @ModelAttribute("locale")
    public Locale locale(HttpServletRequest request, HttpServletResponse response) {
        Locale locale = localeService.getLocaleForCurrentSiteAndUser();
        LocaleResolver resolver = RequestContextUtils.getLocaleResolver(request);
        if (resolver != null) {
            resolver.setLocale(request, response, locale);
        }
        return locale;
    }

    @ModelAttribute("timeZone")
    public ZoneId timeZone() {
        return userTimeService.getLocalTimeZone().toZoneId();
    }

    @ModelAttribute("sakaiHtmlHead")
    public Object sakaiHtmlHead(HttpServletRequest request) {
        return request.getAttribute("sakai.html.head");
    }

    @ModelAttribute("sakaiCsrfToken")
    public String sakaiCsrfToken() {
        return csrfTokens.currentToken();
    }

    @ExceptionHandler(InvalidSakaiCsrfTokenException.class)
    public ModelAndView invalidCsrfToken(HttpServletRequest request) {
        Locale locale = localeService.getLocaleForCurrentSiteAndUser();
        return errorResponse(request, locale, HttpStatus.FORBIDDEN,
                "sitestats_error_request_title", "java.badcsrftoken",
                "The request could not be verified. Refresh the tool and try again.");
    }

    @ExceptionHandler(InvalidReportConfigurationException.class)
    public ModelAndView invalidReportConfiguration(
            HttpServletRequest request, InvalidReportConfigurationException exception) {
        Locale locale = localeService.getLocaleForCurrentSiteAndUser();
        return errorResponse(request, locale, HttpStatus.BAD_REQUEST,
                "sitestats_error_report_configuration_title", exception.getMessageCode(),
                "The selected report configuration is not valid.");
    }

    @ExceptionHandler(SiteStatsOperationException.class)
    public ModelAndView operationFailure(HttpServletRequest request, SiteStatsOperationException exception) {
        log.warn("SiteStats operation failed: {}", exception.getMessage());
        Locale locale = localeService.getLocaleForCurrentSiteAndUser();
        return errorResponse(request, locale, HttpStatus.INTERNAL_SERVER_ERROR,
                "sitestats_error_operation_title", exception.getMessageCode(),
                "The operation could not be completed. Try again.");
    }

    @ModelAttribute("cdnQuery")
    public String cdnQuery() {
        long expirySeconds = serverConfigurationService.getInt("portal.cdn.expire", 0);
        String defaultVersion = serverConfigurationService.getString("version.service", "0");
        String version = serverConfigurationService.getString("portal.cdn.version", defaultVersion);
        StringBuilder query = new StringBuilder("?version=").append(version);
        if (expirySeconds > 0) {
            query.append("&expire=").append(Instant.now().getEpochSecond() / expirySeconds);
        }
        return query.toString();
    }

    private ModelAndView errorResponse(HttpServletRequest request, Locale locale, HttpStatus status,
            String titleCode, String messageCode, String defaultMessage) {
        ModelAndView response = new ModelAndView("error");
        response.setStatus(status);
        response.addObject("locale", locale);
        response.addObject("sakaiHtmlHead", request.getAttribute("sakai.html.head"));
        response.addObject("errorTitle", messageSource.getMessage(titleCode, null, titleCode, locale));
        response.addObject("error", messageSource.getMessage(messageCode, null, defaultMessage, locale));
        return response;
    }
}
