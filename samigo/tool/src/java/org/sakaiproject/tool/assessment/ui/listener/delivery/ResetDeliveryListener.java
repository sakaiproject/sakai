/**********************************************************************************
* $URL: https://source.sakaiproject.org/svn/sam/trunk/tool/src/java/org/sakaiproject/tool/assessment/ui/listener/select/SelectActionListener.java $
* $Id: SelectActionListener.java 5242 2006-01-11 04:36:19Z daisyf@stanford.edu $
***********************************************************************************
*
* Copyright (c) 2004-2006 The Sakai Foundation.
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      https://source.sakaiproject.org/svn/sakai/trunk/sakai_license_1_0.html
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

import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

import java.util.ArrayList;

/**
 * <p>Title: Samigo</p>
 * <p>Purpose:  this module handles the beginning of the assessment
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @version $Id: ResetDeliveryListener.java 694 2005-07-20 04:56:48Z daisyf@stanford.edu $
 */

public class ResetDeliveryListener implements ActionListener
{
  private static Log log = LogFactory.getLog(ResetDeliveryListener.class);
  private static ContextUtil cu;

  /**
   * ACTION.
   * @param ae
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws
    AbortProcessingException
  {
    DeliveryBean bean = (DeliveryBean) cu.lookupBean("delivery");
    bean.setPublishedAssessment(null);
    bean.setPublishedItemHash(null);
    bean.setPublishedItemTextHash(null);
    bean.setPublishedAnswerHash(null);
    // reset timer before begin
    bean.setTimeElapse("0");
    bean.setTimeElapseAfterFileUpload(null);
    bean.setLastTimer(0);
    bean.setTimeLimit("0");
  }
}
