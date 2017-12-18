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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.SectionBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * @version $Id$
 */
@Slf4j
public class ResetPartAttachmentListener
    implements ActionListener
{

  public ResetPartAttachmentListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException {
    SectionBean sectionBean = (SectionBean) ContextUtil.lookupBean("sectionBean");
    AssessmentService assessmentService = null;
    AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
    boolean isEditPendingAssessmentFlow = author.getIsEditPendingAssessmentFlow();
    if (isEditPendingAssessmentFlow) {
    	assessmentService = new AssessmentService();
    }
    else {
    	assessmentService = new PublishedAssessmentService();
    }
    String sectionId = sectionBean.getSectionId();
    if (sectionId !=null && !("").equals(sectionId)){
    	SectionDataIfc section = assessmentService.getSection(sectionId);
    	resetSectionAttachment(assessmentService, sectionBean.getResourceHash(), section.getSectionAttachmentList());
    }
    else{
    	resetSectionAttachment(assessmentService, sectionBean.getResourceHash(), new ArrayList());
    }
  }

  private void resetSectionAttachment(AssessmentService assessmentService, Map resourceHash, List attachmentList){

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
				  log.warn("***removing an empty section attachment association, attachmentId="+attach.getAttachmentId());
				  assessmentService.removeSectionAttachment(attach.getAttachmentId().toString());
			  }
			  catch (TypeException e) {
				  log.warn("TypeException from ContentHostingService:"+e.getMessage());
			  }
		  }
	  }
  }
}

