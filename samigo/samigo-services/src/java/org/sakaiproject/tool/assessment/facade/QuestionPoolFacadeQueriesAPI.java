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


package org.sakaiproject.tool.assessment.facade;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.model.Tree;
import org.sakaiproject.tool.assessment.data.dao.questionpool.QuestionPoolAccessData;
import org.sakaiproject.tool.assessment.data.dao.questionpool.QuestionPoolData;
import org.sakaiproject.tool.assessment.data.dao.questionpool.QuestionPoolItemData;
import org.sakaiproject.tool.assessment.osid.shared.impl.IdImpl;

public interface QuestionPoolFacadeQueriesAPI
{

  public IdImpl getQuestionPoolId(String id);

  public IdImpl getQuestionPoolId(Long id);

  public IdImpl getQuestionPoolId(long id);

  /**
   * Get a list of all the pools in the site. Note that questions in each pool will not
   * be populated. We must keep this list updated.
   */
  public List getAllPools();

  /**
   * Get all the pools that the agent has access to. The easiest way seems to be
   * #1. get all the existing pool
   * #2. get all the QuestionPoolAccessData record of the agent
   * #3. go through the existing pools and check it against the QuestionPoolAccessData (qpa) record to see if
   * the agent is granted access to it. qpa record (if exists) always trumps the default access right set
   * up for a pool
   * e.g. if the defaultAccessType for a pool is ACCESS_DENIED but the qpa record say ADMIN, then access=ADMIN
   * e.g. if the defaultAccessType for a pool is ADMIN but the qpa record say ACCESS_DENIED, then access=ACCESS_DENIED
   * e.g. if no qpa record exists, then access rule will follow the defaultAccessType set by the pool
   */
  public QuestionPoolIteratorFacade getAllPools(String agentId);

  public QuestionPoolIteratorFacade getAllPoolsWithAccess(String agentId);
  
  public List<QuestionPoolFacade> getBasicInfoOfAllPools(String agentId);
 
  public boolean poolIsUnique(Long questionPoolId, String title, Long parentPoolId, String agentId);

  public List getAllItems(Long questionPoolId);

  public List getAllItemsIds(final Long questionPoolId);

  public List getAllItemFacadesOrderByItemText(Long questionPoolId,
					       String orderBy, String ascending);

  public List getAllItemFacadesOrderByItemType(Long questionPoolId,
					       String orderBy, String ascending);

  public List getAllItemFacades(Long questionPoolId);

  /**
   * This method returns an ItemFacade that we can use to construct our ItemImpl
   */
  public ItemFacade getItem(String id);

  /**
   * Get a pool based on poolId. I am not sure why agent is not used though is being parsed.
   *
   * @param poolid DOCUMENTATION PENDING
   * @param agent DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public QuestionPoolFacade getPool(Long poolId, String agent);

  public void setPoolAccessType(QuestionPoolData qpp, String agentId);

  public QuestionPoolAccessData getQuestionPoolAccessData(Long poolId,
      String agentId);

  /**
   * DOCUMENTATION PENDING
   *
   * @param ids DOCUMENTATION PENDING
   * @param sectionId DOCUMENTATION PENDING
   */
  public void addItemsToSection(Collection ids, long sectionId);

  /**
   * add a question to a pool
   *
   * @param itemId DOCUMENTATION PENDING
   * @param poolId DOCUMENTATION PENDING
   */
  public void addItemToPool(QuestionPoolItemData qpi);

  /**
   * Delete pool and questions attached to it plus any subpool under it
   *
   * @param itemId DOCUMENTATION PENDING
   * @param poolId DOCUMENTATION PENDING
   */
  public void deletePool(Long poolId, String agent, Tree tree);

  /**
   * Move pool under another pool. The dest pool must not be the
   * descendant of the source nor can they be the same pool .
   */
  public void movePool(String agentId, Long sourcePoolId, Long destPoolId);

  /**
   * Is destination a descendant of the source?
   */
  public boolean isDescendantOf(QuestionPoolFacade destPool,
      QuestionPoolFacade sourcePool);

  /**
   * DOCUMENTATION PENDING
   *
   * @param itemId DOCUMENTATION PENDING
   * @param poolId DOCUMENTATION PENDING
   */
  public void removeItemFromPool(Long itemId, Long poolId);

  /**
   * DOCUMENTATION PENDING
   *
   * @param itemId DOCUMENTATION PENDING
   * @param poolId DOCUMENTATION PENDING
   */
  public void moveItemToPool(Long itemId, Long sourceId, Long destId);

  /**
   * DOCUMENTATION PENDING
   *
   * @param pool DOCUMENTATION PENDING
   */
  public QuestionPoolFacade savePool(QuestionPoolFacade pool);

  /**
   * Get all the children pools of a pool. Return a list of QuestionPoolData
   * should return QuestionPool instead - need fixing, daisyf
   *
   * @param itemId DOCUMENTATION PENDING
   * @param poolId DOCUMENTATION PENDING
   */

  public List getSubPools(Long poolId);

  public long getSubPoolSize(Long poolId);

  /**
   * get number of subpools for each pool in a single query.
   * returns a List of Long arrays. Each array is 0: poolid, 1: count of subpools
   *
   * @param agent
   * @return List<Long[]>
   */
  public List<Long[]> getSubPoolSizes(String agent);

  /**
   * DOCUMENTATION PENDING
   *
   * @param itemId DOCUMENTATION PENDING
   * @param poolId DOCUMENTATION PENDING
   */

  public boolean hasSubPools(Long poolId);

  /**
   * Return a list of questionPoolId (java.lang.Long)
   *
   * @param itemId DOCUMENTATION PENDING
   * @param poolId DOCUMENTATION PENDING
   */

  public List<Long> getPoolIdsByAgent(String agentId);

  /**
   * Return a list of questionPoolId (java.lang.Long)
   *
   * @param itemId DOCUMENTATION PENDING
   * @param poolId DOCUMENTATION PENDING
   */

  public List getPoolIdsByItem(String itemId);

  /**
   * Copy a pool to a new location.
   */
  public void copyPool(Tree tree, String agentId, Long sourceId, Long destId, String prependString1, String prependString2);

  public Long add();

  public QuestionPoolFacade getPoolById(Long questionPoolId);

  public Map getQuestionPoolItemMap();

  public Long copyItemFacade(ItemDataIfc itemData);
  
  public ItemFacade copyItemFacade2(ItemDataIfc itemData);

  public Integer getCountItemFacades(Long questionPoolId);
  
  public Map<Long, Integer> getCountItemFacadesForUser(String agentId);
  
  /**
   * Shared Pools with other user
   */
  public void addQuestionPoolAccess(Tree tree, String user, Long questionPoolId, Long accessTypeId);
     
  public void removeQuestionPoolAccess(Tree tree, String user, Long questionPoolId, Long accessTypeId);   

  public List<AgentFacade> getAgentsWithAccess(final Long questionPoolId);
  
  //SAM-2049
  public void transferPoolsOwnership(String ownerId, List<Long> poolIds);

}
