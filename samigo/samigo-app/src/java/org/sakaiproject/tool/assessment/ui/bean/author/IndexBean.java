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
 *       http://www.osedu.org/licenses/ECL-2.0
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
import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.assessment.facade.AssessmentTemplateFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.util.BeanSort;

/**
 * Used to be org.navigoproject.ui.web.form.IndexForm.java
 *
 * @author $author$
 * @version $Id$
 */
public class IndexBean implements Serializable
{
  private static BeanSort bs;
  private Collection templateList;
  private ArrayList sortTemplateList;

  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = 7919219404875270127L;
  private Collection templateNames;
  private Collection templateIds;
//  private Collection studentAssessmentList;
//  private Collection assessmentList;
//  private Collection assessmentTypeList;
  private String assessmentTypeChoice;
  private String course_id;
  private String agent_id;
  private String templateOrderBy= "templateName";
  private boolean templateAscending= true;
  private boolean automaticSubmissionEnabled = false;

  private static Log log = LogFactory.getLog(IndexBean.class);

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
      ArrayList list = delegate.getBasicInfoOfAllActiveAssessmentTemplates("title");
      //ArrayList list = delegate.getAllAssessmentTemplates();
      ArrayList templates = new ArrayList();
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
      e.printStackTrace();
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

  public ArrayList getSortTemplateList()
  {
   return this.sortTemplateList;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param templateList DOCUMENTATION PENDING
   */
  public void setSortTemplateList(ArrayList sortTemplateList)
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

//  /**
//   * This is designed to quickly and efficiently obtain a small number of
//   * pieces of information in the assessment list.  Specifically, you will not
//   * be able to obtain properties from the getData() method.  To get a more
//   * complete, but less efficient list, use getStudentAssessmentList()
//   * instead.
//   *
//   * @return bare Collection of assessments
//   */
//  public Collection getAssessmentList()
//  {
//    try
//    {
//      AssessmentServiceDelegate delegate = new AssessmentServiceDelegate();
//      AssessmentIterator aiter = null;
//      try
//      {
//        aiter = delegate.getAssessments((new Long(course_id)).longValue());
//      }
//      catch(Exception e)
//      {
//        log.warn("No Course ID specified.");
//        aiter = delegate.getAssessments();
//      }
//
//      assessmentList = new ArrayList();
//
//      while(aiter.hasNext())
//      {
//        Assessment assessment = (Assessment) aiter.next();
//        assessmentList.add(assessment);
//      }
//    }
//    catch(Exception e)
//    {
//      log.error(e);
//      if(assessmentList == null)
//      {
//        assessmentList = new ArrayList();
//      }
//    }
//
//    return assessmentList;
//  }
//
//  /**
//   * This is designed to obtain a list of assessments and their properties.
//   *
//   * @return Collection of assessments
//   */
//  public Collection getStudentAssessmentList()
//  {
//    try
//    {
//      AssessmentServiceDelegate delegate = new AssessmentServiceDelegate();
//      Collection coll = null;
//      try
//      {
//        SharedManager sm = OsidManagerFactory.createSharedManager();
//        AuthenticationManager am =
//          OsidManagerFactory.createAuthenticationManager(
//            OsidManagerFactory.getOsidOwner());
//        Agent agent =
//          sm.getAgent(
//            am.getUserId(new TypeImpl("Stanford", "AAM", "agent", "sunetid")));
//        long courseId = new Long(course_id).longValue();
//        coll = delegate.getStudentView(courseId, agent);
//      }
//      catch(Exception e)
//      {
//        log.warn("Either No Course ID or Agent ID or Both.");
//        coll = delegate.getStudentView(0, null);
//      }
//
//      studentAssessmentList = new ArrayList(coll);
//    }
//    catch(Exception e)
//    {
//      log.error(e);
//      if(studentAssessmentList == null)
//      {
//        studentAssessmentList = new ArrayList();
//      }
//    }
//
//    return studentAssessmentList;
//  }

//  /**
//   * DOCUMENTATION PENDING
//   *
//   * @param assessmentList DOCUMENTATION PENDING
//   */
//  public void setStudentAssessmentList(Collection assessmentList)
//  {
//    studentAssessmentList = assessmentList;
//  }
//
//  /**
//   * DOCUMENTATION PENDING
//   *
//   * @param assessmentList DOCUMENTATION PENDING
//   */
//  public void setAssessmentList(Collection assessmentList)
//  {
//    this.assessmentList = assessmentList;
//  }

//  /**
//   * DOCUMENTATION PENDING
//   *
//   * @return DOCUMENTATION PENDING
//   */
//  public Collection getAssessmentTypeList()
//  {
//    return assessmentTypeList;
//  }
//
//  /**
//   * DOCUMENTATION PENDING
//   *
//   * @param assessmentTypeList DOCUMENTATION PENDING
//   */
//  public void setAssessmentTypeList(Collection assessmentTypeList)
//  {
//    this.assessmentTypeList = assessmentTypeList;
//  }

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
