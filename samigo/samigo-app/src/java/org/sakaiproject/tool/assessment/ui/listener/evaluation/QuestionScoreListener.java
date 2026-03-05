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

package org.sakaiproject.tool.assessment.ui.listener.evaluation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.rubrics.api.model.ToolItemRubricAssociation;
import org.sakaiproject.rubrics.api.RubricsConstants;
import org.sakaiproject.rubrics.api.RubricsService;
import org.sakaiproject.spring.SpringBeanLocator;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.EvaluationModel;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAnswer;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingAttachment;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.MediaData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.PublishedItemService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.AgentResults;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.HistogramScoresBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.PartData;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.QuestionScoresBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.SubmissionStatusBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.TotalScoresBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.BeanSort;
import org.sakaiproject.tool.assessment.util.ItemCancellationUtil;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.api.FormattedText;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;

/**
 * <p>
 * This handles the selection of the Question Score entry page.
 * </p>
 * <p>
 * Description: Action Listener for Evaluation Question Score front door
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Organization: Sakai Project
 * </p>
 * 
 * @author Ed Smiley
 * @version $Id: QuestionScoreListener.java 11438 2006-06-30 20:06:03Z
 *          daisyf@stanford.edu $
 */

@Slf4j
 public class QuestionScoreListener implements ActionListener,
		ValueChangeListener {

	// private static EvaluationListenerUtil util;
	private BeanSort bs;

	private static final ResourceLoader evaluationMessages = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.EvaluationMessages");
	private static final String noAnswer = evaluationMessages.getString("no_answer");
	private static final String noneOfTheAbove = evaluationMessages.getString("none_above");

	private RubricsService rubricsService = (RubricsService) SpringBeanLocator.getInstance().getBean("org.sakaiproject.rubrics.api.RubricsService");

	/**
	 * Standard process action method.
	 * 
	 * @param event
	 *            ActionEvent
	 * @throws AbortProcessingException
	 */
	public void processAction(ActionEvent event)
			throws AbortProcessingException {
		log.debug("QuestionScore LISTENER.");
		QuestionScoresBean bean = (QuestionScoresBean) ContextUtil
				.lookupBean("questionScores");

		// Reset the search field
		String defaultSearchString = evaluationMessages.getString("search_default_student_search_string");
		bean.setSearchString(defaultSearchString);

		// we probably want to change the poster to be consistent
		String publishedId = ContextUtil.lookupParam("publishedId");

		AuthorizationBean authzBean = (AuthorizationBean) ContextUtil.lookupBean("authorization");
		AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean("assessmentBean");
		PublishedAssessmentService pubService = new PublishedAssessmentService();
		Long pubId = new Long(publishedId);
		String assessmentOwner = pubService.getPublishedAssessmentOwner(pubId);
		if (!authzBean.isUserAllowedToGradeAssessment(publishedId, assessmentOwner, true)) {
			throw new IllegalArgumentException("QuestionScoreListener unauthorized attempt to get scores for " + publishedId);
		}

		if (!questionScores(publishedId, bean, false)) {
			throw new RuntimeException("failed to call questionScores.");
		}

	}

	/**
	 * Process a value change.
	 */
	public void processValueChange(ValueChangeEvent event) {
		log.debug("QuestionScore CHANGE LISTENER.");
		ResetQuestionScoreListener reset = new ResetQuestionScoreListener();
		reset.processAction(null);

		QuestionScoresBean bean = (QuestionScoresBean) ContextUtil.lookupBean("questionScores");
		TotalScoresBean totalBean = (TotalScoresBean) ContextUtil.lookupBean("totalScores");
		HistogramScoresBean histogramBean = (HistogramScoresBean) ContextUtil.lookupBean("histogramScores");
		SubmissionStatusBean submissionbean = (SubmissionStatusBean) ContextUtil.lookupBean("submissionStatus");
	    

		// we probably want to change the poster to be consistent
		String publishedId = ContextUtil.lookupParam("publishedId");
		boolean toggleSubmissionSelection = false;

		String selectedvalue = (String) event.getNewValue();
		if ((selectedvalue != null) && (!selectedvalue.equals(""))) {
			if (event.getComponent().getId().indexOf("sectionpicker") > -1) {
				bean.setSelectedSectionFilterValue(selectedvalue); // changed
				totalBean.setSelectedSectionFilterValue(selectedvalue);
				submissionbean.setSelectedSectionFilterValue(selectedvalue);
			} else if (event.getComponent().getId().indexOf("allSubmissions") > -1) {
				bean.setAllSubmissions(selectedvalue); // changed submission
				// pulldown
				totalBean.setAllSubmissions(selectedvalue); // changed for total
				// score bean
				histogramBean.setAllSubmissions(selectedvalue); // changed for
				// histogram
				// score bean
				toggleSubmissionSelection = true;
			} else // inline or popup
			{
				bean.setSelectedSARationaleView(selectedvalue); // changed
				// submission
				// pulldown
			}
		}

		if (!questionScores(publishedId, bean, toggleSubmissionSelection)) {
			throw new RuntimeException("failed to call questionScores.");
		}

		FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "questionScores");

	}

	/**
	 * This will populate the QuestionScoresBean with the data associated with
	 * the particular versioned assessment based on the publishedId.
	 * 
	 * @todo Some of this code will change when we move this to Hibernate
	 *       persistence.
	 * @param publishedId
	 *            String
	 * @param bean
	 *            QuestionScoresBean
	 * @return boolean
	 */
	public boolean questionScores(String publishedId, QuestionScoresBean bean,
			boolean isValueChange) {
		log.debug("questionScores()");
		try {
			PublishedAssessmentService pubService = new PublishedAssessmentService();
			PublishedItemService pubItemService = new PublishedItemService();
			// get the PublishedAssessment based on publishedId
			QuestionScoresBean questionBean = (QuestionScoresBean) ContextUtil
					.lookupBean("questionScores");
			PublishedAssessmentIfc publishedAssessment = questionBean
					.getPublishedAssessment();
			if (publishedAssessment == null) {
				publishedAssessment = pubService
						.getPublishedAssessment(publishedId);
				questionBean.setPublishedAssessment(publishedAssessment);
			}
			// build a hashMap (publishedItemId, publishedItem)
			Map<Long, ItemDataIfc> publishedItemHash = pubService.preparePublishedItemHash(publishedAssessment);
			log.debug("publishedItemHash.size = {}", publishedItemHash.size());
			// build a hashMap (publishedItemTextId, publishedItemText)
			Map<Long, ItemTextIfc> publishedItemTextHash = pubService.preparePublishedItemTextHash(publishedAssessment);
			log.debug("publishedItemTextHash.size = {}", publishedItemTextHash.size());
			GradingService delegate = new GradingService();
			HashMap<Long, TreeMap<Long, ItemTextIfc>>  allItemsHash = new HashMap<>();
			for (Long thisKey : publishedItemTextHash.keySet()) {
				ItemTextIfc thisItemTextIfc = (ItemTextIfc) publishedItemTextHash.get(thisKey);
				for (AnswerIfc thisAnswerIfc : thisItemTextIfc.getAnswerSet()) {
					log.debug("{}", thisAnswerIfc.getId());
				}
				if (delegate.isDistractor(thisItemTextIfc)) {
					log.debug("item is a distractor");
				}

				TreeMap<Long, ItemTextIfc> thisItemOptions = allItemsHash.get(thisItemTextIfc.getItem().getItemId());
				if (thisItemOptions == null) {
					thisItemOptions = new TreeMap<>();
					thisItemOptions.put(thisItemTextIfc.getSequence(), thisItemTextIfc);
					allItemsHash.put(thisItemTextIfc.getItem().getItemId(), thisItemOptions);
				} else {
					thisItemOptions.put(thisItemTextIfc.getSequence(), thisItemTextIfc);
					allItemsHash.put(thisItemTextIfc.getItem().getItemId(), thisItemOptions);
				}
				log.debug("item = {}:{}-{}", thisItemTextIfc.getSequence(), thisItemTextIfc.getText(), thisItemTextIfc.getItem().getItemId());

			}
			Map<Long, AnswerIfc> publishedAnswerHash = pubService.preparePublishedAnswerHash(publishedAssessment);
			// re-attach session and load all lazy loaded parent/child stuff

//			Set<Long> publishedAnswerHashKeySet = publishedAnswerHash.keySet();
//
//			for (Long key : publishedAnswerHashKeySet) {
//				AnswerIfc answer = (AnswerIfc) publishedAnswerHash.get(key);
//
//				if (!Hibernate.isInitialized(answer.getChildAnswerSet())) {
//					pubItemService.eagerFetchAnswer(answer);
//				}
//			}
			log.debug("questionScores(): publishedAnswerHash.size = {}", publishedAnswerHash.size());
			Map<Long, AgentResults> agentResultsByItemGradingIdMap = new HashMap<>();

			TotalScoresBean totalBean = (TotalScoresBean) ContextUtil
					.lookupBean("totalScores");

			if (ContextUtil.lookupParam("sortBy") != null
					&& !ContextUtil.lookupParam("sortBy").trim().equals(""))
				bean.setSortType(ContextUtil.lookupParam("sortBy"));
			
			String itemId = ContextUtil.lookupParam("itemId");
			if (ContextUtil.lookupParam("newItemId") != null
					&& !ContextUtil.lookupParam("newItemId").trim().equals("")
					&& !ContextUtil.lookupParam("newItemId").trim().equals("null"))
				itemId = ContextUtil.lookupParam("newItemId");

			if (ContextUtil.lookupParam("sortAscending") != null
					&& !ContextUtil.lookupParam("sortAscending").trim().equals(
							"")) {
				bean.setSortAscending(Boolean.valueOf(
						ContextUtil.lookupParam("sortAscending"))
						.booleanValue());
			}

			String which = bean.getAllSubmissions();
			if (which == null && totalBean.getAllSubmissions() != null) {
				// use totalscore's selection
				which = totalBean.getAllSubmissions();
				bean.setAllSubmissions(which);
			}

			totalBean.setSelectedSectionFilterValue(bean
					.getSelectedSectionFilterValue()); // set section pulldown

			if (bean.getSelectedSARationaleView() == null) {
				// if bean.showSARationaleInLine is null, then set inline to be
				// the default
				bean.setSelectedSARationaleView(QuestionScoresBean.SHOW_SA_RATIONALE_RESPONSES_INLINE);
			}

			if ("true".equalsIgnoreCase(totalBean.getAnonymous())) {
				boolean groupRelease = publishedAssessment.getAssessmentAccessControl().getReleaseTo().equals(AssessmentAccessControl.RELEASE_TO_SELECTED_GROUPS);
		    	if (groupRelease) {
		    		totalBean.setSelectedSectionFilterValue(TotalScoresBean.RELEASED_SECTIONS_GROUPS_SELECT_VALUE);
		    	}
		    	else {
		    		totalBean.setSelectedSectionFilterValue(TotalScoresBean.ALL_SECTIONS_SELECT_VALUE);
		    	}
			}

			bean.setPublishedId(publishedId);
			Date dueDate = null;

			Map map = getItemScores(Long.valueOf(publishedId), Long
					.valueOf(itemId), which, isValueChange);
			log.debug("questionScores(): map .size = {}", map.size());
			List allscores = new ArrayList();
			Iterator keyiter = map.keySet().iterator();
			while (keyiter.hasNext()) {
				allscores.addAll((List) map.get(keyiter.next()));
			}

			log.debug("questionScores(): allscores.size = {}", allscores.size());

			// now we need filter by sections selected
			List scores = new ArrayList(); // filtered list
			Map useridMap = totalBean.getUserIdMap(TotalScoresBean.CALLED_FROM_QUESTION_SCORE_LISTENER, AgentFacade.getCurrentSiteId());
			bean.setUserIdMap(useridMap);
			log.debug("questionScores(): useridMap.size = " + useridMap.size());

			/*
			 * if ("true".equalsIgnoreCase(totalBean.getAnonymous())){ // skip
			 * section filter if it is anonymous grading, SAK-4395,
			 * scores.addAll(allscores); }
			 */
			if (totalBean.getReleaseToAnonymous()) {
				// skip section filter if it's published to anonymous users
				scores.addAll(allscores);
			} else {
				Iterator allscores_iter = allscores.iterator();
				// get the Map of all users(keyed on userid) belong to the
				// selected sections
				while (allscores_iter.hasNext()) {
					// AssessmentGradingData data = (AssessmentGradingData)
					// allscores_iter.next();
					ItemGradingData idata = (ItemGradingData) allscores_iter
							.next();
					// String agentid =
					// idata.getAssessmentGrading().getAgentId();
					String agentid = idata.getAgentId();
					// now we only include scores of users belong to the
					// selected sections
					if (useridMap.containsKey(agentid)) {
						scores.add(idata);
					}
				}
			}

			log.debug("questionScores(): scores.size = {}", scores.size());

			Iterator iter = scores.iterator();
			List agents = new ArrayList();

			log.debug("questionScores(): calling populateSections ");

			populateSections(publishedAssessment, bean, totalBean, scores,
					pubService); // set up the Q1, Q2... links
			if (!iter.hasNext()) {
				// this section has no students
				log.debug("questionScores(): this section has no students");
				bean.setAgents(agents);
				bean.setAllAgents(agents);
				bean.setTotalPeople(Integer.toString(agents.size()));
				bean.setAnonymous(totalBean.getAnonymous());
				//return true;
			}

			// List them by item and assessmentgradingid, so we can
			// group answers by item and save them for update use.

			Map scoresByItem = new HashMap();
			while (iter.hasNext()) {
				ItemGradingData idata = (ItemGradingData) iter.next();
				ItemTextIfc pubItemText = (ItemTextIfc) publishedItemTextHash
						.get(idata.getPublishedItemTextId());
				AnswerIfc pubAnswer = (AnswerIfc) publishedAnswerHash.get(idata
						.getPublishedAnswerId());

				List temp = (ArrayList) scoresByItem.get(idata
						.getAssessmentGradingId()
						+ ":" + idata.getPublishedItemId());
				if (temp == null)
					temp = new ArrayList();

				// Very small numbers, so bubblesort is fast
				Iterator iter2 = temp.iterator();
				List newList = new ArrayList();
				boolean added = false;
				while (iter2.hasNext()) {
					ItemGradingData tmpData = (ItemGradingData) iter2.next();
					ItemTextIfc tmpPublishedText = (ItemTextIfc) publishedItemTextHash
							.get(tmpData.getPublishedItemTextId());
					AnswerIfc tmpAnswer = (AnswerIfc) publishedAnswerHash
							.get(tmpData.getPublishedAnswerId());

					if (pubAnswer != null
							&& tmpAnswer != null
							&& !added
							&& (pubItemText.getSequence().intValue() < tmpPublishedText
									.getSequence().intValue() || (pubItemText
									.getSequence().intValue() == tmpPublishedText
									.getSequence().intValue() && pubAnswer
									.getSequence().intValue() < tmpAnswer
									.getSequence().intValue()))) {
						newList.add(idata);
						added = true;
					}
					newList.add(tmpData);
				}
				if (!added)
					newList.add(idata);
				scoresByItem.put(idata.getAssessmentGradingId() + ":"
						+ idata.getPublishedItemId(), newList);
			}
			log.debug("questionScores(): scoresByItem.size = {}", scoresByItem.size());
			bean.setScoresByItem(scoresByItem);

			try {
				bean.setAnonymous(publishedAssessment.getEvaluationModel()
						.getAnonymousGrading().equals(
								EvaluationModel.ANONYMOUS_GRADING) ? "true"
										: "false");
			} catch (RuntimeException e) {
				bean.setAnonymous("false");
			}
			
			// below properties don't seem to be used in jsf pages,
			try {
				bean.setLateHandling(publishedAssessment
						.getAssessmentAccessControl().getLateHandling()
						.toString());
			} catch (Exception e) {
				bean
				.setLateHandling(AssessmentAccessControl.NOT_ACCEPT_LATE_SUBMISSION
						.toString());
			}
			try {
				bean.setDueDate(publishedAssessment
						.getAssessmentAccessControl().getDueDate().toString());
				dueDate = publishedAssessment.getAssessmentAccessControl()
				.getDueDate();
			} catch (RuntimeException e) {
				bean.setDueDate(new Date().toString());
			}
			try {
				bean.setMaxScore(publishedAssessment.getEvaluationModel()
						.getFixedTotalScore());
			} catch (RuntimeException e) {
				double score = (double) 0.0;
				Iterator iter2 = publishedAssessment.getSectionArraySorted()
				.iterator();
				while (iter2.hasNext()) {
					SectionDataIfc sdata = (SectionDataIfc) iter2.next();
					Iterator iter3 = sdata.getItemArraySortedForGrading()
					.iterator();
					while (iter3.hasNext()) {
						ItemDataIfc idata = (ItemDataIfc) iter3.next();
						if (idata.getItemId().equals(Long.valueOf(itemId)))
							score = idata.getScore().doubleValue();
					}
				}
				bean.setMaxScore(score);
			}
			
			// need to get id from somewhere else, not from data. data only
			// contains answered items , we want to return all items.
			// ItemDataIfc item = (ItemDataIfc) publishedItemHash.get(data.getPublishedItemId());
			ItemDataIfc item = (ItemDataIfc) publishedItemHash.get(Long.valueOf(itemId));
			
			if (item != null) {
				log.debug("item!=null steting type id = "
						+ item.getTypeId().toString());
				bean.setTypeId(item.getTypeId().toString());
				bean.setItemId(item.getItemId().toString());
				bean.setPartName(item.getSection().getSequence().toString());
				bean.setItemName(item.getSequence().toString());
				bean.setItemData(item);
				item.setHint("***"); // Keyword to not show student answer
				// for short answer/ essey question, if there is a model short
				// answer for this question
				// set haveModelShortAnswer to true
				if (item.getTypeId().equals(Long.valueOf(5))) {
					Iterator iterator = publishedAnswerHash.values().iterator();
					while (iterator.hasNext()) {
						PublishedAnswer publishedAnswer = (PublishedAnswer) iterator
								.next();
						if (publishedAnswer.getItem().getItemId().equals(
								item.getItemId())) {
							if (publishedAnswer.getText() == null
									|| publishedAnswer.getText().equals("")) {
								bean.setHaveModelShortAnswer(false);
							} else {
								bean.setHaveModelShortAnswer(true);
							}
							break;
						}
					}
				}
			} else {
				log.debug("item==null ");
			}

			List deliveryItems = new ArrayList(); // so we can use the var
			if (item != null)
				deliveryItems.add(item);
			bean.setDeliveryItem(deliveryItems);

			boolean randomItemPresent = publishedItemHash.values().stream()
					.filter(publishedItem -> ItemCancellationUtil.isRandomItem(publishedItem))
					.findAny()
					.isPresent();

			// At least one other question, that is not cancelled should exist and the item can't be random
			bean.setCancellationAllowed(!randomItemPresent && publishedItemHash.values().stream()
					.filter(publishedItem -> !TypeIfc.EXTENDED_MATCHING_ITEMS.equals(publishedItem.getTypeId()))
					.filter(publishedItem -> !ItemCancellationUtil.isCancelled(publishedItem))
					.collect(Collectors.counting()) > 1);
			log.debug("setCancellationAllowed({})", bean.isCancellationAllowed());

			bean.setEmiItemPresent(publishedItemHash.values().stream()
					.filter(publishedItem -> TypeIfc.EXTENDED_MATCHING_ITEMS.equals(publishedItem.getTypeId()))
					.filter(publishedItem -> !ItemCancellationUtil.isCancelled(publishedItem))
					.collect(Collectors.counting())
					.intValue() > 0);
			log.debug("setEmiItemPresent({})", bean.isEmiItemPresent());

			bean.setRandomItemPresent(randomItemPresent);
			log.debug("setRandomItemPresent({})", randomItemPresent);

			if (ContextUtil.lookupParam("roleSelection") != null) {
				bean.setRoleSelection(ContextUtil.lookupParam("roleSelection"));
			}

			if (bean.getSortType() == null) {
				if (bean.getAnonymous().equals("true")) {
					bean.setSortType("totalAutoScore");
				} else {
					bean.setSortType("lastName");
				}
			}

			// recordingData encapsulates the inbeanation needed for recording.
			// set recording agent, agent assessmentId,
			// set course_assignment_context value
			// set max tries (0=unlimited), and 30 seconds max length

			// String courseContext = bean.getAssessmentName() + " total ";

			// Note this is HTTP-centric right now, we can't use in Faces
			// AuthoringHelper authoringHelper = new AuthoringHelper();
			// authoringHelper.getRemoteUserID() needs servlet stuff
			// authoringHelper.getRemoteUserName() needs servlet stuff

			/* Dump the grading and agent information into AgentResults */
			iter = scoresByItem.values().iterator();
			while (iter.hasNext()) {
				AgentResults results = new AgentResults();
				// Get all the answers for this question to put in one grading
				// row
				List answerList = (List) iter.next();
				results.setItemGradingArrayList(answerList);
				// The list is sorted by item id so that it will come back from the student in a 
				// predictable order. This is also required by the getCalcQResult method.
				if (TypeIfc.CALCULATED_QUESTION.equals(Long.parseLong(bean.getTypeId()))) { // CALCULATED_QUESTION
					// list is sorted by answer id for calculated question
					Collections.sort(answerList, new Comparator<ItemGradingData>() {
						public int compare(ItemGradingData i1, ItemGradingData i2) {
						if (i1 == i2) {
							return 0;
						} else if (i1 == null || i1.getPublishedAnswerId() == null) {
							return -1;
						} else if (i2 == null || i2.getPublishedAnswerId() == null) {
							return 1;
						} else {
							return NumberUtils.compare(i1.getPublishedAnswerId(),i2.getPublishedAnswerId());
						}
					   }
					});

				} else { // Non calculated question
					// The list is sorted by item id so that it will come back from the student in a 
					// predictable order. This is also required by the getCalcQResult method. 
					Collections.sort(answerList, new Comparator<ItemGradingData>() {
						public int compare(ItemGradingData i1, ItemGradingData i2) {
						if (i1 == i2) {
							return 0;
						} else if (i1 == null || i1.getPublishedItemId() == null) {
							return -1;
						} else if (i2 == null || i2.getPublishedItemId() == null) {
							return 1;
						} else {
							return NumberUtils.compare(i1.getPublishedItemId(),i2.getPublishedItemId());
						}
					   }
					});
				}
				Iterator iter2 = answerList.iterator();
				List<ItemGradingAttachment> itemGradingAttachmentList = new ArrayList<>();
				int i = 1;
				Map<Integer, String> answersMap = new HashMap<Integer, String>();
				LinkedHashMap<String, String> answersMapValues = new LinkedHashMap<String, String>();
				LinkedHashMap<String, String> globalanswersMapValues = new LinkedHashMap<String, String>();
				LinkedHashMap<String, String> mainvariablesWithValues = new LinkedHashMap<String, String>();
				while (iter2.hasNext()) {
					ItemGradingData gdata = (ItemGradingData) iter2.next();
					results.setItemGrading(gdata);
					delegate.extractCalcQAnswersArray(answersMap, answersMapValues, globalanswersMapValues, mainvariablesWithValues, item,
								gdata.getAssessmentGradingId(), gdata.getAgentId());
					itemGradingAttachmentList.addAll(gdata.getItemGradingAttachmentSet());
					agentResultsByItemGradingIdMap.put(gdata.getItemGradingId(), results);
										
					ItemTextIfc gdataPubItemText = (ItemTextIfc) publishedItemTextHash
							.get(gdata.getPublishedItemTextId());
					AnswerIfc gdataAnswer = (AnswerIfc) publishedAnswerHash
							.get(gdata.getPublishedAnswerId());

					// This all just gets the text of the answer to display
					String answerText = noAnswer;
					String rationale = "";
					String fullAnswerText = noAnswer;
					
					// Answer Key and Decimal Places for Calculated Questions
					String answerKey = noAnswer;
					int decimalPlaces;

					// if question type = MC, MR, Survey, TF, Matching, if user
					// has not submit an answer
					// answerText = noAnswer. These question type do not use the
					// itemGrading.answerText field for
					// storing answers, thye use temGrading.publishedAnswerId to
					// make their selection
					if (bean.getTypeId().equals("1")
							|| bean.getTypeId().equals("2")
							|| bean.getTypeId().equals("12")
							|| bean.getTypeId().equals("3")
							|| bean.getTypeId().equals("4")
							|| bean.getTypeId().equals("9")
							|| bean.getTypeId().equals("13")) {
						if (gdataAnswer != null)
							answerText = gdataAnswer.getText();
					} else {
						// this handles the other question types: SAQ, File
						// upload, Audio, FIB, Fill in Numeric
						// These question type use itemGrading.answetText to
						// store information about their answer
						if ((bean.getTypeId().equals("8") || bean.getTypeId().equals("11") || bean.getTypeId().equals("14")) && gdataAnswer == null) {
							answerText = "";
						} 
						else if (bean.getTypeId().equals("14")) {//gopalrc - EMI
							answerText = gdataPubItemText.getSequence() + ": " + gdataAnswer.getLabel();
						}
						else {
							answerText = gdata.getAnswerText();
						}
					}

					if ("4".equals(bean.getTypeId())) {
						if ("true".equals(answerText)) {
							answerText = evaluationMessages.getString("true_msg");
						}
						else if ("false".equals(answerText)) {
							answerText = evaluationMessages.getString("false_msg");
						}
					}
					
					if (bean.getTypeId().equals("9")) {
						if (gdataPubItemText == null) {
							// the matching pair is deleted
							answerText = "";
						}
						else {
							answerText = gdataPubItemText.getSequence() + ":"
								+ answerText;
						}
					}

					if (bean.getTypeId().equals("8")) {
						if (gdataAnswer != null && gdataAnswer.getSequence() != null) {
							answerText = gdataAnswer.getSequence() + ":"
									+ answerText;
						}
					}

					if (bean.getTypeId().equals("11")) {
						if (gdataAnswer != null && gdataAnswer.getSequence() != null) {
							answerText = gdataAnswer.getSequence() + ":"
									+ answerText;
						}
					}
					if (bean.getTypeId().equals("13")) {
						if (gdataPubItemText == null) {
							answerText = "";
						}
						else {
							int answerNo = gdataPubItemText.getSequence().intValue();
							answerText = answerNo + ":" + answerText;
						}
					}
					// file upload
					if (bean.getTypeId().equals("6")) {
						gdata.setMediaArray(delegate.getMediaArray2(gdata
								.getItemGradingId().toString()));
					}

					// audio recording
					if (bean.getTypeId().equals("7")) {
						List<MediaData> mediaList = delegate.getMediaArray2(gdata
								.getItemGradingId().toString());
						setDurationIsOver(item, mediaList);
						gdata.setMediaArray(mediaList);
					}
					if (bean.getTypeId().equals("16")) {
						if (gdataPubItemText == null) {
							// the matching pair is deleted
							answerText = "";
						}
						else {
							answerText = gdataPubItemText.getSequence() + ":"+ answerText;
						}
					}
					if (answerText == null)
						answerText = noAnswer;
					else {
						if (gdata.getRationale() != null
								&& !gdata.getRationale().trim().equals(""))
							rationale = "\n" + evaluationMessages.getString("rationale") + ": " + gdata.getRationale();
					}
					// Huong's temp commandout
					// answerText = answerText.replaceAll("<.*?>", "");
					answerText = answerText.replaceAll("(\r\n|\r)", "<br/>");
					rationale = rationale.replaceAll("<.*?>", "");
					rationale = rationale.replaceAll("(\r\n|\r)", "<br/>");
					fullAnswerText = answerText; // this is the
					// non-abbreviated answers
					// for essay questions

					int answerTextLength = ServerConfigurationService.getInt("samigo.questionScore.answerText.length", 1000);
					if (bean.getTypeId().equals("5")) {
						answerTextLength = 35;
					}

					log.debug("answerText=" + answerText);
					// Fix for SAK-6932: Strip out all HTML tags except image tags
 					if (answerText.length() > answerTextLength) {
						String noHTMLAnswerText;
						noHTMLAnswerText = answerText.replaceAll(
								"<((..?)|([^iI][^mM][^gG].*?))>", "");

						int index = noHTMLAnswerText.toLowerCase().indexOf(
								"<img");
						if (index != -1) {
							answerText = noHTMLAnswerText;
						} else {
							if (noHTMLAnswerText.length() > answerTextLength) {
								answerText = noHTMLAnswerText.substring(0, answerTextLength)
										+ "...";
							} else {
								answerText = noHTMLAnswerText;
							}
						}
					}
					/*
					 * // no need to shorten it if (rationale.length() > 35)
					 * rationale = rationale.substring(0, 35) + "...";
					 */

					//SAM-755-"checkmark" indicates right, add "X" to indicate wrong
					String correct = evaluationMessages.getString("alt_correct");
					String incorrect = evaluationMessages.getString("alt_incorrect");
					String checkmarkGif = String.format("<span title=\"%s\" class=\"si si-check-lg\"></span>", correct);
					String crossmarkGif = String.format("<span title=\"%s\" class=\"si si-remove feedBackCross\"></span>", incorrect);
					if (gdataAnswer != null) {
						answerText = ComponentManager.get(FormattedText.class).escapeHtml(answerText, true);
						if (bean.getTypeId().equals("8") || bean.getTypeId().equals("11")) {
							if (gdata.getIsCorrect() == null) {
								boolean result = false;
								if (bean.getTypeId().equals("8")) {
									result = delegate.getFIBResult(gdata, new HashMap<Long, Set<String>>(), item, publishedAnswerHash);
								}
								else {
									result = delegate.getFINResult(gdata, item, publishedAnswerHash);
								}

								if (result) {
									answerText = checkmarkGif + answerText;
								} else {
									answerText = crossmarkGif + answerText;
								}
							}
							else {
								if (gdata.getIsCorrect().booleanValue()) {
									answerText = checkmarkGif + answerText;
								}
								else {
									answerText = crossmarkGif + answerText;
								}
							}
						}
						else if (bean.getTypeId().equals("15")) {  // CALCULATED_QUESTION
							// Answers Keys
							answerKey = (String)answersMap.get(i);
							decimalPlaces = Integer.valueOf(answerKey.substring(answerKey.lastIndexOf(',')+1, answerKey.length()));
							answerKey = answerKey.substring(0, answerKey.lastIndexOf("|")); // cut off extra data e.g. "|2,3"
							// We need the key formatted in scientificNotation
							answerKey = delegate.toScientificNotation(answerKey, decimalPlaces);

							// Answers
							if (delegate.getCalcQResult(gdata, item, answersMap, i++)) {
								answerText = checkmarkGif + answerText;
							} else {
								answerText = crossmarkGif + answerText;
							}
						}
						else if(!bean.getTypeId().equals("3")){
							if((gdataAnswer.getIsCorrect() != null && gdataAnswer.getIsCorrect()) || 
								(gdataAnswer.getPartialCredit() != null && gdataAnswer.getPartialCredit() > 0)){
								answerText = checkmarkGif + answerText;
							}else if(gdataAnswer.getIsCorrect() != null && !gdataAnswer.getIsCorrect()){
								answerText = crossmarkGif + answerText;
							}
						}
					} else if (bean.getTypeId().equals("9")) {
						log.debug("scoring a type 9 - matching");
						boolean itemHasCorrectAnswers = hasCorrectAnswers(gdataPubItemText.getAnswerSet());
						ItemGradingData thisItemGradingData = null;
						for (Object thisItem : allscores) {
							thisItemGradingData = (ItemGradingData) thisItem;
							log.debug("thisItemGradingData.getItemGradingId().intValue()={}", thisItemGradingData.getItemGradingId().intValue());
							log.debug("gdata.getItemGradingId().intValue()={}", gdata.getItemGradingId().intValue());
							log.debug("thisItemGradingData.getAnswerText()={}", thisItemGradingData.getAnswerText());
							if (thisItemGradingData.getItemGradingId().equals(gdata.getItemGradingId())) {
								break;
							}
						}
						if (thisItemGradingData != null) {
							log.debug("thisItemGradingData was found");
						}
						if (answerText.contains(noAnswer) && fullAnswerText.contains(noAnswer)) {
							log.debug("check point A");
							if (thisItemGradingData.getPublishedAnswerId() == null) {
								log.debug("null anwser id:thisItemGradingData.getAnswerText()={}", thisItemGradingData.getAnswerText());
								if (answerList.size() == 1) {
									TreeMap<Long, ItemTextIfc> thisItemOptions = allItemsHash.get(item.getItemId());
									StringBuilder optionsBuffer = new StringBuilder();
									for (Long thisItemKey : thisItemOptions.keySet()) {
										ItemTextIfc thisItemOptionText = (ItemTextIfc) thisItemOptions.get(thisItemKey);
										optionsBuffer.append(crossmarkGif).append(" ").append(thisItemOptionText.getSequence().toString()).append(":No Response <br/>");
									}
									answerText = optionsBuffer.toString();
								} else {
									answerText = crossmarkGif + gdataPubItemText.getSequence() + ":" + "No Response";
								}
							} else if (itemHasCorrectAnswers && thisItemGradingData.getPublishedAnswerId() < 0) {
								answerText = crossmarkGif + gdataPubItemText.getSequence() + ":" + noneOfTheAbove;
							} else {
								answerText = checkmarkGif + gdataPubItemText.getSequence() + ":" + noneOfTheAbove;
							}
							log.debug("answerText={}", answerText);
							String thisAgentId = gdata.getAgentId();
							boolean agentFound = false;
							for (Object thisAgent : bean.getAgents()) {
								AgentResults thisAgentResult = (AgentResults) thisAgent;
								if (thisAgentResult.getIdString().compareTo(thisAgentId) == 0) {
									agentFound = true;
									break;
								}
							}
						}
					}

					log.debug("check point B answerText={}", answerText);
					// -- Got the answer text --
					if (!answerList.get(0).equals(gdata)) { // We already have
						// an agentResults
						// for this one
						log.debug("check point C1");
						results.setAnswer(results.getAnswer() + "<br/>"
								+ answerText);
						if (gdata.getAutoScore() != null) {
							BigDecimal dataAutoScore = new BigDecimal(gdata.getAutoScore());
							BigDecimal exactTotalAutoScore = new BigDecimal(results.getExactTotalAutoScore());
							exactTotalAutoScore = exactTotalAutoScore.add(dataAutoScore);
							results.setTotalAutoScore(String.valueOf(exactTotalAutoScore.doubleValue()));
						}
						else {
							results.setTotalAutoScore(Double.toString((Double.valueOf(
									results.getExactTotalAutoScore())).doubleValue()));
						}
						results.setItemGradingAttachmentList(itemGradingAttachmentList);
						if (bean.getTypeId().equals("15")){ // CALCULATED_QUESTION Answer Key
							results.setAnswerKey(results.getAnswerKey()+ " <br/>" + answerKey);
						}
					} else {
						log.debug("check point C2");
						results.setItemGradingId(gdata.getItemGradingId());
						results.setAssessmentGradingId(gdata
								.getAssessmentGradingId());
						if (gdata.getAutoScore() != null) {
							// for example, if an assessment has one fileupload
							// question, the autoscore = null
							results.setTotalAutoScore(gdata.getAutoScore()
									.toString());
						} else {
							results.setTotalAutoScore(Double.toString(0));
						}
						results.setComments(ComponentManager.get(FormattedText.class).convertFormattedTextToPlaintext(gdata.getComments()));
						results.setAnswer(answerText);
						if (bean.getTypeId().equals("15")){ // CALCULATED_QUESTION Answer Key
							results.setAnswerKey(answerKey);
						}
						results.setFullAnswer(fullAnswerText);
						results.setRationale(rationale);
						results.setSubmittedDate(gdata.getSubmittedDate());
						if(gdata.getSubmittedDate() != null && gdata.getAttemptDate() != null) {
							results.setTimeElapsed((int)((gdata.getSubmittedDate().getTime() - gdata.getAttemptDate().getTime())/1000));
						} else {
							results.setTimeElapsed(0);
						}

						AgentFacade agent = new AgentFacade(gdata.getAgentId());
						results.setLastName(agent.getLastName());
						results.setFirstName(agent.getFirstName());
						results.setDisplayName(agent.getDisplayName());
						results.setEmail(agent.getEmail());
						if (results.getLastName() != null
								&& results.getLastName().length() > 0)
							results.setLastInitial(results.getLastName()
									.substring(0, 1));
						else if (results.getFirstName() != null
								&& results.getFirstName().length() > 0)
							results.setLastInitial(results.getFirstName()
									.substring(0, 1));
						else
							results.setLastInitial("Anonymous");
						results.setIdString(agent.getIdString());
						results.setAgentEid(agent.getEidString());
                        results.setAgentDisplayId(agent.getDisplayIdString());
						log.debug("testing agent getEid agent.getFirstname= "
                                + agent.getFirstName());
						log.debug("testing agent getEid agent.getid= "
								+ agent.getIdString());
						log.debug("testing agent getEid agent.geteid = "
								+ agent.getEidString());
                        log.debug("testing agent getDisplayId agent.getdisplayid = "
                                + agent.getDisplayIdString());

						results.setRole(agent.getRole());
						results.setItemGradingAttachmentList(itemGradingAttachmentList);
						agents.add(results);
					}
				}
			}

			bs = new BeanSort(agents, bean.getSortType());
			log.debug("check point D");
			if ((bean.getSortType()).equals("assessmentGradingId")
					|| (bean.getSortType()).equals("totalAutoScore")
					|| (bean.getSortType()).equals("totalOverrideScore")
					|| (bean.getSortType()).equals("finalScore")
					|| (bean.getSortType()).equals("timeElapsed")) {
				bs.toNumericSort();
			} else {
				bs.toStringSort();
			}

			if (bean.isSortAscending()) {
				log.debug("sortAscending");
				agents = (List) bs.sort();
			} else {
				log.debug("!sortAscending");
				agents = (List) bs.sortDesc();
			}

			if (bean.getTypeId().equals("9")) {
				agents = sortMatching(agents);
			}

			bean.setAgents(agents);
			bean.setAllAgents(agents);
			bean.setTotalPeople(Integer.valueOf(agents.size()).toString());
			bean.setAgentResultsByItemGradingId(agentResultsByItemGradingIdMap);

			bean.setRubricStateDetails("");
			ToolItemRubricAssociation tira = rubricsService.getRubricAssociation(RubricsConstants.RBCS_TOOL_SAMIGO, RubricsConstants.RBCS_PUBLISHED_ASSESSMENT_ENTITY_PREFIX + bean.getPublishedId() + "." + bean.getItemId()).orElse(null);
			boolean associated = tira != null ? true : false;
			bean.setHasAssociatedRubric(associated);
			if (associated) {
				String associationType = tira.getFormattedAssociation().get(RubricsConstants.RBCS_ASSOCIATE) != null ? tira.getFormattedAssociation().get(RubricsConstants.RBCS_ASSOCIATE) : "1";
				bean.setAssociatedRubricType(associationType);
			}
		}

		catch (RuntimeException e) {
			log.error(e.getMessage(), e);
			return false;
		}

		return true;
	}

	private List sortMatching(List<AgentResults> agentResults){

		List<AgentResults> returnValues = new ArrayList<>();
		for (AgentResults thisAgentResult : agentResults) {
			String thisItemOptions[] = thisAgentResult.getAnswer().split("<br/>");
			for (int s = 0; s < thisItemOptions.length; s++) {
				int endOfCheckmark = thisItemOptions[s].indexOf(">");
				int colonAt = thisItemOptions[s].indexOf(":");
				if(endOfCheckmark==-1||colonAt==-1) {
					continue;
				}
				String thisSequence = thisItemOptions[s].substring(endOfCheckmark, colonAt);
				StringBuilder editItemBuffer = new StringBuilder();
				editItemBuffer.append(thisSequence).append("|").append(thisItemOptions[s]);
				thisItemOptions[s] = editItemBuffer.toString();
			}
			Arrays.sort(thisItemOptions);
			StringBuilder optionBuffer = new StringBuilder();
			for (String thisItemOption : thisItemOptions) {
				int dlmIndex = thisItemOption.indexOf('|');
				if (dlmIndex != -1) {
					optionBuffer.append(thisItemOption.substring(dlmIndex + 1)).append("<br/>");
				}
			}
			log.debug("sortedOptions{}", optionBuffer);
			thisAgentResult.setAnswer(optionBuffer.toString());
			returnValues.add(thisAgentResult);
		}
		return returnValues;
	}

	/**
	 * getting a list of itemGrading for a publishedItemId is a lot of work,
	 * read the code in GradingService.getItemScores() after we get the list, we
	 * are saving it in QuestionScoreBean.itemScoresMap itemScoresMap =
	 * (publishedItemId, HashMap) = (Long publishedItemId, (Long
	 * publishedItemId, Array itemGradings)) itemScoresMap will be refreshed
	 * when the next QuestionScore link is click
	 */
	private Map getItemScores(Long publishedId, Long itemId, String which,
			boolean isValueChange) {
		log.debug("getItemScores");
		GradingService delegate = new GradingService();
		QuestionScoresBean questionScoresBean = (QuestionScoresBean) ContextUtil
				.lookupBean("questionScores");
		Map itemScoresMap = questionScoresBean.getItemScoresMap();
		log.debug("getItemScores: itemScoresMap ==null ?" + itemScoresMap);
		log.debug("getItemScores: isValueChange ?" + isValueChange);

		if (itemScoresMap == null || isValueChange || questionScoresBean.isAnyItemGradingAttachmentListModified()) {
			log
					.debug("getItemScores: itemScoresMap == null or isValueChange == true ");
			log.debug("getItemScores: isValueChange = " + isValueChange);
			itemScoresMap = new HashMap();
			questionScoresBean.setItemScoresMap(itemScoresMap);
			// reset this anyway (because the itemScoresMap will be refreshed as well as the 
			// attachment list)
			questionScoresBean.setAnyItemGradingAttachmentListModified(false);
		}
		log
				.debug("getItemScores: itemScoresMap.size() "
						+ itemScoresMap.size());
		Map map = (Map) itemScoresMap.get(itemId);
		if (map == null) {
			log.debug("getItemScores: map == null ");
			map = delegate.getItemScores(publishedId, itemId, which, true);
			log.debug("getItemScores: map size " + map.size());
			itemScoresMap.put(itemId, map);
		}
		return map;
	}

	private void setDurationIsOver(ItemDataIfc item, List<MediaData> mediaList) {
		try {
			int maxDurationAllowed = item.getDuration().intValue();
			for (int i = 0; i < mediaList.size(); i++) {
				MediaData m = mediaList.get(i);
				double duration = (Double.valueOf(m.getDuration())).doubleValue();
				if (duration > maxDurationAllowed) {
					m.setDurationIsOver(true);
					m.setTimeAllowed(String.valueOf(maxDurationAllowed));
				} else
					m.setDurationIsOver(false);
			}
		} catch (Exception e) {
			log.warn("**duration recorded is not an integer value="
					+ e.getMessage());
		}
	}

	private boolean hasCorrectAnswers(Set<AnswerIfc> answerSet){
		for (AnswerIfc thisAnswer : answerSet) {
			if (((PublishedAnswer) thisAnswer).getIsCorrect()) {
				return true;
			}
		}
		return false;
	}

	private void populateSections(PublishedAssessmentIfc publishedAssessment,
			QuestionScoresBean bean, TotalScoresBean totalBean,
			List scores, PublishedAssessmentService pubService) {
		List sections = new ArrayList();
		log.debug("questionScores(): populate sctions publishedAssessment.getSectionArraySorted size = {}",
				publishedAssessment.getSectionArraySorted().size());
		Iterator iter = publishedAssessment.getSectionArraySorted().iterator();
		int i = 1;
		while (iter.hasNext()) {
			SectionDataIfc section = (SectionDataIfc) iter.next();
			List items = new ArrayList();
			PartData part = new PartData();
			boolean isRandomDrawPart = pubService.isRandomDrawPart(
					publishedAssessment.getPublishedAssessmentId(), section
							.getSectionId());
			part.setIsRandomDrawPart(isRandomDrawPart);
			boolean isFixedRandomDrawPart = pubService.isFixedRandomDrawPart(
					publishedAssessment.getPublishedAssessmentId(), section
							.getSectionId());
			part.setIsFixedRandomDrawPart(isFixedRandomDrawPart);
			part.setPartNumber("" + i);
			part.setId(section.getSectionId().toString());

			if (isRandomDrawPart) {
				if (section.getSectionMetaDataByLabel(SectionDataIfc.NUM_QUESTIONS_DRAWN) !=null ) {
					int numberToBeDrawn = Integer.parseInt(section.getSectionMetaDataByLabel(SectionDataIfc.NUM_QUESTIONS_DRAWN));
					part.setNumberQuestionsDraw(numberToBeDrawn);
				}
				PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
				Set itemSet = publishedAssessmentService.getPublishedItemSet(publishedAssessment
					.getPublishedAssessmentId(), section.getSectionId());
				section.setItemSet(itemSet);
				part.setNumberQuestionsTotal(itemSet.size());
			} else if (isFixedRandomDrawPart) {
				int numberToBeFixed = 0;
				if (section.getSectionMetaDataByLabel(SectionDataIfc.NUM_QUESTIONS_DRAWN) !=null ) {
					int numberToBeDrawn = Integer.parseInt(section.getSectionMetaDataByLabel(SectionDataIfc.NUM_QUESTIONS_DRAWN));
					part.setNumberQuestionsDraw(numberToBeDrawn);
				}
				if (section.getSectionMetaDataByLabel(SectionDataIfc.NUM_QUESTIONS_FIXED) !=null ) {
					numberToBeFixed = Integer.parseInt(section.getSectionMetaDataByLabel(SectionDataIfc.NUM_QUESTIONS_FIXED));
					part.setNumberQuestionsFixed(numberToBeFixed);
				}
				PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
				Set itemSet = publishedAssessmentService.getPublishedItemSet(publishedAssessment
					.getPublishedAssessmentId(), section.getSectionId());
				section.setItemSet(itemSet);
				part.setNumberQuestionsTotal(itemSet.size() - numberToBeFixed);
			} else {
				GradingService gradingService = new GradingService();
				Set<PublishedItemData> itemSet = gradingService.getItemSet(publishedAssessment
					.getPublishedAssessmentId(), section.getSectionId());
				section.setItemSet(itemSet);
			}
			Iterator iter2 = section.getItemArraySortedForGrading().iterator();
			int j = 1;
			while (iter2.hasNext()) {
				ItemDataIfc item = (ItemDataIfc) iter2.next();
				PartData partitem = new PartData();

				partitem.setPartNumber("" + j);
				partitem.setId(item.getItemId().toString());
				log.debug("*   item.getId = " + item.getItemId());
				partitem.setLinked(true);
				partitem.setItemCancelled(ItemDataIfc.ITEM_DISTRIBUTED_CANCELLED == item.getCancellation()
						|| ItemDataIfc.ITEM_TOTAL_SCORE_CANCELLED == item.getCancellation());

				// Iterator iter3 = scores.iterator();
				items.add(partitem);
				j++;
			}
			log.debug("questionScores(): items size = " + items.size());
			part.setQuestionNumberList(items);
			sections.add(part);
			i++;
		}
		log.debug("questionScores(): sections size = " + sections.size());
		bean.setSections(sections);
	}
}
