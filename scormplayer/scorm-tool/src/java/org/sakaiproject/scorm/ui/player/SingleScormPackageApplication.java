package org.sakaiproject.scorm.ui.player;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.settings.IExceptionSettings;
import org.sakaiproject.scorm.ui.ContentPackageResourceMountStrategy;
import org.sakaiproject.scorm.ui.console.pages.DisplayDesignatedPackage;
import org.sakaiproject.wicket.protocol.http.SakaiWebApplication;

public class SingleScormPackageApplication extends SakaiWebApplication {

	private static Log log = LogFactory.getLog(SingleScormPackageApplication.class);

	//ScormResourceService resourceService;

	@Override
	public void init() {
		log.info(">>> Initializing singleScormPackageApplication");
		super.init();
		mount(new ContentPackageResourceMountStrategy("contentpackages"));

		getExceptionSettings().setUnexpectedExceptionDisplay(IExceptionSettings.SHOW_INTERNAL_ERROR_PAGE);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class getHomePage() {
		return DisplayDesignatedPackage.class;
	}

//	public ScormResourceService getResourceService() {
//		return resourceService;
//	}
//
//	public void setResourceService(ScormResourceService resourceService) {
//		this.resourceService = resourceService;
//	}

} // class
