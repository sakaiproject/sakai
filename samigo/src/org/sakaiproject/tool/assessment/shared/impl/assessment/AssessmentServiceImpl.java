/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/trunk/sakai/sam/src/org/sakaiproject/tool/assessment/services/ItemService.java $
 * $Id: ItemService.java 1285 2005-08-19 02:05:48Z esmiley@stanford.edu $
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentTemplateData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentTemplateIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.SectionFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.shared.api.assessment.AssessmentServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.assessment.AssessmentServiceException;

/**
 * AssessmentServiceImpl implements a shared interface to get/set assessment
 * information.
 * @author Ed Smiley <esmiley@stanford.edu>
 */
public class AssessmentServiceImpl implements AssessmentServiceAPI
{
  private static Log log = LogFactory.getLog(AssessmentServiceImpl.class);

  /**
   * Get assessment template from idstring.
   * @param assessmentTemplateId
   * @return the assessment template
   */
  public AssessmentTemplateIfc getAssessmentTemplate(String assessmentTemplateId)
  {
    try
    {
      AssessmentService service = new AssessmentService();
      return service.getAssessmentTemplate(assessmentTemplateId);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }

  /**
   * Get assessment from id string.
   * @param assessmentId
   * @return teh assessment
   */
  public AssessmentIfc getAssessment(String assessmentId)
  {
    try
    {
      AssessmentService service = new AssessmentService();
      return service.getAssessment(assessmentId);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }

  /**
   * Get an assessment with only basic info populated
   * @param assessmentId
   * @return an assessment with only basic info populated
   */
  public AssessmentIfc getBasicInfoOfAnAssessment(String assessmentId)
  {
    try
    {
      AssessmentService service = new AssessmentService();
      return service.getBasicInfoOfAnAssessment(assessmentId);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }

  /**
   * Get a list of assessment templates.
   * @return teh list.
   */
  public List getAllAssessmentTemplates()
  {
    try
    {
      AssessmentService service = new AssessmentService();
      return service.getAllAssessmentTemplates();
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }

  /**
   * Get a list of all active assessment templates.
   * @return teh list
   */
  public List getAllActiveAssessmentTemplates()
  {
    try
    {
      AssessmentService service = new AssessmentService();
      return service.getAllActiveAssessmentTemplates();
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }

  /**
   * Get a list of all the assessment template titles.
   * @return teh list.
   */
  public List
    getTitleOfAllActiveAssessmentTemplates()
  {
    try
    {
      AssessmentService service = new AssessmentService();
      return service.getTitleOfAllActiveAssessmentTemplates();
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }

  /**
   * Get an ordered list of assessments.
   * @param orderBy sort order field.
   * @return the list.
   */
  public List getAllAssessments(String orderBy)
  {
    try
    {
      AssessmentService service = new AssessmentService();
      return service.getAllAssessments(orderBy);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }

  /**
   * Get all active assessments.
   * @param orderBy sort order field.
   * @return the list.
   */
  public List getAllActiveAssessments(String
                                      orderBy)
  {
    try
    {
      AssessmentService service = new AssessmentService();
      return service.getAllActiveAssessments(orderBy);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }

  }

  /**
   * Get list of all active assessment's settings.
   * @param orderBy sort order field.
   * @return the list.
   */
  public List getSettingsOfAllActiveAssessments(String orderBy)
  {
    try
    {
      AssessmentService service = new AssessmentService();
      return service.getSettingsOfAllActiveAssessments(orderBy);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }

  }

  /**
   * Get list of all active assessments with only basic info populated.
   * @param orderBy
   * @param ascending ascending sort if true
   * @param orderBy sort order field.
   * @return the list.
   */
  public List getBasicInfoOfAllActiveAssessments(
    String orderBy,
    boolean ascending)
  {
    try
    {
      AssessmentService service = new AssessmentService();
      return service.getBasicInfoOfAllActiveAssessments(orderBy, ascending);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }

  /**
   * Get list of all active assessments with only basic info populated.
   * @param orderBy
   * @param orderBy sort order field.
   * @return the list.
   */
  public List getBasicInfoOfAllActiveAssessments(String orderBy)
  {
    try
    {
      AssessmentService service = new AssessmentService();
      return service.getBasicInfoOfAllActiveAssessments(orderBy);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }

  /**
   * Get list of all active assessments.
   * @param orderBy
   * @param pageSize number in a page
   * @param pageNumber number of the page
   * @param orderBy sort order field.
   * @return the list.
   */
  public List getAllAssessments(int pageSize, int pageNumber, String orderBy)
  {
    try
    {
      AssessmentService service = new AssessmentService();
      return service.getAllAssessments(pageSize,
        pageNumber, orderBy);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }

  /**
   * Create an assessment.
   * @param title teh title
   * @param description teh description
   * @param typeId the type id
   * @param templateId the template's template id
   * @return teh created assessment.
   */
  public AssessmentIfc createAssessment(String
                                        title, String description,
                                        String typeId, String templateId)
  {
    try
    {
      AssessmentService service = new AssessmentService();
      return service.createAssessment(title, description, typeId, templateId);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }

  /**
   * Get number of questions.
   * @param assessmentId the assessment id string.
   * @return the number.
   */
  public int getQuestionSize(String assessmentId)
  {
    try
    {
      AssessmentService service = new AssessmentService();
      return service.getQuestionSize(assessmentId);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }

  /**
   * Update an assessment coupled to the persistence layer
   * @param assessment the assessment interface of the POJO
   */
  public void update(AssessmentIfc assessment)
  {
    try
    {
      AssessmentService service = new AssessmentService();
      AssessmentFacade facade =
        service.getAssessment(assessment.getAssessmentId().toString());
      service.update(facade);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }

  public void save(AssessmentTemplateIfc template)
  {
    try
    {
      AssessmentService service = new AssessmentService();
      AssessmentTemplateData data = (AssessmentTemplateData)
        service.getAssessmentTemplate(
        template.getAssessmentTemplateId().toString()).getData();
      service.save(data);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }

  /**
   * Save an assessment.
   * @param assessment the assessment.
   */
  public void saveAssessment(AssessmentIfc assessment)
  {
    try
    {
      AssessmentService service = new AssessmentService();
      AssessmentFacade facade =
        service.getAssessment(assessment.getAssessmentId().toString());
      service.saveAssessment(facade);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }

  /**
   * Delete an assessment template from an assessment.
   * @param assessmentId the assessment id of the assessment.
   */
  public void deleteAssessmentTemplate(Long assessmentId)
  {
    try
    {
      AssessmentService service = new AssessmentService();
      service.deleteAssessmentTemplate(assessmentId);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }

  /**
   * Remove the assessment.
   * @param assessmentId the assessment id string.
   */
  public void removeAssessment(String assessmentId)
  {
    try
    {
      AssessmentService service = new AssessmentService();
      service.removeAssessment(assessmentId);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }

  /**
   * Add a section to an assessment.
   * @param assessmentId teh assessment id string.
   * @return teh section.
   */
  public SectionDataIfc addSection(String assessmentId)
  {
    try
    {
      AssessmentService service = new AssessmentService();
      return service.addSection(assessmentId);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }

  /**
   * Remove a section.
   * @param sectionId its id.
   */
  public void removeSection(String sectionId)
  {
    try
    {
      AssessmentService service = new AssessmentService();
      service.removeSection(sectionId);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }

  /**
   * Get a section.
   * @param sectionId teh section id string.
   * @return the section.
   */
  public SectionDataIfc getSection(String sectionId)
  {
    try
    {
      AssessmentService service = new AssessmentService();
      return service.getSection(sectionId);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }

  /**
   * Perform persistence saveOrUpdate on section.
   * @param section the section.
   */
  public void saveOrUpdateSection(SectionDataIfc section)
  {
    try
    {
      AssessmentService service = new AssessmentService();
      SectionFacade facade =
        service.getSection(section.getSectionId().toString());
      service.saveOrUpdateSection(facade);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }


  /**
   * Move items between sections.
   * @param sourceSectionId source id.
   * @param destSectionId destination id.
   */
  public void moveAllItems(String sourceSectionId, String destSectionId)
  {
    try
    {
      AssessmentService service = new AssessmentService();
      service.moveAllItems(sourceSectionId, destSectionId);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }


  /**
   * Remove all items from a section.
   * @param sourceSectionId teh section id string.
   */
  public void removeAllItems(String sourceSectionId)
  {
    try
    {
      AssessmentService service = new AssessmentService();
      service.removeAllItems(sourceSectionId);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }

  /**
 * Get list of all active assessment templatess with only basic info populated.
 * @param orderBy
 * @return the list.
 */

  public List getBasicInfoOfAllActiveAssessmentTemplates(String orderBy)
  {
    try
    {
      AssessmentService service = new AssessmentService();
      return service.getBasicInfoOfAllActiveAssessmentTemplates(orderBy);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }

  /**
   * Create an assessment without a default section.
   * Section must be created later.  (This facilitates batch uploads such as
   * QTI import where the section titles are all named in the incoming
   * document.)
   *
   * @param title
   * @param description
   * @param typeId
   * @param templateId
   * @return
   */
  public AssessmentIfc
    createAssessmentWithoutDefaultSection(
    String title, String description, String typeId, String templateId)
  {
    try
    {
      AssessmentService service = new AssessmentService();
      return service.createAssessmentWithoutDefaultSection(
        title, description, typeId, templateId);
    }
    catch (Exception ex)
    {
      throw new AssessmentServiceException(ex);
    }
  }
}