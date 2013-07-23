/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.basiclti.util;

import java.util.Properties;
import java.util.Map;
import java.net.URL;

import org.imsglobal.basiclti.BasicLTIUtil;
import org.imsglobal.basiclti.BasicLTIConstants;
import org.sakaiproject.linktool.LinkToolUtil;

import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.api.privacy.PrivacyManager;
import org.sakaiproject.authz.cover.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Web;

/**
 * Some Sakai Utility code for IMS Basic LTI
 * This is mostly code to support the Sakai conventions for 
 * making and launching BLTI resources within Sakai.
 */
@SuppressWarnings("deprecation")
public class SakaiBLTIUtil {

	public static final boolean verbosePrint = false;

	public static final String BASICLTI_OUTCOMES_ENABLED = "basiclti.outcomes.enabled";
	public static final String BASICLTI_SETTINGS_ENABLED = "basiclti.settings.enabled";
	public static final String BASICLTI_ROSTER_ENABLED = "basiclti.roster.enabled";
	public static final String BASICLTI_LORI_ENABLED = "basiclti.lori.enabled";
	public static final String BASICLTI_CONTENTLINK_ENABLED = "basiclti.contentlink.enabled";
	public static final String BASICLTI_CONSUMER_USERIMAGE_ENABLED = "basiclti.consumer.userimage.enabled";
	public static final String BASICLTI_ENCRYPTION_KEY = "basiclti.encryption.key";

	public static void dPrint(String str)
	{
		if ( verbosePrint ) System.out.println(str);
	}

	// Retrieve the property from the configuration unless it
	// is overridden by the server configurtation (i.e. sakai.properties)
	public static String getCorrectProperty(Properties config,
			String propName, Placement placement)
	{
		// Check for global overrides in properties
		String allowSettings = ServerConfigurationService.getString(BASICLTI_SETTINGS_ENABLED, null);
		if ( "allowsettings".equals(propName) && ! "true".equals(allowSettings) ) return "false";

		String allowRoster = ServerConfigurationService.getString(BASICLTI_ROSTER_ENABLED, null);
		if ( "allowroster".equals(propName) && ! "true".equals(allowRoster) ) return "false";

		String allowLori = ServerConfigurationService.getString(BASICLTI_LORI_ENABLED, null);
		if ( "allowlori".equals(propName) && ! "true".equals(allowLori) ) return "false";

		String allowContentLink = ServerConfigurationService.getString(BASICLTI_CONTENTLINK_ENABLED, null);
		if ( "contentlink".equals(propName) && ! "true".equals(allowContentLink) ) return null;

		// Check for explicit setting in properties
		String propertyName = placement.getToolId() + "." + propName;
		String propValue = ServerConfigurationService.getString(propertyName,null);
		if ( propValue != null && propValue.trim().length() > 0 ) {
			// System.out.println("Sakai.home "+propName+"="+propValue);
			return propValue;
		}

		// Take it from the placement
		return config.getProperty("imsti."+propName, null);
	}

	// Look at a Placement and come up with the launch urls, and
	// other launch parameters to drive the launch.
	public static boolean loadFromPlacement(Properties info, Properties launch, Placement placement)
	{
		Properties config = placement.getConfig();
		dPrint("Sakai properties=" + config);
		String launch_url = toNull(getCorrectProperty(config,"launch", placement));
		setProperty(info, "launch_url", launch_url);
		if ( launch_url == null ) {
			String xml = toNull(getCorrectProperty(config,"xml", placement));
			if ( xml == null ) return false;
			BasicLTIUtil.parseDescriptor(info, launch, xml);
		}

		String secret = getCorrectProperty(config,"secret", placement);

		// BLTI-195 - Compatibility mode for old-style encrypted secrets
		if ( secret == null || secret.trim().length() < 1 ) {
			String eSecret = getCorrectProperty(config,"encryptedsecret", placement);
			if ( eSecret != null && eSecret.trim().length() > 0 ) {
				secret = eSecret.trim() + ":" + SimpleEncryption.CIPHER;
			}
		}

		setProperty(info, "secret", secret );

		setProperty(info, "key", getCorrectProperty(config,"key", placement) );
		setProperty(info, "debug", getCorrectProperty(config,"debug", placement) );
		setProperty(info, "frameheight", getCorrectProperty(config,"frameheight", placement) );
		setProperty(info, "newpage", getCorrectProperty(config,"newpage", placement) );
		setProperty(info, "title", getCorrectProperty(config,"tooltitle", placement) );

		// Pull in and parse the custom parameters
		String customstr = toNull(getCorrectProperty(config,"custom", placement) );
		parseCustom(info, customstr);

		if ( info.getProperty("launch_url", null) != null || 
				info.getProperty("secure_launch_url", null) != null ) {
			return true;
		}
		return false;
	}

	public static void parseCustom(Properties info, String customstr)
	{
		if ( customstr != null ) {
			String [] params = customstr.split("[\n;]");
			for (int i = 0 ; i < params.length; i++ ) {
				String param = params[i];
				if ( param == null ) continue;
				if ( param.length() < 1 ) continue;
				int pos = param.indexOf("=");
				if ( pos < 1 ) continue;
				if ( pos+1 > param.length() ) continue;
				String key = BasicLTIUtil.mapKeyName(param.substring(0,pos));
				if ( key == null ) continue;
				String value = param.substring(pos+1);
                if ( value == null ) continue;
				value = value.trim();
				if ( value.length() < 1 ) continue;
				setProperty(info, "custom_"+key, value);
			}
		}
	}

	public static String encryptSecret(String orig)
	{
		if ( orig == null || orig.trim().length() < 1 ) return orig;
		String encryptionKey = ServerConfigurationService.getString(BASICLTI_ENCRYPTION_KEY, null);
		if ( encryptionKey == null ) return orig;
	
		// May throw runtime exception - just let it log as this is abnormal...
		String newsecret = SimpleEncryption.encrypt(encryptionKey, orig);
		return newsecret;
	}

	public static String decryptSecret(String orig)
	{
		if ( orig == null || orig.trim().length() < 1 ) return orig;
		String encryptionKey = ServerConfigurationService.getString(BASICLTI_ENCRYPTION_KEY, null);
		if ( encryptionKey == null ) return orig;
		try {
			String newsecret = SimpleEncryption.decrypt(encryptionKey, orig);
			return newsecret;
		} catch (RuntimeException re) {
			dPrint("Exception when decrypting secret - this is normal if the secret is unencrypted");      
			return orig;
		}
	}

	public static boolean sakaiInfo(Properties props, Placement placement, ResourceLoader rb)
	{
		dPrint("placement="+ placement.getId());
		dPrint("placement title=" + placement.getTitle());
		String context = placement.getContext();
		dPrint("ContextID="+context);

		return sakaiInfo(props, context, placement.getId(), rb);
	}

	public static void addSiteInfo(Properties props, Site site)
	{
		if ( site != null ) {
			String context_type = site.getType();
			if ( context_type != null && context_type.toLowerCase().contains("course") ){
				setProperty(props,BasicLTIConstants.CONTEXT_TYPE,BasicLTIConstants.CONTEXT_TYPE_COURSE_SECTION);
			}
			setProperty(props,BasicLTIConstants.CONTEXT_ID,site.getId());
			setProperty(props,BasicLTIConstants.CONTEXT_LABEL,site.getTitle());
			setProperty(props,BasicLTIConstants.CONTEXT_TITLE,site.getTitle());
			String courseRoster = getExternalRealmId(site.getId());
			if ( courseRoster != null ) 
			{
				setProperty(props,BasicLTIConstants.LIS_COURSE_OFFERING_SOURCEDID,courseRoster);
			}
		}

		// Fix up the return Url
		String returnUrl =	ServerConfigurationService.getString("basiclti.consumer_return_url",null);
		if ( returnUrl == null ) {
			returnUrl = getOurServerUrl() + "/imsblis/service/return-url";  
			Session s = SessionManager.getCurrentSession();
			if (s != null) {
				String controllingPortal = (String) s.getAttribute("sakai-controlling-portal");
				if ( controllingPortal == null ) {
					returnUrl = returnUrl + "/site";
				} else {	
					returnUrl = returnUrl + "/" + controllingPortal;
				}
			}
			returnUrl = returnUrl + "/" + site.getId();
		}

		setProperty(props, BasicLTIConstants.LAUNCH_PRESENTATION_RETURN_URL, returnUrl);
	}

	public static void addRoleInfo(Properties props, String context)
	{
		String theRole = "Learner";
		if ( SecurityService.isSuperUser() )
		{
			theRole = "Instructor,Administrator,urn:lti:instrole:ims/lis/Administrator,urn:lti:sysrole:ims/lis/Administrator";
		}
		else if ( SiteService.allowUpdateSite(context) ) 
		{
			theRole = "Instructor";
		}
		setProperty(props,BasicLTIConstants.ROLES,theRole);

		String realmId = SiteService.siteReference(context);
		try {
			User user = UserDirectoryService.getCurrentUser();
			if ( user != null ) {
				Role role = null;
				String roleId = null;
				AuthzGroup realm = AuthzGroupService.getAuthzGroup(realmId);
				if ( realm != null ) role = realm.getUserRole(user.getId());
				if ( role != null ) roleId = role.getId();
				if ( roleId != null && roleId.length() > 0 ) setProperty(props, "ext_sakai_role", roleId);
			}
		} catch (GroupNotDefinedException e) {
			dPrint("SiteParticipantHelper.getExternalRealmId: site realm not found"+e.getMessage());
		}
	}

	// Retrieve the Sakai information about users, etc.
	public static boolean sakaiInfo(Properties props, String context, String placementId, ResourceLoader rb)
	{

		Site site = null;
		try {
			site = SiteService.getSite(context);
		} catch (Exception e) {
			dPrint("No site/page associated with Launch context="+context);
			return false;
		}

		// Add the generic information
		addGlobalData(props, rb);
		addRoleInfo(props, context);
		addSiteInfo(props, site);

		// Add Placement Information
		addPlacementInfo(props, placementId);
		return true;
	}

	public static void addPlacementInfo(Properties props, String placementId)
	{

		// Get the placement to see if we are to release information
		ToolConfiguration placement = SiteService.findTool(placementId);
		Properties config = placement.getConfig();

		// Start setting the Basici LTI parameters
		setProperty(props,BasicLTIConstants.RESOURCE_LINK_ID,placementId);
		String pagetitle = toNull(getCorrectProperty(config,"pagetitle", placement));
		if ( pagetitle != null ) setProperty(props,BasicLTIConstants.RESOURCE_LINK_TITLE,pagetitle);
		String tooltitle = toNull(getCorrectProperty(config,"tooltitle", placement));
		if ( tooltitle != null ) setProperty(props,BasicLTIConstants.RESOURCE_LINK_DESCRIPTION,tooltitle);

		String releasename = toNull(getCorrectProperty(config,"releasename", placement));
		String releaseemail = toNull(getCorrectProperty(config,"releaseemail", placement));

		User user = UserDirectoryService.getCurrentUser();

        PrivacyManager pm = (PrivacyManager) 
                ComponentManager.get("org.sakaiproject.api.privacy.PrivacyManager");

		// TODO: Think about anonymus
		if ( user != null )
		{
		    String context = placement.getContext();
            boolean isViewable = pm.isViewable("/site/" + context, user.getId());
            setProperty(props,"ext_sakai_privacy", isViewable ? "visible" : "hidden");

			setProperty(props,BasicLTIConstants.USER_ID,user.getId());

			if(ServerConfigurationService.getBoolean(SakaiBLTIUtil.BASICLTI_CONSUMER_USERIMAGE_ENABLED, true)) {
                String imageUrl = getOurServerUrl() + "/direct/profile/" + user.getId() + "/image";                     
                setProperty(props,BasicLTIConstants.USER_IMAGE,imageUrl);
            }

			if ( "on".equals(releasename) ) {
				setProperty(props,BasicLTIConstants.LIS_PERSON_NAME_GIVEN,user.getFirstName());
				setProperty(props,BasicLTIConstants.LIS_PERSON_NAME_FAMILY,user.getLastName());
				setProperty(props,BasicLTIConstants.LIS_PERSON_NAME_FULL,user.getDisplayName());
			}
			if ( "on".equals(releaseemail) ) {
				setProperty(props,BasicLTIConstants.LIS_PERSON_CONTACT_EMAIL_PRIMARY,user.getEmail());
				setProperty(props,BasicLTIConstants.LIS_PERSON_SOURCEDID,user.getEid());
				setProperty(props,"ext_sakai_eid",user.getEid());
			}

			String assignment = null;
			// It is a little tricky - the tool configuration on/off decides whether
			// We check the serverCongigurationService true/false
			// We use the tool configuration to force outcomes off regardless of
			// server settings (i.e. an external tool never wants the outcomes
			// UI shown because it simply does not handle outcomes).
			String allowOutcomes = toNull(getCorrectProperty(config,"allowoutcomes", placement));
			if ( ! "off".equals(allowOutcomes) ) {
				assignment = toNull(getCorrectProperty(config,"assignment", placement));
				allowOutcomes = ServerConfigurationService.getString(
						SakaiBLTIUtil.BASICLTI_OUTCOMES_ENABLED, null);
				if ( ! "true".equals(allowOutcomes) ) allowOutcomes = null;
			}

			String allowSettings = toNull(getCorrectProperty(config,"allowsettings", placement));
			if ( ! "on".equals(allowSettings) ) allowSettings = null;

			String allowRoster = toNull(getCorrectProperty(config,"allowroster", placement));
			if ( ! "on".equals(allowRoster) ) allowRoster = null;

			String allowLori = toNull(getCorrectProperty(config,"allowlori", placement));
			if ( ! "on".equals(allowLori) ) allowLori = null;

			String result_sourcedid = getSourceDID(user, placement, config);
			if ( result_sourcedid != null ) {

				if ( "true".equals(allowOutcomes) && assignment != null ) {
					setProperty(props,"lis_result_sourcedid", result_sourcedid);  

					// New Basic Outcomes URL
					String outcome_url = ServerConfigurationService.getString("basiclti.consumer.ext_ims_lis_basic_outcome_url",null);
					if ( outcome_url == null ) outcome_url = getOurServerUrl() + "/imsblis/service/";  
					setProperty(props,"ext_ims_lis_basic_outcome_url", outcome_url);  
					outcome_url = ServerConfigurationService.getString("basiclti.consumer."+BasicLTIConstants.LIS_OUTCOME_SERVICE_URL,null);
					if ( outcome_url == null ) outcome_url = getOurServerUrl() + "/imsblis/service/";  
					setProperty(props,BasicLTIConstants.LIS_OUTCOME_SERVICE_URL, outcome_url);  
				}

				if ( "on".equals(allowSettings) ) {
					setProperty(props,"ext_ims_lti_tool_setting_id", result_sourcedid);  

					String setting = config.getProperty("toolsetting", null);
					if ( setting != null ) {
						setProperty(props,"ext_ims_lti_tool_setting", setting);  
					}
					String service_url = ServerConfigurationService.getString("basiclti.consumer.ext_ims_lti_tool_setting_url",null);
					if ( service_url == null ) service_url = getOurServerUrl() + "/imsblis/service/";  
					setProperty(props,"ext_ims_lti_tool_setting_url", service_url);  
				}

				if ( "on".equals(allowRoster) ) {
					setProperty(props,"ext_ims_lis_memberships_id", result_sourcedid);  

					String roster_url = ServerConfigurationService.getString("basiclti.consumer.ext_ims_lis_memberships_url",null);
					if ( roster_url == null ) roster_url = getOurServerUrl() + "/imsblis/service/";  
					setProperty(props,"ext_ims_lis_memberships_url", roster_url);  
				}

				if ( "on".equals(allowLori) ) {
					setProperty(props,"ext_lori_api_token", result_sourcedid);  
					setProperty(props,"lis_result_sourcedid", result_sourcedid);  
					String lori_url = ServerConfigurationService.getString("basiclti.consumer.ext_lori_api_url",null);
					if ( lori_url == null ) lori_url = getOurServerUrl() + "/imsblis/service/";  
					String lori_url_xml = ServerConfigurationService.getString("basiclti.consumer.ext_lori_api_url_xml",null);
					if ( lori_url_xml == null ) lori_url_xml = getOurServerUrl() + "/imsblis/service/";  
					setProperty(props,"ext_lori_api_url", lori_url);  
					setProperty(props,"ext_lori_api_url_xml", lori_url_xml);  
				}
			}
		}

		// Send along the content link
		String contentlink = toNull(getCorrectProperty(config,"contentlink", placement));
		if ( contentlink != null ) setProperty(props,"ext_resource_link_content",contentlink);

		// Send along the signed session if requested
		String sendsession = toNull(getCorrectProperty(config,"ext_sakai_session", placement));
		if ( "true".equals(sendsession) ) {
			Session s = SessionManager.getCurrentSession();
			if (s != null) {
				String sessionid = s.getId();
				if (sessionid != null) {
					sessionid = LinkToolUtil.encrypt(sessionid);
					setProperty(props,"ext_sakai_session",sessionid);
				}
			}
		}
	} 

	public static void addGlobalData(Properties props, ResourceLoader rb)
	{

		if ( rb != null ) setProperty(props,BasicLTIConstants.LAUNCH_PRESENTATION_LOCALE,rb.getLocale().toString()); 

		// Get the organizational information
		setProperty(props,BasicLTIConstants.TOOL_CONSUMER_INSTANCE_GUID, 
				ServerConfigurationService.getString("basiclti.consumer_instance_guid",null));
		setProperty(props,BasicLTIConstants.TOOL_CONSUMER_INSTANCE_NAME, 
				ServerConfigurationService.getString("basiclti.consumer_instance_name",null));
		setProperty(props,BasicLTIConstants.TOOL_CONSUMER_INSTANCE_DESCRIPTION, 
				ServerConfigurationService.getString("basiclti.consumer_instance_description",null));
		setProperty(props,BasicLTIConstants.TOOL_CONSUMER_INSTANCE_CONTACT_EMAIL, 
				ServerConfigurationService.getString("basiclti.consumer_instance_contact_email",null));
		setProperty(props,BasicLTIConstants.TOOL_CONSUMER_INSTANCE_URL, 
				ServerConfigurationService.getString("basiclti.consumer_instance_url",null));

		// Send along the CSS URL
		String tool_css = ServerConfigurationService.getString("basiclti.consumer.launch_presentation_css_url",null);
		if ( tool_css == null ) tool_css = getOurServerUrl() + "/library/skin/default/tool.css";  
		setProperty(props,BasicLTIConstants.LAUNCH_PRESENTATION_CSS_URL, tool_css);  

		// Let tools know we are coming from Sakai
		String sakaiVersion = ServerConfigurationService.getString("version.sakai","2");
		setProperty(props,"ext_lms", "sakai-"+sakaiVersion);  
		setProperty(props,BasicLTIConstants.TOOL_CONSUMER_INFO_PRODUCT_FAMILY_CODE, 
			"sakai");  
		setProperty(props,BasicLTIConstants.TOOL_CONSUMER_INFO_VERSION, sakaiVersion);  

		// We pass this along in the Sakai world - it might
		// might be useful to the external tool
		String serverId = ServerConfigurationService.getServerId();
		setProperty(props,"ext_sakai_serverid",serverId);
		setProperty(props,"ext_sakai_server",getOurServerUrl());

	}
	// getProperty(String name);
	// Gnerate HTML from a descriptor and properties from 
	public static String[] postLaunchHTML(String descriptor, String contextId, String resourceId, ResourceProperties props, ResourceLoader rb)
	{
		if ( descriptor == null || contextId == null || resourceId == null ) 
			return postError("<p>" + getRB(rb, "error.descriptor" ,"Error, missing contextId, resourceid or descriptor")+"</p>" );

		// Add user, course, etc to the launch parameters
		Properties launch = new Properties();
		if ( ! sakaiInfo(launch, contextId, resourceId, rb) ) {
			return postError("<p>" + getRB(rb, "error.info.resource",
						"Error, cannot load Sakai information for resource=")+resourceId+".</p>");
		}

		Properties info = new Properties();
		if ( ! BasicLTIUtil.parseDescriptor(info, launch, descriptor) ) {
			return postError("<p>" + getRB(rb, "error.badxml.resource",
						"Error, cannot parse descriptor for resource=")+resourceId+".</p>");
		}

		return postLaunchHTML(info, launch, rb);
	}

	// This must return an HTML message as the [0] in the array
	// If things are successful - the launch URL is in [1]
	public static String[] postLaunchHTML(Map<String, Object> content, Map<String,Object> tool, ResourceLoader rb)
	{
		if ( content == null ) {
			return postError("<p>" + getRB(rb, "error.content.missing" ,"Content item is missing or improperly configured.")+"</p>" ); 
		}
		if ( tool == null ) {
			return postError("<p>" + getRB(rb, "error.tool.missing" ,"Tool item is missing or improperly configured.")+"</p>" ); 
		}

		int status = getInt(tool.get("status"));
		if ( status == 1 ) return postError("<p>" + getRB(rb, "tool.disabled" ,"Tool is currently disabled")+"</p>" ); 

		// Go with the content url first
		String launch_url = (String) content.get("launch");
		if ( launch_url == null ) launch_url = (String) tool.get("launch");
		if ( launch_url == null ) return postError("<p>" + getRB(rb, "error.nolaunch" ,"This tool is not yet configured.")+"</p>" );

		String context = (String) content.get("SITE_ID");
		Site site = null;
		try {
			site = SiteService.getSite(context);
		} catch (Exception e) {
			dPrint("No site/page associated with Launch context="+context);
			return postError("<p>" + getRB(rb, "error.site.missing" ,"Cannot load site.")+context+"</p>" ); 
		}

		// Start building up the properties
		Properties ltiProps = new Properties();
		Properties toolProps = new Properties();
		addGlobalData(ltiProps, rb);
		addSiteInfo(ltiProps, site);
		addRoleInfo(ltiProps, context);

		String resource_link_id = "content:"+content.get("id");
		setProperty(ltiProps,BasicLTIConstants.RESOURCE_LINK_ID,resource_link_id);

		setProperty(toolProps, "launch_url", launch_url);

		String secret = (String) content.get("secret");
		if ( secret == null ) secret = (String) tool.get("secret");
		String key = (String) content.get("consumerkey");
		if ( key == null ) key = (String) tool.get("consumerkey");

		if ( "-----".equals(key) && "-----".equals(secret) ) {
			return postError("<p>" + getRB(rb, "error.tool.partial" ,"Tool item is incomplete, missing a key and secret.")+"</p>" ); 
		}

		setProperty(toolProps, "secret", secret );
		setProperty(toolProps, "key", key );

		int debug = getInt(tool.get("debug"));
		if ( debug == 2 ) debug = getInt(content.get("debug"));
		setProperty(toolProps, "debug", debug+"");

		int frameheight = getInt(tool.get("frameheight"));
		if ( frameheight == 2 ) frameheight = getInt(content.get("frameheight"));
		setProperty(toolProps, "frameheight", frameheight+"" );

		int newpage = getInt(tool.get("newpage"));
		if ( newpage == 2 ) newpage = getInt(content.get("newpage"));
		setProperty(toolProps, "newpage", newpage+"" );

		String title = (String) content.get("title");
		if ( title == null ) title = (String) tool.get("title");
		if ( title != null ) setProperty(ltiProps,BasicLTIConstants.RESOURCE_LINK_TITLE,title);

		// Pull in and parse the custom parameters
		int allowCustom = getInt(tool.get("allowcustom"));
		if ( allowCustom == 1 ) parseCustom(ltiProps, (String) content.get("custom"));

		// Tool custom parameters override content parameters
		parseCustom(ltiProps, (String) tool.get("custom"));

		int releasename = getInt(tool.get("sendname"));
		int releaseemail = getInt(tool.get("sendemailaddr"));

		User user = UserDirectoryService.getCurrentUser();
		if ( user != null )
		{
			setProperty(ltiProps,BasicLTIConstants.USER_ID,user.getId());
			if ( releasename == 1 ) {
				setProperty(ltiProps,BasicLTIConstants.LIS_PERSON_NAME_GIVEN,user.getFirstName());
				setProperty(ltiProps,BasicLTIConstants.LIS_PERSON_NAME_FAMILY,user.getLastName());
				setProperty(ltiProps,BasicLTIConstants.LIS_PERSON_NAME_FULL,user.getDisplayName());
			}
			if ( releaseemail == 1 ) {
				setProperty(ltiProps,BasicLTIConstants.LIS_PERSON_CONTACT_EMAIL_PRIMARY,user.getEmail());
				setProperty(ltiProps,BasicLTIConstants.LIS_PERSON_SOURCEDID,user.getEid());
				// Only send the display ID if it's different to the EID.
				if (!user.getEid().equals(user.getDisplayId())) {
					setProperty(ltiProps,BasicLTIConstants.EXT_SAKAI_PROVIDER_DISPLAYID,user.getDisplayId());
				}
			}
		}

		int allowoutcomes = getInt(tool.get("allowoutcomes"));
		int allowroster = getInt(tool.get("allowroster"));
		int allowsettings = getInt(tool.get("allowsettings"));
		int allowlori = getInt(tool.get("allowlori"));
		String placement_secret = (String) content.get("placementsecret");

		String result_sourcedid = getSourceDID(user, resource_link_id, placement_secret);
		if ( result_sourcedid != null ) {

			if ( allowoutcomes == 1 ) {
				setProperty(ltiProps,"lis_result_sourcedid", result_sourcedid);  

				// New Basic Outcomes URL
				String outcome_url = ServerConfigurationService.getString("basiclti.consumer.ext_ims_lis_basic_outcome_url",null);
				if ( outcome_url == null ) outcome_url = getOurServerUrl() + "/imsblis/service/";  
				setProperty(ltiProps,"ext_ims_lis_basic_outcome_url", outcome_url);  
				outcome_url = ServerConfigurationService.getString("basiclti.consumer."+BasicLTIConstants.LIS_OUTCOME_SERVICE_URL,null);
				if ( outcome_url == null ) outcome_url = getOurServerUrl() + "/imsblis/service/";  
				setProperty(ltiProps,BasicLTIConstants.LIS_OUTCOME_SERVICE_URL, outcome_url);  
			}

			if ( allowsettings == 1 ) {
				setProperty(ltiProps,"ext_ims_lti_tool_setting_id", result_sourcedid);  

				String setting = (String) content.get("settings");
				if ( setting != null ) {
					setProperty(ltiProps,"ext_ims_lti_tool_setting", setting);  
				}
				String service_url = ServerConfigurationService.getString("basiclti.consumer.ext_ims_lti_tool_setting_url",null);
				if ( service_url == null ) service_url = getOurServerUrl() + "/imsblis/service/";  
				setProperty(ltiProps,"ext_ims_lti_tool_setting_url", service_url);  
			}

			if ( allowroster == 1 ) {
				setProperty(ltiProps,"ext_ims_lis_memberships_id", result_sourcedid);  

				String roster_url = ServerConfigurationService.getString("basiclti.consumer.ext_ims_lis_memberships_url",null);
				if ( roster_url == null ) roster_url = getOurServerUrl() + "/imsblis/service/";  
				setProperty(ltiProps,"ext_ims_lis_memberships_url", roster_url);  
			}

			if ( allowlori == 1 ) {
				setProperty(ltiProps,"ext_lori_api_token", result_sourcedid);  
				setProperty(ltiProps,"lis_result_sourcedid", result_sourcedid);  
				String lori_url = ServerConfigurationService.getString("basiclti.consumer.ext_lori_api_url",null);
				if ( lori_url == null ) lori_url = getOurServerUrl() + "/imsblis/service/";  
				String lori_url_xml = ServerConfigurationService.getString("basiclti.consumer.ext_lori_api_url_xml",null);
				if ( lori_url_xml == null ) lori_url_xml = getOurServerUrl() + "/imsblis/service/";  
				setProperty(ltiProps,"ext_lori_api_url", lori_url);  
				setProperty(ltiProps,"ext_lori_api_url_xml", lori_url_xml);  
			}
		}

		// System.out.println("ltiProps="+ltiProps);
		// System.out.println("toolProps="+toolProps);

		return postLaunchHTML(toolProps, ltiProps, rb);
	}

	// This must return an HTML message as the [0] in the array
	// If things are successful - the launch URL is in [1]
	public static String[] postLaunchHTML(String placementId, ResourceLoader rb)
	{
		if ( placementId == null ) return postError("<p>" + getRB(rb, "error.missing" ,"Error, missing placementId")+"</p>" );
		ToolConfiguration placement = SiteService.findTool(placementId);
		if ( placement == null ) return postError("<p>" + getRB(rb, "error.load" ,"Error, cannot load placement=")+placementId+".</p>");

		// Add user, course, etc to the launch parameters
		Properties ltiProps = new Properties();
		if ( ! sakaiInfo(ltiProps, placement, rb) ) {
			return postError("<p>" + getRB(rb, "error.missing",
						"Error, cannot load Sakai information for placement=")+placementId+".</p>");
		}

		// Retrieve the launch detail
		Properties toolProps = new Properties();
		if ( ! loadFromPlacement(toolProps, ltiProps, placement) ) {
			return postError("<p>" + getRB(rb, "error.nolaunch" ,"Not Configured.")+"</p>");
		}
		return postLaunchHTML(toolProps, ltiProps, rb);
	}

	public static String[] postLaunchHTML(Properties toolProps, Properties ltiProps, ResourceLoader rb)
	{

		String launch_url = toolProps.getProperty("secure_launch_url");
		if ( launch_url == null ) launch_url = toolProps.getProperty("launch_url");
		if ( launch_url == null ) return postError("<p>" + getRB(rb, "error.missing" ,"Not configured")+"</p>");

		String org_guid = ServerConfigurationService.getString("basiclti.consumer_instance_guid",null);
		String org_desc = ServerConfigurationService.getString("basiclti.consumer_instance_description",null);
		String org_url = ServerConfigurationService.getString("basiclti.consumer_instance_url",null);

		// Look up the LMS-wide secret and key - default key is guid
		String key = getToolConsumerInfo(launch_url,"key");
		if ( key == null ) key = org_guid;
		String secret = getToolConsumerInfo(launch_url,"secret");

		// Demand key/secret in a pair
		if ( key == null || secret == null ) {
			key = null;
			secret = null;
		}

		// If we do not have LMS-wide info, use the local key/secret
		if ( secret == null ) {
			secret = toNull(toolProps.getProperty("secret"));
			key = toNull(toolProps.getProperty("key"));
		}

		// If secret is encrypted, decrypt it
		secret = decryptSecret(secret);

		// Pull in all of the custom parameters
		for(Object okey : toolProps.keySet() ) {
			String skey = (String) okey;  
			if ( ! skey.startsWith(BasicLTIConstants.CUSTOM_PREFIX) ) continue;
			String value = toolProps.getProperty(skey);
			if ( value == null ) continue;
			setProperty(ltiProps, skey, value);
		}

		String oauth_callback = ServerConfigurationService.getString("basiclti.oauth_callback",null);
		// Too bad there is not a better default callback url for OAuth
		// Actually since we are using signing-only, there is really not much point 
		// In OAuth 6.2.3, this is after the user is authorized
		if ( oauth_callback == null ) oauth_callback = "about:blank";
		setProperty(ltiProps, "oauth_callback", oauth_callback);
		setProperty(ltiProps, BasicLTIUtil.BASICLTI_SUBMIT, getRB(rb, "launch.button", "Press to Launch External Tool"));

		// Sanity checks
		if ( secret == null ) {
			return postError("<p>" + getRB(rb, "error.nosecret", "Error - must have a secret.")+"</p>");
		}
		if (  secret != null && key == null ){
			return postError("<p>" + getRB(rb, "error.nokey", "Error - must have a secret and a key.")+"</p>");
		}

		ltiProps = BasicLTIUtil.signProperties(ltiProps, launch_url, "POST", 
				key, secret, org_guid, org_desc, org_url);

		if ( ltiProps == null ) return postError("<p>" + getRB(rb, "error.sign", "Error signing message.")+"</p>");
		dPrint("LAUNCH III="+ltiProps);

		String debugProperty = toolProps.getProperty("debug");
		boolean dodebug = "on".equals(debugProperty) || "1".equals(debugProperty);
		String postData = BasicLTIUtil.postLaunchHTML(ltiProps, launch_url, dodebug);

		String [] retval = { postData, launch_url };
		return retval;
	}

	public static String getSourceDID(User user, Placement placement, Properties config)
	{
		String placementSecret = toNull(getCorrectProperty(config,"placementsecret", placement));
		if ( placementSecret == null ) return null;
		return getSourceDID(user, placement.getId(), placementSecret);
	}

	public static String getSourceDID(User user, String placeStr, String placementSecret)
	{
		if ( placementSecret == null ) return null;
		String suffix = ":::" +  user.getId() + ":::" + placeStr;
		String base_string = placementSecret + suffix;
		String signature = ShaUtil.sha256Hash(base_string);
		return signature + suffix;
	}

	public static String[] postError(String str) {
		String [] retval = { str };
		return retval;
	}

	public static String getRB(ResourceLoader rb, String key, String def)
	{
		if ( rb == null ) return def;
		return rb.getString(key, def);
	}

	// To make absolutely sure we never send an XSS, we clean these values
	public static void setProperty(Properties props, String key, String value)
	{
		if ( value == null ) return;
		value = Web.cleanHtml(value);
		if ( value.trim().length() < 1 ) return;
		props.setProperty(key, value);
	}

	private static String getExternalRealmId(String siteId) {
		String realmId = SiteService.siteReference(siteId);
		String rv = null;
		try {
			AuthzGroup realm = AuthzGroupService.getAuthzGroup(realmId);
			rv = realm.getProviderGroupId();
		} catch (GroupNotDefinedException e) {
			dPrint("SiteParticipantHelper.getExternalRealmId: site realm not found"+e.getMessage());
		}
		return rv;
	} // getExternalRealmId

	// Look through a series of secrets from the properties based on the launchUrl
	private static String getToolConsumerInfo(String launchUrl, String data)
	{
		String default_secret = ServerConfigurationService.getString("basiclti.consumer_instance_"+data,null);
		dPrint("launchUrl = "+launchUrl);
		URL url = null;
		try {
			url = new URL(launchUrl);
		}
		catch (Exception e) {
			url = null;
		}
		if ( url == null ) return default_secret;
		String hostName = url.getHost();
		dPrint("host = "+hostName);
		if ( hostName == null || hostName.length() < 1 ) return default_secret;
		// Look for the property starting with the full name
		String org_info = ServerConfigurationService.getString("basiclti.consumer_instance_"+data+"."+hostName,null);
		if ( org_info != null ) return org_info;
		for ( int i = 0; i < hostName.length(); i++ ) {
			if ( hostName.charAt(i) != '.' ) continue;
			if ( i > hostName.length()-2 ) continue;
			String hostPart = hostName.substring(i+1);
			String propName = "basiclti.consumer_instance_"+data+"."+hostPart;
			org_info = ServerConfigurationService.getString(propName,null);
			if ( org_info != null ) return org_info;
		}
		return default_secret;
	}

	static private String getOurServerUrl() {
		String ourUrl = ServerConfigurationService.getString("sakai.rutgers.linktool.serverUrl");
		if (ourUrl == null || ourUrl.equals(""))
			ourUrl = ServerConfigurationService.getServerUrl();
		if (ourUrl == null || ourUrl.equals(""))
			ourUrl = "http://127.0.0.1:8080";

		return ourUrl;
	}

	public static String toNull(String str)
	{
		if ( str == null ) return null;
		if ( str.trim().length() < 1 ) return null;
		return str;
	}

	public static int getInt(Object o)
	{
		if ( o instanceof String ) {
			try {
				return (new Integer((String) o)).intValue();
			} catch (Exception e) {
				return -1;
			}
		}
		if ( o instanceof Number ) return ( (Number) o).intValue();
		return -1;
	}
}
