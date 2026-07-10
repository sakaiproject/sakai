package org.sakaiproject.sitestats.tool.mvc;

import java.time.ZoneId;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.portal.util.PortalUtils;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.util.api.LocaleService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

@ControllerAdvice(basePackages = "org.sakaiproject.sitestats.tool.mvc")
public class SiteStatsControllerAdvice {

    private final LocaleService localeService;
    private final UserTimeService userTimeService;

    public SiteStatsControllerAdvice(LocaleService localeService,
            @Qualifier("org.sakaiproject.time.api.UserTimeService") UserTimeService userTimeService) {
        this.localeService = localeService;
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

    @ModelAttribute("cdnQuery")
    public String cdnQuery() {
        return PortalUtils.getCDNQuery();
    }
}
