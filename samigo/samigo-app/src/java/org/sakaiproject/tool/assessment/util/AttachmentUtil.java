/**
 * Copyright (c) 2005-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.tool.assessment.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.FilePickerHelper;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemAttachment;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingAttachment;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.GradingAttachmentData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingAttachment;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AttachmentIfc;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.cover.SessionManager;

@Slf4j
public class AttachmentUtil {

	public AttachmentUtil() {}

	private HashMap getResourceIdHash(Set attachmentSet){
		HashMap map = new HashMap();
		if (attachmentSet !=null ){
			Iterator iter = attachmentSet.iterator();
			while (iter.hasNext()){
				GradingAttachmentData attach = (GradingAttachmentData) iter.next();
				map.put(attach.getResourceId(), attach);
			}
		}
		return map;
	}

	public List prepareAssessmentAttachment(Object gradingData, Set gradingAttachmentSet){
		ToolSession session = SessionManager.getCurrentToolSession();
		if (session.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS) != null) {
			GradingService gradingService = new GradingService();

			HashMap map = getResourceIdHash(gradingAttachmentSet);
			ArrayList newAttachmentList = new ArrayList();
			String protocol = ContextUtil.getProtocol();

			List refs = (List)session.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
			if (refs != null && refs.size() > 0){
				Reference ref;
				for(int i = 0; i < refs.size(); i++) {
					ref = (Reference) refs.get(i);
					String resourceId = ref.getId();
					if (map.get(resourceId) == null){
						// new attachment, add 
						log.debug("**** ref.Id="+ref.getId());
						log.debug("**** ref.name="+ref.getProperties().getProperty(ref.getProperties().getNamePropDisplayName()));
						if (gradingData instanceof ItemGradingData) {
							ItemGradingAttachment newAttach = gradingService.createItemGradingAttachment(
									(ItemGradingData)gradingData,
									ref.getId(), ref.getProperties().getProperty(
											ref.getProperties().getNamePropDisplayName()),
											protocol);
							newAttachmentList.add(newAttach);
						}
						else if (gradingData instanceof AssessmentGradingData) {
							AssessmentGradingAttachment newAttach = gradingService.createAssessmentGradingAttachment(
									(AssessmentGradingData)gradingData,
									ref.getId(), ref.getProperties().getProperty(
											ref.getProperties().getNamePropDisplayName()),
											protocol);
							newAttachmentList.add(newAttach);
						}
					}
					else{ 
						// attachment already exist, let's add it to new list and check it off from map
						newAttachmentList.add((GradingAttachmentData)map.get(resourceId));
						map.remove(resourceId);
					}
				}
			}
			session.removeAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
			session.removeAttribute(FilePickerHelper.FILE_PICKER_CANCEL);
			return newAttachmentList;
		}
		return new ArrayList();
	}

	public List<Reference> prepareReferenceList(List attachmentList){
		List<Reference> list = new ArrayList<>();
		for (int i=0; i<attachmentList.size(); i++){
			ContentResource cr = null;
			AttachmentIfc attach = (AttachmentIfc) attachmentList.get(i);
			try{
				cr = ContentHostingService.getResource(attach.getResourceId());
			}
			catch (PermissionException e) {
				log.warn("PermissionException from ContentHostingService:"+e.getMessage());
			}
			catch (IdUnusedException e) {
				log.warn("IdUnusedException from ContentHostingService:"+e.getMessage());
				// <-- bad sign, some left over association of assessment and resource, 
				// use case: user remove resource in file picker, then exit modification without
				// proper cancellation by clicking at the left nav instead of "cancel".
				// Also in this use case, any added resource would be left orphan. 
				GradingService gradingService = new GradingService();
				if (attach instanceof ItemGradingAttachment) {
					gradingService.removeItemGradingAttachment(attach.getAttachmentId().toString());
				}
				if (attach instanceof AssessmentGradingAttachment) {
					gradingService.removeAssessmentGradingAttachment(attach.getAttachmentId().toString());
				}
			}
			catch (TypeException e) {
				log.warn("TypeException from ContentHostingService:"+e.getMessage());
			}
			if (cr!=null){
				Reference ref = EntityManager.newReference(cr.getReference());
				if (ref !=null ) list.add(ref);
			}
		}
		return list;
	}
}
