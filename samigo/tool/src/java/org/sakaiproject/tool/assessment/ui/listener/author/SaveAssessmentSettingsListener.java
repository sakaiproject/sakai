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
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>2
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class SaveAssessmentSettingsListener
    implements ActionListener
{
  private static Log log = LogFactory.getLog(SaveAssessmentSettingsListener.class);
  private static ContextUtil cu;

  public SaveAssessmentSettingsListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    FacesContext context = FacesContext.getCurrentInstance();
    Map reqMap = context.getExternalContext().getRequestMap();
    Map requestParams = context.getExternalContext().getRequestParameterMap();

    AssessmentSettingsBean assessmentSettings = (AssessmentSettingsBean) cu.
        lookupBean("assessmentSettings");

    String err="";
    boolean error=false;
    String assessmentId=String.valueOf(assessmentSettings.getAssessmentId()); 
    AssessmentService assessmentService = new AssessmentService();
  
    //#2 - check if name is unique
    String assessmentName=assessmentSettings.getTitle();
    if(!assessmentService.assessmentTitleIsUnique(assessmentId,assessmentName,false)){
	err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","assessmentName_error");
	context.addMessage(null,new FacesMessage(err));
	error=true;
    }

    //#3 if timed assessment, does it has value for time
    Object time=assessmentSettings.getValueMap().get("hasTimeAssessment");
    boolean isTime=false;
    try
    {
      if (time != null)
      {
        isTime = ( (Boolean) time).booleanValue();
      }
    }
    catch (Exception ex)
    {
      // keep default
      log.warn("Expecting Boolean hasTimeAssessment, got: " + time);

    }
    if((isTime) &&((assessmentSettings.getTimeLimit().intValue())==0)){
	err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","timeSelect_error");
	context.addMessage(null,new FacesMessage(err));
        error=true;
    }

    if (error){
      assessmentSettings.setOutcomePublish("editAssessmentSettings");
      return;
    }
 
    assessmentSettings.setOutcomeSave("saveSettings_success");
    SaveAssessmentSettings s= new SaveAssessmentSettings();
    s.save(assessmentSettings);
    // reset the core listing in case assessment title changes
    AuthorBean author = (AuthorBean) cu.lookupBean(
                       "author");
 
    ArrayList assessmentList = assessmentService.getBasicInfoOfAllActiveAssessments(
                      author.getCoreAssessmentOrderBy(),author.isCoreAscending());
    // get the managed bean, author and set the list
    author.setAssessments(assessmentList);

    // goto Question Authoring page
    EditAssessmentListener editA= new EditAssessmentListener();
    editA.processAction(null);

  }

}
