/**********************************************************************************
* $HeadURL$
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

import java.util.Iterator;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.facade.AssessmentTemplateFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.TemplateBean;

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
  private static Log log = LogFactory.getLog(ConfirmDeleteTemplateListener.class);

  public void processAction(ActionEvent ae) throws
    AbortProcessingException
  {
    FacesContext context = FacesContext.getCurrentInstance();
    Map reqMap = context.getExternalContext().getRequestMap();
    Map requestParams = context.getExternalContext().
                        getRequestParameterMap();
    log.info("CONFIRM DELETE TEMPLATE LISTENER.");

    String templateId = (String) FacesContext.getCurrentInstance().
        getExternalContext().getRequestParameterMap().get("templateId");
    AssessmentService assessmentService = new AssessmentService();
    AssessmentTemplateFacade template = assessmentService.getAssessmentTemplate(
        templateId.toString());

    TemplateBean templateBean = lookupTemplateBean(context);
    templateBean.setIdString(templateId);
    templateBean.setTemplateName(template.getTitle());
  }

  /**
   * Obtain the deleteId parameter.
   * @param requestParams params passed
   * @return true if we have no id parameter
   */
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

}
