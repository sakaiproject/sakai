/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
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


package org.sakaiproject.tool.assessment.shared.impl.questionpool;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.osid.shared.SharedException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.tool.assessment.data.dao.questionpool.QuestionPoolData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.questionpool.QuestionPoolDataIfc;
import org.sakaiproject.tool.assessment.data.model.Tree;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.facade.QuestionPoolFacade;
import org.sakaiproject.tool.assessment.facade.QuestionPoolIteratorFacade;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.assessment.services.QuestionPoolServiceException;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.shared.api.questionpool.QuestionPoolServiceAPI;
import org.sakaiproject.tool.cover.SessionManager;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * The QuestionPoolServiceAPI declares a shared interface to control question
 * pool information.
 * @author Ed Smiley <esmiley@stanford.edu>
 */
@Slf4j
public class QuestionPoolServiceImpl
  implements QuestionPoolServiceAPI
{

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
      QuestionPoolIteratorFacade iter = service.getOwnPools(agentId);
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
   * Get a list of pools that have a specific item
   */
  public List getPoolIdsByItem(Long itemId)
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

  public boolean hasItem(Long itemId, Long poolId)
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
  public long getSubPoolSize(Long poolId)
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
  public void addItemToPool(Long itemId, Long poolId)
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
  public void moveItemToPool(Long itemId, Long sourceId, Long destId)
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
  public void removeQuestionFromPool(Long questionId, Long poolId)
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
                       Long destId, String prependString1, String prependString2)
  {
    try
    {
      QuestionPoolService service = new QuestionPoolService();
      service.copyPool(tree, agentId, sourceId, destId, prependString1, prependString2);
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
      agentId = pool.getOwner().getIdString();
    }
    catch (Exception ax)
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

  private static final class UserPoolAttachmentReport
  {
    private final StringBuilder report = new StringBuilder();

    private void inspectAttachmentsInText(String text)
    {
      if (StringUtils.isEmpty(text)) {
        return;
      }

      Set<String> attachments = new HashSet<>();
      for (String source : StringUtils.splitByWholeSeparator(text, "src=\"")) {
        String href = StringUtils.substringBefore(source, "\"");
        if (StringUtils.contains(href, "/access/content/")) {
          attachments.add(href);
        }
      }
      if (!attachments.isEmpty()) {
        report.append("\nFound ").append(attachments.size())
            .append(" attachments buried in question or answer text.\n\n");
        attachments.forEach(this::inspectAttachment);
      }
    }

    private void inspectAttachment(String attachment)
    {
      String resourceId = "/" + StringUtils.substringAfter(attachment, "/access/content/");
      resourceId = URLDecoder.decode(resourceId, StandardCharsets.UTF_8);

      try {
        AssessmentService.getContentHostingService().getResource(resourceId);
      } catch (IdUnusedException e) {
        report.append("\nCould not find attachment (").append(resourceId).append(").\n\n");
      } catch (TypeException e) {
        report.append("\nTypeException for resource (").append(resourceId)
            .append(") that was embedded in a question or answer.\n\n");
      } catch (PermissionException e) {
        report.append("\nNo permission for attachment (").append(resourceId).append(").\n\n");
      }
    }
  }

  @Override
  public String getUserPoolAttachmentReport(String userId, Long poolId)
  {
    if (StringUtils.isBlank(userId) || !userId.equals(SessionManager.getCurrentSessionUserId()) || poolId == null) {
      return "";
    }

    QuestionPoolData pool = PersistenceService.getInstance().getQuestionPoolFacadeQueries()
        .getOwnedPoolSummary(poolId, userId).orElse(null);
    if (pool == null) {
      return "";
    }

    UserPoolAttachmentReport attachmentReport = new UserPoolAttachmentReport();
    StringBuilder report = attachmentReport.report;
    report.append("POOL ---> ").append(pool.getTitle()).append(" - POOL ID: ")
        .append(pool.getQuestionPoolId()).append("\n\n");

    for (Object item : getAllItems(poolId)) {
      ItemFacade question = (ItemFacade) item;
      for (ItemTextIfc text : question.getItemTextSet()) {
        report.append("Question Text ---> ").append(text.getText()).append('\n');
        attachmentReport.inspectAttachmentsInText(text.getText());
        for (AnswerIfc answer : text.getAnswerSet()) {
          attachmentReport.inspectAttachmentsInText(answer.getText());
        }
      }

      List<ItemAttachmentIfc> attachments = question.getItemAttachmentList();
      if (!attachments.isEmpty()) {
        report.append("\nFound ").append(attachments.size()).append(" uploaded attachments in the question.\n\n");
        attachments.forEach(attachment -> attachmentReport.inspectAttachment(attachment.getLocation()));
      }
    }

    return report.toString();
  }
}
