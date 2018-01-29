/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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


package org.sakaiproject.tool.assessment.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.tool.assessment.data.dao.questionpool.QuestionPoolItemData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.model.Tree;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.facade.QuestionPoolFacade;
import org.sakaiproject.tool.assessment.facade.QuestionPoolIteratorFacade;

/**
 * The QuestionPoolService calls the service locator to reach the
 * manager on the back end.
 * @author Rachel Gollub <rgollub@stanford.edu>
 */
 @Slf4j
 public class QuestionPoolService
{

  /**
   * Creates a new QuestionPoolService object.
   */
  public QuestionPoolService()
  {
  }

  /**
   * Get all pools from the back end.
   */
  public List getAllPools()
  {
    List results = null;
      results =
        (List) PersistenceService.getInstance().
           getQuestionPoolFacadeQueries().getAllPools();
    return results;
  }

  /**
   * Get all pools from the back end.
   */
  public QuestionPoolIteratorFacade getAllPools(String agentId)
  {
    QuestionPoolIteratorFacade results = null;
      results =
        (QuestionPoolIteratorFacade) PersistenceService.getInstance().
           getQuestionPoolFacadeQueries().getAllPools(agentId);
    return results;
  }

  public QuestionPoolIteratorFacade getAllPoolsWithAccess(String agentId)
  {
	  QuestionPoolIteratorFacade results = null;
	  results =
		  (QuestionPoolIteratorFacade) PersistenceService.getInstance().
		  getQuestionPoolFacadeQueries().getAllPoolsWithAccess(agentId);
	  return results;
  }
  
  /**
   * Get basic info for pools(just id and  title)  for displaying in pulldown .
   */
  public List getBasicInfoOfAllPools(String agentId)
  {
    List results = null;
      results = PersistenceService.getInstance().
           getQuestionPoolFacadeQueries().getBasicInfoOfAllPools(agentId);
    return results;
  }

  /**
   * Get a particular pool from the backend, with all questions.
   */
  public QuestionPoolFacade getPool(Long poolId, String agentId)
  {
    QuestionPoolFacade pool = null;
    try
    {
      pool =
        PersistenceService.getInstance().getQuestionPoolFacadeQueries().
          getPool(poolId, agentId);
    }
    catch(Exception e)
    {
      log.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }

    return pool;
  }

  /**
   * Get a list of pools that have a specific Agent
   */
  public List getPoolIdsByItem(String itemId)
  {
    List idList = null;
    try
    {
      idList =
        PersistenceService.getInstance().getQuestionPoolFacadeQueries().
          getPoolIdsByItem(itemId);
    }
    catch(Exception e)
    {
      log.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }

    return idList;
  }


  public boolean hasItem(String itemId, Long poolId)
  {
        List poollist = null;
	boolean found = false;
    try
    {
	poollist= getPoolIdsByItem(itemId);
	if (poollist!=null) {
	found = poollist.contains(poolId);
	}
	else {
	found = false;
	}
    }
    catch(Exception e)
    {
      log.error(e.getMessage(), e);
    }
	return found;

  }

  /**
   * Get pool id's by agent.
   */
  public List<Long> getPoolIdsByAgent(String agentId)
  {
    List<Long> idList = null;
    try
    {
      idList = PersistenceService.getInstance().getQuestionPoolFacadeQueries().getPoolIdsByAgent(agentId);
    }
    catch(Exception e)
    {
      log.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }

    return idList;
  }

  /**
   * Get a list of pools that have a specific parent
   */
  public List getSubPools(Long poolId)
  {
    List poolList = null;
    try
    {
      poolList =
	  PersistenceService.getInstance().getQuestionPoolFacadeQueries().
            getSubPools(poolId);
    }
    catch(Exception e)
    {
      log.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }

    return poolList;
  }

  /**
   * Get the size of a subpool.
   */
  public long getSubPoolSize(Long poolId)
  {
    long poolSize;
    try {
      poolSize = PersistenceService.getInstance().getQuestionPoolFacadeQueries().getSubPoolSize(poolId);
    } catch(Exception e) {
      log.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
    return poolSize;
  }

  /**
   * Checks to see if a pool has subpools
   */
  public boolean hasSubPools(Long poolId)
  {
    boolean result = false;
    try
    {
      result =
        PersistenceService.getInstance().getQuestionPoolFacadeQueries().
          hasSubPools(poolId);
    }
    catch(Exception e)
    {
      log.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }

    return result;
  }

  /**
   * Get all scores for a published assessment from the back end.
   */
  public List getAllItems(Long poolId)
  {
    List results = null;
    try {
      results =
        new ArrayList(PersistenceService.getInstance().
           getQuestionPoolFacadeQueries().getAllItemFacades(poolId));
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
    return results;
  }

  /**
   * Get all scores for a published assessment from the back end.
   */
  public ArrayList getAllItemsIds(Long poolId)
  {
    ArrayList results = null;
    try {
      results =
              new ArrayList(PersistenceService.getInstance().
                      getQuestionPoolFacadeQueries().getAllItemsIds(poolId));
    } catch (Exception e) {
        log.error(e.getMessage(), e);
    }
    return results;
  }



  /**
   * Save a question to a pool.
   */
  public void addItemToPool(Long itemId, Long poolId)
  {
    try
    {
      PersistenceService.getInstance().getQuestionPoolFacadeQueries().
        addItemToPool(new QuestionPoolItemData(poolId, itemId));
    }
    catch(Exception e)
    {
      log.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Move a question to a pool.
   */
  public void moveItemToPool(Long itemId, Long sourceId, Long destId)
  {
    try
    {
      PersistenceService.getInstance().getQuestionPoolFacadeQueries().
        moveItemToPool(itemId, sourceId, destId);
    }
    catch(Exception e)
    {
      log.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Is a pool a descendant of the other?
   */
  public boolean isDescendantOf(Long poolA,Long poolB, String agentId)
  {
    try{
      Long tempPoolId = poolA;
      while((tempPoolId !=null)&&(tempPoolId.toString().compareTo("0")>0)){
        QuestionPoolFacade tempPool = getPool(tempPoolId, agentId);
        if(tempPool.getParentPoolId().toString().compareTo(poolB.toString())==0) return true;
        tempPoolId = tempPool.getParentPoolId();
      }
      return false;

    }catch(Exception e){
      log.error(e.getMessage(), e);
      return false;
    }
  }


  /**
   * Move a subpool to a pool.
   */
  public void movePool(String agentId, Long sourceId, Long destId)
  {
    try
    {
      if (!isDescendantOf(destId, sourceId, agentId)) {
        if (!sourceId.equals(destId)) {

        PersistenceService.getInstance().getQuestionPoolFacadeQueries().
          movePool(agentId, sourceId, destId);
        }
        else {
          log.warn("Illegal Move: Can not move a pool to itself." );
        }
      }
      else {
        log.warn("Illegal Move: Can not move a pool to its descendant." );
      }
    }
    catch(Exception e)
    {
      log.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }

  }

  /**
   * Delete a pool
   */
  public void deletePool(Long poolId, String agentId, Tree tree)
  {
    try
    {
      QuestionPoolFacade qp = PersistenceService.getInstance().
        getQuestionPoolFacadeQueries().getPool(poolId, agentId);

      // you are not allowed to delete pool if you are not the owner
      if (!qp.getOwnerId().equals(agentId))
        throw new Exception("You are not allowed to delete pool if you are not the owner");
      PersistenceService.getInstance().getQuestionPoolFacadeQueries().
        deletePool(poolId, agentId, tree);
    }
    catch(Exception e)
    {
      log.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  /**
   * removes a Question from the question pool. This does not  *delete* the question itself
   */
  public void removeQuestionFromPool(Long questionId, Long poolId)
  {
    try
    {
      PersistenceService.getInstance().getQuestionPoolFacadeQueries().
        removeItemFromPool(questionId, poolId);
    }
    catch(Exception e)
    {
      log.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Copy a subpool to a pool.
   */
  public void copyPool(Tree tree, String agentId, Long sourceId,
    Long destId, String prependString1, String prependString2)
  {
    try
    {
      if (!isDescendantOf(destId, sourceId, agentId)) {
        PersistenceService.getInstance().getQuestionPoolFacadeQueries().copyPool
        (tree, agentId, sourceId, destId, prependString1, prependString2);
      }
      else {
        log.warn("Illegal Copy: Can not copy a pool to its descendant!" );
      }

    }
    catch(Exception e)
    {
      log.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Copy a question to a pool
   */
  public void copyQuestion(
    osid.shared.Id questionId, osid.shared.Id destId )
  {
    try
    {
//TODO must call the Service.
	//questionPoolService.copyQuestion(questionId, destId );
    }
    catch(Exception e)
    {
      log.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Copy a question to a pool
   */
  public void copyQuestion(
    osid.shared.Id questionId, osid.shared.Id destId, boolean duplicateCopy)
  {
    try
    {
//TODO must call the Service.
	//questionPoolService.copyQuestion(questionId, destId ,duplicateCopy);
    }
    catch(Exception e)
    {
      log.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }


  /*
   * Exports a Question as an Xml file
   */
  public String exportQuestion(osid.shared.Id questionId)
  {
    try{
	return "";//questionPoolService.exportQuestion(questionId);
    }catch(Exception e){
      log.error("Exception in exportQuestion", e);
      return null;
    }
  }

  /**
   * Save a question pool.
   */
  public QuestionPoolFacade savePool(QuestionPoolFacade pool)
  {
    try
    {
      return PersistenceService.getInstance().getQuestionPoolFacadeQueries().savePool(pool);
    }
    catch(Exception e)
    {
      log.error(e.getMessage(), e);
      return pool;
    }
  }

  public Map getQuestionPoolItemMap(){
    return PersistenceService.getInstance().getQuestionPoolFacadeQueries().
        getQuestionPoolItemMap();
  }


 public boolean poolIsUnique(String questionPoolId, String title, String parentPoolId, String agentId) {
    return PersistenceService.getInstance().getQuestionPoolFacadeQueries().poolIsUnique(new Long(questionPoolId), title, new Long(parentPoolId), agentId);
  }


 public Long copyItemFacade(ItemDataIfc itemData){
   return PersistenceService.getInstance().getQuestionPoolFacadeQueries().copyItemFacade(itemData);
 }
 
 public ItemFacade copyItemFacade2(ItemDataIfc itemData){
	   return PersistenceService.getInstance().getQuestionPoolFacadeQueries().copyItemFacade2(itemData);
	 }
 
 /**
  * Get the count of items for one poolId from the back end.
  */
  public int getCountItems(Long poolId)
  {
     Integer result = Integer.valueOf(0);
     try {
       result =
         PersistenceService.getInstance().
            getQuestionPoolFacadeQueries().getCountItemFacades(poolId);
     } catch (Exception e) {
    	 log.error("getCountItems", e);
     }
     return result.intValue();
  }
  
  /**
   * Get the count of items for all pools for one user
   */
   public Map<Long, Integer> getCountItemsForUser(String agentId)
   {
      Map<Long, Integer> result = new HashMap<Long, Integer>();
      try {
        result = PersistenceService.getInstance().getQuestionPoolFacadeQueries().getCountItemFacadesForUser(agentId);
      } catch (Exception e) {
        log.error("getCountItemsForUser", e);
      }
      return result;
   }

  /**
   * Shared Pools with other user
   */
  public void addQuestionPoolAccess(Tree tree, String user, Long questionPoolId, Long accessTypeId) {
	  try {
		  PersistenceService.getInstance().
		  getQuestionPoolFacadeQueries().addQuestionPoolAccess(tree, user, questionPoolId, accessTypeId);
	  } catch (Exception e) {
        log.error(e.getMessage(), e);
	  }
  }

  public void removeQuestionPoolAccess(Tree tree, String user, Long questionPoolId, Long accessTypeId) {
	  try {
		  PersistenceService.getInstance().
		  getQuestionPoolFacadeQueries().removeQuestionPoolAccess(tree, user, questionPoolId, accessTypeId);
	  } catch (Exception e) {
        log.error(e.getMessage(), e);
	  }
  }

  public List<AgentFacade> getAgentsWithAccess(Long questionPoolId) {

	  List<AgentFacade> agents = null;
	  try {
		  agents = PersistenceService.getInstance().
		  getQuestionPoolFacadeQueries().getAgentsWithAccess(questionPoolId);
	  } catch (Exception e) {
        log.error(e.getMessage(), e);
	  }

	  return agents;
  }

  public List<AgentFacade> getAgentsWithoutAccess(Long questionPoolId, String realmId) {

	  List<AgentFacade> agents = new ArrayList<AgentFacade>();

	  try {
		  // Get agents with access
		  List<AgentFacade> agentsWithAccess = getAgentsWithAccess(questionPoolId);

		  List<String> azGroups = new ArrayList<String>();
		  azGroups.add("/site/" + realmId);

		  // Get all agents
		  Set<String> users = ComponentManager.get(AuthzGroupService.class).getUsersIsAllowed("assessment.questionpool.create", azGroups);

		  // Create the AgentFacade
		  for (String userId : users) {
			  AgentFacade agent = new AgentFacade(userId);
			  agents.add(agent);
		  }

		  agents.removeAll(agentsWithAccess);

	  } catch (Exception e) {
        log.error(e.getMessage(), e);
	  }

	  return agents;
  }
  
  // SAM-2049
  public void transferPoolsOwnership(String ownerId, List<Long> poolIds) {
	  try {
		  PersistenceService.getInstance().getQuestionPoolFacadeQueries().transferPoolsOwnership(ownerId, poolIds);
	  } catch (Exception ex) {
          log.error(ex.getMessage(), ex);
		  throw new RuntimeException(ex);
	  }
  }
}
