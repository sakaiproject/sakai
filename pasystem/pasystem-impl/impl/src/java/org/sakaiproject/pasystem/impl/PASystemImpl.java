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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.cache.TemplateCache;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import org.sakaiproject.authz.cover.FunctionManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.db.cover.SqlService;
import org.sakaiproject.pasystem.api.Banner;
import org.sakaiproject.pasystem.api.Banners;
import org.sakaiproject.pasystem.api.I18n;
import org.sakaiproject.pasystem.api.PASystem;
import org.sakaiproject.pasystem.api.PASystemException;
import org.sakaiproject.pasystem.api.MissingUuidException;
import org.sakaiproject.pasystem.api.Popup;
import org.sakaiproject.pasystem.api.Popups;
import org.sakaiproject.pasystem.impl.banners.BannerStorage;
import org.sakaiproject.pasystem.impl.common.SakaiI18n;
import org.sakaiproject.pasystem.impl.handlebars.ForeverTemplateCache;
import org.sakaiproject.pasystem.impl.popups.PopupForUser;
import org.sakaiproject.pasystem.impl.popups.PopupStorage;
import org.sakaiproject.portal.util.PortalUtils;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;

/**
 * The implementation of the PA System service.  Provides system initialization
 * and access to the PA System banner and popup sub-services.
 */
@Slf4j
class PASystemImpl implements PASystem {

    private static final String POPUP_SCREEN_SHOWN = "pasystem.popup.screen.shown";

    private TemplateCache cache;

    @Override
    public void init() {
        if (ServerConfigurationService.getBoolean("auto.ddl", false) || ServerConfigurationService.getBoolean("pasystem.auto.ddl", false)) {
            runDBMigration(ServerConfigurationService.getString("vendor@org.sakaiproject.db.api.SqlService"));
        }

        // No need to parse each time.
        cache = new ForeverTemplateCache();

        FunctionManager.registerFunction("pasystem.manage");
    }

    @Override
    public void destroy() {
    }

    @Override
    public String getFooter() {
        StringBuilder result = new StringBuilder();

        I18n i18n = getI18n(this.getClass().getClassLoader(), "org.sakaiproject.pasystem.impl.i18n.pasystem");
        Handlebars handlebars = loadHandleBars(i18n);

        Session session = SessionManager.getCurrentSession();

        Map<String, Object> context = new HashMap<String, Object>();

        try {
            Template template = handlebars.compile("templates/shared_footer");

            context.put("portalCDNQuery", PortalUtils.getCDNQuery());
            context.put("sakai_csrf_token", session.getAttribute("sakai.csrf.token"));

            result.append(template.apply(context));
        } catch (IOException e) {
            log.warn("IOException while getting footer", e);
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
    public I18n getI18n(ClassLoader loader, String resourceBase) {
        return new SakaiI18n(loader, resourceBase);
    }

    private Handlebars loadHandleBars(final I18n i18n) {
        Handlebars handlebars = new Handlebars()
                .with(cache);

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
        String migrationFile = "db/migration/" + vendor + ".sql";
        InputStream is = PASystemImpl.class.getClassLoader().getResourceAsStream(migrationFile);

        if (is == null) {
            throw new PASystemException("Failed to find migration file: " + migrationFile);
        }

        InputStreamReader migrationInput = new InputStreamReader(is);

        try {
            Connection db = SqlService.borrowConnection();

            try {
                for (String sql : parseMigrationFile(migrationInput)) {
                    try {
                        PreparedStatement ps = db.prepareStatement(sql);
                        ps.execute();
                        ps.close();
                    } catch (SQLException e) {
                        log.warn("runDBMigration: " + e + "(sql: " + sql + ")");
                    }
                }
            } catch (IOException e) {
                throw new PASystemException("Failed to read migration file: " + migrationFile, e);
            } finally {
                SqlService.returnConnection(db);

                try {
                    migrationInput.close();
                } catch (IOException e) {}
            }
        } catch (SQLException e) {
            throw new PASystemException("Database migration failed", e);
        }
    }

    private String[] parseMigrationFile(InputStreamReader migrationInput) throws IOException {
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[4096];

        int len;
        while ((len = migrationInput.read(buf)) > 0) {
            sb.append(buf, 0, len);
        }

        return sb.toString().replace("\n", " ").split(";\\s*");
    }

    private String getBannersFooter(Handlebars handlebars, Map<String, Object> context) {
        try {
            Template template = handlebars.compile("templates/banner_footer");

            context.put("bannerJSON", getActiveBannersJSON());

            return template.apply(context);
        } catch (IOException e) {
            log.warn("IOException while getting banners footer", e);
            return "";
        }
    }

    private String getActiveBannersJSON() {
        JSONArray banners = new JSONArray();
        String serverId = ServerConfigurationService.getString("serverId", "localhost");

        User currentUser = UserDirectoryService.getCurrentUser();

        if (currentUser != null && currentUser.getId() != null && !"".equals(currentUser.getId())) {
            for (Banner banner : getBanners().getRelevantBanners(serverId, currentUser.getId())) {
                try {
                    JSONObject bannerData = new JSONObject();
                    bannerData.put("id", banner.getUuid());
                    bannerData.put("message", banner.getMessage());
                    bannerData.put("dismissible", banner.isDismissible());
                    bannerData.put("dismissed", banner.isDismissed());
                    bannerData.put("type", banner.getType());
                    banners.add(bannerData);
                } catch (Exception e) {
                    log.warn("Error processing banner: " + banner, e);
                }
            }
        }

        return banners.toJSONString();
    }

    private String getPopupsFooter(Handlebars handlebars, Map<String, Object> context) {
        Session session = SessionManager.getCurrentSession();
        User currentUser = UserDirectoryService.getCurrentUser();

        if (currentUser == null || currentUser.getId() == null || "".equals(currentUser.getId())) {
            return "";
        }

        try {
            if (session.getAttribute(POPUP_SCREEN_SHOWN) == null) {
                Popup popup = new PopupForUser(currentUser).getPopup();
                if (popup.isActiveNow()) {
                    context.put("popupTemplate", popup.getTemplate());
                    context.put("popupUuid", popup.getUuid());
                    context.put("popup", true);

                    if (currentUser.getId() != null) {
                        // Delivered!
                        session.setAttribute(POPUP_SCREEN_SHOWN, "true");
                    }
                }
            }


            Template template = handlebars.compile("templates/popup_footer");
            return template.apply(context);
        } catch (IOException | MissingUuidException e) {
            log.warn("IOException while getting popups footer", e);
            return "";
        }
    }

    private String getTimezoneCheckFooter(Handlebars handlebars, Map<String, Object> context) {
        if (ServerConfigurationService.getBoolean("pasystem.timezone-check", true)) {

            try {
                Template template = handlebars.compile("templates/timezone_footer");

                return template.apply(context);
            } catch (IOException e) {
                log.warn("Timezone footer failed", e);
                return "";
            }
        } else {
            return "";
        }
    }
}
