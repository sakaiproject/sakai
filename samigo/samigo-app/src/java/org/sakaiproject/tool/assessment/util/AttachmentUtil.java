package org.sakaiproject.tool.assessment.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.FilePickerHelper;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.ItemGradingAttachmentIfc;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.cover.SessionManager;

public class AttachmentUtil {

	private static Log log = LogFactory.getLog(AttachmentUtil.class);

	public AttachmentUtil() {}

	private HashMap getResourceIdHash(Set attachmentSet){
		HashMap map = new HashMap();
		if (attachmentSet !=null ){
			Iterator iter = attachmentSet.iterator();
			while (iter.hasNext()){
				ItemGradingAttachmentIfc attach = (ItemGradingAttachmentIfc) iter.next();
				map.put(attach.getResourceId(), attach);
			}
		}
		return map;
	}

	public List prepareAssessmentAttachment(ItemGradingData itemGradingData, Set itemGradingAttachmentSet){
		ToolSession session = SessionManager.getCurrentToolSession();
		if (session.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS) != null) {
			GradingService gradingService = new GradingService();

			HashMap map = getResourceIdHash(itemGradingAttachmentSet);
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
						ItemGradingAttachmentIfc newAttach = gradingService.createItemGradingAttachment(
								itemGradingData,
								ref.getId(), ref.getProperties().getProperty(
										ref.getProperties().getNamePropDisplayName()),
										protocol);
						newAttachmentList.add(newAttach);
					}
					else{ 
						// attachment already exist, let's add it to new list and check it off from map
						newAttachmentList.add((ItemGradingAttachmentIfc)map.get(resourceId));
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


	public List prepareReferenceList(List attachmentList){
		List list = new ArrayList();
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
				gradingService.removeItemGradingAttachment(attach.getAttachmentId().toString());
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
