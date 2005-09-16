/**********************************************************************************
* $URL: https://source.sakaiproject.org/svn/trunk/sakai/sam/src/org/sakaiproject/tool/assessment/services/assessment/PublishedAssessmentService.java $
* $Id: PublishedAssessmentService.java 632 2005-07-14 21:22:50Z janderse@umich.edu $
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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
                                             boolean ascending);

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

  public PublishedAssessmentIfc publishAssessment(AssessmentIfc assessment);

  public PublishedAssessmentIfc publishPreviewAssessment(AssessmentIfc
      assessment);

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
