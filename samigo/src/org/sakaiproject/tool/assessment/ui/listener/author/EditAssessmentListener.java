/*
 * Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
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
 */

package org.sakaiproject.tool.assessment.ui.listener.author;

import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemAuthorBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>2
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id: EditAssessmentListener.java,v 1.8 2005/05/31 19:14:24 janderse.umich.edu Exp $
 */

public class EditAssessmentListener
    implements ActionListener
{
  private static Log log = LogFactory.getLog(EditAssessmentListener.class);
  private static ContextUtil cu;

  public EditAssessmentListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    FacesContext context = FacesContext.getCurrentInstance();
    Map reqMap = context.getExternalContext().getRequestMap();
    Map requestParams = context.getExternalContext().getRequestParameterMap();
    //System.out.println("debugging ActionEvent: " + ae);
    //System.out.println("debug requestParams: " + requestParams);
    //System.out.println("debug reqMap: " + reqMap);

    AssessmentBean assessmentBean = (AssessmentBean) cu.lookupBean(
                                          "assessmentBean");

    ItemAuthorBean itemauthorBean = (ItemAuthorBean) cu.lookupBean(
                                          "itemauthor");

    // #1a - come from authorIndex.jsp, load the assessment
    // goto editAssessment.jsp
    String assessmentId = (String) FacesContext.getCurrentInstance().
        getExternalContext().getRequestParameterMap().get("assessmentId");
    AssessmentService assessmentService = new AssessmentService();
    AssessmentFacade assessment = assessmentService.getAssessment(
        assessmentId);
    //System.out.println("** assessment = "+assessment);
    //System.out.println("** assessment Bean= "+assessmentBean);
    assessmentBean.setAssessment(assessment);
    itemauthorBean.setTarget(itemauthorBean.FROM_ASSESSMENT); // save to assessment
    // initalize the itemtype
    itemauthorBean.setItemType("");
    itemauthorBean.setItemTypeString("");

  }

}
