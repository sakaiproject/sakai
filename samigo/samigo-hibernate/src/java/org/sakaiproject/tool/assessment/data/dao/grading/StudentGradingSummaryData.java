/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/samigo-hibernate/src/java/org/sakaiproject/tool/assessment/data/dao/grading/AssessmentGradingSummaryData.java $
 * $Id: AssessmentGradingSummaryData.java 15083 2006-09-20 20:03:55Z lydial@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.data.dao.grading;

import java.util.Date;

import org.sakaiproject.tool.assessment.data.ifc.grading.StudentGradingSummaryIfc;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class StudentGradingSummaryData
    implements StudentGradingSummaryIfc
{
  private Long studentGradingSummaryId;
  private Long publishedAssessmentId;
  private String agentId;
  private Integer numberRetake;
  private String createdBy;
  private Date createdDate;
  private String lastModifiedBy;
  private Date lastModifiedDate;
  
  public StudentGradingSummaryData() {
  }

  public Long getStudentGradingSummaryId() {
    return studentGradingSummaryId;
  }
  public void setStudentGradingSummaryId(Long studentGradingSummaryId) {
    this.studentGradingSummaryId = studentGradingSummaryId;
  }

  public Long getPublishedAssessmentId() {
    return publishedAssessmentId;
  }
  public void setPublishedAssessmentId(Long publishedAssessmentId) {
    this.publishedAssessmentId = publishedAssessmentId;
  }

  public String getAgentId() {
    return agentId;
  }
  public void setAgentId(String agentId) {
    this.agentId = agentId;
  }
  
  public Integer getNumberRetake() {
    return numberRetake;
  }
  public void setNumberRetake(Integer numberRetake) {
    this.numberRetake = numberRetake;
  }
  public String getCreatedBy() {
    return this.createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public Date getCreatedDate() {
    return this.createdDate;
  }

  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }

  public String getLastModifiedBy() {
    return this.lastModifiedBy;
  }

  public void setLastModifiedBy(String lastModifiedBy) {
    this.lastModifiedBy = lastModifiedBy;
  }

  public Date getLastModifiedDate() {
    return this.lastModifiedDate;
  }

  public void setLastModifiedDate(Date lastModifiedDate) {
    this.lastModifiedDate = lastModifiedDate;
  }
}
