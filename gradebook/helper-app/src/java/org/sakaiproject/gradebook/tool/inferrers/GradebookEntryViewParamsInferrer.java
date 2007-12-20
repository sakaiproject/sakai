package org.sakaiproject.gradebook.tool.inferrers;

import org.sakaiproject.entitybroker.IdEntityReference;
import org.sakaiproject.gradebook.tool.entity.GradebookEntryEntityProvider;
import org.sakaiproject.gradebook.tool.params.AddGradebookItemViewParams;
import org.sakaiproject.gradebook.tool.helper.AddGradebookItemProducer;

import org.sakaiproject.service.gradebook.shared.GradebookService;


import uk.ac.cam.caret.sakai.rsf.entitybroker.EntityViewParamsInferrer;
import uk.org.ponder.rsf.viewstate.ViewParameters;

public class GradebookEntryViewParamsInferrer implements EntityViewParamsInferrer {

	private GradebookService gradebookService;
	public void setGradebookService(GradebookService gradebookService) {
		this.gradebookService = gradebookService;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.cam.caret.sakai.rsf.entitybroker.EntityViewParamsInferrer#getHandledPrefixes()
	 */
	public String[] getHandledPrefixes() {
		return new String[] { GradebookEntryEntityProvider.ENTITY_PREFIX };
	}

	/* (non-Javadoc)
	 * @see uk.ac.cam.caret.sakai.rsf.entitybroker.EntityViewParamsInferrer#inferDefaultViewParameters(java.lang.String)
	 */
	public ViewParameters inferDefaultViewParameters(String reference) {
		IdEntityReference ep = new IdEntityReference(reference);
		String siteId = ep.id;

		return new AddGradebookItemViewParams(AddGradebookItemProducer.VIEW_ID, null);
	}
}