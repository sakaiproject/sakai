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

package org.sakaiproject.tool.assessment.ui.listener.author;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.services.PublishedItemService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AnswerBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemAuthorBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * @version $Id$
 */
@Slf4j
public class ResetItemAttachmentListener
    implements ActionListener
{

  public ResetItemAttachmentListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException {
	AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
	boolean isEditPendingAssessmentFlow =  author.getIsEditPendingAssessmentFlow();
	ItemService itemService = null;
	AssessmentService assessmentService = null;
	if (isEditPendingAssessmentFlow) {
		itemService = new ItemService();
		assessmentService = new AssessmentService();
	}
	else {
		itemService = new PublishedItemService();
		assessmentService = new PublishedAssessmentService();
	}

    ItemAuthorBean itemauthorBean = (ItemAuthorBean) ContextUtil.lookupBean("itemauthor");
    String itemId = itemauthorBean.getItemId();

//    Object aBean = itemauthorBean.getCurrentAnswer();
    AnswerBean answerBean = itemauthorBean.getCurrentAnswer();

    if (answerBean == null) {
	    if (itemId !=null && !("").equals(itemId)){
	      ItemFacade item = itemService.getItem(itemId);
	      log.debug("*** item attachment="+item.getItemAttachmentList());
	      resetItemAttachment(itemauthorBean.getResourceHash(), item.getItemAttachmentList(), assessmentService, item, itemService);
	    } // else never saved, so no attachments to clean up
	}
	else {
	    Long sequence = answerBean.getSequence();
	    if (itemId !=null && !("").equals(itemId)){
            ItemFacade item = itemService.getItem(itemId);
	      ItemTextIfc itemText = item.getItemTextBySequence(sequence);
	      resetItemTextAttachment(answerBean.getResourceHash(), itemText.getItemTextAttachmentSet(), assessmentService, itemText, item, itemService);
	    }
		
	}
  }

    private void resetItemAttachment(Map resourceHash, List attachmentList, AssessmentService service, ItemFacade item, ItemService itemService){
    // 1. we need to make sure that attachment removed/added by file picker 
    //    will be restored/remove when user cancels the entire modification
    if (attachmentList != null){
      boolean itemEdited = false;
      for (int i=0; i<attachmentList.size(); i++){
         AttachmentIfc attach = (AttachmentIfc) attachmentList.get(i);
         try{
           ContentResource cr = AssessmentService.getContentHostingService().getResource(attach.getResourceId());
	 }
         catch (PermissionException e) {
           log.warn("PermissionException from ContentHostingService:"+e.getMessage());
         }
         catch (IdUnusedException e) {
           log.warn("IdUnusedException from ContentHostingService:"+e.getMessage());
           // <-- bad sign, 
           // use case: ContentHosting deleted the resource
           // and user cancel out all the modification
           // including those that CHS has removed
           // according to Glenn , it is a bug in CHS.
           // so we would just do clean up to avoid having attachments
           // points to empty resources
           log.warn("***2.removing an empty item attachment association, attachmentId="+attach.getAttachmentId());
           item.removeItemAttachmentById(attach.getAttachmentId());
           itemEdited = true;

           /* forget it #1
           if (resourceHash!=null){
             ContentResource old_cr = (ContentResource) resourceHash.get(attach.getResourceId());
             if (old_cr!=null){ 
               resourceHash.remove(attach.getResourceId());
	     }
	   }
           */
         }
         catch (TypeException e) {
    	   log.warn("TypeException from ContentHostingService:"+e.getMessage());
	 }
      }
      if ( itemEdited ) {
          itemService.saveItem(item);
      }
    }

    /* forget it #2
       the fact that resources belongs to other item get deleted is too great if there is a
        mistake in the code or sequence of users action that I haven't foreseen. So I am commenting 
        this out. These resources shall remain in the DB as orphan. Let's leave the clean up for CHS.
        Afterall, it shan't commit before the tool tell it to.
    // 2. any leftover in resourceHash are files that are uploaded
    //    but has no association with the item and we should remove
    //    it. use case: add attachment and cancel the entire modification
    removeLeftOverResources(resourceHash);
    // VERY IMPORTANT to clean up itemauthorBean.resourceHash
    itemauthorBean.setResourceHash(null); 
    */
  }

  /* forget it #3
  private void removeLeftOverResources(HashMap resourceHash){
    if (resourceHash == null) return;
    Set keys = resourceHash.keySet();
    Iterator iter1 = keys.iterator();
    while (iter1.hasNext()){
      String resourceId = (String)iter1.next();
      try{
       log.debug("***removing left over resourceId="+resourceId);
        ContentHostingService.removeResource(resourceId);
      }
      catch (PermissionException e) {
        log.warn("PermissionException from ContentHostingService:"+e.getMessage());
      }
      catch (IdUnusedException e) {
        log.warn("IdUnusedException from ContentHostingService:"+e.getMessage());
      }
      catch (TypeException e) {
        log.warn("TypeException from ContentHostingService:"+e.getMessage());
      }
      catch (InUseException e) {
        log.warn("InUseException from ContentHostingService:"+e.getMessage());
      }
    }
  }
  */
    
    private void resetItemTextAttachment(Map resourceHash, Set<ItemTextAttachmentIfc> attachmentSet, AssessmentService service, ItemTextIfc itemText, ItemFacade item, ItemService itemService){
        // 1. we need to make sure that attachment removed/added by file picker
        //    will be restored/remove when user cancels the entire modification
        if (attachmentSet != null){
          for (Iterator<ItemTextAttachmentIfc> it = attachmentSet.iterator(); it.hasNext();) {
             AttachmentIfc attach = (AttachmentIfc) it.next();
             try{
               ContentResource cr = AssessmentService.getContentHostingService().getResource(attach.getResourceId());
             }
             catch (PermissionException e) {
               log.warn("PermissionException from ContentHostingService:"+e.getMessage());
             }
             catch (IdUnusedException e) {
               log.warn("IdUnusedException from ContentHostingService:"+e.getMessage());
               // <-- bad sign, 
               // use case: ContentHosting deleted the resource
               // and user cancel out all the modification
               // including those that CHS has removed
               // according to Glenn , it is a bug in CHS.
               // so we would just do clean up to avoid having attachments
               // points to empty resources
               log.warn("***2.removing an empty item attachment association, attachmentId="+attach.getAttachmentId());
                 itemText.removeItemTextAttachmentById(attach.getAttachmentId());
               /* forget it #1
               if (resourceHash!=null){
                 ContentResource old_cr = (ContentResource) resourceHash.get(attach.getResourceId());
                 if (old_cr!=null){ 
                   resourceHash.remove(attach.getResourceId());
    	     }
    	   }
               */
             }
             catch (TypeException e) {
        	   log.warn("TypeException from ContentHostingService:"+e.getMessage());
    	 }
          }
        }
    }
 }
