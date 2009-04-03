/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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



package org.sakaiproject.tool.assessment.data.ifc.grading;

import java.util.Date;

import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public interface AssessmentGradingSummaryIfc
    extends java.io.Serializable{
  Long getAssessmentGradingSummaryId();
  void setAssessmentGradingSummaryId(Long assessmentGradingSummaryId);
  PublishedAssessmentIfc getPublishedAssessment();
  void setPublishedAssessment(PublishedAssessmentIfc publishedAssessment);
  String getAgentId();
  void setAgentId(String agentId);
  Integer getTotalSubmitted();
  void setTotalSubmitted(Integer totalSubmitted);
  Integer getTotalSubmittedForGrade();
  void setTotalSubmittedForGrade(Integer totalSubmittedForGrade);
  AssessmentGradingIfc getLastSubmittedAssessmentGrading();
  void setLastSubmittedAssessmentGrading(AssessmentGradingIfc lastSubmittedAssessmentGrading);
  Date getLastSubmittedDate();
  void setLastSubmittedDate(Date lastSubmittedDate);
  Boolean getLastSubmittedAssessmentIsLate();
  void setLastSubmittedAssessmentIsLate(Boolean lastSubmittedAssessmentIsLate);
  Float getSumOf_autoScoreForGrade();
  void setSumOf_autoScoreForGrade(Float sumOf_autoScoreForGrade);
  Float getAverage_autoScoreForGrade();
  void setAverage_autoScoreForGrade(Float average_autoScoreForGrade);
  Float getHighest_autoScoreForGrade();
  void setHighest_autoScoreForGrade(Float highest_autoScoreForGrade);
  Float getLowest_autoScoreForGrade();
  void setLowest_autoScoreForGrade(Float lowest_autoScoreForGrade);
  Float getLast_autoScoreForGrade() ;
  void setLast_autoScoreForGrade(Float last_autoScoreForGrade);
  Float getSumOf_overrideScoreForGrade();
  void setSumOf_overrideScoreForGrade(Float sumOf_overrideScoreForGrade);
  Float getAverage_overrideScoreForGrade();
  void setAverage_overrideScoreForGrade(Float average_overrideScoreForGrade);
  Float getHighest_overrideScoreForGrade();
  void setHighest_overrideScoreForGrade(Float highest_overrideScoreForGrade);
  Float getLowest_overrideScoreForGrade();
  void setLowest_overrideScoreForGrade(Float lowest_overrideScoreForGrade);
  Float getLast_overrideScoreForGrade();
  void setLast_overrideScoreForGrade(Float last_overrideScoreForGrade);
  Integer getScoringType();
  void setScoringType(Integer scoringType);
  AssessmentGradingIfc getAcceptedAssessmentGrading();
  void setAcceptedAssessmentGrading(AssessmentGradingIfc acceptedAssessmentGrading);
  Boolean getAcceptedAssessmentIsLate();
  void setAcceptedAssessmentIsLate(Boolean acceptedAssessmentIsLate);
  Float getFinalAssessmentScore();
  void setFinalAssessmentScore(Float finalAssessmentScore);
  Boolean getFeedToGradeBook();
  void setFeedToGradeBook(Boolean feedToGradeBook);

}
