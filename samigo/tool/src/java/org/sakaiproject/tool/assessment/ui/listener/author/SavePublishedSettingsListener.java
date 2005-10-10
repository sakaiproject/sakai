/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.assessment.ui.listener.author;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.spring.SpringBeanLocator;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedEvaluationModel;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.facade.GradebookFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.integration.context.IntegrationContextFactory;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.PublishedAssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>2
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class SavePublishedSettingsListener
    implements ActionListener
{
  private static Log log = LogFactory.getLog(SavePublishedSettingsListener.class);
  private static ContextUtil cu;
  private static final GradebookServiceHelper gbsHelper =
    IntegrationContextFactory.getInstance().getGradebookServiceHelper();
  private static final boolean integrated =
    IntegrationContextFactory.getInstance().isIntegrated();

  public SavePublishedSettingsListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    FacesContext context = FacesContext.getCurrentInstance();
    Map reqMap = context.getExternalContext().getRequestMap();
    Map requestParams = context.getExternalContext().getRequestParameterMap();

    PublishedAssessmentSettingsBean assessmentSettings = (PublishedAssessmentSettingsBean) cu.lookupBean(
                         "publishedSettings");
    // create an assessment based on the title entered and the assessment
    // template selected
    // #1 - set Assessment
    Long assessmentId = assessmentSettings.getAssessmentId();
    //log.info("**** save assessment assessmentId ="+assessmentId.toString());
    PublishedAssessmentService assessmentService = new PublishedAssessmentService();
    PublishedAssessmentFacade assessment = assessmentService.getPublishedAssessment(
        assessmentId.toString());
    //log.info("** assessment = "+assessment);

    // #2 - update delivery dates in AssessmentAccessControl
    PublishedAccessControl control = (PublishedAccessControl)assessment.getAssessmentAccessControl();
    if (control == null){
      control = new PublishedAccessControl();
      // need to fix accessControl so it can take AssessmentFacade later
      control.setAssessmentBase(assessment.getData());
    }
    // a. LATER set dueDate, retractDate, startDate, releaseTo
    control.setStartDate(assessmentSettings.getStartDate());
    control.setDueDate(assessmentSettings.getDueDate());
    control.setRetractDate(assessmentSettings.getRetractDate());
    control.setFeedbackDate(assessmentSettings.getFeedbackDate());

    //#3 - add or remove external assessment to gradebook
    // a. if Gradebook does not exists, do nothing, 'cos setting should have been hidden
    // b. if Gradebook exists, just call addExternal and removeExternal and swallow any exception. The
    //    exception are indication that the assessment is already in the Gradebook or there is nothing
    //    to remove.
    GradebookService g = (GradebookService) SpringBeanLocator.getInstance().
    getBean("org.sakaiproject.service.gradebook.GradebookService");

    if (gbsHelper.gradebookExists(GradebookFacade.getGradebookUId(), g)){ // => something to do
      PublishedEvaluationModel evaluation = (PublishedEvaluationModel)assessment.getEvaluationModel();
      if (evaluation == null){
        evaluation = new PublishedEvaluationModel();
        evaluation.setAssessmentBase(assessment.getData());
      }
      evaluation.setToGradeBook(assessmentSettings.getToDefaultGradebook());
      if (evaluation.getToGradeBook().equals(EvaluationModelIfc.TO_DEFAULT_GRADEBOOK.toString())){
        //add and copy scores over if any

        try{
          gbsHelper.addToGradebook((PublishedAssessmentData)assessment.getData(), g);
          // any score to copy over? get all the assessmentGradingData and copy over
          GradingService gradingService = new GradingService();
          ArrayList list = gradingService.getAllSubmissions(assessment.getPublishedAssessmentId().toString());
          for (int i=0; i<list.size();i++){
            AssessmentGradingData ag = (AssessmentGradingData)list.get(i);
            gbsHelper.updateExternalAssessmentScore(ag, g);
          }
        }
        catch(Exception e){
          log.debug("oh well, must have been added already:"+e.getMessage());
        }
      }
      else{ //remove
        try{
          gbsHelper.removeExternalAssessment(
            GradebookFacade.getGradebookUId(),
            assessment.getPublishedAssessmentId().toString(), g);
        }
        catch(Exception e){
          log.debug("*** oh well, looks like there is nothing to remove:"+e.getMessage());
        }
      }
    }

    assessmentService.saveAssessment(assessment);

    //#4 - regenerate the publsihed assessment list in autor bean again
    // sortString can be of these value:title,releaseTo,dueDate,startDate
    // get the managed bean, author and reset the list.
    // Yes, we need to do that just in case the user change those delivery
    // dates and turning an inactive pub to active pub
    GradingService gradingService = new GradingService();
    HashMap map = gradingService.getSubmissionSizeOfAllPublishedAssessments();
    AuthorBean author = (AuthorBean) cu.lookupBean(
                       "author");
    ArrayList publishedList = assessmentService.
        getBasicInfoOfAllActivePublishedAssessments(author.getPublishedAssessmentOrderBy(),author.isPublishedAscending());
    // get the managed bean, author and set the list
    author.setPublishedAssessments(publishedList);
    setSubmissionSize(publishedList, map);

    ArrayList inactivePublishedList = assessmentService.
        getBasicInfoOfAllInActivePublishedAssessments(author.getInactivePublishedAssessmentOrderBy(),author.isInactivePublishedAscending());
    // get the managed bean, author and set the list
    author.setInactivePublishedAssessments(inactivePublishedList);
    setSubmissionSize(inactivePublishedList, map);
  }

  private void setSubmissionSize(ArrayList list, HashMap map){
    for (int i=0; i<list.size();i++){
      PublishedAssessmentFacade p =(PublishedAssessmentFacade)list.get(i);
      Integer size = (Integer) map.get(p.getPublishedAssessmentId());
      if (size != null){
        p.setSubmissionSize(size.intValue());
        //log.info("*** submission size" + size.intValue());
      }
    }
  }


}


