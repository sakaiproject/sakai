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

package org.sakaiproject.tool.assessment.ui.listener.delivery;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.services.GradingService;

/**
 * <p>Title: Samigo</p>
 * <p>Purpose:  this module handles the beginning of the assessment
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class UpdateTimerListener implements ActionListener
{
  private static Log log = LogFactory.getLog(UpdateTimerListener.class);
  private static ContextUtil cu;

  /**
   * ACTION.
   * @param ae
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws
    AbortProcessingException
  {
    GradingService gradingService = new GradingService();
    // get managed bean
    DeliveryBean delivery = (DeliveryBean) cu.lookupBean("delivery");
    String elapsedString = (String) FacesContext.getCurrentInstance().
        getExternalContext().getRequestParameterMap().get("takeAssessmentForm:assessmentDeliveryHeading:elapsed");

    try{
      if (elapsedString != null){
         int elapsed = Integer.parseInt(elapsedString);
         log.debug("**** DeliveryBean 1: elapsed="+elapsed);
         delivery.setTimeElapse("" +
	   new Integer(elapsed / 10));
         log.debug("**** DeliveryBean 2: time elapsed="+delivery.getTimeElapse());
         AssessmentGradingData adata = delivery.getAssessmentGrading();
         adata.setTimeElapsed(new Integer(elapsed / 10));
         gradingService.saveOrUpdateAssessmentGrading(adata);
         delivery.setTimeRunning(true);
      }
    }
    catch (NumberFormatException ex)
    {
      log.warn(ex.getMessage());
    }
  }
}
