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

package org.sakaiproject.pasystem.impl.rest;

import java.io.ByteArrayInputStream;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;

import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.pasystem.api.Banner;
import org.sakaiproject.pasystem.api.Errors;
import org.sakaiproject.pasystem.api.PASystem;
import org.sakaiproject.pasystem.api.PASystemException;
import org.sakaiproject.pasystem.api.Popup;
import org.sakaiproject.pasystem.api.TemplateStream;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.api.User;

/**
 * Web services for managing popups and banners.  Intended for administrator use.
 */
@Slf4j
public class PASystemAdminEntityProvider implements EntityProvider, AutoRegisterEntityProvider, ActionsExecutable, Outputable, Describeable {

    private static final String ADMIN_SITE_REALM = "/site/!admin";
    private static final String SAKAI_SESSION_TOKEN_PROPERTY = "sakai.pasystem-admin.token";
    private static final String REQUEST_SESSION_PARAMETER = "session";

    protected DeveloperHelperService developerHelperService;
    private EntityProviderManager entityProviderManager;

    @Override
    public String[] getHandledOutputFormats() {
        return new String[] { Formats.JSON };
    }

    @Override
    public String getEntityPrefix() {
        return "pasystem-admin";
    }

    /**
     * Return a PA System service token to be passed with subsequent requests.
     */
    @EntityCustomAction(action = "startSession", viewKey = EntityView.VIEW_NEW)
    public String startSession(EntityView view, Map<String, Object> params) {
        try {
            assertPermission();

            JSONObject result = new JSONObject();
            String newSessionId = mintSessionId();
            result.put(REQUEST_SESSION_PARAMETER, newSessionId);

            SessionManager.getCurrentSession().setAttribute(SAKAI_SESSION_TOKEN_PROPERTY, newSessionId);

            return result.toJSONString();
        } catch (Exception e) {
            return respondWithError(e);
        }
    }

    @EntityCustomAction(action = "createPopup", viewKey = EntityView.VIEW_NEW)
    public String createPopup(EntityView view, Map<String, Object> params) {
        try {
            assertSession(params);

            WrappedParams wp = new WrappedParams(params);

            Popup popup = Popup.create(wp.getString("descriptor"),
                    wp.getEpochMS("start_time"),
                    wp.getEpochMS("end_time"),
                    wp.getBoolean("is_open_campaign"));

            Errors errors = popup.validate();

            if (errors.hasErrors()) {
                return respondWithError(errors);
            }

            final String template = wp.getString("template");
            TemplateStream templateStream = new TemplateStream(new ByteArrayInputStream(template.getBytes()),
                    template.length());

            String uuid = paSystem().getPopups().createCampaign(popup,
                    templateStream,
                    Optional.ofNullable(wp.getCommaList("assign_to_users")));

            JSONObject result = new JSONObject();
            result.put("status", "OK");
            result.put("created_id", uuid);
            return result.toJSONString();
        } catch (Exception e) {
            return respondWithError(e);
        }
    }

    @EntityCustomAction(action = "deletePopup", viewKey = EntityView.VIEW_NEW)
    public String deletePopup(EntityView view, Map<String, Object> params) {
        try {
            assertSession(params);

            WrappedParams wp = new WrappedParams(params);

            String uuid = wp.getString("id");
            paSystem().getPopups().deleteCampaign(uuid);

            JSONObject result = new JSONObject();
            result.put("status", "OK");
            result.put("id", uuid);
            return result.toJSONString();
        } catch (Exception e) {
            return respondWithError(e);
        }
    }

    @EntityCustomAction(action = "createBanner", viewKey = EntityView.VIEW_NEW)
    public String createBanner(EntityView view, Map<String, Object> params) {
        try {
            assertSession(params);

            WrappedParams wp = new WrappedParams(params);

            Banner banner = new Banner(wp.getString("message"),
                    wp.getString("hosts", ""),
                    wp.getBoolean("is_active"),
                    wp.getEpochMS("start_time"),
                    wp.getEpochMS("end_time"),
                    wp.getString("type"));

            Errors errors = banner.validate();

            if (errors.hasErrors()) {
                return respondWithError(errors);
            }

            String uuid = paSystem().getBanners().createBanner(banner);

            JSONObject result = new JSONObject();
            result.put("status", "OK");
            result.put("created_id", uuid);
            return result.toJSONString();
        } catch (Exception e) {
            return respondWithError(e);
        }
    }

    @EntityCustomAction(action = "deleteBanner", viewKey = EntityView.VIEW_NEW)
    public String deleteBanner(EntityView view, Map<String, Object> params) {
        try {
            assertSession(params);

            WrappedParams wp = new WrappedParams(params);

            String uuid = wp.getString("id");
            paSystem().getBanners().deleteBanner(uuid);

            JSONObject result = new JSONObject();
            result.put("status", "OK");
            result.put("id", uuid);
            return result.toJSONString();
        } catch (Exception e) {
            return respondWithError(e);
        }
    }

    private String respondWithError(Exception e) {
        JSONObject result = new JSONObject();
        result.put("status", "ERROR");
        result.put("message", e.getMessage());

        log.error("Caught an error while handling a request", e);

        return result.toJSONString();
    }

    private String respondWithError(Errors e) {
        JSONObject result = new JSONObject();
        result.put("status", "ERROR");
        result.put("message", e.toMap());

        return result.toJSONString();
    }

    private void assertSession(Map<String, Object> params) {
        assertPermission();

        String tokenFromUser = (String)params.get(REQUEST_SESSION_PARAMETER);
        String tokenFromSession = (String)SessionManager.getCurrentSession().getAttribute(SAKAI_SESSION_TOKEN_PROPERTY);

        if (tokenFromSession == null || tokenFromUser == null || !tokenFromSession.equals(tokenFromUser)) {
            log.error("assertSession failed for user " + SessionManager.getCurrentSessionUserId());
            throw new PASystemException("Access denied");
        }
    }

    private void assertPermission() {
        if (!SecurityService.unlock("pasystem.manage", ADMIN_SITE_REALM)) {
            log.error("assertPermission denied access to user " + SessionManager.getCurrentSessionUserId());
            throw new PASystemException("Access denied");
        }
    }

    private PASystem paSystem() {
        return (PASystem) ComponentManager.get(PASystem.class);
    }

    private String mintSessionId() {
        byte[] b = new byte[32];

        try {
            SecureRandom.getInstance("SHA1PRNG").nextBytes(b);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Couldn't generate a session ID", e);
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            sb.append(String.format("%02x", b[i]));
        }

        return sb.toString();
    }

    public void setEntityProviderManager(EntityProviderManager entityProviderManager) {
        this.entityProviderManager = entityProviderManager;
    }

    public void setDeveloperHelperService(DeveloperHelperService developerHelperService) {
        this.developerHelperService = developerHelperService;
    }

    private class WrappedParams {

        private final Map<String, Object> params;

        public WrappedParams(Map<String, Object> params) {
            this.params = params;
        }

        public String getString(String name) {
            String result = (String)params.get(name);

            if (result == null) {
                throw new IllegalArgumentException("Parameter " + name + " cannot be null.");
            }

            return result;
        }

        public String getString(String name, String defaultValue) {
            if (containsKey(name)) {
                return getString(name);
            } else {
                return defaultValue;
            }
        }

        public long getEpochMS(String name) {
            return Long.valueOf(getString(name));
        }

        public boolean getBoolean(String name) {
            return Boolean.valueOf(getString(name));
        }

        public List<String> getCommaList(String name) {
            if (containsKey("assign_to_users")) {
                return Arrays.asList(getString("assign_to_users").split("[, ]+"));
            } else {
                return null;
            }
        }

        public boolean containsKey(String name) {
            return this.params.containsKey(name);
        }
    }
}
