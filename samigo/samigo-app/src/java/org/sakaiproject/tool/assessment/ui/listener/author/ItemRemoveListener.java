/**
 * Copyright (c) 2005-2017 The Apereo Foundation
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

package org.sakaiproject.tool.assessment.ui.listener.author;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.facade.SectionFacade;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemAuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.bean.questionpool.QuestionPoolBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

@Slf4j
public class ItemRemoveListener implements ActionListener
{

	public void processAction(ActionEvent ae) throws AbortProcessingException
	{		
		ItemAuthorBean item = (ItemAuthorBean) ContextUtil.lookupBean("itemauthor");
		ItemService delegate = new ItemService();
		if(item.getItemToDelete() == null) {
			return;
		}
		String deleteId = String.valueOf(item.getItemToDelete().getItemId());
		ItemFacade itemf = delegate.getItem(deleteId);
		// save the currSection before itemf.setSection(null), used to reorder question sequences
		SectionFacade  currSection = (SectionFacade) itemf.getSection();
		Integer  currSeq = itemf.getSequence();
		QuestionPoolService qpdelegate = new QuestionPoolService();
		if (qpdelegate.getPoolIdsByItem(deleteId) ==  null || qpdelegate.getPoolIdsByItem(deleteId).isEmpty()) {
			// if no reference to this item at all, ie, this item is created in assessment but not assigned to any pool
			AuthorizationBean authzBean = (AuthorizationBean) ContextUtil.lookupBean("authorization");
			AssessmentService assessdelegate = new AssessmentService();
			AssessmentFacade af = assessdelegate.getBasicInfoOfAnAssessmentFromSectionId(currSection.getSectionId());
			if (!authzBean.isUserAllowedToEditAssessment(af.getAssessmentBaseId().toString(), af.getCreatedBy(), false)) {
				throw new IllegalArgumentException("User does not have permission to delete item in assessment: " + af.getAssessmentBaseId());
			}
			delegate.deleteItem(Long.valueOf(deleteId), AgentFacade.getAgentString());
		}
		else {
			if (currSection == null) {
				// if this item is created from question pool
				QuestionPoolBean  qpoolbean= (QuestionPoolBean) ContextUtil.lookupBean("questionpool");
				ItemFacade itemfacade= delegate.getItem(deleteId);
				ArrayList<ItemFacade> items = new ArrayList<>();
				items.add(itemfacade);
				qpoolbean.setItemsToDelete(items);
				qpoolbean.removeQuestionsFromPool();
				return;
			}
			else {
				// if some pools still reference to this item, ie, this item is 
				// created in assessment but also assigned a a pool
				// then just set section = null
				itemf.setSection(null);
				delegate.saveItem(itemf);
			}
		}
		//An item has been deleted
		EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_ITEM_DELETE, "/sam/" +AgentFacade.getCurrentSiteId() + "/removed itemId=" + deleteId, true));
		AssessmentService assessdelegate = new AssessmentService();

		// reorder item numbers
		SectionFacade sectfacade = assessdelegate.getSection(currSection.getSectionId().toString());
		Set<ItemFacade> itemset = sectfacade.getItemFacadeSet();
		// should be size-1 now.
		for (ItemFacade itemfacade : itemset) {
			Integer itemfacadeseq = itemfacade.getSequence();
			if (itemfacadeseq.compareTo(currSeq) > 0 ){
				itemfacade.setSequence( Integer.valueOf(itemfacadeseq.intValue()-1) );
				delegate.saveItem(itemfacade);
			}
		}
		// this will ensure that in case the same bean is re-POST, it won't be in scope
		item.setItemToDelete(null);
		//  go to editAssessment.jsp, need to first reset assessmentBean
		AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean(
				"assessmentBean");
		AssessmentFacade assessment = assessdelegate.getAssessment(assessmentBean.getAssessmentId());
		assessmentBean.setAssessment(assessment);
		assessdelegate.updateAssessmentLastModifiedInfo(assessment);
		//Assessment has been revised
		EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_REVISE, "/sam/" +AgentFacade.getCurrentSiteId() + "/removed itemId=" + deleteId + "from assessmentId=" + assessmentBean.getAssessmentId(), true));
	}

}