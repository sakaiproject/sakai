package org.sakaiproject.poll.tool.mvc;

import java.util.Locale;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PollsLocaleService {

    private final PreferencesService preferencesService;
    private final ServerConfigurationService serverConfigurationService;
    private final SiteService siteService;
    private final ToolManager toolManager;
    private final SessionManager sessionManager;

    public Locale getLocaleForCurrentSiteAndUser() {
        Optional<Site> currentSite = getCurrentSite();
        if (currentSite.isPresent()) {
            ResourceProperties siteProperties = currentSite.get().getProperties();
            String siteLocale = (String) siteProperties.get(Site.PROP_SITE_LOCALE);
            if (StringUtils.isNotBlank(siteLocale)) {
                Locale locale = serverConfigurationService.getLocaleFromString(siteLocale);
                if (locale != null) {
                    return locale;
                }
            }
        }

        String userId = sessionManager.getCurrentSessionUserId();
        if (StringUtils.isNotBlank(userId)) {
            Locale locale = preferencesService.getLocale(userId);
            if (locale != null) {
                return locale;
            }
        }

        return Locale.getDefault();
    }

    private Optional<Site> getCurrentSite() {
        try {
            String siteId = toolManager.getCurrentPlacement().getContext();
            if (StringUtils.isBlank(siteId)) {
                return Optional.empty();
            }
            return Optional.of(siteService.getSite(siteId));
        } catch (Exception e) {
            log.debug("Unable to resolve current site for polls locale handling", e);
            return Optional.empty();
        }
    }
}
