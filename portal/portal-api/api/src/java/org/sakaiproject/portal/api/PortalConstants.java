/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.portal.api;

public class PortalConstants {

    public static final String PROP_CURRENT_EXPANDED = "currentExpanded";

    public static final String PROP_EXPANDED_SITE = "expandedSite";

    public static final String PROP_SIDEBAR_COLLAPSED = "sidebarCollapsed";

    public static final String PROP_BULLHORN_ALERTS_ENABLED = "portal.bullhorns.enabled";
    public static final String PROP_CONTAINER_LOGIN = "container.login";
    public static final String PROP_COPYRIGHT_TEXT = "bottom.copyrighttext";
    public static final String PROP_DISPLAY_USER_LOGIN = "display.userlogin.info";
    public static final String PROP_FOOTER_URLS = "footerlinks";
    public static final String PROP_GATEWAY_SITE_URL = "gatewaySiteUrl";
    public static final String PROP_GLOBAL_ALERT_MESSAGE = "portal.use.global.alert.message";
    public static final String PROP_GOOGLE_ANALYTICS_ID = "portal.google.universal_analytics_id";
    public static final String PROP_GOOGLE_CONTAINER_ID = "portal.google.tag.manager.container_id";
    public static final String PROP_GOOGLE_GA4_ID = "portal.google.ga4_id";
    public static final String PROP_DISPLAY_HELP_ICON = "display.help.icon";
    public static final String PROP_INCLUDE_EXTRAHEAD = "portal.include.extrahead";
    public static final String PROP_MATHJAX_ENABLED = "portal.mathjax.enabled";
    public static final String PROP_MATHJAX_FORMAT = "mathjax.config.format";
    public static final String PROP_MATHJAX_SRC_PATH = "portal.mathjax.src.path";
    public static final String PROP_PA_SYSTEM_ENABLED = "pasystem.enabled";
    public static final String PROP_PORTAL_CHAT_POLL_INTERVAL = "portal.chat.pollInterval";
    public static final String PROP_PORTAL_CHAT_AVATAR = "portal.neoavatar";
    public static final String PROP_PORTAL_CHAT_VIDEO = "portal.chat.video";
    public static final String PROP_PORTAL_CHAT_VIDEO_TIMEOUT = "portal.chat.video.timeout";
    public static final String PROP_PORTAL_DEFAULT_HANDLER = "portal.handler.default";
    public static final String PROP_PORTAL_DISPLAY_CURRENT_ROLE = "portal.display.current.role";
    public static final String PROP_PORTAL_FAV_ICON = "portal.favicon.url";
    public static final String PROP_PORTAL_LOGIN_ICON = "login.icon";
    public static final String PROP_PORTAL_LOGIN_TEXT = "login.text";
    public static final String PROP_PORTAL_LOGIN_URL = "login.url";
    public static final String PROP_PORTAL_LOGOUT_CONFIRM = "portal.logout.confirmation";
    public static final String PROP_PORTAL_LOGOUT_ICON = "logout.icon";
    public static final String PROP_PORTAL_LOGOUT_TEXT = "logout.text";
    public static final String PROP_PORTAL_PATH = "portalPath";
    public static final String PROP_PORTAL_THEMES = "portal.themes";
    public static final String PROP_PORTAL_THEMES_AUTO_DARK = "portal.themes.autoDetectDark";
    public static final String PROP_PORTAL_THEMES_SWITCHER = "portal.themes.switcher";
    public static final String PROP_PORTAL_TUTORIAL = "portal.use.tutorial";
    public static final String PROP_PORTAL_XLOGIN_ENABLED = "xlogin.enabled";
    public static final String PROP_PORTAL_XLOGIN_ICON = "xlogin.icon";
    public static final String PROP_PORTAL_XLOGIN_TEXT = "xlogin.text";
    public static final String PROP_POWERED_BY_ALT_TEXT = "powered.alt";
    public static final String PROP_POWERED_BY_IMAGE = "powered.img";
    public static final String PROP_POWERED_BY_URL = "powered.url";
    public static final String PROP_PUSH_NOTIFICATIONS = "portal.notifications.push.enabled";
    public static final String PROP_PUSH_NOTIFICATIONS_DEBUG = "portal.notifications.debug";
    public static final String PROP_SAKAI_VERSION = "version.sakai";
    public static final String PROP_SERVICE_NAME = "ui.service";
    public static final String PROP_SERVICE_VERSION = "version.service";
    public static final String PROP_SHOW_SERVER_TIME = "portal.show.time";
    public static final String PROP_SKIN_REPO = "skin.repo";
    public static final String PROP_TOP_LOGIN = "top.login";
    public static final String PROP_USE_PAGE_ALIAS = "portal.use.page.aliases";
    public static final String PROP_XLOGIN_RELOGIN = "login.use.xlogin.to.relogin";
    public static final String PROP_GOOGLE_ANON_IP = "portal.google.anonymize.ip";
    public static final String PROP_PORTAL_TIMEOUT_DIALOG_ENABLED = "timeoutDialogEnabled";
    public static final String PROP_PORTAL_TIMEOUT_DIALOG_WARN_SECONDS = "timeoutDialogWarningSeconds";
    public static final String PROP_PORTAL_COOKIE_WARN_ENABLED = "portal.cookie.policy.warning.enabled";
    public static final String PROP_PORTAL_COOKIE_WARN_URL = "portal.cookie.policy.warning.url";
    public static final String PROP_PORTAL_TOOL_MENU_MAX = "portal.tool.menu.max";
    public static final String PROP_PORTAL_DIRECT_TOOL_URL_ENABLED = "portal.tool.direct.url.enabled";
    public static final String PROP_PORTAL_SHORT_URL_TOOL_ENABLED = "shortenedurl.portal.tool.enabled";
    public static final String PROP_PORTAL_SCROLL_TOOLBAR_ENABLED = "portal.scrolling.toolbar.enabled";

    public static final String SERVER_COPYRIGHT_CURRENT_YEAR_KEYWORD = "currentYearFromServer";

    private PortalConstants() {
        throw new RuntimeException(this.getClass().getCanonicalName() + " is not to be instantiated");
    }
}
