package org.sakaiproject.scorm.ui.player;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.scorm.service.api.ScormResourceService;
import org.sakaiproject.scorm.ui.ContentPackageResourceMountStrategy;
import org.sakaiproject.scorm.ui.console.pages.DisplayDesignatedPackage;
import org.sakaiproject.scorm.ui.console.pages.PackageListPage;
import org.sakaiproject.wicket.protocol.http.SakaiWebApplication;

public class SingleScormPackageApplication extends SakaiWebApplication {

    private static Log log = LogFactory.getLog(SingleScormPackageApplication.class);
    
    ScormResourceService resourceService;
    
    @Override
    public void init() {
        log.info(">>> Initializing singleScormPackageApplication");
        super.init();
        mount(new ContentPackageResourceMountStrategy("contentpackages"));
    }
    
    @Override
    public Class getHomePage() {
        log.debug(">>> Getting homepage DisplayDesignatedPackage.class");
        return DisplayDesignatedPackage.class;
    }
    
    
    public ScormResourceService getResourceService() {
        return resourceService;
    }

    public void setResourceService(ScormResourceService resourceService) {
        this.resourceService = resourceService;
    }

} // class
