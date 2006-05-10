
package org.sakaiproject.tool.assessment.ui.listener.evaluation;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.assessment.ui.bean.evaluation.QuestionScoresBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

import java.util.HashMap;

/**
 * <p>Title: Samigo</p>
 * <p>Purpose:  this module handles the beginning of the assessment
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id: ResetQuestionScoreListener.java 694 2005-07-20 04:56:48Z daisyf@stanford.edu $
 */

public class ResetQuestionScoreListener implements ActionListener
{
  private static Log log = LogFactory.getLog(ResetQuestionScoreListener.class);
  private static ContextUtil cu;

  /**
   * ACTION.
   * @param ae
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws
    AbortProcessingException
  {
    QuestionScoresBean bean = (QuestionScoresBean) cu.lookupBean("questionScores");
    bean.setItemScoresMap(new HashMap());
    bean.setPublishedAssessment(null);
    //System.out.println("****reset itemScoresMap");
  }
}
