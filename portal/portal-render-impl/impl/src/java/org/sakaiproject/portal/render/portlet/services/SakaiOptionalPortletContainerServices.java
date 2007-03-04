package org.sakaiproject.portal.render.portlet.services;

import org.apache.pluto.OptionalContainerServices;
import org.apache.pluto.OptionalContainerServices;
import org.apache.pluto.spi.optional.PortletPreferencesService;
import org.apache.pluto.spi.optional.PortletEnvironmentService;
import org.apache.pluto.spi.optional.PortletInvokerService;
import org.apache.pluto.spi.optional.PortletRegistryService;
import org.apache.pluto.spi.optional.PortletInfoService;
import org.apache.pluto.spi.optional.PortalAdministrationService;
// TODO: Uncomment for Pluto 1.1.1
//import org.apache.pluto.spi.optional.UserInfoService;

import org.apache.pluto.PortletContainerException;

import javax.portlet.PortletRequest;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.Enumeration;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.apache.pluto.spi.optional.P3PAttributes;

import org.apache.pluto.PortletWindow;
import org.apache.pluto.internal.InternalPortletPreference;

import org.sakaiproject.authz.cover.SecurityService;

// import org.sakaiproject.tool.api.Placement;

import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;

import java.net.URLEncoder;
import java.net.URLDecoder;


// Reuse the Pluto preference implementation - nothing wrong with it!
import org.apache.pluto.internal.impl.PortletPreferenceImpl;


public class SakaiOptionalPortletContainerServices implements OptionalContainerServices {

    private static Log M_log = LogFactory.getLog(SakaiOptionalPortletContainerServices.class);

    // OptionalContainerServices Impl ------------------------------------------
    
    // TODO: Uncomment for Pluto 1.1.1
    // private UserInfoService userInfoService = new SakaiUserInfoService();

    private PortletPreferencesService prefService = new SakaiPortletPreferencesService();

    private boolean prefLog = true;
    public PortletPreferencesService getPortletPreferencesService() {
	if ( prefLog) M_log.info("Sakai Optional Portal Services returning "+prefService);
        prefLog = false;  // Only log once
        return prefService;
    }


    public PortletRegistryService getPortletRegistryService() {
        // System.out.println("Sakai portletRegistryService called.");
        return null;
    }

    public PortletEnvironmentService getPortletEnvironmentService() {
        // System.out.println("Sakai portletEnvironmentService called.");
        return null;
    }
    
    public PortletInvokerService getPortletInvokerService() {
        // System.out.println("Sakai portletInvokerService called.");
        return null;
    }

    public PortletInfoService getPortletInfoService() {
        // System.out.println("Sakai portletInfoService called.");
        return null;
    }

    public PortalAdministrationService getPortalAdministrationService() {
        // System.out.println("Sakai portalAdministrationService called.");
        return null;
    }

    // TODO: Uncomment for Pluto 1.1.1
/*
    private boolean userInfoLog = true;
    public UserInfoService getUserInfoService() {
	if ( userInfoLog) M_log.info("Sakai Optional Portal Services returning "+userInfoService);
        userInfoLog = false;  // Only log once
        return userInfoService;
    }

    // Our implementations of these local services

    // At some level this could return a clever proxy which did lazy loading
    public class SakaiUserInfoService implements UserInfoService {

        public Map getUserInfo(PortletRequest request) throws PortletContainerException {

            Map retval = null;

            User user = UserDirectoryService.getCurrentUser();
            if ( user != null ) {
                // System.out.println("Found Current User="+user.getEid());
                retval = new HashMap<String,String> ();
                retval.put(P3PAttributes.USER_HOME_INFO_ONLINE_EMAIL,user.getEmail());
                retval.put(P3PAttributes.USER_BUSINESS_INFO_ONLINE_EMAIL,user.getEmail());
                retval.put(P3PAttributes.USER_NAME_GIVEN,user.getFirstName());
                retval.put(P3PAttributes.USER_NAME_FAMILY,user.getLastName());
                retval.put(P3PAttributes.USER_NAME_NICKNAME,user.getDisplayName());
            }

            // System.out.println("Returning=" +retval);
            if ( retval == null ) retval = new HashMap();
            return retval;
        }
    }
*/
    // TODO: End Uncomment for Pluto 1.1.1


    public class SakaiPortletPreferencesService implements PortletPreferencesService {
	
	public SakaiPortletPreferencesService() {
		// Do nothing.
	}

	/**
	 * Returns the stored portlet preferences array. The preferences managed by
	 * this service should be protected from being directly accessed, so this
	 * method returns a cloned copy of the stored preferences.
	 * 
	 * @param portletWindow  the portlet window.
	 * @param request  the portlet request from which the remote user is retrieved.
	 * @return a copy of the stored portlet preferences array.
	 * @throws PortletContainerException
	 */
	public InternalPortletPreference[] getStoredPreferences(
			PortletWindow portletWindow,
			PortletRequest request)
	throws PortletContainerException {


	    String key = portletWindow.getId().getStringId();

            // find the tool from some site
            ToolConfiguration siteTool = SiteService.findTool(key);
            // System.out.println("siteTool="+siteTool);

	    ArrayList<InternalPortletPreference> prefArray = new ArrayList<InternalPortletPreference> ();
       	    if ( siteTool != null ) {
		Properties props = siteTool.getPlacementConfig();
		// System.out.println("props = "+props);
     		for (Enumeration e = props.propertyNames() ; e.hasMoreElements() ;) {
		    String propertyName = (String) e.nextElement();
		    // System.out.println("Property name = "+propertyName);
		    if ( propertyName != null && propertyName.startsWith("javax.portlet:") && propertyName.length() > 14) {
		    	String propertyValue = props.getProperty(propertyName);
		    	String [] propertyList = deSerializeStringArray(propertyValue);
		    	String internalName = propertyName.substring(14);
		    	// System.out.println("internalName="+internalName+" propertyList="+propertyList);
		    	InternalPortletPreference newPref = new PortletPreferenceImpl(internalName,propertyList);
		    	// System.out.println("newPref = "+newPref);
			prefArray.add(newPref);
		    }
     		}
	    }

            InternalPortletPreference[] preferences = new InternalPortletPreference[prefArray.size()];

            preferences = (InternalPortletPreference[]) prefArray.toArray(preferences);

            if (M_log.isDebugEnabled()) {
            	M_log.debug("Got " + preferences.length + " stored preferences.");
            }
            return preferences;
	}
	
	/**
	 * Stores the portlet preferences to the in-memory storage. This method
	 * should be invoked after the portlet preferences are validated by the
	 * preference validator (if defined).
	 * <p>
	 * The preferences managed by this service should be protected from being
	 * directly accessed, so this method clones the passed-in preferences array
	 * and saves it.
	 * </p>
	 * 
	 * @see javax.portlet.PortletPreferences#store()
	 *
	 * @param portletWindow  the portlet window
	 * @param request  the portlet request from which the remote user is retrieved.
	 * @param preferences  the portlet preferences to store.
	 * @throws PortletContainerException
	 */
    	public void store(PortletWindow portletWindow,
                      PortletRequest request,
                      InternalPortletPreference[] preferences)
    	throws PortletContainerException {

	    String key = portletWindow.getId().getStringId();

            // find the tool from some site
            ToolConfiguration siteTool = SiteService.findTool(key);
            // System.out.println("siteTool="+siteTool);
	    if ( siteTool == null ) return;

	    Properties props = siteTool.getPlacementConfig();
	    if ( props == null ) return;

            String siteId = siteTool.getSiteId();
            // System.out.println("siteId="+siteId);

            String siteReference = SiteService.siteReference(siteId);
            // System.out.println("Reference="+siteReference);

	    // If you don't have site.upd - silently return not storing
  	    // In an ideal world perhaps we should throw java.io.IOException
	    // As per PortletPreferences API on the store() method
            if ( ! SecurityService.unlock("site.upd",siteReference) ) {
		// System.out.println("You do not have site.upd - silently returning and not storing");
		return;
	    }

	    // System.out.println("props before cleanup= "+props);

	    boolean changed = false;

            // Remove properties from the placement which did not come back to be stored
	    if ( props != null ) {
     		for (Enumeration e = props.propertyNames() ; e.hasMoreElements() ;) {
		    String propertyName = (String) e.nextElement();
		    // System.out.println("Property name = "+propertyName);
		    if ( propertyName != null && propertyName.startsWith("javax.portlet:") && propertyName.length() > 14) {
		    	String internalName = propertyName.substring(14);
		    	// System.out.println("making sure we still have a prop named internalName="+internalName);
			boolean found = false;
    	                for (int i = 0; i < preferences.length; i++) {
		            if ( preferences[i] != null ) {
                                String propName = preferences[i].getName();
		                // System.out.println("Store["+i+"]  ="+propName);
				if ( internalName.equals(propName) ) {
                                    found = true;
                                    break;
                                }
                            }
                        }
			if ( ! found ) {
                           // System.out.println("Removing "+propertyName);
                           props.remove(propertyName);
                           changed = true;
                        }
                   }
               }
            }

	    // System.out.println("props after cleanup= "+props);

	    // Add / up date property values
    	    for (int i = 0; i < preferences.length; i++) {
		// System.out.println("Store["+i+"]  ="+preferences[i]);
		if ( preferences[i] != null && props != null ) {
		    String propKey = "javax.portlet:"+preferences[i].getName();
		    String storeString = serializeStringArray(preferences[i].getValues());
		    String oldString = props.getProperty(propKey);

                    // System.out.println("propKey = "+propKey);
                    // System.out.println("storeString = "+storeString);
                    // System.out.println("oldString = "+oldString);
                    if ( (oldString == null && storeString != null ) || ( ! storeString.equals(oldString) ) ) {
		        // System.out.println("Setting "+propKey+" value="+storeString);
		        props.setProperty(propKey,storeString);
		        changed = true;
	    	    }
                }
	    }

            // System.out.println("props after update= "+props);
	    // System.out.println("changed="+changed);

	    if ( changed && siteTool != null ) {
		siteTool.save();
		// System.out.println("Saved");
	    }

            if (M_log.isDebugEnabled()) {
                M_log.debug("Portlet preferences stored for: " + key);
            }
    	}
    
    	private String serializeStringArray(String [] input)
    	{
	    if ( input == null || input.length < 1 ) return null;

	    String retval = "";
	    for ( int i=0; i< input.length; i++ )
	    {
	        if ( i > 0 ) retval = retval + "!";
	        retval += URLEncoder.encode(input[i]);
	    }
	    return retval;
    	}

    	private String [] deSerializeStringArray(String input)
    	{
	    // System.out.println("Input="+input);
	    String [] retval = input.split("!");
	    // System.out.println("Found "+retval.length+" items.");
	    for ( int i = 0; i< retval.length; i++ ) {
	    	retval[i] = URLDecoder.decode(retval[i]);
	    }
	    return retval;
    	}

    }  // End of SakaiPortletPreferencesService

}
