/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2012 The Sakai Foundation, 2013- The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.basiclti.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.Key;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.Collection;
import java.util.stream.Collectors;
import java.time.Instant;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Precision;
import org.apache.http.client.utils.URIBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.sakaiproject.api.privacy.PrivacyManager;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.GroupProvider;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.component.api.ServerConfigurationService.ConfigData;
import org.sakaiproject.component.api.ServerConfigurationService.ConfigItem;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.linktool.LinkToolUtil;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.portal.util.CSSUtils;
import org.sakaiproject.portal.util.ToolUtils;
import org.sakaiproject.service.gradebook.shared.AssessmentNotFoundException;
// We don't import either of these to make sure we are never confused and always fully qualify
// import org.sakaiproject.service.gradebook.shared.Assignment;   // We call this a "column"
// import org.sakaiproject.assignment.api.model.Assignment        // We call this an "assignment"
import org.sakaiproject.service.gradebook.shared.CommentDefinition;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.api.FormattedText;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Web;
import org.sakaiproject.util.foorm.Foorm;
import org.sakaiproject.lti13.util.SakaiLineItem;
import org.sakaiproject.lti13.util.SakaiDeepLink;
import org.sakaiproject.lti13.util.SakaiLaunchJWT;
import org.sakaiproject.lti13.util.SakaiExtension;
import org.tsugi.basiclti.BasicLTIConstants;
import org.tsugi.basiclti.BasicLTIUtil;
import org.tsugi.jackson.JacksonUtil;
import org.tsugi.ags2.objects.Score;
import org.tsugi.lti13.LTI13ConstantsUtil;
import org.tsugi.lti13.DeepLinkResponse;
import org.tsugi.lti13.LTICustomVars;
import org.tsugi.lti13.LTI13KeySetUtil;
import org.tsugi.lti13.LTI13Util;
import org.tsugi.lti13.LTI13JwtUtil;
import org.sakaiproject.lti13.LineItemUtil;
import org.tsugi.lti13.objects.BasicOutcome;
import org.tsugi.lti13.objects.Context;
import org.tsugi.lti13.objects.DeepLink;
import org.tsugi.lti13.objects.Endpoint;
import org.tsugi.lti13.objects.LaunchJWT;
import org.tsugi.lti13.objects.LaunchLIS;
import org.tsugi.lti13.objects.NamesAndRoles;
import org.tsugi.lti13.objects.ResourceLink;
import org.tsugi.lti13.objects.ToolPlatform;
import org.tsugi.lti13.objects.ForUser;
import org.tsugi.lti13.objects.LTI11Transition;
import org.tsugi.basiclti.ContentItem;

import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import net.oauth.OAuth;

/**
 * Some Sakai Utility code for IMS Basic LTI This is mostly code to support the
 * Sakai conventions for making and launching BLTI resources within Sakai.
 */
@SuppressWarnings("deprecation")
@Slf4j
public class SakaiBLTIUtil {

	public static final boolean verbosePrint = false;

	// Property: If false(default), allows comment to be returned in an LTI 1.1 POX read outcome
	public static final String LTI_STRICT = "basiclti.strict";

	public static final String BASICLTI_OUTCOMES_ENABLED = "basiclti.outcomes.enabled";
	public static final String BASICLTI_OUTCOMES_ENABLED_DEFAULT = "true";
	public static final String BASICLTI_SETTINGS_ENABLED = "basiclti.settings.enabled";
	public static final String BASICLTI_SETTINGS_ENABLED_DEFAULT = "true";
	public static final String BASICLTI_ROSTER_ENABLED = "basiclti.roster.enabled";
	public static final String BASICLTI_ROSTER_ENABLED_DEFAULT = "true";
	public static final String BASICLTI_LINEITEMS_ENABLED = "basiclti.lineitems.enabled";
	public static final String BASICLTI_LINEITEMS_ENABLED_DEFAULT = "true";
	public static final String BASICLTI_CONSUMER_USERIMAGE_ENABLED = "basiclti.consumer.userimage.enabled";
	public static final String INCOMING_ROSTER_ENABLED = "basiclti.incoming.roster.enabled";
	public static final String BASICLTI_ENCRYPTION_KEY = "basiclti.encryption.key";
	public static final String BASICLTI_LAUNCH_SESSION_TIMEOUT = "basiclti.launch.session.timeout";
	public static final String LTI13_DEPLOYMENT_ID = "lti13.deployment_id";
	public static final String LTI13_DEPLOYMENT_ID_DEFAULT = "1"; // To match Moodle
	public static final String LTI_CUSTOM_SUBSTITION_PREFIX =  "lti.custom.substitution.";
	// SAK-45491 - Key rotation interval
	public static final String LTI_ADVANTAGE_KEY_ROTATION_DAYS = "lti.advantage.key.rotation.days";
	public static final String LTI_ADVANTAGE_KEY_ROTATION_DAYS_DEFAULT = "30";

	// These are the field names in old school portlet placements
	public static final String BASICLTI_PORTLET_KEY = "key";
	public static final String BASICLTI_PORTLET_ALLOWSETTINGS = "allowsettings";
	public static final String BASICLTI_PORTLET_ALLOWROSTER = "allowroster";
	public static final String BASICLTI_PORTLET_OFF = "off";
	public static final String BASICLTI_PORTLET_ON = "on";
	public static final String BASICLTI_PORTLET_ASSIGNMENT = "assignment";
	public static final String BASICLTI_PORTLET_RELEASENAME = "releasename";
	public static final String BASICLTI_PORTLET_RELEASEEMAIL = "releaseemail";
	public static final String BASICLTI_PORTLET_TOOLSETTING = "toolsetting";
	public static final String BASICLTI_PORTLET_TOOLTITLE = "tooltitle";
	public static final String BASICLTI_PORTLET_PLACEMENTSECRET = LTIService.LTI_PLACEMENTSECRET;
	public static final String BASICLTI_PORTLET_OLDPLACEMENTSECRET = LTIService.LTI_OLDPLACEMENTSECRET;

	public static final String BASICLTI_LTI11_LAUNCH_TYPE = "basiclti.lti11.launchtype";
	public static final String BASICLTI_LTI11_LAUNCH_TYPE_LEGACY = "legacy";
	public static final String BASICLTI_LTI11_LAUNCH_TYPE_LTI112 = "lti112";
	public static final String BASICLTI_LTI11_LAUNCH_TYPE_DEFAULT = BASICLTI_LTI11_LAUNCH_TYPE_LEGACY;

	public static final String LTI11_SERVICE_PATH = "/imsblis/service/";
	public static final String LTI13_PATH = "/imsblis/lti13/";

	// The path for servlet bits that neither sets nor uses cookies
	public static final String LTI1_ANON_PATH = "/imsoidc/lti11/";

	public static final String SESSION_LAUNCH_CODE = "launch_code:";

	public static final String FOR_USER = "for_user";

	// Message type
	public static final String MESSAGE_TYPE_PARAMETER = "message_type";
	public static final String MESSAGE_TYPE_PARAMETER_PRIVACY = "privacy";
	public static final String MESSAGE_TYPE_PARAMETER_CONTENT_REVIEW = "content_review";

		public static boolean rosterEnabled() {
			String allowRoster = ServerConfigurationService.getString(BASICLTI_ROSTER_ENABLED, BASICLTI_ROSTER_ENABLED_DEFAULT);
			return "true".equals(allowRoster);
		}

		public static boolean outcomesEnabled() {
			String allowOutcomes = ServerConfigurationService.getString(BASICLTI_OUTCOMES_ENABLED, BASICLTI_OUTCOMES_ENABLED_DEFAULT);
			return "true".equals(allowOutcomes);
		}

		public static boolean settingsEnabled() {
			String allowSettings = ServerConfigurationService.getString(BASICLTI_SETTINGS_ENABLED, BASICLTI_SETTINGS_ENABLED_DEFAULT);
			return "true".equals(allowSettings);
		}

		public static boolean lineItemsEnabled() {
			String allowLineItems = ServerConfigurationService.getString(BASICLTI_LINEITEMS_ENABLED, BASICLTI_LINEITEMS_ENABLED_DEFAULT);
			return "true".equals(allowLineItems);
		}

		// Retrieve the property from the placement configuration unless it
		// is overridden by the server configuration (i.e. sakai.properties)
		public static String getCorrectProperty(Properties config, String propName, Placement placement) {
			// Check for global overrides in properties
			if (BASICLTI_PORTLET_ALLOWSETTINGS.equals(propName) && !settingsEnabled()) {
				return BASICLTI_PORTLET_OFF;
			}

			if (BASICLTI_PORTLET_ALLOWROSTER.equals(propName) && !rosterEnabled()) {
				return BASICLTI_PORTLET_OFF;
			}

			// Check for explicit setting in properties
			String propertyName = placement.getToolId() + "." + propName;
			String propValue = ServerConfigurationService.getString(propertyName, null);
			if (propValue != null && propValue.trim().length() > 0) {
				log.debug("Sakai.home {}={}", propName, propValue);
				return propValue;
			}

			// Take it from the placement
			return config.getProperty("imsti." + propName, null);
		}

		// Look at a Placement and come up with the launch urls, and
		// other launch parameters to drive the launch.
		public static boolean loadFromPlacement(Properties info, Properties launch, Placement placement) {
			Properties config = placement.getConfig();
			log.debug("Sakai properties={}", config);
			String launch_url = toNull(getCorrectProperty(config, LTIService.LTI_LAUNCH, placement));
			setProperty(info, "launch_url", launch_url);
			if (launch_url == null) {
				String xml = toNull(getCorrectProperty(config, "xml", placement));
				if (xml == null) {
					return false;
				}
				BasicLTIUtil.parseDescriptor(info, launch, xml);
			}

			String secret = getCorrectProperty(config, LTIService.LTI_SECRET, placement);

			// BLTI-195 - Compatibility mode for old-style encrypted secrets
			if (secret == null || secret.trim().length() < 1) {
				String eSecret = getCorrectProperty(config, "encryptedsecret", placement);
				if (eSecret != null && eSecret.trim().length() > 0) {
					secret = eSecret.trim() + ":" + SimpleEncryption.CIPHER;
				}
			}

			setProperty(info, LTIService.LTI_SECRET, secret);

			// This is not "consumerkey" on purpose - we are mimicking the old placement model
			setProperty(info, BASICLTI_PORTLET_KEY, getCorrectProperty(config, BASICLTI_PORTLET_KEY, placement));
			setProperty(info, LTIService.LTI_DEBUG, getCorrectProperty(config, LTIService.LTI_DEBUG, placement));
			setProperty(info, LTIService.LTI_FRAMEHEIGHT, getCorrectProperty(config, LTIService.LTI_FRAMEHEIGHT, placement));
			setProperty(info, LTIService.LTI_NEWPAGE, getCorrectProperty(config, LTIService.LTI_NEWPAGE, placement));
			setProperty(info, LTIService.LTI_TITLE, getCorrectProperty(config, BASICLTI_PORTLET_TOOLTITLE, placement));

			// Pull in and parse the custom parameters
			String customstr = toNull(getCorrectProperty(config, LTIService.LTI_CUSTOM, placement));
			parseCustom(info, customstr);

			if (info.getProperty("launch_url", null) != null
					|| info.getProperty("secure_launch_url", null) != null) {
				return true;
			}
			return false;
		}

		public static void parseCustom(Properties info, String customstr) {
			if (customstr != null) {
				String splitChar = "\n";
				if (customstr.trim().indexOf("\n") == -1) {
					splitChar = ";";
				}
				String[] params = customstr.split(splitChar);
				for (int i = 0; i < params.length; i++) {
					String param = params[i];
					if (param == null) {
						continue;
					}
					if (param.length() < 1) {
						continue;
					}
					int pos = param.indexOf("=");
					if (pos < 1) {
						continue;
					}
					if (pos + 1 > param.length()) {
						continue;
					}
					String key = BasicLTIUtil.mapKeyName(param.substring(0, pos));
					if (key == null) {
						continue;
					}
					String value = param.substring(pos + 1);
					if (value == null) {
						continue;
					}
					value = value.trim();
					if (value.length() < 1) {
						continue;
					}
					setProperty(info, "custom_" + key, value);
				}
			}
		}

		/**
		 * adjustCustom - Deal with various custom parameter strings
		 *
		 * The correct way: x=1\ny=2\nz=3 The old Sakai way: x=1;y=2;z=3 A format
		 * string that confuses things: x=1;\ny=2\nz=3 In this string, the first
		 * parameter should be "1;"
		 *
		 * So here are the rules:
		 *
		 * If it is null, blank, or has no equal signs return unchanged If there is
		 * a newline anywhere in the trimmed string return unchanged If there is one
		 * equal sign return unchanged If there is a new line anywhere in the string
		 * after trim, return unchanged If we see ..=..;..=..;..=..[;] - we replace
		 * ; with \n
		 */
		public static String adjustCustom(String customstr) {
			if (customstr == null) {
				return customstr;
			}
			String trim = customstr.trim();
			if (trim.length() == 0) {
				return customstr;
			}

			if (trim.indexOf('\n') >= 0) {
				return customstr;
			}

			String[] pieces = trim.split("=");

			// Two pieces means one equal sign (a=42)
			if (pieces.length <= 2) {
				return customstr;
			}

			// Insist that all but the first and last piece have a semicolon
			// x | 1;y | 2;z | 3
			for (int i = 1; i < pieces.length - 1; i++) {
				String piece = pieces[i];
				String[] chunks = piece.split(";");
				if (chunks.length != 2) {
					return customstr;
				}
			}

			// Now we can assume that ';' maps to '\n'
			return customstr.replace(';', '\n');
		}

		/**
		 * For LTI 2.x launches we do not support the semicolon-separated launches
		*/
		public static boolean mergeLTI1Custom(Properties custom, String customstr)
		{
			if ( customstr == null || customstr.length() < 1 ) return true;

			String splitChar = "\n";
			String [] params = customstr.split(splitChar);
			for (int i = 0 ; i < params.length; i++ ) {
				String param = params[i];
				if ( param == null ) continue;
				if ( param.length() < 1 ) continue;

				int pos = param.indexOf("=");
				if ( pos < 1 ) continue;
				if ( pos+1 > param.length() ) continue;
				String key = BasicLTIUtil.mapKeyName(param.substring(0,pos));
				if ( key == null ) continue;

				if ( custom.containsKey(key) ) continue;

				String value = param.substring(pos+1);
				if ( value == null ) continue;
				value = value.trim();
				if ( value.length() < 1 ) continue;
				setProperty(custom, key, value);
			}
			return true;
		}

		// Place the custom values into the launch
		public static void addCustomToLaunch(Properties ltiProps, Properties custom)
		{
			Enumeration<?> e = custom.propertyNames();
			while (e.hasMoreElements()) {
				String keyStr = (String) e.nextElement();
				String value =  custom.getProperty(keyStr);
				setProperty(ltiProps,"custom_"+keyStr,value);
				String mapKeyStr = BasicLTIUtil.mapKeyName(keyStr);
				if ( ! mapKeyStr.equals(keyStr) ) {
					setProperty(ltiProps,"custom_"+mapKeyStr,value);
				}
			}
		}

		public static String encryptSecret(String orig) {
			String encryptionKey = ServerConfigurationService.getString(BASICLTI_ENCRYPTION_KEY, null);
			return encryptSecret(orig, encryptionKey);
		}

		// For unit tests mostly
		public static String encryptSecret(String orig, String encryptionKey) {
			if (StringUtils.isEmpty(orig) || StringUtils.isEmpty(encryptionKey) ) {
				return orig;
			}

			// Never double encrypt
			String check = decryptSecret(orig, encryptionKey, true);
			if ( ! orig.equals(check) ) {
				return orig;
			}

			// May throw runtime exception - just let it log as this is abnormal...
			String newsecret = SimpleEncryption.encrypt(encryptionKey, orig);
			return newsecret;
		}

		public static String decryptSecret(String orig) {
			String encryptionKey = ServerConfigurationService.getString(BASICLTI_ENCRYPTION_KEY, null);
			return decryptSecret(orig, encryptionKey, false);
		}

		public static String decryptSecret(String orig, String encryptionKey, boolean checkonly) {
			if (StringUtils.isEmpty(orig) || StringUtils.isEmpty(encryptionKey) ) {
				return orig;
			}

			try {
				String newsecret = SimpleEncryption.decrypt(encryptionKey, orig);
				return newsecret;
			} catch (RuntimeException re) {
				if ( ! checkonly ) log.debug("Exception when decrypting secret - this is normal if the secret is unencrypted");
				return orig;
			}
		}

		public static boolean sakaiInfo(Properties props, Placement placement, ResourceLoader rb) {
			String context = placement.getContext();
			log.debug("ContextID={} placement={} placement title={}", context, placement.getId(), placement.getTitle());

			return sakaiInfo(props, context, placement.getId(), rb);
		}


	public static void addSiteInfo(Properties props, Properties lti13subst, Site site) {
		if (site != null) {
			String context_type = site.getType();
			if (context_type != null && context_type.toLowerCase().contains("course")) {
				setProperty(props, BasicLTIConstants.CONTEXT_TYPE, BasicLTIConstants.CONTEXT_TYPE_COURSE_SECTION);
				setProperty(lti13subst, LTICustomVars.CONTEXT_TYPE, LTICustomVars.CONTEXT_TYPE_DEFAULT);
			} else {
				setProperty(props, BasicLTIConstants.CONTEXT_TYPE, BasicLTIConstants.CONTEXT_TYPE_GROUP);
				setProperty(lti13subst, LTICustomVars.CONTEXT_TYPE, BasicLTIConstants.CONTEXT_TYPE_GROUP);
			}
			setProperty(props, BasicLTIConstants.CONTEXT_ID, site.getId());
			setProperty(lti13subst, LTICustomVars.COURSESECTION_SOURCEDID, site.getId());
			setProperty(lti13subst, LTICustomVars.CONTEXT_ID, site.getId());

			String context_id_history = site.getProperties().getProperty(LTICustomVars.CONTEXT_ID_HISTORY);
			setProperty(lti13subst, LTICustomVars.CONTEXT_ID_HISTORY, context_id_history);

			setProperty(props, BasicLTIConstants.CONTEXT_LABEL, site.getTitle());
			setProperty(lti13subst, LTICustomVars.COURSESECTION_LABEL, site.getTitle());
			setProperty(lti13subst, LTICustomVars.CONTEXT_LABEL, site.getTitle());

			setProperty(props, BasicLTIConstants.CONTEXT_TITLE, site.getTitle());
			setProperty(lti13subst, LTICustomVars.COURSESECTION_LONGDESCRIPTION, site.getTitle());
			setProperty(lti13subst, LTICustomVars.CONTEXT_TITLE, site.getTitle());

			String courseRoster = getExternalRealmId(site.getId());
			if (courseRoster != null) {
				setProperty(props, BasicLTIConstants.LIS_COURSE_OFFERING_SOURCEDID, courseRoster);
				setProperty(props, BasicLTIConstants.LIS_COURSE_SECTION_SOURCEDID, courseRoster);
				setProperty(lti13subst, LTICustomVars.COURSESECTION_SOURCEDID, courseRoster);
				setProperty(lti13subst, LTICustomVars.COURSEOFFERING_SOURCEDID, courseRoster);
			} else {
				setProperty(props, BasicLTIConstants.LIS_COURSE_OFFERING_SOURCEDID, site.getId());
				setProperty(props, BasicLTIConstants.LIS_COURSE_SECTION_SOURCEDID, site.getId());
				setProperty(lti13subst, LTICustomVars.COURSESECTION_SOURCEDID, site.getId());
				setProperty(lti13subst, LTICustomVars.COURSEOFFERING_SOURCEDID, site.getId());
			}

			// SAK-31282 - Add the Academic Session (ext_sakai_academic_session) to LTI launches
			String termPropertyName = ServerConfigurationService.getString("irubric.termPropertyName", "");
			if (termPropertyName.length() > 0) {
				String academicSessionId = site.getProperties().getProperty(termPropertyName);
				if ((academicSessionId == "") || (academicSessionId == null)) {
					academicSessionId = "OTHER";
				}
				setProperty(props, "ext_sakai_academic_session", academicSessionId);
			}
		}

		// Fix up the return Url
		String returnUrl = ServerConfigurationService.getString("basiclti.consumer_return_url", null);
		if (returnUrl == null) {
			returnUrl = getOurServerUrl() + LTI1_ANON_PATH + "return-url";
			Session s = SessionManager.getCurrentSession();
			if (s != null) {
				String controllingPortal = (String) s.getAttribute("sakai-controlling-portal");
				if (controllingPortal == null) {
					returnUrl = returnUrl + "/site";
				} else {
					returnUrl = returnUrl + "/" + controllingPortal;
				}
			}
			returnUrl = returnUrl + "/" + site.getId();
		}

		setProperty(props, BasicLTIConstants.LAUNCH_PRESENTATION_RETURN_URL, returnUrl);

	}

	public static void addUserInfo(Properties ltiProps, Properties lti13subst, Map<String, Object> tool) {
		int releasename = getInt(tool.get(LTIService.LTI_SENDNAME));
		int releaseemail = getInt(tool.get(LTIService.LTI_SENDEMAILADDR));
		User user = UserDirectoryService.getCurrentUser();
		if (user != null) {
			setProperty(ltiProps, BasicLTIConstants.USER_ID, user.getId());
			setProperty(lti13subst, LTICustomVars.USER_ID, user.getId());
			setProperty(ltiProps, BasicLTIConstants.LIS_PERSON_SOURCEDID, user.getEid());
			setProperty(lti13subst, LTICustomVars.USER_USERNAME, user.getEid());
			setProperty(lti13subst, LTICustomVars.PERSON_SOURCEDID, user.getEid());

			UserTimeService userTimeService = ComponentManager.get(UserTimeService.class);
			TimeZone tz = userTimeService.getLocalTimeZone(user.getId());
			if (tz != null) {
				setProperty(lti13subst, LTICustomVars.PERSON_ADDRESS_TIMEZONE, tz.getID());
			}

			if (releasename == 1) {
				setProperty(ltiProps, BasicLTIConstants.LIS_PERSON_NAME_GIVEN, user.getFirstName());
				setProperty(ltiProps, BasicLTIConstants.LIS_PERSON_NAME_FAMILY, user.getLastName());
				setProperty(ltiProps, BasicLTIConstants.LIS_PERSON_NAME_FULL, user.getDisplayName());
				setProperty(lti13subst, LTICustomVars.PERSON_NAME_GIVEN, user.getFirstName());
				setProperty(lti13subst, LTICustomVars.PERSON_NAME_FAMILY, user.getLastName());
				setProperty(lti13subst, LTICustomVars.PERSON_NAME_FULL, user.getDisplayName());
			}
			if (releaseemail == 1) {
				setProperty(ltiProps, BasicLTIConstants.LIS_PERSON_CONTACT_EMAIL_PRIMARY, user.getEmail());
				setProperty(lti13subst, LTICustomVars.PERSON_EMAIL_PRIMARY, user.getEmail());
				// Only send the display ID if it's different to the EID.
				// the anonymous user has a null EID.
				if (user.getEid() != null && !user.getEid().equals(user.getDisplayId())) {
					setProperty(ltiProps, BasicLTIConstants.EXT_SAKAI_PROVIDER_DISPLAYID, user.getDisplayId());
				}
			}
		}
	}

	public static String upgradeRoleString(String roleString)
	{
		if ( StringUtils.isEmpty(roleString) ) return roleString;

		String[] pieces = roleString.split(",");
		StringBuffer sb = new StringBuffer();
		for (String s : pieces) {
			s = s.trim();
			if ( s.length() == 0 ) continue;
			if ( sb.length() > 0 ) sb.append(",");
			if ( s.startsWith("http://") || s.startsWith("https://") ) {
				sb.append(s);
				continue;
			}
			// http://www.imsglobal.org/spec/lti/v1p3/
			if ( s.equalsIgnoreCase(BasicLTIConstants.MEMBERSHIP_ROLE_LEARNER) ) sb.append(LTI13ConstantsUtil.ROLE_LEARNER);
			else if ( s.equalsIgnoreCase(BasicLTIConstants.MEMBERSHIP_ROLE_INSTRUCTOR) ) sb.append(LTI13ConstantsUtil.ROLE_INSTRUCTOR);
			else if ( s.equalsIgnoreCase(BasicLTIConstants.MEMBERSHIP_ROLE_CONTEXT_ADMIN) ) sb.append(LTI13ConstantsUtil.ROLE_CONTEXT_ADMIN);
			else if ( s.equalsIgnoreCase(BasicLTIConstants.MEMBERSHIP_ROLE_SYSTEM_ADMIN) ) sb.append(LTI13ConstantsUtil.ROLE_SYSTEM_ADMIN);
			else if ( s.equalsIgnoreCase(BasicLTIConstants.MEMBERSHIP_ROLE_INSTITUTION_ADMIN) ) sb.append(LTI13ConstantsUtil.ROLE_INSTITUTION_ADMIN);
			else sb.append(s);
		}
		return sb.toString();
	}

	public static String getRoleString(String context) {
        String theRole = BasicLTIConstants.MEMBERSHIP_ROLE_LEARNER;
		if (SecurityService.isSuperUser()) {
			theRole = BasicLTIConstants.MEMBERSHIP_ROLE_INSTRUCTOR +
				"," + BasicLTIConstants.MEMBERSHIP_ROLE_CONTEXT_ADMIN +
				"," + BasicLTIConstants.MEMBERSHIP_ROLE_SYSTEM_ADMIN +
				"," + BasicLTIConstants.MEMBERSHIP_ROLE_INSTITUTION_ADMIN;
		} else if (SiteService.allowUpdateSite(context)) {
			theRole = BasicLTIConstants.MEMBERSHIP_ROLE_INSTRUCTOR;
		}
		return theRole;
	}

	public static void addRoleInfo(Properties props, Properties lti13subst, String context, String roleMapProp) {
		String theRole = getRoleString(context);

		setProperty(props, BasicLTIConstants.ROLES, theRole);
		setProperty(lti13subst, LTICustomVars.MEMBERSHIP_ROLE, theRole);

		String realmId = SiteService.siteReference(context);
		User user = null;
		Map<String, String> roleMap = convertRoleMapPropToMap(roleMapProp);
		try {
			user = UserDirectoryService.getCurrentUser();
			if (user != null) {
				Role role = null;
				String roleId = null;
				AuthzGroup realm = ComponentManager.get(AuthzGroupService.class).getAuthzGroup(realmId);
				if (realm != null) {
					role = realm.getUserRole(user.getId());
				}
				if (role != null) {
					roleId = role.getId();
				}
				if (roleId != null && roleId.length() > 0) {
					setProperty(props, "ext_sakai_role", roleId);
					setProperty(lti13subst, "Sakai.ext.role", roleId);
				}
				if (roleMap.containsKey(roleId)) {
					setProperty(props, BasicLTIConstants.ROLES, roleMap.get(roleId));
					setProperty(lti13subst, LTICustomVars.MEMBERSHIP_ROLE, upgradeRoleString(roleMap.get(roleId)));
				}
			}
		} catch (GroupNotDefinedException e) {
			log.error("SiteParticipantHelper.getExternalRealmId: site realm not found {}", e.getMessage());
		}

		// Check if there are sections the user is part of (may be more than one)
		String courseRoster = getExternalRealmId(context);
		if (user != null && courseRoster != null) {
			GroupProvider groupProvider = (GroupProvider) ComponentManager.get(
					org.sakaiproject.authz.api.GroupProvider.class);
			String[] courseRosters = groupProvider.unpackId(courseRoster);
			List<String> rosterList = new ArrayList<>();
			String userEid = user.getEid();
			for (int i = 0; i < courseRosters.length; i++) {
				String providerId = courseRosters[i];
				Map userRole = groupProvider.getUserRolesForGroup(providerId);
				if (userRole.containsKey(userEid)) {
					rosterList.add(providerId);
				}
			}
			if (rosterList.size() > 0) {
				String[] sArray = new String[rosterList.size()];
				sArray = (String[]) rosterList.toArray(sArray);
				String providedGroups = groupProvider.packId(sArray);
				setProperty(props, "ext_sakai_section", providedGroups);
			}
		}
	}

	// Retrieve the Sakai information about users, etc.
	public static boolean sakaiInfo(Properties props, String context, String placementId, ResourceLoader rb) {

		Site site;
		try {
			site = SiteService.getSite(context);
		} catch (IdUnusedException e) {
			log.error("No site/page associated with Launch context={}", context);
			return false;
		}

		// Add the generic information
		addGlobalData(site, props, null, rb);
		ToolConfiguration placement = SiteService.findTool(placementId);
		Properties config = placement.getConfig();
		String roleMapProp = toNull(getCorrectProperty(config, LTIService.LTI_ROLEMAP, placement));
		addRoleInfo(props, null, context, roleMapProp);
		addSiteInfo(props, null, site);

		// Add Placement Information
		addPlacementInfo(props, placementId);
		return true;
	}

	public static void addPlacementInfo(Properties props, String placementId) {

		// Get the placement to see if we are to release information
		ToolConfiguration placement = SiteService.findTool(placementId);
		Properties config = placement.getConfig();

		// Get the "modern" version of config
		LTIService ltiService = (LTIService) ComponentManager.get("org.sakaiproject.lti.api.LTIService");
		Properties normalProps = normalizePlacementProperties(placementId, ltiService);

		// Start setting the Basici LTI parameters
		setProperty(props, BasicLTIConstants.RESOURCE_LINK_ID, placementId);
		String pagetitle = toNull(getCorrectProperty(config, LTIService.LTI_PAGETITLE, placement));
		String tooltitle = toNull(getCorrectProperty(config, BASICLTI_PORTLET_TOOLTITLE, placement));

		// Cross-copy these if they are blank
		if (pagetitle == null) {
			pagetitle = tooltitle;
		}
		if (tooltitle == null) {
			tooltitle = pagetitle;
		}

		if (pagetitle != null) {
			setProperty(props, BasicLTIConstants.RESOURCE_LINK_TITLE, pagetitle);
		} else {
			setProperty(props, BasicLTIConstants.RESOURCE_LINK_TITLE, placement.getTitle());
		}
		if (tooltitle != null) {
			setProperty(props, BasicLTIConstants.RESOURCE_LINK_DESCRIPTION, tooltitle);
		} else {
			setProperty(props, BasicLTIConstants.RESOURCE_LINK_DESCRIPTION, placement.getTitle());
		}

		String releasename = toNull(getCorrectProperty(config, BASICLTI_PORTLET_RELEASENAME, placement));
		String releaseemail = toNull(getCorrectProperty(config, BASICLTI_PORTLET_RELEASEEMAIL, placement));

		User user = UserDirectoryService.getCurrentUser();

		PrivacyManager pm = (PrivacyManager) ComponentManager.get("org.sakaiproject.api.privacy.PrivacyManager");

		// TODO: Think about anonymous
		if (user != null) {
			String context = placement.getContext();
			boolean isViewable = pm.isViewable("/site/" + context, user.getId());
			setProperty(props, "ext_sakai_privacy", isViewable ? "visible" : "hidden");

			setProperty(props, BasicLTIConstants.USER_ID, user.getId());

			if (ServerConfigurationService.getBoolean(BASICLTI_CONSUMER_USERIMAGE_ENABLED, true)) {
				String imageUrl = getOurServerUrl() + "/direct/profile/" + user.getId() + "/image";
				setProperty(props, BasicLTIConstants.USER_IMAGE, imageUrl);
			}

			if (BASICLTI_PORTLET_ON.equals(releasename)) {
				setProperty(props, BasicLTIConstants.LIS_PERSON_NAME_GIVEN, user.getFirstName());
				setProperty(props, BasicLTIConstants.LIS_PERSON_NAME_FAMILY, user.getLastName());
				setProperty(props, BasicLTIConstants.LIS_PERSON_NAME_FULL, user.getDisplayName());
			}
			if (BASICLTI_PORTLET_ON.equals(releaseemail)) {
				setProperty(props, BasicLTIConstants.LIS_PERSON_CONTACT_EMAIL_PRIMARY, user.getEmail());
				setProperty(props, BasicLTIConstants.LIS_PERSON_SOURCEDID, user.getEid());
				setProperty(props, "ext_sakai_eid", user.getEid());
			}

			String gradebookColumn = null;
			// TODO: Figure this out
			// It is a little tricky - the tool configuration on/off decides whether
			// We check the serverCongigurationService true/false
			// We use the tool configuration to force outcomes off regardless of
			// server settings (i.e. an external tool never wants the outcomes
			// UI shown because it simply does not handle outcomes).
			String allowOutcomes = toNull(getCorrectProperty(config, LTIService.LTI_ALLOWOUTCOMES, placement));
			if (!BASICLTI_PORTLET_OFF.equals(allowOutcomes)) {
				gradebookColumn = toNull(getCorrectProperty(config, "assignment", placement));
				if (!outcomesEnabled()) {
					allowOutcomes = null;
				}
			}

			String allowRoster = (String) normalProps.get(LTIService.LTI_ALLOWROSTER);
			String allowSettings = (String) normalProps.get(LTIService.LTI_ALLOWSETTINGS_EXT);

			String result_sourcedid = getSourceDID(user, placement, config);

			String theRole = getRoleString(context);

			// if ( result_sourcedid != null && theRole.indexOf(LTICustomVars.MEMBERSHIP_ROLE_LEARNER) >= 0 ) {
			if (result_sourcedid != null) {

				if ("true".equals(allowOutcomes) && gradebookColumn != null) {
					if (theRole.contains(LTICustomVars.MEMBERSHIP_ROLE_LEARNER)) {
						setProperty(props, BasicLTIConstants.LIS_RESULT_SOURCEDID, result_sourcedid);
					}
					setProperty(props, "ext_outcome_data_values_accepted", "text");  // SAK-25696

					// New Basic Outcomes URL
					String outcome_url = ServerConfigurationService.getString("basiclti.consumer.ext_ims_lis_basic_outcome_url", null);
					if (outcome_url == null) {
						outcome_url = getOurServerUrl() + LTI11_SERVICE_PATH;
					}
					setProperty(props, "ext_ims_lis_basic_outcome_url", outcome_url);
					outcome_url = ServerConfigurationService.getString("basiclti.consumer." + BasicLTIConstants.LIS_OUTCOME_SERVICE_URL, null);
					if (outcome_url == null) {
						outcome_url = getOurServerUrl() + LTI11_SERVICE_PATH;
					}
					setProperty(props, BasicLTIConstants.LIS_OUTCOME_SERVICE_URL, outcome_url);
				}

				if (settingsEnabled() && BASICLTI_PORTLET_ON.equals(allowSettings) ) {
					setProperty(props, "ext_ims_lti_tool_setting_id", result_sourcedid);

					String service_url = ServerConfigurationService.getString("basiclti.consumer.ext_ims_lti_tool_setting_url", null);
					if (service_url == null) {
						service_url = getOurServerUrl() + LTI11_SERVICE_PATH;
					}
					setProperty(props, "ext_ims_lti_tool_setting_url", service_url);
				}

				if (rosterEnabled() && BASICLTI_PORTLET_ON.equals(allowRoster) ) {
					setProperty(props, "ext_ims_lis_memberships_id", result_sourcedid);

					String roster_url = ServerConfigurationService.getString("basiclti.consumer.ext_ims_lis_memberships_url", null);
					if (roster_url == null) {
						roster_url = getOurServerUrl() + LTI11_SERVICE_PATH;
					}
					setProperty(props, "ext_ims_lis_memberships_url", roster_url);
				}

			}

			// Send along the deprecated LinkTool encrypted session if requested
			String sendsession = toNull(getCorrectProperty(config, "ext_sakai_session", placement));
			if ("true".equals(sendsession)) {
				Session s = SessionManager.getCurrentSession();
				if (s != null) {
					String sessionid = s.getId();
					if (sessionid != null) {
						sessionid = LinkToolUtil.encrypt(sessionid);
						setProperty(props, "ext_sakai_session", sessionid);
					}
				}
			}

			// Send along the SAK-28125 encrypted session if requested
			String encryptsession = toNull(getCorrectProperty(config, "ext_sakai_encrypted_session", placement));
			String secret = toNull(getCorrectProperty(config, LTIService.LTI_SECRET, placement));
			String key = toNull(getCorrectProperty(config, BASICLTI_PORTLET_KEY, placement));
			if (secret != null && key != null && "true".equals(encryptsession)
					&& !SecurityService.isSuperUser()) {

				secret = decryptSecret(secret);
				// sha1secret is 160-bits hex the sha1 for "secret" is
				// e5e9fa1ba31ecd1ae84f75caaa474f3a663f05f4
				String sha1Secret = PortableShaUtil.sha1Hash(secret);
				Session s = SessionManager.getCurrentSession();
				if (s != null) {
					String sessionid = s.getId();
					if (sessionid != null) {
						sessionid = BlowFish.encrypt(sha1Secret, sessionid);
						setProperty(props, "ext_sakai_encrypted_session", sessionid);
						// Don't just change this as it will break existing connections
						// Especially to LTI tools written in Java with the default JCE
						setProperty(props, "ext_sakai_blowfish_length", "128");
					}
				}
			}
		}

		// Send along the content link
		String contentlink = toNull(getCorrectProperty(config, "contentlink", placement));
		if (contentlink != null) {
			setProperty(props, "ext_resource_link_content", contentlink);
		}
	}

	public static void addConsumerData(Properties props, Properties custom) {
		final String defaultName =  ServerConfigurationService.getString("serverName",
			ServerConfigurationService.getString("serverUrl","localhost.sakailms"));

		// Get the organizational information
		setProperty(custom, LTICustomVars.TOOLPLATFORMINSTANCE_GUID,
				ServerConfigurationService.getString("basiclti.consumer_instance_guid", defaultName));
		setProperty(props, BasicLTIConstants.TOOL_CONSUMER_INSTANCE_GUID,
				ServerConfigurationService.getString("basiclti.consumer_instance_guid", defaultName));

		setProperty(custom,  LTICustomVars.TOOLPLATFORMINSTANCE_NAME,
				ServerConfigurationService.getString("basiclti.consumer_instance_name", defaultName));
		setProperty(props, BasicLTIConstants.TOOL_CONSUMER_INSTANCE_NAME,
				ServerConfigurationService.getString("basiclti.consumer_instance_name", defaultName));

		setProperty(custom, LTICustomVars.TOOLPLATFORMINSTANCE_DESCRIPTION,
				ServerConfigurationService.getString("basiclti.consumer_instance_description", defaultName));
		setProperty(props, BasicLTIConstants.TOOL_CONSUMER_INSTANCE_DESCRIPTION,
				ServerConfigurationService.getString("basiclti.consumer_instance_description", defaultName));

		setProperty(custom, LTICustomVars.TOOLPLATFORMINSTANCE_URL,
				ServerConfigurationService.getString("basiclti.consumer_instance_url",
						ServerConfigurationService.getString("serverUrl", null)));
		setProperty(props, BasicLTIConstants.TOOL_CONSUMER_INSTANCE_URL,
				ServerConfigurationService.getString("basiclti.consumer_instance_url",
						ServerConfigurationService.getString("serverUrl", null)));

		setProperty(custom, LTICustomVars.TOOLPLATFORMINSTANCE_CONTACTEMAIL,
				ServerConfigurationService.getString("basiclti.consumer_instance_contact_email", null));
		setProperty(props, BasicLTIConstants.TOOL_CONSUMER_INSTANCE_CONTACT_EMAIL,
				ServerConfigurationService.getString("basiclti.consumer_instance_contact_email", null));

	}

	// Custom variable substitutions extensions follow the form of
	// $com.example.Foo.bar
	// http://www.imsglobal.org/spec/lti/v1p3/#custom-variables
	public static void addPropertyExtensionData(Properties props, Properties custom) {
		org.sakaiproject.component.api.ServerConfigurationService serverConfigurationService =
			(org.sakaiproject.component.api.ServerConfigurationService) ComponentManager.get("org.sakaiproject.component.api.ServerConfigurationService");
		if ( serverConfigurationService == null ) return;

		ConfigData configData = serverConfigurationService.getConfigData();
		List<ConfigItem> configItems = configData.getItems();
		for(ConfigItem configItem : configItems) {
			String name = configItem.getName();
			// Be *very* careful here - we only want properties with the prefix
			if ( ! name.startsWith(LTI_CUSTOM_SUBSTITION_PREFIX) ) continue;
			Object obj = configItem.getValue();
			if ( ! (obj instanceof String) ) continue;
			String value = (String) obj;
			name = name.substring(LTI_CUSTOM_SUBSTITION_PREFIX.length());
			name = name.trim();
			if ( name.length() < 1 ) continue;
			setProperty(custom, name, value);
		}
	}

	public static void addGlobalData(Site site, Properties props, Properties custom, ResourceLoader rb) {
		if (rb != null) {
			String locale = rb.getLocale().toString();
			setProperty(props, BasicLTIConstants.LAUNCH_PRESENTATION_LOCALE, locale);
			setProperty(custom, LTICustomVars.MESSAGE_LOCALE, locale);
		}

		addPropertyExtensionData(props, custom);
		addConsumerData(props, custom);

		// Send along the CSS URL
		String tool_css = ServerConfigurationService.getString("basiclti.consumer.launch_presentation_css_url", null);
		if (tool_css == null) {
			tool_css = getOurServerUrl() + CSSUtils.getCssToolBase();
		}
		setProperty(props, BasicLTIConstants.LAUNCH_PRESENTATION_CSS_URL, tool_css);

		// Send along the CSS URL list
		String tool_css_all = ServerConfigurationService.getString("basiclti.consumer.ext_sakai_launch_presentation_css_url_all", null);
		if (site != null && tool_css_all == null) {
			tool_css_all = getOurServerUrl() + CSSUtils.getCssToolBase() + ',' + getOurServerUrl() + CSSUtils.getCssToolSkinCDN(CSSUtils.getSkinFromSite(site));
		}
		setProperty(props, "ext_sakai_" + BasicLTIConstants.LAUNCH_PRESENTATION_CSS_URL + "_list", tool_css_all);

		// Let tools know we are coming from Sakai
		String sakaiVersion = ServerConfigurationService.getString("version.sakai", "2");
		setProperty(props, "ext_lms", "sakai-" + sakaiVersion);
		setProperty(props, BasicLTIConstants.TOOL_CONSUMER_INFO_PRODUCT_FAMILY_CODE, "sakai");
		setProperty(props, BasicLTIConstants.TOOL_CONSUMER_INFO_VERSION, sakaiVersion);

		setProperty(custom, LTICustomVars.TOOLCONSUMERINFO_PRODUCTFAMILYCODE, "sakai"); // Old
		setProperty(custom, LTICustomVars.TOOLCONSUMERINFO_VERSION, sakaiVersion); // Old

		setProperty(custom, LTICustomVars.TOOLPLATFORM_PRODUCTFAMILYCODE, "sakai"); // Post LTI 1.3
		setProperty(custom, LTICustomVars.TOOLPLATFORM_VERSION, sakaiVersion); // Post LTI 1.3

		// We pass this along in the Sakai world - it might
		// might be useful to the external tool
		String serverId = ServerConfigurationService.getServerId();
		setProperty(props, "ext_sakai_serverid", serverId);
		setProperty(props, "ext_sakai_server", getOurServerUrl());
	}

		// Gnerate HTML from a descriptor and properties from
		public static String[] postLaunchHTML(String descriptor, String contextId, String resourceId, ResourceProperties props, ResourceLoader rb) {
			if (descriptor == null || contextId == null || resourceId == null) {
				return postError("<p>" + getRB(rb, "error.descriptor", "Error, missing contextId, resourceid or descriptor") + "</p>");
			}

			// Add user, course, etc to the launch parameters
			Properties launch = new Properties();
			if (!sakaiInfo(launch, contextId, resourceId, rb)) {
				return postError("<p>" + getRB(rb, "error.info.resource",
						"Error, cannot load Sakai information for resource=") + resourceId + ".</p>");
			}

			Properties info = new Properties();
			if (!BasicLTIUtil.parseDescriptor(info, launch, descriptor)) {
				return postError("<p>" + getRB(rb, "error.badxml.resource",
						"Error, cannot parse descriptor for resource=") + resourceId + ".</p>");
			}

			return postLaunchHTML(info, launch, rb);
		}

		// This must return an HTML message as the [0] in the array
		// If things are successful - the launch URL is in [1]
		public static String[] postLaunchHTML(Map<String, Object> content, Map<String, Object> tool,
				String state, String nonce, LTIService ltiService, ResourceLoader rb) {

			log.debug("state={} nonce={}", state, nonce);
			if (content == null) {
				return postError("<p>" + getRB(rb, "error.content.missing", "Content item is missing or improperly configured.") + "</p>");
			}
			if (tool == null) {
				return postError("<p>" + getRB(rb, "error.tool.missing", "Tool item is missing or improperly configured.") + "</p>");
			}

			int status = getInt(tool.get(LTIService.LTI_STATUS));
			if (status == 1) {
				return postError("<p>" + getRB(rb, "tool.disabled", "Tool is currently disabled") + "</p>");
			}

			// Go with the content url first
			String launch_url = (String) content.get(LTIService.LTI_LAUNCH);
			if (launch_url == null) {
				launch_url = (String) tool.get(LTIService.LTI_LAUNCH);
			}
			if (launch_url == null) {
				return postError("<p>" + getRB(rb, "error.nolaunch", "This tool is not yet configured.") + "</p>");
			}

			String context = (String) content.get(LTIService.LTI_SITE_ID);
			Site site;
			try {
				site = SiteService.getSite(context);
			} catch (IdUnusedException e) {
				log.error("No site/page associated with Launch context={}", context);
				return postError("<p>" + getRB(rb, "error.site.missing", "Cannot load site.") + context + "</p>");
			}

			// See if there are the necessary items
			String secret = getSecret(tool, content);
			String key = getKey(tool, content);

			if (LTIService.LTI_SECRET_INCOMPLETE.equals(key) && LTIService.LTI_SECRET_INCOMPLETE.equals(secret)) {
				return postError("<p>" + getRB(rb, "error.tool.partial", "Tool item is incomplete, missing a key and secret.") + "</p>");
			}

			boolean isLTI13 = isLTI13(tool, content);

			log.debug("isLTI13={}", isLTI13);

			// Start building up the properties
			Properties ltiProps = new Properties();
			Properties toolProps = new Properties();
			Properties lti13subst = new Properties();
			setProperty(ltiProps, BasicLTIConstants.LTI_VERSION, BasicLTIConstants.LTI_VERSION_1);
			addGlobalData(site, ltiProps, lti13subst, rb);
			addSiteInfo(ltiProps, lti13subst, site);
			addRoleInfo(ltiProps, lti13subst, context, (String) tool.get(LTIService.LTI_ROLEMAP));
			addUserInfo(ltiProps, lti13subst, tool);

			String resource_link_id = getResourceLinkId(content);
			setProperty(ltiProps, BasicLTIConstants.RESOURCE_LINK_ID, resource_link_id);

			setProperty(toolProps, "launch_url", launch_url);
			setProperty(toolProps, "state", state);  // So far LTI 1.3 only
			setProperty(toolProps, "nonce", nonce);  // So far LTI 1.3 only

			// LTI 1.1.2
			String tool_state = (String) tool.get("tool_state");
			if ( StringUtils.isNotEmpty(tool_state) ) setProperty(ltiProps, "tool_state", tool_state);
			String platform_state = (String) tool.get("platform_state");
			if ( StringUtils.isNotEmpty(platform_state) ) setProperty(ltiProps, "platform_state", platform_state);
			String relaunch_url = (String) tool.get("relaunch_url");
			if ( StringUtils.isNotEmpty(relaunch_url) ) setProperty(ltiProps, "relaunch_url", relaunch_url);

			setProperty(toolProps, LTIService.LTI_SECRET, secret);
			setProperty(toolProps, BASICLTI_PORTLET_KEY, key);

			int debug = getInt(tool.get(LTIService.LTI_DEBUG));
			if (debug == 2) {
				debug = getInt(content.get(LTIService.LTI_DEBUG));
			}
			setProperty(toolProps, LTIService.LTI_DEBUG, debug + "");

			int frameheight = getInt(tool.get(LTIService.LTI_FRAMEHEIGHT));
			if (frameheight == 2) {
				frameheight = getInt(content.get(LTIService.LTI_FRAMEHEIGHT));
			}
			setProperty(toolProps, LTIService.LTI_FRAMEHEIGHT, frameheight + "");
			setProperty(lti13subst, LTICustomVars.MESSAGE_HEIGHT, frameheight + "");

			int newpage = getInt(tool.get(LTIService.LTI_NEWPAGE));
			if (newpage == 2) {
				newpage = getInt(content.get(LTIService.LTI_NEWPAGE));
			}
			setProperty(toolProps, LTIService.LTI_NEWPAGE, newpage + "");

			String title = (String) content.get(LTIService.LTI_TITLE);
			if (title == null) {
				title = (String) tool.get(LTIService.LTI_TITLE);
			}

			// SAK-43966 - Prior to Sakai-21 there is no description field in Content
			String description = (String) content.get(LTIService.LTI_DESCRIPTION);

			// SAK-40044 - If there is no description, we fall back to the pre-21 description in JSON
			String content_settings = (String) content.get(LTIService.LTI_SETTINGS);
			JSONObject content_json = org.tsugi.basiclti.BasicLTIUtil.parseJSONObject(content_settings);
			if (StringUtils.isBlank(description) ) {
				description = (String) content_json.get(LTIService.LTI_DESCRIPTION);
			}

			// All else fails, use pre-SAK-40044 title as description
			if (StringUtils.isBlank(description)) {
				description = title;
			}

			if (StringUtils.isNotBlank(title)) {
				setProperty(ltiProps, BasicLTIConstants.RESOURCE_LINK_TITLE, title);
				setProperty(lti13subst, LTICustomVars.RESOURCELINK_TITLE, title);
			}

			if ( StringUtils.isNotBlank(description) ) {
				setProperty(ltiProps, BasicLTIConstants.RESOURCE_LINK_DESCRIPTION, description);
				setProperty(lti13subst, LTICustomVars.RESOURCELINK_DESCRIPTION, description);
			}

			// Pull in the ResouceLink.id.history value from JSON
			String content_id_history = (String) content_json.get(LTIService.LTI_ID_HISTORY);
			if ( StringUtils.isNotBlank(content_id_history) ) {
				setProperty(lti13subst, LTICustomVars.RESOURCELINK_ID_HISTORY, content_id_history);
			}

			// Bring in the substitution variables from Assignments via JSON
			String[] jsonSubst  = {
				DeepLinkResponse.RESOURCELINK_AVAILABLE_STARTDATETIME,
				DeepLinkResponse.RESOURCELINK_AVAILABLE_ENDDATETIME,
				DeepLinkResponse.RESOURCELINK_SUBMISSION_STARTDATETIME,
				DeepLinkResponse.RESOURCELINK_SUBMISSION_ENDDATETIME
			};

			for (String subKey : jsonSubst) {
				String value = StringUtils.trimToNull((String) content_json.get(subKey));
				if ( value == null ) continue;
				setProperty(lti13subst, subKey, value);
			}

			User user = UserDirectoryService.getCurrentUser();

			int allowoutcomes = getInt(tool.get(LTIService.LTI_ALLOWOUTCOMES));
			int allowroster = getInt(tool.get(LTIService.LTI_ALLOWROSTER));
			int allowsettings = getInt(tool.get(LTIService.LTI_ALLOWSETTINGS_EXT));
			String placement_secret = (String) content.get(LTIService.LTI_PLACEMENTSECRET);

			String result_sourcedid = getSourceDID(user, resource_link_id, placement_secret);
			log.debug("allowoutcomes={} allowroster={} allowsettings={} result_sourcedid={}",
					allowoutcomes, allowroster, allowsettings, result_sourcedid);

			if (result_sourcedid != null) {
				String theRole = getRoleString(context);
				log.debug("theRole={}", theRole);
				if (allowoutcomes == 1) {
					setProperty(ltiProps, "ext_outcome_data_values_accepted", "text");  // SAK-25696
					// New Basic Outcomes URL
					String outcome_url = ServerConfigurationService.getString("basiclti.consumer.ext_ims_lis_basic_outcome_url", null);
					if (outcome_url == null) {
						outcome_url = getOurServerUrl() + LTI11_SERVICE_PATH;
					}
					setProperty(ltiProps, "ext_ims_lis_basic_outcome_url", outcome_url);
					outcome_url = ServerConfigurationService.getString("basiclti.consumer." + BasicLTIConstants.LIS_OUTCOME_SERVICE_URL, null);
					if (outcome_url == null) {
						outcome_url = getOurServerUrl() + LTI11_SERVICE_PATH;
					}
					setProperty(ltiProps, BasicLTIConstants.LIS_OUTCOME_SERVICE_URL, outcome_url);

					if (theRole.contains(BasicLTIConstants.MEMBERSHIP_ROLE_LEARNER)) {
						setProperty(ltiProps, BasicLTIConstants.LIS_RESULT_SOURCEDID, result_sourcedid);
					}
				}

				// We continue to support the old settings for LTI 2 see SAK-25621
				if (allowsettings == 1) {
					setProperty(ltiProps, "ext_ims_lti_tool_setting_id", result_sourcedid);

					String service_url = ServerConfigurationService.getString("basiclti.consumer.ext_ims_lti_tool_setting_url", null);
					if (service_url == null) {
						service_url = getOurServerUrl() + LTI11_SERVICE_PATH;
					}
					setProperty(ltiProps, "ext_ims_lti_tool_setting_url", service_url);
				}

				if (allowroster == 1) {
					setProperty(ltiProps, "ext_ims_lis_memberships_id", result_sourcedid);

					String roster_url = ServerConfigurationService.getString("basiclti.consumer.ext_ims_lis_memberships_url", null);
					if (roster_url == null) {
						roster_url = getOurServerUrl() + LTI11_SERVICE_PATH;
					}
					setProperty(ltiProps, "ext_ims_lis_memberships_url", roster_url);
				}

			}

			// Construct the ultimate custom values for Launch
			Properties custom = new Properties();

			String contentCustom = (String) content.get(LTIService.LTI_CUSTOM);
			contentCustom = adjustCustom(contentCustom);
			mergeLTI1Custom(custom, contentCustom);

			String toolCustom = (String) tool.get(LTIService.LTI_CUSTOM);
			toolCustom = adjustCustom(toolCustom);
			mergeLTI1Custom(custom, toolCustom);

			// See if there are any locally deployed substitutions
			ltiService.filterCustomSubstitutions(lti13subst, tool, site);

			log.debug("lti13subst={}", lti13subst);
			log.debug("before custom={}", custom);
			LTI13Util.substituteCustom(custom, lti13subst);
			log.debug("after custom={}", custom);

			log.debug("ltiProps={}", ltiProps);
			log.debug("custom={}", custom);

			// Place the custom values into the launch
			addCustomToLaunch(ltiProps, custom);

			if (isLTI13) {
				return postLaunchJWT(toolProps, ltiProps, site, tool, content, rb);
			}
			return postLaunchHTML(toolProps, ltiProps, rb);
		}

		/**
		 * Build a URL, Adding Sakai's CSRF token
		 */
		public static String addCSRFToken(String url) {
			Session session = SessionManager.getCurrentSession();
			Object csrfToken = session.getAttribute(UsageSessionService.SAKAI_CSRF_SESSION_ATTRIBUTE);
			if (!url.contains("?")) {
				url = url + "?";
			} else {
				url = url + "&";
			}
			url = url + "sakai_csrf_token=" + URLEncoder.encode(csrfToken.toString());
			return url;
		}

		/**
		 * Create a ContentItem from the current request (may throw runtime)
		 */
		public static ContentItem getContentItemFromRequest(Map<String, Object> tool) {

			Placement placement = ToolManager.getCurrentPlacement();
			String siteId = placement.getContext();

			String toolSiteId = (String) tool.get(LTIService.LTI_SITE_ID);
			if (toolSiteId != null && !toolSiteId.equals(siteId)) {
				throw new RuntimeException("Incorrect site id");
			}

			HttpServletRequest req = ToolUtils.getRequestFromThreadLocal();

			String lti_log = req.getParameter("lti_log");
			String lti_errorlog = req.getParameter("lti_errorlog");
			if (lti_log != null) {
				log.debug(lti_log);
			}
			if (lti_errorlog != null) {
				log.warn(lti_errorlog);
			}

			ContentItem contentItem = new ContentItem(req);

			String oauth_consumer_key = req.getParameter("oauth_consumer_key");
			String oauth_secret = (String) tool.get(LTIService.LTI_SECRET);
			oauth_secret = decryptSecret(oauth_secret);

			String URL = getOurServletPath(req);
			if (!contentItem.validate(oauth_consumer_key, oauth_secret, URL)) {
				log.warn("Provider failed to validate message: {}", contentItem.getErrorMessage());
				String base_string = contentItem.getBaseString();
				if (base_string != null) {
					log.warn("base_string={}", base_string);
				}
				throw new RuntimeException("Failed OAuth validation");
			}
			return contentItem;
		}

		/**
		 * getPublicKey - Get the appropriate public key for use for an incoming request
		 */
		public static Key getPublicKey(Map<String, Object> tool, String id_token)
		{
			JSONObject jsonHeader = LTI13JwtUtil.jsonJwtHeader(id_token);
			if (jsonHeader == null) {
				throw new RuntimeException("Could not parse Jwt Header in client_assertion");
			}
			String incoming_kid = (String) jsonHeader.get("kid");

			String tool_keyset = (String) tool.get(LTIService.LTI13_TOOL_KEYSET);
			if (tool_keyset == null) {
				throw new RuntimeException("Could not find tool keyset url");
			}

			Key publicKey = null;
			if ( tool_keyset != null ) {
				log.debug("Retrieving kid="+incoming_kid+" from "+tool_keyset);

				// TODO: Read from Earle's super-cluster-cache one day - SAK-43700
				try {
					publicKey = LTI13KeySetUtil.getKeyFromKeySet(incoming_kid, tool_keyset);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					// Sorry - too many exceptions to explain here - lets keep it simple after logging it
					throw new RuntimeException("Unable to retrieve kid="+incoming_kid+" from "+tool_keyset+" detail="+e.getMessage());
				}
				// TODO: Store in Earle's super-cluster-cache one day - SAK-43700

			}
			return publicKey;
		}

		/**
		 * Create a ContentItem from the current request (may throw runtime)
		 */
		public static DeepLinkResponse getDeepLinkFromToken(Map<String, Object> tool, String id_token) {

			Placement placement = ToolManager.getCurrentPlacement();
			String siteId = placement.getContext();

			String toolSiteId = (String) tool.get(LTIService.LTI_SITE_ID);
			if (toolSiteId != null && !toolSiteId.equals(siteId)) {
				throw new RuntimeException("Incorrect site id");
			}

			HttpServletRequest req = ToolUtils.getRequestFromThreadLocal();

			String lti_log = req.getParameter("lti_log");
			String lti_errorlog = req.getParameter("lti_errorlog");
			if (lti_log != null) {
				log.debug(lti_log);
			}
			if (lti_errorlog != null) {
				log.warn(lti_errorlog);
			}

			// May throw a RunTimeException on our behalf :)
			Key publicKey = SakaiBLTIUtil.getPublicKey(tool, id_token);

			// Fill up the object, validate and return
			DeepLinkResponse dlr = new DeepLinkResponse(id_token);
			if ( ! dlr.validate(publicKey) ) {
				throw new RuntimeException("Could not verify signature");
			}

			return dlr;
		}

		/**
		 * An LTI 2.0 ContentItemSelectionRequest launch
		 *
		 * This must return an HTML message as the [0] in the array If things are
		 * successful - the launch URL is in [1]
		 */
		public static String[] postContentItemSelectionRequest(Long toolKey, Map<String, Object> tool,
				String state, String nonce, ResourceLoader rb, String contentReturn, Properties dataProps) {
			if (tool == null) {
				return postError("<p>" + getRB(rb, "error.tool.missing", "Tool is missing or improperly configured.") + "</p>");
			}

			String launch_url = (String) tool.get("launch");
			if (launch_url == null) {
				return postError("<p>" + getRB(rb, "error.tool.noreg", "This tool is has no launch url.") + "</p>");
			}

			String consumerkey = (String) tool.get(LTIService.LTI_CONSUMERKEY);
			String secret = (String) tool.get(LTIService.LTI_SECRET);

			// If secret is encrypted, decrypt it
			secret = decryptSecret(secret);

			boolean isLTI13 = isLTI13(tool, null);

			log.debug("isLTI13={}", isLTI13);

			if (!isLTI13 && (secret == null || consumerkey == null)) {
				return postError("<p>" + getRB(rb, "error.tool.partial", "Tool is incomplete, missing a key and secret.") + "</p>");
			}

			// Start building up the properties
			Properties ltiProps = new Properties();

			setProperty(ltiProps, BasicLTIConstants.LTI_VERSION, BasicLTIConstants.LTI_VERSION_1);
			setProperty(ltiProps, BasicLTIConstants.LTI_MESSAGE_TYPE, BasicLTIConstants.CONTENT_ITEM_SELECTION_REQUEST);

			setProperty(ltiProps, ContentItem.ACCEPT_MEDIA_TYPES, ContentItem.MEDIA_LTILINKITEM);
			setProperty(ltiProps, BasicLTIConstants.ACCEPT_PRESENTATION_DOCUMENT_TARGETS, "iframe,window"); // Nice to add overlay
			setProperty(ltiProps, BasicLTIConstants.ACCEPT_UNSIGNED, "false");
			setProperty(ltiProps, BasicLTIConstants.ACCEPT_MULTIPLE, "false");
			setProperty(ltiProps, BasicLTIConstants.ACCEPT_COPY_ADVICE, "false"); // ???
			setProperty(ltiProps, BasicLTIConstants.AUTO_CREATE, "true");
			setProperty(ltiProps, BasicLTIConstants.CAN_CONFIRM, "false");
			// setProperty(ltiProps, BasicLTIConstants.TITLE, "");
			// setProperty(ltiProps, BasicLTIConstants.TEXT, "");

			// Pull in additonal data
			JSONObject dataJSON = new JSONObject();
			Enumeration en = dataProps.keys();
			while (en.hasMoreElements()) {
				String key = (String) en.nextElement();
				String value = dataProps.getProperty(key);
				if (value == null) {
					continue;
				}

				// Allow overrides
				if (BasicLTIConstants.ACCEPT_MEDIA_TYPES.equals(key)) {
					setProperty(ltiProps, BasicLTIConstants.ACCEPT_MEDIA_TYPES, value);
					continue;
				} else if (BasicLTIConstants.ACCEPT_MULTIPLE.equals(key)) {
					setProperty(ltiProps, BasicLTIConstants.ACCEPT_MULTIPLE, value);
					continue;
				} else if (BasicLTIConstants.ACCEPT_PRESENTATION_DOCUMENT_TARGETS.equals(key)) {
					setProperty(ltiProps, BasicLTIConstants.ACCEPT_PRESENTATION_DOCUMENT_TARGETS, value);
					continue;
				} else if (BasicLTIConstants.ACCEPT_UNSIGNED.equals(key)) {
					setProperty(ltiProps, BasicLTIConstants.ACCEPT_UNSIGNED, value);
					continue;
				} else if (BasicLTIConstants.AUTO_CREATE.equals(key)) {
					setProperty(ltiProps, BasicLTIConstants.AUTO_CREATE, value);
					continue;
				} else if (BasicLTIConstants.CAN_CONFIRM.equals(key)) {
					setProperty(ltiProps, BasicLTIConstants.CAN_CONFIRM, value);
					continue;
				} else if (BasicLTIConstants.TITLE.equals(key)) {
					setProperty(ltiProps, BasicLTIConstants.TITLE, value);
					continue;
				} else if (BasicLTIConstants.TEXT.equals(key)) {
					setProperty(ltiProps, BasicLTIConstants.TEXT, value);
					continue;
				}

				// Pass in data for use to get back.
				dataJSON.put(key, value);
			}
			setProperty(ltiProps, BasicLTIConstants.DATA, dataJSON.toString());

			setProperty(ltiProps, BasicLTIConstants.CONTENT_ITEM_RETURN_URL, contentReturn);

			// This must always be there
			String context = (String) tool.get(LTIService.LTI_SITE_ID);
			Site site;
			try {
				site = SiteService.getSite(context);
			} catch (IdUnusedException e) {
				log.error("No site/page associated with Launch context={}", context);
				return postError("<p>" + getRB(rb, "error.site.missing", "Cannot load site.") + context + "</p>");
			}

			Properties lti13subst = new Properties();
			addGlobalData(site, ltiProps, lti13subst, rb);
			addSiteInfo(ltiProps, lti13subst, site);
			addRoleInfo(ltiProps, lti13subst, context, (String) tool.get(LTIService.LTI_ROLEMAP));

			int releasename = getInt(tool.get(LTIService.LTI_SENDNAME));
			int releaseemail = getInt(tool.get(LTIService.LTI_SENDEMAILADDR));
			addUserInfo(ltiProps, lti13subst, tool);

			// Don't sent the normal return URL when we are doing ContentItem launch
			// Certification Issue
			if (BasicLTIConstants.CONTENT_ITEM_SELECTION_REQUEST.equals(ltiProps.getProperty(BasicLTIConstants.LTI_MESSAGE_TYPE))) {

				ltiProps.remove(BasicLTIConstants.LAUNCH_PRESENTATION_RETURN_URL);
			}

			boolean dodebug = getInt(tool.get(LTIService.LTI_DEBUG)) == 1;
			if (log.isDebugEnabled()) {
				dodebug = true;
			}

			// Merge all the sources of custom vaues and run the substitution
			Properties custom = new Properties();

			String toolCustom = (String) tool.get(LTIService.LTI_CUSTOM);
			toolCustom = adjustCustom(toolCustom);
			mergeLTI1Custom(custom, toolCustom);

			// See if there are any locally deployed substitutions
			LTIService ltiService = (LTIService) ComponentManager.get("org.sakaiproject.lti.api.LTIService");
			ltiService.filterCustomSubstitutions(lti13subst, tool, site);

			log.debug("lti13subst={}", lti13subst);
			log.debug("before custom={}", custom);
			LTI13Util.substituteCustom(custom, lti13subst);
			log.debug("after custom={}", custom);

			log.debug("ltiProps={}", ltiProps);
			log.debug("custom={}", custom);

			// Place the custom values into the launch
			addCustomToLaunch(ltiProps, custom);

			if ( isLTI13 ) {
				Properties toolProps = new Properties();
				toolProps.put("launch_url", launch_url);
				setProperty(toolProps, "state", state);  // So far LTI 1.3 only
				setProperty(toolProps, "nonce", nonce);  // So far LTI 1.3 only
				toolProps.put(LTIService.LTI_DEBUG, dodebug ? "1" : "0");

				Map<String, Object> content = null;
				return postLaunchJWT(toolProps, ltiProps, site, tool, content, rb);
			}

			// LTI 1.1.2
			String tool_state = (String) tool.get("tool_state");
			if ( StringUtils.isNotEmpty(tool_state) ) setProperty(ltiProps, "tool_state", tool_state);
			String platform_state = (String) tool.get("platform_state");
			if ( StringUtils.isNotEmpty(platform_state) ) setProperty(ltiProps, "platform_state", platform_state);
			String relaunch_url = (String) tool.get("relaunch_url");
			if ( StringUtils.isNotEmpty(relaunch_url) ) setProperty(ltiProps, "relaunch_url", relaunch_url);

			Map<String, String> extra = new HashMap<>();
			extra.put(BasicLTIUtil.EXTRA_ERROR_TIMEOUT, rb.getString("error.submit.timeout"));
			extra.put(BasicLTIUtil.EXTRA_HTTP_POPUP, BasicLTIUtil.EXTRA_HTTP_POPUP_FALSE);  // Don't bother oening in new window in protocol mismatch
			ltiProps = BasicLTIUtil.signProperties(ltiProps, launch_url, "POST",
					consumerkey, secret, extra);

			log.debug("signed ltiProps={}", ltiProps);

			String launchtext = getRB(rb, "launch.button", "Press to Launch External Tool");
			String postData = BasicLTIUtil.postLaunchHTML(ltiProps, launch_url, launchtext, dodebug, extra);

			String[] retval = {postData, launch_url};
			return retval;
		}

		// This must return an HTML message as the [0] in the array
		// If things are successful - the launch URL is in [1]
		public static String[] postLaunchHTML(String placementId, ResourceLoader rb) {
			log.debug("placementId={}", placementId);
			if (placementId == null) {
				return postError("<p>" + getRB(rb, "error.missing", "Error, missing placementId") + "</p>");
			}
			FormattedText formattedText = ComponentManager.get(FormattedText.class);
			placementId = formattedText.escapeHtml(placementId);
			ToolConfiguration placement = SiteService.findTool(placementId);
			if (placement == null) {
				return postError("<p>" + getRB(rb, "error.load", "Error, cannot load placement=") + placementId + ".</p>");
			}

			// Add user, course, etc to the launch parameters
			Properties ltiProps = new Properties();
			if (!sakaiInfo(ltiProps, placement, rb)) {
				return postError("<p>" + getRB(rb, "error.missing",
						"Error, cannot load Sakai information for placement=") + placementId + ".</p>");
			}

			// Retrieve the launch detail
			Properties toolProps = new Properties();
			if (!loadFromPlacement(toolProps, ltiProps, placement)) {
				return postError("<p>" + getRB(rb, "error.nolaunch", "Not Configured.") + "</p>");
			}
			return postLaunchHTML(toolProps, ltiProps, rb);
		}

		public static String[] postLaunchHTML(Properties toolProps, Properties ltiProps, ResourceLoader rb) {
			log.debug("LTI 1.1");

			String launch_url = toolProps.getProperty("secure_launch_url");
			if (launch_url == null) {
				launch_url = toolProps.getProperty("launch_url");
			}
			if (launch_url == null) {
				return postError("<p>" + getRB(rb, "error.missing", "Not configured") + "</p>");
			}

			// Look up the LMS-wide secret and key - default key is guid
			String key = getToolConsumerInfo(launch_url, BASICLTI_PORTLET_KEY);
			if (key == null) {
				key = ServerConfigurationService.getString("basiclti.consumer_instance_guid",
						ServerConfigurationService.getString("serverName", null));
			}
			String secret = getToolConsumerInfo(launch_url, LTIService.LTI_SECRET);

			// Demand key/secret in a pair
			if (key == null || secret == null) {
				key = null;
				secret = null;
			}

			// If we do not have LMS-wide info, use the local key/secret
			if (secret == null) {
				secret = toNull(toolProps.getProperty(LTIService.LTI_SECRET));
				key = toNull(toolProps.getProperty(BASICLTI_PORTLET_KEY));
			}

			// If secret is encrypted, decrypt it
			secret = decryptSecret(secret);

			// Pull in all of the custom parameters
			for (Object okey : toolProps.keySet()) {
				String skey = (String) okey;
				if (!skey.startsWith(BasicLTIConstants.CUSTOM_PREFIX)) {
					continue;
				}
				String value = toolProps.getProperty(skey);
				if (value == null) {
					continue;
				}
				setProperty(ltiProps, skey, value);
			}

			String oauth_callback = ServerConfigurationService.getString("basiclti.oauth_callback", null);
			// Too bad there is not a better default callback url for OAuth
			// Actually since we are using signing-only, there is really not much point
			// In OAuth 6.2.3, this is after the user is authorized
			if (oauth_callback == null) {
				oauth_callback = "about:blank";
			}
			setProperty(ltiProps, "oauth_callback", oauth_callback);

			// Sanity checks
			if (secret == null) {
				return postError("<p>" + getRB(rb, "error.nosecret", "Error - must have a secret.") + "</p>");
			}
			if (secret != null && key == null) {
				return postError("<p>" + getRB(rb, "error.nokey", "Error - must have a secret and a key.") + "</p>");
			}

			addPropertyExtensionData(ltiProps, null);
			addConsumerData(ltiProps, null);

			Map<String, String> extra = new HashMap<>();
			extra.put(BasicLTIUtil.EXTRA_ERROR_TIMEOUT, rb.getString("error.submit.timeout"));
			ltiProps = BasicLTIUtil.signProperties(ltiProps, launch_url, "POST", key, secret, extra);

			if (ltiProps == null) {
				return postError("<p>" + getRB(rb, "error.sign", "Error signing message.") + "</p>");
			}
			log.debug("LAUNCH III={}", ltiProps);

			String debugProperty = toolProps.getProperty(LTIService.LTI_DEBUG);
			boolean dodebug = BASICLTI_PORTLET_ON.equals(debugProperty) || "1".equals(debugProperty);
			if (log.isDebugEnabled()) {
				dodebug = true;
			}

			String launchtext = getRB(rb, "launch.button", "Press to Launch External Tool");
			String postData = BasicLTIUtil.postLaunchHTML(ltiProps, launch_url, launchtext, dodebug, extra);

			String[] retval = {postData, launch_url};
			return retval;
		}

		public static String getDeploymentId(String site_id) {
			String deployment_id = ServerConfigurationService.getString(LTI13_DEPLOYMENT_ID, LTI13_DEPLOYMENT_ID_DEFAULT);
			return deployment_id;
		}

		public static String getIssuer(String site_id) {
			String retval = getOurServerUrl();
			String deployment_id = getDeploymentId(site_id);
			if ( ! LTI13_DEPLOYMENT_ID_DEFAULT.equals(deployment_id) ) {
					retval += "/deployment/" + deployment_id;
			}
			if ( StringUtils.isNotEmpty(site_id) ) {
				retval = retval + "/site/" + site_id;
			}
			return retval;
		}

		public static String getSubject(String userId, String site_id) {
			String retval = getOurServerUrl();
			String deployment_id = getDeploymentId(site_id);
			if ( ! LTI13_DEPLOYMENT_ID_DEFAULT.equals(deployment_id) ) {
					retval += "/deployment/" + deployment_id;
			}
			retval = retval + "/user/" + userId;
			return retval;
		}

		// Return the Sakai user_id from an LTI 1.3 Subject
		// https://dev1.sakaicloud.com/user/c71bb6b6-3f3c-4922-a1f9-73855570a0eb
		public static String parseSubject(String subject) {
			if ( subject == null ) return subject;
			String retval = getOurServerUrl();
			if ( ! subject.startsWith(getOurServerUrl()) ) return subject;

			String [] pieces = subject.split("/");
			int where = pieces.length-1;
			if ( where < 0 ) return subject;
			return pieces[where];
		}

		public static String[] postLaunchJWT(Properties toolProps, Properties ltiProps,
				Site site, Map<String, Object> tool, Map<String, Object> content, ResourceLoader rb) {
			log.debug("postLaunchJWT LTI 1.3");
			String launch_url = toolProps.getProperty("secure_launch_url");
			if (launch_url == null) {
				launch_url = toolProps.getProperty("launch_url");
			}
			if (launch_url == null) {
				return postError("<p>" + getRB(rb, "error.missing", "Not configured") + "</p>");
			}

			HttpServletRequest req = ToolUtils.getRequestFromThreadLocal();

			String orig_site_id_null = (String) tool.get("orig_site_id_null");
			String site_id = null;
			if ( ! "true".equals(orig_site_id_null) ) {
				site_id = (String) tool.get(LTIService.LTI_SITE_ID);
			}

			String client_id = (String) tool.get(LTIService.LTI13_CLIENT_ID);
			String platform_public = (String) tool.get(LTIService.LTI13_PLATFORM_PUBLIC);
			String platform_private = decryptSecret((String) tool.get(LTIService.LTI13_PLATFORM_PRIVATE));
			String placement_secret = null;
			if (content != null) {
				placement_secret = (String) content.get(LTIService.LTI_PLACEMENTSECRET);
			}

			if (platform_private == null) {
				return postError("<p>" + getRB(rb, "error.no.platform.private.key", "Missing Platform Private Key.") + "</p>");
			}

		/*

	context_id: mercury
	context_label: mercury site
	context_title: mercury site
	context_type: Group
	custom_x=42
	custom_y=043040450
	ext_ims_lis_basic_outcome_url: http://localhost:8080/imsblis/service/
	ext_ims_lis_memberships_id: c1007fb6345a87cd651785422a2925114d0707fad32c66edb6bfefbf2165819a:::admin:::content:3
	ext_ims_lis_memberships_url: http://localhost:8080/imsblis/service/
	ext_ims_lti_tool_setting_id: c1007fb6345a87cd651785422a2925114d0707fad32c66edb6bfefbf2165819a:::admin:::content:3
	ext_ims_lti_tool_setting_url: http://localhost:8080/imsblis/service/
	ext_lms: sakai-21.3
	ext_sakai_academic_session: OTHER
	ext_sakai_launch_presentation_css_url_list: http://localhost:8080/library/skin/tool_base.css,http://localhost:8080/library/skin/morpheus-default/tool.css?version=49b21ca5
	ext_sakai_role: maintain
	ext_sakai_server: http://localhost:8080
	ext_sakai_serverid: MacBook-Pro-92.local
	launch_presentation_css_url: http://localhost:8080/library/skin/tool_base.css
	launch_presentation_locale: en_US
	launch_presentation_return_url: http://localhost:8080/imsblis/service/return-url/site/mercury
	lis_course_offering_sourcedid: mercury
	lis_course_section_sourcedid: mercury
	lis_outcome_service_url: http://localhost:8080/imsblis/service/
	lis_person_name_family: Administrator
	lis_person_name_full: Sakai Administrator
	lis_person_name_given: Sakai
	lis_person_sourcedid: admin
	lti_message_type: basic-lti-launch-request
	lti_version: LTI-1p0
	resource_link_description: Tsugi Breakout
	resource_link_id: content:3
	resource_link_title: Tsugi Breakout
	roles: Instructor,Administrator,urn:lti:instrole:ims/lis/Administrator,urn:lti:sysrole:ims/lis/Administrator
	user_id: admin
			 */

			String context_id = ltiProps.getProperty(BasicLTIConstants.CONTEXT_ID);
			String user_id = (String) ltiProps.getProperty(BasicLTIConstants.USER_ID);

			// Lets make a JWT from the LTI 1.x data
			boolean deepLink = false;
			SakaiLaunchJWT lj = new SakaiLaunchJWT();
			lj.target_link_uri = launch_url;  // The actual launch URL

			// See if we have a lineItem associated with this launch in case we need it later

			SakaiLineItem sakaiLineItem = null;
			if ( content != null ) {
				String lineItemStr = (String) content.get(LTIService.LTI_LINEITEM);
				sakaiLineItem = LineItemUtil.parseLineItem(lineItemStr);
			}

			String messageTypeParm = req.getParameter(MESSAGE_TYPE_PARAMETER);
			if ( MESSAGE_TYPE_PARAMETER_PRIVACY.equals(messageTypeParm)) {
				lj.message_type = LaunchJWT.MESSAGE_TYPE_LTI_DATA_PRIVACY_LAUNCH_REQUEST;
			} else if ( MESSAGE_TYPE_PARAMETER_CONTENT_REVIEW.equals(messageTypeParm)) {
				lj.message_type = LaunchJWT.MESSAGE_TYPE_LTI_SUBMISSION_REVIEW_REQUEST;
				if ( sakaiLineItem != null && sakaiLineItem.submissionReview != null && StringUtils.isNotEmpty(sakaiLineItem.submissionReview.url) ) lj.target_link_uri = sakaiLineItem.submissionReview.url;
			} else if ( BasicLTIConstants.LTI_MESSAGE_TYPE_CONTENTITEMSELECTIONREQUEST.equals(ltiProps.getProperty(BasicLTIConstants.LTI_MESSAGE_TYPE)) ) {
				lj.message_type = LaunchJWT.MESSAGE_TYPE_DEEP_LINK;
				deepLink = true;
			}

			lj.launch_presentation.css_url = ltiProps.getProperty(BasicLTIConstants.LAUNCH_PRESENTATION_CSS_URL);
			lj.locale = ltiProps.getProperty(BasicLTIConstants.LAUNCH_PRESENTATION_LOCALE);
			lj.launch_presentation.return_url = ltiProps.getProperty(BasicLTIConstants.LAUNCH_PRESENTATION_RETURN_URL);
			lj.audience = client_id;
			lj.issuer = getIssuer(site_id);
			lj.subject = getSubject(user_id, context_id);

			// The name and email info have been checked for release value in addUserInfo
			lj.name = ltiProps.getProperty(BasicLTIConstants.LIS_PERSON_NAME_FULL);
			lj.given_name = ltiProps.getProperty(BasicLTIConstants.LIS_PERSON_NAME_GIVEN);
			lj.family_name = ltiProps.getProperty(BasicLTIConstants.LIS_PERSON_NAME_FAMILY);
			lj.email = ltiProps.getProperty(BasicLTIConstants.LIS_PERSON_CONTACT_EMAIL_PRIMARY);

			lj.nonce = toolProps.getProperty("nonce");
			lj.issued = new Long(System.currentTimeMillis() / 1000L);
			lj.expires = lj.issued + 3600L;
			lj.deployment_id = getDeploymentId(context_id);

			String lti1_roles = upgradeRoleString(ltiProps.getProperty("roles"));
			if (lti1_roles != null ) {
				Set<String> roleSet = new HashSet<String>();
				roleSet.addAll(Arrays.asList(lti1_roles.split(",")));
				lj.roles.addAll(roleSet);
			} else {
				lj.roles.add(LaunchJWT.ROLE_LEARNER);
			}

			String resource_link_id = ltiProps.getProperty(BasicLTIConstants.RESOURCE_LINK_ID);
			if ( resource_link_id != null ) {
				lj.resource_link = new ResourceLink();
				lj.resource_link.id = resource_link_id;
				lj.resource_link.title = ltiProps.getProperty(BasicLTIConstants.RESOURCE_LINK_TITLE);
				lj.resource_link.description = ltiProps.getProperty(BasicLTIConstants.RESOURCE_LINK_DESCRIPTION);
			}

			// Construct the LTI 1.1 -> LTIAdvantage transition claim
			// https://www.imsglobal.org/spec/lti/v1p3/migr#lti-1-1-migration-claim
			String oauth_consumer_key = (String) tool.get(LTIService.LTI_CONSUMERKEY);
			String oauth_secret = (String) tool.get(LTIService.LTI_SECRET);
			oauth_secret = decryptSecret(oauth_secret);
			if ( oauth_consumer_key != null && oauth_secret != null ) {
				lj.lti11_transition = new LTI11Transition();
				lj.lti11_transition.user_id = user_id;
				lj.lti11_transition.oauth_consumer_key = oauth_consumer_key;
				String oauth_signature = LTI13Util.signLTI11Transition(lj, oauth_secret);
				if ( oauth_signature != null ) {
					lj.lti11_transition.oauth_consumer_key_sign = oauth_signature;
				} else {
					lj.lti11_transition = null;
				}
			}

			// Load up the context data
			lj.context = new Context();
			lj.context.id = context_id;
			lj.context.label = ltiProps.getProperty(BasicLTIConstants.CONTEXT_LABEL);
			lj.context.title = ltiProps.getProperty(BasicLTIConstants.CONTEXT_TITLE);
			lj.context.type.add(Context.COURSE_OFFERING);

			lj.tool_platform = new ToolPlatform();
			lj.tool_platform.name = "Sakai";
			lj.tool_platform.version = ltiProps.getProperty(BasicLTIConstants.TOOL_CONSUMER_INFO_VERSION);
			lj.tool_platform.product_family_code = ltiProps.getProperty(BasicLTIConstants.TOOL_CONSUMER_INFO_PRODUCT_FAMILY_CODE);
			lj.tool_platform.url = ltiProps.getProperty(BasicLTIConstants.TOOL_CONSUMER_INSTANCE_URL);
			lj.tool_platform.description = ltiProps.getProperty(BasicLTIConstants.TOOL_CONSUMER_INSTANCE_DESCRIPTION);

			LaunchLIS lis = new LaunchLIS();
			lis.person_sourcedid = ltiProps.getProperty(BasicLTIConstants.LIS_PERSON_SOURCEDID);
			lis.course_offering_sourcedid = ltiProps.getProperty(BasicLTIConstants.LIS_COURSE_OFFERING_SOURCEDID);
			lis.course_section_sourcedid = ltiProps.getProperty(BasicLTIConstants.LIS_COURSE_SECTION_SOURCEDID);
			lis.version = new ArrayList<>();
			lis.version.add("1.0.0");
			lis.version.add("1.1.0");
			lj.lis = lis;

			String for_user = req.getParameter(FOR_USER);
			if ( for_user != null ) {
				ForUser forUser = new ForUser();
				forUser.user_id =  getSubject(for_user, context_id);
				lj.for_user = forUser;
			}

			lj.custom = new TreeMap<>();
			for (Map.Entry<Object, Object> entry : ltiProps.entrySet()) {
				String custom_key = (String) entry.getKey();
				String custom_val = (String) entry.getValue();
				if (!custom_key.startsWith("custom_")) {
					continue;
				}
				custom_key = custom_key.substring(7);
				lj.custom.put(custom_key, custom_val);
			}

			int allowOutcomes = getInt(tool.get(LTIService.LTI_ALLOWOUTCOMES));
			int allowRoster = getInt(tool.get(LTIService.LTI_ALLOWROSTER));
			int allowSettings = getInt(tool.get(LTIService.LTI_ALLOWSETTINGS_EXT));
			int allowLineItems = getInt(tool.get(LTIService.LTI_ALLOWLINEITEMS));

			String sourcedid = ltiProps.getProperty("lis_result_sourcedid");

			if (sourcedid != null) {
				BasicOutcome outcome = new BasicOutcome();
				outcome.lis_result_sourcedid = ltiProps.getProperty("lis_result_sourcedid");
				outcome.lis_outcome_service_url = ltiProps.getProperty("lis_outcome_service_url");
				lj.basicoutcome = outcome;
			}

			String signed_placement = null;
			if (placement_secret != null && resource_link_id != null && context_id != null) {
				signed_placement = getSignedPlacement(context_id, resource_link_id, placement_secret);
			}

			if (signed_placement != null && (
				  ( (allowOutcomes != 0 && outcomesEnabled()) ||
					(allowLineItems != 0 && lineItemsEnabled()) )
				  )
				) {
				Endpoint endpoint = new Endpoint();
				endpoint.scope = new ArrayList<>();
				endpoint.scope.add(Endpoint.SCOPE_LINEITEM);

				if ( allowOutcomes != 0 && outcomesEnabled() ) {
					SakaiLineItem defaultLineItem = LineItemUtil.getDefaultLineItem(site, content);
					if ( defaultLineItem != null ) endpoint.lineitem = defaultLineItem.id;
				}
				if ( allowOutcomes != 0 && outcomesEnabled() ) {
					endpoint.lineitems = getOurServerUrl() + LTI13_PATH + "lineitems/" + signed_placement;
				}
				lj.endpoint = endpoint;
			}

			if (allowRoster != 0 && rosterEnabled() && signed_placement != null) {
				NamesAndRoles nar = new NamesAndRoles();
				nar.context_memberships_url = getOurServerUrl() + LTI13_PATH + "namesandroles/" + signed_placement;
				lj.names_and_roles = nar;
			}

			// Add Sakai Extensions from ltiProps
			SakaiExtension se = new SakaiExtension();
			se.copyFromPost(ltiProps);
			lj.sakai_extension = se;

			/*
				Extra fields for DeepLink
				lti_message_type=ContentItemSelectionRequest
				accept_copy_advice=false
				accept_media_types=application/vnd.ims.lti.v1.ltiResourceLink
				accept_multiple=false
				accept_presentation_document_targets=iframe,window
				accept_unsigned=true
				auto_create=true
				can_confirm=false
				content_item_return_url=http://localhost:8080/portal/tool/6bdb721d-07f9-445b-a973-2190b50654cc/sakai.basiclti.admin.helper.helper?eventSubmit_doContentItemResponse=Save&sakai.session=22702e53-60f3-45fd-b8db-a9d803eed3d4.MacBook-Pro-92.local&returnUrl=http%3A%2F%2Flocalhost%3A8080%2Fportal%2Fsite%2F92e7ddf2-1c60-486c-97ae-bc2ffbde8e67%2Ftool%2F4099b420-119a-4c39-9e05-0a933b2e5858%2FBltiPicker%3F3%26itemId%3D-1%26addBefore%3D&panel=PostContentItem&tool_id=13&sakai_csrf_token=458f712764cd597e96be99d2bab6d9da17d63c3834bc3770851a3d93ea8cdb83
				data={"remember":"always bring a towel"}

				"deep_link_return_url": "https://platform.example/deep_links",
				"accept_types": ["link", "file", "html", "ltiResourceLink", "image"],
				"accept_media_types": "image/:::asterisk:::,text/html",
				"accept_presentation_document_targets": ["iframe", "window", "embed"],
				"accept_multiple": true,
				"auto_create": true,
				"title": "This is the default title",
				"text": "This is the default text",
				"data": "csrftoken:c7fbba78-7b75-46e3-9201-11e6d5f36f53"
			*/

			if ( deepLink ) {
				SakaiDeepLink ci = new SakaiDeepLink();
				// accept_copy_advice is not in deep linking - files are to be copied - images maybe
				String accept_media_types = ltiProps.getProperty("accept_media_types");
				if ( ContentItem.MEDIA_LTILINKITEM.equals(accept_media_types) ) {
					ci.accept_types.add(DeepLink.ACCEPT_TYPE_LTILINK);
				} else if ( ContentItem.MEDIA_ALL.equals(accept_media_types) ) {
					ci.accept_types.add(DeepLink.ACCEPT_TYPE_LTILINK);
					ci.accept_types.add(DeepLink.ACCEPT_TYPE_LINK);
					ci.accept_types.add(DeepLink.ACCEPT_TYPE_IMAGE);
				} else {
					ci.accept_types.add(DeepLink.ACCEPT_TYPE_FILE);
					ci.accept_media_types = ltiProps.getProperty("accept_media_types");
				}
				ci.accept_multiple = "true".equals(ltiProps.getProperty("accept_multiple"));
				String target = ltiProps.getProperty("accept_presentation_document_targets");
				if ( target != null ) {
					String [] pieces = target.split(",");
					for (String piece : pieces) {
						ci.accept_presentation_document_targets.add(piece);
					}
				}

				String flow = req.getParameter("flow");
				if ( flow != null ) {
					ci.sakai_placement = flow;
					if ( SakaiDeepLink.PLACEMENT_LESSONS.equals(flow) ) {
						ci.sakai_accept_lineitem = Boolean.TRUE;
						ci.sakai_accept_available = Boolean.FALSE;
						ci.sakai_accept_submission = Boolean.FALSE;
					} else if ( SakaiDeepLink.PLACEMENT_ASSIGNMENT.equals(flow) ) {
						ci.sakai_accept_lineitem = Boolean.TRUE;
						ci.sakai_accept_available = Boolean.TRUE;
						ci.sakai_accept_submission = Boolean.TRUE;
					} else if ( SakaiDeepLink.PLACEMENT_EDITOR.equals(flow) ) {
						ci.sakai_accept_lineitem = Boolean.FALSE;
						ci.sakai_accept_available = Boolean.FALSE;
						ci.sakai_accept_submission = Boolean.FALSE;
					}
					ci.accept_lineitem = ci.sakai_accept_lineitem;
				}

				// Accept_unsigned is not in DeepLinking - they are signed JWTs
				ci.auto_create = "true".equals(ltiProps.getProperty("auto_create"));
				// can_confirm is not there
				ci.deep_link_return_url = ltiProps.getProperty(BasicLTIConstants.CONTENT_ITEM_RETURN_URL);
				ci.data = ltiProps.getProperty("data");
				lj.deep_link = ci;
			}

			String ljs = JacksonUtil.toString(lj);
			log.debug("ljs = {}", ljs);

			Key privateKey = LTI13Util.string2PrivateKey(platform_private);
			Key publicKey = LTI13Util.string2PublicKey(platform_public);

			if ( privateKey == null | publicKey == null ) {
				return postError("<p>" + getRB(rb, "error.no.pki", "Public and/or Private Key(s) not configured.") + "</p>");
			}

			String kid = LTI13KeySetUtil.getPublicKID(publicKey);

			String jws = Jwts.builder().setHeaderParam("kid", kid).
					setPayload(ljs).signWith(privateKey).compact();

			log.debug("jws = {}", jws);

			String debugProperty = toolProps.getProperty(LTIService.LTI_DEBUG);
			boolean dodebug = BASICLTI_PORTLET_ON.equals(debugProperty) || "1".equals(debugProperty);
			if (log.isDebugEnabled()) {
				dodebug = true;
			}

			String state = toolProps.getProperty("state");
			state = StringUtils.trimToNull(state);

			// This is a comma separated list of valid redirect URLs - lame as heck
			String lti13_oidc_redirect = toNull((String) tool.get(LTIService.LTI13_OIDC_REDIRECT));

			// If we have been told to send this to a redirect_uri instead of a launch...
			String redirect_uri = req.getParameter("redirect_uri");
			if ( redirect_uri != null && lti13_oidc_redirect != null ) {
				if ( lti13_oidc_redirect.indexOf(redirect_uri) >= 0 ) {
					launch_url = redirect_uri;
				}
			}

			Integer form_id = jws.hashCode();
			String html = "<form action=\"" + launch_url + "\" id=\"jwt-launch-"+ form_id + "\" method=\"POST\">\n"
					+ "    <input type=\"hidden\" name=\"id_token\" value=\"" + BasicLTIUtil.htmlspecialchars(jws) + "\" />\n";

			if ( state != null ) {
				html += "    <input type=\"hidden\" name=\"state\" value=\"" + BasicLTIUtil.htmlspecialchars(state) + "\" />\n";
			}

			if ( dodebug ) {
				html += "    <input type=\"submit\" value=\"Proceed with LTI 1.3 Launch\" />\n</form>\n";
			}
			html += "    </form>\n";

			if ( ! dodebug ) {
				String launch_error = rb.getString("error.submit.timeout")+" "+launch_url;
				html += "<script>\n";
				html += "setTimeout(function() { document.getElementById(\"jwt-launch-" + form_id + "\").submit(); }, 200 );\n";
				html += "setTimeout(function() { alert(\""+BasicLTIUtil.htmlspecialchars(launch_error)+"\"); }, 4000);\n";
				html += "</script>\n";
			} else {
				html += "<p>\n--- Unencoded JWT:<br/>"
						+ BasicLTIUtil.htmlspecialchars(ljs)
						+ "</p>\n<p>\n--- State:<br/>"
						+ BasicLTIUtil.htmlspecialchars(state)
						+ "</p>\n<p>\n--- Encoded JWT:<br/>"
						+ BasicLTIUtil.htmlspecialchars(jws)
						+ "</p>\n";
			}
			String[] retval = {html, launch_url};
			return retval;
		}

		public static String getSourceDID(User user, Placement placement, Properties config) {
			String placementSecret = toNull(getCorrectProperty(config, BASICLTI_PORTLET_PLACEMENTSECRET, placement));
			if (placementSecret == null) {
				return null;
			}
			return getSourceDID(user, placement.getId(), placementSecret);
		}

		public static String getSourceDID(User user, String placeStr, String placementSecret) {
			if (placementSecret == null) {
				return null;
			}
			String suffix = ":::" + user.getId() + ":::" + placeStr;
			String base_string = placementSecret + suffix;
			String signature = LegacyShaUtil.sha256Hash(base_string);
			return signature + suffix;
		}

		/*
		 * get a signed placement from a content item
		 */
		public static String getResourceLinkId(Map<String, Object> content) {
			String resource_link_id = "content:" + content.get(LTIService.LTI_ID);
			return resource_link_id;
		}

		public static String getSignedPlacement(String context_id, String resource_link_id, String placementSecret) {
			if (placementSecret == null) {
				return null;
			}
			String suffix = ":::" + context_id + ":::" + resource_link_id;
			String base_string = placementSecret + suffix;
			String signature = LegacyShaUtil.sha256Hash(base_string);
			return signature + suffix;
		}

		/*
		 * get a signed placement from a content item
		 */
		public static String getSignedPlacement(Map<String, Object> content) {
			String context_id = (String) content.get(LTIService.LTI_SITE_ID);
			String placement_secret = (String) content.get(LTIService.LTI_PLACEMENTSECRET);
			String resource_link_id = getResourceLinkId(content);
			String signed_placement = null;
			if ( placement_secret != null ) {
				signed_placement = getSignedPlacement(context_id, resource_link_id, placement_secret);
			}
			return signed_placement;
		}

		public static String trackResourceLinkID(Map<String, Object> oldContent) {
			boolean retval = false;

			String old_settings = (String) oldContent.get(LTIService.LTI_SETTINGS);
			JSONObject old_json = BasicLTIUtil.parseJSONObject(old_settings);
			String old_id_history = (String) old_json.get(LTIService.LTI_ID_HISTORY);

			String old_resource_link_id = getResourceLinkId(oldContent);

			String id_history = BasicLTIUtil.mergeCSV(old_id_history, null, old_resource_link_id);
			return id_history;
		}

		public static boolean trackResourceLinkID(Map<String, Object> newContent, Map<String, Object> oldContent) {
			boolean retval = false;

			String old_settings = (String) oldContent.get(LTIService.LTI_SETTINGS);
			JSONObject old_json = BasicLTIUtil.parseJSONObject(old_settings);
			String old_id_history = (String) old_json.get(LTIService.LTI_ID_HISTORY);

			String new_settings = (String) newContent.get(LTIService.LTI_SETTINGS);
			JSONObject new_json = BasicLTIUtil.parseJSONObject(new_settings);
			String new_id_history = (String) new_json.get(LTIService.LTI_ID_HISTORY);

			String old_resource_link_id = getResourceLinkId(oldContent);

			String id_history = BasicLTIUtil.mergeCSV(old_id_history, new_id_history, old_resource_link_id);
			if ( id_history.equals(new_id_history) ) return false;

			new_json.put(LTIService.LTI_ID_HISTORY, id_history);
			newContent.put(LTIService.LTI_SETTINGS, new_json.toString());
			return true;
		}

		public static String[] postError(String str) {
			String[] retval = {str};
			return retval;
		}

		public static String getRB(ResourceLoader rb, String key, String def) {
			if (rb == null) {
				return def;
			}
			return rb.getString(key, def);
		}

		// To make absolutely sure we never send an XSS, we clean these values
		public static void setProperty(Properties props, String key, String value) {
			if (value == null) {
				return;
			}
			if (props == null) {
				return;
			}
			value = Web.cleanHtml(value);
			if (value.trim().length() < 1) {
				return;
			}
			props.setProperty(key, value);
		}

		private static String getExternalRealmId(String siteId) {
			String realmId = SiteService.siteReference(siteId);
			String rv = null;
			try {
				AuthzGroup realm = ComponentManager.get(AuthzGroupService.class).getAuthzGroup(realmId);
				rv = realm.getProviderGroupId();
			} catch (GroupNotDefinedException e) {
				log.error("SiteParticipantHelper.getExternalRealmId: site realm not found {}", e.getMessage());
			}
			return rv;
		} // getExternalRealmId

		// Look through a series of secrets from the properties based on the launchUrl
		private static String getToolConsumerInfo(String launchUrl, String data) {
			String default_secret = ServerConfigurationService.getString("basiclti.consumer_instance_" + data, null);
			log.debug("launchUrl = {}", launchUrl);
			URL url;
			try {
				url = new URL(launchUrl);
			} catch (MalformedURLException e) {
				url = null;
			}

			if (url == null) {
				return default_secret;
			}
			String hostName = url.getHost();
			log.debug("host = {}", hostName);
			if (hostName == null || hostName.length() < 1) {
				return default_secret;
			}
			// Look for the property starting with the full name
			String org_info = ServerConfigurationService.getString("basiclti.consumer_instance_" + data + "." + hostName, null);
			if (org_info != null) {
				return org_info;
			}
			for (int i = 0; i < hostName.length(); i++) {
				if (hostName.charAt(i) != '.') {
					continue;
				}
				if (i > hostName.length() - 2) {
					continue;
				}
				String hostPart = hostName.substring(i + 1);
				String propName = "basiclti.consumer_instance_" + data + "." + hostPart;
				org_info = ServerConfigurationService.getString(propName, null);
				if (org_info != null) {
					return org_info;
				}
			}
			return default_secret;
		}

		// expected_oauth_key can be null - if it is non-null it must match the key in the request
		public static Object validateMessage(HttpServletRequest request, String URL,
				String oauth_secret, String expected_oauth_key) {
			oauth_secret = decryptSecret(oauth_secret);
			return BasicLTIUtil.validateMessage(request, URL, oauth_secret, expected_oauth_key);
		}

		// Returns:
		// String implies error
		// Boolean.TRUE - Sourcedid checks out
		// Boolean.FALSE - Sourcedid or secret fail
		public static Object checkSourceDid(String sourcedid, HttpServletRequest request,
				LTIService ltiService) {
			return handleGradebook(sourcedid, request, ltiService, false, false, null, null);
		}

		// Grade retrieval Map<String, Object> with "grade" => Double  and "comment" => String
		public static Object getGrade(String sourcedid, HttpServletRequest request,
				LTIService ltiService) {
			return handleGradebook(sourcedid, request, ltiService, true, false, null, null);
		}

		// Boolean.TRUE - Grade updated
		public static Object setGrade(String sourcedid, HttpServletRequest request,
				LTIService ltiService, Double grade, String comment) {
			return handleGradebook(sourcedid, request, ltiService, false, false, grade, comment);
		}

		// Boolean.TRUE - Grade deleted
		public static Object deleteGrade(String sourcedid, HttpServletRequest request,
				LTIService ltiService) {
			return handleGradebook(sourcedid, request, ltiService, false, true, null, null);
		}

	// Quite a long bit of code
	private static Object handleGradebook(String sourcedid, HttpServletRequest request,
			LTIService ltiService, boolean isRead, boolean isDelete,
			Double scoreGiven, String comment) {
		// Truncate this to the maximum length to insure no cruft at the end
		if (sourcedid.length() > 2048) {
			sourcedid = sourcedid.substring(0, 2048);
		}

		// Attempt to parse the sourcedid, any failure is fatal
		String placement_id = null;
		String signature = null;
		String user_id = null;
		try {
			int pos = sourcedid.indexOf(":::");
			if (pos > 0) {
				signature = sourcedid.substring(0, pos);
				String dec2 = sourcedid.substring(pos + 3);
				pos = dec2.indexOf(":::");
				user_id = dec2.substring(0, pos);
				placement_id = dec2.substring(pos + 3);
			}
		} catch (Exception e) {
			return "Unable to decrypt result_sourcedid=" + sourcedid;
		}

		log.debug("signature={} user_id={} placement_id={}", signature, user_id, placement_id);

		Properties normalProps = normalizePlacementProperties(placement_id, ltiService);
		if (normalProps == null) {
			return "Error retrieving result_sourcedid information";
		}

		String siteId = normalProps.getProperty(LTIService.LTI_SITE_ID);
		Site site;
		try {
			site = SiteService.getSite(siteId);
		} catch (IdUnusedException e) {
			return "Error retrieving result_sourcedid site: " + e.getLocalizedMessage();
		}

		// Check the message signature using OAuth
		String oauth_secret = normalProps.getProperty(LTIService.LTI_SECRET);
		log.debug("oauth_secret: {}", oauth_secret);
		oauth_secret = decryptSecret(oauth_secret);
		log.debug("oauth_secret (decrypted): {}", oauth_secret);

		String oauth_consumer_key = normalProps.getProperty(LTIService.LTI_CONSUMERKEY);
		log.debug("oauth_consumer_key: {}", oauth_consumer_key);

		String URL = getOurServletPath(request);

		// Validate the incoming message
		Object retval = validateMessage(request, URL, oauth_secret, oauth_consumer_key);
		if (retval instanceof String) {
			return retval;
		}

		// Check the signature of the sourcedid to make sure it was not altered
		String placement_secret = normalProps.getProperty(LTIService.LTI_PLACEMENTSECRET);
		if (placement_secret == null) {
			return "Could not find placement secret";
		}

		String pre_hash = placement_secret + ":::" + user_id + ":::" + placement_id;
		String received_signature = LegacyShaUtil.sha256Hash(pre_hash);
		log.debug("Received signature={} received={}", signature, received_signature);
		boolean matched = signature.equals(received_signature);

		String old_placement_secret = normalProps.getProperty(LTIService.LTI_OLDPLACEMENTSECRET);
		if (old_placement_secret != null && !matched) {
			pre_hash = placement_secret + ":::" + user_id + ":::" + placement_id;
			received_signature = LegacyShaUtil.sha256Hash(pre_hash);
			log.debug("Received signature II={} received={}", signature, received_signature);
			matched = signature.equals(received_signature);
		}

		if (!matched) {
			return "Sourcedid signature did not match";
		}

		// If we are not supposed to lookup or set the grade, we are done
		if (isRead == false && isDelete == false && scoreGiven == null) {
			return new Boolean(matched);
		}

		// Look up the gradebook column so we can find the max points
		GradebookService g = (GradebookService) ComponentManager
				.get("org.sakaiproject.service.gradebook.GradebookService");

		// Make sure the user exists in the site
		boolean userExistsInSite = false;
		try {
			Member member = site.getMember(user_id);
			if (member != null) {
				userExistsInSite = true;
			}
		} catch (Exception e) {
			log.warn("{} siteId={}, {}", e.getLocalizedMessage(), siteId, e);
			return "User not found in site";
		}

		// Make sure the placement is configured to receive grades
		String title = normalProps.getProperty(BASICLTI_PORTLET_ASSIGNMENT);
		log.debug("Column Title={}", title);
		if (title == null) {
			return "Gradebook column not set in placement";
		}

        // Load assignment if it exists
		org.sakaiproject.assignment.api.model.Assignment assignment;
        String contentKeyStr = normalProps.getProperty("contentKey");
        Long contentKey = getLongKey(contentKeyStr);
        if (contentKey > 0) {
                Map<String, Object> content = new TreeMap<String, Object> ();
                content.put(LTIService.LTI_ID, contentKey);
                assignment = getAssignment(site, content);
        } else {
                assignment = null;
		}

		// If we are an assignment, we call the AssignmentService to communicate grades, etc and let it
		// inform the gradebook.
		if ( assignment != null ) {
			Score scoreObj = new Score();
			scoreObj.scoreGiven = scoreGiven;
			scoreObj.scoreMaximum = 1.0;
			scoreObj.comment = comment;
			retval = handleAssignment(assignment, user_id, scoreObj);
			return retval;
		}

		// Now read, set, or delete the non-assignment grade...
		Session sess = SessionManager.getCurrentSession();

		SakaiLineItem lineItem = new SakaiLineItem();
		lineItem.scoreMaximum = 100.0D;
		org.sakaiproject.service.gradebook.shared.Assignment gradebookColumn = getGradebookColumn(site, user_id, title, lineItem);
		if (gradebookColumn == null) {
			log.warn("gradebookColumn or Id is null, cannot proceed with grading in site {} for column {}", siteId, title);
			return "Grade failure siteId=" + siteId;
		}

		try {
			// Indicate "who" is setting this grade - needs to be a real user account
			String gb_user_id = ServerConfigurationService.getString(
					"basiclti.outcomes.userid", "admin");
			String gb_user_eid = ServerConfigurationService.getString(
					"basiclti.outcomes.usereid", gb_user_id);
			sess.setUserId(gb_user_id);
			sess.setUserEid(gb_user_eid);
			if (isRead) {
				String actualGrade = g.getAssignmentScoreString(siteId, gradebookColumn.getId(), user_id);
				Double dGrade = null;
				if (actualGrade != null && actualGrade.length() > 0) {
					dGrade = new Double(actualGrade);
					dGrade = dGrade / gradebookColumn.getPoints();
				}
				CommentDefinition commentDef = g.getAssignmentScoreComment(siteId, gradebookColumn.getId(), user_id);
				Map<String, Object> retMap = new TreeMap<>();
				retMap.put("grade", dGrade);
				if (commentDef != null) {
					retMap.put("comment", commentDef.getCommentText());
				}
				retval = retMap;
			} else if (isDelete) {
				g.setAssignmentScoreString(siteId, gradebookColumn.getId(), user_id, null, "External Outcome");
				g.setAssignmentScoreComment(siteId, gradebookColumn.getId(), user_id, null);
				log.info("Delete Score site={} title={} user_id={}", siteId, title, user_id);
				retval = Boolean.TRUE;
			} else {
				String gradeI18n = getRoundedGrade(scoreGiven, gradebookColumn.getPoints());
				gradeI18n = (",").equals((ComponentManager.get(FormattedText.class)).getDecimalSeparator()) ? gradeI18n.replace(".",",") : gradeI18n;
				g.setAssignmentScoreString(siteId, gradebookColumn.getId(), user_id, gradeI18n, "External Outcome");
				g.setAssignmentScoreComment(siteId, gradebookColumn.getId(), user_id, comment);

				log.info("Stored Score={} title={} user_id={} score={}", siteId, title, user_id, scoreGiven);

				retval = Boolean.TRUE;
			}
		} catch (Exception e) {
			retval = "Grade failure " + e.getMessage() + " siteId=" + siteId;
			log.warn("handleGradebook Grade failure in site: {}, error: {}", siteId, e);
		} finally {
			sess.invalidate(); // Make sure to leave no traces
		}

		return retval;
	}

	// When lineitem_key is null we are the "default" lineitem associated with the content object
	// if the content item is associated with an assignment, we talk to the assignment API,
	// if the content item is not associated with an assignment, we talk to the gradebook API
	// If the scoreGiven is null, we are clearing out the grade value
	// Note that scoreObj.userId is subject, not userId
	public static Object handleGradebookLTI13(Site site,  Long tool_id, Map<String, Object> content, String userId,
			Long lineitem_key, Score scoreObj) {

		Object retval;
		String title;

		// An empty / null score given means to delete the score
		SakaiLineItem lineItem = new SakaiLineItem();
		String siteId = site.getId();

		org.sakaiproject.service.gradebook.shared.Assignment gradebookColumn;

		// Are we in the default lineitem for the content object?
		// Check if this is as assignment placement and handle it if it is
		if ( lineitem_key == null ) {
			org.sakaiproject.assignment.api.model.Assignment assignment = getAssignment(site, content);
			if ( assignment != null ) {
				retval = handleAssignment(assignment, userId, scoreObj);
				return retval;
			}
			title = (String) content.get(LTIService.LTI_TITLE);
			if (title == null || title.length() < 1) {
				log.error("Could not determine content title {}", content.get(LTIService.LTI_ID));
				return "Could not determine content title key="+content.get(LTIService.LTI_ID);
			}
			gradebookColumn = getGradebookColumn(site, userId, title, lineItem);
		} else {
			gradebookColumn = LineItemUtil.getColumnByKeyDAO(siteId, tool_id, lineitem_key);
			if ( gradebookColumn == null || gradebookColumn.getName() == null ) {
				log.error("Could not determine assignment_name title {}", content.get(LTIService.LTI_ID));
				return "Unable to load column for lineitem_key="+lineitem_key;
			}
			title = gradebookColumn.getName();
		}

		if (gradebookColumn == null) {
			log.warn("gradebookColumn or Id is null, cannot proceed with grading for site {}, title {}", siteId, title);
			return "Grade failure siteId=" + siteId;
		}

		// Send the grades to the gradebook
		Double scoreGiven = scoreObj.scoreGiven;
		String comment = scoreObj.comment;
		if ( scoreObj.scoreMaximum == null ) scoreObj.scoreMaximum = lineItem.scoreMaximum != null ? lineItem.scoreMaximum : 100D;
		Double scoreMaximum = scoreObj.scoreMaximum;
		log.debug("scoreGiven={} scoreMaximum={} userId={} comment={}", scoreGiven, scoreMaximum, userId, comment);

		// Look up the gradebook column so we can find the max points
		GradebookService g = (GradebookService) ComponentManager
				.get("org.sakaiproject.service.gradebook.GradebookService");

		// Fall through to send the grade to a gradebook column
		// Now read, set, or delete the grade...
		Session sess = SessionManager.getCurrentSession();

		try {
			// Indicate "who" is setting this grade - needs to be a real user account
			String gb_user_id = ServerConfigurationService.getString(
					"basiclti.outcomes.userid", "admin");
			String gb_user_eid = ServerConfigurationService.getString(
					"basiclti.outcomes.usereid", gb_user_id);
			sess.setUserId(gb_user_id);
			sess.setUserEid(gb_user_eid);
			if (scoreGiven == null) {
				g.setAssignmentScoreString(siteId, gradebookColumn.getId(), userId, null, "External Outcome");
				// Since LTI 13 uses update semantics on grade delete, we accept the comment if it is there
				g.setAssignmentScoreComment(siteId, gradebookColumn.getId(), userId, comment);

				log.info("Delete Score site={} title={} userId={}", siteId, title, userId);
				return Boolean.TRUE;
			} else {
				Double gradebookColumnPoints = gradebookColumn.getPoints();
				Double assignedGrade = null;
				if ( scoreMaximum == null || gradebookColumnPoints.equals(scoreMaximum) ) {
					assignedGrade = scoreGiven;
				} else {
					assignedGrade = (scoreGiven / scoreMaximum) * gradebookColumnPoints;
				}
				g.setAssignmentScoreString(siteId, gradebookColumn.getId(), userId, assignedGrade.toString(), "External Outcome");
				g.setAssignmentScoreComment(siteId, gradebookColumn.getId(), userId, comment);

				log.info("Stored Score={} title={} userId={} score={}", siteId, title, userId, scoreGiven);
				return Boolean.TRUE;
			}
		} catch (NumberFormatException | AssessmentNotFoundException | GradebookNotFoundException e) {
			retval = "Grade failure " + e.getMessage() + " siteId=" + siteId;
			log.warn("handleGradebook Grade failure in site: {}, error: {}", siteId, e);
		} finally {
			sess.invalidate(); // Make sure to leave no traces
		}
		return Boolean.FALSE;
	}

	public static org.sakaiproject.assignment.api.model.Assignment getAssignment(Site site, Map<String, Object> content) {

		Long contentId = getLongNull(content.get(LTIService.LTI_ID));
		if ( contentId == null ) return null;

		pushAdvisor();
		try {

			org.sakaiproject.assignment.api.AssignmentService assignmentService = ComponentManager.get(org.sakaiproject.assignment.api.AssignmentService.class);
			Collection<org.sakaiproject.assignment.api.model.Assignment> assignments = assignmentService.getAssignmentsForContext(site.getId());

			for (org.sakaiproject.assignment.api.model.Assignment a : assignments) {
				Integer assignmentContentId = a.getContentId();
				if ( assignmentContentId == null ) continue;
				if ( ! assignmentContentId.equals(contentId.intValue()) ) continue;
				return a;
			}
			return null;
		}  finally {
			popAdvisor();
		}
	}

	public static Object handleAssignment(org.sakaiproject.assignment.api.model.Assignment a, String userId, Score scoreObj) {

		Integer	scaledGrade = null;
		String stringGrade = null;

		// Scale up the score if appropriate
		if (scoreObj.scoreGiven != null) {
			Integer assignmentMax = a.getMaxGradePoint();
			Integer assignmentScale = a.getScaleFactor();
			// Sanity check - don't ever divide by zero
			if ( assignmentScale == 0 ) assignmentScale = 1;

			Integer incomingScoreGiven = new Double(scoreObj.scoreGiven * assignmentScale).intValue();
			Integer incomingScoreMax = new Double(scoreObj.scoreMaximum * assignmentScale).intValue();
			if ( incomingScoreMax == 0 ) incomingScoreMax = assignmentMax;
			if ( incomingScoreMax == 0 ) incomingScoreMax = 100 * assignmentScale;


			if ( incomingScoreMax.equals(assignmentMax) ) {
				scaledGrade = incomingScoreGiven;
			} else {
				scaledGrade = (incomingScoreGiven * assignmentMax ) / incomingScoreMax;
			}
			stringGrade = scaledGrade.toString();
		}

		org.sakaiproject.assignment.api.AssignmentService assignmentService = ComponentManager.get(org.sakaiproject.assignment.api.AssignmentService.class);
		org.sakaiproject.user.api.PreferencesService preferencesService  = ComponentManager.get(org.sakaiproject.user.api.PreferencesService.class);
		UserTimeService userTimeService = ComponentManager.get(UserTimeService.class);

		String activityProgress = scoreObj.activityProgress != null ? scoreObj.activityProgress : Score.ACTIVITY_COMPLETED ;
		String gradingProgress = scoreObj.gradingProgress != null ? scoreObj.gradingProgress : Score.GRADING_FULLYGRADED;

		User user;
		try {
			user = UserDirectoryService.getUser(userId);
		} catch (org.sakaiproject.user.api.UserNotDefinedException e) {
			return "Could not look up user "+userId;
		}

		pushAdvisor();
		try {
			org.sakaiproject.assignment.api.model.AssignmentSubmission submission = assignmentService.getSubmission(a.getId(), user);
			if ( submission == null ) {
				submission = assignmentService.addSubmission(a.getId(), user.getId());
			}

			StringBuilder logEntry = new StringBuilder();
			DateTimeFormatter dtf = DateTimeFormatter.RFC_1123_DATE_TIME
				.withZone(userTimeService.getLocalTimeZone(userId).toZoneId())
				.withLocale(preferencesService.getLocale(userId));
			logEntry.append(dtf.format(Instant.now()));

			logEntry.append(" LTI");

			if ( scaledGrade != null && scaledGrade > 0 ) {
				logEntry.append(" ");
				logEntry.append(scaledGrade.doubleValue()/a.getScaleFactor());
			}
			if ( StringUtils.isNotBlank(scoreObj.comment) ) {
				logEntry.append(" ");
				logEntry.append(scoreObj.comment);
			}

			// From IMS:
			// ACTIVITY_INITIALIZED ACTIVITY_STARTED ACTIVITY_INPROGRESS ACTIVITY_SUBMITTED ACTIVITY_COMPLETED
			// GRADING_PENDING GRADING_PENDINGMANUAL GRADING_FAILED GRADING_NOTREADY GRADING_FULLYGRADED

			// From Sakai
			// submission not Submitted | submission not Graded | DateCreated not equals DateModified = IN_PROGRESS
			// submission is Submitted | DateSubmitted exists | submission not Returned | User can't Grade = SUBMITTED
			// submission not Submitted | submission Graded | submission not Returned | User can Grade | Grade exists = GRADED

			Instant now = Instant.now();
			submission.setUserSubmission(true);
			submission.setFeedbackComment(scoreObj.comment);
			submission.setDateModified(now);

			// SAK-46548 - Any new LTI grade unchecks assignments "released to student"
			submission.setGradeReleased(false);

			// If we are in any of these states - set the grade to null
			if ( gradingProgress.equals(Score.GRADING_PENDING) || gradingProgress.equals(Score.GRADING_PENDINGMANUAL) ||
					gradingProgress.equals(Score.GRADING_FAILED) ||  gradingProgress.equals(Score.GRADING_NOTREADY) ) {
				submission.setGrade(null);
				submission.setGraded(false);
			} else {
				submission.setGrade(stringGrade);  // Which might also be null
				submission.setGraded(true);
			}

			if ( activityProgress.equals(Score.ACTIVITY_INITIALIZED) || activityProgress.equals(Score.ACTIVITY_STARTED) ||
					 activityProgress.equals(Score.ACTIVITY_INPROGRESS) ) {
				submission.setSubmitted(false);
				submission.setDateSubmitted(null);
			} else {
				submission.setSubmitted(true);
				submission.setDateSubmitted(now);
			}

			submission.getProperties().put(getNextSubmissionLogKey(submission), logEntry.toString());

			 try {
				assignmentService.updateSubmission(submission);
				log.info("Submitted submission={} userId={} log={}", submission.getId(), userId, logEntry.toString());
			} catch (org.sakaiproject.exception.PermissionException e) {
				log.warn("Could not update submission: {}, {}", submission.getId(), e);
				return "Could not update submission="+submission.getId()+" "+e;
			}
		} catch (org.sakaiproject.exception.PermissionException e) {
			log.warn("Could not process submission: {}, {}", a.getId(), e);
			return "Could not process submission assignment="+a.getId()+" "+e;
		}  finally {
			popAdvisor();
		}
		return Boolean.TRUE;
	}

	private static String getNextSubmissionLogKey(org.sakaiproject.assignment.api.model.AssignmentSubmission submission) {
		String keyPrefix = "log";
		Map<String, String> properties = submission.getProperties();
		List<Integer> keys = properties.keySet().stream()
				.filter(k -> k.startsWith("log"))
				.map(k -> new Integer(StringUtils.split(k, "log")[0]))
				.sorted()
				.collect(Collectors.toList());
		int next = keys.isEmpty() ? 0 : keys.get(keys.size() - 1) + 1;
		return keyPrefix + next;
	}

	public static org.sakaiproject.service.gradebook.shared.Assignment getGradebookColumn(Site site, String userId, String title, SakaiLineItem lineItem) {
		// Look up the gradebook column so we can find the max points
		GradebookService g = (GradebookService) ComponentManager
				.get("org.sakaiproject.service.gradebook.GradebookService");

		String siteId = site.getId();

		if ( lineItem == null ) lineItem = new SakaiLineItem();
		Double scoreMaximum = lineItem.scoreMaximum == null ? 100D : lineItem.scoreMaximum;

		org.sakaiproject.service.gradebook.shared.Assignment returnColumn = null;

		pushAdvisor();

		try {
			List gradeboolColumns = g.getAssignments(siteId);
			for (Iterator i = gradeboolColumns.iterator(); i.hasNext();) {
				org.sakaiproject.service.gradebook.shared.Assignment aColumn = (org.sakaiproject.service.gradebook.shared.Assignment) i.next();

				if (title.trim().equalsIgnoreCase(aColumn.getName().trim())) {
					returnColumn = aColumn;
					break;
				}
			}
		} catch (GradebookNotFoundException e) {
			returnColumn = null; // Just to make double sure
		} finally {
			popAdvisor();
		}

		// Attempt to add column to grade book
		if (returnColumn == null && g.isGradebookDefined(siteId)) {
			pushAdvisor();
			try {
				returnColumn = new org.sakaiproject.service.gradebook.shared.Assignment();
				returnColumn.setPoints(scoreMaximum);
				returnColumn.setExternallyMaintained(false);
				returnColumn.setName(title);
				// SAK-40043
				Boolean releaseToStudent = lineItem.releaseToStudent == null ? Boolean.TRUE : lineItem.releaseToStudent; // Default to true
				Boolean includeInComputation = lineItem.includeInComputation == null ? Boolean.TRUE : lineItem.includeInComputation; // Default true
				returnColumn.setReleased(releaseToStudent); // default true
				returnColumn.setUngraded(! includeInComputation); // default false
				Long gradebookColumnId = g.addAssignment(siteId, returnColumn);
				returnColumn.setId(gradebookColumnId);
				log.info("Added gradebook column: {} with Id: {}", title, gradebookColumnId);
			} catch (ConflictingAssignmentNameException e) {
				log.warn("ConflictingAssignmentNameException while adding gradebook column {}", e.getMessage());
				returnColumn = null; // Just to make sure
			} catch (Exception e) {
				log.warn("GradebookNotFoundException (may be because GradeBook has not yet been added to the Site) {}", e.getMessage());
				returnColumn = null; // Just to make double sure
			} finally {
				popAdvisor();
			}
		}

		if (returnColumn == null || returnColumn.getId() == null) {
			log.warn("returnColumn or Id is null.");
			returnColumn = null;
		}

		return returnColumn;
	}

	// Returns scoreGiven * points rounded to 2 digits (as a String)
	// Used for testing and to avoid precision problems
	public static String getRoundedGrade(Double scoreGiven, Double points) throws Exception {
		if (scoreGiven < 0.0 || scoreGiven > 1.0) {
			throw new Exception("Grade out of range");
		}
		scoreGiven = scoreGiven * points;
		scoreGiven = Precision.round(scoreGiven, 2);
		return String.valueOf(scoreGiven);
	}

	// Extract the necessary properties from a placement that emulate a new-generation tool
	// The returned properties should have indexes and values as if they were a new-generation
	// tool
	public static Properties normalizePlacementProperties(String placement_id, LTIService ltiService) {
		// These are the fields from a placement - they are not an exact match
		// for the fields in tool/content
		String[] fieldList = {BASICLTI_PORTLET_KEY, LTIService.LTI_SECRET, BASICLTI_PORTLET_PLACEMENTSECRET,
			BASICLTI_PORTLET_OLDPLACEMENTSECRET, BASICLTI_PORTLET_ALLOWSETTINGS,
			BASICLTI_PORTLET_ASSIGNMENT, BASICLTI_PORTLET_ALLOWROSTER, BASICLTI_PORTLET_RELEASENAME, BASICLTI_PORTLET_RELEASEEMAIL,
			BASICLTI_PORTLET_TOOLSETTING};

		Properties retval = new Properties();

		String siteId;
		if (isPlacement(placement_id)) {
			ToolConfiguration placement;
			Properties config;
			try {
				placement = SiteService.findTool(placement_id);
				config = placement.getConfig();
				siteId = placement.getSiteId();
			} catch (Exception e) {
				log.debug("Error normalizePlacementProperties: {}, error: {}", e.getLocalizedMessage(), e);
				return null;
			}
			retval.setProperty("placementId", placement_id);
			retval.setProperty(LTIService.LTI_SITE_ID, siteId);
			for (String field : fieldList) {
				String value = toNull(getCorrectProperty(config, field, placement));
				if (field.equals(BASICLTI_PORTLET_ALLOWSETTINGS)) {
					field = LTIService.LTI_ALLOWSETTINGS_EXT;
				}
				if (field.equals(BASICLTI_PORTLET_ALLOWROSTER)) {
					field = LTIService.LTI_ALLOWROSTER;
				}
				if (field.equals(BASICLTI_PORTLET_ASSIGNMENT)) {
					String outval = outcomesEnabled() && StringUtils.isNotEmpty(value) ? "1" : "0";
				}
				if (field.equals(BASICLTI_PORTLET_RELEASENAME)) {
					field = LTIService.LTI_SENDNAME;
				}
				if (field.equals(BASICLTI_PORTLET_RELEASEEMAIL)) {
					field = LTIService.LTI_SENDEMAILADDR;
				}
				// These field names are the same but for completeness..
				if (field.equals(BASICLTI_PORTLET_OLDPLACEMENTSECRET)) {
					field = LTIService.LTI_OLDPLACEMENTSECRET;
				}
				// These field names are the same but for completeness..
				if (field.equals(BASICLTI_PORTLET_PLACEMENTSECRET)) {
					field = LTIService.LTI_PLACEMENTSECRET;
				}
				if (value == null) {
					continue;
				}
				if (field.equals(BASICLTI_PORTLET_KEY)) {
					field = LTIService.LTI_CONSUMERKEY;
				}
				retval.setProperty(field, value);
			}
		} else { // Get information from content item
			Map<String, Object> content;
			Map<String, Object> tool;

			String contentStr = placement_id.substring(8);
			Long contentKey = getLongKey(contentStr);
			if (contentKey < 0) {
				return null;
			}

			// Leave off the siteId - bypass all checking - because we need to
			// finde the siteId from the content item
			content = ltiService.getContentDao(contentKey);
			if (content == null) {
				return null;
			}
			siteId = (String) content.get(LTIService.LTI_SITE_ID);
			if (siteId == null) {
				return null;
			}

			retval.setProperty("contentKey", contentStr);
			retval.setProperty(LTIService.LTI_SITE_ID, siteId);

			Long toolKey = getLongKey(content.get(LTIService.LTI_TOOL_ID));
			if (toolKey < 0) {
				return null;
			}
			tool = ltiService.getToolDao(toolKey, siteId);
			if (tool == null) {
				return null;
			}

			// Adjust the content items based on the tool items
			ltiService.filterContent(content, tool);

			for (String formInput : LTIService.TOOL_MODEL) {
				Properties info = parseFormString(formInput);
				String field = info.getProperty("field", null);
				String type = info.getProperty("type", null);
				Object o = tool.get(field);
				if (o instanceof String) {
					retval.setProperty(field, (String) o);
					continue;
				}
				if ("checkbox".equals(type)) {
					int check = getInt(o);
					if (check == 1) {
						retval.setProperty(field, BASICLTI_PORTLET_ON);
					} else {
						retval.setProperty(field, BASICLTI_PORTLET_OFF);
					}
				}
			}

			for (String formInput : LTIService.CONTENT_MODEL) {
				Properties info = parseFormString(formInput);
				String field = info.getProperty("field", null);
				String type = info.getProperty("type", null);
				Object o = content.get(field);
				if (o instanceof String) {
					retval.setProperty(field, (String) o);
					continue;
				}
				if ("checkbox".equals(type)) {
					int check = getInt(o);
					if (check == 1) {
						retval.setProperty(field, BASICLTI_PORTLET_ON);
					} else {
						retval.setProperty(field, BASICLTI_PORTLET_OFF);
					}
				}
			}
			String aTitle = (String) content.get("title");
			retval.setProperty(BASICLTI_PORTLET_ASSIGNMENT, aTitle.trim());
		}
		return retval;
	}

	/**
	 * getLaunchCodeKey - Return the launch code key for a content item
	 */
	public static String getLaunchCodeKey(Map<String, Object> content) {
		int id = getInt(content.get(LTIService.LTI_ID));
		return SESSION_LAUNCH_CODE + id;
	}

	/**
	 * getLaunchCode - Return the launch code for a content item
	 */
	public static String getLaunchCode(Map<String, Object> content) {
		/*
		long now = (new java.util.Date()).getTime();
		int id = getInt(content.get(LTIService.LTI_ID));
		String placement_secret = (String) content.get(LTIService.LTI_PLACEMENTSECRET);
		String base_string = id + ":" + now + ":" + placement_secret;
		String signature = LegacyShaUtil.sha256Hash(base_string);
		String retval = id + ":" + now + ":" + signature;
		*/
		String content_id = content.get(LTIService.LTI_ID).toString();
		String placement_secret = (String) content.get(LTIService.LTI_PLACEMENTSECRET);
		String retval = LTI13Util.timeStampSign(content_id, placement_secret);
		return retval;
	}

	/**
	 * checkLaunchCode - check to see if a launch code is properly signed and not expired
	 */
	public static boolean checkLaunchCode(Map<String, Object> content, String launch_code) {
		String content_id = content.get(LTIService.LTI_ID).toString();
		// Make sure that the token belongs to this content item
		if ( ! launch_code.contains(":"+content_id+":") ) return false;
		String placement_secret = (String) content.get(LTIService.LTI_PLACEMENTSECRET);
		int delta = 5*60*60; // Five minutes
		boolean retval = LTI13Util.timeStampCheckSign(launch_code, placement_secret, delta);
		return retval;
	}

	public static boolean isPlacement(String placement_id) {
		if (placement_id == null) {
			return false;
		}
		return !(placement_id.startsWith("content:") && placement_id.length() > 8);
	}

	// Since ServerConfigurationService.getServerUrl() is wonky because it sometimes looks
	// at request.getServerName() instead of the serverUrl property we have our own
	// priority to determine our current url.
	// BLTI-273
	public static String getOurServerUrl() {
		String ourUrl = ServerConfigurationService.getString("sakai.lti.serverUrl");
		if (ourUrl == null || ourUrl.equals("")) {
			ourUrl = ServerConfigurationService.getString("serverUrl");
		}
		if (ourUrl == null || ourUrl.equals("")) {
			ourUrl = ServerConfigurationService.getServerUrl();
		}
		if (ourUrl == null || ourUrl.equals("")) {
			ourUrl = "http://127.0.0.1:8080";
		}

		if (ourUrl.endsWith("/") && ourUrl.length() > 2) {
			ourUrl = ourUrl.substring(0, ourUrl.length() - 1);
		}

		return ourUrl;
	}

	public static String getOurServletPath(HttpServletRequest request) {
		String URLstr = request.getRequestURL().toString();
		String retval = URLstr.replaceFirst("^https??://[^/]*", getOurServerUrl());
		return retval;
	}

	public static String[] positional = {"field", "type"};

	public static Properties parseFormString(String str) {
		Properties op = new Properties();
		String[] pairs = str.split(":");
		int i = 0;
		for (String s : pairs) {
			String[] kv = s.split("=");
			if (kv.length == 2) {
				op.setProperty(kv[0], kv[1]);
			} else if (kv.length == 1 && i < positional.length) {
				op.setProperty(positional[i++], kv[0]);
			} else {
				// TODO : Logger something here
			}
		}
		return op;
	}

	// Refactored into tsugi-util - mark as legacy later
	public static String toNull(String str) {
		return LTI13Util.toNull(str);
	}

	public static int getInt(Object o) {
		return LTI13Util.getInt(o);
	}

	public static Long getLongKey(Object key) {
		return LTI13Util.getLongKey(key);
	}

	public static URL getUrlOrNull(String urlString) {
		if ( urlString == null ) return null;
		try
		{
			URL url = new URL(urlString);
			url.toURI();
			return url;
		} catch (Exception exception) {
			return null;
		}
	}

	public static String stripOffQuery(String urlString)
	{
		if ( urlString == null ) return null;
		try {
			URIBuilder uriBuilder = new URIBuilder(urlString);
			uriBuilder.removeQuery();
			return uriBuilder.build().toString();
		} catch(java.net.URISyntaxException e) {
			return null;
		}
	}

	public static Map<String, Object> findBestToolMatch(boolean global, String launchUrl, List<Map<String,Object>> tools)
	{
		boolean local = ! global;  // Makes it easier to read :)

		// First we look for a tool with an exact match
		for ( Map<String,Object> tool : tools ) {
			String toolLaunch = (String) tool.get(LTIService.LTI_LAUNCH);
			String toolSite = (String) tool.get(LTIService.LTI_SITE_ID);

			if ( local && StringUtils.stripToNull(toolSite) == null ) continue;
			if ( global && StringUtils.stripToNull(toolSite) != null ) continue;

			if ( launchUrl != null && launchUrl.equals(toolLaunch) ) {
				log.debug("Matched exact tool {}={}", launchUrl, toolLaunch);
				return tool;
			}
		}

		// Next we snip off the query string and check again
		String launchUrlBase = stripOffQuery(launchUrl);

		// Look for tool with an query-less match
		// https://www.py4e.com/mod/gift/
		for ( Map<String,Object> tool : tools ) {
			String toolLaunchBase = stripOffQuery((String) tool.get(LTIService.LTI_LAUNCH));
			String toolSite = (String) tool.get(LTIService.LTI_SITE_ID);

			if ( local && StringUtils.stripToNull(toolSite) == null ) continue;
			if ( global && StringUtils.stripToNull(toolSite) != null ) continue;

			if ( launchUrlBase != null && launchUrlBase.equals(toolLaunchBase) ) {
				log.debug("Matched query-free tool {}={}", launchUrl, toolLaunchBase);
				return tool;
			}
		}

		// Find the longest prefix
		// https://www.py4e.com/mod/   <-- Selected
		// https://www.py4e.com/
		String bestPrefix = "";
		Map<String,Object> bestTool = null;
		for ( Map<String,Object> tool : tools ) {
			String toolLaunch = (String) tool.get(LTIService.LTI_LAUNCH);
			String toolSite = (String) tool.get(LTIService.LTI_SITE_ID);

			if ( local && StringUtils.stripToNull(toolSite) == null ) continue;
			if ( global && StringUtils.stripToNull(toolSite) != null ) continue;

			String prefix = StringUtils.getCommonPrefix(launchUrl, toolLaunch);
			if ( prefix.length() > 0 && prefix.length() > bestPrefix.length() ) {
				bestTool = tool;
				bestPrefix = prefix;
			}
		}

		// We want at least the scheme and domain to match
		if ( bestTool != null ) {
			URL launchUrlObj = getUrlOrNull(launchUrl);
			URL prefixUrlObj = getUrlOrNull(bestPrefix);
			if ( launchUrlObj != null && prefixUrlObj != null &&
				 launchUrlObj.getProtocol().equals(prefixUrlObj.getProtocol()) &&
				 launchUrlObj.getHost().equals(prefixUrlObj.getHost()) ){
				log.debug("Matched scheme / server {}={}", launchUrl, bestPrefix);
				return bestTool;
			}
		}

		// After all that - still nothing
		return null;

	}

	public static Map<String, Object> findBestToolMatch(String launchUrl, List<Map<String,Object>> tools)
	{
		// Example launch URL:
		// https://www.py4e.com/mod/gift/?quiz=02-Python.txt

		boolean global = true;
		Map<String,Object> retval = findBestToolMatch(!global, launchUrl, tools);

		if ( retval != null ) return retval;

		retval = findBestToolMatch(global, launchUrl, tools);
		return retval;
	}

	/**
	 * Copy an LTI Content Item from an old site into a new site
	 *
	 * This copies an LTI Content Item from one site to another site.
	 * The content item is linked to an appropriate tool entry - either in
	 * the new site or globally avalable.  If no suitable tool can be found,
	 * it is created.
	 *
	 * This routine uses Dao access and assumes the calling code has insured
	 * that the logged in user has appropriate permissions in both sites
	 * before calling this routine.
	 *
	 * @param  contentKey  The old content item key from the old site
	 * @param  siteId  The site id that the item is being copied from
	 * @param  oldSiteId  The site id that the item is being copied from
	 */
	public static Object copyLTIContent(Long contentKey, String siteId, String oldSiteId)
	{
		LTIService ltiService = (LTIService) ComponentManager.get("org.sakaiproject.lti.api.LTIService");
		Map<String, Object> ltiContent = ltiService.getContentDao(contentKey, oldSiteId, true);
		return copyLTIContent(ltiContent, siteId, oldSiteId);
	}

	/**
	 * Copy an LTI Content Item from an old site into a new site
	 *
	 * This copies an LTI Content Item from one site to another site.
	 * The content item is linked to an appropriate tool entry - either in
	 * the new site or globally avalable.  If no suitable tool can be found,
	 * it is created.
	 *
	 * This routine uses Dao access and assumes the calling code has insured
	 * that the logged in user has appropriate permissions in both sites
	 * before calling this routine.
	 *
	 * @param  ltiContent  The old content item from the old site
	 * @param  siteId  The site id that the item is being copied from
	 * @param  oldSiteId  The site id that the item is being copied from
	 */
	public static Object copyLTIContent(Map<String, Object> ltiContent, String siteId, String oldSiteId)
	{
		LTIService ltiService = (LTIService) ComponentManager.get("org.sakaiproject.lti.api.LTIService");

		// The ultimate tool id for the about to be created content item
		Long newToolId = null;

		// Check the tool_id - if the tool_id is global we are cool
		Long ltiToolId = getLong(ltiContent.get(LTIService.LTI_TOOL_ID));

		// Get the tool bypassing security
		Map<String, Object> ltiTool = ltiService.getToolDao(ltiToolId, siteId, true);
		if ( ltiTool == null ) {
			return null;
		}

		// Lets either verifiy we have a good tool or make a copy if needed
		String toolSiteId = (String) ltiTool.get(LTIService.LTI_SITE_ID);
		String toolLaunch = (String) ltiTool.get(LTIService.LTI_LAUNCH);
		// Global tools have no site id - the simplest case
		if ( toolSiteId == null ) {
			newToolId = ltiToolId;
		} else {
			// Check if we have a suitable tool already in the site
			List<Map<String,Object>> tools = ltiService.getTools(null,null,0,0,siteId);
			for ( Map<String,Object> tool : tools ) {
				String oldLaunch = (String) tool.get(LTIService.LTI_LAUNCH);
				if ( oldLaunch == null ) continue;
				if ( oldLaunch.equals(toolLaunch) ) {
					newToolId = getLong(tool.get(LTIService.LTI_ID));
					break;
				}
			}

			// If we don't have the tool in the new site, check the tools from the old site
			if ( newToolId == null ) {
				tools = ltiService.getToolsDao(null,null,0,0,oldSiteId, true);
				for ( Map<String,Object> tool : tools ) {
					String oldLaunch = (String) tool.get(LTIService.LTI_LAUNCH);
					if ( oldLaunch == null ) continue;
					if ( oldLaunch.equals(toolLaunch) ) {
						// Remove stuff that will be regenerated
						tool.remove(LTIService.LTI_SITE_ID);
						tool.remove(LTIService.LTI_CREATED_AT);
						tool.remove(LTIService.LTI_UPDATED_AT);
						Object newToolInserted = ltiService.insertTool(tool, siteId);
						if ( newToolInserted instanceof Long ) {
							newToolId = (Long) newToolInserted;
							log.debug("Copied tool={} from site={} tosite={} tool={}",ltiToolId,oldSiteId,siteId,newToolInserted);
							break;
						} else {
							log.warn("Could not insert tool - {}",newToolInserted);
							return null;
						}
					}
				}
			}

			if ( newToolId == null ) {
				log.warn("Could not copy tool, launch={}",toolLaunch);
				return null;
			}
		}

		// Finally insert the content item...
		Properties contentProps = new Properties();

		for (Map.Entry<String, Object> entry : ltiContent.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if ( value == null ) continue;
			contentProps.put(key, value.toString());
		}

		// Point at the correct (possibly the same) tool id
		contentProps.put(LTIService.LTI_TOOL_ID, newToolId.toString());

		// Track the resource_link_history
		Map<String, Object> updates = new HashMap<String, Object> ();
		String id_history = SakaiBLTIUtil.trackResourceLinkID(ltiContent);
		if ( StringUtils.isNotBlank(id_history) ) {
			String new_settings = (String) contentProps.get(LTIService.LTI_SETTINGS);
			JSONObject new_json = BasicLTIUtil.parseJSONObject(new_settings);
			new_json.put(LTIService.LTI_ID_HISTORY, id_history);
			contentProps.put(LTIService.LTI_SETTINGS, new_json.toString());
		}

		// Remove stuff that will be regenerated
		contentProps.remove(LTIService.LTI_SITE_ID);
		contentProps.remove(LTIService.LTI_CREATED_AT);
		contentProps.remove(LTIService.LTI_UPDATED_AT);

		// Most secrets are in the tool, it is rare to override in the content
		contentProps.remove(LTIService.LTI_SECRET);
		contentProps.remove("launch_url"); // Derived on retrieval

		Object result = ltiService.insertContent(contentProps, siteId);
		return result;
	}

	/**
	 * rotateToolKeys - If necessary - rotate tool keys
	 *
	 * This is controlled by a sakai.property
	 *
	 * lti.advantage.key.rotation.days=30
	 *
	 * For positive numbers this is the number of days before rotation happens
	 * For negative numbers it is the number of minutes before rotation happens (for testing)
	 * If it is zero, no rotation happens
	 *
	 */
	// SAK-45491 - Support LTI 1.3 Key Rotation
	public static void rotateToolKeys(Long toolKey, Map<String, Object> tool)
	{
		// Get services
		LTIService ltiService = (LTIService) ComponentManager.get("org.sakaiproject.lti.api.LTIService");
		org.sakaiproject.component.api.ServerConfigurationService serverConfigurationService =
			(org.sakaiproject.component.api.ServerConfigurationService) ComponentManager.get("org.sakaiproject.component.api.ServerConfigurationService");

		String daysStr = serverConfigurationService.getString(LTI_ADVANTAGE_KEY_ROTATION_DAYS, LTI_ADVANTAGE_KEY_ROTATION_DAYS_DEFAULT);
		int days = LTI13Util.getInt(daysStr);
		if ( days == 0 ) return;

		Instant now = Instant.now();
		Instant nextInstant = Foorm.getInstantUTC(tool.get(LTIService.LTI13_PLATFORM_PUBLIC_NEXT_AT));

		Map<String, Object> updates = new TreeMap<String, Object>();

		// Generate next Keypair in case we update
		KeyPair kp = LTI13Util.generateKeyPair();
		String pub = LTI13Util.getPublicEncoded(kp);
		String priv = LTI13Util.getPrivateEncoded(kp);
		priv = SakaiBLTIUtil.encryptSecret(priv);
		updates.put(LTIService.LTI13_PLATFORM_PUBLIC_NEXT, pub);
		updates.put(LTIService.LTI13_PLATFORM_PRIVATE_NEXT, priv);

		updates.put(LTIService.LTI13_PLATFORM_PUBLIC_NEXT_AT, Foorm.now());
		String siteId = null; // bypass

		if ( nextInstant == null ) {
			Object retval = ltiService.updateToolDao(toolKey, updates, siteId);
			if ( retval instanceof String) {
				log.error("Could not update tool={} retval={}", toolKey, retval);
			} else if ( nextInstant == null ) {
				log.info("Created future keys for tool={}", toolKey);
			}
		} else {
			long deltaDays = Duration.between(now, nextInstant).abs().toDays();
			long deltaMinutes = Duration.between(now, nextInstant).abs().toMinutes();

			// Should we rotate?
			if ( ( days > 0 && deltaDays >= days ) || ( days < -1 && deltaMinutes >= (-1*days) ) ) {

				// Only rotate next->current if next already contains valid values
				String publicSerializedNext = BasicLTIUtil.toNull((String) tool.get(LTIService.LTI13_PLATFORM_PUBLIC_NEXT));
				String privateSerializedNext = BasicLTIUtil.toNull((String) tool.get(LTIService.LTI13_PLATFORM_PRIVATE_NEXT));
				Key publicKeyNext = LTI13Util.string2PublicKey(publicSerializedNext);
				// Key privateKeyNext = (privateSerializedNext == null) ? null : LTI13Util.string2PrivateKey(SakaiBLTIUtil.decryptSecret(privateSerializedNext));
				Key privateKeyNext = LTI13Util.string2PrivateKey(SakaiBLTIUtil.decryptSecret(privateSerializedNext));

				if ( publicKeyNext != null && privateKeyNext != null ) {
					String publicSerializedCurrent = BasicLTIUtil.toNull((String) tool.get(LTIService.LTI13_PLATFORM_PUBLIC));

					updates.put(LTIService.LTI13_PLATFORM_PUBLIC, publicSerializedNext);
					updates.put(LTIService.LTI13_PLATFORM_PRIVATE, privateSerializedNext);
					updates.put(LTIService.LTI13_PLATFORM_PUBLIC_OLD, publicSerializedCurrent);
					updates.put(LTIService.LTI13_PLATFORM_PUBLIC_OLD_AT, Foorm.now());
				}

				// If the next key is somehow broken, at least we update the next values
				Object retval = ltiService.updateToolDao(toolKey, updates, siteId);
				if ( retval instanceof String) {
					log.error("Could not update tool={} retval={}", toolKey, retval);
				} else {
					log.info("Rotated keys for tool={} days={} delta={}", toolKey, days, deltaDays);
				}
			}
		}
	}

	public static Long getLong(Object key) {
		return LTI13Util.getLong(key);
	}

	public static Long getLongNull(Object key) {
		return LTI13Util.getLongNull(key);
	}

	public static Double getDoubleNull(Object key) {
		return LTI13Util.getDoubleNull(key);
	}

	public static String getStringNull(Object value) {
		return LTI13Util.getStringNull(value);
	}

	/**
	 * Setup a security advisor.
	 */
	public static void pushAdvisor() {
		// setup a security advisor
		SecurityService.pushAdvisor(new SecurityAdvisor() {
			public SecurityAdvice isAllowed(String userId, String function,
					String reference) {
				return SecurityAdvice.ALLOWED;
			}
		});
	}

	/**
	 * Remove our security advisor.
	 */
	public static void popAdvisor() {
		SecurityService.popAdvisor();
	}

	/**
	 * Converts a string from a comma-separated list of role maps to a
	 * Map<String, String>. Each role mapping in the string should be of the
	 * form sakairole1:ltirole1,sakairole2:ltirole2
	 * or sakairole4:ltirole4,ltirole5;sakairole6:ltirole6
	 * Using semicolon as the delimiter allows you to indicate more than one IMS role.
	 */
	public static Map<String, String> convertRoleMapPropToMap(String roleMapProp) {
		Map<String, String> roleMap = new HashMap<>();
		if (roleMapProp == null) {
			return roleMap;
		}

		String delim = ",";
		if( roleMapProp.contains(";") ) delim = ";";
		String[] roleMapPairs = roleMapProp.split(delim);
		for (String s : roleMapPairs) {
			if ( s.trim().length() < 1 ) continue;
			String[] roleMapPair = s.split(":", 2);
			if (roleMapPair.length != 2) continue;
			roleMap.put(roleMapPair[0].trim(), roleMapPair[1].trim());
		}
		return roleMap;
	}

	/**
	 *  Check if we are an LTI 1.3 launch or not
	 */
	public static boolean isLTI13(Map<String, Object> tool, Map<String, Object> content) {
		// 0=inherit from tool, 1=LTI 1.1, 2=LTI 1.3
		if ( content != null ) {
			Long contentLTI13 = getLong(content.get(LTIService.LTI13));
			if ( contentLTI13.equals(2L)) return true;
			if ( contentLTI13.equals(1L)) return false;
		}

		if ( tool == null ) return false;
		Long toolLTI13 = getLong(tool.get(LTIService.LTI13));
		return toolLTI13.equals(1L);
	}

	/**
	 * Get the secret based on inheritance rules
	 */
	public static String getSecret(Map<String, Object> tool, Map<String, Object> content) {
		String secret = null;
		if ( content != null ) {
			secret = (String) content.get(LTIService.LTI_SECRET);
		}
		if (secret == null) {
			secret = (String) tool.get(LTIService.LTI_SECRET);
		}
		return secret;
	}

	/**
	 * Get the consumer key based on inheritance rules
	 */
	public static String getKey(Map<String, Object> tool, Map<String, Object> content) {
		String key = null;
		if ( content != null ) {
			key = (String) content.get(LTIService.LTI_CONSUMERKEY);
		}
		if (key == null) {
			key = (String) tool.get(LTIService.LTI_CONSUMERKEY);
		}
		return key;
	}
}
