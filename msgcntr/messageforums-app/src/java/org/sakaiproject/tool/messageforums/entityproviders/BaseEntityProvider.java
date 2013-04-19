package org.sakaiproject.tool.messageforums.entityproviders;

import javax.servlet.http.HttpServletResponse;

import lombok.Setter;

import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.ToolManager;

public abstract class BaseEntityProvider extends AbstractEntityProvider {
	
	@Setter
	protected DiscussionForumManager forumManager;
	
	@Setter
	protected UIPermissionsManager uiPermissionsManager;
	
	@Setter
	protected SiteService siteService;
	
	@Setter
	protected ToolManager toolManager;
	
	/**
	 * Checks whether the current user can access this site and whether they can
	 * see the forums tool.
	 * 
	 * @param siteId
	 * @throws EntityException
	 */
	protected void checkSiteAndToolAccess(String siteId) throws EntityException {
        
		//check user can access this site
		Site site;
		try {
			site = siteService.getSiteVisit(siteId);
		} catch (IdUnusedException e) {
			throw new EntityException("Invalid siteId: " + siteId,"", HttpServletResponse.SC_BAD_REQUEST);
		} catch (PermissionException e) {
			throw new EntityException("No access to site: " + siteId,"",HttpServletResponse.SC_UNAUTHORIZED);
		}

		//check user can access the tool, it might be hidden
		ToolConfiguration toolConfig = site.getToolForCommonId("sakai.forums");
		if(!toolManager.isVisible(site, toolConfig)) {
			throw new EntityException("No access to tool in site: " + siteId, "",HttpServletResponse.SC_UNAUTHORIZED);
		}
		
	}
}
