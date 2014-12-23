package org.sakaiproject.scorm.ui.player;

import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Page;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.protocol.http.WebRequestCycleProcessor;
import org.apache.wicket.request.IRequestCycleProcessor;
import org.apache.wicket.settings.IExceptionSettings;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.scorm.ui.ContentPackageResourceMountStrategy;
import org.sakaiproject.scorm.ui.console.pages.DisplayDesignatedPackage;
import org.sakaiproject.scorm.ui.console.pages.NotConfiguredPage;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.wicket.protocol.http.SakaiWebApplication;

public class SingleScormPackageApplication extends SakaiWebApplication {

	private static Log log = LogFactory.getLog(SingleScormPackageApplication.class);

	@Override
	public void init() {
		log.info(">>> Initializing singleScormPackageApplication");
		super.init();
		
		mount(new ContentPackageResourceMountStrategy("contentpackages"));

		getExceptionSettings().setUnexpectedExceptionDisplay(IExceptionSettings.SHOW_INTERNAL_ERROR_PAGE);
		
		getResourceSettings().setDisableGZipCompression(false);
	}
	
	@Override
	protected IRequestCycleProcessor newRequestCycleProcessor() {
	    return new WebRequestCycleProcessor(){
	    	@Override
	    	public void respond(RuntimeException e, RequestCycle requestCycle) {
	    		log.debug("Exception occured during normal web processing (may be a 'valid' exception)", e);
	    	    super.respond(e, requestCycle);
	    	}
	    	
	    };
	}

	@Override
	public Class<? extends Page> getHomePage() {
		ToolManager toolManager = (ToolManager) ComponentManager.get(ToolManager.class);
		Properties cfgPlacement = toolManager.getCurrentPlacement().getPlacementConfig();
		String resourceId = cfgPlacement.getProperty(DisplayDesignatedPackage.CFG_PACKAGE_NAME);
		if (StringUtils.isNotEmpty(resourceId)) {
			ScormContentService scormContentService = (ScormContentService) ComponentManager.get(ScormContentService.class);
			ContentPackage contentPackage = scormContentService.getContentPackageByResourceId(resourceId);
			if (contentPackage != null) {
				return DisplayDesignatedPackage.class;
			}
		}
		return NotConfiguredPage.class;
	}

} // class
