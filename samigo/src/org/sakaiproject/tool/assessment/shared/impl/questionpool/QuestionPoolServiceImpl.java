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
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osid.shared.SharedException;

import org.sakaiproject.tool.assessment.data.model.Tree;
import org.sakaiproject.tool.assessment.data.ifc.questionpool.QuestionPoolDataIfc;
import org.sakaiproject.tool.assessment.facade.QuestionPoolIteratorFacade;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.assessment.shared.api.questionpool.QuestionPoolServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.questionpool.QuestionPoolServiceException;
import org.osid.agent.*;
import org.sakaiproject.tool.assessment.facade.QuestionPoolFacade;

/**
 *
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
    try
    {
      QuestionPoolService service = new QuestionPoolService();
      return service.getBasicInfoOfAllPools(agentId);
    }
    catch (Exception ex)
    {
      throw new QuestionPoolServiceException(ex);
    }
  }

  /**
   * Get a particular pool from the backend, with all questions.
   */
  public QuestionPoolDataIfc getPool(Long poolId, String agentId)
  {
    try
    {
      QuestionPoolService service = new QuestionPoolService();
      return service.getPool(poolId, agentId);
    }
    catch (Exception ex)
    {
      throw new QuestionPoolServiceException(ex);
    }
  }

  /**
   * Get a list of pools that have a specific Agent
   */
  public List getPoolIdsByItem(String itemId)
  {
    try
    {
      QuestionPoolService service = new QuestionPoolService();
      return service.getPoolIdsByItem(itemId);
    }
    catch (Exception ex)
    {
      throw new QuestionPoolServiceException(ex);
    }
  }

  public boolean hasItem(String itemId, Long poolId)
  {
    try
    {
      QuestionPoolService service = new QuestionPoolService();
      return service.hasItem(itemId, poolId);
    }
    catch (Exception ex)
    {
      throw new QuestionPoolServiceException(ex);
    }
  }

  /**
   * Get pool id's by agent.
   */
  public List getPoolIdsByAgent(String agentId)
  {
    try
    {
      QuestionPoolService service = new QuestionPoolService();
      return service.getPoolIdsByAgent(agentId);
    }
    catch (Exception ex)
    {
      throw new QuestionPoolServiceException(ex);
    }
  }

  /**
   * Get a list of pools that have a specific parent
   */
  public List getSubPools(Long poolId)
  {
    try
    {
      QuestionPoolService service = new QuestionPoolService();
      return service.getSubPools(poolId);
    }
    catch (Exception ex)
    {
      throw new QuestionPoolServiceException(ex);
    }
  }

  /**
   * Get the size of a subpool.
   */
  public int getSubPoolSize(Long poolId)
  {
    try
    {
      QuestionPoolService service = new QuestionPoolService();
      return service.getSubPoolSize(poolId);
    }
    catch (Exception ex)
    {
      throw new QuestionPoolServiceException(ex);
    }
  }

  /**
   * Checks to see if a pool has subpools
   */
  public boolean hasSubPools(Long poolId)
  {
    try
    {
      QuestionPoolService service = new QuestionPoolService();
      return service.hasSubPools(poolId);
    }
    catch (Exception ex)
    {
      throw new QuestionPoolServiceException(ex);
    }
  }

  /**
   * Get all items sorted by orderby
   */
  public List getAllItemsSorted(Long poolId, String orderBy)
  {
    try
    {
      QuestionPoolService service = new QuestionPoolService();
      return service.getAllItemsSorted(poolId, orderBy);
    }
    catch (Exception ex)
    {
      throw new QuestionPoolServiceException(ex);
    }
  }

  /**
   * Get all scores for a published assessment from the back end.
   */
  public List getAllItems(Long poolId)
  {
    try
    {
      QuestionPoolService service = new QuestionPoolService();
      return service.getAllItems(poolId);
    }
    catch (Exception ex)
    {
      throw new QuestionPoolServiceException(ex);
    }
  }

  /**
   * Save a question to a pool.
   */
  public void addItemToPool(String itemId, Long poolId)
  {
    try
    {
      QuestionPoolService service = new QuestionPoolService();
      service.addItemToPool(itemId, poolId);
    }
    catch (Exception ex)
    {
      throw new QuestionPoolServiceException(ex);
    }
  }

  /**
   * Move a question to a pool.
   */
  public void moveItemToPool(String itemId, Long sourceId, Long destId)
  {
    try
    {
      QuestionPoolService service = new QuestionPoolService();
      service.moveItemToPool(itemId, sourceId, destId);
    }
    catch (Exception ex)
    {
      throw new QuestionPoolServiceException(ex);
    }
  }

  /**
   * Is a pool a descendant of the other?
   */
  public boolean isDescendantOf(Long poolA, Long poolB, String agentId)
  {
    try
    {
      QuestionPoolService service = new QuestionPoolService();
      return service.isDescendantOf(poolA, poolB, agentId);
    }
    catch (Exception ex)
    {
      throw new QuestionPoolServiceException(ex);
    }
  }

  /**
   * Move a subpool to a pool.
   */
  public void movePool(String agentId, Long sourceId, Long destId)
  {
    try
    {
      QuestionPoolService service = new QuestionPoolService();
      service.movePool(agentId, sourceId, destId);
    }
    catch (Exception ex)
    {
      throw new QuestionPoolServiceException(ex);
    }
  }

  /**
   * Delete a pool
   */
  public void deletePool(Long poolId, String agentId, Tree tree)
  {
    try
    {
      QuestionPoolService service = new QuestionPoolService();
      service.deletePool(poolId, agentId, tree);
    }
    catch (Exception ex)
    {
      throw new QuestionPoolServiceException(ex);
    }
  }

  /**
   * removes a Question from the question pool. This does not  *delete* the question itself
   */
  public void removeQuestionFromPool(String questionId, Long poolId)
  {
    try
    {
      QuestionPoolService service = new QuestionPoolService();
      service.removeQuestionFromPool(questionId, poolId);
    }
    catch (Exception ex)
    {
      throw new QuestionPoolServiceException(ex);
    }
  }

  /**
   * Copy a subpool to a pool.
   */
  public void copyPool(Tree tree, String agentId, Long sourceId,
                       Long destId)
  {
    try
    {
      QuestionPoolService service = new QuestionPoolService();
      service.copyPool(tree, agentId, sourceId, destId);
    }
    catch (Exception ex)
    {
      throw new QuestionPoolServiceException(ex);
    }
  }

  /**
   * Save a question pool.
   */
  public QuestionPoolDataIfc savePool(QuestionPoolDataIfc pool)
  {
  try
  {
    QuestionPoolService service = new QuestionPoolService();
    Long poolId = pool.getQuestionPoolId();
    String agentId = null;
    try
    {
      agentId = pool.getOwner().getId().toString();
    }
    catch (AgentException ax)
    {
      throw new QuestionPoolServiceException(ax);
    }
    QuestionPoolFacade facade = service.getPool(poolId, agentId);

    return service.savePool(facade);
  }
  catch (Exception ex)
  {
    throw new QuestionPoolServiceException(ex);
  }
  }

  public Map getQuestionPoolItemMap()
  {
    try
    {
      QuestionPoolService service = new QuestionPoolService();
      return service.getQuestionPoolItemMap();
    }
    catch (Exception ex)
    {
      throw new QuestionPoolServiceException(ex);
    }
  }

}
