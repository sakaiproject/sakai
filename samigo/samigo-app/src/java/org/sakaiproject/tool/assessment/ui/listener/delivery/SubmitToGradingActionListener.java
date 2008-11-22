/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tool.assessment.ui.listener.delivery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedSectionData;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.ItemGradingIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.GradebookServiceException;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.ItemContentsBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.SectionContentsBean;
import org.sakaiproject.tool.assessment.ui.bean.shared.PersonBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>
 * Title: Samigo
 * </p>
 * <p>
 * Purpose: This listener is called in delivery when the user clicks on submit,
 * save, or previous, next, toc.... It calculates and saves scores in DB
 * 
 * @version $Id: SubmitToGradingActionListener.java 11634 2006-07-06 17:35:54Z
 *          daisyf@stanford.edu $
 */

public class SubmitToGradingActionListener implements ActionListener {
	private static Log log = LogFactory
			.getLog(SubmitToGradingActionListener.class);

	/**
	 * ACTION.
	 * 
	 * @param ae
	 * @throws AbortProcessingException
	 */
	public void processAction(ActionEvent ae) throws AbortProcessingException {
		try {
			log.debug("SubmitToGradingActionListener.processAction() ");
			
			// get managed bean
			DeliveryBean delivery = (DeliveryBean) ContextUtil
					.lookupBean("delivery");

			if ((ContextUtil.lookupParam("showfeedbacknow") != null
					&& "true"
							.equals(ContextUtil.lookupParam("showfeedbacknow")) || delivery
					.getActionMode() == DeliveryBean.PREVIEW_ASSESSMENT))
				delivery.setForGrade(false);

			// get service
			PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();

			// get assessment
			PublishedAssessmentFacade publishedAssessment = null;
			if (delivery.getPublishedAssessment() != null)
				publishedAssessment = delivery.getPublishedAssessment();
			else {
				publishedAssessment = publishedAssessmentService
						.getPublishedAssessment(delivery.getAssessmentId());
				delivery.setPublishedAssessment(publishedAssessment);
			}
			
			AssessmentGradingData adata = submitToGradingService(ae, publishedAssessment, delivery);
			// set AssessmentGrading in delivery
			delivery.setAssessmentGrading(adata);

			// set url & confirmation after saving the record for grade
			if (adata != null && delivery.getForGrade())
				setConfirmation(adata, publishedAssessment, delivery);

			if (isForGrade(adata) && !isUnlimited(publishedAssessment)) {
				delivery.setSubmissionsRemaining(delivery
						.getSubmissionsRemaining() - 1);
			}


		} catch (GradebookServiceException ge) {
			ge.printStackTrace();
			FacesContext context = FacesContext.getCurrentInstance();
			String err = (String) ContextUtil.getLocalizedString(
					"org.sakaiproject.tool.assessment.bundle.AuthorMessages",
					"gradebook_exception_error");
			context.addMessage(null, new FacesMessage(err));
			return;

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private boolean isForGrade(AssessmentGradingData aData) {
		if (aData != null)
			return (Boolean.TRUE).equals(aData.getForGrade());
		else
			return false;
	}

	private boolean isUnlimited(PublishedAssessmentFacade publishedAssessment) {
		return (Boolean.TRUE).equals(publishedAssessment
				.getAssessmentAccessControl().getUnlimitedSubmissions());
	}

	/**
	 * This method set the url & confirmation string for submitted.jsp. The
	 * confirmation string =
	 * assessmentGradingId-publishedAssessmentId-agentId-submitteddate
	 * 
	 * @param adata
	 * @param publishedAssessment
	 * @param delivery
	 */
	private void setConfirmation(AssessmentGradingData adata,
			PublishedAssessmentFacade publishedAssessment, DeliveryBean delivery) {
		if (publishedAssessment.getAssessmentAccessControl() != null) {
			setFinalPage(publishedAssessment, delivery);
			setSubmissionMessage(publishedAssessment, delivery);
		}
		setConfirmationId(adata, publishedAssessment, delivery);
	}

	/**
	 * Set confirmationId which is AssessmentGradingId-TimeStamp.
	 * 
	 * @param adata
	 * @param publishedAssessment
	 * @param delivery
	 */
	private void setConfirmationId(AssessmentGradingData adata,
			PublishedAssessmentFacade publishedAssessment, DeliveryBean delivery) {
		delivery.setConfirmation(adata.getAssessmentGradingId() + "-"
				+ publishedAssessment.getPublishedAssessmentId() + "-"
				+ adata.getAgentId() + "-"
				+ adata.getSubmittedDate().toString());
	}

	/**
	 * Set the submission message.
	 * 
	 * @param publishedAssessment
	 * @param delivery
	 */
	private void setSubmissionMessage(
			PublishedAssessmentFacade publishedAssessment, DeliveryBean delivery) {
		String submissionMessage = publishedAssessment
				.getAssessmentAccessControl().getSubmissionMessage();
		if (submissionMessage != null)
			delivery.setSubmissionMessage(submissionMessage);
	}

	/**
	 * Set finalPage url in delivery bean.
	 * 
	 * @param publishedAssessment
	 * @param delivery
	 */
	private void setFinalPage(PublishedAssessmentFacade publishedAssessment,
			DeliveryBean delivery) {
		String url = publishedAssessment.getAssessmentAccessControl()
				.getFinalPageUrl();
		if (url != null)
			url = url.trim();
		delivery.setUrl(url);
	}

	/**
	 * Invoke submission and return the grading data
	 * 
	 * @param publishedAssessment
	 * @param delivery
	 * @return
	 */
	private synchronized AssessmentGradingData submitToGradingService(
			ActionEvent ae, PublishedAssessmentFacade publishedAssessment, DeliveryBean delivery) {
		log.debug("****1a. inside submitToGradingService ");
		String submissionId = "";
		HashSet itemGradingHash = new HashSet();
		// daisyf decoding: get page contents contains SectionContentsBean, a
		// wrapper for SectionDataIfc
		Iterator iter = delivery.getPageContents().getPartsContents()
				.iterator();
		log.debug("****1b. inside submitToGradingService, iter= " + iter);
		HashSet adds = new HashSet();
		HashSet removes = new HashSet();

		// we go through all the answer collected from JSF form per each
		// publsihedItem and
		// work out which answer is an new addition and in cases like
		// MC/MCMR/Survey, we will
		// discard any existing one and just save teh new one. For other
		// question type, we
		// simply modify the publishedText or publishedAnswer of teh existing
		// ones.
		while (iter.hasNext()) {
			SectionContentsBean part = (SectionContentsBean) iter.next();
			log.debug("****1c. inside submitToGradingService, part " + part);
			Iterator iter2 = part.getItemContents().iterator();
			while (iter2.hasNext()) { // go through each item from form
				ItemContentsBean item = (ItemContentsBean) iter2.next();
				log.debug("****** before prepareItemGradingPerItem");
				prepareItemGradingPerItem(ae, delivery, item, adds, removes);
				log.debug("****** after prepareItemGradingPerItem");
			}
		}
		
		AssessmentGradingData adata = persistAssessmentGrading(ae, delivery,
				itemGradingHash, publishedAssessment, adds, removes);
		delivery.setSubmissionId(submissionId);
		delivery.setSubmissionTicket(submissionId);// is this the same thing?
		// hmmmm
		delivery.setSubmissionDate(new Date());
		delivery.setSubmitted(true);
		return adata;
	}

	private AssessmentGradingData persistAssessmentGrading(ActionEvent ae, 
			DeliveryBean delivery, HashSet itemGradingHash,
			PublishedAssessmentFacade publishedAssessment, HashSet adds,
			HashSet removes) {
		AssessmentGradingData adata = null;
		if (delivery.getAssessmentGrading() != null) {
			adata = delivery.getAssessmentGrading();
		}

		GradingService service = new GradingService();
		log.debug("**adata=" + adata);
		if (adata == null) { // <--- this shouldn't happened 'cos it should
			// have been created by BeginDelivery
			adata = makeNewAssessmentGrading(publishedAssessment, delivery,
					itemGradingHash);
			delivery.setAssessmentGrading(adata);
		} else {
			// 1. add all the new itemgrading for MC/Survey and discard any
			// itemgrading for MC/Survey
			// 2. add any modified SAQ/TF/FIB/Matching/MCMR/FIN
			// 3. save any modified Mark for Review in FileUplaod/Audio

			HashMap fibMap = getFIBMap(publishedAssessment);
			HashMap finMap = getFINMap(publishedAssessment);
			HashMap mcmrMap = getMCMRMap(publishedAssessment);
			Set itemGradingSet = adata.getItemGradingSet();
			log.debug("*** 2a. before removal & addition " + (new Date()));
			if (itemGradingSet != null) {
				log.debug("*** 2aa. removing old itemGrading " + (new Date()));
				itemGradingSet.removeAll(removes);
				service.deleteAll(removes);
				// refresh itemGradingSet & assessmentGrading after removal
				log.debug("*** 2ab. reload itemGradingSet " + (new Date()));
				itemGradingSet = service.getItemGradingSet(adata
						.getAssessmentGradingId().toString());
				log.debug("*** 2ac. load assessmentGarding " + (new Date()));
				adata = service.load(adata.getAssessmentGradingId().toString());

				Iterator iter = adds.iterator();
				while (iter.hasNext()) {
					((ItemGradingIfc) iter.next()).setAssessmentGradingId(adata
							.getAssessmentGradingId());
				}
				// make update to old item and insert new item
				// and we will only update item that has been changed
				log
						.debug("*** 2ad. set assessmentGrading with new/updated itemGrading "
								+ (new Date()));
				log
						.debug("Submitforgrading: before calling .....................oldItemGradingSet.size = "
								+ itemGradingSet.size());
				log.debug("Submitforgrading: newItemGradingSet.size = "
						+ adds.size());

				HashSet updateItemGradingSet = getUpdateItemGradingSet(
						itemGradingSet, adds, fibMap, finMap, mcmrMap, adata);
				adata.setItemGradingSet(updateItemGradingSet);
			}
		}

		adata.setIsLate(isLate(publishedAssessment));
		adata.setForGrade(Boolean.valueOf(delivery.getForGrade()));
		log.debug("*** 2b. before storingGrades, did all the removes and adds "
				+ (new Date()));
		
		if (delivery.getNavigation().equals("1") && ae != null && "showFeedback".equals(ae.getComponent().getId())) {
			log.debug("Do not persist to db if it is linear access and the action is show feedback");
			// 3. let's build three HashMap with (publishedItemId, publishedItem),
			// (publishedItemTextId, publishedItem), (publishedAnswerId,
			// publishedItem) to help with storing grades to adata only, not db
			HashMap publishedItemHash = delivery.getPublishedItemHash();
			HashMap publishedItemTextHash = delivery.getPublishedItemTextHash();
			HashMap publishedAnswerHash = delivery.getPublishedAnswerHash();
			service.storeGrades(adata, publishedAssessment, publishedItemHash, publishedItemTextHash, publishedAnswerHash, false);
		}
		else {
			log.debug("Persist to db otherwise");
			// The following line seems redundant. I cannot see a reason why we need to save the adata here
			// and then again in following service.storeGrades(). Comment it out.
			//service.saveOrUpdateAssessmentGrading(adata);
			log.debug("*** 3. before storingGrades, did all the removes and adds " + (new Date()));
			// 3. let's build three HashMap with (publishedItemId, publishedItem),
			// (publishedItemTextId, publishedItem), (publishedAnswerId,
			// publishedItem) to help with storing grades to adata and then persist to DB
			HashMap publishedItemHash = delivery.getPublishedItemHash();
			HashMap publishedItemTextHash = delivery.getPublishedItemTextHash();
			HashMap publishedAnswerHash = delivery.getPublishedAnswerHash();
			service.storeGrades(adata, publishedAssessment, publishedItemHash, publishedItemTextHash, publishedAnswerHash);
			log.debug("*** 4. after storingGrades, did all the removes and adds " + (new Date()));
		}
		return adata;
	}

	private HashMap getFIBMap(PublishedAssessmentIfc publishedAssessment) {
		PublishedAssessmentService s = new PublishedAssessmentService();
		return s.prepareFIBItemHash(publishedAssessment);
	}

  
  	private HashMap getFINMap(PublishedAssessmentIfc publishedAssessment){
	    PublishedAssessmentService s = new PublishedAssessmentService();
	    return s.prepareFINItemHash(publishedAssessment);
	}
  

	private HashMap getMCMRMap(PublishedAssessmentIfc publishedAssessment) {
		PublishedAssessmentService s = new PublishedAssessmentService();
		return s.prepareMCMRItemHash(publishedAssessment);
	}

	private HashSet getUpdateItemGradingSet(Set oldItemGradingSet,
			Set newItemGradingSet, HashMap fibMap, HashMap finMap, HashMap mcmrMap,
			AssessmentGradingData adata) {
		log.debug("Submitforgrading: oldItemGradingSet.size = "
				+ oldItemGradingSet.size());
		log.debug("Submitforgrading: newItemGradingSet.size = "
				+ newItemGradingSet.size());
		HashSet updateItemGradingSet = new HashSet();
		Iterator iter = oldItemGradingSet.iterator();
		HashMap map = new HashMap();
		while (iter.hasNext()) { // create a map with old itemGrading
			ItemGradingIfc item = (ItemGradingIfc) iter.next();
			map.put(item.getItemGradingId(), item);
		}

		// go through new itemGrading
		Iterator iter1 = newItemGradingSet.iterator();
		while (iter1.hasNext()) {
			ItemGradingIfc newItem = (ItemGradingIfc) iter1.next();
			ItemGradingIfc oldItem = (ItemGradingIfc) map.get(newItem
					.getItemGradingId());
			if (oldItem != null) {
				// itemGrading exists and value has been change, then need
				// update
				Boolean oldReview = oldItem.getReview();
				Boolean newReview = newItem.getReview();
				Long oldAnswerId = oldItem.getPublishedAnswerId();
				Long newAnswerId = newItem.getPublishedAnswerId();
				String oldRationale = oldItem.getRationale();
				String newRationale = ContextUtil.processFormattedText(log, newItem.getRationale());
				String oldAnswerText = oldItem.getAnswerText();
				String newAnswerText = ContextUtil.processFormattedText(log, newItem.getAnswerText());
				if ((oldReview != null && !oldReview.equals(newReview))
				    || (newReview!=null && !newReview.equals(oldReview))
						|| (oldAnswerId != null && !oldAnswerId
								.equals(newAnswerId))
						|| (newAnswerId != null && !newAnswerId
								.equals(oldAnswerId))
						|| (oldRationale != null && !oldRationale
								.equals(newRationale))
						|| (newRationale != null && !newRationale
								.equals(oldRationale))
						|| (oldAnswerText != null && !oldAnswerText
								.equals(newAnswerText))
						|| (newAnswerText != null && !newAnswerText
								.equals(oldAnswerText))
						|| fibMap.get(oldItem.getPublishedItemId()) != null
						|| finMap.get(oldItem.getPublishedItemId())!=null
						|| mcmrMap.get(oldItem.getPublishedItemId()) != null) {
					oldItem.setReview(newItem.getReview());
					oldItem.setPublishedAnswerId(newItem.getPublishedAnswerId());
					oldItem.setRationale(newRationale);
							
					oldItem.setAnswerText(newAnswerText);
					oldItem.setSubmittedDate(new Date());
					oldItem.setAutoScore(newItem.getAutoScore());
					oldItem.setOverrideScore(newItem.getOverrideScore());
					updateItemGradingSet.add(oldItem);
					// log.debug("**** SubmitToGrading: need update
					// "+oldItem.getItemGradingId());
				}
			} else { // itemGrading from new set doesn't exist, add to set in
				// this case
				// log.debug("**** SubmitToGrading: need add new item");
				newItem.setAgentId(adata.getAgentId());
				updateItemGradingSet.add(newItem);
			}
		}
		return updateItemGradingSet;
	}

	/**
	 * Make a new AssessmentGradingData object for delivery
	 * 
	 * @param publishedAssessment
	 *            the PublishedAssessmentFacade
	 * @param delivery
	 *            the DeliveryBean
	 * @param itemGradingHash
	 *            the item data
	 * @return
	 */
	private AssessmentGradingData makeNewAssessmentGrading(
			PublishedAssessmentFacade publishedAssessment,
			DeliveryBean delivery, HashSet itemGradingHash) {
		PersonBean person = (PersonBean) ContextUtil.lookupBean("person");
		AssessmentGradingData adata = new AssessmentGradingData();
		adata.setAgentId(person.getId());
		adata.setPublishedAssessmentId(publishedAssessment
				.getPublishedAssessmentId());
		adata.setForGrade(Boolean.valueOf(delivery.getForGrade()));
		adata.setItemGradingSet(itemGradingHash);
		adata.setAttemptDate(new Date());
		adata.setIsLate(Boolean.FALSE);
		adata.setStatus(new Integer(0));
		adata.setTotalOverrideScore(new Float(0));
		adata.setTimeElapsed(new Integer("0"));
		return adata;
	}

	/*
	 * This is specific to JSF - question for each type is layout differently in
	 * JSF and the answers submitted are being collected differently too. e.g.
	 * for each MC/Survey/MCMR, an itemgrading is associated with each choice.
	 * whereas there is only one itemgrading per each question for SAQ/TF/Audio,
	 * and one for ecah blank in FIB. To understand the logic in this method, it
	 * is best to study jsf/delivery/item/deliver*.jsp
	 */
	private void prepareItemGradingPerItem(ActionEvent ae, DeliveryBean delivery,
			ItemContentsBean item, HashSet adds, HashSet removes) {
		ArrayList grading = item.getItemGradingDataArray();
		int typeId = item.getItemData().getTypeId().intValue();
		// 1. add all the new itemgrading for MC/Survey and discard any
		// itemgrading for MC/Survey
		// 2. add any modified SAQ/TF/FIB/Matching/MCMR/Audio/FIN
		switch (typeId) {
		case 1: // MC
		case 12: // MC Single Selection
		case 3: // Survey
			boolean answerModified = false;
			for (int m = 0; m < grading.size(); m++) {
				ItemGradingData itemgrading = (ItemGradingData) grading.get(m);

				if (itemgrading.getItemGradingId() == null
						|| itemgrading.getItemGradingId().intValue() <= 0) { // =>
					// new answer
					if (itemgrading.getPublishedAnswerId() != null) { // null=>
						// skipping this  question
						answerModified = true;
						break;
					}
				}
			}


			if (answerModified) {
				for (int m = 0; m < grading.size(); m++) {
					ItemGradingData itemgrading = (ItemGradingData) grading
							.get(m);
					if (itemgrading.getItemGradingId() != null
							&& itemgrading.getItemGradingId().intValue() > 0) {
						// remove all old answer for MC & Surevy

						removes.add(itemgrading);
					} else {
						// add new answer
						if (itemgrading.getPublishedAnswerId() != null
								|| itemgrading.getAnswerText() != null) {
							// null=> skipping this question
							itemgrading.setAgentId(AgentFacade.getAgentString());
							itemgrading.setSubmittedDate(new Date());
							if (itemgrading.getRationale() != null && itemgrading.getRationale().length() > 0) {
								itemgrading.setRationale(ContextUtil.processFormattedText(log, itemgrading.getRationale()));
							}
							// the rest of the info is collected by
							// ItemContentsBean via JSF form
							adds.add(itemgrading);
						}
					}
				}
			}
                        else{
                          handleMarkForReview(grading, adds);
			}
			break;
		case 4: // T/F
		case 9: // Matching
			for (int m = 0; m < grading.size(); m++) {
				ItemGradingData itemgrading = (ItemGradingData) grading.get(m);
				itemgrading.setAgentId(AgentFacade.getAgentString());
				itemgrading.setSubmittedDate(new Date());
			}
			for (int m = 0; m < grading.size(); m++) {
				ItemGradingData itemgrading = (ItemGradingData) grading.get(m);
				if (itemgrading.getItemGradingId() != null
						&& itemgrading.getItemGradingId().intValue() > 0) {
					adds.addAll(grading);
					break;
				} else if (itemgrading.getPublishedAnswerId() != null
						|| itemgrading.getAnswerText() != null ) {
					if (itemgrading.getRationale() != null && itemgrading.getRationale().length() > 0) {
						itemgrading.setRationale(ContextUtil.processFormattedText(log, itemgrading.getRationale()));
					}
					adds.addAll(grading);
					break;
				}
			}
			break;
		case 5: // SAQ
			for (int m = 0; m < grading.size(); m++) {
				ItemGradingData itemgrading = (ItemGradingData) grading.get(m);
				itemgrading.setAgentId(AgentFacade.getAgentString());
				itemgrading.setSubmittedDate(new Date());
			}
			for (int m = 0; m < grading.size(); m++) {
				ItemGradingData itemgrading = (ItemGradingData) grading.get(m);
				if (itemgrading.getItemGradingId() != null
						&& itemgrading.getItemGradingId().intValue() > 0) {
					adds.addAll(grading);
					break;
				} else if (itemgrading.getAnswerText() != null && !itemgrading.getAnswerText().equals("")) {
					itemgrading.setAnswerText(ContextUtil.processFormattedText(log, itemgrading.getAnswerText()));
					adds.addAll(grading);
					break;
				}
			}
			break;			
		case 8: // FIB
		case 11: // FIN
			boolean addedToAdds = false;
			for (int m = 0; m < grading.size(); m++) {
				ItemGradingData itemgrading = (ItemGradingData) grading.get(m);
				itemgrading.setAgentId(AgentFacade.getAgentString());
				itemgrading.setSubmittedDate(new Date());
			}
			for (int m = 0; m < grading.size(); m++) {
				ItemGradingData itemgrading = (ItemGradingData) grading.get(m);
				if (itemgrading.getItemGradingId() != null
						&& itemgrading.getItemGradingId().intValue() > 0) {
					adds.addAll(grading);
					break;
				} else if (itemgrading.getAnswerText() != null && !itemgrading.getAnswerText().equals("")) {
					String s = itemgrading.getAnswerText();
					log.debug("s = " + s);
					itemgrading.setAnswerText(ContextUtil.processFormattedText(log, s));
					adds.addAll(grading);
					if (!addedToAdds) {
						adds.addAll(grading);
						addedToAdds = true;
					}
				}
			}
			break;
		case 2: // MCMR
			for (int m = 0; m < grading.size(); m++) {
				ItemGradingData itemgrading = (ItemGradingData) grading.get(m);
				if (itemgrading.getItemGradingId() != null
						&& itemgrading.getItemGradingId().intValue() > 0) {
					// old answer, check which one to keep, not keeping null  answer
					if (itemgrading.getPublishedAnswerId() != null) {
						itemgrading.setAgentId(AgentFacade.getAgentString());
						itemgrading.setSubmittedDate(new Date());
						adds.add(itemgrading);
					} else {
						removes.add(itemgrading);
					}
				} else if (itemgrading.getPublishedAnswerId() != null) { // new  addition  not accepting any new answer with null for MCMR
					itemgrading.setAgentId(AgentFacade.getAgentString());
					itemgrading.setSubmittedDate(new Date());
					adds.add(itemgrading);
				}
			}
			break;
		case 6: // File Upload
		case 7: // Audio
                        handleMarkForReview(grading, adds);
                        break;
		}
		// if it is linear access and there is not answer, we add an fake ItemGradingData
		String actionCommand = "";
		if (ae != null) {
			actionCommand = ae.getComponent().getId();
			log.debug("ae is not null, getActionCommand() = " + actionCommand);	
		}
		else {
			log.debug("ae is null");
		}
		
		if (delivery.getNavigation().equals("1") && adds.size() ==0 && !"showFeedback".equals(actionCommand)) {
			log.debug("enter here");
			Long assessmentGradingId = delivery.getAssessmentGrading().getAssessmentGradingId();
			Long publishedItemId = item.getItemData().getItemId();
			log.debug("assessmentGradingId = " + assessmentGradingId);
			log.debug("publishedItemId = " + publishedItemId);
			GradingService gradingService = new GradingService();
			// For File Upload question, if user clicks on "Upload", a ItemGradingData will be created. 
			// Therefore, when user clicks on "Next", we shouldn't create it again.
			// Same for Audio question, if user records anything, a ItemGradingData will be created.
			// We don't create it again when user clicks on "Next".
			if ((typeId == 6 || typeId == 7) && gradingService.getItemGradingData(assessmentGradingId.toString(), publishedItemId.toString()) != null ) {
				log.debug("File Upload or Audio! Do not create empty ItemGradingData if there exists one");
			}
			else {
				log.debug("Create a new (fake) ItemGradingData");
				ItemGradingData itemGrading = new ItemGradingData();
				itemGrading.setAssessmentGradingId(assessmentGradingId);
				itemGrading.setAgentId(AgentFacade.getAgentString());
				itemGrading.setPublishedItemId(publishedItemId);
				ItemService itemService = new ItemService();
				Long itemTextId = itemService.getItemTextId(publishedItemId);
				log.debug("itemTextId = " + itemTextId);
				itemGrading.setPublishedItemTextId(itemTextId);
				adds.add(itemGrading);
			}
		}
	}

    private void handleMarkForReview(ArrayList grading, HashSet adds){
      for (int m = 0; m < grading.size(); m++) {
        ItemGradingData itemgrading = (ItemGradingData) grading.get(m);
        if (itemgrading.getItemGradingId() != null 
            && itemgrading.getItemGradingId().intValue() > 0
            && itemgrading.getReview() != null)  {
            // we will save itemgarding even though answer was not modified 
            // 'cos mark for review may have been modified
          adds.add(itemgrading);
        }
      }
    }

   
	private Boolean isLate(PublishedAssessmentIfc pub) {
		AssessmentAccessControlIfc a = pub.getAssessmentAccessControl();
		if (a.getDueDate() != null && a.getDueDate().before(new Date()))
			return Boolean.TRUE;
		else
			return Boolean.FALSE;
	}


	/*
	 * We create an ItemGradingData when it is not yet created
	 */
	public void completeItemGradingData() {
		DeliveryBean delivery = (DeliveryBean) ContextUtil.lookupBean("delivery");
		completeItemGradingData(delivery.getAssessmentGrading());
	}

	/*
	 * We create an ItemGradingData when it is not yet created
	 */
	public void completeItemGradingData(AssessmentGradingData assessmentGradingData) {
		ArrayList answeredPublishedItemIdList = new ArrayList();
		GradingService gradingService = new GradingService();
		List itemGradingIds = gradingService.getItemGradingIds(assessmentGradingData.getAssessmentGradingId());
		Iterator iter = itemGradingIds.iterator();
		Long answeredPublishedItemId;
		while (iter.hasNext()) {
			answeredPublishedItemId = (Long) iter.next();
			log.debug("answeredPublishedItemId = " + answeredPublishedItemId);
			answeredPublishedItemIdList.add(answeredPublishedItemId);
		}

		PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
		HashSet sectionSet = publishedAssessmentService.getSectionSetForAssessment(assessmentGradingData.getPublishedAssessmentId());
		PublishedSectionData publishedSectionData;
		iter = sectionSet.iterator();
		while (iter.hasNext()) {
			ArrayList itemArrayList;
			Long publishedItemId;
			PublishedItemData publishedItemData;
			publishedSectionData = (PublishedSectionData) iter.next();
			log.debug("sectionId = " + publishedSectionData.getSectionId());
			String authorType = publishedSectionData.getSectionMetaDataByLabel(SectionDataIfc.AUTHOR_TYPE);
			if (authorType != null && authorType.equals(SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL.toString())) {
				log.debug("Random draw from questonpool");
				itemArrayList = publishedSectionData.getItemArray();
				long seed = (long) AgentFacade.getAgentString().hashCode();
				if (publishedSectionData.getSectionMetaDataByLabel(SectionDataIfc.RANDOMIZATION_TYPE) != null && publishedSectionData.getSectionMetaDataByLabel(SectionDataIfc.RANDOMIZATION_TYPE).equals(SectionDataIfc.PER_SUBMISSION)) {
					seed = (long) (assessmentGradingData.getAssessmentGradingId().toString() + "_" + publishedSectionData.getSectionId().toString()).hashCode();
				}

				Collections.shuffle(itemArrayList,  new Random(seed));

				Integer numberToBeDrawn = new Integer(0);
				if (publishedSectionData.getSectionMetaDataByLabel(SectionDataIfc.NUM_QUESTIONS_DRAWN) !=null ) {
					numberToBeDrawn= new Integer(publishedSectionData.getSectionMetaDataByLabel(SectionDataIfc.NUM_QUESTIONS_DRAWN));
				}

				int samplesize = numberToBeDrawn.intValue();
				for (int i=0; i < samplesize; i++){
					publishedItemData = (PublishedItemData) itemArrayList.get(i);
					publishedItemId = publishedItemData.getItemId();
					log.debug("publishedItemId = " + publishedItemId); 
					if (!answeredPublishedItemIdList.contains(publishedItemId)) {
						saveItemGradingData(assessmentGradingData, publishedItemId);
					}
				}
			}
			else {
				log.debug("Not random draw from questonpool");
				itemArrayList = publishedSectionData.getItemArray();
				Iterator itemIter = itemArrayList.iterator();
				while (itemIter.hasNext()) {
					publishedItemData = (PublishedItemData) itemIter.next();
					publishedItemId = publishedItemData.getItemId();
					log.debug("publishedItemId = " + publishedItemId);
					if (!answeredPublishedItemIdList.contains(publishedItemId)) {
						saveItemGradingData(assessmentGradingData, publishedItemId);
					}
				}
			}
		}
	}

	private void saveItemGradingData(AssessmentGradingData assessmentGradingData, Long publishedItemId) {
		log.debug("Adding one ItemGradingData...");
		ItemGradingData itemGradingData = new ItemGradingData();
		itemGradingData.setAssessmentGradingId(assessmentGradingData.getAssessmentGradingId());
		if (AgentFacade.getAgentString() == null || AgentFacade.getAgentString().equals("")) {
			itemGradingData.setAgentId(assessmentGradingData.getAgentId());
		}
		else {
			itemGradingData.setAgentId(AgentFacade.getAgentString());
		}
		itemGradingData.setPublishedItemId(publishedItemId);
		ItemService itemService = new ItemService();
		Long itemTextId = itemService.getItemTextId(publishedItemId);
		log.debug("itemTextId = " + itemTextId);
		itemGradingData.setPublishedItemTextId(itemTextId);
		GradingService gradingService = new GradingService();
		gradingService.saveItemGrading(itemGradingData);
	}
}
