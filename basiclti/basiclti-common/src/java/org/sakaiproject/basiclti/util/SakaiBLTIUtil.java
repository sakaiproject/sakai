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
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.basiclti.util;

import java.util.Properties;
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
import org.sakaiproject.authz.cover.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.component.cover.ServerConfigurationService;
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
    public static final String BASICLTI_CONTENTLINK_ENABLED = "basiclti.contentlink.enabled";

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
        setProperty(info, "secret", getCorrectProperty(config,"secret", placement) );
        setProperty(info, "key", getCorrectProperty(config,"key", placement) );
        setProperty(info, "debug", getCorrectProperty(config,"debug", placement) );
        setProperty(info, "frameheight", getCorrectProperty(config,"frameheight", placement) );
        setProperty(info, "newpage", getCorrectProperty(config,"newpage", placement) );
        setProperty(info, "title", getCorrectProperty(config,"tooltitle", placement) );

	// Pull in and parse the custom parameters
	String customstr = toNull(getCorrectProperty(config,"custom", placement) );
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
			value = value.trim();
			if ( value.length() < 1 ) continue;
			if ( value == null ) continue;
        		setProperty(info, "custom_"+key, value);
		}
	}

        if ( info.getProperty("launch_url", null) != null || 
             info.getProperty("secure_launch_url", null) != null ) {
            return true;
        }
        return false;
    }

   public static boolean sakaiInfo(Properties props, Placement placement, ResourceLoader rb)
   {
	dPrint("placement="+ placement.getId());
	dPrint("placement title=" + placement.getTitle());
        String context = placement.getContext();
        dPrint("ContextID="+context);

        return sakaiInfo(props, context, placement.getId(), rb);
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
                
	User user = UserDirectoryService.getCurrentUser();

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

	// TODO: Think about anonymus
	if ( user != null )
	{
		setProperty(props,BasicLTIConstants.USER_ID,user.getId());
		setProperty(props,BasicLTIConstants.LAUNCH_PRESENTATION_LOCALE,rb.getLocale().toString()); 
		if ( "on".equals(releasename) ) {
			setProperty(props,BasicLTIConstants.LIS_PERSON_NAME_GIVEN,user.getFirstName());
			setProperty(props,BasicLTIConstants.LIS_PERSON_NAME_FAMILY,user.getLastName());
			setProperty(props,BasicLTIConstants.LIS_PERSON_NAME_FULL,user.getDisplayName());
		}
		if ( "on".equals(releaseemail) ) {
			setProperty(props,BasicLTIConstants.LIS_PERSON_CONTACT_EMAIL_PRIMARY,user.getEmail());
			setProperty(props,BasicLTIConstants.LIS_PERSON_SOURCEDID,user.getEid());
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

		String result_sourcedid = getSourceDID(user, placement, config);
		if ( result_sourcedid != null ) {

                	if ( "true".equals(allowOutcomes) && assignment != null ) {
				setProperty(props,"lis_result_sourcedid", result_sourcedid);  
	
				// New Basic Outcomes URL
				String outcome_url = ServerConfigurationService.getString("basiclti.consumer.ext_ims_lis_basic_outcome_url",null);
        			if ( outcome_url == null ) outcome_url = getOurServerUrl() + "/imsblis/service/";  
				setProperty(props,"ext_ims_lis_basic_outcome_url", outcome_url);  
	
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
		}
	}

	String theRole = "Learner";
	if ( SecurityService.isSuperUser() )
	{
		theRole = "Instructor";
	}
	else if ( SiteService.allowUpdateSite(context) ) 
	{
		theRole = "Instructor";
	}
	setProperty(props,"roles",theRole);

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


        // Sakai-Unique fields - compatible with LinkTool
        Session s = SessionManager.getCurrentSession();
        if (s != null) {
                String sessionid = s.getId();
                if (sessionid != null) {
                        sessionid = LinkToolUtil.encrypt(sessionid);
                        setProperty(props,"ext_sakai_session",sessionid);
                }
        }

	// We pass this along in the Sakai world - it might
	// might be useful to the external tool
	String serverId = ServerConfigurationService.getServerId();
	setProperty(props,"ext_sakai_serverid",serverId);
        setProperty(props,"ext_sakai_server",getOurServerUrl());

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
	setProperty(props,BasicLTIConstants.LAUNCH_PRESENTATION_RETURN_URL, 
		ServerConfigurationService.getString("basiclti.consumer_return_url",null));

	// Send along the content link
        String contentlink = toNull(getCorrectProperty(config,"contentlink", placement));
	if ( contentlink != null ) setProperty(props,"ext_resource_link_content",contentlink);

	// Send along the CSS URL
	String tool_css = ServerConfigurationService.getString("basiclti.consumer.launch_presentation_css_url",null);
        if ( tool_css == null ) tool_css = getOurServerUrl() + "/library/skin/default/tool.css";  
	setProperty(props,BasicLTIConstants.LAUNCH_PRESENTATION_CSS_URL, tool_css);  

	// Let tools know we are coming from Sakai
	setProperty(props,"ext_lms", "sakai2");  
	return true;
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
    public static String[] postLaunchHTML(String placementId, ResourceLoader rb)
    {
        if ( placementId == null ) return postError("<p>" + getRB(rb, "error.missing" ,"Error, missing placementId")+"</p>" );
        ToolConfiguration placement = SiteService.findTool(placementId);
        if ( placement == null ) return postError("<p>" + getRB(rb, "error.load" ,"Error, cannot load placement=")+placementId+".</p>");
    
        // Add user, course, etc to the launch parameters
        Properties launch = new Properties();
        if ( ! sakaiInfo(launch, placement, rb) ) {
           return postError("<p>" + getRB(rb, "error.missing",
                "Error, cannot load Sakai information for placement=")+placementId+".</p>");
        }
        
        // Retrieve the launch detail
        Properties info = new Properties();
        if ( ! loadFromPlacement(info, launch, placement) ) {
           return postError("<p>" + getRB(rb, "error.nolaunch" ,"Not Configured.")+"</p>");
	}
    	return postLaunchHTML(info, launch, rb);
    }

    public static String[] postLaunchHTML(Properties info, Properties launch, ResourceLoader rb)
    {

        String launch_url = info.getProperty("secure_launch_url");
	if ( launch_url == null ) launch_url = info.getProperty("launch_url");
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
		secret = toNull(info.getProperty("secret"));
		key = toNull(info.getProperty("key"));
	}

	// Pull in all of the custom parameters
        for(Object okey : info.keySet() ) {
                String skey = (String) okey;  
		if ( ! skey.startsWith(BasicLTIConstants.CUSTOM_PREFIX) ) continue;
		String value = info.getProperty(skey);
		if ( value == null ) continue;
        	setProperty(launch, skey, value);
	}

        String oauth_callback = ServerConfigurationService.getString("basiclti.oauth_callback",null);
	// Too bad there is not a better default callback url for OAuth
        // Actually since we are using signing-only, there is really not much point 
	// In OAuth 6.2.3, this is after the user is authorized
	if ( oauth_callback == null ) oauth_callback = "about:blank";
        setProperty(launch, "oauth_callback", oauth_callback);
        setProperty(launch, BasicLTIUtil.BASICLTI_SUBMIT, getRB(rb, "launch.button", "Press to Launch External Tool"));

        // Sanity checks
        if ( secret == null ) {
            return postError("<p>" + getRB(rb, "error.nosecret", "Error - must have a secret.")+"</p>");
        }
        if (  secret != null && key == null ){
            return postError("<p>" + getRB(rb, "error.nokey", "Error - must have a secret and a key.")+"</p>");
        }

        launch = BasicLTIUtil.signProperties(launch, launch_url, "POST", 
            key, secret, org_guid, org_desc, org_url);

        if ( launch == null ) return postError("<p>" + getRB(rb, "error.sign", "Error signing message.")+"</p>");
        dPrint("LAUNCH III="+launch);

	boolean dodebug = "on".equals(info.getProperty("debug"));
        String postData = BasicLTIUtil.postLaunchHTML(launch, launch_url, dodebug);

        String [] retval = { postData, launch_url };
        return retval;
    }


    public static String getSourceDID(User user, Placement placement, Properties config)
    {
	String placementSecret = toNull(getCorrectProperty(config,"placementsecret", placement));
	if ( placementSecret == null ) return null;
	String suffix = ":::" +  user.getId() + ":::" + placement.getId();
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


}
