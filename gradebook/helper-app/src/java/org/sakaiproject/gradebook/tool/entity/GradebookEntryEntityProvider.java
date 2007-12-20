package org.sakaiproject.gradebook.tool.entity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.sakaiproject.gradebook.tool.helper.AddGradebookItemProducer;

import uk.ac.cam.caret.sakai.rsf.entitybroker.EntityViewParamsInferrer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/*
 * This is a provider for looking up and adding/editing Gradebook Items.
 */
public class GradebookEntryEntityProvider implements EntityProvider, CoreEntityProvider,
EntityViewParamsInferrer {
    private Log log = LogFactory.getLog(GradebookEntryEntityProvider.class);
    public final static String ENTITY_PREFIX = "grade-entry";
    private EntityProviderManager entityProviderManager;
    
    public void init() {
        log.info("init()");
        entityProviderManager.registerEntityProvider(this);
    }
    
    public void destroy() {
        log.info("destroy()");
        entityProviderManager.unregisterEntityProvider(this);
    }
    
    public String getEntityPrefix() {
        return ENTITY_PREFIX;
    }

    public boolean entityExists(String id) {
        return true;
    }

    public String[] getHandledPrefixes() {
        return new String[] {ENTITY_PREFIX};
    }

    public ViewParameters inferDefaultViewParameters(String reference) {
        return new SimpleViewParameters(AddGradebookItemProducer.VIEW_ID);
    }

    public void setEntityProviderManager(EntityProviderManager entityProviderManager) {
        this.entityProviderManager = entityProviderManager;
    }

}
