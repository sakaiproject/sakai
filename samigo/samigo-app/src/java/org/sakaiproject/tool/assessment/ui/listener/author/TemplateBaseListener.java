/**********************************************************************************
 * $URL$
 * $Id$
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


package org.sakaiproject.tool.assessment.ui.listener.author;

import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.facade.AssessmentTemplateFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.TemplateBean;



/**
 * <p>Title: Sakai Project</p>
 * <p>Description:  Samigo Assessment Manager.  This is a base class with some common methods used by
 *  template action listerners.</p>
 * <p>Copyright: * * Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University, \n*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation \n* \n* Licensed under the Educational Community License Version 1.0 (the "License"); \n* By obtaining, using and/or copying this Original Work, you agree that you have read,\n * understand, and will comply with the terms and conditions of the Educational Community License. \n* You may obtain a copy of the License at: \n*\n *      \nhttp://cvs.sakaiproject.org/licenses/license_1_0.html * * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, n* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING \n* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.</p>
 * <p>Company: Trustees of Indiana University, The Regents of the University of Michigan, and Stanford University, all rights reserved. </p>
 * @author <a href="mailto:esmiley@stanford.edu">Ed Smiley</a>
 * @version $Id$
 */

public abstract class TemplateBaseListener implements ActionListener
{
  // forces you to implement your own processAction
  abstract public void processAction(ActionEvent parm1) throws javax.faces.event.AbortProcessingException;
  private static Log log = LogFactory.getLog(TemplateBaseListener.class);

  /**
   * Get a template from the template id
   * @todo refactor to use the new service
   * @param templateId
   * @return the template object, or null on failure
   */

  protected AssessmentTemplateFacade getAssessmentTemplate(String templateId)
  {
    try
    {
      AssessmentService delegate = new AssessmentService();
      AssessmentTemplateFacade mytemplate =
          delegate.getAssessmentTemplate(templateId);
      return mytemplate;
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      return null;
    }
  }

  /**
   * Helper method to look up template backing bean.
   * @param context the faces context
   * @return the backing bean
   * @throws FacesException
   */
  protected TemplateBean lookupTemplateBean(FacesContext context) throws
      FacesException
  {
    TemplateBean templateBean;
    FacesContext facesContext = FacesContext.getCurrentInstance();
    ApplicationFactory factory = (ApplicationFactory) FactoryFinder.getFactory(
        FactoryFinder.APPLICATION_FACTORY);
    Application application = factory.getApplication();
    templateBean = (TemplateBean)
        application.getVariableResolver().resolveVariable(context, "template");
    return templateBean;
  }

}
