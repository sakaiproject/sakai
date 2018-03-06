/**
 * Copyright (c) 2005-2016 The Apereo Foundation
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.sakaiproject.tool.assessment.data.dao.assessment.ItemData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.facade.PublishedSectionFacade;
import org.sakaiproject.tool.assessment.facade.SectionFacade;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.ItemContentsBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.SectionContentsBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

public class UpdateAssessmentQuestionsOrder implements ActionListener {

	ItemService delegate;
	AssessmentService assessdelegate;
	PublishedAssessmentService publisheddelegate;

	int controlP = 1;
	int controlQ = 1;

	public void processAction(ActionEvent ae) throws AbortProcessingException {

		FacesContext context = FacesContext.getCurrentInstance();

		AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
		delegate = new ItemService();

		boolean isEditPendingAssessmentFlow = author
				.getIsEditPendingAssessmentFlow();

		AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean("assessmentBean");


		boolean published = isEditPendingAssessmentFlow ? false : true;

		if(published) {
			publisheddelegate = new PublishedAssessmentService();
		} else {
			assessdelegate = new AssessmentService();
		}

		if(reorderItems(published)){
			String err=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages", "edit_order_warning");
			context.addMessage(null,new FacesMessage(err));
		}

		assessmentBean.setAssessment(published ? publisheddelegate.getAssessment(assessmentBean.getAssessmentId())
				: assessdelegate.getAssessment(assessmentBean.getAssessmentId()));
	}

	public boolean reorderItems(boolean published) {
		AssessmentBean assessmentBean = (AssessmentBean) ContextUtil
				.lookupBean("assessmentBean");

		Map<Integer,SectionFacade> sections = new HashMap<>();
		Map<Integer,Long> changedSections = new HashMap<>();
		int sectionsNum = assessmentBean.getSections().size();
		boolean duplicated = false;

		//first loop, update parts changed by the instructor
		for(SectionContentsBean curSection : assessmentBean.getSections()) {
			
			SectionFacade sectFc = 	published ? 
						(PublishedSectionFacade) publisheddelegate.getSection(curSection.getSectionId()) :
						(SectionFacade) assessdelegate.getSection(curSection.getSectionId());

			Integer sectionNumber = Integer.valueOf(curSection.getNumber());
			if(sectionNumber != sectFc.getSequence()) {
				if(changedSections.get(sectionNumber) != null){
					//there are several parts with the same new section number, change only the first
					duplicated = true;
					sections.put(sectFc.getSequence(), sectFc);
				} else {
					//position has changed, prioritize this value
					sectFc.setSequence(sectionNumber);
					changedSections.put(sectionNumber, sectFc.getSectionId());
					if(published) { 
						publisheddelegate.saveOrUpdateSection(sectFc);
					} else {
						assessdelegate.saveOrUpdateSection(sectFc);
					}
				}
			} else {
				//position not changed, adding to aux map
				sections.put(sectFc.getSequence(), sectFc);
			}
		}

		//second loop, fill the gaps
		IntStream.range(1, sectionsNum+1).forEach(
			nbr -> {
				if(changedSections.get(nbr) == null){
					SectionFacade sectFc = null;
					for(int i = controlP; i <= sectionsNum; i++){
						if(sections.get(i) != null){
							//taking first section left from the not changed map
							sectFc = sections.get(i);
							sectFc.setSequence(nbr);
							controlP = i+1;
							if(published) {
								publisheddelegate.saveOrUpdateSection(sectFc);
							} else {
								assessdelegate.saveOrUpdateSection(sectFc);
							}
							break;
						}
					}
				}
			}
		);

		sections.clear();
		changedSections.clear();
		boolean duplicatedQ = updateItemsOrder(assessmentBean);
		return (duplicated) ? duplicated : duplicatedQ;
	}
	
	private boolean updateItemsOrder(AssessmentBean assessmentbean) {
		
		boolean duplicated = false;
		for(SectionContentsBean curSection : assessmentbean.getSections()) {
			
			int numberOfItems = curSection.getQuestions();
			controlQ = 1;
			Map<Integer,Long> changedItems = new HashMap<>();
			Map<Integer,ItemFacade> sectionItems = new HashMap<>();
			
			for(ItemContentsBean icb : (List<ItemContentsBean>)curSection.getItemContents()) {
				ItemData curItem = (ItemData) (icb).getItemData();
				ItemFacade itemFc = delegate.getItem(String.valueOf(curItem.getItemId()));

				Integer curSequence = curItem.getSequence();
				if(curSequence != itemFc.getData().getSequence()) {
					if(changedItems.get(curSequence) != null){
						//there are several items with the same new section number, change only the first
						duplicated = true;
						sectionItems.put(itemFc.getSequence(), itemFc);
					} else {
						//position has changed, prioritize this value
						itemFc.getData().setSequence(curSequence);
						itemFc.setSequence(curSequence);
						changedItems.put(curSequence, itemFc.getItemId());
						delegate.saveItem(itemFc);
					}
				} else {
					//position not changed, adding to aux map
					sectionItems.put(itemFc.getSequence(), itemFc);
				}
			}

			//second loop, fill the gaps
			IntStream.range(1, numberOfItems+1).forEach(
				nbr -> {
					if(changedItems.get(nbr) == null){
						ItemFacade itemFc = null;
						for(int i = controlQ; i <= numberOfItems; i++){
							if(sectionItems.get(i) != null){
								//taking first section left from the not changed map
								itemFc = sectionItems.get(i);
								itemFc.setSequence(nbr);
								controlQ = i+1;
								delegate.saveItem(itemFc);
								break;
							}
						}
					}
				}
			);

			changedItems.clear();
			sectionItems.clear();
		}
		return duplicated;
	}
}