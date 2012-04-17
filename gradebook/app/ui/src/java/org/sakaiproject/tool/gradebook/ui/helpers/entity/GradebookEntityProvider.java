package org.sakaiproject.tool.gradebook.ui.helpers.entity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.tool.gradebook.ui.helpers.producers.GradebookItemProducer;
import org.sakaiproject.tool.gradebook.ui.helpers.producers.AuthorizationFailedProducer;
import org.sakaiproject.tool.gradebook.ui.helpers.params.GradebookItemViewParams;
import org.sakaiproject.service.gradebook.shared.GradebookService;

import uk.ac.cam.caret.sakai.rsf.entitybroker.EntityViewParamsInferrer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/*
 * This is a provider for looking up and adding/editing Gradebook Items.
 * It is actually passing along to a gradebook UI via RSF and does not provide any rest access to grades data
 */
public class GradebookEntityProvider implements EntityProvider, CoreEntityProvider, EntityViewParamsInferrer, Describeable {
    private Log log = LogFactory.getLog(GradebookEntityProvider.class);
    public final static String ENTITY_PREFIX = "gradebook";
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
        //IdEntityReference ep = new IdEntityReference(reference);
        //String contextId = ep.id;
        String contextId = new EntityReference(reference).getId();

        if(gradebookService.currentUserHasEditPerm(contextId)){
            Long gradebookEntryId = null;
            return new GradebookItemViewParams(GradebookItemProducer.VIEW_ID, contextId, gradebookEntryId);
        }else{
            return new SimpleViewParameters(AuthorizationFailedProducer.VIEW_ID);
        }

    }

    public void setEntityProviderManager(EntityProviderManager entityProviderManager) {
        this.entityProviderManager = entityProviderManager;
    }

    public void setGradebookService(GradebookService gradebookService) {
        this.gradebookService = gradebookService;
    }

}
