/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008, 2009 The Sakai Foundation
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

import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentTemplateData;
import org.sakaiproject.tool.assessment.facade.AssessmentTemplateFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.listener.author.TemplateListener;

/**
 * <p>Description: Action Listener for deletion of template.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */
@Slf4j
public class DeleteTemplateListener extends TemplateBaseListener implements ActionListener
{
  //boolean isTemplate = true;

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    FacesContext context = FacesContext.getCurrentInstance();

    String deleteId = this.lookupTemplateBean(context).getIdString();
    if(!deleteTemplate(deleteId))
    {
      // todo: define package specific RuntimeException
      throw new RuntimeException("Cannot delete template.");
    }
    // reset template list
    TemplateListener lis = new TemplateListener();
    lis.processAction(null); 
  }

  /**
   * This deletes a template with all its associated parts, items, etc..
   *
   * @param session DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public boolean deleteTemplate(String deleteId)
  {

    try
    {
      AssessmentService delegate = new AssessmentService();
      //rules: if the template has been used by assessment, we set
      // its status = 0 (inactive) instead of removing it from the DB
      List l = delegate.getAssessmentByTemplate(deleteId);
      if (l.size()==0){ // save to delete
        delegate.deleteAssessmentTemplate(new Long(deleteId));
      }
      else{ // set status to "0"
	AssessmentTemplateFacade t = delegate.getAssessmentTemplate(deleteId);
        t.setStatus(AssessmentTemplateFacade.INACTIVE_STATUS);
        delegate.save((AssessmentTemplateData)t.getData());
      }
      return true;
    }
    catch(Exception e)
    {
      log.error(e.getMessage(), e);
      return false;
    }
  }

}
