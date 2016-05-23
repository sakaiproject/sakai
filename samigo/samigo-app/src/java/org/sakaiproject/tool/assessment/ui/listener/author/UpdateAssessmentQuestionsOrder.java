package org.sakaiproject.tool.assessment.ui.listener.author;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.sakaiproject.tool.assessment.data.dao.assessment.ItemData;
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
		this.delegate = new ItemService();
		
		boolean isEditPendingAssessmentFlow = author
				.getIsEditPendingAssessmentFlow();

		AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean("assessmentBean");
		
		// get sections with oldPos
		if (isEditPendingAssessmentFlow) {
			reorderWorkingCopy();
			assessmentBean.setAssessment(assessdelegate.getAssessment(assessmentBean.getAssessmentId()));
		} else {
			reorderPublishedCopy();
			assessmentBean.setAssessment(publisheddelegate.getAssessment(assessmentBean.getAssessmentId()));
		}
	}

	public void reorderWorkingCopy() {
		
		assessdelegate = new AssessmentService();
		AssessmentBean assessmentBean = (AssessmentBean) ContextUtil
				.lookupBean("assessmentBean");
		
		Map<Integer,Integer> swapSections = new HashMap<Integer,Integer>();
		Map<Integer,SectionFacade> sections = new HashMap<Integer,SectionFacade>();
		
		Iterator secsIt = assessmentBean.getSections().iterator();
		while(secsIt.hasNext()) {
			
			SectionContentsBean curSection = (SectionContentsBean) secsIt.next();
			SectionFacade sectFc = (SectionFacade) assessdelegate.getSection(String.valueOf(curSection.getSectionId()));
			
			/* Handle sections swaps */
			boolean dirtySection = false;
			
			if(Integer.valueOf(curSection.getNumber()) != sectFc.getSequence()) {
				// there was a swap
				if(sections.containsKey(Integer.valueOf(curSection.getNumber()))) {
					SectionFacade sec = sections.get(Integer.valueOf(curSection.getNumber()));
					sec.setSequence(sectFc.getSequence());
					swapSections.put(sec.getSequence(), sectFc.getSequence());
					assessdelegate.saveOrUpdateSection(sec);
				}
				int val = Integer.valueOf(curSection.getNumber());
				swapSections.put(val,sectFc.getSequence());
				sectFc.setSequence(val);
				dirtySection = true;
			} else if (swapSections.containsKey(Integer.valueOf(curSection.getNumber()))) {
				// previous swap, change values
				int val = (Integer) swapSections.get(Integer.valueOf(curSection.getNumber()));
				swapSections.put(sectFc.getSequence(),val);
				sectFc.setSequence(val);
				dirtySection = true;
				
			}
			if(dirtySection) {
				assessdelegate.saveOrUpdateSection(sectFc);
			}
			sections.put(sectFc.getSequence(), sectFc);
			/* End Handle Section */
			
		}
		sections.clear();
		swapSections.clear();
		
		/* Sort Items */
		secsIt = assessmentBean.getSections().iterator();
		updateItemsOrder(secsIt);
		
		
	}

	public void reorderPublishedCopy() {
		this.publisheddelegate = new PublishedAssessmentService();
		
		AssessmentBean assessmentBean = (AssessmentBean) ContextUtil
				.lookupBean("assessmentBean");
		
		Map<Integer,Integer> swapSections = new HashMap<Integer,Integer>();
		Map<Integer,PublishedSectionFacade> sections = new HashMap<Integer,PublishedSectionFacade>();
		
		Iterator secsIt = assessmentBean.getSections().iterator();
		while(secsIt.hasNext()) {
			
			SectionContentsBean curSection = (SectionContentsBean) secsIt.next();
			PublishedSectionFacade sectFc = (PublishedSectionFacade) publisheddelegate.getSection(String.valueOf(curSection.getSectionId()));
			
			/* Handle sections swaps */
			boolean dirtySection = false;
			
			if(Integer.valueOf(curSection.getNumber()) != sectFc.getSequence()) {
				// there was a swap
				if(sections.containsKey(Integer.valueOf(curSection.getNumber()))) {
					PublishedSectionFacade sec = sections.get(Integer.valueOf(curSection.getNumber()));
					sec.setSequence(sectFc.getSequence());
					swapSections.put(sec.getSequence(), sectFc.getSequence());
					publisheddelegate.saveOrUpdateSection(sec);
				}
				int val = Integer.valueOf(curSection.getNumber());
				swapSections.put(val,sectFc.getSequence());
				sectFc.setSequence(val);
				dirtySection = true;
			} else if (swapSections.containsKey(Integer.valueOf(curSection.getNumber()))) {
				// previous swap, change values
				int val = (Integer) swapSections.get(Integer.valueOf(curSection.getNumber()));
				swapSections.put(sectFc.getSequence(),val);
				sectFc.setSequence(val);
				dirtySection = true;
				
			}
			if(dirtySection) {
				publisheddelegate.saveOrUpdateSection(sectFc);
			}
			sections.put(sectFc.getSequence(), sectFc);
			/* End Handle Section */
			
		}
		sections.clear();
		swapSections.clear();
		
		/* Sort Items */
		secsIt = assessmentBean.getSections().iterator();
		updateItemsOrder(secsIt);
		
	}
	
	private void updateItemsOrder(Iterator secsIt) {
		while(secsIt.hasNext()) {
			
			SectionContentsBean curSection = (SectionContentsBean) secsIt.next();
			int numberOfItems = curSection.getQuestions();
			
			Map<Integer,Integer> swapItems = new HashMap<Integer,Integer>();
			Map<Integer,ItemFacade> sectionItems = new HashMap<Integer,ItemFacade>();
			
			Iterator itemsIt = curSection.getItemContents().iterator();
			while(itemsIt.hasNext()) {
				
				boolean dirty = false;
				ItemData curItem = (ItemData) ((ItemContentsBean) itemsIt.next()).getItemData();
				ItemFacade itemFc = this.delegate.getItem(curItem.getItemId(), AgentFacade.getAgentString());
				if(curItem.getSequence() != itemFc.getData().getSequence())  {
					swapItems.put(curItem.getSequence(), itemFc.getData().getSequence());
					itemFc.getData().setSequence(curItem.getSequence());
					itemFc.setSequence(curItem.getSequence());
					dirty = true;
					// deal retroactively	
					if(sectionItems.containsKey(curItem.getSequence())) {	// change already found items
						ItemFacade it = sectionItems.get(curItem.getSequence());
						int val = itemFc.getSequence();
						// if the position was taken by any other value, swap it for the previous change, prioritize latest change
						int control = 0;
						// cascade through items until we find an empty one
						if(swapItems.containsKey(val)) {
							do {
								val = swapItems.get(val);
								control++;
							} while(swapItems.containsKey(val) && control <= numberOfItems);
						}
						it.setSequence(val);
						it.getData().setSequence(val);
						swapItems.put(val, curItem.getSequence()); // replace latest
						sectionItems.put(it.getData().getSequence(), it);
						delegate.saveItem(it);
					}
				} else if (swapItems.containsKey(curItem.getSequence())) {
					int val = swapItems.get(curItem.getSequence());
					// do not duplicate, cascade through until and empty one has been found
					int control = 0;
					if(swapItems.containsKey(val)) {
						// loop until no items was swapped with that number
						do {
							val = swapItems.get(val);
							control++;
						} while(swapItems.containsKey(val) && control <= numberOfItems);
					}
					swapItems.put(val, itemFc.getSequence());
					itemFc.setSequence(val);
					dirty = true;
				}
				if(dirty) { 
					this.delegate.saveItem(itemFc);
				}
				sectionItems.put(itemFc.getSequence(), itemFc);
			}
			swapItems.clear();
			sectionItems.clear();
		}
	}

}