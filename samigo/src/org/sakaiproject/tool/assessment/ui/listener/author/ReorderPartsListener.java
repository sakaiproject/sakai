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

import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.SectionFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.SectionContentsBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @version $Id$
 */

public class ReorderPartsListener
    implements ValueChangeListener
{
    private static Log log = LogFactory.getLog(ReorderPartsListener.class);

  /**
   * Standard process action method.
   * @param ae ValueChangeEvent
   * @throws AbortProcessingException
   */
  public void processValueChange(ValueChangeEvent ae) throws AbortProcessingException
  {
    log.info("ReorderQuestionsListener valueChangeLISTENER.");
    SectionContentsBean partBean  = (SectionContentsBean) ContextUtil.lookupBean("partBean");
    AssessmentBean assessmentBean  = (AssessmentBean) ContextUtil.lookupBean("assessmentBean");

    FacesContext context = FacesContext.getCurrentInstance();

    Map reqMap = context.getExternalContext().getRequestMap();
    Map requestParams = context.getExternalContext().getRequestParameterMap();
    //System.out.println("debugging ActionEvent: " + ae);
    //System.out.println("debug requestParams: " + requestParams);
    //System.out.println("debug reqMap: " + reqMap);

    String oldPos= ae.getOldValue().toString();
    //System.out.println("**** ae.getOldValue : " + oldPos);
    String newPos= ae.getNewValue().toString();
    //System.out.println("**** ae.getNewValue : " + newPos);

    // get sections with oldPos
    AssessmentFacade assessment = assessmentBean.getAssessment();
    SectionFacade section1 = (SectionFacade) assessment.getSection(new Long(oldPos));
    SectionFacade section2 = (SectionFacade) assessment.getSection(new Long(newPos));
    if (section1!=null && section2!=null){
      section1.setSequence(new Integer(newPos));
      section2.setSequence(new Integer(oldPos));
      AssessmentService service = new AssessmentService();
      service.saveOrUpdateSection(section1);
      service.saveOrUpdateSection(section2);
    }

   // goto editAssessment.jsp, so reset assessmentBean
    assessmentBean.setAssessment(assessment);
  }
}
