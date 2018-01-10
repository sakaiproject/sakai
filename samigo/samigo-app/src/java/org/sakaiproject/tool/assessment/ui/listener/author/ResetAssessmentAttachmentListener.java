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
import java.util.List;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AttachmentIfc;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.PublishedAssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * @version $Id$
 */
@Slf4j
public class ResetAssessmentAttachmentListener
    implements ActionListener
{

  public ResetAssessmentAttachmentListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException {
	  AssessmentService assessmentService = new AssessmentService();
	  AssessmentSettingsBean assessmentSettingsBean = (AssessmentSettingsBean) ContextUtil.lookupBean("assessmentSettings");
	  String assessmentId = assessmentSettingsBean.getAssessmentId().toString();

	  log.debug("***assessmentId="+assessmentId);
	  if (assessmentId !=null && !("").equals(assessmentId)){
		  AssessmentIfc assessment = (AssessmentIfc) assessmentService.getAssessment(assessmentId);
		  resetAssessmentAttachment(assessment.getAssessmentAttachmentList(), assessmentService);
	  }
	  else{
		  resetAssessmentAttachment(new ArrayList(), assessmentService);
	  }
  }

  public void resetAssessmentAttachment(List attachmentList, AssessmentService assessmentService){

    // 1. we need to make sure that attachment removed/added by file picker 
    //    will be restored/remove when user cancels the entire modification
    if (attachmentList != null){
      for (int i=0; i<attachmentList.size(); i++){
         AttachmentIfc attach = (AttachmentIfc) attachmentList.get(i);
         try{
           ContentResource cr = AssessmentService.getContentHostingService().getResource(attach.getResourceId());
	 }
         catch (PermissionException e) {
           log.warn("PermissionException from ContentHostingService:"+e.getMessage());
         }
         catch (IdUnusedException e) {
           // <-- bad sign, 
           // use case: ContentHosting deleted the resource
           // and user cancel out all the modification
           // including those that CHS has removed
           // according to Glenn , it is a bug in CHS.
           // so we would just do clean up to avoid having attachments
           // points to empty resources
           log.warn("IdUnusedException from ContentHostingService:"+e.getMessage());
           log.warn("***removing an empty assessment attachment association, attachmentId="+attach.getAttachmentId());
           assessmentService.removeAssessmentAttachment(attach.getAttachmentId().toString());

           /* forget it #1
           if (resourceHash != null){
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

    /* forget it #2
       the fact that resources belongs to other assessment get deleted is too great if there is a
       mistake in the code or sequence of users action that I haven't foreseen. So I am commenting 
       this out. These resources shall remain in the DB as orphan. Let's leave the clean up for CHS.
       Afterall, it shan't commit before the tool tell it to.
    // 2. any leftover in resourceHash are files that are uploaded
    //    but has no association with the assessment and we should remove
    //    it. use case: add attachment and cancel the entire modification
    removeLeftOverResources(resourceHash);
    // VERY IMPORTANT to clean up bean.resourceHash
    bean.setResourceHash(null); 
    */
  }

  /* forget it #3
  private void removeLeftOverResources(HashMap resourceHash){
    AssessmentService assessmentService = new AssessmentService();
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

 }

