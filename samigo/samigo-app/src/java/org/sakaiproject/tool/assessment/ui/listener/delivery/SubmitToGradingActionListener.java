/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.FinFormatException;
import org.sakaiproject.tool.assessment.services.GradebookServiceException;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.services.SaLengthException;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.FinBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.ItemContentsBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.SectionContentsBean;
import org.sakaiproject.tool.assessment.ui.bean.shared.PersonBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.ExtendedTimeDeliveryService;
import org.sakaiproject.tool.assessment.util.SamigoLRSStatements;
import org.sakaiproject.tool.assessment.util.TextFormat;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.UserDirectoryService;

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
@Slf4j
public class SubmitToGradingActionListener implements ActionListener {
    private final EventTrackingService eventTrackingService= ComponentManager.get( EventTrackingService.class );

	
	/**
	 * The publishedAssesmentService
	 */
	private final PublishedAssessmentService publishedAssesmentService = new PublishedAssessmentService();

	private final PreferencesService preferencesService = ComponentManager.get( PreferencesService.class );
	private final UserDirectoryService userDirectoryService = ComponentManager.get( UserDirectoryService.class );

	/**
	 * ACTION.
	 * 
	 * @param ae
	 * @throws AbortProcessingException
	 */
	public void processAction(ActionEvent ae) throws AbortProcessingException, FinFormatException, SaLengthException {
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

			
			// get assessment
			PublishedAssessmentFacade publishedAssessment;
			if (delivery.getPublishedAssessment() != null)
				publishedAssessment = delivery.getPublishedAssessment();
			else {
				publishedAssessment = publishedAssesmentService
						.getPublishedAssessment(delivery.getAssessmentId());
				delivery.setPublishedAssessment(publishedAssessment);
			}
			
			Map invalidFINMap = new HashMap();
			List invalidSALengthList = new ArrayList();
			AssessmentGradingData adata = submitToGradingService(ae, publishedAssessment, delivery, invalidFINMap, invalidSALengthList);
			// set AssessmentGrading in delivery
			delivery.setAssessmentGrading(adata);
            if (adata.getForGrade()) {
                Event event = eventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_AUTO_GRADED, adata.getPublishedAssessmentTitle(), null, true, NotificationService.NOTI_OPTIONAL, SamigoLRSStatements.getStatementForGradedAssessment(adata, publishedAssessment));
                eventTrackingService.post(event);
            }
			// set url & confirmation after saving the record for grade
			if (delivery.getForGrade())
			{
				setConfirmation(adata, publishedAssessment, delivery);
				setReceiptEmailSetting( delivery );
			}

			if (isForGrade(adata) && !isUnlimited(publishedAssessment)) {
				delivery.setSubmissionsRemaining(delivery
						.getSubmissionsRemaining() - 1);
			}

			if (!invalidFINMap.isEmpty()) {
				delivery.setIsAnyInvalidFinInput(true);
				throw new FinFormatException ("Not a valid FIN input");
			}
			
			if (!invalidSALengthList.isEmpty()) {
				delivery.setIsAnyInvalidFinInput(true);
				throw new SaLengthException ("Short Answer input is too long");
			}
			
			delivery.setIsAnyInvalidFinInput(false);

		} catch (GradebookServiceException ge) {
			log.warn(ge.getMessage(), ge);
			FacesContext context = FacesContext.getCurrentInstance();
			String err = (String) ContextUtil.getLocalizedString(
					"org.sakaiproject.tool.assessment.bundle.AuthorMessages",
					"gradebook_exception_error");
			context.addMessage(null, new FacesMessage(err));
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
	 * This method sets the submitting user's receipt email setting from Preferences.
	 * It will either be, 'None', 'Digest', or 'Immediate'.
	 * 
	 * @param delivery 
	 */
	private void setReceiptEmailSetting( DeliveryBean delivery )
	{
		int submitterEmailReceiptPref = SamigoConstants.NOTI_PREF_DEFAULT;
		Preferences submitterPrefs = preferencesService.getPreferences( userDirectoryService.getCurrentUser().getId() );
		ResourceProperties props = submitterPrefs.getProperties( NotificationService.PREFS_TYPE + SamigoConstants.NOTI_PREFS_TYPE_SAMIGO );
		try
		{
			submitterEmailReceiptPref = (int) props.getLongProperty( "2" );
		}
		catch( EntityPropertyNotDefinedException | EntityPropertyTypeException ex ) { /* User hasn't changed preference */ }

		switch( submitterEmailReceiptPref )
		{
			case NotificationService.PREF_IGNORE:
			{
				delivery.setReceiptEmailSetting( (String) ContextUtil.getLocalizedString( 
						"org.sakaiproject.tool.assessment.bundle.DeliveryMessages", "receiptEmail_none") );
				break;
			}
			case NotificationService.PREF_DIGEST:
			{
				delivery.setReceiptEmailSetting( (String) ContextUtil.getLocalizedString( 
						"org.sakaiproject.tool.assessment.bundle.DeliveryMessages", "receiptEmail_digest") );
				break;
			}
			case NotificationService.PREF_IMMEDIATE:
			{
				delivery.setReceiptEmailSetting( (String) ContextUtil.getLocalizedString( 
						"org.sakaiproject.tool.assessment.bundle.DeliveryMessages", "receiptEmail_immediate") );
				break;
			}
		}
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
			ActionEvent ae, PublishedAssessmentFacade publishedAssessment, DeliveryBean delivery, Map invalidFINMap, List invalidSALengthList) throws FinFormatException {
		log.debug("****1a. inside submitToGradingService ");
		String submissionId = "";
		HashSet<ItemGradingData> itemGradingHash = new HashSet<>();
		// daisyf decoding: get page contents contains SectionContentsBean, a
		// wrapper for SectionDataIfc
		Iterator<SectionContentsBean> iter = delivery.getPageContents().getPartsContents()
				.iterator();
		log.debug("****1b. inside submitToGradingService, iter= " + iter);
		HashSet<ItemGradingData> adds = new HashSet<>();
		HashSet<ItemGradingData> removes = new HashSet<>();

		// we go through all the answer collected from JSF form per each
		// publsihedItem and
		// work out which answer is an new addition and in cases like
		// MC/MCMR/Survey, we will
		// discard any existing one and just save the new one. For other
		// question type, we
		// simply modify the publishedText or publishedAnswer of the existing
		// ones.
		while (iter.hasNext()) {
			SectionContentsBean part = iter.next();
			log.debug("****1c. inside submitToGradingService, part " + part);
			Iterator<ItemContentsBean> iter2 = part.getItemContents().iterator();
			while (iter2.hasNext()) { // go through each item from form
				ItemContentsBean item = iter2.next();
				log.debug("****** before prepareItemGradingPerItem");
				prepareItemGradingPerItem(ae, delivery, item, adds, removes);
				log.debug("****** after prepareItemGradingPerItem");
			}
		}
		
		AssessmentGradingData adata = persistAssessmentGrading(ae, delivery,
				itemGradingHash, publishedAssessment, adds, removes, invalidFINMap, invalidSALengthList);

		
		StringBuilder redrawAnchorName = new StringBuilder("p");
		String tmpAnchorName = "";

		Iterator<SectionContentsBean> iterPart = delivery.getPageContents().getPartsContents().iterator();
		while (iterPart.hasNext()) {
			SectionContentsBean part = iterPart.next();
			String partSeq = part.getNumber();
			Iterator<ItemContentsBean> iterItem = part.getItemContents().iterator();
			while (iterItem.hasNext()) { // go through each item from form
				ItemContentsBean item = iterItem.next();
				String itemSeq = item.getSequence();
				Long itemId = item.getItemData().getItemId();
				if (item.getItemData().getTypeId() == 5) {
					if (invalidSALengthList.contains(itemId)) {
						item.setIsInvalidSALengthInput(true);
						redrawAnchorName.append(partSeq);
						redrawAnchorName.append("q");
						redrawAnchorName.append(itemSeq);
						if (tmpAnchorName.equals("") || tmpAnchorName.compareToIgnoreCase(redrawAnchorName.toString()) > 0) {
							tmpAnchorName = redrawAnchorName.toString();
						}
					}
					else {
						item.setIsInvalidSALengthInput(false);
					}
				}
				else if (item.getItemData().getTypeId() == 11) {
					if (invalidFINMap.containsKey(itemId)) {
						item.setIsInvalidFinInput(true);
						redrawAnchorName.append(partSeq);
						redrawAnchorName.append("q");
						redrawAnchorName.append(itemSeq);
						if (tmpAnchorName.equals("") || tmpAnchorName.compareToIgnoreCase(redrawAnchorName.toString()) > 0) {
							tmpAnchorName = redrawAnchorName.toString();
						}
						List list = (List) invalidFINMap.get(itemId);
						List<FinBean> finArray = item.getFinArray();
						Iterator<FinBean> iterFin = finArray.iterator();
						while (iterFin.hasNext()) {
							FinBean finBean = iterFin.next();
							if (finBean.getItemGradingData() != null) {
								Long itemGradingId = finBean.getItemGradingData().getItemGradingId();
								if (list.contains(itemGradingId)) {
									finBean.setIsCorrect(Boolean.FALSE);
								}
							}
						}
					}
					else {
						item.setIsInvalidFinInput(false);
					}
				}
			}
		}
		
		
		if (tmpAnchorName != null && !tmpAnchorName.equals("")) {
			delivery.setRedrawAnchorName(tmpAnchorName);
		}
		else {
			delivery.setRedrawAnchorName("");
		}
		
		delivery.setSubmissionId(submissionId);
		delivery.setSubmissionTicket(submissionId);// is this the same thing?
		// hmmmm
		delivery.setSubmissionDate(new Date());
		delivery.setSubmitted(true);
		return adata;
	}

	private AssessmentGradingData persistAssessmentGrading(ActionEvent ae,
			DeliveryBean delivery, HashSet<ItemGradingData> itemGradingHash,
			PublishedAssessmentFacade publishedAssessment, HashSet<ItemGradingData> adds,
			HashSet<ItemGradingData> removes, Map invalidFINMap, List invalidSALengthList) throws FinFormatException {
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

			Map<Long, ItemDataIfc> calcQuestionMap = getCalcQuestionMap(publishedAssessment); // CALCULATED_QUESTION
			Map<Long, ItemDataIfc> imagQuestionMap = getImagQuestionMap(publishedAssessment); // IMAGEMAP_QUESTION
			Map<Long, ItemDataIfc> emiMap = getEMIMap(publishedAssessment);
			Set<ItemGradingData> itemGradingSet = adata.getItemGradingSet();
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
				adata = service.load(adata.getAssessmentGradingId().toString(), false);

				Iterator<ItemGradingData> iter = adds.iterator();
				while (iter.hasNext()) {
					iter.next().setAssessmentGradingId(adata
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

				HashSet<ItemGradingData> updateItemGradingSet = getUpdateItemGradingSet(
						itemGradingSet, adds, calcQuestionMap,imagQuestionMap, emiMap, adata);
				adata.setItemGradingSet(updateItemGradingSet);
			}
		}
		
		adata.setSubmitFromTimeoutPopup(delivery.getsubmitFromTimeoutPopup());
		adata.setIsLate(isLate(publishedAssessment, delivery.getsubmitFromTimeoutPopup(), adata.getAgentId()));
		adata.setForGrade(delivery.getForGrade());
		
		// If this assessment grading data has been updated (comments or adj. score) by grader and then republic and allow student to resubmit
		// when the student submit his answers, we update the status back to 0 and remove the grading entry/info.
		if (AssessmentGradingData.ASSESSMENT_UPDATED_NEED_RESUBMIT.equals(adata.getStatus()) || AssessmentGradingData.ASSESSMENT_UPDATED.equals(adata.getStatus())) {
			adata.setStatus(0);
			adata.setGradedBy(null);
			adata.setGradedDate(null);
			adata.setComments(null);
			adata.setTotalOverrideScore(0d);
		}
	
		log.debug("*** 2b. before storingGrades, did all the removes and adds "
				+ (new Date()));
		
		if (delivery.getNavigation().equals("1") && ae != null && "showFeedback".equals(ae.getComponent().getId())) {
			log.debug("Do not persist to db if it is linear access and the action is show feedback");
			// 3. let's build three HashMap with (publishedItemId, publishedItem),
			// (publishedItemTextId, publishedItem), (publishedAnswerId,
			// publishedItem) to help with storing grades to adata only, not db
			Map publishedItemHash = delivery.getPublishedItemHash();
			Map publishedItemTextHash = delivery.getPublishedItemTextHash();
			Map publishedAnswerHash = delivery.getPublishedAnswerHash();
			service.storeGrades(adata, publishedAssessment, publishedItemHash, publishedItemTextHash, publishedAnswerHash, false, invalidFINMap, invalidSALengthList);
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
			Map publishedItemHash = delivery.getPublishedItemHash();
			Map publishedItemTextHash = delivery.getPublishedItemTextHash();
			Map publishedAnswerHash = delivery.getPublishedAnswerHash();
			service.storeGrades(adata, publishedAssessment, publishedItemHash, publishedItemTextHash, publishedAnswerHash, invalidFINMap, invalidSALengthList);
		}
		return adata;
	}

  	/**
  	 * CALCULATED_QUESTION
  	 * @param publishedAssessment
  	 * @return map of calc items
  	 */
  	private Map<Long, ItemDataIfc> getCalcQuestionMap(PublishedAssessmentIfc publishedAssessment){
	    return (Map<Long, ItemDataIfc>) publishedAssesmentService.prepareCalcQuestionItemHash(publishedAssessment);
	}
  
  	/**
  	 * IMAGEMAP_QUESTION
  	 * @param publishedAssessment
  	 * @return map of image items
  	 */
  	private Map<Long, ItemDataIfc> getImagQuestionMap(PublishedAssessmentIfc publishedAssessment){
	    return (Map<Long, ItemDataIfc>) publishedAssesmentService.prepareImagQuestionItemHash(publishedAssessment);
	}

	private Map<Long, ItemDataIfc> getEMIMap(PublishedAssessmentIfc publishedAssessment) {
		PublishedAssessmentService s = new PublishedAssessmentService();
		return s.prepareEMIItemHash(publishedAssessment);
	}

	private HashSet<ItemGradingData> getUpdateItemGradingSet(Set oldItemGradingSet, Set<ItemGradingData> newItemGradingSet,
															 Map<Long, ItemDataIfc> calcQuestionMap, Map<Long, ItemDataIfc> imagQuestionMap,
															 Map<Long, ItemDataIfc> emiMap, AssessmentGradingData adata) {
		log.debug("Submitforgrading: oldItemGradingSet.size = "
				+ oldItemGradingSet.size());
		log.debug("Submitforgrading: newItemGradingSet.size = "
				+ newItemGradingSet.size());
		HashSet<ItemGradingData> updateItemGradingSet = new HashSet<>();
		Iterator iter = oldItemGradingSet.iterator();
		Map<Long, ItemGradingData> map = new HashMap<>();
		while (iter.hasNext()) { // create a map with old itemGrading
			ItemGradingData item = (ItemGradingData) iter.next();
			map.put(item.getItemGradingId(), item);
		}

		// go through new itemGrading
		Iterator<ItemGradingData> iter1 = newItemGradingSet.iterator();
		while (iter1.hasNext()) {
			ItemGradingData newItem = iter1.next();
			ItemGradingData oldItem = map.get(newItem
					.getItemGradingId());
			if (oldItem != null) {
			    if (!oldItem.equals(newItem) || 
			    // Check for presence of old data in the EMI, calcQuestion and imagQuestion maps.
			    // FIB, NR, and MCMR maps are not checked; checking them for previous data can cause updates when only the date has changed.
			    // The above check (!oldItem.equals(newItem) suffices for checking new data against these question types. See SAK-39928 for more details.
			            emiMap.get(oldItem.getPublishedItemId()) != null
			            || calcQuestionMap.get(oldItem.getPublishedItemId())!=null
						|| imagQuestionMap.get(oldItem.getPublishedItemId()) != null) {
			        String newAnswerText = newItem.getAnswerText();
			        oldItem.setReview(newItem.getReview());
			        oldItem.setPublishedAnswerId(newItem.getPublishedAnswerId());
			        String newRationale = TextFormat.convertPlaintextToFormattedTextNoHighUnicode(newItem.getRationale());
			        oldItem.setRationale(newRationale);
			        oldItem.setAnswerText(newAnswerText);
			        oldItem.setSubmittedDate(new Date());
			        oldItem.setAutoScore(newItem.getAutoScore());
			        oldItem.setOverrideScore(newItem.getOverrideScore());
			        updateItemGradingSet.add(oldItem);
			    }
			} else { // itemGrading from new set doesn't exist, add to set in this case a new item should always have the grading ID set to null
				newItem.setItemGradingId(null);
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
			DeliveryBean delivery, HashSet<ItemGradingData> itemGradingHash) {
		PersonBean person = (PersonBean) ContextUtil.lookupBean("person");
		AssessmentGradingData adata = new AssessmentGradingData();
		adata.setAgentId(person.getId());
		adata.setPublishedAssessmentId(publishedAssessment
				.getPublishedAssessmentId());
		adata.setForGrade(delivery.getForGrade());
		adata.setItemGradingSet(itemGradingHash);
		adata.setAttemptDate(new Date());
		adata.setIsLate(Boolean.FALSE);
		adata.setStatus(0);
		adata.setTotalOverrideScore(Double.valueOf(0));
		adata.setTimeElapsed(Integer.valueOf("0"));
		return adata;
	}

	/**
	 * This is specific to JSF - question for each type is layout differently in
	 * JSF and the answers submitted are being collected differently too. e.g.
	 * for each MC/Survey/MCMR, an itemgrading is associated with each choice.
	 * whereas there is only one itemgrading per each question for SAQ/TF/Audio,
	 * and one for each blank in FIB. To understand the logic in this method, it
	 * is best to study jsf/delivery/item/deliver*.jsp
	 */
	private void prepareItemGradingPerItem(ActionEvent ae, DeliveryBean delivery,
			ItemContentsBean item, HashSet<ItemGradingData> adds, HashSet<ItemGradingData> removes) {
		List<ItemGradingData> grading = item.getItemGradingDataArray();
		int typeId = item.getItemData().getTypeId().intValue();
		
		//no matter what kinds of type questions, if it marks as review, add it in.
		for (int m = 0; m < grading.size(); m++) {
			ItemGradingData itemgrading = grading.get(m);
			if (itemgrading.getItemGradingId() == null && (itemgrading.getReview() != null && itemgrading.getReview())  == true) {
				adds.add(itemgrading);
			} 
		}
		
		// 1. add all the new itemgrading for MC/Survey and discard any
		// itemgrading for MC/Survey
		// 2. add any modified SAQ/TF/FIB/Matching/MCMR/Audio/FIN
		switch (typeId) {
		case 1: // MC
		case 12: // MC Single Selection
		case 3: // Survey
			boolean answerModified = false;
			for (int m = 0; m < grading.size(); m++) {
				ItemGradingData itemgrading = grading.get(m);

				if (itemgrading.getItemGradingId() == null
						|| itemgrading.getItemGradingId().intValue() <= 0) { // =>
					// new answer
					if (itemgrading.getPublishedAnswerId() != null || (itemgrading.getRationale() != null && !itemgrading.getRationale().trim().equals(""))) { 
						answerModified = true;
						break;
					}
				}
			}
			
			// Click the Reset Selection link
			if(item.getUnanswered()) {
				answerModified = true;
			}
			
			if (answerModified) {
				for (int m = 0; m < grading.size(); m++) {
					ItemGradingData itemgrading = grading
					.get(m);
					if (itemgrading.getItemGradingId() != null
							&& itemgrading.getItemGradingId().intValue() > 0) {
						// remove all old answer for MC & Surevy
						removes.add(itemgrading);
					} else {
						// add new answer
						if (itemgrading.getPublishedAnswerId() != null
							|| itemgrading.getAnswerText() != null
							|| (itemgrading.getRationale() != null 
								&& !itemgrading.getRationale().trim().equals(""))) { 
							// null=> skipping this question
							itemgrading.setAgentId(AgentFacade.getAgentString());
							itemgrading.setSubmittedDate(new Date());
							if (itemgrading.getRationale() != null && itemgrading.getRationale().length() > 0) {
								itemgrading.setRationale(TextFormat.convertPlaintextToFormattedTextNoHighUnicode(itemgrading.getRationale()));
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
				ItemGradingData itemgrading = grading.get(m);
				itemgrading.setAgentId(AgentFacade.getAgentString());
				itemgrading.setSubmittedDate(new Date());
			}
			for (int m = 0; m < grading.size(); m++) {
				ItemGradingData itemgrading = grading.get(m);
				if ((itemgrading.getItemGradingId() != null	&& itemgrading.getItemGradingId().intValue() > 0) ||
					(itemgrading.getPublishedAnswerId() != null || itemgrading.getAnswerText() != null) ||
					(itemgrading.getRationale() != null && !itemgrading.getRationale().trim().equals(""))) {
					adds.addAll(grading);
					break;
				} 
			}
			break;
		case 5: // SAQ
			for (int m = 0; m < grading.size(); m++) {
				ItemGradingData itemgrading = grading.get(m);
				itemgrading.setAgentId(AgentFacade.getAgentString());
				itemgrading.setSubmittedDate(new Date());
			}
			for (int m = 0; m < grading.size(); m++) {
				ItemGradingData itemgrading = grading.get(m);
				if (itemgrading.getItemGradingId() != null
						&& itemgrading.getItemGradingId().intValue() > 0) {
					adds.addAll(grading);
					break;
				} else if (itemgrading.getAnswerText() != null && !itemgrading.getAnswerText().equals("")) {
					// Change to allow student submissions in rich-text [SAK-17021]
					itemgrading.setAnswerText(itemgrading.getAnswerText());
					adds.addAll(grading);
					break;
				}
			}
			break;			
		case 8: // FIB
		case 15: // CALCULATED_QUESTION
		case 16: //IMAGEMAP_QUESTION 	
		case 11: // FIN
			boolean addedToAdds = false;
			for (int m = 0; m < grading.size(); m++) {
				ItemGradingData itemgrading = grading.get(m);
				itemgrading.setAgentId(AgentFacade.getAgentString());
				itemgrading.setSubmittedDate(new Date());
			}
			for (int m = 0; m < grading.size(); m++) {
				ItemGradingData itemgrading = grading.get(m);
				if (itemgrading.getItemGradingId() != null
						&& itemgrading.getItemGradingId().intValue() > 0) {
					adds.addAll(grading);
					break;
				} else if (itemgrading.getAnswerText() != null && !itemgrading.getAnswerText().equals("")) {
					String s = itemgrading.getAnswerText();
					log.debug("s = " + s);
					// Change to allow student submissions in rich-text [SAK-17021]
					itemgrading.setAnswerText(s);
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
				ItemGradingData itemgrading = grading.get(m);
				if (itemgrading.getItemGradingId() != null
						&& itemgrading.getItemGradingId().intValue() > 0) {
					// old answer, check which one to keep, not keeping null  answer
					if (itemgrading.getPublishedAnswerId() != null || 
						(itemgrading.getRationale() != null && !itemgrading.getRationale().trim().equals(""))) {
						itemgrading.setAgentId(AgentFacade.getAgentString());
						itemgrading.setSubmittedDate(new Date());
						adds.add(itemgrading);
					} else {
						removes.add(itemgrading);
					}
				} else { 
					 // new answer
					if (itemgrading.getPublishedAnswerId() != null ||
							(itemgrading.getRationale() != null && !itemgrading.getRationale().trim().equals(""))) {
						// new  addition  not accepting any new answer with null for MCMR
						itemgrading.setAgentId(AgentFacade.getAgentString());
						itemgrading.setSubmittedDate(new Date());
						adds.add(itemgrading);
					}
				}
			}
			break;
		case 14: // Extended Matching Item
			Long assessmentGradingId = delivery.getAssessmentGrading().getAssessmentGradingId();
                       	Long publishedItemId = item.getItemData().getItemId();
			log.debug("Updating answer set for EMI question: publishedItemId=" + publishedItemId + " grading.size()=" + grading.size() + 
				" item id=" + item.getItemData().getItemId() + " assessmentGradingId=" + assessmentGradingId);

			for (int m = 0; m < grading.size(); m++) {
				ItemGradingData itemgrading = (ItemGradingData) grading.get(m);
				if (itemgrading.getItemGradingId() != null
						&& itemgrading.getItemGradingId().intValue() > 0) {
					// old answer, check which one to keep, not keeping null  answer
					if (itemgrading.getPublishedAnswerId() != null) {
						itemgrading.setAgentId(AgentFacade.getAgentString());
						itemgrading.setSubmittedDate(new Date());
						adds.add(itemgrading);
						log.debug("adding answer: " + itemgrading.getItemGradingId());
					} else {
						removes.add(itemgrading);
						log.debug("remove answer: " + itemgrading.getItemGradingId());
					}
				} else { 
					 // new answer
					if (itemgrading.getPublishedAnswerId() != null) {
						// new  addition  not accepting any new answer with null for EMI
						itemgrading.setAgentId(AgentFacade.getAgentString());
						itemgrading.setSubmittedDate(new Date());
						adds.add(itemgrading);
						log.debug("adding new answer answer: " + itemgrading.getItemGradingId());
					}
				}
				
			}

			// We need to remove any answer (response) items in the storage that are not in the above lists
			removes.addAll(identifyOrphanedEMIAnswers(grading, publishedItemId, assessmentGradingId));
			
			break;
		case 6: // File Upload
		case 7: // Audio
                        handleMarkForReview(grading, adds);
                        break;
		case 13: //Matrix Choices question
			answerModified = false;
			for (int m = 0; m < grading.size(); m++) {
				ItemGradingData itemgrading = grading.get(m);
				if (itemgrading !=null && (itemgrading.getItemGradingId() == null || itemgrading.getItemGradingId().intValue() <= 0)) {
					if (itemgrading.getPublishedAnswerId() != null || (itemgrading.getRationale() != null && StringUtils.isNotBlank(itemgrading.getRationale()))) { 
						answerModified = true;
						break;
					}
				}
			}
			// Click the Reset Selection link
			if(item.getUnanswered()) {
				answerModified = true;
			}

			if (answerModified) {
				for (int m = 0; m < grading.size(); m++) {
					ItemGradingData itemgrading = grading.get(m);

					// Remove all previous answers
					if (itemgrading !=null && itemgrading.getItemGradingId() != null && itemgrading.getItemGradingId().intValue() > 0) {
						removes.add(itemgrading);
					}

					// Add all provided answers, regardless if they're new or not
					if (itemgrading !=null && (itemgrading.getPublishedAnswerId() != null || itemgrading.getAnswerText() != null
							|| (itemgrading.getRationale() != null && StringUtils.isNotBlank(itemgrading.getRationale())))) { 
						itemgrading.setAgentId(AgentFacade.getAgentString());
						itemgrading.setSubmittedDate(new Date());
						if (itemgrading.getRationale() != null && itemgrading.getRationale().length() > 0) {
							itemgrading.setRationale(TextFormat.convertPlaintextToFormattedTextNoHighUnicode(itemgrading.getRationale()));
						}
						adds.add(itemgrading);
					}
				}
			}
			else{
				updateItemGradingData(grading, adds);
			}
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
		
		if ("1".equals(delivery.getNavigation()) && adds.isEmpty() && !"showFeedback".equals(actionCommand)) {
			log.debug("enter here");
			Long assessmentGradingId = delivery.getAssessmentGrading().getAssessmentGradingId();
			Long publishedItemId = item.getItemData().getItemId();
			log.debug("assessmentGradingId = " + assessmentGradingId);
			log.debug("publishedItemId = " + publishedItemId);
			GradingService gradingService = new GradingService();
			
			if (gradingService.getItemGradingData(assessmentGradingId.toString(), publishedItemId.toString()) == null) {
				log.debug("Create a new (fake) ItemGradingData");
				ItemGradingData itemGrading = new ItemGradingData();
				itemGrading.setAssessmentGradingId(assessmentGradingId);
				itemGrading.setAgentId(AgentFacade.getAgentString());
				itemGrading.setPublishedItemId(publishedItemId);
				ItemService itemService = new ItemService();
				Long itemTextId = itemService.getItemTextId(publishedItemId);
				log.debug("itemTextId = " + itemTextId);
				if(itemTextId != -1){
					itemGrading.setPublishedItemTextId(itemTextId);
					adds.add(itemGrading);
				}
			}
			else {
				// For File Upload question, if user clicks on "Upload", a ItemGradingData will be created. 
				// Therefore, when user clicks on "Next", we shouldn't create it again.
				// Same for Audio question, if user records anything, a ItemGradingData will be created.
				// We don't create it again when user clicks on "Next".
				if ((typeId == 6 || typeId == 7)) {
					log.debug("File Upload or Audio! Do not create empty ItemGradingData if there exists one");
				}
			}
		}
	}

	/**
	 * Identify the items in an EMI Answer that are orphaned
	 * @param grading
	 * @return a list of ItemGradings to be removed
	 */
    private Collection<ItemGradingData> identifyOrphanedEMIAnswers(List<ItemGradingData> grading, Long publishedItemId, Long assessmentGradingId) {
	Set<ItemGradingData> ret = new HashSet<>();
		
	List<Long> itemsInGrading = new ArrayList<>();
	for (int i = 0; i < grading.size(); i++) {
		ItemGradingData data = grading.get(i);
		itemsInGrading.add(data.getItemGradingId());
	}
		
    	GradingService gradingService = new GradingService();
    	List<ItemGradingData> data = gradingService.getAllItemGradingDataForItemInGrading(assessmentGradingId, publishedItemId);
    	log.debug("got " + data.size() + " answers from storage");
    	log.debug("got " + grading.size() + " items in the grading object");
    	for (int i = 0; i < data.size(); i++) {
    		ItemGradingData item = data.get(i);
    		if (!itemsInGrading.contains(item.getItemGradingId())) {
    			log.debug("we will remove "  + item.getItemGradingId());
    			ret.add(item);
    		}
    	}
    	
	return ret;
   }

    private void updateItemGradingData(List<ItemGradingData> grading, HashSet<ItemGradingData> adds){
        for (int m = 0; m < grading.size(); m++) {
          ItemGradingData itemgrading = grading.get(m);
          if (itemgrading.getItemGradingId() != null && itemgrading.getItemGradingId().intValue() > 0)  {
        	  adds.add(itemgrading);
          }
        }
      }
    
    private void handleMarkForReview(List<ItemGradingData> grading, HashSet<ItemGradingData> adds){
      for (int m = 0; m < grading.size(); m++) {
        ItemGradingData itemgrading = grading.get(m);
        if (itemgrading.getItemGradingId() != null 
            && itemgrading.getItemGradingId().intValue() > 0
            && itemgrading.getReview() != null)  {
            // we will save itemgrading even though answer was not modified 
            // 'cos mark for review may have been modified
          adds.add(itemgrading);
        }
      }
    }

   
	private Boolean isLate(PublishedAssessmentIfc pub, boolean submitFromTimeoutPopup, String adataAgentId) {
		AssessmentAccessControlIfc a = pub.getAssessmentAccessControl();
		// If submit from timeout popup, we don't record LATE
		if(submitFromTimeoutPopup) {
			return Boolean.FALSE;
		}

		Boolean isLate = false;
		if (a.getDueDate() != null && a.getDueDate().before(new Date())) {
			isLate = Boolean.TRUE;
		} else {
			isLate = Boolean.FALSE;
		}

		if (isLate) {
			ExtendedTimeDeliveryService assessmentExtended = new ExtendedTimeDeliveryService((PublishedAssessmentFacade) pub, adataAgentId);
			if (assessmentExtended.hasExtendedTime() && assessmentExtended.getDueDate() != null && assessmentExtended.getDueDate().after(new Date())) {
				isLate = Boolean.FALSE;	
			}
		}

		return isLate;
	}
}
