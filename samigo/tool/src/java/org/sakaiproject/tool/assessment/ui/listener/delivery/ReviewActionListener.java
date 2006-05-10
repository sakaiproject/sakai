/**********************************************************************************
* $URL$
* $Id$
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

import java.util.HashMap;
import java.util.Iterator;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.ItemContentsBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.SectionContentsBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>
 * <p>Purpose:  this module creates the lists of published assessments for the select index
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class ReviewActionListener implements ActionListener
{
  private static Log log = LogFactory.getLog(ReviewActionListener.class);
  private static ContextUtil cu;

  /**
   * ACTION.
   * @param ae
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws
    AbortProcessingException
  {
    //log.info("ReviewActionListener.processAction() ");

    try {
      // get managed bean
      DeliveryBean delivery = (DeliveryBean) cu.lookupBean("delivery");

      String reviewAssessmentId = cu.lookupParam("reviewAssessmentId");

      // get service
      PublishedAssessmentService publishedAssessmentService = new
        PublishedAssessmentService();

      // get assessment
      PublishedAssessmentFacade publishedAssessment =
        publishedAssessmentService.getPublishedAssessment(reviewAssessmentId);
      HashMap publishedAnswerHash = publishedAssessmentService.preparePublishedAnswerHash(publishedAssessment);

      GradingService service = new GradingService();
      HashMap itemData = service.getLastItemGradingData
        (reviewAssessmentId, AgentFacade.getAgentString());

      // get current page contents
      if (delivery.getPageContents() == null)
      {
        DeliveryActionListener listener = new DeliveryActionListener();
        delivery.setPageContents(listener.getPageContents(publishedAssessment, delivery, 
        itemData, publishedAnswerHash));
      }

      Iterator iter = delivery.getPageContents().getPartsContents().iterator();
      while (iter.hasNext()) {

        SectionContentsBean sectbean = (SectionContentsBean) iter.next();
        Iterator itemiter = sectbean.getItemContents().iterator();
        while (itemiter.hasNext()) {
          ItemContentsBean itembean = (ItemContentsBean) itemiter.next();
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
