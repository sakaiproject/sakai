package org.sakaiproject.tool.gradebook.ui.helpers.entity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.entitybroker.IdEntityReference;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.sakaiproject.tool.gradebook.ui.helpers.producers.GradeGradebookItemProducer;
import org.sakaiproject.tool.gradebook.ui.helpers.producers.PermissionsErrorProducer;
import org.sakaiproject.tool.gradebook.ui.helpers.params.GradeGradebookItemViewParams;
import org.sakaiproject.service.gradebook.shared.GradebookService;

import uk.ac.cam.caret.sakai.rsf.entitybroker.EntityViewParamsInferrer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/*
 * This is a provider for looking up and adding/editing Gradebook Items.
 */
public class GradeGradebookItemEntityProvider implements EntityProvider, CoreEntityProvider,
EntityViewParamsInferrer {
    private Log log = LogFactory.getLog(GradeGradebookItemEntityProvider.class);
    public final static String ENTITY_PREFIX = "grade-gradebook-item";
    private EntityProviderManager entityProviderManager;
    
    private GradebookService gradebookService;
    
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
        IdEntityReference ep = new IdEntityReference(reference);
    	String contextId = ep.id;
    	
    	if(gradebookService.currentUserHasGradingPerm(contextId) || gradebookService.currentUserHasGradeAllPerm(contextId)){
    		String userId = null;
    		Long gradebookItemId = null;
    		return new GradeGradebookItemViewParams(GradeGradebookItemProducer.VIEW_ID, contextId, gradebookItemId, userId);
    	}else{
    		return new SimpleViewParameters(PermissionsErrorProducer.VIEW_ID);
    	}
    }

    public void setEntityProviderManager(EntityProviderManager entityProviderManager) {
        this.entityProviderManager = entityProviderManager;
    }
    
    public void setGradebookService(GradebookService gradebookService) {
    	this.gradebookService = gradebookService;
    }

}
