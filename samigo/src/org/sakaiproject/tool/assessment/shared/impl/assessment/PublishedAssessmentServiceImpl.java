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
package org.sakaiproject.tool.assessment.shared.impl.assessment;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.shared.api.assessment.PublishedAssessmentServiceAPI;

/** @todo implement methods
 * PublishedAssessmentServiceImpl implements a shared interface to get/set
 * published assessment information.
 * @author Ed Smiley <esmiley@stanford.edu>
 */
public class PublishedAssessmentServiceImpl implements PublishedAssessmentServiceAPI
{
  private static Log log = LogFactory.getLog(PublishedAssessmentServiceImpl.class);

  /**
   * rachelgollub: So takeable is that you have *not* reached the number of
   * submissions and you're either before the due date or (you're after the due
   * date, you haven't submitted yet, and late handling is enabled).
   * - quoted from IM on 1/31/05
   * Marc said some of teh assessment do not have any due date, e.g. survey
   *
   * @param agentId
   * @param orderBy
   * @param ascending
   * @return
   */
  public List getBasicInfoOfAllPublishedAssessments(String agentId, String orderBy, boolean ascending)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.assessment.PublishedAssessmentServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method getBasicInfoOfAllPublishedAssessments() not yet implemented.");
  }
  public List getAllActivePublishedAssessments(String orderBy)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.assessment.PublishedAssessmentServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method getAllActivePublishedAssessments() not yet implemented.");
  }
  public List getAllActivePublishedAssessments(int pageSize, int pageNumber, String orderBy)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.assessment.PublishedAssessmentServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method getAllActivePublishedAssessments() not yet implemented.");
  }
  public List getAllInActivePublishedAssessments(String orderBy)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.assessment.PublishedAssessmentServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method getAllInActivePublishedAssessments() not yet implemented.");
  }
  public List getAllInActivePublishedAssessments(int pageSize, int pageNumber, String orderBy)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.assessment.PublishedAssessmentServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method getAllInActivePublishedAssessments() not yet implemented.");
  }
  public List getAllPublishedAssessments(String orderBy, Integer status)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.assessment.PublishedAssessmentServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method getAllPublishedAssessments() not yet implemented.");
  }
  public List getAllPublishedAssessments(int pageSize, int pageNumber, String orderBy, Integer status)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.assessment.PublishedAssessmentServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method getAllPublishedAssessments() not yet implemented.");
  }
  public PublishedAssessmentIfc getPublishedAssessment(String assessmentId)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.assessment.PublishedAssessmentServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method getPublishedAssessment() not yet implemented.");
  }
  public Long getPublishedAssessmentId(String assessmentId)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.assessment.PublishedAssessmentServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method getPublishedAssessmentId() not yet implemented.");
  }
  public PublishedAssessmentIfc publishAssessment(AssessmentIfc assessment)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.assessment.PublishedAssessmentServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method publishAssessment() not yet implemented.");
  }
  public PublishedAssessmentIfc publishPreviewAssessment(AssessmentIfc assessment)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.assessment.PublishedAssessmentServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method publishPreviewAssessment() not yet implemented.");
  }
  public void saveAssessment(PublishedAssessmentIfc assessment)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.assessment.PublishedAssessmentServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method saveAssessment() not yet implemented.");
  }
  public void removeAssessment(String assessmentId)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.assessment.PublishedAssessmentServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method removeAssessment() not yet implemented.");
  }

  /**
 * return an array list of the last AssessmentGradingIfc per assessment that
 * a user has submitted for grade.
 * @param agentId
 * @param orderBy
 * @param ascending
 * @return
 */
  public List getBasicInfoOfAllActivePublishedAssessments(String orderBy, boolean ascending)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.assessment.PublishedAssessmentServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method getBasicInfoOfAllActivePublishedAssessments() not yet implemented.");
  }
  public List getBasicInfoOfAllInActivePublishedAssessments(String orderBy, boolean ascending)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.assessment.PublishedAssessmentServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method getBasicInfoOfAllInActivePublishedAssessments() not yet implemented.");
  }
  public PublishedAssessmentIfc getSettingsOfPublishedAssessment(String assessmentId)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.assessment.PublishedAssessmentServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method getSettingsOfPublishedAssessment() not yet implemented.");
  }
  public ItemDataIfc loadPublishedItem(String itemId)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.assessment.PublishedAssessmentServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method loadPublishedItem() not yet implemented.");
  }
  public ItemTextIfc loadPublishedItemText(String itemTextId)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.assessment.PublishedAssessmentServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method loadPublishedItemText() not yet implemented.");
  }
  public List getBasicInfoOfLastSubmittedAssessments(String agentId, String orderBy, boolean ascending)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.assessment.PublishedAssessmentServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method getBasicInfoOfLastSubmittedAssessments() not yet implemented.");
  }

  /**
 *
 * @param agentId
 * @return Map (Long publishedAssessmentId, Integer totalSubmittedForGrade);
 */

  public Map getTotalSubmissionPerAssessment(String agentId)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.assessment.PublishedAssessmentServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method getTotalSubmissionPerAssessment() not yet implemented.");
  }
  public Integer getTotalSubmission(String agentId, String publishedAssessmentId)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.assessment.PublishedAssessmentServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method getTotalSubmission() not yet implemented.");
  }
  public PublishedAssessmentIfc getPublishedAssessmentIdByAlias(String alias)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.assessment.PublishedAssessmentServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method getPublishedAssessmentIdByAlias() not yet implemented.");
  }
  public void saveOrUpdateMetaData(ItemMetaDataIfc meta)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.assessment.PublishedAssessmentServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method saveOrUpdateMetaData() not yet implemented.");
  }
  public Map getFeedbackHash()
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.assessment.PublishedAssessmentServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method getFeedbackHash() not yet implemented.");
  }
  public Map getAllAssessmentsReleasedToAuthenticatedUsers()
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.assessment.PublishedAssessmentServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method getAllAssessmentsReleasedToAuthenticatedUsers() not yet implemented.");
  }
  public String getPublishedAssessmentOwner(Long publishedAssessmentId)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.shared.api.assessment.PublishedAssessmentServiceAPI method*/
    throw new java.lang.UnsupportedOperationException("Method getPublishedAssessmentOwner() not yet implemented.");
  }
}