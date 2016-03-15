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


package org.sakaiproject.tool.assessment.services;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.tool.assessment.data.dao.assessment.Answer;
import org.sakaiproject.tool.assessment.data.dao.assessment.AnswerFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.FavoriteColChoices;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemAttachment;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemMetaData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemText;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemTextAttachment;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextAttachmentIfc;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;

/**
 * The ItemService calls persistent service locator to reach the
 * manager on the back end.
 */
public class ItemService
{
  private Log log = LogFactory.getLog(ItemService.class);

  /**
   * Creates a new ItemService object.
   */
  public ItemService()
  {
  }


  /**
   * Get a particular item from the backend, with all questions.
   * @param itemId
   * @param agentId
   * @return
   * @deprecated 
   */
  public ItemFacade getItem(Long itemId, String agentId)
  {
    ItemFacade item = null;
    try
    {
      item =
        PersistenceService.getInstance().getItemFacadeQueries().
          getItem(itemId, agentId);
    }
    catch(Exception e)
    {
      log.error(e); throw new RuntimeException(e);
    }

    return item;
  }

  /**
   * Delete a item
   */
  public void deleteItem(Long itemId, String agentId)
  {
    try
    {
      //ItemFacade item= PersistenceService.getInstance().
        //getItemFacadeQueries().getItem(itemId, agentId);

/*  do not check for owner, anyone who has maintain role can modify items see SAK-2214
      // you are not allowed to delete item if you are not the owner
      if (!item.getData().getCreatedBy().equals(agentId))
        throw new RuntimeException("you are not allowed to delete item if you are not the owner");
*/
      PersistenceService.getInstance().getItemFacadeQueries().
        deleteItem(itemId, agentId);
    }
    catch(Exception e)
    {
      log.error(e); throw new RuntimeException(e);
    }
  }


  /**
   * Delete itemtextset for an item, used for modify
   */
  public void deleteItemContent(Long itemId, String agentId)
  {
    try
    {
      //ItemFacade item= PersistenceService.getInstance().
        //getItemFacadeQueries().getItem(itemId, agentId);

/*  do not check for owner, anyone who has maintain role can modify items see SAK-2214
      // you are not allowed to delete item if you are not the owner
      if (!item.getData().getCreatedBy().equals(agentId))
        throw new RuntimeException("you are not allowed to delete item if you are not the owner");
*/
      PersistenceService.getInstance().getItemFacadeQueries().
        deleteItemContent(itemId, agentId);
    }
    catch(Exception e)
    {
      log.error(e); throw new RuntimeException(e);
    }
  }


  /**
   * Delete metadata for an item, used for modify
   * param:  itemid, label, agentId
   */
  public void deleteItemMetaData(Long itemId, String label, String agentId)
  {
    try
    {
      //ItemFacade item= PersistenceService.getInstance().
        //getItemFacadeQueries().getItem(itemId, agentId);

/*  do not check for owner, anyone who has maintain role can modify items see SAK-2214
      // you are not allowed to delete item if you are not the owner
      if (!item.getData().getCreatedBy().equals(agentId))
        throw new Error(new Exception());
*/
      PersistenceService.getInstance().getItemFacadeQueries().
        deleteItemMetaData(itemId, label);
    }
    catch(Exception e)
    {
      log.error(e); throw new RuntimeException(e);
    }
  }

   /**
   * Add metadata for an item, used for modify
   * param:  itemid, label, value, agentId
   */
  public void addItemMetaData(Long itemId, String label, String value, String agentId)
  {
    try
    {
      //ItemFacade item= PersistenceService.getInstance().
        //getItemFacadeQueries().getItem(itemId, agentId);

/*  do not check for owner, anyone who has maintain role can modify items see SAK-2214
      // you are not allowed to delete item if you are not the owner
      if (!item.getData().getCreatedBy().equals(agentId))
        throw new Error(new Exception());
*/
      PersistenceService.getInstance().getItemFacadeQueries().
        addItemMetaData(itemId, label, value);
    }
    catch(Exception e)
    {
      log.error(e); throw new RuntimeException(e);
    }
  }




  /**
   * Save a question item.
   */
  public ItemFacade saveItem(ItemFacade item)
  {
    try
    {
      return PersistenceService.getInstance().getItemFacadeQueries().saveItem(item);
    }
    catch(Exception e)
    {
      log.error(e);

      return item;
    }
  }

  /**
   *  Get a particular item from the backend, with all questions.
   * @param itemId
   * @return
   */
  public ItemFacade getItem(String itemId) {
    try{
      return PersistenceService.getInstance().getItemFacadeQueries().
          getItem(new Long(itemId));
    }
    catch(Exception e)
    {
      log.error(e); throw new RuntimeException(e);
    }
  }

  public HashMap getItemsByKeyword(String keyword)
  {
    keyword="%" + keyword + "%";
    HashMap map= null;
      map= PersistenceService.getInstance().getItemFacadeQueries().getItemsByKeyword(keyword);
    return map;

  }

  public Long getItemTextId(Long publishedItemId) {
	    try{
	      return PersistenceService.getInstance().getItemFacadeQueries().
	      getItemTextId(publishedItemId);
	    }
	    catch(Exception e)
	    {
	      log.error(e);
	      return -1l;
	    }
  }

  public ItemData cloneItem(ItemDataIfc item){
    ItemData cloned= new ItemData(
        item.getSection(),item.getSequence(), item.getDuration(), item.getInstruction(),
        item.getDescription(),item.getTypeId(),item.getGrade(),item.getScore(), item.getScoreDisplayFlag(), item.getDiscount(), item.getMinScore(),
        item.getHint(),item.getHasRationale(),item.getStatus(),item.getCreatedBy(),
        item.getCreatedDate(),item.getLastModifiedBy(),item.getLastModifiedDate(),
        null, null, null, item.getTriesAllowed(), item.getPartialCreditFlag());

    // perform deep copy, set ItemTextSet, itemMetaDataSet and itemFeedbackSet
    Set newItemTextSet = copyItemTextSet(cloned, item.getItemTextSet());
    Set newItemMetaDataSet = copyItemMetaDataSet(cloned, item.getItemMetaDataSet());
    Set newItemFeedbackSet = copyItemFeedbackSet(cloned, item.getItemFeedbackSet());
    Set newItemAttachmentSet = copyItemAttachmentSet(cloned, item.getItemAttachmentSet());
    cloned.setItemTextSet(newItemTextSet);
    cloned.setItemMetaDataSet(newItemMetaDataSet);
    cloned.setItemFeedbackSet(newItemFeedbackSet);
    cloned.setItemAttachmentSet(newItemAttachmentSet);
    cloned.setAnswerOptionsSimpleOrRich(item.getAnswerOptionsSimpleOrRich());
    cloned.setAnswerOptionsRichCount(item.getAnswerOptionsRichCount());

    return cloned;
  }

  private Set copyItemTextSet(ItemData cloned, Set itemTextSet) {
    HashSet h = new HashSet();
    Iterator k = itemTextSet.iterator();
    while (k.hasNext()) {
      ItemText itemText = (ItemText) k.next();
      ItemText newItemText = new ItemText(cloned, itemText.getSequence(), itemText.getText(), null);
      newItemText.setRequiredOptionsCount(itemText.getRequiredOptionsCount());
      newItemText.setItemTextAttachmentSet(copyItemAttachmentSetItemText(newItemText, itemText.getItemTextAttachmentSet()));
      Set newAnswerSet = copyAnswerSet(newItemText, itemText.getAnswerSet());
      newItemText.setAnswerSet(newAnswerSet);
      h.add(newItemText);
    }
    return h;
  }

  private Set copyAnswerSet(ItemText newItemText, Set answerSet) {
    HashSet h = new HashSet();
    Iterator l = answerSet.iterator();
    while (l.hasNext()) {
      Answer answer = (Answer) l.next();
      Answer newAnswer = new Answer(
          newItemText, answer.getText(), answer.getSequence(),
          answer.getLabel(),
      	  answer.getIsCorrect(), answer.getGrade(), answer.getScore(), answer.getPartialCredit(), answer.getDiscount(), 
      	  //answer.getCorrectOptionLabels(), 
      	  null);
      Set newAnswerFeedbackSet = copyAnswerFeedbackSet(
          newAnswer, answer.getAnswerFeedbackSet());
      newAnswer.setAnswerFeedbackSet(newAnswerFeedbackSet);
      h.add(newAnswer);
    }
    return h;
  }

  private Set copyAnswerFeedbackSet(Answer newAnswer, Set answerFeedbackSet) {
    HashSet h = new HashSet();
    Iterator m = answerFeedbackSet.iterator();
    while (m.hasNext()) {
      AnswerFeedback answerFeedback = (AnswerFeedback) m.next();
      AnswerFeedback newAnswerFeedback = new AnswerFeedback(
          newAnswer, answerFeedback.getTypeId(), answerFeedback.getText());
      h.add(newAnswerFeedback);
    }
    return h;
  }

  private Set copyItemMetaDataSet(ItemData cloned, Set itemMetaDataSet) {
    HashSet h = new HashSet();
    Iterator n = itemMetaDataSet.iterator();
    while (n.hasNext()) {
      ItemMetaData itemMetaData = (ItemMetaData) n.next();
      ItemMetaData newItemMetaData = new ItemMetaData(
          cloned, itemMetaData.getLabel(), itemMetaData.getEntry());
      h.add(newItemMetaData);
    }
    return h;
  }

  private Set copyItemFeedbackSet(ItemData cloned, Set itemFeedbackSet) {
    HashSet h = new HashSet();
    Iterator o = itemFeedbackSet.iterator();
    while (o.hasNext()) {
      ItemFeedback itemFeedback = (ItemFeedback) o.next();
      ItemFeedback newItemFeedback = new ItemFeedback(
          cloned, itemFeedback.getTypeId(), itemFeedback.getText());
      h.add(newItemFeedback);
    }
    return h;
  }

  private Set copyItemAttachmentSet(ItemData cloned, Set itemAttachmentSet) {
    AssessmentService service = new AssessmentService();
    HashSet h = new HashSet();
    Iterator n = itemAttachmentSet.iterator();
    while (n.hasNext()) {
      ItemAttachmentIfc itemAttachment = (ItemAttachmentIfc) n.next();
      ContentResource cr_copy = service.createCopyOfContentResource(
                           itemAttachment.getResourceId(), itemAttachment.getFilename());
      ItemAttachmentIfc newItemAttachment = new ItemAttachment(
        null, cr_copy.getId(), itemAttachment.getFilename(),
        itemAttachment.getMimeType(), itemAttachment.getFileSize(), itemAttachment.getDescription(),
        cr_copy.getUrl(), itemAttachment.getIsLink(), itemAttachment.getStatus(),
        itemAttachment.getCreatedBy(), itemAttachment.getCreatedDate(), itemAttachment.getLastModifiedBy(),
        itemAttachment.getLastModifiedDate());
      newItemAttachment.setItem(cloned);
      h.add(newItemAttachment);
    }
    return h;
  }
  
  private Set copyItemAttachmentSetItemText(ItemText itemText, Set itemAttachmentSet) {
	AssessmentService service = new AssessmentService();
	HashSet h = new HashSet();
	Iterator n = itemAttachmentSet.iterator();
	while (n.hasNext()) {
	  ItemTextAttachmentIfc ItemTextAttachment = (ItemTextAttachmentIfc) n.next();
	  ContentResource cr_copy = service.createCopyOfContentResource(
	    		  			ItemTextAttachment.getResourceId(), ItemTextAttachment.getFilename());
	  ItemTextAttachmentIfc newItemTextAttachment = new ItemTextAttachment(
	    null, cr_copy.getId(), ItemTextAttachment.getFilename(),
	    ItemTextAttachment.getMimeType(), ItemTextAttachment.getFileSize(), ItemTextAttachment.getDescription(),
	    cr_copy.getUrl(), ItemTextAttachment.getIsLink(), ItemTextAttachment.getStatus(),
	    ItemTextAttachment.getCreatedBy(), ItemTextAttachment.getCreatedDate(), ItemTextAttachment.getLastModifiedBy(),
	    ItemTextAttachment.getLastModifiedDate());
	   newItemTextAttachment.setItemText(itemText);
	   h.add(newItemTextAttachment);
	}
	return h;
  }

  public void deleteSet(Set s)
  {
    try
    {
      PersistenceService.getInstance().getItemFacadeQueries().
      deleteSet(s);
    }
    catch(Exception e)
    {
      log.error(e); throw new RuntimeException(e);
    }
  }
  
  /**
   * Save favorite column choices for matrix survey question.
   */
  public void saveFavoriteColumnChoices(FavoriteColChoices choices)
  { 
	  try
	  {
		  PersistenceService.getInstance().getFavoriteColChoicesFacadeQueries().saveOrUpdate(choices);
	  }
	  catch(Exception e)
	  {
		  log.error(e); throw new RuntimeException(e);
	  }
  }
}
