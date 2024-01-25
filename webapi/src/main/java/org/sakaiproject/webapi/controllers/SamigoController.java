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

import java.util.Date;

import javax.annotation.Resource;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.webapi.beans.TimerBean;
import org.sakaiproject.webapi.beans.TimerBean.TimerBeanBuilder;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class SamigoController extends AbstractSakaiApiController {
	
	private static final String QUESTION_TYPE = "question";
	private static final String PART_TYPE = "part";

	@Resource
	private SecurityService securityService;

	@GetMapping(value = "/assessmentgrading/{assessmentGradingId}/timerinfo/{type}/{itemId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public TimerBean getTimerInfo(@PathVariable String assessmentGradingId, @PathVariable String type, @PathVariable String itemId) {
		Session session = checkSakaiSession();
		String currentUserId = session.getUserId();
		
		log.debug("Get Timer info: currentUserId={}, type={}, itemId={}, assessmentGradingId={}", currentUserId, type, itemId, assessmentGradingId);

		TimerBeanBuilder ret = TimerBean.builder();
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
}
