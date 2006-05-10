
package org.sakaiproject.tool.assessment.ui.listener.questionpool;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.ui.bean.questionpool.QuestionPoolBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @version $Id$
 */

public class StartRemoveItemsListener implements ActionListener
{
  private static Log log = LogFactory.getLog(StartRemoveItemsListener.class);
  private static ContextUtil cu;


  /**
   * Standard process action method.
   * @param ae ActionEvent
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    //log.info("StartRemoveItemsListener:");
    QuestionPoolBean  qpoolbean= (QuestionPoolBean) cu.lookupBean("questionpool");
    if (!startRemoveItems(qpoolbean))
    {
      throw new RuntimeException("failed to populateItemBean.");
    }

  }


  public boolean startRemoveItems(QuestionPoolBean qpoolbean){
// used by the editPool.jsp, to remove one or more items
    try {
      String itemId= "";

      ArrayList destItems= ContextUtil.paramArrayValueLike("removeCheckbox");

      if (destItems.size() > 0) {
                // only go to remove confirmatin page when at least one  checkbox is checked

        List items= new ArrayList();
	Iterator iter = destItems.iterator();
        while(iter.hasNext())
        {

          itemId = (String) iter.next();

          ItemService delegate = new ItemService();
          ItemFacade itemfacade= delegate.getItem(new Long(itemId), AgentFacade.getAgentString());
          items.add(itemfacade);

        }

      qpoolbean.setItemsToDelete(items);
      qpoolbean.setOutcomeEdit("removeQuestionFromPool");
      qpoolbean.setOutcome("removeQuestionFromPool");
      }
      else {
         // otherwise go to poollist
        qpoolbean.setOutcome("poolList");
        }
    }
    catch (Exception e) {
      e.printStackTrace();
      return false;	
    }
    return true;
  }


}
