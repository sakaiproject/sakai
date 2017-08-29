/**
 * Copyright (c) 2005-2015 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.tool.assessment.rsf.bean;

import org.sakaiproject.entitybroker.IdEntityReference;
import org.sakaiproject.tool.assessment.entity.api.PublishedAssessmentEntityProvider;
import org.sakaiproject.tool.assessment.entity.api.CoreAssessmentEntityProvider;
import org.sakaiproject.tool.assessment.rsf.params.BeginAssessmentViewParameters;
import org.sakaiproject.tool.assessment.rsf.producers.BeginAssessmentProducer;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacadeQueriesAPI;

import org.sakaiproject.rsf.entitybroker.EntityViewParamsInferrer;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * Entity View Paramater Inferrer for samigo PublishedAssessments
 * 
 * @author Joshua Ryan  josh@asu.edu  alt^I
 *
 */
public class PublishedAssessmentVPInferrer implements EntityViewParamsInferrer {

  private PublishedAssessmentFacadeQueriesAPI publishedAssessmentFacadeQueries;
  public void setPublishedAssessmentFacadeQueries(
      PublishedAssessmentFacadeQueriesAPI publishedAssessmentFacadeQueries) {
    this.publishedAssessmentFacadeQueries = publishedAssessmentFacadeQueries;
  }

  public String[] getHandledPrefixes() {
      return new String[] {PublishedAssessmentEntityProvider.ENTITY_PREFIX, CoreAssessmentEntityProvider.ENTITY_PREFIX};
  }

  public ViewParameters inferDefaultViewParameters(String reference) {
    BeginAssessmentViewParameters params = new BeginAssessmentViewParameters();
    IdEntityReference ep = new IdEntityReference(reference);
    if (reference.startsWith("/" + CoreAssessmentEntityProvider.ENTITY_PREFIX + "/")) {
	Long id = null;
	Long publishedId = null;
	if (ep.id != null) {
	    try {
		id = new Long(ep.id);
	    } catch (Exception e) {
		// bad number, will end up using 0 which will fail in the producer
	    }	       
	}
	if (id != null)
	    publishedId = publishedAssessmentFacadeQueries.getPublishedAssessmentId(id);
	if (publishedId != null)
	    params.pubReference = publishedId.toString();
	else
	    params.pubReference = "0";
    } else
	params.pubReference = ep.id;
    params.viewID = BeginAssessmentProducer.VIEW_ID;
    return params;
  }
}
