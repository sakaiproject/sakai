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




package org.sakaiproject.tool.assessment.shared.api.assessment;

import java.util.List;
import java.util.Map;

import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;

/**
 * The PublishedAssessmentServiceAPI declares a shared interface to get/set assessment
 * information for published assessments.
 * @author Ed Smiley <esmiley@stanford.edu>
 */
public interface PublishedAssessmentServiceAPI {

  public List getBasicInfoOfAllPublishedAssessments(String agentId, String orderBy,
                                             boolean ascending, String siteId);

  public List getAllActivePublishedAssessments(String orderBy);

  public List getAllActivePublishedAssessments(
      int pageSize, int pageNumber, String orderBy);

  public List getAllInActivePublishedAssessments(String orderBy);

  public List getAllInActivePublishedAssessments(
      int pageSize, int pageNumber, String orderBy);

  public List getAllPublishedAssessments(String orderBy, Integer status);

  public List getAllPublishedAssessments(
      int pageSize, int pageNumber, String orderBy, Integer status);

  public PublishedAssessmentIfc getPublishedAssessment(String assessmentId);

  public Long getPublishedAssessmentId(String assessmentId);
  
  public PublishedAssessmentIfc publishPreviewAssessment(AssessmentIfc
      assessment, String protocol);

  public void saveAssessment(PublishedAssessmentIfc assessment);

  public void removeAssessment(String assessmentId);

  public List getBasicInfoOfAllActivePublishedAssessments(String orderBy, boolean ascending);

  public List getBasicInfoOfAllInActivePublishedAssessments(String orderBy, boolean ascending);

  public PublishedAssessmentIfc getSettingsOfPublishedAssessment(String assessmentId);

  public ItemDataIfc loadPublishedItem(String itemId);

  public ItemTextIfc loadPublishedItemText(String itemTextId);

  public List getBasicInfoOfLastSubmittedAssessments(String agentId,
      String orderBy, boolean ascending);

  public Map getTotalSubmissionPerAssessment(String agentId);

  public Integer getTotalSubmission(String agentId, String publishedAssessmentId);

  public PublishedAssessmentIfc getPublishedAssessmentIdByAlias(String alias);

  public void saveOrUpdateMetaData(ItemMetaDataIfc meta);

  public Map getFeedbackHash();

  public Map getAllAssessmentsReleasedToAuthenticatedUsers();

  public String getPublishedAssessmentOwner(Long publishedAssessmentId);

}
