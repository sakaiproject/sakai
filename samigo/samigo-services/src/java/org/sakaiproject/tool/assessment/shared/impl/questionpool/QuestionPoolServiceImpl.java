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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.osid.shared.SharedException;

import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityAdvisor.SecurityAdvice;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.questionpool.QuestionPoolDataIfc;
import org.sakaiproject.tool.assessment.data.dao.assessment.Answer;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemText;
import org.sakaiproject.tool.assessment.data.model.Tree;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.facade.QuestionPoolFacade;
import org.sakaiproject.tool.assessment.facade.QuestionPoolIteratorFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.assessment.services.QuestionPoolServiceException;
import org.sakaiproject.tool.assessment.shared.api.questionpool.QuestionPoolServiceAPI;

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

  final class UserPoolAttachmentReport
  {
	  private final StringBuilder report;

	  public UserPoolAttachmentReport()
	  {
		  this.report = new StringBuilder();
	  }

	  public String getReport() {
		  return report.toString();
	  }
	  
	  private void addToReport(String info)
	  {
		  this.report.append(info);
	  }

	  public String findAttachmentsInText(String text, String contextToReplace)
	  {
		  String replacedAttachment;
		  
		  if(text != null)
		  {
			  String[] sources = StringUtils.splitByWholeSeparator(text, "src=\"");

			  Set<String> attachments = new HashSet<String>();
			  for (String source : sources)
			  {
				  String theHref = StringUtils.substringBefore(source, "\"");
				  if (StringUtils.contains(theHref, "/access/content/"))
				  {
					  attachments.add(theHref);
				  }
			  }
			  if (attachments.size() > 0)
			  {
				  addToReport("\nFound " + attachments.size() + " attachments buried in question or answer text.\n\n");
				
				  for (String attachment: attachments)
				  {
					  replacedAttachment=replaceAttachment(attachment, contextToReplace);
					  if ((!replacedAttachment.equals(attachment))&&(contextToReplace!=null))
					  {
						  text = StringUtils.replace(text, attachment, replacedAttachment);
					  }
				  }
			  }
		  }
		  return text;
	  }

	  private String replaceAttachment(String attachment, String contextToReplace)
	  {
		  ContentResource cr = null;
		  
		  String resourceIdOrig = "/" + StringUtils.substringAfter(attachment, "/access/content/");
		  String resourceId = URLDecoder.decode(resourceIdOrig);
		  String filename = StringUtils.substringAfterLast(attachment, "/");
		  
		  try
		  {
			  cr = AssessmentService.getContentHostingService().getResource(resourceId);
		  }
		  catch (IdUnusedException e)
		  {
			  addToReport("\nCould not find attachment (" + resourceId + ").\n\n");
		  }
		  catch (TypeException e)
		  {
			  addToReport("\nTypeException for resource (" + resourceId + ") that was embedded in a question or answer.\n\n");
		  }
		  catch (PermissionException e)
		  {
			  addToReport("\nNo permission for attachment (" + resourceId + ").\n\n");

			  //If resource exists but user has not access to it, make a copy of the resource in the new accessible context.
			  if ((contextToReplace!=null) && StringUtils.isNotEmpty(filename))
			  {
				  //Overriding current user's permissions to make a copy of the resource.
				  SecurityService.pushAdvisor(new SecurityAdvisor(){
					  @Override
					  public SecurityAdvice isAllowed(String arg0, String arg1,
							  String arg2) {
						  if("content.read".equals(arg1)){
							  return SecurityAdvice.ALLOWED;
						  }else{
							  return SecurityAdvice.PASS;
						  }
					  }
				  });

				  try
				  {
					  cr = AssessmentService.getContentHostingService().getResource(resourceId);

					  ContentResource crCopy = new AssessmentService().createCopyOfContentResource(cr.getId(), filename, contextToReplace);
					  //getUrl respects non-ascii chars, getReference does not.
					  attachment = StringUtils.replace(attachment, resourceIdOrig, StringUtils.substringAfter(crCopy.getUrl(), "/content"));
					  addToReport("\nCopied unusable attachment to new context resources folder: "+attachment+" .\n\n");
				  }
				  catch(Exception e2)
				  {
					  addToReport("\nCould NOT copy old attachment "+attachment+" to new attachment in site "+contextToReplace+" .\n\n");
					  log.error(e2.getMessage(), e2);
				  }
				  finally
				  {
					  SecurityService.popAdvisor();
				  }
			  }

		  }
		  return attachment;
	  }
  }
  
  public String getUserPoolAttachmentReport(String userId, Long poolId, String contextToReplace)
  {
	  String parsedText = null;
	  UserPoolAttachmentReport upar = new UserPoolAttachmentReport();

	  boolean flagQuestionPoolUpdated=false;
	  boolean flagTextUpdated = false;
	  boolean flagAttachmentUpdated = false;
	  boolean flagAnswerUpdated = false;

	  ItemService itemService = new ItemService();

	  //Get user's pool with questions.
	  //QuestionPoolFacade qpf = this.getPool(poolId, userId);
	  QuestionPoolDataIfc qpf = this.getPool(poolId, userId);
	  
	  if (qpf==null)
	  {
		  upar.addToReport("POOL ID: "+poolId+" NOT FOUND IN USER "+userId+".");
		  return upar.toString();
	  }
	  upar.addToReport("POOL ---> "+qpf.getTitle()+" - POOL ID: "+qpf.getQuestionPoolId()+"\n\n");

	  Iterator iter = this.getAllItems(poolId).iterator();

	  while (iter.hasNext())
	  {
		  ItemFacade itemData = (ItemFacade) iter.next();

		  //Parsing the question text looking for embedded attachments.
		  Set misItemText = itemData.getItemTextSet();

		  HashSet newItemTextSet = new HashSet(); //Modified question text.
		  HashSet newAnswerSet = new HashSet(); //Modified answers text.

		  Iterator itemTextIter = misItemText.iterator();
		  while (itemTextIter.hasNext())
		  {
			  //Looking for bad attachments in question text.
			  ItemText iti = (ItemText) itemTextIter.next();
			  upar.addToReport("Question Text ---> "+iti.getText()+"\n");

			  parsedText = upar.findAttachmentsInText(iti.getText(), contextToReplace);
			  if (!parsedText.equals(iti.getText()))
			  {
				  flagTextUpdated=true;
				  iti.setText(parsedText);
			  }

			  //Looking for bad attachments in question text.
			  Set myAnswerSet = iti.getAnswerSet();
			  Iterator answerIter = myAnswerSet.iterator();
			  while (answerIter.hasNext())
			  {
				  Answer myAnswer = (Answer) answerIter.next();
				  parsedText = upar.findAttachmentsInText(myAnswer.getText(), contextToReplace);
				  if (!parsedText.equals(myAnswer.getText()))
				  {
					  flagAnswerUpdated=flagTextUpdated=true;
					  myAnswer.setText(parsedText);
				  }
				  newAnswerSet.add(myAnswer);
			  }

			  if (flagAnswerUpdated) iti.setAnswerSet(newAnswerSet);
			  flagAnswerUpdated=false;

			  newItemTextSet.add(iti);
		  }
		  if (flagTextUpdated) itemData.setItemTextSet(newItemTextSet);

		  //Looking for bad attachments in question's attachments (out of CKEditor).
		  StringBuilder uploadedAttachments = new StringBuilder();
		  ArrayList misAttachments = (ArrayList<ItemAttachmentIfc>) itemData.getItemAttachmentList();
		  if (misAttachments.size()>0) upar.addToReport("\nFound " + misAttachments.size() + " uploaded attachments in the question.\n\n");
		  for (int i=0;i<misAttachments.size();i++)
		  {
			  ItemAttachmentIfc ia = (ItemAttachmentIfc) misAttachments.get(i);
			  //uploadedAttachments.append("src=\""+ia.getLocation()+"\" ");
			  parsedText = upar.replaceAttachment(ia.getLocation(), contextToReplace);
			  if (!parsedText.equals(ia.getLocation()))
			  {
				  flagAttachmentUpdated=true;
				  ia.setLocation(parsedText);
				  misAttachments.set(i, ia);
			  }
		  }
		  if (flagAttachmentUpdated) itemData.setItemAttachmentSet(new HashSet(misAttachments));

		  if (flagTextUpdated || flagAttachmentUpdated)
		  {
			  flagQuestionPoolUpdated=true;
			  itemService.saveItem(itemData);
		  }

		  flagTextUpdated=false;
		  flagAttachmentUpdated=false;
	  }
	  if (flagQuestionPoolUpdated) this.savePool(qpf);
	  return upar.getReport();
  }
}
