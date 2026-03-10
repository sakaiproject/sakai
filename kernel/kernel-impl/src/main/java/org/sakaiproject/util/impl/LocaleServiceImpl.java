/**
 * Copyright (c) 2003-2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.util.impl;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.util.api.LocaleService;

@Slf4j
@Setter
public class LocaleServiceImpl implements LocaleService {

    private SiteService siteService;
    private PreferencesService preferencesService;
    private ToolManager toolManager;
    private SessionManager sessionManager;

    public void init() {
        Objects.requireNonNull(siteService);
        Objects.requireNonNull(preferencesService);
        Objects.requireNonNull(toolManager);
        Objects.requireNonNull(sessionManager);
    }

    @Override
    public Locale getLocaleForCurrentSiteAndUser() {
        String siteId = null;

        try {
            Placement currentPlacement = toolManager.getCurrentPlacement();
            siteId = currentPlacement != null ? currentPlacement.getContext() : null;
        } catch (Exception e) {
            log.debug("Unable to resolve current placement while resolving locale", e);
        }

        String userId = sessionManager.getCurrentSessionUserId();
        return getLocaleForSiteAndUser(siteId, userId);
    }

    @Override
    public Locale getLocaleForSiteAndUser(String siteId, String userId) {
        if (StringUtils.isNotBlank(siteId)) {
            try {
                Optional<Locale> siteLocale = siteService.getSiteLocale(siteId);
                if (siteLocale != null && siteLocale.isPresent()) {
                    return siteLocale.get();
                }
            } catch (Exception e) {
                log.debug("Unable to resolve site locale for site {}", siteId, e);
            }
        }

        if (StringUtils.isNotBlank(userId)) {
            try {
                Locale locale = preferencesService.getLocale(userId);
                if (locale != null) {
                    return locale;
                }
            } catch (Exception e) {
                log.debug("Unable to resolve user locale for user {}", userId, e);
            }
        }

        return defaultLocale();
    }

    private Locale defaultLocale() {
        return Optional.ofNullable(Locale.getDefault()).orElse(Locale.ENGLISH);
    }
}
