/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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



package org.sakaiproject.tool.assessment.ui.bean.author;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.faces.context.FacesContext;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.tool.assessment.facade.AssessmentTemplateFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.util.BeanSort;

/**
 * Used to be org.navigoproject.ui.web.form.IndexForm.java
 *
 * @author $author$
 * @version $Id$
 */
@Slf4j
public class IndexBean implements Serializable
{
  private static BeanSort bs;
  private Collection templateList;
  private List sortTemplateList;

  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = 7919219404875270127L;
  private Collection templateNames;
  private Collection templateIds;
  private String assessmentTypeChoice;
  private String course_id;
  private String agent_id;
  private String templateOrderBy= "templateName";
  private boolean templateAscending= true;
  private boolean automaticSubmissionEnabled = false;

  /**
   * Creates a new IndexBean object.
   */
  public IndexBean()
  {
//    if(assessmentTypeList == null)
//    {
//      assessmentTypeList = new ArrayList();
//    }
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public Collection getTemplateList()
  {

    try
    {
      AssessmentService delegate = new AssessmentService();
      List list = delegate.getBasicInfoOfAllActiveAssessmentTemplates("title");
      //ArrayList list = delegate.getAllAssessmentTemplates();
      List templates = new ArrayList();
      Iterator iter = list.iterator();
      while (iter.hasNext())
      {
        AssessmentTemplateFacade facade =
          (AssessmentTemplateFacade) iter.next();
        TemplateBean bean = new TemplateBean();
        bean.setTemplateName(facade.getTitle());
        bean.setIdString(facade.getAssessmentBaseId().toString());
        bean.setLastModified(facade.getLastModifiedDate().toString());
        templates.add(bean);
      }
     this.templateList = templates;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      templateList = new ArrayList();
      }

     return this.templateList;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param templateList DOCUMENTATION PENDING
   */
  public void setTemplateList(Collection templateList)
  {
    this.templateList = templateList;
  }

  public List getSortTemplateList()
  {
   return this.sortTemplateList;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param templateList DOCUMENTATION PENDING
   */
  public void setSortTemplateList(List sortTemplateList)
  {
    this.sortTemplateList = sortTemplateList;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public Collection getTemplateNames()
  {
    if(templateNames == null)
    {
      getTemplateList();
    }

    return templateNames;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param names DOCUMENTATION PENDING
   */
  public void setTemplateNames(Collection names)
  {
    templateNames = names;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public Collection getTemplateIds()
  {
    if(templateIds == null)
    {
      getTemplateList();
    }

    return templateIds;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param ids DOCUMENTATION PENDING
   */
  public void setTemplateIds(Collection ids)
  {
    templateIds = ids;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getAssessmentTypeChoice()
  {
    return assessmentTypeChoice;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param typeChoice DOCUMENTATION PENDING
   */
  public void setAssessmentTypeChoice(String typeChoice)
  {
    assessmentTypeChoice = typeChoice;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getCourseId()
  {
    return course_id;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param id DOCUMENTATION PENDING
   */
  public void setCourseId(String id)
  {
    log.debug("Setting course id to " + id);
    course_id = id;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getAgentId()
  {
    return agent_id;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param id DOCUMENTATION PENDING
   */
  public void setAgentId(String id)
  {
    log.debug("Setting agent id to " + id);
    agent_id = id;
  }

  public String getTemplateOrderBy() {
    return this.templateOrderBy;
  }

  public void setTemplateOrderBy(String templateOrderBy) {
    this.templateOrderBy = templateOrderBy;
  }

/**
   * is Template table sorted in ascending order
   * @return true if it is
   */
  public boolean isTemplateAscending()
  {
    return templateAscending;
  }

  /**
   *
   * @param Ascending is template table sorted in ascending order
   */
  public void setTemplateAscending(boolean templateAscending)
  {
    this.templateAscending = templateAscending;
  }

  private String outcome;
  public void setOutcome(String outcome){
    this.outcome=outcome;
  }
  public String getOutcome(){
    return outcome;
  }

  public void setAutomaticSubmissionEnabled(boolean automaticSubmissionEnabled){
	  this.automaticSubmissionEnabled = automaticSubmissionEnabled ;
  }

  public boolean getAutomaticSubmissionEnabled(){
	  return automaticSubmissionEnabled;
  }
  
}
