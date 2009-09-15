/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/samigo-app/src/java/org/sakaiproject/tool/assessment/ui/listener/questionpool/PoolSaveListener.java $
 * $Id: PoolSaveListener.java 17186 2006-10-18 07:10:01Z lydial@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/


package org.sakaiproject.tool.assessment.ui.listener.questionpool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Iterator;
 
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.model.ListDataModel;
import javax.faces.application.FacesMessage;

import javax.faces.context.FacesContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.data.model.Tree;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.QuestionPoolFacade;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.ui.bean.questionpool.QuestionPoolBean;
import org.sakaiproject.tool.assessment.ui.bean.questionpool.QuestionPoolDataBean;
import org.sakaiproject.tool.assessment.ui.bean.questionpool.QuestionPoolDataModel;
//import org.sakaiproject.tool.assessment.data.dao.questionpool.QuestionPoolData;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.util.FormattedText;
//import org.sakaiproject.tool.assessment.data.model.Tree;
//import org.sakaiproject.tool.assessment.business.questionpool.QuestionPoolTreeImpl;
/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * @version $Id: PoolSaveListener.java 17186 2006-10-18 07:10:01Z lydial@stanford.edu $
 */

public class PoolSaveListener implements ActionListener
{

  private static Log log = LogFactory.getLog(PoolSaveListener.class);
  //private static ContextUtil cu;


  /**
   * Standard process action method.
   * @param ae ActionEvent
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    //log.info("PoolSaveListener :");
    QuestionPoolBean  qpoolbean= (QuestionPoolBean) ContextUtil.lookupBean("questionpool");
    String currentName= FormattedText.convertPlaintextToFormattedText(qpoolbean.getCurrentPool().getDisplayName());
   
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
	    isUnique=service.poolIsUnique("0",currentName,""+currentParentId, AgentFacade.getAgentString()) ;
	}
	else {
	     isUnique=service.poolIsUnique(""+currentId,currentName,""+currentParentId, AgentFacade.getAgentString());
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
    qpoolbean.setAddOrEdit("edit");
	   
    }
    catch(Exception e){
	throw new RuntimeException(e);
    } //if error=false then save, if not then create error message
    
  }

  public boolean savePool(QuestionPoolBean qpbean) {
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
      questionpool.updateDisplayName(FormattedText.convertPlaintextToFormattedText(bean.getDisplayName()));
      questionpool.updateDescription(FormattedText.convertPlaintextToFormattedText(bean.getDescription()));
      questionpool.setOrganizationName(FormattedText.convertPlaintextToFormattedText(bean.getOrganizationName()));
      questionpool.setObjectives(FormattedText.convertPlaintextToFormattedText(bean.getObjectives()));
      questionpool.setKeywords(FormattedText.convertPlaintextToFormattedText(bean.getKeywords()));
      // need to set owner and accesstype
      questionpool.setAccessTypeId(QuestionPoolFacade.ACCESS_DENIED); // set as default

      QuestionPoolService service = new QuestionPoolService();
      // add pool
      if (beanid.toString().equals("0")) {
    	  questionpool.setOwnerId(AgentFacade.getAgentString());
    	  questionpool.setDateCreated(new Date());
      }
      // edit pool
      else {
    	  questionpool.setOwnerId(service.getPool(beanid, AgentFacade.getAgentString()).getOwnerId());
    	  questionpool.setDateCreated(bean.getDateCreated());
      }
      QuestionPoolService delegate = new QuestionPoolService();
      //log.info("Saving pool");
      delegate.savePool(questionpool);

      // Rebuild the tree with the new pool
      qpbean.buildTree();

      //  System.out.println( "SAVE - POOLSOURCE= "+qpbean.getAddPoolSource());
      //where do you get value from addPoolSource?  It always return null though.
      if ("editpool".equals(qpbean.getAddPoolSource())) {
    // so reset subpools tree
//    QuestionPoolFacade thepool= delegate.getPool(parentid, AgentFacade.getAgentString());
//    qpbean.getCurrentPool().setNumberOfSubpools(thepool.getSubPoolSize().toString());
      qpbean.startEditPoolAgain(parentid.toString());  // return to edit poolwith the current pool set to the parentpoo
      
      // Reset the properties for current pool to reflect the early changes (before click on "Add") in edit pool
      QuestionPoolDataBean currentPool = qpbean.getCurrentPool();
      currentPool.setDisplayName(ContextUtil.processFormattedText(log, qpbean.getParentPool().getDisplayName()));
      currentPool.setOrganizationName(ContextUtil.processFormattedText(log, qpbean.getParentPool().getOrganizationName()));
      currentPool.setDescription(ContextUtil.processFormattedText(log, qpbean.getParentPool().getDescription()));
      currentPool.setObjectives(ContextUtil.processFormattedText(log, qpbean.getParentPool().getObjectives()));
      currentPool.setKeywords(ContextUtil.processFormattedText(log, qpbean.getParentPool().getKeywords()));
      ArrayList addedPools = qpbean.getAddedPools();
      if (addedPools == null) {
    	  addedPools = new ArrayList();
      }
      addedPools.add(questionpool.getQuestionPoolId());
      qpbean.setAddedPools(addedPools);
	  qpbean.setOutcome("editPool");
	  qpbean.setAddPoolSource("");
	  qpbean.setSubQpDataModelByLevel();
      }
      else {
	  qpbean.setOutcomeEdit("poolList");
	  qpbean.setOutcome("poolList");
      qpbean.setQpDataModelByLevel();
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
