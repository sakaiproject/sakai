package org.sakaiproject.poll.tool.mvc;

import java.util.Locale;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.PreferencesService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PollsLocaleService {

    private final PreferencesService preferencesService;
    private final SiteService siteService;
    private final ToolManager toolManager;
    private final SessionManager sessionManager;

    public Locale getLocaleForCurrentSiteAndUser() {
        Optional<String> currentSiteId = getCurrentSiteId();
        if (currentSiteId.isPresent()) {
            Optional<Locale> siteLocale = siteService.getSiteLocale(currentSiteId.get());
            if (siteLocale.isPresent()) {
                return siteLocale.get();
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

    private Optional<String> getCurrentSiteId() {
        try {
            Placement currentPlacement = toolManager.getCurrentPlacement();
            String siteId = currentPlacement != null ? currentPlacement.getContext() : null;
            if (StringUtils.isBlank(siteId)) {
                return Optional.empty();
            }
            return Optional.of(siteId);
        } catch (Exception e) {
            log.debug("Unable to resolve current site for polls locale handling", e);
            return Optional.empty();
        }
    }
}
