/**********************************************************************************
 *
 * Copyright (c) 2015 The Sakai Foundation
 *
 * Original developers:
 *
 *   New York University
 *   Payten Giles
 *   Mark Triggs
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.pasystem.impl;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.Template;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.flywaydb.core.Flyway;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.sakaiproject.authz.cover.FunctionManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.pasystem.api.Banner;
import org.sakaiproject.pasystem.api.Banners;
import org.sakaiproject.pasystem.api.I18n;
import org.sakaiproject.pasystem.api.PASystem;
import org.sakaiproject.pasystem.api.Popup;
import org.sakaiproject.pasystem.api.Popups;
import org.sakaiproject.pasystem.impl.banners.BannerStorage;
import org.sakaiproject.pasystem.impl.common.JSONI18n;
import org.sakaiproject.pasystem.impl.popups.PopupForUser;
import org.sakaiproject.pasystem.impl.popups.PopupStorage;
import org.sakaiproject.portal.util.PortalUtils;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.PreferencesService;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The implementation of the PA System service.  Provides system initialization
 * and access to the PA System banner and popup sub-services.
 */
class PASystemImpl implements PASystem {

    private static final Logger LOG = LoggerFactory.getLogger(PASystemImpl.class);

    private static final String POPUP_SCREEN_SHOWN = "pasystem.popup.screen.shown";

    private Map<String, I18n> i18nStore;

    @Override
    public void init() {
        if (ServerConfigurationService.getBoolean("auto.ddl", false) || ServerConfigurationService.getBoolean("pasystem.auto.ddl", false)) {
            runDBMigration(ServerConfigurationService.getString("vendor@org.sakaiproject.db.api.SqlService"));
        }

        FunctionManager.registerFunction("pasystem.manage");

        i18nStore = new ConcurrentHashMap<String, I18n>(1);
    }

    @Override
    public void destroy() {
    }

    @Override
    public String getFooter() {
        StringBuilder result = new StringBuilder();

        Locale userLocale = PreferencesService.getLocale(SessionManager.getCurrentSessionUserId());
        I18n i18n = getI18n(this.getClass().getClassLoader(), "i18n", userLocale);
        Handlebars handlebars = loadHandleBars(i18n);

        Session session = SessionManager.getCurrentSession();

        Map<String, Object> context = new HashMap<String, Object>();

        try {
            Template template = handlebars.compile("templates/shared_footer");

            context.put("portalCDNQuery", PortalUtils.getCDNQuery());
            context.put("sakai_csrf_token", session.getAttribute("sakai.csrf.token"));

            result.append(template.apply(context));
        } catch (IOException e) {
            LOG.warn("IOException while getting footer", e);
            return "";
        }

        result.append(getBannersFooter(handlebars, context));
        result.append(getPopupsFooter(handlebars, context));
        result.append(getTimezoneCheckFooter(handlebars, context));

        return result.toString();
    }

    @Override
    public Banners getBanners() {
        return new BannerStorage();
    }

    @Override
    public Popups getPopups() {
        return new PopupStorage();
    }

    @Override
    public I18n getI18n(ClassLoader loader, String resourceBase, Locale locale) {
        String language = "en";

        if (locale != null) {
            language = locale.getLanguage();
        }

        String i18nKey = resourceBase + "::" + language + "::" + loader.hashCode();

        if (!i18nStore.containsKey(i18nKey)) {
            i18nStore.put(i18nKey, new JSONI18n(loader, resourceBase, locale));
        }

        return i18nStore.get(i18nKey);
    }

    private Handlebars loadHandleBars(final I18n i18n) {
        Handlebars handlebars = new Handlebars();

        handlebars.registerHelper("t", new Helper<Object>() {
            @Override
            public CharSequence apply(final Object context, final Options options) {
                String key = options.param(0);
                return i18n.t(key);
            }
        });

        return handlebars;
    }

    private void runDBMigration(final String vendor) {

        // We run this in a separate thread because Flyway will look to the
        // current thread's class loader for its files.  The system StartStop
        // thread has an unpredictable classloader state so we provide our own.

        Thread migrationRunner = new Thread() {
            @Override
            public void run() {
                Flyway flyway = new Flyway();

                flyway.setLocations("db/migration/" + vendor);
                flyway.setBaselineOnMigrate(true);
                flyway.setTable("pasystem_schema_version");

                flyway.setDataSource(ServerConfigurationService.getString("url@javax.sql.BaseDataSource"),
                        ServerConfigurationService.getString("username@javax.sql.BaseDataSource"),
                        ServerConfigurationService.getString("password@javax.sql.BaseDataSource"));

                flyway.migrate();
            }
        };

        migrationRunner.setContextClassLoader(PASystemImpl.class.getClassLoader());
        migrationRunner.start();

        try {
            migrationRunner.join();
        } catch (InterruptedException e) {
            LOG.warn("Interruption during DB migration", e);
        }
    }

    private String getBannersFooter(Handlebars handlebars, Map<String, Object> context) {
        try {
            Template template = handlebars.compile("templates/banner_footer");

            context.put("bannerJSON", getActiveBannersJSON());

            return template.apply(context);
        } catch (IOException e) {
            LOG.warn("IOException while getting banners footer", e);
            return "";
        }
    }

    private String getActiveBannersJSON() {
        JSONArray banners = new JSONArray();
        String serverId = ServerConfigurationService.getString("serverId", "localhost");

        User currentUser = UserDirectoryService.getCurrentUser();

        if (currentUser != null && currentUser.getEid() != null) {
            for (Banner banner : getBanners().getRelevantBanners(serverId, currentUser.getEid())) {
                JSONObject bannerData = new JSONObject();
                bannerData.put("id", banner.getUuid());
                bannerData.put("message", banner.getMessage());
                bannerData.put("dismissible", banner.isDismissible());
                bannerData.put("dismissed", banner.isDismissed());
                bannerData.put("type", banner.getType());
                banners.add(bannerData);
            }
        }

        return banners.toJSONString();
    }

    private String getPopupsFooter(Handlebars handlebars, Map<String, Object> context) {
        Session session = SessionManager.getCurrentSession();
        User currentUser = UserDirectoryService.getCurrentUser();

        if (currentUser == null) {
            return "";
        }

        if (session.getAttribute(POPUP_SCREEN_SHOWN) == null) {
            Popup popup = new PopupForUser(currentUser).getPopup();
            if (popup.isActiveNow()) {
                context.put("popupTemplate", popup.getTemplate());
                context.put("popupUuid", popup.getUuid());
                context.put("popup", true);

                if (currentUser.getEid() != null) {
                    // Delivered!
                    session.setAttribute(POPUP_SCREEN_SHOWN, "true");
                }
            }
        }

        try {
            Template template = handlebars.compile("templates/popup_footer");
            return template.apply(context);
        } catch (IOException e) {
            LOG.warn("IOException while getting popups footer", e);
            return "";
        }
    }

    private String getTimezoneCheckFooter(Handlebars handlebars, Map<String, Object> context) {
        if (ServerConfigurationService.getBoolean("pasystem.timezone-check", false)) {

            try {
                Template template = handlebars.compile("templates/timezone_footer");

                return template.apply(context);
            } catch (IOException e) {
                LOG.warn("Timezone footer failed", e);
                return "";
            }
        } else {
            return "";
        }
    }
}
