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
package org.sakaiproject.tool.assessment.data.dao.grading;

import java.util.Comparator;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;

/**
 * For sorting AssessmentGradingData by score and display id
 *
 * @author Gopal Ramasammy-Cook
 * @version 1.0
 */
@Slf4j
public class AssessmentGradingComparatorByScoreAndUniqueIdentifier implements
		Comparator {

	private boolean anonymous;
	
	public AssessmentGradingComparatorByScoreAndUniqueIdentifier(boolean anonymous) {
		this.anonymous = anonymous;
	}

	public int compare(Object arg0, Object arg1) {
		// TODO Auto-generated method stub
		
		if (arg0 == null || arg1 == null) {
			throw new RuntimeException("AssessmentGradingData objects expected for comparrison by AssessmentGradingDataScoreAndDisplayIdComparator - null received. "); 			
		}
		if (!(arg0 instanceof AssessmentGradingData) || !(arg1 instanceof AssessmentGradingData)) {
			throw new RuntimeException("AssessmentGradingData objects expected for comparrison by AssessmentGradingDataScoreAndDisplayIdComparator. "); 			
		}
		
		AssessmentGradingData assessmentGrading0 = (AssessmentGradingData) arg0;
		AssessmentGradingData assessmentGrading1 = (AssessmentGradingData) arg1;
		
		Double zero =  Double.valueOf(0);
		Double score0 = assessmentGrading0.getFinalScore() == null ? zero : assessmentGrading0.getFinalScore();
		Double score1 = assessmentGrading1.getFinalScore() == null ? zero : assessmentGrading1.getFinalScore();
		
		//String id0=null, id1=null;
		
		if (!(score0.equals(score1)))
			return score0.compareTo(score1);
		else {
			if (anonymous) {
				return assessmentGrading0.getAssessmentGradingId()
				.compareTo(assessmentGrading1.getAssessmentGradingId());
			}
			else {
				String agentEid0=null, agentEid1=null;
				try {
					agentEid0 = UserDirectoryService.getUser(assessmentGrading0.getAgentId()).getEid();
					agentEid1 = UserDirectoryService.getUser(assessmentGrading1.getAgentId()).getEid();
					return agentEid0.compareTo(agentEid1);
				} catch (Exception e) {
					if (agentEid0==null && agentEid1==null) {
						log.warn("agentEid0==null && agentEid1==null");
						return 0;
					}
					else if (agentEid0==null) {
						log.warn("agentEid0 == null");
						return -1;
					}
					else if (agentEid1==null) {
						log.warn("agentEid1 == null");
						return 1;
					}
					else {
						return agentEid0.compareTo(agentEid1);
					}
				}
			}
		}
	}

}
