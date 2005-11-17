/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/
package org.sakaiproject.tool.assessment.facade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

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

  public ArrayList getBasicInfoOfAllPools(String agentId);
 public ArrayList getIdAllPools(String agentId);

  public List getAllItems(Long questionPoolId);

  public List getAllItemFacadesOrderByItemText(Long questionPoolId,
      String orderBy);

  public List getAllItemFacadesOrderByItemType(Long questionPoolId,
      String orderBy);

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
  public void removeItemFromPool(String itemId, Long poolId);

  /**
   * DOCUMENTATION PENDING
   *
   * @param itemId DOCUMENTATION PENDING
   * @param poolId DOCUMENTATION PENDING
   */
  public void moveItemToPool(String itemId, Long sourceId, Long destId);

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

  public int getSubPoolSize(Long poolId);

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

  public List getPoolIdsByAgent(String agentId);

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
  public void copyPool(Tree tree, String agentId, Long sourceId, Long destId);

  public Long add();

  public QuestionPoolFacade getPoolById(Long questionPoolId);

  public HashMap getQuestionPoolItemMap();

}