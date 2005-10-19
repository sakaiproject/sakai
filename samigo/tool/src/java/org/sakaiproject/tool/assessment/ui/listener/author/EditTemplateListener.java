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
import java.util.Collection;
import java.util.Iterator;
//import java.util.ResourceBundle;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.ui.bean.author.TemplateBean;
import org.sakaiproject.tool.assessment.ui.bean.author.IndexBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.facade.AssessmentTemplateFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;

/**
 * <p>Description: Action Listener to edit a new or existing template</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class EditTemplateListener
    extends TemplateBaseListener implements ActionListener
{
  private static Log log = LogFactory.getLog(EditTemplateListener.class);
  /**
   * Standard processAction.
   * @param ae
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    log.info("EDIT TEMPLATE LISTENER.");

    TemplateBean templateBean = (TemplateBean)
      ContextUtil.lookupBean("template");

      log.info("Editing new template.");
      String tempName=templateBean.getNewName();
      System.out.println("TEMPNAME= "+tempName);
 AssessmentService assessmentService = new AssessmentService();
    IndexBean templateIndex = (IndexBean) ContextUtil.lookupBean(
                       "templateIndex");

    ArrayList templates = new ArrayList();
 boolean duplicateName=false;
 int count=0;
    try
    {
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().
		    put("template", new TemplateBean());
	ArrayList list = assessmentService.getBasicInfoOfAllActiveAssessmentTemplates("title");
        Iterator iter = list.iterator();
     
        while (iter.hasNext())
        { 
	 AssessmentTemplateFacade facade =
	 (AssessmentTemplateFacade) iter.next();
	
	     String n=facade.getTitle();
             System.out.println("TempName: "+n);
	      if((tempName.trim()).equals(n)){
		  if(count==0){
		      count++;
		  }
		  else{
		      duplicateName=true;
		      break;
		  }
	      }
	}
    }
    catch(Exception e)
    {
		  e.printStackTrace();
		  throw new Error(e);
    }
    if(duplicateName){
	String error=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.TemplateMessages","duplicateName_error");
FacesContext context = FacesContext.getCurrentInstance();

 context.addMessage(null,new FacesMessage(error));

      }
      else{ 
      
      templateBean.setTemplateName(tempName);
     
        templateBean.setIdString("0"); //new template
     
      }

  }

}
