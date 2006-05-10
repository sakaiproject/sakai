
package org.sakaiproject.tool.assessment.ui.listener.questionpool;

import java.util.ArrayList;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.assessment.ui.bean.questionpool.QuestionPoolBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>2
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @version $Id$
 */

public class SortQuestionListListener
    implements ActionListener
{
  private static Log log = LogFactory.getLog(SortQuestionListListener.class);
  private static ContextUtil cu;


  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    FacesContext context = FacesContext.getCurrentInstance();

    // get service and managed bean
    QuestionPoolBean questionpoolbean = (QuestionPoolBean) cu.lookupBean("questionpool");

    String orderBy = cu.lookupParam("orderBy");
      QuestionPoolService delegate = new QuestionPoolService();
      ArrayList list = delegate.getAllItemsSorted(questionpoolbean.getCurrentPool().getId(), orderBy);
      questionpoolbean.setAllItems(list);

  }
}
