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

	public void processAction(ActionEvent ae) throws AbortProcessingException {

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

		reorderItems(published);

		assessmentBean.setAssessment(published ? publisheddelegate.getAssessment(assessmentBean.getAssessmentId())
				: assessdelegate.getAssessment(assessmentBean.getAssessmentId()));
	}

	public void reorderItems(boolean published) {
		AssessmentBean assessmentBean = (AssessmentBean) ContextUtil
				.lookupBean("assessmentBean");
		
		Map<Integer,Integer> swapSections = new HashMap<>();
		Map<Integer,SectionFacade> sections = new HashMap<>();
		
		for(SectionContentsBean curSection : assessmentBean.getSections()) {
			
			SectionFacade sectFc = 	published ? 
						(PublishedSectionFacade) publisheddelegate.getSection(curSection.getSectionId()) :
						(SectionFacade) assessdelegate.getSection(curSection.getSectionId());
			boolean dirty = false;
			Integer sectionNumber = Integer.valueOf(curSection.getNumber());
			Integer secVal = swapSections.get(sectionNumber);
			if(sectionNumber != sectFc.getSequence()) {
				SectionFacade sec = sections.get(sectionNumber);
				if(sec != null) {
					sec.setSequence(sectFc.getSequence());
					swapSections.put(sec.getSequence(), sectFc.getSequence());
					if(published) { 
						publisheddelegate.saveOrUpdateSection(sec);
					} else {
						assessdelegate.saveOrUpdateSection(sec);
					}
				}
				int val = sectionNumber;
				swapSections.put(val,sectFc.getSequence());
				sectFc.setSequence(val);
				dirty = true;
			} else if (secVal != null) {
				// previous swap, change values
				swapSections.put(sectFc.getSequence(),secVal);
				sectFc.setSequence(secVal);
				dirty = true;
			}
			if(dirty) {
				if(published) { 
					publisheddelegate.saveOrUpdateSection(sectFc);
				} else {
					assessdelegate.saveOrUpdateSection(sectFc);
				}
			}
			sections.put(sectFc.getSequence(), sectFc);
		}
		sections.clear();
		swapSections.clear();
		updateItemsOrder(assessmentBean);
	}
	
	private void updateItemsOrder(AssessmentBean assessmentbean) {
		
		for(SectionContentsBean curSection : assessmentbean.getSections()) {
			
			int numberOfItems = curSection.getQuestions();
			
			Map<Integer,Integer> swapItems = new HashMap<>();
			Map<Integer,ItemFacade> sectionItems = new HashMap<>();
			
			for(ItemContentsBean icb : (List<ItemContentsBean>)curSection.getItemContents()) {
				boolean dirty = false;
				ItemData curItem = (ItemData) (icb).getItemData();
				ItemFacade itemFc = delegate.getItem(String.valueOf(curItem.getItemId()));
				int curSequence = curItem.getSequence();
				Integer t = swapItems.get(curSequence);
				if(curSequence != itemFc.getData().getSequence())  {
					swapItems.put(curSequence, itemFc.getData().getSequence());
					itemFc.getData().setSequence(curSequence);
					itemFc.setSequence(curSequence);
					dirty = true;
					// deal retroactively	
					ItemFacade it = sectionItems.get(curSequence);
					// change already found items
					if(it != null) {	
						int val = itemFc.getSequence();
						// if the position was taken by any other value, swap it for the previous change, prioritize latest change
						int control = 0;
						// cascade through items until we find an empty one
						t = swapItems.get(val);
						if(t != null) {
							do {
								val = swapItems.get(val);
								control++;
							} while(swapItems.get(val) != null && control <= numberOfItems);
						}
						it.setSequence(val);
						swapItems.put(val, curSequence); // replace latest
						sectionItems.put(it.getSequence(), it);
						delegate.saveItem(it);
					}
				} else if (t != null) {
					int val = swapItems.get(curSequence);
					// do not duplicate, cascade through until and empty one has been found
					int control = 0;
					t = swapItems.get(val);
					if(t != null) {
						// loop until no items was swapped with that number
						do {
							val = swapItems.get(val);
							control++;
						} while(swapItems.get(val) != null && control <= numberOfItems);
					}
					swapItems.put(val, itemFc.getSequence());
					itemFc.setSequence(val);
					dirty = true;
				}
				if(dirty) { 
					delegate.saveItem(itemFc);
				}
				sectionItems.put(itemFc.getSequence(), itemFc);
			}
			swapItems.clear();
			sectionItems.clear();
		}
	}
}