/**********************************************************************************
*
* $Header: /cvs/sakai2/sam/src/org/sakaiproject/tool/assessment/ui/listener/author/ExportAssessmentListener.java,v 1.9 2005/05/31 19:14:24 janderse.umich.edu Exp $
*
***********************************************************************************
 * Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.ui.bean.qti.XMLController;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id: ExportAssessmentListener.java,v 1.9 2005/05/31 19:14:24 janderse.umich.edu Exp $
 */

public class ExportAssessmentListener implements ActionListener
{
  private static Log log = LogFactory.getLog(ExportAssessmentListener.class);
  private static ContextUtil cu;



  public ExportAssessmentListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    log.info("ExportAssessmentListener");
    log.info("ExportAssessmentListener processAction");
    String assessmentId = (String) cu.lookupParam("assessmentId");
    log.info("ExportAssessmentListener assessmentId="+assessmentId);
    XMLController xmlController = (XMLController) cu.lookupBean(
                                          "xmlController");
    log.info("ExportAssessmentListener xmlController.setId(assessmentId)");
    xmlController.setId(assessmentId);
// debug
//    xmlController.setQtiVersion(2);
    log.info("xmlController.setQtiVersion(1)");
    xmlController.setQtiVersion(1);
    log.info(
        "ExportAssessmentListener xmlController.getQtiVersion(): " +
        xmlController.getQtiVersion());
    log.info("ExportAssessmentListener xmlController.displayAssessmentXml");
    xmlController.displayAssessmentXml();
    log.info("ExportAssessmentListener processAction done");
  }

}
