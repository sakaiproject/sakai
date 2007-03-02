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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.apache.pluto.spi.optional.P3PAttributes;

public class SakaiOptionalPortletContainerServices implements OptionalContainerServices {

    private static Log M_log = LogFactory.getLog(SakaiOptionalPortletContainerServices.class);

    // OptionalContainerServices Impl ------------------------------------------
    
    // TODO: Uncomment for Pluto 1.1.1
    // private UserInfoService userInfoService = new SakaiUserInfoService();

    public PortletPreferencesService getPortletPreferencesService() {
        // System.out.println("Sakai portletPreferencesService called.");
        return null;
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

}
