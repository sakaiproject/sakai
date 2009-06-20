package org.sakaiproject.basiclti;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import java.net.URL;

import org.imsglobal.basiclti.BasicLTIUtil;

// Sakai APIs
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.tool.api.ActiveTool;
import org.sakaiproject.tool.cover.ActiveToolManager;
import org.sakaiproject.authz.cover.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.component.cover.ServerConfigurationService;

/**
 * Some Sakai Utility code for IMS Basic LTI
 */
public class IMSBLTIUtil {

    public static final boolean verbosePrint = true;

    public static void dPrint(String str)
    {
        if ( verbosePrint ) System.out.println(str);
    }

   public static void loadProperty(Properties retProp, String propKey, Map<String,String> tm, String xmlKey1, String xmlKey2)
   {
	String value = null;
	if ( value == null && xmlKey1 != null ) {
		value = tm.get(xmlKey1);
	}
	if ( value == null && xmlKey2 != null ) {
 		value = tm.get(xmlKey2);
        }
	if ( value == null ) return;
	retProp.setProperty(propKey, value);	
   }

    // Look at the properties and figure out the launchurl, secret, 
    // frameheight, and new window
    //
    public static boolean launchInfo(Properties info, Properties launch, Placement placement)
    {
	Properties config = placement.getConfig();
	dPrint("Sakai properties=" + config);
        String launch_url = toNull(config.getProperty("imsti.launch", null));
        setProperty(info, "launch_url", launch_url);
        if ( launch_url == null ) {
            String xml = config.getProperty("imsti.xml", null);
	    BasicLTIUtil.launchInfo(info, launch, xml);
        }
        setProperty(info, "secret", config.getProperty("imsti.secret", null) );
        setProperty(info, "frameheight", config.getProperty("imsti.frameheight", null) );
        setProperty(info, "newwindow", config.getProperty("imsti.newwindow", null) );
        setProperty(info, "title", config.getProperty("imsti.tooltitle", null) );
        if ( info.getProperty("launch_url", null) != null || 
             info.getProperty("secure_launch_url", null) != null ) {
            return true;
        }
        return false;
    }

   // Retrieve the Sakai information about users, etc.
   public static boolean sakaiInfo(Properties props, Placement placement)
   {
	dPrint("placement="+ placement.getId());
	dPrint("placement title=" + placement.getTitle());
        String context = placement.getContext();
        dPrint("ContextID="+context);

        ToolConfiguration toolConfig = SiteService.findTool(placement.getId());
        // ActiveTool at = ActiveToolManager.getActiveTool(toolConfig.getToolId());
        Site site = null;
        SitePage page = null;
        try {
		site = SiteService.getSite(context);
        	page = site.getPage(toolConfig.getPageId());
        } catch (Exception e) {
                dPrint("No site/page associated with Launch context="+context);
                return false;
	}
                
	User user = UserDirectoryService.getCurrentUser();

	// Start setting the Basici LTI parameters
	setProperty(props,"resource_link_id",placement.getId());

	// TODO: Think about anonymus
	if ( user != null )
	{
		setProperty(props,"user_id",user.getId());
		setProperty(props,"launch_presentaion_locale","en_US"); // TODO: Really get this
		setProperty(props,"lis_person_name_given",user.getFirstName());
		setProperty(props,"lis_person_name_family",user.getLastName());
		setProperty(props,"lis_person_name_full",user.getDisplayName());
		setProperty(props,"lis_person_contact_emailprimary",user.getEmail());
		setProperty(props,"lis_person_sourced_id",user.getEid());
	}

	String theRole = "Student";
	if ( SecurityService.isSuperUser() )
	{
		theRole = "Administrator";
	}
	else if ( SiteService.allowUpdateSite(context) ) 
	{
		theRole = "Instructor";
	}
	setProperty(props,"roles",theRole);

	if ( site != null ) {
		String context_type = site.getType();
System.out.println("SITE TYPE "+context_type);
		if ( context_type != null && context_type.toLowerCase().contains("course") ){
			setProperty(props,"context_type","CourseOffering");
		}
		setProperty(props,"context_id",site.getId());
		setProperty(props,"context_title",site.getTitle());
		setProperty(props,"course_name",site.getShortDescription());
		String courseRoster = getExternalRealmId(site.getId());
		if ( courseRoster != null ) 
		{
			setProperty(props,"lis_course_offering_sourced_id",courseRoster);
		}
	}

	// We pass this along in the Sakai world - it might
	// might be useful to the external tool
	String serverId = ServerConfigurationService.getServerId();
	setProperty(props,"sakai_serverid",serverId);
        setProperty(props,"sakai_server",getOurServerUrl());

	// Get the organizational information
	setProperty(props,"tool_consmer_instance_guid", ServerConfigurationService.getString("basiclti.consumer_instance_guid",null));
	setProperty(props,"tool_consmer_instance_name", ServerConfigurationService.getString("basiclti.consumer_instance_name",null));
	setProperty(props,"tool_consmer_instance_url", ServerConfigurationService.getString("basiclti.consumer_instance_url",null));
	return true;
    } 

    public static void setProperty(Properties props, String key, String value)
    {
        if ( value == null ) return;
        if ( value.trim().length() < 1 ) return;
        props.setProperty(key, value);
    }

    private static String getContext()
    {
        String retval = ToolManager.getCurrentPlacement().getContext();
        return retval;
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

    // String org_secret = ServerConfigurationService.getString("simplelti.org_secret");
    private static String getOrgSecret(String launchUrl)
    {
        String default_secret = ServerConfigurationService.getString("simplelti.org_secret",null);
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
        String org_secret = ServerConfigurationService.getString("simplelti.org_secret."+hostName,null);
        if ( org_secret != null ) return org_secret;
        for ( int i = 0; i < hostName.length(); i++ ) {
            if ( hostName.charAt(i) != '.' ) continue;
            if ( i > hostName.length()-2 ) continue;
            String hostPart = hostName.substring(i+1);
            String propName = "simplelti.org_secret."+hostPart;
            org_secret = ServerConfigurationService.getString(propName,null);
            if ( org_secret != null ) return org_secret;
        }
        return default_secret;
    }

    static private String getOurServerUrl() {
        String ourUrl = ServerConfigurationService.getString("sakai.rutgers.linktool.serverUrl");
        // System.out.println("linktool url " + ourUrl);
        if (ourUrl == null || ourUrl.equals(""))
            ourUrl = ServerConfigurationService.getServerUrl();
        // System.out.println("linktool url " + ourUrl);
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
