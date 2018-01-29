/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
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
import java.util.Date;
import java.util.List;
import java.util.Iterator;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.QuestionPoolFacade;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.ui.bean.questionpool.QuestionPoolBean;
import org.sakaiproject.tool.assessment.ui.bean.questionpool.QuestionPoolDataBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.TextFormat;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * @version $Id$
 */

@Slf4j
 public class PoolSaveListener implements ActionListener
{

  /**
   * Standard process action method.
   * @param ae ActionEvent
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    QuestionPoolBean  qpoolbean= (QuestionPoolBean) ContextUtil.lookupBean("questionpool");
    String currentName= TextFormat.convertPlaintextToFormattedTextNoHighUnicode(qpoolbean.getCurrentPool().getDisplayName());
   
    String addsource = (String)ae.getComponent().getAttributes().get("addsource");
    qpoolbean.setAddPoolSource(addsource);
    
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
      questionpool.updateDisplayName(TextFormat.convertPlaintextToFormattedTextNoHighUnicode(bean.getDisplayName()));
      questionpool.updateDescription(TextFormat.convertPlaintextToFormattedTextNoHighUnicode(bean.getDescription()));
      questionpool.setOrganizationName(TextFormat.convertPlaintextToFormattedTextNoHighUnicode(bean.getOrganizationName()));
      questionpool.setObjectives(TextFormat.convertPlaintextToFormattedTextNoHighUnicode(bean.getObjectives()));
      questionpool.setKeywords(TextFormat.convertPlaintextToFormattedTextNoHighUnicode(bean.getKeywords()));
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
      delegate.savePool(questionpool);

      // Rebuild the tree with the new pool
      qpbean.buildTree();

      //where do you get value from addPoolSource?  It always return null though.
      if ("editpool".equals(qpbean.getAddPoolSource()) && !qpbean.ORIGIN_TOP.equals(qpbean.getOutcome()) && (qpbean.getOutcomePool() > 0) ) {
    // so reset subpools tree
//    QuestionPoolFacade thepool= delegate.getPool(parentid, AgentFacade.getAgentString());
//    qpbean.getCurrentPool().setNumberOfSubpools(thepool.getSubPoolSize().toString());
      qpbean.startEditPoolAgain(Long.toString(qpbean.getOutcomePool()));  // return to edit pool with the pool where the action was performed
      
      // Reset the properties for current pool to reflect the early changes (before click on "Add") in edit pool
      QuestionPoolDataBean currentPool = qpbean.getCurrentPool();
      currentPool.setDisplayName(TextFormat.convertPlaintextToFormattedTextNoHighUnicode(qpbean.getParentPool().getDisplayName()));
      currentPool.setOrganizationName(TextFormat.convertPlaintextToFormattedTextNoHighUnicode(qpbean.getParentPool().getOrganizationName()));
      currentPool.setDescription(TextFormat.convertPlaintextToFormattedTextNoHighUnicode(qpbean.getParentPool().getDescription()));
      currentPool.setObjectives(TextFormat.convertPlaintextToFormattedTextNoHighUnicode(qpbean.getParentPool().getObjectives()));
      currentPool.setKeywords(TextFormat.convertPlaintextToFormattedTextNoHighUnicode(qpbean.getParentPool().getKeywords()));
      List addedPools = qpbean.getAddedPools();
      if (addedPools == null) {
    	  addedPools = new ArrayList();
      }
      addedPools.add(questionpool.getQuestionPoolId());
      qpbean.setAddedPools(addedPools);
	  qpbean.setOutcome("editPool");
	  qpbean.setAddPoolSource("");
	  qpbean.setSubQpDataModelByLevel();
      }
      else if("editpoolattr".equals(qpbean.getAddPoolSource())){
    	  //when change the pool's field
    	  qpbean.startEditPoolAgain(bean.getId().toString());
    	  qpbean.setOutcomeEdit("editPool");
    	  qpbean.setOutcome("editPool");
          qpbean.setSubQpDataModelByLevel();
      }
      else {
	  qpbean.setOutcomeEdit("poolList");
	  qpbean.setOutcome("poolList");
	  qpbean.setCurrentPool(null);
	  qpbean.setOutcomePool(0);
		
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
      log.error(e.getMessage(), e);
      return false;	
    }
    return true;
  }
}
