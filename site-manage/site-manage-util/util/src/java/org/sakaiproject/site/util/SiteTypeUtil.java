package org.sakaiproject.site.util;

import java.util.List;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.site.api.SiteService;

public class SiteTypeUtil {
	
	private static org.sakaiproject.site.api.SiteService siteService = (org.sakaiproject.site.api.SiteService) ComponentManager
	.get("org.sakaiproject.site.api.SiteService");
	
	private static org.sakaiproject.component.api.ServerConfigurationService serverConfigurationService = (org.sakaiproject.component.api.ServerConfigurationService) ComponentManager
	.get("org.sakaiproject.component.api.ServerConfigurationService");
	
    /**
     * get all site type strings associated with course site type
     * @return
     */
    public static List<String> getCourseSiteTypes()
    {
    	return siteService.getSiteTypeStrings("course");
    }
    
    /**
     * get all site type strings associated with project site type
     * @return
     */
    public static List<String> getProjectSiteTypes()
    {
    	return siteService.getSiteTypeStrings("project");
    }
    
	/**
	 * Is the siteType of course site types
	 * @param siteType
	 * @return
	 */
    public static boolean isCourseSite(String siteType)
    {
    	return isOfSiteType("course", siteType);
    }

	/**
	 * Is the siteType of project site types
	 * @param siteType
	 * @return
	 */
    public static boolean isProjectSite(String siteType)
    {
    	return isOfSiteType("project", siteType); 
    }
    
    /**
     * site type lookup
     * @param targetSiteType
     * @param targetSiteTypeDefault
     * @param currentSiteType
     * @return
     */
	private static boolean isOfSiteType(String targetSiteType, String currentSiteType) {
		boolean rv = false;
		List<String> siteTypes = siteService.getSiteTypeStrings(targetSiteType);
		if (currentSiteType != null && siteTypes != null && siteTypes.contains(currentSiteType))
		{
			rv = true;
		}
		return rv;
	}
	
	/**
	 * returns the site type String to use for sites created based on the template site with siteType
	 * @param siteType
	 * @return
	 */
	public static String getTargetSiteType(String siteType)
	{
		// defaults to current siteType
		String rv = siteType;
		
		// TODO: we only look for course and project site types for now
		if (isCourseSite(siteType))
		{
			rv = serverConfigurationService.getString("courseSiteTargetType", siteType);
		}
		else if (isProjectSite(siteType))
		{
			rv = serverConfigurationService.getString("projectSiteTargetType", siteType);
		}
		
		return rv;
	}
}
