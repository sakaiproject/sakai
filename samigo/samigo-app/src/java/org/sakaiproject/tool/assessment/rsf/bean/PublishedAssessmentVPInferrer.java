package org.sakaiproject.tool.assessment.rsf.bean;

import org.sakaiproject.entitybroker.IdEntityReference;
import org.sakaiproject.tool.assessment.entity.api.PublishedAssessmentEntityProvider;
import org.sakaiproject.tool.assessment.rsf.params.BeginAssessmentViewParameters;
import org.sakaiproject.tool.assessment.rsf.producers.BeginAssessmentProducer;

import uk.ac.cam.caret.sakai.rsf.entitybroker.EntityViewParamsInferrer;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * Entity View Paramater Inferrer for samigo PublishedAssessments
 * 
 * @author Joshua Ryan  josh@asu.edu  alt^I
 *
 */
public class PublishedAssessmentVPInferrer implements EntityViewParamsInferrer {

  public String[] getHandledPrefixes() {
    return new String[] {PublishedAssessmentEntityProvider.ENTITY_PREFIX};
  }

  public ViewParameters inferDefaultViewParameters(String reference) {
    BeginAssessmentViewParameters params = new BeginAssessmentViewParameters();
    IdEntityReference ep = new IdEntityReference(reference);
    params.pubReference = ep.id;
    params.viewID = BeginAssessmentProducer.VIEW_ID;
    return params;
  }
}
