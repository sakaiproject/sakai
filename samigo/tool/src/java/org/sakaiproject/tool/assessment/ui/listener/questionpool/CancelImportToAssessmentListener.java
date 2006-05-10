
package org.sakaiproject.tool.assessment.ui.listener.questionpool;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.ui.bean.questionpool.QuestionPoolBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @version $Id$
 */

public class CancelImportToAssessmentListener implements ActionListener
{
  private static Log log = LogFactory.getLog(CancelImportToAssessmentListener.class);
  private static ContextUtil cu;


  /**
   * Standard process action method.
   * @param ae ActionEvent
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    //log.info("CancelImportToAssessmentListener:");
    QuestionPoolBean  qpoolbean= (QuestionPoolBean) cu.lookupBean("questionpool");
    qpoolbean.setImportToAuthoring(false);

  }

}
