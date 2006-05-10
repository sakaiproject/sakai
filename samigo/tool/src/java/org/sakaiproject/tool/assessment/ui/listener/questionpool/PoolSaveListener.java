
package org.sakaiproject.tool.assessment.ui.listener.questionpool;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Iterator;
import org.osid.shared.Id;
import org.osid.shared.SharedException;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.application.FacesMessage;

import javax.faces.context.FacesContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.QuestionPoolFacade;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.ui.bean.questionpool.QuestionPoolBean;
import org.sakaiproject.tool.assessment.ui.bean.questionpool.QuestionPoolDataBean;
import org.sakaiproject.tool.assessment.data.dao.questionpool.QuestionPoolData;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.data.model.Tree;
import org.sakaiproject.tool.assessment.business.questionpool.QuestionPoolTreeImpl;
/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @version $Id$
 */

public class PoolSaveListener implements ActionListener
{

  private static Log log = LogFactory.getLog(PoolSaveListener.class);
  private static ContextUtil cu;


  /**
   * Standard process action method.
   * @param ae ActionEvent
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    //log.info("PoolSaveListener :");
    QuestionPoolBean  qpoolbean= (QuestionPoolBean) cu.lookupBean("questionpool");
    String currentName= qpoolbean.getCurrentPool().getDisplayName();
   
    boolean isUnique=true;
    QuestionPoolService service = new QuestionPoolService();
    QuestionPoolDataBean bean = qpoolbean.getCurrentPool();
    Long currentId = new Long ("0");
    FacesContext context = FacesContext.getCurrentInstance();
      if(bean.getId() != null)
      {
	  currentId = bean.getId();
      }

      Long currentParentId = new Long("0");
      if(bean.getParentPoolId() != null)
      {
        currentParentId = bean.getParentPoolId();
      }
     if(currentName!=null &&(currentName.trim()).equals("")){
     	String err1=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.QuestionPoolMessages","poolName_empty");
	context.addMessage(null,new FacesMessage(err1));
        qpoolbean.setOutcomeEdit("editPool");
	qpoolbean.setOutcome("addPool");
	return;
    }
     
    try {
       
	if((qpoolbean.getAddOrEdit()).equals("add")){
	    isUnique=service.poolIsUnique("0",currentName,""+currentParentId);
	}
	else {
	     isUnique=service.poolIsUnique(""+currentId,currentName,""+currentParentId);
	}
       
	if(!isUnique){
	   
	   String error=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.QuestionPoolMessages","duplicateName_error");
	   
	   context.addMessage(null,new FacesMessage(error));
          
	   qpoolbean.setOutcomeEdit("editPool");
	   qpoolbean.setOutcome("addPool");
	 
	   return;
        }
       
	
	if (!savePool(qpoolbean)){
		
	    throw new RuntimeException("failed to populateItemBean.");   
	}
   
        if((qpoolbean.getAddOrEdit()).equals("edit")){
	    if (!startRemoveItems(qpoolbean)){
		throw new RuntimeException("failed to populateItemBean.");
	    }
	}
        
	   
    }
    catch(Exception e){
	throw new Error(e);
    } //if error=false then save, if not then create error message
    
  }

  public boolean savePool(QuestionPoolBean qpbean) {

    try
    {
      QuestionPoolDataBean bean = qpbean.getCurrentPool();
      Long beanid = new Long ("0");
      if(bean.getId() != null)
      {
        beanid = bean.getId();
      }

      Long parentid = new Long("0");
      if(bean.getParentPoolId() != null)
      {
        parentid = bean.getParentPoolId();
      }

      QuestionPoolFacade questionpool =
        new QuestionPoolFacade (beanid, parentid);
      questionpool.updateDisplayName(bean.getDisplayName());
      questionpool.updateDescription(bean.getDescription());
      questionpool.setOrganizationName(bean.getOrganizationName());
      questionpool.setObjectives(bean.getObjectives());
      questionpool.setKeywords(bean.getKeywords());
// need to set owner and accesstype
//owner is hardcoded for now
      questionpool.setOwnerId(AgentFacade.getAgentString());
      questionpool.setAccessTypeId(QuestionPoolFacade.ACCESS_DENIED); // set as default

      QuestionPoolService delegate = new QuestionPoolService();
      //log.info("Saving pool");
      delegate.savePool(questionpool);

      // Rebuild the tree with the new pool
      qpbean.buildTree();
//	qpbean.setCurrentPool(null);



      //  System.out.println( "SAVE - POOLSOURCE= "+qpbean.getAddPoolSource());
      //where do you get value from addPoolSource?  It always return null though.
      if ("editpool".equals(qpbean.getAddPoolSource())) {
    // so reset subpools tree
//    QuestionPoolFacade thepool= delegate.getPool(parentid, AgentFacade.getAgentString());
//    qpbean.getCurrentPool().setNumberOfSubpools(thepool.getSubPoolSize().toString());
      qpbean.startEditPoolAgain(parentid.toString());  // return to edit poolwith the current pool set to the parentpool
	qpbean.setOutcome("editPool");
	qpbean.setAddPoolSource("");
      }
      else {
	  qpbean.setOutcomeEdit("poolList");
	qpbean.setOutcome("poolList");
      }
   }
    catch(Exception e)
    {
      e.printStackTrace();
      return false;
    }


	// set outcome for action
	return true;
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
