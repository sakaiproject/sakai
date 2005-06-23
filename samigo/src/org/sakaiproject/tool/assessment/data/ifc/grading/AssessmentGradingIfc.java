/**********************************************************************************
* $HeadURL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/
package org.sakaiproject.tool.assessment.data.ifc.grading;
import java.util.Date;
import java.util.Set;

import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;

public interface AssessmentGradingIfc
    extends java.io.Serializable{

  Long getAssessmentGradingId();

  void setAssessmentGradingId(Long assessmentGradingId);

  PublishedAssessmentIfc getPublishedAssessment();

  void setPublishedAssessment(PublishedAssessmentIfc publishedAssessment);

  String getAgentId();

  void setAgentId(String agentId);

  //AgentIfc getAgent();

  Date getSubmittedDate();

  void setSubmittedDate(Date submittedDate);

  // Is isLate determined by comparing the submitted date with the duedate
  // of published assessment or core assessment?
  // if the former, then we need to store the duedate info in DB
  // if latter, isLate is determined on the fly -
  // 'cos core assessment due date can be changed.
  Boolean getIsLate();

  void setIsLate(Boolean isLate);

  Boolean getForGrade();

  void setForGrade(Boolean forGrade);

  // sum of item score through auto scoring
  Float getTotalAutoScore();

  void setTotalAutoScore(Float totalAutoScore);

  // sum of item score through instructor grading
  Float getTotalOverrideScore();

  void setTotalOverrideScore(Float totalOverrideScore);

  // grader can override the total score with a final score
  Float getFinalScore();

  void setFinalScore(Float finalScore);

  String getComments();

  void setComments(String comments);

  String getGradedBy();

  void setGradedBy(String GradedBy);

  Date getGradedDate();

  void setGradedDate(Date GradedDate);

  Integer getStatus();

  void setStatus(Integer status);

  Set getItemGradingSet();

  void setItemGradingSet(Set itemGradingSet);

  Date getAttemptDate();

  void setAttemptDate(Date attemptDate);

  Integer getTimeElapsed();

  void setTimeElapsed(Integer timeElapsed);

}
