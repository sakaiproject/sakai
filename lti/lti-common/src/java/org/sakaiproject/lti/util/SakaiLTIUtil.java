/**
 * G
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
package org.sakaiproject.lti.util;

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
import java.util.Collections;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.time.Instant;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
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
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.lti.api.LTIExportService.ExportType;
import org.sakaiproject.lti.beans.LtiContentBean;
import org.sakaiproject.lti.beans.LtiToolBean;
import org.sakaiproject.portal.util.CSSUtils;
import org.sakaiproject.portal.util.ToolUtils;
import org.sakaiproject.grading.api.AssessmentNotFoundException;
// We don't import either of these to make sure we are never confused and always fully qualify
// import org.sakaiproject.grading.api.Assignment;   // We call this a "column"
// import org.sakaiproject.assignment.api.model.Assignment        // We call this an "assignment"
import org.sakaiproject.grading.api.CommentDefinition;
import org.sakaiproject.grading.api.ConflictingAssignmentNameException;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.grading.api.SortType;
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
import org.tsugi.lti.LTIConstants;
import org.tsugi.lti.LTIUtil;
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
import org.tsugi.lti13.objects.GroupService;
import org.tsugi.lti13.objects.PNPService;
import org.tsugi.lti13.objects.ResourceLink;
import org.tsugi.lti13.objects.ToolPlatform;
import org.tsugi.lti13.objects.ForUser;
import org.tsugi.lti13.objects.LTI11Transition;
import org.tsugi.lti.ContentItem;

import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import net.oauth.OAuth;

/**
 * Some Sakai Utility code for LTI This is mostly code to support the
 * Sakai conventions for making and launching LTI resources within Sakai.
 */
@SuppressWarnings("deprecation")
@Slf4j
public class SakaiLTIUtil {

	public static final boolean verbosePrint = false;

	// Property: If false(default), allows comment to be returned in an LTI 1.1 POX read outcome
	public static final String LTI_STRICT = "lti.strict";

	public static final String LTI_OUTCOMES_ENABLED = "lti.outcomes.enabled";
	public static final String LTI_OUTCOMES_ENABLED_DEFAULT = "true";
	public static final String LTI_ROSTER_ENABLED = "lti.roster.enabled";
	public static final String LTI_ROSTER_ENABLED_DEFAULT = "true";
	public static final String LTI_LINEITEMS_ENABLED = "lti.lineitems.enabled";
	public static final String LTI_LINEITEMS_ENABLED_DEFAULT = "true";
	public static final String LTI_CONSUMER_USERIMAGE_ENABLED = "lti.consumer.userimage.enabled";
	public static final String INCOMING_ROSTER_ENABLED = "lti.incoming.roster.enabled";
	public static final String LTI_ENCRYPTION_KEY = "lti.encryption.key";
	public static final String LTI_LAUNCH_SESSION_TIMEOUT = "lti.launch.session.timeout";
	public static final String LTI13_DEPLOYMENT_ID = "lti13.deployment_id";
	public static final String LTI13_DEPLOYMENT_ID_DEFAULT = "1"; // To match Moodle
	public static final String LTI_CUSTOM_SUBSTITION_PREFIX =  "lti.custom.substitution.";
	// SAK-45491 - Key rotation interval
	public static final String LTI_ADVANTAGE_KEY_ROTATION_DAYS = "lti.advantage.key.rotation.days";
	public static final String LTI_ADVANTAGE_KEY_ROTATION_DAYS_DEFAULT = "30";
	// SAK-45951 - Control post verify feature - default off
	public static final String LTI_ADVANTAGE_POST_VERIFY_ENABLED = "lti.advantage.post.verify.enabled";
	public static final String LTI_ADVANTAGE_POST_VERIFY_ENABLED_DEFAULT = "false";

	// These are the field names in old school portlet placements
	public static final String LTI_PORTLET_KEY = "key";
	public static final String LTI_PORTLET_ALLOWROSTER = "allowroster";
	public static final String LTI_PORTLET_OFF = "off";
	public static final String LTI_PORTLET_ON = "on";
	public static final String LTI_PORTLET_ASSIGNMENT = "assignment";
	public static final String LTI_PORTLET_RELEASENAME = "releasename";
	public static final String LTI_PORTLET_RELEASEEMAIL = "releaseemail";
	public static final String LTI_PORTLET_TOOLSETTING = "toolsetting";
	public static final String LTI_PORTLET_TOOLTITLE = "tooltitle";
	public static final String LTI_PORTLET_DESCRIPTION = "description";
	public static final String LTI_PORTLET_PLACEMENTSECRET = LTIService.LTI_PLACEMENTSECRET;
	public static final String LTI_PORTLET_OLDPLACEMENTSECRET = LTIService.LTI_OLDPLACEMENTSECRET;

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

	// Default Outbound Role Mapping - Sakai role to a comma-separated list of LTI Roles
	// https://www.imsglobal.org/spec/lti/v1p3/#role-vocabularies
	public static final String LTI_OUTBOUND_ROLE_MAP = "lti.outbound.role.map";
	public static final String LTI_OUTBOUND_ROLE_MAP_DEFAULT =
		// Admin is weird - tools that are simple see them as Instructors, more complex tools know both roles
		// And we send legacy LTI 1.0 roles as well
		"admin:Instructor,http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor," +
			"http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor," +
			"Administrator," +
			"http://purl.imsglobal.org/vocab/lis/v2/institution/person#Administrator," +
			"http://purl.imsglobal.org/vocab/lis/v2/system/person#Administrator;" +
		// !site.template roles
		"access:Learner,http://purl.imsglobal.org/vocab/lis/v2/membership#Learner;" +
		"maintain:Instructor,http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor;" +
		// !site.template.course roles
		"Instructor:Instructor,http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor;" +
		"Student:Learner,http://purl.imsglobal.org/vocab/lis/v2/membership#Learner;" +
		// A blank *is* part of the Sakai role and *is not* part of the LTI role
		"Teaching Assistant:TeachingAssistant,http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor#TeachingAssistant;" +
		// !site.template.lti roles - The simplest mapping :)
		"Learner:Learner,http://purl.imsglobal.org/vocab/lis/v2/membership#Learner;" +
		"Mentor:Mentor,http://purl.imsglobal.org/vocab/lis/v2/membership#Mentor;" +
		"ContentDeveloper:ContentDeveloper,http://purl.imsglobal.org/vocab/lis/v2/membership#ContentDeveloper;"
	;

	public static final String LTI_INBOUND_ROLE_MAP = "lti.inbound.role.map";
	public static final String LTI_INBOUND_ROLE_MAP_DEFAULT =
		"http://purl.imsglobal.org/vocab/lis/v2/membership#Administrator=Instructor,maintain;" +
		"http://purl.imsglobal.org/vocab/lis/v2/membership#ContentDeveloper=ContentDeveloper,Instructor,maintain;" +
		"http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor#TeachingAssistant=Teaching Assistant,Instructor,maintain;" +
		"http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor=Instructor,maintain;" +
		"http://purl.imsglobal.org/vocab/lis/v2/membership#Learner=Learner,Student,access;" +
		"http://purl.imsglobal.org/vocab/lis/v2/membership#Mentor=Mentor,Teaching Assistant,Learner,Student,access;" +

		"http://purl.imsglobal.org/vocab/lis/v2/membership#Manager=Manager,Guest,Student,access;" +
		"http://purl.imsglobal.org/vocab/lis/v2/membership#Member=Member,Guest,Student,access;" +
		"http://purl.imsglobal.org/vocab/lis/v2/membership#Officer=Officer,Guest,Student,access;"
	;

	public static final String LTI_LEGACY_ROLE_MAP = "lti.legacy.role.map";
	public static final String LTI_LEGACY_ROLE_MAP_DEFAULT =
		"Learner=http://purl.imsglobal.org/vocab/lis/v2/membership#Learner;" +
		"learner=http://purl.imsglobal.org/vocab/lis/v2/membership#Learner;" +
		"Instructor=http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor;" +
		"instructor=http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor;" +
		"TeachingAssistant=http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor#TeachingAssistant;" +
		"Mentor=http://purl.imsglobal.org/vocab/lis/v2/membership#Mentor;" +
		"ContentDeveloper=http://purl.imsglobal.org/vocab/lis/v2/membership#ContentDeveloper;" +
		"Administrator=http://purl.imsglobal.org/vocab/lis/v2/institution/person#Administrator;" +
		"urn:lti:sysrole:ims/lis/Administrator=http://purl.imsglobal.org/vocab/lis/v2/institution/person#Administrator;" +
		"urn:lti:instrole:ims/lis/Administrator=http://purl.imsglobal.org/vocab/lis/v2/institution/person#Administrator;"
	;

		public static boolean rosterEnabled() {
			String allowRoster = ServerConfigurationService.getString(LTI_ROSTER_ENABLED, LTI_ROSTER_ENABLED_DEFAULT);
			return "true".equals(allowRoster);
		}

		public static boolean outcomesEnabled() {
			String allowOutcomes = ServerConfigurationService.getString(LTI_OUTCOMES_ENABLED, LTI_OUTCOMES_ENABLED_DEFAULT);
			return "true".equals(allowOutcomes);
		}

		public static boolean lineItemsEnabled() {
			String allowLineItems = ServerConfigurationService.getString(LTI_LINEITEMS_ENABLED, LTI_LINEITEMS_ENABLED_DEFAULT);
			return "true".equals(allowLineItems);
		}

		// Retrieve the property from the placement configuration unless it
		// is overridden by the server configuration (i.e. sakai.properties)
		public static String getCorrectProperty(Properties config, String propName, Placement placement) {
			// Check for global overrides in properties

			if (LTI_PORTLET_ALLOWROSTER.equals(propName) && !rosterEnabled()) {
				return LTI_PORTLET_OFF;
			}

			// Check for explicit setting in properties
			String propertyName = placement.getToolId() + "." + propName;
			String propValue = ServerConfigurationService.getString(propertyName, null);
			if (StringUtils.isNotBlank(propValue)) {
				log.debug("Sakai.home {}={}", propName, propValue);
				return propValue;
			}

			// Take it from the placement
			return config.getProperty("imsti." + propName, null);
		}

		// Look at a Placement and come up with the launch urls, and
		// other launch parameters to drive the launch.
		private static boolean loadFromPlacement(Properties info, Properties launch, Placement placement) {
			Properties config = placement.getConfig();
			log.debug("Sakai properties={}", config);
			String launch_url = StringUtils.trimToNull(getCorrectProperty(config, LTIService.LTI_LAUNCH, placement));
			setProperty(info, "launch_url", launch_url);

			String secret = getCorrectProperty(config, LTIService.LTI_SECRET, placement);

			// LTI-195 - Compatibility mode for old-style encrypted secrets
			if (secret == null || secret.trim().length() < 1) {
				String eSecret = getCorrectProperty(config, "encryptedsecret", placement);
				if (StringUtils.isNotBlank(eSecret) ) {
					secret = eSecret.trim() + ":" + SimpleEncryption.CIPHER;
				}
			}

			setProperty(info, LTIService.LTI_SECRET, secret);

			// This is not "consumerkey" on purpose - we are mimicking the old placement model
			setProperty(info, LTI_PORTLET_KEY, getCorrectProperty(config, LTI_PORTLET_KEY, placement));
			setProperty(info, LTIService.LTI_DEBUG, getCorrectProperty(config, LTIService.LTI_DEBUG, placement));
			setProperty(info, LTIService.LTI_FRAMEHEIGHT, getCorrectProperty(config, LTIService.LTI_FRAMEHEIGHT, placement));
			setProperty(info, LTIService.LTI_NEWPAGE, getCorrectProperty(config, LTIService.LTI_NEWPAGE, placement));
			setProperty(info, LTIService.LTI_TITLE, getCorrectProperty(config, LTI_PORTLET_TOOLTITLE, placement));

			// Pull in and parse the custom parameters
			String customstr = StringUtils.trimToNull(getCorrectProperty(config, LTIService.LTI_CUSTOM, placement));
			parseCustom(info, customstr);

			if (info.getProperty("launch_url", null) != null
					|| info.getProperty("secure_launch_url", null) != null) {
				return true;
			}
			return false;
		}

		private static void parseCustom(Properties info, String customstr) {
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
					String key = LTIUtil.mapKeyName(param.substring(0, pos));
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
			if (StringUtils.isBlank(trim)) {
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
				String key = LTIUtil.mapKeyName(param.substring(0,pos));
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

		public static String encryptSecret(String orig) {
			String encryptionKey = ServerConfigurationService.getString(LTI_ENCRYPTION_KEY, null);
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
			String encryptionKey = ServerConfigurationService.getString(LTI_ENCRYPTION_KEY, null);
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


	private static void addSiteInfo(Properties props, Properties lti13subst, Site site) {
		if (site != null) {
			String context_type = site.getType();
			if (context_type != null && context_type.toLowerCase().contains("course")) {
				setProperty(props, LTIConstants.CONTEXT_TYPE, LTIConstants.CONTEXT_TYPE_COURSE_SECTION);
				setProperty(lti13subst, LTICustomVars.CONTEXT_TYPE, LTICustomVars.CONTEXT_TYPE_DEFAULT);
			} else {
				setProperty(props, LTIConstants.CONTEXT_TYPE, LTIConstants.CONTEXT_TYPE_GROUP);
				setProperty(lti13subst, LTICustomVars.CONTEXT_TYPE, LTIConstants.CONTEXT_TYPE_GROUP);
			}
			setProperty(props, LTIConstants.CONTEXT_ID, site.getId());
			setProperty(lti13subst, LTICustomVars.COURSESECTION_SOURCEDID, site.getId());
			setProperty(lti13subst, LTICustomVars.CONTEXT_ID, site.getId());

			String context_id_history = site.getProperties().getProperty(LTICustomVars.CONTEXT_ID_HISTORY);
			setProperty(lti13subst, LTICustomVars.CONTEXT_ID_HISTORY, context_id_history);

			TimeZone context_timezone = TimeZone.getDefault ();
			setProperty(lti13subst, LTICustomVars.CONTEXT_TIMEZONE, context_timezone.getID());

			setProperty(props, LTIConstants.CONTEXT_LABEL, site.getTitle());
			setProperty(lti13subst, LTICustomVars.COURSESECTION_LABEL, site.getTitle());
			setProperty(lti13subst, LTICustomVars.CONTEXT_LABEL, site.getTitle());

			setProperty(props, LTIConstants.CONTEXT_TITLE, site.getTitle());
			setProperty(lti13subst, LTICustomVars.COURSESECTION_LONGDESCRIPTION, site.getTitle());
			setProperty(lti13subst, LTICustomVars.CONTEXT_TITLE, site.getTitle());

			String courseRoster = getExternalRealmId(site.getId());
			if (courseRoster != null) {
				setProperty(props, LTIConstants.LIS_COURSE_OFFERING_SOURCEDID, courseRoster);
				setProperty(props, LTIConstants.LIS_COURSE_SECTION_SOURCEDID, courseRoster);
				setProperty(lti13subst, LTICustomVars.COURSESECTION_SOURCEDID, courseRoster);
				setProperty(lti13subst, LTICustomVars.COURSEOFFERING_SOURCEDID, courseRoster);
			} else {
				setProperty(props, LTIConstants.LIS_COURSE_OFFERING_SOURCEDID, site.getId());
				setProperty(props, LTIConstants.LIS_COURSE_SECTION_SOURCEDID, site.getId());
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
		String returnUrl = ServerConfigurationService.getString("lti.consumer_return_url", null);
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

		setProperty(props, LTIConstants.LAUNCH_PRESENTATION_RETURN_URL, returnUrl);
	}

	private static void addUserInfo(Properties ltiProps, Properties lti13subst, User user, LtiToolBean tool) {
		int releasename = (tool != null && Boolean.TRUE.equals(tool.sendname)) ? 1 : 0;
		int releaseemail = (tool != null && Boolean.TRUE.equals(tool.sendemailaddr)) ? 1 : 0;
		if (user != null) {
			setProperty(ltiProps, LTIConstants.USER_ID, user.getId());
			setProperty(lti13subst, LTICustomVars.USER_ID, user.getId());
			setProperty(ltiProps, LTIConstants.LIS_PERSON_SOURCEDID, user.getEid());
			setProperty(lti13subst, LTICustomVars.USER_USERNAME, user.getEid());
			setProperty(lti13subst, LTICustomVars.PERSON_SOURCEDID, user.getEid());

			ResourceProperties userProperties = user.getProperties();
			userProperties.getPropertyNames().forEachRemaining(name ->
				setProperty(lti13subst, LTIConstants.SAKAI_USER_PROPERTY + "." + name, userProperties.getProperty(name))
			);

			UserTimeService userTimeService = ComponentManager.get(UserTimeService.class);
			TimeZone tz = userTimeService.getLocalTimeZone(user.getId());
			if (tz != null) {
				setProperty(lti13subst, LTICustomVars.PERSON_ADDRESS_TIMEZONE, tz.getID());
			}

			if (releasename == 1) {
				setProperty(ltiProps, LTIConstants.LIS_PERSON_NAME_GIVEN, user.getFirstName());
				setProperty(ltiProps, LTIConstants.LIS_PERSON_NAME_FAMILY, user.getLastName());
				setProperty(ltiProps, LTIConstants.LIS_PERSON_NAME_FULL, user.getDisplayName());
				setProperty(lti13subst, LTICustomVars.PERSON_NAME_GIVEN, user.getFirstName());
				setProperty(lti13subst, LTICustomVars.PERSON_NAME_FAMILY, user.getLastName());
				setProperty(lti13subst, LTICustomVars.PERSON_NAME_FULL, user.getDisplayName());
			}
			if (releaseemail == 1) {
				setProperty(ltiProps, LTIConstants.LIS_PERSON_CONTACT_EMAIL_PRIMARY, user.getEmail());
				setProperty(lti13subst, LTICustomVars.PERSON_EMAIL_PRIMARY, user.getEmail());
				// Only send the display ID if it's different to the EID.
				// the anonymous user has a null EID.
				if (user.getEid() != null && !user.getEid().equals(user.getDisplayId())) {
					setProperty(ltiProps, LTIConstants.EXT_SAKAI_PROVIDER_DISPLAYID, user.getDisplayId());
				}
			}
		}
	}

	private static String getCurrentUserSakaiRole(User user, String context) {
		if (user == null) return null;

		String realmId = SiteService.siteReference(context);

		try {
            Role role = null;
            String sakaiRole = null;
            AuthzGroup realm = ComponentManager.get(AuthzGroupService.class).getAuthzGroup(realmId);
            if (realm != null) {
                role = realm.getUserRole(user.getId());

				// Handle a delegated access user
				if (role == null && SiteService.allowUpdateSite(context)) {
					role = realm.getRole(realm.getMaintainRole());
				}
            }
            if (role != null) {
                sakaiRole = role.getId();
            }
            if (StringUtils.isNotBlank(sakaiRole)) return sakaiRole;
        } catch (GroupNotDefinedException e) {
			log.error("site realm not found {}", e.toString());
			log.debug("Stacktrace:", e);
		}
		return null;
	}

	private static String getFallBackRole(String context) {
		if (SecurityService.isSuperUser()) {
			return LTI13ConstantsUtil.ROLE_INSTRUCTOR + ","
				+ LTI13ConstantsUtil.ROLE_CONTEXT_ADMIN + ","
				+ LTI13ConstantsUtil.ROLE_SYSTEM_ADMIN;
		} else if (SiteService.allowUpdateSite(context)) {
			return LTI13ConstantsUtil.ROLE_INSTRUCTOR;
		}
		return LTI13ConstantsUtil.ROLE_LEARNER;
	}

	private static void addRoleInfo(Properties props, Properties lti13subst, User user, String context, LtiToolBean tool) {
		String roleMapProp = (tool != null && tool.rolemap != null) ? tool.rolemap : null;

		String sakaiRole = SecurityService.isSuperUser() ? "admin" : getCurrentUserSakaiRole(user, context);

		String outboundRole = null;
		if (StringUtils.isNotBlank(sakaiRole)) {
			setProperty(props, "ext_sakai_role", sakaiRole);
			setProperty(lti13subst, "Sakai.ext.role", sakaiRole);
			outboundRole = mapOutboundRole(sakaiRole, roleMapProp);
		}

		if ( outboundRole == null ) outboundRole = getFallBackRole(context);

		setProperty(props, LTIConstants.ROLES, outboundRole);
		setProperty(lti13subst, LTICustomVars.MEMBERSHIP_ROLE, outboundRole);

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

	/**
	 * Go through a role string and upgrade legacy roles to modern roles
	 */
	public static String fixLegacyRoles(String roleString)
	{
		if ( StringUtils.isEmpty(roleString) ) return roleString;

		Map<String, String> propLegacyMap = convertLegacyRoleMapPropToMap(ServerConfigurationService.getString(LTI_LEGACY_ROLE_MAP));
		Map<String, String> defaultLegacyMap = convertLegacyRoleMapPropToMap(LTI_LEGACY_ROLE_MAP_DEFAULT);

		return fixLegacyRoles(roleString, propLegacyMap, defaultLegacyMap);
	}

	/**
	 * Go through a role string and upgrade legacy roles to modern roles
	 */
	public static String fixLegacyRoles(String roleString, Map<String, String> propLegacyMap, Map<String, String> defaultLegacyMap)
	{
		if ( StringUtils.isEmpty(roleString) ) return roleString;

		String[] pieces = roleString.split(",");
		StringBuffer sb = new StringBuffer();
		for (String s : pieces) {
			s = s.trim();
			if ( StringUtils.isBlank(s) ) continue;
			if ( sb.length() > 0 ) sb.append(",");
			if ( s.startsWith("http://") || s.startsWith("https://") ) {
				sb.append(s);
				continue;
			}
			if ( propLegacyMap != null && propLegacyMap.containsKey(s) ) sb.append(propLegacyMap.get(s));
			else if ( defaultLegacyMap != null && defaultLegacyMap.containsKey(s) ) sb.append(defaultLegacyMap.get(s));
			else sb.append(s);
		}
		return sb.toString();
	}

	/**
	 * Take an inbound IMS Role String (comma separated with multiple roles) and use to to choose one
	 * of the roles in a site.  This is a pretty complex process.   A role map can be associated with
	 * a tenant, provided by a property and then mapped through a default in that order.  The role
	 * mappings are consulted in priority order.  A role map looks like:
	 *
	 * "http://purl.imsglobal.org/vocab/lis/v2/membership#Learner=Learner,Student,access;" +
	 *
	 * If the inbound role matches this entry, then the we check if the Sakai site has any of
	 * the roles in the comma-separated list, choosing the first we encounter.
	 *
	 */
	public static String mapInboundRole(String incomingRoles, Set<String> siteRoles, String tenantInboundMapStr)
	{
		// Helps upgrade legacy roles like Instructor or urn:lti:sysrole:ims/lis/Administrator
		Map<String, String> propLegacyMap = convertLegacyRoleMapPropToMap(ServerConfigurationService.getString(LTI_LEGACY_ROLE_MAP));
		Map<String, String> defaultLegacyMap = convertLegacyRoleMapPropToMap(LTI_LEGACY_ROLE_MAP_DEFAULT);

		Map<String, List<String>> tenantInboundMap = convertInboundRoleMapPropToMap(tenantInboundMapStr);
		Map<String, List<String>> propInboundMap = convertInboundRoleMapPropToMap(LTI_INBOUND_ROLE_MAP_DEFAULT);
		Map<String, List<String>> defaultInboundMap = convertInboundRoleMapPropToMap(
			ServerConfigurationService.getString(LTI_INBOUND_ROLE_MAP)
		);

		return mapInboundRole(incomingRoles, siteRoles, tenantInboundMap, propInboundMap, defaultInboundMap, propLegacyMap, defaultLegacyMap);
	}

	/**
	 * Take an inbund IMS Role String (comma separated with multiple roles) and use to to choose one
	 * of the roles in a site.  This is a pretty complex process.   A role map can be associated with
	 * a tenant, provided by a property and then mapped through a default in that order.  The role
	 * mappings are consulted in priority order.  A role map looks like:
	 *
	 * "http://purl.imsglobal.org/vocab/lis/v2/membership#Learner=Learner,Student,access;" +
	 *
	 * If the inbound role matches this entry, then the we check if the Sakai site has any of
	 * the roles in the comma-separated list, choosing the first we encounter.
	 *
	 */
	public static String mapInboundRole(String incomingRoles, Set<String> siteRoles,
		Map<String, List<String>> tenantInboundMap, Map<String, List<String>> propInboundMap, Map<String, List<String>> defaultInboundMap,
		Map<String, String> propLegacyMap, Map<String, String> defaultLegacyMap)
	{
		if ( StringUtils.isEmpty(incomingRoles) ) return null;

		// Learner,http://purl.imsglobal.org/vocab/lis/v2/membership#Learner,...
		// Loop through all the LTI roles in the string after we modernise the roles
		String[] ltiRolePiecess = fixLegacyRoles(incomingRoles, propLegacyMap, defaultLegacyMap).split(",");
		for (String s : ltiRolePiecess) {
			s = s.trim();
			if ( StringUtils.isBlank(s) ) continue;

			List<String> sakaiRoleList = null;
			if ( tenantInboundMap != null && tenantInboundMap.containsKey(s) ) sakaiRoleList = tenantInboundMap.get(s);
			else if ( propInboundMap != null && propInboundMap.containsKey(s) ) sakaiRoleList = propInboundMap.get(s);
			else if ( defaultInboundMap != null && defaultInboundMap.containsKey(s) ) sakaiRoleList = defaultInboundMap.get(s);

			if ( sakaiRoleList == null ) continue;

			// Loop through Learner,Student,access
			for (String sakaiRole : sakaiRoleList) {
				if ( siteRoles.contains(sakaiRole) ) return sakaiRole;
			}
		}
		return null;
	}

	/**
	 * Take a Sakai role and choose the appropriate IMS Role String to send out.
	 */
	public static String mapOutboundRole(String sakaiRole, String toolOutboundMapStr)
	{
		Map<String, String> propLegacyMap = convertLegacyRoleMapPropToMap(ServerConfigurationService.getString(LTI_LEGACY_ROLE_MAP));
		Map<String, String> defaultLegacyMap = convertLegacyRoleMapPropToMap(LTI_LEGACY_ROLE_MAP_DEFAULT);

		Map<String, String> toolRoleMap = convertOutboundRoleMapPropToMap(toolOutboundMapStr);

		Map<String, String> propRoleMap = convertOutboundRoleMapPropToMap(ServerConfigurationService.getString(LTI_OUTBOUND_ROLE_MAP));
		Map<String, String> defaultRoleMap = convertOutboundRoleMapPropToMap(LTI_OUTBOUND_ROLE_MAP_DEFAULT);

		return mapOutboundRole(sakaiRole, toolRoleMap, propRoleMap, defaultRoleMap, propLegacyMap, defaultLegacyMap);
	}

	public static String mapOutboundRole(String sakaiRole, Map<String, String> toolRoleMap,
		Map<String, String> propRoleMap, Map<String, String> defaultRoleMap,
		Map<String, String> propLegacyMap, Map<String, String> defaultLegacyMap)
	{
		String imsRole = null;
		if ( toolRoleMap.containsKey(sakaiRole) ) {
			// User-entered Tool Role Map may have legacy roles
			imsRole = fixLegacyRoles(toolRoleMap.get(sakaiRole), propLegacyMap, defaultLegacyMap);
		} else if ( propRoleMap.containsKey(sakaiRole) ) {
			imsRole = propRoleMap.get(sakaiRole);
		} else if ( defaultRoleMap.containsKey(sakaiRole) ) {
			imsRole = defaultRoleMap.get(sakaiRole);
		}
		log.debug("sakaiRole={} imsRole={}", sakaiRole, imsRole);
		return imsRole;
	}

	// Retrieve the Sakai information about users, etc.
	public static boolean sakaiInfo(Properties props, String context, String placementId, ResourceLoader rb) {

		Site site;
		try {
			site = SiteService.getSite(context);
		} catch (IdUnusedException e) {
			log.error("No site/page associated with Launch context={}", context);
			log.debug("Stacktrace:", e);
			return false;
		}

		User user = UserDirectoryService.getCurrentUser();

		// Add the generic information
		addGlobalData(site, props, null, rb);
		ToolConfiguration placement = SiteService.findTool(placementId);
		Properties config = placement.getConfig();
		String roleMapProp = StringUtils.trimToNull(getCorrectProperty(config, LTIService.LTI_ROLEMAP, placement));
		addRoleInfo(props, null, user, context, LtiToolBean.of(roleMapProp != null ? Collections.singletonMap(LTIService.LTI_ROLEMAP, roleMapProp) : null));
		addSiteInfo(props, null, site);

		// Add Placement Information
		addPlacementInfo(props, placementId);
		return true;
	}

	private static void addPlacementInfo(Properties props, String placementId) {

		// Get the placement to see if we are to release information
		ToolConfiguration placement = SiteService.findTool(placementId);
		Properties config = placement.getConfig();

		// Get the "modern" version of config
		LTIService ltiService = (LTIService) ComponentManager.get("org.sakaiproject.lti.api.LTIService");
		Properties normalProps = normalizePlacementProperties(placementId, ltiService);

		// Start setting the Basici LTI parameters
		setProperty(props, LTIConstants.RESOURCE_LINK_ID, placementId);
		String title = StringUtils.trimToNull(getCorrectProperty(config, LTIService.LTI_TITLE, placement));
		if ( title == null ) {
			title = StringUtils.trimToNull(getCorrectProperty(config, LTI_PORTLET_TOOLTITLE, placement));
		}

		if (title != null) {
			setProperty(props, LTIConstants.RESOURCE_LINK_TITLE, title);
		} else {
			setProperty(props, LTIConstants.RESOURCE_LINK_TITLE, placement.getTitle());
		}

		String description = StringUtils.trimToNull(getCorrectProperty(config, LTIService.LTI_DESCRIPTION, placement));
		if ( description == null ) {
			description = title;
		}

		if (description != null) {
			setProperty(props, LTIConstants.RESOURCE_LINK_DESCRIPTION, description);
		} else {
			setProperty(props, LTIConstants.RESOURCE_LINK_DESCRIPTION, placement.getTitle());
		}

		String releasename = StringUtils.trimToNull(getCorrectProperty(config, LTI_PORTLET_RELEASENAME, placement));
		String releaseemail = StringUtils.trimToNull(getCorrectProperty(config, LTI_PORTLET_RELEASEEMAIL, placement));

		User user = UserDirectoryService.getCurrentUser();

		PrivacyManager pm = (PrivacyManager) ComponentManager.get("org.sakaiproject.api.privacy.PrivacyManager");

		// TODO: Think about anonymous
		if (user != null) {
			String context = placement.getContext();
			boolean isViewable = pm.isViewable("/site/" + context, user.getId());
			setProperty(props, "ext_sakai_privacy", isViewable ? "visible" : "hidden");

			setProperty(props, LTIConstants.USER_ID, user.getId());

			if (ServerConfigurationService.getBoolean(LTI_CONSUMER_USERIMAGE_ENABLED, true)) {
				String imageUrl = getOurServerUrl() + "/api/users/" + user.getId() + "/profile/image";
				setProperty(props, LTIConstants.USER_IMAGE, imageUrl);
			}

			if (LTI_PORTLET_ON.equals(releasename)) {
				setProperty(props, LTIConstants.LIS_PERSON_NAME_GIVEN, user.getFirstName());
				setProperty(props, LTIConstants.LIS_PERSON_NAME_FAMILY, user.getLastName());
				setProperty(props, LTIConstants.LIS_PERSON_NAME_FULL, user.getDisplayName());
			}
			if (LTI_PORTLET_ON.equals(releaseemail)) {
				setProperty(props, LTIConstants.LIS_PERSON_CONTACT_EMAIL_PRIMARY, user.getEmail());
				setProperty(props, LTIConstants.LIS_PERSON_SOURCEDID, user.getEid());
				setProperty(props, "ext_sakai_eid", user.getEid());
			}

			String allowRoster = (String) normalProps.get(LTIService.LTI_ALLOWROSTER);
			String result_sourcedid = getSourceDID(user, placement, config);
			String theRole = props.getProperty(LTIConstants.ROLES);

			// If the server configuration says "no" that is it
			// If the server configuration says "yes" and the tool configuration says "no" then it is "no"
			String allowOutcomes = "false";
			String gradebookColumn = null;

			String allowOutcomesTool = StringUtils.trimToNull(getCorrectProperty(config, LTIService.LTI_ALLOWOUTCOMES, placement));
			if ( outcomesEnabled() && !LTI_PORTLET_OFF.equals(allowOutcomesTool) ) {
				allowOutcomes = "true";
				gradebookColumn = StringUtils.trimToNull(getCorrectProperty(config, "assignment", placement));
			}

			if (result_sourcedid != null) {
				if ("true".equals(allowOutcomes) && gradebookColumn != null) {
					if (theRole.contains(LTICustomVars.MEMBERSHIP_ROLE_LEARNER)) {
						setProperty(props, LTIConstants.LIS_RESULT_SOURCEDID, result_sourcedid);
					}
					setProperty(props, "ext_outcome_data_values_accepted", "text");  // SAK-25696

					// Standard Outcomes service URL
					String outcome_url = ServerConfigurationService.getString("lti.consumer." + LTIConstants.LIS_OUTCOME_SERVICE_URL, null);
					if (outcome_url == null) {
						outcome_url = getOurServerUrl() + LTI11_SERVICE_PATH;
					}
					setProperty(props, LTIConstants.LIS_OUTCOME_SERVICE_URL, outcome_url);
				}

				if (rosterEnabled() && LTI_PORTLET_ON.equals(allowRoster) ) {
					setProperty(props, "ext_ims_lis_memberships_id", result_sourcedid);

					String roster_url = ServerConfigurationService.getString("lti.consumer.ext_ims_lis_memberships_url", null);
					if (roster_url == null) {
						roster_url = getOurServerUrl() + LTI11_SERVICE_PATH;
					}
					setProperty(props, "ext_ims_lis_memberships_url", roster_url);
				}

			}
		}

		// Send along the content link
		String contentlink = StringUtils.trimToNull(getCorrectProperty(config, "contentlink", placement));
		if (contentlink != null) {
			setProperty(props, "ext_resource_link_content", contentlink);
		}
	}

	private static void addConsumerData(Properties props, Properties custom) {
		final String defaultName =  ServerConfigurationService.getString("serverName",
			ServerConfigurationService.getString("serverUrl","localhost.sakailms"));

		// Get the organizational information
		setProperty(custom, LTICustomVars.TOOLPLATFORMINSTANCE_GUID,
				ServerConfigurationService.getString("lti.consumer_instance_guid", defaultName));
		setProperty(props, LTIConstants.TOOL_CONSUMER_INSTANCE_GUID,
				ServerConfigurationService.getString("lti.consumer_instance_guid", defaultName));

		setProperty(custom,  LTICustomVars.TOOLPLATFORMINSTANCE_NAME,
				ServerConfigurationService.getString("lti.consumer_instance_name", defaultName));
		setProperty(props, LTIConstants.TOOL_CONSUMER_INSTANCE_NAME,
				ServerConfigurationService.getString("lti.consumer_instance_name", defaultName));

		setProperty(custom, LTICustomVars.TOOLPLATFORMINSTANCE_DESCRIPTION,
				ServerConfigurationService.getString("lti.consumer_instance_description", defaultName));
		setProperty(props, LTIConstants.TOOL_CONSUMER_INSTANCE_DESCRIPTION,
				ServerConfigurationService.getString("lti.consumer_instance_description", defaultName));

		setProperty(custom, LTICustomVars.TOOLPLATFORMINSTANCE_URL,
				ServerConfigurationService.getString("lti.consumer_instance_url",
						ServerConfigurationService.getString("serverUrl", null)));
		setProperty(props, LTIConstants.TOOL_CONSUMER_INSTANCE_URL,
				ServerConfigurationService.getString("lti.consumer_instance_url",
						ServerConfigurationService.getString("serverUrl", null)));

		setProperty(custom, LTICustomVars.TOOLPLATFORMINSTANCE_CONTACTEMAIL,
				ServerConfigurationService.getString("lti.consumer_instance_contact_email", null));
		setProperty(props, LTIConstants.TOOL_CONSUMER_INSTANCE_CONTACT_EMAIL,
				ServerConfigurationService.getString("lti.consumer_instance_contact_email", null));

	}

	// Custom variable substitutions extensions follow the form of
	// $com.example.Foo.bar
	// http://www.imsglobal.org/spec/lti/v1p3/#custom-variables
	private static void addPropertyExtensionData(Properties props, Properties custom) {
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

	private static void addGlobalData(Site site, Properties props, Properties custom, ResourceLoader rb) {
		if (rb != null) {
			String locale = rb.getLocale().toString();
			setProperty(props, LTIConstants.LAUNCH_PRESENTATION_LOCALE, locale);
			setProperty(custom, LTICustomVars.MESSAGE_LOCALE, locale);
		}

		addPropertyExtensionData(props, custom);
		addConsumerData(props, custom);

		// Send along the CSS URL
		String tool_css = ServerConfigurationService.getString("lti.consumer.launch_presentation_css_url", null);
		if (tool_css == null) {
			tool_css = getOurServerUrl() + CSSUtils.getCssToolBase();
		}
		setProperty(props, LTIConstants.LAUNCH_PRESENTATION_CSS_URL, tool_css);

		// Send along the CSS URL list
		String tool_css_all = ServerConfigurationService.getString("lti.consumer.ext_sakai_launch_presentation_css_url_all", null);
		if (site != null && tool_css_all == null) {
			tool_css_all = getOurServerUrl() + CSSUtils.getCssToolBase() + ',' + getOurServerUrl() + CSSUtils.getCssToolSkinCDN(CSSUtils.getSkinFromSite(site));
		}
		setProperty(props, "ext_sakai_" + LTIConstants.LAUNCH_PRESENTATION_CSS_URL + "_list", tool_css_all);

		// Let tools know we are coming from Sakai
		String sakaiVersion = ServerConfigurationService.getString("version.sakai", "2");
		setProperty(props, "ext_lms", "sakai-" + sakaiVersion);
		setProperty(props, LTIConstants.TOOL_CONSUMER_INFO_PRODUCT_FAMILY_CODE, "sakai");
		setProperty(props, LTIConstants.TOOL_CONSUMER_INFO_VERSION, sakaiVersion);

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

		// This must return an HTML message as the [0] in the array
		// If things are successful - the launch URL is in [1]
		public static String[] postLaunchHTML(LtiContentBean content, LtiToolBean tool,
				String state, String nonce, LTIService ltiService, ResourceLoader rb) {

			log.debug("state={} nonce={}", state, nonce);
			if (content == null) {
				return postError("<p>" + getRB(rb, "error.content.missing", "Content item is missing or improperly configured.") + "</p>");
			}
			if (tool == null) {
				return postError("<p>" + getRB(rb, "error.tool.missing", "Tool item is missing or improperly configured.") + "</p>");
			}

			if ("disable".equals(tool.status)) {
				return postError("<p>" + getRB(rb, "tool.disabled", "Tool is currently disabled") + "</p>");
			}

			// Go with the content url first
			String launch_url = content.launch;
			if (launch_url == null) {
				launch_url = tool.launch;
			}
			if (launch_url == null) {
				return postError("<p>" + getRB(rb, "error.nolaunch", "This tool is not yet configured.") + "</p>");
			}

			String context = content.siteId;
			Site site;
			try {
				site = SiteService.getSite(context);
			} catch (IdUnusedException e) {
				log.error("No site/page associated with Launch context={}", context);
				log.debug("Stacktrace:", e);
				return postError("<p>" + getRB(rb, "error.site.missing", "Cannot load site.") + context + "</p>");
			}

			// SAK-47573 - Make sure the gradebook is initialised
			GradingService gradingService = (GradingService) ComponentManager.get("org.sakaiproject.grading.api.GradingService");
			gradingService.initializeGradebooksForSite(context);

			// See if there are the necessary items
			String secret = getSecret(tool);
			String key = getKey(tool);

			if (LTIService.LTI_SECRET_INCOMPLETE.equals(key) && LTIService.LTI_SECRET_INCOMPLETE.equals(secret)) {
				return postError("<p>" + getRB(rb, "error.tool.partial", "Tool item is incomplete, missing a key and secret.") + "</p>");
			}

			boolean isLTI13 = isLTI13(tool);

			log.debug("isLTI13={}", isLTI13);

			User user = UserDirectoryService.getCurrentUser();

			// Start building up the properties
			Properties ltiProps = new Properties();
			Properties toolProps = new Properties();
			Properties lti13subst = new Properties();
			setProperty(ltiProps, LTIConstants.LTI_VERSION, LTIConstants.LTI_VERSION_1);
			addGlobalData(site, ltiProps, lti13subst, rb);
			addSiteInfo(ltiProps, lti13subst, site);
			addRoleInfo(ltiProps, lti13subst, user, context, tool);
			addUserInfo(ltiProps, lti13subst, user, tool);

			String resource_link_id = getResourceLinkId(content);
			setProperty(ltiProps, LTIConstants.RESOURCE_LINK_ID, resource_link_id);

			setProperty(toolProps, "launch_url", launch_url);
			setProperty(toolProps, "state", state);  // So far LTI 1.3 only
			setProperty(toolProps, "nonce", nonce);  // So far LTI 1.3 only

			setProperty(toolProps, LTIService.LTI_SECRET, secret);
			setProperty(toolProps, LTI_PORTLET_KEY, key);

			int debug = (tool.debug != null) ? tool.debug : 0;
			if (debug == 2) {
				debug = (content.debug != null && content.debug) ? 1 : 0;
			}
			setProperty(toolProps, LTIService.LTI_DEBUG, debug + "");

			int frameheight = (content.frameheight != null && content.frameheight > 0) ? content.frameheight : 0;
			if (frameheight < 1) {
				frameheight = (tool.frameheight != null && tool.frameheight > 0) ? tool.frameheight : 0;
			}

			setProperty(toolProps, LTIService.LTI_FRAMEHEIGHT, frameheight + "");
			setProperty(lti13subst, LTICustomVars.MESSAGE_HEIGHT, frameheight + "");

			int newpage = getNewpage(tool, content, true) ? 1 : 0;
			String title = getToolTitle(tool, content, "");
			String description = content.description;

			// SAK-40044 - If there is no description, we fall back to the pre-21 description in JSON
			String content_settings = content.settings;
			JSONObject content_json = org.tsugi.lti.LTIUtil.parseJSONObject(content_settings);
			if (StringUtils.isBlank(description) ) {
				description = (String) content_json.get(LTIService.LTI_DESCRIPTION);
			}

			// All else fails, use title as description
			if (StringUtils.isBlank(description)) {
				description = title;
			}

			if (StringUtils.isNotBlank(title)) {
				setProperty(ltiProps, LTIConstants.RESOURCE_LINK_TITLE, title);
				setProperty(lti13subst, LTICustomVars.RESOURCELINK_TITLE, title);
			}

			if ( StringUtils.isNotBlank(description) ) {
				setProperty(ltiProps, LTIConstants.RESOURCE_LINK_DESCRIPTION, description);
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
				DeepLinkResponse.RESOURCELINK_SUBMISSION_ENDDATETIME,
				LTICustomVars.COURSEGROUP_ID
			};

			for (String subKey : jsonSubst) {
				String value = StringUtils.trimToNull((String) content_json.get(subKey));
				if ( value == null ) continue;
				setProperty(lti13subst, subKey, value);
			}

			int allowoutcomes = (tool.allowoutcomes != null && tool.allowoutcomes) ? 1 : 0;
			int allowroster = (tool.allowroster != null && tool.allowroster) ? 1 : 0;
			String placement_secret = content.placementsecret;

			String result_sourcedid = getSourceDID(user, resource_link_id, placement_secret);
			log.debug("allowoutcomes={} allowroster={} result_sourcedid={}",
					allowoutcomes, allowroster, result_sourcedid);

			if (result_sourcedid != null) {
				String theRole = ltiProps.getProperty(LTIConstants.ROLES);
				log.debug("theRole={}", theRole);
				if (allowoutcomes == 1) {
					setProperty(ltiProps, "ext_outcome_data_values_accepted", "text");  // SAK-25696

					// Standard Outcomes service URL
					String outcome_url = ServerConfigurationService.getString("lti.consumer." + LTIConstants.LIS_OUTCOME_SERVICE_URL, null);
					if (outcome_url == null) {
						outcome_url = getOurServerUrl() + LTI11_SERVICE_PATH;
					}
					setProperty(ltiProps, LTIConstants.LIS_OUTCOME_SERVICE_URL, outcome_url);

					if (theRole.contains(LTIConstants.MEMBERSHIP_ROLE_LEARNER)) {
						setProperty(ltiProps, LTIConstants.LIS_RESULT_SOURCEDID, result_sourcedid);
					}
				}

				if (allowroster == 1) {
					setProperty(ltiProps, "ext_ims_lis_memberships_id", result_sourcedid);

					String roster_url = ServerConfigurationService.getString("lti.consumer.ext_ims_lis_memberships_url", null);
					if (roster_url == null) {
						roster_url = getOurServerUrl() + LTI11_SERVICE_PATH;
					}
					setProperty(ltiProps, "ext_ims_lis_memberships_url", roster_url);
				}

			}

			// Construct the ultimate custom values for Launch
			Properties custom = new Properties();

			String contentCustom = content.custom;
			contentCustom = adjustCustom(contentCustom);
			mergeLTI1Custom(custom, contentCustom);

			String toolCustom = tool.custom;
			toolCustom = adjustCustom(toolCustom);
			mergeLTI1Custom(custom, toolCustom);

			ltiService.filterCustomSubstitutions(lti13subst, tool, site);

			log.debug("lti13subst={}", lti13subst);
			log.debug("before custom={}", custom);
			LTI13Util.substituteCustom(custom, lti13subst);
			log.debug("after custom={}", custom);

			log.debug("ltiProps={}", ltiProps);
			log.debug("custom={}", custom);

			// Place the custom values into the launch
			LTI13Util.addCustomToLaunch(ltiProps, custom);

			if (isLTI13) {
				return postLaunchJWT(toolProps, ltiProps, site, tool, content, rb);
			}
			return postLaunchHTML(toolProps, ltiProps, rb);
		}

		public static String[] postLaunchHTML(Map<String, Object> content, Map<String, Object> tool,
				String state, String nonce, LTIService ltiService, ResourceLoader rb) {
			return postLaunchHTML(LtiContentBean.of(content), LtiToolBean.of(tool), state, nonce, ltiService, rb);
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
		public static ContentItem getContentItemFromRequest(LtiToolBean tool) {
			if (tool == null) {
				throw new RuntimeException("Tool is null");
			}

			Placement placement = ToolManager.getCurrentPlacement();
			String siteId = placement.getContext();

			String toolSiteId = tool.siteId;
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
			String oauth_secret = getSecret(tool);
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

		public static ContentItem getContentItemFromRequest(Map<String, Object> tool) {
			return getContentItemFromRequest(LtiToolBean.of(tool));
		}

		/**
		 * getPublicKey - Get the appropriate public key for use for an incoming request (bean overload)
		 */
		public static Key getPublicKey(LtiToolBean tool, String id_token) {
			if (tool == null) {
				throw new RuntimeException("Tool is null");
			}
			JSONObject jsonHeader = LTI13JwtUtil.jsonJwtHeader(id_token);
			if (jsonHeader == null) {
				throw new RuntimeException("Could not parse Jwt Header in client_assertion");
			}
			String incoming_kid = (String) jsonHeader.get("kid");

			String tool_keyset = tool.lti13ToolKeyset;
			if (tool_keyset == null) {
				throw new RuntimeException("Could not find tool keyset url");
			}

			log.debug("Retrieving kid={} from {}", incoming_kid, tool_keyset);
			try {
				return LTI13KeySetUtil.getKeyFromKeySet(incoming_kid, tool_keyset);
			} catch (Exception e) {
				log.error(e.toString(), e);
				log.debug("Stacktrace:", e);
				throw new RuntimeException("Unable to retrieve kid=" + incoming_kid + " from " + tool_keyset + " detail=" + e.toString());
			}
		}

		private static Key getPublicKey(Map<String, Object> tool, String id_token) {
			return getPublicKey(LtiToolBean.of(tool), id_token);
		}

		/**
		 * Create a DeepLinkResponse from the current request (may throw runtime). Bean overload.
		 */
		public static DeepLinkResponse getDeepLinkFromToken(LtiToolBean tool, String id_token) {
			if (tool == null) {
				throw new RuntimeException("Tool is null");
			}

			Placement placement = ToolManager.getCurrentPlacement();
			String siteId = placement.getContext();

			String toolSiteId = tool.siteId;
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

			Key publicKey = SakaiLTIUtil.getPublicKey(tool, id_token);

			DeepLinkResponse dlr = new DeepLinkResponse(id_token);
			if ( ! dlr.validate(publicKey) ) {
				throw new RuntimeException("Could not verify signature");
			}

			return dlr;
		}

		public static DeepLinkResponse getDeepLinkFromToken(Map<String, Object> tool, String id_token) {
			return getDeepLinkFromToken(LtiToolBean.of(tool), id_token);
		}

		/**
		 * An LTI ContentItemSelectionRequest launch. Bean overload.
		 */
		public static String[] postContentItemSelectionRequest(Long toolKey, LtiToolBean tool,
				String state, String nonce, ResourceLoader rb, String contentReturn, Properties dataProps) {
			if (tool == null) {
				return postError("<p>" + getRB(rb, "error.tool.missing", "Tool is missing or improperly configured.") + "</p>");
			}

			String launch_url = tool.launch;
			if (launch_url == null) {
				return postError("<p>" + getRB(rb, "error.tool.noreg", "This tool is has no launch url.") + "</p>");
			}

			String consumerkey = tool.consumerkey;
			String secret = getSecret(tool);
			secret = decryptSecret(secret);

			boolean isLTI13 = isLTI13(tool);

			log.debug("isLTI13={}", isLTI13);

			if (!isLTI13 && (secret == null || consumerkey == null)) {
				return postError("<p>" + getRB(rb, "error.tool.partial", "Tool is incomplete, missing a key and secret.") + "</p>");
			}

			// Start building up the properties
			Properties ltiProps = new Properties();

			setProperty(ltiProps, LTIConstants.LTI_VERSION, LTIConstants.LTI_VERSION_1);
			setProperty(ltiProps, LTIConstants.LTI_MESSAGE_TYPE, LTIConstants.CONTENT_ITEM_SELECTION_REQUEST);

			setProperty(ltiProps, ContentItem.ACCEPT_MEDIA_TYPES, ContentItem.MEDIA_LTILINKITEM);
			setProperty(ltiProps, LTIConstants.ACCEPT_PRESENTATION_DOCUMENT_TARGETS, "iframe,window"); // Nice to add overlay
			setProperty(ltiProps, LTIConstants.ACCEPT_UNSIGNED, "false");
			setProperty(ltiProps, LTIConstants.ACCEPT_MULTIPLE, "false");
			setProperty(ltiProps, LTIConstants.ACCEPT_COPY_ADVICE, "false"); // ???
			setProperty(ltiProps, LTIConstants.AUTO_CREATE, "true");
			setProperty(ltiProps, LTIConstants.CAN_CONFIRM, "false");
			// setProperty(ltiProps, LTIConstants.TITLE, "");
			// setProperty(ltiProps, LTIConstants.TEXT, "");

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
				if (LTIConstants.ACCEPT_MEDIA_TYPES.equals(key)) {
					setProperty(ltiProps, LTIConstants.ACCEPT_MEDIA_TYPES, value);
					continue;
				} else if (LTIConstants.ACCEPT_MULTIPLE.equals(key)) {
					setProperty(ltiProps, LTIConstants.ACCEPT_MULTIPLE, value);
					continue;
				} else if (LTIConstants.ACCEPT_PRESENTATION_DOCUMENT_TARGETS.equals(key)) {
					setProperty(ltiProps, LTIConstants.ACCEPT_PRESENTATION_DOCUMENT_TARGETS, value);
					continue;
				} else if (LTIConstants.ACCEPT_UNSIGNED.equals(key)) {
					setProperty(ltiProps, LTIConstants.ACCEPT_UNSIGNED, value);
					continue;
				} else if (LTIConstants.AUTO_CREATE.equals(key)) {
					setProperty(ltiProps, LTIConstants.AUTO_CREATE, value);
					continue;
				} else if (LTIConstants.CAN_CONFIRM.equals(key)) {
					setProperty(ltiProps, LTIConstants.CAN_CONFIRM, value);
					continue;
				} else if (LTIConstants.TITLE.equals(key)) {
					setProperty(ltiProps, LTIConstants.TITLE, value);
					continue;
				} else if (LTIConstants.TEXT.equals(key)) {
					setProperty(ltiProps, LTIConstants.TEXT, value);
					continue;
				}

				// Pass in data for use to get back.
				dataJSON.put(key, value);
			}
			setProperty(ltiProps, LTIConstants.DATA, dataJSON.toString());

			setProperty(ltiProps, LTIConstants.CONTENT_ITEM_RETURN_URL, contentReturn);

			// This must always be there
			String context = tool.siteId;
			Site site;
			try {
				site = SiteService.getSite(context);
			} catch (IdUnusedException e) {
				log.error("No site/page associated with Launch context={}", context);
				log.debug("Stacktrace:", e);
				return postError("<p>" + getRB(rb, "error.site.missing", "Cannot load site.") + context + "</p>");
			}

			// SAK-47573 - Make sure the gradebook is initialised
			GradingService gradingService = (GradingService) ComponentManager.get("org.sakaiproject.grading.api.GradingService");
			gradingService.initializeGradebooksForSite(context);

			User user = UserDirectoryService.getCurrentUser();

			Properties lti13subst = new Properties();
			addGlobalData(site, ltiProps, lti13subst, rb);
			addSiteInfo(ltiProps, lti13subst, site);
			addRoleInfo(ltiProps, lti13subst, user, context, tool);

			addUserInfo(ltiProps, lti13subst, user, tool);

			// Don't sent the normal return URL when we are doing ContentItem launch
			// Certification Issue
			if (LTIConstants.CONTENT_ITEM_SELECTION_REQUEST.equals(ltiProps.getProperty(LTIConstants.LTI_MESSAGE_TYPE))) {

				ltiProps.remove(LTIConstants.LAUNCH_PRESENTATION_RETURN_URL);
			}

			boolean dodebug = (tool.debug != null && tool.debug == 1);

			// Merge all the sources of custom vaues and run the substitution
			Properties custom = new Properties();

			String toolCustom = tool.custom;
			toolCustom = adjustCustom(toolCustom);
			mergeLTI1Custom(custom, toolCustom);

			LTIService ltiService = (LTIService) ComponentManager.get("org.sakaiproject.lti.api.LTIService");
			ltiService.filterCustomSubstitutions(lti13subst, tool, site);

			log.debug("lti13subst={}", lti13subst);
			log.debug("before custom={}", custom);
			LTI13Util.substituteCustom(custom, lti13subst);
			log.debug("after custom={}", custom);

			log.debug("ltiProps={}", ltiProps);
			log.debug("custom={}", custom);

			// Place the custom values into the launch
			LTI13Util.addCustomToLaunch(ltiProps, custom);

			if ( isLTI13 ) {
				Properties toolProps = new Properties();
				toolProps.put("launch_url", launch_url);
				setProperty(toolProps, "state", state);  // So far LTI 1.3 only
				setProperty(toolProps, "nonce", nonce);  // So far LTI 1.3 only
				toolProps.put(LTIService.LTI_DEBUG, dodebug ? "1" : "0");

				return postLaunchJWT(toolProps, ltiProps, site, tool, null, rb);
			}

			// LTI 1.1.2
			if ( StringUtils.isNotEmpty(tool.toolState) ) setProperty(ltiProps, "tool_state", tool.toolState);
			if ( StringUtils.isNotEmpty(tool.platformState) ) setProperty(ltiProps, "platform_state", tool.platformState);
			if ( StringUtils.isNotEmpty(tool.relaunchUrl) ) setProperty(ltiProps, "relaunch_url", tool.relaunchUrl);

			String submit_form_id = java.util.UUID.randomUUID().toString() + "";
			boolean autosubmit = !dodebug;

			Map<String, String> extra = new HashMap<>();
			extra.put(LTIUtil.EXTRA_FORM_ID, submit_form_id);
			extra.put(LTIUtil.EXTRA_ERROR_TIMEOUT, rb.getString("error.submit.timeout"));
			extra.put(LTIUtil.EXTRA_HTTP_POPUP, LTIUtil.EXTRA_HTTP_POPUP_FALSE);  // Don't bother opening in new window in protocol mismatch
			extra.put(LTIUtil.EXTRA_JAVASCRIPT, getLaunchJavaScript(submit_form_id, autosubmit));
			ltiProps = LTIUtil.signProperties(ltiProps, launch_url, "POST", consumerkey, secret, extra);

			log.debug("signed ltiProps={}", ltiProps);

			String launchtext = getRB(rb, "launch.button", "Press to Launch External Tool");
			autosubmit = false;  // We handle this in our JavaScript
			String postData = LTIUtil.postLaunchHTML(ltiProps, launch_url, launchtext, autosubmit, dodebug, extra);

			String[] retval = {postData, launch_url};
			return retval;
		}

		public static String[] postContentItemSelectionRequest(Long toolKey, Map<String, Object> tool,
				String state, String nonce, ResourceLoader rb, String contentReturn, Properties dataProps) {
			return postContentItemSelectionRequest(toolKey, LtiToolBean.of(tool), state, nonce, rb, contentReturn, dataProps);
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
			String key = getToolConsumerInfo(launch_url, LTI_PORTLET_KEY);
			if (key == null) {
				key = ServerConfigurationService.getString("lti.consumer_instance_guid",
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
				secret = StringUtils.trimToNull(toolProps.getProperty(LTIService.LTI_SECRET));
				key = StringUtils.trimToNull(toolProps.getProperty(LTI_PORTLET_KEY));
			}

			// If secret is encrypted, decrypt it
			secret = decryptSecret(secret);

			// Pull in all of the custom parameters
			for (Object okey : toolProps.keySet()) {
				String skey = (String) okey;
				if (!skey.startsWith(LTIConstants.CUSTOM_PREFIX)) {
					continue;
				}
				String value = toolProps.getProperty(skey);
				if (value == null) {
					continue;
				}
				setProperty(ltiProps, skey, value);
			}

			String oauth_callback = ServerConfigurationService.getString("lti.oauth_callback", null);
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

			String debugProperty = toolProps.getProperty(LTIService.LTI_DEBUG);
			boolean dodebug = LTI_PORTLET_ON.equals(debugProperty) || "1".equals(debugProperty);

			String submit_form_id = java.util.UUID.randomUUID().toString() + "";
			boolean autosubmit = !dodebug;

			Map<String, String> extra = new HashMap<>();
			extra.put(LTIUtil.EXTRA_FORM_ID, submit_form_id);
			extra.put(LTIUtil.EXTRA_ERROR_TIMEOUT, rb.getString("error.submit.timeout"));
			extra.put(LTIUtil.EXTRA_HTTP_POPUP, LTIUtil.EXTRA_HTTP_POPUP_FALSE);  // Don't bother opening in new window in protocol mismatch
			extra.put(LTIUtil.EXTRA_JAVASCRIPT, getLaunchJavaScript(submit_form_id, autosubmit));
			ltiProps = LTIUtil.signProperties(ltiProps, launch_url, "POST", key, secret, extra);

			if (ltiProps == null) {
				return postError("<p>" + getRB(rb, "error.sign", "Error signing message.") + "</p>");
			}
			log.debug("LAUNCH III={}", ltiProps);

			String launchtext = getRB(rb, "launch.button", "Press to Launch External Tool");
			autosubmit = false;  // We handle this in our JavaScript
			String postData = LTIUtil.postLaunchHTML(ltiProps, launch_url, launchtext, autosubmit, dodebug, extra);

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
			return postLaunchJWT(toolProps, ltiProps, site, LtiToolBean.of(tool), LtiContentBean.of(content), rb);
		}

		public static String[] postLaunchJWT(Properties toolProps, Properties ltiProps,
				Site site, LtiToolBean tool, LtiContentBean content, ResourceLoader rb) {
			log.debug("postLaunchJWT LTI 1.3");
			if (tool == null) {
				return postError("<p>" + getRB(rb, "error.missing", "Not configured") + "</p>");
			}
			String launch_url = toolProps.getProperty("secure_launch_url");
			if (launch_url == null) {
				launch_url = toolProps.getProperty("launch_url");
			}
			if (launch_url == null) {
				return postError("<p>" + getRB(rb, "error.missing", "Not configured") + "</p>");
			}

			HttpServletRequest req = ToolUtils.getRequestFromThreadLocal();

			String orig_site_id_null = tool.origSiteIdNull;
			String site_id = null;
			if ( ! "true".equals(orig_site_id_null) ) {
				site_id = tool.siteId;
			}

			String client_id = tool.lti13ClientId;
			String placement_secret = null;
			if (content != null) {
				placement_secret = content.placementsecret;
			}

		/*

	context_id: mercury
	context_label: mercury site
	context_title: mercury site
	context_type: Group
	custom_x=42
	custom_y=043040450
	ext_ims_lis_memberships_id: c1007fb6345a87cd651785422a2925114d0707fad32c66edb6bfefbf2165819a:::admin:::content:3
	ext_ims_lis_memberships_url: http://localhost:8080/imsblis/service/
	ext_ims_lti_tool_setting_id: c1007fb6345a87cd651785422a2925114d0707fad32c66edb6bfefbf2165819a:::admin:::content:3
	ext_ims_lti_tool_setting_url: http://localhost:8080/imsblis/service/
	ext_lms: sakai-26-SNAPSHOT
	ext_sakai_academic_session: OTHER
	ext_sakai_launch_presentation_css_url_list: http://localhost:8080/library/skin/tool_base.css,http://localhost:8080/library/skin/default-skin/tool.css?version=49b21ca5
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

			String context_id = ltiProps.getProperty(LTIConstants.CONTEXT_ID);
			String user_id = (String) ltiProps.getProperty(LTIConstants.USER_ID);

			// Lets make a JWT from the LTI 1.x data
			boolean deepLink = false;
			SakaiLaunchJWT lj = new SakaiLaunchJWT();
			lj.target_link_uri = launch_url;  // The actual launch URL

			// See if we have a lineItem associated with this launch in case we need it later

			SakaiLineItem sakaiLineItem = null;
			if ( content != null ) {
				String lineItemStr = content.contentitem;
				sakaiLineItem = LineItemUtil.parseLineItem(lineItemStr);
			}

			String messageTypeParm = req.getParameter(MESSAGE_TYPE_PARAMETER);
			if ( MESSAGE_TYPE_PARAMETER_PRIVACY.equals(messageTypeParm)) {
				lj.message_type = LaunchJWT.MESSAGE_TYPE_LTI_DATA_PRIVACY_LAUNCH_REQUEST;
			} else if ( MESSAGE_TYPE_PARAMETER_CONTENT_REVIEW.equals(messageTypeParm)) {
				lj.message_type = LaunchJWT.MESSAGE_TYPE_LTI_SUBMISSION_REVIEW_REQUEST;
				if ( sakaiLineItem != null && sakaiLineItem.submissionReview != null && StringUtils.isNotEmpty(sakaiLineItem.submissionReview.url) ) lj.target_link_uri = sakaiLineItem.submissionReview.url;
			} else if ( LTIConstants.LTI_MESSAGE_TYPE_CONTENTITEMSELECTIONREQUEST.equals(ltiProps.getProperty(LTIConstants.LTI_MESSAGE_TYPE)) ) {
				lj.message_type = LaunchJWT.MESSAGE_TYPE_DEEP_LINK;
				deepLink = true;
			}

			lj.launch_presentation.css_url = ltiProps.getProperty(LTIConstants.LAUNCH_PRESENTATION_CSS_URL);
			lj.locale = ltiProps.getProperty(LTIConstants.LAUNCH_PRESENTATION_LOCALE);
			lj.launch_presentation.return_url = ltiProps.getProperty(LTIConstants.LAUNCH_PRESENTATION_RETURN_URL);
			lj.audience = client_id;
			lj.issuer = getIssuer(site_id);
			lj.subject = getSubject(user_id, context_id);

			// The name and email info have been checked for release value in addUserInfo
			lj.name = ltiProps.getProperty(LTIConstants.LIS_PERSON_NAME_FULL);
			lj.given_name = ltiProps.getProperty(LTIConstants.LIS_PERSON_NAME_GIVEN);
			lj.family_name = ltiProps.getProperty(LTIConstants.LIS_PERSON_NAME_FAMILY);
			lj.email = ltiProps.getProperty(LTIConstants.LIS_PERSON_CONTACT_EMAIL_PRIMARY);

			lj.nonce = toolProps.getProperty("nonce");
			lj.issued = Long.valueOf(System.currentTimeMillis() / 1000L);
			lj.expires = lj.issued + 3600L;
			lj.deployment_id = getDeploymentId(context_id);

			String lti1_roles = fixLegacyRoles(ltiProps.getProperty("roles"));
			if (lti1_roles != null ) {
				Set<String> roleSet = new HashSet<String>();
				roleSet.addAll(Arrays.asList(lti1_roles.split(",")));
				lj.roles.addAll(roleSet);
			} else {
				lj.roles.add(LaunchJWT.ROLE_LEARNER);
			}

			String resource_link_id = ltiProps.getProperty(LTIConstants.RESOURCE_LINK_ID);
			if ( resource_link_id != null ) {
				lj.resource_link = new ResourceLink();
				lj.resource_link.id = resource_link_id;
				lj.resource_link.title = ltiProps.getProperty(LTIConstants.RESOURCE_LINK_TITLE);
				lj.resource_link.description = ltiProps.getProperty(LTIConstants.RESOURCE_LINK_DESCRIPTION);
			}

			// Construct the LTI 1.1 -> LTIAdvantage transition claim
			// https://www.imsglobal.org/spec/lti/v1p3/migr#lti-1-1-migration-claim
			String oauth_consumer_key = tool.consumerkey;
			String oauth_secret = getSecret(tool);
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
			lj.context.label = ltiProps.getProperty(LTIConstants.CONTEXT_LABEL);
			lj.context.title = ltiProps.getProperty(LTIConstants.CONTEXT_TITLE);
			lj.context.type.add(Context.COURSE_OFFERING);

			lj.tool_platform = new ToolPlatform();
			lj.tool_platform.name = "Sakai";
			lj.tool_platform.guid = ltiProps.getProperty(LTIConstants.TOOL_CONSUMER_INSTANCE_GUID, "guid-missing-42");

			lj.tool_platform.version = ltiProps.getProperty(LTIConstants.TOOL_CONSUMER_INFO_VERSION);
			lj.tool_platform.product_family_code = ltiProps.getProperty(LTIConstants.TOOL_CONSUMER_INFO_PRODUCT_FAMILY_CODE);
			lj.tool_platform.url = ltiProps.getProperty(LTIConstants.TOOL_CONSUMER_INSTANCE_URL);
			lj.tool_platform.description = ltiProps.getProperty(LTIConstants.TOOL_CONSUMER_INSTANCE_DESCRIPTION);

			LaunchLIS lis = new LaunchLIS();
			lis.person_sourcedid = ltiProps.getProperty(LTIConstants.LIS_PERSON_SOURCEDID);
			lis.course_offering_sourcedid = ltiProps.getProperty(LTIConstants.LIS_COURSE_OFFERING_SOURCEDID);
			lis.course_section_sourcedid = ltiProps.getProperty(LTIConstants.LIS_COURSE_SECTION_SOURCEDID);
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

			int allowOutcomes = (tool.allowoutcomes != null && Boolean.TRUE.equals(tool.allowoutcomes)) ? 1 : 0;
			int allowRoster = (tool.allowroster != null && Boolean.TRUE.equals(tool.allowroster)) ? 1 : 0;
			int allowLineItems = (tool.allowlineitems != null && Boolean.TRUE.equals(tool.allowlineitems)) ? 1 : 0;

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

			if (context_id != null &&
				  ( (allowOutcomes != 0 && outcomesEnabled()) || (allowLineItems != 0 && lineItemsEnabled()) )
				) {
				// Let the tool know what we are capable of supporting
				Endpoint endpoint = new Endpoint();
				endpoint.scope = new ArrayList<>();
				endpoint.scope.add(LTI13ConstantsUtil.SCOPE_LINEITEM);
				endpoint.scope.add(LTI13ConstantsUtil.SCOPE_LINEITEM_READONLY);
				endpoint.scope.add(LTI13ConstantsUtil.SCOPE_SCORE);
				endpoint.scope.add(LTI13ConstantsUtil.SCOPE_RESULT_READONLY);

				if ( allowOutcomes != 0 && outcomesEnabled() && content != null) {
					SakaiLineItem defaultLineItem = LineItemUtil.getDefaultLineItem(site, content);
					if ( defaultLineItem != null ) endpoint.lineitem = defaultLineItem.id;
				}
				if ( allowOutcomes != 0 && outcomesEnabled() ) {
					// SAK-47261 - Legacy URL patterns with signed placement
					// endpoint.lineitems = getOurServerUrl() + LTI13_PATH + "lineitems/" + signed_placement;
					endpoint.lineitems = getOurServerUrl() + LTI13_PATH + "lineitems/" + context_id;
				}
				lj.endpoint = endpoint;
			}

			if (allowRoster != 0 && rosterEnabled() && context_id != null) {
				NamesAndRoles nar = new NamesAndRoles();
				// SAK-47261 - Legacy URL patterns with signed placement
				// nar.context_memberships_url = getOurServerUrl() + LTI13_PATH + "namesandroles/" + signed_placement;
				nar.context_memberships_url = getOurServerUrl() + LTI13_PATH + "namesandroles/" + context_id;
				lj.names_and_roles = nar;

				// SAK-48745 - Add support for GroupService
				GroupService gs = new GroupService();
				gs.context_groups_url = getOurServerUrl() + LTI13_PATH + "groupservice/" + context_id;
				lj.group_service = gs;
			}

			// SAK-50682 - Add support for PNPService
			String pnpBaseUrl = ServerConfigurationService.getString("lti.pnp.baseurl", null);
			Boolean pnpUseEmail = ServerConfigurationService.getBoolean("lti.pnp.use_email", false);
			String user_email = ltiProps.getProperty(LTIConstants.LIS_PERSON_CONTACT_EMAIL_PRIMARY);
			if ( StringUtils.isNotEmpty(user_id) && StringUtils.isNotEmpty(pnpBaseUrl) ) {
				PNPService ps = new PNPService();
				String afapnp_endpoint_url = pnpBaseUrl;
				if ( pnpUseEmail && StringUtils.isNotEmpty(user_email) ) {
					afapnp_endpoint_url = afapnp_endpoint_url.replace("@", user_email);
				} else {
					afapnp_endpoint_url = afapnp_endpoint_url.replace("@", user_id);
				}
				ps.afapnp_endpoint_url = afapnp_endpoint_url;
				lj.pnp_service = ps;
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
				content_item_return_url=http://localhost:8080/portal/tool/6bdb721d-07f9-445b-a973-2190b50654cc/sakai.lti.admin.helper.helper?eventSubmit_doContentItemResponse=Save&sakai.session=22702e53-60f3-45fd-b8db-a9d803eed3d4.MacBook-Pro-92.local&returnUrl=http%3A%2F%2Flocalhost%3A8080%2Fportal%2Fsite%2F92e7ddf2-1c60-486c-97ae-bc2ffbde8e67%2Ftool%2F4099b420-119a-4c39-9e05-0a933b2e5858%2FBltiPicker%3F3%26itemId%3D-1%26addBefore%3D&panel=PostContentItem&tool_id=13&sakai_csrf_token=458f712764cd597e96be99d2bab6d9da17d63c3834bc3770851a3d93ea8cdb83
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

			// Add post-launch call back claim
			if ( checkSendPostVerify() ) {
				lj.origin = getOurServerUrl();
				lj.postverify = getOurServerUrl() + LTI13_PATH + "postverify/" + signed_placement;
			}

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
				ci.deep_link_return_url = ltiProps.getProperty(LTIConstants.CONTENT_ITEM_RETURN_URL);
				ci.data = ltiProps.getProperty("data");
				lj.deep_link = ci;
			}

			String ljs = JacksonUtil.toString(lj);
			log.debug("ljs = {}", ljs);

			KeyPair kp = SakaiKeySetUtil.getCurrent();
			Key privateKey = kp.getPrivate();
			Key publicKey = kp.getPublic();

			if ( privateKey == null | publicKey == null ) {
				return postError("<p>" + getRB(rb, "error.no.pki", "Public and/or Private Key(s) not configured.") + "</p>");
			}

			String kid = LTI13KeySetUtil.getPublicKID(publicKey);

			String jws = Jwts.builder().setHeaderParam("kid", kid).
					setPayload(ljs).signWith(privateKey).compact();

			log.debug("jws = {}", jws);

			String debugProperty = toolProps.getProperty(LTIService.LTI_DEBUG);
			boolean dodebug = LTI_PORTLET_ON.equals(debugProperty) || "1".equals(debugProperty);

			String state = toolProps.getProperty("state");
			state = StringUtils.trimToNull(state);

			// This is a comma separated list of valid redirect URLs - lame as heck
			String lti13_tool_redirect = StringUtils.trimToNull(tool.lti13OidcRedirect);

			// If we have been told to send this to a redirect_uri instead of a launch...
			String redirect_uri = req.getParameter("redirect_uri");
			if ( redirect_uri != null && lti13_tool_redirect != null ) {
				if ( lti13_tool_redirect.indexOf(redirect_uri) >= 0 ) {
					launch_url = redirect_uri;
				}
			}

			String launch_error = rb.getString("error.submit.timeout")+" "+launch_url;
			String html = getJwsHTMLForm(launch_url, "id_token", jws, ljs, state, launch_error, dodebug);

			String[] retval = {html, launch_url};
			return retval;
		}

		public static String getJwsHTMLForm(String launch_url, String form_field, String jwt, String jsonStr, String state, String launch_error, boolean dodebug) {

			String submit_form_id = "jwt-launch-"+jwt.hashCode();
			StringBuffer sb = new StringBuffer();

			sb.append("<form action=\"" + launch_url + "\" id=\""+ submit_form_id + "\" method=\"POST\">\n");
			sb.append("    <input type=\"hidden\" name=\""+form_field+"\" value=\"" + LTIUtil.htmlspecialchars(jwt) + "\" />\n");
			sb.append("    <input type=\"hidden\" name=\"lti_storage_target\" value=\"_parent\" />\n");

			if ( state != null ) {
				sb.append("    <input type=\"hidden\" name=\"state\" value=\"" + LTIUtil.htmlspecialchars(state) + "\" />\n");
			}

			if ( dodebug ) {
				sb.append("    <input type=\"submit\" value=\"Proceed with LTI 1.3 Launch\" />\n</form>\n");
			}
			sb.append("    </form>\n");

			if ( dodebug ) {
				sb.append("<p>\n--- Unencoded JWT:<br/><pre>\n");
				sb.append(LTIUtil.htmlspecialchars(jsonStr));
				sb.append("</pre>\n</p>\n<p>\n--- State:<br/>");
				sb.append(LTIUtil.htmlspecialchars(state));
				sb.append("</p>\n<p>\n--- Encoded JWT:<br/>");
				sb.append(LTIUtil.htmlspecialchars(jwt));
				sb.append("</p>\n");
			} else {
				sb.append("<script>\n");
				sb.append("setTimeout(function() { alert(\""+LTIUtil.htmlspecialchars(launch_error)+"\"); }, 4000);\n");
				sb.append("</script>\n");
			}

			boolean autosubmit = !dodebug;
			String extraJS = getLaunchJavaScript(submit_form_id, autosubmit);

			sb.append("<script>\n");
			sb.append(extraJS);
			sb.append("</script>\n");
			return sb.toString();
		}

		public static String getSourceDID(User user, Placement placement, Properties config) {
			String placementSecret = StringUtils.trimToNull(getCorrectProperty(config, LTI_PORTLET_PLACEMENTSECRET, placement));
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
		 * get a signed placement from a content item. Bean overload.
		 */
		public static String getResourceLinkId(LtiContentBean content) {
			if (content == null || content.id == null) return null;
			return "content:" + content.id;
		}

		public static String getResourceLinkId(Map<String, Object> content) {
			return getResourceLinkId(LtiContentBean.of(content));
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
		 * get a signed placement from a content item. Bean overload.
		 */
		public static String getSignedPlacement(LtiContentBean content) {
			if (content == null) return null;
			String context_id = content.getSiteId();
			String placement_secret = content.getPlacementsecret();
			String resource_link_id = getResourceLinkId(content);
			if (placement_secret == null) return null;
			return getSignedPlacement(context_id, resource_link_id, placement_secret);
		}

		public static String getSignedPlacement(Map<String, Object> content) {
			return getSignedPlacement(LtiContentBean.of(content));
		}

		public static String trackResourceLinkID(LtiContentBean oldContent) {
			return trackResourceLinkID(oldContent != null ? oldContent.asMap() : null);
		}

		public static String trackResourceLinkID(Map<String, Object> oldContent) {
			boolean retval = false;

			String old_settings = (String) oldContent.get(LTIService.LTI_SETTINGS);
			JSONObject old_json = LTIUtil.parseJSONObject(old_settings);
			String old_id_history = (String) old_json.get(LTIService.LTI_ID_HISTORY);

			String old_resource_link_id = getResourceLinkId(oldContent);

			String id_history = LTIUtil.mergeCSV(old_id_history, null, old_resource_link_id);
			return id_history;
		}

		public static boolean trackResourceLinkID(LtiContentBean newContent, LtiContentBean oldContent) {
			return trackResourceLinkID(newContent != null ? newContent.asMap() : null, oldContent != null ? oldContent.asMap() : null);
		}

		public static boolean trackResourceLinkID(Map<String, Object> newContent, Map<String, Object> oldContent) {
			boolean retval = false;

			String old_settings = (String) oldContent.get(LTIService.LTI_SETTINGS);
			JSONObject old_json = LTIUtil.parseJSONObject(old_settings);
			String old_id_history = (String) old_json.get(LTIService.LTI_ID_HISTORY);

			String new_settings = (String) newContent.get(LTIService.LTI_SETTINGS);
			JSONObject new_json = LTIUtil.parseJSONObject(new_settings);
			String new_id_history = (String) new_json.get(LTIService.LTI_ID_HISTORY);

			String old_resource_link_id = getResourceLinkId(oldContent);

			String id_history = LTIUtil.mergeCSV(old_id_history, new_id_history, old_resource_link_id);
			if ( id_history.equals(new_id_history) ) return false;

			new_json.put(LTIService.LTI_ID_HISTORY, id_history);
			newContent.put(LTIService.LTI_SETTINGS, new_json.toString());
			return true;
		}

		private static String[] postError(String str) {
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
		private static void setProperty(Properties props, String key, String value) {
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
				log.error("SiteParticipantHelper.getExternalRealmId: site realm not found {}", e.toString());
				log.debug("Stacktrace:", e);
			}
			return rv;
		} // getExternalRealmId

		// Look through a series of secrets from the properties based on the launchUrl
		private static String getToolConsumerInfo(String launchUrl, String data) {
			String default_secret = ServerConfigurationService.getString("lti.consumer_instance_" + data, null);
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
			String org_info = ServerConfigurationService.getString("lti.consumer_instance_" + data + "." + hostName, null);
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
				String propName = "lti.consumer_instance_" + data + "." + hostPart;
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
			return LTIUtil.validateMessage(request, URL, oauth_secret, expected_oauth_key);
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
			return Boolean.valueOf(matched);
		}

		// Look up the gradebook column so we can find the max points
		GradingService gradingService = (GradingService) ComponentManager
				.get("org.sakaiproject.grading.api.GradingService");

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
		String title = normalProps.getProperty(LTI_PORTLET_ASSIGNMENT);
		log.debug("Column Title={}", title);
		if (title == null) {
			return "Gradebook column not set in placement";
		}

        // Load assignment if it exists
		org.sakaiproject.assignment.api.model.Assignment assignment;
        String contentKeyStr = normalProps.getProperty("contentKey");
        Long contentKey = LTIUtil.toLongKey(contentKeyStr);
        if (contentKey > 0) {
                Map<String, Object> content = new TreeMap<String, Object> ();
                content.put(LTIService.LTI_ID, contentKey);
                try {
                    assignment = getAssignment(site, content);
                } catch (Exception e) {
                    log.error("Error getting assignment in handleGradebook", e);
                    return "Error retrieving assignment: " + e.getMessage();
                }
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
			try {
				retval = handleAssignment(assignment, user_id, scoreObj);
				return retval;
			} catch (Exception e) {
				log.error("Error in handleAssignment", e);
				return "Error processing assignment: " + e.getMessage();
			}
		}

		// Now read, set, or delete the non-assignment grade...
		// For LTI 1.1 columns we don't need to mark them for AGS LineItems retrieval
		Long tool_id = null;
		Map<String, Object> content = null;

		Session sess = SessionManager.getCurrentSession();

		SakaiLineItem lineItem = new SakaiLineItem();
		lineItem.scoreMaximum = 100.0D;

		org.sakaiproject.grading.api.Assignment gradebookColumn = getGradebookColumn(site, user_id, title, lineItem, tool_id, content);
		if (gradebookColumn == null) {
			log.warn("gradebookColumn or Id is null, cannot proceed with grading in site {} for column {}", siteId, title);
			return "Grade failure siteId=" + siteId;
		}
		String gradebookUid = gradebookColumn.getGradebookUid() != null ? gradebookColumn.getGradebookUid() : siteId;

		try {
			// Indicate "who" is setting this grade - needs to be a real user account
			String gb_user_id = ServerConfigurationService.getString(
					"lti.outcomes.userid", "admin");
			String gb_user_eid = ServerConfigurationService.getString(
					"lti.outcomes.usereid", gb_user_id);
			sess.setUserId(gb_user_id);
			sess.setUserEid(gb_user_eid);
			if (isRead) {
				String actualGrade = gradingService.getAssignmentScoreString(gradebookUid, siteId, gradebookColumn.getId(), user_id);
				Double dGrade = null;
				if (StringUtils.isNotBlank(actualGrade)) {
					dGrade = Double.valueOf(actualGrade);
					dGrade = dGrade / gradebookColumn.getPoints();
				}
				CommentDefinition commentDef = gradingService.getAssignmentScoreComment(gradebookUid, gradebookColumn.getId(), user_id);
				Map<String, Object> retMap = new TreeMap<>();
				retMap.put("grade", dGrade);
				if (commentDef != null) {
					retMap.put("comment", commentDef.getCommentText());
				}
				retval = retMap;
			} else if (isDelete) {
				gradingService.setAssignmentScoreString(gradebookUid, siteId, gradebookColumn.getId(), user_id, null, "External Outcome", null);
				gradingService.deleteAssignmentScoreComment(gradebookUid, gradebookColumn.getId(), user_id);
				log.info("Delete Score site={} gradebook={} title={} user_id={}", siteId, gradebookUid, title, user_id);
				retval = Boolean.TRUE;
			} else {
				String gradeI18n = getRoundedGrade(scoreGiven, gradebookColumn.getPoints());
				gradeI18n = (",").equals((ComponentManager.get(FormattedText.class)).getDecimalSeparator()) ? gradeI18n.replace(".",",") : gradeI18n;
				gradingService.setAssignmentScoreString(gradebookUid, siteId, gradebookColumn.getId(), user_id, gradeI18n, "External Outcome", null);
				if ( StringUtils.isBlank(comment) ) {
					gradingService.deleteAssignmentScoreComment(gradebookUid, gradebookColumn.getId(), user_id);
				} else {
					gradingService.setAssignmentScoreComment(gradebookUid, gradebookColumn.getId(), user_id, comment);
				}

				log.info("Stored Score site={} gradebook={} title={} user_id={} score={}", siteId, gradebookUid, title, user_id, scoreGiven);

				retval = Boolean.TRUE;
			}
		} catch (Exception e) {
			retval = "Grade failure " + e.toString() + " siteId=" + siteId;
			log.warn("handleGradebook Grade failure in site: {}, error: {}", siteId, e);
		} finally {
			sess.invalidate(); // Make sure to leave no traces
		}

		return retval;
	}

	/**
	 * Handle gradebook LTI13 with content Map
	 * @param site the site
	 * @param tool_id the tool id
	 * @param content the content Map (can be null)
	 * @param userId the user id
	 * @param lineitem_key the line item key
	 * @param scoreObj the score object
	 * @return the result

	 * When lineitem_key is null we are the "default" lineitem associated with the content object.
	 * if the content item is associated with an assignment, we talk to the assignment API,
	 * if the content item is not associated with an assignment, we talk to the gradebook API
     * content can be null if the lineitem_key is defined.
	 * If the scoreGiven is null, we are clearing out the grade value.
	 * Note that scoreObj.userId is subject, not userId (naming inconsistency in IMS specs but we have to follow here).
	 */
	public static Object handleGradebookLTI13(Site site, Long tool_id, Map<String, Object> content, String userId,
			Long lineitem_key, Score scoreObj) {
		return handleGradebookLTI13(site, tool_id, LtiContentBean.of(content), userId, lineitem_key, scoreObj);
	}

	public static Object handleGradebookLTI13(Site site, Long tool_id, LtiContentBean content, String userId,
			Long lineitem_key, Score scoreObj) {

		Object retval;
		String title;

		log.debug("siteid: {} tool_id: {} content: {} lineitem_key: {} userId: {} scoreObj: {}",
			site.getId(), tool_id, (content == null ? "null" : content.id), lineitem_key, userId, scoreObj);

		// An empty / null score given means to delete the score
		SakaiLineItem lineItem = new SakaiLineItem();
		String siteId = site.getId();

		org.sakaiproject.grading.api.Assignment gradebookColumn;

		// Are we in the default lineitem for the content object?
		// Check if this is as assignment placement and handle it if it is
		if ( lineitem_key == null ) {
			if (content == null ) {
				log.error("handleGradebookLTI13 requires either content to be not null or have a lineitem_key");
				return "handleGradebookLTI13 requires either content to be not null or have a lineitem_key";
			}
			pushAdvisor(); // Add security advisor to allow access to assignments
			try {
				org.sakaiproject.assignment.api.model.Assignment assignment = getAssignment(site, content);
				if ( assignment != null ) {
					try {
						retval = handleAssignment(assignment, userId, scoreObj);
						return retval;
					} catch (Exception e) {
						log.error("Error in handleAssignment", e);
						return "Error processing assignment: " + e.getMessage();
					}
				}
			} catch (Exception e) {
				log.error("Error in assignment processing", e);
				return "Error processing assignment: " + e.getMessage();
			} finally {
				popAdvisor(); // Remove security advisor
			}
			title = content.title;
			if (title == null || title.length() < 1) {
				log.error("Could not determine content title {}", content.id);
				return "Could not determine content title key=" + content.id;
			}
			gradebookColumn = getGradebookColumn(site, userId, title, lineItem, tool_id, content);
		} else {
			gradebookColumn = LineItemUtil.getColumnByKeyDAO(siteId, tool_id, lineitem_key);
			if ( gradebookColumn == null || gradebookColumn.getName() == null ) {
				log.error("Could not determine assignment_name title {}", (content != null ? content.id : null));
				return "Unable to load column for lineitem_key="+lineitem_key;
			}

			// Check if this is a gradebook column that is owned by assignments
			String external_id = gradebookColumn.getExternalId();
			log.debug("external_id: {} {}", external_id);
			if ( external_id != null && LineItemUtil.isAssignmentColumn(external_id) ) {
				pushAdvisor(); // Add security advisor to allow access to assignments
				try {
					org.sakaiproject.assignment.api.AssignmentService assignmentService = ComponentManager.get(org.sakaiproject.assignment.api.AssignmentService.class);
					org.sakaiproject.assignment.api.model.Assignment assignment;
					try {
						org.sakaiproject.assignment.api.AssignmentReferenceReckoner.AssignmentReference assignmentReference = org.sakaiproject.assignment.api.AssignmentReferenceReckoner.reckoner().reference(external_id).reckon();
						log.debug("assignmentReference.id {}", assignmentReference.getId());
						assignment = assignmentService.getAssignment(assignmentReference.getId());
					} catch (Exception e) {
						log.error("Error getting assignment", e);
						return "Error retrieving assignment: " + e.getMessage();
					}

					if ( assignment != null ) {
						log.debug("Gradebook column is owned by assignment: {}", assignment.getId());
						try {
							retval = handleAssignment(assignment, userId, scoreObj);
							return retval;
						} catch (Exception e) {
							log.error("Error in handleAssignment", e);
							return "Error processing assignment: " + e.getMessage();
						}
					}
				} finally {
					popAdvisor(); // Remove security advisor
				}
			}
			title = gradebookColumn.getName();
		}

		if (gradebookColumn == null) {
			log.warn("gradebookColumn or Id is null, cannot proceed with grading for site {}, title {}", siteId, title);
			return "Grade failure siteId=" + siteId;
		}

		// Send the grades to the gradebook
		Double scoreGiven = scoreObj.getScoreGiven();
		String comment = scoreObj.comment;
		Double scoreMaximum = scoreObj.getScoreMaximum();
		log.debug("scoreGiven={} scoreMaximum={} userId={} comment={}", scoreGiven, scoreMaximum, userId, comment);

		// Look up the gradebook column so we can find the max points
		GradingService gradingService = (GradingService) ComponentManager
				.get("org.sakaiproject.grading.api.GradingService");
		String gradebookUid = gradebookColumn.getGradebookUid() != null ? gradebookColumn.getGradebookUid() : siteId;

		// Fall through to send the grade to a gradebook column
		// Now read, set, or delete the grade...
		Session sess = SessionManager.getCurrentSession();

		try {
			// Indicate "who" is setting this grade - needs to be a real user account
			String gb_user_id = ServerConfigurationService.getString(
					"lti.outcomes.userid", "admin");
			String gb_user_eid = ServerConfigurationService.getString(
					"lti.outcomes.usereid", gb_user_id);
			sess.setUserId(gb_user_id);
			sess.setUserEid(gb_user_eid);
			if (scoreGiven == null) {
				gradingService.setAssignmentScoreString(gradebookUid, siteId, gradebookColumn.getId(), userId, null, "External Outcome", null);

				// Since LTI 13 uses update semantics on grade delete, we accept the comment if it is there
				if ( StringUtils.isBlank(comment) ) {
					gradingService.deleteAssignmentScoreComment(gradebookUid, gradebookColumn.getId(), userId);
				} else {
					gradingService.setAssignmentScoreComment(gradebookUid, gradebookColumn.getId(), userId, comment);
				}
				log.info("Delete Score site={} gradebook={} title={} userId={}", siteId, gradebookUid, title, userId);
				return Boolean.TRUE;
			} else {
				Double gradebookColumnPoints = gradebookColumn.getPoints();
				Double assignedGrade = null;
				if ( scoreMaximum == null || gradebookColumnPoints.equals(scoreMaximum) ) {
					assignedGrade = scoreGiven;
				} else {
					assignedGrade = (scoreGiven / scoreMaximum) * gradebookColumnPoints;
				}
				String gradeI18n = assignedGrade.toString();
				gradeI18n = (",").equals((ComponentManager.get(FormattedText.class)).getDecimalSeparator()) ? gradeI18n.replace(".",",") : gradeI18n;
				gradingService.setAssignmentScoreString(gradebookUid, siteId, gradebookColumn.getId(), userId, gradeI18n, "External Outcome", null);
				if ( StringUtils.isBlank(comment) ) {
					gradingService.deleteAssignmentScoreComment(gradebookUid, gradebookColumn.getId(), userId);
				} else {
					gradingService.setAssignmentScoreComment(gradebookUid, gradebookColumn.getId(), userId, comment);
				}
				log.info("Stored Score site={} gradebook={} title={} userId={} score={}", siteId, gradebookUid, title, userId, scoreGiven);
				return Boolean.TRUE;
			}
		} catch (NumberFormatException | AssessmentNotFoundException e) {
			retval = "Grade failure " + e.toString() + " siteId=" + siteId;
			log.warn("handleGradebook Grade failure in site: {}, error: {}", siteId, e);
		} finally {
			sess.invalidate(); // Make sure to leave no traces
		}
		return Boolean.FALSE;
	}

	/**
	 * Handle gradebook LTI13 with content bean
	 * @param site the site
	 * @param tool_id the tool id
	 * @param content the content bean (can be null)
	 * @param userId the user id
	 * @param lineitem_key the line item key
	 * @param scoreObj the score object
	 * @return the result
	 */
	public static org.sakaiproject.assignment.api.model.Assignment getAssignment(Site site, Map<String, Object> content) {
		return getAssignment(site, LtiContentBean.of(content));
	}

	public static org.sakaiproject.assignment.api.model.Assignment getAssignment(Site site, LtiContentBean content) {
		Long contentId = (content != null) ? content.getId() : null;
		if ( contentId == null ) return null;

		pushAdvisor();
		try {

			org.sakaiproject.assignment.api.AssignmentService assignmentService = ComponentManager.get(org.sakaiproject.assignment.api.AssignmentService.class);
			Collection<org.sakaiproject.assignment.api.model.Assignment> assignments = assignmentService.getAssignmentsForContext(site.getId());

			for (org.sakaiproject.assignment.api.model.Assignment a : assignments) {
				Integer assignmentContentId = a.getContentId();
				if ( assignmentContentId == null ) continue;
				if ( contentId.longValue() != assignmentContentId.longValue() ) continue;
				return a;
			}
			return null;
		} catch (Exception e) {
			log.error("Error getting assignment", e);
			return null;
		} finally {
			popAdvisor();
		}
	}

	public static Object handleAssignment(org.sakaiproject.assignment.api.model.Assignment a, String userId, Score scoreObj) {

		Integer	scaledGrade = null;
		String stringGrade = null;
		log.debug("handleAssignment assignment: {} {} user: {} {}", a.getId(), a.getTitle(), userId, scoreObj.getScoreGiven());

		// Scale up the score if appropriate
		if (scoreObj.scoreGiven != null) {
			Integer assignmentMax = a.getMaxGradePoint();
			Integer assignmentScale = a.getScaleFactor();
			// Sanity check - don't ever divide by zero
			if ( assignmentScale == 0 ) assignmentScale = 1;

			Integer incomingScoreGiven = Double.valueOf(scoreObj.getScoreGiven() * assignmentScale).intValue();
			Integer incomingScoreMax = Double.valueOf(scoreObj.getScoreMaximum() * assignmentScale).intValue();
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
		org.sakaiproject.site.api.SiteService siteService = ComponentManager.get(org.sakaiproject.site.api.SiteService.class);

		String activityProgress = scoreObj.activityProgress != null ? scoreObj.activityProgress : Score.ACTIVITY_COMPLETED ;
		String gradingProgress = scoreObj.gradingProgress != null ? scoreObj.gradingProgress : Score.GRADING_FULLYGRADED;
		log.debug("activityProgress: {} gradingProgress: {}", activityProgress, gradingProgress);

		User user;
		try {
			user = UserDirectoryService.getUser(userId);
		} catch (org.sakaiproject.user.api.UserNotDefinedException e) {
			log.error("Could not look up user {}: {}", userId, e);
			return "Could not look up user " + userId;
		}

		pushAdvisor();
		try {
			org.sakaiproject.assignment.api.model.AssignmentSubmission submission = null;
			
			// For group assignments, we need to find the group the user belongs to
			if (a.getIsGroup()) {
				log.debug("This is a group assignment {}", a.getId());
				String context = a.getContext();
				try {
					org.sakaiproject.site.api.Site site = siteService.getSite(context);
					// Find all groups user is a member of that are also assignment groups
					Set<String> assignmentGroups = a.getGroups();
					Collection<org.sakaiproject.site.api.Group> userGroups = site.getGroupsWithMember(userId);
					
					// Find the first matching group between assignment groups and user groups
					String groupId = null;
					for (org.sakaiproject.site.api.Group group : userGroups) {
						if (assignmentGroups.contains(group.getReference())) {
							groupId = group.getId();
							break;
						}
					}
					
					if (groupId != null) {
						log.debug("Found group for user: groupId={} userId={}", groupId, userId);
						// Try to get existing group submission
						submission = assignmentService.getSubmission(a.getId(), groupId);
						if (submission == null) {
							// Create new submission for the group
							submission = assignmentService.addSubmission(a.getId(), groupId);
						}
					} else {
						log.warn("No matching group found for user in group assignment: userId={}, assignmentId={}", userId, a.getId());
					}
				} catch (org.sakaiproject.exception.IdUnusedException e) {
					log.warn("Site not found: {}, {}", context, e.toString());
				}
			}
			
			// If this isn't a group assignment, or we couldn't find a group, get/create individual submission
			if (submission == null) {
				submission = assignmentService.getSubmission(a.getId(), user);
				if (submission == null) {
					submission = assignmentService.addSubmission(a.getId(), user.getId());
				}
			}
			
			log.debug("submission: {} {} {}", scaledGrade, user.getId(), submission);

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

			// From LTI:
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
			
			// For individual submissions or when we know the specific user
			if (!a.getIsGroup() || submission.getSubmitters().stream().anyMatch(s -> s.getSubmitter().equals(userId))) {
				submission.getSubmitters().stream().filter(s -> s.getSubmitter().equals(userId)).findFirst().ifPresent(s -> s.setSubmittee(true));
			}

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
				log.debug("Submitted submission={} userId={} log={}", submission.getId(), userId, logEntry.toString());
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
				.map(k -> Integer.valueOf(StringUtils.split(k, "log")[0]))
				.sorted()
				.collect(Collectors.toList());
		int next = keys.isEmpty() ? 0 : keys.get(keys.size() - 1) + 1;
		return keyPrefix + next;
	}

	public static org.sakaiproject.grading.api.Assignment getGradebookColumn(Site site, String userId, String title, SakaiLineItem lineItem, Long tool_id, Map<String, Object> content) {
		return getGradebookColumn(site, userId, title, lineItem, tool_id, LtiContentBean.of(content));
	}

	public static org.sakaiproject.grading.api.Assignment getGradebookColumn(Site site, String userId, String title, SakaiLineItem lineItem, Long tool_id, LtiContentBean content) {
		// Look up the gradebook columns so we can find the max points
		GradingService gradingService = (GradingService) ComponentManager.get("org.sakaiproject.grading.api.GradingService");

		String siteId = site.getId();

		if ( lineItem == null ) lineItem = new SakaiLineItem();
		Double scoreMaximum = lineItem.scoreMaximum == null ? 100D : lineItem.scoreMaximum;

		org.sakaiproject.grading.api.Assignment returnColumn = null;
		String returnGradebookUid = siteId;

		pushAdvisor();

		try {
			List<String> userGradebooks = Arrays.asList(siteId);
			if (gradingService.isGradebookGroupEnabled(siteId)) {
				userGradebooks = gradingService.getGradebookInstancesForUser(siteId, userId);
				returnGradebookUid = userGradebooks.get(0);
			}
			for (String gradebookUid : userGradebooks) {
				List<org.sakaiproject.grading.api.Assignment> gradebookColumns = gradingService.getAssignments(gradebookUid, siteId, SortType.SORT_BY_NONE);
				for (org.sakaiproject.grading.api.Assignment aColumn : gradebookColumns) {
					if (title.trim().equalsIgnoreCase(aColumn.getName().trim())) {
						returnColumn = aColumn;
						returnGradebookUid = gradebookUid;
						break;
					}
				}
			}
		} finally {
			popAdvisor();
		}

		// Attempt to add column to grade book
		if (returnColumn == null) {
			pushAdvisor();
			try {
				returnColumn = new org.sakaiproject.grading.api.Assignment();
				returnColumn.setPoints(scoreMaximum);
				returnColumn.setExternallyMaintained(false);
				returnColumn.setName(title);
				if ( tool_id != null && content != null ) {
					String external_id = LineItemUtil.constructExternalId(content, lineItem);
					returnColumn.setExternalAppName(LineItemUtil.GB_EXTERNAL_APP_NAME);
					returnColumn.setExternalId(external_id);
				}
				// SAK-40043
				Boolean releaseToStudent = lineItem.releaseToStudent == null ? Boolean.TRUE : lineItem.releaseToStudent; // Default to true
				Boolean includeInComputation = lineItem.includeInComputation == null ? Boolean.TRUE : lineItem.includeInComputation; // Default true
				returnColumn.setReleased(releaseToStudent); // default true
				returnColumn.setUngraded(! includeInComputation); // default false
				Long gradebookColumnId = gradingService.addAssignment(returnGradebookUid, siteId, returnColumn);
				returnColumn.setId(gradebookColumnId);
				returnColumn.setGradebookUid(returnGradebookUid);
				log.info("Added gradebook column: {} with Id: {}", title, gradebookColumnId);
			} catch (ConflictingAssignmentNameException e) {
				log.warn("ConflictingAssignmentNameException while adding gradebook column {}", e.toString());
				returnColumn = null; // Just to make sure
			} catch (Exception e) {
				log.warn("Exception (may be because GradeBook has not yet been added to the Site) {}", e.toString());
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
		if (scoreGiven == null || scoreGiven < 0.0 || scoreGiven > 1.0) {
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
		String[] fieldList = {LTI_PORTLET_KEY, LTIService.LTI_SECRET, LTI_PORTLET_PLACEMENTSECRET,
			LTI_PORTLET_OLDPLACEMENTSECRET,
			LTI_PORTLET_ASSIGNMENT, LTI_PORTLET_ALLOWROSTER, LTI_PORTLET_RELEASENAME, LTI_PORTLET_RELEASEEMAIL,
			LTI_PORTLET_TOOLSETTING};

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
				String value = StringUtils.trimToNull(getCorrectProperty(config, field, placement));
				if (field.equals(LTI_PORTLET_ALLOWROSTER)) {
					field = LTIService.LTI_ALLOWROSTER;
				}
				if (field.equals(LTI_PORTLET_ASSIGNMENT)) {
					String outval = outcomesEnabled() && StringUtils.isNotEmpty(value) ? "1" : "0";
				}
				if (field.equals(LTI_PORTLET_RELEASENAME)) {
					field = LTIService.LTI_SENDNAME;
				}
				if (field.equals(LTI_PORTLET_RELEASEEMAIL)) {
					field = LTIService.LTI_SENDEMAILADDR;
				}
				// These field names are the same but for completeness..
				if (field.equals(LTI_PORTLET_OLDPLACEMENTSECRET)) {
					field = LTIService.LTI_OLDPLACEMENTSECRET;
				}
				// These field names are the same but for completeness..
				if (field.equals(LTI_PORTLET_PLACEMENTSECRET)) {
					field = LTIService.LTI_PLACEMENTSECRET;
				}
				if (value == null) {
					continue;
				}
				if (field.equals(LTI_PORTLET_KEY)) {
					field = LTIService.LTI_CONSUMERKEY;
				}
				retval.setProperty(field, value);
			}
		} else { // Get information from content item
			Map<String, Object> content;
			Map<String, Object> tool;

			String contentStr = placement_id.substring(8);
			Long contentKey = LTIUtil.toLongKey(contentStr);
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

			Long toolKey = LTIUtil.toLongKey(content.get(LTIService.LTI_TOOL_ID));
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
				Properties info = Foorm.parseFormString(formInput);
				String field = info.getProperty("field", null);
				String type = info.getProperty("type", null);
				Object o = tool.get(field);
				if (o instanceof String) {
					retval.setProperty(field, (String) o);
					continue;
				}
				if ("checkbox".equals(type)) {
					int check = LTIUtil.toInt(o);
					if (check == 1) {
						retval.setProperty(field, LTI_PORTLET_ON);
					} else {
						retval.setProperty(field, LTI_PORTLET_OFF);
					}
				}
			}

			for (String formInput : LTIService.CONTENT_MODEL) {
				Properties info = Foorm.parseFormString(formInput);
				String field = info.getProperty("field", null);
				String type = info.getProperty("type", null);
				Object o = content.get(field);
				if (o instanceof String) {
					retval.setProperty(field, (String) o);
					continue;
				}
				if ("checkbox".equals(type)) {
					int check = LTIUtil.toInt(o);
					if (check == 1) {
						retval.setProperty(field, LTI_PORTLET_ON);
					} else {
						retval.setProperty(field, LTI_PORTLET_OFF);
					}
				}
			}
			String aTitle = (String) content.get("title");
			retval.setProperty(LTI_PORTLET_ASSIGNMENT, aTitle.trim());
		}
		return retval;
	}

	/**
	 * getLaunchJavaScript - Return JavaScript to finish the launch
	 */
	public static String getLaunchJavaScript(String submit_form_id, boolean autosubmit) {

		String doSubmit = "document.getElementById('"+submit_form_id+"').submit();\n";

		StringBuffer sb = new StringBuffer();
		sb.append("window.addEventListener('message', function (event) {\n");
		sb.append("  var message = event.data;\n");
		sb.append("  if ( typeof message == 'string' ) message = JSON.parse(message);\n  console.log(message);\n");
		if ( autosubmit ) {
			sb.append("  if ( message.subject == 'org.sakailms.lti.prelaunch.response' ) {\n");
			sb.append("    console.log('submitting based on org.sakailms.lti.prelaunch.response');\n    ");
			sb.append("    clearTimeout(plTimeOut);\n  "); // Cancel current timeout, POST is launched already
			sb.append(doSubmit);
			sb.append("  }\n");
		}
		sb.append("});\n\n");

		sb.append("parent.postMessage('{ \"subject\": \"org.sakailms.lti.prelaunch\" }', '*');\nconsole.log('Sending prelaunch request');\n\n");

		if ( autosubmit ) {
			sb.append("var plTimeOut = setTimeout(function() {\n  console.warn('Submitting after prelaunch timeout');\n  ");
			sb.append(doSubmit);
			sb.append("}, 2000);\n");
		}
		return sb.toString();
	}

	/**
	 * getLaunchCodeKey - Return the launch code key for a content item
	 */
	public static String getLaunchCodeKey(LtiContentBean content) {
		if (content == null) return SESSION_LAUNCH_CODE + "0";
		int id = (content.getId() != null) ? content.getId().intValue() : 0;
		return SESSION_LAUNCH_CODE + id;
	}

	public static String getLaunchCodeKey(Map<String, Object> content) {
		return getLaunchCodeKey(LtiContentBean.of(content));
	}

	/**
	 * getLaunchCode - Return the launch code for a content item
	 */
	public static String getLaunchCode(LtiContentBean content) {
		if (content == null) return LTI13Util.timeStampSign("0", null);
		String content_id = (content.getId() != null) ? content.getId().toString() : "0";
		String placement_secret = content.getPlacementsecret();
		return LTI13Util.timeStampSign(content_id, placement_secret);
	}

	public static String getLaunchCode(Map<String, Object> content) {
		return getLaunchCode(LtiContentBean.of(content));
	}

	/**
	 * checkLaunchCode - check to see if a launch code is properly signed and not expired. Bean overload.
	 */
	public static boolean checkLaunchCode(LtiContentBean content, String launch_code) {
		if (content == null) return false;
		String content_id = (content.getId() != null) ? content.getId().toString() : "0";
		if (!launch_code.contains(":" + content_id + ":")) return false;
		String placement_secret = content.getPlacementsecret();
		int delta = 5 * 60; // Five minutes
		return LTI13Util.timeStampCheckSign(launch_code, placement_secret, delta);
	}

	public static boolean checkLaunchCode(Map<String, Object> content, String launch_code) {
		return checkLaunchCode(LtiContentBean.of(content), launch_code);
	}

	/**
	 * sendPostVerify - Check if we are supposed to send the postVerify Claims
	 */
	public static boolean checkSendPostVerify()
	{
		String postVerify = ServerConfigurationService.getString(
		    LTI_ADVANTAGE_POST_VERIFY_ENABLED, LTI_ADVANTAGE_POST_VERIFY_ENABLED_DEFAULT);
		return "true".equals(postVerify);
	}

	/**
	 * addSakaiBaseCapabilities - Add a list of Sakai base capabilities to a URL under construction
	 *
	 * There may be additional capabilities available through an org.imsglobal.lti.capabilities message.
	 * These are the commonly available capabilities to all LTI placements, comma separated.
	 */

	public static void addSakaiBaseCapabilities(URIBuilder redirect) {
		redirect.addParameter("sakai_base_capabilities", "org.imsglobal.lti.capabilities,org.imsglobal.lti.put_data,org.imsglobal.lti.get_data");
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
	// LTI-273
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

	public static Map<String, Object> findBestToolMatch(boolean global, String launchUrl, String importCheckSum, List<Map<String,Object>> tools)
	{
		boolean local = ! global;  // Makes it easier to read :)

		// Next we look for a tool with a checksum match
		if ( StringUtils.isNotEmpty(importCheckSum) ) {
			for ( Map<String,Object> tool : tools ) {
				String toolCheckSum = computeToolCheckSum(tool);
				if ( StringUtils.isEmpty(toolCheckSum) ) continue;
				if ( toolCheckSum.equals(importCheckSum) ) {
					log.debug("Found tool {} with matching checksum {}", tool.get(LTIService.LTI_ID), toolCheckSum);
					return tool;
				}
			}
		}

		// Next we look for a tool with an exact match
		for ( Map<String,Object> tool : tools ) {
			String toolLaunch = (String) tool.get(LTIService.LTI_LAUNCH);
			String toolSite = (String) tool.get(LTIService.LTI_SITE_ID);

			if ( local && StringUtils.trimToNull(toolSite) == null ) continue;
			if ( global && StringUtils.trimToNull(toolSite) != null ) continue;

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

			if ( local && StringUtils.trimToNull(toolSite) == null ) continue;
			if ( global && StringUtils.trimToNull(toolSite) != null ) continue;

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

			if ( local && StringUtils.trimToNull(toolSite) == null ) continue;
			if ( global && StringUtils.trimToNull(toolSite) != null ) continue;

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

	public static Map<String, Object> findBestToolMatch(String launchUrl, String toolCheckSum, List<Map<String,Object>> tools)
	{
		// Example launch URL:
		// https://www.py4e.com/mod/gift/?quiz=02-Python.txt

		boolean global = true;
		Map<String,Object> retval = findBestToolMatch(!global, launchUrl, toolCheckSum, tools);

		if ( retval != null ) return retval;

		retval = findBestToolMatch(global, launchUrl, toolCheckSum, tools);
		return retval;
	}

	/**
	 * Bean overload for findBestToolMatch
	 */
	public static org.sakaiproject.lti.beans.LtiToolBean findBestToolMatchBean(String launchUrl, String toolCheckSum, List<org.sakaiproject.lti.beans.LtiToolBean> tools)
	{
		// Guard against null tools parameter
		if (tools == null) {
			return null;
		}
		
		// Convert Beans to maps for the existing logic
		List<Map<String,Object>> toolMaps = new ArrayList<>();
		for (org.sakaiproject.lti.beans.LtiToolBean tool : tools) {
			if (tool != null) {
				toolMaps.add(tool.asMap());
			}
		}

		Map<String,Object> result = findBestToolMatch(launchUrl, toolCheckSum, toolMaps);
		return result != null ? org.sakaiproject.lti.beans.LtiToolBean.of(result) : null;
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
	 * Converts a string from a comma-separated list of outbound role maps to a
	 * Map<String, String>. Each role mapping in the string should be of the
	 * form sakairole1:ltirole1,sakairole2:ltirole2
	 * or sakairole4:ltirole4,ltirole5;sakairole6:ltirole6
	 * Using semicolon as the delimiter allows you to indicate more than one LTI role.
	 *
	 * You might wonder why the inbound, outbound, and legacy maps have different
	 * formats.   The outbound maps were created in 2010 so we have to hang with the
	 * legacy stuff.   The inbound and legacy are modern and share conventions.
	 */
	public static Map<String, String> convertOutboundRoleMapPropToMap(String roleMapProp) {
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
	 * Converts a string from a list of inbound role maps to a
	 * Map<String, List<String>>. Each role mapping in the string should be of the form
	 * http://purl.imsglobal.org/vocab/lis/v2/membership#Administrator=Instructor,maintain
	 * The Sakai roles are checked in order from left to right until one matches
	 * for a particular incoming LTI role.
	 */
	public static Map<String, List<String>> convertInboundRoleMapPropToMap(String roleMapProp) {
		Map<String, List<String>> roleMap = new HashMap<>();
		if (roleMapProp == null) {
			return roleMap;
		}

		String delim = ";";
		String[] roleMapPairs = roleMapProp.split(delim);
		for (String s : roleMapPairs) {
			if ( s.trim().length() < 1 ) continue;
			String[] roleMapPair = s.split("=", 2);
			if (roleMapPair.length != 2) continue;
			String[] sakaiRoles = roleMapPair[1].trim().split(",");
			roleMap.put(roleMapPair[0].trim(), List.of(sakaiRoles));
		}
		return roleMap;
	}

	/**
	 * Deserialize a Map from a string with each entry ended by a
	 * semicolon and the key anf value separated by equals sign and return a
	 * Map<String, String>. Each role mapping should be of the form:
	 * key=value;
	 */
	public static Map<String, String> deserializeMap(String mapSer) {
		Map<String, String> retval = new HashMap<>();
		if (mapSer == null) {
			return retval;
		}

		String delim = ";";
		String[] pairs = mapSer.split(delim);
		for (String s : pairs) {
			if ( s.trim().length() < 1 ) continue;
			String[] pair = s.split("=", 2);
			if (pair.length != 2) continue;
			retval.put(pair[0].trim(), pair[1].trim());
		}
		return retval;
	}

	/**
	 * Converts a string from a semicolon-separated list of legacy lti roles mapped to a modern LTI role to a
	 * Map<String, String>. Each role mapping should be of the form:
	 * Learner=http://purl.imsglobal.org/vocab/lis/v2/membership#Learner
	 */
	public static Map<String, String> convertLegacyRoleMapPropToMap(String roleMapProp) {
		return deserializeMap(roleMapProp);
	}

	/**
	 *  Check if we are an LTI 1.1 launch or not. Bean overload.
	 */
	public static boolean isLTI11(LtiToolBean tool) {
		if (tool == null) return false;
		Integer lti13 = tool.getLti13();
		if (lti13 == null) return true;
		long v = lti13.longValue();
		if (v == LTIService.LTI13_LTI11) return true;
		if (v == LTIService.LTI13_LTI13) return false;
		if (v == LTIService.LTI13_BOTH) return true;
		return true;
	}

	/**
	 *  Check if we are an LTI 1.1 launch or not
	 */
	public static boolean isLTI11(Map<String, Object> tool) {
		return isLTI11(LtiToolBean.of(tool));
	}


	/**
	 *  Check if we are an LTI 1.3 launch or not. Bean overload.
	 */
	public static boolean isLTI13(LtiToolBean tool) {
		if (tool == null) return false;
		Integer lti13 = tool.getLti13();
		if (lti13 == null) return false;
		long v = lti13.longValue();
		if (v == LTIService.LTI13_LTI11) return false;
		if (v == LTIService.LTI13_LTI13) return true;
		if (v == LTIService.LTI13_BOTH) return true;
		return false;
	}

	/**
	 *  Check if we are an LTI 1.3 launch or not
	 */
	public static boolean isLTI13(Map<String, Object> tool) {
		return isLTI13(LtiToolBean.of(tool));
	}

	/**
	 * Get the secret for the tool. Content does not have secret; only tool is used.
	 */
	public static String getSecret(LtiToolBean tool) {
		return (tool != null) ? tool.getSecret() : null;
	}

	/**
	 * Get the consumer key for the tool. Content does not have consumerkey; only tool is used.
	 */
	public static String getKey(LtiToolBean tool) {
		return (tool != null) ? tool.getConsumerkey() : null;
	}

	/**
	 * Get the correct frameheight for a content / combination based on inheritance rules
	 */
	public static String getFrameHeight(LtiToolBean tool, LtiContentBean content, String defaultValue) {
		String height = defaultValue;

		// Check tool first (default behavior)
		if (tool != null && tool.getFrameheight() != null && tool.getFrameheight() > 0) {
			height = tool.getFrameheight() + "px";
		}

		// Check content second (content overrides tool if not null)
		if (content != null && content.getFrameheight() != null && content.getFrameheight() > 0) {
			height = content.getFrameheight() + "px";
		}

		return height;
	}

	public static String getFrameHeight(Map<String, Object> tool, Map<String, Object> content, String defaultValue) {
		return getFrameHeight(LtiToolBean.of(tool), LtiContentBean.of(content), defaultValue);
	}

	/**
	 * Get the new page setting for a content / combination based on inheritance rules
	 */
	public static boolean getNewpage(LtiToolBean tool, LtiContentBean content, boolean defaultValue) {
		boolean newpage = defaultValue;

		// Check content first (lower priority)
		if (content != null && content.getNewpage() != null) {
			newpage = content.getNewpage();
		}

		// Check tool second (higher priority - overrides content)
		if (tool != null && tool.getNewpage() != null) {
			if (tool.getNewpage() == LTIService.LTI_TOOL_NEWPAGE_OFF) newpage = false;
			if (tool.getNewpage() == LTIService.LTI_TOOL_NEWPAGE_ON) newpage = true;
		}
		return newpage;
	}

	public static boolean getNewpage(Map<String, Object> tool, Map<String, Object> content, boolean defaultValue) {
		return getNewpage(LtiToolBean.of(tool), LtiContentBean.of(content), defaultValue);
	}

	/**
	 * Get the debug setting for a content / tool combination based on inheritance rules
	 */
	public static boolean getDebug(LtiToolBean tool, LtiContentBean content, boolean defaultValue) {
		boolean debug = defaultValue;

		// Check content first (lower priority)
		if (content != null && content.getDebug() != null) {
			debug = content.getDebug();
		}

		// Check tool second (higher priority - overrides content)
		if (tool != null && tool.getDebug() != null) {
			if (tool.getDebug() == LTIService.LTI_TOOL_DEBUG_OFF) debug = false;
			if (tool.getDebug() == LTIService.LTI_TOOL_DEBUG_ON) debug = true;
		}
		return debug;
	}

	/** Map shim for backward compatibility; prefer bean overload. */
	private static boolean getDebug(Map<String, Object> tool, Map<String, Object> content, boolean defaultValue) {
		return getDebug(LtiToolBean.of(tool), LtiContentBean.of(content), defaultValue);
	}

	/**
	 * Get the title for a content / tool combination based on inheritance rules. Bean overload.
	 */
	public static String getToolTitle(LtiToolBean tool, LtiContentBean content, String defaultValue) {
		String title = defaultValue;

		if (tool != null && StringUtils.isNotEmpty(tool.getTitle())) {
			title = tool.getTitle();
		}

		if (content != null && StringUtils.isNotEmpty(content.getTitle())) {
			title = content.getTitle();
		}

		return title;
	}

	/**
	 * Get the title for a content / combination based on inheritance rules
	 */
	public static String getToolTitle(Map<String, Object> tool, Map<String, Object> content, String defaultValue) {
		return getToolTitle(LtiToolBean.of(tool), LtiContentBean.of(content), defaultValue);
	}

	public static Element archiveTool(Document doc, LtiToolBean tool) {
		return archiveTool(doc, tool != null ? tool.asMap() : null);
	}

	/**
	 * Archive an LTI tool from a bean without using asMap or Foorm.
	 * Produces the same element as {@link #archiveTool(Document, Map)}.
	 * This is a Foorm-free code path for future migration away from Foorm.
	 */
	public static Element archiveToolBean(Document doc, LtiToolBean tool) {
		if (tool == null) {
			return null;
		}
		Element retval = doc.createElement(LTIService.ARCHIVE_LTI_TOOL_TAG);
		java.util.Properties info = new java.util.Properties();
		for (String formInput : LTIService.TOOL_MODEL) {
			info = parseArchiveFieldInfo(formInput, info);
			String field = info.getProperty("field", null);
			String type = info.getProperty("type", null);
			if (!"true".equals(info.getProperty("archive", null))) {
				continue;
			}
			if (LTIService.SAKAI_TOOL_CHECKSUM.equals(field)) {
				continue; // added separately below
			}
			Object o = tool.getValueByFieldName(field);
			if (o == null) {
				continue;
			}
			String text = formatArchiveValue(o, type);
			if (text == null) {
				continue;
			}
			Element child = doc.createElement(field);
			child.setTextContent(text);
			retval.appendChild(child);
		}
		String checksum = computeToolCheckSum(tool);
		if (checksum != null) {
			Element checksumEl = doc.createElement(LTIService.SAKAI_TOOL_CHECKSUM);
			checksumEl.setTextContent(checksum);
			retval.appendChild(checksumEl);
		}
		return retval;
	}

	private static java.util.Properties parseArchiveFieldInfo(String formInput, java.util.Properties out) {
		out.clear();
		String[] pairs = formInput.split(":");
		String[] positional = { "field", "type" };
		int i = 0;
		for (String s : pairs) {
			String[] kv = s.split("=");
			if (kv.length == 2) {
				out.setProperty(kv[0], kv[1]);
			} else if (kv.length == 1 && i < positional.length) {
				out.setProperty(positional[i++], kv[0]);
			}
		}
		return out;
	}

	private static String formatArchiveValue(Object o, String type) {
		if (o == null) return null;
		if ("checkbox".equals(type) || "radio".equals(type) || "integer".equals(type) || "key".equals(type)) {
			if (o instanceof Boolean) {
				return Boolean.TRUE.equals(o) ? "1" : "0";
			}
		}
		if (o instanceof java.util.Date) {
			return String.valueOf(((java.util.Date) o).getTime());
		}
		return o.toString();
	}

	public static Element archiveTool(Document doc, Map<String, Object> tool) {
		Element retval = Foorm.archiveThing(doc, LTIService.ARCHIVE_LTI_TOOL_TAG, LTIService.TOOL_MODEL, tool);
		String checksum = computeToolCheckSum(tool);
		if (checksum != null) {
			Element newElement = doc.createElement(LTIService.SAKAI_TOOL_CHECKSUM);
			newElement.setTextContent(checksum);
			retval.appendChild(newElement);
		}
		return retval;
	}

	public static Element archiveContent(Document doc, LtiContentBean content, LtiToolBean tool) {
		return archiveContent(doc, content != null ? content.asMap() : null, tool != null ? tool.asMap() : null);
	}

	public static Element archiveContent(Document doc, Map<String, Object> content, Map<String, Object> tool) {
		// Check if the content launchURL is empty - if so, inherit from tool for the future
		Map<String, Object> contentCopy = new HashMap(content);
		String launchUrl = (String) contentCopy.get(LTIService.LTI_LAUNCH);
		if (tool != null && StringUtils.isEmpty(launchUrl)) contentCopy.put(LTIService.LTI_LAUNCH, tool.get(LTIService.LTI_LAUNCH));
		Element retval = Foorm.archiveThing(doc, LTIService.ARCHIVE_LTI_CONTENT_TAG, LTIService.CONTENT_MODEL, contentCopy);

		if (tool != null) {
			Element toolElement = archiveTool(doc, tool);
			retval.appendChild(toolElement);
		}
		return retval;
	}

	public static void mergeTool(Element element, LtiToolBean tool) {
		mergeTool(element, tool != null ? tool.asMap() : null);
	}

	public static void mergeTool(Element element, Map<String, Object> tool) {
		Foorm.mergeThing(element, LTIService.TOOL_MODEL, tool);
	}

	public static void mergeContent(Element element, LtiContentBean content, LtiToolBean tool) {
		mergeContent(element, content != null ? content.asMap() : null, tool != null ? tool.asMap() : null);
	}

	public static void mergeContent(Element element, Map<String, Object> content, Map<String, Object> tool) {
		Foorm.mergeThing(element, LTIService.CONTENT_MODEL, content);
		if (tool != null) {
			NodeList nl = element.getElementsByTagName(LTIService.ARCHIVE_LTI_TOOL_TAG);
			if (nl.getLength() >= 1) {
				Node toolNode = nl.item(0);
				if (toolNode.getNodeType() == Node.ELEMENT_NODE) {
					Element toolElement = (Element) toolNode;
					mergeTool(toolElement, tool);
				}
			}
		}
	}

	public static String computeToolCheckSum(LtiToolBean tool) {
		if (tool == null) return null;
		if (StringUtils.isEmpty(tool.launch)) return null;
		if (StringUtils.isNotEmpty(tool.consumerkey) && StringUtils.isNotEmpty(tool.secret)) {
			// LTI 1.1
		} else if (StringUtils.isNotEmpty(tool.lti13ClientId) && StringUtils.isNotEmpty(tool.lti13ToolKeyset)) {
			// LTI 1.3
		} else {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		sb.append(tool.secret != null ? tool.secret : "");
		sb.append(tool.consumerkey != null ? tool.consumerkey : "");
		sb.append(tool.lti13ClientId != null ? tool.lti13ClientId : "");
		sb.append(tool.lti13ToolKeyset != null ? tool.lti13ToolKeyset : "");
		sb.append(tool.launch != null ? tool.launch : "");
		return LTI13Util.sha256(sb.toString());
	}

	public static String computeToolCheckSum(Map<String, Object> tool) {
		return computeToolCheckSum(LtiToolBean.of(tool));
	}

	// /access/lti/site/22153323-3037-480f-b979-c630e3e2b3cf/content:1
	public static Long getContentKeyFromLaunch(String launch) {
		if ( launch == null ) return null;
		Pattern ltiPattern = null;
		try {
			ltiPattern = Pattern.compile(LTIService.LAUNCH_CONTENT_REGEX);
		}
		catch (Exception e) {
			return -1L;
		}
		Matcher ltiMatcher = ltiPattern.matcher(launch);
		if (ltiMatcher.find()) {
			String number = ltiMatcher.group(1);
			Long contentKey = NumberUtils.toLong(number, -1);
			return contentKey;
		}
		return -1L;
	}

	public static String getContentLaunch(LtiContentBean content) {
		if (content == null) return null;
		Long id = content.getId();
		long key = (id != null) ? id.longValue() : -1L;
		String siteId = content.getSiteId();
		if (key < 0 || siteId == null) return null;
		return LTIService.LAUNCH_PREFIX + siteId + "/content:" + key;
	}

	public static String getContentLaunch(Map<String, Object> content) {
		return getContentLaunch(LtiContentBean.of(content));
	}

	public static String getToolLaunch(LtiToolBean tool, String siteId) {
		if (tool == null || siteId == null) return null;
		Long id = tool.getId();
		long key = (id != null) ? id.longValue() : -1L;
		if (key < 0) return null;
		return LTIService.LAUNCH_PREFIX + siteId + "/tool:" + key;
	}

	public static String getToolLaunch(Map<String, Object> tool, String siteId) {
		return getToolLaunch(LtiToolBean.of(tool), siteId);
	}

	public static String getExportUrl(String siteId, String filterId, ExportType exportType) {
		if (siteId == null) {
			return null;
		}
		return LTIService.LAUNCH_PREFIX + siteId + "/export:" + exportType + ((filterId != null && !"".equals(filterId)) ? (":" + filterId) : "");
	}

	/*
	 * Parse a rich edtext editor string of the format
	 *
	 * <p>Yada</p>
	 * <p><a class="lti-launch"
	 * href="http://localhost:8080/access/lti/site/7d529bf7-b856-4400-9da1-ba8670ed1489/content:1"
	 * rel="noopener" target="_blank">Breakout</a></p>
	 *
	 * Extract the lti-launch urls and return them as a list of strings	
	 *
	 */
	public static List<String> extractLtiLaunchUrls(String html) {
		List<String> retval = new ArrayList<>();
		if (html == null) {
			return retval;
		}
		Pattern pattern = Pattern.compile("https?://[^\\s\"']+/access/(b)?lti/site/[^\\s\"']+/content:\\d+");
		Matcher matcher = pattern.matcher(html);
		while (matcher.find()) {
			retval.add(matcher.group());
		}
		return retval;
	}

	/**
	 * Extract the site id and content key from a LTI Launch URL
	 *
	 * @param url
	 * @return an array of two strings, the first is the site id, the second is the content key
	 */
	public static String[] getContentKeyAndSiteId(String url) {
		if (url == null) {
			return null;
		}
		Pattern pattern = Pattern.compile("https?://[^\\s\"']+/access/(?:b)?lti/site/([^\\s\"'/]+)/content:(\\d+)");
		Matcher matcher = pattern.matcher(url);
		if (matcher.find()) {
			return new String[] { matcher.group(1), matcher.group(2) };
		}
		return null;
	}

}
