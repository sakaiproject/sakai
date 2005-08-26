/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/trunk/sakai/sam/src/org/sakaiproject/tool/assessment/services/QuestionPoolServiceAPI.java $
 * $Id: QuestionPoolServiceAPI.java 1285 2005-08-19 02:05:48Z esmiley@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.assessment.shared.impl.questionpool;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osid.shared.SharedException;

import org.sakaiproject.tool.assessment.business.AAMTree;
import org.sakaiproject.tool.assessment.data.ifc.questionpool.QuestionPoolDataIfc;
import org.sakaiproject.tool.assessment.shared.api.questionpool.QuestionPoolServiceAPI;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.assessment.facade.QuestionPoolIteratorFacade;
import org.sakaiproject.tool.assessment.shared.api.questionpool.QuestionPoolServiceException;

/**
 * @todo implement some of the methods
 * The QuestionPoolServiceAPI declares a shared interface to control question
 * pool information.
 * @author Ed Smiley <esmiley@stanford.edu>
 */
public class QuestionPoolServiceImpl
  implements QuestionPoolServiceAPI
{
  private static Log log = LogFactory.getLog(QuestionPoolServiceImpl.class);

  /**
   * Creates a new QuestionPoolServiceImpl object.
   */
  public QuestionPoolServiceImpl()
  {
  }

  /**
   * Get all pools from the back end.
   */
  public List getAllPools(String agentId)
  {
    List list = new ArrayList();
    try
    {
      QuestionPoolService service = new QuestionPoolService();
      QuestionPoolIteratorFacade iter = service.getAllPools(agentId);
      while (iter.hasNext())
      {
        QuestionPoolDataIfc pool = (QuestionPoolDataIfc) iter.next();
        list.add(pool);
      }
      return list;
    }
    catch (SharedException ex)
    {
      throw new QuestionPoolServiceException(ex);
    }
    catch (Exception ex)
    {
      throw new QuestionPoolServiceException(ex);
    }
  }

  /**
   * Get basic info for pools(just id and  title)  for displaying in pulldown .
   */
  public List getBasicInfoOfAllPools(String agentId)
  {
    /**@todo Implement this
     * org.sakaiproject.tool.assessment.shared.api.questionpool.QuestionPoolServiceAPI method
     * */
    throw new java.lang.UnsupportedOperationException(
      "Method getBasicInfoOfAllPools() not yet implemented.");
  }

  /**
   * Get a particular pool from the backend, with all questions.
   */
  public QuestionPoolDataIfc getPool(Long poolId, String agentId)
  {
    /**@todo Implement this
     * org.sakaiproject.tool.assessment.shared.api.questionpool.QuestionPoolServiceAPI method
     * */
    throw new java.lang.UnsupportedOperationException(
      "Method getPool() not yet implemented.");
  }

  /**
   * Get a list of pools that have a specific Agent
   */
  public List getPoolIdsByItem(String itemId)
  {
    /**@todo Implement this
     * org.sakaiproject.tool.assessment.shared.api.questionpool.QuestionPoolServiceAPI method
     * */
    throw new java.lang.UnsupportedOperationException(
      "Method getPoolIdsByItem  () not yet implemented.");
  }

  public boolean hasItem(String itemId, Long poolId)
  {
    /**@todo Implement this
     * org.sakaiproject.tool.assessment.shared.api.questionpool.QuestionPoolServiceAPI method
     * */
    throw new java.lang.UnsupportedOperationException(
      "Method hasItem() not yet implemented.");
  }

  /**
   * Get pool id's by agent.
   */
  public List getPoolIdsByAgent(String agentId)
  {
    /**@todo Implement this
     * org.sakaiproject.tool.assessment.shared.api.questionpool.QuestionPoolServiceAPI method
     * */
    throw new java.lang.UnsupportedOperationException(
      "Method getPoolIdsByAgent() not yet implemented.");
  }

  /**
   * Get a list of pools that have a specific parent
   */
  public List getSubPools(Long poolId)
  {
    /**@todo Implement this
     * org.sakaiproject.tool.assessment.shared.api.questionpool.QuestionPoolServiceAPI method
     * */
    throw new java.lang.UnsupportedOperationException(
      "Method getSubPools() not yet implemented.");
  }

  /**
   * Get the size of a subpool.
   */
  public int getSubPoolSize(Long poolId)
  {
    /**@todo Implement this
     * org.sakaiproject.tool.assessment.shared.api.questionpool.QuestionPoolServiceAPI method
     * */
    throw new java.lang.UnsupportedOperationException(
      "Method getSubPoolSize() not yet implemented.");
  }

  /**
   * Checks to see if a pool has subpools
   */
  public boolean hasSubPools(Long poolId)
  {
    /**@todo Implement this
     * org.sakaiproject.tool.assessment.shared.api.questionpool.QuestionPoolServiceAPI method
     * */
    throw new java.lang.UnsupportedOperationException(
      "Method hasSubPools() not yet implemented.");
  }

  /**
   * Get all items sorted by orderby
   */
  public List getAllItemsSorted(Long poolId, String orderBy)
  {
    /**@todo Implement this
     * org.sakaiproject.tool.assessment.shared.api.questionpool.QuestionPoolServiceAPI method
     * */
    throw new java.lang.UnsupportedOperationException(
      "Method getAllItemsSorted() not yet implemented.");
  }

  /**
   * Get all scores for a published assessment from the back end.
   */
  public List getAllItems(Long poolId)
  {
    /**@todo Implement this
     * org.sakaiproject.tool.assessment.shared.api.questionpool.QuestionPoolServiceAPI method
     * */
    throw new java.lang.UnsupportedOperationException(
      "Method getAllItems() not yet implemented.");
  }

  /**
   * Save a question to a pool.
   */
  public void addItemToPool(String itemId, Long poolId)
  {
    /**@todo Implement this
     * org.sakaiproject.tool.assessment.shared.api.questionpool.QuestionPoolServiceAPI method
     * */
    throw new java.lang.UnsupportedOperationException(
      "Method addItemToPool() not yet implemented.");
  }

  /**
   * Move a question to a pool.
   */
  public void moveItemToPool(String itemId, Long sourceId, Long destId)
  {
    /**@todo Implement this
     * org.sakaiproject.tool.assessment.shared.api.questionpool.QuestionPoolServiceAPI method
     * */
    throw new java.lang.UnsupportedOperationException(
      "Method moveItemToPool() not yet implemented.");
  }

  /**
   * Is a pool a descendant of the other?
   */
  public boolean isDescendantOf(Long poolA, Long poolB, String agentId)
  {
    /**@todo Implement this
     * org.sakaiproject.tool.assessment.shared.api.questionpool.QuestionPoolServiceAPI method
     * */
    throw new java.lang.UnsupportedOperationException(
      "Method isDescendantOf() not yet implemented.");
  }

  /**
   * Move a subpool to a pool.
   */
  public void movePool(String agentId, Long sourceId, Long destId)
  {
    /**@todo Implement this
     * org.sakaiproject.tool.assessment.shared.api.questionpool.QuestionPoolServiceAPI method
     * */
    throw new java.lang.UnsupportedOperationException(
      "Method movePool() not yet implemented.");
  }

  /**
   * Delete a pool
   */
  public void deletePool(Long poolId, String agentId, AAMTree tree)
  {
    /**@todo Implement this
     * org.sakaiproject.tool.assessment.shared.api.questionpool.QuestionPoolServiceAPI method
     * */
    throw new java.lang.UnsupportedOperationException(
      "Method deletePool() not yet implemented.");
  }

  /**
   * removes a Question from the question pool. This does not  *delete* the question itself
   */
  public void removeQuestionFromPool(String questionId, Long poolId)
  {
    /**@todo Implement this
     * org.sakaiproject.tool.assessment.shared.api.questionpool.QuestionPoolServiceAPI method
     * */
    throw new java.lang.UnsupportedOperationException(
      "Method removeQuestionFromPool() not yet implemented.");
  }

  /**
   * Copy a subpool to a pool.
   */
  public void copyPool(AAMTree tree, String agentId, Long sourceId,
                       Long destId)
  {
    /**@todo Implement this
     * org.sakaiproject.tool.assessment.shared.api.questionpool.QuestionPoolServiceAPI method
     * */
    throw new java.lang.UnsupportedOperationException(
      "Method copyPool() not yet implemented.");
  }

  /**
   * Save a question pool.
   */
  public QuestionPoolDataIfc savePool(QuestionPoolDataIfc pool)
  {
    /**@todo Implement this
     * org.sakaiproject.tool.assessment.shared.api.questionpool.QuestionPoolServiceAPI method
     * */
    throw new java.lang.UnsupportedOperationException(
      "Method savePool() not yet implemented.");
  }

  public Map getQuestionPoolItemMap()
  {
    /**@todo Implement this
     * org.sakaiproject.tool.assessment.shared.api.questionpool.QuestionPoolServiceAPI method
     * */
    throw new java.lang.UnsupportedOperationException(
      "Method getQuestionPoolItemMap() not yet implemented.");
  }

}
