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
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.shared.api.assessment.AssessmentServiceException;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemMetaData;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.facade.ItemFacade;

/**
 * PublishedAssessmentServiceImpl implements a shared interface to get/set
 * published assessment information.
 * @author Ed Smiley <esmiley@stanford.edu>
 */
public class PublishedAssessmentServiceImpl implements PublishedAssessmentServiceAPI
{
  private static Log log = LogFactory.getLog(PublishedAssessmentServiceImpl.class);

  /**
  * Get list of all active published assessments with basic info populated.
  * @param agentId teh agent takign the assessments
  * @param orderBy sort order field.
  * @return the list.
  */
// * rachelgollub: So takeable is that you have *not* reached the number of
// * submissions and you're either before the due date or (you're after the due
// * date, you haven't submitted yet, and late handling is enabled).
// * - quoted from IM on 1/31/05
// * Marc said some of teh assessment do not have any due date, e.g. survey
// *
  public List getBasicInfoOfAllPublishedAssessments(String agentId, String orderBy,
    boolean ascending)
  {
    try
    {
      PublishedAssessmentService service = new PublishedAssessmentService();
      return service.getBasicInfoOfAllPublishedAssessments(
       agentId, orderBy, ascending);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }

  /**
   * Get list of all active published assessments.
   * @param orderBy sort order field.
   * @return the list.
   */
  public List getAllActivePublishedAssessments(String orderBy)
  {
    try
    {
      PublishedAssessmentService service = new PublishedAssessmentService();
      return service.getAllActivePublishedAssessments(orderBy);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }

  /**
   * Get list of all active published assessments.
   * @param pageSize number in a page
   * @param pageNumber number of the page
   * @param orderBy sort order field.
   * @return the list.
   */
  public List getAllActivePublishedAssessments(int pageSize, int pageNumber, String orderBy)
  {
    try
    {
      PublishedAssessmentService service = new PublishedAssessmentService();
      return service.getAllActivePublishedAssessments(pageSize, pageNumber, orderBy);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }
  /**
   * Get list of all inactive published assessments.
   * @param orderBy sort order field.
   * @return the list.
   */
  public List getAllInActivePublishedAssessments(String orderBy)
  {
    try
    {
      PublishedAssessmentService service = new PublishedAssessmentService();
      return service.getAllInActivePublishedAssessments(orderBy);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }
  /**
   * Get list of all inactive published assessments.
   * @param pageSize number in a page
   * @param pageNumber number of the page
   * @param orderBy sort order field.
   * @return the list.
   */

  public List getAllInActivePublishedAssessments(int pageSize, int pageNumber, String orderBy)
  {
    try
    {
      PublishedAssessmentService service = new PublishedAssessmentService();
      return service.getAllInActivePublishedAssessments(pageSize, pageNumber, orderBy);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }
  /**
   * Get list of all inactive published assessments.
   * @param orderBy sort order field.
   * @param status the status code
   * @return the list.
   */

  public List getAllPublishedAssessments(String orderBy, Integer status)
  {
    try
    {
      PublishedAssessmentService service = new PublishedAssessmentService();
      return service.getAllPublishedAssessments(orderBy, status);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }

  /**
   * Get list of all inactive published assessments.
   * @param pageSize number in a page
   * @param pageNumber number of the page
   * @param orderBy sort order field.
   * @param status the status
   * @return the list.
   */
  public List getAllPublishedAssessments(int pageSize, int pageNumber, String orderBy, Integer status)
  {
    try
    {
      PublishedAssessmentService service = new PublishedAssessmentService();
      return service.getAllPublishedAssessments(pageSize, pageNumber,
                                                orderBy, status);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }

  /**
   * Get published assessment.
   * @param assessmentId the published assessment id string
   * @return teh published assessment.
   */
  public PublishedAssessmentIfc getPublishedAssessment(String assessmentId)
  {
    try
    {
      PublishedAssessmentService service = new PublishedAssessmentService();
      return service.getPublishedAssessment(assessmentId);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }
  public Long getPublishedAssessmentId(String assessmentId)
  {
    try
    {
      PublishedAssessmentService service = new PublishedAssessmentService();
      return service.getPublishedAssessmentId(assessmentId);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }
  public PublishedAssessmentIfc publishAssessment(AssessmentIfc assessment)
  {
    try
    {
      PublishedAssessmentService service = new PublishedAssessmentService();
      AssessmentService assessmentService = new AssessmentService();
      AssessmentFacade facade = assessmentService.getAssessment(
        assessment.getAssessmentId().toString());
      return service.publishAssessment(facade);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }
  public PublishedAssessmentIfc publishPreviewAssessment(AssessmentIfc assessment)
  {
    try
    {
      PublishedAssessmentService service = new PublishedAssessmentService();
      AssessmentService assessmentService = new AssessmentService();
      AssessmentFacade facade = assessmentService.getAssessment(
        assessment.getAssessmentId().toString());
      return service.publishPreviewAssessment(facade);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }
  public void saveAssessment(PublishedAssessmentIfc assessment)
  {
    try
    {
      PublishedAssessmentService service = new PublishedAssessmentService();
      PublishedAssessmentFacade facade = service.getPublishedAssessment(
        assessment.getAssessmentId().toString());
      service.saveAssessment(facade);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }
  public void removeAssessment(String assessmentId)
  {
    try
    {
      PublishedAssessmentService service = new PublishedAssessmentService();
      service.removeAssessment(assessmentId);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }

  /**
   * Get list of all active published assessments with only basic info populated.
   * @param ascending true if ascending sort.
   * @param orderBy sort order field.
   * @return the list.
   */
  public List getBasicInfoOfAllActivePublishedAssessments(String orderBy, boolean ascending)
  {
    try
    {
      PublishedAssessmentService service = new PublishedAssessmentService();
      return service.getBasicInfoOfAllActivePublishedAssessments(orderBy, ascending);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }

  /**
   * Get list of all inactive published assessments with only basic info populated.
   * @param ascending true if ascending sort.
   * @param orderBy sort order field.
   * @return the list.
   */
  public List getBasicInfoOfAllInActivePublishedAssessments(String orderBy, boolean ascending)
  {
    try
    {
      PublishedAssessmentService service = new PublishedAssessmentService();
      return service.getBasicInfoOfAllInActivePublishedAssessments(orderBy, ascending);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }

  /**
   * Get setttings of published assessment.
   * @param assessmentId teh published assessment id string
   * @return teh published assessment
   */
  public PublishedAssessmentIfc getSettingsOfPublishedAssessment(String assessmentId)
  {
    try
    {
      PublishedAssessmentService service = new PublishedAssessmentService();
      return service.getSettingsOfPublishedAssessment(assessmentId);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }

  /**
   * Load an item that has been published
   * @param itemId
   * @return
   */
  public ItemDataIfc loadPublishedItem(String itemId)
  {
    try
    {
      PublishedAssessmentService service = new PublishedAssessmentService();
      return service.loadPublishedItem(itemId);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }
  public ItemTextIfc loadPublishedItemText(String itemTextId)
  {
    try
    {
      PublishedAssessmentService service = new PublishedAssessmentService();
      return service.loadPublishedItemText(itemTextId);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }

  /**
   * Get list of all last submitterd published assessments with only basic info populated.
   * @param agentId the agent taking the assessments.
   * @param ascending true if ascending sort.
   * @param orderBy sort order field.
   * @return the list.
   */
  public List getBasicInfoOfLastSubmittedAssessments(String agentId, String orderBy, boolean ascending)
  {
    try
    {
      PublishedAssessmentService service = new PublishedAssessmentService();
      return service.getBasicInfoOfLastSubmittedAssessments(agentId, orderBy, ascending);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }

 /**
 * Get teh number of total submissions per assignment.
 * @param agentId teh agent making submissions
 * @return Map by(Long publishedAssessmentId->Integer totalSubmittedForGrade)
 */

  public Map getTotalSubmissionPerAssessment(String agentId)
  {
    try
    {
      PublishedAssessmentService service = new PublishedAssessmentService();
      return service.getTotalSubmissionPerAssessment(agentId);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }

  /**
  * Get teh number of total submissions for one assignment.
  * @param agentId teh agent making submissions
  * @param publishedAssessmentId teh published assessment id string
  * @return the total
  */
  public Integer getTotalSubmission(String agentId, String publishedAssessmentId)
  {
    try
    {
      PublishedAssessmentService service = new PublishedAssessmentService();
      return service.getTotalSubmission(agentId, publishedAssessmentId);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }

  /**
   * Get a published assessment using an alias
   * @param alias teh alias
   * @return teh published assessment
   */
  public PublishedAssessmentIfc getPublishedAssessmentIdByAlias(String alias)
  {
    try
    {
      PublishedAssessmentService service = new PublishedAssessmentService();
      return service.getPublishedAssessmentIdByAlias(alias);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }

  /**
   * @todo check persistence of this method!
   * @param meta
   */
  // because our service implementation cannot cast to
  // the PublishedAssessmentService.saveOrUpdateMetaData()
  // we get the item, update the metadata, and persist
  public void saveOrUpdateMetaData(ItemMetaDataIfc meta)
  {
    try
    {
      ItemService itemService = new ItemService();
      ItemDataIfc itemIfc = meta.getItem();
      itemIfc.getItemMetaDataSet().add(meta);
      ItemFacade facade = itemService.getItem(itemIfc.getItemIdString());
      itemService.saveItem(facade);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }

  /**
   * Get a map of the feedback
   * @return the map
   */
  public Map getFeedbackHash()
  {
    try
    {
      PublishedAssessmentService service = new PublishedAssessmentService();
      return service.getFeedbackHash();
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }

  /**
   * Get list of all active published assessmentsreleased to authenticated users.
   * @return the list.
   */
  public Map getAllAssessmentsReleasedToAuthenticatedUsers()
  {
    try
    {
      PublishedAssessmentService service = new PublishedAssessmentService();
      return service.getAllAssessmentsReleasedToAuthenticatedUsers();
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }

  /**
   * Get the owner.
   * @param publishedAssessmentId
   * @return teh owner string.
   */
  public String getPublishedAssessmentOwner(Long publishedAssessmentId)
  {
    try
    {
      PublishedAssessmentService service = new PublishedAssessmentService();
      return service.getPublishedAssessmentOwner(publishedAssessmentId);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }
}