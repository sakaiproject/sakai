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




package org.sakaiproject.tool.assessment.shared.api.questionpool;

import java.util.List;
import java.util.Map;

import org.sakaiproject.tool.assessment.data.ifc.questionpool.QuestionPoolDataIfc;
import org.sakaiproject.tool.assessment.data.model.Tree;


/**
 * The QuestionPoolServiceAPI declares a shared interface to control question
 * pool information.
 * @author Ed Smiley <esmiley@stanford.edu>
 */
public interface QuestionPoolServiceAPI
{
  /**
   * Get all pools from the back end.
   */
  public List getAllPools(String agentId);

  /**
   * Get basic info for pools(just id and  title)  for displaying in pulldown .
   */
  public List getBasicInfoOfAllPools(String agentId);


  /**
   * Get a particular pool from the backend, with all questions.
   */
  public QuestionPoolDataIfc getPool(Long poolId, String agentId);

  /**
   * Get a list of pools that have a specific Agent
   */
  public List getPoolIdsByItem(String itemId);

  public boolean hasItem(String itemId, Long poolId);

  /**
   * Get pool id's by agent.
   */
  public List getPoolIdsByAgent(String agentId);

  /**
   * Get a list of pools that have a specific parent
   */
  public List getSubPools(Long poolId);

  /**
   * Get the size of a subpool.
   */
  public long getSubPoolSize(Long poolId);

  /**
   * Checks to see if a pool has subpools
   */
  public boolean hasSubPools(Long poolId);

  /**
   * Get all scores for a published assessment from the back end.
   */
  public List getAllItems(Long poolId);

  /**
   * Save a question to a pool.
   */
  public void addItemToPool(Long itemId, Long poolId);

  /**
   * Move a question to a pool.
   */
  public void moveItemToPool(Long itemId, Long sourceId, Long destId);

  /**
   * Is a pool a descendant of the other?
   */
  public boolean isDescendantOf(Long poolA,Long poolB, String agentId);

  /**
   * Move a subpool to a pool.
   */
  public void movePool(String agentId, Long sourceId, Long destId);

  /**
   * Delete a pool
   */
  public void deletePool(Long poolId, String agentId, Tree tree);

  /**
   * removes a Question from the question pool. This does not  *delete* the question itself
   */
  public void removeQuestionFromPool(Long questionId, Long poolId);

  /**
   * Copy a subpool to a pool.
   */
  public void copyPool(Tree tree, String agentId, Long sourceId,
    Long destId, String prependString1, String prependString2);


  /**
   * Save a question pool.
   */
  public QuestionPoolDataIfc savePool(QuestionPoolDataIfc pool);

  public Map getQuestionPoolItemMap();
  
  public String getUserPoolAttachmentReport(String userId, Long poolId, String contextToReplace);

}
