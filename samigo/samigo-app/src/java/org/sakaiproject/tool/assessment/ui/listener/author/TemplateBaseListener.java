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

package org.sakaiproject.tool.assessment.ui.listener.author;

import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.FacesException;
import javax.faces.FactoryFinder;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.tool.assessment.facade.AssessmentTemplateFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.TemplateBean;

/**
 * <p>Title: Sakai Project</p>
 * <p>Description:  Samigo Assessment Manager.  This is a base class with some common methods used by
 *  template action listerners.</p>
 * @author <a href="mailto:esmiley@stanford.edu">Ed Smiley</a>
 * @version $Id$
 */
@Slf4j
public abstract class TemplateBaseListener implements ActionListener
{

  // forces you to implement your own processAction
  abstract public void processAction(ActionEvent parm1) throws javax.faces.event.AbortProcessingException;

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
      log.error(ex.getMessage(), ex);
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
    //FacesContext facesContext = FacesContext.getCurrentInstance();
    ApplicationFactory factory = (ApplicationFactory) FactoryFinder.getFactory(
        FactoryFinder.APPLICATION_FACTORY);
    Application application = factory.getApplication();
    templateBean = (TemplateBean)
        application.getVariableResolver().resolveVariable(context, "template");
    return templateBean;
  }

}
