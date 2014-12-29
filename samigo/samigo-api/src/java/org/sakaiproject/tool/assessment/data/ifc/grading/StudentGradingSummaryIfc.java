/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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



package org.sakaiproject.tool.assessment.data.ifc.grading;

import java.util.Date;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public interface StudentGradingSummaryIfc
    extends java.io.Serializable{
  public Long getStudentGradingSummaryId() ;
  public void setStudentGradingSummaryId(Long studentGradingSummaryId);

  public Long getPublishedAssessmentId();
  public void setPublishedAssessmentId(Long publishedAssessmentId);

  public String getAgentId();
  public void setAgentId(String agentId);
  
  public Integer getNumberRetake();
  public void setNumberRetake(Integer numberRetake);
  
  public String getCreatedBy();
  public void setCreatedBy(String createdBy);

  public Date getCreatedDate();
  public void setCreatedDate(Date createdDate);

  public String getLastModifiedBy();
  public void setLastModifiedBy(String lastModifiedBy);

  public Date getLastModifiedDate();
  public void setLastModifiedDate(Date lastModifiedDate);

}
