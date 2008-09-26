/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/samigo-app/src/java/org/sakaiproject/tool/assessment/ui/bean/questionpool/QuestionPoolBean.java $
 * $Id: QuestionPoolBean.java 46438 2008-05-14 22:05:27Z ktsao@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2006 Sakai Foundation
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



package org.sakaiproject.tool.assessment.ui.bean.questionpool;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.data.dao.questionpool.QuestionPoolData;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.QuestionPoolFacade;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.BeanSort;

// from navigo


/**
 * This holds question pool information.
 *
 * Used to be org.navigoproject.ui.web.form.questionpool.QuestionPoolForm
 *
 * @author Rachel Gollub <rgollub@stanford.edu>
 * @author Lydia Li<lydial@stanford.edu>
 * $Id: QuestionPoolBean.java 46438 2008-05-14 22:05:27Z ktsao@stanford.edu $
 */
public class QuestionPoolShareBean implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1154189308380595101L;

	private static Log log = LogFactory.getLog(QuestionPoolShareBean.class);
	
	// QuestionPool
	private Long questionPoolId;
	private String questionPoolOwnerId;
	private String questionPoolName;

	private String[] destPools = {  }; // for multibox jsf
  
	// for sorting
	private String sortPropertyWith = "displayName";
	private boolean sortAscendingWith = true;
	private String sortPropertyWithout = "displayName";
	private boolean sortAscendingWithout = true;
  
	// collections of Agents
	private Collection<AgentFacade> agentsWithAccess;
	private Collection<AgentFacade> agentsWithoutAccess;


  	/**
  	 * Creates a new QuestionPoolShareBean object.
  	 */
  	public QuestionPoolShareBean()
  	{
  	}

  	public String startSharePool()
  	{
  		log.debug("inside startSharePool()");  
	
  		String qpid = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("qpid");

  		QuestionPoolService delegate = new QuestionPoolService();
    
  		QuestionPoolFacade thepool = delegate.getPool(new Long(qpid), AgentFacade.getAgentString());
         
  		setAgentsWithAccess(delegate.getAgentsWithAccess(new Long(qpid)));
  		setAgentsWithoutAccess(delegate.getAgentsWithoutAccess(new Long(qpid), AgentFacade.getCurrentSiteId()));
  		setQuestionPoolId(new Long(qpid));
  		setQuestionPoolOwnerId(thepool.getOwnerId());
  		setQuestionPoolName(thepool.getDisplayName());
	
  		// order by default
  		sortAgentsWithAccess();
  		sortAgentsWithoutAccess();
  		
  		return "sharePool";
  	}
  
  	public String sharePool() {

  		QuestionPoolService delegate = new QuestionPoolService();
	  
  		ArrayList<String> revoke = ContextUtil.paramArrayValueLike("revokeCheckbox");
 	
  		Iterator<String> iter = revoke.iterator();
  		while(iter.hasNext()) {
  			String agentId = (String) iter.next();
          
  			try {
  				delegate.removeQuestionPoolAccess(agentId, getQuestionPoolId(), QuestionPoolData.READ_COPY);
  			}
  			catch(Exception e) {
  				e.printStackTrace();
  				throw new RuntimeException(e);
  			}
  		}
	  
  		ArrayList<String> grant = ContextUtil.paramArrayValueLike("grantCheckbox");
	 	
  		iter = grant.iterator();
  		while(iter.hasNext()) {
  			String agentId = (String) iter.next();
          
  			try {
  				delegate.addQuestionPoolAccess(agentId, this.getQuestionPoolId(), QuestionPoolData.READ_COPY);
  			}
  			catch(Exception e) {
  				e.printStackTrace();
  				throw new RuntimeException(e);
  			}
  		}

  		return "poolList";
  	}

  	public String sortByColumnHeader() {
  		String sortString = ContextUtil.lookupParam("orderBy");
  		String ascending = ContextUtil.lookupParam("ascending");
  		String list = ContextUtil.lookupParam("list");
  		
  		if (list.equals("agentsWithAccess")) {
  			this.setSortPropertyWith(sortString);
  	  		this.setSortAscendingWith((Boolean.valueOf(ascending)).booleanValue());
  			
  			sortAgentsWithAccess();
  		}
  		else { 
  			this.setSortPropertyWithout(sortString);
  	  		this.setSortAscendingWithout((Boolean.valueOf(ascending)).booleanValue());
  	  		
  	  		sortAgentsWithoutAccess();
  		}
        
  		return "shareList";
  	}
  	
  	public void sortAgentsWithAccess() {
  		BeanSort sort = new BeanSort(agentsWithAccess, sortPropertyWith);
		sort.toStringSort();
	        
		agentsWithAccess = sortAscendingWith ? (ArrayList)sort.sort() : (ArrayList)sort.sortDesc();
  	}

  	public void sortAgentsWithoutAccess() {
  		BeanSort sort = new BeanSort(agentsWithoutAccess, sortPropertyWithout);
		sort.toStringSort();
	        
		agentsWithoutAccess = sortAscendingWithout ? (ArrayList)sort.sort() : (ArrayList)sort.sortDesc();
  	}

  	  
	public Collection<AgentFacade> getAgentsWithAccess() {
		return agentsWithAccess;
	}

	public void setAgentsWithAccess(Collection<AgentFacade> agentsWithAccess) {
		this.agentsWithAccess = agentsWithAccess;
	}

	public Collection<AgentFacade> getAgentsWithoutAccess() {
		return agentsWithoutAccess;
	}

	public void setAgentsWithoutAccess(Collection<AgentFacade> agentsWithoutAccess) {
		this.agentsWithoutAccess = agentsWithoutAccess;
	}

	public String getQuestionPoolOwnerId() {
		return questionPoolOwnerId;
	}

	public void setQuestionPoolOwnerId(String questionPoolOwnerId) {
		this.questionPoolOwnerId = questionPoolOwnerId;
	}

	public Long getQuestionPoolId() {
		return questionPoolId;
	}

	public void setQuestionPoolId(Long questionPoolId) {
		this.questionPoolId = questionPoolId;
	}

	public String getQuestionPoolName() {
		return questionPoolName;
	}

	public void setQuestionPoolName(String questionPoolName) {
		this.questionPoolName = questionPoolName;
	}

	public void setDestPools(String[] destPools) {
		this.destPools = destPools;
	}

	public String[] getDestPools() {
		return destPools;
	}

	public void setSortPropertyWith(String sortPropertyWith) {
		this.sortPropertyWith = sortPropertyWith;
	}

	public String getSortPropertyWith() {
		return sortPropertyWith;
	}

	public void setSortAscendingWith(boolean sortAscendingWith) {
		this.sortAscendingWith = sortAscendingWith;
	}

	public boolean isSortAscendingWith() {
		return sortAscendingWith;
	}

	public void setSortPropertyWithout(String sortPropertyWithout) {
		this.sortPropertyWithout = sortPropertyWithout;
	}

	public String getSortPropertyWithout() {
		return sortPropertyWithout;
	}

	public void setSortAscendingWithout(boolean sortAscendingWithout) {
		this.sortAscendingWithout = sortAscendingWithout;
	}

	public boolean isSortAscendingWithout() {
		return sortAscendingWithout;
	}

}
