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

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.sakaiproject.tool.assessment.facade.AssessmentTemplateFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.TemplateBean;
import org.sakaiproject.user.cover.UserDirectoryService;

/**
 * <p> Stub</p>
 * <p>Description: Action Listener to confrim deletion of template.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class ConfirmDeleteTemplateListener
  extends TemplateBaseListener
  implements ActionListener
{
  public void processAction(ActionEvent ae) throws
    AbortProcessingException
  {
    FacesContext context = FacesContext.getCurrentInstance();
    //Map reqMap = context.getExternalContext().getRequestMap();
    //Map requestParams = context.getExternalContext().getRequestParameterMap();

    String templateId = (String) FacesContext.getCurrentInstance().
        getExternalContext().getRequestParameterMap().get("templateId");
    AssessmentService assessmentService = new AssessmentService();
    AssessmentTemplateFacade template = assessmentService.getAssessmentTemplate(templateId);

    TemplateBean templateBean = lookupTemplateBean(context);

    String author =  (String)template.getCreatedBy();
    if (author == null || !author.equals(UserDirectoryService.getCurrentUser().getId())) {
        throw new AbortProcessingException("Attempted to delete template owned by another " + author + " " + UserDirectoryService.getCurrentUser().getId());
    }

    templateBean.setIdString(templateId);
    templateBean.setTemplateName(template.getTitle());
  }

  /**
   * Obtain the deleteId parameter.
   * @param requestParams params passed
   * @return true if we have no id parameter
   */
  /*
  private String lookupKey(String key, Map requestParams)
  {
    Iterator iter = requestParams.keySet().iterator();
    while (iter.hasNext())
    {
      String currKey = (String) iter.next();
      if (currKey.endsWith(key))
      {
        return (String) requestParams.get(currKey);
      }
    }
    return null;
  }
  */
}
