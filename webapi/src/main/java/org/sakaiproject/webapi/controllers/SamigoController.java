/******************************************************************************
 * Copyright 2023 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.webapi.controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.assessment.data.dao.grading.StudentGradingSummaryData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacadeQueries;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.shared.api.assessment.SecureDeliveryServiceAPI;
import org.sakaiproject.tool.assessment.util.ExtendedTimeDeliveryService;
import org.sakaiproject.webapi.beans.AssessmentDeliveryRestBean;
import org.sakaiproject.webapi.beans.PageResponseRest;
import org.sakaiproject.webapi.beans.TimerBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestParam;


@Slf4j
@RestController
public class SamigoController extends AbstractSakaiApiController {

	private static final String QUESTION_TYPE = "question";
	private static final String PART_TYPE = "part";

	@Resource
	private SecurityService securityService;

	private PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();

	@GetMapping(value = "/assessmentgrading/{assessmentGradingId}/timerinfo/{type}/{itemId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public TimerBean getTimerInfo(@PathVariable String assessmentGradingId, @PathVariable String type, @PathVariable String itemId) {
		Session session = checkSakaiSession();
		String currentUserId = session.getUserId();
		
		log.debug("Get Timer info: currentUserId={}, type={}, itemId={}, assessmentGradingId={}", currentUserId, type, itemId, assessmentGradingId);

		TimerBean.TimerBeanBuilder ret = TimerBean.builder();
		GradingService gradingService = new GradingService();
		try {
			Date start;
			ret.id(Long.valueOf(itemId));
			switch(type) {
				case QUESTION_TYPE:
					start = gradingService.getLastItemGradingDataByAgent(itemId, currentUserId).getAttemptDate();
					ret.type(QUESTION_TYPE);
					break;
				case PART_TYPE:
					start = gradingService.getSectionGradingData(Long.valueOf(assessmentGradingId), Long.valueOf(itemId), currentUserId).getAttemptDate();
					ret.type(PART_TYPE);
					break;
				default:
					start = null;
			}
			
			if(start != null) {
				Date now = new Date();
				ret.timeElapsed((now.getTime() - start.getTime())/1000);
			}
		}catch(Exception e) {
			ret.timeElapsed(-1l);
			log.error("Error getting Timer info: currentUserId={}, type={}, itemId={}, assessmentGradingId={}", currentUserId, type, itemId, assessmentGradingId);
		}
		return ret.build();
	}

	@GetMapping(value = "/samigo/select/takeable", produces = MediaType.APPLICATION_JSON_VALUE)
	public PageResponseRest<AssessmentDeliveryRestBean> getTakeableAssessmentsPaged(
			@RequestParam String siteId,
			@RequestParam boolean ascending,
			@RequestParam String sort,
			@RequestParam String search,
			@RequestParam Integer page,
			@RequestParam Integer size) {

		checkSakaiSession();

		String returnType;

		switch (sort == null ? "title" : sort) {
			case "due":
				returnType = PublishedAssessmentFacadeQueries.DUE;
				break;
			case "timeLimit":
				returnType = PublishedAssessmentFacadeQueries.TIME_LIMIT;
				break;
			default:
				returnType = PublishedAssessmentFacadeQueries.TITLE;
				break;
		}

		Map<Long, Integer> h = publishedAssessmentService.getTotalSubmissionPerAssessment(
			AgentFacade.getAgentString(), siteId);

		List<PublishedAssessmentFacade> publishedAssessmentList =
			publishedAssessmentService.getBasicInfoOfAllPublishedAssessmentsByTitle(
			AgentFacade.getAgentString(), returnType,
			ascending, search, siteId);

		GradingService gradingService = new GradingService();
		// It contains two lists:
		// The first one is the list of assessment that are updated and need resubmit
		// The second one is the list of assessment that are updated but do not need resubmit
		List<List<Long>> list = gradingService.getUpdatedAssessmentList(AgentFacade.getAgentString(), siteId);

		List<Long> updatedAssessmentNeedResubmitList = new ArrayList<>();
		List<Long> updatedAssessmentList = new ArrayList<>();

		// separate the two lists
		if (list != null && list.size() == 2) {
			updatedAssessmentNeedResubmitList = list.get(0);
			updatedAssessmentList = list.get(1);
		}

		// filter out the one that the given user do not have right to access
		List<PublishedAssessmentFacade> takeableList = getTakeableList(publishedAssessmentList, h, updatedAssessmentNeedResubmitList, updatedAssessmentList);

		// 1c. prepare delivery bean
		List<AssessmentDeliveryRestBean> takeablePublishedList = new ArrayList<>();
		for (PublishedAssessmentFacade f : takeableList) {
			// note that this object is PublishedAssessmentFacade(assessmentBaseId,
			// title, releaseTo, startDate, dueDate, retractDate,lateHandling,
			// unlimitedSubmissions, submissionsAllowed). It
			// carries the min. info to create an index list. - daisyf
			AssessmentDeliveryRestBean delivery = new AssessmentDeliveryRestBean();
			delivery.setAssessmentId(f.getPublishedAssessmentId().toString());
			delivery.setAssessmentTitle(f.getTitle());
			delivery.setDueDate(f.getDueDate());
			delivery.setTimeRunning(false);// set to true in BeginDeliveryActionListener

			setTimedAssessment(delivery, f);

			// check pastDue
			delivery.setPastDue(f.getDueDate() != null && new Date().after(f.getDueDate()));

			delivery.setAssessmentUpdatedNeedResubmit(
				updatedAssessmentNeedResubmitList.contains(f.getPublishedAssessmentId())
			);

			delivery.setAssessmentUpdated(
				updatedAssessmentList.contains(f.getPublishedAssessmentId())
			);

			/* We need to make the alternative delivery URL available in webapi
				try {
					if (secureDelivery != null && secureDelivery.isSecureDeliveryAvaliable()) {
						final String moduleId = f.getAssessmentMetaDataByLabel(SecureDeliveryServiceAPI.MODULE_KEY);
						if (moduleId != null) {
							delivery.setAlternativeDeliveryUrl(secureDelivery.getAlternativeDeliveryUrl(
									moduleId,
									Long.valueOf(delivery.getAssessmentId()),
									AgentFacade.getAgentString()
							).orElse("") );
						}
					}
				} catch (Exception e) {
					log.debug("SecureDelivery lookup failed for assessment {}", delivery.getAssessmentId(), e);
				}
			*/

			takeablePublishedList.add(delivery);
		}

		int totalElements = takeablePublishedList.size();
		int fromIndex = (page - 1) * size;
		if (fromIndex > totalElements) fromIndex = totalElements;
		int toIndex = Math.min(fromIndex + size, totalElements);

		List<AssessmentDeliveryRestBean> pageContent = takeablePublishedList.subList(fromIndex, toIndex);

		return new PageResponseRest<>(pageContent, page, size, totalElements);
	}

	private void setTimedAssessment(AssessmentDeliveryRestBean delivery, PublishedAssessmentFacade pubAssessment){
		if (pubAssessment.getTimeLimit() != null) {
			int seconds = pubAssessment.getTimeLimit();
			int hour = 0;
			int minute = 0;
			if (seconds>=3600) {
				hour = Math.abs(seconds/3600);
				minute =Math.abs((seconds-hour*3600)/60);
			}
			else {
				minute = Math.abs(seconds/60);
			}
			delivery.setTimeLimit_hour(hour);
			delivery.setTimeLimit_minute(minute);
		}

		else{
			delivery.setTimeLimit_hour(0);
			delivery.setTimeLimit_minute(0);
		}
	}
	// go through the pub list retrieved from DB and check if
	// agent is authorizaed and filter out the one that does not meet the
	// takeable criteria.
	// SAK-1464: we also want to filter out assessment released To Anonymous Users
	private List<PublishedAssessmentFacade> getTakeableList(List assessmentList, Map <Long,Integer> h, List updatedAssessmentNeedResubmitList, List updatedAssessmentList) {
		List<PublishedAssessmentFacade> takeableList = new ArrayList<>();
		GradingService gradingService = new GradingService();
		Map<Long, StudentGradingSummaryData> numberRetakeHash = gradingService.getNumberRetakeHash(AgentFacade.getAgentString());
		Map<Long, Integer> actualNumberRetake = gradingService.getActualNumberRetakeHash(AgentFacade.getAgentString());
		ExtendedTimeDeliveryService extendedTimeDeliveryService;
		for (int i = 0; i < assessmentList.size(); i++) {
		PublishedAssessmentFacade f = (PublishedAssessmentFacade)assessmentList.get(i);
				// Handle extended time info
				extendedTimeDeliveryService = new ExtendedTimeDeliveryService(f);
				if (extendedTimeDeliveryService.hasExtendedTime()) {
					f.setStartDate(extendedTimeDeliveryService.getStartDate());
					f.setDueDate(extendedTimeDeliveryService.getDueDate());
					//Override late handling here, availability check done later
					if (extendedTimeDeliveryService.getRetractDate() != null) {
						f.setRetractDate(extendedTimeDeliveryService.getRetractDate());
						f.setLateHandling(AssessmentAccessControlIfc.ACCEPT_LATE_SUBMISSION);
					}
					f.setTimeLimit(extendedTimeDeliveryService.getTimeLimit());
				}
		if (f.getReleaseTo()!=null && !("").equals(f.getReleaseTo())
			&& !f.getReleaseTo().contains("Anonymous Users") ) {
			if (isAvailable(f, h, numberRetakeHash, actualNumberRetake, updatedAssessmentNeedResubmitList, updatedAssessmentList)) {
				takeableList.add(f);
			}
		}
		}
		return takeableList;
	}

	public boolean isAvailable(PublishedAssessmentFacade f, Map <Long, Integer> h, Map<Long, StudentGradingSummaryData> numberRetakeHash, Map <Long, Integer> actualNumberRetakeHash, List updatedAssessmentNeedResubmitList, List updatedAssessmentList) {
		boolean returnValue = false;
		//1. prepare our significant parameters
		Integer status = f.getStatus();
		Date currentDate = new Date();
		Date startDate = f.getStartDate();
		Date dueDate = f.getDueDate();
		Date retractDate = f.getRetractDate();
		boolean acceptLateSubmission = AssessmentAccessControlIfc.ACCEPT_LATE_SUBMISSION.equals(f.getLateHandling());

		if (dueDate == null && (retractDate != null && acceptLateSubmission)) {
			dueDate = retractDate;
		}

		if (!Integer.valueOf(1).equals(status)) {
			return false;
		}
		
		if (startDate != null && startDate.after(currentDate)) {
			return false;
		}

		int totalSubmitted = 0;

		//boolean notSubmitted = false;
		if (h.get(f.getPublishedAssessmentId()) != null){
		totalSubmitted = ((Integer) h.get(f.getPublishedAssessmentId()));
		}
		
		if (acceptLateSubmission && (dueDate != null && dueDate.before(currentDate)) && retractDate == null && totalSubmitted == 0) {
		return true;
		}
		
		if (acceptLateSubmission
				&& (dueDate != null && dueDate.before(currentDate))
				&& (retractDate == null || retractDate.before(currentDate))) {
			return false;
		}
		
		if (updatedAssessmentNeedResubmitList.contains(f.getPublishedAssessmentId()) || updatedAssessmentList.contains(f.getPublishedAssessmentId())) {
			return true;
		}
		
		int maxSubmissionsAllowed = 9999;
		if ( (Boolean.FALSE).equals(f.getUnlimitedSubmissions())){
		maxSubmissionsAllowed = f.getSubmissionsAllowed();
		}

		int numberRetake = 0;
		if (numberRetakeHash.get(f.getPublishedAssessmentId()) != null) {
			numberRetake = (((StudentGradingSummaryData) numberRetakeHash.get(f.getPublishedAssessmentId())).getNumberRetake());
		}
		
		//2. time to go through all the criteria
		// Tests if dueDate has passed
		if (dueDate != null && dueDate.before(currentDate)) {
			// DUE DATE HAS PASSED
			if (acceptLateSubmission) {
				// LATE SUBMISSION ARE HANDLED: The assessment is available in these situations:
				//    * Is the first submission
				//    * A retake has been granted 
				// (if late submission are handled, a previous test implies that retract date has not yet passed)
				if (totalSubmitted == 0) {
					returnValue = true;
				} else {
					int actualNumberRetake = 0;
					if (actualNumberRetakeHash.get(f.getPublishedAssessmentId()) != null) {
						actualNumberRetake = (actualNumberRetakeHash.get(f.getPublishedAssessmentId()));
					}
					if (actualNumberRetake < numberRetake) {
						returnValue = true;
					} else {
						returnValue = false;
					}
				}
			} else {
				returnValue = false;
			}
		}
		else {
			if (totalSubmitted < maxSubmissionsAllowed + numberRetake) {
				returnValue = true;
			}
		}
		
		return returnValue;
	}
}
