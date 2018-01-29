/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.shared.impl.assessment;

import java.util.List;

import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentTemplateData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentTemplateIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.SectionFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.shared.api.assessment.AssessmentServiceAPI;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentServiceException;

/**
 * AssessmentServiceImpl implements a shared interface to get/set assessment
 * information.
 * @author Ed Smiley <esmiley@stanford.edu>
 */
public class AssessmentServiceImpl implements AssessmentServiceAPI
{

  /**
   * Get assessment template from id string.
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
   * @return the assessment
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
   * Get an assessment with only basic info populated..
   * @param assessmentId teh assessment id string.
   * @return an assessment with only basic info populated.
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
   * @return the list.
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
   * @return the list
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
   * @return the list.
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
   * @param title the title
   * @param description the description
   * @param typeId the type id
   * @param templateId the template's template id
   * @return the created assessment.
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
   * @param assessmentId the assessment id string.
   * @return the section.
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
   * @param sectionId the section id string.
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
   * @param sourceSectionId the section id string.
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
 * Get list of all active assessment templates with only basic info populated.
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
