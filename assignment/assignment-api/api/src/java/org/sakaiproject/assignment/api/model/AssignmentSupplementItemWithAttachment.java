/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.assignment.api.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.cover.EntityManager;


/**
 * The base class for SupplementItem which has attachment(s)
 * @author zqian
 *
 */
public class AssignmentSupplementItemWithAttachment {

	/************* constructors ***********************/
	public AssignmentSupplementItemWithAttachment() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	/*************** attributes and methods *************/
	/** id in db **/
	private Long id;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	/** attachments **/
	private Set<AssignmentSupplementItemAttachment> attachmentSet;	// the attachment set
	public Set<AssignmentSupplementItemAttachment> getAttachmentSet() {
		return attachmentSet;
	}
	public void setAttachmentSet(
			Set<AssignmentSupplementItemAttachment> attachmentSet) {
		this.attachmentSet = attachmentSet;
	}
	
	/**
	 * return the set of Reference objects for attachments
	 * @return
	 */
	public Set<Reference> getAttachmentObjectSet()
	{
		Set<Reference> rv = new HashSet<Reference>();
		Set<AssignmentSupplementItemAttachment> attachmentSet = getAttachmentSet();
		for (Iterator<AssignmentSupplementItemAttachment> iAttachmentSet = attachmentSet.iterator(); iAttachmentSet.hasNext();)
		{
			AssignmentSupplementItemAttachment attachment = iAttachmentSet.next();
			rv.add(EntityManager.newReference(attachment.getAttachmentId()));
		}
		return rv;
	}
}
