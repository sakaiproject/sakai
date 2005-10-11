/**********************************************************************************
* $URL$
* $Id$
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

package org.sakaiproject.tool.assessment.services.assessment;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentTemplateData;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.facade.AssessmentTemplateFacade;
import org.sakaiproject.tool.assessment.facade.SectionFacade;
import org.sakaiproject.tool.assessment.facade.TypeFacade;
import org.sakaiproject.tool.assessment.services.PersistenceService;


/**
 * The AssessmentService calls the service locator to reach the
 * manager on the back end.
 * @author Rachel Gollub <rgollub@stanford.edu>
 */
public class AssessmentService
{
  private static Log log = LogFactory.getLog(AssessmentService.class);

  /**
   * Creates a new QuestionPoolService object.
   */
  public AssessmentService()  {
  }

  public AssessmentTemplateFacade getAssessmentTemplate(String assessmentTemplateId)
  {
    try{
      return PersistenceService.getInstance().getAssessmentFacadeQueries().
          getAssessmentTemplate(new Long(assessmentTemplateId));
    }
    catch(Exception e)
    {
      log.error(e); throw new Error(e);
    }
  }

  public AssessmentFacade getAssessment(String assessmentId)
  {
    try{
      return PersistenceService.getInstance().getAssessmentFacadeQueries().
          getAssessment(new Long(assessmentId));
    }
    catch(Exception e)
    {
      log.error(e); throw new Error(e);
    }
  }

  public AssessmentFacade getBasicInfoOfAnAssessment(String assessmentId)
  {
    try{
      return PersistenceService.getInstance().getAssessmentFacadeQueries().
          getBasicInfoOfAnAssessment(new Long(assessmentId));
    }
    catch(Exception e)
    {
      log.error(e); throw new Error(e);
    }
  }

  public ArrayList getAllAssessmentTemplates()
  {
    try{
      return PersistenceService.getInstance().getAssessmentFacadeQueries().
          getAllAssessmentTemplates();
    }
    catch(Exception e)
    {
      log.error(e); throw new Error(e);
    }
  }

  public ArrayList getAllActiveAssessmentTemplates()
  {
    try{
      return PersistenceService.getInstance().getAssessmentFacadeQueries().
          getAllActiveAssessmentTemplates();
    }
    catch(Exception e)
    {
      log.error(e); throw new Error(e);
    }
  }

  public ArrayList getTitleOfAllActiveAssessmentTemplates()
  {
    try{
      return PersistenceService.getInstance().getAssessmentFacadeQueries().
          getTitleOfAllActiveAssessmentTemplates();
    }
    catch(Exception e)
    {
      log.error(e); throw new Error(e);
    }
  }

  public ArrayList getAllAssessments(String orderBy)
  {
    return PersistenceService.getInstance().getAssessmentFacadeQueries().
        getAllAssessments(orderBy); // signalling all & no paging
  }

  public ArrayList getAllActiveAssessments(String orderBy)
  {
    return PersistenceService.getInstance().getAssessmentFacadeQueries().
        getAllActiveAssessments(orderBy); // signalling all & no paging
  }

  /**
   * @param orderBy
   * @return an ArrayList of AssessmentFacade. It is IMPORTANT to note that the
   * object is a partial object which contains no SectionFacade
   */
  public ArrayList getSettingsOfAllActiveAssessments(String orderBy)
  {
    return PersistenceService.getInstance().getAssessmentFacadeQueries().
        getSettingsOfAllActiveAssessments(orderBy); // signalling all & no paging
  }

  /**
   * @param orderBy
   * @return an ArrayList of AssessmentFacade. It is IMPORTANT to note that the
   * object is a partial object which contains only Assessment basic info such as
   * title, lastModifiedDate. This method is used by Authoring Front Door
   */
  public ArrayList getBasicInfoOfAllActiveAssessments(String orderBy, boolean ascending)
  {
    String siteAgentId = AgentFacade.getCurrentSiteId();
    return PersistenceService.getInstance().getAssessmentFacadeQueries().
        getBasicInfoOfAllActiveAssessmentsByAgent(orderBy,siteAgentId, ascending); // signalling all & no paging
  }

  public ArrayList getBasicInfoOfAllActiveAssessments(String orderBy)
  {
    String siteAgentId = AgentFacade.getCurrentSiteId();
    return PersistenceService.getInstance().getAssessmentFacadeQueries().
        getBasicInfoOfAllActiveAssessmentsByAgent(orderBy,siteAgentId); // signalling all & no paging
  }

  public ArrayList getAllAssessments(
      int pageSize, int pageNumber, String orderBy)
  {
    try{
      if (pageSize >0 && pageNumber > 0){
        return PersistenceService.getInstance().getAssessmentFacadeQueries().
            getAllAssessments(pageSize, pageNumber, orderBy);
      }
      else{
        return PersistenceService.getInstance().getAssessmentFacadeQueries().
            getAllAssessments(orderBy);
      }
    }
    catch(Exception e)
    {
      log.error(e); throw new Error(e);
    }
  }

  public AssessmentFacade createAssessment(
    String title, String description, String typeId, String templateId){
    AssessmentFacade assessment = null;
    try{
      AssessmentTemplateFacade assessmentTemplate = null;
      // #1 - check templateId and prepared it in Long
      Long templateIdLong = AssessmentTemplateFacade.DEFAULTTEMPLATE;
      if (templateId != null && !templateId.equals(""))
        templateIdLong = new Long(templateId);

      // #2 - check typeId and prepared it in Long
      Long typeIdLong = TypeFacade.HOMEWORK;
      if (typeId != null && !typeId.equals(""))
        typeIdLong = new Long(typeId);

      AssessmentFacadeQueriesAPI queries = PersistenceService.getInstance().getAssessmentFacadeQueries();
      assessment = queries.createAssessment(title, description, typeIdLong, templateIdLong);
    }
    catch(Exception e)
    {
      log.error(e); throw new Error(e);
    }
    return assessment;
  }

  public int getQuestionSize(String assessmentId){
    return PersistenceService.getInstance().getAssessmentFacadeQueries().
        getQuestionSize(new Long(assessmentId));
  }

  public void update(AssessmentFacade assessment){
    PersistenceService.getInstance().getAssessmentFacadeQueries().
        saveOrUpdate(assessment);
  }

  public void save(AssessmentTemplateData template) {
    PersistenceService.getInstance().getAssessmentFacadeQueries().
      saveOrUpdate(template);
  }

  public void saveAssessment(AssessmentFacade assessment) {
    PersistenceService.getInstance().getAssessmentFacadeQueries().
      saveOrUpdate(assessment);
  }

  public void deleteAssessmentTemplate(Long assessmentId) {
    PersistenceService.getInstance().getAssessmentFacadeQueries().
      deleteTemplate(assessmentId);
  }

  public void removeAssessment(String assessmentId)
  {
    PersistenceService.getInstance().getAssessmentFacadeQueries().
          removeAssessment(new Long(assessmentId));
  }


/**
public int checkDelete(long assessmentId){
  return assessmentService.checkDelete(assessmentId);
}

public void deleteAssessment(Id assessmentId)
  throws osid.assessment.AssessmentException
{
    assessmentService.deleteAssessment(assessmentId);
}

  public AssessmentIterator getAssessments()
    throws osid.assessment.AssessmentException
  {
      return assessmentService.getAssessments();
  }

*/

  public SectionFacade addSection(String assessmentId)
  {
    SectionFacade section = null;
    try{
      Long assessmentIdLong = new Long(assessmentId);
      AssessmentFacadeQueriesAPI queries = PersistenceService.getInstance().getAssessmentFacadeQueries();
      section = queries.addSection(assessmentIdLong);
    }
    catch(Exception e){
      e.printStackTrace();
    }
     return section;
  }

  public void removeSection(String sectionId){
    try{
      Long sectionIdLong = new Long(sectionId);
      AssessmentFacadeQueriesAPI queries = PersistenceService.getInstance().getAssessmentFacadeQueries();
      queries.removeSection(sectionIdLong);
    }
    catch(Exception e){
      e.printStackTrace();
    }

  }

  public SectionFacade getSection(String sectionId)
  {
    try{
      return PersistenceService.getInstance().getAssessmentFacadeQueries().
          getSection(new Long(sectionId));
    }
    catch(Exception e)
    {
      log.error(e); throw new Error(e);
    }
  }

  public void saveOrUpdateSection(SectionFacade section)
  {
    try{
      PersistenceService.getInstance().getAssessmentFacadeQueries().
          saveOrUpdateSection(section);
    }
    catch(Exception e)
    {
      log.error(e); throw new Error(e);
    }
  }

  public void moveAllItems(String sourceSectionId, String destSectionId)
  {
    PersistenceService.getInstance().getAssessmentFacadeQueries().
        moveAllItems(new Long(sourceSectionId), new Long(destSectionId)); // signalling all & no paging
  }


  public void removeAllItems(String sourceSectionId)
  {
    PersistenceService.getInstance().getAssessmentFacadeQueries().
        removeAllItems(new Long(sourceSectionId));
  }

  public ArrayList getBasicInfoOfAllActiveAssessmentTemplates(String orderBy)
  {
    return PersistenceService.getInstance().getAssessmentFacadeQueries().
        getBasicInfoOfAllActiveAssessmentTemplates(orderBy); // signalling all & no paging
  }

  public AssessmentFacade createAssessmentWithoutDefaultSection(
      String title, String description, String typeId, String templateId) {
    AssessmentFacade assessment = null;
    try{
      AssessmentTemplateFacade assessmentTemplate = null;
      // #1 - check templateId and prepared it in Long
      Long templateIdLong = AssessmentTemplateFacade.DEFAULTTEMPLATE;
      if (templateId != null && !templateId.equals(""))
        templateIdLong = new Long(templateId);

      // #2 - check typeId and prepared it in Long
      Long typeIdLong = TypeFacade.HOMEWORK;
      if (typeId != null && !typeId.equals(""))
        typeIdLong = new Long(typeId);

      AssessmentFacadeQueriesAPI queries = PersistenceService.getInstance().getAssessmentFacadeQueries();
      assessment = queries.createAssessmentWithoutDefaultSection(title, description, typeIdLong, templateIdLong);
    }
    catch(Exception e)
    {
      log.error(e); throw new Error(e);
    }
    return assessment;
  }
}
