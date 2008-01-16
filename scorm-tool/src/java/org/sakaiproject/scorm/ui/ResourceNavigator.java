package org.sakaiproject.scorm.ui;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.scorm.navigation.INavigable;
import org.sakaiproject.scorm.service.api.ScormResourceService;
import org.sakaiproject.scorm.ui.player.pages.PlayerPage;

public abstract class ResourceNavigator implements INavigable, Serializable {

	private static Log log = LogFactory.getLog(ResourceNavigator.class);
	
	protected abstract ScormResourceService resourceService();
	
	public abstract Object getApplication();
	
	public void displayResource(final SessionBean sessionBean, Object target) {
		if (null == target)
			return;
		
		if (null == sessionBean)
			return;
		
		if (sessionBean.isEnded()) {		
			((AjaxRequestTarget)target).appendJavascript("window.location.href='" + sessionBean.getCompletionUrl() + "';");
		}
		
		if (sessionBean.getLaunchData() == null)
			return;
		
		String resourceId = sessionBean.getContentPackage().getResourceId();		
		String launchLine = sessionBean.getLaunchData().getLaunchLine();
		
		if (launchLine.startsWith("/"))
			launchLine = launchLine.substring(1);
		if (resourceId.startsWith("/"))
			resourceId = resourceId.substring(1);
		
		StringBuilder nameBuilder = new StringBuilder(resourceId);
		
		if (!resourceId.endsWith("/") && !launchLine.startsWith("/")) 
			nameBuilder.append("/");
		
		nameBuilder.append(launchLine);
		
		String resourceName = nameBuilder.toString();
	
		/*ResourceReference reference = new ResourceReference(PlayerPage.class, resourceName);
	
		String url = RequestCycle.get().urlFor(reference).toString();
			//resourceService().getUrl(sessionBean);
		*/
		
		String url = null;
		
		if (launchLine != null)
			url = "contentPackages/resourceName/" + resourceName;
		
		// Don't bother to display anything if a null url is returned. 
		if (null == url)
			return;
		
		if (log.isInfoEnabled())
			log.info("Going to " + url);
			
		((AjaxRequestTarget)target).appendJavascript("parent.scormContent.location.href='" + url + "'");
	}

}
