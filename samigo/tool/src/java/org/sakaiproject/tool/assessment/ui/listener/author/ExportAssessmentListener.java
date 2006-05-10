/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the"License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/


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
 * @version $Id$
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
    //log.info("ExportAssessmentListener");
    //log.info("ExportAssessmentListener processAction");
    String assessmentId = (String) cu.lookupParam("assessmentId");
    //log.info("ExportAssessmentListener assessmentId="+assessmentId);
    XMLController xmlController = (XMLController) cu.lookupBean(
                                          "xmlController");
    //log.info("ExportAssessmentListener xmlController.setId(assessmentId)");
    xmlController.setId(assessmentId);
// debug
//    xmlController.setQtiVersion(2);
    //log.info("xmlController.setQtiVersion(1)");
    xmlController.setQtiVersion(1);
    //log.info("ExportAssessmentListener xmlController.displayAssessmentXml");
    xmlController.displayAssessmentXml();
    //log.info("ExportAssessmentListener processAction done");
  }

}
