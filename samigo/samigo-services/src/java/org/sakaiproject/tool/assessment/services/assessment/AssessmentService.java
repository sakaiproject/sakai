/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/services/assessment/AssessmentService.java $
 * $Id: AssessmentService.java 9273 2006-05-10 22:34:28Z daisyf@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the"License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/


package org.sakaiproject.tool.assessment.services.assessment;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentTemplateData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
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
    String title, String description, String typeId, String templateId)
    throws Exception{
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
      System.out.println("**** AssessmentFacadeQueries="+queries);
      assessment = queries.createAssessment(title, description, typeIdLong, templateIdLong);
    }
    catch(Exception e)
    {
      log.error(e); throw new Exception(e);
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

  public void deleteAllSecuredIP(AssessmentIfc assessment) {
    PersistenceService.getInstance().getAssessmentFacadeQueries().
      deleteAllSecuredIP(assessment);
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
      String title, String description, String typeId, String templateId) 
      throws Exception{
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
      log.error(e); throw new Exception(e);
    }
    return assessment;
  }

  public boolean assessmentTitleIsUnique(String assessmentBaseId, String title, boolean isTemplate) {
    return PersistenceService.getInstance().getAssessmentFacadeQueries().
           assessmentTitleIsUnique(new Long(assessmentBaseId), title, new Boolean(isTemplate));
  }

  public List getAssessmentByTemplate(String templateId){
    return PersistenceService.getInstance().getAssessmentFacadeQueries().
           getAssessmentByTemplate(new Long(templateId));
  }

  public List getDefaultMetaDataSet(){
      return PersistenceService.getInstance().getAssessmentFacadeQueries().
	  getDefaultMetaDataSet();
  }

}
